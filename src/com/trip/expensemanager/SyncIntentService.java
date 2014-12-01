package com.trip.expensemanager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.trip.expensemanager.distributionendpoint.Distributionendpoint;
import com.trip.expensemanager.distributionendpoint.model.Distribution;
import com.trip.expensemanager.expenseendpoint.Expenseendpoint;
import com.trip.expensemanager.expenseendpoint.model.CollectionResponseExpense;
import com.trip.expensemanager.expenseendpoint.model.Expense;
import com.trip.expensemanager.loginendpoint.Loginendpoint;
import com.trip.expensemanager.loginendpoint.model.LogIn;
import com.trip.expensemanager.tosyncendpoint.Tosyncendpoint;
import com.trip.expensemanager.tosyncendpoint.model.CollectionResponseToSync;
import com.trip.expensemanager.tosyncendpoint.model.ToSync;
import com.trip.expensemanager.tripendpoint.Tripendpoint;
import com.trip.expensemanager.tripendpoint.model.Trip;
import com.trip.utils.Constants;
import com.trip.utils.DistributionBean;
import com.trip.utils.DistributionBean1;
import com.trip.utils.ExpenseBean;
import com.trip.utils.Global;
import com.trip.utils.LocalDB;
import com.trip.utils.TripBean;

public class SyncIntentService extends IntentService{

	public static final String RESULT = "com.trip.expensemanager.REQUEST_PROCESSED";
	private long lngUserId;
	private NotificationManager mNotificationManager;
	private LocalBroadcastManager broadcaster;

	public SyncIntentService() {
		super("ExpenseSyncService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		broadcaster = LocalBroadcastManager.getInstance(this);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			if(Global.isConnected(getApplicationContext())){
				syncOthers();
			}
			if(Global.isConnected(getApplicationContext())){
				syncAllTrips();
			}
			if(Global.isConnected(getApplicationContext())){
				syncAllExpenses();
			}
			if(intent.getBooleanExtra(Constants.STR_IS_FROM_GCM, false)){
				GcmBroadcastReceiver.completeWakefulIntent(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendResult() {
		Intent intent = new Intent(RESULT);
		broadcaster.sendBroadcast(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	private void syncOthers() {
		LocalDB localDb=new LocalDB(getApplicationContext());
		lngUserId=localDb.retrieve();
		Tosyncendpoint.Builder builder = new Tosyncendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
		builder = CloudEndpointUtils.updateBuilder(builder);
		Tosyncendpoint endpoint = builder.build();
		long userId=localDb.retrieve();
		long devId=localDb.retrieveDeviceId();
		CollectionResponseToSync entities=null;
		try {
			entities = endpoint.listToSync().setUserId(devId).execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (entities == null || entities.getItems() == null || entities.getItems().size() < 1) {

		} else{
			Tripendpoint.Builder tripBuilder = new Tripendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
			tripBuilder = CloudEndpointUtils.updateBuilder(tripBuilder);
			Tripendpoint tripEndpoint = tripBuilder.build();
			Trip tripTemp=null;
			Expenseendpoint.Builder expenseBuilder = new Expenseendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
			expenseBuilder = CloudEndpointUtils.updateBuilder(expenseBuilder);
			Expenseendpoint expenseEndpoint = expenseBuilder.build();
			Expense expenseTemp=null;
			Loginendpoint.Builder loginBuilder = new Loginendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
			loginBuilder = CloudEndpointUtils.updateBuilder(loginBuilder);
			Loginendpoint loginEndpoint = loginBuilder.build();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

			for(ToSync toSyncTemp:entities.getItems()){
				if(toSyncTemp.getSyncType().equals(Constants.STR_TRIP_UPDATED)){
					try {
						tripTemp=tripEndpoint.getTrip(toSyncTemp.getSyncItemId()).execute();
					} catch (IOException e) {
						e.printStackTrace();
						continue;
					}
					if(tripTemp!=null){
						TripBean tripBean=localDb.retrieveTripDetails(tripTemp.getId());
						long changerId=toSyncTemp.getChangerId();
						String oldTripName=tripBean.getName();
						String newTripName=tripTemp.getName();
						if(!oldTripName.equals(newTripName)){
							changerId=tripTemp.getChangerId();
							String username=localDb.retrieveUsername(changerId);
							LogIn login=null;
							if(username==null){
								try {
									login=loginEndpoint.getLogIn(changerId).execute();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							if(login!=null){
								username=login.getUsername();
								localDb.insertPerson(login.getId(), username, login.getPrefferedName());
							}
							localDb.updateTrip(tripTemp.getId(), tripTemp.getName(), Constants.STR_SYNCHED);
							if(changerId!=userId){
								if(username!=null){
									sendNotification(toSyncTemp.getSyncType(), "EG Changed", "Expense-group name changed from "+oldTripName+" to "+newTripName+" by "+username+"!!", new Intent(this, UpdatesActivity.class), Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, tripTemp.getId());
								} else{
									sendNotification(toSyncTemp.getSyncType(), "EG Changed", "Expense-group name changed from "+oldTripName+" to "+newTripName+"!!", new Intent(this, UpdatesActivity.class), Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, tripTemp.getId());
								}
							}
							sendResult();
						}
					}
				} else if(toSyncTemp.getSyncType().equals(Constants.STR_TRIP_ADDED)){
					LogIn login=null;
					List<Long> listUsersTemp;
					String date;
					try {
						tripTemp=tripEndpoint.getTrip(toSyncTemp.getSyncItemId()).execute();
					} catch (IOException e) {
						e.printStackTrace();
						continue;
					}
					TripBean tripBean=localDb.retrieveTripDetails(tripTemp.getId());
					if(tripBean==null){
						StringBuffer sbUsers=new StringBuffer();
						listUsersTemp=tripTemp.getUserIDs();
						if(listUsersTemp==null){
							listUsersTemp=new ArrayList<Long>();
						}
						date = sdf.format(new Date(tripTemp.getCreationDate().getValue()));
						String strUserTemp;
						for(long userIdTemp:listUsersTemp){
							strUserTemp=localDb.retrieveUsername(userIdTemp);
							if(strUserTemp==null){
								try {
									login=loginEndpoint.getLogIn(userIdTemp).execute();
								} catch (Exception e) {
									e.printStackTrace();
									continue;
								}
								strUserTemp=login.getUsername();
								localDb.insertPerson(userIdTemp, strUserTemp, login.getPrefferedName());

							}
							sbUsers.append(userIdTemp);
							sbUsers.append(',');
						}
						sbUsers.delete(sbUsers.length()-1, sbUsers.length());
						localDb.insertTrip(tripTemp.getName(), tripTemp.getId(), date, sbUsers.toString(), tripTemp.getAdmin(), Constants.STR_SYNCHED);
						sendResult();
					}
				} else if(toSyncTemp.getSyncType().equals(Constants.STR_USER_DELETED)){
					try {
						tripTemp=tripEndpoint.getTrip(toSyncTemp.getSyncItemId()).execute();
					} catch (IOException e) {
						e.printStackTrace();
						continue;
					}
					if(tripTemp!=null){
						TripBean tripBean=localDb.retrieveTripDetails(tripTemp.getId());
						if(tripBean!=null && !tripBean.getSyncStatus().equals(Constants.STR_EXITED)){
							long changerId=toSyncTemp.getChangerId();
							localDb.deleteExpenseOfTripnUser(tripBean.getId(), toSyncTemp.getChangerId());
							List<Long> users=tripTemp.getUserIDs();
							LogIn login=null;
							String username=null;
							StringBuffer sbUsers=new StringBuffer();
							for(Long userIdTemp:users){
								username=localDb.retrieveUsername(userIdTemp);
								if(username==null){
									try {
										login=loginEndpoint.getLogIn(userIdTemp).execute();
									} catch (IOException e) {
										e.printStackTrace();
									}
									if(login!=null){
										username=login.getUsername();
										localDb.insertPerson(login.getId(), username, login.getPrefferedName());
									}
								}
								sbUsers.append(userIdTemp).append(',');
							}
							sbUsers.delete(sbUsers.length()-1, sbUsers.length());
							String strUsers=sbUsers.toString();

							localDb.updateTripUsers(tripTemp.getId(), strUsers);
							Intent intentToCall=new Intent(this, UpdatesActivity.class);
							//							intentToCall.putExtra(Constants.STR_SHOW_TAB, 3);
							//							intentToCall.putExtra(Constants.STR_TRIP_NAME, tripBean.getName());
							//							intentToCall.putExtra(Constants.STR_USER_ID, userId);
							//							intentToCall.putExtra(Constants.STR_TRIP_ID, tripBean.getId());
							//							intentToCall.putExtra(Constants.STR_ADMIN_ID, tripBean.getAdminId());
							String user=localDb.retrievePrefferedName(toSyncTemp.getChangerId());
							if(changerId!=userId){
								if(user!=null){
									sendNotification(toSyncTemp.getSyncType(), "Person Exited", user+" exited from the expense-group "+tripBean.getName()+"!!", intentToCall , Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, tripTemp.getId());
								} else{
									sendNotification(toSyncTemp.getSyncType(), "Person Exited", "Person exited from the expense-group "+tripBean.getName()+"!!", intentToCall , Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, tripTemp.getId());
								}
							}
							sendResult();
						}
					}
				} else if(toSyncTemp.getSyncType().equals(Constants.STR_USER_ADDED)){
					try {
						tripTemp=tripEndpoint.getTrip(toSyncTemp.getSyncItemId()).execute();
					} catch (IOException e) {
						e.printStackTrace();
						continue;
					}
					if(tripTemp!=null){
						TripBean tripBean=localDb.retrieveTripDetails(tripTemp.getId());
						if(tripBean!=null){
							List<Long> users=tripTemp.getUserIDs();
							long changerId=toSyncTemp.getChangerId();
							LogIn login=null;
							String username=null;
							username=localDb.retrieveUsername(changerId);
							if(username==null){
								try {
									login=loginEndpoint.getLogIn(changerId).execute();
								} catch (IOException e) {
									e.printStackTrace();
								}
								if(login!=null){
									username=login.getUsername();
									localDb.insertPerson(login.getId(), username, login.getPrefferedName());
								}
							}
							long[] userIdsOfTrip=tripBean.getUserIds();
							boolean userAlreadyThere=false;

							for(long userIdTemp:userIdsOfTrip){
								if(userIdTemp==changerId){
									userAlreadyThere=true;
									break;
								}
							}

							if(!userAlreadyThere){
								StringBuffer sbUsers=new StringBuffer();
								for(Long userIdTemp:users){
									username=localDb.retrieveUsername(userIdTemp);
									if(username==null){
										try {
											login=loginEndpoint.getLogIn(userIdTemp).execute();
										} catch (IOException e) {
											e.printStackTrace();
										}
										if(login!=null){
											username=login.getUsername();
											localDb.insertPerson(login.getId(), username, login.getPrefferedName());
										}
									}
									sbUsers.append(userIdTemp).append(',');
								}
								sbUsers.delete(sbUsers.length()-1, sbUsers.length());
								String strUsers=sbUsers.toString();

								localDb.updateTripUsers(tripTemp.getId(), strUsers);
								Intent intentToCall=new Intent(this, UpdatesActivity.class);
								if(changerId!=userId){
									sendNotification(toSyncTemp.getSyncType(), "Person Added", "New person added to the expense-group "+tripBean.getName()+"!!", intentToCall , Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, tripTemp.getId());
								}
								sendResult();
							}
						}
					}
				} else if(toSyncTemp.getSyncType().equals(Constants.STR_TRIP_DELETED)){
					LogIn login=null;
					TripBean tripBean=localDb.retrieveTripDetails(toSyncTemp.getSyncItemId());
					long changerId=toSyncTemp.getChangerId();
					if(tripBean!=null && !tripBean.getSyncStatus().equals(Constants.STR_DELETED)){
						String strChanger=localDb.retrieveUsername(changerId);
						if(strChanger==null){
							try {
								login=loginEndpoint.getLogIn(toSyncTemp.getChangerId()).execute();
							} catch (IOException e) {
								e.printStackTrace();
							}
							if(login!=null){
								strChanger=login.getUsername();
								localDb.insertPerson(login.getId(), strChanger, login.getPrefferedName());
							}
						}
						localDb.deleteTrip(tripBean.getId());
						if(tripBean.getAdminId()!=userId){
							if(strChanger!=null){
								sendNotification(toSyncTemp.getSyncType(), "EG Deleted", "Expense-group "+tripBean.getName()+" deleted by "+strChanger+"!!", new Intent(this, UpdatesActivity.class), Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, tripBean.getId());
							} else{
								sendNotification(toSyncTemp.getSyncType(), "EG Deleted", "Expense-group "+tripBean.getName()+" deleted!!", new Intent(this, ExpenseActivity.class), Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, tripBean.getId());
							}
						}
						sendResult();
					}
				} else if(toSyncTemp.getSyncType().equals(Constants.STR_EXPENSE_ADDED)){
					try {
						expenseTemp=expenseEndpoint.getExpense(toSyncTemp.getSyncItemId()).execute();
					} catch (IOException e) {
						e.printStackTrace();
						continue;
					}
					if(expenseTemp!=null){
						TripBean tripBean=localDb.retrieveTripDetails(expenseTemp.getTripId());
						if(tripBean!=null){
							ExpenseBean expenseBean=localDb.retrieveExpense(expenseTemp.getId());
							if(expenseBean==null){
								String date = sdf.format(new Date(expenseTemp.getCreationDate().getValue()));
								List<Long> userIds=expenseTemp.getExpenseUserIds();
								String strUserIds = Global.listToString(userIds);

								List<String> amounts = expenseTemp.getExpenseAmounts();
								String strAmounts = Global.listToString(amounts);
								localDb.insertExpense(expenseTemp.getName(), expenseTemp.getId(), date, "INR", expenseTemp.getAmount(), expenseTemp.getDescription(), expenseTemp.getTripId(), expenseTemp.getUserId(), strUserIds, strAmounts, Constants.STR_SYNCHED);

								DistributionBean1 distBean;
								String strAmount;
								long rowId;
								distBean=localDb.retrieveDistributionByUsers(expenseTemp.getUserId(), lngUserId, expenseTemp.getTripId());
								int pos=userIds.indexOf(lngUserId);
								if(pos!=-1){
									strAmount=amounts.get(pos);
									if(distBean==null){
										rowId=localDb.insertDistribution(lngUserId, expenseTemp.getUserId(), strAmount, expenseTemp.getTripId());
										localDb.updateDistributionId(rowId, rowId);
									} else{
										if(distBean.getToId()!=distBean.getFromId() && distBean.getToId()!=lngUserId){
											strAmount=String.valueOf(Global.add(Float.parseFloat(distBean.getAmount()), strAmount));
										} else{
											strAmount=String.valueOf(Global.subtract(Float.parseFloat(distBean.getAmount()), strAmount));
										}
										localDb.updateDistAmount(distBean.getDistributionId(), strAmount);
									}
								}
								String username=localDb.retrieveUsername(expenseTemp.getUserId());
								LogIn login=null;
								if(username==null){
									try {
										login=loginEndpoint.getLogIn(expenseTemp.getUserId()).execute();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
								Intent intentToCall=new Intent(this, UpdatesActivity.class);
								//							intentToCall.putExtra(Constants.STR_SHOW_TAB, 2);
								//							intentToCall.putExtra(Constants.STR_TRIP_NAME, tripBean.getName());
								//							intentToCall.putExtra(Constants.STR_USER_ID, userId);
								//							intentToCall.putExtra(Constants.STR_TRIP_ID, tripBean.getId());
								//							intentToCall.putExtra(Constants.STR_ADMIN_ID, tripBean.getAdminId());
								if(login!=null){
									username=login.getUsername();
									localDb.insertPerson(login.getId(), username, login.getPrefferedName());
								}
								long expenseUserId=expenseTemp.getUserId();
								if(expenseUserId!=userId){
									if(username!=null){
										sendNotification(toSyncTemp.getSyncType(), "Expense Added", "New expense "+expenseTemp.getName()+" added to "+tripBean.getName()+" by "+username+"!!", intentToCall, Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, expenseTemp.getTripId());
									} else{
										sendNotification(toSyncTemp.getSyncType(), "Expense Added", "New expense "+expenseTemp.getName()+" added to "+tripBean.getName()+"!!", intentToCall, Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, expenseTemp.getTripId());
									}
								}
								sendResult();
							}
						}
					}
				} else if(toSyncTemp.getSyncType().equals(Constants.STR_EXPENSE_UPDATED)){
					try {
						expenseTemp=expenseEndpoint.getExpense(toSyncTemp.getSyncItemId()).execute();
					} catch (IOException e) {
						e.printStackTrace();
						continue;
					}
					if(expenseTemp!=null){
						TripBean tripBean=localDb.retrieveTripDetails(expenseTemp.getTripId());
						ExpenseBean expenseBean=localDb.retrieveExpense(expenseTemp.getId());
						if(tripBean!=null){
							if(expenseBean!=null){
								List<Long> userIds = expenseTemp.getExpenseUserIds();
								if(userIds.contains(lngUserId)){
									localDb.updateExpense(expenseTemp.getName(), expenseTemp.getAmount(), expenseTemp.getDescription(), Global.listToString(userIds), Global.listToString(expenseTemp.getExpenseAmounts()), Constants.STR_SYNCHED, expenseTemp.getId());
									List<Long> lstUsers=expenseTemp.getExpenseUserIds();
									List<String> lstAmounts=expenseTemp.getExpenseAmounts();
									List<Long> lstUsersPrev=Global.longToList(expenseBean.getUserIds());
									List<String> lstAmountsPrev=Global.stringToList(expenseBean.getAmounts());
									if(!(lstUsers.equals(lstUsersPrev) && lstAmounts.equals(lstAmountsPrev))){
										DistributionBean1 distBean;
										String strAmountPrev="0", strAmount="0";
										distBean=localDb.retrieveDistributionByUsers(expenseBean.getUserId(), lngUserId, expenseBean.getTripId());
										int posPrev=lstUsersPrev.indexOf(lngUserId);
										int pos=lstUsers.indexOf(lngUserId);
										if(posPrev!=-1 || pos!=-1){
											if(posPrev!=-1){
												strAmountPrev=lstAmountsPrev.get(posPrev);
											}
											if(pos!=-1){
												strAmount=lstAmounts.get(pos);
											}
											if(distBean==null){
												long rowId=localDb.insertDistribution(lngUserId, expenseBean.getUserId(), strAmount, expenseBean.getTripId());
												localDb.updateDistributionId(rowId, rowId);
											} else{
												float fAmount;
												if(distBean.getToId()!=distBean.getFromId() && distBean.getToId()!=lngUserId){
													fAmount=Global.subtract(Float.parseFloat(distBean.getAmount()), strAmountPrev);
													strAmount=String.valueOf(Global.add(fAmount, strAmount));
												} else{
													fAmount=Global.add(Float.parseFloat(distBean.getAmount()), strAmountPrev);
													strAmount=String.valueOf(Global.add(fAmount, strAmount));
												}
												localDb.updateDistAmount(distBean.getDistributionId(), strAmount);
											}
										}
									}
								} else{
									localDb.deleteExpense(expenseTemp.getId());
								}
							} else{
								String date = sdf.format(new Date(expenseTemp.getCreationDate().getValue()));
								localDb.insertExpense(expenseTemp.getName(), expenseTemp.getId(), date, "INR", expenseTemp.getAmount(), expenseTemp.getDescription(), expenseTemp.getTripId(), expenseTemp.getUserId(), Global.listToString(expenseTemp.getExpenseUserIds()), Global.listToString(expenseTemp.getExpenseAmounts()), Constants.STR_SYNCHED);
								List<Long> lstUsers=expenseTemp.getExpenseUserIds();
								List<String> lstAmounts=expenseTemp.getExpenseAmounts();
								DistributionBean1 distBean;
								String strAmount;
								distBean=localDb.retrieveDistributionByUsers(expenseTemp.getUserId(), lngUserId, expenseTemp.getTripId());
								int pos=lstUsers.indexOf(lngUserId);
								float fAmount;
								if(pos!=-1){
									strAmount=lstAmounts.get(pos);
									if(distBean==null){
										long rowId=localDb.insertDistribution(lngUserId, expenseTemp.getUserId(), strAmount, expenseTemp.getTripId());
										localDb.updateDistributionId(rowId, rowId);
									} else{
										if(distBean.getToId()!=distBean.getFromId() && distBean.getToId()!=lngUserId){
											fAmount=Global.subtract(Float.parseFloat(distBean.getAmount()), strAmount);
											strAmount=String.valueOf(Global.add(fAmount, strAmount));
										} else{
											fAmount=Global.add(Float.parseFloat(distBean.getAmount()), strAmount);
											strAmount=String.valueOf(Global.add(fAmount, strAmount));
										}
										localDb.updateDistAmount(distBean.getDistributionId(), strAmount);
									}
								}
							}
							expenseBean=localDb.retrieveExpense(expenseTemp.getId());
							

							String username=localDb.retrieveUsername(expenseTemp.getUserId());
							LogIn login=null;
							if(username==null){
								try {
									login=loginEndpoint.getLogIn(expenseTemp.getUserId()).execute();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							Intent intentToCall=new Intent(this, UpdatesActivity.class);
							//							intentToCall.putExtra(Constants.STR_SHOW_TAB, 2);
							//							intentToCall.putExtra(Constants.STR_TRIP_NAME, tripBean.getName());
							//							intentToCall.putExtra(Constants.STR_USER_ID, userId);
							//							intentToCall.putExtra(Constants.STR_TRIP_ID, tripBean.getId());
							//							intentToCall.putExtra(Constants.STR_ADMIN_ID, tripBean.getAdminId());
							if(login!=null){
								username=login.getUsername();
								localDb.insertPerson(login.getId(), username, login.getPrefferedName());
							}
							if(username!=null){
								sendNotification(toSyncTemp.getSyncType(), "Expense Updated", "Expense "+expenseTemp.getName()+" of expense-group "+tripBean.getName()+" updated by "+username+"!!", intentToCall, Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, expenseTemp.getTripId());
							} else{
								sendNotification(toSyncTemp.getSyncType(), "Expense Updated", "Expense "+expenseTemp.getName()+" of expense-group "+tripBean.getName()+" updated!!", intentToCall, Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, expenseTemp.getTripId());
							}
							sendResult();
						}
					}
				} else if(toSyncTemp.getSyncType().equals(Constants.STR_EXPENSE_DELETED)){
					ExpenseBean expenseBean=localDb.retrieveExpense(toSyncTemp.getSyncItemId());
					TripBean tripBean=localDb.retrieveTripDetails(expenseBean.getTripId());
					if(expenseBean!=null && !expenseBean.getSyncStatus().equals(Constants.STR_DELETED)){
						localDb.deleteExpense(expenseBean.getId());
						List<Long> lstUsersPrev=Global.longToList(expenseBean.getUserIds());
						List<String> lstAmountsPrev=Global.stringToList(expenseBean.getAmounts());

						DistributionBean1 distBean;
						String strAmountPrev;
						distBean=localDb.retrieveDistributionByUsers(expenseBean.getUserId(), lngUserId, expenseBean.getTripId());
						int pos=lstUsersPrev.indexOf(lngUserId);
						if(pos!=-1){
							strAmountPrev=lstAmountsPrev.get(pos);
							if(distBean!=null){
								if(distBean.getToId()!=distBean.getFromId() && distBean.getToId()!=lngUserId){
									strAmountPrev=String.valueOf(Global.subtract(Float.parseFloat(distBean.getAmount()), strAmountPrev));
								} else{
									strAmountPrev=String.valueOf(Global.add(Float.parseFloat(distBean.getAmount()), strAmountPrev));
								}
								localDb.updateDistAmount(distBean.getDistributionId(), strAmountPrev);
							}
						}
						String username=localDb.retrieveUsername(expenseBean.getUserId());
						LogIn login=null;
						if(username==null){
							try {
								login=loginEndpoint.getLogIn(expenseBean.getUserId()).execute();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						Intent intentToCall=new Intent(this, UpdatesActivity.class);
						//						intentToCall.putExtra(Constants.STR_SHOW_TAB, 2);
						//						intentToCall.putExtra(Constants.STR_TRIP_NAME, tripBean.getName());
						//						intentToCall.putExtra(Constants.STR_USER_ID, userId);
						//						intentToCall.putExtra(Constants.STR_TRIP_ID, tripBean.getId());
						//						intentToCall.putExtra(Constants.STR_ADMIN_ID, tripBean.getAdminId());
						if(login!=null){
							username=login.getUsername();
							localDb.insertPerson(login.getId(), username, login.getPrefferedName());
						}
						if(expenseBean.getUserId()!=userId){
							if(username!=null){
								sendNotification(toSyncTemp.getSyncType(), "Expense Deleted", "Expense "+expenseBean.getName()+" of expense-group "+tripBean.getName()+" deleted by "+username+"!!", intentToCall, Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, expenseBean.getTripId());
							} else{
								sendNotification(toSyncTemp.getSyncType(), "Expense Deleted", "Expense "+expenseBean.getName()+" of expense-group "+tripBean.getName()+" deleted!!", intentToCall, Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, expenseBean.getTripId());
							}
						}
						sendResult();
					}
				}
				try {
					endpoint.removeToSync(toSyncTemp.getId()).execute();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void sendNotification(String action, String title, String msg, Intent intent, int type, String group, long lngItemId) {
		mNotificationManager = (NotificationManager)
				this.getSystemService(Context.NOTIFICATION_SERVICE);
		SharedPreferences prefs = getSharedPreferences(Constants.STR_PREFERENCE, MODE_PRIVATE);
		int count = prefs.getInt(Constants.STR_COUNT, 0);
		count++;
		if(count>1){
			title=count+Constants.STR_NEW_UPDATES;
			msg=Constants.STR_CLICK;
		}
		new LocalDB(getApplicationContext()).insertToSync(action, msg, lngItemId);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK), PendingIntent.FLAG_UPDATE_CURRENT);
		Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.ic_notification_icon)
		.setContentTitle(title)
		.setStyle(new NotificationCompat.BigTextStyle()
		.bigText(msg))
		.setGroup(group)
		.setSound(alarmSound)
		.setAutoCancel(true)
		.setContentText(msg)
		.setTicker(Constants.STR_APP_NOTIFICATION)
		.setNumber(Constants.I_NOTIFICATION_ID);
		mBuilder.setContentIntent(contentIntent);
		prefs.edit().putInt(Constants.STR_COUNT, count);
		mNotificationManager.notify(type, mBuilder.build());
	}

	private void syncAllTrips() {
		long lngTripId=0L;
		long lngNewTripId=0L;
		LogIn login=null;
		Trip trip = null;
		Trip retTrip=null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		List<Long> liUserIds=new ArrayList<Long>();
		List<Long> listTripsTemp=null;
		try {
			LocalDB localDb=new LocalDB(getApplicationContext());
			lngUserId=localDb.retrieve();
			liUserIds.add(lngUserId);
			ArrayList<TripBean> arrTripsNotSynched = localDb.retrieveNotSynchedTrips();
			Tripendpoint.Builder builder = new Tripendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
			builder = CloudEndpointUtils.updateBuilder(builder);
			Tripendpoint endpoint = builder.build();
			Loginendpoint.Builder loginBuilder = new Loginendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
			loginBuilder = CloudEndpointUtils.updateBuilder(loginBuilder);
			Loginendpoint loginEndpoint = loginBuilder.build();
			String strTripName=null;
			for(TripBean tripTemp : arrTripsNotSynched){
				trip = new Trip();
				lngTripId=tripTemp.getId();
				strTripName=tripTemp.getName();

				try {
					if(tripTemp.getSyncStatus().equals(Constants.STR_UPDATED)){
						trip=endpoint.getTrip(lngTripId).execute();
						trip.setName(strTripName);
						trip.setChangerId(localDb.retrieveDeviceId());
						endpoint.updateTrip(trip).execute();
						localDb.updateTripSyncStatus(lngTripId);
					} else if(tripTemp.getSyncStatus().equals(Constants.STR_EXITED)){
						List<Long> lstTripTemp=null;
						trip=endpoint.getTrip(lngTripId).execute();
						lstTripTemp=trip.getUserIDs();
						if(lstTripTemp!=null){
							lstTripTemp.remove(lngUserId);
						}
						trip.setUserIDs(lstTripTemp);
						trip.setChangerId(localDb.retrieveDeviceId());
						endpoint.updateTrip(trip).execute();
						localDb.deleteTrip(lngTripId);
					} else if(tripTemp.getSyncStatus().equals(Constants.STR_DELETED)){
						ArrayList<ExpenseBean> arrExpenses = localDb.retrieveExpenses(lngTripId);
						for(ExpenseBean expense:arrExpenses){
							try {
								localDb.deleteExpense(expense.getId());
							} catch (Exception e) {
								e.printStackTrace();
								continue;
							}
						}
						endpoint.removeTrip(lngTripId).execute();
						localDb.deleteTrip(lngTripId);
					} else if(tripTemp.getSyncStatus().equals(Constants.STR_NOT_SYNCHED)){
						trip.setAdmin(lngUserId);
						trip.setName(tripTemp.getName());
						trip.setUserIDs(liUserIds);
						trip.setCreationDate(new DateTime(sdf.parse(tripTemp.getCreationDate())));
						trip.setChangerId(localDb.retrieveDeviceId());
						retTrip=endpoint.insertTrip(trip).execute();
						lngNewTripId=retTrip.getId();
						login=loginEndpoint.getLogIn(lngUserId).execute();
						listTripsTemp=login.getTripIDs();
						if(listTripsTemp==null){
							listTripsTemp=new ArrayList<Long>();
						}
						if(!listTripsTemp.contains(retTrip.getId())){
							listTripsTemp.add(retTrip.getId());
						}
						login.setTripIDs(listTripsTemp);
						loginEndpoint.updateLogIn(login).execute();
						Expenseendpoint.Builder expenseBuilder = new Expenseendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
						expenseBuilder = CloudEndpointUtils.updateBuilder(expenseBuilder);
						Expenseendpoint expenseEndpoint = expenseBuilder.build();
						ArrayList<ExpenseBean> arrExpenses = localDb.retrieveExpenses(lngTripId);
						boolean updatedExpenses=true;
						Expense expense=new Expense();
						for(ExpenseBean expenseTemp:arrExpenses){
							if(!expenseTemp.getSyncStatus().equals(Constants.STR_NOT_SYNCHED)){
								try {
									expense=expenseEndpoint.getExpense(expenseTemp.getId()).execute();
									expense.setTripId(lngNewTripId);
									expense.setChangerId(localDb.retrieveDeviceId());
									expense=expenseEndpoint.updateExpense(expense).execute();
								} catch (Exception e) {
									e.printStackTrace();
									updatedExpenses=false;
									continue;
								}
							}
						}
						if(updatedExpenses){
							localDb.updateTripSyncStatus(lngTripId, lngNewTripId);
							localDb.updateExpenseTripId(lngTripId, lngNewTripId);
						}
					} else if(tripTemp.getSyncStatus().equals(Constants.STR_QR_ADDED)){
						Expenseendpoint.Builder expenseBuilder = new Expenseendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
						expenseBuilder = CloudEndpointUtils.updateBuilder(expenseBuilder);
						Expenseendpoint expenseEndpoint = expenseBuilder.build();
						retTrip=endpoint.getTrip(lngTripId).execute();
						List<Long> listUsersTemp=retTrip.getUserIDs();
						String strUsername=localDb.retrieveUsername();
						String strUserTemp=null;
						StringBuffer sbUsers=new StringBuffer(String.valueOf(lngUserId));

						if(listUsersTemp==null){
							listUsersTemp=new ArrayList<Long>();
						}
						for(long userId:listUsersTemp){
							strUserTemp=localDb.retrieveUsername(userId);
							if(strUserTemp==null){
								try {
									login=loginEndpoint.getLogIn(userId).execute();
								} catch (Exception e) {
									e.printStackTrace();
									continue;
								}
								strUserTemp=login.getUsername();
								localDb.insertPerson(userId, strUserTemp, login.getPrefferedName());

							}
							sbUsers.append(',');
							sbUsers.append(userId);
						}
						CollectionResponseExpense response = expenseEndpoint.listExpense().setTripId(retTrip.getId()).execute();
						if(response!=null){
							ArrayList<Expense> arrExpense = (ArrayList<Expense>) response.getItems();
							String date=null;
							List<Long> userIds;
							List<String> amounts;
							String strUserIds, strAmounts;
							if(arrExpense!=null && arrExpense.size()!=0){
								for(Expense tempExpense:arrExpense){
									date = sdf.format(new Date(tempExpense.getCreationDate().getValue()));
									userIds=tempExpense.getExpenseUserIds();
									if(userIds.contains(lngUserId)){
										strUserIds=Global.listToString(userIds);
	
										amounts=tempExpense.getExpenseAmounts();
										strAmounts=Global.listToString(amounts);
										localDb.insertExpense(tempExpense.getName(), tempExpense.getId(), date, "INR", tempExpense.getAmount(), tempExpense.getDescription(), tempExpense.getTripId(), tempExpense.getUserId(), strUserIds, strAmounts, Constants.STR_SYNCHED);
									}
								}
							}
						}


						login=loginEndpoint.getLogIn(lngUserId).execute();
						listTripsTemp=login.getTripIDs();
						if(listTripsTemp==null){
							listTripsTemp=new ArrayList<Long>();
						}
						if(!listTripsTemp.contains(lngTripId)){
							listTripsTemp.add(lngTripId);
							login.setTripIDs(listTripsTemp);
							loginEndpoint.updateLogIn(login).execute();
						}
						if(!listUsersTemp.contains(lngUserId)){
							listUsersTemp.add(lngUserId);
							retTrip.setUserIDs(listUsersTemp);
							retTrip.setChangerId(localDb.retrieveDeviceId());
							endpoint.updateTrip(retTrip).execute();
						}
						localDb.updateTripUsers(lngTripId, sbUsers.toString());
						localDb.updateTripSyncStatus(lngTripId, retTrip.getId());
					}
					sendResult();
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}

		} catch(Exception e){
			e.printStackTrace();
		}
	}

	private void syncAllExpenses() {
		Expense expense = null;
		Expense retExpense=null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		List<Long> liUserIds=new ArrayList<Long>();
		try {
			LocalDB localDb=new LocalDB(getApplicationContext());
			lngUserId=localDb.retrieve();
			liUserIds.add(lngUserId);
			Expenseendpoint.Builder builder = new Expenseendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
			builder = CloudEndpointUtils.updateBuilder(builder);
			Expenseendpoint endpoint = builder.build();
			ArrayList<ExpenseBean> arrExpensesNotSynched = localDb.retrieveNotSynchedExpenses();
			for(ExpenseBean expenseTemp:arrExpensesNotSynched){
				expense = new Expense();
				try {
					if(expenseTemp.getSyncStatus().equals(Constants.STR_UPDATED)){
						expense=endpoint.getExpense(expenseTemp.getId()).execute();
						if(expense!=null){
							expense.setName(expenseTemp.getName());
							expense.setAmount(expenseTemp.getAmount());
							expense.setDescription(expenseTemp.getDesc());
							expense.setExpenseUserIds(Global.longToList(expenseTemp.getUserIds()));
							expense.setExpenseAmounts(Global.stringToList(expenseTemp.getAmounts()));
							expense.setChangerId(localDb.retrieveDeviceId());
							expense=endpoint.updateExpense(expense).execute();
							localDb.updateExpenseSyncStatus(expenseTemp.getId(), expense.getId());
						}
					} else if(expenseTemp.getSyncStatus().equals(Constants.STR_DELETED)){
						Expenseendpoint.Builder expenseBuilder = new Expenseendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
						expenseBuilder = CloudEndpointUtils.updateBuilder(expenseBuilder);
						Expenseendpoint expenseEndpoint = expenseBuilder.build();
						expenseEndpoint.removeExpense(expense.getId()).execute();
						localDb.deleteExpense(expense.getId());
					} else if(expenseTemp.getSyncStatus().equals(Constants.STR_NOT_SYNCHED)){
						expense.setName(expenseTemp.getName());
						expense.setDescription(expenseTemp.getDesc());
						expense.setTripId(expenseTemp.getTripId());
						expense.setUserId(expenseTemp.getUserId());
						expense.setAmount(expenseTemp.getAmount());
						expense.setExpenseUserIds(Global.longToList(expenseTemp.getUserIds()));
						expense.setExpenseAmounts(Global.stringToList(expenseTemp.getAmounts()));
						expense.setCurrency(expenseTemp.getCurrency());
						expense.setCreationDate(new DateTime(sdf.parse(expenseTemp.getCreationDate())));
						expense.setChangerId(localDb.retrieveDeviceId());
						retExpense=endpoint.insertExpense(expense).execute();
						if(retExpense!=null){
							localDb.updateExpenseSyncStatus(expenseTemp.getId(), retExpense.getId());
						}
					}
					sendResult();
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}

		} catch(Exception e){
			e.printStackTrace();
		}
	}
}

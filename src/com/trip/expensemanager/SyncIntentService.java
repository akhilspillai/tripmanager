package com.trip.expensemanager;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
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
import android.util.Log;
import android.util.Patterns;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.trip.expensemanager.deviceinfoendpoint.Deviceinfoendpoint;
import com.trip.expensemanager.deviceinfoendpoint.model.DeviceInfo;
import com.trip.expensemanager.distributionendpoint.Distributionendpoint;
import com.trip.expensemanager.distributionendpoint.model.CollectionResponseDistribution;
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
import com.trip.utils.DistributionBean1;
import com.trip.utils.ExpenseBean;
import com.trip.utils.Global;
import com.trip.utils.LocalDB;
import com.trip.utils.TripBean;
import com.trip.utils.UpdateBean;
import com.trip.utils.billing.IabHelper;
import com.trip.utils.billing.IabResult;
import com.trip.utils.billing.Inventory;

public class SyncIntentService extends IntentService{

	public static final String RESULT_SYNC = "com.trip.expensemanager.REQUEST_PROCESSED";
	public static final String RESULT_PURCHASE = "com.trip.expensemanager.PURCHASE";

	private long lngUserId;
	private NotificationManager mNotificationManager;
	private LocalBroadcastManager broadcaster;
	private IabHelper mHelper;

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

			String base64EncodedPublicKey=Constants.STR_LICENSE_1+
					Constants.STR_LICENSE_2+Constants.STR_LICENSE_3+
					Constants.STR_LICENSE_4+Constants.STR_LICENSE_5+
					Constants.STR_LICENSE_6+Constants.STR_LICENSE_7;

			mHelper = new IabHelper(this, base64EncodedPublicKey);

			mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
				public void onIabSetupFinished(IabResult result) {
					if (!result.isSuccess()) {
						Log.d("Akhil", "Problem setting up In-app Billing: " + result);
						return;
					}
					IabHelper.QueryInventoryFinishedListener mGotInventoryListener 
					= new IabHelper.QueryInventoryFinishedListener() {
						public void onQueryInventoryFinished(IabResult result,
								Inventory inventory) {
							boolean mIsPremium=false;
							if (!result.isFailure()) {
								mIsPremium = inventory.hasPurchase(Constants.STR_SKU_PREMIUM); 
							}
							
							SharedPreferences prefs = getSharedPreferences(Constants.STR_PREFERENCE, MODE_PRIVATE);
							prefs.edit().putBoolean(Constants.STR_PURCHASED, mIsPremium).commit();
							sendResult(RESULT_PURCHASE);
						}
					};

					mHelper.queryInventoryAsync(mGotInventoryListener);
				}
			});

			if(Global.isConnected(getApplicationContext())){
				SharedPreferences prefs = getSharedPreferences(Constants.STR_PREFERENCE, MODE_PRIVATE);
				String strOldVersion = prefs.getString(Constants.STR_VERSION, "0");
				int iOldVersionCode = prefs.getInt(Constants.STR_VERSION_CODE, 0);
				String strNewVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
				int iNewVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
				if(!strOldVersion.equals(strNewVersion) || iOldVersionCode!=iNewVersionCode){
					if(getNewGCMId()){
						prefs.edit().putString(Constants.STR_VERSION, strNewVersion).commit();
						prefs.edit().putInt(Constants.STR_VERSION_CODE, iNewVersionCode).commit();
					}
				}
			}

			if(Global.isConnected(getApplicationContext())){
				syncOthers();
			}
			if(Global.isConnected(getApplicationContext())){
				if(getSharedPreferences(Constants.STR_PREFERENCE, MODE_PRIVATE).getBoolean(Constants.STR_PURCHASED, false)){
					syncPurchase();
				}
			}
			if(Global.isConnected(getApplicationContext())){
				syncAllTrips();
			}
			if(Global.isConnected(getApplicationContext())){
				syncAllExpenses();
			}
			if(Global.isConnected(getApplicationContext())){
				syncAllSettledDistributions();
			}
			if(intent.getBooleanExtra(Constants.STR_IS_FROM_GCM, false)){
				GcmBroadcastReceiver.completeWakefulIntent(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void syncPurchase() throws IOException {
		LogIn retLogin=null;
		LocalDB localDb=new LocalDB(this);
		String[] strArrPurchases=localDb.getPurchaseId();
		if(strArrPurchases!=null){
			if(strArrPurchases[1]!=null && strArrPurchases[0].equals(Constants.STR_NO)){
				Loginendpoint.Builder builder = new Loginendpoint.Builder(
						AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
				builder = CloudEndpointUtils.updateBuilder(builder);
				Loginendpoint endpoint = builder.build();
				retLogin=endpoint.getLogIn(localDb.retrieve()).execute();
				if(retLogin.getPurchaseId()==null){
					retLogin.setPurchaseId(strArrPurchases[1]);
					endpoint.updateLogIn(retLogin).execute();
				}
			}
		}

		localDb.updatePurchaseToSynced();
	}

	private boolean getNewGCMId() throws IOException {
		LocalDB localDb = new LocalDB(getApplicationContext());
		long deviceId = localDb.retrieveDeviceId();
		Deviceinfoendpoint.Builder devInfoBuilder = new Deviceinfoendpoint.Builder(
				AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
		devInfoBuilder = CloudEndpointUtils.updateBuilder(devInfoBuilder);
		Deviceinfoendpoint devInfoEndpoint = devInfoBuilder.build();
		DeviceInfo devInfo = devInfoEndpoint.getDeviceInfo(deviceId).execute();
		GoogleCloudMessaging gcm=null;
		try {
			if (gcm == null) {
				gcm = GoogleCloudMessaging.getInstance(this);
			}
			String strRegID = gcm.register(ProcessingActivity.PROJECT_NUMBER);
			if(!devInfo.getGcmRegId().equals(strRegID)){
				devInfo.setGcmRegId(strRegID);
				devInfoEndpoint.updateDeviceInfo(devInfo).execute();
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private void sendResult(String strResult) {
		Intent intent = new Intent(strResult);
		broadcaster.sendBroadcast(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}


	private void syncAllSettledDistributions() throws ParseException {
		LocalDB localDb=new LocalDB(getApplicationContext());
		lngUserId=localDb.retrieve();
		Distributionendpoint.Builder builder = new Distributionendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
		builder = CloudEndpointUtils.updateBuilder(builder);
		Distributionendpoint endpoint = builder.build();
		Distribution retDist, dist=new Distribution();
		ArrayList<DistributionBean1> arrDistributionsNotSynched = localDb.retrieveNotSynchedDistributions();
		String amount;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		long deviceId=localDb.retrieveDeviceId();
		for(DistributionBean1 distTemp:arrDistributionsNotSynched){				
			amount=distTemp.getAmount();
			if(distTemp.getFromId()!=lngUserId){
				dist.setFromId(distTemp.getFromId());
			} else{
				dist.setFromId(distTemp.getToId());
				amount=amount.substring(1);
			}
			dist.setToId(lngUserId);
			dist.setAmount(amount);
			dist.setChangerId(deviceId);
			dist.setPaid(Constants.STR_YES);
			dist.setTripId(distTemp.getTripId());
			dist.setCreationDate(new DateTime(sdf.parse(distTemp.getCreationDate())));
			try {
				retDist=endpoint.insertDistribution(dist).execute();
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			localDb.updateDistributionId(distTemp.getDistributionId(), retDist.getId());
			localDb.updateDistributionPaidStatus(retDist.getId(), Constants.STR_YES);
		}
	}

	private void syncOthers() throws IOException {
		LocalDB localDb=new LocalDB(getApplicationContext());
		lngUserId=localDb.retrieve();
		Tosyncendpoint.Builder builder = new Tosyncendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
		builder = CloudEndpointUtils.updateBuilder(builder);
		Tosyncendpoint endpoint = builder.build();
		long userId=localDb.retrieve();
		long devId=localDb.retrieveDeviceId();
		CollectionResponseToSync entities=null;
		Pattern emailPattern = Patterns.EMAIL_ADDRESS;
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
			Distributionendpoint.Builder distBuilder = new Distributionendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
			expenseBuilder = CloudEndpointUtils.updateBuilder(expenseBuilder);
			Distributionendpoint distEndpoint = distBuilder.build();
			Distribution distTemp=null;
			Loginendpoint.Builder loginBuilder = new Loginendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
			loginBuilder = CloudEndpointUtils.updateBuilder(loginBuilder);
			Loginendpoint loginEndpoint = loginBuilder.build();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

			for(ToSync toSyncTemp:entities.getItems()){
				try {
					if(toSyncTemp.getSyncType().equals(Constants.STR_LOG_OUT)){
						localDb.clearAllTables();
						sendResult(RESULT_SYNC);
					} else if(toSyncTemp.getSyncType().equals(Constants.STR_ITEM_PURCHASED)){
						LogIn login=loginEndpoint.getLogIn(lngUserId).execute();
						if(login!=null){
							String strPurchaseId=login.getPurchaseId();
							if(strPurchaseId!=null){
								localDb.updatePurchaseId(strPurchaseId);
								localDb.updatePurchaseToSynced();
								SharedPreferences prefs = getSharedPreferences(Constants.STR_PREFERENCE, MODE_PRIVATE);
								prefs.edit().putBoolean(Constants.STR_PURCHASED, true).commit();
							}
						}
					} else if(toSyncTemp.getSyncType().equals(Constants.STR_TRIP_UPDATED)){
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
								String username=localDb.retrievePrefferedName(changerId);
								LogIn login=null;
								if(username==null || !emailPattern.matcher(username).matches()){
									try {
										login=loginEndpoint.getLogIn(changerId).execute();
									} catch (IOException e) {
										e.printStackTrace();
										continue;
									}
								}
								if(login!=null){
									if(username==null){
										localDb.insertPerson(login.getId(), login.getPrefferedName(), login.getPrefferedName());
									} else{
										localDb.updatePerson(login.getPrefferedName(), login.getId());
									}
								}
								localDb.updateTrip(tripTemp.getId(), tripTemp.getName(), Constants.STR_SYNCHED);
								if(changerId!=userId){
									if(username!=null){
										sendNotification(toSyncTemp.getSyncType(), "EG Name Changed", "Expense-group name changed from "+oldTripName+" to "+newTripName+" by "+username+"!!", new Intent(this, UpdatesActivity.class), Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, tripTemp.getId());
									} else{
										sendNotification(toSyncTemp.getSyncType(), "EG Name Changed", "Expense-group name changed from "+oldTripName+" to "+newTripName+"!!", new Intent(this, UpdatesActivity.class), Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, tripTemp.getId());
									}
								}
								sendResult(RESULT_SYNC);
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
								strUserTemp=localDb.retrievePrefferedName(userIdTemp);
								if(strUserTemp==null){
									try {
										login=loginEndpoint.getLogIn(userIdTemp).execute();
									} catch (Exception e) {
										e.printStackTrace();
										continue;
									}
									strUserTemp=login.getPrefferedName();
									localDb.insertPerson(userIdTemp, strUserTemp, login.getPrefferedName());

								}
								sbUsers.append(userIdTemp);
								sbUsers.append(',');
							}
							sbUsers.delete(sbUsers.length()-1, sbUsers.length());

							localDb.insertTrip(tripTemp.getName(), tripTemp.getId(), date, sbUsers.toString(), tripTemp.getAdmin(), Constants.STR_SYNCHED);
							sendResult(RESULT_SYNC);
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
								if(changerId!=lngUserId){
									List<Long> users=tripTemp.getUserIDs();
									LogIn login=null;
									String username=null;
									StringBuffer sbUsers=new StringBuffer();
									for(Long userIdTemp:users){
										username=localDb.retrievePrefferedName(userIdTemp);
										if(username==null || !emailPattern.matcher(username).matches()){
											try {
												login=loginEndpoint.getLogIn(userIdTemp).execute();
											} catch (IOException e) {
												e.printStackTrace();
												continue;
											}
											if(login!=null){
												if(username==null){
													localDb.insertPerson(login.getId(), login.getPrefferedName(), login.getPrefferedName());
												} else{
													localDb.updatePerson(login.getPrefferedName(), login.getId());
												}
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
								} else{
									if(!tripTemp.getUserIDs().contains(lngUserId)){
										localDb.deleteExpenseofTrip(tripTemp.getId());
										localDb.deleteDistributionofTrip(tripTemp.getId());
										localDb.deleteTrip(tripTemp.getId());
									}
								}
								sendResult(RESULT_SYNC);
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
								username=localDb.retrievePrefferedName(changerId);
								if(username==null || !emailPattern.matcher(username).matches()){
									try {
										login=loginEndpoint.getLogIn(changerId).execute();
									} catch (IOException e) {
										e.printStackTrace();
										continue;
									}
									if(login!=null){
										if(username==null){
											localDb.insertPerson(login.getId(), login.getPrefferedName(), login.getPrefferedName());
										} else{
											localDb.updatePerson(login.getPrefferedName(), login.getId());
										}
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
										username=localDb.retrievePrefferedName(userIdTemp);
										if(username==null || !emailPattern.matcher(username).matches()){
											try {
												login=loginEndpoint.getLogIn(userIdTemp).execute();
											} catch (IOException e) {
												e.printStackTrace();
												continue;
											}
											if(login!=null){
												if(username==null){
													localDb.insertPerson(login.getId(), login.getPrefferedName(), login.getPrefferedName());
												} else{
													localDb.updatePerson(login.getPrefferedName(), login.getId());
												}
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
								}
							} else{
								LogIn login;
								List<Long> listUsersTemp=tripTemp.getUserIDs();
								String strUserTemp=null;
								String date=null;
								StringBuffer sbUsers=new StringBuffer(String.valueOf(lngUserId));

								if(listUsersTemp==null){
									listUsersTemp=new ArrayList<Long>();
								}
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
									sbUsers.append(',');
									sbUsers.append(userIdTemp);
								}
								CollectionResponseExpense response = expenseEndpoint.listExpense().setTripId(tripTemp.getId()).execute();
								if(response!=null){
									ArrayList<Expense> arrExpense = (ArrayList<Expense>) response.getItems();
									List<Long> userIds;
									List<String> amounts;
									String strUserIds, strAmounts;
									if(arrExpense!=null && arrExpense.size()!=0){
										for(Expense tempExpense:arrExpense){
											date = sdf.format(new Date(tempExpense.getCreationDate().getValue()));
											userIds=tempExpense.getExpenseUserIds();
											strUserIds=Global.listToString(userIds);

											amounts=tempExpense.getExpenseAmounts();
											strAmounts=Global.listToString(amounts);
											localDb.insertExpense(tempExpense.getName(), tempExpense.getId(), date, "INR", tempExpense.getAmount(), tempExpense.getDescription(), tempExpense.getTripId(), tempExpense.getUserId(), strUserIds, strAmounts, Constants.STR_SYNCHED);
										}
									}
								}
								CollectionResponseDistribution distResponse = distEndpoint.listDistribution().setTripId(tripTemp.getId()).execute();
								long rowId;
								if(distResponse!=null){
									ArrayList<Distribution> arrDist = (ArrayList<Distribution>) distResponse.getItems();
									if(arrDist!=null && arrDist.size()!=0){
										for(Distribution tempDist:arrDist){
											date = sdf.format(new Date(tempDist.getCreationDate().getValue()));
											rowId=localDb.insertDistribution(tempDist.getFromId(), tempDist.getToId(), tempDist.getAmount(), tempDist.getTripId(), Constants.STR_YES, date);
											localDb.updateDistributionId(rowId, tempDist.getId());
										}
									}
								}
								String strDate = sdf.format(new Date(tripTemp.getCreationDate().getValue()));
								localDb.insertTrip(tripTemp.getName(), tripTemp.getId(), strDate, Global.listToString(tripTemp.getUserIDs()), tripTemp.getAdmin(), Constants.STR_SYNCHED);

							}
							sendResult(RESULT_SYNC);
						}
					} else if(toSyncTemp.getSyncType().equals(Constants.STR_TRIP_DELETED)){
						LogIn login=null;
						TripBean tripBean=localDb.retrieveTripDetails(toSyncTemp.getSyncItemId());
						long changerId=toSyncTemp.getChangerId();
						if(tripBean!=null && !tripBean.getSyncStatus().equals(Constants.STR_DELETED)){
							String strChanger=localDb.retrievePrefferedName(changerId);
							if(strChanger==null){
								try {
									login=loginEndpoint.getLogIn(toSyncTemp.getChangerId()).execute();
								} catch (IOException e) {
									e.printStackTrace();
									continue;
								}
								if(login!=null){
									strChanger=login.getPrefferedName();
									localDb.insertPerson(login.getId(), strChanger, login.getPrefferedName());
								}
							}
							localDb.deleteExpenseofTrip(tripBean.getId());
							localDb.deleteDistributionofTrip(tripBean.getId());
							localDb.deleteTrip(tripBean.getId());
							if(tripBean.getAdminId()!=userId){
								if(strChanger!=null){
									sendNotification(toSyncTemp.getSyncType(), "EG Deleted", "Expense-group "+tripBean.getName()+" deleted by "+strChanger+"!!", new Intent(this, UpdatesActivity.class), Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, tripBean.getId());
								} else{
									sendNotification(toSyncTemp.getSyncType(), "EG Deleted", "Expense-group "+tripBean.getName()+" deleted!!", new Intent(this, UpdatesActivity.class), Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, tripBean.getId());
								}
							}
							sendResult(RESULT_SYNC);
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

									String username=localDb.retrievePrefferedName(expenseTemp.getUserId());
									LogIn login=null;
									if(username==null || !emailPattern.matcher(username).matches()){
										try {
											login=loginEndpoint.getLogIn(expenseTemp.getUserId()).execute();
										} catch (IOException e) {
											e.printStackTrace();
											continue;
										}
									}
									Intent intentToCall=new Intent(this, UpdatesActivity.class);
									if(login!=null){
										if(username==null){
											localDb.insertPerson(login.getId(), login.getPrefferedName(), login.getPrefferedName());
										} else{
											localDb.updatePerson(login.getPrefferedName(), login.getId());
										}
									}
									long expenseUserId=expenseTemp.getUserId();
									if(expenseUserId!=userId){
										if(username!=null){
											sendNotification(toSyncTemp.getSyncType(), "Expense Added", "New expense "+expenseTemp.getName()+" added to "+tripBean.getName()+" by "+username+"!!", intentToCall, Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, expenseTemp.getTripId());
										} else{
											sendNotification(toSyncTemp.getSyncType(), "Expense Added", "New expense "+expenseTemp.getName()+" added to "+tripBean.getName()+"!!", intentToCall, Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, expenseTemp.getTripId());
										}
									}
									sendResult(RESULT_SYNC);
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
									} else{
										localDb.deleteExpense(expenseTemp.getId());
									}
								} else{
									String date = sdf.format(new Date(expenseTemp.getCreationDate().getValue()));
									localDb.insertExpense(expenseTemp.getName(), expenseTemp.getId(), date, "INR", expenseTemp.getAmount(), expenseTemp.getDescription(), expenseTemp.getTripId(), expenseTemp.getUserId(), Global.listToString(expenseTemp.getExpenseUserIds()), Global.listToString(expenseTemp.getExpenseAmounts()), Constants.STR_SYNCHED);
								}
								expenseBean=localDb.retrieveExpense(expenseTemp.getId());

								String username=localDb.retrievePrefferedName(expenseTemp.getUserId());
								LogIn login=null;
								if(username==null || !emailPattern.matcher(username).matches()){
									try {
										login=loginEndpoint.getLogIn(expenseTemp.getUserId()).execute();
									} catch (IOException e) {
										e.printStackTrace();
										continue;
									}
								}
								Intent intentToCall=new Intent(this, UpdatesActivity.class);
								if(login!=null){
									if(username==null){
										localDb.insertPerson(login.getId(), login.getPrefferedName(), login.getPrefferedName());
									} else{
										localDb.updatePerson(login.getPrefferedName(), login.getId());
									}
								}
								if(expenseTemp.getUserId()!=lngUserId){
									if(username!=null){
										sendNotification(toSyncTemp.getSyncType(), "Expense Updated", "Expense "+expenseTemp.getName()+" of expense-group "+tripBean.getName()+" updated by "+username+"!!", intentToCall, Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, expenseTemp.getTripId());
									} else{
										sendNotification(toSyncTemp.getSyncType(), "Expense Updated", "Expense "+expenseTemp.getName()+" of expense-group "+tripBean.getName()+" updated!!", intentToCall, Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, expenseTemp.getTripId());
									}
								}
								sendResult(RESULT_SYNC);
							}
						}
					} else if(toSyncTemp.getSyncType().equals(Constants.STR_EXPENSE_DELETED)){
						ExpenseBean expenseBean=localDb.retrieveExpense(toSyncTemp.getSyncItemId());
						if(expenseBean!=null && !expenseBean.getSyncStatus().equals(Constants.STR_DELETED)){
							TripBean tripBean=localDb.retrieveTripDetails(expenseBean.getTripId());
							localDb.deleteExpense(expenseBean.getId());
							String username=localDb.retrievePrefferedName(expenseBean.getUserId());
							LogIn login=null;
							if(username==null || !emailPattern.matcher(username).matches()){
								try {
									login=loginEndpoint.getLogIn(expenseBean.getUserId()).execute();
								} catch (IOException e) {
									e.printStackTrace();
									continue;
								}
							}
							Intent intentToCall=new Intent(this, UpdatesActivity.class);
							if(login!=null){
								if(username==null){
									localDb.insertPerson(login.getId(), login.getPrefferedName(), login.getPrefferedName());
								} else{
									localDb.updatePerson(login.getPrefferedName(), login.getId());
								}
							}
							if(expenseBean.getUserId()!=userId){
								if(username!=null){
									sendNotification(toSyncTemp.getSyncType(), "Expense Deleted", "Expense "+expenseBean.getName()+" of expense-group "+tripBean.getName()+" deleted by "+username+"!!", intentToCall, Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, expenseBean.getTripId());
								} else{
									sendNotification(toSyncTemp.getSyncType(), "Expense Deleted", "Expense "+expenseBean.getName()+" of expense-group "+tripBean.getName()+" deleted!!", intentToCall, Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, expenseBean.getTripId());
								}
							}
							sendResult(RESULT_SYNC);
						}
					} else if(toSyncTemp.getSyncType().equals(Constants.STR_DISTRIBUTION_ADDED)){
						try {
							distTemp=distEndpoint.getDistribution(toSyncTemp.getSyncItemId()).execute();
						} catch (IOException e) {
							e.printStackTrace();
							continue;
						}
						if(distTemp!=null){
							long fromId=distTemp.getFromId();
							long toId=distTemp.getToId();
							long tripId=distTemp.getTripId();
							long rowId;
							String strAmount=distTemp.getAmount();
							String date = sdf.format(new Date(distTemp.getCreationDate().getValue()));
							if(strAmount.startsWith("-")){
								strAmount=strAmount.substring(1);
							}
							if(toId!=lngUserId){
								rowId=localDb.insertDistribution(fromId, toId, strAmount, tripId, Constants.STR_YES, date);
								localDb.updateDistributionId(rowId, distTemp.getId());
								Intent intentToCall=new Intent(this, UpdatesActivity.class);
								String toUser=localDb.retrievePrefferedName(toId);
								String fromUser=localDb.retrievePrefferedName(fromId);
								if(Constants.STR_YOU.equalsIgnoreCase(fromUser)){
									fromUser="your";
								} else{
									fromUser=fromUser+"'s";
								}
								TripBean tripBean=localDb.retrieveTripDetails(distTemp.getTripId());
								if(toUser!=null && fromUser!=null){
									sendNotification(toSyncTemp.getSyncType(), "Debt Settled", toUser+" marked "+fromUser+" debt of "+strAmount+" in the expense-group "+tripBean.getName()+" as paid!!", intentToCall , Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, tripBean.getId());
								} else{
									sendNotification(toSyncTemp.getSyncType(), "Debt Settled", "Debt of "+strAmount+" in the expense-group "+tripBean.getName()+" is marked as paid!!", intentToCall , Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, tripBean.getId());
								}

							} else{
								rowId=localDb.insertDistribution(fromId, toId, strAmount, tripId, Constants.STR_YES, date);
								localDb.updateDistributionId(rowId, distTemp.getId());
							}
							sendResult(RESULT_SYNC);
						}
					}
					endpoint.removeToSync(toSyncTemp.getId()).execute();
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
			}
		}
	}

	@SuppressLint("InlinedApi")
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
		LocalDB localDb=new LocalDB(getApplicationContext());
		localDb.insertToSync(action, msg, lngItemId);
		List<UpdateBean> lstUpdates=localDb.retrieveUpdates();
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK), PendingIntent.FLAG_UPDATE_CURRENT);
		Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		NotificationCompat.Builder mBuilder = null;
		int size=lstUpdates.size();
		if(size==1){
			mBuilder=new NotificationCompat.Builder(this)
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
		} else{
			StringBuilder sbBigText=new StringBuilder();
			for(UpdateBean updateTemp:lstUpdates){
				sbBigText.append(updateTemp.getUpdate());
				sbBigText.append("\n");
			}
			sbBigText.deleteCharAt(sbBigText.length()-1);
			mBuilder=new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_notification_icon)
			.setContentTitle("Expense Manager")
			.setStyle(new NotificationCompat.BigTextStyle()
			.bigText(sbBigText.toString()))
			.setGroup(group)
			.setSound(alarmSound)
			.setAutoCancel(true)
			.setContentText(size+" new updates")
			.setTicker(Constants.STR_APP_NOTIFICATION)
			.setNumber(Constants.I_NOTIFICATION_ID);
		}
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
						List<ExpenseBean> lstExpenses = localDb.retrieveExpenses(lngTripId);
						List<Long> expUserIds;
						List<String> expAmounts;
						long userId=0L;
						int indexOfExpense;
						String strAmtToGet="0";
						for(ExpenseBean expense:lstExpenses){
							userId=expense.getUserId();
							expUserIds=Global.longToList(expense.getUserIds());
							expAmounts=Global.stringToList(expense.getAmounts());
							if(userId==lngUserId){
								strAmtToGet=Global.add(strAmtToGet, expense.getAmount());
							}
							indexOfExpense=expUserIds.indexOf(lngUserId);
							if(indexOfExpense>=0){
								strAmtToGet=Global.subtract(strAmtToGet, expAmounts.get(indexOfExpense));
							}
						}
						List<DistributionBean1> lstDist=localDb.retrieveSettledDistributionByUser(lngUserId, lngTripId);
						for(DistributionBean1 distTemp:lstDist){
							if(distTemp.getFromId()==lngUserId){
								strAmtToGet=Global.add(strAmtToGet, distTemp.getAmount());
							} else{
								strAmtToGet=Global.subtract(strAmtToGet, distTemp.getAmount());
							}
						}
						float fAmtToGet=Float.parseFloat(strAmtToGet);
						if(fAmtToGet==0){
							Tosyncendpoint.Builder toSyncBuilder = new Tosyncendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
							toSyncBuilder = CloudEndpointUtils.updateBuilder(toSyncBuilder);
							Tosyncendpoint toSyncEndpoint = toSyncBuilder.build();
							long devId=localDb.retrieveDeviceId();
							CollectionResponseToSync entities=null;
							try {
								entities = toSyncEndpoint.listToSync().setUserId(devId).execute();
							} catch (IOException e) {
								e.printStackTrace();
							}
							if (entities == null || entities.getItems() == null || entities.getItems().size() < 1) {
								trip=endpoint.getTrip(lngTripId).execute();
								lstTripTemp=trip.getUserIDs();
								if(lstTripTemp!=null){
									lstTripTemp.remove(lngUserId);
								}
								trip.setUserIDs(lstTripTemp);
								trip.setChangerId(localDb.retrieveDeviceId());
								endpoint.updateTrip(trip).execute();
								localDb.deleteDistributionofTrip(lngTripId);
								localDb.deleteExpenseofTrip(lngTripId);
								localDb.deleteTrip(lngTripId);
							}
						} else{
							localDb.updateTripSyncStatus(lngTripId);
							sendNotification(Constants.STR_NOT_EXITED, "EG Not Exited", "Expense-group "+tripTemp.getName()+" not exited as there are unsettled distributions!!", new Intent(this, UpdatesActivity.class), Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, tripTemp.getId());
						}
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
						String date=null;
						Expenseendpoint.Builder expenseBuilder = new Expenseendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
						expenseBuilder = CloudEndpointUtils.updateBuilder(expenseBuilder);
						Expenseendpoint expenseEndpoint = expenseBuilder.build();
						Distributionendpoint.Builder distBuilder = new Distributionendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
						distBuilder = CloudEndpointUtils.updateBuilder(distBuilder);
						Distributionendpoint distEndpoint = distBuilder.build();
						CollectionResponseDistribution distResponse=null;
						ArrayList<Distribution> arrDist=null;
						retTrip=endpoint.getTrip(lngTripId).execute();
						List<Long> listUsersTemp=retTrip.getUserIDs();
						String strUserTemp=null;
						StringBuffer sbUsers=new StringBuffer(String.valueOf(lngUserId));

						if(listUsersTemp==null){
							listUsersTemp=new ArrayList<Long>();
						}
						if(!listUsersTemp.contains(lngUserId)){
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
								List<Long> userIds;
								List<String> amounts;
								String strUserIds, strAmounts;
								if(arrExpense!=null && arrExpense.size()!=0){
									for(Expense tempExpense:arrExpense){
										date = sdf.format(new Date(tempExpense.getCreationDate().getValue()));
										userIds=tempExpense.getExpenseUserIds();
										strUserIds=Global.listToString(userIds);

										amounts=tempExpense.getExpenseAmounts();
										strAmounts=Global.listToString(amounts);
										localDb.insertExpense(tempExpense.getName(), tempExpense.getId(), date, "INR", tempExpense.getAmount(), tempExpense.getDescription(), tempExpense.getTripId(), tempExpense.getUserId(), strUserIds, strAmounts, Constants.STR_SYNCHED);
									}
								}
							}
							distResponse=distEndpoint.listDistribution().setTripId(retTrip.getId()).execute();
							long rowId;
							if(distResponse!=null){
								arrDist=(ArrayList<Distribution>) distResponse.getItems();
								if(arrDist!=null && arrDist.size()!=0){
									for(Distribution tempDist:arrDist){
										date = sdf.format(new Date(tempDist.getCreationDate().getValue()));
										rowId=localDb.insertDistribution(tempDist.getFromId(), tempDist.getToId(), tempDist.getAmount(), tempDist.getTripId(), Constants.STR_YES, date);
										localDb.updateDistributionId(rowId, tempDist.getId());
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
						} else{
							localDb.deleteTrip(lngTripId);
						}
					}
					sendResult(RESULT_SYNC);
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
			Tripendpoint.Builder tripBuilder = new Tripendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
			tripBuilder = CloudEndpointUtils.updateBuilder(tripBuilder);
			Tripendpoint tripEndpoint = tripBuilder.build();
			List<ExpenseBean> arrExpensesNotSynched = localDb.retrieveNotSynchedExpenses();
			List<Long> userIds;
			List<Long> tripUserIds;
			Trip trip;
			for(ExpenseBean expenseTemp:arrExpensesNotSynched){
				expense = new Expense();
				try {
					userIds = Global.longToList(expenseTemp.getUserIds());
					trip=tripEndpoint.getTrip(expenseTemp.getTripId()).execute();
					if(trip!=null){
						tripUserIds=trip.getUserIDs();
						if(expenseTemp.getSyncStatus().equals(Constants.STR_UPDATED)){
							expense=endpoint.getExpense(expenseTemp.getId()).execute();
							if(tripUserIds.containsAll(userIds)){
								if(expense!=null){
									expense.setName(expenseTemp.getName());
									expense.setAmount(expenseTemp.getAmount());
									expense.setDescription(expenseTemp.getDesc());
									expense.setExpenseUserIds(userIds);
									expense.setExpenseAmounts(Global.stringToList(expenseTemp.getAmounts()));
									expense.setChangerId(localDb.retrieveDeviceId());
									expense=endpoint.updateExpense(expense).execute();
									localDb.updateExpenseSyncStatus(expenseTemp.getId(), expense.getId());
								}
							} else{
								localDb.updateExpense(expense.getName(), expense.getAmount(), expense.getDescription(), 
										Global.listToString(expense.getExpenseUserIds()), Global.listToString(expense.getExpenseAmounts()), 
										Constants.STR_SYNCHED, expense.getId());
								sendNotification(Constants.STR_NOT_UPDATED, "Expense Not Updated", "Expense "
										+expense.getName()+" of expense-group "+trip.getName()+" not updated "
										+ "as a user of the expense has exited the EG!!", new Intent(this, UpdatesActivity.class), 
										Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, trip.getId());
							}
						} else if(expenseTemp.getSyncStatus().equals(Constants.STR_DELETED)){
							if(tripUserIds.containsAll(userIds)){
								Expenseendpoint.Builder expenseBuilder = new Expenseendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
								expenseBuilder = CloudEndpointUtils.updateBuilder(expenseBuilder);
								Expenseendpoint expenseEndpoint = expenseBuilder.build();
								expenseEndpoint.removeExpense(expenseTemp.getId()).execute();
								localDb.deleteExpense(expenseTemp.getId());
							} else{
								localDb.updateExpenseSyncStatus(expenseTemp.getId());
								sendNotification(Constants.STR_NOT_DELETED, "Expense Not Deleted", "Expense "
										+expense.getName()+" of expense-group "+trip.getName()+" not deleted "
										+ "as a user of the expense has exited the EG!!", new Intent(this, UpdatesActivity.class), 
										Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, trip.getId());
							}
						} else if(expenseTemp.getSyncStatus().equals(Constants.STR_NOT_SYNCHED)){
							if(tripUserIds.containsAll(userIds)){
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
							} else{
								localDb.updateExpenseSyncStatus(expenseTemp.getId(), Constants.STR_ERROR_STATUS);
								sendNotification(Constants.STR_NOT_SYNCED, "Expense Not Synced", "Expense "
										+expense.getName()+" of expense-group "+trip.getName()+" not synced "
										+ "as a user of the expense has exited the EG!!", new Intent(this, UpdatesActivity.class), 
										Constants.NOTIFICATION_ID_TRIP, Constants.STR_GROUP, trip.getId());
							}
						}
					}
					sendResult(RESULT_SYNC);
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

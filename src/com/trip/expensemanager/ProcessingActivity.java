package com.trip.expensemanager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.trip.expensemanager.deviceinfoendpoint.Deviceinfoendpoint;
import com.trip.expensemanager.deviceinfoendpoint.model.CollectionResponseDeviceInfo;
import com.trip.expensemanager.deviceinfoendpoint.model.DeviceInfo;
import com.trip.expensemanager.distributionendpoint.Distributionendpoint;
import com.trip.expensemanager.distributionendpoint.model.CollectionResponseDistribution;
import com.trip.expensemanager.distributionendpoint.model.Distribution;
import com.trip.expensemanager.expenseendpoint.Expenseendpoint;
import com.trip.expensemanager.expenseendpoint.model.CollectionResponseExpense;
import com.trip.expensemanager.expenseendpoint.model.Expense;
import com.trip.expensemanager.loginendpoint.Loginendpoint;
import com.trip.expensemanager.loginendpoint.model.CollectionResponseLogIn;
import com.trip.expensemanager.loginendpoint.model.LogIn;
import com.trip.expensemanager.tripendpoint.Tripendpoint;
import com.trip.expensemanager.tripendpoint.model.Trip;
import com.trip.utils.Constants;
import com.trip.utils.Global;
import com.trip.utils.LocalDB;


public class ProcessingActivity extends Activity{

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static final String PROJECT_NUMBER = "28707109757";
	public static final String CLIENT_ID_TEST = "28707109757-1kku1pqd58ahpti4kgi5ocnjeuudfi0g.apps.googleusercontent.com";
	public static final String CLIENT_ID_PROD = "28707109757-e70uh0dndb8voo0qemqo9jpm06c0ng5p.apps.googleusercontent.com";
	private ProcessTask pt=null;
	private ImageView anim;
	Intent returnIntent, intent;
	private int iOpcode;
	private String strRegID;
	public Long lngUserId;
	private GoogleCloudMessaging gcm;
	final private char[] hexArray = "0123456789ABCDEF".toCharArray();
	private static final int AUTH_CODE_REQUEST_CODE = 1000;

	public void onCreate(Bundle savedInstanceState){
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_progress);
			intent=getIntent();
			iOpcode=intent.getIntExtra(Constants.STR_REQUEST,0);
			returnIntent=new Intent();
			setResult(RESULT_CANCELED,returnIntent);

			if(!isConnected()){
				Toast.makeText(getBaseContext(), "Please enable the internet connection to proceed", Toast.LENGTH_LONG).show();
				finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		try {
			anim=(ImageView) findViewById(R.id.pb);
			anim.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_anim));
		} catch (NotFoundException e) {
			returnIntent.putExtra(Constants.STR_RESULT,0);
			setResult(RESULT_CANCELED,returnIntent);
			finish();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		try {
			pt = new ProcessTask();
			pt.execute();
		} catch (Exception e) {
			returnIntent.putExtra(Constants.STR_RESULT,0);
			setResult(RESULT_CANCELED,returnIntent);
			finish();
		}
	}

	private boolean registerForGCM() {
		try {
			if(checkPlayServices()){
				if (gcm == null) {
					gcm = GoogleCloudMessaging.getInstance(this);
				}
				strRegID = gcm.register(PROJECT_NUMBER);
				return true;
			} else{
				returnIntent.putExtra(Constants.STR_RESULT,3);
				returnIntent.putExtra(Constants.STR_ERROR,"No valid Google Play Services APK found!!");
				setResult(RESULT_OK,returnIntent);
				finish();
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			try {
				if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
					GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
				} else {
					Log.i("Expense", "This device is not supported.");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}

	/*@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent.getBooleanExtra("gcmIntentServiceMessage", false)) {
			if (intent.getBooleanExtra("registrationMessage", false)) {
				if (intent.getBooleanExtra("error", false)) {
					returnIntent.putExtra(Constants.STR_RESULT,2);
					returnIntent.putExtra(Constants.STR_ERROR,"GCM registration failed!!");
					setResult(RESULT_OK,returnIntent);
				} else{
					strRegID=intent.getStringExtra("regID");
					if(strRegID!=null){
						pt = new ProcessTask();
						pt.execute();
					} else{
						returnIntent.putExtra(Constants.STR_RESULT,2);
						returnIntent.putExtra(Constants.STR_ERROR,"GCM registration failed!!");
						setResult(RESULT_OK,returnIntent);
					}
				}
			} else if((strRegID=intent.getStringExtra("regID"))!=null){
				returnIntent.putExtra(Constants.STR_RESULT,2);
				returnIntent.putExtra(Constants.STR_ERROR,"GCM registration failed!!");
				setResult(RESULT_OK,returnIntent);
			}
		}
	}*/

	public void onBackPressed(){
		try {
			LocalDB localDb=new LocalDB(getApplicationContext());
			localDb.clearAllTables();
			finish();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onDestroy(){
		try {
			super.onDestroy();
			if(pt!=null && !pt.isCancelled()){
				pt.cancel(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class ProcessTask extends AsyncTask<String, String, String> {


		private static final String NAME_KEY = "given_name";
		private String strToken;

		@Override
		protected void onPreExecute() {
			try {
				super.onPreExecute();

			} catch (Exception e) {
				returnIntent.putExtra(Constants.STR_RESULT, 0);
				setResult(RESULT_CANCELED,returnIntent);
				finish();
			}
		}

		protected String doInBackground(String... strRequest) {
			String result=null;
			try {
				boolean registered=false;
				registered=registerForGCM();
				if(CloudEndpointUtils.LOCAL_ANDROID_RUN){
					registered=true;
					strRegID="ABCDE";
				}
				if(registered){
					switch (iOpcode) {
					case Constants.I_OPCODE_REGISTER:
						result=registerUser();
						break;

					case Constants.I_OPCODE_LOGIN:
						result=logUserIn();
						break;

					case Constants.I_OPCODE_GOOGLE:
						result=googleLogIn();
						break;

					default:
						break;
					}
				} else{
					returnIntent.putExtra(Constants.STR_RESULT,2);
					returnIntent.putExtra(Constants.STR_ERROR,"GCM registration failed!!");
					setResult(RESULT_OK,returnIntent);
					cancel(true);
					finish();
				}
			} catch (Exception e) {
				cancel(true);
			}
			return result;
		}

		protected void onPostExecute(String result) {
			try {
				switch (iOpcode) {
				case Constants.I_OPCODE_REGISTER:
					if(result!=null && result.equals(Constants.STR_SUCCESS)){
						returnIntent.putExtra(Constants.STR_RESULT,1);
						returnIntent.putExtra(Constants.STR_USER_ID,lngUserId);
						returnIntent.putExtra(Constants.STR_ERROR,"Registered succesfully!!");
						setResult(RESULT_OK,returnIntent);
					} else {
						returnIntent.putExtra(Constants.STR_RESULT,2);
						returnIntent.putExtra(Constants.STR_ERROR,"Something went wrong!!");
						setResult(RESULT_OK,returnIntent);
					}
					finish();
				case Constants.I_OPCODE_LOGIN:
					if(result!=null && result.equals(Constants.STR_SUCCESS)){
						returnIntent.putExtra(Constants.STR_RESULT,1);
						returnIntent.putExtra(Constants.STR_USER_ID,lngUserId);
						returnIntent.putExtra(Constants.STR_ERROR,"Login succesful!!");
						setResult(RESULT_OK,returnIntent);
					} else {
						returnIntent.putExtra(Constants.STR_RESULT,2);
						returnIntent.putExtra(Constants.STR_ERROR,"Something went wrong!!");
						setResult(RESULT_OK,returnIntent);
					}
					finish();
				case Constants.I_OPCODE_GOOGLE:
					if(result!=null && result.equals(Constants.STR_SUCCESS)){
						returnIntent.putExtra(Constants.STR_RESULT,1);
						returnIntent.putExtra(Constants.STR_USER_ID,lngUserId);
						returnIntent.putExtra(Constants.STR_ERROR,"Login succesful!!");
						setResult(RESULT_OK,returnIntent);
						finish();
					} else if(result!=null){
						returnIntent.putExtra(Constants.STR_RESULT,2);
						returnIntent.putExtra(Constants.STR_ERROR,"Something went wrong!! Please try again");
						setResult(RESULT_OK,returnIntent);
						finish();
					}
				default:
					break;
				}
			} catch (Exception e) {
				returnIntent.putExtra(Constants.STR_RESULT,0);
				returnIntent.putExtra(Constants.STR_ERROR,"Something went wrong!!");
				setResult(RESULT_OK,returnIntent);
				finish();
			}
		}

		private String googleLogIn() {
			String result=Constants.STR_FAILURE;
			String[] strArrData=intent.getStringArrayExtra(Constants.STR_DATA);
			String strEmail=strArrData[0];
			List<Long> listDevs=new ArrayList<Long>();
			List<Long> listTripIds=new ArrayList<Long>();
			LogIn login = new LogIn();
			LogIn retLogin=null;
			DeviceInfo devInfo=new DeviceInfo();
			DeviceInfo retDevInfo=null;
			Trip retTrip=null;
			LocalDB localDb=new LocalDB(getApplicationContext());
			HttpURLConnection con = null;
			try {
				strToken=fetchToken(strEmail);
				if(strToken==null){
					return null;
				} else if(strToken.equals(Constants.STR_FAILURE)){
					return strToken;
				}
				URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + strToken);
				con = (HttpURLConnection) url.openConnection();
				int sc = con.getResponseCode();
				if (sc == 200) {
					InputStream is = con.getInputStream();
					String strName = getFirstName(readResponse(is));
					if(strName!=null){
						strName=strName.substring(0,1).toUpperCase()+strName.substring(1);
					} else{
						strName=strEmail.substring(0, strEmail.indexOf('@'));
					}
					long userIdTemp=localDb.retrieve();
					if(userIdTemp!=0L){
						Loginendpoint.Builder builder = new Loginendpoint.Builder(
								AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
						builder = CloudEndpointUtils.updateBuilder(builder);
						Loginendpoint endpoint = builder.build();
						Deviceinfoendpoint.Builder devInfoBuilder = new Deviceinfoendpoint.Builder(
								AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
						devInfoBuilder = CloudEndpointUtils.updateBuilder(devInfoBuilder);
						Deviceinfoendpoint devInfoEndpoint = devInfoBuilder.build();
						CollectionResponseLogIn entities = endpoint.listLogIn().setUsername(strEmail).execute();
						String strDeviceName = getDeviceName();
						if (entities == null || entities.getItems() == null || entities.getItems().size() < 1) {
							if(entities==null){
								returnIntent.putExtra(Constants.STR_RESULT,0);
								returnIntent.putExtra(Constants.STR_ERROR,"Something went wrong!!");
								setResult(RESULT_OK,returnIntent);
								cancel(true);
								finish();
							} else{
								String strHashPwd = null;
								login.setUsername(strEmail);
								login.setPassword(strHashPwd);
								login.setPrefferedName(strName);
								CollectionResponseDeviceInfo devInfoEntities = devInfoEndpoint.listDeviceInfo().setGcmRegId(strRegID).execute();
								if (devInfoEntities == null || devInfoEntities.getItems() == null || devInfoEntities.getItems().size() < 1) {
									devInfo.setGcmRegId(strRegID);
									devInfo.setMake(strDeviceName);
									retDevInfo=devInfoEndpoint.insertDeviceInfo(devInfo).execute();
								} else{
									List<DeviceInfo> collDevInfo = devInfoEntities.getItems();
									retDevInfo=collDevInfo.get(0);
								}
								listDevs.add(retDevInfo.getId());
								login.setDeviceIDs(listDevs);
								retLogin=endpoint.insertLogIn(login).execute();
								lngUserId=retLogin.getId();
								localDb.insert(lngUserId, retLogin.getUsername(), "", Constants.STR_YOU, retDevInfo.getId());
								localDb.insertPerson(lngUserId, retLogin.getUsername(), Constants.STR_YOU);
							}
						} else{
							login=entities.getItems().get(0);

							CollectionResponseDeviceInfo devInfoEntities = devInfoEndpoint.listDeviceInfo().setGcmRegId(strRegID).execute();
							if (devInfoEntities == null || devInfoEntities.getItems() == null || devInfoEntities.getItems().size() < 1) {
								devInfo.setGcmRegId(strRegID);
								devInfo.setMake(strDeviceName);
								retDevInfo=devInfoEndpoint.insertDeviceInfo(devInfo).execute();
							} else{
								List<DeviceInfo> collDevInfo = devInfoEntities.getItems();
								retDevInfo=collDevInfo.get(0);
							}
							listDevs=login.getDeviceIDs();
							if(!listDevs.contains(retDevInfo.getId())){
								listDevs.add(retDevInfo.getId());
							}
							login.setDeviceIDs(listDevs);
							lngUserId=login.getId();
							localDb.insert(lngUserId, login.getUsername(), login.getPassword(), Constants.STR_YOU, retDevInfo.getId());
							localDb.insertPerson(lngUserId, strArrData[0], Constants.STR_YOU);
							listTripIds=login.getTripIDs();
							int i=0;
							Tripendpoint.Builder tripBuilder = new Tripendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
							tripBuilder = CloudEndpointUtils.updateBuilder(tripBuilder);
							Tripendpoint tripEndpoint = tripBuilder.build();
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
							String date=null;

							Expenseendpoint.Builder expenseBuilder = new Expenseendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
							expenseBuilder = CloudEndpointUtils.updateBuilder(expenseBuilder);
							Expenseendpoint expenseEndpoint = expenseBuilder.build();
							CollectionResponseExpense response=null;
							ArrayList<Expense> arrExpense=null;

							Distributionendpoint.Builder distBuilder = new Distributionendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
							distBuilder = CloudEndpointUtils.updateBuilder(distBuilder);
							Distributionendpoint distEndpoint = distBuilder.build();
							CollectionResponseDistribution distResponse=null;
							ArrayList<Distribution> arrDist=null;

							List<Long> listUserIdsTemp=null;
							long rowId;
							while (listTripIds!=null && i<listTripIds.size()) {
								retTrip=tripEndpoint.getTrip(listTripIds.get(i++)).execute();
								if(retTrip!=null){
									date = sdf.format(new Date(retTrip.getCreationDate().getValue()));
									listUserIdsTemp=retTrip.getUserIDs();
									long lngAdmin=retTrip.getAdmin();
									StringBuffer sbUsers=new StringBuffer();
									Loginendpoint.Builder loginBuilder = new Loginendpoint.Builder(
											AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
									loginBuilder = CloudEndpointUtils.updateBuilder(loginBuilder);
									Loginendpoint loginEndpoint = loginBuilder.build();
									String username=null;
									for(long userId:listUserIdsTemp){
										username=localDb.retrieveUsername(userId);
										if(username==null){
											retLogin=loginEndpoint.getLogIn(userId).execute();
											username=retLogin.getUsername();
											localDb.insertPerson(userId, username, retLogin.getPrefferedName());
										}
										sbUsers.append(userId+",");
									}
									sbUsers.delete(sbUsers.length()-1, sbUsers.length());
									localDb.insertTrip(retTrip.getName(), retTrip.getId(), date, sbUsers.toString(), lngAdmin, Constants.STR_SYNCHED);
									response=expenseEndpoint.listExpense().setTripId(retTrip.getId()).execute();
									if(response!=null){
										arrExpense=(ArrayList<Expense>) response.getItems();
										if(arrExpense!=null && arrExpense.size()!=0){
											List<Long> userIds;
											List<String> amounts;
											String strUserIds, strAmounts;
											for(Expense tempExpense:arrExpense){
												date = sdf.format(new Date(tempExpense.getCreationDate().getValue()));
												userIds=tempExpense.getExpenseUserIds();
												strUserIds=Global.listToString(userIds);
												if(!userIds.contains(tempExpense.getUserId())){
													userIds.add(tempExpense.getUserId());
												}
												for(long userId:userIds){
													username=localDb.retrieveUsername(userId);
													if(username==null){
														retLogin=loginEndpoint.getLogIn(userId).execute();
														username=retLogin.getUsername();
														localDb.insertPerson(userId, username, retLogin.getPrefferedName());
													}
												}
												amounts=tempExpense.getExpenseAmounts();
												strAmounts=Global.listToString(amounts);
												localDb.insertExpense(tempExpense.getName(), tempExpense.getId(), date, "INR", tempExpense.getAmount(), tempExpense.getDescription(), tempExpense.getTripId(), tempExpense.getUserId(), strUserIds, strAmounts, Constants.STR_SYNCHED);
											}
										}
									}

									distResponse=distEndpoint.listDistribution().setTripId(retTrip.getId()).execute();
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
								}
							}
							endpoint.updateLogIn(login).execute();
						}
					} else{
						cancel(true);
						finish();
					}
					result=Constants.STR_SUCCESS;
				} else if (sc == 401) {
					GoogleAuthUtil.invalidateToken(getApplicationContext(), strToken);
					return result;
				}
			} catch (IOException e) {
				e.printStackTrace();
				localDb.clearAllTables();
			} catch (JSONException e) {
				e.printStackTrace();
				localDb.clearAllTables();
			} catch(Exception e){
				e.printStackTrace();
				localDb.clearAllTables();
			} finally{
				if(con!=null){
					con.disconnect();
				}
			}
			return result;
		}

		private String readResponse(InputStream is) throws IOException {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] data = new byte[2048];
			int len = 0;
			while ((len = is.read(data, 0, data.length)) >= 0) {
				bos.write(data, 0, len);
			}
			return new String(bos.toByteArray(), "UTF-8");
		}

		private String getFirstName(String jsonResponse) throws JSONException {
			JSONObject profile = new JSONObject(jsonResponse);
			return profile.getString(NAME_KEY);
		}

		protected String fetchToken(String mEmail) throws IOException {
			try {
				//				String scope = String.format("oauth2:server:client_id:%s:api_scope:%s", "28707109757-1kku1pqd58ahpti4kgi5ocnjeuudfi0g.apps.googleusercontent.com", "https://www.googleapis.com/auth/plus.login");
				String mScope="oauth2:" + Scopes.PROFILE;
				return GoogleAuthUtil.getToken(getApplicationContext(), mEmail, mScope);
			} catch (UserRecoverableAuthException userRecoverableException) {
				userRecoverableException.printStackTrace();
				startActivityForResult(userRecoverableException.getIntent(), AUTH_CODE_REQUEST_CODE);
			} catch (GoogleAuthException fatalException) {
				fatalException.printStackTrace();
				return Constants.STR_FAILURE;
			}
			return null;
		}

		private String registerUser() {
			String result=Constants.STR_SUCCESS;
			String strHashPwd, strDeviceName;
			String[] strArrData=intent.getStringArrayExtra(Constants.STR_DATA);
			List<Long> listTrip=new ArrayList<Long>();
			List<Long> listDevs=new ArrayList<Long>();
			LogIn login = new LogIn();
			LogIn retLogin=null;
			DeviceInfo devInfo=new DeviceInfo();
			DeviceInfo retDevInfo=null;
			login.setUsername(strArrData[0]);
			//			login.setPassword(strArrData[1]);
			//			login.setRegId(strRegID);
			login.setTripIDs(listTrip);
			LocalDB localDb=new LocalDB(getApplicationContext());
			try {
				strHashPwd=sha1Hash(strArrData[1]);
				login.setPassword(strHashPwd);
				login.setPrefferedName(strArrData[2]);
				strDeviceName=getDeviceName();
				Loginendpoint.Builder builder = new Loginendpoint.Builder(
						AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
				builder = CloudEndpointUtils.updateBuilder(builder);
				Loginendpoint endpoint = builder.build();
				Deviceinfoendpoint.Builder devInfoBuilder = new Deviceinfoendpoint.Builder(
						AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
				devInfoBuilder = CloudEndpointUtils.updateBuilder(devInfoBuilder);
				Deviceinfoendpoint devInfoEndpoint = devInfoBuilder.build();
				CollectionResponseLogIn entities = endpoint.listLogIn().setUsername(strArrData[0]).execute();
				if (entities == null || entities.getItems() == null || entities.getItems().size() < 1) {
					if(entities==null){
						returnIntent.putExtra(Constants.STR_RESULT,0);
						returnIntent.putExtra(Constants.STR_ERROR,"Something went wrong!!");
						setResult(RESULT_OK,returnIntent);
						cancel(true);
						finish();
					} else{
						devInfo.setGcmRegId(strRegID);
						devInfo.setMake(strDeviceName);
						retDevInfo=devInfoEndpoint.insertDeviceInfo(devInfo).execute();
						listDevs.add(retDevInfo.getId());
						login.setDeviceIDs(listDevs);
						retLogin=endpoint.insertLogIn(login).execute();
						lngUserId=retLogin.getId();
						localDb.insert(lngUserId, retLogin.getUsername(), retLogin.getPassword(), Constants.STR_YOU, retDevInfo.getId());
						localDb.insertPerson(lngUserId, retLogin.getUsername(), Constants.STR_YOU);
					}
				} else{
					returnIntent.putExtra(Constants.STR_RESULT,2);
					returnIntent.putExtra(Constants.STR_ERROR,"Username exists!!");
					setResult(RESULT_OK,returnIntent);
					cancel(true);
					finish();
				}
			} catch (IOException e) {
				e.printStackTrace();
				localDb.clearAllTables();
				return null;
			}
			return result;
		}

		private String logUserIn() {
			String result=Constants.STR_SUCCESS;
			String strHashPwd, strDeviceName;
			String[] strArrData=intent.getStringArrayExtra(Constants.STR_DATA);
			List<Long> listDevs=new ArrayList<Long>();
			List<Long> listTripIds=new ArrayList<Long>();
			LogIn login = new LogIn();
			LogIn retLogin=null;
			DeviceInfo devInfo=new DeviceInfo();
			DeviceInfo retDevInfo=null;
			Trip retTrip=null;
			LocalDB localDb=new LocalDB(getApplicationContext());
			try {
				strDeviceName=getDeviceName();
				Loginendpoint.Builder builder = new Loginendpoint.Builder(
						AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
				builder = CloudEndpointUtils.updateBuilder(builder);
				Loginendpoint endpoint = builder.build();
				Deviceinfoendpoint.Builder devInfoBuilder = new Deviceinfoendpoint.Builder(
						AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
				devInfoBuilder = CloudEndpointUtils.updateBuilder(devInfoBuilder);
				Deviceinfoendpoint devInfoEndpoint = devInfoBuilder.build();
				CollectionResponseLogIn entities = endpoint.listLogIn().setUsername(strArrData[0]).execute();
				if (entities == null || entities.getItems() == null || entities.getItems().size() < 1) {
					returnIntent.putExtra(Constants.STR_RESULT,0);
					returnIntent.putExtra(Constants.STR_ERROR,"Wrong username or password!!");
					setResult(RESULT_OK,returnIntent);
					cancel(true);
					finish();
				} else{
					login=entities.getItems().get(0);
					strHashPwd=sha1Hash(strArrData[1]);
					if(!strHashPwd.equals(login.getPassword())){
						returnIntent.putExtra(Constants.STR_RESULT,0);
						returnIntent.putExtra(Constants.STR_ERROR,"Wrong username or password!!");
						setResult(RESULT_OK,returnIntent);
						cancel(true);
						finish();
					} else{
						CollectionResponseDeviceInfo devInfoEntities = devInfoEndpoint.listDeviceInfo().setGcmRegId(strRegID).execute();
						if (devInfoEntities == null || devInfoEntities.getItems() == null || devInfoEntities.getItems().size() < 1) {
							devInfo.setGcmRegId(strRegID);
							devInfo.setMake(strDeviceName);
							retDevInfo=devInfoEndpoint.insertDeviceInfo(devInfo).execute();
						} else{
							List<DeviceInfo> collDevInfo = devInfoEntities.getItems();
							retDevInfo=collDevInfo.get(0);
						}
						listDevs=login.getDeviceIDs();
						if(!listDevs.contains(retDevInfo.getId())){
							listDevs.add(retDevInfo.getId());
						}
						login.setDeviceIDs(listDevs);
						lngUserId=login.getId();
						localDb.insert(lngUserId, login.getUsername(), login.getPassword(), Constants.STR_YOU, retDevInfo.getId());
						localDb.insertPerson(lngUserId, strArrData[0], Constants.STR_YOU);
						listTripIds=login.getTripIDs();
						int i=0;
						Tripendpoint.Builder tripBuilder = new Tripendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
						tripBuilder = CloudEndpointUtils.updateBuilder(tripBuilder);
						Tripendpoint tripEndpoint = tripBuilder.build();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
						String date=null;

						Expenseendpoint.Builder expenseBuilder = new Expenseendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
						expenseBuilder = CloudEndpointUtils.updateBuilder(expenseBuilder);
						Expenseendpoint expenseEndpoint = expenseBuilder.build();
						CollectionResponseExpense response=null;
						ArrayList<Expense> arrExpense=null;

						Distributionendpoint.Builder distBuilder = new Distributionendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
						distBuilder = CloudEndpointUtils.updateBuilder(distBuilder);
						Distributionendpoint distEndpoint = distBuilder.build();
						CollectionResponseDistribution distResponse=null;
						ArrayList<Distribution> arrDist=null;

						List<Long> listUserIdsTemp=null;
						long rowId;
						while (listTripIds!=null && i<listTripIds.size()) {
							retTrip=tripEndpoint.getTrip(listTripIds.get(i++)).execute();
							if(retTrip!=null){
								date = sdf.format(new Date(retTrip.getCreationDate().getValue()));
								listUserIdsTemp=retTrip.getUserIDs();
								long lngAdmin=retTrip.getAdmin();
								StringBuffer sbUsers=new StringBuffer();
								Loginendpoint.Builder loginBuilder = new Loginendpoint.Builder(
										AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
								loginBuilder = CloudEndpointUtils.updateBuilder(loginBuilder);
								Loginendpoint loginEndpoint = loginBuilder.build();
								String username=null;
								for(long userId:listUserIdsTemp){
									username=localDb.retrieveUsername(userId);
									if(username==null){
										retLogin=loginEndpoint.getLogIn(userId).execute();
										username=retLogin.getUsername();
										localDb.insertPerson(userId, username, retLogin.getPrefferedName());
									}
									sbUsers.append(userId+",");
								}
								sbUsers.delete(sbUsers.length()-1, sbUsers.length());
								localDb.insertTrip(retTrip.getName(), retTrip.getId(), date, sbUsers.toString(), lngAdmin, Constants.STR_SYNCHED);
								response=expenseEndpoint.listExpense().setTripId(retTrip.getId()).execute();
								if(response!=null){
									arrExpense=(ArrayList<Expense>) response.getItems();
									if(arrExpense!=null && arrExpense.size()!=0){
										List<Long> userIds;
										List<String> amounts;
										String strUserIds, strAmounts;
										for(Expense tempExpense:arrExpense){
											date = sdf.format(new Date(tempExpense.getCreationDate().getValue()));
											userIds=tempExpense.getExpenseUserIds();
											strUserIds=Global.listToString(userIds);
											if(!userIds.contains(tempExpense.getUserId())){
												userIds.add(tempExpense.getUserId());
											}
											for(long userId:userIds){
												username=localDb.retrieveUsername(userId);
												if(username==null){
													retLogin=loginEndpoint.getLogIn(userId).execute();
													username=retLogin.getUsername();
													localDb.insertPerson(userId, username, retLogin.getPrefferedName());
												}
											}
											amounts=tempExpense.getExpenseAmounts();
											strAmounts=Global.listToString(amounts);
											localDb.insertExpense(tempExpense.getName(), tempExpense.getId(), date, "INR", tempExpense.getAmount(), tempExpense.getDescription(), tempExpense.getTripId(), tempExpense.getUserId(), strUserIds, strAmounts, Constants.STR_SYNCHED);

											//											distributeExpense(tempExpense);
										}
									}
								}

								distResponse=distEndpoint.listDistribution().setTripId(retTrip.getId()).execute();
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
							}
						}
						endpoint.updateLogIn(login).execute();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				localDb.clearAllTables();
				return null;
			}
			return result;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == AUTH_CODE_REQUEST_CODE && resultCode == RESULT_OK) {
			//	        Bundle extra = intent.getExtras();
			//	        String oneTimeToken = extra.getString("authtoken");
			pt.cancel(true);
			pt = new ProcessTask();
			pt.execute();
		}
	}

	public String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return model;
		} else {
			return manufacturer + " " + model;
		}
	}

	private String sha1Hash(String toHash)
	{
		String hash = null;
		try
		{
			toHash=toHash+"TEM";
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] bytes = toHash.getBytes("UTF-8");
			digest.update(bytes, 0, bytes.length);
			bytes = digest.digest();

			hash = bytesToHex(bytes);
		}
		catch( NoSuchAlgorithmException e )
		{
			e.printStackTrace();
		}
		catch( UnsupportedEncodingException e )
		{
			e.printStackTrace();
		}
		return hash;
	}

	private String bytesToHex( byte[] bytes )
	{
		char[] hexChars = new char[ bytes.length * 2 ];
		for( int j = 0; j < bytes.length; j++ )
		{
			int v = bytes[ j ] & 0xFF;
			hexChars[ j * 2 ] = hexArray[ v >>> 4 ];
			hexChars[ j * 2 + 1 ] = hexArray[ v & 0x0F ];
		}
		return new String( hexChars );
	}


	private boolean isConnected() throws Exception {
		boolean connected=false;
		try{
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			connected = cm.getActiveNetworkInfo().isConnectedOrConnecting();
		} catch (Exception e) {
			e.printStackTrace();
			connected = false;
		}
		return connected;
	}

}
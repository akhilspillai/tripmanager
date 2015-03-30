package com.trip.expensemanager.fragments;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.Scopes;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.trip.expensemanager.CloudEndpointUtils;
import com.trip.expensemanager.ExpenseActivity;
import com.trip.expensemanager.R;
import com.trip.expensemanager.deviceinfoendpoint.Deviceinfoendpoint;
import com.trip.expensemanager.deviceinfoendpoint.model.CollectionResponseDeviceInfo;
import com.trip.expensemanager.deviceinfoendpoint.model.DeviceInfo;
import com.trip.expensemanager.distributionendpoint.Distributionendpoint;
import com.trip.expensemanager.distributionendpoint.model.CollectionResponseDistribution;
import com.trip.expensemanager.distributionendpoint.model.Distribution;
import com.trip.expensemanager.expenseendpoint.Expenseendpoint;
import com.trip.expensemanager.expenseendpoint.model.CollectionResponseExpense;
import com.trip.expensemanager.expenseendpoint.model.Expense;
import com.trip.expensemanager.fragments.dialogs.InfoDialogListener;
import com.trip.expensemanager.fragments.dialogs.InformationFragment;
import com.trip.expensemanager.loginendpoint.Loginendpoint;
import com.trip.expensemanager.loginendpoint.model.CollectionResponseLogIn;
import com.trip.expensemanager.loginendpoint.model.LogIn;
import com.trip.expensemanager.tripendpoint.Tripendpoint;
import com.trip.expensemanager.tripendpoint.model.Trip;
import com.trip.utils.Constants;
import com.trip.utils.Global;
import com.trip.utils.LocalDB;

public class NewUserFragment extends CustomFragment {

	public static NewUserFragment newInstance() {
		NewUserFragment fragment=null;
		try {
			fragment=new NewUserFragment();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fragment;
	}

	protected Animation bounceImage;
	protected Animation exitAnim;
	protected Animation enterAnim;
	protected boolean pwdChecked=false;
	protected boolean usernameChecked=false;
	protected boolean prefferedNameChecked;
	protected String strPrefferedName;
	private ProcessTask pt;
	private String strEmail;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View rootView=null;
		super.onCreateView(inflater, container, savedInstanceState);
		rootView= inflater.inflate(R.layout.fragment_new_user,container, false);
		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
		if(Global.isConnected(getActivity())){
			pickUserAccount();
		} else{

		}
	}

	private void pickUserAccount() {
		String[] accountTypes = new String[]{"com.google"};
		try {
			Intent intent = AccountPicker.newChooseAccountIntent(null, null,
					accountTypes, false, null, null, null, null);
			startActivityForResult(intent, Constants.REQUEST_CODE_PICK_ACCOUNT);
		} catch (Exception e) {
			showGMSNotFoundDialog();
		}
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.REQUEST_CODE_PICK_ACCOUNT) {
			if (resultCode == Activity.RESULT_OK) {
				strEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				if(pt!=null){
					pt.cancel(true);
				}
				pt = new ProcessTask();
				pt.execute(strEmail);
			} else if (resultCode == Activity.RESULT_CANCELED) {
				showErrorDialog(Constants.STR_ERR_NO_ACCTS);
			}
		} else if (requestCode == Constants.AUTH_CODE_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				if(pt!=null){
					pt.cancel(true);
				}
				pt = new ProcessTask();
				pt.execute(strEmail);
			}
		}

	}

	private void showErrorDialog(String strMessage) {
		InfoDialogListener listener=new InfoDialogListener() {

			public void onDialogButtonClick(DialogFragment dialog) {
				getActivity().finish();
				dialog.dismiss();
			}
		};
		InformationFragment.newInstance("Error", strMessage, null, R.layout.fragment_dialog_info, listener).show(getActivity().getSupportFragmentManager(), "dialog");
	}



	private class ProcessTask extends AsyncTask<String, String, String> {


		private static final String NAME_KEY = "given_name";
		private String strRegID;

		@Override
		protected void onPreExecute() {

		}

		protected String doInBackground(String... strRequest) {
			String result=null;
			try {
				result=googleLogIn(strRequest[0]);
			} catch (Exception e) {
				cancel(true);
			}
			return result;
		}

		protected void onPostExecute(String result) {
			if(result!null)
			if(result.equals(Constants.STR_SUCCESS)){
				//				Intent intent=new Intent(getActivity(), ExpenseActivity.class);
				//				startActivity(intent);
			} else{
				showErrorDialog(Constants.STR_ERR_FETCH_ACCT);
			}
		}

		private String googleLogIn(String strEmail) {
			String result=Constants.STR_FAILURE;
			List<Long> listDevs=new ArrayList<Long>();
			List<Long> listTripIds=new ArrayList<Long>();
			LogIn login = new LogIn();
			LogIn retLogin=null;
			DeviceInfo devInfo=new DeviceInfo();
			DeviceInfo retDevInfo=null;
			Trip retTrip=null;
			LocalDB localDb=new LocalDB(getActivity());
			HttpURLConnection con = null;
			long lngUserId;
			String strToken;
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
					/*Loginendpoint.Builder builder = new Loginendpoint.Builder(
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
							showErrorDialog(Constants.STR_ERR_FETCH_ACCT);
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
						localDb.insertPerson(lngUserId, strEmail, Constants.STR_YOU);
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
					}*/
					result=Constants.STR_SUCCESS;
				} else if (sc == 401) {
					GoogleAuthUtil.invalidateToken(getActivity(), strToken);
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

		private String getDeviceName() {
			String manufacturer = Build.MANUFACTURER;
			String model = Build.MODEL;
			if (model.startsWith(manufacturer)) {
				return model;
			} else {
				return manufacturer + " " + model;
			}
		}

		private String fetchToken(String strEmail) throws IOException {
			try {
				String mScope="oauth2:" + Scopes.PROFILE;
				return GoogleAuthUtil.getToken(getActivity(), strEmail, mScope);
			} catch (UserRecoverableAuthException userRecoverableException) {
				userRecoverableException.printStackTrace();
				startActivityForResult(userRecoverableException.getIntent(), Constants.AUTH_CODE_REQUEST_CODE);
			} catch (GoogleAuthException fatalException) {
				fatalException.printStackTrace();
				return Constants.STR_FAILURE;
			}
			return null;
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


	}
}
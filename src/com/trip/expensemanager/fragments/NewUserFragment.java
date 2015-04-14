package com.trip.expensemanager.fragments;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.trip.expensemanager.CloudEndpointUtils;
import com.trip.expensemanager.ExpenseActivity;
import com.trip.expensemanager.R;
import com.trip.expensemanager.deviceinfoendpoint.Deviceinfoendpoint;
import com.trip.expensemanager.deviceinfoendpoint.model.CollectionResponseDeviceInfo;
import com.trip.expensemanager.deviceinfoendpoint.model.DeviceInfo;
import com.trip.expensemanager.fragments.dialogs.ConfirmDialogListener;
import com.trip.expensemanager.fragments.dialogs.ConfirmationFragment;
import com.trip.expensemanager.fragments.dialogs.ThreeButtonDialogListener;
import com.trip.expensemanager.fragments.dialogs.ThreeButtonFragment;
import com.trip.expensemanager.loginendpoint.Loginendpoint;
import com.trip.expensemanager.loginendpoint.model.CollectionResponseLogIn;
import com.trip.expensemanager.loginendpoint.model.LogIn;
import com.trip.utils.Constants;
import com.trip.utils.Global;
import com.trip.utils.LocalDB;
import com.trip.utils.billing.IabHelper;
import com.trip.utils.billing.IabResult;
import com.trip.utils.billing.Inventory;
import com.trip.utils.billing.Purchase;

public class NewUserFragment extends CustomFragment implements OnClickListener {

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
	private LoginTask lt;
	private String strEmail;
	private EditText etxtEmail;
	private EditText etxtName;
	private ScrollView sc;
	private RelativeLayout rl;
	private LinearLayout llExit;
	private LinearLayout llNext;
	private View anim;
	private RegisterTask rt;
	private IabHelper mHelper;
	private boolean isPurchaseReady=false;
	private String strUniqueString;
	public String strRegId;
	public String strDeviceName;
	private String strName;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View rootView=null;
		super.onCreateView(inflater, container, savedInstanceState);
		rootView= inflater.inflate(R.layout.fragment_new_user,container, false);

		anim= rootView.findViewById(R.id.anim_line);
		sc=(ScrollView) rootView.findViewById(R.id.sc);
		rl=(RelativeLayout) rootView.findViewById(R.id.footer);
		llExit=(LinearLayout) rootView.findViewById(R.id.ll_exit);
		llNext=(LinearLayout) rootView.findViewById(R.id.ll_next);
		etxtEmail=(EditText) rootView.findViewById(R.id.etxt_email);
		etxtName=(EditText) rootView.findViewById(R.id.etxt_name);
		llExit.setOnClickListener(this);
		llNext.setOnClickListener(this);
		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
		pickUserAccount();
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
				if(lt!=null){
					lt.cancel(true);
				}
				if(Global.isConnected(getActivity())){
					lt = new LoginTask();
					lt.execute(strEmail);
				} else{
					showErrorDialog("Seems like you dont have an internet connection. Please connect to continue. It wouldn't take long.");
				}
			} else if (resultCode == Activity.RESULT_CANCELED) {
				showErrorDialog(Constants.STR_ERR_NO_ACCTS);
			}
		} else if (requestCode == Constants.AUTH_CODE_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				if(lt!=null){
					lt.cancel(true);
				}
				lt = new LoginTask();
				lt.execute(strEmail);
			}
		}

	}

	private void showErrorDialog(String strMessage) {
		ConfirmDialogListener listener=new ConfirmDialogListener() {

			@Override
			public void onDialogPositiveClick(DialogFragment dialog) {
				pickUserAccount();
				dialog.dismiss();
			}

			@Override
			public void onDialogNegativeClick(DialogFragment dialog) {
				getActivity().finish();
				dialog.dismiss();
			}
		};
		ConfirmationFragment.newInstance("Error", strMessage, "Try Again", R.layout.fragment_dialog_confirm, listener).show(getActivity().getSupportFragmentManager(), "dialog");
	}

	private void showRegistrationError(String strMessage, final String strName) {
		ConfirmDialogListener listener=new ConfirmDialogListener() {

			@Override
			public void onDialogPositiveClick(DialogFragment dialog) {
				showDetailsForApproval(strName);
				dialog.dismiss();
			}

			@Override
			public void onDialogNegativeClick(DialogFragment dialog) {
				getActivity().finish();
				dialog.dismiss();
			}
		};
		ConfirmationFragment.newInstance("Error", strMessage, "Try Again", R.layout.fragment_dialog_info, listener).show(getActivity().getSupportFragmentManager(), "dialog");
	}

	private void showDetailsForApproval(String strName) {
		anim.setVisibility(View.INVISIBLE);
		rl.setVisibility(View.VISIBLE);
		sc.setVisibility(View.VISIBLE);
		etxtEmail.setText(strEmail);
		etxtName.setText(strName);
	}


	private class LoginTask extends AsyncTask<String, String, String> {


		private static final String NAME_KEY = "given_name";
		private long sleepTime=1000;
		private IabHelper mHelper;

		protected String doInBackground(String... strRequest) {
			String result=Constants.STR_FAILURE;
			checkIfPurchased();
			try {
				while(true){
					result=googleLogIn(strRequest[0]);
					if(result==null || !result.equals(Constants.STR_INVALIDATE)){
						break;
					} else{
						Thread.sleep(sleepTime*2);
					}
				}
			} catch (Exception e) {
				cancel(true);
			}
			return result;
		}

		private void checkIfPurchased() {
			String base64EncodedPublicKey=Constants.STR_LICENSE_1+
					Constants.STR_LICENSE_2+Constants.STR_LICENSE_3+
					Constants.STR_LICENSE_4+Constants.STR_LICENSE_5+
					Constants.STR_LICENSE_6+Constants.STR_LICENSE_7;

			mHelper = new IabHelper(getActivity(), base64EncodedPublicKey);

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

							SharedPreferences prefs = getActivity().getSharedPreferences(Constants.STR_PREFERENCE, Activity.MODE_PRIVATE);
							prefs.edit().putBoolean(Constants.STR_PURCHASED, mIsPremium).commit();
						}
					};

					mHelper.queryInventoryAsync(mGotInventoryListener);
				}
			});
		}

		protected void onPostExecute(String result) {
			if(result.equals(Constants.STR_FAILURE)){
				showErrorDialog(Constants.STR_ERR_FETCH_ACCT);
			} else if(!result.equals(Constants.STR_AUTH)){
				showDetailsForApproval(result);
			}

		}

		private String googleLogIn(String strEmail) {
			String result=Constants.STR_FAILURE;
			LocalDB localDb=new LocalDB(getActivity());
			HttpURLConnection con = null;
			String strToken;
			try {
				strToken=fetchToken(strEmail);
				if(strToken.equals(Constants.STR_FAILURE) || strToken.equals(Constants.STR_AUTH)){
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
					return strName;
				} else if (sc == 401) {
					GoogleAuthUtil.invalidateToken(getActivity(), strToken);
					return Constants.STR_INVALIDATE;
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

		private String fetchToken(String strEmail) throws IOException {
			try {
				String mScope="oauth2:" + Scopes.PROFILE;
				return GoogleAuthUtil.getToken(getActivity(), strEmail, mScope);
			} catch (UserRecoverableAuthException userRecoverableException) {
				userRecoverableException.printStackTrace();
				startActivityForResult(userRecoverableException.getIntent(), Constants.AUTH_CODE_REQUEST_CODE);
				return Constants.STR_AUTH;
			} catch (GoogleAuthException fatalException) {
				fatalException.printStackTrace();
				return Constants.STR_FAILURE;
			}

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

	@Override
	public void onClick(View v) {
		if(v.equals(llExit)){
			getActivity().finish();
		} else if(v.equals(llNext)){
			strName=etxtName.getText().toString();
			if(!"".equals(strName)){
				rt=new RegisterTask();
				rt.execute(strName);
			} else{
				showError(etxtName, "Cannot be empty!!");
			}
		}
	}

	private class RegisterTask extends AsyncTask<String, Void, String>{


		private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
		public static final String PROJECT_NUMBER = "28707109757";
		private GoogleCloudMessaging gcm;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			((ExpenseActivity)getActivity()).setCustomTitle(R.string.done);
			anim.setVisibility(View.VISIBLE);
			rl.setVisibility(View.INVISIBLE);
			sc.setVisibility(View.INVISIBLE);
		}

		@Override
		protected String doInBackground(String... params) {
			String result=Constants.STR_FAILURE;
			LocalDB localDb = new LocalDB(getActivity());
			long lngUserId=localDb.retrieve();
			if(lngUserId==0L){
				try {
					result=getUserId();
				} catch (IOException e) {
				}
			} else {
				try {
					result=updateLogin();
				} catch (IOException e) {
				}
			}
			return result;
		}

		private String updateLogin() throws IOException {
			String result=Constants.STR_FAILURE;
			LocalDB localDb=new LocalDB(getActivity());
			long lngUserId=localDb.retrieve();
			Loginendpoint.Builder loginBuilder = new Loginendpoint.Builder(
					AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
			loginBuilder = CloudEndpointUtils.updateBuilder(loginBuilder);
			Loginendpoint loginEndpoint = loginBuilder.build();
			LogIn login=loginEndpoint.getLogIn(lngUserId).execute();
			login.setUsername(strEmail);
			login=loginEndpoint.updateLogIn(login).execute();
			localDb.update(login.getUsername(), login.getId());
			localDb.updatePerson(login.getUsername(), login.getId());
			result=Constants.STR_SUCCESS;
			return result;
		}

		private String getUserId() throws IOException {
			String strResult=Constants.STR_FAILURE;
			LogIn login;
			DeviceInfo device;
			LocalDB localDb=new LocalDB(getActivity());
			Loginendpoint.Builder loginBuilder = new Loginendpoint.Builder(
					AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
			loginBuilder = CloudEndpointUtils.updateBuilder(loginBuilder);
			Loginendpoint loginEndpoint = loginBuilder.build();
			Deviceinfoendpoint.Builder devInfoBuilder = new Deviceinfoendpoint.Builder(
					AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
			devInfoBuilder = CloudEndpointUtils.updateBuilder(devInfoBuilder);
			Deviceinfoendpoint devInfoEndpoint = devInfoBuilder.build();
			strDeviceName = getDeviceName();
			strRegId=registerForGCM();
			if(strRegId==null){
				return strResult;
			}
			CollectionResponseLogIn loginEntities = loginEndpoint.listLogIn().setUsername(strEmail).execute();
			CollectionResponseDeviceInfo devInfoEntities = devInfoEndpoint.listDeviceInfo().setGcmRegId(strRegId).execute();
			if (loginEntities == null || loginEntities.getItems() == null || loginEntities.getItems().size() < 1) {
				if(loginEntities!=null){
					if (devInfoEntities == null || devInfoEntities.getItems() == null || devInfoEntities.getItems().size() < 1) {
						device=new DeviceInfo();
						device.setMake(strDeviceName);
						device.setGcmRegId(strRegId);
						device=devInfoEndpoint.insertDeviceInfo(device).execute();
					} else{
						device=devInfoEntities.getItems().get(0);
					}
					login=new LogIn();
					List<Long> devList=new ArrayList<Long>(1);
					devList.add(device.getId());
					login.setDeviceIDs(devList);
					login.setPrefferedName(strName);
					login.setUsername(strEmail);
					login=loginEndpoint.insertLogIn(login).execute();
					localDb.insert(login.getId(), login.getUsername(), login.getPassword(), Constants.STR_YOU, device.getId());
					localDb.insertPerson(login.getId(), login.getUsername(), Constants.STR_YOU);
					strResult=Constants.STR_SUCCESS;
				} 
			} else{
				login=loginEntities.getItems().get(0);
				if (devInfoEntities == null || devInfoEntities.getItems() == null || devInfoEntities.getItems().size() < 1) {
					device=new DeviceInfo();
					device.setMake(strDeviceName);
					device.setGcmRegId(strRegId);
					device=devInfoEndpoint.insertDeviceInfo(device).execute();
					if(isPurchased()){
						List<Long> lstDevIds=login.getDeviceIDs();
						if(lstDevIds==null){
							lstDevIds=new ArrayList<Long>();
						}
						lstDevIds.add(device.getId());
						login.setDeviceIDs(lstDevIds);
						login=loginEndpoint.updateLogIn(login).execute();
						localDb.insert(login.getId(), login.getUsername(), login.getPassword(), Constants.STR_YOU, device.getId());
						localDb.insertPerson(login.getId(), login.getUsername(), Constants.STR_YOU);
						strResult=Constants.STR_SYNC_NEEDED;
					} else{
						strResult=Constants.STR_NOT_PURCHASED;
					}
				} else{
					device=devInfoEntities.getItems().get(0);
					localDb.insert(login.getId(), login.getUsername(), login.getPassword(), Constants.STR_YOU, device.getId());
					localDb.insertPerson(login.getId(), login.getUsername(), Constants.STR_YOU);
					localDb.insert(login.getId(), login.getUsername(), login.getPassword(), Constants.STR_YOU, device.getId());
					localDb.insertPerson(login.getId(), login.getUsername(), Constants.STR_YOU);
					strResult=Constants.STR_SYNC_NEEDED;
				}

			}
			return strResult;
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

		private String registerForGCM() {
			String strRegId=null;
			int count=5;
			long sleepTime=100;
			if(checkPlayServices()){
				while(true){
					try{
						if (gcm == null) {
							gcm = GoogleCloudMessaging.getInstance(getActivity());
						}
						strRegId = gcm.register(PROJECT_NUMBER);
						break;
					} catch (IOException e) {
						if(--count==0){
							break;
						} else{
							try {
								Thread.sleep(sleepTime*2);
							} catch (InterruptedException e1) {

							}
						}
					}
				}
			} 
			return strRegId;
		}

		private boolean checkPlayServices() {
			int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
			if (resultCode != ConnectionResult.SUCCESS) {
				try {
					if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
						GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), PLAY_SERVICES_RESOLUTION_REQUEST).show();
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

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			ActionBarActivity context=(ActionBarActivity)getActivity();
			LocalDB localDb=new LocalDB(context);
			long lngUserId=localDb.retrieve();
			if(result.equals(Constants.STR_FAILURE)){
				showRegistrationError("Oops!! Something went wrong. Please try again.", strName);
			} else if(result.equals(Constants.STR_NOT_PURCHASED)){
				String strMessage=getActivity().getResources().getString(R.string.upgrade_features);
				strMessage=Constants.STR_LOGIN_DIFF_DEV+strMessage;
				showPurchaseOrNotDialog(strMessage);
			} else if(result.equals(Constants.STR_SYNC_NEEDED)){
//				Intent intent=new Intent(getActivity(), ExpenseActivity.class);
//				intent.putExtra(Constants.STR_SYNC_NEEDED, true);
//				getActivity().startActivity(intent);
//				getActivity().finish();
				context.setContentView(R.layout.activity_expense);
				Toolbar toolbar = (Toolbar) context.findViewById(R.id.toolbar);

				if (toolbar != null) {
					context.setSupportActionBar(toolbar);
				}
				context.getSupportFragmentManager().popBackStack(null,FragmentManager.POP_BACK_STACK_INCLUSIVE);
				context.getSupportFragmentManager().beginTransaction().replace(R.id.container, AddTripFragment.newInstance(lngUserId, true)).commit();
			} else if(result.equals(Constants.STR_SUCCESS)){
//				Intent intent=new Intent(getActivity(), ExpenseActivity.class);
//				intent.putExtra(Constants.STR_SYNC_NEEDED, false);
//				getActivity().startActivity(intent);
//				getActivity().finish();
				context.setContentView(R.layout.activity_expense);
				Toolbar toolbar = (Toolbar) context.findViewById(R.id.toolbar);

				if (toolbar != null) {
					context.setSupportActionBar(toolbar);
				}
				context.getSupportFragmentManager().popBackStack(null,FragmentManager.POP_BACK_STACK_INCLUSIVE);
				context.getSupportFragmentManager().beginTransaction().replace(R.id.container, AddTripFragment.newInstance(lngUserId, false)).commit();
			}
		}

	}

	private void showPurchaseOrNotDialog(String strMessage) {
		ThreeButtonDialogListener listener=new ThreeButtonDialogListener() {

			@Override
			public void onDialogFirstButtonClick(DialogFragment dialog) {
				purchaseItem();
			}

			@Override
			public void onDialogSecondButtonClick(DialogFragment dialog) {
				new PurchaseLoginTask().execute(false);
			}

			@Override
			public void onDialogThirdButtonClick(DialogFragment dialog) {
				getActivity().finish();
			}

		};
		ThreeButtonFragment.newInstance(strMessage, listener).show(getActivity().getSupportFragmentManager(), "dialog");
	}
	
	protected void purchaseItem() {
		if(!isPurchaseReady){
			String base64EncodedPublicKey=Constants.STR_LICENSE_1+
					Constants.STR_LICENSE_2+Constants.STR_LICENSE_3+
					Constants.STR_LICENSE_4+Constants.STR_LICENSE_5+
					Constants.STR_LICENSE_6+Constants.STR_LICENSE_7;

			mHelper = new IabHelper(getActivity(), base64EncodedPublicKey);
			mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
				public void onIabSetupFinished(IabResult result) {
					if (!result.isSuccess()) {
						Log.d("Akhil", "Problem setting up In-app Billing: " + result);
						return;
					}
					isPurchaseReady=true;
					continuePurchase();
				}
			});
		} else{
			continuePurchase();
		}
	}

	private void continuePurchase(){
		IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener 
		= new IabHelper.OnIabPurchaseFinishedListener() {
			public void onIabPurchaseFinished(IabResult result, Purchase purchase) 
			{
				if (result.isFailure()) {
					Log.d("Akhil", "Error purchasing: " + result);
					showMessage("There was an error in purchasing! Please try again.");
					return;
				}      
				else if (purchase.getSku().equals(Constants.STR_SKU_PREMIUM)) {
					String strUniqueReturn=purchase.getDeveloperPayload();
					if(strUniqueReturn!=null && strUniqueReturn.equals(strUniqueString)){
						if(Global.isConnected(getActivity())){
							new PurchaseLoginTask().execute(true);
						}
					} else{
						showMessage("There was an error in purchasing! Please try again.");
					}
				}
			}
		};
		strUniqueString=Global.randomString(25);
		if (mHelper != null) mHelper.flagEndAsync();
		mHelper.launchPurchaseFlow(getActivity(), Constants.STR_SKU_PREMIUM, Constants.ORDER_ID,   
				mPurchaseFinishedListener, strUniqueString);
	}
	
	private class PurchaseLoginTask extends AsyncTask<Boolean, Void, String>{

		@Override
		protected String doInBackground(Boolean... params) {
			long sleepTime=100L;
			String result=null;
			while (true) {
				try {
					if(!Global.isConnected(getActivity())){
						break;
					}
					result=updateLoginDetails(params[0]);
				} catch (IOException e) {
					try {
						Thread.sleep(sleepTime*2);
					} catch (InterruptedException e1) {

					}
				}
			}
			return result;
		}

		private String updateLoginDetails(boolean isPurchased) throws IOException {
			String result=Constants.STR_FAILURE;
			LocalDB localDb=new LocalDB(getActivity());
			Loginendpoint.Builder loginBuilder = new Loginendpoint.Builder(
					AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
			loginBuilder = CloudEndpointUtils.updateBuilder(loginBuilder);
			Loginendpoint loginEndpoint = loginBuilder.build();
			Deviceinfoendpoint.Builder devInfoBuilder = new Deviceinfoendpoint.Builder(
					AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
			devInfoBuilder = CloudEndpointUtils.updateBuilder(devInfoBuilder);
			Deviceinfoendpoint devInfoEndpoint = devInfoBuilder.build();
			CollectionResponseLogIn loginEntities = loginEndpoint.listLogIn().setUsername(strEmail).execute();
			CollectionResponseDeviceInfo devInfoEntities = devInfoEndpoint.listDeviceInfo().setGcmRegId(strRegId).execute();
			if(loginEntities == null || loginEntities.getItems() == null || loginEntities.getItems().size() < 1 ){
				return result;
			} else{
				LogIn login = loginEntities.getItems().get(0);
				DeviceInfo device=null;
				if(loginEntities.getItems().size() > 0){
					device = devInfoEntities.getItems().get(0);
				} else{
					device=new DeviceInfo();
					device.setMake(strDeviceName);
					device.setGcmRegId(strRegId);
					device=devInfoEndpoint.insertDeviceInfo(device).execute();
				}
				List<Long> lstDevIds=login.getDeviceIDs();
				if(lstDevIds==null){
					lstDevIds=new ArrayList<Long>();
				}
				if(!isPurchased){
					lstDevIds.removeAll(lstDevIds);
				}
				lstDevIds.add(device.getId());
				login.setDeviceIDs(lstDevIds);
				login.setPurchaseId(strUniqueString);
				login=loginEndpoint.updateLogIn(login).execute();
				SharedPreferences prefs = getActivity().getSharedPreferences(Constants.STR_PREFERENCE, Activity.MODE_PRIVATE);
				prefs.edit().putBoolean(Constants.STR_PURCHASED, true).commit();
				localDb.updatePurchaseId(strUniqueString);
				localDb.updatePurchaseToSynced();
				localDb.insert(login.getId(), login.getUsername(), login.getPassword(), Constants.STR_YOU, device.getId());
				localDb.insertPerson(login.getId(), login.getUsername(), Constants.STR_YOU);
				result=Constants.STR_SYNC_NEEDED;
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if(result.equals(Constants.STR_SYNC_NEEDED)){
				ActionBarActivity context=(ActionBarActivity)getActivity();
				LocalDB localDb=new LocalDB(context);
				long lngUserId=localDb.retrieve();
//				Intent intent=new Intent(getActivity(), ExpenseActivity.class);
//				intent.putExtra(Constants.STR_SYNC_NEEDED, true);
//				getActivity().startActivity(intent);
//				getActivity().finish();
				context.setContentView(R.layout.activity_expense);
				Toolbar toolbar = (Toolbar) context.findViewById(R.id.toolbar);

				if (toolbar != null) {
					context.setSupportActionBar(toolbar);
				}
				context.getSupportFragmentManager().popBackStack(null,FragmentManager.POP_BACK_STACK_INCLUSIVE);
				context.getSupportFragmentManager().beginTransaction().replace(R.id.container, AddTripFragment.newInstance(lngUserId, true)).commit();
			} else{
				showRegistrationError("Oops!! Something went wrong. Please try again.", strName);
			}
		}
	}
}
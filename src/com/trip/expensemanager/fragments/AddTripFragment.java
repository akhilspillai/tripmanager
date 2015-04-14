package com.trip.expensemanager.fragments;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import net.sourceforge.zbar.Symbol;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Patterns;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.trip.expensemanager.CloudEndpointUtils;
import com.trip.expensemanager.R;
import com.trip.expensemanager.SyncIntentService;
import com.trip.expensemanager.TripDetailsActivity;
import com.trip.expensemanager.adapters.CustomTripListAdapter;
import com.trip.expensemanager.distributionendpoint.Distributionendpoint;
import com.trip.expensemanager.distributionendpoint.model.CollectionResponseDistribution;
import com.trip.expensemanager.distributionendpoint.model.Distribution;
import com.trip.expensemanager.expenseendpoint.Expenseendpoint;
import com.trip.expensemanager.expenseendpoint.model.CollectionResponseExpense;
import com.trip.expensemanager.expenseendpoint.model.Expense;
import com.trip.expensemanager.fragments.dialogs.AddTripDialogFragment;
import com.trip.expensemanager.fragments.dialogs.ConfirmDialogListener;
import com.trip.expensemanager.fragments.dialogs.ConfirmationFragment;
import com.trip.expensemanager.loginendpoint.Loginendpoint;
import com.trip.expensemanager.loginendpoint.model.LogIn;
import com.trip.expensemanager.scanner.ZBarConstants;
import com.trip.expensemanager.scanner.ZBarScannerActivity;
import com.trip.expensemanager.tripendpoint.Tripendpoint;
import com.trip.expensemanager.tripendpoint.model.Trip;
import com.trip.utils.Constants;
import com.trip.utils.Global;
import com.trip.utils.LocalDB;
import com.trip.utils.TripBean;

public class AddTripFragment extends CustomFragment implements OnItemClickListener, OnClickListener {

	private static final int ZBAR_SCANNER_REQUEST = 1;

	public static AddTripFragment newInstance(long lngUserId, boolean isSyncNeeded) {
		AddTripFragment fragment=null;
		try {
			fragment=new AddTripFragment();
			Bundle bundle=new Bundle();
			bundle.putLong(Constants.STR_USER_ID, lngUserId);
			bundle.putBoolean(Constants.STR_NEEDS_SYNC, isSyncNeeded);
			fragment.setArguments(bundle);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fragment;
	}


	private ListView listTrip;
	private long lngUserId;
	private CustomTripListAdapter listAdapter;
	private ArrayList<TripBean> arrTrips=new ArrayList<TripBean>();
	private TextView txtNoTrip;
	protected String strTripName;
	private List<String> arrTripNames=new ArrayList<String>();
	private List<Long> arrIds=new ArrayList<Long>();
	private List<Long> arrAdminIds=new ArrayList<Long>();
	private List<String> arrCreationDates=new ArrayList<String>();
	private List<Boolean> arrClosed=new ArrayList<Boolean>();
	private List<Integer> arrSynched=new ArrayList<Integer>();
	public Button btnOk;
	private BroadcastReceiver receiver;
	private ImageButton btnAddTrip;
	private List<Integer> arrColors=new ArrayList<Integer>();
	private SwipeRefreshLayout sl;
	private SyncTask syncTask;
	private boolean isSyncNeeded;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				loadData();
			}
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState) {
		View rootView=null;
		try{
			((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.trips);
			rootView=inflater.inflate(R.layout.fragment_trips, container, false);
			sl=(SwipeRefreshLayout) rootView.findViewById(R.id.swipe_layout);
			listTrip=(ListView) rootView.findViewById(R.id.li_trips);
			txtNoTrip=(TextView) rootView.findViewById(R.id.txt_no_trips);
			btnAddTrip=(ImageButton) rootView.findViewById(R.id.btn_add_trip);

			lngUserId=getArguments().getLong(Constants.STR_USER_ID);
			isSyncNeeded=getArguments().getBoolean(Constants.STR_NEEDS_SYNC, false);

			listAdapter = new CustomTripListAdapter(getActivity(), arrTripNames, arrCreationDates, arrClosed, arrSynched, arrColors);
			listTrip.setAdapter(listAdapter);
			sl.setOnRefreshListener(new OnRefreshListener() {

				@Override
				public void onRefresh() {
					if(syncTask!=null){
						syncTask.cancel(true);
					}
					syncTask=new SyncTask();
					syncTask.execute(lngUserId);
				}
			});
			listTrip.setOnItemClickListener(this);
			btnAddTrip.setOnClickListener(this);
			setHasOptionsMenu(true);
			registerForContextMenu(listTrip);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		if(isSyncNeeded){
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					sl.setRefreshing(true);
					syncTask=new SyncTask();
					syncTask.execute(lngUserId);
					isSyncNeeded=false;
				}
			}, 1000);
		}
		loadData();
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver((receiver), new IntentFilter(SyncIntentService.RESULT_SYNC));
	}

	@Override
	public void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver((receiver));
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		super.onCreateOptionsMenu(menu, inflater);
		if(isPurchased()){
			inflater.inflate(R.menu.add_trip_fragment_action, menu);
		} else{
			inflater.inflate(R.menu.add_trip_fragment_action_upgrade, menu);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.action_scan_qr:
			scanQRCode();
			return true;

		case R.id.action_upgrade:
			showUpgradeDialog(getActivity().getResources().getString(R.string.upgrade_features));
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		int position=info.position;
		menu.setHeaderTitle(arrTripNames.get(position));
		String[] menuItems=new String[0];
		int synced=arrSynched.get(position);
		if(arrAdminIds.get(position)==lngUserId){
			menuItems = getResources().getStringArray(R.array.menu_trip_list_admin);
		} else if(synced!=3){
			menuItems = getResources().getStringArray(R.array.menu_trip_list);
		}
		for (int i = 0; i<menuItems.length; i++) {
			menu.add(Menu.NONE, i, i, menuItems[i]);
		}
	}

	private void loadData() {
		LocalDB localDb=new LocalDB(getActivity());
		arrTrips.removeAll(arrTrips);
		arrTripNames.removeAll(arrTripNames);
		arrIds.removeAll(arrIds);
		arrCreationDates.removeAll(arrCreationDates);
		arrClosed.removeAll(arrClosed);
		arrSynched.removeAll(arrSynched);
		arrAdminIds.removeAll(arrAdminIds);
		arrColors.removeAll(arrColors);
		try {
			long lngUserId=localDb.retrieve();
			if(lngUserId==0L){
				getActivity().finish();
				return;
			}
			arrTrips = localDb.retrieveTrips();

			if(arrTrips.size()!=0){
				String strDate=null;
				int i=0;
				for(TripBean trip:arrTrips){
					if(!trip.getSyncStatus().equals(Constants.STR_DELETED) && !trip.getSyncStatus().equals(Constants.STR_EXITED)){
						arrTripNames.add(trip.getName());
						if(trip.getId()!=0L){
							arrIds.add(trip.getId());
						} else{
							arrIds.add((long) i);
						}

						strDate=trip.getCreationDate();
						strDate=strDate.substring(0,strDate.indexOf(' '));
						arrCreationDates.add("Created on "+strDate);
						arrClosed.add(trip.isClosed());
						arrAdminIds.add(trip.getAdminId());

						if(trip.getSyncStatus().equals(Constants.STR_NOT_SYNCHED)){
							arrSynched.add(0);
						} else if(trip.getSyncStatus().equals(Constants.STR_QR_ADDED)){
							arrSynched.add(3);
						} else{
							arrSynched.add(2);
						}
						i++;
					}
				}

			}
			if(arrTripNames.size()==0){
				listTrip.setVisibility(View.INVISIBLE);
				txtNoTrip.setVisibility(View.VISIBLE);
			} else{
				listTrip.setVisibility(View.VISIBLE);
				txtNoTrip.setVisibility(View.INVISIBLE);
			}
			arrColors.addAll(Global.generateColor(arrTripNames.size()));
			listAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		int menuItemIndex = item.getItemId();
		//		String[] menuItems = getResources().getStringArray(R.array.menu_trip_list_admin);
		long lngTripId = arrIds.get(info.position);
		if(menuItemIndex==0){
			showUpdateTripDialog(arrTripNames.get(info.position), lngTripId);
		} else{
			showDeleteTripDialog(arrTripNames.get(info.position), lngTripId);
		}
		return true;
	}

	protected void showDeleteTripDialog(final String tripName, final long tripId) {
		ConfirmDialogListener listener=new ConfirmDialogListener() {

			@Override
			public void onDialogPositiveClick(DialogFragment dialog) {
				if(arrTripNames.size()==0){
					listTrip.setVisibility(View.INVISIBLE);
					txtNoTrip.setVisibility(View.VISIBLE);
				} else{
					arrColors.addAll(Global.generateColor(arrTripNames.size()));
					listAdapter.notifyDataSetChanged();
				}
				deleteTrip(tripId);
				dialog.dismiss();
			}

			@Override
			public void onDialogNegativeClick(DialogFragment dialog) {
				dialog.dismiss();
			}
		};
		ConfirmationFragment.newInstance(tripName, "Are you sure you want to delete the expense-group "+tripName+"?",null, R.layout.fragment_dialog_confirm, listener).show(getActivity().getSupportFragmentManager(), "dialog");

	}

	protected void deleteTrip(long tripId) {
		Context context=getActivity();
		LocalDB localDb=new LocalDB(context);
		try{
			if(localDb.isTripSynced(tripId)){
				localDb.updateTripStatusToDeleted(tripId);
				Intent serviceIntent=new Intent(context, SyncIntentService.class);
				context.startService(serviceIntent);
			} else{
				localDb.deleteTrip(tripId);
			}
			loadData();
		} catch(Exception e){
			e.printStackTrace();
		}
	}


	private void scanQRCode() {
		Intent intent = new Intent(getActivity(), ZBarScannerActivity.class);
		intent.putExtra(ZBarConstants.SCAN_MODES, new int[]{Symbol.QRCODE});
		startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
	}

	private void showAddTripDialog() {
		ConfirmDialogListener listener=new ConfirmDialogListener() {

			@Override
			public void onDialogPositiveClick(DialogFragment dialog) {
				strTripName=((EditText)dialog.getDialog().findViewById(R.id.etxt_trip_name)).getText().toString();
				addTrip(strTripName);
				dialog.dismiss();
			}

			@Override
			public void onDialogNegativeClick(DialogFragment dialog) {
				dialog.dismiss();
			}
		};
		AddTripDialogFragment.newInstance(getActivity().getResources().getString(R.string.add), "", listener).show(getActivity().getSupportFragmentManager(), "dialog");
	}

	private void showUpdateTripDialog(String tripName, final long tripId) {
		ConfirmDialogListener listener=new ConfirmDialogListener() {

			@Override
			public void onDialogPositiveClick(DialogFragment dialog) {
				strTripName=((EditText)dialog.getDialog().findViewById(R.id.etxt_trip_name)).getText().toString();
				updateTrip(tripId, strTripName);
				dialog.dismiss();
			}

			@Override
			public void onDialogNegativeClick(DialogFragment dialog) {
				dialog.dismiss();
			}
		};
		AddTripDialogFragment.newInstance(getActivity().getResources().getString(R.string.update), tripName, listener).show(getActivity().getSupportFragmentManager(), "dialog");
	}

	protected void updateTrip(long lngTripIdToChange, String strTripNameToChange) {
		Context context=getActivity();
		LocalDB localDb=new LocalDB(context);
		try{
			TripBean trip=localDb.retrieveTripDetails(lngTripIdToChange);
			if(Constants.STR_NOT_SYNCHED.equals(trip.getSyncStatus()) || Constants.STR_QR_ADDED.equals(trip.getSyncStatus())){
				localDb.updateTrip(lngTripIdToChange, strTripNameToChange, trip.getSyncStatus());
			} else{
				localDb.updateTrip(lngTripIdToChange, strTripNameToChange, Constants.STR_UPDATED);
				Intent serviceIntent=new Intent(context, SyncIntentService.class);
				context.startService(serviceIntent);
			}
			loadData();
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	protected void addTrip(String strTripName) {
		Context context = getActivity();
		LocalDB localDB=new LocalDB(context);
		long tripId=(long) (arrTripNames.size());
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			String strDate=sdf.format(new Date());
			long rowId=localDB.insertTrip(strTripName, tripId, strDate, String.valueOf(lngUserId), lngUserId, Constants.STR_NOT_SYNCHED);
			localDB.updateTripId(rowId, rowId);
			loadData();
			Intent serviceIntent=new Intent(context, SyncIntentService.class);
			context.startService(serviceIntent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Context context=getActivity();
		super.onActivityResult(requestCode, resultCode, data);
		try {
			LocalDB localDB=new LocalDB(context);
			if(requestCode==ZBAR_SCANNER_REQUEST){
				if (resultCode == Activity.RESULT_OK) {
					try {
						String strData=data.getStringExtra(ZBarConstants.SCAN_RESULT);
						String[] arrStrData=strData.split(",");
						if(arrStrData.length==4){
							long lngTripId=Long.parseLong(arrStrData[0]);
							if(localDB.isTripPresent(lngTripId)){
								Toast.makeText(getActivity(), "Trip already exists!!", Toast.LENGTH_LONG).show();
							} else{
								strTripName=arrStrData[1];
								String strDate=arrStrData[2];
								long lngAdminId=Long.parseLong(arrStrData[3]);
								String strUserId=String.valueOf(lngUserId);
								localDB.insertTrip(strTripName, lngTripId, strDate, strUserId, lngAdminId, Constants.STR_QR_ADDED);
								loadData();
								Intent serviceIntent=new Intent(context, SyncIntentService.class);
								context.startService(serviceIntent);
							}
						} else{
							Toast.makeText(getActivity(), "Invalid QR!!", Toast.LENGTH_LONG).show();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		String item = (String) parent.getItemAtPosition(position);
		Intent intent=new Intent(getActivity(), TripDetailsActivity.class);
		intent.putExtra(Constants.STR_TRIP_NAME, item);
		intent.putExtra(Constants.STR_USER_ID, lngUserId);
		intent.putExtra(Constants.STR_TRIP_ID, arrIds.get(position));
		intent.putExtra(Constants.STR_ADMIN_ID, arrTrips.get(position).getAdminId());
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.up_n_show, R.anim.no_anim);
	}

	@Override
	public void onClick(View v) {
		if(arrTripNames.size()>=3 && !isPurchased()){
			String strContent="You should be a premium user to add more Expense Groups.\n"+getActivity().getResources().getString(R.string.upgrade_features);
			showUpgradeDialog(strContent);
		} else{
			showAddTripDialog();
		}
	}

	private class SyncTask extends AsyncTask<Long, Void, Boolean>{

		@Override
		protected Boolean doInBackground(Long... params) {
			long sleepTime=100L;
			boolean completedSyncing=false;
			try {
				if(Global.isConnected(getActivity())){
					syncAllTrips(params[0]);
				} 
			} catch (IOException e) {
				try {
					Thread.sleep(sleepTime*2);
				} catch (InterruptedException e1) {

				}

			}
			completedSyncing=true;
			return completedSyncing;
		}

		private void syncAllTrips(long lngUserId) throws IOException {
			Loginendpoint.Builder loginBuilder = new Loginendpoint.Builder(
					AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
			loginBuilder = CloudEndpointUtils.updateBuilder(loginBuilder);
			Loginendpoint loginEndpoint = loginBuilder.build();

			LogIn login=loginEndpoint.getLogIn(lngUserId).execute();

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
			long rowId, tripId;
			List<Long> listTripIds = login.getTripIDs();
			LocalDB localDb=new LocalDB(getActivity());
			Trip retTrip;
			TripBean trip;
			LogIn retLogin;
			int i=0;
			while (listTripIds!=null && i<listTripIds.size()) {
				if(isCancelled()){
					break;
				}
				tripId=listTripIds.get(i);
				trip=localDb.retrieveTripDetails(tripId);
				if(trip!=null){
					continue;
				}
				retTrip=tripEndpoint.getTrip(listTripIds.get(i++)).execute();
				if(retTrip!=null){
					date = sdf.format(new Date(retTrip.getCreationDate().getValue()));
					listUserIdsTemp=retTrip.getUserIDs();
					long lngAdmin=retTrip.getAdmin();
					StringBuffer sbUsers=new StringBuffer();
					String username=null;
					Pattern emailPattern = Patterns.EMAIL_ADDRESS;
					for(long userId:listUserIdsTemp){
						username=localDb.retrieveUsername(userId);
						if(username==null || !emailPattern.matcher(username).matches()){
							retLogin=loginEndpoint.getLogIn(userId).execute();
							if(username==null){
								localDb.insertPerson(retLogin.getId(), retLogin.getUsername(), retLogin.getPrefferedName());
							} else{
								localDb.updatePerson(retLogin.getUsername(), retLogin.getId());
							}
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
									if(username==null || !emailPattern.matcher(username).matches()){
										retLogin=loginEndpoint.getLogIn(userId).execute();
										if(username==null){
											localDb.insertPerson(retLogin.getId(), retLogin.getUsername(), retLogin.getPrefferedName());
										} else{
											localDb.updatePerson(retLogin.getUsername(), retLogin.getId());
										}
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

				publishProgress();
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
			loadData();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			sl.setRefreshing(false);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if(!result){
				showSyncError(Constants.STR_SYNC_PROBLEM);
			} else{
				sl.setRefreshing(false);
			}
		}

		private void showSyncError(String strMessage) {
			ConfirmDialogListener listener=new ConfirmDialogListener() {

				@Override
				public void onDialogPositiveClick(DialogFragment dialog) {
					new SyncTask().execute();
				}

				@Override
				public void onDialogNegativeClick(DialogFragment dialog) {
					getActivity().finish();
					dialog.dismiss();
				}
			};
			ConfirmationFragment.newInstance("Error", strMessage, "Try Again", R.layout.fragment_dialog_info, listener).show(getActivity().getSupportFragmentManager(), "dialog");
		}

	}

}

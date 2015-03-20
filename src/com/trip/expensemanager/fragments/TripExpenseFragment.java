package com.trip.expensemanager.fragments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.trip.expensemanager.AllDetailsActivity;
import com.trip.expensemanager.R;
import com.trip.expensemanager.SyncIntentService;
import com.trip.expensemanager.TripDetailsActivity;
import com.trip.expensemanager.adapters.CustomExpenseListAdapter;
import com.trip.utils.Constants;
import com.trip.utils.ExpenseBean;
import com.trip.utils.Global;
import com.trip.utils.LocalDB;
import com.trip.utils.TripBean;

public class TripExpenseFragment extends CustomFragment implements OnItemClickListener, OnClickListener {


	private static final int REQUEST_CODE_ADD = 1;
	private static final int REQUEST_CODE_UPDATE = 2;

	public static TripExpenseFragment newInstance(String strTrip, long lngUserId, long lngTripId) {
		TripExpenseFragment fragment=null;
		try {
			fragment=new TripExpenseFragment();
			Bundle bundle=new Bundle();
			bundle.putString(Constants.STR_TRIP_NAME, strTrip);
			bundle.putLong(Constants.STR_USER_ID, lngUserId);
			bundle.putLong(Constants.STR_TRIP_ID, lngTripId);
			fragment.setArguments(bundle);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fragment;
	}

	public static Fragment newInstance(long lngExpenseUserId, long lngUserId,	int position, long lngTripId, long lngAdminId) {
		TripExpenseFragment fragment=null;
		try {
			fragment=new TripExpenseFragment();
			Bundle bundle=new Bundle();
			bundle.putLong(Constants.STR_EXP_USR_ID, lngExpenseUserId);
			bundle.putLong(Constants.STR_USER_ID, lngUserId);
			bundle.putInt(Constants.STR_POSITION, position);
			bundle.putLong(Constants.STR_TRIP_ID, lngTripId);
			bundle.putLong(Constants.STR_ADMIN_ID, lngAdminId);
			fragment.setArguments(bundle);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fragment;
	}
	protected AlertDialog alert;
	private ListView listExpense;
	private long lngUserId;
	private ArrayAdapter<String> listAdapter;
	private ArrayList<ExpenseBean> arrExpenses;
	private TextView txtNoExpense;
	private long lngTripId;
	private List<Long> arrIds=new ArrayList<Long>();
	private List<Long> arrUserIds=new ArrayList<Long>();
	private List<String> arrExpenseNames=new ArrayList<String>();
	private List<String> arrExpenseAmount=new ArrayList<String>();
	private List<Integer> arrSynced=new ArrayList<Integer>();
	private List<ExpenseBean> arrExpenseNotSynched=new ArrayList<ExpenseBean>();
	private long lngExpUserId=0L;
	private BroadcastReceiver receiver;
	private ImageButton btnAddExpense;
	private List<Integer> arrColors=new ArrayList<Integer>();
	private static AtomicInteger newExpenseCalled=new AtomicInteger();

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
			rootView=inflater.inflate(R.layout.fragment_trip_expense, container, false);
			listExpense=(ListView) rootView.findViewById(R.id.li_expense);
			txtNoExpense=(TextView) rootView.findViewById(R.id.txt_no_expense);
			btnAddExpense=(ImageButton) rootView.findViewById(R.id.btn_add_expense);
			
			Bundle bundle=getArguments(); 
			lngUserId=bundle.getLong(Constants.STR_USER_ID);
			lngExpUserId=bundle.getLong(Constants.STR_EXP_USR_ID);
			lngTripId=bundle.getLong(Constants.STR_TRIP_ID);
			bundle.getLong(Constants.STR_ADMIN_ID);

			listAdapter = new CustomExpenseListAdapter(getActivity(), arrExpenseNames, arrExpenseAmount, arrSynced, arrColors);
			listExpense.setAdapter(listAdapter);

			listExpense.setOnItemClickListener(this);
			btnAddExpense.setOnClickListener(this);
			loadData();
			setHasOptionsMenu(true);
			registerForContextMenu(listExpense);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rootView;
	}


	private void loadData() {
		LocalDB localDb=new LocalDB(getActivity());
		arrExpenseNames.removeAll(arrExpenseNames);
		arrExpenseAmount.removeAll(arrExpenseAmount);
		arrSynced.removeAll(arrSynced);
		arrColors.removeAll(arrColors);
		try {
			TripBean trip = localDb.retrieveTripDetails(lngTripId);
			if(trip==null){
				getActivity().finish();
			}
			lngTripId=trip.getId();
			if(lngExpUserId!=0L){
				btnAddExpense.setVisibility(View.GONE);
				arrExpenses = localDb.retrieveExpenses(lngTripId, lngExpUserId);
			} else{
				arrExpenses = localDb.retrieveExpenses(lngTripId);
			}

			if(lngExpUserId!=0L){
				String username = localDb.retrievePrefferedName(lngExpUserId);
				((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(username);
				if(username!=null){
					txtNoExpense.setText("No expenses are added by "+username+" for this expense-group!!");
				} else{
					txtNoExpense.setText("No expenses are added by this user for this expense-group!!");
				}
			}
			long usrIdTemp=0L;
			String usernameTemp=null, strDate=null;
			for(ExpenseBean expense:arrExpenses){
				if(!Constants.STR_DELETED.equals(expense.getSyncStatus())){
					usrIdTemp=expense.getUserId();
					usernameTemp=localDb.retrievePrefferedName(usrIdTemp);
					if(usernameTemp==null){
						usernameTemp=String.valueOf(usrIdTemp);
					}
					arrExpenseNames.add(expense.getName()+"(Added by "+usernameTemp+")");
					strDate=expense.getCreationDate();
					strDate=strDate.substring(0,strDate.indexOf(' '));
					arrExpenseAmount.add(expense.getAmount());
					if(expense.getSyncStatus().equals(Constants.STR_ERROR_STATUS)){
						arrSynced.add(2);
					} else if(!expense.getSyncStatus().equals(Constants.STR_NOT_SYNCHED)){
						arrSynced.add(2);
					}else{
						arrSynced.add(0);
					}
					arrIds.add(expense.getId());
					arrUserIds.add(expense.getUserId());
				}
			}
			if(arrExpenseNames.size()!=0){
				listExpense.setVisibility(View.VISIBLE);
				txtNoExpense.setVisibility(View.INVISIBLE);
			} else{
				listExpense.setVisibility(View.INVISIBLE);
				txtNoExpense.setVisibility(View.VISIBLE);
			}
			arrColors.addAll(Global.generateColor(arrExpenseNames.size()));
			listAdapter.notifyDataSetChanged();
			int size=arrExpenseNotSynched.size();
			if(size!=0){
				ExpenseBean[] arrExpensesToSync=new ExpenseBean[size];
				arrExpensesToSync=arrExpenseNotSynched.toArray(arrExpensesToSync);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if(lngExpUserId!=0L){
			LocalBroadcastManager.getInstance(getActivity()).registerReceiver((receiver), new IntentFilter(SyncIntentService.RESULT_SYNC));
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if(lngExpUserId!=0L){
			LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver((receiver));
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		int position=info.position;
		if(arrUserIds.get(position)==lngUserId){
			menu.setHeaderTitle(arrExpenseNames.get(position));
			String[] menuItems = getResources().getStringArray(R.array.menu_expense_list);
			for (int i = 0; i<menuItems.length; i++) {
				menu.add(Menu.NONE, i, i, menuItems[i]);
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		int menuItemIndex = item.getItemId();
		getResources().getStringArray(R.array.menu_expense_list);
		long lngExpenseId = arrIds.get(info.position);
		LocalDB localDb=new LocalDB(getActivity());
		if(menuItemIndex==0){
			TripBean trip = localDb.retrieveTripDetails(lngTripId);
			ExpenseBean expense = localDb.retrieveExpense(lngExpenseId);
			long[] tripUserIds=trip.getUserIds();
			Long[] arrTripUserIds=new Long[tripUserIds.length];
			for(int i=0;i<arrTripUserIds.length;i++){
				arrTripUserIds[i]=tripUserIds[i];
			}
			List<Long> lstTripUserIds=Arrays.asList(arrTripUserIds);
			List<Long> lstExpenseUserIds=Global.longToList(expense.getUserIds());
			if(!lstTripUserIds.containsAll(lstExpenseUserIds)){
				showInfoMessage(Constants.STR_NO_DELETE);
				return true;
			}
			showDeleteExpenseDialog(arrExpenseNames.get(info.position), lngExpenseId, info.position);
		} 
		return true;
	}


	@SuppressLint("InflateParams")
	protected void showDeleteExpenseDialog(final String expenseName, final long expenseId, final int position) {
		try{
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			View view = getActivity().getLayoutInflater().inflate(R.layout.delete_expensegroup_dialog, null);
			builder.setCancelable(true);
			TextView textView = (TextView)view.findViewById(R.id.tv_message);
			Button btnYes = (Button) view.findViewById(R.id.btn_yes);
			Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
			btnYes.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {					
					deleteExpense(expenseId, position);
					alert.cancel();
				}
			});
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					alert.cancel();
				}
			});

			textView.setText("Are you sure you want to delete the expense "+expenseName+"?");

			alert = builder.create();
			alert.setView(view, 0, 0, 0, 0);
			alert.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showAddExpense() {
		Intent intent=new Intent(getActivity(), AllDetailsActivity.class);
		intent.putExtra(Constants.STR_USER_ID, lngUserId);
		intent.putExtra(Constants.STR_TRIP_ID, lngTripId);
		intent.putExtra(Constants.STR_ADMIN_ID, lngUserId);
		intent.putExtra(Constants.STR_OPCODE, Constants.I_OPCODE_ADD_EXPENSE);
		startActivityForResult(intent, REQUEST_CODE_ADD);
		newExpenseCalled.getAndIncrement();
		getActivity().overridePendingTransition(R.anim.up_n_show, R.anim.no_anim);
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		newExpenseCalled.getAndDecrement();
		try {
			if (resultCode == Activity.RESULT_OK) {
				if(requestCode==REQUEST_CODE_ADD){
					try {
						String[] arrStrData=data.getStringArrayExtra(Constants.STR_EXPENSE_DETAIL_ARR);
						if(arrStrData.length==6){
							addExpense(arrStrData[0], arrStrData[1], arrStrData[2], arrStrData[3], arrStrData[4], arrStrData[5]);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if(requestCode==REQUEST_CODE_UPDATE){
					try {
						String[] arrStrData=data.getStringArrayExtra(Constants.STR_EXPENSE_DETAIL_ARR);
						if(arrStrData.length==7){
							updateExpense(arrStrData[0], arrStrData[1], arrStrData[2], Long.parseLong(arrStrData[3]), Integer.parseInt(arrStrData[4]), arrStrData[5], arrStrData[6]);
						} else if(arrStrData.length==2){
							deleteExpense(Long.parseLong(arrStrData[0]), Integer.parseInt(arrStrData[1]));
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

	private void deleteExpense(long expenseId, int position) {
		try{
			Context context=getActivity();
			LocalDB localDb=new LocalDB(context);
			TripBean trip=localDb.retrieveTripDetails(lngTripId);
			ExpenseBean expense=localDb.retrieveExpense(expenseId);
			boolean areAllUsersPresent =	areAllUsersPresent(trip, expense);

			if(areAllUsersPresent){
				if(Constants.STR_NOT_SYNCHED.equals(expense.getSyncStatus()) || Constants.STR_ERROR_STATUS.equals(expense.getSyncStatus())){
					localDb.deleteExpense(expenseId);
				} else{
					localDb.updateExpenseStatusToDeleted(expenseId);
					((TripDetailsActivity)context).updateViews();
					context.startService(new Intent(context, SyncIntentService.class));
				}
			} else{
				showInfoMessage(Constants.STR_NO_DELETE);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	private boolean areAllUsersPresent(TripBean trip, ExpenseBean expense) {
		List<Long> lstExpUsrIds=Global.longToList(expense.getUserIds());
		long[] arrTripUsers=trip.getUserIds();
		List<Long> lstTripUsrIds=new ArrayList<Long>(arrTripUsers.length);
		for(int i=0;i<arrTripUsers.length;i++){
			lstTripUsrIds.add(arrTripUsers[i]);
		}
		return lstTripUsrIds.containsAll(lstExpUsrIds);
	}

	protected void addExpense(String expenseName, String expenseDetail, String expenseAmount, String strDate, String strUserIds, String strAmounts) {
		try{
			Context context=getActivity();
			LocalDB localDb=new LocalDB(context);
			long lngExpenseId=0L;
			lngExpenseId=localDb.insertExpense(expenseName, lngExpenseId, strDate, "INR", expenseAmount, expenseDetail, lngTripId, lngUserId, strUserIds, strAmounts, Constants.STR_NOT_SYNCHED);
			localDb.updateExpenseId(lngExpenseId, lngExpenseId);
			((TripDetailsActivity)context).updateViews();
			context.startService(new Intent(context, SyncIntentService.class));
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	protected void updateExpense(String expenseName, String expenseDetail, String expenseAmount, long expenseId, int position, String strUserIds, String strAmounts) {
		try{
			Context context=getActivity();
			LocalDB localDb=new LocalDB(context);
			TripBean trip=localDb.retrieveTripDetails(lngTripId);
			ExpenseBean expense=localDb.retrieveExpense(expenseId);
			expense.setUserIds(strUserIds);
			boolean areAllUsersPresent = areAllUsersPresent(trip, expense);
			if(areAllUsersPresent){
				if(Constants.STR_NOT_SYNCHED.equals(expense.getSyncStatus())){
					localDb.updateExpense(expenseName, expenseAmount, expenseDetail, strUserIds, strAmounts, expense.getSyncStatus(), expenseId);
				} else if(Constants.STR_ERROR_STATUS.equals(expense.getSyncStatus())){
					localDb.updateExpense(expenseName, expenseAmount, expenseDetail, strUserIds, strAmounts, Constants.STR_NOT_SYNCHED, expenseId);
				}else{
					localDb.updateExpense(expenseName, expenseAmount, expenseDetail, strUserIds, strAmounts, Constants.STR_UPDATED, expenseId);
				}
				((TripDetailsActivity)context).updateViews();
				context.startService(new Intent(context, SyncIntentService.class));
			} else{
				showInfoMessage(Constants.STR_NO_EDIT);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		showUpdateExpense(arrIds.get(position), position);
	}

	private void showUpdateExpense(long expenseId, int position) {
		Intent intent=new Intent(getActivity(), AllDetailsActivity.class);
		intent.putExtra(Constants.STR_TRIP_ID, lngTripId);
		intent.putExtra(Constants.STR_USER_ID, lngUserId);
		intent.putExtra(Constants.STR_EXPENSE_ID, expenseId);
		intent.putExtra(Constants.STR_POSITION, position);
		intent.putExtra(Constants.STR_OPCODE, Constants.I_OPCODE_UPDATE_EXPENSE);
		startActivityForResult(intent, REQUEST_CODE_UPDATE);
		newExpenseCalled.getAndIncrement();
		getActivity().overridePendingTransition(R.anim.up_n_show, R.anim.no_anim);
	}

	@Override
	public void onClick(View v) {
		showAddExpense();
	}

}
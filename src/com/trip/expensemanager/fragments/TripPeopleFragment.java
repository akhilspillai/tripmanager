package com.trip.expensemanager.fragments;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.trip.expensemanager.AllDetailsActivity;
import com.trip.expensemanager.R;
import com.trip.expensemanager.adapters.CustomPeopleListAdapter;
import com.trip.expensemanager.fragments.dialogs.ConfirmDialogListener;
import com.trip.expensemanager.fragments.dialogs.ConfirmationFragment;
import com.trip.utils.Constants;
import com.trip.utils.ExpenseBean;
import com.trip.utils.Global;
import com.trip.utils.LocalDB;
import com.trip.utils.TripBean;

public class TripPeopleFragment extends CustomFragment implements OnItemClickListener, OnClickListener {

	private static final int REQUEST_CODE_SHOW_EXP = 1;

	public static TripPeopleFragment newInstance(String strTrip, long lngUserId, long lngTripId) {
		TripPeopleFragment fragment=null;
		try {
			fragment=new TripPeopleFragment();
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
	protected AlertDialog alert;
	private ListView listPeople;
	private long lngUserId;
	private ArrayAdapter<String> listAdapter;
	private List<String> arrPeople=new ArrayList<String>();
	private List<Long> arrPeopleIds=new ArrayList<Long>();
	private List<String> arrAmount=new ArrayList<String>();
	private TextView txtNoPeople;
	private long lngTripId;
	private String strTripName;
	private List<ExpenseBean> arrExpenses;
	private List<Boolean> arrSynced=new ArrayList<Boolean>();
	private long lngAdminId;
	private String strDate;
	private ImageButton btnAddPeople;
	private List<Integer> arrColors=new ArrayList<Integer>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState) {
		View rootView=null;
		try{
			rootView=inflater.inflate(R.layout.fragment_trip_people, container, false);
			listPeople=(ListView) rootView.findViewById(R.id.li_people);
			txtNoPeople=(TextView) rootView.findViewById(R.id.txt_no_people);
			btnAddPeople=(ImageButton) rootView.findViewById(R.id.btn_add_people);
			
			Bundle bundle=getArguments(); 
			lngUserId=bundle.getLong(Constants.STR_USER_ID);
			strTripName=bundle.getString(Constants.STR_TRIP_NAME);
			lngTripId=bundle.getLong(Constants.STR_TRIP_ID);
			listAdapter = new CustomPeopleListAdapter(getActivity(), arrPeople, arrAmount, arrSynced, arrColors);
			listPeople.setAdapter(listAdapter);
			listPeople.setOnItemClickListener(this);
			btnAddPeople.setOnClickListener(this);
			loadData();
			setHasOptionsMenu(true);
			//			registerForContextMenu(listPeople);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rootView;
	}

	private void loadData() {
		LocalDB localDb=new LocalDB(getActivity());
		arrPeople.removeAll(arrPeople);
		arrAmount.removeAll(arrAmount);
		arrSynced.removeAll(arrSynced);
		arrColors.removeAll(arrColors);
		try {
			TripBean trip=localDb.retrieveTripDetails(lngTripId);
			lngTripId=trip.getId();
			strTripName=trip.getName();
			lngAdminId=trip.getAdminId();
			strDate=trip.getCreationDate();
			long[] arrPeopleIdTemp=trip.getUserIds();
			for(long userIdTemp:arrPeopleIdTemp){
				arrPeople.add(localDb.retrievePrefferedName(userIdTemp));
				arrPeopleIds.add(userIdTemp);
				arrAmount.add("0");
				arrSynced.add(true);
			}
			arrExpenses = localDb.retrieveExpenses(lngTripId);

			String amount="0";
			int position=0;
			boolean synced=false;
			for(ExpenseBean expense:arrExpenses){
				if(!Constants.STR_DELETED.equals(expense.getSyncStatus()) && !Constants.STR_ERROR_STATUS.equals(expense.getSyncStatus())){
					position=arrPeopleIds.indexOf(expense.getUserId());
					if(position>=0){
						amount=arrAmount.get(position);
						arrAmount.set(position, add(expense.getAmount(), amount));
						synced=!expense.getSyncStatus().equals(Constants.STR_NOT_SYNCHED);
						if(!synced){
							arrSynced.set(position, false);
						}
					}
				}
			}
			if(arrPeople.size()!=0){
				if(listPeople.getVisibility()==View.INVISIBLE){
					listPeople.setVisibility(View.VISIBLE);
					txtNoPeople.setVisibility(View.GONE);
				}
			}
			arrColors.addAll(Global.generateColor(arrPeople.size()));
			listAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.add_people_fragment_action, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		LocalDB localDB=new LocalDB(getActivity());
		switch (item.getItemId()) {
		case R.id.action_add_people:
			if(localDB.isTripSynced(lngTripId)){
				showAddPeopleFragment();
			} else{
				showMessage("You have to sync the expense group before adding people to it!!");
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}*/


	private void showAddPeopleFragment() {
		//		getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, AddPeopleFragment.newInstance(lngTripId), Constants.STR_ADD_PEOPLE_TAG).addToBackStack(null).commit();
		Intent intent=new Intent(getActivity(), AllDetailsActivity.class);
		intent.putExtra(Constants.STR_TRIP_NAME, strTripName);
		intent.putExtra(Constants.STR_TRIP_ID, lngTripId);
		intent.putExtra(Constants.STR_ADMIN_ID, lngAdminId);
		intent.putExtra(Constants.STR_DATE, strDate);

		intent.putExtra(Constants.STR_OPCODE, Constants.I_OPCODE_ADD_TRIP_QR);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.up_n_show, R.anim.no_anim);
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		showExpenseByPerson(arrPeopleIds.get(position), position);
	}

	private void showExpenseByPerson(long userId, int position) {
		Intent intent=new Intent(getActivity(), AllDetailsActivity.class);
		intent.putExtra(Constants.STR_USER_ID, lngUserId);
		intent.putExtra(Constants.STR_EXP_USR_ID, userId);
		intent.putExtra(Constants.STR_POSITION, position);
		intent.putExtra(Constants.STR_TRIP_ID, lngTripId);
		intent.putExtra(Constants.STR_ADMIN_ID, lngAdminId);

		intent.putExtra(Constants.STR_OPCODE, Constants.I_OPCODE_SHOW_USER_EXPENSES);
		startActivityForResult(intent, REQUEST_CODE_SHOW_EXP);
		getActivity().overridePendingTransition(R.anim.up_n_show, R.anim.no_anim);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		int position=info.position;
		if(arrExpenses.get(position).getUserId()==lngUserId){
			menu.setHeaderTitle(arrPeople.get(position));
			String[] menuItems = getResources().getStringArray(R.array.menu_people_list);
			for (int i = 0; i<menuItems.length; i++) {
				menu.add(Menu.NONE, i, i, menuItems[i]);
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		int menuItemIndex = item.getItemId();
//		String[] menuItems = getResources().getStringArray(R.array.menu_expense_list);
//		String menuItemName = menuItems[menuItemIndex];
		long lngUserId = arrPeopleIds.get(info.position);
		if(menuItemIndex==0){
			showDeletePersonDialog(arrPeople.get(info.position), lngUserId, info.position, strTripName);
		} 
		return true;
	}

	
	protected void showDeletePersonDialog(final String username, final long userId, final int position, String tripName) {
		ConfirmDialogListener listener=new ConfirmDialogListener() {

			@Override
			public void onDialogPositiveClick(DialogFragment dialog) {
				deleteUser(userId);
				dialog.dismiss();
			}

			@Override
			public void onDialogNegativeClick(DialogFragment dialog) {
				dialog.dismiss();
			}
		};
		ConfirmationFragment.newInstance(username, "Are you sure you want to delete the person "+username+" from trip "+tripName+"?", null, R.layout.fragment_dialog_confirm, listener).show(getActivity().getSupportFragmentManager(), "dialog");
	}

	protected void deleteUser(long userId) {
		// TODO Auto-generated method stub

	}

	private String add(String amount1, String amount2) {
		BigDecimal bg1, bg2, bg3=new BigDecimal("0");
		try {

			bg1 = new BigDecimal(amount1);
			bg2 = new BigDecimal(amount2);

			// subtract bg1 with bg2 using mc and assign result to bg3
			bg3 = bg1.add(bg2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bg3.toString();
	}

	@Override
	public void onClick(View v) {
		if(arrPeople.size()>=5 && !isPurchased()){
			String strContent="You should be a premium user to add more people to this Expense Group.\n"+getActivity().getResources().getString(R.string.upgrade_features);
			showUpgradeDialog(strContent);
		} else{
			showAddPeopleFragment();
		}
	}
	
}

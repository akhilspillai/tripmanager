package com.trip.expensemanager.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.trip.expensemanager.ExpenseActivity;
import com.trip.expensemanager.R;
import com.trip.expensemanager.TripDetailsActivity;
import com.trip.expensemanager.adapters.CustomUpdatesAdapter;
import com.trip.utils.Constants;
import com.trip.utils.LocalDB;
import com.trip.utils.TripBean;
import com.trip.utils.UpdateBean;

public class UpdatesFragment extends CustomFragment implements OnItemClickListener {

	public static UpdatesFragment newInstance() {
		UpdatesFragment fragment=null;
		try {
			fragment=new UpdatesFragment();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fragment;
	}

	private CustomUpdatesAdapter listAdapter;
	private ListView lvUpdatesList;
	private List<UpdateBean> listUpdates=new ArrayList<UpdateBean>();
	private List<String> listLabels=new ArrayList<String>();
	private List<String> listActions=new ArrayList<String>();
	private TextView tvNoUpdates;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState) {
		View rootView=null;
		try{
			rootView=inflater.inflate(R.layout.fragment_updates, container, false);
			lvUpdatesList=(ListView) rootView.findViewById(R.id.li_updates);
			tvNoUpdates=(TextView) rootView.findViewById(R.id.txt_no_updates);
			listAdapter = new CustomUpdatesAdapter(getActivity(), listLabels, listActions);
			lvUpdatesList.setAdapter(listAdapter);
			lvUpdatesList.setOnItemClickListener(this);
			SharedPreferences prefs = getActivity().getSharedPreferences(Constants.STR_PREFERENCE, Activity.MODE_PRIVATE);
			prefs.edit().putInt(Constants.STR_COUNT, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rootView;
	}

	private void loadData() {
		listUpdates.removeAll(listUpdates);
		listLabels.removeAll(listLabels);
		listActions.removeAll(listActions);
		LocalDB localDb = new LocalDB(getActivity());
		listUpdates=localDb.retrieveUnreadUpdates();
		if(listUpdates.size()!=0){
			for(UpdateBean ubTemp:listUpdates){
				listLabels.add(ubTemp.getUpdate());
				listActions.add(ubTemp.getAction());
			}
			tvNoUpdates.setVisibility(View.GONE);
			lvUpdatesList.setVisibility(View.VISIBLE);
			listAdapter.notifyDataSetChanged();
		} else{
			tvNoUpdates.setVisibility(View.VISIBLE);
			lvUpdatesList.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		loadData();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intentToCall=null;
		LocalDB localDb=new LocalDB(getActivity());
		TripBean tripBean=localDb.retrieveTripDetails(listUpdates.get(position).getItemId());
		long userId=localDb.retrieve();
		if(listActions.get(position).equals(Constants.STR_USER_ADDED)){
			intentToCall=new Intent(getActivity(), TripDetailsActivity.class);
			intentToCall.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intentToCall.putExtra(Constants.STR_SHOW_TAB, 3);
			intentToCall.putExtra(Constants.STR_TRIP_NAME, tripBean.getName());
			intentToCall.putExtra(Constants.STR_USER_ID, userId);
			intentToCall.putExtra(Constants.STR_TRIP_ID, tripBean.getId());
			intentToCall.putExtra(Constants.STR_ADMIN_ID, tripBean.getAdminId());
		} else if(listActions.get(position).equals(Constants.STR_USER_DELETED)){
			intentToCall=new Intent(getActivity(), TripDetailsActivity.class);
			intentToCall.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intentToCall.putExtra(Constants.STR_SHOW_TAB, 3);
			intentToCall.putExtra(Constants.STR_TRIP_NAME, tripBean.getName());
			intentToCall.putExtra(Constants.STR_USER_ID, userId);
			intentToCall.putExtra(Constants.STR_TRIP_ID, tripBean.getId());
			intentToCall.putExtra(Constants.STR_ADMIN_ID, tripBean.getAdminId());
		} else if(listActions.get(position).equals(Constants.STR_TRIP_DELETED)){
			intentToCall=new Intent(getActivity(), ExpenseActivity.class);
			intentToCall.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		} else if(listActions.get(position).equals(Constants.STR_TRIP_UPDATED)){
			intentToCall=new Intent(getActivity(), ExpenseActivity.class);
			intentToCall.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		} else if(listActions.get(position).equals(Constants.STR_EXPENSE_ADDED)){
			intentToCall=new Intent(getActivity(), TripDetailsActivity.class);
			intentToCall.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intentToCall.putExtra(Constants.STR_SHOW_TAB, 2);
			intentToCall.putExtra(Constants.STR_TRIP_NAME, tripBean.getName());
			intentToCall.putExtra(Constants.STR_USER_ID, userId);
			intentToCall.putExtra(Constants.STR_TRIP_ID, tripBean.getId());
			intentToCall.putExtra(Constants.STR_ADMIN_ID, tripBean.getAdminId());
		} else if(listActions.get(position).equals(Constants.STR_EXPENSE_DELETED)){
			intentToCall=new Intent(getActivity(), TripDetailsActivity.class);
			intentToCall.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intentToCall.putExtra(Constants.STR_SHOW_TAB, 2);
			intentToCall.putExtra(Constants.STR_TRIP_NAME, tripBean.getName());
			intentToCall.putExtra(Constants.STR_USER_ID, userId);
			intentToCall.putExtra(Constants.STR_TRIP_ID, tripBean.getId());
			intentToCall.putExtra(Constants.STR_ADMIN_ID, tripBean.getAdminId());
		} else if(listActions.get(position).equals(Constants.STR_EXPENSE_UPDATED)){
			intentToCall=new Intent(getActivity(), TripDetailsActivity.class);
			intentToCall.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intentToCall.putExtra(Constants.STR_SHOW_TAB, 2);
			intentToCall.putExtra(Constants.STR_TRIP_NAME, tripBean.getName());
			intentToCall.putExtra(Constants.STR_USER_ID, userId);
			intentToCall.putExtra(Constants.STR_TRIP_ID, tripBean.getId());
			intentToCall.putExtra(Constants.STR_ADMIN_ID, tripBean.getAdminId());
		} else if(listActions.get(position).equals(Constants.STR_DISTRIBUTION_ADDED)){
			intentToCall=new Intent(getActivity(), TripDetailsActivity.class);
			intentToCall.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intentToCall.putExtra(Constants.STR_SHOW_TAB, 1);
			intentToCall.putExtra(Constants.STR_TRIP_NAME, tripBean.getName());
			intentToCall.putExtra(Constants.STR_USER_ID, userId);
			intentToCall.putExtra(Constants.STR_TRIP_ID, tripBean.getId());
			intentToCall.putExtra(Constants.STR_ADMIN_ID, tripBean.getAdminId());
		}
		localDb.deleteToSync(listUpdates.get(position).getId());
		startActivity(intentToCall);
	}

}

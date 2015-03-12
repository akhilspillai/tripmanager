package com.trip.expensemanager;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.trip.expensemanager.adapters.CustomPagerAdapter;
import com.trip.expensemanager.views.SlidingTabLayout;
import com.trip.utils.Constants;
import com.trip.utils.DistributionBean1;
import com.trip.utils.ExpenseBean;
import com.trip.utils.Global;
import com.trip.utils.LocalDB;
import com.trip.utils.TripBean;

public class TripDetailsActivity extends ActionBarActivity {

	private long lngUserId=2L;
	private String strTripName;
	private CustomPagerAdapter mAdapter;
	private ViewPager mViewPager;
	private String[] tabs = {"Details","Expense", "People"};
	private SlidingTabLayout tabLayout;
	private long lngTripId;
	private BroadcastReceiver receiver;
	private long lngAdminId;
	private AlertDialog alert;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.activity_trip_details);
			Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
			if (toolbar != null) {
				setSupportActionBar(toolbar);
			}
			receiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					mAdapter.notifyDataSetChanged();
				}
			};
			Intent intent=getIntent();
			int tabToSelect=intent.getIntExtra(Constants.STR_SHOW_TAB, 0);
			lngUserId=intent.getLongExtra(Constants.STR_USER_ID, 0L);
			strTripName=intent.getStringExtra(Constants.STR_TRIP_NAME);
			lngTripId=intent.getLongExtra(Constants.STR_TRIP_ID, 0L);
			TripBean trip=new LocalDB(this).retrieveTripDetails(lngTripId);
			if(trip==null){
				finish();
			}
			lngTripId=trip.getId();
			lngAdminId=trip.getAdminId();
			intent.getLongExtra(Constants.STR_ADMIN_ID, 0L);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setTitle(trip.getName());
			mAdapter = new CustomPagerAdapter(getSupportFragmentManager(), tabs, strTripName, lngUserId, lngTripId, this);
			mViewPager = (ViewPager) findViewById(R.id.pager);
			mViewPager.setAdapter(mAdapter);
			tabLayout = (SlidingTabLayout) findViewById(R.id.tabs);
			tabLayout.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

			// Setting Custom Color for the Scroll bar indicator of the Tab View
			tabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
				@Override
				public int getIndicatorColor(int position) {
					return getResources().getColor(R.color.tabsScrollColor);
				}
			});

			// Setting the ViewPager For the SlidingTabsLayout
			tabLayout.setViewPager(mViewPager);

			if(tabToSelect!=0){
				mViewPager.setCurrentItem(tabToSelect-1);
			}
			AdView adView = (AdView)findViewById(R.id.adView);
			AdRequest adRequest = new AdRequest.Builder().addTestDevice("07479579BCD31CAC59F426C69FC347F0").addTestDevice("CA38245079883989F4F525CCE75019B4").addTestDevice("73F2A5CA55F628C98441DA7DAFECE33C").build();
			if(adView.getVisibility()==View.VISIBLE){
				adView.loadAd(adRequest);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mAdapter.notifyDataSetChanged();
		LocalBroadcastManager.getInstance(this).registerReceiver((receiver), new IntentFilter(SyncIntentService.RESULT));
	}

	@Override
	public void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver((receiver));
	}

	public void updateViews(){
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
			
		case R.id.action_delete_trip:
			showDeleteTripDialog(strTripName, lngTripId);
			return true;
			
		case R.id.action_exit_eg:
			tryExitingTrip();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		if(lngUserId==lngAdminId){
			inflater.inflate(R.menu.trip_detail_action, menu);
		} else{
			inflater.inflate(R.menu.expense_exit_action, menu);
		}
		return true;
	}
	
	@SuppressLint("InflateParams")
	protected void showDeleteTripDialog(final String tripName, final long tripId) {
		try{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			View view = getLayoutInflater().inflate(R.layout.delete_expensegroup_dialog, null);
			builder.setCancelable(true);
			TextView textView = (TextView)view.findViewById(R.id.tv_message);
			Button btnYes = (Button) view.findViewById(R.id.btn_yes);
			Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
			btnYes.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					deleteTrip(tripId);
					alert.cancel();
				}
			});
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					alert.cancel();
				}
			});

			textView.setText("Are you sure you want to delete the expense-group "+tripName+"?");

			alert = builder.create();
			alert.setView(view, 0, 0, 0, 0);
			alert.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void deleteTrip(long tripId) {
		Context context=this;
		LocalDB localDb=new LocalDB(context);
		try{
			if(localDb.isTripSynced(tripId)){
				localDb.updateTripStatusToDeleted(tripId);
			} else{
				localDb.deleteTrip(tripId);
			}
			context.startService(new Intent(context, SyncIntentService.class));
			((Activity) context).finish();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void tryExitingTrip() {
		LocalDB localDb=new LocalDB(this);
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
			showExitEG();
		} else{
			showInfoMessage(Constants.STR_SETTLE_FIRST);
		}
	}
	
	@SuppressLint("InflateParams")
	protected void showExitEG() {
		Context context=this;
		try{
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			View view = getLayoutInflater().inflate(R.layout.delete_expensegroup_dialog, null);
			builder.setCancelable(true);
			TextView textView = (TextView)view.findViewById(R.id.tv_message);
			Button btnYes = (Button) view.findViewById(R.id.btn_yes);
			Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
			btnYes.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {					
					exitEG(lngUserId, lngTripId);
					alert.cancel();
				}
			});
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					alert.cancel();
				}
			});
			TripBean tripTemp=new LocalDB(context).retrieveTripDetails(lngTripId);
			textView.setText("Are you sure you want to exit from the expense-group "+tripTemp.getName()+"?");

			alert = builder.create();
			alert.setView(view, 0, 0, 0, 0);
			alert.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void exitEG(long lngExpUserIdTemp, long lngTripIdTemp) {
		Context context=this;
		LocalDB localDb=new LocalDB(context);
		TripBean tripTemp=localDb.retrieveTripDetails(lngTripIdTemp);
		if(!Constants.STR_QR_ADDED.equals(tripTemp.getSyncStatus())){
			localDb.updateTripStatusToExited(lngTripIdTemp);
			if(context instanceof TripDetailsActivity){
				((TripDetailsActivity)context).updateViews();
			} else{
				Intent intent=new Intent(context, ExpenseActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				((ActionBarActivity)context).startActivity(intent);
			}
			context.startService(new Intent(context, SyncIntentService.class));
		} else{
			localDb.deleteTrip(lngTripIdTemp);
		}

	}
	
	@SuppressLint("InflateParams")
	protected void showInfoMessage(String strMessage) {
		try{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			View view = getLayoutInflater().inflate(R.layout.registration_error_dialog, null);
			builder.setCancelable(true);
			TextView textView = (TextView)view.findViewById(R.id.error);
			Button btnOk = (Button) view.findViewById(R.id.btnOk);
			btnOk.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					alert.cancel();
				}
			});

			textView.setText(strMessage);

			alert = builder.create();
			alert.setView(view, 0, 0, 0, 0);
			alert.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.no_anim, R.anim.down_n_go);
	}
}

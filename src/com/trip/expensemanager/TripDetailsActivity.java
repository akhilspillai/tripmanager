package com.trip.expensemanager;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.trip.expensemanager.adapters.CustomPagerAdapter;
import com.trip.expensemanager.fragments.dialogs.ConfirmDialogListener;
import com.trip.expensemanager.fragments.dialogs.ConfirmationFragment;
import com.trip.expensemanager.fragments.dialogs.InfoDialogListener;
import com.trip.expensemanager.fragments.dialogs.InformationFragment;
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
	private BroadcastReceiver purchaseReceiver;
	private long lngAdminId;
	
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
			
			purchaseReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					loadAd();
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
				return;
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
			loadAd();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mAdapter.notifyDataSetChanged();
		LocalBroadcastManager.getInstance(this).registerReceiver((receiver), new IntentFilter(SyncIntentService.RESULT_SYNC));
		LocalBroadcastManager.getInstance(this).registerReceiver((purchaseReceiver), new IntentFilter(SyncIntentService.RESULT_PURCHASE));
	}

	@Override
	public void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver((receiver));
		LocalBroadcastManager.getInstance(this).unregisterReceiver((purchaseReceiver));
	}
	
	protected void loadAd() {
		AdView adView = (AdView)findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().addTestDevice("07479579BCD31CAC59F426C69FC347F0").addTestDevice("CA38245079883989F4F525CCE75019B4").addTestDevice("73F2A5CA55F628C98441DA7DAFECE33C").build();
		
		SharedPreferences prefs = getSharedPreferences(Constants.STR_PREFERENCE, MODE_PRIVATE);
		boolean isPurchased=prefs.getBoolean(Constants.STR_PURCHASED, false);
		if(isPurchased){
			adView.setVisibility(View.GONE);
		} else{
			adView.setVisibility(View.VISIBLE);
			adView.loadAd(adRequest);
		}
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
	
	
	protected void showDeleteTripDialog(final String tripName, final long tripId) {
		ConfirmDialogListener listener=new ConfirmDialogListener() {

			@Override
			public void onDialogPositiveClick(DialogFragment dialog) {
				deleteTrip(tripId);
				dialog.dismiss();
			}

			@Override
			public void onDialogNegativeClick(DialogFragment dialog) {
				dialog.dismiss();
			}
		};
		ConfirmationFragment.newInstance(tripName, "Are you sure you want to delete the expense-group "+tripName+"?",null, R.layout.fragment_dialog_confirm, listener).show(getSupportFragmentManager(), "dialog");
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
	
	
	protected void showExitEG() {
		Context context=this;
		

		ConfirmDialogListener listener=new ConfirmDialogListener() {

			@Override
			public void onDialogPositiveClick(DialogFragment dialog) {
				exitEG(lngUserId, lngTripId);
				dialog.dismiss();
			}

			@Override
			public void onDialogNegativeClick(DialogFragment dialog) {
				dialog.dismiss();
			}
		};
		TripBean tripTemp=new LocalDB(context).retrieveTripDetails(lngTripId);
		ConfirmationFragment.newInstance(tripTemp.getName(), "Are you sure you want to exit from the expense-group "+tripTemp.getName()+"?", null, R.layout.fragment_dialog_confirm, listener).show(getSupportFragmentManager(), "dialog");
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
	
	
	protected void showInfoMessage(String strMessage) {
		InfoDialogListener listener=new InfoDialogListener() {

			public void onDialogButtonClick(DialogFragment dialog) {
				dialog.dismiss();
			}
		};
		InformationFragment.newInstance("Info", strMessage,null, R.layout.fragment_dialog_info, listener).show(getSupportFragmentManager(), "dialog");
	}
	
	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.no_anim, R.anim.down_n_go);
	}
}

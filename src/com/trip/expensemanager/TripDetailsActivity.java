package com.trip.expensemanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.trip.expensemanager.adapters.CustomPagerAdapter;
import com.trip.utils.Constants;
import com.trip.utils.LocalDB;
import com.trip.utils.TripBean;

public class TripDetailsActivity extends ActionBarActivity implements TabListener {

	private long lngUserId=2L;
	private String strTripName;
	private CustomPagerAdapter mAdapter;
	private ViewPager mViewPager;
	private String[] tabs = {"Details","Expense", "People"};
	private long lngTripId;
	private BroadcastReceiver receiver;
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
			intent.getLongExtra(Constants.STR_ADMIN_ID, 0L);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//			if(savedInstanceState==null){
				final ActionBar actionBar = getSupportActionBar();

				mAdapter = new CustomPagerAdapter(getSupportFragmentManager(), strTripName, lngUserId, lngTripId);
				setContentView(R.layout.activity_trip_details);
				mViewPager = (ViewPager) findViewById(R.id.pager);
				mViewPager.setAdapter(mAdapter);
				actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

				for (String strTabName : tabs) {
					actionBar.addTab(
							actionBar.newTab()
							.setText(strTabName)
							.setTabListener(this));
				}
				mViewPager.setOnPageChangeListener(
						new ViewPager.SimpleOnPageChangeListener() {
							@Override
							public void onPageSelected(int position) {
								actionBar.setSelectedNavigationItem(position);
							}
						});
//			}
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

//	@Override
//	protected void onNewIntent(Intent intent) {
//		super.onNewIntent(intent);
//		int tabToSelect=intent.getIntExtra(Constants.STR_SHOW_TAB, 0);
//		lngUserId=intent.getLongExtra(Constants.STR_USER_ID, 0L);
//		strTripName=intent.getStringExtra(Constants.STR_TRIP_NAME);
//		lngTripId=intent.getLongExtra(Constants.STR_TRIP_ID, 0L);
//		mAdapter = new CustomPagerAdapter(getSupportFragmentManager(), strTripName, lngUserId, lngTripId);
//		mViewPager.setAdapter(mAdapter);
//		if(tabToSelect!=0){
//			mViewPager.setCurrentItem(tabToSelect-1);
//		}
//	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition());
		switch (tab.getPosition()) {
		case 0:
			getSupportActionBar().setTitle(strTripName);
			break;
		case 1:
			getSupportActionBar().setTitle(R.string.expenses);
			break;
		case 2:
			getSupportActionBar().setTitle(R.string.people);
			break;

		default:
			break;
		}
	}
	;
	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {

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
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.no_anim, R.anim.down_n_go);
	}
}

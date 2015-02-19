package com.trip.expensemanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.trip.expensemanager.fragments.AddExpenseFragment;
import com.trip.expensemanager.fragments.AddPeopleFragment;
import com.trip.expensemanager.fragments.TripExpenseFragment;
import com.trip.utils.Constants;

public class AllDetailsActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_expense);
		Intent retIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, retIntent);
		Intent intent=getIntent();
		long lngTripId=0L;
		long lngUserId=0L;
		String strTripName, strDate;
		int position;
		long lngAdminId;
		int opcode=intent.getIntExtra(Constants.STR_OPCODE,0);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		if (savedInstanceState == null) {
			switch (opcode) {
			case Constants.I_OPCODE_ADD_TRIP_QR:
				lngTripId=intent.getLongExtra(Constants.STR_TRIP_ID, 0L);
				strTripName=intent.getStringExtra(Constants.STR_TRIP_NAME);
				lngAdminId=intent.getLongExtra(Constants.STR_ADMIN_ID, 0L);
				strDate=intent.getStringExtra(Constants.STR_DATE);
				getSupportFragmentManager().beginTransaction().add(R.id.container, AddPeopleFragment.newInstance(lngTripId, strTripName, lngAdminId, strDate)).commit();
				break;
			case Constants.I_OPCODE_ADD_EXPENSE:
				lngTripId=intent.getLongExtra(Constants.STR_TRIP_ID, 0L);
				lngUserId=intent.getLongExtra(Constants.STR_USER_ID, 0L);
				lngAdminId=intent.getLongExtra(Constants.STR_ADMIN_ID, 0L);
				getSupportFragmentManager().beginTransaction().add(R.id.container, AddExpenseFragment.newInstance(lngTripId, lngUserId, opcode)).commit();
				break;
			case Constants.I_OPCODE_UPDATE_EXPENSE:
				lngTripId=intent.getLongExtra(Constants.STR_TRIP_ID, 0L);
				lngUserId=intent.getLongExtra(Constants.STR_USER_ID, 0L);
				long lngExpenseId=intent.getLongExtra(Constants.STR_EXPENSE_ID, 0L);
				position=intent.getIntExtra(Constants.STR_POSITION, 0);
				getSupportFragmentManager().beginTransaction().add(R.id.container, AddExpenseFragment.newInstance(lngTripId, lngExpenseId, lngUserId, position, opcode)).commit();
				break;
			case Constants.I_OPCODE_SHOW_USER_EXPENSES:
				lngUserId=intent.getLongExtra(Constants.STR_USER_ID, 0L);
				long lngExpenseUserId=intent.getLongExtra(Constants.STR_EXP_USR_ID, 0L);
				position=intent.getIntExtra(Constants.STR_POSITION, 0);
				lngTripId=intent.getLongExtra(Constants.STR_TRIP_ID, 0L);
				lngAdminId=intent.getLongExtra(Constants.STR_ADMIN_ID, 0L);
				getSupportFragmentManager().beginTransaction().add(R.id.container, TripExpenseFragment.newInstance(lngExpenseUserId, lngUserId, position, lngTripId, lngAdminId)).commit();
				break;
			default:
				break;
			}
			AdView adView = (AdView)findViewById(R.id.adView);
			AdRequest adRequest = new AdRequest.Builder().addTestDevice("07479579BCD31CAC59F426C69FC347F0").addTestDevice("CA38245079883989F4F525CCE75019B4").addTestDevice("73F2A5CA55F628C98441DA7DAFECE33C").build();
			if(adView.getVisibility()==View.VISIBLE){
				adView.loadAd(adRequest);
			}
		}
		
	}
	
	@Override
	public void onBackPressed() {
		if(getSupportFragmentManager().getBackStackEntryCount()==0){
			finish();
			overridePendingTransition(0, 0);
		} else{
			super.onBackPressed();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
}

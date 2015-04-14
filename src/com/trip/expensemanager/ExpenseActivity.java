package com.trip.expensemanager;

import java.util.regex.Pattern;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.trip.expensemanager.fragments.AddTripFragment;
import com.trip.expensemanager.fragments.NewUserFragment;
import com.trip.utils.Constants;
import com.trip.utils.LocalDB;

public class ExpenseActivity extends ActionBarActivity {

	private BroadcastReceiver receiver;
	private TextView txtTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		
		receiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            loadAd();
	        }
	    };
	    
		LocalDB localDb=new LocalDB(this);
		if (savedInstanceState == null) {
			long lngUserId=localDb.retrieve();
//			localDb.update("test", lngUserId);
			String strUsername=localDb.retrieveUsername();
			Pattern emailPattern = Patterns.EMAIL_ADDRESS;
			if(lngUserId==0L || !emailPattern.matcher(strUsername).matches()){
				setContentView(R.layout.activity_new_user);
				Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
				txtTitle=(TextView) findViewById(R.id.toolbar_title);
				if (toolbar != null) {
					setSupportActionBar(toolbar);
				}
				getSupportActionBar().setDisplayShowTitleEnabled(false);
				getSupportFragmentManager().beginTransaction().add(R.id.container, NewUserFragment.newInstance()).commit();
			} else{
				setContentView(R.layout.activity_expense);
				Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

				if (toolbar != null) {
					setSupportActionBar(toolbar);
				}
				getSupportFragmentManager().beginTransaction().add(R.id.container, AddTripFragment.newInstance(lngUserId, false)).commit();
			}
			Intent serviceIntent=new Intent(this, SyncIntentService.class);
			startService(serviceIntent);
			loadAd();
		}
	}

	public void setCustomTitle(int resource) {
		txtTitle.setText(resource);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onBackPressed() {
		if(getSupportFragmentManager().getBackStackEntryCount()==0){
			finish();
		} else{
			super.onBackPressed();
			overridePendingTransition(R.anim.no_anim, R.anim.down_n_go);
		}
	}
	
	protected void loadAd() {
		AdView adView = (AdView)findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().addTestDevice("07479579BCD31CAC59F426C69FC347F0").addTestDevice("CA38245079883989F4F525CCE75019B4").addTestDevice("73F2A5CA55F628C98441DA7DAFECE33C").build();
		
		SharedPreferences prefs = getSharedPreferences(Constants.STR_PREFERENCE, MODE_PRIVATE);
		boolean isPurchased=prefs.getBoolean(Constants.STR_PURCHASED, false);
		if(adView!=null){
			if(isPurchased){
				adView.setVisibility(View.GONE);
			} else{
				adView.setVisibility(View.VISIBLE);
				adView.loadAd(adRequest);
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		LocalBroadcastManager.getInstance(this).registerReceiver((receiver), new IntentFilter(SyncIntentService.RESULT_PURCHASE));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver((receiver));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}

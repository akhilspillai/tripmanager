package com.trip.expensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.trip.expensemanager.fragments.AddTripFragment;
import com.trip.expensemanager.fragments.LoginFragment;
import com.trip.expensemanager.fragments.UpdatesFragment;
import com.trip.utils.LocalDB;

public class UpdatesActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_expense);
		getSupportActionBar().setTitle(R.string.updates);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, UpdatesFragment.newInstance()).commit();
			AdView adView = (AdView)findViewById(R.id.adView);
			AdRequest adRequest = new AdRequest.Builder().addTestDevice("73F2A5CA55F628C98441DA7DAFECE33C").addTestDevice("D343E752F78628B89D77D8DC1FB8F12F").addTestDevice("E0AD16F9A6CB88345E2E96D2323E9BB7").build();
			adView.loadAd(adRequest);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onBackPressed() {
		if(getSupportFragmentManager().getBackStackEntryCount()==0){
			new LocalDB(this).deleteAllToSync();
			finish();
		} else{
			super.onBackPressed();
		}
	}

}

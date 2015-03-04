package com.trip.expensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

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
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
		getSupportActionBar().setTitle(R.string.updates);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, UpdatesFragment.newInstance()).commit();
			AdView adView = (AdView)findViewById(R.id.adView);
			AdRequest adRequest = new AdRequest.Builder().addTestDevice("07479579BCD31CAC59F426C69FC347F0").addTestDevice("CA38245079883989F4F525CCE75019B4").addTestDevice("73F2A5CA55F628C98441DA7DAFECE33C").build();
			if(adView.getVisibility()==View.VISIBLE){
				adView.loadAd(adRequest);
			}
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

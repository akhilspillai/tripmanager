package com.trip.expensemanager.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.trip.expensemanager.fragments.TripDetailsFragment;
import com.trip.expensemanager.fragments.TripExpenseFragment;
import com.trip.expensemanager.fragments.TripPeopleFragment;

public class CustomPagerAdapter extends FragmentStatePagerAdapter{

	private String strTrip;
	private long lngUserId;
	private long lngTripId;

	public CustomPagerAdapter(FragmentManager fm, String strTrip, long lngUserId, long lngTripId) {
		super(fm);
		this.strTrip=strTrip;
		this.lngUserId=lngUserId;
		this.lngTripId=lngTripId;
	}

	@Override
	public Fragment getItem(int position) {
		Fragment fragment=null;
		if(position==0){
			fragment=TripDetailsFragment.newInstance(strTrip, lngUserId, lngTripId);
		} else if(position==1){
			fragment=TripExpenseFragment.newInstance(strTrip, lngUserId, lngTripId);
		} else if(position==2){
			fragment=TripPeopleFragment.newInstance(strTrip, lngUserId, lngTripId);
		}
		return fragment;
	}

	@Override
	public int getCount() {
		return 3;
	}

	@Override
	public int getItemPosition(Object object){
		return POSITION_NONE;
	}

}

package com.trip.expensemanager;

import com.trip.utils.Global;
import com.trip.utils.LocalDB;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ConnectivityChangeListener extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
    	if(Global.isConnected(context)){
    		long lngUserId=new LocalDB(context).retrieve();
			if(lngUserId!=0L){
				Intent serviceIntent=new Intent(context, SyncIntentService.class);
				context.startService(serviceIntent);
			}
    	}
    }
}

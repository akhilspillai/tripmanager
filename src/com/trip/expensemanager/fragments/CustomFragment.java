package com.trip.expensemanager.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.EditText;

import com.trip.expensemanager.R;
import com.trip.expensemanager.UpdateLoginTask;
import com.trip.expensemanager.fragments.dialogs.ConfirmDialogListener;
import com.trip.expensemanager.fragments.dialogs.ConfirmationFragment;
import com.trip.expensemanager.fragments.dialogs.InfoDialogListener;
import com.trip.expensemanager.fragments.dialogs.InformationFragment;
import com.trip.utils.Constants;
import com.trip.utils.Global;
import com.trip.utils.LocalDB;
import com.trip.utils.billing.IabHelper;
import com.trip.utils.billing.IabResult;
import com.trip.utils.billing.Purchase;

public class CustomFragment extends Fragment {
	private static final int ORDER_ID = 1001;

	private IabHelper mHelper;
	private boolean isPurchaseReady=false;

	private String strUniqueString;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


	}

	protected void purchaseItem() {
		if(!isPurchaseReady){
			String base64EncodedPublicKey=Constants.STR_LICENSE_1+
					Constants.STR_LICENSE_2+Constants.STR_LICENSE_3+
					Constants.STR_LICENSE_4+Constants.STR_LICENSE_5+
					Constants.STR_LICENSE_6+Constants.STR_LICENSE_7;

			mHelper = new IabHelper(getActivity(), base64EncodedPublicKey);
			mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
				public void onIabSetupFinished(IabResult result) {
					if (!result.isSuccess()) {
						Log.d("Akhil", "Problem setting up In-app Billing: " + result);
						return;
					}
					isPurchaseReady=true;
					continuePurchase();
				}
			});
		} else{
			continuePurchase();
		}
	}

	protected void continuePurchase(){
		IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener 
		= new IabHelper.OnIabPurchaseFinishedListener() {
			public void onIabPurchaseFinished(IabResult result, Purchase purchase) 
			{
				if (result.isFailure()) {
					Log.d("Akhil", "Error purchasing: " + result);
					showMessage("There was an error in purchasing! Please try again.");
					return;
				}      
				else if (purchase.getSku().equals(Constants.STR_SKU_PREMIUM)) {
					String strUniqueReturn=purchase.getDeveloperPayload();
					if(strUniqueReturn!=null && strUniqueReturn.equals(strUniqueString)){
						SharedPreferences prefs = getActivity().getSharedPreferences(Constants.STR_PREFERENCE, Activity.MODE_PRIVATE);
						prefs.edit().putBoolean(Constants.STR_PURCHASED, true).commit();
						new LocalDB(getActivity()).updatePurchaseId(strUniqueString);
						showInfoMessage("Purchase was successful. Please restart the application and enjoy you upgrade!! :)");
						if(Global.isConnected(getActivity())){
							new UpdateLoginTask(getActivity()).execute();
						}
					} else{
						showMessage("There was an error in purchasing! Please try again.");
					}
				}
			}
		};
		strUniqueString=Global.randomString(25);
		if (mHelper != null) mHelper.flagEndAsync();
		mHelper.launchPurchaseFlow(getActivity(), Constants.STR_SKU_PREMIUM, ORDER_ID,   
				mPurchaseFinishedListener, strUniqueString);
	}

	protected void showMessage(String strMessage) {
		InfoDialogListener listener=new InfoDialogListener() {

			public void onDialogButtonClick(DialogFragment dialog) {
				dialog.dismiss();
			}
		};
		InformationFragment.newInstance("Error", "Oops!! "+strMessage,null, R.layout.fragment_dialog_info, listener).show(getActivity().getSupportFragmentManager(), "dialog");
	}

	protected void showInfoMessage(String strMessage) {
		InfoDialogListener listener=new InfoDialogListener() {

			public void onDialogButtonClick(DialogFragment dialog) {
				dialog.dismiss();
			}
		};
		InformationFragment.newInstance("Info", strMessage,null, R.layout.fragment_dialog_info, listener).show(getActivity().getSupportFragmentManager(), "dialog");
	}

	protected void showError(EditText et, String strError){
		Drawable d= getActivity().getResources().getDrawable(R.drawable.error);
		d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
		et.setError(strError, d);
		et.requestFocus();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mHelper != null) mHelper.dispose();
		mHelper = null;
		isPurchaseReady=false;
	}
	
	protected void showUpgradeDialog(String strContent) {
		ConfirmDialogListener listener=new ConfirmDialogListener() {

			@Override
			public void onDialogPositiveClick(DialogFragment dialog) {
				
				if(!isPurchased()){
					purchaseItem();
				} else{
					showInfoMessage("The item is already purchased!!");
				}
				dialog.dismiss();
			}

			@Override
			public void onDialogNegativeClick(DialogFragment dialog) {
				dialog.dismiss();
			}
		};
		ConfirmationFragment.newInstance("Upgrade", strContent, getActivity().getResources().getString(R.string.upgrade), R.layout.fragment_dialog_confirm, listener).show(getActivity().getSupportFragmentManager(), "dialog");
	}
	
	protected boolean isPurchased() {
		SharedPreferences prefs = getActivity().getSharedPreferences(Constants.STR_PREFERENCE, Activity.MODE_PRIVATE);
		return prefs.getBoolean(Constants.STR_PURCHASED, false);
	}
	
	protected void showGMSNotFoundDialog() {
		ConfirmDialogListener listener=new ConfirmDialogListener() {

			@Override
			public void onDialogPositiveClick(DialogFragment dialog) {
				try {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.gms")));
				} catch (android.content.ActivityNotFoundException anfe) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.google.android.gms")));
				}
				dialog.dismiss();
			}

			@Override
			public void onDialogNegativeClick(DialogFragment dialog) {
				dialog.dismiss();
			}
		};
		ConfirmationFragment.newInstance("No Google Play Services", "Oops!! Seems like you don't have google play services installed. Please install and try again!!", "Install", R.layout.fragment_dialog_confirm, listener).show(getActivity().getSupportFragmentManager(), "dialog");
	}
}

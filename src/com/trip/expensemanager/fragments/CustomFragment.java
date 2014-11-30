package com.trip.expensemanager.fragments;

import com.trip.expensemanager.R;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CustomFragment extends Fragment {
	protected AlertDialog alert;
	
	@SuppressLint("InflateParams")
	protected void showMessage(String strMessage) {
		try{
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			View view = getActivity().getLayoutInflater().inflate(R.layout.registration_error_dialog, null);
			builder.setCancelable(true);
			TextView textView = (TextView)view.findViewById(R.id.error);
			Button btnOk = (Button) view.findViewById(R.id.btnOk);
			btnOk.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					alert.cancel();
				}
			});

			textView.setText("Oops!! "+strMessage);

			alert = builder.create();
			alert.setView(view, 0, 0, 0, 0);
			alert.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressLint("InflateParams")
	protected void showInfoMessage(String strMessage) {
		try{
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			View view = getActivity().getLayoutInflater().inflate(R.layout.registration_error_dialog, null);
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
	
	protected void showError(EditText et, String strError){
		Drawable d= getActivity().getResources().getDrawable(R.drawable.error);
		d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
		et.setError(strError, d);
		et.requestFocus();
	}
}

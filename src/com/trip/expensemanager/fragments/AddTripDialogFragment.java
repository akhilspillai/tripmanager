package com.trip.expensemanager.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.trip.expensemanager.R;

public class AddTripDialogFragment extends DialogFragment {

	ConfirmDialogListener mListener;

	public AddTripDialogFragment(ConfirmDialogListener listener) {
		this.mListener=listener;
	}

	public static AddTripDialogFragment newInstance(ConfirmDialogListener listener) {
		AddTripDialogFragment fragment=null;
		try {
			fragment=new AddTripDialogFragment(listener);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_FRAME, R.style.ThemeDialogCustom);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.add_trip_dialog, container);

        TextView eTxtTripName = (EditText)view.findViewById(R.id.etxt_trip_name);
		final Button btnOk = (Button) view.findViewById(R.id.btn_ok);
		Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
		eTxtTripName.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				btnOk.setEnabled(false);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if(s.length()!=0){
					btnOk.setEnabled(true);
				}
			}
		});
		btnOk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mListener.onDialogPositiveClick(AddTripDialogFragment.this);
			}
		});
		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mListener.onDialogNegativeClick(AddTripDialogFragment.this);
			}
		});

        return view;
	}

}

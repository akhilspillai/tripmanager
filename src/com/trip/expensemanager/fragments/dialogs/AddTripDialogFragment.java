package com.trip.expensemanager.fragments.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.trip.expensemanager.R;
import com.trip.utils.Constants;

public class AddTripDialogFragment extends DialogFragment {

	ConfirmDialogListener mListener;
	private CharSequence strAction;
	private String strText;

	public AddTripDialogFragment(ConfirmDialogListener listener) {
		this.mListener=listener;
	}

	public static AddTripDialogFragment newInstance(String strAction, String strText, ConfirmDialogListener listener) {
		AddTripDialogFragment fragment=null;
		try {
			fragment=new AddTripDialogFragment(listener);
			Bundle bundle=new Bundle();
			bundle.putString(Constants.STR_ACTION, strAction);
			bundle.putString(Constants.STR_TEXT, strText);
			fragment.setArguments(bundle);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.strAction=getArguments().getString(Constants.STR_ACTION);
		this.strText=getArguments().getString(Constants.STR_TEXT);
	}

	@SuppressLint("InflateParams")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflator = getActivity().getLayoutInflater();
		View view = inflator.inflate(R.layout.add_trip_dialog, null);
		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
		builder.setView(view);
		
		EditText eTxtTripName = (EditText)view.findViewById(R.id.etxt_trip_name);
		final Button btnOk = (Button) view.findViewById(R.id.btn_ok);
		Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
		btnOk.setText(strAction);
        eTxtTripName.setText(strText);
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
		return builder.create();
	}
	
	/*@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.add_trip_dialog, container);

        TextView eTxtTripName = (EditText)view.findViewById(R.id.etxt_trip_name);
		final Button btnOk = (Button) view.findViewById(R.id.btn_ok);
		Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
		btnOk.setText(strAction);
        eTxtTripName.setText(strText);
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
	}*/

}

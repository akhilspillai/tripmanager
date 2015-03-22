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
import android.widget.TextView;
import com.trip.expensemanager.R;
import com.trip.utils.Constants;

public class SettleDebtDialogFragment extends DialogFragment {

	ConfirmDialogListener mListener;
	private String strText;
	private String strAmount;

	public SettleDebtDialogFragment(ConfirmDialogListener listener) {
		this.mListener=listener;
	}

	public static SettleDebtDialogFragment newInstance(String strText, String strAmount, ConfirmDialogListener listener) {
		SettleDebtDialogFragment fragment=null;
		try {
			fragment=new SettleDebtDialogFragment(listener);
			Bundle bundle=new Bundle();
			bundle.putString(Constants.STR_AMOUNT, strAmount);
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
		this.strText=getArguments().getString(Constants.STR_TEXT);
		this.strAmount=getArguments().getString(Constants.STR_AMOUNT);
	}
	
	@SuppressLint("InflateParams")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflator = getActivity().getLayoutInflater();
		View view = inflator.inflate(R.layout.settle_debt_dialog, null);
		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
		builder.setView(view);

		TextView txtContent = (TextView)view.findViewById(R.id.tv_message);
		EditText eTxtAmount = (EditText)view.findViewById(R.id.et_amount);
		 
		final Button btnOk = (Button) view.findViewById(R.id.btn_ok);
		Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
		txtContent.setText(strText);
		eTxtAmount.setText(strAmount);
		
		eTxtAmount.addTextChangedListener(new TextWatcher() {

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
					String strAmount=s.toString();
					float fAmount;
					try {
						fAmount=Float.parseFloat(strAmount);
					} catch (NumberFormatException e) {
						fAmount=0;
					}
					if(fAmount!=0){
						btnOk.setEnabled(true);
					}
				}
			}
		});
		btnOk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mListener.onDialogPositiveClick(SettleDebtDialogFragment.this);
			}
		});
		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mListener.onDialogNegativeClick(SettleDebtDialogFragment.this);
			}
		});
		
		return builder.create();
	}
	
}

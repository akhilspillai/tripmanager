package com.trip.expensemanager.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.trip.expensemanager.R;
import com.trip.utils.Constants;

public class ConfirmationFragment extends DialogFragment {

	ConfirmDialogListener mListener;
	private String strTitle, strContent, strAction;
	private int iLayout;

	public ConfirmationFragment(ConfirmDialogListener listener) {
		this.mListener=listener;
	}

	public static ConfirmationFragment newInstance(String strTitle, String strContent,
			String strAction, int iLayoutId, ConfirmDialogListener listener) {
		ConfirmationFragment fragment=null;
		try {
			fragment=new ConfirmationFragment(listener);
			Bundle bundle=new Bundle();
			bundle.putString(Constants.STR_TITLE, strTitle);
			bundle.putString(Constants.STR_CONTENT, strContent);
			bundle.putString(Constants.STR_ACTION, strAction);
			bundle.putInt(Constants.STR_LAYOUT, iLayoutId);
			fragment.setArguments(bundle);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_FRAME, R.style.ThemeDialogCustom);
		Bundle bundle=getArguments();
		this.strTitle=bundle.getString(Constants.STR_TITLE);
		this.strContent=bundle.getString(Constants.STR_CONTENT);
		this.iLayout=bundle.getInt(Constants.STR_LAYOUT, 0);
		this.strAction=bundle.getString(Constants.STR_ACTION);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(iLayout, container);

        TextView tvTitle=(TextView) view.findViewById(R.id.tv_title);
		TextView tvContent=(TextView) view.findViewById(R.id.tv_content);
		Button btnOk=(Button) view.findViewById(R.id.btn_ok);
		Button btnCancel=(Button) view.findViewById(R.id.btn_cancel);
		tvTitle.setText(strTitle);
		tvContent.setText(strContent);
		if(strAction!=null){
			btnOk.setText(strAction);
		}
		btnOk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mListener.onDialogPositiveClick(ConfirmationFragment.this);
			}
		});
		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mListener.onDialogNegativeClick(ConfirmationFragment.this);
			}
		});

        return view;
	}

}

package com.trip.expensemanager.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.trip.expensemanager.R;
import com.trip.utils.Constants;

public class InformationFragment extends DialogFragment {

	InfoDialogListener mListener;
	private String strTitle, strContent, strAction;
	private int iLayout;

	public InformationFragment(InfoDialogListener listener) {
		this.mListener=listener;
	}

	public static InformationFragment newInstance(String strTitle, String strContent, String strAction, int iLayoutId, InfoDialogListener listener) {
		InformationFragment fragment=null;
		try {
			fragment=new InformationFragment(listener);
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
		this.strAction=bundle.getString(Constants.STR_ACTION);
		this.iLayout=bundle.getInt(Constants.STR_LAYOUT, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(iLayout, container);

        TextView tvTitle=(TextView) view.findViewById(R.id.tv_title);
		TextView tvContent=(TextView) view.findViewById(R.id.tv_content);
		Button btnOk=(Button) view.findViewById(R.id.btn_ok);
		tvTitle.setText(strTitle);
		tvContent.setText(strContent);
		if(strAction!=null){
			btnOk.setText(strAction);
		}
		btnOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mListener.onDialogButtonClick(InformationFragment.this);
			}
		});

        return view;
	}
	
}

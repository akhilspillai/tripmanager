package com.trip.expensemanager.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.trip.expensemanager.R;
import com.trip.expensemanager.fragments.AddExpenseFragment;

public class CustomAddExpenseAdapter extends ArrayAdapter<String> {


	private Context context;
	private List<String> users;
	private List<String> amounts;
	private List<Boolean> isCheckedLst;
	private List<Boolean> isEnabledLst;
	private AddExpenseFragment fragment;

	static class ViewHolder {
		public EditText etAmount;
		public CheckBox cbUser;
	}

	public CustomAddExpenseAdapter(Context context, List<String> users, List<String> amounts, List<Boolean> isCheckedLst, List<Boolean> isEnabledLst, AddExpenseFragment fragment) {
		super(context, R.layout.userids_row_layout, users);
		this.context=context;
		this.users=users;
		this.amounts=amounts;
		this.isCheckedLst=isCheckedLst;
		this.isEnabledLst=isEnabledLst;
		this.fragment=fragment;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;

		final int pos=position;
		
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.userids_row_layout, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.etAmount = (EditText) rowView.findViewById(R.id.et_amount);
			viewHolder.cbUser = (CheckBox) rowView.findViewById(R.id.cb_user);
			rowView.setTag(viewHolder);
		}
		ViewHolder viewHolder=(ViewHolder) rowView.getTag();
		viewHolder.cbUser.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isCheckedLst.get(pos)!=isChecked){
					isCheckedLst.set(pos, isChecked);
					fragment.changeData(false);
				}
			}
		});
		
		if(isCheckedLst.get(position)){
			viewHolder.cbUser.setChecked(true);
			viewHolder.etAmount.setEnabled(true);
		} else{
			viewHolder.cbUser.setChecked(false);
			viewHolder.etAmount.setEnabled(false);
			amounts.set(position, "0");
		}
		if(!isEnabledLst.get(position)){
			viewHolder.cbUser.setEnabled(false);
			viewHolder.etAmount.setEnabled(false);
		}
		viewHolder.cbUser.setText(users.get(position));
		viewHolder.etAmount.setText(amounts.get(position));
		viewHolder.etAmount.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){
					amounts.set(pos, ((EditText)v).getText().toString());
				}
			}
		});
		
		return rowView;
	}

	@Override
	public String getItem(int position) {
		return users.get(position);
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

}
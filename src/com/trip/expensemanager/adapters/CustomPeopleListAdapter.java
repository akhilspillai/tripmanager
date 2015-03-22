package com.trip.expensemanager.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trip.expensemanager.R;

public class CustomPeopleListAdapter extends ArrayAdapter<String> {

	private Context context;
	private List<String> items;
	private List<Boolean> synced;
	private List<String> amount;
	private List<Integer> colors;

	static class ViewHolder {
		public TextView tvLabel;
		public TextView tvAmt;
		public ImageView ivStatus;
		public TextView tvIcon;
		public RelativeLayout rlIcon;
	}

	public CustomPeopleListAdapter(Context context, List<String> items, List<String> amount, List<Boolean> synced, List<Integer> colors) {
		super(context, R.layout.people_row_layout, items);
		this.context=context;
		this.items=items;
		this.amount=amount;
		this.synced=synced;
		this.colors=colors;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		// reuse views
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.people_row_layout, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.tvLabel = (TextView) rowView.findViewById(R.id.tv_label);
			viewHolder.tvAmt = (TextView) rowView.findViewById(R.id.tv_amount);
			viewHolder.ivStatus = (ImageView) rowView.findViewById(R.id.iv_expense_status);
			viewHolder.tvIcon = (TextView) rowView.findViewById(R.id.tv_icon_view);
			viewHolder.rlIcon = (RelativeLayout) rowView.findViewById(R.id.rl1);
			rowView.setTag(viewHolder);
		}
		String strItem=items.get(position);
		ViewHolder viewHolder=(ViewHolder) rowView.getTag();
		viewHolder.tvLabel.setText(strItem);
		char cIcon=strItem.toUpperCase().charAt(0);
		viewHolder.tvIcon.setText(String.valueOf(cIcon));

		int color=colors.get(position);
		viewHolder.rlIcon.setBackgroundColor(color);
		viewHolder.tvAmt.setText(amount.get(position));
		
		if(synced.get(position)){
			viewHolder.ivStatus.setImageResource(R.drawable.ic_expense_synched);
		} else{
			viewHolder.ivStatus.setImageResource(R.drawable.ic_not_synched);
		}
		/*Animation animation = AnimationUtils.loadAnimation(getContext(), (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
		rowView.startAnimation(animation);
		lastPosition = position;
	    animation = null;*/
		
		return rowView;
	}

	@Override
	public String getItem(int position) {
		return items.get(position);
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

}
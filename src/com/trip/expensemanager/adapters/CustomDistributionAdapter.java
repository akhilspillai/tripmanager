package com.trip.expensemanager.adapters;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trip.expensemanager.R;

public class CustomDistributionAdapter extends ArrayAdapter<String> {

	private Context context;
	private List<String> items;
	private int lastPosition=-1;

	static class ViewHolder {
		public TextView tvLabel;
		public ImageView ivIcon;
	}

	public CustomDistributionAdapter(Context context, List<String> items) {
		super(context, R.layout.distribution_row_layout, items);
		this.context=context;
		this.items=items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		// reuse views
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.distribution_row_layout, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.tvLabel = (TextView) rowView.findViewById(R.id.tv_distribution);
			rowView.setTag(viewHolder);
		}
		String strItem=items.get(position);
		ViewHolder viewHolder=(ViewHolder) rowView.getTag();
		viewHolder.tvLabel.setText(strItem);
		
		Animation animation = AnimationUtils.loadAnimation(getContext(), (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
		rowView.startAnimation(animation);
		lastPosition = position;
	    animation = null;
		
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
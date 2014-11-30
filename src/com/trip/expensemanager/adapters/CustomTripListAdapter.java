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

public class CustomTripListAdapter extends ArrayAdapter<String> {

	private Context context;
	private List<String> items;
	private List<String> creationDates;
	private List<Boolean> closed;
	private List<Integer> synched;
	private int lastPosition = -1;

	static class ViewHolder {
		public TextView tvLabel;
		public TextView tvCreationTime;
		public ImageView ivStatus;
		public TextView tvIcon;
		public ProgressBar pbAddTrip;
		public RelativeLayout rlIcon;
	}

	public CustomTripListAdapter(Context context, List<String> items, List<String> creationDates, List<Boolean> closed, List<Integer> synched) {
		super(context, R.layout.trips_row_layout, items);
		this.context=context;
		this.items=items;
		this.creationDates=creationDates;
		this.closed=closed;
		this.synched=synched;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		// reuse views
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.trips_row_layout, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.tvLabel = (TextView) rowView.findViewById(R.id.tv_label);
			viewHolder.tvCreationTime = (TextView) rowView.findViewById(R.id.tv_creation_time);
			viewHolder.tvIcon = (TextView) rowView.findViewById(R.id.tv_icon_view);
			viewHolder.ivStatus = (ImageView) rowView.findViewById(R.id.iv_trip_status);
			viewHolder.pbAddTrip = (ProgressBar) rowView.findViewById(R.id.pb_trip_add);
			viewHolder.rlIcon = (RelativeLayout) rowView.findViewById(R.id.rl1);
			rowView.setTag(viewHolder);
		}
		String strItem=items.get(position);
		ViewHolder viewHolder=(ViewHolder) rowView.getTag();
		viewHolder.tvLabel.setText(strItem);
		char cIcon=strItem.toUpperCase().charAt(0);
		viewHolder.tvIcon.setText(String.valueOf(cIcon));
		int iIcon=cIcon;
		int color=Color.BLUE;

		int value=iIcon%6;
		switch (value) {
		case 0:
			color=Color.rgb(60,179,113);
			break;
		case 1:
			color=Color.rgb(178,34,34);
			break;
		case 2:
			color=Color.rgb(30,144,255);
			break;
		case 3:
			color=Color.rgb(160,82,45);
			break;
		case 4:
			color=Color.rgb(250,164,96);
			break;

		case 5:
			color=Color.rgb(100,149,237);
			break;
		}
		viewHolder.rlIcon.setBackgroundColor(color);
		viewHolder.tvCreationTime.setText(creationDates.get(position));
		if(synched.get(position)==0 || synched.get(position)==3){
			viewHolder.ivStatus.setImageResource(R.drawable.ic_not_synched);
			viewHolder.pbAddTrip.setVisibility(View.GONE);
			viewHolder.ivStatus.setVisibility(View.VISIBLE);
		} else if(synched.get(position)==1){
			viewHolder.ivStatus.setVisibility(View.INVISIBLE);
			viewHolder.pbAddTrip.setVisibility(View.VISIBLE);
		}else{
			if(closed.get(position)){
				viewHolder.ivStatus.setImageResource(R.drawable.ic_trip_closed);
			} else{
				viewHolder.ivStatus.setImageResource(R.drawable.ic_trip_open);
			}
			viewHolder.pbAddTrip.setVisibility(View.GONE);
			viewHolder.ivStatus.setVisibility(View.VISIBLE);
			
			Animation animation = AnimationUtils.loadAnimation(getContext(), (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
			rowView.startAnimation(animation);
			lastPosition = position;
		    animation = null;
	
		}
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
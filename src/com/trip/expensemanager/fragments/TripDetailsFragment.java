package com.trip.expensemanager.fragments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.util.LongSparseArray;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trip.expensemanager.R;
import com.trip.expensemanager.SyncIntentService;
import com.trip.expensemanager.TripDetailsActivity;
import com.trip.expensemanager.adapters.CustomDistributionAdapter;
import com.trip.utils.Constants;
import com.trip.utils.DistributionBean;
import com.trip.utils.DistributionBean1;
import com.trip.utils.ExpenseBean;
import com.trip.utils.Global;
import com.trip.utils.LocalDB;
import com.trip.utils.TripBean;

public class TripDetailsFragment extends CustomFragment implements OnClickListener, OnCheckedChangeListener, OnItemClickListener {

	public static TripDetailsFragment newInstance(String strTrip, long lngUserId, long lngTripId) {
		TripDetailsFragment fragment=null;
		try {
			fragment=new TripDetailsFragment();
			Bundle bundle=new Bundle();
			bundle.putString(Constants.STR_TRIP_NAME, strTrip);
			bundle.putLong(Constants.STR_USER_ID, lngUserId);
			bundle.putLong(Constants.STR_TRIP_ID, lngTripId);
			fragment.setArguments(bundle);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fragment;
	}
	protected AlertDialog alert;
	private long lngUserId;
	private ArrayList<ExpenseBean> arrExpenses;
	private long lngTripId;
	private String strTripName;
	private TextView txtTripName;
	private ImageView ivEdit;
	private LinearLayout llChartContainer;
	//	private String[] code;
	private GraphicalView mChart;
	private TextView txtNoExpenses;
	private LongSparseArray<DistributionBean> expenseSparseArr=new LongSparseArray<DistributionBean>();
	private String strTotalSpent="0";
	private ArrayList<String> arrDistribution=new ArrayList<String>();
	private ArrayList<Long> arrDistUsrIds=new ArrayList<Long>();
	private ListView lvDistributionList;
	private ArrayAdapter<String> listAdapter;
	private EditText eTxtTripName;
	private Button btnModify;
	private Button btnCancel;
	private TextView txtDistribution;
	private long lngAdminId;
	private TextView txtTripAmount;
	private ArrayList<DistributionBean1> arrDistfromDB;
	private RadioGroup floatingBarHeader;
	private RadioButton btnUnsettled;
	private RadioButton btnSettled;
	private RadioGroup rgDist;
	private List<Boolean> arrPossibletoSettle=new ArrayList<Boolean>();
	private Button btnYes;
	protected String strAmount;
	@SuppressLint({ "InflateParams", "InlinedApi" })
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState) {
		View rootView=null;
		try{
			rootView=inflater.inflate(R.layout.fragment_distribution_list, container, false);
			Bundle bundle=getArguments();
			lngUserId=bundle.getLong(Constants.STR_USER_ID);
			lngTripId=bundle.getLong(Constants.STR_TRIP_ID);
			TripBean trip = new LocalDB(getActivity()).retrieveTripDetails(lngTripId);
			if(trip==null){
				getActivity().finish();
			}
			lngTripId=trip.getId();
			strTripName=trip.getName();
			lvDistributionList=(ListView) rootView.findViewById(R.id.lv_distribution);

			View header = inflater.inflate(R.layout.fragment_trip_details, null, false); 

			lvDistributionList.addHeaderView(header);
			listAdapter = new CustomDistributionAdapter(getActivity(), arrDistribution, arrPossibletoSettle, this);
			lvDistributionList.setAdapter(listAdapter);


			txtTripName=(TextView)rootView.findViewById(R.id.txt_trip_name);
			ivEdit=(ImageView)rootView.findViewById(R.id.iv_edit_trip);
			txtNoExpenses=(TextView)rootView.findViewById(R.id.tv_no_expenses);
			txtDistribution=(TextView)rootView.findViewById(R.id.txt_distribution);

			llChartContainer=(LinearLayout)rootView.findViewById(R.id.ll_chart_container);
			txtTripAmount=(TextView)rootView.findViewById(R.id.txt_trip_amount);
			rgDist=(RadioGroup)rootView.findViewById(R.id.rg);


			btnUnsettled=(RadioButton)rootView.findViewById(R.id.btn_unsettled);
			btnSettled=(RadioButton)rootView.findViewById(R.id.btn_settled);

			txtTripName.setText(strTripName);
			ivEdit.setOnClickListener(this);
			lvDistributionList.setOnItemClickListener(this);
			lvDistributionList.setDivider(null);
			loadData(trip);
			setHasOptionsMenu(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rootView;
	}

	private void loadData(TripBean trip) {
		arrDistribution.removeAll(arrDistribution);
		try{
			LocalDB localDb=new LocalDB(getActivity());
			lngAdminId=trip.getAdminId();

			if(trip.getSyncStatus().equals(Constants.STR_QR_ADDED)){
				ivEdit.setVisibility(View.INVISIBLE);
			}
			arrExpenses = localDb.retrieveExpenses(lngTripId);
			if(arrExpenses.size()==0){
				txtNoExpenses.setVisibility(View.VISIBLE);
				txtTripAmount.setText("Total amount spent:0");
				arrDistribution.add("Nobody owes anybody anything!!");
				arrPossibletoSettle.add(false);
				listAdapter.notifyDataSetChanged();
			} else{
				txtNoExpenses.setVisibility(View.INVISIBLE);
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) txtDistribution.getLayoutParams();
				params.addRule(RelativeLayout.BELOW, R.id.ll_chart_container);
				long userId=0L;
				String strUser=null;
				DistributionBean dbTemp=null;
				int indexOfExpense;
				float spentAmount=0f;
				for(ExpenseBean expense:arrExpenses){
					strTotalSpent=Global.add(strTotalSpent, expense.getAmount());
					userId=expense.getUserId();
					indexOfExpense= expenseSparseArr.indexOfKey(userId);
					if(indexOfExpense>=0){
						dbTemp=expenseSparseArr.get(userId);
						spentAmount=dbTemp.getAmount();
					} else{
						strUser=localDb.retrievePrefferedName(userId);
						dbTemp=new DistributionBean();
						dbTemp.setUsername(strUser);
					}
					spentAmount=Global.add(spentAmount, expense.getAmount());
					dbTemp.setAmount(spentAmount);
					expenseSparseArr.put(userId, dbTemp);
				}

				txtTripAmount.setText("Total amount spent:"+strTotalSpent);
				
				rgDist.setOnCheckedChangeListener(this);
				if(btnUnsettled.isChecked()){
					showUnsettledDistribution();
				} else{
					showSettledDistribution();
				}
				openChart();

			}
		}catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		reBuildDistribution();
	}

	private void reBuildDistribution() {
		if(btnUnsettled.isChecked()){
			showUnsettledDistribution();
		} else{
			showSettledDistribution();
		}
	}

	private void showSettledDistribution() {
		LocalDB localDb=new LocalDB(getActivity());
		arrDistribution.removeAll(arrDistribution);
		arrDistUsrIds.removeAll(arrDistUsrIds);
		arrPossibletoSettle.removeAll(arrPossibletoSettle);
		arrDistfromDB=localDb.retrieveSettledDistributionForTrip(lngTripId);
		long fromId, toId;
		String toUser, fromUser, strAmount, tempUser;
		float fAmount;
		if(arrDistfromDB.size()==0){
			arrDistribution.add("No settled debts!!");
			arrPossibletoSettle.add(false);
			arrDistUsrIds.add(0L);
		} else{
			for(DistributionBean1 dist:arrDistfromDB){
				fromId=dist.getFromId();
				toId=dist.getToId();
				fromUser=localDb.retrievePrefferedName(fromId);

				toUser=localDb.retrievePrefferedName(toId);
				strAmount=dist.getAmount();
				fAmount=Float.parseFloat(strAmount);
				long tempId;
				if(fAmount<0){
					tempUser=fromUser;
					fromUser=toUser;
					toUser=tempUser;
					strAmount=strAmount.substring(1);
					tempId=fromId;
					fromId=toId;
					toId=tempId;
				}
				if(toUser.equalsIgnoreCase(Constants.STR_YOU)){
					toUser="you";
				}
				if(fromId!=toId && fAmount!=0){
					arrDistUsrIds.add(fromId);
					arrDistribution.add(fromUser+" paid "+toUser+" back an amount of "+strAmount);
				}
				arrPossibletoSettle.add(false);
			}
		}

		tempUser=null;
		if(arrDistribution.size()==0){
			arrDistribution.add("Nobody owes anybody anything!!");
		}
		listAdapter.notifyDataSetChanged();
	}

	private void showUnsettledDistribution() {
		LocalDB localDb=new LocalDB(getActivity());
		arrDistribution.removeAll(arrDistribution);
		arrPossibletoSettle.removeAll(arrPossibletoSettle);
		arrDistUsrIds.removeAll(arrDistUsrIds);
		arrDistfromDB=localDb.retrieveUnsettledDistributionForTrip(lngTripId);
		DistributionBean1 distBean=localDb.retrieveUnsettledDistributionByUsers(lngUserId, lngUserId, lngTripId);
		if(distBean!=null){
			arrDistribution.add("Own expense: "+distBean.getAmount());
			arrPossibletoSettle.add(false);
			arrDistUsrIds.add(0L);
		}
		long fromId, toId;
		String toUser, fromUser, strAmount, tempUser;
		float fAmount;

		if(arrDistfromDB.size()==0){
			arrDistribution.add("No unsettled debts!!");
			arrPossibletoSettle.add(false);
		} else{
			for(DistributionBean1 dist:arrDistfromDB){
				fromId=dist.getFromId();
				toId=dist.getToId();
				fromUser=localDb.retrievePrefferedName(fromId);

				toUser=localDb.retrievePrefferedName(toId);
				strAmount=dist.getAmount();
				fAmount=Float.parseFloat(strAmount);
				
				long tempId;
				if(fAmount<0){
					tempUser=fromUser;
					fromUser=toUser;
					toUser=tempUser;
					strAmount=strAmount.substring(1);
					tempId=fromId;
					fromId=toId;
					toId=tempId;
				} 
				if(toUser.equalsIgnoreCase(Constants.STR_YOU)){
					toUser="you";
				}
				if(fromId!=toId && fAmount!=0){
					arrDistUsrIds.add(fromId);
					if(fromUser.equalsIgnoreCase(Constants.STR_YOU)){
						arrDistribution.add(fromUser+" owe "+toUser+" an amount of "+strAmount);
						arrPossibletoSettle.add(false);
					} else{
						arrDistribution.add(fromUser+" owes "+toUser+" an amount of "+strAmount);
						arrPossibletoSettle.add(true);
					}
				}
			}
		}

		tempUser=null;
		if(arrDistribution.size()==0){
			arrDistribution.add("Nobody owes anybody anything!!");
			arrPossibletoSettle.add(false);
		}
		listAdapter.notifyDataSetChanged();
	}

	private void openChart(){
		int size=expenseSparseArr.size();
		long lngUserId=0L;
		try {
			CategorySeries distributionSeries = new CategorySeries("Expense distribution of "+strTripName.toLowerCase()+" by users");
			for(int i=0 ;i < size;i++){
				lngUserId=expenseSparseArr.keyAt(i);
				distributionSeries.add(expenseSparseArr.get(lngUserId).getUsername(), expenseSparseArr.get(lngUserId).getAmount());
			}

			// Instantiating a renderer for the Pie Chart
			DefaultRenderer defaultRenderer  = new DefaultRenderer();
			ArrayList<Integer> arrColors=Global.generateColor(size);
			DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
			float val = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, metrics);
			for(int i = 0 ;i<size;i++){

				// Instantiating a render for the slice
				SimpleSeriesRenderer seriesRenderer = new SimpleSeriesRenderer();
				seriesRenderer.setColor(arrColors.get(i));
				seriesRenderer.setDisplayChartValues(true);
				// Adding the renderer of a slice to the renderer of the pie chart
				defaultRenderer.addSeriesRenderer(seriesRenderer);
			}

			defaultRenderer.setLabelsColor(Color.BLACK);
			defaultRenderer.setLegendTextSize(val);
			defaultRenderer.setLabelsTextSize(val);
			defaultRenderer.setZoomButtonsVisible(false);
			defaultRenderer.setZoomEnabled(false);

			// Getting a reference to view group linear layout chart_container

			// Getting PieChartView to add to the custom layout
			mChart = ChartFactory.getPieChartView(getActivity(), distributionSeries, defaultRenderer);

			//			defaultRenderer.setClickEnabled(true);//
			defaultRenderer.setSelectableBuffer(10);
			defaultRenderer.setPanEnabled(false);
			defaultRenderer.setFitLegend(true);

			defaultRenderer.setInScroll(true);
			/*mChart.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					SeriesSelection seriesSelection = mChart.getCurrentSeriesAndPoint();
					if (seriesSelection != null) {

						// Getting the name of the clicked slice
						int seriesIndex = seriesSelection.getPointIndex();
						String selectedSeries="";
						selectedSeries = arrUsers.get(seriesIndex);

						// Getting the value of the clicked slice
						double value = seriesSelection.getXValue();
						DecimalFormat dFormat = new DecimalFormat("#.#");

						// Displaying the message
						Toast.makeText(getActivity(), selectedSeries + " : "  + Double.valueOf(dFormat.format(value)) + " % ", Toast.LENGTH_SHORT).show();
					}
				}
			});*/

			// Adding the pie chart to the custom layout
			int sizeOfChart=Integer.parseInt(getActivity().getResources().getString(R.string.chart_size));
			llChartContainer.addView(mChart, new LayoutParams(sizeOfChart, sizeOfChart));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		super.onCreateOptionsMenu(menu, inflater);
		if(lngUserId==lngAdminId){
			inflater.inflate(R.menu.trip_detail_action, menu);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_delete_trip:
			showDeleteTripDialog(strTripName, lngTripId);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	@SuppressLint("InflateParams")
	protected void showDeleteTripDialog(final String tripName, final long tripId) {
		try{
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			View view = getActivity().getLayoutInflater().inflate(R.layout.delete_expensegroup_dialog, null);
			builder.setCancelable(true);
			TextView textView = (TextView)view.findViewById(R.id.tv_message);
			Button btnYes = (Button) view.findViewById(R.id.btn_yes);
			Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
			btnYes.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					deleteTrip(tripId);
					alert.cancel();
				}
			});
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					alert.cancel();
				}
			});

			textView.setText("Are you sure you want to delete the expense-group "+tripName+"?");

			alert = builder.create();
			alert.setView(view, 0, 0, 0, 0);
			alert.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void deleteTrip(long tripId) {
		LocalDB localDb=new LocalDB(getActivity());
		try{
			if(localDb.isTripSynced(tripId)){
				localDb.updateTripStatusToDeleted(tripId);
			} else{
				localDb.deleteTrip(tripId);
			}
			Context context=getActivity();
			context.startService(new Intent(context, SyncIntentService.class));
			((Activity) context).finish();
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		if(v.equals(ivEdit)){
			showUpdateTripNameDialog(strTripName);
		} else{
			int position = (int) v.getTag();
			showSettleDeptDialog(arrDistUsrIds.get(position));
		}
	}

	@SuppressLint("InflateParams")
	private void showUpdateTripNameDialog(String tripName) {
		try{
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			View view = getActivity().getLayoutInflater().inflate(R.layout.add_trip_dialog, null);
			builder.setCancelable(true);
			eTxtTripName = (EditText)view.findViewById(R.id.etxt_trip_name);
			btnModify = (Button) view.findViewById(R.id.btn_add);
			btnCancel = (Button) view.findViewById(R.id.btn_cancel);
			btnModify.setText("Update");
			eTxtTripName.setText(tripName);
			eTxtTripName.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					btnModify.setEnabled(false);
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {

				}

				@Override
				public void afterTextChanged(Editable s) {
					if(s.length()!=0){
						btnModify.setEnabled(true);
					}
				}
			});
			btnModify.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					strTripName=eTxtTripName.getText().toString();
					alert.cancel();
					updateTrip(lngTripId, strTripName);
					txtTripName.setText(strTripName);

				}
			});
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					alert.cancel();
				}
			});

			alert = builder.create();
			alert.setView(view, 0, 0, 0, 0);
			alert.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void updateTrip(long lngTripIdToChange, String strTripNameToChange) {
		LocalDB localDb=new LocalDB(getActivity());
		try{
			TripBean trip=localDb.retrieveTripDetails(lngTripIdToChange);
			Context context=getActivity();
			if(Constants.STR_NOT_SYNCHED.equals(trip.getSyncStatus()) || Constants.STR_QR_ADDED.equals(trip.getSyncStatus())){
				localDb.updateTrip(lngTripIdToChange, strTripNameToChange, trip.getSyncStatus());
			} else{
				localDb.updateTrip(lngTripIdToChange, strTripNameToChange, Constants.STR_UPDATED);
			}
			((ActionBarActivity)context).getSupportActionBar().setTitle(strTripNameToChange);
			context.startService(new Intent(context, SyncIntentService.class));	
			((TripDetailsActivity)context).updateViews();
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
	}

	@SuppressLint("InflateParams")
	private void showSettleDeptDialog(final long userId) {
		try{
			Context context=getActivity();
			LocalDB localDb=new LocalDB(context);
			DistributionBean1 distBean=localDb.retrieveUnsettledDistributionByUsers(userId, lngUserId, lngTripId);
			String user=localDb.retrievePrefferedName(userId);
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			View view = getActivity().getLayoutInflater().inflate(R.layout.settle_debt_dialog, null);
			builder.setCancelable(true);
			TextView textView = (TextView)view.findViewById(R.id.tv_message);
			EditText etAmount=(EditText) view.findViewById(R.id.et_amount);
			strAmount=distBean.getAmount();
			if(strAmount.startsWith("-")){
				strAmount=strAmount.substring(1);
			}
			etAmount.setText(strAmount);
			btnYes = (Button) view.findViewById(R.id.btn_yes);
			Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
			etAmount.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					btnYes.setEnabled(false);
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
					
				}
				
				@Override
				public void afterTextChanged(Editable s) {
					if(s.length()==0){
						btnYes.setEnabled(false);
					} else{
						strAmount=s.toString();
						float fAmount;
						try {
							fAmount=Float.parseFloat(strAmount);
						} catch (NumberFormatException e) {
							fAmount=0;
						}
						if(fAmount!=0){
							btnYes.setEnabled(true);
						}
					}
				}
			});
			btnYes.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					markDistributionAsSettled(userId, strAmount);
					alert.cancel();
				}
			});
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					alert.cancel();
				}
			});

			textView.setText("Do you want to mark "+user+"'s debt as settled?");

			alert = builder.create();
			alert.setView(view, 0, 0, 0, 0);
			alert.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}

	protected void markDistributionAsSettled(long userId, String strSettledAmount) {
		Context context=getActivity();
		LocalDB localDb=new LocalDB(context);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		DistributionBean1 distBean=localDb.retrieveUnsettledDistributionByUsers(userId, lngUserId, lngTripId);
		if(distBean!=null){
			String strAmount=distBean.getAmount();
			String strNewAmount;
			if(strAmount.startsWith("-")){
				strAmount=strAmount.substring(1);
			}
			if(distBean.getFromId()==lngUserId){
				strNewAmount=Global.add(distBean.getAmount(), strSettledAmount);
			} else{
				strNewAmount=Global.subtract(distBean.getAmount(), strSettledAmount);
			}
			localDb.updateDistAmount(distBean.getDistributionId(), strNewAmount);
			String date = sdf.format(new Date());
			long rowId=localDb.insertDistribution(userId, lngUserId, strSettledAmount, lngTripId, Constants.STR_UNSYNCED, date);
			localDb.updateDistributionId(rowId, rowId);
			reBuildDistribution();
			context.startService(new Intent(context, SyncIntentService.class));
		}
	}
	
	public void settlePaymentClickHandler(View v) {
		
	}

}

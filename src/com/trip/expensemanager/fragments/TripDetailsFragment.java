package com.trip.expensemanager.fragments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.annotation.SuppressLint;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
import com.trip.utils.Heap;
import com.trip.utils.LocalDB;
import com.trip.utils.TripBean;

public class TripDetailsFragment extends CustomFragment implements OnClickListener, OnCheckedChangeListener {

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
	private LongSparseArray<String> toGetSparseArr=new LongSparseArray<String>();
	private String strTotalSpent="0";
	private List<String> arrDistribution=new ArrayList<String>();
	private List<Boolean> arrPossibletoSettle=new ArrayList<Boolean>();
	private ArrayList<Long> arrFromUsrIds=new ArrayList<Long>();
	private ArrayList<String> arrAmountToPay=new ArrayList<String>();
	private ListView lvDistributionList;
	private ArrayAdapter<String> listAdapter;
	private EditText eTxtTripName;
	private Button btnModify;
	private Button btnCancel;
	private TextView txtDistribution;
	private long lngAdminId;
	private TextView txtTripAmount;
	private RadioButton btnUnsettled;
	private RadioButton btnSettled;
	private RadioGroup rgDist;
	private Button btnYes;
	private String strAmount;


	@SuppressLint("InflateParams")
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

			rgDist=(RadioGroup)rootView.findViewById(R.id.rg);

			btnUnsettled=(RadioButton)rootView.findViewById(R.id.btn_unsettled);
			btnSettled=(RadioButton)rootView.findViewById(R.id.btn_settled);
			llChartContainer=(LinearLayout)rootView.findViewById(R.id.ll_chart_container);
			txtTripAmount=(TextView)rootView.findViewById(R.id.txt_trip_amount);

			txtTripName.setText(strTripName);
			ivEdit.setOnClickListener(this);
			lvDistributionList.setDivider(null);
			loadData(trip);
			setHasOptionsMenu(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rootView;
	}

	private void loadData(TripBean trip) {
		LocalDB localDb=new LocalDB(getActivity());
		try{
			lngAdminId=trip.getAdminId();
			arrExpenses = localDb.retrieveExpenses(lngTripId);

			if(trip.getSyncStatus().equals(Constants.STR_QR_ADDED)){
				ivEdit.setVisibility(View.INVISIBLE);
			}

			if(arrExpenses.size()==0){
				txtNoExpenses.setVisibility(View.VISIBLE);
				txtTripAmount.setText("Total amount spent:0");
			} else{
				txtNoExpenses.setVisibility(View.INVISIBLE);
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) txtDistribution.getLayoutParams();
				params.addRule(RelativeLayout.BELOW, R.id.ll_chart_container);

				long userId=0L;
				DistributionBean dbTemp=null;
				int indexOfExpense;
				String spentAmount="0";
				for(ExpenseBean expense:arrExpenses){
					if(!Constants.STR_DELETED.equals(expense.getSyncStatus()) && !Constants.STR_ERROR_STATUS.equals(expense.getSyncStatus())){
						strTotalSpent=Global.add(strTotalSpent, expense.getAmount());
						userId=expense.getUserId();
						long[] tripUserIds=trip.getUserIds();
						Long[] arrTripUserIds=new Long[tripUserIds.length];
						for(int i=0;i<tripUserIds.length;i++){
							arrTripUserIds[i]=tripUserIds[i];
						}
						List<Long> lstTripUsers=Arrays.asList(arrTripUserIds);
						if(lstTripUsers.contains(userId)){
							indexOfExpense= expenseSparseArr.indexOfKey(userId);
							if(indexOfExpense>=0){
								dbTemp=expenseSparseArr.get(userId);
								spentAmount=dbTemp.getAmount();
							} else{
								spentAmount="0";
								dbTemp=new DistributionBean();
								dbTemp.setUserId(userId);
							}
							spentAmount=Global.add(spentAmount, expense.getAmount());
							dbTemp.setAmount(spentAmount);
							expenseSparseArr.put(userId, dbTemp);
						}
					}
				}
				txtTripAmount.setText("Total amount spent:"+strTotalSpent);

				openChart();
			}
			rgDist.setOnCheckedChangeListener(this);
			if(btnUnsettled.isChecked()){
				showUnsettledDistribution();
			} else{
				showSettledDistribution();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void buildDistribution() {
		LocalDB localDb=new LocalDB(getActivity());
		arrDistribution.removeAll(arrDistribution);
		arrPossibletoSettle.removeAll(arrPossibletoSettle);
		arrFromUsrIds.removeAll(arrFromUsrIds);
		toGetSparseArr.clear();
		//		TripBean trip=localDb.retrieveTripDetails(lngTripId);
		List<Long> expUserIds;
		List<String> expAmounts;
		long userId=0L;
		int indexOfExpense;
		String strOwnExpense="0";
		//		long[] tripUserIds=trip.getUserIds();
		//		for(long tripUserId:tripUserIds){
		//			toGetSparseArr.put(tripUserId, "0");
		//		}
		String strAmtToGet;
		int i;
		float fOwed=0f;
		for(ExpenseBean expense:arrExpenses){
			if(!Constants.STR_DELETED.equals(expense.getSyncStatus()) && !Constants.STR_ERROR_STATUS.equals(expense.getSyncStatus())){
				userId=expense.getUserId();
				expUserIds=Global.longToList(expense.getUserIds());
				expAmounts=Global.stringToList(expense.getAmounts());
				i=0;
				indexOfExpense= toGetSparseArr.indexOfKey(userId);
				if(indexOfExpense>=0){
					strAmtToGet=toGetSparseArr.get(userId);
				} else{
					strAmtToGet="0";
				}
				strAmtToGet=Global.add(strAmtToGet, expense.getAmount());
				toGetSparseArr.put(userId, strAmtToGet);

				for(long expUserId:expUserIds){
					indexOfExpense= toGetSparseArr.indexOfKey(expUserId);
					if(indexOfExpense>=0){
						strAmtToGet=toGetSparseArr.get(expUserId);
					} else{
						strAmtToGet="0";
					}
					strAmtToGet=Global.subtract(strAmtToGet,expAmounts.get(i));
					toGetSparseArr.put(expUserId, strAmtToGet);
					if(expUserId==userId){
						if(lngUserId==userId){
							strOwnExpense=Global.add(strOwnExpense, expAmounts.get(i));
						}
					}
					i++;
				}
			}
		}

		List<DistributionBean1> lstPaidDist=localDb.retrieveSettledDistributionForTrip(lngTripId);
		String strPaidAmount;
		for(DistributionBean1 distPaid:lstPaidDist){
			strPaidAmount=distPaid.getAmount();
			userId=distPaid.getFromId();
			indexOfExpense= toGetSparseArr.indexOfKey(userId);
			if(indexOfExpense>=0){
				strAmtToGet=toGetSparseArr.get(userId);
				strAmtToGet=Global.add(strAmtToGet, strPaidAmount);
				toGetSparseArr.put(userId, strAmtToGet);
			}
			userId=distPaid.getToId();
			indexOfExpense= toGetSparseArr.indexOfKey(userId);
			if(indexOfExpense>=0){
				strAmtToGet=toGetSparseArr.get(userId);
				strAmtToGet=Global.subtract(strAmtToGet, strPaidAmount);
				toGetSparseArr.put(userId, strAmtToGet);
			}
		}

		List<DistributionBean> lstToPay=new ArrayList<DistributionBean>();
		List<DistributionBean> lstToGet=new ArrayList<DistributionBean>();
		List<DistributionBean> lstNotOwed=new ArrayList<DistributionBean>();

		DistributionBean distributionBeanTemp;
		String strOwed="0";

		int size=toGetSparseArr.size();

		for(i=0;i<size;i++){
			distributionBeanTemp=new DistributionBean();
			userId=toGetSparseArr.keyAt(i);
			distributionBeanTemp.setUserId(userId);
			strOwed=toGetSparseArr.get(userId);
			fOwed=Float.parseFloat(strOwed);
			if(fOwed<0){
				distributionBeanTemp.setAmount(strOwed.substring(1));
				lstToPay.add(distributionBeanTemp);
			} else if(fOwed==0) {
				distributionBeanTemp.setAmount(strOwed);
				lstNotOwed.add(distributionBeanTemp);
			} else{
				distributionBeanTemp.setAmount(strOwed);
				lstToGet.add(distributionBeanTemp);
			}
		}

		arrDistribution.add("Own expense: "+strOwnExpense);
		arrPossibletoSettle.add(false);
		arrFromUsrIds.add(lngUserId);
		arrAmountToPay.add(strOwnExpense);

		DistributionBean[] adArrToGet=new DistributionBean[lstToGet.size()];
		DistributionBean[] adArrToPay=new DistributionBean[lstToPay.size()];
		DistributionBean[] adArrNotOwed=new DistributionBean[lstNotOwed.size()];

		adArrToGet=lstToGet.toArray(adArrToGet);
		adArrToPay=lstToPay.toArray(adArrToPay);
		adArrNotOwed=lstNotOwed.toArray(adArrNotOwed);

		splitExpense(adArrToGet, adArrToPay, adArrNotOwed);
	}

	private void splitExpense(DistributionBean[] adArrToGet, DistributionBean[] adArrToPay, DistributionBean[] adArrNotOwed) {
		DistributionBean dbTempToGet=null;
		DistributionBean dbTempToPay=null;
		Heap heapToGet=new Heap(adArrToGet);
		Heap heapToPay=new Heap(adArrToPay);
		LocalDB localDb=new LocalDB(getActivity());
		try {
			heapToGet.buildMaxHeap();
			heapToPay.buildMaxHeap();
			String userTo, userFrom;
			while(heapToGet.getArray().length!=0 && heapToPay.getArray().length!=0){
				dbTempToGet=heapToGet.removeMax();
				dbTempToPay=heapToPay.removeMax();
				arrFromUsrIds.add(dbTempToPay.getUserId());
				userFrom=localDb.retrievePrefferedName(dbTempToPay.getUserId());
				userTo=localDb.retrievePrefferedName(dbTempToGet.getUserId());
				String strBalance=Global.subtract(dbTempToPay.getAmount(), dbTempToGet.getAmount());
				float fBalance=Float.parseFloat(strBalance);
				if(fBalance==0){
					if(userFrom.equalsIgnoreCase(Constants.STR_YOU)){
						arrDistribution.add(userFrom+" owe "+userTo+" an amount of "+dbTempToPay.getAmount()+"!");
					} else{
						arrDistribution.add(userFrom+" owes "+userTo+" an amount of "+dbTempToPay.getAmount()+"!");
					}
				} else if(fBalance<0){
					dbTempToGet.setAmount(strBalance.substring(1));
					heapToGet.putValue(dbTempToGet);
					if(userFrom.equalsIgnoreCase(Constants.STR_YOU)){
						arrDistribution.add(userFrom+" owe "+userTo+" an amount of "+dbTempToPay.getAmount()+"!");
					} else{
						arrDistribution.add(userFrom+" owes "+userTo+" an amount of "+dbTempToPay.getAmount()+"!");
					}
				} else{
					dbTempToPay.setAmount(strBalance);
					heapToPay.putValue(dbTempToPay);
					if(userFrom.equalsIgnoreCase(Constants.STR_YOU)){
						arrDistribution.add(userFrom+" owe "+userTo+" an amount of "+dbTempToGet.getAmount()+"!");
					} else{
						arrDistribution.add(userFrom+" owes "+userTo+" an amount of "+dbTempToGet.getAmount()+"!");
					}
				}
				arrAmountToPay.add(dbTempToPay.getAmount());
				if(userTo.equalsIgnoreCase(Constants.STR_YOU)){
					arrPossibletoSettle.add(true);
				} else{
					arrPossibletoSettle.add(false);
				}
			}
			/*TripBean trip=localDb.retrieveTripDetails(lngTripId);
			for(DistributionBean tempBean:adArrNotOwed){
				userFrom=localDb.retrievePrefferedName(tempBean.getUserId());
				arrDistribution.add(userFrom+" doesn't owe anybody anything!");
				arrFromUsrIds.add(tempBean.getUserId());
				arrPossibletoSettle.add(false);
				arrAmountToPay.add(tempBean.getAmount());
			}*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void openChart(){
		int size=expenseSparseArr.size();
		long lngUserId=0L;
		String strExpUser;
		LocalDB localDb=new LocalDB(getActivity());
		try {
			CategorySeries distributionSeries = new CategorySeries("Expense distribution of "+strTripName.toLowerCase()+" by users");
			for(int i=0 ;i < size;i++){
				lngUserId=expenseSparseArr.keyAt(i);
				strExpUser=localDb.retrievePrefferedName(expenseSparseArr.get(lngUserId).getUserId());
				distributionSeries.add(strExpUser, Float.parseFloat(expenseSparseArr.get(lngUserId).getAmount()));
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
			
			int sizeOfChart=Integer.parseInt(getActivity().getResources().getString(R.string.chart_size));
			llChartContainer.setVisibility(View.VISIBLE);
			LayoutParams lp=new LayoutParams(sizeOfChart, sizeOfChart);
			lp.gravity= Gravity.CENTER_HORIZONTAL;
			llChartContainer.addView(mChart, lp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		super.onCreateOptionsMenu(menu, inflater);
		if(lngUserId==lngAdminId){
			inflater.inflate(R.menu.trip_detail_action, menu);
		}
	}*/

	/*@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_delete_trip:
			showDeleteTripDialog(strTripName, lngTripId);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}*/


	/*@SuppressLint("InflateParams")
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
	}*/

	/*protected void deleteTrip(long tripId) {
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
	}*/

	@Override
	public void onClick(View v) {
		if(v.equals(ivEdit)){
			showUpdateTripNameDialog(strTripName);
		} else{
			int position = (int) v.getTag();
			showSettleDeptDialog(position);
		}
	}

	@SuppressLint("InflateParams")
	private void showSettleDeptDialog(final int position) {
		try{
			Context context=getActivity();
			LocalDB localDb=new LocalDB(context);
			String user=localDb.retrievePrefferedName(arrFromUsrIds.get(position));
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			View view = getActivity().getLayoutInflater().inflate(R.layout.settle_debt_dialog, null);
			builder.setCancelable(true);
			TextView textView = (TextView)view.findViewById(R.id.tv_message);
			EditText etAmount=(EditText) view.findViewById(R.id.et_amount);
			strAmount=arrAmountToPay.get(position);
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
					markDistributionAsSettled(position);
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

	protected void markDistributionAsSettled(int position) {
		long userId=arrFromUsrIds.get(position);
		String strSettledAmount=strAmount;
		String strAmountToPay=arrAmountToPay.get(position);
		/*if(strAmountToPay.equals(strSettledAmount)){
			arrDistribution.remove(position);
			arrPossibletoSettle.remove(position);
			arrDistribution.remove(position);
			arrPossibletoSettle.remove(position);
			arrFromUsrIds.remove(position);
		} else{
			strAmountToPay=Global.subtract(strAmountToPay, strSettledAmount);
			if(strAmountToPay.startsWith("-")){
				arrDistribution
			} else{

			}
			arrAmountToPay.remove(position);
		}*/
		Context context=getActivity();
		LocalDB localDb=new LocalDB(context);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String date = sdf.format(new Date());
		long rowId=localDb.insertDistribution(userId, lngUserId, strSettledAmount, lngTripId, Constants.STR_UNSYNCED, date);
		localDb.updateDistributionId(rowId, rowId);
		//		buildDistribution();
		//		listAdapter.notifyDataSetChanged();
		((TripDetailsActivity)context).updateViews();
		context.startService(new Intent(context, SyncIntentService.class));
	}

	@SuppressLint("InflateParams")
	private void showUpdateTripNameDialog(String tripName) {
		try{
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			View view = getActivity().getLayoutInflater().inflate(R.layout.add_trip_dialog, null);
			builder.setCancelable(true);
			eTxtTripName = (EditText)view.findViewById(R.id.etxt_trip_name);
			btnModify = (Button) view.findViewById(R.id.btn_ok);
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
		arrPossibletoSettle.removeAll(arrPossibletoSettle);
		arrFromUsrIds.removeAll(arrFromUsrIds);
		List<DistributionBean1> arrDistfromDB = localDb.retrieveSettledDistributionForTrip(lngTripId);
		long fromId, toId;
		String toUser, fromUser, strAmount, tempUser;
		float fAmount;
		if(arrDistfromDB.size()==0){
			arrDistribution.add("No settled debts!!");
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
		buildDistribution();
		if(arrDistribution.size()==0){
			arrDistribution.add("Nobody owes anybody anything!!");
			arrPossibletoSettle.add(false);
		}
		listAdapter.notifyDataSetChanged();
	}
}

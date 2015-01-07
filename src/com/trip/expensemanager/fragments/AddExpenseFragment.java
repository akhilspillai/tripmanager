package com.trip.expensemanager.fragments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.trip.expensemanager.R;
import com.trip.expensemanager.adapters.CustomAddExpenseAdapter;
import com.trip.utils.Constants;
import com.trip.utils.ExpenseBean;
import com.trip.utils.Global;
import com.trip.utils.LocalDB;
import com.trip.utils.TripBean;

public class AddExpenseFragment extends CustomFragment implements OnClickListener {

	public static Fragment newInstance(long lngTripId, long lngUserId, int iOpcode) {
		Fragment fragment=new AddExpenseFragment();
		try {
			Bundle bundle=new Bundle();
			bundle.putLong(Constants.STR_TRIP_ID, lngTripId);
			bundle.putLong(Constants.STR_USER_ID, lngUserId);
			bundle.putInt(Constants.STR_OPCODE, iOpcode);
			fragment.setArguments(bundle);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fragment;
	}

	public static Fragment newInstance(long lngTripId, long lngExpenseId, long lngUserId, int iPosition, int iOpcode) {
		Fragment fragment=new AddExpenseFragment();
		try {
			Bundle bundle=new Bundle();
			bundle.putLong(Constants.STR_TRIP_ID, lngTripId);
			bundle.putLong(Constants.STR_EXPENSE_ID, lngExpenseId);
			bundle.putLong(Constants.STR_USER_ID, lngUserId);
			bundle.putInt(Constants.STR_POSITION, iPosition);
			bundle.putInt(Constants.STR_OPCODE, iOpcode);
			fragment.setArguments(bundle);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fragment;
	}

	private long lngTripId;
	private long lngUserId;
	private EditText eTxtExpenseName;
	private EditText eTxtExpenseDetail;
	private EditText eTxtExpenseAmount;
	private EditText eTxtExpenseAdmin;
	private EditText eTxtExpenseCreationDate;
	private long lngAdminId;
	private String strDate;
	private int opcode;
	private long lngExpenseId;
	private int iPosition;
	private String strName;
	private String strDetail;
	private String strAmount;
	private ListView lvUsersList;
	private CustomAddExpenseAdapter listAdapter;
	private List<String> strLstUsers=new ArrayList<String>();
	private List<Long> strLstUserIds=new ArrayList<Long>();
	private List<String> strLstAmounts=new ArrayList<String>();
	private List<Boolean> bLstChecked=new ArrayList<Boolean>();
	private List<Boolean> bLstEnabled=new ArrayList<Boolean>();
	private List<String> strLstPrevAmounts;
	private boolean isAutoChanged;
	private TextView txtSelectAll;
	private TextView txtSelectNone;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView=null;
		SimpleDateFormat sdfTemp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		try {
			Bundle bundle=getArguments();
			opcode=bundle.getInt(Constants.STR_OPCODE);
			lngUserId=bundle.getLong(Constants.STR_USER_ID);
			lngTripId=bundle.getLong(Constants.STR_TRIP_ID);
			if(opcode==Constants.I_OPCODE_ADD_EXPENSE){
				((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.add_expense);
			} else{
				lngExpenseId=bundle.getLong(Constants.STR_EXPENSE_ID);
				iPosition=bundle.getInt(Constants.STR_POSITION);
			}
			rootView=inflater.inflate(R.layout.fragment_add_expense_list, container, false);
			lvUsersList=(ListView) rootView.findViewById(R.id.lv_distribution);
			lvUsersList.setDivider(null);
			View header = inflater.inflate(R.layout.fragment_add_expense, null, false); 

			lvUsersList.addHeaderView(header);
			listAdapter = new CustomAddExpenseAdapter(getActivity(), strLstUsers, strLstAmounts, bLstChecked, bLstEnabled, this);
			lvUsersList.setAdapter(listAdapter);
			eTxtExpenseName = (EditText)rootView.findViewById(R.id.etxt_expense_name);
			eTxtExpenseDetail = (EditText)rootView.findViewById(R.id.etxt_expense_detail);
			eTxtExpenseAmount = (EditText)rootView.findViewById(R.id.etxt_expense_amount);
			txtSelectAll = (TextView)rootView.findViewById(R.id.txt_all);
			txtSelectNone = (TextView)rootView.findViewById(R.id.txt_none);
			txtSelectAll.setOnClickListener(this);
			txtSelectNone.setOnClickListener(this);

			eTxtExpenseAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if(!hasFocus){
						if(!isAutoChanged){
							String amount=((EditText)v).getText().toString();
							int size=strLstAmounts.size();
							if(amount.equals("")){
								for(int i=0;i<size;i++){
									strLstAmounts.set(i, "0");
								}
							} else{
								float fAmount=Float.parseFloat(amount);
								int checked=0;
								boolean[] isChecked=new boolean[size];
								for(int i=0;i<size;i++){
									if(bLstChecked.get(i)){
										checked++;
										isChecked[i]=true;
									} else{
										isChecked[i]=false;
									}
								}
								float fDistAmount=0f;
								if(checked!=0){
									fDistAmount=Global.divide(fAmount, checked);
								}
								for(int i=0;i<size;i++){
									if(isChecked[i]){
										strLstAmounts.set(i, String.valueOf(fDistAmount));
									} else{
										strLstAmounts.set(i, String.valueOf(0f));
									}
								}
								listAdapter.notifyDataSetChanged();
							}
						} else{
							isAutoChanged=false;
						}
					}
				}
			});
			eTxtExpenseAdmin = (EditText)rootView.findViewById(R.id.etxt_expense_admin);
			eTxtExpenseCreationDate = (EditText)rootView.findViewById(R.id.etxt_expense_creation_date);
			LocalDB localDb=new LocalDB(getActivity());
			String username;
			Date date;
			TripBean trip=localDb.retrieveTripDetails(lngTripId);
			ExpenseBean expense=null;
			if(opcode==Constants.I_OPCODE_ADD_EXPENSE){
				username=localDb.retrievePrefferedName(lngUserId);
				date=new Date();
				lngAdminId=lngUserId;
				loadUsers(trip);
			} else{
				expense=localDb.retrieveExpense(lngExpenseId);
				((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(expense.getName());
				lngAdminId=expense.getUserId();
				username=localDb.retrievePrefferedName(lngAdminId);
				date=sdf.parse(expense.getCreationDate());
				strName=expense.getName();
				strDetail=expense.getDesc();
				strAmount=expense.getAmount();
				eTxtExpenseName.setText(strName);
				eTxtExpenseDetail.setText(strDetail);
				isAutoChanged=true;
				eTxtExpenseAmount.setText(strAmount);
				if(lngAdminId!=lngUserId){
					eTxtExpenseName.setEnabled(false);
					eTxtExpenseDetail.setEnabled(false);
					eTxtExpenseAmount.setEnabled(false);

				}

				loadUsersnAmounts(trip, expense);
			}
			eTxtExpenseAdmin.setText(username);
			String strDateTemp=sdfTemp.format(date);
			eTxtExpenseCreationDate.setText(strDateTemp);
			strDate=sdf.format(date);
			if(lngAdminId==lngUserId){
				setHasOptionsMenu(true);
			} else{
				setHasOptionsMenu(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return rootView;
	}

	private void loadUsersnAmounts(TripBean trip, ExpenseBean expense) {
		long[] userIds=trip.getUserIds();
		LocalDB localDb=new LocalDB(getActivity());
		List<Long> lstUsers=Global.longToList(expense.getUserIds());
		List<String> lstAmounts=Global.stringToList(expense.getAmounts());
		int i=0;
		for(long userId:userIds){
			i=lstUsers.indexOf(userId);
			if(i>-1){
				strLstAmounts.add(lstAmounts.get(i));
				bLstChecked.add(true);
			} else{
				strLstAmounts.add("0");
				bLstChecked.add(false);
			}
			if(lngAdminId!=lngUserId){
				bLstEnabled.add(false);
			} else{
				bLstEnabled.add(true);
			}
			strLstUserIds.add(userId);
			strLstUsers.add(localDb.retrievePrefferedName(userId));
		}
		strLstPrevAmounts=new ArrayList<String>();
		strLstPrevAmounts.addAll(strLstAmounts);
		listAdapter.notifyDataSetChanged();
	}

	private void loadUsers(TripBean trip) {
		long[] userIds=trip.getUserIds();
		LocalDB localDb=new LocalDB(getActivity());
		for(long userId:userIds){
			strLstAmounts.add("0");
			bLstChecked.add(false);
			bLstEnabled.add(true);
			strLstUserIds.add(userId);
			strLstUsers.add(localDb.retrievePrefferedName(userId));
		}
		listAdapter.notifyDataSetChanged();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		super.onCreateOptionsMenu(menu, inflater);
		if(opcode==Constants.I_OPCODE_ADD_EXPENSE){
			inflater.inflate(R.menu.add_expense_fragment_action, menu);
		} else if(opcode==Constants.I_OPCODE_UPDATE_EXPENSE){
			if(lngAdminId==lngUserId){
				inflater.inflate(R.menu.update_expense_fragment_action, menu);
			}

		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String[] retArr;
		switch (item.getItemId()) {
		case R.id.action_save_expense:
			eTxtExpenseName.requestFocus();
			InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
					Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(eTxtExpenseName.getWindowToken(), 0);
			if(Global.validate(eTxtExpenseName, eTxtExpenseDetail, eTxtExpenseAmount)){
				String strExpenseName=eTxtExpenseName.getText().toString();
				String strExpenseDetail=eTxtExpenseDetail.getText().toString();
				String strExpenseAmount=eTxtExpenseAmount.getText().toString();
				boolean blNameChanged=!strExpenseName.equals(strName);
				boolean blDetailChanged=!strExpenseDetail.equals(strDetail);
				boolean blAmountChanged=!strExpenseAmount.equals(strAmount);
				boolean blUserChanged=!strLstAmounts.equals(strLstPrevAmounts);

				if(!blNameChanged && !blDetailChanged && !blAmountChanged && !blUserChanged){
					showMessage("No values were changed!!");
					return true;
				}
				if(opcode==Constants.I_OPCODE_ADD_EXPENSE){
					retArr=new String[6];
				} else{
					retArr=new String[7];
				}
				float fAmount=Float.parseFloat(strExpenseAmount);
				if(fAmount!=0L){
					int size=strLstAmounts.size();
					StringBuilder sbAmounts=new StringBuilder();
					StringBuilder sbUsers=new StringBuilder();
					String strAmount;
					float fTotal=0f;
					for(int i=0;i<size;i++){
						strAmount=strLstAmounts.get(i);
						if(Float.parseFloat(strAmount)!=0f){
							sbAmounts.append(strAmount);
							sbAmounts.append(',');
							sbUsers.append(strLstUserIds.get(i));
							sbUsers.append(',');
							fTotal=Global.add(fTotal, strAmount);
						}
					}
					if(fTotal==0f){
						//						Toast.makeText(getActivity(), "Please select atleast one user!!", Toast.LENGTH_LONG).show();
						showInfoMessage("Please select atleast one user!!");
						lvUsersList.post(new Runnable(){
							public void run() {
								lvUsersList.setSelection(lvUsersList.getCount() - 1);
							}});;
							return false;
					}
					if(Math.abs(fAmount-fTotal)>0.1f){
						showError(eTxtExpenseAmount, Constants.STR_ERROR_AMT);
						return false;
					}

					sbAmounts.deleteCharAt(sbAmounts.length()-1);
					sbUsers.deleteCharAt(sbUsers.length()-1);
					//					if(strAmount!=null){
					//						return false;
					//					}
					retArr[0]=strExpenseName;
					retArr[1]=strExpenseDetail;
					retArr[2]=strExpenseAmount;
					if(opcode==Constants.I_OPCODE_ADD_EXPENSE){
						retArr[3]=strDate;
						retArr[4]=sbUsers.toString();
						retArr[5]=sbAmounts.toString();
					} else{
						retArr[3]=String.valueOf(lngExpenseId);
						retArr[4]=String.valueOf(iPosition);
						retArr[5]=sbUsers.toString();
						retArr[6]=sbAmounts.toString();
					}
					Intent retIntent = new Intent();
					retIntent.putExtra(Constants.STR_EXPENSE_DETAIL_ARR, retArr);
					getActivity().setResult(Activity.RESULT_OK, retIntent);
					getActivity().finish();
				} else{
					showError(eTxtExpenseAmount, "Not a valid amount!!");
				}
			}
			return true;
		case R.id.action_delete_expense:
			showDeleteExpenseDialog(strName, lngExpenseId, iPosition);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@SuppressLint("InflateParams")
	protected void showDeleteExpenseDialog(final String expenseName, final long expenseId, final int position) {
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
					String[] retArr = new String[2];
					retArr[0]=String.valueOf(expenseId);
					retArr[1]=String.valueOf(position);
					Intent retIntent = new Intent();
					retIntent.putExtra(Constants.STR_EXPENSE_DETAIL_ARR, retArr);
					getActivity().setResult(Activity.RESULT_OK, retIntent);
					getActivity().finish();
					alert.cancel();
				}
			});
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					alert.cancel();
				}
			});

			textView.setText("Are you sure you want to delete the expense "+expenseName+"?");

			alert = builder.create();
			alert.setView(view, 0, 0, 0, 0);
			alert.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void changeData(boolean isRefreshRequired) {
		String strAmount=eTxtExpenseAmount.getText().toString();
		float fAmount=0f;
		int size=strLstAmounts.size();
		if(!"".equals(strAmount)){
			try {
				fAmount = Float.parseFloat(strAmount);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}

			float fDistAmount=0f;
			if(fAmount!=0f){
				int checked=0;
				boolean[] isItemChecked=new boolean[size];
				for(int i=0;i<size;i++){
					if(bLstChecked.get(i)){
						checked++;
						isItemChecked[i]=true;
					} else{
						isItemChecked[i]=false;
					}
				}
				if(checked!=0){
					fDistAmount=Global.divide(fAmount, checked);
				}
				for(int i=0;i<size;i++){
					if(isItemChecked[i]){
						strLstAmounts.set(i, String.valueOf(fDistAmount));
					} else{
						strLstAmounts.set(i, String.valueOf(0f));
					}
				}
			} 
		} else{
			for(int i=0;i<size;i++){
				strLstAmounts.set(i, String.valueOf(0f));
			}
		}
		if(isRefreshRequired){
			listAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onClick(View v) {
		if(lngAdminId==lngUserId){
			int size=bLstChecked.size();
			for(int i=0;i<size;i++){
				if(v.equals(txtSelectAll)){
					bLstChecked.set(i, true);
				} else if(v.equals(txtSelectNone)){
					bLstChecked.set(i, false);
				}
			}
			changeData(true);
		}
	}

}

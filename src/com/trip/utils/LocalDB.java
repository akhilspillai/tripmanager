package com.trip.utils;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class LocalDB{
	private Context context=null;
	Cursor cursor=null;
	private SQLiteHelper dbHelper;
	private final String[] COLUMNS_LOGIN = {SQLiteHelper.COLUMN_USER_ID, SQLiteHelper.COLUMN_USERNAME, SQLiteHelper.COLUMN_PREFFERED_NAME, SQLiteHelper.COLUMN_PASSWORD};
	private final String[] COLUMNS_TRIP = {SQLiteHelper.COLUMN_TRIP_ID, SQLiteHelper.COLUMN_TRIP_NAME, SQLiteHelper.COLUMN_ADMIN, SQLiteHelper.COLUMN_USERS, SQLiteHelper.COLUMN_CREATION_TIME, SQLiteHelper.COLUMN_TRIP_STATUS, SQLiteHelper.COLUMN_IS_SYNCHED, SQLiteHelper.ROW_ID};
	private final String[] COLUMNS_EXPENSE = {SQLiteHelper.COLUMN_EXPENSE_ID, SQLiteHelper.COLUMN_EXPENSE_NAME, SQLiteHelper.COLUMN_EXPENSE_DESC, SQLiteHelper.COLUMN_EXPENSE_AMOUNT, SQLiteHelper.COLUMN_EXPENSE_CURRENCY, SQLiteHelper.COLUMN_TRIP_ID, SQLiteHelper.COLUMN_USER_ID, SQLiteHelper.COLUMN_USERS, SQLiteHelper.COLUMN_AMOUNTS, SQLiteHelper.COLUMN_EXPENSE_CREATION_TIME, SQLiteHelper.COLUMN_IS_SYNCHED, SQLiteHelper.ROW_ID};
	private final String[] COLUMNS_TO_SYNC = {SQLiteHelper.ROW_ID, SQLiteHelper.COLUMN_ACTION, SQLiteHelper.COLUMN_UPDATE, SQLiteHelper.COLUMN_ITEM_ID};
	private final String[] COLUMNS_DISTRIBUTION = {SQLiteHelper.COLUMN_DISTRIBUTION_ID, SQLiteHelper.COLUMN_FROM_ID, SQLiteHelper.COLUMN_TO_ID, SQLiteHelper.COLUMN_EXPENSE_AMOUNT, SQLiteHelper.COLUMN_TRIP_ID, SQLiteHelper.COLUMN_PAID};

	public LocalDB(Context context) {
		try {
			this.context=context;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public SQLiteDatabase open() {
		SQLiteDatabase database=null;
		try {
			dbHelper = new SQLiteHelper(context);
			database = dbHelper.getWritableDatabase();
		} catch (Exception e) {

		}
		return database;
	}

	public void close() {
		try {
			dbHelper.close();
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public boolean insert(long lngUserId, String strUsername, String strPassword, String strPrefferedName, long lngDeviceId) {
		boolean done = false;
		try{
			SQLiteDatabase database=open();
			cursor=database.query(SQLiteHelper.TABLE_LOGIN, new String[]{SQLiteHelper.COLUMN_USER_ID}, SQLiteHelper.COLUMN_USER_ID+"=?", new String[]{String.valueOf(lngUserId)}, null, null, null);
			if(!cursor.moveToFirst()){
				ContentValues values = new ContentValues();
				values.put(SQLiteHelper.COLUMN_USER_ID, lngUserId);
				values.put(SQLiteHelper.COLUMN_USERNAME, strUsername);
				values.put(SQLiteHelper.COLUMN_PASSWORD, strPassword);
				values.put(SQLiteHelper.COLUMN_DEVICE_ID, lngDeviceId);
				values.put(SQLiteHelper.COLUMN_PREFFERED_NAME, strPrefferedName);
				database.insert(SQLiteHelper.TABLE_LOGIN, null,values);
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		finally{
			cursor.close();
			close();
		}
		return done;
	}

	public long insertDistribution(long lngFromId, long lngToId, String strAmount, long lngTripId) {
		long id=0L;
		try{
			SQLiteDatabase database=open();
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.COLUMN_FROM_ID, lngFromId);
			values.put(SQLiteHelper.COLUMN_TO_ID, lngToId);
			values.put(SQLiteHelper.COLUMN_EXPENSE_AMOUNT, strAmount);
			values.put(SQLiteHelper.COLUMN_TRIP_ID, lngTripId);
			values.put(SQLiteHelper.COLUMN_PAID, Constants.STR_NO);
			id=database.insert(SQLiteHelper.TABLE_DISTRIBUTION, null,values);
		} catch (Exception e) {
			e.printStackTrace();

		}
		finally{
			cursor.close();
			close();
		}
		return id;
	}

	public boolean updateDistributionId(long lngRowId, long lngDistributionId) {

		try {
			SQLiteDatabase database=open();
			ContentValues args = new ContentValues();
			args.put(SQLiteHelper.COLUMN_DISTRIBUTION_ID, lngDistributionId);
			database.update(SQLiteHelper.TABLE_DISTRIBUTION, args, SQLiteHelper.ROW_ID+"=?", new String[]{String.valueOf(lngRowId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();

		}finally{
			close();
		}
		return false;

	}

	public ArrayList<DistributionBean1> retrieveDistributionForTrip(long lngTripId) {
		ArrayList<DistributionBean1> strArrDistribution=new ArrayList<DistributionBean1>();
		DistributionBean1 distributionBean=null;
		try {
			SQLiteDatabase database=open(); 
			cursor = database.query(SQLiteHelper.TABLE_DISTRIBUTION, COLUMNS_DISTRIBUTION, SQLiteHelper.COLUMN_TRIP_ID+"=?", new String[]{String.valueOf(lngTripId)},null, null, null);
			if (cursor.moveToFirst()) {
				do {
					distributionBean=new DistributionBean1();
					distributionBean.setDistributionId(cursor.getLong(0));
					distributionBean.setFromId(cursor.getLong(1));
					distributionBean.setToId(cursor.getLong(2));
					distributionBean.setAmount(cursor.getString(3));
					distributionBean.setTripId(cursor.getLong(4));
					distributionBean.setPaid(cursor.getString(5));
					strArrDistribution.add(distributionBean);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {

		}finally{
			if(cursor!=null){
				cursor.close();
			}
			close();
		}
		return strArrDistribution;
	}

	public ArrayList<DistributionBean1> retrieveNotSynchedDistributions() {
		ArrayList<DistributionBean1> strArrDistribution=new ArrayList<DistributionBean1>();
		DistributionBean1 distributionBean=null;
		try {
			SQLiteDatabase database=open(); 
			cursor = database.query(SQLiteHelper.TABLE_DISTRIBUTION, COLUMNS_DISTRIBUTION, SQLiteHelper.COLUMN_IS_SYNCHED+"!=?", new String[]{"S"},null, null, null);
			if (cursor.moveToFirst()) {
				do {
					distributionBean=new DistributionBean1();
					distributionBean.setDistributionId(cursor.getLong(0));
					distributionBean.setFromId(cursor.getLong(1));
					distributionBean.setToId(cursor.getLong(2));
					distributionBean.setAmount(cursor.getString(3));
					distributionBean.setTripId(cursor.getLong(4));
					distributionBean.setPaid(cursor.getString(5));
					distributionBean.setSynced(cursor.getString(6));
					strArrDistribution.add(distributionBean);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {

		}finally{
			if(cursor!=null){
				cursor.close();
			}
			close();
		}
		return strArrDistribution;
	}

	public DistributionBean1 retrieveDistributionByUsers(long frmUserId, long toUserId, long lngTripId) {
		DistributionBean1 distributionBean=null;
		try {
			SQLiteDatabase database=open(); 
			cursor = database.query(SQLiteHelper.TABLE_DISTRIBUTION, COLUMNS_DISTRIBUTION, SQLiteHelper.COLUMN_FROM_ID+"=? AND "+SQLiteHelper.COLUMN_TO_ID+"=? AND "+SQLiteHelper.COLUMN_TRIP_ID+"=?", new String[]{String.valueOf(frmUserId), String.valueOf(toUserId), String.valueOf(lngTripId)},null, null, null);
			if (!cursor.moveToFirst()) {
				cursor = database.query(SQLiteHelper.TABLE_DISTRIBUTION, COLUMNS_DISTRIBUTION, SQLiteHelper.COLUMN_FROM_ID+"=? AND "+SQLiteHelper.COLUMN_TO_ID+"=? AND "+SQLiteHelper.COLUMN_TRIP_ID+"=?", new String[]{String.valueOf(toUserId), String.valueOf(frmUserId), String.valueOf(lngTripId)},null, null, null);
			}
			if (cursor.moveToFirst()) {
				distributionBean=new DistributionBean1();
				distributionBean.setDistributionId(cursor.getLong(0));
				distributionBean.setFromId(cursor.getLong(1));
				distributionBean.setToId(cursor.getLong(2));
				distributionBean.setAmount(cursor.getString(3));
				distributionBean.setTripId(cursor.getLong(4));
				distributionBean.setPaid(cursor.getString(5));
				distributionBean.setSynced(cursor.getString(6));
			}
		} catch (Exception e) {

		}finally{
			if(cursor!=null){
				cursor.close();
			}
			close();
		}
		return distributionBean;
	}


	public boolean updateDistAmount(long distributionId, String strAmount) {
		try {
			SQLiteDatabase database=open();
			ContentValues args = new ContentValues();
			args.put(SQLiteHelper.COLUMN_EXPENSE_AMOUNT, strAmount);
			database.update(SQLiteHelper.TABLE_DISTRIBUTION, args, SQLiteHelper.COLUMN_DISTRIBUTION_ID+"=?", new String[]{String.valueOf(distributionId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();

		}finally{
			close();
		}
		return false;
	}

	public boolean updateDistributionPaidStatus(long lngDistributionId, String strPaidStatus) {
		try {
			SQLiteDatabase database=open();
			ContentValues args = new ContentValues();
			args.put(SQLiteHelper.COLUMN_PAID, strPaidStatus);
			database.update(SQLiteHelper.TABLE_DISTRIBUTION, args, SQLiteHelper.COLUMN_DISTRIBUTION_ID+"=?", new String[]{String.valueOf(lngDistributionId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();

		}finally{
			close();
		}
		return false;
	}

	public boolean delete(String... strUsername) {
		boolean done = false;
		try{
			open().delete(SQLiteHelper.TABLE_LOGIN, SQLiteHelper.COLUMN_USERNAME+ " = ?", strUsername);
			done = true;
		} catch (Exception e) {
			e.printStackTrace();

		}
		finally{
			close();
		}
		return done;
	}



	public String retrievePrefferedName() {
		String strPrefferedName=null;
		try {
			SQLiteDatabase database=open(); 
			cursor = database.query(SQLiteHelper.TABLE_LOGIN,new String[]{SQLiteHelper.COLUMN_PREFFERED_NAME}, null, null,null, null, null);
			if(cursor.moveToFirst()){
				strPrefferedName=cursor.getString(0);
			}
		} catch (Exception e) {

		}finally{
			if(cursor!=null){
				cursor.close();
			}
			close();
		}
		return strPrefferedName;
	}

	public String retrieveUsername(long lngUserId) {
		String strUsername=null;
		try {
			SQLiteDatabase database=open(); 
			cursor = database.query(SQLiteHelper.TABLE_USERS,new String[]{SQLiteHelper.COLUMN_USERNAME}, SQLiteHelper.COLUMN_USER_ID+"=?",new String[]{String.valueOf(lngUserId)}, null, null, null);
			if(cursor.moveToFirst()){
				strUsername=cursor.getString(0);
			}
		} catch (Exception e) {

		}finally{
			if(cursor!=null){
				cursor.close();
			}
			close();
		}
		return strUsername;
	}

	public String retrievePrefferedName(long lngUserId) {
		String strPrefferedName=null;
		try {
			SQLiteDatabase database=open(); 
			cursor = database.query(SQLiteHelper.TABLE_USERS,new String[]{SQLiteHelper.COLUMN_PREFFERED_NAME}, SQLiteHelper.COLUMN_USER_ID+"=?",new String[]{String.valueOf(lngUserId)}, null, null, null);
			if(cursor.moveToFirst()){
				strPrefferedName=cursor.getString(0);
			}
		} catch (Exception e) {

		}finally{
			if(cursor!=null){
				cursor.close();
			}
			close();
		}
		return strPrefferedName;
	}

	public long retrieve() {
		long lngUserId=0L;
		try {
			SQLiteDatabase database=open(); 
			//			String selectQuery = "SELECT  * FROM " +SQLiteHelper.TABLE_LOGIN ;
			//			cursor = database.rawQuery(selectQuery, null);
			cursor=database.query(SQLiteHelper.TABLE_LOGIN,new String[]{SQLiteHelper.COLUMN_USER_ID}, null, null,null, null, null);
			cursor.moveToFirst();
			lngUserId = cursor.getLong(0);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(cursor!=null){
				cursor.close();
			}
			close();
		}
		return lngUserId;
	}

	public long retrieveDeviceId() {
		long lngDevId=0L;
		try {
			SQLiteDatabase database=open();
			cursor=database.query(SQLiteHelper.TABLE_LOGIN,new String[]{SQLiteHelper.COLUMN_DEVICE_ID}, null, null,null, null, null);
			cursor.moveToFirst();
			lngDevId = cursor.getLong(0);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(cursor!=null){
				cursor.close();
			}
			close();
		}
		return lngDevId;
	}

	public boolean update(String strColumn, String strValue, String strUsername) {
		try {
			SQLiteDatabase database=open();
			ContentValues args = new ContentValues();
			args.put(strColumn, strValue); 
			database.update(SQLiteHelper.TABLE_LOGIN, args, SQLiteHelper.COLUMN_USERNAME+"="+ strUsername, null);
			return true;
		} catch (Exception e) {
			e.printStackTrace();

		}finally{
			close();
		}
		return false;
	}


	public boolean checkDataBase()  {
		boolean exists=false;
		try{
			if(retrieve()!=0){
				exists = true;
			}
		} catch (Exception e) {
			exists=false;
		}
		return exists;
	}

	public String retrieveUsername() {
		String strUsername=null;
		SQLiteDatabase database=open();
		try {
			cursor = database.query(SQLiteHelper.TABLE_LOGIN,new String[]{SQLiteHelper.COLUMN_USERNAME}, null, null,null, null, null);
			cursor.moveToFirst();
			strUsername = cursor.getString(0);
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			cursor.close();
			close();
		}
		return strUsername;
	}

	public long insertTrip(String strTripName, long lngTripId, String strCreationTime, String strUsers, long lngAdminId, String strSyncStatus) {
		long id=-1L;
		try{
			SQLiteDatabase database=open();
			cursor=database.query(SQLiteHelper.TABLE_TRIP, new String[]{SQLiteHelper.COLUMN_TRIP_ID}, SQLiteHelper.COLUMN_TRIP_ID+"=?", new String[]{String.valueOf(lngTripId)}, null, null, null);
			if(!cursor.moveToFirst()){
				ContentValues values = new ContentValues();
				values.put(SQLiteHelper.COLUMN_TRIP_ID, lngTripId);
				values.put(SQLiteHelper.COLUMN_TRIP_NAME, strTripName);
				values.put(SQLiteHelper.COLUMN_ADMIN, lngAdminId);
				values.put(SQLiteHelper.COLUMN_USERS, strUsers);
				values.put(SQLiteHelper.COLUMN_CREATION_TIME, strCreationTime);
				values.put(SQLiteHelper.COLUMN_TRIP_STATUS, "O");
				values.put(SQLiteHelper.COLUMN_IS_SYNCHED, strSyncStatus);
				id=database.insert(SQLiteHelper.TABLE_TRIP, null,values);
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		finally{
			close();
		}
		return id;
	}

	public ArrayList<TripBean> retrieveTrips() {
		ArrayList<TripBean> arrTrips=new ArrayList<TripBean>();
		TripBean trip=null;
		try {
			SQLiteDatabase database=open(); 
			cursor = database.query(SQLiteHelper.TABLE_TRIP, COLUMNS_TRIP, null, null,null, null, null);
			String strDate=null;
			String strSynched=null, strStatus=null, strUserIds=null;
			long[] lngArrUserIds=new long[0];
			String[] strArrUserIds=new String[0];
			int i=0;
			if (cursor.moveToFirst()) {
				do {
					trip=new TripBean();
					trip.setId(cursor.getLong(0));
					trip.setName(cursor.getString(1));
					trip.setAdminId(cursor.getLong(2));
					strUserIds=cursor.getString(3);
					if(strUserIds!=null){
						strArrUserIds=strUserIds.split(",");
						i=0;
						lngArrUserIds=new long[strArrUserIds.length];
						for(String strUserId:strArrUserIds){
							lngArrUserIds[i++]=Long.parseLong(strUserId);
						}
					}

					trip.setUserIds(lngArrUserIds);
					strDate=cursor.getString(4);
					//					date=Global.stringToDate(strDate);
					trip.setCreationDate(strDate);
					strStatus=cursor.getString(5);
					strSynched=cursor.getString(6);
					if(strStatus.equals(Constants.STR_OPEN)){
						trip.setClosed(false);
					} else{
						trip.setClosed(true);
					}
					trip.setSyncStatus(strSynched);
					trip.setRowId(cursor.getLong(7));
					arrTrips.add(trip);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {

		}finally{
			if(cursor!=null){
				cursor.close();
			}
			close();
		}
		return arrTrips;
	}

	public ArrayList<TripBean> retrieveNotSynchedTrips() {
		ArrayList<TripBean> arrTrips=new ArrayList<TripBean>();
		TripBean trip=null;
		try {
			SQLiteDatabase database=open(); 
			cursor = database.query(SQLiteHelper.TABLE_TRIP, COLUMNS_TRIP, SQLiteHelper.COLUMN_IS_SYNCHED+"!=?", new String[]{"S"},null, null, null);
			cursor.moveToFirst();
			//			Date date=null;
			String strDate=null;
			String strSynched=null, strStatus=null, strUserIds;
			long[] lngArrUserIds=new long[0];
			String[] strArrUserIds=new String[0];
			int i=0;
			if (cursor.moveToFirst()) {
				do {
					trip=new TripBean();
					trip.setId(cursor.getLong(0));
					trip.setName(cursor.getString(1));
					trip.setAdminId(cursor.getLong(2));
					strUserIds=cursor.getString(3);
					if(strUserIds!=null){
						strArrUserIds=strUserIds.split(",");
						i=0;
						lngArrUserIds=new long[strArrUserIds.length];
						for(String strUserId:strArrUserIds){
							lngArrUserIds[i++]=Long.parseLong(strUserId);
						}
					}

					trip.setUserIds(lngArrUserIds);
					strDate=cursor.getString(4);
					//					date=Global.stringToDate(strDate);
					trip.setCreationDate(strDate);
					strStatus=cursor.getString(5);
					strSynched=cursor.getString(6);
					if(strStatus.equals(Constants.STR_OPEN)){
						trip.setClosed(false);
					} else{
						trip.setClosed(true);
					}
					trip.setSyncStatus(strSynched);
					trip.setRowId(cursor.getLong(7));
					arrTrips.add(trip);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {

		}finally{
			if(cursor!=null){
				cursor.close();
			}
			close();
		}
		return arrTrips;
	}

	public boolean isTripPresent(long lngTripId) {
		boolean isTripPresent=false;
		try {
			SQLiteDatabase database=open();
			cursor = database.query(SQLiteHelper.TABLE_TRIP,new String[]{SQLiteHelper.COLUMN_TRIP_NAME}, SQLiteHelper.COLUMN_TRIP_ID+"=?", new String[]{String.valueOf(lngTripId)},null, null, null);
			isTripPresent=cursor.moveToFirst();
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(cursor!=null){
				cursor.close();
			}
			close();
		}
		return isTripPresent;
	}

	public TripBean retrieveTripDetails(long lngTripId) {
		TripBean trip=null;
		String strStatus=null, strUserIds=null, strDate=null;
		long[] lngArrUserIds=null;
		String[] strArrUserIds=null;
		int i=0;
		try {
			SQLiteDatabase database=open();
			cursor = database.query(SQLiteHelper.TABLE_TRIP,COLUMNS_TRIP, SQLiteHelper.COLUMN_TRIP_ID+"=?", new String[]{String.valueOf(lngTripId)},null, null, null);
			if (!cursor.moveToFirst()) {
				cursor = database.query(SQLiteHelper.TABLE_TRIP,COLUMNS_TRIP, SQLiteHelper.ROW_ID+"=?", new String[]{String.valueOf(lngTripId)},null, null, null);
			}
			if(cursor.moveToFirst()){
				trip=new TripBean();
				trip.setId(cursor.getLong(0));
				trip.setName(cursor.getString(1));
				trip.setAdminId(cursor.getLong(2));
				strUserIds=cursor.getString(3);
				if(strUserIds!=null){
					strArrUserIds=strUserIds.split(",");
					i=0;
					lngArrUserIds=new long[strArrUserIds.length];
					for(String strUserId:strArrUserIds){
						try {
							lngArrUserIds[i++]=Long.parseLong(strUserId);
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						}
					}
				}

				trip.setUserIds(lngArrUserIds);
				strDate=cursor.getString(4);
				//			date=Global.stringToDate(strDate);
				trip.setCreationDate(strDate);
				strStatus=cursor.getString(5);
				if(strStatus.equals(Constants.STR_OPEN)){
					trip.setClosed(false);
				}else{
					trip.setClosed(true);
				}
				trip.setSyncStatus(cursor.getString(6));
				trip.setRowId(cursor.getLong(7));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(cursor!=null){
				cursor.close();
			}
			close();
		}
		return trip;
	}

	public ArrayList<ExpenseBean> retrieveExpenses(long lngTripId) {
		ArrayList<ExpenseBean> strArrExpenses=new ArrayList<ExpenseBean>();
		ExpenseBean expense=null;
		try {
			SQLiteDatabase database=open(); 
			cursor = database.query(SQLiteHelper.TABLE_EXPENSE, COLUMNS_EXPENSE, SQLiteHelper.COLUMN_TRIP_ID+"=?", new String[]{String.valueOf(lngTripId)},null, null, null);
			if (cursor.moveToFirst()) {
				do {
					expense=new ExpenseBean();
					expense.setId(cursor.getLong(0));
					expense.setName(cursor.getString(1));
					expense.setDesc(cursor.getString(2));
					expense.setAmount(cursor.getString(3));
					expense.setCurrency(cursor.getString(4));
					expense.setTripId(cursor.getLong(5));
					expense.setUserId(cursor.getLong(6));
					expense.setUserIds(cursor.getString(7));
					expense.setAmounts(cursor.getString(8));
					expense.setCreationDate(cursor.getString(9));
					expense.setSyncStatus(cursor.getString(10));
					expense.setRowId(cursor.getLong(11));
					strArrExpenses.add(expense);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {

		}finally{
			if(cursor!=null){
				cursor.close();
			}
			close();
		}
		return strArrExpenses;
	}

	public ExpenseBean retrieveExpense(long lngExpenseId) {
		ExpenseBean expense = null;
		String strUserIds=null, strAmounts=null;
		String[] strArrUserIds=null;
		String[] strArrAmounts=null;
		long[] lngArrUserIds=null;
		int[] iArrAmounts=null;
		int i;
		try {
			SQLiteDatabase database=open(); 
			cursor = database.query(SQLiteHelper.TABLE_EXPENSE, COLUMNS_EXPENSE, SQLiteHelper.COLUMN_EXPENSE_ID+"=?", new String[]{String.valueOf(lngExpenseId)},null, null, null);
			if (cursor.moveToFirst()) {
				expense=new ExpenseBean();
				expense.setId(cursor.getLong(0));
				expense.setName(cursor.getString(1));
				expense.setDesc(cursor.getString(2));
				expense.setAmount(cursor.getString(3));
				expense.setCurrency(cursor.getString(4));
				expense.setTripId(cursor.getLong(5));
				expense.setUserId(cursor.getLong(6));
				expense.setUserIds(cursor.getString(7));
				expense.setAmounts(cursor.getString(8));
				expense.setCreationDate(cursor.getString(9));
				expense.setSyncStatus(cursor.getString(10));
				expense.setRowId(cursor.getLong(11));
			}
		} catch (Exception e) {

		}finally{
			if(cursor!=null){
				cursor.close();
			}
			close();
		}
		return expense;
	}


	public long insertExpense(String strExpenseName, long lngExpenseId, String strCreationTime, String strCurrency, String strAmount, String strDesc, long lngTripId, long lngUserId, String strUsers, String strAmounts, String strSyncStatus) {
		long id=0L;
		try{
			SQLiteDatabase database=open();
			cursor=database.query(SQLiteHelper.TABLE_EXPENSE, new String[]{SQLiteHelper.COLUMN_EXPENSE_ID}, SQLiteHelper.COLUMN_EXPENSE_ID+"=?", new String[]{String.valueOf(lngExpenseId)}, null, null, null);
			if(!cursor.moveToFirst()){
				ContentValues values = new ContentValues();
				values.put(SQLiteHelper.COLUMN_EXPENSE_ID, lngExpenseId);
				values.put(SQLiteHelper.COLUMN_EXPENSE_NAME, strExpenseName);
				values.put(SQLiteHelper.COLUMN_EXPENSE_DESC, strDesc);
				values.put(SQLiteHelper.COLUMN_EXPENSE_AMOUNT, strAmount);
				values.put(SQLiteHelper.COLUMN_EXPENSE_CREATION_TIME, strCreationTime);
				values.put(SQLiteHelper.COLUMN_EXPENSE_CURRENCY, strCurrency);
				values.put(SQLiteHelper.COLUMN_TRIP_ID, lngTripId);
				values.put(SQLiteHelper.COLUMN_USER_ID, lngUserId);
				values.put(SQLiteHelper.COLUMN_USERS, strUsers);
				values.put(SQLiteHelper.COLUMN_AMOUNTS, strAmounts);
				values.put(SQLiteHelper.COLUMN_IS_SYNCHED, strSyncStatus);
				id=database.insert(SQLiteHelper.TABLE_EXPENSE, null,values);
				Log.d("Expense", "Success");
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		finally{
			if(cursor!=null){
				cursor.close();
			}
			close();
		}
		return id;
	}

	public boolean updateTripUsers(long lngTripId, String strUsernames) {
		try {
			SQLiteDatabase database=open();
			ContentValues args = new ContentValues();
			args.put(SQLiteHelper.COLUMN_USERS, strUsernames);
			args.put(SQLiteHelper.COLUMN_IS_SYNCHED, Constants.STR_SYNCHED);  
			database.update(SQLiteHelper.TABLE_TRIP, args, SQLiteHelper.COLUMN_TRIP_ID+"=?", new String[]{String.valueOf(lngTripId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();

		}finally{
			close();
		}
		return false;
	}

	public boolean insertPerson(Long lngUserId, String strUsername, String strPrefferedName) {
		boolean done = false;
		try{
			SQLiteDatabase database=open();
			cursor=database.query(SQLiteHelper.TABLE_USERS, new String[]{SQLiteHelper.COLUMN_USER_ID}, SQLiteHelper.COLUMN_USER_ID+"=?", new String[]{String.valueOf(lngUserId)}, null, null, null);
			if(!cursor.moveToFirst()){
				ContentValues values = new ContentValues();
				values.put(SQLiteHelper.COLUMN_USER_ID, lngUserId);
				values.put(SQLiteHelper.COLUMN_USERNAME, strUsername);
				values.put(SQLiteHelper.COLUMN_PREFFERED_NAME, strPrefferedName);
				database.insert(SQLiteHelper.TABLE_USERS, null,values);
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		finally{
			if(cursor!=null){
				cursor.close();
			}
			close();
		}
		return done;
	}

	public boolean updateTripSyncStatus(long lngOldTripId, long lngNewTripId) {
		try {
			SQLiteDatabase database=open();
			ContentValues args = new ContentValues();
			args.put(SQLiteHelper.COLUMN_TRIP_ID, lngNewTripId);
			args.put(SQLiteHelper.COLUMN_IS_SYNCHED, Constants.STR_SYNCHED);
			database.update(SQLiteHelper.TABLE_TRIP, args, SQLiteHelper.COLUMN_TRIP_ID+"=?", new String[]{String.valueOf(lngOldTripId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();

		}finally{
			close();
		}
		return false;
	}

	public boolean updateTripSyncStatus(long lngOldTripId, String strStatus) {
		try {
			SQLiteDatabase database=open();
			ContentValues args = new ContentValues();
			args.put(SQLiteHelper.COLUMN_IS_SYNCHED, strStatus);
			database.update(SQLiteHelper.TABLE_TRIP, args, SQLiteHelper.COLUMN_TRIP_ID+"=?", new String[]{String.valueOf(lngOldTripId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();

		}finally{
			close();
		}
		return false;
	}

	public boolean updateTripSyncStatus(Long lngTripId) {
		try {
			SQLiteDatabase database=open();
			ContentValues args = new ContentValues();
			args.put(SQLiteHelper.COLUMN_IS_SYNCHED, Constants.STR_SYNCHED);
			database.update(SQLiteHelper.TABLE_TRIP, args, SQLiteHelper.COLUMN_TRIP_ID+"=?", new String[]{String.valueOf(lngTripId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();

		}finally{
			close();
		}
		return false;
	}

	public boolean updateExpenseSyncStatus(Long lngOldExpenseId, Long lngExpenseId) {
		try {
			SQLiteDatabase database=open();
			ContentValues args = new ContentValues();
			args.put(SQLiteHelper.COLUMN_EXPENSE_ID, lngExpenseId);
			args.put(SQLiteHelper.COLUMN_IS_SYNCHED, Constants.STR_SYNCHED);
			database.update(SQLiteHelper.TABLE_EXPENSE, args, SQLiteHelper.COLUMN_EXPENSE_ID+"=?", new String[]{String.valueOf(lngOldExpenseId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();

		}finally{
			close();
		}
		return false;
	}

	public boolean updateExpenseSyncStatus(Long lngOldExpenseId, String strStatus) {
		try {
			SQLiteDatabase database=open();
			ContentValues args = new ContentValues();
			args.put(SQLiteHelper.COLUMN_IS_SYNCHED, strStatus);
			database.update(SQLiteHelper.TABLE_EXPENSE, args, SQLiteHelper.COLUMN_EXPENSE_ID+"=?", new String[]{String.valueOf(lngOldExpenseId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();

		}finally{
			close();
		}
		return false;
	}

	public boolean updateExpenseSyncStatus(Long lngExpenseId) {
		try {
			SQLiteDatabase database=open();
			ContentValues args = new ContentValues();
			args.put(SQLiteHelper.COLUMN_EXPENSE_ID, lngExpenseId);
			args.put(SQLiteHelper.COLUMN_IS_SYNCHED, Constants.STR_SYNCHED);
			database.update(SQLiteHelper.TABLE_EXPENSE, args, SQLiteHelper.COLUMN_EXPENSE_ID+"=?", new String[]{String.valueOf(lngExpenseId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();

		}finally{
			close();
		}
		return false;
	}

	public long insertToSync(String strAction, String strUpdate, long lngItemId){
		long id=-1L;
		try {
			SQLiteDatabase database=open();
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.COLUMN_ACTION, strAction);
			values.put(SQLiteHelper.COLUMN_UPDATE, strUpdate);
			values.put(SQLiteHelper.COLUMN_STATUS, Constants.STR_STATUS_UNREAD);
			values.put(SQLiteHelper.COLUMN_ITEM_ID, lngItemId);

			id=database.insert(SQLiteHelper.TABLE_TO_SYNC, null,values);
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			close();
		}
		return id;
	}

	public boolean updateToSyncToRead(long lngRowId) {

		try {
			SQLiteDatabase database=open();
			ContentValues args = new ContentValues();
			args.put(SQLiteHelper.COLUMN_STATUS, Constants.STR_STATUS_READ);
			database.update(SQLiteHelper.TABLE_TO_SYNC, args, SQLiteHelper.ROW_ID+"=?", new String[]{String.valueOf(lngRowId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();

		}finally{
			close();
		}
		return false;

	}

	public ArrayList<UpdateBean> retrieveUpdates() {
		ArrayList<UpdateBean> arrUpdates=new ArrayList<UpdateBean>();
		UpdateBean update=null;
		try {
			SQLiteDatabase database=open(); 
			cursor = database.query(SQLiteHelper.TABLE_TO_SYNC, COLUMNS_TO_SYNC, null, null,null, null, null);
			if (cursor.moveToFirst()) {
				do {
					update=new UpdateBean();
					update.setId(cursor.getLong(0));
					update.setAction(cursor.getString(1));
					update.setUpdate(cursor.getString(2));
					update.setItemId(cursor.getLong(3));
					arrUpdates.add(update);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {

		}finally{
			if(cursor!=null){
				cursor.close();
			}
			close();
		}
		return arrUpdates;
	}

	public ArrayList<UpdateBean> retrieveUnreadUpdates() {
		ArrayList<UpdateBean> arrUpdates=new ArrayList<UpdateBean>();
		UpdateBean update=null;
		try {
			SQLiteDatabase database=open(); 
			cursor = database.query(SQLiteHelper.TABLE_TO_SYNC, COLUMNS_TO_SYNC, SQLiteHelper.COLUMN_STATUS+"=?", new String[]{Constants.STR_STATUS_UNREAD},null, null, null);
			if (cursor.moveToFirst()) {
				do {
					update=new UpdateBean();
					update.setId(cursor.getLong(0));
					update.setAction(cursor.getString(1));
					update.setUpdate(cursor.getString(2));
					update.setItemId(cursor.getLong(3));
					arrUpdates.add(update);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {

		}finally{
			if(cursor!=null){
				cursor.close();
			}
			close();
		}
		return arrUpdates;
	}

	public boolean deleteToSync(long lngRowId){
		try {
			SQLiteDatabase database=open();
			database.delete(SQLiteHelper.TABLE_TO_SYNC, SQLiteHelper.ROW_ID+"=?", new String[]{String.valueOf(lngRowId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			close();
		}
		return false;

	}

	public boolean deleteAllToSync(){
		try {
			SQLiteDatabase database=open();
			database.delete(SQLiteHelper.TABLE_TO_SYNC, null, null);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			close();
		}
		return false;

	}

	public boolean deleteTrips(int iColmnId) {
		try {
			SQLiteDatabase database=open();
			database.delete(SQLiteHelper.TABLE_TRIP, SQLiteHelper.ROW_ID+"=?",new String[]{String.valueOf(iColmnId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			close();
		}
		return false;
	}

	public boolean updateTrip(long lngTripId, String strTripName, String strStatus) {
		try {
			SQLiteDatabase database=open();
			ContentValues args = new ContentValues();
			args.put(SQLiteHelper.COLUMN_TRIP_NAME, strTripName);
			args.put(SQLiteHelper.COLUMN_IS_SYNCHED, strStatus);
			database.update(SQLiteHelper.TABLE_TRIP, args, SQLiteHelper.COLUMN_TRIP_ID+"=?", new String[]{String.valueOf(lngTripId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();

		}finally{
			close();
		}
		return false;
	}

	public boolean updateExpense(String strExpenseName, String strExpenseAmount, String strExpenseDetail, String strUsers, String strAmounts, String strUpdated, long lngExpenseId) {
		try {
			SQLiteDatabase database=open();
			ContentValues args = new ContentValues();
			args.put(SQLiteHelper.COLUMN_EXPENSE_NAME, strExpenseName);
			args.put(SQLiteHelper.COLUMN_EXPENSE_AMOUNT, strExpenseAmount);
			args.put(SQLiteHelper.COLUMN_EXPENSE_DESC, strExpenseDetail);
			args.put(SQLiteHelper.COLUMN_USERS, strUsers);
			args.put(SQLiteHelper.COLUMN_AMOUNTS, strAmounts);

			args.put(SQLiteHelper.COLUMN_IS_SYNCHED, strUpdated);
			database.update(SQLiteHelper.TABLE_EXPENSE, args, SQLiteHelper.COLUMN_EXPENSE_ID+"=?", new String[]{String.valueOf(lngExpenseId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();

		}finally{
			close();
		}
		return false;

	}

	public boolean updateTripId(long lngRowId, long lngTripId) {

		try {
			SQLiteDatabase database=open();
			ContentValues args = new ContentValues();
			args.put(SQLiteHelper.COLUMN_TRIP_ID, lngTripId);
			database.update(SQLiteHelper.TABLE_TRIP, args, SQLiteHelper.ROW_ID+"=?", new String[]{String.valueOf(lngRowId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();

		}finally{
			close();
		}
		return false;

	}

	public boolean updateExpenseId(long lngRowId, long lngExpenseId) {

		try {
			SQLiteDatabase database=open();
			ContentValues args = new ContentValues();
			args.put(SQLiteHelper.COLUMN_EXPENSE_ID, lngExpenseId);
			database.update(SQLiteHelper.TABLE_EXPENSE, args, SQLiteHelper.ROW_ID+"=?", new String[]{String.valueOf(lngRowId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();

		}finally{
			close();
		}
		return false;

	}

	public boolean updateTripIdinExpenses(long lngOldId, long lngNewId) {

		try {
			SQLiteDatabase database=open();
			ContentValues args = new ContentValues();
			args.put(SQLiteHelper.COLUMN_EXPENSE_ID, lngNewId);
			database.update(SQLiteHelper.TABLE_EXPENSE, args, SQLiteHelper.COLUMN_EXPENSE_ID+"=?", new String[]{String.valueOf(lngOldId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();

		}finally{
			close();
		}
		return false;

	}

	public ArrayList<ExpenseBean> retrieveNotSynchedExpenses() {
		ArrayList<ExpenseBean> arrExpenses=new ArrayList<ExpenseBean>();
		ExpenseBean expense=null;
		try {
			SQLiteDatabase database=open(); 
			cursor = database.query(SQLiteHelper.TABLE_EXPENSE, COLUMNS_EXPENSE, SQLiteHelper.COLUMN_IS_SYNCHED+"!=?", new String[]{"S"},null, null, null);
			if (cursor.moveToFirst()) {
				do {
					expense=new ExpenseBean();
					expense.setId(cursor.getLong(0));
					expense.setName(cursor.getString(1));
					expense.setDesc(cursor.getString(2));
					expense.setAmount(cursor.getString(3));
					expense.setCurrency(cursor.getString(4));
					expense.setTripId(cursor.getLong(5));
					expense.setUserId(cursor.getLong(6));
					expense.setUserIds(cursor.getString(7));
					expense.setAmounts(cursor.getString(8));
					expense.setCreationDate(cursor.getString(9));
					expense.setSyncStatus(cursor.getString(10));
					expense.setRowId(cursor.getLong(11));
					arrExpenses.add(expense);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {

		}finally{
			if(cursor!=null){
				cursor.close();
			}
			close();
		}
		return arrExpenses;
	}

	public long retrieveUserId(String person) {
		long lngUserId=0L;
		try {
			SQLiteDatabase database=open(); 
			cursor = database.query(SQLiteHelper.TABLE_USERS,new String[]{SQLiteHelper.COLUMN_USER_ID}, SQLiteHelper.COLUMN_USERNAME+"=?",new String[]{person}, null, null, null);
			if(cursor.moveToFirst()){
				lngUserId=cursor.getLong(0);
			}
		} catch (Exception e) {

		}finally{
			if(cursor!=null){
				cursor.close();
			}
			close();
		}
		return lngUserId;
	}

	public boolean isTripSynced(long lngTripId) {
		try {
			SQLiteDatabase database=open();
			cursor = database.query(SQLiteHelper.TABLE_TRIP,new String[]{SQLiteHelper.COLUMN_IS_SYNCHED}, SQLiteHelper.COLUMN_TRIP_ID+"=?", new String[]{String.valueOf(lngTripId)},null, null, null);
			if(cursor.moveToFirst()){
				if(!cursor.getString(0).equals(Constants.STR_NOT_SYNCHED) && !cursor.getString(0).equals(Constants.STR_QR_ADDED)){
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			cursor.close();
			close();
		}
		return false;
	}

	public boolean updateTripStatusToDeleted(long tripId) {
		try {
			SQLiteDatabase database=open();
			ContentValues args = new ContentValues();
			args.put(SQLiteHelper.COLUMN_IS_SYNCHED, Constants.STR_DELETED);
			database.update(SQLiteHelper.TABLE_TRIP, args, SQLiteHelper.COLUMN_TRIP_ID+"=?", new String[]{String.valueOf(tripId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();

		}finally{
			close();
		}
		return false;

	}

	public boolean updateTripStatusToExited(long tripId) {
		try {
			SQLiteDatabase database=open();
			ContentValues args = new ContentValues();
			args.put(SQLiteHelper.COLUMN_IS_SYNCHED, Constants.STR_EXITED);
			database.update(SQLiteHelper.TABLE_TRIP, args, SQLiteHelper.COLUMN_TRIP_ID+"=?", new String[]{String.valueOf(tripId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();

		}finally{
			close();
		}
		return false;

	}

	public boolean deleteTrip(long lngTripId) {
		try {
			SQLiteDatabase database=open();
			database.delete(SQLiteHelper.TABLE_TRIP, SQLiteHelper.COLUMN_TRIP_ID+"=?",new String[]{String.valueOf(lngTripId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			close();
		}
		return false;
	}

	public boolean deleteExpense(long lngExpenseId) {
		try {
			SQLiteDatabase database=open();
			database.delete(SQLiteHelper.TABLE_EXPENSE, SQLiteHelper.COLUMN_EXPENSE_ID+"=?",new String[]{String.valueOf(lngExpenseId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			close();
		}
		return false;
	}

	public boolean updateExpenseStatusToDeleted(long expenseId) {

		try {
			SQLiteDatabase database=open();
			ContentValues args = new ContentValues();
			args.put(SQLiteHelper.COLUMN_IS_SYNCHED, Constants.STR_DELETED);
			database.update(SQLiteHelper.TABLE_EXPENSE, args, SQLiteHelper.COLUMN_EXPENSE_ID+"=?", new String[]{String.valueOf(expenseId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();

		}finally{
			close();
		}
		return false;


	}

	public boolean updateExpenseTripId(long lngTripId, long lngNewTripId) {
		try {
			SQLiteDatabase database=open();
			ContentValues args = new ContentValues();
			args.put(SQLiteHelper.COLUMN_TRIP_ID, lngNewTripId);
			database.update(SQLiteHelper.TABLE_EXPENSE, args, SQLiteHelper.COLUMN_TRIP_ID+"=?", new String[]{String.valueOf(lngTripId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();

		}finally{
			close();
		}
		return false;


	}

	public ArrayList<ExpenseBean> retrieveExpenses(long lngTripId, long lngExpUserId) {
		ArrayList<ExpenseBean> strArrExpenses=new ArrayList<ExpenseBean>();
		ExpenseBean expense=null;
		try {
			SQLiteDatabase database=open(); 
			cursor = database.query(SQLiteHelper.TABLE_EXPENSE, COLUMNS_EXPENSE, SQLiteHelper.COLUMN_TRIP_ID+"=? AND "+SQLiteHelper.COLUMN_USER_ID+"=?", new String[]{String.valueOf(lngTripId), String.valueOf(lngExpUserId)},null, null, null);
			if (cursor.moveToFirst()) {
				do {
					expense=new ExpenseBean();
					expense.setId(cursor.getLong(0));
					expense.setName(cursor.getString(1));
					expense.setDesc(cursor.getString(2));
					expense.setAmount(cursor.getString(3));
					expense.setCurrency(cursor.getString(4));
					expense.setTripId(cursor.getLong(5));
					expense.setUserId(cursor.getLong(6));
					expense.setUserIds(cursor.getString(7));
					expense.setAmounts(cursor.getString(8));
					expense.setCreationDate(cursor.getString(9));
					expense.setSyncStatus(cursor.getString(10));
					expense.setRowId(cursor.getLong(11));
					strArrExpenses.add(expense);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {

		}finally{
			if(cursor!=null){
				cursor.close();
			}
			close();
		}
		return strArrExpenses;
	}

	public boolean deleteExpenseofTrip(long lngTripIdTemp) {
		try {
			SQLiteDatabase database=open();
			database.delete(SQLiteHelper.TABLE_EXPENSE, SQLiteHelper.COLUMN_TRIP_ID+"=?",new String[]{String.valueOf(lngTripIdTemp)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			close();
		}
		return false;
	}

	public boolean deleteExpenseOfTripnUser(long lngTripId, long lngUserId) {
		try {
			SQLiteDatabase database=open();
			database.delete(SQLiteHelper.TABLE_EXPENSE, SQLiteHelper.COLUMN_TRIP_ID+"=? and "+SQLiteHelper.COLUMN_USER_ID+"=?",new String[]{String.valueOf(lngTripId), String.valueOf(lngUserId)});
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			close();
		}
		return false;
	}

	public void clearAllTables() {
		try {
			SQLiteDatabase db=open();
			db.execSQL("DROP TABLE IF EXISTS" + SQLiteHelper.TABLE_LOGIN);
			db.execSQL("DROP TABLE IF EXISTS" + SQLiteHelper.TABLE_TRIP);
			db.execSQL("DROP TABLE IF EXISTS" + SQLiteHelper.TABLE_EXPENSE);
			db.execSQL("DROP TABLE IF EXISTS" + SQLiteHelper.TABLE_USERS);
			db.execSQL("DROP TABLE IF EXISTS" + SQLiteHelper.TABLE_TO_SYNC);
			db.execSQL("DROP TABLE IF EXISTS" + SQLiteHelper.TABLE_DISTRIBUTION);
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			close();
		}
	}

}

package com.trip.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_LOGIN = "login";
	public static final String TABLE_TRIP = "trip";
	public static final String TABLE_EXPENSE = "expense";
	public static final String TABLE_USERS = "users";
	public static final String TABLE_TO_SYNC = "to_sync";
	public static final String TABLE_DISTRIBUTION = "distribution";

	private static final int DATABASE_VERSION = 2;
	public static final String ROW_ID = "_id";
	public static final String COLUMN_USERNAME = "username";
	public static final String COLUMN_PASSWORD = "password";
	public static final String COLUMN_USER_ID = "user_id";
	public static final String COLUMN_IS_SYNCHED = "is_synched";
	public static final String COLUMN_DEVICE_ID = "device_id";
	public static final String COLUMN_PREFFERED_NAME = "prefferred_name";
	public static final String COLUMN_PURCHASE_ID = "purchase_id";

	private static final String LOGIN_TABLE_CREATE = "create table "
			+ TABLE_LOGIN + "( " +ROW_ID+" integer primary key,"
			+ COLUMN_USER_ID+ " integer,"
			+ COLUMN_DEVICE_ID+ " integer,"
			+ COLUMN_USERNAME+ " text,"
			+ COLUMN_PASSWORD+ " text,"
			+ COLUMN_PURCHASE_ID+ " text,"
			+ COLUMN_PREFFERED_NAME+ " text,"
			+ COLUMN_IS_SYNCHED+" text);";

	public static final String COLUMN_TRIP_ID = "trip_id";
	public static final String COLUMN_TRIP_NAME = "trip_name";
	public static final String COLUMN_ADMIN = "admin";
	public static final String COLUMN_USERS = "users";
	public static final String COLUMN_CREATION_TIME = "creation_time";
	public static final String COLUMN_TRIP_STATUS = "trip_status";


	private static final String TRIP_TABLE_CREATE = "create table "
			+ TABLE_TRIP + "( " +ROW_ID+" integer primary key,"
			+ COLUMN_TRIP_ID+ " integer,"
			+ COLUMN_TRIP_NAME+ " text,"
			+ COLUMN_ADMIN+ " integer,"
			+ COLUMN_USERS+ " text,"
			+ COLUMN_CREATION_TIME+ " date,"
			+ COLUMN_TRIP_STATUS+ " text,"
			+ COLUMN_IS_SYNCHED+" text);";

	public static final String COLUMN_EXPENSE_ID = "expense_id";
	public static final String COLUMN_EXPENSE_NAME = "expense_name";
	public static final String COLUMN_EXPENSE_DESC = "expense_desc";
	public static final String COLUMN_EXPENSE_AMOUNT= "expense_amount";
	public static final String COLUMN_AMOUNTS= "amounts";
	public static final String COLUMN_EXPENSE_CURRENCY = "expense_currency";
	public static final String COLUMN_EXPENSE_CREATION_TIME = "expense_creation_time";


	private static final String EXPENSE_TABLE_CREATE = "create table "
			+ TABLE_EXPENSE + "( " +ROW_ID+" integer primary key,"
			+ COLUMN_EXPENSE_ID+ " integer,"
			+ COLUMN_EXPENSE_NAME+ " text,"
			+ COLUMN_EXPENSE_DESC+ " text,"
			+ COLUMN_EXPENSE_AMOUNT+ " integer,"
			+ COLUMN_TRIP_ID+ " integer,"
			+ COLUMN_USER_ID+ " integer,"
			+ COLUMN_USERS+ " text,"
			+ COLUMN_AMOUNTS+ " text,"
			+ COLUMN_EXPENSE_CURRENCY+ " text,"
			+ COLUMN_EXPENSE_CREATION_TIME+ " date,"
			+ COLUMN_IS_SYNCHED+" text);";

	private static final String USERS_TABLE_CREATE = "create table "
			+ TABLE_USERS + "( " +ROW_ID+" integer primary key,"
			+ COLUMN_USER_ID+ " integer,"
			+ COLUMN_USERNAME+ " text,"
			+ COLUMN_PREFFERED_NAME+ " text);";

	public static final String COLUMN_ITEM_ID = "item_id";
	public static final String COLUMN_ACTION = "action";
	public static final String COLUMN_UPDATE = "notification";
	public static final String COLUMN_STATUS = "status";

	private static final String TO_SYNC_TABLE_CREATE = "create table "
			+ TABLE_TO_SYNC + "( " +ROW_ID+" integer primary key,"
			+ COLUMN_ACTION+ " text,"
			+ COLUMN_STATUS+ " text,"
			+ COLUMN_ITEM_ID+ " integer,"
			+ COLUMN_UPDATE+ " text);";

	public static final String COLUMN_DISTRIBUTION_ID = "distribution_id";
	public static final String COLUMN_FROM_ID = "from_id";
	public static final String COLUMN_TO_ID = "to_id";
	public static final String COLUMN_PAID = "is_paid";

	private static final String DISTRIBUTION_TABLE_CREATE = "create table "
			+ TABLE_DISTRIBUTION + "( " +ROW_ID+" integer primary key,"
			+ COLUMN_DISTRIBUTION_ID+ " integer,"
			+ COLUMN_FROM_ID+ " integer,"
			+ COLUMN_TO_ID+ " integer,"
			+ COLUMN_TRIP_ID+ " integer,"
			+ COLUMN_EXPENSE_AMOUNT+ " text,"
			+ COLUMN_CREATION_TIME+ " date,"
			+ COLUMN_PAID+ " text);";


	public SQLiteHelper(Context context) {
		super(context, Constants.DATABASE, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		try {
			database.execSQL(LOGIN_TABLE_CREATE);
			database.execSQL(TRIP_TABLE_CREATE);
			database.execSQL(EXPENSE_TABLE_CREATE);
			database.execSQL(USERS_TABLE_CREATE);
			database.execSQL(TO_SYNC_TABLE_CREATE);
			database.execSQL(DISTRIBUTION_TABLE_CREATE);
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		try {
			
			db.execSQL("ALTER TABLE "+TABLE_LOGIN+" ADD COLUMN "+COLUMN_IS_SYNCHED+" text default 'N';");
			db.execSQL("ALTER TABLE "+TABLE_LOGIN+" ADD COLUMN "+COLUMN_PURCHASE_ID+" text;");
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

}


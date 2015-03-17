package com.example.lollipoptest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.example.utils.DBCursorAdapter;
import com.example.utils.LocalDB;

public class MainActivity extends ActionBarActivity implements NoticeDialogListener{

	@SuppressLint({ "NewApi", "InlinedApi" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
		//	        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		//	        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
		//	    }
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
		}
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		private ListView lv;
		private LocalDB localDb;
		private DBCursorAdapter adapter;
		private Cursor c;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			lv=(ListView)rootView.findViewById(R.id.lv);
			localDb=new LocalDB(getActivity());
			//			localDb.insert("Akhil");
			c=localDb.retrieve();
			Log.d("Cursor value count", ""+c.getCount());
			adapter=new DBCursorAdapter(getActivity(), c);
			lv.setAdapter(adapter);
			Log.d("Adapter value count", ""+adapter.getCount());
			lv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					new CheckFragment().show(getActivity().getSupportFragmentManager(), "dialog");
				}
			});
			Handler handler=new Handler();
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					c=localDb.retrieve();
					adapter.changeCursor(c);
				}
			}, 5000);
			return rootView;
		}
	}

	public static class CheckFragment extends DialogFragment {

		

		NoticeDialogListener mListener;

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);

			try {
				mListener = (NoticeDialogListener) activity;
			} catch (ClassCastException e) {
				throw new ClassCastException(activity.toString()
						+ " must implement NoticeDialogListener");
			}
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			LayoutInflater inflater = getActivity().getLayoutInflater();
			builder.setView(inflater.inflate(R.layout.dialog_signin, null));
			
			return builder.create();
		}
	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		// TODO Auto-generated method stub
		
	}
}



interface NoticeDialogListener {
	public void onDialogPositiveClick(DialogFragment dialog);
	public void onDialogNegativeClick(DialogFragment dialog);
}

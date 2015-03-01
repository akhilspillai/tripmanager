package com.trip.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.trip.expensemanager.R;


public class Global {

	public static boolean validate(View... views) {
		String strError=null;
		try {
			for(int i=0;i<views.length;i++){
				if(views[i] instanceof EditText){
					EditText et = (EditText) views[i];
					if(et.getText()==null||et.getText().toString().trim().equals("")){
						strError="Cannot be empty!!";
						showError(et, strError);
						return false;
					}
					else if((et.getHint().toString().equals("MPIN") || et.getHint().toString().equals("New MPIN") || et.getHint().toString().equals("Confirm MPIN")) && et.getText().toString().length()!=4){
						Toast toast = Toast.makeText(et.getContext(), "MPIN should be of 4 digits", Toast.LENGTH_SHORT);
						toast.show();
						return false;
					}
				}
				else if(views[i] instanceof CheckBox){
					CheckBox cb = (CheckBox)views[i];
					if(!cb.isChecked()){
						Toast toast = Toast.makeText(cb.getContext(), "Please check the checkbox", Toast.LENGTH_SHORT);
						toast.show();
						return false;
					}
				}
				//				else if(views[i] instanceof Spinner){
				//					Spinner spinner = (Spinner)views[i];
				//					if(spinner.getSelectedItem().toString()==Constants.STR_NO_DETAILS[0]){
				//						Toast toast = Toast.makeText(spinner.getContext(), "No details selected", Toast.LENGTH_SHORT);
				//						toast.show();
				//						return false;
				//					}
				//				}
				else if(views[i] instanceof RadioGroup){
					RadioGroup radioGroup = (RadioGroup)views[i];
					if(radioGroup.getCheckedRadioButtonId()==-1){
						Toast toast = Toast.makeText(radioGroup.getContext(), "No radio buttons clicked", Toast.LENGTH_SHORT);
						toast.show();
						return false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public static void showError(EditText et, String strError) {
		try {
			//			Animation shake = AnimationUtils.loadAnimation(et.getContext(), R.anim.shake);
			//			et.startAnimation(shake);
			Drawable d= et.getContext().getResources().getDrawable(R.drawable.error);
			d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
			et.setError(strError, d);
			et.requestFocus();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Date stringToDate(String strDate) {
		SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date date=null;
		try {  
			date = format.parse(strDate);  
			System.out.println(date);   
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public static String arrToString(Long[] arrUserIds) {
		StringBuffer sbUserIds=new StringBuffer();
		Long userId;
		try {
			for(int i=0;i<arrUserIds.length;i++){
				userId=arrUserIds[i];
				sbUserIds.append(userId);
				if(i!=arrUserIds.length-1){
					sbUserIds.append(",");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sbUserIds.toString();
	}
	
	public static String listToString(List<?> list) {
		StringBuffer sbUserIds=new StringBuffer();
		int length=list.size();
		try {
			for(int i=0;i<length;i++){
				sbUserIds.append(list.get(i));
				if(i!=length-1){
					sbUserIds.append(",");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sbUserIds.toString();
	}
	
	public static List<String> stringToList(String strValues) {
		String[] strArr;
		List<String> list = null;
		try {
			strArr=strValues.split(",");
			list=Arrays.asList(strArr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public static List<Long> longToList(String strValues) {
		String[] strArr;
		List<Long> list = null;
		try {
			strArr=strValues.split(",");
			list=new ArrayList<Long>(strArr.length);
			for(String strValue:strArr){
				list.add(Long.parseLong(strValue));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public static float add(float f1, String amount2) {
		BigDecimal bg1, bg2, bg3=new BigDecimal(0f);
		try {

			bg1 = new BigDecimal(f1);
			bg2 = new BigDecimal(amount2);

			// subtract bg1 with bg2 using mc and assign result to bg3
			bg3 = bg1.add(bg2);
			bg3.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bg3.floatValue();
	}
	
	public static String add(String amount1, String amount2) {
		BigDecimal bg1, bg2, bg3=new BigDecimal(0f);
		try {

			bg1 = new BigDecimal(amount1);
			bg2 = new BigDecimal(amount2);

			// subtract bg1 with bg2 using mc and assign result to bg3
			bg3 = bg1.add(bg2);
			bg3.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bg3.toPlainString();
	}
	
	public static float subtract(float f1, String amount2) {
		BigDecimal bg1, bg2, bg3=new BigDecimal(0f);
		try {

			bg1 = new BigDecimal(f1);
			bg2 = new BigDecimal(amount2);

			// subtract bg1 with bg2 using mc and assign result to bg3
			bg3 = bg1.subtract(bg2);
			bg3.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bg3.floatValue();
	}
	
	public static String subtract(String amount1, String amount2) {
		BigDecimal bg1, bg2, bg3=new BigDecimal(0f);
		try {

			bg1 = new BigDecimal(amount1);
			bg2 = new BigDecimal(amount2);

			// subtract bg1 with bg2 using mc and assign result to bg3
			bg3 = bg1.subtract(bg2);
			bg3.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bg3.toPlainString();
	}
	
	public static String divide(String s1, String s2) {
		BigDecimal bg1, bg2, bg3=new BigDecimal(0f);
		try {

			bg1 = new BigDecimal(s1);
			bg2 = new BigDecimal(s2);

			// subtract bg1 with bg2 using mc and assign result to bg3
			bg3 = bg1.divide(bg2, 2, RoundingMode.HALF_UP);
			bg3.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bg3.toPlainString();
	};

	public static View getViewByPosition(int pos, ListView listView) {
		try {
			final int firstListItemPosition = listView.getFirstVisiblePosition();
			final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

			if (pos < firstListItemPosition || pos > lastListItemPosition ) {
				return listView.getAdapter().getView(pos, null, listView);
			} else {
				final int childIndex = pos - firstListItemPosition;
				return listView.getChildAt(childIndex);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean isConnected(Context context) {
		boolean connected=false;
		try{
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			connected = cm.getActiveNetworkInfo().isConnectedOrConnecting();
		} catch (Exception e) {
			e.printStackTrace();
			connected = false;
		}
		return connected;
	}

	public static ArrayList<Integer> generateColor(int noOfValues){
		Random rnd = new Random();
		int color=0, preColor=-1, i=0;
		ArrayList<Integer> arrColors=new ArrayList<Integer>();
		while(i<noOfValues){
			color = Color.argb(255, rnd.nextInt(200), rnd.nextInt(200), rnd.nextInt(200));
			if(i!=0 && i==noOfValues-1){
				preColor=arrColors.get(0);
			} else{
				if(i!=0){
					preColor=arrColors.get(i-1);
				}
			}
			if(preColor!=color){
				arrColors.add(color);
				i++;
			}
		}
		return arrColors;
	}
}

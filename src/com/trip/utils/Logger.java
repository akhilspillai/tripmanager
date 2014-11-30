package com.trip.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import android.os.Environment;
import android.util.Log;

public class Logger {
	private static final boolean LOG = true;

	public static void addLog(String log) {
		FileWriter writer = null;
		try {
			if(LOG){
				if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){    
					File filename = new File(Environment.getExternalStorageDirectory()+"/logfile.txt");
					if(!filename.exists()){
						filename.createNewFile();
					}
					writer = new FileWriter(filename,true);
					writer.append("\r\n"+log);
					writer.flush();
				} else{
					Log.d("Akhil", log);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if(writer!=null){
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void addLog(Exception ex) {
		FileWriter writer = null;
		PrintWriter pw=null;
		try {
			if(LOG){
				if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){    
					File filename = new File(Environment.getExternalStorageDirectory()+"/error.txt");
					if(filename.exists()){
						filename.createNewFile();
					}
					writer = new FileWriter(filename,true);
					pw = new PrintWriter(writer);
					ex.printStackTrace(pw);
					pw.flush();
					writer.flush();
				} 
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if(pw!=null){
					pw.close();
					pw=null;
				}
				if(writer!=null){
					writer.close();
					writer=null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

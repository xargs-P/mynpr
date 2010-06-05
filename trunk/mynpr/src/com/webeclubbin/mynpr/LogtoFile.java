package com.webeclubbin.mynpr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.Log;

public class LogtoFile {

	BufferedWriter buf = null;
	final String TAG = "LogtoFile";
	
	LogtoFile(Context c) {
		Log.d(TAG, "Setup log to file;");
		File f =  new File(c.getCacheDir(), "log.txt");
		try {
			buf = new BufferedWriter(new FileWriter(f));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    } 
	
	public void d ( String tag , String msg ){
		Log.d(TAG, "D");
		try {
			buf.write (tag + " " + getTime() + " " + msg );
			buf.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
	}
	public void v ( String tag , String msg ){
		Log.d(TAG, "V");
		try {
			buf.write (tag + " " + getTime() + " " + msg );
			buf.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void e ( String tag , String msg , Exception exception){
		Log.d(TAG, "E2");
		try {
			buf.write (tag + " " + getTime() + " " + msg + " " + exception.toString() );
			buf.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void e ( String tag , String msg ){
		Log.d(TAG, "E1");
		try {
			buf.write (tag + " " + getTime() + " " + msg );
			buf.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void save () {
		try {
			buf.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getTime () {
        // current time.
        Date now = new Date();

        //Tue Nov 04 21:53:43 EST 2003
        SimpleDateFormat format =
            new SimpleDateFormat("MMM dd HH:mm:ss zzz yyyy");

        return format.format(now);
	}
	
	
	
	
}

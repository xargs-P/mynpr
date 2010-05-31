package com.webeclubbin.mynpr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;

public class LogtoFile {

	BufferedWriter buf = null;
	
	LogtoFile(Context c) {
		
		File f =  new File(c.getCacheDir(), "log");
		try {
			buf = new BufferedWriter(new FileWriter(f));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    } 
	
	public void d ( String tag , String msg ){
		try {
			buf.write (tag + " " + msg , 0, 50);
			buf.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
	}
	public void v ( String tag , String msg ){
		try {
			buf.write (tag + " " + msg , 0, 50);
			buf.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void e ( String tag , String msg ){
		try {
			buf.write (tag + " " + msg , 0, 50);
			buf.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	
	
}

package com.webeclubbin.mynpr;
//Some icons used: http://forum.xda-developers.com/showthread.php?t=471195
import java.util.Set;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TabHost;

public class MyNPR extends TabActivity {

	Activity maincontext = null;
	
	final String tPOP = "tab_pop";
	final String tSEARCH = "tab_search";
	final static String packagename = "com.webeclubbin.mynpr";
	final static String apikey = "";

	Bundle b = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final String TAG = "onCreate - MyNPR";
        setContentView(com.webeclubbin.mynpr.R.layout.main);
        maincontext = this;
        
        b = savedInstanceState;
        if (savedInstanceState == null){
        	Log.i(TAG, "Bundle savedInstanceState is null.");
        } else {
        	Log.i(TAG, "Bundle savedInstanceState is NOT null.");
        	
        	final Set<String> ourset = savedInstanceState.keySet();
        	String[] s = {"temp"};
        	final String[] ourstrings = ourset.toArray(s);
        	final int bundlesize =  ourstrings.length;
        	Log.i(TAG, "Bundle size: " + String.valueOf( bundlesize ) );
        	
        	for(int i=0; i< bundlesize ; i++){
        		Log.i(TAG, "Bundle contents: " + ourstrings[i]);
           }

        }
        
        // Resize Button Images
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        Log.i(TAG, "Setup TAB");
        TabHost tabHost = getTabHost();
        Intent tempip = new Intent();
        
        Log.i(TAG, "Setup Pop Story Tab");
        tempip.setClass(maincontext , com.webeclubbin.mynpr.PopStoryTab.class );
        tabHost.addTab(tabHost.newTabSpec(tPOP).setIndicator("What's Popular?").setContent( tempip ));
        
        Intent tempis = new Intent();
        Log.i(TAG, "Setup Station Search Tab");
        //s = new SearchStationTab();
        tempis.setClass(maincontext , com.webeclubbin.mynpr.SearchStationTab.class );
        tabHost.addTab(tabHost.newTabSpec(tSEARCH).setIndicator("Station Finder").setContent( tempis ) );
        tabHost.setCurrentTab(0);  

    }
    
    public boolean isbundlenull () {
    	if ( b == null) {
    		return true;
    	} else {
    		return false;
    	}
    }
}

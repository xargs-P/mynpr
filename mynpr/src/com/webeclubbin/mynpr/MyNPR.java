package com.webeclubbin.mynpr;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Process;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TabHost;

public class MyNPR extends TabActivity {

	Activity maincontext = null;
	
	public static final String tPOP = "tab_pop";
	public static final String tSEARCH = "tab_search";
	public static final String tPLAY = "tab_play";
	public static final String STREAM = "myNPR_Stream";
	final static String packagename = "com.webeclubbin.mynpr";
	public final static int PLAYING_ID = 1;
	
	TabHost tabHost = null;

	Bundle b = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final String TAG = "onCreate - MyNPR";
        setContentView(com.webeclubbin.mynpr.R.layout.main);
        maincontext = this;
        
        b = savedInstanceState;
        
        
        // Resize Button Images
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        Log.d(TAG, "Setup TAB");
        tabHost = getTabHost();
        Intent tempip = new Intent();
        
        Log.d(TAG, "Setup Pop Story Tab");
        tempip.setClass(maincontext , com.webeclubbin.mynpr.PopStoryTab.class );
        tabHost.addTab(tabHost.newTabSpec(tPOP).setIndicator("Popular").setContent( tempip ));
        //tabHost.addTab(tabHost.newTabSpec(tPOP).setIndicator("What's Popular?").setContent( tempip ));
        
        Intent tempis = new Intent();
        Log.d(TAG, "Setup Station Search Tab");
        tempis.setClass(maincontext , com.webeclubbin.mynpr.SearchStationTab.class );
        tabHost.addTab(tabHost.newTabSpec(tSEARCH).setIndicator("Stations").setContent( tempis ) );
        
        Intent tempipl = new Intent();
        Log.d(TAG, "Playlist Tab");
        tempipl.setClass(maincontext , com.webeclubbin.mynpr.PlayListTab.class );
        tabHost.addTab(tabHost.newTabSpec(tPLAY).setIndicator("Playlist").setContent( tempipl ) );
        
        if (savedInstanceState == null){
        	Log.d(TAG, "Bundle savedInstanceState is null.");
        	tabHost.setCurrentTab(0); 
        } else {
        	Log.d(TAG, "Bundle savedInstanceState is NOT null.");
        	
        	final Set<String> ourset = savedInstanceState.keySet();
        	String[] s = {"temp"};
        	final String[] ourstrings = ourset.toArray(s);
        	final int bundlesize =  ourstrings.length;
        	Log.d(TAG, "Bundle size: " + String.valueOf( bundlesize ) );
        	
        	for(int i=0; i< bundlesize ; i++){
        		Log.d(TAG, "Bundle contents: " + ourstrings[i]);
           }

        }
         

    }
    
    //Returns if the bundle sent to the onCreate method was null or not
    public boolean isbundlenull () {
    	if ( b == null) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    //Broadcast intent from one tab to another
    public void broadcastToTab( Intent i, String permissions ){
    	String TAG = "broadcastToTab";
    	Log.d(TAG, "Switch to tab");
    	tabHost.setCurrentTabByTag(permissions);
    	Log.d(TAG, "Sending intent");
    	//maincontext.sendBroadcast(i , permissions);
    	PackageManager pm = getPackageManager();
    	List<ResolveInfo> list = pm.queryBroadcastReceivers(i, PackageManager.GET_INTENT_FILTERS);
    	Iterator it = list.iterator();
    	while (it.hasNext()){
    		ResolveInfo r = (ResolveInfo)it.next();
    		Log.d(TAG, r.toString() );
    	}
    	maincontext.sendBroadcast(i );
    }
    
    //Check Priority level
    public void checkThreadPriority(){
    	String TAG = "checkThreadPriority";
    	Log.d(TAG, "Start" );
    	Log.v(TAG, "MyNPR Process priority: " + Process.getThreadPriority(Process.myTid()));
    }
    
    //Raise Priority level
    public void raiseThreadPriority(){
    	//This save my butt http://www.anddev.org/viewtopic.php?p=1085
    	String TAG = "checkThreadPriority";
    	Log.d(TAG, "Start" );
    	Log.d(TAG, "Set Thread priority");
    	Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
    }
    
    //Lower Priority level
    public void lowerThreadPriority(){
    	String TAG = "lowerThreadPriority";
    	Log.d(TAG, "Start" );
    	Log.d(TAG, "Set Thread priority");
    	Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
    }
    
    //Clean up work
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	String TAG = "onDestroy()";
    	//Clear any notifications we may have.
    	Log.d(TAG, "clear any hanging around notifications");
    	//turnOffNotify();
    }

}

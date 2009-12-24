package com.webeclubbin.mynpr;

import java.util.Set;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PlayListTab extends Activity implements Runnable, ServiceConnection {
	private ListView lv = null;

	final static public String HTML_MIME =  "html";
	final static public String STATION = "STATION";
	final static public String LOGO = "LOGO";
	final static public String URL = "URL";
	
	final public String PLAYLIST = "PLAYLIST";
	final public String IMAGES = "IMAGES";
	
	private IntentFilter ourintentfilter ; 

	private PlayList playlist = new PlayList(this);
	private Activity maincontext = null;
	
	private boolean updatescreen = true;
	private boolean doNotStart = false;
	private ImageHelper ih = null;

	private Thread thread = null;
	private ImageView spinner = null;
	private ImageButton button_playstatus = null;
	
	private String currentStation = "";
	private String currentURL = "";
	
	private boolean playstatus = false;
	
	private final int MENU_CLEAN_ALL = 0; 

	public static final String MSG = "MESSAGE";
	public static final int UPDATE = 0;
	public static final int STOP = 1;
	public static final int START = 2;
	public static final int SPIN = 3;
	public static final int STOPSPIN = 4;
	public static final int TROUBLEWITHAUDIO = 5;
	public static final int RAISEPRIORITY = 6;
	public static final int CHECKRIORITY = 7;
	public static final int LOWERPRIORITY = 8;
	
	private IStreamingMediaPlayer.Stub streamerBinder = null;
	
    private BroadcastReceiver playListReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	        	final String TAG = "BroadcastReceiver - onReceive";

	        	String temps = intent.getStringExtra(STATION);
	        	if (temps != null) {
	  	        	//Grab Image and/or Station Name from intent extra
	  	        	Log.i(TAG, "STATION " + intent.getStringExtra(STATION));
	  	        	Log.i(TAG, "LOGO " + intent.getStringExtra(LOGO));
	  	        	Log.i(TAG, "URL " + intent.getStringExtra(URL));
	  	        	Log.i(TAG, "MIME " + intent.getType());

		  	        playlist.addStation(intent.getStringExtra(STATION), intent.getStringExtra(LOGO));
		  	        playlist.addUrl(intent.getStringExtra(STATION), intent.getStringExtra(URL));
		  	        playlist.savetofile();
		  	        updatescreen();
		  	        play( intent.getStringExtra(STATION) ,intent.getStringExtra(URL) );
		  	        //TODO scroll text? Marquee?
		  	        //Scroller s = new Scroller(context , new AnticipateOvershootInterpolator (0) );
		  	        //content.setScroller(s);
	        	} else {
	        		int message = intent.getIntExtra(MSG, -1);
	        		if (message != -1){
	        			Log.i(TAG, "Send status update");
	        			handler.sendEmptyMessage(message);
	        		} else {
	        			Log.i(TAG, "NO update to Send: -1");
	        		}
	        	}
	        }
    };

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
    	String TAG = "onServiceConnected";
    	Log.i(TAG, "START");
    	streamerBinder = (IStreamingMediaPlayer.Stub) service;
    	
    	try {
    		Log.i(TAG, "Is service playing audio? " + streamerBinder.playing() );
    		Log.i(TAG, "Do not start player? " + doNotStart );
    		if ( streamerBinder.playing() == false  && doNotStart == false ){
    			//Start it up
    			Log.i(TAG, "Start audio");
    			MyNPR parent = (MyNPR) maincontext.getParent();
    			Intent i = new Intent(maincontext, StreamingMediaPlayer.class);

    			i.putExtra(PlayListTab.URL, currentURL );
    			i.putExtra(PlayListTab.STATION, currentStation );
    			Log.i(TAG, "startService(i)");
    			parent.startService(i) ; 
    			//streamerBinder.startAudio();
    			Log.i(TAG, "Done starting audio");
    			 
    		} else if (streamerBinder.playing()) {
    			Log.i(TAG, "setup playing content on screen");
				currentStation = streamerBinder.getStation();
				currentURL = streamerBinder.getUrl();
				TextView station = (TextView) findViewById(com.webeclubbin.mynpr.R.id.playingstation);
				station.setText(currentStation + ": ");   
				TextView content = (TextView) findViewById(com.webeclubbin.mynpr.R.id.playingcontent);
				content.setText(currentURL);
				setplaystatus( true );
    		}  
    		doNotStart = false;
    	} catch (RemoteException e) {
                    Log.e(TAG, "ServiceConnection.onServiceConnected", e);
        }
    }
    
    @Override
    public void onServiceDisconnected(ComponentName className) {
    	String TAG = "onServiceDisconnected";
    	Log.i(TAG, "START");
    	Log.i(TAG,"Null out binder");
    	streamerBinder = null;
    }

       
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final String TAG = "onCreate - PlayListTab";
        setContentView(com.webeclubbin.mynpr.R.layout.playlisttab);
        
        maincontext = this;

        Log.i(TAG, "Setup IntentFilter");
        ourintentfilter = new IntentFilter(MyNPR.tPLAY);
		
        //Start listening for Intents
		Log.i(TAG, "Register IntentFilter");
        registerReceiver (playListReceiver, ourintentfilter);
        
        button_playstatus = (ImageButton) findViewById(com.webeclubbin.mynpr.R.id.playstatus);
  		lv = (ListView) findViewById(com.webeclubbin.mynpr.R.id.playlist);

        button_playstatus.setOnClickListener(new OnClickListener() {
     	   public void onClick(View v) {
     		   String TAG = "PlayStatus - onClick";
     		   Log.i(TAG,"Begin");
     		  
     		   if (streamerBinder != null){
     			   boolean p = false;
     			   try {
    				   p = streamerBinder.playing();
    			   } catch (RemoteException e) {
    		        	Log.e(TAG,  e.toString());
    		       }
    			   if (p == true){
    				   //Stop audio
    				   Log.i(TAG, "Stop audio");
    				   stopplayer();
    			   } else {
    				   Log.i(TAG, "Play audio - streamerBinder is not null and not playing");

         			   if ( ! currentURL.equals("") ){
         				   play(currentStation, currentURL);
         			   } else {
         				   Log.i(TAG, "Skip Playing audio. No link to play.");
         			   }
    			   }
     		   } else {
     			   Log.i(TAG, "Play audio - streamerBinder was null");

     			   if ( ! currentURL.equals("") ){
     				   play(currentStation, currentURL);
     			   } else {
     				   Log.i(TAG, "Skip Playing audio. No link to play.");
     			   }

     		   } 
     		   
     	   }
        }); 
        
        
        //Setup service connection
        Intent in = new Intent(maincontext, StreamingMediaPlayer.class);
        doNotStart = true;
		Log.i(TAG,"Bind to our Streamer service");
		MyNPR parent = (MyNPR) maincontext.getParent();
		parent.bindService (in, this , Context.BIND_AUTO_CREATE);
        
        //Setup currently playing views
        /*boolean p = false;
		try {
			if (streamerBinder != null){
				Log.i(TAG, "Checking to see if we are currently playing");
				p = streamerBinder.playing() ;
			}
			
			if (p == true){
				Log.i(TAG, "setup playing content on screen");
				currentStation = streamerBinder.getStation();
				currentURL = streamerBinder.getUrl();
				TextView station = (TextView) findViewById(com.webeclubbin.mynpr.R.id.playingstation);
				station.setText(currentStation + ": ");   
				TextView content = (TextView) findViewById(com.webeclubbin.mynpr.R.id.playingcontent);
				content.setText(currentURL);
				setplaystatus( true );
			}
		} catch (RemoteException e) {
			Log.e(TAG, e.toString());
		}*/
		
	   
		//thread = new Thread(this);
		//thread.start();
        if (savedInstanceState == null){
        	Log.i(TAG, "Bundle savedInstanceState is null.");
    		thread = new Thread(this);
    		thread.start();
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
	
        	ih = new ImageHelper(maincontext);
        	//ih.setImageStorage(savedInstanceState.getStringArray(IMAGES));
        	
        	String[] station = savedInstanceState.getStringArray(PLAYLIST);
        	if (station != null){
        		Log.i(TAG, "dump data into playlist object");
        		playlist.dumpDataIn(station);
        	} else {
        		Log.i(TAG, "Create new playlist object");
        		playlist = new PlayList(this);
        	}
        	handler.sendEmptyMessage(PlayListTab.UPDATE);
        	
        	currentStation = savedInstanceState.getString(STATION);
    		currentURL = savedInstanceState.getString(URL);
    		if ( ! currentStation.equals("")){
    			TextView stationTV = (TextView) findViewById(com.webeclubbin.mynpr.R.id.playingstation);
    			stationTV.setText( currentStation + ": ");
    			TextView contentTV = (TextView) findViewById(com.webeclubbin.mynpr.R.id.playingcontent);
        	    contentTV.setText( currentURL );
    		}
        	

        }

    }
 
    //Thread process for grabbing data
    public void run() {	
    		handler.sendEmptyMessage(PlayListTab.SPIN);
    		playlist = grabdata_playlist();
    		handler.sendEmptyMessage(PlayListTab.UPDATE);
    }
    
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {    
        	String TAG = "handleMessage";
        	if (msg.what == PlayListTab.UPDATE){
        		Log.i(TAG, "Update Screen");
        		updatescreen();
        		    		
        		if (spinner != null ) {
        			if (spinner.getAnimation() != null){
        				spinner.clearAnimation();
        			}        			
        		}
        	} else if (msg.what == PlayListTab.STOP){
        		//If we are sending 1, then the audio stopped playing
        		Log.i(TAG, "Got 'stop' message");
        		stopplayer() ;
        	} else if (msg.what == PlayListTab.START){
        		//If we are sending 2, then the audio started playing
        		Log.i(TAG, "Got 'start' message");
        		setplaystatus( true );
        		turnOnNotify();
        	} else if (msg.what == PlayListTab.SPIN){
        		//Start spinner
        		Log.i(TAG, "Spin Spinner");
        		final Animation rotate = AnimationUtils.loadAnimation(maincontext, R.anim.rotate);
        		spinner = (ImageView) findViewById(R.id.process); 
        		spinner.startAnimation(rotate);
        	} else if (msg.what == PlayListTab.STOPSPIN){
        		//Stop spinner
        		Log.i(TAG, "Stop Spinner");
        		spinner = (ImageView) findViewById(R.id.process); 
        		spinner.clearAnimation();
        	} else if (msg.what == PlayListTab.TROUBLEWITHAUDIO){
        		//Trouble with Audio downloading
        		Log.i(TAG, "Send screen message about trouble with audio");
        		Toast.makeText(maincontext, "Could not connect with Audio Stream" , Toast.LENGTH_LONG).show();
        		//turnOffNotify();
        		stopplayer() ;
        	} else if (msg.what == PlayListTab.RAISEPRIORITY){
        		Log.i(TAG, "Raise priority level for main process");
        		MyNPR parent = (MyNPR) maincontext.getParent();
        		parent.raiseThreadPriority();
        	} else if (msg.what == PlayListTab.CHECKRIORITY){
        		Log.i(TAG, "Check priority level for main process");
        		MyNPR parent = (MyNPR) maincontext.getParent();
        		parent.checkThreadPriority();
        	} else if (msg.what == PlayListTab.LOWERPRIORITY){
        		Log.i(TAG, "Lower priority level for main process");
        		MyNPR parent = (MyNPR) maincontext.getParent();
        		parent.lowerThreadPriority();
        	}
        }
    };
    
    //Set play status
    private void setplaystatus( boolean p) {
    	String TAG = "setplaystatus";
    	playstatus = p;
    	
    	//If set to false . Reset screen
    	if ( playstatus == false ){
    		//Audio stopped
    		ImageView spinner = (ImageView) findViewById(R.id.process); 
    		spinner.clearAnimation();
    		ImageButton button_playstatus = (ImageButton) findViewById(com.webeclubbin.mynpr.R.id.playstatus);
    		button_playstatus.setImageResource(com.webeclubbin.mynpr.R.drawable.play);
			button_playstatus.postInvalidate();
    	} else {
    		//Audio started
    		button_playstatus.setImageResource(com.webeclubbin.mynpr.R.drawable.stop);
			button_playstatus.postInvalidate();
    	}
    }
    
    //Play audio link
    public void play(final String ourstation, final String audiolink){
    	final String TAG = "PLAY audio";

    	Log.i(TAG, "START");
    	
    	currentStation = ourstation;
    	currentURL = audiolink;
    	//Stop service
    	Log.i(TAG, "Check to see if we need to stop the player");
    	if (streamerBinder != null ){
    		boolean p = false;
    		try {
    			p = streamerBinder.playing() ;
    		} catch (RemoteException e) {
    			Log.e(TAG, e.toString());
    		}
    		if (p == true){
    			stopplayer() ;
    		}
		}    
    	
    	Log.i(TAG, "stop player if it is running");
    	turnOffNotify();
    	
    	Log.i(TAG,"Start Spinner");
    	handler.sendEmptyMessage(PlayListTab.SPIN);
    	
    	TextView station = (TextView) findViewById(com.webeclubbin.mynpr.R.id.playingstation);
	    station.setText(ourstation + ": ");   
	    TextView content = (TextView) findViewById(com.webeclubbin.mynpr.R.id.playingcontent);
	    content.setText(audiolink);
        
	    Intent i = new Intent(maincontext, StreamingMediaPlayer.class);

		i.putExtra(PlayListTab.URL, audiolink );
		i.putExtra(PlayListTab.STATION, ourstation );
		
		Log.i(TAG,"Bind to our Streamer service");
		MyNPR parent = (MyNPR) maincontext.getParent();
		parent.bindService (i, this , Context.BIND_AUTO_CREATE);
		Log.i(TAG,"Bind Done");
		//maybe  startService (Intent service) also
	    
    }
    
    private void stopplayer(){
    	String TAG = "stopplayer";
    	
    	try {
    		if (streamerBinder != null){
    			Log.i(TAG, "Tell player to stop");
    			streamerBinder.stopAudio();
    		}
		} catch (RemoteException e) {
			   Log.e(TAG,  e.toString());
		}
    	
    	Log.i(TAG, "set status");
    	setplaystatus( false );
    	
    	Log.i(TAG, "Turn off notify");
    	turnOffNotify();
    }
    
    private void updatescreen(){

		String TAG = "updatescreen - Playlist";

		Log.i(TAG, "ENTER");
		/*if ( ih == null ){
			ih = new ImageHelper(maincontext);
			ih.setImageStorage(playlist.getLogos());
		} else if (updatescreen == true) {
			ih.setImageStorage(playlist.getLogos());
		} */

		if (playlist.getStations() != null){
			//TODO sort adapter
			lv.setAdapter( new PlayListAdapter(maincontext,
					com.webeclubbin.mynpr.R.layout.playlistrow, 
					playlist, ih) );
		} else {
			Log.i(TAG, "No stations to display");
		}
		

    	//Tell UI to update our list
		Log.i(TAG, "update screen");
    	lv.invalidate();
    }    
   
    /** Playlist from file */
    private PlayList grabdata_playlist() {

    	String TAG = "grabdata - Playlist";
    	PlayList p = new PlayList(this);
    	if (p.loadfromfile()){
    		Log.i(TAG, "Loaded file");    	
    	} else {
    		Log.e(TAG, "Could not Load file");
    	}
    	
    	return p;
    }
    
	//Save UI state changes to the instanceState.
    @Override
    public void onSaveInstanceState(Bundle instanceState) {
    	String TAG = "onSaveInstanceState - PlayListTab";

    	Log.i(TAG, "START");
    	
    	if (! playlist.isSaved()){
    		Log.i(TAG, "Save playlist");
    		playlist.savetofile();
    	}
    	
    	//Save playlist
    	String playlistdump[] = playlist.dumpDataOut();
    	if (playlistdump != null){
    		Log.i(TAG, "Saving playlist in instanceState");
    		instanceState.putStringArray(PLAYLIST, playlistdump);
    	}

        Log.i(TAG, "Saving TextViews");
        instanceState.putString(STATION, currentStation );
        instanceState.putString(URL, currentURL );
    	
    	super.onSaveInstanceState(instanceState);
    }
    
    /** Set up Menu for this Tab */
    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
    	menu.add(0, MENU_CLEAN_ALL, Menu.NONE, "Clear Playlist").setIcon(android.R.drawable.ic_menu_delete);;
        return true;
    }
    
    /* Handles item selections */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_CLEAN_ALL:
        	clearlist();
            return true;
        }
        return false;
    }
    
    //Clear playlist
    private void clearlist(){
    	String TAG = "clearlist";
    	
    	Log.i(TAG, "clear playlist");
    	//Delete file
    	playlist.deleteList();
		
    	//Refresh screen
    	Log.i(TAG, "update screen");
    	updatescreen();
    }
    
    //Set up Notify for user to click on
    private void turnOnNotify() {
    	String TAG = "turnOnNotify";
    	
    	Log.i(TAG, "Grab NotificationManager");
    	//Get a reference to the NotificationManager
    	String ns = Context.NOTIFICATION_SERVICE;
    	NotificationManager nm = (NotificationManager) getSystemService(ns);
    	
    	Log.i(TAG, "Instantiate");
    	//Instantiate the Notification:
    	int icon = com.webeclubbin.mynpr.R.drawable.processing2;

    	CharSequence tickerText = currentStation ;
    	long when = System.currentTimeMillis();
	    
    	Notification notification = new Notification(icon, tickerText, when);
    	
    	Log.i(TAG, "Setup");
    	//Define the Notification's expanded message and Intent
    	CharSequence contentTitle = currentStation;
    	CharSequence contentText = currentURL;
    	Intent notificationIntent = new Intent(maincontext, MyNPR.class);

    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
    	//Put both flags into "flags" using the 
    	notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
    	//TODO Create custom expanded view spinner
    	notification.setLatestEventInfo(maincontext, contentTitle, contentText, contentIntent);
    	
    	//Pass the Notification to the NotificationManager
    	Log.i(TAG, "Notify");
    	nm.notify(MyNPR.PLAYING_ID, notification);
    }
    
    //Turn off Notify for user
    private void turnOffNotify() {
    	String TAG = "turnOffNotify";
    	
    	MyNPR parent = (MyNPR) maincontext.getParent();
		Log.i(TAG, "unbind from service");
		try {
			parent.unbindService (this);
		} catch (IllegalArgumentException e){
			Log.e(TAG, "Does not look like we are bound: " + e.toString());
		}
		streamerBinder = null;
		
		handler.sendEmptyMessage( PlayListTab.LOWERPRIORITY );
		
    	Log.i(TAG, "Grab NotificationManager");
    	//Get a reference to the NotificationManager
    	String ns = Context.NOTIFICATION_SERVICE;
    	NotificationManager nm = (NotificationManager) getSystemService(ns);
    	
    	Log.i(TAG, "Cancel Notification");
    	//Cancel Notification:
    	nm.cancel(MyNPR.PLAYING_ID);
    }
    
    //Clean up work
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	String TAG = "onDestroy()";
    	Log.i(TAG,"unregisterReceiver");
    	unregisterReceiver (playListReceiver);
    	/*
    	//Clear any notifications we may have.
    	Log.i(TAG, "clear any hanging around notifications");
    	turnOffNotify();*/
    }
}

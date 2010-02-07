package com.webeclubbin.mynpr;

import java.io.IOException;
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
import android.media.MediaPlayer;
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
	final static public String RSS_MIME =  "rss";
	final static public String XML_MIME =  "xml";
	final static public String STATION = "STATION";
	final static public String LOGO = "LOGO";
	final static public String URL = "URL";
	final static public String REGULARSTREAM = "REGULARSTREAM";
	//final static public String RSS = "RSS";
	final static public String SPINNING = "SPINNING";
	
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
	private boolean currentRegularStream = false;
	
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
	public static final int RESETPLAYSTATUS = 9;
	
	private IStreamingMediaPlayer.Stub streamerBinder = null;
	
    private BroadcastReceiver playListReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, final Intent intent) {
	        	final String TAG = "BroadcastReceiver - onReceive";

	        	String temps = intent.getStringExtra(STATION);
	          
	    	    
	        	if (temps != null) {
	  	        	//Grab Image and/or Station Name from intent extra
	  	        	Log.d(TAG, "STATION " + intent.getStringExtra(STATION));
	  	        	Log.d(TAG, "LOGO " + intent.getStringExtra(LOGO));
	  	        	Log.d(TAG, "URL " + intent.getStringExtra(URL));
	  	        	Log.d(TAG, "MIME " + intent.getType());
	  	        	Log.d(TAG, "RSS " + intent.getBooleanExtra(PlayList.SPLITTERRSS, false));
	  	        	
	  	        	playlist.addStation(intent.getStringExtra(STATION), intent.getStringExtra(LOGO));

	  	        	boolean isrss = intent.getBooleanExtra(PlayList.SPLITTERRSS, false);
	  	        	
	  	        	if (isrss){
	  	        		PlayURL pu = new PlayURL();
	  	        		pu.setLogo(intent.getStringExtra(LOGO));
	  	        		pu.setRSS(true);
	  	        		pu.setStation(intent.getStringExtra(STATION));
	  	        		pu.setTitle(intent.getStringExtra( PlayList.SPLITTERRSSTITLE ));
	  	        		pu.setURL(intent.getStringExtra(URL));
	  	        		playlist.addRSSUrl(pu);
	  	        	} else {
	  	        		playlist.addUrl(intent.getStringExtra(STATION), intent.getStringExtra(URL));
	  	        	}
		  	        		  	        
		  	        playlist.savetofile();
		  	        updatescreen();
		  	        if (! isrss) {
		  	        	play( intent.getStringExtra(STATION) ,intent.getStringExtra(URL), false  );
		  	        } else {
		  	        	//Allow user to select what podcast they want to listen to
		  	        	Log.d(TAG,"Allow user to select what podcast they want to listen to");
		  	        }
		  	        //TODO scroll text? Marquee?
		  	        //Scroller s = new Scroller(context , new AnticipateOvershootInterpolator (0) );
		  	        //content.setScroller(s);
	        	} else {
	        		Runnable r = new Runnable() {   
	    	        	public void run() {
	        				int message = intent.getIntExtra(MSG, -1);
	        				if (message != -1){
	        					Log.d(TAG, "Send status update");
	        					handler.sendEmptyMessage(message);
	        				} else {
	        					Log.d(TAG, "NO update to Send: -1");
	        				}
	        			}
	        		};
	        		new Thread(r).start(); 
	        	} 
	        	
	        }
    };

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
    	String TAG = "onServiceConnected";
    	Log.d(TAG, "START");
    	
    	streamerBinder = (IStreamingMediaPlayer.Stub) service;
    	
    	try {
    		Log.d(TAG, "Is service playing audio? " + streamerBinder.playing() );
    		Log.d(TAG, "Do not start player? " + doNotStart );
    		if ( streamerBinder.playing() == false  && doNotStart == false ){
    			//Start it up
    			Log.d(TAG, "Start audio");
    			MyNPR parent = (MyNPR) maincontext.getParent();
    			Intent i = new Intent(maincontext, StreamingMediaPlayer.class);

    			i.putExtra(PlayListTab.URL, currentURL );
    			i.putExtra(PlayListTab.STATION, currentStation );
    			i.putExtra(PlayListTab.REGULARSTREAM, currentRegularStream );
    			Log.d(TAG, "startService(i)");
    			parent.startService(i) ;
    			//streamerBinder.startAudio();
    			Log.d(TAG, "Done starting audio");
    			 
    		} else if (streamerBinder.playing()) {
    			Log.d(TAG, "setup playing content on screen");
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
    	Log.d(TAG, "START");
    	Log.d(TAG,"Null out binder");
    	streamerBinder = null;
    }

       
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final String TAG = "onCreate - PlayListTab";
        setContentView(com.webeclubbin.mynpr.R.layout.playlisttab);
        
        maincontext = this;

        Log.d(TAG, "Setup IntentFilter");
        ourintentfilter = new IntentFilter(MyNPR.tPLAY);
		
        Log.d(TAG,"Unregister any Receivers");
        try {
        	unregisterReceiver (playListReceiver);
		} catch (IllegalArgumentException e){
			Log.e(TAG, "Does not look like we have a registered receiver: " + e.toString());
		}
    	
        //Start listening for Intents
		Log.d(TAG, "Register IntentFilter");
        registerReceiver (playListReceiver, ourintentfilter);
        
        button_playstatus = (ImageButton) findViewById(com.webeclubbin.mynpr.R.id.playstatus);
  		lv = (ListView) findViewById(com.webeclubbin.mynpr.R.id.playlist);

        button_playstatus.setOnClickListener(new OnClickListener() {
     	   public void onClick(View v) {
     		   String TAG = "PlayStatus - onClick";
     		   Log.d(TAG,"Begin");
     		  
     		   if (streamerBinder != null){
     			   boolean p = false;
     			   try {
    				   p = streamerBinder.playing();
    			   } catch (RemoteException e) {
    		        	Log.e(TAG,  e.toString());
    		       }
    			   if (p == true){
    				   //Stop audio
    				   Log.d(TAG, "Stop audio");
    				   stopplayer();
    			   } else {
    				   Log.d(TAG, "Play audio - streamerBinder is not null and not playing");

         			   if ( ! currentURL.equals("") ){
         				   play(currentStation, currentURL, currentRegularStream);
         			   } else {
         				   Log.d(TAG, "Skip Playing audio. No link to play.");
         			   }
    			   }
     		   } else {
     			   Log.d(TAG, "Play audio - streamerBinder was null");

     			   if ( ! currentURL.equals("") ){
     				   play(currentStation, currentURL, currentRegularStream);
     			   } else {
     				   Log.d(TAG, "Skip Playing audio. No link to play.");
     			   }

     		   } 
     		   
     	   }
        }); 
        
        
        //Setup service connection
        Intent in = new Intent(maincontext, StreamingMediaPlayer.class);
        doNotStart = true;
		Log.d(TAG,"Bind to our Streamer service");
		MyNPR parent = (MyNPR) maincontext.getParent();
		parent.bindService (in, this , Context.BIND_AUTO_CREATE);
        
        
        if (savedInstanceState == null){
        	Log.d(TAG, "Bundle savedInstanceState is null.");
    		thread = new Thread(this);
    		thread.start();
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
	
        	ih = new ImageHelper(maincontext);
        	
        	String[] station = savedInstanceState.getStringArray(PLAYLIST);
        	if (station != null){
        		Log.d(TAG, "dump data into playlist object");
        		playlist.dumpDataIn(station);
        	} else {
        		Log.d(TAG, "Create new playlist object");
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
    		
    		if ( savedInstanceState.getBoolean(SPINNING) ){
    			handler.sendEmptyMessage(PlayListTab.SPIN);
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
        		Log.d(TAG, "Update Screen");
        		updatescreen();
        		    		
        		if (spinner != null ) {
        			if (spinner.getAnimation() != null){
        				spinner.clearAnimation();
        			}        			
        		}
        	} else if (msg.what == PlayListTab.STOP){
        		//If we are sending 1, then the audio stopped playing
        		Log.d(TAG, "Got 'stop' message");
        		stopplayer() ;
        	} else if (msg.what == PlayListTab.START){
        		//If we are sending 2, then the audio started playing
        		Log.d(TAG, "Got 'start' message");
        		setplaystatus( true );
        		turnOnNotify();
        	} else if (msg.what == PlayListTab.SPIN){
        		//Start spinner
        		Log.d(TAG, "Spin Spinner");
        		final Animation rotate = AnimationUtils.loadAnimation(maincontext, R.anim.rotate);
        		spinner = (ImageView) findViewById(R.id.process); 
        		spinner.startAnimation(rotate);
        	} else if (msg.what == PlayListTab.STOPSPIN){
        		//Stop spinner
        		Log.d(TAG, "Stop Spinner");
        		spinner = (ImageView) findViewById(R.id.process); 
        		spinner.clearAnimation();
        	} else if (msg.what == PlayListTab.TROUBLEWITHAUDIO){
        		//Trouble with Audio downloading
        		Log.d(TAG, "Send screen message about trouble with audio");
        		Toast.makeText(maincontext, "Could not connect with Audio Stream" , Toast.LENGTH_LONG).show();
        		
        		stopplayer() ;
        	} else if (msg.what == PlayListTab.RAISEPRIORITY){
        		Log.d(TAG, "Raise priority level for main process");
        		MyNPR parent = (MyNPR) maincontext.getParent();
        		parent.raiseThreadPriority();
        	} else if (msg.what == PlayListTab.CHECKRIORITY){
        		Log.d(TAG, "Check priority level for main process");
        		MyNPR parent = (MyNPR) maincontext.getParent();
        		parent.checkThreadPriority();
        	} else if (msg.what == PlayListTab.LOWERPRIORITY){
        		Log.d(TAG, "Lower priority level for main process");
        		MyNPR parent = (MyNPR) maincontext.getParent();
        		parent.lowerThreadPriority();
        	} else if (msg.what == PlayListTab.RESETPLAYSTATUS){
        		Log.d(TAG, "Reset play status to not playing.");
        		setplaystatus( false );
        		
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
    public void play(final String ourstation, final String audiolink, boolean regularStream){
    	final String TAG = "PLAY audio";

    	Log.d(TAG, "START");
    	
    	currentStation = ourstation;
    	currentURL = audiolink;
    	currentRegularStream = regularStream;
    	
    	Log.d(TAG, currentStation);
    	Log.d(TAG, currentURL);
    	
    	//Stop service
    	Log.d(TAG, "Check to see if we need to stop the player");
    	if (streamerBinder != null ){
    		boolean p = false;
    		try {
    			p = streamerBinder.playing() ;
    		} catch (RemoteException e) {
    			Log.e(TAG, e.toString());
    		}
    		if (p == true){
    			Log.d(TAG, "Stop player");
    			stopplayer() ;
    		}
		}    
    	
    	Log.d(TAG, "Turn off Notify");
    	turnOffNotify();
    	
    	Log.d(TAG,"Start Spinner");
    	handler.sendEmptyMessage(PlayListTab.SPIN);
    	
    	TextView station = (TextView) findViewById(com.webeclubbin.mynpr.R.id.playingstation);
	    station.setText(ourstation + ": ");   
	    TextView content = (TextView) findViewById(com.webeclubbin.mynpr.R.id.playingcontent);
	    content.setText(audiolink);
        
	    Intent i = new Intent(maincontext, StreamingMediaPlayer.class);
	   
		i.putExtra(PlayListTab.URL, audiolink );
		i.putExtra(PlayListTab.STATION, ourstation );
		i.putExtra(PlayListTab.REGULARSTREAM, regularStream);
		
		Log.d(TAG,"Bind to our Streamer service");
		MyNPR parent = (MyNPR) maincontext.getParent();
		parent.bindService (i, this , Context.BIND_AUTO_CREATE);
		Log.d(TAG,"Bind Done");
		//maybe  startService (Intent service) also
	    
    }
    
    private void stopplayer(){
    	String TAG = "stopplayer";
    	
    	try {
    		if (streamerBinder != null){
    			Log.d(TAG, "Tell player to stop");
    			streamerBinder.stopAudio();
    		}
		} catch (RemoteException e) {
			   Log.e(TAG,  e.toString());
		}
    	
    	Log.d(TAG, "set status");
    	setplaystatus( false );
    	
    	Log.d(TAG, "Turn off notify");
    	turnOffNotify();
    }
    
    private void updatescreen(){

		String TAG = "updatescreen - Playlist";

		Log.d(TAG, "ENTER");

		if (playlist.getStations() != null){
			//TODO sort adapter
			lv.setAdapter( new PlayListAdapter(maincontext,
					com.webeclubbin.mynpr.R.layout.playlistrow, 
					playlist, ih) );
		} else {
			Log.d(TAG, "No stations to display");
			lv.setAdapter( null );
		} 
		

    	//Tell UI to update our list
		Log.d(TAG, "update screen");
    	lv.invalidate();
    }    
   
    /** Playlist from file */
    private PlayList grabdata_playlist() {

    	String TAG = "grabdata - Playlist";
    	PlayList p = new PlayList(this);
    	if (p.loadfromfile()){
    		Log.d(TAG, "Loaded file");    	
    	} else {
    		Log.e(TAG, "Could not Load file");
    	}
    	
    	return p;
    }
    
	//Save UI state changes to the instanceState.
    @Override
    public void onSaveInstanceState(Bundle instanceState) {
    	String TAG = "onSaveInstanceState - PlayListTab";

    	Log.d(TAG, "START");
    	
    	if (! playlist.isSaved()){
    		Log.d(TAG, "Save playlist");
    		playlist.savetofile();
    	}
    	
    	//Save playlist
    	String playlistdump[] = playlist.dumpDataOut();
    	if (playlistdump != null){
    		Log.d(TAG, "Saving playlist in instanceState");
    		instanceState.putStringArray(PLAYLIST, playlistdump);
    	}

        Log.d(TAG, "Saving TextViews");
        instanceState.putString(STATION, currentStation );
        instanceState.putString(URL, currentURL );
        
        if (spinner != null){
        	Log.d(TAG, "Save whether spinner is moving");
        	Animation a = spinner.getAnimation();
        	boolean spinning = false;
        	if (a != null){
        		spinning = true;
        	}
            instanceState.putBoolean(SPINNING, spinning);
        }
        
    	
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
    	
    	Log.d(TAG, "clear playlist");
    	//Delete file
    	playlist.deleteList();
		
    	//Refresh screen
    	Log.d(TAG, "update screen");
    	updatescreen();
    }
    
    //Set up Notify for user to click on
    private void turnOnNotify() {
    	String TAG = "turnOnNotify";
    	
    	Log.d(TAG, "Grab NotificationManager");
    	//Get a reference to the NotificationManager
    	String ns = Context.NOTIFICATION_SERVICE;
    	NotificationManager nm = (NotificationManager) getSystemService(ns);
    	
    	Log.d(TAG, "Instantiate");
    	//Instantiate the Notification:
    	int icon = com.webeclubbin.mynpr.R.drawable.processing2;

    	CharSequence tickerText = currentStation ;
    	long when = System.currentTimeMillis();
	    
    	Notification notification = new Notification(icon, tickerText, when);
    	
    	Log.d(TAG, "Setup");
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
    	Log.d(TAG, "Notify");
    	nm.notify(MyNPR.PLAYING_ID, notification);
    }
    
    //Turn off Notify for user
    private void turnOffNotify() {
    	String TAG = "turnOffNotify";
    	
    	MyNPR parent = (MyNPR) maincontext.getParent();
		Log.d(TAG, "unbind from service");
		try {
			parent.unbindService (this);
		} catch (IllegalArgumentException e){
			Log.e(TAG, "Does not look like we are bound: " + e.toString());
		}
		streamerBinder = null;
		
		handler.sendEmptyMessage( PlayListTab.LOWERPRIORITY );
		
    	Log.d(TAG, "Grab NotificationManager");
    	//Get a reference to the NotificationManager
    	String ns = Context.NOTIFICATION_SERVICE;
    	NotificationManager nm = (NotificationManager) getSystemService(ns);
    	
    	Log.d(TAG, "Cancel Notification");
    	//Cancel Notification:
    	nm.cancel(MyNPR.PLAYING_ID);
    }
    
    //Clean up work
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	String TAG = "onDestroy()";
    	Log.d(TAG,"unregisterReceiver");
    	try {
    		unregisterReceiver (playListReceiver);
		} catch (IllegalArgumentException e){
			Log.e(TAG, "Does not look like we have a registered receiver: " + e.toString());
		}
    	
    	/*
    	//Clear any notifications we may have.
    	Log.d(TAG, "clear any hanging around notifications");
    	turnOffNotify();*/
    }
}

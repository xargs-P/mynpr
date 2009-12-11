package com.webeclubbin.mynpr;

import java.io.IOException;

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
	//final static public String AUDIO_MIME =  "audio/mpeg";
	final static public String HTML_MIME =  "html";
	final static public String STATION = "STATION";
	final static public String LOGO = "LOGO";
	final static public String URL = "URL";
	final public String PLAYLIST = "PLAYLIST";
	
	private IntentFilter ourintentfilter ; 
	final private int MENU_LIVE_NPR = 0;
	private final String NPRLIVEURL = "http://www.npr.org/streams/mp3/nprlive24.pls";
	
	private PlayList playlist = new PlayList(this);
	private Activity maincontext = null;
	
	private boolean updatescreen = true;
	private ImageHelper ih = null;

	private Thread thread = null;
	private ImageView spinner = null;
	private ImageButton button_playstatus = null;
	
	private String currentStation = "";
	private String currentURL = "";
	
	private boolean playstatus = false;
	
	//private final String IMAGES = "IMAGES";
	
	public static final int UPDATE = 0;
	public static final int STOP = 1;
	public static final int START = 2;
	public static final int SPIN = 3;
	public static final int STOPSPIN = 4;
	public static final int TROUBLEWITHAUDIO = 5;
	
	private IStreamingMediaPlayer.Stub streamerBinder = null;
	

    private BroadcastReceiver playListReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	        	final String TAG = "BroadcastReceiver - onReceive";

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
	        }
    };

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
    	String TAG = "onServiceConnected";
    	Log.i(TAG, "START");
    	streamerBinder = (IStreamingMediaPlayer.Stub) service;
    	
    	try {
    		Log.i(TAG, "Is service playing audio? " + streamerBinder.playing() );
    		if ( !  streamerBinder.playing() ){
    			//Start it up
    			 streamerBinder.startAudio();
    		}
    	} catch (RemoteException e) {
                    Log.e(TAG, "ServiceConnection.onServiceConnected", e);
        }
    	/*andCellTrackService = IAndCellTrackServiceBase.Stub.asInterface((IBinder)service);
    	try {
    		andCellTrackService.registerObserver(cellTrackStateListener);
    		//onStateChanged(andCellTrackService.getCellInfo());
        } catch (RemoteException e) {
                    Log.e(TAG, "ServiceConnection.onServiceConnected", e);
        }*/
    }
    
    @Override
    public void onServiceDisconnected(ComponentName className) {
    	String TAG = "onServiceDisconnected";
    	Log.i(TAG, "START");
    	Log.i(TAG,"Null out binder");
    	streamerBinder = null;
    	/*try {
    		andCellTrackService.unregisterObserver(cellTrackStateListener);
        } catch (RemoteException e) {
        	Log.e(TAG, "AndCellTrack.ServiceConnection.onServiceDisconnected", e);
        }*/

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
    				   try {
    					   streamerBinder.stopAudio();
    				   } catch (RemoteException e) {
    					   Log.e(TAG,  e.toString());
    				   }
    			   }
     		   } 
     		   
     		   if (playstatus == false) {
     			  //play audio
      			  Log.i(TAG, "Play audio");

           		  if ( ! currentURL.equals("") ){
             		  play(currentStation, currentURL);
           		  } else {
           			Log.i(TAG, "Skip Playing audio. No link to play.");
           		  }
     		   }
     		   
     	   }
        }); 
        
        //Setup any saved views
        handler.sendEmptyMessage(PlayListTab.SPIN);
		   
		thread = new Thread(this);
		thread.start();
        /*if (savedInstanceState == null){
        	Log.i(TAG, "Bundle savedInstanceState is null.");
        	
        	handler.sendEmptyMessage(PlayListTab.SPIN);
  		   
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
        	ih.setImageStorage(savedInstanceState.getStringArray(IMAGES));
        	
        	byte[] b = savedInstanceState.getByteArray(PLAYLIST);
        	if ( b != null) {
        		try {     	    
        	        // Deserialize from a byte array
        			Log.i(TAG, "Deserialize Playlist from saved Bundle");
        	        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b));
        	        playlist = (PlayList) in.readObject();
        	        in.close();
        	        
        	        if ( playlist != null ) {
        	        	//Update the list view
        	        	Log.i(TAG, "Update screen with playlist");
        	        	updatescreen();
        	        } else {
        	        	Log.i(TAG, "Skipping screen update, null playlist");
        	        }
        	    } catch (ClassNotFoundException e) {
        	    	Log.e(TAG, e.toString());
        	    } catch (IOException e) {
        	    	Log.e(TAG, e.toString());
        	    }
        	}

        } */
		
		Runnable r = new Runnable() {   
	        public void run() {   
	        	String TAG = "Stream Checker";
	        	while (true){
	        		if (streamerBinder != null){
	        			int stat = -1;
	        			try {
	        				stat = streamerBinder.checkStatus();
	        			} catch (RemoteException e) {
	        				Log.e(TAG,e.toString());
	        			}
	        			if (stat  != -1 ) {
	        				Log.i(TAG, "We have a new status: " + stat);
	        				handler.sendEmptyMessage(stat);
	        			}
	        		}
	        	}
	        }   
	    };   
	    
	    new Thread(r).start();

    }
 
    //Thread process for grabbing data
    public void run() {	
    		
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
        		Log.i(TAG, "Set playstatus = stop");
        		setplaystatus( false );
        		turnOffNotify();
        	} else if (msg.what == PlayListTab.START){
        		//If we are sending 2, then the audio started playing
        		Log.i(TAG, "Set playstatus = start");
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
        	}
        }
    };
    
    //Set play status
    private void setplaystatus( boolean p) {
    	playstatus = p;
    	
    	//If set to false . Reset screen
    	if ( playstatus == false ){
    		//Audio stopped
    		ImageView spinner = (ImageView) findViewById(R.id.process); 
    		spinner.clearAnimation();
    		ImageButton button_playstatus = (ImageButton) findViewById(com.webeclubbin.mynpr.R.id.playstatus);
    		button_playstatus.setImageResource(com.webeclubbin.mynpr.R.drawable.play);
			button_playstatus.postInvalidate();
			MyNPR parent = (MyNPR) maincontext.getParent();
			parent.unbindService (this);
    	} else {
    		//Audio started
    		button_playstatus.setImageResource(com.webeclubbin.mynpr.R.drawable.stop);
			button_playstatus.postInvalidate();
    	}
    }
    
    //Play audio link
    public void play(String ourstation, final String audiolink){
    	final String TAG = "PLAY audio";

    	Log.i(TAG, "START");
    	
    	currentStation = ourstation;
    	currentURL = audiolink;
    	//Stop service
    	if (streamerBinder != null ){
    		boolean p = false;
    		try {
    			p = streamerBinder.playing() ;
    		} catch (RemoteException e) {
    			Log.e(TAG, e.toString());
    		}
    		if (p == true){
    			try {
    				streamerBinder.stopAudio();
    			} catch (RemoteException e){
    				Log.e(TAG, e.toString());
    			}
    		}
		} 
    	
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
	    
    }
    
    private void updatescreen(){

		String TAG = "updatescreen - Playlist";

		Log.i(TAG, "ENTER");
		if ( ih == null ){
			ih = new ImageHelper(maincontext);
			ih.setImageStorage(playlist.getLogos());
		} else if (updatescreen == true) {
			ih.setImageStorage(playlist.getLogos());
		}

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
    	/*byte[] bufOfPlaylist = null;
    	try {
    		// Serialize to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
            ObjectOutputStream out = new ObjectOutputStream(bos) ;
            out.writeObject(playlist);
            out.close();
        
            // Get the bytes of the serialized object
            bufOfPlaylist = bos.toByteArray();
        } catch (IOException e) {
        	Log.e(TAG, e.toString());
        }
        
        Log.i(TAG, "Saving playlist in instanceState");
        instanceState.putByteArray(PLAYLIST, bufOfPlaylist); */

    	super.onSaveInstanceState(instanceState);
    }
    
    /** Set up Menu for this Tab */
    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
    	menu.add(0, MENU_LIVE_NPR, Menu.NONE, "Launch NPR.org Live Stream").setIcon(com.webeclubbin.mynpr.R.drawable.npr2);;
        return true;
    }
    
    /* Handles item selections */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_LIVE_NPR:
        	String [] list = SearchStationTab.parsePLS(NPRLIVEURL, maincontext);
        	SearchStationTab.launchhelper(list, maincontext, null, "NPR.org", "http://media.npr.org/chrome/nprlogo_24.gif");
            return true;
        }
        return false;
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
    	//TextView station = (TextView) findViewById(com.webeclubbin.mynpr.R.id.playingstation);
    	//TextView content = (TextView) findViewById(com.webeclubbin.mynpr.R.id.playingcontent);
    	
    	CharSequence tickerText = currentStation ;
    	long when = System.currentTimeMillis();
	    
    	Notification notification = new Notification(icon, tickerText, when);
    	
    	Log.i(TAG, "Setup");
    	//Define the Notification's expanded message and Intent
    	CharSequence contentTitle = currentStation;
    	CharSequence contentText = currentURL;
    	Intent notificationIntent = new Intent(maincontext, MyNPR.class);
    	//TODO maybe make broadcast for this.
    	//TODO also we may need to make this into a service all together
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
    	
    	Log.i(TAG, "Grab NotificationManager");
    	//Get a reference to the NotificationManager
    	String ns = Context.NOTIFICATION_SERVICE;
    	NotificationManager nm = (NotificationManager) getSystemService(ns);
    	
    	Log.i(TAG, "Cancel Notification");
    	//Cancel Notification:
    	nm.cancel(MyNPR.PLAYING_ID);
    }
}

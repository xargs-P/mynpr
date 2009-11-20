package com.webeclubbin.mynpr;

import java.io.IOException;
import java.util.Set;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

public class PlayListTab extends Activity implements Runnable {
	private ListView lv = null;
	//final static public String AUDIO_MIME =  "audio/mpeg";
	final static public String HTML_MIME =  "html";
	final static public String STATION = "STATION";
	final static public String LOGO = "LOGO";
	final static public String URL = "URL";
	
	private IntentFilter ourintentfilter ; 
	final private int MENU_LIVE_NPR = 0;
	private final String NPRLIVEURL = "http://stream.npr.org:8002/listen.pls";
	
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
	
	private StreamingMediaPlayer audioStreamer = null;
 
	private final String IMAGES = "IMAGES";
	
	public static final int UPDATE = 0;
	public static final int STOP = 1;
	public static final int START = 2;
	public static final int SPIN = 3;
	public static final int STOPSPIN = 4;

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

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final String TAG = "onCreate - PlayListTab";
        setContentView(com.webeclubbin.mynpr.R.layout.playlisttab);
        maincontext = this;

        Log.i(TAG, "Setup IntentFilter");
        ourintentfilter = new IntentFilter(MyNPR.tPLAY);
		
		Log.i(TAG, "Register IntentFilter");
        registerReceiver (playListReceiver, ourintentfilter);
        
        button_playstatus = (ImageButton) findViewById(com.webeclubbin.mynpr.R.id.playstatus);
  		lv = (ListView) findViewById(com.webeclubbin.mynpr.R.id.playlist);

        button_playstatus.setOnClickListener(new OnClickListener() {
     	   public void onClick(View v) {
     		   String TAG = "PlayStatus - onClick";
     		   
     		   if (audioStreamer != null){
     			   if (audioStreamer.isPlaying()) {
     				  //Stop audio
     				  Log.i(TAG, "Stop audio");
     				  audioStreamer.stop();
     			   }
     			   audioStreamer = null;

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
        if (savedInstanceState == null){
        	Log.i(TAG, "Bundle savedInstanceState is null.");
        	
        	final Animation rotate = AnimationUtils.loadAnimation(maincontext, R.anim.rotate);
    		spinner = (ImageView) findViewById(R.id.process); 
    		spinner.startAnimation(rotate);
  		   
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

        }

    }
 
    //Thread process for grabbing data
    public void run() {	
    		
    		playlist = grabdata_playlist();
    		handler.sendEmptyMessage(0);
    }
    
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {    
        	if (msg.what == PlayListTab.UPDATE){
        		updatescreen();
        		    		
        		if (spinner != null ) {
        			if (spinner.getAnimation() != null){
        				spinner.clearAnimation();
        			}        			
        		}
        	} else if (msg.what == PlayListTab.STOP){
        		//If we are sending 1, then the audio stopped playing
        		setplaystatus( false );
        	} else if (msg.what == PlayListTab.START){
        		//If we are sending 2, then the audio started playing
        		setplaystatus( true );
        	} else if (msg.what == PlayListTab.SPIN){
        		//Start spinner
        		final Animation rotate = AnimationUtils.loadAnimation(maincontext, R.anim.rotate);
        		spinner = (ImageView) findViewById(R.id.process); 
        		spinner.startAnimation(rotate);
        	} else if (msg.what == PlayListTab.STOPSPIN){
        		//Stop spinner
        		spinner = (ImageView) findViewById(R.id.process); 
        		spinner.clearAnimation();
        	}
        }
    };
    
    //Set play status
    private void setplaystatus( boolean p) {
    	playstatus = p;
    	
    	//If set to false . Reset screen
    	if ( playstatus == false ){
    		ImageView spinner = (ImageView) findViewById(R.id.process); 
    		spinner.clearAnimation();
    		ImageButton button_playstatus = (ImageButton) findViewById(com.webeclubbin.mynpr.R.id.playstatus);
    		button_playstatus.setImageResource(com.webeclubbin.mynpr.R.drawable.play);
			button_playstatus.postInvalidate();
    	}
    }
    
    //Play audio link
    public void play(String ourstation, final String audiolink){
    	final String TAG = "PLAY audio";

    	currentStation = ourstation;
    	currentURL = audiolink;
    	if (audioStreamer != null ){
    		if (audioStreamer.isPlaying()) {
    			audioStreamer.stop();
    		}
			audioStreamer = null;
		}
    	
    	final Animation rotate = AnimationUtils.loadAnimation(maincontext, R.anim.rotate);
		spinner = (ImageView) findViewById(R.id.process); 
		spinner.startAnimation(rotate);
    	TextView station = (TextView) findViewById(com.webeclubbin.mynpr.R.id.playingstation);
	    station.setText(ourstation + ": ");   
	    TextView content = (TextView) findViewById(com.webeclubbin.mynpr.R.id.playingcontent);
	    content.setText(audiolink); 
	    
	    button_playstatus.setImageResource(com.webeclubbin.mynpr.R.drawable.stop);
		button_playstatus.invalidate();
	    
    	Runnable r = new Runnable() {   
	        public void run() {   
	        	try {
	        		audioStreamer = new StreamingMediaPlayer(maincontext);
	        		audioStreamer.startStreaming(audiolink);
	        	} catch (IOException e){
	        		Log.e(TAG, e.toString());
	        		audioStreamer.stop();
	        	}
	        }   
	    };   
	    new Thread(r).start();
	    
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
        	SearchStationTab.launchhelper(list, maincontext, null, null, null);
            return true;
        }
        return false;
    }
}

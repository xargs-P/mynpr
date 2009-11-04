package com.webeclubbin.mynpr;

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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class PlayListTab extends Activity implements Runnable {
	private ListView lv = null;
	final static public String AUDIO_MIME =  "audio/mpeg";
	final static public String HTML_MIME =  "html";
	final static public String STATION = "STATION";
	final static public String LOGO = "LOGO";
	final static public String URL = "URL";
	
	private IntentFilter ourintentfilter ; 
	//private ProgressDialog dialog = null;
	private PlayList playlist = new PlayList(this);
	private Activity maincontext = null;
	//private String popstorydate = null;
	private boolean updatescreen = true;
	private ImageHelper ih = null;

	private final String POPSTORYLISTVIEW = "POPSTORYLISTVIEW";
	//private final String POPDATE = "POPDATE";
	//private final String IMAGES = "IMAGES";
	
	//private final String playlistfile = "playlist";

	private Thread thread = null;
	private ImageView spinner,npr,button_playstatus = null;
 
	private final int MENU_REFRESH_POPSTORIES = 0;
	private final String IMAGES = "IMAGES";
	//private final String SPLITTERAUDIO = "<MYNPR>";
	//private final String SPLITTERSTATION = "[MYNPR]";
	
    private BroadcastReceiver playListReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	        	final String TAG = "BroadcastReceiver - onReceive";

	  	        	//Grab Image and/or Station Name from intent extra
	  	        	Log.i(TAG, "STATION " + intent.getStringExtra(STATION));
	  	        	Log.i(TAG, "LOGO " + intent.getStringExtra(LOGO));
	  	        	Log.i(TAG, "URL " + intent.getStringExtra(URL));
	  	        	Log.i(TAG, "MIME " + intent.getType());

	        		TextView station = (TextView) findViewById(com.webeclubbin.mynpr.R.id.playingstation);
		  	        station.setText(intent.getStringExtra(STATION) + ": ");   
		  	        TextView content = (TextView) findViewById(com.webeclubbin.mynpr.R.id.playingcontent);
		  	        content.setText(intent.getStringExtra(URL)); 
		  	        
		  	        playlist.addStation(intent.getStringExtra(STATION), intent.getStringExtra(LOGO));
		  	        playlist.addUrl(intent.getStringExtra(STATION), intent.getStringExtra(URL));
		  	        updatescreen();
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
        
        button_playstatus = (ImageView) findViewById(com.webeclubbin.mynpr.R.id.playstatus);
  		lv = (ListView) findViewById(com.webeclubbin.mynpr.R.id.playlist);
        
        /*lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Uri uri = null;
                Intent i = null;
                String oururl = null;
                String TAG = "Playlist List";
                
                //Open Selected stories webpage
            	String [] link = lvpopstories[2];
            	oururl = link[position];

            	uri = Uri.parse( oururl );
 
                i = new Intent("android.intent.action.VIEW", uri, maincontext, com.webeclubbin.mynpr.HTMLviewer.class  );
                //launch intent
                Log.i(TAG, "Position:" + position + " url " + oururl);
                startActivity(i);
            	
            }
        });*/

        button_playstatus.setOnClickListener(new OnClickListener() {
     	   public void onClick(View v) {

     		   final Animation shrinkspin = AnimationUtils.loadAnimation(PlayListTab.this, R.anim.shrinkspin);
     		   final Animation rotate = AnimationUtils.loadAnimation(PlayListTab.this, R.anim.rotate);
     		   //npr = (ImageView) findViewById(R.id.npr);
     		   spinner = (ImageView) findViewById(R.id.process); 
     		   spinner.startAnimation(rotate);
     		   //npr.startAnimation(shrinkspin);
     		   //updatepopstories = true;
     		   //thread = new Thread(PlayListTab.this);
     		   //thread.start();
     	   }
        }); 
        
        //Setup any saved views
        if (savedInstanceState == null){
        	Log.i(TAG, "Bundle savedInstanceState is null.");
        	
         	
    		if (playlist.loadfromfile()) {
    			Log.i(TAG, "Loaded playlist");
    		} else {
    			Log.e(TAG, "Could noy Load playlist");
    		}
    		final Animation rotate = AnimationUtils.loadAnimation(PlayListTab.this, R.anim.rotate);
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
        	//byte[] b = savedInstanceState.getByteArray(POPSTORYLISTVIEW);
        	
        	ih = new ImageHelper(maincontext);
        	ih.setImageStorage(savedInstanceState.getStringArray(IMAGES));
        	
        	/*if ( b != null ) {
        		try {     	    
        	        // Deserialize from a byte array
        			Log.i(TAG, "Deserialize objects from saved Bundle");
        	        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b));
        	        lvpopstories = (String[][]) in.readObject();
        	        in.close();
        	        
        	        //Update the view
        	        Log.i(TAG, "Update list view for Stories");
        	        updatepopscreen();
        	    } catch (ClassNotFoundException e) {
        	    	Log.e(TAG, e.toString());
        	    } catch (IOException e) {
        	    	Log.e(TAG, e.toString());
        	    }
        	}*/

        }

    }
 
    //Thread process for grabbing data
    public void run() {	
    		
    		playlist = grabdata_playlist();
    		handler.sendEmptyMessage(0);
    }
    
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {      
        		updatescreen();
        		    		
        		if (spinner != null ) {
        			if (spinner.getAnimation() != null){
        				spinner.clearAnimation();
        			}        			
        		}
        }
    };
	
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
    	
    	/*try {
    		FileInputStream fis = openFileInput(playlistfile);
    		if (fis == null){
    			Log.i(TAG, "No playlist file to open");
    			return null;
    		}
    		DataInputStream in = new DataInputStream(fis);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            Log.i(TAG,"loop through file");
            //Read File Line By Line
            while ((strLine = br.readLine()) != null)   {
            	// Print the content on the console
            	
            	String [] s = Pattern.compile(SPLITTERAUDIO).split(strLine);
            	if ( strLine.contains(SPLITTERAUDIO) ) {
            		p.addUrl(s[0], s[1]);
            	} else if ( strLine.contains(SPLITTERSTATION) ) {
            		p.addStation(s[0], s[1]);
            	}
            }
    		
    	} catch (IOException ioe) {
    		Log.e(TAG, "Invalid XML format?? " + ioe.getMessage() );
    	}*/
    	
    	return p;
    }
    
	//Save UI state changes to the instanceState.
    /*@Override
    public void onSaveInstanceState(Bundle instanceState) {
    	String TAG = "onSaveInstanceState - PopStoryTab";

    	Log.i(TAG, "START");
    	//Store station list view
    	byte[] bufOfStations = null;

    	try {
    		// Serialize to a byte array - Stations
            ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
            ObjectOutputStream out = new ObjectOutputStream(bos) ;
            out.writeObject(lvpopstories);
            out.close();
        
            // Get the bytes of the serialized object
            bufOfStations = bos.toByteArray();

        } catch (IOException e) {
        	Log.e(TAG, e.toString());
        }
        Log.i(TAG, "Saving lvresults"); //POPDATE
        instanceState.putByteArray(POPSTORYLISTVIEW, bufOfStations);
        Log.i(TAG, "Saving Update Time");
        instanceState.putString(POPDATE, popstorydate);
        Log.i(TAG, "Saving Image Urls");
        instanceState.putStringArray(IMAGES, ih.getUrls() );

    	super.onSaveInstanceState(instanceState);
    }*/
    
    /** Set up Menu for this Tab */
    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
    	menu.add(0, MENU_REFRESH_POPSTORIES, Menu.NONE, "Refresh Stories").setIcon(com.webeclubbin.mynpr.R.drawable.icon);;
        return true;
    }
    
    /* Handles item selections */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_REFRESH_POPSTORIES:
        	button_playstatus.performClick();
            return true;
        }
        return false;
    }
}

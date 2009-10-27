package com.webeclubbin.mynpr;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiManager;
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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class PlayListTab extends Activity implements Runnable {
	private ListView lv = null;
	final static public String AUDIO_MIME =  "audio/mpeg";
	final static public String HTML_MIME =  "html";
	private IntentFilter ourintentfilter ; 
	private ProgressDialog dialog = null;
	private String [][] lvpopstories = null;
	private Activity maincontext = null;
	private String popstorydate = null;
	private boolean updatepopstories = true;
	private ImageHelper ih = null;

	private final String POPSTORYLISTVIEW = "POPSTORYLISTVIEW";
	private final String POPDATE = "POPDATE";
	private final String IMAGES = "IMAGES";
	
	private final String popfile = "popstory.xml";
	private final String popdatefile = "popstory.timeofday";

	
 
	private Thread thread = null;
	private ImageView spinner,npr,button_playstatus = null;
 
	private final int MENU_REFRESH_POPSTORIES = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final String TAG = "onCreate - PlayListTab";
        setContentView(com.webeclubbin.mynpr.R.layout.playlisttab);
        maincontext = this;

        try {
        	ourintentfilter = new IntentFilter(Intent.ACTION_VIEW, AUDIO_MIME);
        } catch (IntentFilter.MalformedMimeTypeException  e ) {
        	Log.e(TAG, "MalformedMimeTypeException");
        }
        registerReceiver (playListReceiver, ourintentfilter);
        
        button_playstatus = (ImageView) findViewById(com.webeclubbin.mynpr.R.id.playstatus);
  		lv = (ListView) findViewById(com.webeclubbin.mynpr.R.id.playlist);
        
  		
  		

        
        
        lv.setOnItemClickListener(new OnItemClickListener() {
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
        });

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
        /*if (savedInstanceState == null){
        	Log.i(TAG, "Bundle savedInstanceState is null.");
        	
         	
    		MyNPR parent = (MyNPR) getParent(); 
    		Log.i(TAG, "Parent| bundle null? " + parent.isbundlenull() );
        	if ( parent.isbundlenull()  ) {
        		Log.i(TAG, "Set up Dialog box ");
        		dialog = new ProgressDialog(this);
        		dialog.setIndeterminate(true);
        		dialog.setCancelable(true); 
        		dialog.setTitle("One Moment...");
    		
        		if (updatepopstories == true){	
        			dialog.setMessage("Grabbing stories from npr.org...");	
        		}
        		dialog.show();
        	} else { 
        		Log.i(TAG, "Skip setting up Dialog box");
        	} 
    		
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
        	byte[] b = savedInstanceState.getByteArray(POPSTORYLISTVIEW);
        	popstorydate = savedInstanceState.getString(POPDATE);
        	ih = new ImageHelper(maincontext);
        	ih.setImageStorage(savedInstanceState.getStringArray(IMAGES));
        	
        	if ( b != null ) {
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
        	}

        }*/

    }
    
    private BroadcastReceiver playListReceiver = new BroadcastReceiver() {
  	        @Override
  	        public void onReceive(Context context, Intent intent) {
  	        	final String TAG = "BroadcastReceiver - onReceive";
  	        	Uri uri = intent.getData();
  	        	if ( uri == null ) {
  	        		Log.e(TAG , "uri null");
  	        	} else {
  	        		Log.i(TAG, uri.toString());
  	        		TextView t = (TextView) findViewById(com.webeclubbin.mynpr.R.id.playingcontent);
  	  	        	t.setText(uri.toString());
  	  	        	//Grab Image and/or Station Name from intent extra
  	        		
  	        	}
  	          
  	        }
  	    };
 
    
    public void run() {	
    		//lvpopstories = grabdata_popstories(popstoriesurl);
    		handler.sendEmptyMessage(0);
    }
    
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {      
        		updatepopscreen();
        		
        		if ( dialog != null) {
        			if ( dialog.isShowing() ){
        				dialog.cancel();
        			}
        		}
        		
        		if (spinner != null && npr != null) {
        			if (spinner.getAnimation() != null){
        				spinner.clearAnimation();
        			}
        			if (npr.getAnimation() != null){
        				npr.clearAnimation();
        			}
        			
        		}
        }
    };
	
    private void updatepopscreen(){
		String [] title = null; 
		String [] image = null;

		title = lvpopstories[0];
		image = lvpopstories[1];

		String TAG = "updatepopscreen";

		Log.i(TAG, "ENTER");
		if ( ih == null ){
			ih = new ImageHelper(maincontext);
			ih.setImageStorage(image);
		} else if (updatepopstories == true) {
			ih.setImageStorage(image);
		}

		lv.setAdapter( new PopStoriesAdapter(maincontext,
				com.webeclubbin.mynpr.R.layout.popstoryrow, 
				title, image, ih) );
		
		//Set date on view
		TextView poprefresh = (TextView) findViewById(com.webeclubbin.mynpr.R.id.poprefresh);
		poprefresh.setText("Refreshed " + popstorydate );
		
    	//Tell UI to update our list
		Log.i(TAG, "update screen");
    	lv.invalidate();
    }    
   
    /** Parse Popular Stories XML file */
    private String[][] grabdata_popstories(String strURL) {
    	URL url;
    	URLConnection urlConn = null;
    	String TAG = "grabdata - pop stories";
    	SAXParser saxParser;
    	long saxelapsedTimeMillis;
    	long saxstart;
    	int byteRead = 0;
    	byte[] buf = new byte[1024];

    	SAXParserFactory factory = SAXParserFactory.newInstance();
    	MyContentHandlePopStoriesSAX myHandler = new MyContentHandlePopStoriesSAX();

    	//Check to see if we need to download anything
    	if ( updatepopstories == true ) {
    	
    		try {
    			url = new URL(strURL);
    			urlConn = url.openConnection();
    			Log.i( TAG, "Got data for SAX");
    		} catch (IOException ioe) {
    			Log.e( TAG, "Could not connect to " + strURL );
    		}
    	
    		//Save to file
    		try {
    			InputStream is = urlConn.getInputStream() ;
    			FileOutputStream fos = openFileOutput(popfile, Context.MODE_PRIVATE);
    		  		
    			while (  (byteRead = is.read(buf)) != -1) {
    				fos.write(buf, 0, byteRead);
    			}
    			is.close();
    			fos.close();
    			Log.i( TAG, "Saved xml to file");
    		
    			//Save time of day we downloaded file
    			Calendar cal = Calendar.getInstance();
    			//SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
    			//popstorydate = sdf.format(cal.getTime());
    		
    			fos = openFileOutput(popdatefile, 0);
    			PrintWriter datewriter = new PrintWriter(fos,true);
    			//print text to file
    			datewriter.println( System.currentTimeMillis() );
    			fos.close();
    			datewriter.close();
    			Log.i( TAG, "Saved xml download date to file");

    		} catch (FileNotFoundException	e) {
    			Log.e( TAG, e.toString() );
    		} catch (IOException ioe) {
    			Log.e(TAG, "Unable to save file locally " + ioe.getMessage() );
    		}
    	}
    	saxstart = System.currentTimeMillis();
    	try {
    		FileInputStream fis = openFileInput(popfile);
    		saxParser =  factory.newSAXParser();
    		Log.i( TAG, "Before: Parser - SAX");
    		saxParser.parse( fis , myHandler);
    		//saxParser.parse( urlConn.getInputStream() , myHandler);
    		Log.i( TAG, "AFTER: Parse - SAX");
    	} catch (IOException ioe) {
    		Log.e(TAG, "Invalid XML format?? " + ioe.getMessage() );
    	} catch (ParserConfigurationException pce) {
    		Log.e(TAG, "Could not parse XML " + pce.getMessage());
    	} catch (SAXException se) {
    		Log.e(TAG, "Could not parse XML"  + se.getMessage());
    	}
    	saxelapsedTimeMillis = (System.currentTimeMillis() - saxstart ) / 1000;
    	Log.i("SAX - TIMER", "Time it took in seconds:" + Long.toString(saxelapsedTimeMillis));
    	String [][] r = { myHandler.getTitles() , myHandler.getImages(), myHandler.getLinks() };

    	return r;
    }
    
	//Save UI state changes to the instanceState.
    @Override
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
    }
    
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

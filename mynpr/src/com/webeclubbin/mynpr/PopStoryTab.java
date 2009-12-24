package com.webeclubbin.mynpr;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
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
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class PopStoryTab extends Activity implements Runnable {
	private ListView lv = null;
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

	private String popstoriesurl = "http://api.npr.org/query?id=100&fields=title,image&output=NPRML&apiKey="  ;
	//private final String DATE_FORMAT_NOW = "EEE, MMM d 'at' hh:mm a";
	private final String DATE_FORMAT_NOW = "h:mm a";
	private Thread thread = null;
	private ImageView spinner,npr = null;
	private TextView button_poprefresh = null;
	private boolean madeAttempt = false;
	
	private final int MENU_REFRESH_POPSTORIES = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final String TAG = "onCreate - PopStoryTab";
        setContentView(com.webeclubbin.mynpr.R.layout.popstorytab);
        popstoriesurl = popstoriesurl + this.getString( com.webeclubbin.mynpr.R.string.nprapi );
        
        maincontext = this;

        // Resize Button Images
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        button_poprefresh = (TextView) findViewById(com.webeclubbin.mynpr.R.id.poprefresh);
        
		lv = (ListView) findViewById(com.webeclubbin.mynpr.R.id.lview);
       
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Uri uri = null;
                Intent i = null;
                String oururl = null;
                String TAG = "Intent / Open URL";
                
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

        button_poprefresh.setOnClickListener(new OnClickListener() {
     	   public void onClick(View v) {

     		   final Animation shrinkspin = AnimationUtils.loadAnimation(PopStoryTab.this, R.anim.shrinkspin);
     		   final Animation rotate = AnimationUtils.loadAnimation(PopStoryTab.this, R.anim.rotate);
     		   npr = (ImageView) findViewById(R.id.npr);
     		   spinner = (ImageView) findViewById(R.id.process); 
     		   spinner.startAnimation(rotate);
     		   npr.startAnimation(shrinkspin);
     		   updatepopstories = true;
     		   thread = new Thread(PopStoryTab.this);
     		   thread.start();
     	   }
        }); 
        
        //Setup any saved views
        if (savedInstanceState == null){
        	Log.i(TAG, "Bundle savedInstanceState is null.");
        	
    		//Check to see if we need to even download the web content    		
        	timeToUpdate();	
        	
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
        	//ih.setImageStorage(savedInstanceState.getStringArray(IMAGES));
        	
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
        	
        	if ( timeToUpdate() ){
        		Toast.makeText(PopStoryTab.this, "Refreshing Stories...", Toast.LENGTH_LONG).show();
        		button_poprefresh.performClick();
        	}
        	
        }

    }
    
    private boolean timeToUpdate(){
    	final String TAG = "timeToUpdate";
    	final int TIMETOWAIT = 1800;
		//Check to see if we need to even download the web content
    	try {
    		
    		FileInputStream fis = openFileInput(popdatefile);
    		InputStreamReader isr = new InputStreamReader(fis);
    		BufferedReader input =  new BufferedReader(isr);  
    		long oldtime = Long.parseLong( input.readLine() );
    		long currenttime = System.currentTimeMillis() ;
    		long elaspedtime =  ( currenttime - oldtime ) / 1000 ;

    		fis.close();
    		isr.close();
    		input.close();
    		Log.i( TAG, "Got time since last update");
    		
    		if ( elaspedtime < TIMETOWAIT) {
    			//under TIMETOWAIT
    			updatepopstories = false;
    			//Save time of day we downloaded file
            	SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
            	popstorydate = sdf.format( new Date(oldtime) );
            	
    			Log.i( TAG, "Under " + (TIMETOWAIT/60) + "mins since last update. So no need to redownload: "  + elaspedtime);
        		return false;
        	
    		} else {
    			Log.i( TAG, "Over " + (TIMETOWAIT/60) + "mins since last update: " + elaspedtime);
    			return true;
    		}

    	} catch (FileNotFoundException	e) {
    		Log.e( TAG, e.toString() );
    		return true;
    	} catch (IOException ioe) {
    		Log.e(TAG, ioe.getMessage() );
    		return true;
    	}
    }
    
    public void run() {	
    		String TAG = "Thread - Run";
    		lvpopstories = grabdata_popstories(popstoriesurl);
    		if (lvpopstories != null){
    			handler.sendEmptyMessage(0);
    		} else {
    			if (madeAttempt == false){
    				madeAttempt = true;
    				Log.i(TAG, "Try to redownload again.");
    				updatepopstories = true;
    				thread = new Thread(this);
    				thread.start();
    			} else {
    				Log.i(TAG, "We have already tried once. Give up.");
    				madeAttempt = false;
    				Toast.makeText(this, "Trouble connecting to npr.org. Try to refresh later.", Toast.LENGTH_LONG).show();
    			}
    		}
    		
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
		String TAG = "updatepopscreen";
		
		Log.i(TAG, "ENTER");
		
		title = lvpopstories[0];
		image = lvpopstories[1];
		
		if (title == null){
			Log.i(TAG, "Null Title");
		}
		if (image == null){
			Log.i(TAG, "Null image");
		}
		

		
		
		/*if ( ih == null ){
			ih = new ImageHelper(maincontext);
			ih.setImageStorage(image);
		} else if (updatepopstories == true) {
			ih.setImageStorage(image);
		}*/

		lv.setAdapter( new PopStoriesAdapter(maincontext,
				com.webeclubbin.mynpr.R.layout.popstoryrow, 
				title, image, ih) );
		
		//Set date on view
		TextView poprefresh = (TextView) findViewById(com.webeclubbin.mynpr.R.id.poprefresh);
		if (popstorydate == null){
			popstorydate = "";
		}
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
    	
    		Log.i( TAG, "Connect to " + strURL );
    		try {
    			url = new URL(strURL);
    			urlConn = url.openConnection();
    			urlConn.setConnectTimeout(10000);
    			Log.i( TAG, "Opened connection");
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
    			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
    			popstorydate = sdf.format(cal.getTime());
    		
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
    		
    		Log.i( TAG, "AFTER: Parse - SAX");
    	} catch (FileNotFoundException notfound) {
    		Log.e(TAG, "File not found.");
    		//Log.i(TAG, "redownload data");
    		//updatepopstories = true;
			//grabdata_popstories( strURL );
    	} catch (IOException ioe) {
    		Log.e(TAG, "Invalid XML format?? " + ioe.getMessage() );
    	} catch (ParserConfigurationException pce) {
    		Log.e(TAG, "Could not parse XML " + pce.getMessage());
    	} catch (SAXException se) {
    		Log.e(TAG, "Could not parse XML "  + se.getMessage());
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

        if (ih != null){
            Log.i(TAG, "Saving Image Urls");
        	instanceState.putStringArray(IMAGES, ih.getUrls() );
        }

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
        	button_poprefresh.performClick();
            return true;
        }
        return false;
    }
}

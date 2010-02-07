package com.webeclubbin.mynpr;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SearchStationTab extends Activity implements Runnable {

	private ListView lvsearch = null;

	private Activity maincontext = null;
	private boolean updatestationsearch = false;
	
	private String searchstationurl = "http://api.npr.org/stations.php?apiKey=" ;
	private String searchstationurlwhole = "";
	
	final private int MENU_LIVE_NPR = 0;
	private final String NPRLIVEURL = "http://www.npr.org/streams/mp3/nprlive24.pls";
	
	final String STATIONLISTVIEW = "STATIONLISTVIEW";
	final String HIDESEARCH = "HIDESEARCH";

	private Thread thread = null;
	private ImageView spinner = null;
	private Station[] stationstodisplay = null;
	
	final static String PLS = ".pls";
    final static String M3U = ".m3u";
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final String TAG = "onCreate - SearchStationTab";
        searchstationurl = searchstationurl + this.getString( com.webeclubbin.mynpr.R.string.nprapi );
        setContentView(com.webeclubbin.mynpr.R.layout.searchtab);    
        
        final ImageView button_search = (ImageView) findViewById(com.webeclubbin.mynpr.R.id.stationsearchbutton);

		lvsearch = (ListView) findViewById(com.webeclubbin.mynpr.R.id.lvstatfind);
		EditText txtsearch = (EditText) findViewById(com.webeclubbin.mynpr.R.id.stationsearchbox);
		maincontext = this;		
		
		//SlidingDrawer sd = (SlidingDrawer) findViewById( com.webeclubbin.mynpr.R.id.drawer);
		//playlisttab.onCreate(new Bundle());
        
        button_search.setOnClickListener(new OnClickListener() {
      	   public void onClick(View v) {
      		   
      		   // Perform action on clicks
      		   final String TAG = "button_search";
      		   String searchquery = null;
      		   String city, state = null;
      		   String [] loc = null;
      		   
      		   EditText ed = (EditText) findViewById( com.webeclubbin.mynpr.R.id.stationsearchbox);
      		   final LinearLayout header = (LinearLayout) findViewById( com.webeclubbin.mynpr.R.id.header);
      		   
      		   //Show views if they are hiding else process users input
      		   
      		   if (header.getVisibility() == View.GONE){

      			   header.setVisibility(View.VISIBLE);
      			   Log.d(TAG,"Show hidden views");
      		   } else {
      			   Log.d(TAG,"Process user input");
      			   String searchtext = ed.getText().toString();
      			   searchtext = searchtext.trim();
      			   Log.d(TAG, "TEXT" + searchtext + "TEXT");
      			   Pattern p = Pattern.compile("^[0-9]+$");
      			   Matcher m = p.matcher( searchtext );
      			   boolean zip = m.matches(); 
      			   p = Pattern.compile(".*,.*");
      			   m = p.matcher( searchtext );
      			   boolean citystate = m.matches(); 
      			   boolean grabdata = true;
      			   if ( zip == true) {
      				   Log.d(TAG,"found zip");
      				   searchquery="&zip=" + searchtext;
      			   } else if ( citystate == true) {
      				   Log.d(TAG,"Found city , state");
      				   loc = Pattern.compile(",").split(searchtext);
      				   city = loc[0].trim();
      				   city = city.toLowerCase();
      				   state = loc[1].trim();
      				   state = state.toUpperCase();
      				   try {
      					   city = URLEncoder.encode(city,"UTF-8");
      					   state = URLEncoder.encode(state,"UTF-8");
      				   } catch (UnsupportedEncodingException e) {
      					   Log.e(TAG, e.toString() );
      				   }
      				   searchquery="&city=" + city + "&state=" + state;
      			   
      			   } else {
      				   //Error!
      				   Toast.makeText(SearchStationTab.this, "Opps! Please enter numerical ZIP or 'City, State'", Toast.LENGTH_LONG).show();
      				   grabdata = false;
      			   }
      		   
      			   if (grabdata){
      				   searchstationurlwhole = searchstationurl + searchquery;
      				   final Animation rotate = AnimationUtils.loadAnimation(SearchStationTab.this, R.anim.rotate);
      				   spinner = (ImageView) findViewById(R.id.stationprocess); 
      				   spinner.startAnimation(rotate);
      		   
      				   updatestationsearch = true;
      				   thread = new Thread(SearchStationTab.this);
      				   thread.start();
      			   }
      		   }
      	   }
        }); 
        
        txtsearch.setOnKeyListener(new OnKeyListener(){
        	public boolean onKey(View v, int keyCode, KeyEvent event) {
        		if ( event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
        			Log.d(TAG, "Text Search box - Enter Pressed: Run search");
        			// close soft keyboard 
        			InputMethodManager inputManager = (InputMethodManager) maincontext.getSystemService(Context.INPUT_METHOD_SERVICE); 
        			inputManager.hideSoftInputFromWindow( v.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY); 
        			
        			button_search.performClick();
        			return true;
        		} else {
        			return false;
        		}
        	}
        });
        
        //Setup any saved views
        if (savedInstanceState == null){
        	Log.d(TAG, "Bundle savedInstanceState is null.");
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
        	
        	int visibility = savedInstanceState.getInt(HIDESEARCH);
        	final LinearLayout header = (LinearLayout) findViewById( com.webeclubbin.mynpr.R.id.header);
        	Log.d(TAG, "Current Visibility " + header.getVisibility() );
        	Log.d(TAG, "old header visibility " + String.valueOf(visibility ) );
        	Log.d(TAG, "View GONE " + String.valueOf(View.GONE ) );
        	Log.d(TAG, "View VISIBLE " + String.valueOf(View.VISIBLE ) );
        	header.setVisibility(visibility);
        	Log.d(TAG, " Visibility after setting" + header.getVisibility() );
        	
        	byte[] b = savedInstanceState.getByteArray(STATIONLISTVIEW);
        	if ( b != null) {
        		try {     	    
        	        // Deserialize from a byte array
        			Log.d(TAG, "Deserialize Stations from saved Bundle");
        	        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b));
        	        stationstodisplay = (Station[]) in.readObject();
        	        in.close();
        	        
        	        if ( stationstodisplay != null ) {
        	        	//Update the list view
        	        	Log.d(TAG, "Update list view for stations");
        	        	updatesearchstationscreen();
        	        } else {
        	        	Log.d(TAG, "Skipping list view update, null station list");
        	        }
        	    } catch (ClassNotFoundException e) {
        	    	Log.e(TAG, e.toString());
        	    } catch (IOException e) {
        	    	Log.e(TAG, e.toString());
        	    }
        	}
        	
        }
        
    }
    
    public void run() {	
    		stationstodisplay = grabdata_searchradio(searchstationurlwhole);
    		handler.sendEmptyMessage(0);
    }
    
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {      
        	
        	if (updatestationsearch){
        		updatesearchstationscreen();
        		if (spinner != null) {
        			if (spinner.getAnimation() != null){
        				spinner.clearAnimation();
        			}
        		}
        		updatestationsearch = false;
        	} 
        }
    };
	   
    private void updatesearchstationscreen(){
    	String TAG = "updatesearchstationscreen";
    	
    	Log.d(TAG, "ENTER");

    	final LinearLayout header = (LinearLayout) findViewById( com.webeclubbin.mynpr.R.id.header);   	
 		
    	lvsearch.setAdapter( new SearchStationAdapter(maincontext, 
    			com.webeclubbin.mynpr.R.layout.stationrow, stationstodisplay) );
 		
		//Tell UI to update our list
		Log.d(TAG, "update screen - invalidate");
		lvsearch.invalidate();
		header.setVisibility(View.GONE);
		
    }
    
    
    /** Parse XML file  */
    private Station[] grabdata_searchradio(String strURL) {
    	URL url;
    	URLConnection urlConn = null;
    	final String TAG = "grabdata - searchradio";
    	SAXParser saxParser;
    	long saxelapsedTimeMillis;
    	long saxstart;

    	SAXParserFactory factory = SAXParserFactory.newInstance();
    	MyContentHandleSearchStationSAX myHandler = new MyContentHandleSearchStationSAX();

    	try {
    		url = new URL(strURL);
    		urlConn = url.openConnection();
    		Log.d( TAG, "Got data for SAX " + strURL);
    	} catch (IOException ioe) {
    		Log.e( TAG, "Could not connect to " +  strURL);
    	}
    	saxstart = System.currentTimeMillis();

    	//Do error checking!!!!
    	
    	//Parse xml
    	try {
    		saxParser =  factory.newSAXParser();
    		Log.d( TAG, "Before: Parser - SAX");
    		InputStream is = urlConn.getInputStream();
    		saxParser.parse( is , myHandler);
    		is.close();
    		Log.d( TAG, "AFTER: Parse - SAX");
    	} catch (IOException ioe) {
    		Log.e(TAG, "Invalid XML format?? " + ioe.getMessage() );
    	} catch (ParserConfigurationException pce) {
    		Log.e(TAG, "Could not parse XML " + pce.getMessage());
    	} catch (SAXException se) {
    		Log.e(TAG, "Could not parse XML"  + se.getMessage());
    	}
    	saxelapsedTimeMillis = (System.currentTimeMillis() - saxstart ) / 1000;
    	
    	Log.d("SAX - TIMER", "Time it took in seconds:" + Long.toString(saxelapsedTimeMillis));

    	return myHandler.getStations();
    }
    
    /** Parse Playlist file - PLS */
    public static String[] parsePLS(String strURL, Context c) {
    	URL url;
    	URLConnection urlConn = null;
    	String TAG = "parsePLS";
    	Vector<String> radio = new Vector<String>();
    	final String filetoken = "file";
    	final String SPLITTER = "=";

   		try {
   			url = new URL(strURL);
   			urlConn = url.openConnection();
   			Log.d( TAG, "Got data");
   		} catch (IOException ioe) {
   			Log.e( TAG, "Could not connect to " + strURL );
   		}
    
   		
    	try {

    		DataInputStream in = new DataInputStream( urlConn.getInputStream() ); 
    	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
    	    String strLine;
    	    //Read File Line By Line
    	    while ((strLine = br.readLine()) != null)   {
    	    	String temp = strLine.toLowerCase();
    	    	Log.d(TAG, strLine);
    	    	//Look for files we want
    	    	if ( temp.startsWith(filetoken) ){
    	    		String [] s = Pattern.compile(SPLITTER).split(temp);
    	    		radio.add(s[1]);
    	    		Log.d(TAG, "Found audio " + s[1]);
    	    	}
    	    }
    	    //Close the streams
    	    br.close();
    	    in.close();

    	} catch (Exception e) {
    		Log.e(TAG, "Trouble reading file: " + e.getMessage() );
    	}

    	String[] t = new String [0];
    	String[] r = null;
    	if ( radio.size() != 0 ) {
    		r = (String[])radio.toArray(t);
    		Log.d(TAG, "Found total: " + String.valueOf( r.length ) );
    	}
    	return r;
    }
    
    /** Parse Playlist file - M3U */
    public static String[] parseM3U(String strURL, Context c) {
    	URL url;
    	URLConnection urlConn = null;
    	String TAG = "parseM3U";
    	Vector<String> radio = new Vector<String>();
    	final String filetoken = "http";
    	
   		try {
   			url = new URL(strURL);
   			urlConn = url.openConnection();
   			Log.d( TAG, "Got data");
   		} catch (IOException ioe) {
   			Log.e( TAG, "Could not connect to " + strURL );
   		}
    		
    	try {
    		DataInputStream in = new DataInputStream(urlConn.getInputStream());
    	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
    	    String strLine;
    	    //Read File Line By Line
    	    while ((strLine = br.readLine()) != null)   {
    	    	String temp = strLine.toLowerCase();
    	    	Log.d(TAG, strLine);
    	    	//Look for file we want
    	    	if ( temp.startsWith(filetoken) ){
    	    		radio.add(temp);
    	    		Log.d(TAG, "Found audio " + temp);
    	    	}
    	    }
    	    //Close the streams
    	    br.close();
    	    in.close();
    	} catch (Exception e) {
    		Log.e(TAG, "Trouble reading file: " + e.getMessage() );
    	}

    	String[] t = new String [0];
    	String[] r = null;
    	if ( radio.size() != 0 ) {
    		r = (String[])radio.toArray(t);
    		Log.d(TAG, "Found total: " + String.valueOf( r.length ) );
    	}
    	return r;
    }
    
	//Save UI state changes to the instanceState.
    @Override
    public void onSaveInstanceState(Bundle instanceState) {
    	String TAG = "onSaveInstanceState - SearchStationTab";

    	Log.d(TAG, "START");
    	//Store station list view
    	byte[] bufOfStations = null;
    	try {
    		// Serialize to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
            ObjectOutputStream out = new ObjectOutputStream(bos) ;
            out.writeObject(stationstodisplay);
            out.close();
        
            // Get the bytes of the serialized object
            bufOfStations = bos.toByteArray();
        } catch (IOException e) {
        	Log.e(TAG, e.toString());
        }
        
        Log.d(TAG, "Saving stationstodisplay");
        instanceState.putByteArray(STATIONLISTVIEW, bufOfStations);
        Log.d(TAG, "Saving " + HIDESEARCH);
        final LinearLayout header = (LinearLayout) findViewById( com.webeclubbin.mynpr.R.id.header);
        instanceState.putInt(HIDESEARCH, header.getVisibility() );
    	super.onSaveInstanceState(instanceState);
    }
    
	//Launch URL user selected and display dialog if more than one choice
	public static void launchhelper( String[] s , final Activity a, final Dialog previousdialog, final String station, final String logo) {
		//TODO Let users select which link they want to play
		String TAG = "launchhelper";
		Log.d(TAG, "START" );
		
		Uri uri = null;
        Intent i = null;
        
        if (s == null){
        	Log.d(TAG, "No data received" );
			Toast.makeText(a, "Unable to grab audio. Please try again.", Toast.LENGTH_LONG).show();
			return;
        }
        
		if (s.length == 1){
			String playthis = s[0];
			Log.d(TAG, "Only one url: " + playthis );
			i = new Intent(Intent.ACTION_VIEW); 
        	uri = Uri.parse( playthis );

            URL url;
            URLConnection urlConn = null;

        	try {
        		url = new URL(playthis);
        		urlConn = (HttpURLConnection)url.openConnection();
        		//See if this is a type we can handle
        		String ctype = urlConn.getContentType () ;
        		Log.d(TAG, "Content Type: " + ctype );
        		if (ctype == null){
        			ctype = "";
        		}
        		 
        		if (ctype.contains(StreamingMediaPlayer.AUDIO_MPEG) || ctype.equals("") ){
        			
        			i = new Intent(MyNPR.tPLAY);

                    i.putExtra(PlayListTab.STATION, station);
                    i.putExtra(PlayListTab.LOGO, logo);
                    i.putExtra(PlayListTab.URL, uri.toString());
                    
                    Uri u = i.getData();
                    if ( u == null){
                    	Log.e(TAG, "uri null");
                    } else {
                    	Log.v(TAG, "uri okay: " + u.toString());
                    	
                    }
                    Log.v(TAG, "mime type: " + i.getType());
                    //launch intent
                    Log.d(TAG, "Launch Playlist Tab");
                    if (previousdialog != null){
                    	previousdialog.dismiss();
                    }

                    MyNPR parent = (MyNPR) a.getParent();
                    
                    Log.d(TAG, "Switch to playlist tab");
                    parent.tabHost.setCurrentTabByTag(MyNPR.tPLAY);
                    
                    Log.d(TAG, "Broadcast playlist intent");
                    a.sendBroadcast (i) ;
                     
        		} else {

                	uri = Uri.parse( playthis );
                    i = new Intent(Intent.ACTION_VIEW, uri, a, com.webeclubbin.mynpr.HTMLviewer.class  );
                    
                    //launch intent
                    Log.d(TAG, "Launch HTML viewer");
                    a.startActivity(i);
        		}
	
        	} catch (IOException ioe) {
        		Log.e( TAG, "Could not connect to " +  playthis );
        	} 

		} else { 
			final Dialog d = new Dialog(a);
			d.setContentView(com.webeclubbin.mynpr.R.layout.urlpopup);
			d.setTitle("Multiple Audio links found.");
			ListView lv2 = (ListView) d.findViewById(com.webeclubbin.mynpr.R.id.urllist);
			lv2.setAdapter(new ArrayAdapter<String>(a,
        		com.webeclubbin.mynpr.R.layout.urllist ,
                s));
			lv2.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView parent, View v, int position, long id) {
					Uri uri = null;
					Intent i = null;
					String playthis;
					String TAG = "Intent / Open URL";
                
					//Open Selected media URL
					TextView t = (TextView)v;
					
					String loc = t.getText().toString();
					String[] r = null;
					if ( loc.toLowerCase().endsWith(PLS)) {
						r = parsePLS( loc , a );
					} else if ( loc.toLowerCase().endsWith(M3U)) {
						r = parseM3U( loc, a );
					} else {
						r = new String[] {loc};
					}
            	
					//Check data we get back
					if ( r == null ) {
						//Create toast telling user we have nothing.
						Log.d(TAG, "No data returned" );
						Toast.makeText(a, "No Audio Found inside Station's Playlist", Toast.LENGTH_LONG).show();
					} else if ( (r.length == 1) && (! r[0].toLowerCase().endsWith(PLS)) && (! r[0].toLowerCase().endsWith(M3U)) ) {
						Log.d(TAG, "Found One" );
						
						launchhelper(r, a, d, station, logo);

					} else {
						Log.d(TAG, "Found Several or a list: " );
						//Let users select which link they want to play
						launchhelper(r, a, d, station, logo);
					}
				}
			});
    	
			if ( previousdialog != null){
				Log.d(TAG, "Show Dialog and dismiss old one" );
				previousdialog.dismiss();
			}
			d.show();
		}
	}
	
	//Launch URL user selected and display dialog if more than one choice
	public static void launchhelper( final PlayURL pu , final Activity a, final Dialog previousdialog) {
		//TODO Let users select which link they want to play
		String TAG = "launchhelper";
		Log.d(TAG, "START" );
		
		Uri uri = null;
        Intent i = null;
        
        if (pu == null){
        	Log.d(TAG, "No data received" );
			Toast.makeText(a, "Unable to grab audio. Please try again.", Toast.LENGTH_LONG).show();
			return;
        }
        
		
		String playthis = pu.getURL();
		Log.d(TAG, "Only one url: " + playthis );
		i = new Intent(Intent.ACTION_VIEW); 
        uri = Uri.parse( playthis );

        URL url;
        URLConnection urlConn = null;

        try {
        	url = new URL(playthis);
        	urlConn = (HttpURLConnection)url.openConnection();
        	//See if this is a type we can handle
        	String ctype = urlConn.getContentType () ;
        	Log.d(TAG, "Content Type: " + ctype );
        	if (ctype == null){
        		ctype = "";
        	}
        	 
        	if (ctype.contains(StreamingMediaPlayer.AUDIO_MPEG) || ctype.contains(PlayListTab.RSS_MIME) || ctype.contains(PlayListTab.XML_MIME) || ctype.equals("") ){
        		
        		i = new Intent(MyNPR.tPLAY);
        		
        		i.putExtra(PlayListTab.URL, pu.getURL());
                i.putExtra(PlayListTab.STATION, pu.getStation());
                i.putExtra(PlayListTab.LOGO, pu.getLogo());
                    
                if (ctype.contains(PlayListTab.RSS_MIME) || ctype.contains(PlayListTab.XML_MIME) || pu.isRSS()){
                	Log.d(TAG, "RSS content");
                   	i.putExtra(PlayList.SPLITTERRSS, true);
                   	i.putExtra(PlayList.SPLITTERRSSTITLE, pu.getTitle());
                }
                    
                Uri u = i.getData();
                if ( u == null){
                   	Log.e(TAG, "uri null");
                } else {
                   	Log.v(TAG, "uri okay: " + u.toString());
                }
                
                Log.v(TAG, "mime type: " + i.getType());
                //launch intent
                Log.d(TAG, "Launch Playlist Tab");
                if (previousdialog != null){
                   	previousdialog.dismiss();
                }

                MyNPR parent = (MyNPR) a.getParent();
                    
                Log.d(TAG, "Switch to playlist tab");
                parent.tabHost.setCurrentTabByTag(MyNPR.tPLAY);
                    
                Log.d(TAG, "Broadcast playlist intent");
                a.sendBroadcast (i) ;
                     
        	} else {

                uri = Uri.parse( playthis );
                i = new Intent(Intent.ACTION_VIEW, uri, a, com.webeclubbin.mynpr.HTMLviewer.class  );
                    
                //launch intent
                Log.d(TAG, "Launch HTML viewer");
                a.startActivity(i);
        	}
	
        } catch (IOException ioe) {
        	Log.e( TAG, "Could not connect to " +  playthis );
        } 
		
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
}

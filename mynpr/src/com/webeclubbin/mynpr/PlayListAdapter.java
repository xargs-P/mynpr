package com.webeclubbin.mynpr;



import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
//Display playlist information
public class PlayListAdapter extends ArrayAdapter<String> {

	Activity context;  
	int ourlayoutview;
	ImageHelper im = null;
	PlayList playlist = null;
	String [] stations = null;
	String TAG = "PlayListAdapter";
	ProgressDialog waitdialog = null;
	Dialog dialog = null;
	
	//Too lazy to send this inside a Message for Handler
	Podcast[] pods = null;
	  
	PlayListAdapter(Activity context, int ourview, PlayList p, ImageHelper imagehelper) {
		super(context, ourview, p.getStations()); 
		
		Log.d(TAG,"Creation");

        ourlayoutview = ourview;
        this.context=context;  
        
        stations = p.getStations();
        playlist = p;
        
        if (imagehelper != null){
        	Log.d(TAG,"Store imagehelper received");
        	this.im = imagehelper;
        } else {
        	im = new ImageHelper(context);
        }
    }  

	@Override
    public View getView(final int position, View convertView, ViewGroup parent) {  
         
    	Log.d(TAG,"getView " + stations[position] );
    	LinearLayout  row = (LinearLayout) View.inflate(context, ourlayoutview, null);
 
        String [] urls = playlist.getUrls(stations[position]);
        if (urls != null){
        	for (int i = 0; i < urls.length; i++) { 
        		View audiourl = null;
        		if (urls[i].contains(PlayList.SPLITTERRSS)) {
        			//We have an rss url
        			String temp = urls[i].substring(PlayList.SPLITTERRSS.length() ) ;
        			audiourl = (LinearLayout) View.inflate(context, com.webeclubbin.mynpr.R.layout.rssaudiolink, null);
        			TextView t = (TextView) audiourl.findViewById( com.webeclubbin.mynpr.R.id.audiourl );
            		t.setText(temp);
            		ImageView iv = (ImageView) audiourl.findViewById( com.webeclubbin.mynpr.R.id.rss );
            		iv.setVisibility(ImageView.VISIBLE);
        		} else {
        			audiourl = (TextView) View.inflate(context, com.webeclubbin.mynpr.R.layout.audiolink, null);
            		((TextView) audiourl).setText(urls[i]);
        		}
        		
        		
        		audiourl.setOnClickListener(new View.OnClickListener() {
                    public void onClick( View v ) {
                    	String TAG = "Playlist List Click";
                        Log.d(TAG, "Grab url and play");
                        TextView t = (TextView) v;
                        String oururl = (String) t.getText();
                        
                        PlayListTab p = (PlayListTab) context; 
                        p.play(stations[position], oururl, false);

                    }
                });
        		/*setOnLongClickListener(View.OnLongClickListener l)
        		  audiourl.setOnLongClickListener(new View.OnLongClickListener() {
                    public void onLongClick( View v ) {
                    	String TAG = "Playlist Long Click";
                        Log.d(TAG, "Create dialog to delete");
                        TextView t = (TextView) v;
                        String oururl = (String) t.getText();
                        
                        PlayListTab p = (PlayListTab) context; 
                        p.play(stations[position], oururl);
						//We need to modify playlist inside playlisttab
						//Also we need to determine if this is the last link for the station and if so, delete the station
						//We also need to reset the adapter
                    }
                });
        		*/
        		row.addView(audiourl);
        	}
        	
        } else {
        	//row = new LinearLayout(context);
        	Log.d(TAG, "No urls to display");
        }
        	
        	//Next grab RSS feeds
        	final HashMap<String, String> rssurls = playlist.getRSSUrls(stations[position]);
        	Set<String> rset = null;
        	if (rssurls != null){
        		rset = rssurls.keySet();
        	}
        	 
            if (rset != null){
            	Iterator<String> riterator = rset.iterator();
            	while (riterator.hasNext()) { 
            		View audiourl = null;
            		
            		//We have an rss url
           			audiourl = (LinearLayout) View.inflate(context, com.webeclubbin.mynpr.R.layout.rssaudiolink, null);
           			TextView t = (TextView) audiourl.findViewById( com.webeclubbin.mynpr.R.id.audiourl );
               		t.setText( riterator.next() );
               		ImageView iv = (ImageView) audiourl.findViewById( com.webeclubbin.mynpr.R.id.rss );
               		iv.setVisibility(ImageView.VISIBLE);
	
            		audiourl.setOnClickListener(new View.OnClickListener() {
                        public void onClick( View v ) {
                        	String TAG = "Playlist List Click";
                            
                            LinearLayout ll = (LinearLayout) v;
                            TextView t = (TextView) ll.findViewById( com.webeclubbin.mynpr.R.id.audiourl );
                            String showtitle = (String) t.getText();
                            
                            Log.d(TAG, "Grab rss url");
                            final String rssurl = rssurls.get(showtitle);
                            
                            Log.d(TAG, "Wait dialog");
                            waitdialog = new ProgressDialog(context);
                            waitdialog.setIndeterminate(true);
                            waitdialog.setCancelable(true); 
                            waitdialog.setTitle("One Moment...");
                            waitdialog.show();
                            
                            Log.d(TAG, "RSS Menu: " + showtitle);
                            Runnable r = new Runnable() {   
                    			public void run() {   
                    				processPodcastList(rssurl, stations[position]);
                    			}   
                    		};   
                    		new Thread(r).start();
                            
                        }
                    });

            		row.addView(audiourl);
            	}
            } else {
            	Log.d(TAG, "No RSS urls to display");
            }
        	
        	ImageView logo = (ImageView)row.findViewById(com.webeclubbin.mynpr.R.id.thumbnail);
            //GRAB Station logo
            Bitmap b = im.getImageBitmap( playlist.getLogo( stations[position] ) );
            if (b != null){
                logo.setImageBitmap( b );
            } else {
            	logo.setImageBitmap( null );
            	
            	Log.d(TAG, "set name instead of image"); 
            	TextView label=(TextView)row.findViewById(com.webeclubbin.mynpr.R.id.stationname);
                label.setText(stations[position]);
                label.setVisibility(View.VISIBLE);
            	 
            }
        

        return(row); 
    }
	
    /** Parse Podcast XML */
    private Podcast[] grabPodcastXML(String strURL, String station) {
    	URL url;
    	URLConnection urlConn = null;
    	final String TAG = "grabdata - podcast";
    	SAXParser saxParser;
    	long saxelapsedTimeMillis;
    	long saxstart;

    	SAXParserFactory factory = SAXParserFactory.newInstance();
    	MyContentHandlePodcastSAX myHandler = new MyContentHandlePodcastSAX();
    	myHandler.setStation(station);
    	try {
    		url = new URL(strURL);
    		urlConn = url.openConnection();
    		Log.d( TAG, "Open connection for data" + strURL);
    	} catch (IOException ioe) {
    		Log.e( TAG, "Could not connect to " +  strURL);
    		return null;
    	}
    	saxstart = System.currentTimeMillis();

    	//TODO Do error checking!!!!
    	
    	//Parse xml
    	try {
    		saxParser =  factory.newSAXParser();
    		Log.d( TAG, "Before: Parser - SAX");
    		InputStream is = urlConn.getInputStream();
    		saxParser.parse( is, myHandler);
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

    	return myHandler.getPodcasts();
    }
    
    private void processPodcastList(String rssurl, String station){
    	Log.d(TAG, "Parse rss url");
        //TODO This may take too long. We may need to put in a "please wait" dialog
        pods = grabPodcastXML(rssurl, station);
        Log.d(TAG, "Number of podcast(s): " + pods.length);

    	Log.d(TAG, "Show Dialog" );
    	handler.sendEmptyMessage(0);	
    }
    
    //Handle out of Main thread messages
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {      
        		
        		dialog = new Dialog(context);
            	dialog.setContentView(com.webeclubbin.mynpr.R.layout.podcastselect);
            	dialog.setTitle("Select Show to Play");
        	    	
        	   	//Set up list from podcast
        	   	ListView lv = (ListView) dialog.findViewById(com.webeclubbin.mynpr.R.id.podcastlist);
        	    	                  
        	   	lv.setAdapter(new PodcastAdapter(context,
        	    		com.webeclubbin.mynpr.R.layout.podcast ,
        	            pods, dialog));
        	   	
        	   	
        	    	/* lv.setOnItemClickListener(new OnItemClickListener() {
        	            public void onItemClick(AdapterView parent, View v, int position, long id) {
        	            	onItemClickHelper(v, position, act, dialog , s.getName(), s.getLogo() );
        	            }
        	        }); */
        	    	
        	   	Log.d(TAG, "Show dialog");
        	   	dialog.show();
        	   	waitdialog.dismiss();
        		
        		
        }
    };
	
}

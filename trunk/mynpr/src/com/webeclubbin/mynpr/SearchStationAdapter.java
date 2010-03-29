package com.webeclubbin.mynpr;

import java.util.HashMap;
import java.util.Set;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class SearchStationAdapter extends ArrayAdapter<Station> {

	String TAG = "SearchStationAdapter";
	int ourlayoutview ;
	ImageHelper im = null;

	SearchStationAdapter(Activity context, int ourview, Station[] stations) {
		super(context, ourview, stations); 
        ourlayoutview = ourview; 
        im = new ImageHelper(context);
        //TODO what happens when we get no results?
        Log.d(TAG, "Create SearchStationAdapter");
    } 

	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {  
		Log.d(TAG, "Start getView");
        final View row=View.inflate( getContext() , ourlayoutview , null); 
        final Station s =  this.getItem(position);   
        
        final ImageView logo = (ImageView)row.findViewById(com.webeclubbin.mynpr.R.id.stationlogo);
        
        Log.d(TAG, "set logo");
        if ( s.getLogo() != null ) {  
            
            Log.d(TAG, "station " + Integer.toString(position)  + " image " + s.getLogo() );
            Bitmap b = im.getImageBitmap( s.getLogo() );
            if ( b != null ){
            	logo.setImageBitmap( b );
            } else {
            	logo.setImageBitmap( null );
            	Log.d(TAG, "set name"); 
            	TextView label=(TextView)row.findViewById(com.webeclubbin.mynpr.R.id.stationname);
                label.setText(s.getName());
                label.setVisibility(View.VISIBLE);
            }
            
        }  else {
        	logo.setImageBitmap( null );
        	Log.d(TAG, "set name"); 
        	TextView label=(TextView)row.findViewById(com.webeclubbin.mynpr.R.id.stationname);
            label.setText(s.getName());
            label.setVisibility(View.VISIBLE);
        }
        
        ImageView stream = (ImageView)row.findViewById(com.webeclubbin.mynpr.R.id.pulsestream);
        ImageView mp3 = (ImageView)row.findViewById(com.webeclubbin.mynpr.R.id.pulsemp3);
        ImageView podcast = (ImageView)row.findViewById(com.webeclubbin.mynpr.R.id.pulsepodcast);

        final String[] aurls = s.getAUrl();
        final String[] murls = s.getMUrl();
        final HashMap<String, String> purls = s.getPUrl();
        
        final Activity act = (Activity) getContext();
        
        //TODO Grab URL titles
        
        stream.setOnClickListener(new View.OnClickListener() {
            public void onClick( View v ) {
                String TAG = "Pulse Stream Click: " + s.getName() ;

                // If URL. list is 0 skip function
                if (aurls.length != 0) {
                	final Dialog dialog = new Dialog(act);

                	dialog.setContentView(com.webeclubbin.mynpr.R.layout.urlpopup);
                	dialog.setTitle("Select Audio Stream");

                	//Set up Logo
                	ImageView image = (ImageView) dialog.findViewById(com.webeclubbin.mynpr.R.id.slogo);
                	image.setImageDrawable(logo.getDrawable());
                
                	//Set up URL list
                	ListView lv = (ListView) dialog.findViewById(com.webeclubbin.mynpr.R.id.urllist);
                	Log.d(TAG, "String array: " + String.valueOf(  aurls.length));
                
                	lv.setAdapter(new ArrayAdapter<String>(act,
                		com.webeclubbin.mynpr.R.layout.urllist ,
                        aurls));
                	lv.setOnItemClickListener(new OnItemClickListener() {
                        public void onItemClick(AdapterView parent, View v, int position, long id) {
                        	onItemClickHelper(v, position, act, dialog , s.getName(), s.getLogo() );
                        }
                    });
                
                	Log.d(TAG, "Show Dialog" );
                	dialog.show();
                }
            }
        });
        mp3.setOnClickListener(new View.OnClickListener() {
            public void onClick( View v ) {
                final String TAG = "Live Stream Click: " + s.getName() ;
                
                // If URL. list is 0 skip function
                if (murls.length != 0) {
                	final Dialog dialog = new Dialog(act);

                	dialog.setContentView(com.webeclubbin.mynpr.R.layout.urlpopup);
                	dialog.setTitle("Select Live MP3 Stream Link");

                	//Set up Logo
                	ImageView image = (ImageView) dialog.findViewById(com.webeclubbin.mynpr.R.id.slogo);
                	image.setImageDrawable(logo.getDrawable());
                
                	//Set up URL list
                	ListView lv = (ListView) dialog.findViewById(com.webeclubbin.mynpr.R.id.urllist);
                	Log.d(TAG, "String array: " + String.valueOf(  murls.length));
                
                	lv.setAdapter(new ArrayAdapter<String>(act,
                		com.webeclubbin.mynpr.R.layout.urllist ,
                        murls));
                	
                	lv.setOnItemClickListener(new OnItemClickListener() {
                        public void onItemClick(AdapterView parent, View v, int position, long id) {
                        	onItemClickHelper(v, position, act, dialog , s.getName(), s.getLogo() );
                        }
                    });
                
                	Log.d(TAG, "Show Dialog" ); 
                	dialog.show();
                }
            }
        });
        podcast.setOnClickListener(new View.OnClickListener() {
            public void onClick( View v ) {
                String TAG = "Podcast Click: " + s.getName() ;

                // If URL. list is 0 skip function
                Set<String> u = purls.keySet();
                String[] t = {""} ;
                if (u.size() != 0) {
                	final Dialog dialog = new Dialog(act);

                	dialog.setContentView(com.webeclubbin.mynpr.R.layout.urlpopup);
                	dialog.setTitle("Select Podcast Show");

                	//Set up Logo
                	ImageView image = (ImageView) dialog.findViewById(com.webeclubbin.mynpr.R.id.slogo);
                	image.setImageDrawable(logo.getDrawable());
                
                	//Set up URL list
                	ListView lv = (ListView) dialog.findViewById(com.webeclubbin.mynpr.R.id.urllist);
                	Log.d(TAG, "String array: " + String.valueOf(  u.size()) + " " + u.toArray(t)[0]);
                
                	lv.setAdapter(new ArrayAdapter<String>(act,
                		com.webeclubbin.mynpr.R.layout.urllist ,
                		u.toArray(t)));

                	lv.setOnItemClickListener(new OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        	final String TAG = "onItemClick";
                			Log.d(TAG, "Podcast regular click" );
                        	TextView t = (TextView)v;
                        	String title = t.getText().toString();
                        	//Grab out url of podcast
                        	String urladdr = purls.get( title) ;
                        	
                        	PlayURL pu = new PlayURL();
                        	pu.setLogo(s.getLogo());
                        	pu.setRSS(true);
                        	pu.setStation(s.getName());
                        	pu.setTitle(title);
                        	pu.setURL(urladdr);
                        	SearchStationTab.launchhelper(pu, act, dialog ); 
                        }
                    }); 

                	
                	/*
                	lv.setOnItemLongClickListener(new OnItemLongClickListener() {
                		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
                			final String TAG = "onItemLongClick";
                			Log.d(TAG, "Podcast longclick" );
                			ClipboardManager cm = (ClipboardManager) act.getSystemService(Context.CLIPBOARD_SERVICE);
                			TextView t = (TextView)view;
                        	String title = t.getText().toString();
                        	//Grab out url of podcast
                        	String urladdr = purls.get( title) ;
                        	cm.setText( urladdr );
                        	Toast.makeText(act, "Copied link to clipboard.", Toast.LENGTH_SHORT).show();
                        	Toast.makeText(act, "Paste in your favorite RSS reader.", Toast.LENGTH_SHORT).show();
                        	//onItemClickHelper(v, position, act, dialog , s.getName(), s.getLogo() );
                			return true;
                		}
                	});
                */
                	Log.d(TAG, "Show Dialog" );
                	dialog.show();
                }
            }
        });
        
        //Set up Text for number of links
        TextView streamnum = (TextView)row.findViewById(com.webeclubbin.mynpr.R.id.streamnumber);
        TextView mp3num = (TextView)row.findViewById(com.webeclubbin.mynpr.R.id.mp3text);
        TextView podnum = (TextView)row.findViewById(com.webeclubbin.mynpr.R.id.streampodcasttext);
        streamnum.setText( String.valueOf( aurls.length ) );
        mp3num.setText( String.valueOf( murls.length ) );
        podnum.setText( String.valueOf( purls.size() ) );

        return(row); 
    }
	
	//Setup Click listeners and dialogs if we need to.
	public void onItemClickHelper(View v, int position, Activity a, final Dialog d, String stationname, String logo) {
        Uri uri = null;
        Intent i = null;
        String TAG = "SearchStationApdapter - onItemClickHelper";
        
        //Open Selected media URL
        TextView t = (TextView)v;
        String url = t.getText().toString();
        if ( ! url.contains("http") ){
        	//Let us assume this is a podcast
        	
        }

    	String[] r = null;
    	if ( url.toLowerCase().endsWith(SearchStationTab.PLS)) {
    		r = SearchStationTab.parsePLS( url , a );
    	} else if ( url.toLowerCase().endsWith(SearchStationTab.M3U)) {
    		r = SearchStationTab.parseM3U( url, a );
    	} else {
    		r = new String[] {url};
    	}
    	
    	//Check data we get back
    	if ( r == null ) {
    		//Create toast telling user we have nothing.
    		Log.d(TAG, "No data returned" );
    		Toast.makeText(a, "No Audio Found inside Station's Playlist", Toast.LENGTH_LONG).show();
    	} else if (r.length == 1) {
            //launch intent
            Log.d(TAG, "Position:" + position + " url " + t.getText().toString() + " Got this url out of it " + r[0]  );
            SearchStationTab.launchhelper(r, a, d, stationname, logo);
            
    	} else {
    		//Let users select which link they want to play
    		Log.d(TAG, "Multiple selections for audio" );
    		SearchStationTab.launchhelper(r, a, d, stationname, logo);
    	}
	}


}

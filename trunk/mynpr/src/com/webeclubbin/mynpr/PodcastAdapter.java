package com.webeclubbin.mynpr;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class PodcastAdapter extends ArrayAdapter<Podcast> {

	String TAG = "PodcastAdapter";
	int ourlayoutview ;
	Podcast[] podcast = null;
	Activity parentContext = null;
	Dialog dialog = null;

	PodcastAdapter(Activity context, int ourview, Podcast[] p, Dialog d) {
		super(context, ourview, p); 
		Log.d(TAG, "Setup");
		ourlayoutview = ourview; 
        podcast = p;
        parentContext = context;
        dialog = d;
        
        Log.d(TAG,"Number of rows: " + p.length);

        //TODO what happens when we get no results?
        
    } 

	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {  
		Log.d(TAG, "Start getView");
		ViewHolder holder;
		
		//final View row; 
        final Podcast pod =  this.getItem(position);  
		if (convertView == null) {
			convertView=View.inflate( getContext() , ourlayoutview , null); 
			
			holder = new ViewHolder();
			holder.title = (TextView)convertView.findViewById(com.webeclubbin.mynpr.R.id.title); 

			convertView.setTag(holder);
		} else {
			Log.d(TAG, "Use old view object");
			holder = (ViewHolder) convertView.getTag();
		}
         
        
        Log.d(TAG, "set title");
        holder.title.setText(pod.getTitle() + " - (" + pod.getPubDate() + ")");
        
        if (checkPlayedStatus(pod)){
        	//we have played this file before, set different status
        	Log.d(TAG, "set played status to true");
        	holder.title.setCompoundDrawablesWithIntrinsicBounds (  com.webeclubbin.mynpr.R.drawable.processing2 , 0,0,0);
        } else {
        	Log.d(TAG, "set played status to false");
        	holder.title.setCompoundDrawablesWithIntrinsicBounds (  com.webeclubbin.mynpr.R.drawable.processing2light , 0,0,0);
        }

        return(convertView); 
    }
	
	//Helper class to speed up getView()
	static class ViewHolder {
        TextView title;
    }
	
	//setup podcast status file
	public static void setPlayedStatus(Podcast p, Context c){
		final String PREFIX = "PODCAST";
		final String TAG = "setPlayedStatus";
		
		File f = c.getDir("pods", Context.MODE_PRIVATE);
		String podcastStatusFile =  f.getPath() + File.separator + PREFIX + String.valueOf( p.getAudioUrl().hashCode() ) + String.valueOf( p.getPubDate().hashCode() );
		Log.d(TAG, "Saving temp file: " + podcastStatusFile);
		File podstatus = new File (podcastStatusFile);
		if ( ! podstatus.exists() ){
			//file does not exist
			try {
				podstatus.createNewFile();
			} catch (IOException e) {
				Log.e(TAG, "Problem saving temp file: " + podcastStatusFile);
				Log.e(TAG, e.toString() );
			}
		} 
	}
	//Check podcast status file
	private boolean checkPlayedStatus(Podcast p){
		final String PREFIX = "PODCAST";
		final String TAG = "CheckPlayedStatus";
		boolean status = false;
		
		File f = parentContext.getDir("pods", Context.MODE_PRIVATE);
		
		Log.d(TAG, "url " + p.getAudioUrl() + " pubdate " + p.getPubDate());

		if (p != null){
			String podcastStatusFile =  f.getPath() + File.separator + PREFIX + String.valueOf( p.getAudioUrl().hashCode() ) + String.valueOf( p.getPubDate().hashCode() );
			Log.d(TAG, " pod status file: " + podcastStatusFile);
			File podstatus = new File (podcastStatusFile);
		
			if ( podstatus.exists() ){
				status = true;
			} 
		} else {
			status = false;	
		}
		Log.d(TAG, String.valueOf( status ));
		return status;
	}
	
	//Setup Click listeners and dialogs if we need to.
	public void onItemClickHelper(View v, int position, Activity a, final Dialog d, String stationname, String logo) {

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

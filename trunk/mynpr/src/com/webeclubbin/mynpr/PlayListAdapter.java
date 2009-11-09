package com.webeclubbin.mynpr;



import java.util.Comparator;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class PlayListAdapter extends ArrayAdapter<String> {

	Activity context;  
	int ourlayoutview;
	ImageHelper im = null;
	PlayList playlist = null;
	String [] stations = null;
	String TAG = "PlayListAdapter";
	  
	PlayListAdapter(Activity context, int ourview, PlayList p, ImageHelper imagehelper) {
		super(context, ourview, p.getStations()); 
		
		Log.i(TAG,"Creation");

        ourlayoutview = ourview;
        this.context=context;  
        
        stations = p.getStations();
        playlist = p;
        
        if (imagehelper != null){
        	Log.i(TAG,"Store imagehelper received");
        	this.im = imagehelper;
        } else {
        	im = new ImageHelper(context);
        }
    }  

	@Override
    public View getView(final int position, View convertView, ViewGroup parent) {  
         
    	Log.i(TAG,"getView " + stations[position] );
    	LinearLayout  row = (LinearLayout) View.inflate(context, ourlayoutview, null);
 
        String [] urls = playlist.getUrls(stations[position]);
        if (urls != null){
        	for (int i = 0; i < urls.length; i++) { 
        		TextView audiourl = (TextView) View.inflate(context, com.webeclubbin.mynpr.R.layout.audiolink, null);
        		audiourl.setText(urls[i]);
        		
        		audiourl.setOnClickListener(new View.OnClickListener() {
                    public void onClick( View v ) {
                    	String TAG = "Playlist List Click";
                        Log.i(TAG, "Grab url and play");
                        TextView t = (TextView) v;
                        String oururl = (String) t.getText();
                        
                        PlayListTab p = (PlayListTab) context; 
                        p.play(stations[position], oururl);

                    }
                });
        		
        		row.addView(audiourl);
        	}
        	
        	ImageView logo = (ImageView)row.findViewById(com.webeclubbin.mynpr.R.id.thumbnail);
            //GRAB Station logo
            Bitmap b = im.getImageBitmap( playlist.getLogo( stations[position] ) );
            if (b != null){
                logo.setImageBitmap( b );
            } else {
            	logo.setImageBitmap( null );
            	/*
            	 * Log.i(TAG, "set name"); 
            	TextView label=(TextView)row.findViewById(com.webeclubbin.mynpr.R.id.stationname);
                label.setText(s.getName());
                label.setVisibility(View.VISIBLE);
            	 */
            }
        } else {
        	row = new LinearLayout(context);
        	Log.i(TAG, "Using blank view since we have nothing to display");
        }

        return(row); 
    }
	
}

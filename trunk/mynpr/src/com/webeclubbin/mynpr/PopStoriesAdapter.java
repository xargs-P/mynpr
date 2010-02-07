package com.webeclubbin.mynpr;



import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PopStoriesAdapter extends ArrayAdapter<String> {

	Activity context;  
	String[] title, image = null;
	int ourlayoutview;
	ImageHelper im = new ImageHelper(context);
	String TAG = "PopStoriesAdapter";
	  
	PopStoriesAdapter(Activity context, int ourview, String[] t, String[] i, ImageHelper imagehelper) {
		super(context, ourview, t); 
		
		Log.d(TAG,"Creation");
        title = t.clone();
        image = i.clone();
        ourlayoutview = ourview;
        this.context=context;  
        
        Log.d(TAG,"Number of rows: " + t.length);
        
        if (imagehelper != null){
        	Log.d(TAG,"Store imagehelper we received");
        	this.im = imagehelper;
        } else {
        	im = new ImageHelper(context);
        }
        
        Runnable r = new Runnable() {   
	        public void run() {   
	        	im.setImageStorage(image);
	        }   
	    };   
	    new Thread(r).start(); 
    }  

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {  
         
    	Log.d(TAG,"getView");
        View row=View.inflate(context, ourlayoutview, null);  
        TextView label=(TextView)row.findViewById(com.webeclubbin.mynpr.R.id.popstoryrowlabel);  

        label.setText(title[position]);  
        
        ImageView icon = (ImageView)row.findViewById(com.webeclubbin.mynpr.R.id.thumbnail);
        if ( (image[position] != null) &&  ( ! image[position].equals(" ") ) && ( ! image[position].equals("") ) ) {  
            
            Log.d(TAG, "image " + Integer.toString(position) + " " + image[position] );
            Bitmap b = im.getImageBitmap( image[position] );
            icon.setImageBitmap( b );
        }  else {
        	icon.setImageBitmap( null );
        }

        return(row); 
    }
	
}

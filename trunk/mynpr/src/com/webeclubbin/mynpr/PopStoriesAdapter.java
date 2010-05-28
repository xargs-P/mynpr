package com.webeclubbin.mynpr;



import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class PopStoriesAdapter extends ArrayAdapter<String> {

	Activity context;  
	String[] title, image = null;
	int ourlayoutview;
	ImageHelper im = new ImageHelper(context);
	Bitmap defaultimage = null;
	String TAG = "PopStoriesAdapter";
	
	  
	PopStoriesAdapter(Activity context, int ourview, String[] t, String[] i) {
		super(context, ourview, t); 
		
		Log.d(TAG,"Creation");
        title = t.clone();
        image = i.clone();
        ourlayoutview = ourview;
        this.context=context;  
        
        Log.d(TAG,"Number of rows: " + t.length);
        
        im = new ImageHelper(context);
        
        defaultimage = BitmapFactory.decodeResource(context.getResources(),  com.webeclubbin.mynpr.R.drawable.processing2light );
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
    	ViewHolder holder;
    	
		if (convertView == null) {
			convertView=View.inflate(context, ourlayoutview, null);  
			
			holder = new ViewHolder();
			holder.label = (LazyTextView)convertView.findViewById(com.webeclubbin.mynpr.R.id.popstoryrowlabel); 
			
			convertView.setTag(holder);
		} else {
			Log.d(TAG, "Use old view object");
			holder = (ViewHolder) convertView.getTag();
		}
    	
        holder.label.setText(title[position]);  
        
        if ( (image[position] != null) &&  ( ! image[position].equals(" ") ) && ( ! image[position].equals("") ) ) {  
            
            Log.d(TAG, "image " + Integer.toString(position) + " " + image[position] );
            //Bitmap b = im.getImageBitmap( image[position] );
            
            if ( im.isAvailable(image[position]) ) {
            	Log.d(TAG, "Set image");
            	holder.label.setImageUrl( image[position] );
            } else {
            	Log.d(TAG, "Set default then get image");
            	//set default
            	holder.label.setCompoundDrawablesWithIntrinsicBounds(null, new BitmapDrawable ( defaultimage ), null, null);
                //Next download image
                holder.label.setImageUrl( image[position] );
            }
            
        }  else {
        	holder.label.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }

        return(convertView); 
    }
	
	//Helper class to speed up getView()
	static class ViewHolder {
        LazyTextView label;
    }
}

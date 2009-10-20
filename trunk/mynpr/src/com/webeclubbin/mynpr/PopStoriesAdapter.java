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
		
		Log.i(TAG,"Creation");
        title = t.clone();
        image = i.clone();
        ourlayoutview = ourview;
        this.context=context;  
        
        if (imagehelper != null){
        	Log.i(TAG,"Store imagehelper we received");
        	this.im = imagehelper;
        }
    }  

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {  
         
    	Log.i(TAG,"getView");
        View row=View.inflate(context, ourlayoutview, null);  
        TextView label=(TextView)row.findViewById(com.webeclubbin.mynpr.R.id.popstoryrowlabel);  

        label.setText(title[position]);  
        
        ImageView icon = (ImageView)row.findViewById(com.webeclubbin.mynpr.R.id.thumbnail);
        if ( (image[position] != null) &&  ( ! image[position].equals(" ") ) && ( ! image[position].equals("") ) ) {  
            
            Log.i(TAG, "image " + Integer.toString(position) + " " + image[position] );
            Bitmap b = im.getImageBitmap( image[position] );
            //Bitmap b = com.webeclubbin.mynpr.MyNPR.getImageBitmap( image[position] );
            icon.setImageBitmap( b );
        }  else {
        	icon.setImageBitmap( null );
        }

        return(row); 
    }
	
	/*public ImageHelper getImageHelper(){
		return im;
	}*/
	
}

package com.webeclubbin.mynpr;
//Load the image from cache(memory or local disk) and if we need to download
//the image we should send out a default image until we grab the image.

//Examples used: 
// http://evancharlton.com/thoughts/lazy-loading-images-in-a-listview/
// http://blog.jteam.nl/2009/09/17/exploring-the-world-of-android-part-2/

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.TextView;

public class LazyTextView extends TextView {
	
	String pathtofile = null;
	String weblocation = null;
	Bitmap theimage = null;
	ImageHelper im = new ImageHelper ( (Activity) getContext() );

	public LazyTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public LazyTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	public LazyTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	//Grab image in background
	public void setImageUrl (String url) {
		weblocation = url;

		//Start up thread to go get image
		new Thread() {
			@Override
			public void run() {
				theimage = im.getImageBitmap(weblocation);
				handler.sendEmptyMessage(0);
			}
		}.start();
	}
		
	//Update image on screen
	private void refreshimage() {
        if (theimage != null) {
        	setCompoundDrawablesWithIntrinsicBounds(null, new BitmapDrawable (  theimage ), null, null);
        } else {
        	//get rid of default image
        	setCompoundDrawablesWithIntrinsicBounds(null, null , null, null);
        }
    }
	
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            refreshimage();
        }
    };
       
}

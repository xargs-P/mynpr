package com.webeclubbin.mynpr;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class ImageHelper {

	private HashMap<String, Bitmap> imagestorage = new HashMap<String, Bitmap>();
	private final String PREFIX = "IMAGE";
	Activity context = null;
	private final String TAG = "ImageHelper";
	    
	ImageHelper(Activity c) {
		context = c;
        Log.i(TAG, "Constructor ImageHelper");
    } 
    //Grab image from URL and create bitmap from it
    public Bitmap getImageBitmap(String url) { 
        Bitmap bm = null; 
        String TAG = "getImageBitmap";
        processImage(url); 
        bm = imagestorage.get(url);
        Log.i(TAG, "Return image");
        //Return bitmap
        return bm; 
    } 
    
    //Grab image from URL and save bitmap for it
    public void setImageStorage(String[] url) { 
        String TAG = "setImageStorage";
        if ( url != null ) {
        	Log.i(TAG, "Number of image urls: " + String.valueOf(url.length));
        	Log.i(TAG, "Cycle through");
        	for (int i=0; i < url.length; i++){
        		if ( (url[i] == null) ||  ( url[i].equals(" ") ) || ( url[i].equals("") ) ) {  
        			continue;
        		}
        		processImage(url[i]); 
        	}
        }

    } 
    
    private void processImage (String url) {
    	//TODO Save image as it's own format. Don't change it.
    	Bitmap bm = null; 
    	String TAG = "processImage";
    	if (url == null){
    		return;
    	}
    	bm = imagestorage.get(url);
    	File f = context.getCacheDir();
    	String [] s = Pattern.compile("?", Pattern.LITERAL).split(url);
    	String [] s1 = {""}; 
    	// suffix = "";
    	s1 = Pattern.compile(".", Pattern.LITERAL).split(s[0]);
    	String suffix = s1[s1.length - 1];
    	
    	String imagefile = f.getPath() + File.separator + PREFIX + String.valueOf( url.hashCode() ) + "." + suffix;
    	
    	if (bm == null) {
    		Log.i(TAG, "See if we can grab image from disk cache. Not in memory: " + url);
    		
    		//See if we have the file saved in a cache file

    		File image = new File (imagefile);
    		if ( image.exists() ){
    			Log.i(TAG, "Found image in cache dir " + imagefile );
    			bm = BitmapFactory.decodeFile(imagefile);
    			
    			if (bm == null) {
    				Log.i(TAG, "We could not read file. We need to redownload");
    				Log.i(TAG, "Delete local file");
    				image.delete();
    			} else {
    				Log.i(TAG, "Storing image in memory" );
					imagestorage.put(url, bm);
    			}

    		} else {
    			Log.i(TAG, "Did NOT find image in cache dir " + imagefile);
    		}
    		
    		//Just in case we did not get the image from cache
    		if (bm == null) {
    			try { 
    				URL aURL = new URL(url); 
    				URLConnection conn = aURL.openConnection();
    				conn.setConnectTimeout(1000 * 5);
    				conn.setReadTimeout(1000 * 5);
    				//conn.connect();
    				Log.i(TAG, "grabbing " + url );
    				InputStream is = conn.getInputStream(); 
    				//BufferedInputStream bis = new BufferedInputStream(is, 8192);
    				//BufferedInputStream bis = new BufferedInputStream(is);

    				FileOutputStream fos = new FileOutputStream(imagefile);
    				//BufferedOutputStream bout = new BufferedOutputStream ( fos ) ;
    				
    				
    				int b;
    				while((b=is.read())!=-1) 
    				{ 
    					fos.write(b); 
    				} 
    				Log.i(TAG," writing done"); 
    				is.close(); 
    				fos.close(); 
    				Log.i(TAG," Create bitmap"); 
    				bm = BitmapFactory.decodeFile (imagefile); 
    				//bis.close(); 
    				//is.close(); 
    				
    				if (bm != null) {
    					Log.i(TAG, "Storing image in memory" );
    					imagestorage.put(url, bm);
    				} else {
    					Log.i( TAG, "Unable to download image " + url );
    				}
        
    			} catch (IOException e) { 
    				Log.e(TAG, "Error getting bitmap: " + url, e); 
    			} 
    		
    			/*if (bm != null) {
    				//Save to file
    				try {
    					FileOutputStream outToFile = new FileOutputStream( imagefile );
    					bm.compress (Bitmap.CompressFormat.PNG, 100, outToFile);
    					outToFile.flush(); 
    					outToFile.close(); 

    					Log.i( TAG, "Saved Image to file: " + imagefile);
    				} catch (FileNotFoundException	e) {
    					Log.e( TAG, e.toString() );
    				} catch (IOException ioe) {
    					Log.e(TAG, "Unable to save file locally " + ioe.getMessage() );
    				}
    			} else {
    				Log.i( TAG, "Skip saving file since we did not download it" );
    			}*/
    		}
    	} else {
    		Log.i(TAG, "found image in storage memory");
    	}
    	
    }
    
    //Resize image to certain height and width
    public Drawable resizeImage(Context ctx, int resId, int w, int h) {

  	  // load the original Bitmap
  	  Bitmap BitmapOrg = BitmapFactory.decodeResource(ctx.getResources(),
  	                                                  resId);
  	  String TAG = "resizeImage";
  	  int width = BitmapOrg.getWidth();
  	  int height = BitmapOrg.getHeight();
  	  //int newWidth = w;
  	  //int newHeight = h;

  	  // calculate the scale
  	  //TODO**** What happens when the screen is REALLY big?
        double ratio = 0.3 ;
        float scaleWidth = (float) ratio ; 
        float scaleHeight = (float) ratio ;
        //float scaleWidth = (float) (w * ratio) ; 
        //float scaleHeight = (float) (h * ratio) ; 
        Log.i(TAG, "resId=" + resId + " scaleWidth=" + scaleWidth + " scaleHeight=" + scaleHeight) ;
        Log.i(TAG, "resId=" + resId + " Width=" + width + " Height=" + height) ;
  	  //float scaleWidth = ((float) newWidth) / width;
  	  //float scaleHeight = ((float) newHeight) / height;

  	  // create a matrix for the manipulation
  	  Matrix matrix = new Matrix();
  	  // resize the Bitmap
  	  matrix.preScale(scaleWidth, scaleHeight);
  	  //matrix.postScale(scaleWidth, scaleHeight);
  	  // if you want to rotate the Bitmap
  	  // matrix.postRotate(45);

  	  // recreate the new Bitmap
  	  Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0,
  			  width, height, matrix, true);

  	  // make a Drawable from Bitmap to allow to set the Bitmap
  	  // to the ImageView, ImageButton or what ever
  	  return new BitmapDrawable(resizedBitmap);

  	}

    public String[] getUrls() {
    	Set<String> s = imagestorage.keySet();
    	String temp[] = {""};
    	return s.toArray(temp);
    }
}

package com.webeclubbin.mynpr;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
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

	//Using a softrefence here to allow the garage collect to clean this up if it needs to. 
	//We should have the image saved on disk so we can get it from there if we need to.
	//If that doesn't work, we can just redownload.
	private HashMap<String, SoftReference<Bitmap>> imagestorage = new HashMap<String, SoftReference<Bitmap>>();
	private final String PREFIX = "IMAGE";
	Activity context = null;
	private final String TAG = "ImageHelper";
	//TODO lazytextview may want to know if so we won't double download. But we need to code that logic first 
	private String currentUrlInProcess = null;
	private boolean processList = false; 
	    
	ImageHelper(Activity c) {
		context = c;
        Log.d(TAG, "Constructor ImageHelper");
    } 
    //Grab image from URL and create bitmap from it
    public Bitmap getImageBitmap(String url ) { 
        Bitmap bm = null; 
        String TAG = "getImageBitmap";
        processImage(url); 
        SoftReference<Bitmap> softBitmap = imagestorage.get(url);
        if ( softBitmap != null){
    		bm = softBitmap.get();
    	}
        Log.d(TAG, "Return image");
        //Return bitmap
        return bm; 
    } 
    
    //Is the image available?
    public boolean isAvailable(String url){
    	String filename = getFilename(url);
    	final String TAG = "isAvailable" ;
    	
    	boolean result = false;
    	if ( imagestorage.get(url) != null) {
    		result = true;
    	} else if (  new File (filename).exists() ){
    		result = true;
    	}
    	
    	Log.d(TAG, Boolean.toString(result) );
    	return result;
    }
    
    //Grab image from URL and create bitmap from it
    public String storeImage (String url ) { 
        String TAG = "storeImage";
        String s = processImage(url);
        return s;
    } 
    
    //Grab image from URL and save bitmap for it
    public void setImageStorage(String[] url) { 
        String TAG = "setImageStorage";
        processList = true;
        if ( url != null ) {
        	Log.d(TAG, "Number of image urls: " + String.valueOf(url.length));
        	Log.d(TAG, "Cycle through");
        	//for (int i=0; i < url.length; i++){
        	//Grab pictures from the bottom of the list first.
        	//We will assume the ListAdapter using this object will download from the top when the 
        	//LazyTextView downloads it on it's own
        	for (int i=url.length - 1; i >= 0; i--){
        		if ( (url[i] == null) ||  ( url[i].equals(" ") ) || ( url[i].equals("") ) ) {  
        			continue;
        		}
        		currentUrlInProcess = url[i];
        		processImage(url[i]); 
        		currentUrlInProcess = null;
        	}
        }
        processList = false;

    } 
    
    //Grab image from memory , disk or the image
    private String processImage (String url) {
    	//TODO Save image as it's own format. Don't change it.
    	Bitmap bm = null; 
    	String TAG = "processImage";
    	if (url == null){
    		return null;
    	}
    	SoftReference<Bitmap> softBitmap = imagestorage.get(url);
    	if ( softBitmap != null){
    		bm = softBitmap.get();
    	}
 
    	String imagefile = getFilename(url);
    	
    	if (bm == null) {
    		Log.d(TAG, "See if we can grab image from disk cache. Not in memory: " + url);
    		
    		//See if we have the file saved in a cache file

    		File image = new File (imagefile);
    		if ( image.exists() ){
    			Log.d(TAG, "Found image in cache dir " + imagefile );
    			bm = BitmapFactory.decodeFile(imagefile);
    			
    			if (bm == null) {
    				Log.d(TAG, "We could not read file. We need to redownload");
    				Log.d(TAG, "Delete local file");
    				image.delete();
    			} else {
    				Log.d(TAG, "Storing image in memory" );
					imagestorage.put(url,  new SoftReference<Bitmap>(bm) );
    			}

    		} else {
    			Log.d(TAG, "Did NOT find image in cache dir " + imagefile);
    		}
    		
    		//Just in case we did not get the image from cache
    		if (bm == null) {
    			try { 
    				URL aURL = new URL(url); 
    				URLConnection conn = aURL.openConnection();
    				conn.setConnectTimeout(1000 * 5);
    				conn.setReadTimeout(1000 * 5);
    				//conn.connect();
    				Log.d(TAG, "grabbing " + url );
    				InputStream is = conn.getInputStream(); 
    				BufferedInputStream bis = new BufferedInputStream(is, 8192);
    				FileOutputStream fos = new FileOutputStream(imagefile);
    				//BufferedOutputStream bout = new BufferedOutputStream ( fos ) ;
    				
    				
    				int b;
    				while((b=bis.read())!=-1) 
    				{ 
    					fos.write(b); 
    				} 
    				Log.d(TAG," writing done"); 
    				is.close(); 
    				fos.close(); 
    				Log.d(TAG," Create bitmap"); 
    				bm = BitmapFactory.decodeFile (imagefile); 
    				bis.close(); 
    				is.close(); 
    				
    				if (bm != null) {
    					Log.d(TAG, "Storing image in memory" );
    					imagestorage.put(url, new SoftReference<Bitmap>(bm) );
    				} else {
    					Log.d( TAG, "Unable to download image " + url );
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

    					Log.d( TAG, "Saved Image to file: " + imagefile);
    				} catch (FileNotFoundException	e) {
    					Log.e( TAG, e.toString() );
    				} catch (IOException ioe) {
    					Log.e(TAG, "Unable to save file locally " + ioe.getMessage() );
    				}
    			} else {
    				Log.d( TAG, "Skip saving file since we did not download it" );
    			}*/
    		}
    	} else {
    		Log.d(TAG, "found image in storage memory");
    	}
    	return imagefile;
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
        Log.d(TAG, "resId=" + resId + " scaleWidth=" + scaleWidth + " scaleHeight=" + scaleHeight) ;
        Log.d(TAG, "resId=" + resId + " Width=" + width + " Height=" + height) ;
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

    //Figure out filename to save image as
    private String getFilename (String url){
    	File f = context.getCacheDir();
    	String [] s = Pattern.compile("?", Pattern.LITERAL).split(url);
    	String [] s1 = {""}; 
    	// suffix = "";
    	s1 = Pattern.compile(".", Pattern.LITERAL).split(s[0]);
    	String suffix = s1[s1.length - 1];
    	
    	String imagefile = f.getPath() + File.separator + PREFIX + String.valueOf( url.hashCode() ) + "." + suffix;
    	
    	return imagefile;
    }
    
    public String[] getUrls() {
    	Set<String> s = imagestorage.keySet();
    	String temp[] = {""};
    	return s.toArray(temp);
    }
}

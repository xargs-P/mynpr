package com.webeclubbin.mynpr;
//package com.pocketjourney.media;
//Code taken from below URL. 
//http://blog.pocketjourney.com/2008/04/04/tutorial-custom-media-streaming-for-androids-mediaplayer/
//Good looking out!

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * MediaPlayer does not yet support streaming from external URLs so this class provides a pseudo-streaming function
 * by downloading the content incrementally & playing as soon as we get enough audio in our temporary storage.
 */
public class StreamingMediaPlayer {

    private int INTIAL_KB_BUFFER ;
    private int BIT = 8 ;
    private int SECONDS = 30 ;

	private int totalKbRead = 0;
	
	private File downloadingMediaFile ; 
	private String DOWNFILE = "downloadingMediaFile";

	private Context context;
	private int counter = 0;
	private int playedcounter = 0;
	private Vector<MediaPlayer> mediaplayers = new Vector<MediaPlayer>(3);
	private boolean started = false; 

 	public StreamingMediaPlayer(Context c) 
 	{
 		context = c;
 		downloadingMediaFile = new File(context.getCacheDir(),DOWNFILE + counter);
	}
	
    /**  
     * Progressivly download the media to a temporary location and update the MediaPlayer as new content becomes available.
     */  
    public void startStreaming(final String mediaUrl, int bitrate) throws IOException {
    	
    	final String TAG = "startStreaming";
    	//Set up buffer size
    	//Assume XX kbps * XX seconds / 8 bits per byte
    	INTIAL_KB_BUFFER =  bitrate * SECONDS / BIT;
    	
		Runnable r = new Runnable() {   
	        public void run() {   
	            try {   
	        		downloadAudioIncrement(mediaUrl);
	            } catch (IOException e) {
	            	Log.e(TAG, "Unable to initialize the MediaPlayer for fileUrl=" + mediaUrl, e);
	            	return;
	            }   
	        }   
	    };   
	    new Thread(r).start();
    }
    
    /**  
     * Download the url stream to a temporary location and then call the setDataSource  
     * for that local file
     */  
    public void downloadAudioIncrement(String mediaUrl) throws IOException {
    	final String TAG = "downloadAudioIncrement";

    	URLConnection cn = new URL(mediaUrl).openConnection(); 
        cn.connect();   
        InputStream stream = cn.getInputStream();
        if (stream == null) {
        	Log.e(TAG, "Unable to create InputStream for mediaUrl: " + mediaUrl);
        }
        
		Log.i(TAG, "File name: " + downloadingMediaFile);
		BufferedOutputStream bout = new BufferedOutputStream ( new FileOutputStream(downloadingMediaFile), 32 * 1024 );   
        byte buf[] = new byte[16 * 1024];
        int totalBytesRead = 0, incrementalBytesRead = 0;
        boolean stop = false;
        do {
        	if (bout == null) {
        		counter++;
        		Log.i(TAG, "FileOutputStream is null, Create new one: " + DOWNFILE + counter);
        		downloadingMediaFile = new File(context.getCacheDir(),DOWNFILE + counter);
        		bout = new BufferedOutputStream ( new FileOutputStream(downloadingMediaFile) );	
        	}

        	int numread = stream.read(buf);  
        	
            if (numread <= 0) {  
                break;   
            	
            } else {
            	//Log.v(TAG, "write to file");
                bout.write(buf, 0, numread);

                totalBytesRead += numread;
                incrementalBytesRead += numread;
                totalKbRead = totalBytesRead/1000;
            }
            
            
            
            if ( totalKbRead >= INTIAL_KB_BUFFER && ! stop) {
            	Log.v(TAG, "Reached Buffer amount we want: " + "totalKbRead: " + totalKbRead + " INTIAL_KB_BUFFER: " + INTIAL_KB_BUFFER);
            	bout.flush();
            	bout.close();
            	            	
            	bout = null;
            	
            	setupplayer(downloadingMediaFile);
            	totalBytesRead = 0;

            }
            
        } while (true);   

       	stream.close();

    }  
    
    /**
     * Test whether we need to transfer buffered data to the MediaPlayer.
     * Interacting with MediaPlayer on non-main UI thread can causes crashes to so perform this using a Handler.
     */  
    private void  setupplayer(File partofaudio) {
    	final File f = partofaudio;
    	final String TAG = "setupplayer";
    	Log.i(TAG, "File " + f.getAbsolutePath());
	    Runnable r = new Runnable() {
	        public void run() {
	        	
	        	MediaPlayer mp = new MediaPlayer();
	        	try {
	        		
	        		MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener () {
	        			public void onCompletion(MediaPlayer mp){
	        				String TAG = "MediaPlayer.OnCompletionListener";
	        				/*Runnable r = new Runnable() {   
	        			        public void run() {
	        			        	String TAG = "MediaPlayer.OnCompletionListener";
	        			        	Log.i(TAG, "Current size of mediaplayer list: " + mediaplayers.size() );
	        			        	//Make sure we have the second mediaplayer ready to go before we start playing
	        			        	while (mediaplayers.size() <= 1){
	        			        		Log.v(TAG, "waiting for another mediaplayer");
	        			        	}
	        			        	//Get second media player
	        			        	MediaPlayer mp2 = mediaplayers.get(1);
	        			        	mp2.start();
	        			        }
	        				};
	        			    new Thread(r).start();*/
	        				Log.i(TAG, "Current size of mediaplayer list: " + mediaplayers.size() );
	        				while (mediaplayers.size() <= 1){
    			        		Log.v(TAG, "waiting for another mediaplayer");
    			        	}
	        				MediaPlayer mp2 = mediaplayers.get(1);
    			        	mp2.start();
    			        	Log.i(TAG, "Start new player");
    			        	
	        				mp.release();
	        				mediaplayers.remove(mp);
	        				removefile();
	        				
	        			}
	        		};
	        		
	        		FileInputStream ins = new FileInputStream( f );
	            	mp.setDataSource(ins.getFD());
	        		mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
	        		
	        		mp.setOnCompletionListener(listener);
	        		Log.i(TAG, "Prepare Media Player " + f);
	        		
	        		if ( ! started  ){
	        			mp.prepare();
	        		} else {
	        			//This will save us a few more seconds
	        			mp.prepareAsync();
	        		}
	        		
	        		mediaplayers.add(mp);
	        		if ( ! started  ){
		        		Log.i(TAG, "Start Media Player " + f);
		        		startMediaPlayer();
		        	}
	        	} catch  (IllegalStateException	e) {
	        		Log.e(TAG, e.toString());
	        	} catch  (IOException	e) {
	        		Log.e(TAG, e.toString());
	        	}
	        	
 	        }
	    };
	    new Thread(r).start();

    }
   
    //Removed file from cache
    private void removefile (){
    	String TAG = "removefile";
    	File temp = new File(context.getCacheDir(),DOWNFILE + playedcounter);
    	Log.i(TAG, temp.getAbsolutePath());
    	temp.delete();
    	playedcounter++;
    }
    
    //Start first audio clip
    private void startMediaPlayer() {
    	String TAG = "startMediaPlayer";
    	
    	//Grab out first media player
    	started = true;
    	MediaPlayer mp = mediaplayers.get(0);
    	Log.i(TAG,"Start Player");
    	mp.start();  
    	
    }

    
	public void moveFile(File	oldLocation, File	newLocation)
	throws IOException {

		if ( oldLocation.exists( )) {
			BufferedInputStream  reader = new BufferedInputStream( new FileInputStream(oldLocation) );
			//BufferedOutputStream  writer = new BufferedOutputStream( new FileOutputStream(newLocation, false));
			BufferedOutputStream  writer = new BufferedOutputStream( new FileOutputStream(newLocation));
            try {
            	
		        byte[]  buff = new byte[8192];
		        int numChars;
		        Log.v(getClass().getName(),"write old file on to new file");
		        //while ( (numChars = reader.read(  buff, 0, buff.length ) ) != -1) {
		        while ((numChars = reader.read(buff)) > 0){
		        	writer.write( buff, 0, numChars );
      		    }
            } catch( IOException ex ) {
				throw new IOException("IOException when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());
            } finally {
                try {
                    if ( reader != null ){
                    	Log.v(getClass().getName(),"close reader and writer");
                    	writer.close();
                        reader.close();
                    }
                } catch( IOException ex ){
				    Log.e(getClass().getName(),"Error closing files when transferring " + oldLocation.getPath() + " to " + newLocation.getPath() ); 
				}
            }
        } else {
			throw new IOException("Old location does not exist when transferring " + oldLocation.getPath() + " to " + newLocation.getPath() );
        }
	}
}


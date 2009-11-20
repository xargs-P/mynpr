package com.webeclubbin.mynpr;
//package com.pocketjourney.media;
//Code taken from below URL. 
//http://blog.pocketjourney.com/2008/04/04/tutorial-custom-media-streaming-for-androids-mediaplayer/
//Good looking out!

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * MediaPlayer does not yet support "Shoutcast"-like streaming from external URLs so this class provides a pseudo-streaming function
 * by downloading the content incrementally & playing as soon as we get enough audio in our temporary storage.
 */ 
public class StreamingMediaPlayer {

	final static public String AUDIO_MIME =  "audio/mpeg";
	final static public String BITERATE_HEADER =  "icy-br";
    private int INTIAL_KB_BUFFER ;
    private int BIT = 8 ;
    private int SECONDS = 30 ;

	private int totalKbRead = 0;
	
	private File downloadingMediaFile ; 
	private String DOWNFILE = "downloadingMediaFile";

	private Context context;
	private int counter = 0;
	private int playedcounter = 0;
	//TODO should convert to Stack object instead of Vector
	private Vector<MediaPlayer> mediaplayers = new Vector<MediaPlayer>(3);
	private boolean started = false; 
	private InputStream stream = null;

 	public StreamingMediaPlayer(Context c) 
 	{
 		context = c;
 		downloadingMediaFile = new File(context.getCacheDir(),DOWNFILE + counter);
 		downloadingMediaFile.deleteOnExit();
	}
	
    /**  
     * Progressivly download the media to a temporary location and update the MediaPlayer as new content becomes available.
     */  
    public void startStreaming(final String mediaUrl) throws IOException {
    	
    	final String TAG = "startStreaming";
    	
    	URL url;
        URLConnection urlConn = null;
        int bitrate = 56;
        PlayListTab a = (PlayListTab) context ;
        
        a.handler.sendEmptyMessage(PlayListTab.START);
        
    	try {
    		url = new URL(mediaUrl);
    		urlConn = (HttpURLConnection)url.openConnection();
    		urlConn.setReadTimeout(1000 * 5);
    		urlConn.setConnectTimeout(1000 * 5);
    		urlConn.connect();
    		String ctype = urlConn.getContentType ().toLowerCase() ;
    		//See if this is a type we can handle
    		Log.i(TAG, "Content Type: " + ctype );
    		if (ctype.contains(AUDIO_MIME)){
    			
    			String temp = urlConn.getHeaderField(BITERATE_HEADER);
    			Log.i(TAG, "Bitrate: " + temp );
    			if (temp != null){
    				bitrate = new Integer(temp).intValue();
    			}
    		} else {
    			Log.e(TAG, "Does not look like we can play this audio type: " + ctype);
    			Log.e(TAG, "Or we could not connect to audio");
    			Toast.makeText(context, "myNPR can not play this audio type: " + ctype , Toast.LENGTH_LONG).show();
    			stop();
    			return;
    		}
    	} catch (IOException ioe) {
    		Log.e( TAG, "Could not connect to " +  mediaUrl );
    		stop();
    		Toast.makeText(context, "Could not connect to " +  mediaUrl , Toast.LENGTH_LONG).show();
    		return;
    	} 
    	
    	//Set up buffer size
    	//Assume XX kbps * XX seconds / 8 bits per byte
    	INTIAL_KB_BUFFER =  bitrate * SECONDS / BIT;
    	
		Runnable r = new Runnable() {   
	        public void run() {   
	            try {   
	        		downloadAudioIncrement(mediaUrl);
	            } catch (IOException e) {
	            	Log.e(TAG, "Unable to initialize the MediaPlayer for Audio Url = " + mediaUrl, e);
	            	stop();
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
    	cn.setConnectTimeout(1000 * 30);
        cn.connect();   
        stream = cn.getInputStream();
        if (stream == null) {
        	Log.e(TAG, "Unable to create InputStream for mediaUrl: " + mediaUrl);
        }
        
		Log.i(TAG, "File name: " + downloadingMediaFile);
		BufferedOutputStream bout = new BufferedOutputStream ( new FileOutputStream(downloadingMediaFile), 32 * 1024 );   
        byte buf[] = new byte[16 * 1024];
        int totalBytesRead = 0, incrementalBytesRead = 0, numread = 0;
        
        do {
        	if (bout == null) {
        		counter++;
        		Log.i(TAG, "FileOutputStream is null, Create new one: " + DOWNFILE + counter);
        		downloadingMediaFile = new File(context.getCacheDir(),DOWNFILE + counter);
        		downloadingMediaFile.deleteOnExit();
        		bout = new BufferedOutputStream ( new FileOutputStream(downloadingMediaFile) );	
        	}

        	try {
        		numread = stream.read(buf);
        	} catch (IOException e){
        		Log.e(TAG, e.toString());
        		if (stream != null){
        			Log.i(TAG, "Bad read. Let's try to reconnect to source and continue downloading");
        			cn = new URL(mediaUrl).openConnection(); 
        			cn.setConnectTimeout(1000 * 30);
        	        cn.connect();   
        	        stream = cn.getInputStream();
        	        numread = stream.read(buf);
        		}
        	}
        	
            if (numread <= 0) {  
                break;   
            	
            } else {
            	//Log.v(TAG, "write to file");
            	bout.write(buf, 0, numread);

            	totalBytesRead += numread;
            	incrementalBytesRead += numread;
            	totalKbRead = totalBytesRead/1000;
            }
            
            if ( totalKbRead >= INTIAL_KB_BUFFER ) {
            	Log.v(TAG, "Reached Buffer amount we want: " + "totalKbRead: " + totalKbRead + " INTIAL_KB_BUFFER: " + INTIAL_KB_BUFFER);
            	bout.flush();
            	bout.close();
            	            	
            	bout = null;
            	
            	setupplayer(downloadingMediaFile);
            	totalBytesRead = 0;

            }
            
        } while (stream != null);   


    }  
    
    /**
     * Set Up player(s)
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

	        				Log.i(TAG, "Current size of mediaplayer list: " + mediaplayers.size() );
	        				boolean waitingForPlayer = false;
	        				boolean leave = false;
	        				while (mediaplayers.size() <= 1 && leave == false){
    			        		Log.v(TAG, "waiting for another mediaplayer");
    			        		if (waitingForPlayer == false ){
    			        			try {
    			        				Log.v(TAG, "Sleep for a moment");
    			        				//Spin the spinner
    			        				PlayListTab a = (PlayListTab) context ;
    			        				a.handler.sendEmptyMessage(PlayListTab.SPIN);
    			        				Thread.sleep(1000 * 15);
    			        				a.handler.sendEmptyMessage(PlayListTab.STOPSPIN);
    			        				waitingForPlayer = true;
    			        			} catch (InterruptedException e) {
    			        				Log.e(TAG, e.toString());
    			        			}
    			        		} else {
    			        			Log.e(TAG, "Timeout occured waiting for another media player");
    			        			Toast.makeText(context, "Trouble downloading audio. :-(" , Toast.LENGTH_LONG).show();
    			        			stop();
    			        			
    			        			leave = true;
    			        		}
    			        	}
	        				if (leave == false){
	        					MediaPlayer mp2 = mediaplayers.get(1);
	        					mp2.start();
	        					Log.i(TAG, "Start another player");
    			        	
	        					mp.release();
	        					mediaplayers.remove(mp);
	        					removefile();
	        				}
	        				
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
	        		stop();
	        	} catch  (IOException	e) {
	        		Log.e(TAG, e.toString());
	        		stop();
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
    	PlayListTab a = (PlayListTab) context;

    	a.handler.sendEmptyMessage(PlayListTab.STOPSPIN);
    	//ImageView spinner = (ImageView) a.findViewById(R.id.process); 
		//spinner.clearAnimation();
    	
    }
    
    //Stop Audio
    public void stop(){
    	String TAG = "STOP";
    	Log.i(TAG,"Stop Player");
    	PlayListTab a = (PlayListTab) context;
    	try {
    		if (! mediaplayers.isEmpty() ){
    			MediaPlayer mp = mediaplayers.get(0);
    			if (mp.isPlaying()){
    				mp.pause();
    			}
    		}
    		
    		if (stream != null){
    			stream.close();
    		}
    		stream = null;
    		
    		a.handler.sendEmptyMessage(PlayListTab.STOP);

    	} catch (ArrayIndexOutOfBoundsException e) {
    		Log.e(TAG, "No items in Media player List");
    		a.handler.sendEmptyMessage(PlayListTab.STOP);
    	} catch (IOException e) {
    		Log.e(TAG, "error closing open connection");
    		a.handler.sendEmptyMessage(PlayListTab.STOP);
    	}
    }

    //Is the streamer playing audio?
    public boolean isPlaying() {
    	String TAG = "isPlaying";
    	boolean result = false;
    	try {
    		MediaPlayer mp = mediaplayers.get(0);
    		if (mp.isPlaying()){
    			result = true;
    		} else {
    			result = false;
    		}
    	} catch (ArrayIndexOutOfBoundsException e) {
    		Log.e(TAG, "No items in Media player List");
    	}
    	
    	return result;
    }
    
}


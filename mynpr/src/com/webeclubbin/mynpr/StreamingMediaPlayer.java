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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * MediaPlayer does not yet support "Shoutcast"-like streaming from external URLs so this class provides a pseudo-streaming function
 * by downloading the content incrementally & playing as soon as we get enough audio in our temporary storage.
 */ 
public class StreamingMediaPlayer extends Service {

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
	private boolean processHasStarted = false; 
	private InputStream stream = null;
	private URL url = null;
    private URLConnection urlConn = null;
    
    private String station = null;
    private String audiourl = null;
    
    private Intent startingIntent = null;
    //private int currentStatus = -1;

    //This object will allow other processes to interact with our service
    private final IStreamingMediaPlayer.Stub ourBinder = new IStreamingMediaPlayer.Stub(){
        String TAG = "IStreamingMediaPlayer.Stub";
        // Returns Currently Station Name
        public String getStation(){
        	Log.i(TAG, "getStation" );
        	return station;
        }
        
        // Returns Currently Playing audio url
        public String getUrl(){
        	Log.i(TAG, "getUrl" );
        	return audiourl;
        }
        
        // Check to see if service is playing audio
    	public boolean playing(){
    		Log.i(TAG, "playing?" );
    		return isPlaying();
    	}
    	
    	//Start playing audio
    	public void startAudio(){
    		Log.i(TAG, "startAudio" );
    		raiseThreadPriority();
    		
    		Runnable r = new Runnable() {   
    			public void run() {   
    				onStart (startingIntent, 0);
    			}   
    		};   
    		new Thread(r).start(); 
    		//onStart (startingIntent, 0);
    		 
    		/*Log.i(TAG, "Intent: " + startingIntent.getStringExtra(PlayListTab.URL));
        	Log.i(TAG, "Station: " + startingIntent.getStringExtra(PlayListTab.STATION));
        	
        	processHasStarted = true;
        	
        	audiourl =  startingIntent.getStringExtra(PlayListTab.URL);
        	station =  startingIntent.getStringExtra(PlayListTab.STATION);
        	
        	//Stop player if it is running
        	if ( isPlaying() ) {
        		stop();
        	}
    		
    		try {
    			counter = 0;
    			downloadingMediaFile = new File(context.getCacheDir(),DOWNFILE + counter);
         		downloadingMediaFile.deleteOnExit();
         		
        		startStreaming( audiourl );
        	} catch (IOException e) {
        		Log.i(TAG, e.toString() );
        	}*/
    		
    	}
    	
    	//Stop playing audio
    	public void stopAudio() {
    		Log.i(TAG, "stopAudio" );
    		stop();
    	}
    	
    	/*
    	//Return current status of streaming service
    	public int checkStatus() {
    		int tempstat = currentStatus;
    		if ( tempstat != -1 ) {
    			//reset currentStatus back to normal since someone already
    			//checked the status by calling this method
    			currentStatus = -1;
    		}
    		return tempstat;
    	} */
    };
    
    @Override 
    public void onCreate() {
    	  super.onCreate();
    	  
    	  String TAG = "StreamingMediaPlayer - onCreate";
    	  Log.i(TAG, "START");

    	  // listen for calls
    	  // http://www.androidsoftwaredeveloper.com/2009/04/20/how-to-detect-call-state/
    	  
    	  final PhoneStateListener myPhoneListener = new PhoneStateListener() {
    		  public void onCallStateChanged(int state, String incomingNumber) {
    			  String TAG = "PhoneStateListener";
    			  
    			  switch (state) {
    			  	case TelephonyManager.CALL_STATE_RINGING:
    			  		Log.i(TAG, "Someone's calling. Let us stop the service");
    			  		sendMessage(PlayListTab.STOP);
    				  	break;
    			  	case TelephonyManager.CALL_STATE_OFFHOOK:
    				  	break;
    				case TelephonyManager.CALL_STATE_IDLE:
    				  	break;
    				default:
    				  	Log.d(TAG, "Unknown phone state = " + state);
    			  }
    		  }
    	  };
    	  Log.i(TAG, "Setup Phone listener");
    	  TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
    	  tm.listen(myPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);

    }
    
    @Override
    public void onStart (Intent intent, int startId){
    	super.onStart(intent, startId);
    	
    	String TAG = "StreamingMediaPlayer - onStart";
    	Log.i(TAG, "START");

    	Log.i(TAG, "Intent: " + intent.getStringExtra(PlayListTab.URL));
    	Log.i(TAG, "Station: " + intent.getStringExtra(PlayListTab.STATION));
    	
    	processHasStarted = true;
    	
    	audiourl =  intent.getStringExtra(PlayListTab.URL);
    	station =  intent.getStringExtra(PlayListTab.STATION);
    	
    	Log.i(TAG,"Run startStreaming function");
    	try {
    		context = this;
     		downloadingMediaFile = new File(context.getCacheDir(),DOWNFILE + counter);
     		downloadingMediaFile.deleteOnExit();

    		startStreaming( audiourl );
     	    		
    	} catch (IOException e) {
    		Log.i(TAG, e.toString() );
    	}
    	
    }
    
    @Override 
    public void onDestroy() {
    	  super.onDestroy();

    	  String TAG = "StreamingMediaPlayer - onDestroy";
    	  Log.i(TAG, "START");
    	  
    	  // stop the service here

    }
    
    @Override
    public IBinder onBind (Intent intent){
    	String TAG = "StreamingMediaPlayer - onBind";
    	Log.i(TAG, "START");
    	Log.i(TAG, "Intent: " + intent.getStringExtra(PlayListTab.URL));
    	Log.i(TAG, "Station: " + intent.getStringExtra(PlayListTab.STATION));
    	startingIntent = intent;

    	context = this;
    	
    	return ourBinder;
    }
   
	
    /**  
     * Progressivly download the media to a temporary location and update the MediaPlayer as new content becomes available.
     */  
    public void startStreaming(final String mediaUrl) throws IOException {
    	
    	final String TAG = "startStreaming";
        int bitrate = 56;
        
        sendMessage( PlayListTab.CHECKRIORITY );
        
		sendMessage( PlayListTab.RAISEPRIORITY );
        
        sendMessage( PlayListTab.START );
        
    	try {
    		url = new URL(mediaUrl);
    		urlConn = (HttpURLConnection)url.openConnection();
    		urlConn.setReadTimeout(1000 * 20);
    		urlConn.setConnectTimeout(1000 * 5);

    		String ctype = urlConn.getContentType () ;
    		if (ctype == null){
    			ctype = "";
    		} else {
    			ctype = ctype.toLowerCase() ;
    		}
    		
    		//See if we can handle this type 
    		Log.i(TAG, "Content Type: " + ctype );
    		if (ctype.contains(AUDIO_MIME) || ctype.equals("")){
    			
    			String temp = urlConn.getHeaderField(BITERATE_HEADER);
    			Log.i(TAG, "Bitrate: " + temp );
    			if (temp != null){
    				bitrate = new Integer(temp).intValue();
    			}
    		} else {
    			Log.e(TAG, "Does not look like we can play this audio type: " + ctype);
    			Log.e(TAG, "Or we could not connect to audio");
    			sendMessage (PlayListTab.TROUBLEWITHAUDIO);
    			stop();
    			return;
    		}
    	} catch (IOException ioe) {
    		Log.e( TAG, "Could not connect to " +  mediaUrl );
    		sendMessage( PlayListTab.TROUBLEWITHAUDIO);
    		stop();
    		return;
    	} 
    	
    	//Set up buffer size
    	//Assume XX kbps * XX seconds / 8 bits per byte
    	INTIAL_KB_BUFFER =  bitrate * SECONDS / BIT;
    	
    	/*try {   
    		downloadAudioIncrement(mediaUrl);
        } catch (IOException e) {
        	Log.e(TAG, "Unable to initialize the MediaPlayer for Audio Url = " + mediaUrl, e);
        	sendMessage( PlayListTab.TROUBLEWITHAUDIO);
        	stop();
        	return;
        }  */
    	 
		Runnable r = new Runnable() {   
	        public void run() {   
	            try {   
	        		downloadAudioIncrement(mediaUrl);
	            } catch (IOException e) {
	            	Log.e(TAG, "Unable to initialize the MediaPlayer for Audio Url = " + mediaUrl, e);
	            	sendMessage( PlayListTab.TROUBLEWITHAUDIO);
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

    	//URLConnection cn = new URL(mediaUrl).openConnection(); 
    	//cn.setConnectTimeout(1000 * 30);
    	//cn.setReadTimeout(1000 * 15);
        //cn.connect();   
        stream = urlConn.getInputStream();
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
        			urlConn = new URL(mediaUrl).openConnection(); 
        			urlConn.setConnectTimeout(1000 * 30);
        			urlConn.connect();   
        	        stream = urlConn.getInputStream();
        	        numread = stream.read(buf);
        		}
        	} catch (NullPointerException e) {
        		//Let's get out of here
        		break;
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
            	sendMessage( PlayListTab.CHECKRIORITY );
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
	        				Log.i(TAG, "Start");
	        				Log.i(TAG, "Current size of mediaplayer list: " + mediaplayers.size() );
	        				boolean waitingForPlayer = false;
	        				boolean leave = false;
	        				while (mediaplayers.size() <= 1 && leave == false){
    			        		Log.v(TAG, "waiting for another mediaplayer");
    			        		if (waitingForPlayer == false ){
    			        			try {
    			        				Log.v(TAG, "Sleep for a moment");
    			        				//Spin the spinner
    			        				
    			        				sendMessage( PlayListTab.SPIN ) ;
    			        				//Thread.currentThread().sleep(1000 * 15);
    			        				Thread.sleep(1000 * 15);
    			        				
    			        				
    			        				sendMessage( PlayListTab.STOPSPIN );
    			        				waitingForPlayer = true;
    			        			} catch (InterruptedException e) {
    			        				Log.e(TAG, e.toString());
    			        			}
    			        		} else {
    			        			Log.e(TAG, "Timeout occured waiting for another media player");
    			        			//Toast.makeText(context, "Trouble downloading audio. :-(" , Toast.LENGTH_LONG).show();
    			        			sendMessage( PlayListTab.TROUBLEWITHAUDIO);
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
	        		
	        		Log.i(TAG, "Setup player completion listener");
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
	        		sendMessage( PlayListTab.TROUBLEWITHAUDIO);
	        		stop();
	        	} catch  (IOException	e) {
	        		Log.e(TAG, e.toString());
	        		sendMessage( PlayListTab.TROUBLEWITHAUDIO);
	        		stop();
	        	}
	        	
 	        }
	    };
	    Thread ourthread = new Thread(r);
	    ourthread.start();
	    
	    // Wait indefinitely for the thread to finish
	    if ( ! started  ){
	    	try {
	    		Log.i(TAG, "Start and wait for first audio clip to be prepared.");
	    		ourthread.join();
	    		// Finished
	    	} catch (InterruptedException e) {
	    		// Thread was interrupted
	    	}
	    }  

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
    	
    	sendMessage(PlayListTab.STOPSPIN);
    		
    }
    
    //Stop Audio
    public void stop(){
    	String TAG = "STOP";
    	Log.i(TAG,"Entry");
    	
    	try {
    		if (! mediaplayers.isEmpty() ){
    			MediaPlayer mp = mediaplayers.get(0);
    			if (mp.isPlaying()){
    				Log.i(TAG,"Stop Player");
    				mp.pause();
    			}
    		}
    		
    		if (stream != null){
    			Log.i(TAG,"Close stream");
    			stream.close();
    		}
    		stream = null;
    		
    		//if (tellPlayList) {
    		//	sendMessage(PlayListTab.STOP);
    		//}
    		//sendMessage(PlayListTab.RESETPLAYSTATUS);
    		stopSelf();
    		

    	} catch (ArrayIndexOutOfBoundsException e) {
    		Log.e(TAG, "No items in Media player List");
    		sendMessage(PlayListTab.STOP);
    	} catch (IOException e) {
    		Log.e(TAG, "error closing open connection");
    		sendMessage(PlayListTab.STOP);
    	}
    }

    //Is the streamer playing audio?
    public boolean isPlaying() {
    	String TAG = "isPlaying";
    	Log.i(TAG, " = "  + processHasStarted);
    	/*boolean result = false;
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
    	
    	return result; */
    	return processHasStarted ;
    }
    
    //Send Message to Playlist
    private synchronized void sendMessage(int m){
    	String TAG = "sendMessage";
    	Intent i = new Intent(MyNPR.tPLAY);

    	i.putExtra(PlayListTab.MSG, m);
    	Log.i(TAG, "Broadcast Message intent");
    	context.sendBroadcast (i) ;
    }
    
    /*private void checkThreadPriority(){
    	String TAG = "checkThreadPriority";
    	Log.i(TAG, "Start" );
    	//Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO); 
    	Log.v(TAG, "Process priority: " + Process.getThreadPriority(Process.myTid()));
    	//Process.THREAD_PRIORITY_FOREGROUND
    } */
    
    private void raiseThreadPriority(){
    	String TAG = "raiseThreadPriority";
    	Log.i(TAG, "Start" );
    	Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO); 
    	//Log.v(TAG, "Process priority: " + Process.getThreadPriority(Process.myTid()));
    	//Process.THREAD_PRIORITY_FOREGROUND
    } 
    
}


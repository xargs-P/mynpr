//AIDL file for StreamMediaPlayer
package com.webeclubbin.mynpr;

// Interface for Streaming Player.
interface IStreamingMediaPlayer {
    
    // Returns Currently Station Name
    String getStation();
    // Returns Currently Playing audio url
    String getUrl();
    // Check to see if service is playing audio
	boolean playing();
	//Start playing audio
	void startAudio();
	//Stop playing audio
	void stopAudio();
	//Return current status of streaming service
	int checkStatus();
	
}
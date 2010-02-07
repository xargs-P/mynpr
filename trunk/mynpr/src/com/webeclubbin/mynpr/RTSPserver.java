package com.webeclubbin.mynpr;

/* ------------------
RTSP Server from http://www.csee.umbc.edu/~pmundur/courses/CMSC691C/lab5-kurose-ross.html
---------------------- */


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

public class RTSPserver {

//RTP variables:
//----------------
DatagramSocket RTPsocket; //socket to be used to send and receive UDP packets
DatagramPacket senddp; //UDP packet containing the video frames

InetAddress ClientIPAddr; //Client IP address
int RTP_dest_port = 0; //destination port for RTP packets  (given by the RTSP Client)

//Video variables:
//----------------
int imagenb = 0; //image nb of the image currently transmitted
//VideoStream video; //VideoStream object used to access video frames
String audioStreamUrl ;  //Source to download audio
static int MJPEG_TYPE = 26; //RTP payload type for MJPEG video
static int FRAME_PERIOD = 100; //Frame period of the video to stream, in ms
//static int VIDEO_LENGTH = 500; //length of the video in frames

Timer timer; //timer used to send the audio at the audio frame rate
byte[] buf; //buffer used to store the images to send to the client 

//RTSP variables
//----------------
//rtsp states
final static int INIT = 0;
final static int READY = 1;
final static int PLAYING = 2;
//rtsp message types
final static int SETUP = 3;
final static int PLAY = 4;
final static int PAUSE = 5;
final static int TEARDOWN = 6;

int intialport = 90123;

static int state; //RTSP Server state == INIT or READY or PLAY
Socket RTSPsocket; //socket used to send/receive RTSP messages
//input and output stream filters
static BufferedReader RTSPBufferedReader;
static BufferedWriter RTSPBufferedWriter;

static int RTSP_ID = 123456; //ID of the RTSP session
int RTSPSeqNb = 0; //Sequence number of RTSP messages within the session

final static String CRLF = "\r\n";

//--------------------------------
//Constructor
//--------------------------------
public RTSPserver(){

 //init Frame
 super();

 //init Timer
 timer = new Timer();
 //timer.schedule(processTask, 0, 0);

 //allocate memory for the sending buffer
 buf = new byte[15000]; 

}
       
//------------------------------------
//main
//------------------------------------
public void startup() throws Exception
{
	final String TAG = "startup";
 //create a Server object
	RTSPserver theServer = new RTSPserver();

 //get RTSP socket port from the command line
 int RTSPport = intialport ;

 //Initiate TCP connection with the client for the RTSP session
 ServerSocket listenSocket = new ServerSocket(RTSPport);
 theServer.RTSPsocket = listenSocket.accept();
 listenSocket.close();

 //Get Client IP address
 theServer.ClientIPAddr = theServer.RTSPsocket.getInetAddress();

 //Initiate RTSPstate
 state = INIT;

 //Set input and output stream filters:
 RTSPBufferedReader = new BufferedReader(new InputStreamReader(theServer.RTSPsocket.getInputStream()) );
 RTSPBufferedWriter = new BufferedWriter(new OutputStreamWriter(theServer.RTSPsocket.getOutputStream()) );

 //Wait for the SETUP message from the client
 int request_type;
 boolean done = false;
 while(!done)
   {
	request_type = theServer.parse_RTSP_request(); //blocking
	
	if (request_type == SETUP)
	  {
	    done = true;

	    //update RTSP state
	    state = READY;
	    Log.d(TAG,"New RTSP state: READY");

	    //Send response
	    theServer.send_RTSP_response();

	    //init the audio url
	    theServer.audioStreamUrl = audioStreamUrl;

	    //init RTP socket
	    theServer.RTPsocket = new DatagramSocket();
	  }
   }

  //loop to handle RTSP requests
 while(true)
   {
	//parse the request
	request_type = theServer.parse_RTSP_request(); //blocking
	    
	if ((request_type == PLAY) && (state == READY))
	  {
	    //send back response
	    theServer.send_RTSP_response();
	    //start timer
	    //theServer.timer.start();
	    timer.schedule(processTask, 0, 0);
	    //update state
	    state = PLAYING;
	    Log.d(TAG,"New RTSP state: PLAYING");
	  }
	else if ((request_type == PAUSE) && (state == PLAYING))
	  {
	    //send back response
	    theServer.send_RTSP_response();
	    //stop timer
	    //theServer.timer.stop();
	    timer.cancel();
	    //update state
	    state = READY;
	    Log.d(TAG, "New RTSP state: READY");
	  }
	else if (request_type == TEARDOWN)
	  {
	    //send back response
	    theServer.send_RTSP_response();
	    //stop timer
	    //theServer.timer.stop();
	    timer.cancel();
	    //close sockets
	    theServer.RTSPsocket.close();
	    theServer.RTPsocket.close();

	    System.exit(0);
	  }
   }
}


//------------------------
//Handler for timer
//------------------------
public TimerTask processTask = new TimerTask() {

	@Override
	public void run() {
		final String TAG = "processTask";
		
		//update current imagenb
		imagenb++;
    
		try {
			//get next frame to send from the video, as well as its size
			//int image_length = video.getnextframe(buf);
			int image_length = 0;

			//Builds an RTPpacket object containing the frame
			RTPpacket rtp_packet = new RTPpacket(MJPEG_TYPE, imagenb, imagenb*FRAME_PERIOD, buf, image_length);
	  
			//get to total length of the full rtp packet to send
			int packet_length = rtp_packet.getlength();

			//retrieve the packet bitstream and store it in an array of bytes
			byte[] packet_bits = new byte[packet_length];
			rtp_packet.getpacket(packet_bits);

			//send the packet as a DatagramPacket over the UDP socket 
			senddp = new DatagramPacket(packet_bits, packet_length, ClientIPAddr, RTP_dest_port);
			RTPsocket.send(senddp);

			Log.d(TAG, "Send frame #"+imagenb);
			//print the header bitstream
			rtp_packet.printheader();

		} catch (Exception ex) {
			Log.e(TAG, "Exception caught: "+ex);
			System.exit(0);
		}
		
	}
};

//------------------------------------
//Parse RTSP Request
//------------------------------------
private int parse_RTSP_request()
{
	final String TAG = "parse_RTSP_request";
 int request_type = -1;
 try{
   //parse request line and extract the request_type:
   String RequestLine = RTSPBufferedReader.readLine();
   Log.v(TAG,"RTSP Server - Received from Client:");
   Log.v(TAG,RequestLine);

   StringTokenizer tokens = new StringTokenizer(RequestLine);
   String request_type_string = tokens.nextToken();

   //convert to request_type structure:
   if ((new String(request_type_string)).compareTo("SETUP") == 0)
	request_type = SETUP;
   else if ((new String(request_type_string)).compareTo("PLAY") == 0)
	request_type = PLAY;
   else if ((new String(request_type_string)).compareTo("PAUSE") == 0)
	request_type = PAUSE;
   else if ((new String(request_type_string)).compareTo("TEARDOWN") == 0)
	request_type = TEARDOWN;

   if (request_type == SETUP)
	{
	  //extract url from RequestLine
	  audioStreamUrl = tokens.nextToken();
	}

   //parse the SeqNumLine and extract CSeq field
   String SeqNumLine = RTSPBufferedReader.readLine();
   Log.v(TAG, SeqNumLine);
   tokens = new StringTokenizer(SeqNumLine);
   tokens.nextToken();
   RTSPSeqNb = Integer.parseInt(tokens.nextToken());
	
   //get LastLine
   String LastLine = RTSPBufferedReader.readLine();
   
   Log.v(TAG, LastLine);

   if (request_type == SETUP)
	{
	  //extract RTP_dest_port from LastLine
	  tokens = new StringTokenizer(LastLine);
	  for (int i=0; i<3; i++)
	    tokens.nextToken(); //skip unused stuff
	  RTP_dest_port = Integer.parseInt(tokens.nextToken());
	}
   //else LastLine will be the SessionId line ... do not check for now.
 }
 catch(Exception ex)
   {
	 Log.e(TAG, "Exception caught: "+ex);
	System.exit(0);
   }
 return(request_type);
}

//------------------------------------
//Send RTSP Response
//------------------------------------
private void send_RTSP_response()
{
	final String TAG = "send_RTSP_response";
 try{
   RTSPBufferedWriter.write("RTSP/1.0 200 OK"+CRLF);
   RTSPBufferedWriter.write("CSeq: "+RTSPSeqNb+CRLF);
   RTSPBufferedWriter.write("Session: "+RTSP_ID+CRLF);
   RTSPBufferedWriter.flush();
   Log.v(TAG, "RTSP Server - Sent response to Client.");
 }
 catch(Exception ex)
   {
	Log.e(TAG, "Exception caught: "+ex);
	System.exit(0);
   }
}
}
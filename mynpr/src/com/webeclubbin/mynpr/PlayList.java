package com.webeclubbin.mynpr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

//Holds the playlist information
public class PlayList  {

	private HashMap<String, Vector<String> > plist = new HashMap<String, Vector<String> >();
	private HashMap<String, String> logos = new HashMap<String, String>();
	private String TAG = "PlayList";
	private final String playlistfile = "playlist";
	private final String SPLITTERAUDIO = "<MYNPR>";
	private final String SPLITTERSTATION = "#MYNPR#";
	private Activity context = null;
	private boolean saved = true;
	
	PlayList(Activity context) {
        Log.i(TAG, "Constructor PlayList");
        this.context = context;
    } 
	
	//Add audio url
	public void addUrl(String station, String url){
		Log.i(TAG, "addUrl: " + station);
		
		if ( plist.containsKey(station) ) {
			Log.i(TAG, "add url, station already here");
			Vector<String> v = (Vector<String>) plist.get(station);
			
			Log.i(TAG, "urls : " + v.size());
			String[] t = {""} ;
			String[] temp = v.toArray(t);
			boolean alreadyHere = false;
			for (int i = 0; i < v.size() ; i++){
				if ( url.equals(temp[i])){
					alreadyHere = true;
				}
			}
			if (alreadyHere == false){
				v.add(url);
				plist.put(station, v);
			}
		} else {
			Log.i(TAG, "add station and url");
			Vector<String> v = new Vector<String>(); 
			v.add(url);
			plist.put(station, v);
			
		}
		saved = false;
	}
	
	//Is the data saved to disk?
	public boolean isSaved(){
		return saved;
	}
	
	//Add station to logo
	public void addStation(String station, String logo){
		Log.i(TAG, "AddStation: " + station);
		logos.put(station,logo);
		saved = false;
	}
	
	//Get Stations
	public String[] getStations(){
		Log.i(TAG, "getStations");
		Set<String> s = plist.keySet();
		String[] t = {""} ;
		Log.i(TAG, "Number of Stations: " + s.toArray(t).length);
		return s.toArray(t);
	}
	
	//Get Logos
	public String[] getLogos(){
		Log.i(TAG, "getLogos");
		Collection<String> v = logos.values();
		String[] t = {""} ;
		return v.toArray(t);
	}
	
	//Get Logo
	public String getLogo(String station){
		Log.i(TAG, "getLogo");
		return logos.get(station);
	}
	
	//Grab audio urls
	public String[] getUrls(String station){
		Log.i(TAG, "get urls for station: " + station);
		Vector<String> urls = (Vector<String>) plist.get(station);
		if (urls != null){
			Log.i(TAG, "urls : " + urls.size());
			String[] t = {""} ;
			return urls.toArray(t);
		} else {
			return null;
		}
	}
	
	//Load data from file
	public boolean loadfromfile(){
		String TAG = "Load Playlist from File";
		try {
    		FileInputStream fis = context.openFileInput(playlistfile);
    		if (fis == null){
    			Log.e(TAG, "No playlist file to open");
    			return false;
    		}
    		DataInputStream in = new DataInputStream(fis);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            
            String strLine;
            Log.i(TAG,"loop through file");
            //Read File Line By Line
            while ((strLine = br.readLine()) != null)   {
            	Log.i(TAG,strLine);
            	if ( strLine.contains(SPLITTERAUDIO) ) {
            		String [] s = Pattern.compile(SPLITTERAUDIO).split(strLine);
            		addUrl(s[0], s[1]);
            	} else if ( strLine.contains(SPLITTERSTATION) ) {
            		String [] s = Pattern.compile(SPLITTERSTATION).split(strLine);
            		addStation(s[0], s[1]);
            	}
            	
            }
            br.close();
            in.close();
            fis.close();
    		
    	} catch (IOException ioe) {
    		Log.e(TAG, "Can't read file " + ioe.getMessage() );
    	}
    	return true;
	}
	
	//Save data to file
	public boolean savetofile(){
		String TAG = "Save Playlist to File";
		try {
    		FileOutputStream fos = context.openFileOutput(playlistfile, Context.MODE_PRIVATE);
    		if (fos == null){
    			Log.e(TAG, "No playlist file to open");
    			return false;
    		}
    		DataOutputStream out = new DataOutputStream(fos);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
            
            Log.i(TAG,"loop through file");
            String [] s = getStations();
            //Read File Line By Line
            for (int i = 0; i <  s.length; i++)   {
            	String strLine = s[i] + SPLITTERSTATION + logos.get(s[i]);
            	bw.write(strLine);
            	bw.newLine();
            	
            	String [] a = getUrls(s[i]);
            	for (int y = 0; y <  a.length; y++) {
            		strLine = s[i] + SPLITTERAUDIO + a[y];
            		bw.write(strLine);
            		bw.newLine();
            	}

            }
            bw.close();
            out.close();
            fos.close();
    		
    	} catch (IOException ioe) {
    		Log.e(TAG, "Problem writing to file " + ioe.getMessage() );
    	}
    	saved = true;
    	return true;
	}
	
	//Delete list
	public void deleteList() {
		String TAG = "deleteList";
		
    	Log.i(TAG, "remove: " + playlistfile );
    	context.deleteFile(playlistfile);
    	
    	//Reset variables
    	Log.i(TAG, "Reset variables");
    	plist = new HashMap<String, Vector<String> >();
    	logos = new HashMap<String, String>();
    	
    	saved = true;
	}
	
	//Dump data so we can save in a Bundle
	public String [] dumpDataOut() {
		String TAG = "dumpDataOut";
		
		Log.i(TAG, "Start");
		
    	Vector<String> lineofdata = new Vector<String>();
    	String [] s = getStations();
        //Grab data out
        for (int i = 0; i <  s.length; i++)   {
        	Log.i(TAG, s[i]);
        	lineofdata.add(  s[i] + SPLITTERSTATION + logos.get(s[i]) );
        	
        	String [] a = getUrls(s[i]);
        	for (int y = 0; y <  a.length; y++) {
        		lineofdata.add( s[i] + SPLITTERAUDIO + a[y] );
        	}

        }
        return lineofdata.toArray(s);
	}
	
	//Load playlist object from data dump
	public void dumpDataIn(String [] d) {
		String TAG = "dumpDataIn";
		
		Log.i(TAG, "Start");
		
		for (int i = 0; i < d.length; i++)   {
        	Log.i(TAG,d[i]);
        	if ( d[i].contains(SPLITTERAUDIO) ) {
        		String [] s = Pattern.compile(SPLITTERAUDIO).split(d[i]);
        		addUrl(s[0], s[1]);
        	} else if ( d[i].contains(SPLITTERSTATION) ) {
        		String [] s = Pattern.compile(SPLITTERSTATION).split(d[i]);
        		addStation(s[0], s[1]);
        	}
        }
		saved = false;
	}
	
}

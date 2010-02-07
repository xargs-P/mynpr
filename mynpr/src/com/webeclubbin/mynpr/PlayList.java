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
import java.util.Iterator;
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
	//setup for podcasts variable: <Station Name, <Title, url>>
	private HashMap<String, HashMap<String, String>> podcasts = new HashMap<String, HashMap<String, String>>();
	private String TAG = "PlayList";
	private final String playlistfile = "playlist";
	private static final String SPLITTERAUDIO = "<MYNPR>";
	public static final String SPLITTERRSS = "@RSS@";
	public static final String SPLITTERRSSTITLE = "@RSSTITLE@";
	private static final String SPLITTERSTATION = "#MYNPR#";
	private Activity context = null;
	private boolean saved = true;
	
	PlayList(Activity context) {
        Log.d(TAG, "Constructor PlayList");
        this.context = context;
    } 
	
	//Add audio url
	public void addUrl(String station, String url){
		Log.d(TAG, "addUrl: " + station);
		
		if ( plist.containsKey(station) ) {
		
			Log.d(TAG, "add url, station already here");
			Vector<String> v = (Vector<String>) plist.get(station);
			
			Log.d(TAG, "urls : " + v.size());
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
			Log.d(TAG, "add station and url");
			Vector<String> v = new Vector<String>(); 
			v.add(url);
			plist.put(station, v);
		}

		saved = false;
	}
	
	//Add RSS url
	public void addRSSUrl(PlayURL pu){
		Log.d(TAG, "addRSSUrl: " + pu.getStation());
		
		if ( podcasts.containsKey(pu.getStation()) ) {
				
			Log.d(TAG, "add url, station already here");
			HashMap<String, String> v = (HashMap<String, String>) podcasts.get(pu.getStation());
			
			Log.d(TAG, "urls : " + v.size());
			v.put(pu.getTitle(), pu.getURL());
			podcasts.put(pu.getStation(), v);
		} else {
			Log.d(TAG, "add station and url");
			HashMap<String, String> v = new HashMap<String, String>(); 
			v.put(pu.getTitle(), pu.getURL());
			podcasts.put(pu.getStation(), v);
		}
		saved = false;
	}
	
	//Is the data saved to disk?
	public boolean isSaved(){
		return saved;
	}
	
	//Add station to logo
	public void addStation(String station, String logo){
		Log.d(TAG, "AddStation: " + station);
		logos.put(station,logo);
		saved = false;
	}
	
	//Get Stations
	public String[] getStations(){
		Log.d(TAG, "getStations");
		//Set<String> s = plist.keySet();
		Set<String> s = logos.keySet();
		String[] t = {""} ;
		Log.d(TAG, "Number of Stations: " + s.toArray(t).length);
		if (s.toArray(t).length == 1){
			String[] temp = s.toArray(t);
			if (temp[0] == null){
				return null;
			}
		}
		return s.toArray(t); 
		
	}
	
	//Get Logos
	public String[] getLogos(){
		Log.d(TAG, "getLogos");
		Collection<String> v = logos.values();
		String[] t = {""} ;
		return v.toArray(t);
	}
	
	//Get Logo
	public String getLogo(String station){
		Log.d(TAG, "getLogo");
		return logos.get(station);
	}
	
	//Grab audio urls
	public String[] getUrls(String station){
		Log.d(TAG, "get urls for station: " + station);
		Vector<String> urls = (Vector<String>) plist.get(station);
		if (urls != null){
			Log.d(TAG, "urls : " + urls.size());
			String[] t = {""} ;
			return urls.toArray(t);
		} else {
			return null;
		}
	}
	
	//Grab RSS URLs
	public HashMap<String, String> getRSSUrls(String station){
		Log.d(TAG, "send out rss urls");
		return podcasts.get(station);
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
            Log.d(TAG,"loop through file");
            //Read File Line By Line
            while ((strLine = br.readLine()) != null)   {
            	Log.d(TAG,strLine);
            	if ( strLine.contains(SPLITTERAUDIO) ) {
            		String [] s = Pattern.compile(SPLITTERAUDIO).split(strLine);
            		addUrl(s[0], s[1]);
            	} else if ( strLine.contains(SPLITTERSTATION) ) {
            		String [] s = Pattern.compile(SPLITTERSTATION).split(strLine);
            		addStation(s[0], s[1]);
            	} else if ( strLine.contains(SPLITTERRSS) ) {
            		String [] s = Pattern.compile(SPLITTERRSS).split(strLine);
            		PlayURL pu = new PlayURL();
            		pu.setRSS(true);
            		pu.setStation(s[0]);
            		pu.setTitle(s[1]);
            		pu.setURL(s[2]);
            		addRSSUrl(pu);
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
            
            Log.d(TAG,"loop through file");
            String [] s = getStations();
            //Save File Line By Line
            for (int i = 0; i <  s.length; i++)   {
            	String strLine = s[i] + SPLITTERSTATION + logos.get(s[i]);
            	bw.write(strLine);
            	bw.newLine();
            	
            	String [] a = getUrls(s[i]);
            	int len = 0;
            	if (a != null){
            		len = a.length;
            	}
            	//Process audio urls
            	for (int y = 0; y <  len; y++) {

            		strLine = s[i] + SPLITTERAUDIO + a[y];
            		
            		bw.write(strLine);
            		bw.newLine();
            	}
            	//Process rss urls
            	HashMap <String, String> rss = getRSSUrls(s[i]);
            	Set<String> rset = null;
            	if (rss != null){
            		rset = rss.keySet();
            	}
            	 
                if (rset != null){
                	Iterator<String> riterator = rset.iterator();
                	while (riterator.hasNext()) { 
                		String title = riterator.next();
                		String rssurl = rss.get(title);
                		strLine = s[i] + SPLITTERRSS + title + SPLITTERRSS + rssurl;
                    	bw.write(strLine);
                    	bw.newLine();
                	}
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
		
    	Log.d(TAG, "remove: " + playlistfile );
    	context.deleteFile(playlistfile);
    	
    	//Reset variables
    	Log.d(TAG, "Reset variables");
    	plist = new HashMap<String, Vector<String> >();
    	logos = new HashMap<String, String>();
    	
    	saved = true;
	}
	
	//Dump data so we can save in a Bundle
	public String [] dumpDataOut() { 
		String TAG = "dumpDataOut";
		
		Log.d(TAG, "Start");
		
    	Vector<String> lineofdata = new Vector<String>();
    	String [] s = getStations();
    	if (s == null){
    		return null;
    	}
        //Grab data out
        for (int i = 0; i <  s.length; i++)   {
        	Log.d(TAG, s[i]);
        	lineofdata.add(  s[i] + SPLITTERSTATION + logos.get(s[i]) );
        	
        	String [] a = getUrls(s[i]);
        	int len = 0;
        	if (a != null){
        		len = a.length;
        	}
        	for (int y = 0; y <  len; y++) {
        		lineofdata.add( s[i] + SPLITTERAUDIO + a[y] );
        	}
        	//Process rss urls
        	HashMap <String, String> rss = getRSSUrls(s[i]);
        	Set<String> rset = null; 
        	if (rss != null){
        		rset = rss.keySet();
        	}
            if (rset != null){
            	Iterator<String> riterator = rset.iterator();
            	while (riterator.hasNext()) { 
            		String title = riterator.next();
            		String rssurl = rss.get(title);
            		lineofdata.add(  s[i] + SPLITTERRSS + title + SPLITTERRSS + rssurl );
            	}
            }

        }
        return lineofdata.toArray(s);
	}
	
	//Load playlist object from data dump
	public void dumpDataIn(String [] d) {
		String TAG = "dumpDataIn";
		
		Log.d(TAG, "Start");
		
		for (int i = 0; i < d.length; i++)   {
        	Log.d(TAG,d[i]);
        	if ( d[i].contains(SPLITTERAUDIO) ) {
        		String [] s = Pattern.compile(SPLITTERAUDIO).split(d[i]);
        		addUrl(s[0], s[1]);
        	} else if ( d[i].contains(SPLITTERSTATION) ) {
        		String [] s = Pattern.compile(SPLITTERSTATION).split(d[i]);
        		addStation(s[0], s[1]);
        	}  else if ( d[i].contains(SPLITTERRSS) ) {
        		String [] s = Pattern.compile(SPLITTERRSS).split(d[i]);
        		PlayURL pu = new PlayURL();
        		pu.setRSS(true);
        		pu.setStation(s[0]);
        		pu.setTitle(s[1]);
        		pu.setURL(s[2]);
        		addRSSUrl(pu);
        	}
        }
		saved = false;
	}
	
}

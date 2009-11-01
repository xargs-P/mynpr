package com.webeclubbin.mynpr;

import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

//Holds the playlist information
public class PlayList {

	private HashMap<String, Vector<String> > plist = new HashMap<String, Vector<String> >();
	private HashMap<String, String> logos = new HashMap<String, String>();
	
	//Add audio url
	public void addurl(String station, String url){
		
		if ( plist.containsKey(station) ) {
			Vector<String> v = (Vector<String>) plist.get(station);
			v.add(url);
			plist.put(station, v);
		} else {
			Vector<String> v = new Vector<String>(); 
			v.add(url);
			plist.put(station, v);
		}
	}
	
	//Add station to logo
	public void addstation(String station, String logo){
		logos.put(station,logo);
	}
	
	//Get Stations
	public String[] getStations(){
		Set<String> s = plist.keySet();
		String[] t = {""} ;
		return s.toArray(t);
	}
	
	//Grab audio urls
	public String[] geturls(String station){
		Vector<String> urls = (Vector<String>) plist.get(station);
		String[] t = {""} ;
		return urls.toArray(t);
	}
}

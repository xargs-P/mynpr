package com.webeclubbin.mynpr;

import java.io.Serializable;
import java.util.Vector;

//Radio Station Object
public class Station implements Cloneable, Serializable {
	//Serial id number
	private static final long serialVersionUID = 7602100891193004964L;
	String name = null;
	String market = null;
	String logo = null;
	String orgurl = null;
	Vector<String> astreamurls = new Vector<String>();
	Vector<String> mstreamurls = new Vector<String>();
	Vector<String> podcasturls = new Vector<String>();
		
	final static String ORG = "Organization Home Page";
	final static String ASTREAM = "Audio Stream"; 
	final static String MP3STREAM = "Audio MP3 Stream";
	final static String PODCAST = "Podcast";
	final static String ORGID = "1";
	final static String ASTREAMID = "7"; 
	final static String MP3STREAMID = "10";
	final static String PODCASTID = "9";
	
	public void setName(String temp) {
		name = temp ;
	}
	public void setMarket(String temp) {
		market = temp;
	}
	public void setLogo(String temp) {
		logo = temp.trim();
	}
	public void setOUrl(String temp) {
		orgurl = temp;
	}
	public void setAUrl(String temp) {
		astreamurls.add(temp);
	}
	public void setMUrl(String temp) {
		mstreamurls.add(temp);
	}
	public void setPUrl(String temp) {
		podcasturls.add(temp);
	}
	
	
	
	public String getName() {
			return name;
	}
	public String getMarket() {
			return market;
	}
	public String getLogo() {
			return logo;
	}
	public String getOUrl() {
		return orgurl;
	}
	public String[] getAUrl() {
			String [] temp = new String [0];
			return (String[])astreamurls.toArray(temp);
	}
	public String[] getMUrl() {
		String [] temp = new String [0];
		return (String[])mstreamurls.toArray(temp);
	}
	public String[] getPUrl() {
		String [] temp = new String [0];
		return (String[])podcasturls.toArray(temp);
	}
	
	public Object clone() throws CloneNotSupportedException
	{
		Station another = (Station) super.clone();
		//… take care of any deep copies 
		another.astreamurls = (Vector<String>) this.astreamurls.clone();
		another.mstreamurls = (Vector<String>) this.mstreamurls.clone();
		another.podcasturls = (Vector<String>) this.podcasturls.clone();
		return another;
	}
	
}

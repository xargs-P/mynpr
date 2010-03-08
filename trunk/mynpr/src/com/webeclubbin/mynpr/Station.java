package com.webeclubbin.mynpr;

import java.io.Serializable;
import java.util.HashMap;
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
	//Vector<String, String> podcasturls = new Vector<String, String>();
	HashMap<String, String> podcasturls = new HashMap<String, String>();
		
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
	public void setPUrl(String url, String title) {
		//podcasturls.add(temp);
		//I am hoping the titles are unique
		//If not something will get erased. :-)
		podcasturls.put(title, url);
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
	public HashMap getPUrl() {
		//String [] temp = new String [0];
		//return (String[])podcasturls.toArray(temp);
		return podcasturls;
		/*Set<String> s = podcasturls.keySet();
		String[] t = {""} ;
		Iterator<String> it = s.iterator()
		
		while (it.hasNext()){
			if (s.toArray(t).length == 1){
				String[] temp = s.toArray(t);
				if (temp[0] == null){
					return null;
				}
			}
		} */
		//return s.toArray(t);
	}
	
	public Object clone() throws CloneNotSupportedException
	{
		Station another = (Station) super.clone();
		//… take care of any deep copies 
		another.astreamurls = (Vector<String>) this.astreamurls.clone();
		another.mstreamurls = (Vector<String>) this.mstreamurls.clone();
		another.podcasturls = (HashMap<String,String>) this.podcasturls.clone();
		return another;
	}
	
}

package com.webeclubbin.mynpr;

import java.io.Serializable;

//Radio Station Podcast Object
public class Podcast implements Cloneable, Serializable {
	/**
	 * Serial id number
	 */
	private static final long serialVersionUID = -2472794637052392218L;

	String title = null;
	String description = null;
	String pubDate = null;
	String audioUrl = null;
	String storyLink = null;
	String geoTag = null;
	String stationName = null;
	
	public final static String TITLE = "title";
	public final static String DESCRIPTION = "description";
	public final static String PUBDATE = "pubDate"; 
	public final static String AUDIOURL = "enclosure";
	public final static String STORYLINK = "link";
	public final static String GEOTAG = "georss:point";
	
	public void setTitle(String temp) {
		title = temp ;
	}
	public void setDescription(String temp) {
		description = temp ;
	}
	public void setPubDate(String temp) {
		pubDate = temp ;
	}
	public void setAudioUrl(String temp) {
		audioUrl = temp ;
	}
	public void setStoryLink(String temp) {
		storyLink = temp ;
	}
	public void setGeoTag(String temp) {
		geoTag = temp ;
	}
	public void setStation(String temp) {
		stationName = temp ;
	}
	
	public String getTitle() {
			return title;
	}
	public String getDescription() {
		return description;
	}
	public String getPubDate() {
		return pubDate;
	}
	public String getAudioUrl() {
		return audioUrl;
	}
	public String getStoryLink() {
		return storyLink;
	}
	public String getGeoTag() {
		return geoTag;
	}
	public String getStation() {
		return stationName;
	}


	
	public Object clone() throws CloneNotSupportedException
	{
		Podcast another = (Podcast) super.clone();
		//… take care of any deep copies 
		return another;
	}
	
}

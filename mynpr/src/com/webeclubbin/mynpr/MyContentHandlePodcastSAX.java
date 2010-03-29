package com.webeclubbin.mynpr;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class MyContentHandlePodcastSAX extends DefaultHandler {
	boolean isItem = false, isTitle = false, isDescription= false, isPubDate = false, isStoryLink = false, isGeoTag = false;
	final String TAG = "MyContentHandlePodcast";
	String title = "", description = "", pubdate = "", storylink = "", geotag = "";

	Vector<Podcast> podcasts = new Vector<Podcast> (50, 50);
	Podcast p = new Podcast();
	String station = null;
	int count = 0;
	
	public void setStation(String stationname){
		station = stationname;
		p.setStation(stationname);
	}
	
	public void startElement(String uri, String localName, 
		String qName, Attributes atts) {
		Log.d(TAG, "startElement "+ localName);
		if (localName.equals("item"))  {
			isItem = true; 
			count = count + 1;
		}
		if (localName.equalsIgnoreCase(Podcast.TITLE) )  {
			isTitle = true;
		}
		if (localName.equalsIgnoreCase(Podcast.DESCRIPTION) )  {
			isDescription = true;
		}
		if (localName.equalsIgnoreCase(Podcast.PUBDATE) )  {
			isPubDate = true;
		}
		if ( localName.equalsIgnoreCase(Podcast.AUDIOURL)   )  {
			String val = atts.getValue("url") ;
			Log.d(TAG, "val type: " + val);
			p.setAudioUrl(val);
		}
		if (localName.equalsIgnoreCase(Podcast.STORYLINK) )  {
			isStoryLink = true;
		}
		if (localName.equalsIgnoreCase(Podcast.GEOTAG) )  {
			isGeoTag = true;
		}

	}
	
	public void characters(char[ ] chars, int start, int length) {
		//This function can be called more than once for each node. So will need to save the contents across function calls
		if(isItem && isTitle) {
			title = title + new String(chars, start, length);
		}

		if(isItem && isDescription) {
			description = description + new String(chars, start, length);
		}
		
		if(isItem && isPubDate) {
			pubdate = pubdate + new String(chars, start, length);
		}
		
		if(isItem && isStoryLink) {
			storylink = storylink + new String(chars, start, length);
		}
		if(isItem && isGeoTag) {
			geotag = geotag + new String(chars, start, length);
		}
	}
	
	public void endElement(String uri, String localName, 
		String qName) {

		if (localName.equalsIgnoreCase("item"))  {
			isItem = false; 
			podcasts.add(p);
			resetVars();
		}
		if (localName.equalsIgnoreCase(Podcast.TITLE) )  {
			isTitle = false;
			p.setTitle(title);
		}
		if (localName.equalsIgnoreCase(Podcast.DESCRIPTION) )  {
			isDescription = false;
			p.setDescription(description);
		}
		if (localName.equalsIgnoreCase(Podcast.PUBDATE) )  {
			isPubDate = false;
			p.setPubDate(pubdate);
		}

		if (localName.equalsIgnoreCase(Podcast.STORYLINK) )  {
			isStoryLink = false;
			p.setStoryLink(storylink);
		}
		if (localName.equalsIgnoreCase(Podcast.GEOTAG) )  {
			isGeoTag = false;
			p.setGeoTag(geotag);
		}
	}

	public void endDocument (){	
		Log.d(TAG, "Podcast(s): " + count );
    }
	
	public Podcast[] getPodcasts() {
		return (Podcast[])podcasts.toArray(new Podcast[podcasts.size()]);
	}
	
	//Reset Variables
	private void resetVars(){
		title = "";
		description = "";
		pubdate = "";
		storylink = "";
		geotag = "";
		p = new Podcast();
		p.setStation(station);

	}
}

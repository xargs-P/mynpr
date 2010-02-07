package com.webeclubbin.mynpr;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class MyContentHandleSearchStationSAX extends DefaultHandler {
	boolean isStation = false, isName = false, isMarket = false, isUrl = false, isLogo = false;
	final String TAG = "MyContentHandleSearchStation";
	String name = "", market = "", url = "", urlattr = "", logo = "", urlTitle = "";
	String supernames = null, supermarkets = null, superlogos = null;
	String superurlorg = null, superurla = null, superurlmp3 = null, superurlpod = null;
	
	final String SPLITTER = "1mynpr1";
	String[] names, markets, urlorg, urla, urlmp3, urlpod, logos ;
	
	Vector<Station> stations = new Vector<Station> (7, 1);
	Station s = new Station();
	
	int count = 0;
	public void startElement(String uri, String localName, 
		String qName, Attributes atts) {
		Log.d(TAG, "startElement "+ localName);
		if (localName.equals("station"))  {
			isStation = true; 
			count = count + 1;
		}
		if (localName.equals("name") )  {
			isName = true;
		}
		if (localName.equals("marketCity") )  {
			isMarket = true;
		}
		//TODO Grab URL titles also
		if ( localName.equals("url")   )  {
			String val = atts.getValue("typeId") ;
			String valTitle = atts.getValue("title") ;
			Log.d(TAG, "val type: " + val);
			Log.d(TAG, "val title: " + valTitle);
			if ( val != null) {
				if ( val.equals(Station.ORGID) 
						|| val.equals(Station.MP3STREAMID) || val.equals(Station.PODCASTID)){
					isUrl = true;
					urlattr = val;
					if (val.equals(Station.PODCASTID)){
						urlTitle = valTitle;
					}
				} else {
					isUrl = false;
					urlattr = "";
					urlTitle = "";
				}
				if ( isUrl == true) {
					Log.d(TAG, "startElement " + localName + " " + val);
				}
				
			} else {
				isUrl = false;
			}
		}
		
		if ( localName.equals("image") && atts.getValue("type").equals("logo")  )  {
			isLogo = true;
		}
	}
	
	public void characters(char[ ] chars, int start, int length) {
		if(isStation && isName) {
			name = name + new String(chars, start, length);
		}

		if(isStation && isMarket) {
			market = market + new String(chars, start, length);
		}
		
		if(isStation && isUrl) {
			url = url + new String(chars, start, length);
		}
		
		if(isStation && isLogo) {
			logo = logo + new String(chars, start, length);
		}
	}
	
	public void endElement(String uri, String localName, 
		String qName) {
		//String orgtemp = " " , atemp = " ", mp3temp = " ", podtemp = " ";
		
		if( localName.equals("station") ) {
			isStation = false;
			
			//Some stations have no URLs.
			//So we need to check and dump them.
			if ( s.getOUrl() == null){
				Log.d(TAG, "Forget this station:" + s.getName() );
				//Erase this station
				s = new Station();
			}else {
				Station tempstation = null;
				try {
					tempstation = (Station) s.clone();
				} catch (CloneNotSupportedException e) {
					Log.e(TAG, "Trying to copy station object: " + e.toString());
				}
				stations.add( tempstation );
				s = new Station();
			}
			
		}
		
		if ( localName.equals("name") ) {
			isName = false;
			
			name = name.trim();
			if (name.equals("")){
				name = " ";
			}
			
			s.setName(name);
			Log.d(TAG, count + " name " + name);
			name = "";
		} 

		if ( localName.equals("marketCity") ) {
			isMarket = false;
			
			market = market.trim();
			if (market.equals("")){
				market = " ";
			}
			s.setMarket(market);
			Log.d(TAG, count + " market " + market);

			market = "";
		} 
		
		if ( localName.equals("url") && isUrl) {
			isUrl = false;

			url = url.trim();
			if (url.equals("")){
				url = " ";
			}
			Log.d(TAG, count + " url " + url);
			
			if ( urlattr.equals(Station.ORGID) ){
				//orgtemp = url; 
				s.setOUrl(url);
				Log.d(TAG + "end element", Station.ORG );
			} else if ( urlattr.equals(Station.ASTREAMID) ){
				//atemp = url;
				s.setAUrl(url);
				Log.d(TAG + "end element", Station.ASTREAM );
			} else if ( urlattr.equals(Station.MP3STREAMID) ){
				//mp3temp = url;
				s.setMUrl(url);
				Log.d(TAG + "end element", Station.MP3STREAM );
			} else if ( urlattr.equals(Station.PODCASTID) ){
				//podtemp = url;
				s.setPUrl(url, urlTitle);
				Log.d(TAG + "end element", Station.PODCAST );
			}

			url = "";
		} 

		if ( localName.equals("image") ) {
			isLogo = false;
			
			logo = logo.trim();
			if (logo.equals("")){
				logo = " ";
			}
			s.setLogo(logo);
			Log.d(TAG, count + " logo " + logo);
			logo = "";
		} 
	}

	public void endDocument (){	
		Log.d(TAG, "Stations: " + stations.size() );
    }
	
	public Station[] getStations() {
		return (Station[])stations.toArray(new Station[stations.size()]);
	}
}

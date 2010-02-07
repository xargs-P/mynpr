package com.webeclubbin.mynpr;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class MyContentHandlePopStoriesSAX extends DefaultHandler {
	boolean isTitle, noMoreTitles, isStory, isImage, noMoreImages, isLink = false;
	final String TAG = "MyContentHandlePopStories";
	String title = "", image = "", link = "";
	String supertitles = null, superimages = null, superlinks = null;
	final String SPLITTER = "1mynpr1";
	String[] titles, images, links;
	int count, tcount = 0;
	public void startElement(String uri, String localName, 
		String qName, Attributes atts) {
		//Log.d(TAG, "startElement " + localName );

		if (localName.equals("story"))  {
			isStory = true; 
			count = count + 1;
			Log.d(TAG, "startElement " + localName );
		}
		if (localName.equals("title") && isStory)  {
			if ( noMoreTitles == true) {
				isTitle = false; 
			} else {
				isTitle = true;
				Log.d(TAG, "startElement " + localName );
			}
		}
		if (localName.equals("image") && isStory )  {
			if ( noMoreImages == true) {
				isImage = false;
			} else {
				isImage = true;
				image = atts.getValue("src") ;
			}
		}
		if ( localName.equals("link") && isStory  )  {
			String val = atts.getValue("type") ;
			if ( val != null) {
				if ( val.equals("html")){
					isLink = true;
					Log.d(TAG, "startElement " + localName );
				}
			}
		}

	}
	
	public void characters(char[ ] chars, int start, int length) {
		if(isStory && isTitle) {
			Log.d(TAG,"found title text");
			title = title + new String(chars, start, length);
		}
		//if(isStory && isImage) {
		//	Log.d(TAG,"found image text " );
		//	image = image + new String(chars, start, length);
		//}
		if(isStory && isLink) {
			Log.d(TAG,"found link text");
			link = link + new String(chars, start, length);
		}
	}
	
	public void endElement(String uri, String localName, 
		String qName) {
		
		if(localName.equals("title") && (noMoreTitles == false) && isStory ) {
			isTitle = false;
			noMoreTitles = true;
			Log.d(TAG, count + " title " + "tcount " + tcount + title);
			
			title = title.trim();
			if (supertitles == null){
					supertitles = title;
			} else {
				supertitles = supertitles + SPLITTER + title;
			}
			title = "";
			tcount = tcount + 1;
		} else {
			title = "";
		}
		
		if (localName.equals("story")) {
			isStory = false;
			noMoreTitles = false;
			noMoreImages = false;
			//Set up images, one for each story
			image = image.trim();
			if (image.equals("")){
				image = " ";
			}
			Log.d(TAG, count + " image " + image);
			if (superimages == null){
				superimages = image;
				 
			}else{
				superimages = superimages + SPLITTER + image;
			}
			image = "";
			//Log.d(TAG, "count Story  " + count   );
			//Log.d(TAG,  superthumbs  );
			//Log.d(TAG, "count Images array" + Integer.toString(Pattern.compile(SPLITTER).split(superthumbs).length) );
			
		} 

		if (localName.equals("image") && (noMoreImages == false) && isStory )  {
			isImage = false;
			noMoreImages = true;
		}
		
		if (localName.equals("link") && isLink )  {
			isLink = false;
			Log.d(TAG, count + " Link " + link);
			if (superlinks == null){
				superlinks = link;
			}else{
				superlinks = superlinks + SPLITTER + link;
			}
			link = "";
		}

	}


	
	public void endDocument (){
		if ( supertitles != null ) {
			//URL Decode this first: URLDecoder 
			try {
				supertitles = URLDecoder.decode(supertitles,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, e.toString() );
			}
			titles = Pattern.compile(SPLITTER).split(supertitles);
		}
		if ( superimages != null){
			images = Pattern.compile(SPLITTER).split(superimages);
		}
		if ( superlinks != null){
			links = Pattern.compile(SPLITTER).split(superlinks);
		}
		
		//Log.d(TAG, supertitles );
		//Log.d(TAG, superimages );
		//Log.d(TAG, superlinks);
		
		Log.d(TAG, "count Titles array" + Integer.toString(titles.length) );
		Log.d(TAG, "count Images array" + Integer.toString(images.length) );
		Log.d(TAG, "count Links array" + Integer.toString(links.length) );
		Log.d(TAG, "count Story  " + Integer.toString(count) );
    }
	
	public String[] getTitles() {
		if (supertitles != null){
			return titles;
		} else {
			return null;
		}
	}
	public String[] getImages() {
		if (superimages != null){
			return images;
		} else {
			return null;
		}
	}
	public String[] getLinks() {
		if (superlinks != null){
			return links;
		} else {
			return null;
		}
	}
}

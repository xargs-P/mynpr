package com.webeclubbin.mynpr;


//Holds the information for a URL to play 
public class PlayURL  {
	private String url ;
	private String station;
	private String logo ;
	private boolean isrss ;
	private String title;
	
	public void setURL (String u){
		url = u;
	}
	public void setStation (String s){
		station = s;
	}
	public void setLogo (String l){
		logo = l;
	}
	public void setRSS (boolean r){
		isrss = r;
	}
	public void setTitle (String t){
		title = t;
	}

	public String getURL (){
		return url;
	}
	public String getStation (){
		return station;
	}
	public String getLogo(){
		return logo;
	}
	public boolean isRSS (){
		return isrss;
	}
	public String getTitle (){
		return title;
	}

}

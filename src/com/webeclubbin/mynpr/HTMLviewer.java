package com.webeclubbin.mynpr;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class HTMLviewer extends Activity {

	WebView webview;
	final int MENU_SEND = 0;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        String TAG = "onCreate - HTMLviewer";
        
        Intent intent=getIntent();
        Uri uri=intent.getData();
        if ( uri == null ) {
        	Log.e(TAG, "uri null");
        }
        Log.i(TAG, uri.toString());
        
        webview = (WebView) findViewById(R.id.webview);
        webview.setWebViewClient(new InternalWebViewClient());
        
        WebSettings settings = webview.getSettings();
       	settings.setJavaScriptEnabled(true);

       	Log.i(TAG, "Load url");
        webview.loadUrl(uri.toString());
    }

    //Set up client since npr.org will do a redirect for mobile devices
    //This class will catch redirects and other links the user selects
    private class InternalWebViewClient extends WebViewClient {

    	@Override
    	public boolean shouldOverrideUrlLoading(WebView view, String url) {
    		String TAG = "shouldOverrideUrlLoading";
    		Log.i(TAG, "We have a new url to go to: " + url);
    		view.loadUrl(url);
    		return true;
    	}
	}
    
    //In case the user presses the back button they will get the last web page.
    public boolean onKeyDown(int keyCode, KeyEvent event) { 
    	String TAG = "web keydown";
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) { 
        	Log.i(TAG, "Go back to previous webpage");
            webview.goBack(); 
            return true; 
        } 
        return super.onKeyDown(keyCode, event); 
    }
    
    //Send url in a message
    private void sendtofriend () {
    	
        Intent i = null;
        String oururl = null;
        String TAG = "Send URL in Message";
        
    	oururl = webview.getOriginalUrl();
    	i = new Intent(Intent.ACTION_SEND ); 
    	i.setType("text/plain");

    	i.putExtra(Intent.EXTRA_TEXT, webview.getOriginalUrl() );
    	i.putExtra(Intent.EXTRA_SUBJECT, webview.getTitle() + " [From myNPR]");
       
        //launch intent
        Log.i(TAG, "Start Activity");
        //startActivity(i);
        startActivity(Intent.createChooser(i, "Send to a Friend..."));
    }
    
    /** Set up Menu for this view */
    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
    	menu.add(0, MENU_SEND, Menu.NONE, "Send to Friend").setIcon(com.webeclubbin.mynpr.R.drawable.mail);;
        return true;
    }
    
    /* Handles item selections */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_SEND:
        	sendtofriend();
            return true;
        }
        return false;
    }
}

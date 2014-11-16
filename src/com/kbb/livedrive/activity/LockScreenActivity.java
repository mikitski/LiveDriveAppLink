package com.kbb.livedrive.activity;

import com.kbb.livedrive.R;
import com.kbb.livedrive.applink.AppLinkService;
import com.kbb.livedrive.artifact.Location;
import com.kbb.livedrive.googleplay.GooglePlayService;
import com.kbb.livedrive.vehicledata.DriverScoreService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;


public class LockScreenActivity extends Activity {
	private static LockScreenActivity instance;
	
	private final double PIC_WIDTH = 1920;
	
	private WebView lockscreenView;
	
	final BroadcastReceiver scoreChangedReceiver = new BroadcastReceiver(){
		public void onReceive(android.content.Context context, Intent intent) {
			
			long driverScore = intent.getLongExtra("driverScore", 50);
			long mpgScore = intent.getLongExtra("mpgScore", 50);
			
			//TODO call java script to update the score display on the screen
		};
	};
	
	final BroadcastReceiver realTimeMpgScoreChangedReceiver = new BroadcastReceiver(){
		public void onReceive(android.content.Context context, Intent intent) {
			
			long mpgScore = intent.getLongExtra("mpgScore", 50);
			
			//TODO call java script to update the score display on the screen
			lockscreenView.loadUrl(String.format("javascript:(drawMpgScore(%s,%s,%s))", mpgScore, mpgScore, "\"--\""));
		};
	};
	
	final BroadcastReceiver realTimeDriverScoreChangedReceiver = new BroadcastReceiver(){
		public void onReceive(android.content.Context context, Intent intent) {
			
			long driverScore = intent.getLongExtra("driverScore", 50);
		
			//TODO call java script to update the score display on the screen
			lockscreenView.loadUrl(String.format("javascript:(drawDriverScore(%s,%s,%s))", driverScore, driverScore, "\"--\""));
		};
	};



	static {
		LockScreenActivity.instance = null;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lockscreen);
		LockScreenActivity.instance = this;

		LocalBroadcastManager lbManager = LocalBroadcastManager.getInstance(this);
        lbManager.registerReceiver(scoreChangedReceiver, new IntentFilter(DriverScoreService.ACTION_SCORE_CHANGED));
        lbManager.registerReceiver(realTimeMpgScoreChangedReceiver, new IntentFilter(DriverScoreService.ACTION_RT_MPG_SCORE_CHANGED));
        lbManager.registerReceiver(realTimeDriverScoreChangedReceiver, new IntentFilter(DriverScoreService.ACTION_RT_DRIVER_SCORE_CHANGED));

    	lockscreenView = (WebView) findViewById(R.id.lockscreenView);
    	lockscreenView.getSettings().setJavaScriptEnabled(true);
    	lockscreenView.setWebViewClient(Client);
    	
    	lockscreenView.setPadding(0, 0, 0, 0);
    	lockscreenView.setInitialScale(getScale());

		
    	lockscreenView.loadUrl("file:///android_asset/lockscreen.html");
		
    }
    
	private int getScale(){
	    Double val = new Double(PIC_WIDTH)/new Double(PIC_WIDTH);
	    val = val * 100d;
	    return val.intValue();
	}

    
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
    	
    	    	
    	return super.onCreateView(name, context, attrs);
    }

    // Disable back button on lockscreens
    @Override
    public void onBackPressed() {
    }

    @Override
    public void onDestroy() {
		LockScreenActivity.instance = null;
		super.onDestroy();
    }

    public void exit() {
    	super.finish();
    }

    public static LockScreenActivity getInstance() {
    	return instance;
    }
    
    
	final WebViewClient Client = new WebViewClient () {

		@Override
	    public void onPageFinished(WebView view, String url) {	
			
			if(url.equals("file:///android_asset/lockscreen.html")){
				lockscreenView.loadUrl(String.format("javascript:(drawDriverScore(%s,%s,%s))", 74, 74, "\"--\""));
				lockscreenView.loadUrl(String.format("javascript:(drawMpgScore(%s,%s,%s))", 86, 86, "\"--\""));
			}
			
	    }
	};

}

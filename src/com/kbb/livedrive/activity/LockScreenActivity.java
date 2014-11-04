package com.kbb.livedrive.activity;

import com.kbb.livedrive.R;
import com.kbb.livedrive.artifact.Location;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;


public class LockScreenActivity extends Activity {
	private static LockScreenActivity instance;
	private Location currentLocation = null;
	private TextView locationTextView = null;

	static {
		LockScreenActivity.instance = null;
	}

	/**
	 * Receiver for changes in location from the app UI.
	 */
	protected final BroadcastReceiver changeLocationReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
        }
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lockscreen);
		LockScreenActivity.instance = this;
		locationTextView = (TextView) findViewById(R.id.locationTextView);
		LocalBroadcastManager.getInstance(this).registerReceiver(changeLocationReceiver, new IntentFilter("com.kbb.livedrive.Location"));
    }

    // Disable back button on lockscreens
    @Override
    public void onBackPressed() {
    }

    @Override
    public void onDestroy() {
		LockScreenActivity.instance = null;
		LocalBroadcastManager.getInstance(this).unregisterReceiver(changeLocationReceiver);
		super.onDestroy();
    }

    public void exit() {
    	super.finish();
    }

    public static LockScreenActivity getInstance() {
    	return instance;
    }

    private void updateLocation() {
        if (currentLocation != null && currentLocation.city != null && currentLocation.state != null) {
        	if (locationTextView != null) {
        		locationTextView.setText(currentLocation.city + ", " + currentLocation.state);
        	}
        }
    }
}

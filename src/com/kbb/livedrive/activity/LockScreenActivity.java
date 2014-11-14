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


	static {
		LockScreenActivity.instance = null;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lockscreen);
		LockScreenActivity.instance = this;

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

}

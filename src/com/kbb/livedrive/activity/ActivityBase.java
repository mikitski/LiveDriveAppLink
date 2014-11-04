package com.kbb.livedrive.activity;

import com.kbb.livedrive.app.LiveDriveApplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class ActivityBase extends Activity {
	private boolean activityOnTop;
	
	/**
	 * Activity is moving to the foreground.
	 * Set this activity as the current activity and that it is on top.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		LiveDriveApplication.setCurrentActivity(this);
		activityOnTop = true;
	}
	
	/**
	 * Activity becoming partially visible (obstructed by another).
	 * Activity if no longer on top.
	 */
	@Override
	protected void onPause() {
		activityOnTop = false;
		super.onPause();
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	/**
	 * Activity is no longer visible on the screen.
	 */
	@Override
	protected void onStop() {
    	super.onStop();		
	}
	
    @Override
	protected void onDestroy() {
		// Set the current activity to null if no other activity has taken the foreground.
		if (LiveDriveApplication.getCurrentActivity() == this) {
			LiveDriveApplication.setCurrentActivity(null);
		}
    	super.onDestroy();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	return true;	            
	}
    
	public boolean isActivityonTop(){
		return activityOnTop;
	}
}

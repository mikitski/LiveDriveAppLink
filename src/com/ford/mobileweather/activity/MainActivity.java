package com.ford.mobileweather.activity;

import com.ford.mobileweather.R;
import com.ford.mobileweather.app.LiveDriveApplication;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class MainActivity extends ActivityBase implements ActionBar.TabListener {

	private static final String SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	/**
	 * Receiver to handle updates to weather conditions.
	 */
	private final BroadcastReceiver weatherConditionsReceiver = new BroadcastReceiver() {
		
        @Override
        public void onReceive(Context context, Intent intent) {
        }
	};

	/**
	 * Receiver to handle updates to the forecast.
	 */
	private final BroadcastReceiver forecastReceiver = new BroadcastReceiver() {
		
        @Override
        public void onReceive(Context context, Intent intent) {
        }
	};
		
	/**
	 * Receiver for changes in location from the app UI.
	 */
	protected final BroadcastReceiver changeLocationReceiver = new BroadcastReceiver() {
		
        @Override
        public void onReceive(Context context, Intent intent) {
        }
	};
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        LocalBroadcastManager lbManager = LocalBroadcastManager.getInstance(this);
        lbManager.registerReceiver(changeLocationReceiver, new IntentFilter("com.ford.mobileweather.Location"));
        lbManager.registerReceiver(weatherConditionsReceiver, new IntentFilter("com.ford.mobileweather.WeatherConditions"));
        lbManager.registerReceiver(forecastReceiver, new IntentFilter("com.ford.mobileweather.Forecast"));
        
		LiveDriveApplication app = LiveDriveApplication.getInstance();
		if (app != null) {
			app.startServices();
		}

		// Create tabs
		ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		bar.addTab(bar.newTab().setText(R.string.title_stats).setTabListener(this));
		bar.addTab(bar.newTab().setText(R.string.title_friends).setTabListener(this));
		bar.setSelectedNavigationItem(0);
		
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    /** 
     * Called whenever we call invalidateOptionsMenu()
     * */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }
    
    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    return super.onOptionsItemSelected(item);
	}
    
    @Override
	protected void onResume() {
		super.onResume();
    }
    
	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		try {
			LocalBroadcastManager lbManager = LocalBroadcastManager.getInstance(this);
			lbManager.unregisterReceiver(changeLocationReceiver);
			lbManager.unregisterReceiver(weatherConditionsReceiver);
			lbManager.unregisterReceiver(forecastReceiver);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

		LiveDriveApplication app = LiveDriveApplication.getInstance();
		if (app != null) {
			LiveDriveApplication.setCurrentActivity(null);
			app.stopServices();
		}

		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
	    moveTaskToBack(true);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent e) {
	    if (keyCode == KeyEvent.KEYCODE_MENU) {
	        return true;
	    }
	    return super.onKeyDown(keyCode, e);
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState.containsKey(SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(SELECTED_NAVIGATION_ITEM));
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(SELECTED_NAVIGATION_ITEM, getActionBar().getSelectedNavigationIndex());
	}

}

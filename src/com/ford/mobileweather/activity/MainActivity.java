package com.ford.mobileweather.activity;

import com.ford.mobileweather.R;
import com.ford.mobileweather.app.MobileWeatherApplication;

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

    private Fragment currentFragment;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
	private String[] drawerTitles;
	private ArrayAdapter<String> drawerAdapter;
    private CharSequence drawerTitle;

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
	
	/**
	 * Drawer item click listener that handles menu actions in the navigation drawer.
	 */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
	    @Override
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	        selectItem(position);
	    }
	}
	
    private void selectItem(int position) {
    	String item = drawerAdapter.getItem(position);
    	
    	if ("Update Weather".equals(item)) {
    			Intent intent = new Intent("com.ford.mobileweather.WeatherUpdate");
    			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    	}
    	else if ("About".equals(item)) {
    		MobileWeatherApplication.getInstance().showAppVersion(this);
    	}
    	drawerList.setItemChecked(position, false);
        drawerLayout.closeDrawer(drawerList);
    }
		
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        LocalBroadcastManager lbManager = LocalBroadcastManager.getInstance(this);
        lbManager.registerReceiver(changeLocationReceiver, new IntentFilter("com.ford.mobileweather.Location"));
        lbManager.registerReceiver(weatherConditionsReceiver, new IntentFilter("com.ford.mobileweather.WeatherConditions"));
        lbManager.registerReceiver(forecastReceiver, new IntentFilter("com.ford.mobileweather.Forecast"));
        
		MobileWeatherApplication app = MobileWeatherApplication.getInstance();
		if (app != null) {
			app.startServices();
		}

		// Create tabs
		ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		bar.addTab(bar.newTab().setText(R.string.title_conditions).setTabListener(this));
		bar.addTab(bar.newTab().setText(R.string.title_forecast).setTabListener(this));
		bar.setSelectedNavigationItem(0);
		
		// Init Drawer list
        drawerTitle = getTitle();
		drawerTitles = getResources().getStringArray(R.array.nav_drawer_items);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerAdapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawerTitles);

        // Set the adapter for the list view
        drawerList.setAdapter(drawerAdapter);
        // Set the list's click listener
        drawerList.setOnItemClickListener(new DrawerItemClickListener());
        
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(drawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(drawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
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
        // If the nav drawer is open, hide action items related to the content view
        /*boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);*/
        return super.onPrepareOptionsMenu(menu);
    }
    
    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

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

		MobileWeatherApplication app = MobileWeatherApplication.getInstance();
		if (app != null) {
			MobileWeatherApplication.setCurrentActivity(null);
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
	        if (!drawerLayout.isDrawerOpen(drawerList)) {
	            drawerLayout.openDrawer(drawerList);
	        }
	        else {
	        	drawerLayout.closeDrawer(drawerList);
	        }
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

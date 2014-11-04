package com.kbb.livedrive.activity;

import com.kbb.livedrive.R;
import com.kbb.livedrive.app.LiveDriveApplication;

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

import com.google.android.gms.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameUtils;



public class MainActivity extends ActivityBase implements 
			ActionBar.TabListener, 
			GoogleApiClient.ConnectionCallbacks,
			GoogleApiClient.OnConnectionFailedListener {

	private static final String SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	private static int RC_SIGN_IN = 9001;

	private boolean mResolvingConnectionFailure = false;
	private boolean mAutoStartSignInFlow = true;
	private boolean mSignInClicked = false;
	
	private LiveDriveApplication app;
		
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
        
        app = LiveDriveApplication.getInstance();
        
     // Create the Google Api Client with access to Plus and Games
        app.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                // add other APIs and scopes here as needed
                .build();
        
        setContentView(R.layout.activity_main);
        
        LocalBroadcastManager lbManager = LocalBroadcastManager.getInstance(this);
        lbManager.registerReceiver(changeLocationReceiver, new IntentFilter("com.kbb.livedrive.Location"));
        lbManager.registerReceiver(forecastReceiver, new IntentFilter("com.kbb.livedrive.Forecast"));
        
		
		if (app != null) {
			app.startServices();
		}

		// Create tabs
		ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		bar.addTab(bar.newTab().setText(R.string.title_stats).setTabListener(this));
		bar.addTab(bar.newTab().setText(R.string.title_settings).setTabListener(this));
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

	
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
	    if (mResolvingConnectionFailure) {
	        // already resolving
	        return;
	    }

	    // if the sign-in button was clicked or if auto sign-in is enabled,
	    // launch the sign-in flow
	    if (mSignInClicked || mAutoStartSignInFlow) {
	        mAutoStartSignInFlow = false;
	        mSignInClicked = false;
	        mResolvingConnectionFailure = true;

	        // Attempt to resolve the connection failure using BaseGameUtils.
	        // The R.string.signin_other_error value should reference a generic
	        // error string in your strings.xml file, such as "There was
	        // an issue with sign-in, please try again later."
	        if (!BaseGameUtils.resolveConnectionFailure(this,
	                app.mGoogleApiClient, connectionResult,
	                RC_SIGN_IN, getString(R.string.signin_other_error))) {
	            mResolvingConnectionFailure = false;
	        }
	    }
	}
	
	protected void onActivityResult(int requestCode, int resultCode,
	        Intent intent) {
	    if (requestCode == RC_SIGN_IN) {
	        mSignInClicked = false;
	        mResolvingConnectionFailure = false;
	        if (resultCode == RESULT_OK) {
	            app.mGoogleApiClient.connect();
	        } else {
	            // Bring up an error dialog to alert the user that sign-in
	            // failed. The R.string.signin_failure should reference an error
	            // string in your strings.xml file that tells the user they
	            // could not be signed in, such as "Unable to sign in."
	            BaseGameUtils.showActivityResultError(this, requestCode,
	                resultCode, R.string.signin_failure,
	                R.string.signin_other_error);
	        }
	    }
	}
		

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	    app.mGoogleApiClient.connect();
	    
	}
	
	@Override
	protected void onStop() {
	    super.onStop();
	    if (app.mGoogleApiClient.isConnected()) {
	        app.mGoogleApiClient.disconnect();
	    }
	}
	
	

}

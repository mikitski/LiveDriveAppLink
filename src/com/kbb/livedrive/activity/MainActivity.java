package com.kbb.livedrive.activity;

import com.kbb.livedrive.R;
import com.kbb.livedrive.app.LiveDriveApplication;
import com.kbb.livedrive.applink.AppLinkService;
import com.kbb.livedrive.fragments.WebViewFragment;
import com.kbb.livedrive.fragments.VehicleSummaryFragment;
import com.kbb.livedrive.googleplay.GooglePlayService;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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

	private CharSequence drawerTitle;
	private String[] drawerTitles;
	private DrawerLayout drawerLayout;
	private ListView drawerList;
	private ArrayAdapter<String> drawerAdapter;

	private Fragment currentFragment;
	
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
    	Fragment fragment = null;
    	
    	if("Home".equals(item)){
    		fragment = new WebViewFragment();

    	}
    	else if ("Vehicle".equals(item)) {
    		// acrivate VehicleInfo fragment
    		fragment = new VehicleSummaryFragment();
    	}
    	else if("Driver Leadeboard".equals(item)){
    		
    		GooglePlayService svc = GooglePlayService.getInstance();
    		startActivityForResult(Games.Leaderboards.getLeaderboardIntent(svc.mGoogleApiClient,getString(R.string.leaderboard_good_driver)), 1977);    

    	}
    	else if ("MPG Leadeboard".equals(item)) {
    		GooglePlayService svc = GooglePlayService.getInstance();
    		startActivityForResult(Games.Leaderboards.getLeaderboardIntent(svc.mGoogleApiClient,getString(R.string.leaderboard_mpg)), 1978);    
    	
    	}
    	else if ("Reset".equals(item)){
    		AppLinkService.getInstance().reset();
    	}
    	else if ("About".equals(item)) {
    		LiveDriveApplication.getInstance().showAppVersion(this);
    	}
    	
    	if(fragment != null){
			currentFragment = fragment;
			getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();    		
    	}
    	
    	drawerList.setItemChecked(position, false);
        drawerLayout.closeDrawer(drawerList);
    }
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        
        app = LiveDriveApplication.getInstance();
        LiveDriveApplication.setCurrentActivity(this);
        
		// Start Services - AppLink, GooglePlay, DriverScore
		if (app != null) {
			app.startServices();
		}

		//Fragment fragment = new WebViewFragment();
		//getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
		
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
        
        LocalBroadcastManager lbManager = LocalBroadcastManager.getInstance(this);
        lbManager.registerReceiver(changeLocationReceiver, new IntentFilter("com.kbb.livedrive.Location"));
        lbManager.registerReceiver(forecastReceiver, new IntentFilter("com.kbb.livedrive.Forecast"));
		
        
		// Create tabs
		ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		bar.addTab(bar.newTab()
				.setText(R.string.title_scores)
				.setTabListener(new TabListener<WebViewFragment>(
						this, getText(R.string.title_scores).toString(), WebViewFragment.class)));

		bar.addTab(bar.newTab()
				.setText(R.string.title_vehicle)
				.setTabListener(new TabListener<VehicleSummaryFragment>(
						this, getText(R.string.title_vehicle).toString(), VehicleSummaryFragment.class)));

		if (savedInstanceState != null) {
			bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
		}
		
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

	}
	
	protected void onActivityResult(int requestCode, int resultCode,
	        Intent intent) {
	    if (requestCode == RC_SIGN_IN) {
	        mSignInClicked = false;
	        mResolvingConnectionFailure = false;
	        if (resultCode == RESULT_OK) {
	    		GooglePlayService gp = GooglePlayService.getInstance();
	    		if(gp != null)
	    			gp.connect();
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

		
	}

	@Override
	public void onConnectionSuspended(int arg0) {

		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
//	    app.mGoogleApiClient.connect();
		GooglePlayService gp = GooglePlayService.getInstance();
		if(gp != null)
			gp.connect();
	    
	}
	
	@Override
	protected void onStop() {
	    super.onStop();
	    
		GooglePlayService gp = GooglePlayService.getInstance();
		if(gp != null)
			gp.disconnect();
	}
	
    public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private final Activity mActivity;
        private final String mTag;
        private final Class<T> mClass;
        private final Bundle mArgs;
        private Fragment mFragment;

        public TabListener(Activity activity, String tag, Class<T> clz) {
            this(activity, tag, clz, null);
        }

        public TabListener(Activity activity, String tag, Class<T> clz, Bundle args) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
            mArgs = args;

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            mFragment = mActivity.getFragmentManager().findFragmentByTag(mTag);
            if (mFragment != null && !mFragment.isDetached()) {
                FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
                ft.hide(mFragment);
                ft.commit();
            }
        }

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            if (mFragment == null) {
                mFragment = Fragment.instantiate(mActivity, mClass.getName(), mArgs);
                //ft.add(android.R.id.content, mFragment, mTag);
                ft.add(R.id.content_frame, mFragment, mTag);
            } else {
                ft.show(mFragment);
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                ft.hide(mFragment);
            }
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            //Toast.makeText(mActivity, "Reselected!", Toast.LENGTH_SHORT).show();
        }
    }	
}

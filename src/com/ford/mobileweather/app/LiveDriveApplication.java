package com.ford.mobileweather.app;

import com.ford.mobileweather.applink.AppLinkService;
import com.ford.syncV4.proxy.SyncProxyALM;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class LiveDriveApplication extends Application {
	
	public static final String TAG = "LIVEDrive";
	private static LiveDriveApplication instance;
	private static Activity currentUIActivity;

	
	static {
		instance = null;
	}
	
	private static synchronized void setInstance(LiveDriveApplication app) {
		instance = app;
	}
	
	public static synchronized LiveDriveApplication getInstance() {
		return instance;
	}
	
	public static synchronized void setCurrentActivity(Activity act) {
		currentUIActivity = act;
	}
	
	public static synchronized Activity getCurrentActivity() {
		return currentUIActivity;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		LiveDriveApplication.setInstance(this);

	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}
	
    public void startWeatherUpdates() {

    }
    
    public void endWeatherUpdates() {
    }
    
    
    public void startSyncProxyService() {
        // Get the local Bluetooth adapter
        //BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        // If BT adapter exists, is enabled, and there are paired devices, start service/proxy
        //if (btAdapter != null)
		//{
			//if ((btAdapter.isEnabled() && btAdapter.getBondedDevices().isEmpty() == false)) 
			//{
	        	Intent startIntent = new Intent(this, AppLinkService.class);
	        	startService(startIntent);
			//}
		//}
	}

    // Recycle the proxy
	public void endSyncProxyInstance() {
		AppLinkService serviceInstance = AppLinkService.getInstance();
		if (serviceInstance != null){
			SyncProxyALM proxyInstance = serviceInstance.getProxy();
			// if proxy exists, reset it
			if(proxyInstance != null){
				serviceInstance.reset();
			// if proxy == null create proxy
			} else {
				serviceInstance.startProxy();
			}
		}
	}

	// Stop the AppLinkService
	public void endSyncProxyService() {
		AppLinkService serviceInstance = AppLinkService.getInstance();
		if (serviceInstance != null){
			serviceInstance.stopService();
		}
	}
    
    public void startServices() {
        startWeatherUpdates();
        startSyncProxyService();
    }
    
    public void stopServices() {
    	if (currentUIActivity == null) {
    		endWeatherUpdates();
    	}
    }

	public void showAppVersion(Context context) {
		String appMessage = "LIVEDrive Version Info not available";
		try {
			appMessage = "LIVEDrive Version: " +
						  getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Log.d(LiveDriveApplication.TAG, "Can't get package info", e);
		}
		
		new AlertDialog.Builder(context).setTitle("App Version Information")
									 .setMessage(appMessage)
									 .setNeutralButton(android.R.string.ok, null).create().show();
	}
}

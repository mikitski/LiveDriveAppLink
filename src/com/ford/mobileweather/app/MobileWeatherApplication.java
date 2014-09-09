package com.ford.mobileweather.app;

import com.ford.mobileweather.applink.AppLinkService;
import com.ford.syncV4.proxy.SyncProxyALM;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class MobileWeatherApplication extends Application {
	
	public static final String TAG = "MobileWeather";
	private static MobileWeatherApplication instance;
	private static Activity currentUIActivity;

	
	static {
		instance = null;
	}
	
	private static synchronized void setInstance(MobileWeatherApplication app) {
		instance = app;
	}
	
	public static synchronized MobileWeatherApplication getInstance() {
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
		MobileWeatherApplication.setInstance(this);

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
		String appMessage = "MobileWeather Version Info not available";    		    		    		
		try {
			appMessage = "MobileWeather Version: " + 
						  getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Log.d(MobileWeatherApplication.TAG, "Can't get package info", e);
		}
		
		new AlertDialog.Builder(context).setTitle("App Version Information")
									 .setMessage(appMessage)
									 .setNeutralButton(android.R.string.ok, null).create().show();
	}
}

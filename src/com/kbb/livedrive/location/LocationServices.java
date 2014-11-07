package com.kbb.livedrive.location;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.kbb.livedrive.app.LiveDriveApplication;
import com.kbb.livedrive.artifact.GPSLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class LocationServices implements
	LocationListener,
	GooglePlayServicesClient.ConnectionCallbacks,
	GooglePlayServicesClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
	
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int UPDATE_INTERVAL_IN_SECONDS = 30;
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 10;
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    private static final double MIN_TIME_BETWEEN_LOCATION_UPDATES = 30.0;
    
    // Define an object that holds accuracy and frequency parameters
    private LocationRequest locationRequest;
    private LocationClient locationClient;
    private Geocoder geocoder;
    private boolean updatesRequested;
    private LiveDriveApplication applicationContext;
//    private WeatherDataManager dataManager;
    protected long lastLocationTime = 0;

    public LocationServices() {
    	applicationContext = LiveDriveApplication.getInstance();
    	// Create the LocationRequest object
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationClient = new LocationClient(applicationContext, this, this);
        geocoder = new Geocoder(applicationContext, Locale.getDefault());
        updatesRequested = false;
//        dataManager = WeatherDataManager.getInstance();
    }

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			PowerManager powerManager = (PowerManager) applicationContext.getSystemService(Context.POWER_SERVICE);
			WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MobileWeatherLocationChanged");

			try {
				wakeLock.acquire();
				Intent intent = new Intent("com.kbb.livedrive.Location");
				com.kbb.livedrive.artifact.Location loc = processLocation(location);
				
				if (loc != null) {
//					if (dataManager != null) {
//						dataManager.setCurrentLocation(loc);
//					}
			        	if ((System.currentTimeMillis() - lastLocationTime) / 1000.0 > MIN_TIME_BETWEEN_LOCATION_UPDATES) {
			        		LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent);
			        		lastLocationTime = System.currentTimeMillis();
					}
				}
			} finally {
				wakeLock.release();
			}
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(LiveDriveApplication.getCurrentActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            showErrorDialog(connectionResult.getErrorCode());
        }
	}

	@Override
	public void onConnected(Bundle arg0) {
		getLastKnownLocation();
		if (updatesRequested) {
            locationClient.requestLocationUpdates(locationRequest, this);
        }
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
	}
	
	public void start() {
		updatesRequested = true;
		if (!locationClient.isConnected()) {
			locationClient.connect();
		}
	}
	
	public void stop() {
		// If the client is connected
        if (locationClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
            locationClient.removeLocationUpdates(this);
        }
        /*
         * After disconnect() is called, the client is
         * considered "dead".
         */
        locationClient.disconnect();
        updatesRequested = false;
	}
	
	public void getLastKnownLocation() {
		if (locationClient.isConnected()) {
			Location location = locationClient.getLastLocation();
			com.kbb.livedrive.artifact.Location loc = processLocation(location);
//			if (dataManager != null && loc != null) {
//				dataManager.setCurrentLocation(loc);
//			}
			if(loc != null) {
				Intent intent = new Intent("com.kbb.livedrive.Location");
				LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent);
			}
		}
	}
	
	public com.kbb.livedrive.artifact.Location processLocation(Location location) {
		if (location != null) {
			PowerManager powerManager = (PowerManager) applicationContext.getSystemService(Context.POWER_SERVICE);
			WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MobileWeatherProcessLocation");

			try {
				wakeLock.acquire();
				com.kbb.livedrive.artifact.Location loc = null;
				
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent()) {
					loc = reverseGeocode(location);
				}
				
				if (loc == null) {
					loc = new com.kbb.livedrive.artifact.Location();
					GPSLocation gpsLoc = new GPSLocation();
					gpsLoc.latitude = String.valueOf(location.getLatitude());
					gpsLoc.longitude = String.valueOf(location.getLongitude());
					loc.gpsLocation = gpsLoc;
				}

				return loc;
			} finally {
				wakeLock.release();
			}
		}
		return null;
	}
	
	private com.kbb.livedrive.artifact.Location reverseGeocode(Location location) {
        List<Address> addresses = null;
        
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e1) {
        	Log.e(LiveDriveApplication.TAG, "IO Exception in getFromLocation()");
        	e1.printStackTrace();
        	return null;
        } catch (IllegalArgumentException e2) {
        	Log.e(LiveDriveApplication.TAG, "Illegal Argument Exception in getFromLocation()");
        	e2.printStackTrace();
        	return null;
        }
        // If the reverse geocode returned an address
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
        	com.kbb.livedrive.artifact.Location resolvedLoc = new com.kbb.livedrive.artifact.Location();
        	resolvedLoc.gpsLocation = new GPSLocation();
        	resolvedLoc.gpsLocation.latitude = String.valueOf(location.getLatitude());
        	resolvedLoc.gpsLocation.longitude = String.valueOf(location.getLongitude());
            resolvedLoc.city = address.getLocality();
            resolvedLoc.state = address.getAdminArea();
            resolvedLoc.zipCode = address.getPostalCode();
            return resolvedLoc;
        } else {
            return null;
        }
	}
		
    /**
     * Show a dialog returned by Google Play services for the
     * connection error code
     *
     * @param errorCode An error code returned from onConnectionFailed
     */
    private void showErrorDialog(int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
            errorCode,
            LiveDriveApplication.getCurrentActivity(),
            CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {
        	errorDialog.show();
        }
    }
}

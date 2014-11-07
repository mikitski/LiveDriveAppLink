package com.kbb.livedrive.googleplay;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.google.android.gms.games.leaderboard.*;


import com.kbb.livedrive.R;
import com.kbb.livedrive.app.LiveDriveApplication;
import com.kbb.livedrive.applink.AppLinkService;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

public class GooglePlayService extends Service implements 
							GoogleApiClient.ConnectionCallbacks,
							GoogleApiClient.OnConnectionFailedListener{
	
	public static final String GOOD_DRIVER_LEADERBOARD = "CgkIxNLeo8UREAIQAQ";

	private static final int REQUEST_LEADERBOARD = 1977;

	private static GooglePlayService instance = null;
	
	public GoogleApiClient mGoogleApiClient;

	private LiveDriveApplication app; 
	
	
	private static int RC_SIGN_IN = 9001;
	private boolean mResolvingConnectionFailure = false;
	private boolean mAutoStartSignInFlow = true;
	private boolean mSignInClicked = false;
	
	
	@Override
	public void onCreate() {
		instance = this;
		
		app = LiveDriveApplication.getInstance();
		
        mGoogleApiClient = new GoogleApiClient.Builder(this)
        	.addConnectionCallbacks(this)
        	.addOnConnectionFailedListener(this)
        	.addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
        	.addApi(Games.API).addScope(Games.SCOPE_GAMES)
        // add other APIs and scopes here as needed
        	.build();

		
	};
	
	@Override
	public void onStart(Intent intent, int startId) {
	    mGoogleApiClient.connect();
	};
	
	@Override
	public boolean stopService(Intent name) {
		this.disconnect();
		
		return super.stopService(name);
	};
		
	
	public static synchronized GooglePlayService getInstance() {
		return instance;
	}	
	

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// TODO Auto-generated method stub
		
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
	        if (!BaseGameUtils.resolveConnectionFailure(LiveDriveApplication.getCurrentActivity(),
	                mGoogleApiClient, connectionResult,
	                RC_SIGN_IN, app.getString(R.string.signin_other_error))) {
	            mResolvingConnectionFailure = false;
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
	
	public void submitDriverScore(long driverScore){
 
		if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
		    // Call a Play Games services API method, for example:
		    Games.Leaderboards.submitScore(mGoogleApiClient, GOOD_DRIVER_LEADERBOARD, driverScore);
		} else {
		    // Alternative implementation (or warn user that they must
		    // sign in to use this feature)
		}

	}

	public void connect() {

		if(mGoogleApiClient != null && !mGoogleApiClient.isConnected())
			mGoogleApiClient.connect();
	}

	public void disconnect() {
	    if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
	        mGoogleApiClient.disconnect();
	    }
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void showGoodDriverLeaderboard() {
	}

}

package com.kbb.livedrive.googleplay;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.google.android.gms.games.leaderboard.*;
import com.google.android.gms.games.leaderboard.Leaderboards.LeaderboardMetadataResult;
import com.google.android.gms.games.leaderboard.Leaderboards.LoadPlayerScoreResult;
import com.google.android.gms.games.leaderboard.Leaderboards.LoadScoresResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;


import com.kbb.livedrive.R;
import com.kbb.livedrive.app.LiveDriveApplication;
import com.kbb.livedrive.applink.AppLinkService;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class GooglePlayService extends Service implements 
							GoogleApiClient.ConnectionCallbacks,
							GoogleApiClient.OnConnectionFailedListener{
	

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
        
        Log.i("GooglePlayService", "started");

		
	};
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		connect();
		
		//Hack: submit some low scores to init leaderboards
		submitDriverScore(51);
		submitMPGScore(51);
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	
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
		
		
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		
		Log.e("GooglePlayService", "Connection Suspended");
		
	}
	
	
	public void submitDriverScore(long driverScore){
 
		if(mGoogleApiClient != null){
			
			connect();
			
			try{

		    // Call a Play Games services API method, for example:
				Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderboard_good_driver), driverScore);
			}
			catch(Exception ex){
				Log.e("DriverScoreService", ex.getMessage());
			}

		} else {
		    // Alternative implementation (or warn user that they must
		    // sign in to use this feature)
		}
	}
		
	public void getDriverLeaderboard(ResultCallback<Leaderboards.LoadScoresResult> callback){
		
				
		try{
			if(mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
				
				PendingResult<LoadScoresResult> res = Games.Leaderboards.loadTopScores(mGoogleApiClient, getString(R.string.leaderboard_good_driver), LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC, 10);
				res.setResultCallback(callback);
				
			}
		}
		catch(Exception e){
			Log.e("GooglePlayService", e.getMessage());
		}
		
	}
	
	public void getDriverScore(ResultCallback<LoadPlayerScoreResult> callback) {
		try {

			if(mGoogleApiClient != null){
				
				connect();
				
				PendingResult<LoadPlayerScoreResult> pres = Games.Leaderboards
						.loadCurrentPlayerLeaderboardScore(mGoogleApiClient,
								getString(R.string.leaderboard_good_driver),
								LeaderboardVariant.TIME_SPAN_ALL_TIME,
								LeaderboardVariant.COLLECTION_PUBLIC);
	
				pres.setResultCallback(callback);

			}
		} catch (Exception e) {
			Log.e("GooglePlayService", e.getMessage());
		}
		

	}
	
	public void submitMPGScore(long mpgScore){
		 
		if(mGoogleApiClient != null){
			
			connect();
			
			try{			
		    // Call a Play Games services API method, for example:
				Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderboard_mpg), mpgScore);
			}
			catch(Exception ex){
				Log.e("DriverScoreService", ex.getMessage());
			}
		
		} else {
		    // Alternative implementation (or warn user that they must
		    // sign in to use this feature)
		}
	}		

	
	public void getMPGLeaderboard(ResultCallback<Leaderboards.LoadScoresResult> callback){
		
		
		try{
			if(mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
				
				PendingResult<LoadScoresResult> res = Games.Leaderboards.loadTopScores(mGoogleApiClient, getString(R.string.leaderboard_good_driver), LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC, 10);
				res.setResultCallback(callback);
				
			}
		}
		catch(Exception e){
			Log.e("GooglePlayService", e.getMessage());
		}
		
	}
	
	public void getMPGScore(ResultCallback<LoadPlayerScoreResult> callback) {
		try {

			PendingResult<LoadPlayerScoreResult> pres = Games.Leaderboards
					.loadCurrentPlayerLeaderboardScore(mGoogleApiClient,
							getString(R.string.leaderboard_good_driver),
							LeaderboardVariant.TIME_SPAN_ALL_TIME,
							LeaderboardVariant.COLLECTION_PUBLIC);

			pres.setResultCallback(callback);

		} catch (Exception e) {
			Log.e("GooglePlayService", e.getMessage());
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
		
		return null;
	}

}

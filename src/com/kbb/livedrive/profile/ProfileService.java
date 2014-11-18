package com.kbb.livedrive.profile;

import java.util.ArrayList;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.leaderboard.Leaderboard;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.android.gms.games.leaderboard.Leaderboards.LoadPlayerScoreResult;
import com.google.android.gms.games.leaderboard.Leaderboards.LoadScoresResult;
import com.kbb.livedrive.googleplay.GooglePlayService;
import com.kbb.livedrive.scoring.DriverScoreService;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class ProfileService extends Service {
	
	
	private static ProfileService instance;
	
	public static String ACTION_DRIVER_SCORE_CHANGED = "com.kbb.livedrive.ProfileService.ACTION_DRIVER_SCORE_CHANGED";
	public static String ACTION_MPG_SCORE_CHANGED = "com.kbb.livedrive.ProfileService.ACTION_MPG_SCORE_CHANGED";
	public static String ACTION_DRIVER_PLAYER_RANK_CHANGED = "com.kbb.livedrive.ProfileService.ACTION_DRIVER_PLAYER_RANK_CHANGED";
	public static String ACTION_MPG_PLAYER_RANK_CHANGED = "com.kbb.livedrive.ProfileService.ACTION_MPG_PLAYER_RANK_CHANGED";
	public static String ACTION_DRIVER_LEADERBOARD_CHANGED = "com.kbb.livedrive.ProfileService.ACTION_DRIVER_LEADERBOARD_CHANGED";
	public static String ACTION_MPG_LEADERBOARD_CHANGED = "com.kbb.livedrive.ProfileService.ACTION_MPG_LEADERBOARD_CHANGED";
	
	public static String ACTION_ODOMETER_CHANGED = "com.kbb.livedrive.ProfileService.ACTION_ODOMETER_CHANGED";
	
	
	
	static{
		instance = null;
	}

	public static ProfileService getInstance(){
		return instance;
	}
	
	private CurrentPlayer player;
	
	private VehicleDetails currentVehicle;
	
	public VehicleDetails getCurrentVehicle(){
		if(currentVehicle == null){
			currentVehicle = VehicleDetails.createDummy();
		}
		
		return currentVehicle;
	}

	public CurrentPlayer getCurrentPlayer(){
		
		if(player == null){
			player = CurrentPlayer.createDummy();
		}
		
		return player;
	}
	
	@Override
	public void onCreate() {
		instance = this;
		
		// restore player from local store
		loadPlayerFromPersistentStore();

		
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		return super.onStartCommand(intent, flags, startId);
	}
	
	private void loadPlayerFromPersistentStore() {
		
		SharedPreferences pref = this.getSharedPreferences("player_store", Context.MODE_PRIVATE);
		
			try{
				player = new CurrentPlayer();
				player.setUserName("LiveDrive2014"); //pref.getString("userName", "LiveDrive2014"));
				player.setImageUrl(pref.getString("imageUrl", ""));
				
				player.setBestDriverScore(Double.longBitsToDouble(pref.getLong("bestDriverScore", Double.doubleToRawLongBits(76))));
				player.setPreviousDriverScore(Double.longBitsToDouble(pref.getLong("previousDriverScore", Double.doubleToRawLongBits(75))));
				player.setLatestDriverScore(Double.longBitsToDouble(pref.getLong("latestDriverScore", Double.doubleToRawLongBits(68))));
				player.setDriverRank(pref.getString("driverRank", "10"));
				
				player.setBestMpgScore(Double.longBitsToDouble(pref.getLong("bestMpgScore", Double.doubleToRawLongBits(86))));
				player.setPreviousMpgScore(Double.longBitsToDouble(pref.getLong("previousMpgScore", Double.doubleToRawLongBits(80))));
				player.setLatestMpgScore(Double.longBitsToDouble(pref.getLong("latestMpgScore", Double.doubleToRawLongBits(84))));
				player.setMpgRank(pref.getString("mpgRank", "5"));
				
			}
			catch(Exception e){
				Log.e("ProfileService", e.getMessage());
			}
		
		pref = this.getSharedPreferences("vehicle_store", Context.MODE_PRIVATE);
			
		try{
			currentVehicle = new VehicleDetails();
			currentVehicle.setOdometer(Double.longBitsToDouble(pref.getLong("odometer", Double.doubleToRawLongBits(12736))));
		}
		catch(Exception e){
			Log.e("ProfileService", e.getMessage());
		}
		
	}
	
	private void savePlayerToPersistentStore(){
		SharedPreferences pref = this.getSharedPreferences("player_store", Context.MODE_PRIVATE);
		
		SharedPreferences.Editor prefEdit = pref.edit();
		
		try{
			prefEdit.putString("userName", player.getUserName());
			prefEdit.putString("imageUrl", player.getImageUrl());
			prefEdit.putLong("bestDriverScore", Double.doubleToRawLongBits(player.getBestDriverScore()));
			prefEdit.putLong("previousDriverScore", Double.doubleToRawLongBits(player.getPreviousDriverScore()));
			prefEdit.putLong("latestDriverScore", Double.doubleToRawLongBits(player.getLatestDriverScore()));
			prefEdit.putString("driverRank", player.getDriverRank());
			
			prefEdit.putLong("bestMpgScore", Double.doubleToRawLongBits(player.getBestMpgScore()));
			prefEdit.putLong("previousMpgScore", Double.doubleToRawLongBits(player.getPreviousMpgScore()));
			prefEdit.putLong("latestMpgScore", Double.doubleToRawLongBits(player.getLatestMpgScore()));
			prefEdit.putString("mpgRank", player.getMpgRank());
			
		}
		finally{
			prefEdit.commit();
		}
		
		
		pref = this.getSharedPreferences("vehicle_store", Context.MODE_PRIVATE);
		
		prefEdit = pref.edit();
		
		try{
			prefEdit.putLong("odometer", Double.doubleToRawLongBits(currentVehicle.getRawOdometer()));
			
		}
		finally{
			prefEdit.commit();
		}
		

	}

	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}
	

	public void submitDriverScore(double driverScore) {
		
		player.setPreviousDriverScore(player.getLatestDriverScore());
		player.setLatestDriverScore(driverScore);
		if(player.getBestDriverScore() < driverScore){
			player.setBestDriverScore(driverScore);
		}
		
		savePlayerToPersistentStore();
		
		notifyDriverScoreChanged();
		
		// submit to google play
		GooglePlayService gp = GooglePlayService.getInstance();
		gp.submitDriverScore((int)player.getLatestDriverScoreLong());
		
	}

	public void submitMpgScore(double mpgScore){
		
		player.setPreviousMpgScore(player.getLatestMpgScore());
		player.setLatestMpgScore(mpgScore);
		if(player.getBestMpgScore() < mpgScore){
			player.setBestMpgScore(mpgScore);
		}
		
		savePlayerToPersistentStore();
		
		notifyMpgScoreChanged();

		
		//submit to google play
		GooglePlayService gp = GooglePlayService.getInstance();
		gp.submitMPGScore((int)player.getLatestMpgScoreLong());

		
	}

	private void notifyDriverScoreChanged() {
		//broadcast score changed notification
		Intent intent = new Intent(ACTION_DRIVER_SCORE_CHANGED);
		
		intent.putExtra("driverScore", player.getLatestDriverScoreLong());
		intent.putExtra("previousDriverScore", player.getPreviousDriverScoreLong());
		intent.putExtra("bestDriverScore", player.getBestDriverScoreLong());
		intent.putExtra("driverRank", player.getDriverRank());
		
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
	
	private void notifyOdometerChanged(){

		Intent intent = new Intent(ACTION_ODOMETER_CHANGED);
		
		intent.putExtra("odometer", currentVehicle.getOdometer());
		
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);		
		
	}
	
	
	private void notifyMpgScoreChanged() {
		//broadcast score changed notification
		Intent intent = new Intent(ACTION_MPG_SCORE_CHANGED);
		
		intent.putExtra("mpgScore", player.getLatestMpgScoreLong());
		intent.putExtra("previousMpgScore", player.getPreviousMpgScoreLong());
		intent.putExtra("bestMpgScore", player.getBestMpgScoreLong());
		intent.putExtra("mpgRank", player.getMpgRank());
		
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
	
	public void requestDriverScoreUpdate(){
		//send Driver Score Update
		
		notifyDriverScoreChanged();
	}
	
	public void requestMpgScoreUpdate(){
		//send Mpg Score Update
		
		notifyMpgScoreChanged();
		
	}
	
	public void requestOdometerUpdate() {
		notifyOdometerChanged();
	}

	
	public void requestDriverLeaderboardUpdate(){
		//TODO send Google Play request to get Driver Leaderboard
	}
	
	public void requestMpgLeaderboardUpdate(){
		//TODO send Google Play request to get Mpg Leaderboard
	}
	
	public void requestDriverRankUpdate(){
		//TODO send Google Play request to get current User's Driver Leaderboard position
	}
	
	public void requestMpgRankUpdate(){
		//TODO send Google Play request to get current User's Mpg Leaderboard position
	}
	
	
	
	
	
	final ResultCallback<LoadPlayerScoreResult> scoresDriverRankCallback = new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
		
		@Override
		public void onResult(LoadPlayerScoreResult result) {
									
			LeaderboardScore score = result.getScore();
			
			if(score != null){
				player.setScore(score.getDisplayScore());
				
				String lbPosition = score.getDisplayRank();
				
				player.setRank(lbPosition);
				
				player.setDriverRank(lbPosition);
				
				
				player.setImageUrl(score.getScoreHolderIconImageUrl());
				player.setUserName(score.getScoreHolderDisplayName());				
			
			
				Intent intent = new Intent(ACTION_DRIVER_PLAYER_RANK_CHANGED);
				
				intent.putExtra("player", (Player) player);
				intent.putExtra("driverScore", player.getLatestDriverScoreLong());
				intent.putExtra("previousDriverScore", player.getPreviousDriverScoreLong());
				intent.putExtra("bestDriverScore", player.getBestDriverScoreLong());
				intent.putExtra("rank", player.getDriverRank());
				
				LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
				
			}

			
		}
	};
	
	final ResultCallback<Leaderboards.LoadScoresResult> scoresDriverLeaderboardCallback = new ResultCallback<Leaderboards.LoadScoresResult>(){
		@Override
		public void onResult(LoadScoresResult arg0) {
			Leaderboard lb = arg0.getLeaderboard();
			
			LeaderboardScoreBuffer scoreBuffer = arg0.getScores();
			
			ArrayList<LeaderboardScore> scores = new ArrayList<LeaderboardScore>(scoreBuffer.getCount());
			
			for(int i = 0; i < scoreBuffer.getCount(); i++){
				
				LeaderboardScore score = scoreBuffer.get(i);
				if(score != null){
					
					long rawScore = score.getRawScore();
					String scoreDisplay = score.getDisplayScore();
					String leaderboardPosition = score.getDisplayRank();
					String iconUrl = score.getScoreHolderIconImageUrl();
					String userName = score.getScoreHolderDisplayName();
					
					scores.add(score);
				}
				
				//TODO send Leaderboard Update notification
				
			}
			
		}
	};

	final ResultCallback<LoadPlayerScoreResult> scoresMPGRankCallback = new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
		
		@Override
		public void onResult(LoadPlayerScoreResult result) {
			
			LeaderboardScore score = result.getScore();
			
			if(score != null){
				player.setScore(score.getDisplayScore());
				
				String lbPosition = score.getDisplayRank();
				
				player.setRank(lbPosition);
				
				player.setMpgRank(lbPosition);
				
				
				player.setImageUrl(score.getScoreHolderIconImageUrl());
				player.setUserName(score.getScoreHolderDisplayName());				
			
			
				Intent intent = new Intent(ACTION_DRIVER_PLAYER_RANK_CHANGED);
				
				intent.putExtra("player", (Player) player);
				intent.putExtra("driverScore", player.getLatestDriverScoreLong());
				intent.putExtra("previousDriverScore", player.getPreviousDriverScoreLong());
				intent.putExtra("bestDriverScore", player.getBestDriverScoreLong());
				intent.putExtra("rank", player.getDriverRank());
				
				LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
				
			}			
		}
	};
	
	final ResultCallback<Leaderboards.LoadScoresResult> scoresMPGLeaderboardCallback = new ResultCallback<Leaderboards.LoadScoresResult>(){
		@Override
		public void onResult(LoadScoresResult arg0) {
			Leaderboard lb = arg0.getLeaderboard();
			
			LeaderboardScoreBuffer scoreBuffer = arg0.getScores();
			
			ArrayList<LeaderboardScore> scores = new ArrayList<LeaderboardScore>(scoreBuffer.getCount());
			
			for(int i = 0; i < scoreBuffer.getCount(); i++){
				
				LeaderboardScore score = scoreBuffer.get(i);
				if(score != null){

					long rawScore = score.getRawScore();
					String scoreDisplay = score.getDisplayScore();
					String leaderboardPosition = score.getDisplayRank();
					String iconUrl = score.getScoreHolderIconImageUrl();
					String userName = score.getScoreHolderDisplayName();
					
					scores.add(scoreBuffer.get(i));
				}
			}
			//TODO send Leaderboard Update notification
		}
	};

	
		
}



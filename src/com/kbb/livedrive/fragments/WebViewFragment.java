package com.kbb.livedrive.fragments;



import java.util.ArrayList;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.leaderboard.Leaderboard;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardVariantRef;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.android.gms.games.leaderboard.Leaderboards.LoadPlayerScoreResult;
import com.google.android.gms.games.leaderboard.Leaderboards.LoadScoresResult;
import com.kbb.livedrive.R;
import com.kbb.livedrive.app.LiveDriveApplication;
import com.kbb.livedrive.applink.AppLinkService;
import com.kbb.livedrive.artifact.Location;
import com.kbb.livedrive.googleplay.GooglePlayService;
//import com.kbb.livedrive.adapter.ForecastListAdapter;
//import com.kbb.livedrive.weather.DayForecast;
//import com.kbb.livedrive.weather.WeatherDataManager;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class WebViewFragment extends BaseFragment {
	private View fragmentView;
	
	private WebView leaderboardView; 
	
	private String leaderboardType = ""; //getString(R.string.leaderboard_type_driver);
	
	private boolean viewLoadFinished = false;
	
	final ResultCallback<LoadPlayerScoreResult> scoresDriverScoreCallback = new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
		
		@Override
		public void onResult(LoadPlayerScoreResult result) {
			
			LeaderboardScore score = result.getScore();
			if(score != null){
				long rawScore = score.getRawScore();
				String scoreDisplay = score.getDisplayScore();
				String leaderboardPosition = score.getDisplayRank();
				String iconUrl = score.getScoreHolderIconImageUrl();
				String userName = score.getScoreHolderDisplayName();
				
				//TODO return Player's Driver score back to UI
				// this is for the Leaderboard page and other some elements on the score page
			}
			
			// TODO, use real score
			leaderboardView.loadUrl("javascript:draw(75,80);");
									
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
				
				//TODO return Driver Leaderboard back to the UI
				// this is for the leaderboard table
				
			}
			
		}
	};


	final ResultCallback<LoadPlayerScoreResult> scoresMPGScoreCallback = new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
		
		@Override
		public void onResult(LoadPlayerScoreResult result) {
			
			LeaderboardScore score = result.getScore();
			String scoreDisplay = score.getDisplayScore();
			
			//TODO return Player's MPG score back to UI
						
		}
	};
	
	final ResultCallback<Leaderboards.LoadScoresResult> scoresMPGLeaderboardCallback = new ResultCallback<Leaderboards.LoadScoresResult>(){
		@Override
		public void onResult(LoadScoresResult arg0) {
			Leaderboard lb = arg0.getLeaderboard();
			
			LeaderboardScoreBuffer scoreBuffer = arg0.getScores();
			
			ArrayList<LeaderboardScore> scores = new ArrayList<LeaderboardScore>(scoreBuffer.getCount());
			
			for(int i = 0; i < scoreBuffer.getCount(); i++){
				scores.add(scoreBuffer.get(i));
			}
			//TODO return MPG Leaderboard back to UI
		}
	};
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		fragmentView = inflater.inflate(R.layout.fragment_web_view, null);
	
		leaderboardView = (WebView) fragmentView.findViewById(R.id.viewLeaderboard);
		leaderboardView.getSettings().setJavaScriptEnabled(true);
		leaderboardView.setWebViewClient(Client);
		
		leaderboardView.loadUrl("file:///android_asset/leaderboard.html");
						
		return fragmentView;
	}

	public String getLeaderboardType (){
		return leaderboardType;
	}
	
	public void setLeaderboardType(String type){
		leaderboardType = type;
	}
	
	final WebViewClient Client = new WebViewClient () {
		

		@Override
	    public void onPageFinished(WebView view, String url) {	
			
			if(url.contains("file:///android_asset/leaderboard.html")){
			
				GooglePlayService gp = GooglePlayService.getInstance();
				
				gp.getDriverScore(scoresDriverScoreCallback);
				
				//gp.getDriverLeaderboard(scoresDriverLeaderboardCallback);
				
				//gp.getMPGScore(scoresMPGScoreCallback);
				
				//gp.getMPGLeaderboard(scoresMPGLeaderboardCallback);
			}
			
	    }
	    
	    
	};
	

	
}

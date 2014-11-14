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

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Display;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class WebViewFragment extends BaseFragment {
	private View fragmentView;

	private TextView mTextView;
	private WebView leaderboardView; 
	
	private String leaderboardType = ""; //getString(R.string.leaderboard_type_driver);
	
	private boolean viewLoadFinished = false;
	
	private final double PIC_WIDTH = 1920;
	
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
			leaderboardView.loadUrl("javascript:drawDriverScore(75,80);");
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
			leaderboardView.loadUrl("javascript:drawMpgScore(65,80);");
						
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
	
        mTextView = (TextView) fragmentView.findViewById(R.id.textview);
        mTextView.setText(getTextViewLabel());
        
		leaderboardView = (WebView) fragmentView.findViewById(R.id.scoreswebview);
		leaderboardView.getSettings().setJavaScriptEnabled(true);
		leaderboardView.setWebViewClient(Client);
		
		leaderboardView.setPadding(0, 0, 0, 0);
		leaderboardView.setInitialScale(getScale());

		leaderboardView.loadUrl("file:///android_asset/leaderboard.html");
						
		return fragmentView;
	}
	
	private int getScale(){
	    Double val = new Double(PIC_WIDTH)/new Double(PIC_WIDTH);
	    val = val * 100d;
	    return val.intValue();
	}

	private CharSequence getTextViewLabel() {
		//android:text="LadiesMan217|2014 Ford Flex Limited"
        String driverName = "LadiesMan217";
        String vehicleName = "2014 Ford Flex Limited";
        		
        SpannableString span1 = new SpannableString(driverName);
        SpannableString span2 = new SpannableString(vehicleName);

        span1.setSpan(
        		new RelativeSizeSpan(1.0f),  0, driverName.length(), 0);
        span1.setSpan(
        		new ForegroundColorSpan(Color.parseColor("#1155A5")), 
        		0, driverName.length(), 0);

        span2.setSpan(
        		new RelativeSizeSpan(0.75f),  0, vehicleName.length(), 0);
        span2.setSpan(new ForegroundColorSpan(Color.BLACK), 
        		0, vehicleName.length(), 0);	
        
        return TextUtils.concat(span1, "\n", span2);
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
				
				gp.getMPGScore(scoresMPGScoreCallback);
				
				//gp.getMPGLeaderboard(scoresMPGLeaderboardCallback);
			}
	    }
	};
	
}

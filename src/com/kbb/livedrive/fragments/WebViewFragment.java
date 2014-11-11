package com.kbb.livedrive.fragments;


import com.kbb.livedrive.R;
import com.kbb.livedrive.artifact.Location;
import com.kbb.livedrive.googleplay.GooglePlayService;
//import com.kbb.livedrive.adapter.ForecastListAdapter;
//import com.kbb.livedrive.weather.DayForecast;
//import com.kbb.livedrive.weather.WeatherDataManager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class WebViewFragment extends BaseFragment {
	private View fragmentView;
	
	private WebView leaderboardView; 
	
	private String leaderboardType = ""; //getString(R.string.leaderboard_type_driver);
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		fragmentView = inflater.inflate(R.layout.fragment_web_view, null);
	
		leaderboardView = (WebView) fragmentView.findViewById(R.id.viewLeaderboard);
		leaderboardView.loadUrl("file:///android_asset/leaderboard.html");
		
		GooglePlayService gp = GooglePlayService.getInstance();
		
		gp.getDriverLeaderboard();
		
		return fragmentView;
	}

	public String getLeaderboardType (){
		return leaderboardType;
	}
	
	public void setLeaderboardType(String type){
		leaderboardType = type;
	}
	
}

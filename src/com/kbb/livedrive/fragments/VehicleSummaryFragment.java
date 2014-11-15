package com.kbb.livedrive.fragments;


import com.kbb.livedrive.R;
import com.kbb.livedrive.artifact.Location;
import com.kbb.livedrive.googleplay.GooglePlayService;
//import com.kbb.livedrive.adapter.ForecastListAdapter;
//import com.kbb.livedrive.weather.DayForecast;
//import com.kbb.livedrive.weather.WeatherDataManager;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class VehicleSummaryFragment extends BaseFragment {
	private View fragmentView;
	
	private WebView vehicleView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		
		fragmentView = inflater.inflate(R.layout.fragment_vehicle_summary, null);
		
		vehicleView = (WebView) fragmentView.findViewById(R.id.viewVehicle);
		vehicleView.getSettings().setJavaScriptEnabled(true);
		vehicleView.setWebViewClient(Client);
		
		vehicleView.loadUrl("file:///android_asset/vehicleSummary.html");
						
		return fragmentView;
	}
	
	final WebViewClient Client = new WebViewClient () {
		
		@Override
	    public void onPageFinished(WebView view, String url) {	
			
			if(url.contains("file:///android_asset/vehicleSummary.html")){
				
				//TODO get and load vehicle details into webView
			
			}
			
	    }
	    
	    
	};

	
	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
	}
	
	
	
}

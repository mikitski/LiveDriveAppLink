package com.kbb.livedrive.fragments;


import java.text.NumberFormat;
import java.util.Locale;

import com.kbb.livedrive.R;
import com.kbb.livedrive.applink.AppLinkService;
import com.kbb.livedrive.artifact.Location;
import com.kbb.livedrive.googleplay.GooglePlayService;
import com.kbb.livedrive.profile.ProfileService;
//import com.kbb.livedrive.adapter.ForecastListAdapter;
//import com.kbb.livedrive.weather.DayForecast;
//import com.kbb.livedrive.weather.WeatherDataManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
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
	
	private BroadcastReceiver odometerChangedReceiver = new BroadcastReceiver(){
		public void onReceive(android.content.Context context, Intent intent) {
			
			long odometer = intent.getIntExtra("odometer", 50);
			
			//update UI on driver score change
			vehicleView.loadUrl(String.format("javascript:setVehicleDetails(%s);", odometer));
		}
	};

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		
		fragmentView = inflater.inflate(R.layout.fragment_vehicle_summary, null);
		
		LocalBroadcastManager lb = LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
		lb.registerReceiver(odometerChangedReceiver, new IntentFilter(AppLinkService.ACTION_VEHICLE_DRIVING_CHANGED));
		lb.registerReceiver(odometerChangedReceiver, new IntentFilter(ProfileService.ACTION_ODOMETER_CHANGED));
		
		
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
				
				ProfileService.getInstance().requestOdometerUpdate();
			
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

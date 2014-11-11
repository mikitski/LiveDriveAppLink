package com.kbb.livedrive.fragments;


import com.kbb.livedrive.R;
import com.kbb.livedrive.artifact.Location;
//import com.kbb.livedrive.adapter.ForecastListAdapter;
//import com.kbb.livedrive.weather.DayForecast;
//import com.kbb.livedrive.weather.WeatherDataManager;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class VehicleSumaryFragment extends BaseFragment {
	private View fragmentView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		fragmentView = inflater.inflate(R.layout.fragment_vehicle_summary, null);
		
		return fragmentView;
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	
	
}

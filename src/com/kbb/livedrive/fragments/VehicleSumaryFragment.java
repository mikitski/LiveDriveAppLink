package com.kbb.livedrive.fragments;


import com.kbb.livedrive.R;
import com.kbb.livedrive.artifact.Location;
//import com.kbb.livedrive.adapter.ForecastListAdapter;
//import com.kbb.livedrive.weather.DayForecast;
//import com.kbb.livedrive.weather.WeatherDataManager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class VehicleSumaryFragment extends BaseFragment {
	private View fragmentView;
//	private WeatherDataManager dataManager;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		fragmentView = inflater.inflate(R.layout.fragment_vehicle_summary, null);
		
//		dataManager = WeatherDataManager.getInstance();
//		if (dataManager != null) {
//			DayForecast[] forecast = dataManager.getForecast();
//			String units = dataManager.getUnits();
//			Location location = dataManager.getCurrentLocation();
//			if (location != null) {
//				setLocation(location);
//			}
//			if (forecast != null) {
//				setForecast(forecast, units);
//			}
//			else {
//				DayForecast loadingForecast = new DayForecast();
//				loadingForecast.conditionTitle = "Loading..";
//				DayForecast[] loading = { loadingForecast };
//				setForecast(loading, units);
//			}
//		}
	
		return fragmentView;
	}
	
	public void updateLocation() {
//		if (dataManager != null) {
//			Location location = dataManager.getCurrentLocation();
//			if (location != null) {
//				setLocation(location);
//			}
//		}		
	}
	
	public void setLocation(Location location) {
	}
	
	public void updateForecast() {
//		if (dataManager != null) {
//			DayForecast[] forecast = dataManager.getForecast();
//			String units = dataManager.getUnits();
//			if (forecast != null) {
//				setForecast(forecast, units);
//			}
//		}
	}

	/**
	 * Store the current forecast in the fragment
	 * @param forecast
	 */
//	public void setForecast(DayForecast[] forecast, String units) {
//		if (forecast != null) {
//			ForecastListAdapter adapter = new ForecastListAdapter(getActivity(), forecast);
//			forecastListView.setAdapter(adapter);
//			forecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//	            @Override
//	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//	            	// TODO: forecast onClick
//	            }
//	        });
//		}
//	}
}

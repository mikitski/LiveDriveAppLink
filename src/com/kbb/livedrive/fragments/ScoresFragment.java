package com.kbb.livedrive.fragments;


import java.util.Locale;

import com.kbb.livedrive.R;
import com.kbb.livedrive.artifact.Location;
import com.kbb.livedrive.processor.ImageProcessor;
//import com.kbb.livedrive.weather.WeatherService;
//import com.kbb.livedrive.weather.WeatherConditions;
//import com.kbb.livedrive.weather.WeatherDataManager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ScoresFragment extends BaseFragment {
	
	private TextView locationTextView;
//	private WeatherDataManager dataManager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_scores, null);

//		dataManager = WeatherDataManager.getInstance();
//		if (dataManager != null) {
//			WeatherConditions conditions = dataManager.getWeatherConditions();
//			String units = dataManager.getUnits();
//			Location location = dataManager.getCurrentLocation();
//			if (conditions != null) {
//				setConditions(conditions, units);
//			}
//			if (location != null) {
//				setLocation(location);
//			}
//		}

	    return v;
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
        if (location != null && location.city != null && location.state != null) {
        	if (locationTextView != null) {
        		locationTextView.setText(location.city + ", " + location.state);
        	}
        }
	}
	
	public void updateConditions() {
//		if (dataManager != null) {
//			WeatherConditions conditions = dataManager.getWeatherConditions();
//			String units = dataManager.getUnits();
//			if (conditions != null) {
//				setConditions(conditions, units);
//			}
//		}
	}
	
	/**
	 * Store the current conditions in the fragment
	 * @param conditions
	 */
//	public void setConditions(WeatherConditions conditions, String units) {
//		if (conditions != null) {
//			Float temperature = conditions.temperature;
//			Float windSpeed = conditions.windSpeed;
//			Float humidity = conditions.humidity;
//			Float adjustedTemperature = conditions.feelsLikeTemperature;
//			String temp = null;
//			String wind = null;
//			String humid = null;
//			String adjTemp = null;
//			String tempUnits = null;
//			String speedUnits = null;
//						
//			if ("imperial".equalsIgnoreCase(units) || units == null) {
//				if (temperature != null)
//					temperature = Float.valueOf(WeatherService.convertTemperatureToImperial(temperature.floatValue()));
//				if (windSpeed != null)
//					windSpeed = Float.valueOf(WeatherService.convertSpeedToImperial(windSpeed.floatValue()));
//				if (adjustedTemperature != null)
//					adjustedTemperature = Float.valueOf(WeatherService.convertTemperatureToImperial(adjustedTemperature.floatValue()));
//				tempUnits = "F";
//				speedUnits = "mph";
//			}
//			else {
//				tempUnits = "C";
//				speedUnits = "kph";
//			}
//			if (temperature != null)
//				temp = String.format(Locale.getDefault(), "%.0f \u00B0 %s", temperature.floatValue(), tempUnits);
//			if (windSpeed != null)
//				wind = String.format(Locale.getDefault(), "Wind Speed: %.0f %s", windSpeed.floatValue(), speedUnits);
//			if (humidity != null)
//				humid = String.format(Locale.getDefault(), "Humidity: %.0f %%", humidity.floatValue());
//			if (adjustedTemperature != null)
//				adjTemp = String.format(Locale.getDefault(), "Feels Like: %.0f \u00B0 %s", adjustedTemperature.floatValue(), tempUnits);
//			
//			currentTempView.setText(temp);
//			conditionsTextView.setText(conditions.conditionTitle);
//			adjustedTempView.setText(adjTemp);			
//			windSpeedView.setText(wind);
//			humidityView.setText(humid);
//			
//			if (conditions.conditionIcon != null)
//				ImageProcessor.setConditionsImage(conditionsIconView, conditions.conditionIcon);
//		}
//	}
}

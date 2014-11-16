package com.kbb.livedrive.location;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.kbb.livedrive.app.LiveDriveApplication;
import com.kbb.livedrive.artifact.GPSLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class LocationServices extends Service{
	
	private static LocationServices instance = null;
	
	private Random rand = new Random();
	
	
	
	public static LocationServices getInstance(){
		return instance;
	}
	
	
	@Override
	public void onCreate() {
		
		super.onCreate();
	
		instance = this;
	}
	
	private String makeHTTPRequest(String Url){
		
		HttpClient httpclient = new DefaultHttpClient();
	    HttpResponse response;
		try {
			response = httpclient.execute(new HttpGet(Url));
		    StatusLine statusLine = response.getStatusLine();
		    if(statusLine.getStatusCode() == HttpStatus.SC_OK){
		        ByteArrayOutputStream out = new ByteArrayOutputStream();
		        response.getEntity().writeTo(out);
		        out.close();
		        String responseString = out.toString();
		        return responseString;
		    } else{
		        //Closes the connection.
		        response.getEntity().getContent().close();
		        return "";
		    }
		} catch (ClientProtocolException e) {
			Log.e("HttpRequest", e.getMessage());
		} catch (IOException e) {
			Log.e("HttpRequest", e.getMessage());
		}
		
		return "";
	    
	}

	public String getCurrentRoadClass(double latitude, double longitude){
		
		//TODO add reoad class from lat long 
		// use http://api.geonames.org/findNearbyStreetsJSON?lat=38.492621&lng=-121.515197&username=mikitski
		
		String mtfcc = "S1400";
		String geoNamesUrl = "http://api.geonames.org/findNearbyStreetsJSON?lat=%s1&lng=%s&username=mikitski";
		
		String response = makeHTTPRequest(String.format(geoNamesUrl, latitude, longitude));
		
		if(response != ""){
			mtfcc = GeonamesJsonProcessor.getGeoNamesRoadType(response);
		}
		
		if("S1100".equals(mtfcc) || "S1200".equals(mtfcc)) {
			return "Highway";
		}
		else {
			return "Street";
		}		
	}
	
	public int getCurrentAvgSpeed(double latitude, double longitude){
		
		//TODO get Real Time Avg Speed:
		
		// http://pems.dot.ca.gov/?report_form=1&dnode=Freeway&content=spatial&tab=spatial2d&export=text&fwy=5&dir=N&s_time_id=1415733000&s_mm=11&s_dd=11&s_yy=2014&s_hh=19&s_mi=10&start_pm=512.435&end_pm=512.435&crossings=&station_type=ml&agg=on&q=speed
		//fwy=5&
		//dir=N&
		//start_pm=512.435&end_pm=512.435& ===== get these from detector config file (see below)
		//s_time_id=1415733000&
		//s_mm=11&s_dd=11&s_yy=2014&s_hh=19&s_mi=10&  =============set to current time minus a little delay (10 min or so)
		//crossings=&station_type=ml&
		//agg=on&
		//q=speed 
		
		//Detector config file - use to get nearest traffic detector (VDS) by lat/long
		//http://pems.dot.ca.gov/vds_config.xml
		
		return 45 + 5 - rand.nextInt(10);
		
	}

	public String getCurrentSunStatus(double latitude, double longitude){
		
		return "Day";
//		//TODO get Real Time Day/Night indicator from weather sunrise/sunset data
//		Calendar cal = Calendar.getInstance();
//		if(cal.get(Calendar.HOUR_OF_DAY) > 5 && cal.get(Calendar.HOUR_OF_DAY) < 19 ){
//			return "Day";
//		}
//		else {
//			return "Night";
//		}
		
	}
	
	public String getWetaherConditionsdouble(double latitude, double longitude){
		
		//TODO get Real Time weather conditions indicator 
		
		return "Sunny";
		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		
		return null;
	}
	
}

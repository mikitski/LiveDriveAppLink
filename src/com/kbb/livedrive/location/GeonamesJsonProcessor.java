package com.kbb.livedrive.location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.kbb.livedrive.processor.JsonProcessor;

public class GeonamesJsonProcessor {
	
	public static String getGeoNamesRoadType(String jsonString){
		
		String mtfcc = "S1400";
		
		JSONObject json = new JsonProcessor().getJsonFromString(jsonString);
		try {
			JSONArray streetSegments = json.getJSONArray("streetSegment");
			if(streetSegments != null && streetSegments.length() > 0){
				JSONObject streetSegment = streetSegments.getJSONObject(0);

				if(streetSegment != null){
					mtfcc = streetSegment.getString("mtfcc");
				}
			}
		} catch (JSONException e) {
			
			Log.e("JSON Parser", e.getMessage());
		}
		
		return mtfcc;
		
	}

}

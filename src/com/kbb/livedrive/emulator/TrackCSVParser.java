package com.kbb.livedrive.emulator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.kbb.livedrive.app.LiveDriveApplication;

import android.util.Log;

public class TrackCSVParser {
	
	public static EmulatorTrack parseFromCsv(String fileName){
		
		EmulatorTrack track = new EmulatorTrack();
		
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
	 
		try {
	 
	
			br = new BufferedReader(new InputStreamReader(LiveDriveApplication.getInstance().getAssets().open("tracks/" + fileName)));
			while ((line = br.readLine()) != null) {
	 
			        // use comma as separator
				String[] pointText = line.split(cvsSplitBy);
				
				try{
				
					EmulatorTrackPoint point = new EmulatorTrackPoint();
					
					point.setLatitude(Double.parseDouble(pointText[0]));
					point.setLongitude(Double.parseDouble(pointText[1]));
					point.setElevation(Double.parseDouble(pointText[2]));
					
					Date dt = ( new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ", Locale.US).parse(pointText[3].replaceFirst("Z", "-0800")));
					point.setTimestamp(dt);
					
					track.getTrack().add(point);
				}
				catch(Exception e){
					Log.e("parseFromCSV", e.getMessage());
				}
			}
		} 
		catch (FileNotFoundException e) {
			Log.e("parseFromCSV", e.getMessage());
		} 
		catch (IOException e) {
			Log.e("parseFromCSV", e.getMessage());
		} 
		finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					Log.e("parseFromCSV", e.getMessage());
				}
			}
		}
		
		return track;
		
	}

}

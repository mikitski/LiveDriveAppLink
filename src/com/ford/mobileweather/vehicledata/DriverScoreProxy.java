package com.ford.mobileweather.vehicledata;

import java.util.List;

import com.ford.syncV4.proxy.rpc.OnVehicleData;

public class DriverScoreProxy {
	
	private static Object blah = new Object();
	
	private static double currentDriverScore = 73.4;
	private static double currentMPGScore = 77.5;
	
	
	public static double calculateDriverScore(List<OnVehicleData> data){
		
		double driverScore = (double) 73.4;
		
		synchronized (blah) {
			currentDriverScore = driverScore;
		}
		
		return driverScore;
	}
	
	public static double getDriverScore() {
		double score = 0;
		
		synchronized (blah) {
			score = currentDriverScore;
		}
		
		return score;
	}
	
	public static String getDriverScoreDisplay(){
		String display;
		double score = getDriverScore();
		
		if(score < 50)
			display = "Low";
		else if (score > 50 && score < 75)
			display = "Not Bad";
		else 
			display = String.valueOf(score);
		
		return display;
	}


	public static double getMPGScore() {
		double score = 0;
		
		synchronized (blah) {
			score = currentMPGScore;
		}
		
		return score;
	}

	public static String getMPGScoreDisplay(){
		String display;
		double score = getMPGScore();
		
		if(score < 50)
			display = "Low";
		else if (score > 50 && score < 75)
			display = "Not Bad";
		else 
			display = String.valueOf(score);
		
		return display;
	}

}

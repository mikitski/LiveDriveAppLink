package com.ford.mobileweather.vehicledata;

import java.util.List;
import java.util.Random;

import com.ford.syncV4.proxy.rpc.OnVehicleData;

public class DriverScoreProxy {
	
	private static Object blah = new Object();
	
	private static double currentDriverScore = 73.4;
	private static double currentMPGScore = 77.5;
	
	private static int SPEED_LIMIT = 50;
	
	
	public static double calculateDriverScore(List<OnVehicleData> data){
		
		
		double driverScore = getDriverScore();
		
		for(int i = 0; i < data.size(); i++)
		{
			OnVehicleData item = data.get(i);
			double currentSpeed = item.getSpeed();
			
			double speedDelta = Math.abs(SPEED_LIMIT - currentSpeed);
						
			//double newScore = Math.exp(Math.sqrt(1/speedDelta));
			
			double newScore = 100 * (1 - 1/(1+ Math.exp(5-speedDelta/2)));
			
			driverScore = (driverScore + newScore) / 2;
		}
		
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
			display = "Not Too Bad";
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
	
	public static String getLeaderboard(){
		String display;
		double score = getDriverScore();
		
		Random ran = new Random();
		
		int pos = ran.nextInt(15) + 1;
		
		if(score < 75)
			display = "You don't want to know...";
		else 
			display = "You are 10th in your leaderboard";
		
		return display;
	}

	public static void fakeDriverScore(double score){
		synchronized (blah) {
			currentDriverScore = score;
		}
		
	}
	
}

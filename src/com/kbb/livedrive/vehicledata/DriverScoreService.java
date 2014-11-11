package com.kbb.livedrive.vehicledata;

import java.util.List;
import java.util.Random;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.ford.syncV4.proxy.rpc.OnVehicleData;

public class DriverScoreService extends Service {
	
	private static DriverScoreService instance = null;
		
	private static double currentDriverScore = 73.4;
	private static double currentMPGScore = 77.5;
	
	private static int SPEED_LIMIT = 50;
	
	private static int avgMPG = 25;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		instance = this;
		
	}
	
	public static DriverScoreService getInstance(){
		return instance;
	}
	
	public synchronized double getDriverScore(){
		return currentDriverScore;
	}
	
	public synchronized void setDriverScore(double score){
		currentDriverScore = score;
	}
	
	public synchronized double getMPGScore() {		
		return currentMPGScore;
	}
	
	public synchronized void setMPGScore(double score){
		currentMPGScore = score;
	}
	

	public long calculateDriverScore(List<OnVehicleData> data){
		
		
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
		
		setDriverScore(driverScore);
		
		return Math.round(driverScore);
	}
	
	public long calculateMPGScore(List<OnVehicleData> data){
		
		double mpgScore = getMPGScore();
		
		for(int i = 0; i < data.size(); i++){
			OnVehicleData item = data.get(i);
			double mpg = item.getInstantFuelConsumption();
			
			double newScore = mpg / avgMPG;
			
			mpgScore = (mpgScore + newScore) / 2;
		}
		
		setMPGScore(mpgScore);
		
		return Math.round(mpgScore);
	}
	
	public String getDriverScoreDisplay(){
		String display;
		long score = Math.round(getDriverScore());
		
		if(score < 50)
			display = "Low";
		else if (score > 50 && score < 75)
			display = "Not Bad";
		else 
			display = String.valueOf(score);
		
		return display;
	}

	public String getMPGScoreDisplay(){
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
	
//	public String getLeaderboard(){
//		String display;
//		double score = getDriverScore();
//		
//		Random ran = new Random();
//		
//		int pos = ran.nextInt(15) + 1;
//		
//		if(score < 75)
//			display = "You don't want to know...";
//		else 
//			display = "You are 10th in your leaderboard";
//		
//		return display;
//	}

	public void fakeDriverScore(double score){
		setDriverScore(score);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
}

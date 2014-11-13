package com.kbb.livedrive.vehicledata;

import java.util.List;
import java.util.Random;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.ford.syncV4.proxy.rpc.OnVehicleData;
import com.kbb.livedrive.location.LocationServices;

public class DriverScoreService extends Service {
	
	private static DriverScoreService instance = null;
		
	private static double currentDriverScore = 73.4;
	private static double previousDriverScore = 73.4;
	
	private static double currentMPGScore = 77.5;
	private static double previousMPGScore = 77.5;
	
	private boolean isMoving = false;
	
	private static int SPEED_LIMIT = 50;
	private static int AVG_MPG = 25;
	
	private VehicleDataCache cache = new VehicleDataCache();

	private long tripStartOdometer = 0;
	private long tripEndOdometer = 0;
	
	private long tripHwyMiles = 0;
	private long tripCityMiles = 0;
	
	private final BroadcastReceiver drivingStateReceiver = new BroadcastReceiver(){
		public void onReceive(android.content.Context context, Intent intent) {
			
			String drivingState = intent.getStringExtra("drivingState");
			long odometer = intent.getLongExtra("odometer", 0);
			if("DRIVING".equals(drivingState)){
				startTrip(odometer);
			}
			else if ("PARKED".equals(drivingState)){
				endTrip(odometer);
			}
			
		};
	};
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		instance = this;
		
        LocalBroadcastManager lbManager = LocalBroadcastManager.getInstance(this);
        lbManager.registerReceiver(drivingStateReceiver, new IntentFilter("com.kbb.livedrive.AppLinkService.ACTION_VEHICLE_DRIVING_CHANGED"));

	}
	
	public static DriverScoreService getInstance(){
		return instance;
	}
	
	private synchronized double getDriverScore(){
		return currentDriverScore;
	}
	
	private synchronized void setDriverScore(double score){
		currentDriverScore = score;
	}
	
	private synchronized double getMPGScore() {		
		return currentMPGScore;
	}
	
	private synchronized void setMPGScore(double score){
		currentMPGScore = score;
	}

	public void addVehicleData(OnVehicleData data){
		cache.addVehicleData(data);
	}

	public void calculateScores(){

		List<OnVehicleData> data = cache.getDataCache();
		cache.clearVehicleDataCache();
		
		double driverScore = getDriverScore();
		double mpgScore = getMPGScore();
		
		try{
			driverScore = calculateDriverScoreExternal(data);
			mpgScore = calculateMPGScoreExternal(data);
			
			
		}
		finally {
			setDriverScore(driverScore);
			setMPGScore(mpgScore);
			
		}

	}
	
	private long calculateDriverScore(){
		
		List<OnVehicleData> data = cache.getDataCache();
		cache.clearVehicleDataCache();
		
		double driverScore = getDriverScore();
		
		try{
			driverScore = calculateDriverScoreExternal(data);
			
			
		}
		finally {
			setDriverScore(driverScore);
			
		}
		
		return Math.max(Math.round(driverScore), 50);
	}

	private double calculateDriverScoreExternal(List<OnVehicleData> data) {
		
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
		return driverScore;
	}
	
	private double calculateDriverScoreExternalV2(List<OnVehicleData> data) {
		
		LocationServices loc = LocationServices.getInstance();
		
		double driverScore = getDriverScore();
		
		OnVehicleData lastData = data.get(data.size() - 1);
		
		double currLat= lastData.getGps().getLatitudeDegrees();
		double currLong= lastData.getGps().getLongitudeDegrees();
		
		int sliceDistance = lastData.getOdometer() - data.get(0).getOdometer();
		
		
		int currAvgSpeed = loc.getCurrentAvgSpeed(currLat, currLong);
		
		String currDayStatus = loc.getCurrentSunStatus(currLat, currLong);
		
		double currScore = 0;
		
		for(int i = 0; i < data.size(); i++){
			
			OnVehicleData item = data.get(i);
			
			double speed = item.getSpeed();
			
			if(currDayStatus == "Day"){
				currScore = currScore + 100-(50000*(Math.pow(10,0.0014*Math.pow(speed-currAvgSpeed,2)-0.0141*(speed-currAvgSpeed)+2.1095)*1/12*Math.pow(10,-8)));
			}
			else{
				currScore = currScore + 100-(50000*(Math.pow(10,0.0016*Math.pow(speed-currAvgSpeed,2)-0.0069*(speed-currAvgSpeed)+2.4605)*1/12*Math.pow(10,-8)));
			}
		}
		
		currScore = currScore / data.size();
		
		driverScore = (driverScore + currScore) / 2;
		
		return driverScore;
	}

	
	private long calculateMPGScore(){
		
		List<OnVehicleData> data = cache.getDataCache();
		cache.clearVehicleDataCache();
		
		double mpgScore = getMPGScore();
		
		try{
			mpgScore = calculateMPGScoreExternal(data);
		}
		finally{
			setMPGScore(mpgScore);
			
		}
		
		return Math.max(Math.round(mpgScore), 50);
	}

	private double calculateMPGScoreExternal(List<OnVehicleData> data) {
		
		LocationServices loc = LocationServices.getInstance();
		VehicleDetails currentVehicle = new VehicleDetailsService().getCurrent();
		
		OnVehicleData lastData = data.get(data.size() - 1);
		
		double currLat= lastData.getGps().getLatitudeDegrees();
		double currLong= lastData.getGps().getLongitudeDegrees();
		
		int sliceDistance = lastData.getOdometer() - data.get(0).getOdometer();
		
		long tripMiles = tripStartOdometer + sliceDistance;
		
		String roadClass = loc.getCurrentRoadClass(currLat, currLong);
		
		double currMpgScore = 0;
		
		for(int i = 0; i < data.size(); i++){
			currMpgScore += data.get(i).getInstantFuelConsumption();
		}
		
		currMpgScore = currMpgScore / data.size();
		
			
		if(roadClass == "Highway"){			
			tripHwyMiles += sliceDistance;
			
			currMpgScore += currMpgScore / currentVehicle.getHwyMPG() * sliceDistance/tripMiles;
		}
		else{
			
			tripCityMiles += sliceDistance;
			
			currMpgScore += currMpgScore / currentVehicle.getCityMPG() * sliceDistance/tripMiles;
			
		}
		
		double mpgScore = getMPGScore() + 100 * currMpgScore;
		
		return mpgScore;		
	}
	
	public String getDriverScoreDisplay(){
		
		String display;
		long score = Math.max(Math.round(getDriverScore()), 50);
		
		if(score < 50)
			display = "Low";
		else 
			display = String.valueOf(score);
		
		return display;
	}

	public String getMPGScoreDisplay(){
		String display;
		double score = Math.max(Math.round(getMPGScore()), 50);
		
		if(score < 50)
			display = "Low";
		else 
			display = String.valueOf(score);
		
		return display;
	}
	
	public void fakeDriverScore(double score){
		setDriverScore(score);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void startTrip(long odometer) {
		
		
		tripStartOdometer = odometer;
		previousDriverScore = currentDriverScore;
		previousMPGScore = currentMPGScore;
		
		isMoving = true;
	}

	public void endTrip(long odometer) {
		
		
		tripEndOdometer = odometer;
		isMoving = false;
		
	}
	
}

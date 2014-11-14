package com.kbb.livedrive.vehicledata;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ford.syncV4.proxy.rpc.OnVehicleData;
import com.kbb.livedrive.applink.AppLinkService;
import com.kbb.livedrive.googleplay.GooglePlayService;
import com.kbb.livedrive.location.LocationServices;

public class DriverScoreService extends Service {
	
	private static DriverScoreService instance = null;
	
	public static String ACTION_SCORE_CHANGED = "com.kbb.livedrive.DriverScoreService.ACTION_SCORE_CHANGED";
		
	private static double currentDriverScore = 50;
	private static double previousDriverScore = 50;
	
	private static double currentMPGScore = 50;
	private static double previousMPGScore = 50;
	
	private boolean isMoving = false;
	
	private VehicleDataCache cache = new VehicleDataCache();

	private long tripStartOdometer = 0;
	private long tripEndOdometer = 0;
	
	private long tripHwyMiles = 0;
	private long tripCityMiles = 0;
	
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	
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
        lbManager.registerReceiver(drivingStateReceiver, new IntentFilter(AppLinkService.ACTION_VEHICLE_DRIVING_CHANGED));

	}
	
	public static DriverScoreService getInstance(){
		return instance;
	}
	
	private synchronized double getRawDriverScore(){
		return currentDriverScore;
	}
	
	private synchronized void setDriverScore(double score){
		currentDriverScore = score;
	}
	
	private synchronized double getRawMPGScore() {		
		return currentMPGScore;
	}
	
	private synchronized void setMPGScore(double score){
		currentMPGScore = score;
	}
	
	public long getDriverScore(){
		return Math.max(Math.round(getRawDriverScore()), 50);
	}
	
	public long getMPGScore(){
		return Math.max(Math.round(getRawMPGScore()), 50);
		
	}	
	
	public synchronized long getPreviousDriverScore(){
		return Math.max(Math.round(previousDriverScore), 50);
	}

	public synchronized long getMPGDriverScore(){
		return Math.max(Math.round(previousMPGScore), 50);
	}
	
	public void addVehicleData(OnVehicleData data){
		cache.addVehicleData(data);
	}

	public void calculateScores(){

		List<OnVehicleData> data = cache.getDataCache();
		cache.clearVehicleDataCache();
		
		double driverScore = getRawDriverScore();
		double mpgScore = getRawMPGScore();
		
		try{
			//driverScore = calculateDriverScoreExternalV2(data);
			mpgScore = calculateMPGScoreExternal(data);
		}
		catch(Exception e){
			Log.e("Score Calculator", e.getMessage());
		}
		
		setDriverScore(driverScore);
		setMPGScore(mpgScore);

		
		Intent intent = new Intent(ACTION_SCORE_CHANGED);
		intent.putExtra("driverScore", getDriverScore());
		intent.putExtra("mpgScore", getMPGScore());
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);


	}
	
	private double calculateDriverScoreExternal(List<OnVehicleData> data) {
		
		double driverScore = getRawDriverScore();
		
		for(int i = 0; i < data.size(); i++)
		{
			OnVehicleData item = data.get(i);
			double currentSpeed = item.getSpeed();
			
			double speedDelta = Math.abs(50 - currentSpeed);
						
			//double newScore = Math.exp(Math.sqrt(1/speedDelta));
			
			double newScore = 100 * (1 - 1/(1+ Math.exp(5-speedDelta/2)));
			
			driverScore = (driverScore + newScore) / 2;
		}
		return driverScore;
	}
	
	private double calculateDriverScoreExternalV2(List<OnVehicleData> data) {
		
		LocationServices loc = LocationServices.getInstance();
		
		double driverScore = getRawDriverScore();
		
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

	private double calculateMPGScoreExternal(List<OnVehicleData> data) {
		
		double mpgScore = getRawMPGScore();
		
		try{
		
			LocationServices loc = LocationServices.getInstance();
			VehicleDetails currentVehicle = new VehicleDetailsService().getCurrent();
			
			OnVehicleData lastData = data.get(data.size() - 1);
			
			double currLat= lastData.getGps().getLatitudeDegrees();
			double currLong= lastData.getGps().getLongitudeDegrees();
			
			int sliceStartOdometer = data.get(0).getOdometer();
			int sliceEndOdometer = lastData.getOdometer();
			
			int sliceDistance = sliceEndOdometer - sliceStartOdometer;
			
			long tripOdometer = sliceEndOdometer - tripStartOdometer;
			
			String roadClass = loc.getCurrentRoadClass(currLat, currLong);
			
			double currMpgScore = 0;
			
			for(int i = 0; i < data.size(); i++){
				currMpgScore += data.get(i).getInstantFuelConsumption();
			}
			
			currMpgScore = currMpgScore / data.size();
			
				
			if(roadClass == "Highway"){			
				tripHwyMiles += sliceDistance;
				
				currMpgScore += currMpgScore / currentVehicle.getHwyMPG(); // * sliceDistance/tripMiles;
			}
			else{
				
				tripCityMiles += sliceDistance;
				
				currMpgScore += currMpgScore / currentVehicle.getCityMPG(); // * sliceDistance/tripMiles;
				
			}
			
			mpgScore = (currMpgScore * sliceDistance + mpgScore * sliceStartOdometer) / tripOdometer;
		} 
		catch(Exception ex){
			Log.e("calculateMPGScoreExternal", ex.getMessage());
		}
		
		return mpgScore;		
	}
	
	public String getDriverScoreDisplay(){
		
		String display;
		long score = getDriverScore();
		
		if(score < 50)
			display = "Low";
		else 
			display = String.valueOf(score);
		
		return display;
	}

	public String getMPGScoreDisplay(){
		String display;
		double score = getMPGScore();
		
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
		return null;
	}
	
	final Runnable scoreCalculator = new Runnable() {

		public void run() {

			int currentDrivingMode = 0;
			calculateScores();
		}

	};

	private ScheduledFuture<?> calculatorHandle;


	public void startTrip(long odometer) {
		
		
		tripStartOdometer = odometer;
		previousDriverScore = currentDriverScore;
		previousMPGScore = currentMPGScore;
		
		if(calculatorHandle == null || calculatorHandle.isDone() ||calculatorHandle.isCancelled() ){
			
			calculatorHandle = scheduler.scheduleAtFixedRate(scoreCalculator, 60, 60, SECONDS);
			
		}
		
		isMoving = true;
	}

	public void endTrip(long odometer) {
		
		tripEndOdometer = odometer;
		
		if(calculatorHandle != null){
			
			calculatorHandle.cancel(true);
		}
		
		calculateScores();
		
		long mpgScore = getMPGScore();
		long driverScore = getDriverScore();
		
		GooglePlayService gp = GooglePlayService.getInstance();
		
		gp.submitDriverScore(driverScore);
		gp.submitMPGScore(mpgScore);
		
		isMoving = false;
		
	}
	
}

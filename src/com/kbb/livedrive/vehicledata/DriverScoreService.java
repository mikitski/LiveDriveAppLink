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
	public static String ACTION_RT_MPG_SCORE_CHANGED = "com.kbb.livedrive.DriverScoreService.ACTION_RT_MPG_SCORE_CHANGED";
	public static String ACTION_RT_DRIVER_SCORE_CHANGED = "com.kbb.livedrive.DriverScoreService.ACTION_RT_DRIVER_SCORE_CHANGED";
		
	private double currentDriverScore = 50;
	private double previousDriverScore = 50;
	
	private double currentMPGScore = 50;
	private double previousMPGScore = 50;
	
	private boolean isMoving = false;
	
	private VehicleDataCache cache = new VehicleDataCache();

	private int tripStartOdometer = 0;
	private int tripEndOdometer = 0;
	
	private int tripHwyMiles = 0;
	private int tripCityMiles = 0;
	
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	
	private final BroadcastReceiver drivingStateReceiver = new BroadcastReceiver(){
		public void onReceive(android.content.Context context, Intent intent) {
			
			String drivingState = intent.getStringExtra("drivingState");
			int odometer = intent.getIntExtra("odometer", 0);
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

	public synchronized long getPreviousMPGScore(){
		return Math.max(Math.round(previousMPGScore), 50);
	}
	
	public void addVehicleData(OnVehicleData data){
		cache.addVehicleData(data);
	}
	
	private void notifyScoresChanged() {
		Intent intent = new Intent(ACTION_SCORE_CHANGED);
		intent.putExtra("driverScore", getDriverScore());
		intent.putExtra("mpgScore", getMPGScore());
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
	
	private void notifyRealTimeMPGScoreChanged(long mpgScore){
		Intent intent = new Intent(ACTION_RT_MPG_SCORE_CHANGED);
		intent.putExtra("mpgScore", mpgScore);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
	
	private void notifyRealTimeDriverScoreChanged(long driverScore){
		Intent intent = new Intent(ACTION_RT_DRIVER_SCORE_CHANGED);
		intent.putExtra("mpgScore", driverScore);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

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

		
		notifyScoresChanged();

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
			VehicleDetails currentVehicle = VehicleDetailsService.getInstance().getCurrent();
			
			OnVehicleData lastData = data.get(data.size() - 1);
			OnVehicleData firstData = data.get(0);
			
			
			double currLat= lastData.getGps().getLatitudeDegrees();
			double currLong= lastData.getGps().getLongitudeDegrees();
			
			int sliceStartOdometer = firstData.getOdometer();
			int sliceEndOdometer = lastData.getOdometer();
			
			int sliceDistance = sliceEndOdometer - sliceStartOdometer;
			
			int sliceStartTripDistance = Math.max(sliceStartOdometer - tripStartOdometer, 0);
			
			long tripDistance = sliceEndOdometer - tripStartOdometer;
			
			String roadClass = loc.getCurrentRoadClass(currLat, currLong);
			
			double avgFuelConsumption = 0;
			
			for(int i = 0; i < data.size(); i++){
				avgFuelConsumption += data.get(i).getInstantFuelConsumption();
			}
			
			avgFuelConsumption = avgFuelConsumption / data.size();
			
			double realTimeMpgScore = 0;
				
			if(roadClass == "Highway"){			
				tripHwyMiles += sliceDistance;
				
				realTimeMpgScore = avgFuelConsumption / currentVehicle.getHwyMPG(); // * sliceDistance/tripMiles;
			}
			else{
				
				tripCityMiles += sliceDistance;
				
				realTimeMpgScore = avgFuelConsumption / currentVehicle.getCityMPG(); // * sliceDistance/tripMiles;
				
			}
			
			realTimeMpgScore = Math.min(realTimeMpgScore * 100, 96);
			
			notifyRealTimeMPGScoreChanged(Math.round(realTimeMpgScore));
			
			//TODO send realtime MPG score change notificaton
			
			mpgScore = (realTimeMpgScore * sliceDistance + mpgScore * sliceStartTripDistance) / tripDistance;
		} 
		catch(Exception ex){
			Log.e("calculateMPGScoreExternal", ex.getMessage());
		}
		
		return Math.min(mpgScore, 100);		
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


	public void startTrip(int odometer) {

		//TODO convert current score to lifetime score (previous score is lifetime)
		previousDriverScore = currentDriverScore;
		previousMPGScore = currentMPGScore;

		
		tripStartOdometer = odometer;

		if(calculatorHandle == null || calculatorHandle.isDone() ||calculatorHandle.isCancelled() ){
			
			calculatorHandle = scheduler.scheduleAtFixedRate(scoreCalculator, 30, 30, SECONDS);
			
		}
		
		isMoving = true;
	}

	public void endTrip(int odometer) {
		
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

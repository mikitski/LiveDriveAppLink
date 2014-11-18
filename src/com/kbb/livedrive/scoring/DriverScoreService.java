package com.kbb.livedrive.scoring;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
import com.kbb.livedrive.profile.ProfileService;
import com.kbb.livedrive.profile.VehicleDetails;

public class DriverScoreService extends Service {
	

	private static DriverScoreService instance = null;
	
	public static String ACTION_RT_MPG_SCORE_CHANGED = "com.kbb.livedrive.DriverScoreService.ACTION_RT_MPG_SCORE_CHANGED";
	public static String ACTION_RT_DRIVER_SCORE_CHANGED = "com.kbb.livedrive.DriverScoreService.ACTION_RT_DRIVER_SCORE_CHANGED";
		
	private double currentDriverScore = 50;
	private double previousDriverScore = 50;
	
	private double currentMPGScore = 50;
	private double previousMPGScore = 50;
	
	private boolean isMoving = false;
	
	private VehicleDataCache cache = new VehicleDataCache();

	private int tripStartOdometer = 0;
	private Date tripStartTime;
	
	private int tripEndOdometer = 0;
	private Date tripEndTime;
	
	private int tripHwyMiles = 0;
	private int tripCityMiles = 0;
	
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	
	private final BroadcastReceiver drivingStateReceiver = new BroadcastReceiver(){
		public void onReceive(android.content.Context context, Intent intent) {
			
			String drivingState = intent.getStringExtra("drivingState");
			int odometer = intent.getIntExtra("odometer", 0);
			Date time = Calendar.getInstance().getTime();
			try {
				time = (new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH)).parse(intent.getStringExtra("time"));
			} catch (ParseException e) {
				
				e.printStackTrace();
			}
			
			if("DRIVING".equals(drivingState)){
				startTrip(odometer, time);
			}
			else if ("PARKED".equals(drivingState)){
				endTrip(odometer, time);
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
	
	private long getDriverScore(){
		return Math.max(Math.round(getRawDriverScore()), 50);
	}
	
	private long getMPGScore(){
		return Math.max(Math.round(getRawMPGScore()), 50);
		
	}	
	
	private synchronized long getPreviousDriverScore(){
		return Math.max(Math.round(previousDriverScore), 50);
	}

	private synchronized long getPreviousMPGScore(){
		return Math.max(Math.round(previousMPGScore), 50);
	}
	
	public void addVehicleData(OnVehicleData data){
		cache.addVehicleData(data);
	}
		
	private void notifyRealTimeMPGScoreChanged(long mpgScore){
		Intent intent = new Intent(ACTION_RT_MPG_SCORE_CHANGED);
		intent.putExtra("mpgScore", mpgScore);
		intent.putExtra("mpgScoreDisplay", getMPGScoreDisplay());
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
	
	private void notifyRealTimeDriverScoreChanged(long driverScore){
		Intent intent = new Intent(ACTION_RT_DRIVER_SCORE_CHANGED);
		intent.putExtra("driverScore", driverScore);
		intent.putExtra("driverScoreDisplay", getDriverScoreDisplay());		
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

	}
	


	public void calculateScores(){

		List<OnVehicleData> data = cache.getDataCache();
		cache.clearVehicleDataCache();
		
		double driverScore = getRawDriverScore();
		double mpgScore = getRawMPGScore();
		
		try{
			driverScore = calculateDriverScoreExternalV2(data);
			mpgScore = calculateMPGScoreExternal(data);
		}
		catch(Exception e){
			Log.e("Score Calculator", e.getMessage());
		}
		
		setDriverScore(driverScore);
		setMPGScore(mpgScore);

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
	
	private double driverScoreV1(double currentSpeed, double referenceSpeed){
		
		double speedDelta = Math.abs(referenceSpeed - currentSpeed);
		
		double newScore = 100 * (1 - 1/(1+ Math.exp(5-speedDelta/2)));
		
		return newScore;
	}
	
	private double driverScoreV2Day(double currentSpeed, double referenceSpeed){
	
		double speedDelta = Math.abs(referenceSpeed - currentSpeed);
		
		double newScore = 100-(50000*(Math.pow(10,0.0014*Math.pow(speedDelta,2)-0.0141*(speedDelta)+2.1095)*1/12*Math.pow(10,-8)));
		
		return newScore;
	}
	
	private double driverScoreV2Night(double currentSpeed, double referenceSpeed){
		
		double speedDelta = Math.abs(referenceSpeed - currentSpeed);
		
		double newScore = 100-(50000*(Math.pow(10,0.0016*Math.pow(speedDelta,2)-0.0069*(speedDelta)+2.4605)*1/12*Math.pow(10,-8)));
		
		return newScore;
	}
	
	
	private double calculateDriverScoreExternalV2(List<OnVehicleData> data) {
		
		LocationServices loc = LocationServices.getInstance();
		
		
		double driverScore = getRawDriverScore();
		
		OnVehicleData lastData = data.get(data.size() - 1);
		OnVehicleData firstData = data.get(0);
		
		double currLat= lastData.getGps().getLatitudeDegrees();
		double currLong= lastData.getGps().getLongitudeDegrees();
		
		int currAvgTrafficSpeed = loc.getCurrentAvgSpeed(currLat, currLong);
		String currDayStatus = loc.getCurrentSunStatus(currLat, currLong);
		
		
		Date sliceStart = AppLinkService.getInstance().getDateFromGps(firstData.getGps());
		Date sliceEnd = AppLinkService.getInstance().getDateFromGps(lastData.getGps());
		
		long sliceTime = Math.max(sliceEnd.getTime() - sliceStart.getTime(), 0);
		
		long tripTime = Math.max(sliceEnd.getTime() - tripStartTime.getTime(), 0); 
		long tripTimeAtStart = Math.max(tripTime - sliceTime, 0);
		
		double realTimeScore = 0;
		double avgSpeed = 0;
		
		for(int i = 0; i < data.size(); i++){
			
			OnVehicleData item = data.get(i);
			
			double speed = item.getSpeed();
			avgSpeed += speed;
			
			if(currDayStatus == "Day"){
				realTimeScore += driverScoreV1(speed, currAvgTrafficSpeed);
				//realTimeScore += driverScoreV2Day(speed, currAvgTrafficSpeed);
			}
			else{
				realTimeScore += driverScoreV2Night(speed, currAvgTrafficSpeed);
			}
		}
		
		avgSpeed = avgSpeed / data.size();
		realTimeScore = Math.max(Math.min(realTimeScore / data.size(), 96), 50);
		
		driverScore = (realTimeScore * sliceTime + driverScore * (tripTimeAtStart)) / tripTime;
		driverScore = Math.min(driverScore, 100);
		
		notifyRealTimeDriverScoreChanged(Math.round(driverScore));
		
		Log.i("DriverScore", String.format("nobs: %s; avgSpd: %s; avgEnvSpd: %s; rtScore: %s; sliceTm: %s; tripTm: %s; tripScore: %s", data.size(), avgSpeed, currAvgTrafficSpeed, realTimeScore, sliceTime, tripTime, driverScore));
		
		return driverScore;
	}

	private double calculateMPGScoreExternal(List<OnVehicleData> data) {
		
		double mpgScore = getRawMPGScore();
		
		try{
		
			LocationServices loc = LocationServices.getInstance();
			VehicleDetails currentVehicle = ProfileService.getInstance().getCurrentVehicle();
			
			OnVehicleData lastData = data.get(data.size() - 1);
			OnVehicleData firstData = data.get(0);
			
			
			double currLat= lastData.getGps().getLatitudeDegrees();
			double currLong= lastData.getGps().getLongitudeDegrees();
			
			Date sliceStart = AppLinkService.getInstance().getDateFromGps(firstData.getGps());
			Date sliceEnd = AppLinkService.getInstance().getDateFromGps(lastData.getGps());
			
			long sliceTime = Math.max(sliceEnd.getTime() - sliceStart.getTime(), 0);
			
			long tripTime = Math.max(sliceEnd.getTime() - tripStartTime.getTime(), 0); 
			long tripTimeAtStart = Math.max(tripTime - sliceTime, 0);
			
			String roadClass = loc.getCurrentRoadClass(currLat, currLong);
			
			double avgFuelConsumption = 0;
			
			for(int i = 0; i < data.size(); i++){
				avgFuelConsumption += data.get(i).getInstantFuelConsumption();
			}
			
			avgFuelConsumption = avgFuelConsumption / data.size();
			
			double realTimeMpgScore = 0;
			double referenceMpg = 25;
				
			if(roadClass == "Highway"){
				
				referenceMpg = currentVehicle.getHwyMPG(); // * sliceDistance/tripMiles;
			}
			else{

				referenceMpg = currentVehicle.getCityMPG(); // * sliceDistance/tripMiles;
			}
			
			realTimeMpgScore = avgFuelConsumption / referenceMpg;
			
			realTimeMpgScore = Math.max(Math.min(realTimeMpgScore * 100, 96), 50);
						
			mpgScore = (realTimeMpgScore * sliceTime + mpgScore * (tripTimeAtStart)) / tripTime;
			mpgScore = Math.min(mpgScore, 100);
			
			notifyRealTimeMPGScoreChanged(Math.round(mpgScore));

			Log.i("MpgScore", String.format("nobs: %s; avgMpg: %s; refMpg: %s; rtScore: %s; sliceTm: %s; tripTm: %s; tripScore: %s", data.size(), avgFuelConsumption, referenceMpg, realTimeMpgScore, sliceTime, tripTime, mpgScore));
			
		} 
		catch(Exception ex){
			Log.e("calculateMPGScoreExternal", ex.getMessage());
		}
		
		return 	mpgScore;
	}
	
	private String getDriverScoreDisplay(){
		
		String display;
		long score = getDriverScore();
		
		if(score < 60)
			display = "Low";
		else 
			display = String.valueOf(score);
		
		return display;
	}

	private String getMPGScoreDisplay(){
		String display;
		double score = getMPGScore();
		
		if(score < 60)
			display = "Low";
		else 
			display = String.valueOf(score);
		
		return display;
	}
	
	public void fakeDriverScore(double score){
		setDriverScore(score);
		
		notifyRealTimeDriverScoreChanged(Math.round(score));
		
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


	public void startTrip(int odometer, Date time) {

		previousDriverScore = ProfileService.getInstance().getCurrentPlayer().getPreviousDriverScore();
		previousMPGScore = ProfileService.getInstance().getCurrentPlayer().getPreviousMpgScore();
		
		currentDriverScore = ProfileService.getInstance().getCurrentPlayer().getLatestDriverScore();
		currentMPGScore = ProfileService.getInstance().getCurrentPlayer().getLatestMpgScore();

		tripStartOdometer = odometer;
		tripStartTime = time;

		if(calculatorHandle == null || calculatorHandle.isDone() ||calculatorHandle.isCancelled() ){
			
			calculatorHandle = scheduler.scheduleAtFixedRate(scoreCalculator, 15, 15, SECONDS);
			
		}
		
		isMoving = true;
	}

	public void endTrip(int odometer, Date time) {
		
		tripEndOdometer = odometer;
		tripEndTime = time;
		
		if(calculatorHandle != null){
			
			calculatorHandle.cancel(true);
		}
		
		calculateScores();
		
		double mpgScore = getRawMPGScore();
		double driverScore = getRawDriverScore();
		
		ProfileService ps = ProfileService.getInstance();
		
		ps.submitDriverScore(driverScore);
		ps.submitMpgScore(mpgScore);
		
		isMoving = false;
		
	}
	
}

package com.kbb.livedrive.emulator;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.ford.syncV4.proxy.rpc.GPSData;
import com.ford.syncV4.proxy.rpc.OnVehicleData;
import com.ford.syncV4.proxy.rpc.enums.PRNDL;
import com.kbb.livedrive.applink.AppLinkService;
import com.kbb.livedrive.profile.ProfileService;

import android.app.LauncherActivity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

public class VehicleDataEmulatorService extends Service {
	
	public static String ACTION_EMULATED_DATA = "com.kbb.liedrive.VehicleDataEmulatorService.ACTION_EMULATED_DATA";
	
	private static VehicleDataEmulatorService instance = null;
	
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	private ScheduledFuture handle;
	
	private IVehicleDataReceiver sendTo = null;
	
	private Random rand = new Random();
	private static int SPEED_LIMIT = 50;
	
	private static final int DRIVING_MODE_GOOD = 101;
	private static final int DRIVING_MODE_SPEEDING = 102;
	private static final int DRIVING_MODE_SLOW = 103;
	private static final int DRIVING_MODE_RECKLESS = 104;
	private static final int DRIVING_MODE_RACING = 105;

	private int drivingMode = DRIVING_MODE_GOOD;
	
	private EmulatorTrack track;
	private int nextTrackPoint = 0;
	
	private final BroadcastReceiver drivingStateReceiver = new BroadcastReceiver(){
		public void onReceive(android.content.Context context, Intent intent) {
			
			String drivingState = intent.getStringExtra("drivingState");
			long odometer = intent.getLongExtra("odometer", 0);
			if ("PARKED".equals(drivingState)){
				stopRunner();
			}
			
		}
	};

	
	public static VehicleDataEmulatorService getInstance(){
		return instance;
	}
	
	@Override
	public void onCreate() {
		instance = this;
		super.onCreate();
		
        LocalBroadcastManager lbManager = LocalBroadcastManager.getInstance(this);
        lbManager.registerReceiver(drivingStateReceiver, new IntentFilter(AppLinkService.ACTION_VEHICLE_DRIVING_CHANGED));		
		
	}

	@Override
	public IBinder onBind(Intent arg0) {

		return null;
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {

		if(handle != null){
			handle.cancel(true);
		}
		
		super.onDestroy();
	}
	
	public synchronized int getDrivingMode() {
		return drivingMode;
	}
	
	public synchronized void setDrivingMode(int mode){
		drivingMode = mode;
	}

	final Runnable dataSender = new Runnable() {
		
		public void run() {
			
				double currentOdometer = ProfileService.getInstance().getCurrentVehicle().getRawOdometer();
				
				EmulatorTrackPoint currPoint = track.getTrack().get(nextTrackPoint);
				
				nextTrackPoint ++;
				
				if(track.getTrack().size() <= nextTrackPoint){  //if no more data, send Parked - end of trip
					
					Intent emulatedDataIntent = new Intent(VehicleDataEmulatorService.ACTION_EMULATED_DATA);
					setIntentData(emulatedDataIntent, currPoint, 0, PRNDL.PARK, (int) Math.round(currentOdometer));
					LocalBroadcastManager.getInstance(instance).sendBroadcast(emulatedDataIntent);
					    	
					stopRunner();
				}
				else {
					EmulatorTrackPoint nextPoint = track.getTrack().get(nextTrackPoint);
					
					double distance = calculateDistance(currPoint, nextPoint);
					double odometer = currentOdometer + distance;
					
					ProfileService.getInstance().getCurrentVehicle().setOdometer(odometer);
					
					double speed = calculateSpeed(distance, currPoint, nextPoint);
					
					// set intent event with current emulated VehicleData
					Intent emulatedDataIntent = new Intent(VehicleDataEmulatorService.ACTION_EMULATED_DATA);
					
					if(nextTrackPoint == 1) { // if we are at the first step, start in PARKED mode
						
						setIntentData(emulatedDataIntent, currPoint, speed, PRNDL.PARK, (int) Math.round(odometer));
						
					}
					else {
						
						setIntentData(emulatedDataIntent, currPoint, speed, PRNDL.DRIVE, (int) Math.round(odometer));
						
					}
					
					LocalBroadcastManager.getInstance(instance).sendBroadcast(emulatedDataIntent);
					
					
				}
		}

		
		private double deg2rad(double deg) {
			  return (deg * Math.PI / 180.0);
		}
		
		private double rad2deg(double rad) {
			  return (rad * 180 / Math.PI);
		}


		private double calculateDistance(EmulatorTrackPoint start, EmulatorTrackPoint end){
			

			double lat1 = start.getLatitude();
			double lon1 = start.getLongitude();
			double lat2 = end.getLatitude();
			double lon2 = end.getLongitude();
			double theta = lon1 - lon2;

			double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
			
			dist = Math.acos(dist);
			dist = rad2deg(dist);
			dist = dist * 60 * 1.1515;
			return (dist);
		}
		
		private double calculateSpeed(double distance, EmulatorTrackPoint start, EmulatorTrackPoint end) {
			
			// TODO calculate speed from travel coordinates and start/end time
			long diff = end.getTimestamp().getTime() - start.getTimestamp().getTime();
			
			long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
			
			double speed = distance / seconds * 3600;
			
			return speed;
			
		}
				
	};
	
	private void luanchBackgroundWorker(){
    	
    	handle = scheduler.scheduleAtFixedRate(dataSender, 1, 1, SECONDS);
	}
	
	private void setIntentData(Intent intent, EmulatorTrackPoint point, double speed, PRNDL prndl, int odometer){
		
		intent.putExtra("Speed",speed);
		intent.putExtra("Prndl",prndl.toString());
		intent.putExtra("Odometer", odometer);
		intent.putExtra("InstantFuelConsumption", emulateInstantFuelConsumption());

		
		intent.putExtra("LatitudeDegrees",point.getLatitude());
		intent.putExtra("LongitudeDegrees",point.getLongitude());
		intent.putExtra("Altitude",point.getElevation());
		intent.putExtra("UtcYear",point.getTimestamp().getYear());
		intent.putExtra("UtcMonth",point.getTimestamp().getMonth());
		intent.putExtra("UtcDay",point.getTimestamp().getDay());
		intent.putExtra("UtcHours",point.getTimestamp().getHours());
		intent.putExtra("UtcMinutes",point.getTimestamp().getMinutes());
		intent.putExtra("UtcSeconds",point.getTimestamp().getSeconds());
		intent.putExtra("gpsSpeed",speed);
		
	}
	
	private double emulateInstantFuelConsumption(){
		int cityMpg = ProfileService.getInstance().getCurrentVehicle().getCityMPG();
		int hwyMpg = ProfileService.getInstance().getCurrentVehicle().getHwyMPG();
		
		double r = rand.nextDouble();
		
		int realTimeRange = (hwyMpg - cityMpg) * 2;
		
		return hwyMpg - realTimeRange * r;
	}

	private void sendRacingData() {
		
		if(sendTo != null) {
			float currSpeed = 80 + 10 * rand.nextFloat();
			
			OnVehicleData vData = new OnVehicleData();
			vData.setSpeed(Double.valueOf(currSpeed));
			sendTo.onOnVehicleData(vData);
		}
		
	}
    
	private void sendRecklessData() {
		
		if(sendTo != null){
			float currSpeed = SPEED_LIMIT + 6 + 10 * rand.nextFloat();
			
			OnVehicleData vData = new OnVehicleData();
			vData.setSpeed(Double.valueOf(currSpeed));
			sendTo.onOnVehicleData(vData);
		}
		
	}

	private void sendSlowData() {
		
		if(sendTo != null){
			float currSpeed = SPEED_LIMIT - 10 - 5 * rand.nextFloat();
			
			OnVehicleData vData = new OnVehicleData();
			vData.setSpeed(Double.valueOf(currSpeed));
			sendTo.onOnVehicleData(vData);
		}

	}

	private void sendSpeedingData() {
		if(sendTo != null){		
			float currSpeed = SPEED_LIMIT + 8 + 5 * rand.nextFloat();
			
			OnVehicleData vData = new OnVehicleData();
			vData.setSpeed(Double.valueOf(currSpeed));
			sendTo.onOnVehicleData(vData);

		}
	}

	private void sendGoodData() {
		
		if(sendTo != null) {
			float currSpeed = SPEED_LIMIT + 5 - 10 * rand.nextFloat();
			
			OnVehicleData vData = new OnVehicleData();
			vData.setSpeed(Double.valueOf(currSpeed));
			sendTo.onOnVehicleData(vData);
		}
	};
	
	public void loadTrack(int trackIdx){
		
		if(handle != null){
			handle.cancel(true);
		}
		
		switch(trackIdx){
			case 1:
				track = TrackCSVParser.parseFromCsv("20141114182331.csv");
				nextTrackPoint = 0;
				break;
			case 2:
				track = TrackCSVParser.parseFromCsv("20141115034353.csv");
				nextTrackPoint = 0;
				break;
				
		}
		
		luanchBackgroundWorker(); 
	}
	
	private void stopRunner() {
		
		if(handle != null){
			handle.cancel(true);
		}
		
	};
	
	public void interruptTrack(){
		stopRunner();
		
		EmulatorTrackPoint currPoint = track.getTrack().get(nextTrackPoint);
		
		int odometer = ProfileService.getInstance().getCurrentVehicle().getOdometer();

		Intent emulatedDataIntent = new Intent(VehicleDataEmulatorService.ACTION_EMULATED_DATA);
		setIntentData(emulatedDataIntent, currPoint, 0, PRNDL.PARK, odometer);
		LocalBroadcastManager.getInstance(instance).sendBroadcast(emulatedDataIntent);
		
	}


}

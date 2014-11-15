package com.kbb.livedrive.emulator;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import com.ford.syncV4.proxy.rpc.GPSData;
import com.ford.syncV4.proxy.rpc.OnVehicleData;
import com.ford.syncV4.proxy.rpc.enums.PRNDL;

import android.app.LauncherActivity;
import android.app.Service;
import android.content.Intent;
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
	private int lastTrackPoint = 0;

	
	public static VehicleDataEmulatorService getInstance(){
		return instance;
	}
	
	@Override
	public void onCreate() {
		instance = this;
		super.onCreate();
		
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
			
				EmulatorTrackPoint currPoint = track.getTrack().get(lastTrackPoint);
				
				lastTrackPoint ++;
				
				if(track.getTrack().size() <= lastTrackPoint){  //if no more data, send Parked - end of trip
					
					Intent emulatedDataIntent = new Intent(VehicleDataEmulatorService.ACTION_EMULATED_DATA);
					setIntentData(emulatedDataIntent, currPoint, 0, PRNDL.PARK);
					LocalBroadcastManager.getInstance(instance).sendBroadcast(emulatedDataIntent);
					    					
					handle.cancel(true);
				}
				else {
					EmulatorTrackPoint nextPoint = track.getTrack().get(lastTrackPoint);
					
					double speed = calculateSpeed(currPoint, nextPoint);
					Intent emulatedDataIntent = new Intent(VehicleDataEmulatorService.ACTION_EMULATED_DATA);
					setIntentData(emulatedDataIntent, currPoint, speed, PRNDL.DRIVE);
					LocalBroadcastManager.getInstance(instance).sendBroadcast(emulatedDataIntent);
					
					
				}
		}

		private double calculateSpeed(EmulatorTrackPoint start, EmulatorTrackPoint end) {
			
			// TODO calculate speed from travel coordinates and start/end time
			return 100;
			
		}
	};
	
	private void luanchBackgroundWorker(){
    	
    	handle = scheduler.scheduleAtFixedRate(dataSender, 5, 5, SECONDS);
	}
	
	private void setIntentData(Intent intent, EmulatorTrackPoint point, double speed, PRNDL prndl){
		
		intent.putExtra("Speed",speed);
		intent.putExtra("Prndl",prndl);

		
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
				lastTrackPoint = 0;
				break;
			case 2:
				track = TrackCSVParser.parseFromCsv("20141115034353.csv");
				lastTrackPoint = 0;
				break;
				
		}
		
		luanchBackgroundWorker(); 
	}

}

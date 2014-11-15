package com.kbb.livedrive.emulator;

import java.util.Calendar;
import java.util.Date;

import com.ford.syncV4.proxy.rpc.GPSData;
import com.ford.syncV4.proxy.rpc.OnVehicleData;
import com.ford.syncV4.proxy.rpc.enums.PRNDL;

public class EmulatorTrackPoint {
	
	private double latitude;
	private double longitude;
	private double elevation;
	private Date timestamp;
	
	public double getLatitude() {
		return latitude;
	}
	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public double getElevation() {
		return elevation;
	}
	
	public void setElevation(double elevation) {
		this.elevation = elevation;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public OnVehicleData getVehicleData(double speed, PRNDL prndl){
		
		OnVehicleData data = new OnVehicleData();
		
		data.setSpeed(speed);
		data.setPrndl(prndl);

		GPSData gps = new GPSData();
		gps.setLatitudeDegrees(getLatitude());
		gps.setLongitudeDegrees(getLongitude());
		gps.setAltitude(getElevation());
		gps.setUtcYear(getTimestamp().getYear());
		gps.setUtcMonth(getTimestamp().getMonth());
		gps.setUtcDay(getTimestamp().getDay());
		gps.setUtcHours(getTimestamp().getHours());
		gps.setUtcMinutes(getTimestamp().getMinutes());
		gps.setUtcSeconds(getTimestamp().getSeconds());
		gps.setSpeed(speed);
		
		data.setGps(gps);
		

		
		return data;
	
	}
	
	public double getSpeed(EmulatorTrackPoint prevPoint){
		
		//TODO calculate speed from track point
		return 0;
	}

}

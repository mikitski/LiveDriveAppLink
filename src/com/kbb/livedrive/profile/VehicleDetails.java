package com.kbb.livedrive.profile;

public class VehicleDetails {
	
	private String vin;
	private String make;
	private String model;
	private String modelYear;
	private String vehicleName = "2014 Ford Flex Limited";
	private int	hwyMPG = 27;
	private int cityMPG = 34;
	private double odometer = 12736;
	
	
	public static VehicleDetails createDummy() {
		
		VehicleDetails dummy = new VehicleDetails();
		
		
		return dummy;
	}
	

	public String getVIN() {
		return vin;
	}
	private void setVIN(String VIN) {
		vin = VIN;
	}
	public String getMake() {
		return make;
	}
	private void setMake(String make) {
		this.make = make;
	}
	public String getModel() {
		return model;
	}
	private void setModel(String model) {
		this.model = model;
	}
	public String getModelYear() {
		return modelYear;
	}
	private void setModelYear(String modelYear) {
		this.modelYear = modelYear;
	}
	public String getVehicleName() {
		return vehicleName;
	}
	private void setVehicleName(String vehicleName) {
		this.vehicleName = vehicleName;
	}
	public int getHwyMPG() {
		return hwyMPG;
	}
	private void setHwyMPG(int hwyMPG) {
		this.hwyMPG = hwyMPG;
	}
	public int getCityMPG() {
		return cityMPG;
	}
	private void setCityMPG(int cityMPG) {
		this.cityMPG = cityMPG;
	}

	public synchronized int getOdometer() {
		return (int) Math.round(odometer);
	}
	
	public synchronized double getRawOdometer() {
		return odometer;
	}


	public synchronized void setOdometer(double odometer) {
		this.odometer = odometer;
	}

}

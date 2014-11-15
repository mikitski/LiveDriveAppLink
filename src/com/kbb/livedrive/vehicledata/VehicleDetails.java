package com.kbb.livedrive.vehicledata;

public class VehicleDetails {
	
	private String vin;
	private String make;
	private String model;
	private String modelYear;
	private String vehicleName;
	private int	hwyMPG;
	private int cityMPG;
	private int odometer = 12736;
	
	
	public static VehicleDetails createDummy() {
		
		VehicleDetails dummy = new VehicleDetails();
		
		dummy.setCityMPG(27);
		dummy.setHwyMPG(34);
		
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
		return odometer;
	}

	public synchronized void setOdometer(int odometer) {
		this.odometer = odometer;
	}

}

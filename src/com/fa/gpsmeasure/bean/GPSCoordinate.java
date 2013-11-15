package com.fa.gpsmeasure.bean;

public class GPSCoordinate {
	public double latitude;
	public double longitude;
	public double altitude;

	public GPSCoordinate(double latitude, double longitude, double altitude) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
	}
}

package com.fa.gpsmeasure.bean;

import java.util.Vector;

import com.google.android.maps.GeoPoint;

public class Simple {

	private int id;
	private String name;
	private double area;
	private double length;
	private String time;

	private Vector<FlatCoordinate> flatVertices;
	private Vector<GeoPoint> googleVertices;

	private int lastPointUsedForMeasure;

	public Simple() {
		lastPointUsedForMeasure = 0;
		this.flatVertices = new Vector<FlatCoordinate>();
		this.googleVertices = new Vector<GeoPoint>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getArea() {
		return Math.abs(area);
	}

	public void setArea(double area) {
		this.area = area;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public Vector<GeoPoint> getGoogleVertices() {
		return googleVertices;
	}

	public void setGoogleVertices(Vector<GeoPoint> googleVertices) {
		this.googleVertices = googleVertices;
	}

	public int getNumVertices() {
		return flatVertices.size();
	}

	public void addVertex(FlatCoordinate flatCoordinate, GeoPoint geoPoint) {
		flatVertices.add(flatCoordinate);
		googleVertices.add(geoPoint);

		reCalculate();
	}

	public void reCalculate() {// calculate distance and area
		if (lastPointUsedForMeasure != getNumVertices()) {
			if (getNumVertices() > 1) {
				double vector1X;
				double vector1Y;
				double vector2X;
				double vector2Y;

				vector1X = flatVertices.elementAt(lastPointUsedForMeasure).x
						- flatVertices.elementAt(0).x;
				vector1Y = flatVertices.elementAt(lastPointUsedForMeasure).y
						- flatVertices.elementAt(0).y;

				while (lastPointUsedForMeasure < getNumVertices() - 1) {
					vector2X = flatVertices
							.elementAt(lastPointUsedForMeasure + 1).x
							- flatVertices.elementAt(0).x;
					vector2Y = flatVertices
							.elementAt(lastPointUsedForMeasure + 1).y
							- flatVertices.elementAt(0).y;

					length += Math.sqrt((vector2X - vector1X)
							* (vector2X - vector1X) + (vector2Y - vector1Y)
							* (vector2Y - vector1Y));
					if (lastPointUsedForMeasure != 0)
						area += (vector1X * vector2Y - vector1Y * vector2X)
								/ (double) 2;

					vector1X = vector2X;
					vector1Y = vector2Y;

					lastPointUsedForMeasure++;
				}
			}
		}
	}
}

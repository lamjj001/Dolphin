package com.fa.gpsmeasure;

import java.util.LinkedList;
import java.util.Queue;

import com.fa.gpsmeasure.bean.FlatCoordinate;
import com.fa.gpsmeasure.bean.GPSCoordinate;
import com.fa.gpsmeasure.bean.Simple;
import com.fa.gpsmeasure.util.CoordConvert;
import com.fa.gpsmeasure.util.NetworkListener;
import com.google.android.maps.GeoPoint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class Measure {

	public static final int LOGGING_MODE_AUTO = 0;
	public static final int LOGGING_MODE_MANUAL = 1;

	private static final String queueNotNull = "s";

	Context context;
	Handler mHandler;
	private Simple simple;

	private int loggingMode;

	private static boolean needCorrect;

	private static boolean centerPlaced;

	LocationManager locationManager;
	GPSLocationListener gps;

	private double lastLon = 0;
	private double lastLat = 0;

	static Queue<GPSCoordinate> points = new LinkedList<GPSCoordinate>();
	static Queue<FlatCoordinate> coordinates = new LinkedList<FlatCoordinate>();

	public GeoPoint geoPoint;
	FlatCoordinate flatCoordinate;

	public Measure(Context context, Handler mHandler) {
		this.context = context;
		this.mHandler = mHandler;

		setSimple(new Simple());

		locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		gps = new GPSLocationListener();

	}

	public void onStart() {
		needCorrect = true;
		centerPlaced = false;
		gps.clearState();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				1000, 1.0f, gps);

		Thread worker = new Thread(new Worker());
		worker.start();
	}

	public void onStop() {
		gps.clearState();
		locationManager.removeUpdates(gps);
	}

	public void setSimple(Simple simple) {
		this.simple = simple;
	}

	public Simple getSimple() {
		return simple;
	}

	public boolean isAutoLogMode() {
		return loggingMode == LOGGING_MODE_AUTO;
	}

	public void setLoggingAuto() {
		loggingMode = LOGGING_MODE_AUTO;
	}

	public void setLoggingManual() {
		loggingMode = LOGGING_MODE_MANUAL;
	}

	public void setNeedCorrect() {
		this.needCorrect = true;
	}

	public boolean isReadyToStart() {
		return gps.isActive() && !gps.isStarted;

	}

	public boolean isGPSActive() {
		return gps.isActive();
	}

	public boolean isRunning() {
		return gps.isStarted;
	}

	public void start() {
		importFromGPS();
		gps.start();
	}

	public void reset() {

		gps.reset();
		gps.clearState();
		points.clear();
		coordinates.clear();
		geoPoint = null;
		flatCoordinate = null;
		setNeedCorrect();
		setSimple(new Simple());

	}

	public void stop() {
		gps.stop();
	}

	public void importFromGPS() {
		gps.ensureOrigoIsFixed();
		if (flatCoordinate != null && geoPoint != null) {
			simple.addVertex(flatCoordinate, geoPoint);
		}
		mHandler.sendEmptyMessage(0x11);
	}

	private class GPSLocationListener implements LocationListener {

		private static final int radiusEarth = 6378137;

		private double flatXUnitX;
		private double flatXUnitY;
		private double flatXUnitZ;
		private double flatYUnitX;
		private double flatYUnitY;
		private double flatYUnitZ;

		private boolean origoIsFixed;
		private boolean hasRecievedSignal;
		private boolean isStarted;

		public GPSLocationListener() {
			origoIsFixed = false;
			hasRecievedSignal = false;
			isStarted = false;
		}

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub

			double longitude = location.getLongitude();// ||
			double latitude = location.getLatitude();// ----
			double altitude = location.getAltitude();// Height

			if (Math.abs((latitude - lastLat) * 1E6) > 1600
					|| Math.abs((longitude - lastLon) * 1E6) > 1600) {
				needCorrect = true;
			}

			double x = (radiusEarth + altitude)
					* Math.cos(latitude * Math.PI / 180)
					* Math.cos(longitude * Math.PI / 180);
			double y = (radiusEarth + altitude)
					* Math.cos(latitude * Math.PI / 180)
					* Math.sin(longitude * Math.PI / 180);
			double z = (radiusEarth + altitude)
					* Math.sin(latitude * Math.PI / 180);

			if (!origoIsFixed) {
				flatXUnitX = -y / Math.sqrt(x * x + y * y);
				flatXUnitY = x / Math.sqrt(x * x + y * y);
				flatXUnitZ = 0;
				flatYUnitX = (y * flatXUnitZ - z * flatXUnitY);
				flatYUnitY = (z * flatXUnitX - x * flatXUnitZ);
				flatYUnitZ = (x * flatXUnitY - y * flatXUnitX);
				double length = Math.sqrt(flatYUnitX * flatYUnitX + flatYUnitY
						* flatYUnitY + flatYUnitZ * flatYUnitZ);
				flatYUnitX = flatYUnitX / length;
				flatYUnitY = flatYUnitY / length;
				flatYUnitZ = flatYUnitZ / length;
			}

			double spaceX = (radiusEarth + altitude)
					* Math.cos(latitude * Math.PI / 180)
					* Math.cos(longitude * Math.PI / 180);
			double spaceY = (radiusEarth + altitude)
					* Math.cos(latitude * Math.PI / 180)
					* Math.sin(longitude * Math.PI / 180);
			double spaceZ = (radiusEarth + altitude)
					* Math.sin(latitude * Math.PI / 180);

			coordinates.add(new FlatCoordinate(spaceX * flatXUnitX + spaceY
					* flatXUnitY + spaceZ * flatXUnitZ, spaceX * flatYUnitX
					+ spaceY * flatYUnitY + spaceZ * flatYUnitZ));
			points.add(new GPSCoordinate(latitude, longitude, altitude));

			synchronized (queueNotNull) {
				queueNotNull.notify();
			}
		}

		@SuppressLint("ShowToast")
		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			Toast.makeText(context, R.string.gpsout, Toast.LENGTH_LONG).show();
		}

		@SuppressLint("ShowToast")
		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			Toast.makeText(context, R.string.gpsin, Toast.LENGTH_LONG).show();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

		public void start() {
			isStarted = true;
		}

		public void reset() {
			origoIsFixed = false;
		}

		public void stop() {
			isStarted = false;
		}

		public boolean isActive() {
			return hasRecievedSignal;
		}

		public void clearState() {
			hasRecievedSignal = false;
		}

		public void setState() {
			hasRecievedSignal = true;
		}

		public void ensureOrigoIsFixed() {
			// if (!origoIsFixed)
			origoIsFixed = true;
		}
	}

	private class Worker implements Runnable {
		public static final String networkFinish = "s0";

		private double longitude;
		private double latitude;

		private int transLatE6 = 0;
		private int transLonE6 = 0;

		@Override
		public void run() {
			// TODO Auto-generated method stub

			while (true) {

				while (points.isEmpty()) {
					synchronized (queueNotNull) {
						try {
							queueNotNull.notify();
							queueNotNull.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				if (!points.isEmpty()) {

					try {

						flatCoordinate = coordinates.poll();
						GPSCoordinate gpsCoordinate = points.poll();
						latitude = gpsCoordinate.latitude;
						longitude = gpsCoordinate.longitude;

						if (needCorrect) {
							synchronized (networkFinish) {
								CoordConvert.parseGoogle(longitude, latitude,
										new NetworkListener() {

											@Override
											public void onFinish(GeoPoint geoPt) {
												// TODO Auto-generated method
												// stub
												synchronized (networkFinish) {
													geoPoint = geoPt;

													transLatE6 = (int) (geoPoint
															.getLatitudeE6() - latitude * 1E6);
													transLonE6 = (int) (geoPoint
															.getLongitudeE6() - longitude * 1E6);

													lastLat = latitude;
													lastLon = longitude;

													needCorrect = false;

													gps.setState();

													networkFinish.notify();
												}
											}

											@Override
											public void onError() {
												// TODO Auto-generated method
												// stub
												synchronized (networkFinish) {
													geoPoint = new GeoPoint(
															(int) (latitude * 1E6 + transLatE6),
															(int) (longitude * 1E6 + transLonE6));
													networkFinish.notify();
												}
											}
										});
								networkFinish.notify();
								networkFinish.wait();
							}
						} else {
							geoPoint = new GeoPoint(
									(int) (latitude * 1E6 + transLatE6),
									(int) (longitude * 1E6 + transLonE6));
						}

						if (centerPlaced == false) {
							mHandler.sendEmptyMessage(0x12);
							centerPlaced = true;
						}
						
						if (loggingMode == LOGGING_MODE_AUTO && isRunning()) {
							importFromGPS();
						}
						mHandler.sendEmptyMessage(0x14);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
}

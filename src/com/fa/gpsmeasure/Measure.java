package com.fa.gpsmeasure;

import com.fa.gpsmeasure.bean.FlatCoordinate;
import com.fa.gpsmeasure.bean.Simple;
import com.fa.gpsmeasure.util.CoordConvert;
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

	private static int WAVE = 2;

	public static final int LOGGING_MODE_AUTO = 0;
	public static final int LOGGING_MODE_MANUAL = 1;

	Context context;
	Handler mHandler;
	private Simple simple;

	private int loggingMode;

	private boolean needCorrect;

	LocationManager locationManager;
	GPSLocationListener gps;

	private static double longitude = 0;
	private static double latitude = 0;
	private static double altitude = 0;

	private int transLatE6 = 0;
	private int transLonE6 = 0;

	private double lastLon = 0;
	private double lastLat = 0;

	public GeoPoint geoPoint;

	private boolean centerPlaced;

	public Measure(Context context, Handler mHandler) {
		this.context = context;
		this.mHandler = mHandler;

		setSimple(new Simple());

		locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		gps = new GPSLocationListener();
	}

	public void onResume() {
		needCorrect = true;
		centerPlaced = false;
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				1000, 1.0f, gps);

	}

	public void onDestroy() {
		locationManager.removeUpdates(gps);
	}

	public void onPause() {
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
		setSimple(new Simple());
		gps.reset();
	}

	public void stop() {
		gps.stop();
	}

	public void importFromGPS() {
		gps.ensureOrigoIsFixed();
		simple.addVertex(new FlatCoordinate(gps.flatCoordinate.x,
				gps.flatCoordinate.y), geoPoint);
		update();
	}

	public void update() {
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

		public FlatCoordinate flatCoordinate;
		private boolean origoIsFixed;
		private boolean hasRecievedSignal;
		private boolean isStarted;

		public GPSLocationListener() {
			flatCoordinate = new FlatCoordinate(0.0D, 0.0D);
			origoIsFixed = false;
			hasRecievedSignal = false;
			isStarted = false;
		}

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub

			if (WAVE > 0 || (transLatE6 == 0 && transLonE6 == 0)) {
				WAVE--;

				double lat = location.getLatitude();
				double lon = location.getLongitude();
				GeoPoint gp = CoordConvert.parseGoogle(lon, lat);

				if (gp != null) {
					transLatE6 = (int) (gp.getLatitudeE6() - lat * 1E6);
					transLonE6 = (int) (gp.getLongitudeE6() - lon * 1E6);

				}
				return;
			}

			hasRecievedSignal = true;

			longitude = location.getLongitude();// ||
			latitude = location.getLatitude();// ----
			altitude = location.getAltitude();// Height

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

			flatCoordinate.x = spaceX * flatXUnitX + spaceY * flatXUnitY
					+ spaceZ * flatXUnitZ;
			flatCoordinate.y = spaceX * flatYUnitX + spaceY * flatYUnitY
					+ spaceZ * flatYUnitZ;

			if (needCorrect) {
				GeoPoint gp = CoordConvert.parseGoogle(longitude, latitude);
				if (gp != null) {
					transLatE6 = (int) (gp.getLatitudeE6() - latitude * 1E6);
					transLonE6 = (int) (gp.getLongitudeE6() - longitude * 1E6);

					lastLat = latitude;
					lastLon = longitude;

					needCorrect = false;
				}
			}

			geoPoint = new GeoPoint((int) (latitude * 1E6 + transLatE6),
					(int) (longitude * 1E6 + transLonE6));

			if (centerPlaced == false) {

				mHandler.sendEmptyMessage(0x12);

				centerPlaced = true;
			}

			if (loggingMode == LOGGING_MODE_AUTO && isStarted) {
				importFromGPS();
			}
		}

		@SuppressLint("ShowToast")
		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			Toast.makeText(context, R.string.gpsout, Toast.LENGTH_LONG);
		}

		@SuppressLint("ShowToast")
		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			Toast.makeText(context, R.string.gpsin, Toast.LENGTH_LONG);
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

		public void ensureOrigoIsFixed() {
			if (!origoIsFixed) {
				origoIsFixed = true;
			}
		}
	}
}

package com.fa.gpsmeasure.util;

import com.google.android.maps.GeoPoint;

public interface NetworkListener {
	public void onFinish(GeoPoint geoPoint);

	public void onError();
}

package com.fa.gpsmeasure;

import java.text.DecimalFormat;
import java.util.List;

import com.fa.gpsmeasure.db.GeoPointHelper;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

public class ReviewActivity extends MapActivity {

	// map
	MapView mapView;
	MapController controller;
	List<Overlay> mapOverlays;

	// widget
	TextView nameText, timeText, distanceText, areaText;
	int id;
	String name, time;
	double distance, area;

	// paint
	Paint linePaint;
	Paint closelinePaint;
	Paint paint;
	Paint origoPaint;
	Paint txtPaint;

	// overlay
	PathOverlay pathOverlay;

	// points
	List<GeoPoint> points;

	DecimalFormat df = new DecimalFormat("#.000");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_review);

		id = getIntent().getExtras().getInt("id");
		name = getIntent().getExtras().getString("name");
		time = getIntent().getExtras().getString("time");
		distance = getIntent().getExtras().getDouble("length");
		area = getIntent().getExtras().getDouble("area");

		widgetInit();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		paintInit();
		mapInit();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		new LoadPointsTask().execute(id);
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	private void paintInit() {
		this.linePaint = new Paint();
		linePaint.setColor(Color.argb(192, 0x78, 0xba, 0));
		linePaint.setAntiAlias(true);
		linePaint.setDither(true);
		linePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		linePaint.setStrokeJoin(Paint.Join.ROUND);
		linePaint.setStrokeCap(Paint.Cap.ROUND);
		linePaint.setStrokeWidth(3);

		this.closelinePaint = new Paint();
		closelinePaint.setColor(Color.argb(128, 0, 0x72, 0xc6));
		closelinePaint.setAntiAlias(true);
		closelinePaint.setDither(true);
		closelinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		closelinePaint.setStrokeJoin(Paint.Join.ROUND);
		closelinePaint.setStrokeCap(Paint.Cap.ROUND);
		closelinePaint.setStrokeWidth(3);

		this.paint = new Paint();
		paint.setColor(Color.argb(96, 0xff, 0xff, 0));
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setStyle(Paint.Style.FILL);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);

		origoPaint = new Paint();
		origoPaint.setColor(Color.argb(192, 0xae, 0x11, 0x3d));
		origoPaint.setAntiAlias(true);
		origoPaint.setStyle(Paint.Style.STROKE);

		txtPaint = new Paint();
		txtPaint.setColor(0xae113d);
		txtPaint.setAlpha(192);
		txtPaint.setTextSize(25);
		txtPaint.setAntiAlias(true);
	}

	private void mapInit() {
		// 获得界面上MapView对象
		mapView = (MapView) findViewById(R.id.mapview);

		// 设置显示放大、缩小的按钮
		mapView.setBuiltInZoomControls(true);
		// 创建MapController对象
		controller = mapView.getController();
		controller.setZoom(18);

		mapOverlays = mapView.getOverlays();

	}

	private void widgetInit() {
		nameText = (TextView) findViewById(R.id.name);
		timeText = (TextView) findViewById(R.id.time);
		distanceText = (TextView) findViewById(R.id.distance);
		areaText = (TextView) findViewById(R.id.area);

		nameText.setText(name);
		timeText.setText(time);

		distanceText.setText("距离: " + df.format(distance) + " m");
		areaText.setText("面积: " + df.format(area) + " m\u00b2");
	}

	class LoadPointsTask extends AsyncTask<Integer, Integer, String> {

		@Override
		protected String doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			GeoPointHelper geoPointHelper = new GeoPointHelper(
					getApplicationContext());
			points = geoPointHelper.queryFromGeoPoint(params[0]);
			geoPointHelper.close();
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (points != null) {
				pathOverlay = new PathOverlay();
				mapOverlays.add(pathOverlay);
			}
		}

	}

	class PathOverlay extends Overlay {

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			// TODO Auto-generated method stub
			super.draw(canvas, mapView, shadow);
			if (points != null) {

				Projection projection = mapView.getProjection();

				int size = points.size();
				Path path = new Path();
				for (int i = 0; i < size; i++) {
					Point p0 = new Point();
					Point p1 = new Point();

					projection.toPixels(points.get(i), p0);
					projection.toPixels(points.get((i + 1) % size), p1);

					if (i + 1 != size) {
						canvas.drawLine(p0.x, p0.y, p1.x, p1.y, linePaint);
					} else {
						canvas.drawLine(p0.x, p0.y, p1.x, p1.y, closelinePaint);
					}

					if (i == 0) {
						path.moveTo(p0.x, p0.y);
						canvas.drawCircle(p0.x, p0.y, 6, origoPaint);
						canvas.drawCircle(p0.x, p0.y, 1, origoPaint);
					} else {
						path.lineTo(p0.x, p0.y);
					}
				}
				path.close();
				canvas.drawPath(path, paint);// 画出路径
			}
		}
	}

}

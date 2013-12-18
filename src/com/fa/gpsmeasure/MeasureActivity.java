package com.fa.gpsmeasure;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.fa.gpsmeasure.bean.Simple;
import com.fa.gpsmeasure.db.GeoPointHelper;
import com.fa.gpsmeasure.db.SimpleHelper;
import com.fa.gpsmeasure.util.NetworkDetector;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MeasureActivity extends MapActivity {

	// map
	MapView mapView;
	MapController controller;
	List<Overlay> mapOverlays;
	Bitmap posBitmap;

	// compass
	SensorManager sensorManager;
	Bitmap compassImage;
	Matrix matrix;
	int imageWidth;
	int imageHeight;

	// paint
	Paint linePaint;
	Paint closelinePaint;
	Paint paint;
	Paint origoPaint;

	// overlay
	CenterOverLay centerOverLay;
	CompassOverlay compassOverlay;
	PathOverlay pathOverlay;

	// measure
	Measure measure;

	// widget
	TextView logmode, distance, area;
	Button startButton, saveButon, resetButton;

	// log mode record sp
	SharedPreferences sp;

	// timer task
	Timer timer;
	TimerTask task;

	DecimalFormat df = new DecimalFormat("#.000");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_measure);

		matrix = new Matrix();
		measure = new Measure(this, mHandler);

		sp = getSharedPreferences("setinfo", Context.MODE_PRIVATE);

		widgetInit();
		mapInit();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		testNetWork();
		checkGPS();

		paintInit();
		compassInit();
		measure.onStart();

		if (timer == null) {
			createTimerTask();
			timer = new Timer();
			timer.schedule(task, 3000, 10000);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (timer != null) {
			timer.cancel();
			timer.purge();
			timer = null;
		}
		measure.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	private void paintInit() {
		if (linePaint == null) {
			linePaint = new Paint();
		}
		linePaint.setColor(Color.argb(192, 0x78, 0xba, 0));
		linePaint.setAntiAlias(true);
		linePaint.setDither(true);
		linePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		linePaint.setStrokeJoin(Paint.Join.ROUND);
		linePaint.setStrokeCap(Paint.Cap.ROUND);
		linePaint.setStrokeWidth(3);

		if (closelinePaint == null) {
			closelinePaint = new Paint();
		}
		closelinePaint.setColor(Color.argb(128, 0, 0x72, 0xc6));
		closelinePaint.setAntiAlias(true);
		closelinePaint.setDither(true);
		closelinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		closelinePaint.setStrokeJoin(Paint.Join.ROUND);
		closelinePaint.setStrokeCap(Paint.Cap.ROUND);
		closelinePaint.setStrokeWidth(3);

		if (paint == null) {
			paint = new Paint();
		}
		paint.setColor(Color.argb(96, 0xff, 0xff, 0));
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setStyle(Paint.Style.FILL);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);

		if (origoPaint == null) {
			origoPaint = new Paint();
		}
		origoPaint.setColor(Color.argb(192, 0xae, 0x11, 0x3d));
		origoPaint.setAntiAlias(true);
		origoPaint.setStyle(Paint.Style.STROKE);
	}

	private void mapInit() {
		// 获得界面上MapView对象
		if (mapView == null) {
			mapView = (MapView) findViewById(R.id.mv);
			// 设置显示放大、缩小的按钮
			mapView.setBuiltInZoomControls(true);
			// 创建MapController对象
			controller = mapView.getController();
			controller.setZoom(18);

			mapOverlays = mapView.getOverlays();

		}
		if (posBitmap == null) {
			posBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.center_overlay);
		}
	}

	private void compassInit() {

		if (compassImage == null) {
			compassImage = Bitmap.createScaledBitmap(BitmapFactory
					.decodeResource(getResources(), R.drawable.compass_needle),
					99, 99, true);

			imageWidth = compassImage.getWidth();
			imageHeight = compassImage.getHeight();
		}

		sensorManager = (SensorManager) MeasureActivity.this
				.getSystemService(Context.SENSOR_SERVICE);

		sensorManager.registerListener(new CompassSensorEventListener(),
				sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_GAME);
	}

	private void testNetWork() {
		if (!NetworkDetector.detect(this)) {
			Toast.makeText(getApplicationContext(), "请检查网络链接",
					Toast.LENGTH_LONG).show();
		}
	}

	private void checkGPS() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

			new AlertDialog.Builder(MeasureActivity.this)
					.setTitle(R.string.gpsservice)
					.setMessage(R.string.gpsmsg)
					.setCancelable(false)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									MeasureActivity.this
											.startActivity(new Intent(
													Settings.ACTION_LOCATION_SOURCE_SETTINGS));
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									Toast.makeText(getApplicationContext(),
											R.string.gpswarning,
											Toast.LENGTH_LONG).show();
									finish();
								}
							}).show();

		}
	}

	private void widgetInit() {
		logmode = (TextView) findViewById(R.id.logmode);
		distance = (TextView) findViewById(R.id.distance);
		area = (TextView) findViewById(R.id.area);

		startButton = (Button) findViewById(R.id.startBn);
		saveButon = (Button) findViewById(R.id.saveBn);
		resetButton = (Button) findViewById(R.id.resetBn);

		if ("MANUAL".equals(sp.getString("logmode", "AUTO"))) {
			logmode.setText(R.string.manumark);
			measure.setLoggingManual();
			startButton.setText(R.string.mark);
			startButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (measure.isGPSActive()) {
						measure.importFromGPS();
					} else {
						Toast.makeText(getApplicationContext(),
								"GPS信号正在链接 请稍候..", Toast.LENGTH_LONG).show();
					}
				}
			});
		} else {
			logmode.setText(R.string.automark);
			measure.setLoggingAuto();
			startButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (measure.isGPSActive()) {
						if (measure.isReadyToStart()) {
							measure.start();
							startButton.setText(R.string.stop);
						} else if (measure.isRunning()) {
							measure.stop();
							startButton.setText(R.string.start);
						}
					} else {
						Toast.makeText(getApplicationContext(),
								"GPS信号正在链接 请稍候..", Toast.LENGTH_LONG).show();
					}
				}
			});
		}

		saveButon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final EditText mEditText = new EditText(MeasureActivity.this);
				new AlertDialog.Builder(MeasureActivity.this)
						.setTitle("请输入样本名称")
						.setView(mEditText)
						.setCancelable(true)
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										String name = mEditText
												.getEditableText().toString();
										if (name.length() == 0) {
											Toast.makeText(
													getApplicationContext(),
													"请输入样本名称",
													Toast.LENGTH_SHORT).show();
											return;
										} else {
											new SaveSimpleTask().execute(name);
										}
									}
								}).setNegativeButton("取消", null).show();

			}
		});

		resetButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(MeasureActivity.this)
						.setTitle("确定重置?")
						.setCancelable(true)
						.setPositiveButton(R.string.ok,
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {
										measure.reset();
										updateView();
									}
								}).setNegativeButton(R.string.cancel, null)
						.show();
			}
		});
	}

	private void createTimerTask() {
		task = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mHandler.sendEmptyMessage(0x13);
			}

		};
	}

	private void updateView() {
		if (pathOverlay != null) {
			mapOverlays.remove(pathOverlay);
		}
		if (measure.getSimple().getGoogleVertices() != null) {
			pathOverlay = new PathOverlay();
			mapOverlays.add(pathOverlay);
		}

		mapView.invalidate();

		distance.setText("距离: " + df.format(measure.getSimple().getLength())
				+ " m");
		area.setText("面积: " + df.format(measure.getSimple().getArea())
				+ " m\u00b2");
	}

	private void updateCenter() {
		if (measure.geoPoint != null) {
			if (centerOverLay != null) {
				mapOverlays.remove(centerOverLay);
			}
			centerOverLay = new CenterOverLay();
			mapOverlays.add(centerOverLay);
			mapView.invalidate();
		}
	}

	private void animateToCenter() {
		if (measure.geoPoint != null) {
			controller.animateTo(measure.geoPoint);
			mapView.invalidate();
		}
	}

	class SaveSimpleTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			SimpleHelper simpleHelper = new SimpleHelper(
					getApplicationContext());
			Simple simple = measure.getSimple();
			simple.setName(params[0]);

			simpleHelper.insertIntoSimple(simple);
			int sid = simpleHelper.getLastSimpleID();
			simpleHelper.close();

			GeoPointHelper geoPointHelper = new GeoPointHelper(
					getApplicationContext());
			geoPointHelper.insertIntoGeoPoint(simple.getGoogleVertices(), sid);
			geoPointHelper.close();

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT)
					.show();
		}

	}

	class PathOverlay extends Overlay {
		List<GeoPoint> points;

		public PathOverlay() {
			points = measure.getSimple().getGoogleVertices();
		}

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

	public class CenterOverLay extends Overlay {

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			if (!shadow) {

				if (measure.geoPoint != null) {
					// 获取MapView的Projection对象
					Projection proj = mapView.getProjection();
					Point p = new Point();
					// 将真实的地理坐标转化为屏幕上的坐标
					proj.toPixels(measure.geoPoint, p);
					// 在指定位置绘制图片
					canvas.drawBitmap(posBitmap,
							p.x - posBitmap.getWidth() / 2,
							p.y - posBitmap.getHeight(), null);
				}
			}
		}
	}

	public class CompassOverlay extends Overlay {

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			// TODO Auto-generated method stub
			super.draw(canvas, mapView, shadow);
			canvas.drawBitmap(compassImage, matrix, null);
		}

	}

	private class CompassSensorEventListener implements SensorEventListener {

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub

			int sensorType = event.sensor.getType();
			switch (sensorType) {
			case Sensor.TYPE_ORIENTATION:

				float direction = event.values[0] * -1.0f;
				float mTargetDirection = normalizeDegree(direction);// 赋值给全局变量，让指南针旋转
				matrix.setRotate(mTargetDirection, imageWidth / 2 + 1,
						imageHeight / 2 + 1);
				matrix.postTranslate(getWindowManager().getDefaultDisplay()
						.getWidth() - 99, 0);
			}

			if (compassOverlay != null) {
				mapOverlays.remove(compassOverlay);
			}

			compassOverlay = new CompassOverlay();
			mapOverlays.add(compassOverlay);
			mapView.invalidate();
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}

		private float normalizeDegree(float degree) {
			return (degree + 720) % 360;
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			switch (msg.what) {
			case 0x11:
				updateView();
				break;

			case 0x12:
				animateToCenter();
				break;

			case 0x13:
				measure.setNeedCorrect();
				animateToCenter();
				break;

			case 0x14:
				updateCenter();
				break;
			default:
				break;
			}
		}
	};

}

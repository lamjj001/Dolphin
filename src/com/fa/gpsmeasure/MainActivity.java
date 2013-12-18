package com.fa.gpsmeasure;

import com.fa.gpsmeasure.db.DBHelper;
import com.fa.gpsmeasure.util.NetworkDetector;

import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		new CreateDBTask().execute("");
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		testNetWork();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		finish();
		return super.onOptionsItemSelected(item);
	}

	public void startMeasure(View view) {
		if (!NetworkDetector.detect(this)) {
			Toast.makeText(getApplicationContext(), "请检查网络链接",
					Toast.LENGTH_LONG).show();
			return;
		}

		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

			new AlertDialog.Builder(MainActivity.this)
					.setTitle(R.string.gpsservice)
					.setMessage(R.string.gpsmsg)
					.setCancelable(false)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									MainActivity.this
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
								}
							}).show();

		} else {
			startActivity(new Intent(MainActivity.this, MeasureActivity.class));
		}
	}

	public void startHistory(View view) {
		startActivity(new Intent(MainActivity.this, HistoryActivity.class));
	}

	public void startSet(View view) {
		startActivity(new Intent(MainActivity.this, SetActivity.class));
	}

	private void testNetWork() {
		if (!NetworkDetector.detect(this)) {
			Toast.makeText(getApplicationContext(), "请检查网络链接",
					Toast.LENGTH_LONG).show();
		}
	}

	class CreateDBTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			DBHelper DBManager = new DBHelper(getApplicationContext());
			DBManager.close();
			return null;
		}
	}
}

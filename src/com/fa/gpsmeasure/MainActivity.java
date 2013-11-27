package com.fa.gpsmeasure;

import com.fa.gpsmeasure.db.DBHelper;
import com.fa.gpsmeasure.util.NetworkDetector;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
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
		new CreateDBTask().execute("");
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
		startActivity(new Intent(MainActivity.this, MeasureActivity.class));
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

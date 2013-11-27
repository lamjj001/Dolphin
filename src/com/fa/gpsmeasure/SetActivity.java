package com.fa.gpsmeasure;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;

public class SetActivity extends Activity {

	// log mode record sp
	SharedPreferences sp;
	String currentMode;

	ImageView imageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set);

		imageView = (ImageView) findViewById(R.id.logmodeBn);
		sp = getSharedPreferences("setinfo", Context.MODE_PRIVATE);
		currentMode = sp.getString("logmode", "AUTO");
		if ("MANUAL".equals(currentMode)) {
			imageView.setImageResource(R.drawable.log_mode_manu);
		} else {
			imageView.setImageResource(R.drawable.log_mode_auto);
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	public void setLogMode(View view) {
		if ("MANUAL".equals(currentMode)) {
			imageView.setImageResource(R.drawable.log_mode_auto);
			currentMode = "AUTO";
		} else {
			imageView.setImageResource(R.drawable.log_mode_manu);
			currentMode = "MANUAL";
		}
		sp.edit().putString("logmode", currentMode).commit();
	}

	public void showHelpInfo(View view) {
		Dialog dialog = new Dialog(this, R.style.mydialog);
		dialog.setOwnerActivity(this);
		dialog.setCancelable(true);

		dialog.setContentView(R.layout.helpinfo);

		dialog.show();
	}

	public void showAboutInfo(View view) {
		Dialog dialog = new Dialog(this, R.style.mydialog);
		dialog.setOwnerActivity(this);
		dialog.setCancelable(true);

		dialog.setContentView(R.layout.aboutinfo);

		dialog.show();
	}
}

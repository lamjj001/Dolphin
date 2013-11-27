package com.fa.gpsmeasure;

import java.text.DecimalFormat;
import java.util.List;

import com.fa.gpsmeasure.bean.Simple;
import com.fa.gpsmeasure.db.SimpleHelper;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

public class HistoryActivity extends Activity {

	ListView listView;
	List<Simple> simpleList;
	SimpleListAdapter simpleListAdapter;

	DecimalFormat df = new DecimalFormat("#.000");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);

		listView = (ListView) findViewById(R.id.simpleList);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		widgetInit();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		new LoadSimplesTask().execute("");
	}

	private void widgetInit() {

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				Simple simple = simpleList.get(position);
				Intent intent = new Intent(HistoryActivity.this,
						ReviewActivity.class);
				intent.putExtra("id", simple.getId());
				intent.putExtra("name", simple.getName());
				intent.putExtra("area", simple.getArea());
				intent.putExtra("length", simple.getLength());
				intent.putExtra("time", simple.getTime());
				HistoryActivity.this.startActivity(intent);
			}
		});
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int position, long arg3) {
				// TODO Auto-generated method stub

				new AlertDialog.Builder(HistoryActivity.this)
						.setTitle("删除该条记录?")
						.setCancelable(true)
						.setPositiveButton(R.string.ok,
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {
										SimpleHelper simpleHelper = new SimpleHelper(
												getApplicationContext());
										simpleHelper
												.deleteFromSimple(simpleList
														.get(position).getId());
										simpleHelper.close();

										new LoadSimplesTask().execute("");
									}
								}).setNegativeButton(R.string.cancel, null)
						.show();

				return true;
			}
		});
	}

	class LoadSimplesTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			SimpleHelper simpleHelper = new SimpleHelper(
					getApplicationContext());
			simpleList = simpleHelper.queryFromSimple();
			simpleHelper.close();
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			if (simpleList != null) {
				simpleListAdapter = new SimpleListAdapter(simpleList);
				listView.setAdapter(simpleListAdapter);
				listView.invalidate();
			} else {
				findViewById(R.id.nohistory).setVisibility(View.VISIBLE);
			}
		}
	}

	class SimpleListAdapter extends BaseAdapter {
		List<Simple> list;

		public SimpleListAdapter(List<Simple> list) {
			super();
			this.list = list;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (view == null) {
				view = getLayoutInflater().inflate(R.layout.simple_item, null);
			}
			TextView name = (TextView) view.findViewById(R.id.name);
			TextView area = (TextView) view.findViewById(R.id.area);
			TextView distance = (TextView) view.findViewById(R.id.distance);
			TextView time = (TextView) view.findViewById(R.id.time);

			name.setText(list.get(position).getName());
			area.setText("面积: " + df.format(list.get(position).getArea())
					+ " m\u00b2");
			distance.setText("距离: " + df.format(list.get(position).getLength())
					+ " m");
			time.setText(list.get(position).getTime());

			return view;
		}

	}
}

package com.fa.gpsmeasure.db;

import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.GeoPoint;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class GeoPointHelper {
	private DBHelper.DatabaseHelper dbHelper;
	private SQLiteDatabase db;

	public GeoPointHelper(Context context) {
		dbHelper = new DBHelper.DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
		db.close();
	}

	public void insertIntoGeoPoint(List<GeoPoint> list, int sid) {
		for (int i = 0; i < list.size(); i++) {
			GeoPoint geoPoint = list.get(i);
			ContentValues values = new ContentValues();
			values.put("latitudeE6", geoPoint.getLatitudeE6());
			values.put("longitudeE6", geoPoint.getLongitudeE6());
			values.put("sid", sid);
			db.insert("geopoint", null, values);
		}
	}

	public List<GeoPoint> queryFromGeoPoint(int sid) {
		List<GeoPoint> list = null;

		StringBuilder sb = new StringBuilder(
				"select latitudeE6,longitudeE6 from geopoint where sid = ");
		sb.append(sid);
		sb.append(" order by id ;");
		Cursor cursor = db.rawQuery(sb.toString(), null);
		if (cursor.getCount() != 0) {

			list = new ArrayList<GeoPoint>();

			cursor.moveToFirst();
			int count = cursor.getCount();
			for (int i = 0; i < count; i++) {

				GeoPoint geoPoint = new GeoPoint(cursor.getInt(0),
						cursor.getInt(1));
				list.add(geoPoint);

				cursor.moveToNext();
			}
		}
		cursor.close();
		return list;
	}
}

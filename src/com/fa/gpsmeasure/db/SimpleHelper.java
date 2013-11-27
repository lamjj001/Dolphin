package com.fa.gpsmeasure.db;

import java.util.ArrayList;
import java.util.List;

import com.fa.gpsmeasure.bean.Simple;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SimpleHelper {

	private DBHelper.DatabaseHelper dbHelper;
	private SQLiteDatabase db;

	public SimpleHelper(Context context) {
		dbHelper = new DBHelper.DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
		db.close();
	}

	public void insertIntoSimple(Simple simple) {
		ContentValues values = new ContentValues();
		values.put("name", simple.getName());
		values.put("area", simple.getArea());
		values.put("length", simple.getLength());
		db.insert("simple", null, values);
	}

	public int getLastSimpleID() {
		int id = 0;
		String sql = "select max(id) from simple;";

		Cursor cursor = db.rawQuery(sql, null);
		if (cursor.getCount() != 0) {
			cursor.moveToFirst();
			id = cursor.getInt(0);
		}

		cursor.close();
		return id;
	}

	public List<Simple> queryFromSimple() {
		List<Simple> simpleList = null;

		String sql = "select * from simple order by id desc ;";
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor.getCount() != 0) {

			simpleList = new ArrayList<Simple>();

			cursor.moveToFirst();
			for (int i = 0; i < cursor.getCount(); i++) {

				Simple simple = new Simple();
				simple.setId(cursor.getInt(0));
				simple.setName(cursor.getString(1));
				simple.setArea(cursor.getDouble(2));
				simple.setLength(cursor.getDouble(3));
				simple.setTime(cursor.getString(4));
				simpleList.add(simple);

				cursor.moveToNext();
			}
		}
		cursor.close();
		return simpleList;
	}

	public void deleteFromSimple(int id) {
		String sql = "Delete from simple where id = " + id + ";";
		db.execSQL(sql);
	}
}

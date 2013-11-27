package com.fa.gpsmeasure.db;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper {

	private SQLiteDatabase db;
	private DatabaseHelper dbHelper;
	public final static byte[] _writeLock = new byte[0];

	// 在构造函数中打开数据库 可以节约代码行数
	public DBHelper(Context context) {
		this.dbHelper = new DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
	}

	public boolean isOpen() {
		if (db == null)
			return false;
		return db.isOpen();
	}

	// 打开数据库
	public void openDB(Context context) {
		if (isOpen()) {
			return;
		}

		dbHelper = new DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
	}

	// 关闭数据库
	public void close() {
		dbHelper.close();
		if (db != null) {
			db.close();
		}
	}

	/**
	 * 插入
	 * 
	 * @param list
	 * @param table
	 *            表名
	 */
	public void insert(List<ContentValues> list, String tableName) {
		synchronized (_writeLock) {
			db.beginTransaction();
			try {
				db.delete(tableName, null, null);
				for (int i = 0, len = list.size(); i < len; i++)
					db.insert(tableName, null, list.get(i));
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}
	}

	/**
	 * 用于初始化数据库
	 * 
	 * @author Administrator
	 * 
	 */
	public static class DatabaseHelper extends SQLiteOpenHelper {
		// 定义数据库文件
		private static final String DB_NAME = "GPSDB";
		// 定义数据库版本
		private static final int DB_VERSION = 1;

		public DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);
			// Enable foreign key constraints
			db.execSQL("PRAGMA foreign_keys=ON;");
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			createSimpleTable(db);
			Log.i("createUserTable--->", "创建 simple表 成功！");

			createGeoPointTable(db);
			Log.i("createElectronicTable--->", "创建geopoint表 成功！");

		}

		private void createSimpleTable(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE [simple] (");
			sb.append("[id] INTEGER PRIMARY KEY AUTOINCREMENT ,");
			sb.append("[name] NVARCHAR(30) NOT NULL ,");
			sb.append("[area] DOUBLE NOT NULL ,");
			sb.append("[length] DOUBLE NOT NULL ,");
			sb.append("[time] datetime default (datetime('now', 'localtime')) ); ");

			db.execSQL(sb.toString());
		}

		private void createGeoPointTable(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE [geopoint] (");
			sb.append("[id] INTEGER PRIMARY KEY AUTOINCREMENT,");
			sb.append("[latitudeE6] INTEGER NOT NULL ,");
			sb.append("[longitudeE6] INTEGER NOT NULL ,");
			sb.append("[sid] INTEGER NOT NULL, ");
			sb.append("foreign key(sid) references simple(id) on delete cascade on update cascade );");

			db.execSQL(sb.toString());
		}

		/**
		 * 更新版本时更新表
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			DropTable(db);
			onCreate(db);
			Log.e("database", "onUpgrade");
		}

		/**
		 * 删除表
		 * 
		 * @param db
		 */
		private void DropTable(SQLiteDatabase db) {

			String str = "DROP TABLE IF EXISTS geopoint ;";
			db.execSQL(str);

			str = "DROP TABLE IF EXISTS simple ;";
			db.execSQL(str);

		}

		/**
		 * 清空数据表（仅清空无用数据）
		 * 
		 * @param db
		 */
		public static void ClearData(Context context) {
			DatabaseHelper dbHelper = new DBHelper.DatabaseHelper(context);
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.execSQL("DELETE FROM geopoint");
			db.execSQL("DELETE FROM simple");

			dbHelper.close();
		}
	}
}

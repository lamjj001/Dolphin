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
		private static final String DB_NAME = "electronic";
		// 定义数据库版本
		private static final int DB_VERSION = 1;

		public DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			createUserTable(db);
			Log.i("createUserTable--->", "创建 User 表 成功！");

			createElectronicTable(db);
			Log.i("createElectronicTable--->", "创建 Elec 表 成功！");

			createHistoryTable(db);
			Log.i("createHistoryTable--->", "创建 History 表 成功！");

			createRecommendAppTable(db);
			Log.i("createRecommendAppTable--->", "创建 RecommendApp 表 成功！");
		}

		/**
		 * 
		 * 创建用户表 包括 / 手机IMEI / 当前软件版本 可用于查询更新 /
		 */
		private void createUserTable(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE [user] (");
			sb.append("[IMEI] NVARCHAR(20) PRIMARY KEY NOT NULL DEFAULT ('0') , ");
			sb.append("[Version] NVARCHAR(10) NOT NULL DEFAULT ('0') , ");
			sb.append("[AppTime] NVARCHAR(20) NOT NULL DEFAULT ('0') );");

			db.execSQL(sb.toString());
		}

		/**
		 * 
		 * 创建电子元件表 /
		 */
		private void createElectronicTable(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE [electronic] (");

			sb.append("[DocId] INTEGER(11) PRIMARY KEY NOT NULL , ");
			sb.append("[DocIdFormat] NVARCHAR(6) NOT NULL, ");
			sb.append("[PartNoName] NVARCHAR(20) NOT NULL, ");
			sb.append("[Description] NVARCHAR(60) , ");
			sb.append("[CNDescription] NVARCHAR(60) , ");
			sb.append("[Manufacturer] NVARCHAR(20) , ");
			sb.append("[PdfUrl] NVARCHAR(80) , ");
			sb.append("[Spec] NVARCHAR(400) , ");
			sb.append("[PartNoImg] NVARCHAR(300) , ");

			sb.append("[Count] INTEGER(8) NOT NULL DEFAULT (1), ");
			sb.append("[Down] INTEGER(1) NOT NULL DEFAULT (0));");

			db.execSQL(sb.toString());

			String sql = "create index searchname on Electronic(PartNoName);";

			db.execSQL(sql);
		}

		private void createHistoryTable(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE [history] (");
			sb.append("[Keyword] NVARCHAR(50) PRIMARY KEY NOT NULL, ");
			sb.append("[VisitTime] INTEGER(13)) ");

			db.execSQL(sb.toString());

			String sql = "create index searchvisit on history(VisitTime);";

			db.execSQL(sql);
		}

		// /**
		// *
		// * 创建搜索历史表 包括 / ID / 电子元件名称 / 最近查阅日期 /
		// */
		// private void createHistoryRecordTable(SQLiteDatabase db) {
		// StringBuilder sb = new StringBuilder();
		// sb.append("CREATE TABLE [historyrecord] (");
		// sb.append("[DocId] INTEGER(11) PRIMARY KEY NOT NULL , ");
		// sb.append("[DocIdFormat] NVARCHAR(10) NOT NULL, ");
		// sb.append("[PartNoName] NVARCHAR(20)  NOT NULL, ");
		// sb.append("[VisitTime] INTEGER(13)) ");
		//
		// db.execSQL(sb.toString());
		//
		// String sql = "create index searchvisit on historyrecord(VisitTime);";
		//
		// db.execSQL(sql);
		// }

		private void createRecommendAppTable(SQLiteDatabase db) {

			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE [recommendapp] (");
			sb.append("[Id] INTEGER(8) PRIMARY KEY NOT NULL , ");
			sb.append("[Name] NVARCHAR(30) NOT NULL, ");
			sb.append("[ImgUrl] NVARCHAR(80)  NOT NULL, ");
			sb.append("[DownUrl] NVARCHAR(80) ); ");

			db.execSQL(sb.toString());

			String sql = "create index searchimg on recommendapp(ImgUrl);";

			db.execSQL(sql);
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

			String str = "DROP TABLE IF EXISTS recommendapp ;";
			db.execSQL(str);

			str = "DROP TABLE IF EXISTS history ;";
			db.execSQL(str);

			str = "DROP TABLE IF EXISTS electronic ;";
			db.execSQL(str);

			str = "DROP TABLE IF EXISTS user ;";
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
			db.execSQL("DELETE FROM recommendapp");// 清空recommendapp表
			db.execSQL("DELETE FROM history");// 清空History 表
			db.execSQL("DELETE FROM electronic");// 清空Electronic表
			db.execSQL("DELETE FROM user");// 清空User表

			dbHelper.close();
		}
	}
}

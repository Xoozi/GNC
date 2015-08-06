package com.gnc.dcqtech.project;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.gnc.dcqtech.project.GPSDataBase.GPSPointsColumns;
import com.gnc.dcqtech.utils.AppPreference;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

public abstract class GPSTrackAdapter {
	private static final String DB_FILE		= "gpstrack.db3";
	protected SQLiteDatabase	_dataBase;		//数据库访问工具
	
	
	GPSTrackAdapter(File projectFolder, boolean readonly){
		File	dbFile	= new File(projectFolder, DB_FILE);
		
		int flag;
		if(readonly){
			flag = SQLiteDatabase.CREATE_IF_NECESSARY | SQLiteDatabase.OPEN_READONLY;
		}else{
			flag = SQLiteDatabase.CREATE_IF_NECESSARY | SQLiteDatabase.OPEN_READWRITE;
		}
		
		//如果不能创建db，错误比较严重，就不捕获了，直接抛出，在全局异常记录时捕获
		_dataBase = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), 
				null, flag);
		
		_dataBase.execSQL(GPSPointsColumns.CREATE_SQL);
	}
	
	public	void	close(){
		_dataBase.close();
	}
	
	protected	long	insert(Context context, Location location){
		Date time = new Date();
		ContentValues	values = new ContentValues();
		
		values.put(GPSPointsColumns.LON, location.getLongitude());
		values.put(GPSPointsColumns.LAT, location.getLatitude());
		values.put(GPSPointsColumns.ALT, location.getAltitude());
		values.put(GPSPointsColumns.GPSTIME, location.getTime());
		values.put(GPSPointsColumns.LOCALTIME, time.getTime());
		
		values.put(GPSPointsColumns.ACCOUNT, AppPreference.getAccount(context));
		values.put(GPSPointsColumns.PROJECT, AppPreference.getProject(context));
		
		return _dataBase.insert(GPSPointsColumns.TABLE_NAME, null, values);
	}
	
	public   Cursor	query(Date from, int limit, String account){
	
		String[] columns = null;
		String selection = GPSPointsColumns.LOCALTIME+">? and "+GPSPointsColumns.ACCOUNT+"=?";
		String[] selectionArgs = new String[] { String.valueOf(from.getTime()), account };
		String groupBy = null;
		String having = null;
		String orderBy = GPSPointsColumns.LOCALTIME+" desc";
		String limitString = String.valueOf(limit);
		
		return _dataBase.query(GPSPointsColumns.TABLE_NAME, columns, selection, 
				selectionArgs, groupBy, having, orderBy, limitString);
	}
}

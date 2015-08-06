package com.gnc.dcqtech.project;


/**
 * 定义GPS轨迹数据库文件表结构的类
 * @author xoozi
 *
 */
public class GPSDataBase {
	
	/**
	 * 定义GPS轨迹点表的列
	 * @author xoozi
	 *
	 */
	public interface	GPSPointsColumns{
		public static final String	TABLE_NAME	= "GPSPoints";
		
		public static final String	_ID			= "_id";			//主键
		public static final String	LON			= "Lon";			//经度
		public static final String	LAT			= "Lat";			//纬度
		public static final String	ALT			= "Alt";			//高程
		public static final String	LOCALTIME   = "LocalTime";		//本地时间
		public static final String	GPSTIME		= "GPSTime";		//GPS授时
		public static final String  ACCOUNT		= "Account";
		public static final String  PROJECT		= "Project";
		
		public static final String	CREATE_SQL	= "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME +
				"("+
				" "+_ID+			" INTEGER PRIMARY KEY," +
				" "+ACCOUNT+		" TEXT,"+
				" "+PROJECT+		" TEXT,"+
				" "+LON+			" DOUBLE,"+
				" "+LAT+			" DOUBLE,"+
				" "+ALT+			" DOUBLE,"+
				" "+LOCALTIME+		" INTEGER,"+
				" "+GPSTIME+		" INTEGER" +
				");";
	}

}

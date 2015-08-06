package com.gnc.dcqtech.utils;

import com.xoozi.andromeda.utils.LicenceTools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppPreference {
	
	public static final float PREFERENCE_MIN_INTERVAL = 0.5f;
	
	private static final String  PREFERENCE_KEY_ACCOUNT				= "Account";	
	
	private static final String  PREFERENCE_DEFAULT_ACCOUNT			= "anonymous";
	
	private static final String	PREFERENCE_KEY_PROJECT				= "Project";
	
	private static final String  PREFERENCE_DEFAULT_PROJECT			= "anonymous";
	
	
	private static final String	PREFERENCE_KEY_SENSOR_DELAY			= "SensorDelay";
	
	private static final int	PREFERENCE_DEFAULT_SENSOR_DELAY			= 2;
	
	private static 	final String  	PREFERENCE_KEY_RECENT_PATH		= "RECENTPATH";			//最近打开的路径
	private static  final String	PREFERENCE_DEFAULT_RECENT_PATH	= "3730377612445316471";// 默认字符串，标识空值
	
	private	static final String	PREFERENCE_KEY_REMEMBER_ACCOUNT_AND_PASSWORD = "RememberAccountAndPassword";
	
	private static final boolean	PREFERENCE_DEFAULT_REMEMBER_ACCOUNT_AND_PASSWORD = false;
	
	private	static final String	PREFERENCE_KEY_REMEMBERED_ACCOUNT	= "RememberedAccount";
	
	private static final String	PREFERENCE_DEFAULT_REMEMBERED_ACCOUNT = "";
	
	private	static final String	PREFERENCE_KEY_REMEMBERED_PASSWORD	= "RememberedPassowrd";
	
	private static final String	PREFERENCE_DEFAULT_REMEMBERED_PASSWORD = "";
	
	
	private static final String PREFERENCE_KEY_AUTO_TRACK = "AutoTrack";
	
	private static final boolean PREFERENCE_DEFAULT_AUTO_TRACK = true;
	
	private static final String  PREFERENCE_KEY_INTERVAL_MILLIS = "IntervalMillis";
	
	private static final float   PREFERENCE_DEFAULT_INTERVAL_MILLIS = 1.0f;
	
	private static final String PREFERENCE_KEY_PROJECT_FOLDER = "ProjectFolder";
	
	private static final String PREFERENCE_DEFAULT_PROJECT_FOLDER = "6380b081-0a59-4e53-8528-540a29498f3a";
	
	private static final String PREFERENCE_KEY_LICENCE_FLAG = "LicenctFlag";
	
	private static final boolean  PREFERENCE_DEFAULT_LICENCE_FLAG = false;
	
	private static final String PREFERENCE_KEY_LICENCE_MONTH_COUNT = "LicenceMonthCount";
	
	private static final int PREFERENCE_DEFAULT_LICENCE_MONTH_COUNT = -1;
	
	private static final String PREFERENCE_KEY_LICENCE_START = "LicenceStart";
	
	private static final long PREFERENCE_DEFAULT_LICENCE_START = -1;
	
	private static final String PREFERENCE_KEY_LICENCE_LAST_CHECK = "LicenceLastCheck";
	
	private static final long PREFERENCE_DEFAULT_LICENCE_LAST_CHECK = -1;
	
	private static final String PREFERENCE_KEY_LICENCE_LAST_CHECK_SIGN = "LicenceLastCheckSign";
	
	private static final String PREFERENCE_DEFAULT_LICENCE_LAST_CHECK_SIGN = "GGGGGGGGGG";
	
	private static final String	PREFERENCE_KEY_LICENCE_SIGN = "LicenceSign";
	
	private static final String	PREFERENCE_DEFAULT_LICENCE_SIGN = "cfc5691d-53b4-4c76-8de5-653c7f2d6208";
	
	/**
	 * 获取账户
	 * @param context
	 * @return
	 */
	public static String	getAccount(Context context){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		
		
		return preference.getString(PREFERENCE_KEY_ACCOUNT, PREFERENCE_DEFAULT_ACCOUNT);
	}
	
	/**
	 * 保存账户
	 * @param context
	 * @param uid
	 */
	public static void		setAccount(Context context, String account){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor	editor	= preference.edit();
		editor.putString(PREFERENCE_KEY_ACCOUNT, account);
		editor.commit();
	}
	
	/**
	 * 获取工程名
	 * @param context
	 * @return
	 */
	public static String	getProject(Context context){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		
		
		return preference.getString(PREFERENCE_KEY_PROJECT, PREFERENCE_DEFAULT_PROJECT);
	}
	
	/**
	 * 保存工程名
	 * @param context
	 * @param project
	 */
	public static void		setProject(Context context, String project){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor	editor	= preference.edit();
		editor.putString(PREFERENCE_KEY_PROJECT, project);
		editor.commit();
	}
	
	
	/**
	 * 获取传感器延时
	 * @param context
	 * @return
	 */
	public	static	int		getSensorDelay(Context context){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		
		return preference.getInt(PREFERENCE_KEY_SENSOR_DELAY, PREFERENCE_DEFAULT_SENSOR_DELAY);
	}
	
	/**
	 * 设置传感器延时
	 * @param context
	 * @param sensorDelay
	 */
	public	static	void	setSensorDelay(Context context, int  sensorDelay){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor	editor	= preference.edit();
		editor.putInt(PREFERENCE_KEY_SENSOR_DELAY, sensorDelay);
		editor.commit();
	}
	
	
	/**
	 * 获取RECENT_PATH
	 * @return
	 */
	public static String	getRecentPath(Context context){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		return preference.getString(PREFERENCE_KEY_RECENT_PATH, PREFERENCE_DEFAULT_RECENT_PATH);
	}
	
	/**
	 * 保存RECENT_PATH
	 * @param context
	 * @param uid
	 */
	public static  void		setRecentPath(Context context,String recentPath){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor	editor	= preference.edit();
		editor.putString(PREFERENCE_KEY_RECENT_PATH, recentPath);
		editor.commit();
	}
	
	
	
	
	/**
	 * 是否保存用户名密码
	 * @param context
	 * @return
	 */
	public static boolean isRememberAccountAndPassword(Context context){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		return preference.getBoolean(PREFERENCE_KEY_REMEMBER_ACCOUNT_AND_PASSWORD, 
				PREFERENCE_DEFAULT_REMEMBER_ACCOUNT_AND_PASSWORD);
	}
	
	/**
	 * 设置是否保存用户名密码
	 * @param context
	 * @param isRemember
	 */
	public static void	setIsRememberAccountAndPassword(Context context, boolean isRemember){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor	editor	= preference.edit();
		editor.putBoolean(PREFERENCE_KEY_REMEMBER_ACCOUNT_AND_PASSWORD, isRemember);
		editor.commit();
	}
	
	/**
	 * 获取保存的账户
	 * @param context
	 * @return
	 */
	public static String getRememberedAccount(Context context){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
	
		return preference.getString(PREFERENCE_KEY_REMEMBERED_ACCOUNT, PREFERENCE_DEFAULT_REMEMBERED_ACCOUNT);	
	}
	
	/**
	 * 设置保存的账户
	 * @param context
	 * @param rememberedAccount
	 */
	public static void	setRememberedAccount(Context context, String rememberedAccount){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor	editor	= preference.edit();
		editor.putString(PREFERENCE_KEY_REMEMBERED_ACCOUNT, rememberedAccount);
		editor.commit();
	}
	
	/**
	 * 获取保存的密码
	 * @param context
	 * @return
	 */
	public static String getRememberedPassword(Context context){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
	
		return preference.getString(PREFERENCE_KEY_REMEMBERED_PASSWORD, PREFERENCE_DEFAULT_REMEMBERED_PASSWORD);	
	}
	
	/**
	 * 设置保存的 密码
	 * @param context
	 * @param rememberedPassword
	 */
	public static void	setRememberedPassword(Context context, String rememberedPassword){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor	editor	= preference.edit();
		editor.putString(PREFERENCE_KEY_REMEMBERED_PASSWORD, rememberedPassword);
		editor.commit();
	}
	
	
	
	/**
	 * 是否自动跟踪位置
	 * @param context
	 * @return
	 */
	public static boolean isAutoTrack(Context context){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		return preference.getBoolean(PREFERENCE_KEY_AUTO_TRACK, 
				PREFERENCE_DEFAULT_AUTO_TRACK);
	}
	
	/**
	 * 设置是否自动跟踪位置
	 * @param context
	 * @param isAutoTrack
	 */
	public static void	setIsAutoTrack(Context context, boolean isAutoTrack){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor	editor	= preference.edit();
		editor.putBoolean(PREFERENCE_KEY_AUTO_TRACK, isAutoTrack);
		editor.commit();
	}
	
	
	/**
	 * 获取自动更新时间间隔
	 * @param context
	 * @return
	 */
	public static float getIntervalMins(Context context){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
	
		return preference.getFloat(PREFERENCE_KEY_INTERVAL_MILLIS, PREFERENCE_DEFAULT_INTERVAL_MILLIS);	
	}
	
	/**
	 * 设置自动更新时间间隔
	 * @param context
	 * @param intervalMillis
	 */
	public static void	setIntervalMins(Context context, float intervalMillis){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor	editor	= preference.edit();
		editor.putFloat(PREFERENCE_KEY_INTERVAL_MILLIS, intervalMillis);
		editor.commit();
	}
	
	
	/**
	 * 获取当前工作的工程目录
	 * @param context
	 * @return
	 */
	public static String getProjectFolder(Context context){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
	
		return preference.getString(PREFERENCE_KEY_PROJECT_FOLDER, PREFERENCE_DEFAULT_PROJECT_FOLDER);	
	}
	
	/**
	 * 设置当前工作的工程目录
	 * @param context
	 * @param projectFolder
	 */
	public static void	setProjectFolder(Context context, String projectFolder){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor	editor	= preference.edit();
		editor.putString(PREFERENCE_KEY_PROJECT_FOLDER, projectFolder);
		editor.commit();
	}
	
	/**
	 * 清除目录
	 * @param context
	 */
	public static void	cleanProjectFolder(Context context){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor	editor	= preference.edit();
		editor.putString(PREFERENCE_KEY_PROJECT_FOLDER, PREFERENCE_DEFAULT_PROJECT_FOLDER);
		editor.commit();
	}
	
	
	/**
	 * 获取保存的证书授权月数
	 * @param context
	 * @return
	 */
	public	static int	getLicenceMonthCount(Context context){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		
		return preference.getInt(PREFERENCE_KEY_LICENCE_MONTH_COUNT, PREFERENCE_DEFAULT_LICENCE_MONTH_COUNT);
	}
	
	
	/**
	 * 保存证书授权月数
	 * @param context
	 * @param monthCount
	 */
	public static void	setLicenceMonthCount(Context context, int monthCount){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor	editor	= preference.edit();
		editor.putInt(PREFERENCE_KEY_LICENCE_MONTH_COUNT, monthCount);
		editor.commit();
	}
	
	
	/**
	 * 获取保存的证书开始时间
	 * @param context
	 * @return
	 */
	public	static long	getLicenceStart(Context context){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		
		return preference.getLong(PREFERENCE_KEY_LICENCE_START, PREFERENCE_DEFAULT_LICENCE_START);
	}
	
	/**
	 * 保存证书授权上次校验时间
	 * @param context
	 * @param monthCount
	 */
	public static void	setLicenceLastCheck(Context context, long lastCheck){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor	editor	= preference.edit();
		editor.putLong(PREFERENCE_KEY_LICENCE_LAST_CHECK, lastCheck);
		editor.putString(PREFERENCE_KEY_LICENCE_LAST_CHECK_SIGN, LicenceTools.makeLastCheckLongSign(lastCheck));
		editor.commit();
	}
	
	/**
	 * 获取保存的证书上次校验时间
	 * @param context
	 * @return
	 */
	public	static long	getLicenceLastCheck(Context context){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		long result = preference.getLong(PREFERENCE_KEY_LICENCE_LAST_CHECK, PREFERENCE_DEFAULT_LICENCE_LAST_CHECK);
		String savedSign = preference.getString(PREFERENCE_KEY_LICENCE_LAST_CHECK_SIGN, PREFERENCE_DEFAULT_LICENCE_LAST_CHECK_SIGN);
		
		if(LicenceTools.verifyLastCheckLong(result, savedSign)){
			return result;
		}else{
			return -1;
		}
	}
	
	/**
	 * 保存证书授权开始时间
	 * @param context
	 * @param monthCount
	 */
	public static void	setLicenceStart(Context context, long start){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor	editor	= preference.edit();
		editor.putLong(PREFERENCE_KEY_LICENCE_START, start);
		editor.commit();
	}
	
	/**
	 * 清除证书授权
	 * @param context
	 */
	public static void	cleanLicence(Context context){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor	editor	= preference.edit();
		editor.remove(PREFERENCE_KEY_LICENCE_MONTH_COUNT);
		editor.remove(PREFERENCE_KEY_LICENCE_START);
		editor.remove(PREFERENCE_KEY_LICENCE_SIGN);
		editor.remove(PREFERENCE_KEY_LICENCE_FLAG);
		editor.remove(PREFERENCE_KEY_LICENCE_LAST_CHECK);
		editor.commit();
	}
	
	
	/**
	 * 获取保存的证书签名
	 * @param context
	 * @return
	 */
	public static String	getLicenceSign(Context context){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		
		return preference.getString(PREFERENCE_KEY_LICENCE_SIGN, PREFERENCE_DEFAULT_LICENCE_SIGN);
	}
	
	/**
	 * 保存证书签名
	 * @param context
	 * @param sign
	 */
	public static void		setLicenceSign(Context context, String sign){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor	editor	= preference.edit();
		editor.putString(PREFERENCE_KEY_LICENCE_SIGN, sign);
		editor.commit();
	}
	
	/**
	 * 检测是否已经授权
	 * @param context
	 * @return
	 */
	public static boolean isLicensed(Context context){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		
		return preference.getBoolean(PREFERENCE_KEY_LICENCE_FLAG, PREFERENCE_DEFAULT_LICENCE_FLAG);
	}
	
	/**
	 * 设置授权标志
	 * @param context
	 * @param flag
	 */
	public static void	setLicensed(Context context, boolean flag){
		SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor	editor	= preference.edit();
		editor.putBoolean(PREFERENCE_KEY_LICENCE_FLAG, flag);
		editor.commit();
	}
	
}

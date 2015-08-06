package com.gnc.dcqtech.service;

import java.io.File;
import java.util.Iterator;

import com.gnc.dcqtech.R;
import com.gnc.dcqtech.activities.ActivityMain;
import com.gnc.dcqtech.project.GPSTrackWriter;
import com.gnc.dcqtech.utils.AppPreference;
import com.xoozi.andromeda.utils.Utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.GpsStatus;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;


/**
 * gps轨迹服务
 * @author xoozi
 *
 */
public class GPSTrackService extends Service {
	
	private static final int		REF_ID = 523321;
	private final IBinder 	_binder = new GPSTrackBinder();
	private GPSLocationListener _locationListener = new GPSLocationListener();
	private GPSTrackWriter 	_trackWriter;
	private LocationManager _locationManager;
	private AlarmManager 	_alarmManager;
	private Location		_lastLocation = null;
	private int				_satelliteCount = 0;
	private PendingIntent   _pendingIntent ;
	private INotifyLocationChanged _iLocationChangedNotify;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		_pendingIntent = PendingIntent.getService(GPSTrackService.this, 0, 
				new Intent(GPSTrackService.this, GPSTrackService.class),
				Intent.FLAG_ACTIVITY_NEW_TASK);
		
		_alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
		
		_locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		
		//一初始化就开始位置监听
		_startLocationListening();
		
		//初始化的时候，检查一次
		checkGPSSettingChanged();
	}


	@Override
	public void onDestroy() {
		Utils.amLog("GPSTrackService destory");
		super.onDestroy();
	}


	@Override
	public void onRebind(Intent intent) {
		Utils.amLog("GPSTrackService rebind");
		super.onRebind(intent);
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		//在长时间后台测试时发现，这里偶尔会因为null而抛出异常，预先检查一下
		if(null==intent){
			Utils.amLog("onStartCommand null intent");
			return super.onStartCommand(intent, flags, startId);
		}
		
		Utils.amLog("onStartCommand flags:"+intent.getFlags());
		
		//当服务被后台(Alarm服务)调用时，记录轨迹
		if(Intent.FLAG_FROM_BACKGROUND==intent.getFlags()){
			
			Utils.amLog("onStartCommand call _recordTrack:");
			_recordTrack();
		}
		
		return Service.START_STICKY;
	}


	@Override
	public boolean onUnbind(Intent intent) {
		
		
		//解除绑定时 检测配置，如果不自动记录，停止位置跟踪
		boolean	autoTrack=false;
		if(!autoTrack)
			_stopLocationListening();
		
		return super.onUnbind(intent);
	}


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return _binder;
	}
	
	/**
	 * 当gps相关设置改变后，做出响应
	 */
	public	void	checkGPSSettingChanged(){
		
		boolean	autoTrack = AppPreference.isAutoTrack(this);
		String  projectPath = AppPreference.getProjectFolder(this);
		float intervalMillis = 60*1000*AppPreference.getIntervalMins(this);
		
		Utils.amLog("autoTrack:"+autoTrack+", interval:"+intervalMillis+", projectPath:"+projectPath);
		
		File projectFolder = new File(projectPath);
		
		boolean	projectFolderExist = projectFolder.exists() && projectFolder.isDirectory();
	
		if(autoTrack && projectFolderExist){//需要自动记录
			
			Utils.amLog("start repeat alarm");
			
			_showNotification();
			
			//清除轨迹写入器
			if(null!=_trackWriter){
				_trackWriter.close();
				_trackWriter = null;
			}
			
			//取得轨迹写入器
			_trackWriter = new GPSTrackWriter(projectFolder);
			
			//开始repeat
			_alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, (long)intervalMillis, 
					(long)intervalMillis, _pendingIntent);
		}else{
			Utils.amLog("stop repeat alarm");
			
			_hideNotification();
			
			//停止repeat
			_alarmManager.cancel(_pendingIntent);
			
			
			//清除轨迹写入器
			if(null!=_trackWriter){
				_trackWriter.close();
				_trackWriter = null;
			}
		}
	}
	
	/**
	 * 获取最新gps位置 供ui使用
	 * @return
	 */
	public Location	getLastLocation(){
		return _lastLocation;
	}
	
	/**
	 * 获取卫星数目
	 * @return
	 */
	public int		getSatelliteCount(){
		return _satelliteCount;
	}
	
	
	/**
	 * 由UI设置
	 * @param locationChangedNotify
	 */
	public void		setLocationChangedNotify(INotifyLocationChanged locationChangedNotify){
		_iLocationChangedNotify = locationChangedNotify;
	}
	
	
	
	/**
	 * UI选择退出应用，停止repeat
	 */
	public	void	exitApp(){
		//停止repeat
		_alarmManager.cancel(_pendingIntent);
		
		_hideNotification();
		
		_stopLocationListening();
		
		//清除轨迹写入器
		if(null!=_trackWriter){
			_trackWriter.close();
			_trackWriter = null;
		}
		
		stopSelf();
	}
	
	
	
	/**
	 * 开始位置监听
	 */
	private void	_startLocationListening(){

		_lastLocation = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0,_locationListener);
		
		_locationManager.addGpsStatusListener(_locationListener);
	}
	
	/**
	 * 停止位置监听
	 */
	private void	_stopLocationListening(){

		_locationManager.removeUpdates(_locationListener);
		_locationManager.removeGpsStatusListener(_locationListener);
	}
	
	
	/**
	 * 开个线程来记录轨迹
	 */
	private void _recordTrack() {
		Thread thread = new Thread(null, _doBackgroundThreadProcessing,
				"Background");
		thread.start();
	}
	private Runnable _doBackgroundThreadProcessing = new Runnable() {
		public void run() {
			backgroundThreadProcessing();
		}
	};

	private void backgroundThreadProcessing() {
		if(null!=_lastLocation && null!=_trackWriter){
			long ret = _trackWriter.insert(this, _lastLocation);
			Utils.amLog("insert gps record result:"+ret);
		}else{
			Utils.amLog("ignore _lastLocation:"+_lastLocation+", _trackWriter"+_trackWriter);
		}
	}
	
	
	private void	_showNotification(){
		
		long when = System.currentTimeMillis();
		Notification notification = new Notification();
		
		notification.when = when;
		notification.icon = R.drawable.ic_launcher;
		notification.defaults = Notification.DEFAULT_ALL;
		notification.flags = notification.flags | Notification.FLAG_NO_CLEAR | Notification.FLAG_ONLY_ALERT_ONCE;
		
		
		Intent mainActivityIntent = new Intent(null, null, this, 
				ActivityMain.class);
	    PendingIntent launchIntent = PendingIntent.getActivity(this, 0, mainActivityIntent, 0);
	    
	    notification.setLatestEventInfo(this, getResources().getString(R.string.label_notification_title), 
	    		getResources().getString(R.string.label_notification_content), launchIntent);
		
		NotificationManager notiManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		notiManager.notify(REF_ID, notification);
	}
	
	private void	_hideNotification(){
		NotificationManager notiManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		notiManager.cancel(REF_ID);
	}
	
	
	
	
	public class GPSTrackBinder extends Binder{
		public GPSTrackService getService(){
			return GPSTrackService.this;
		}
	}
	
	/**
	 * 用于监听gps状态和位置的内部类
	 * @author xoozi
	 *
	 */
	private class GPSLocationListener implements LocationListener, GpsStatus.Listener{

		@Override
		public void onLocationChanged(Location location) {
			Utils.amLog("get gps location ...");
			_lastLocation = location;
			
			if(null!=_iLocationChangedNotify){
				try{
					_iLocationChangedNotify.onLocationChanged(_lastLocation);
				}catch(Throwable e){
					Utils.amLog("catch throwable");
				}
				
			}
			
		}
		@Override
		public void onProviderDisabled(String provider) {
			
		}
		@Override
		public void onProviderEnabled(String provider) {
			
		}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			
		}
		@Override
		public void onGpsStatusChanged(int event) {
			_getSatelliteCount(event);
			
			Utils.amLog("_satelliteCount change to :"+ _satelliteCount);
		}
		
		/**
		 * 当gps状态改变时，统计卫星数
		 */
		private	void	_getSatelliteCount(int event){
			GpsStatus status = _locationManager.getGpsStatus(null); // 取当前状态
			
			if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {  
	            int maxSatellites = status.getMaxSatellites();  
	            Iterator<GpsSatellite> it = status.getSatellites().iterator();  
	            
	            _satelliteCount = 0;  
	            while (it.hasNext() && _satelliteCount <= maxSatellites) {  
	                it.next();  
	                
	                _satelliteCount++;  
	            }  
	        }  
		}
	}
	
	/**
	 * 由上层界面实现，的通知gps位置变更的接口
	 * @author xoozi
	 *
	 */
	public interface INotifyLocationChanged{
		public void onLocationChanged(Location location);
	}

}

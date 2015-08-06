package com.gnc.dcqtech.activities;

import java.io.File;
import java.util.Date;
import java.util.Iterator;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.gnc.dcqtech.R;
import com.gnc.dcqtech.layer.LayerManager;
import com.gnc.dcqtech.poppanel.IPopPanelAction;
import com.gnc.dcqtech.poppanel.PopGPSMarker;
import com.gnc.dcqtech.poppanel.PopLayersControl;
import com.gnc.dcqtech.poppanel.PopLoading;
import com.gnc.dcqtech.poppanel.PopPhotoManage;
import com.gnc.dcqtech.poppanel.PopSelectDirectory;
import com.gnc.dcqtech.project.GPSTrackReader;
import com.gnc.dcqtech.project.Project;
import com.gnc.dcqtech.project.ProjectLoader;
import com.gnc.dcqtech.service.GPSTrackService;
import com.gnc.dcqtech.service.GPSTrackService.INotifyLocationChanged;
import com.gnc.dcqtech.uicontroll.CompassView;
import com.gnc.dcqtech.uicontroll.PanelCamera;
import com.gnc.dcqtech.uicontroll.PanelObservating;
import com.gnc.dcqtech.uicontroll.PanelObservating.IObservatingAction;
import com.gnc.dcqtech.utils.AppPreference;
import com.gnc.dcqtech.utils.GlobalExceptionHandler;
import com.xoozi.andromeda.utils.Utils;

import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationListener;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityMain extends Activity implements IPopPanelAction , IObservatingAction{

				
	private MapView					_mapView;
	private Project					_project;
	private PopSelectDirectory		_popSelectFolder;	//选择数据目录弹出窗口
	private PopPhotoManage			_popPhotoManage;	//照片管理弹出窗口
	private PopLayersControl		_popLayersControl;  //图层控制面板
	private PopGPSMarker			_popGPSMarker;		//gps标志控制面板
	private PopLoading				_popLoading;		//加载动作条
	
	private PanelObservating		_panelObservating;  //调绘面板
	
	private GPSTrackReader			_gpsTrackReader;
	private GPSTrackService 		_serviceRef;
	
	private CompassView				_compassView;		//指北针控件
	private CompassAdapter			_compassAdapter;
	
	private final LocationChangedNotify	_locationChangedNotify = new LocationChangedNotify();

	private ServiceConnection 		_mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			
			_serviceRef = ((GPSTrackService.GPSTrackBinder) service).getService();
			_serviceRef.setLocationChangedNotify(_locationChangedNotify);
		}

		public void onServiceDisconnected(ComponentName className) {
			
			_serviceRef = null;
		}
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {  
	        return _confirmExit();  
	    } else {  
	        return super.onKeyDown(keyCode, event);  
	    }  
		
	}
	
	
	
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//登记捕获全局异常
		Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
		
        //取消状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,    
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		
		_initWork();
		
		/*Intent bindService = new Intent(this, DummyService.class);
		bindService(bindService, this, Context.BIND_AUTO_CREATE);*/
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch(requestCode){
		case ActivitySensorCamera.REQUEST_CODE:
			if(RESULT_OK == resultCode){
				
				// 东西太多，直接传Intent给控件处理
				_panelObservating.takePhotoReturn(data);
				
				String	strPhoto 	= data.getStringExtra(ActivitySensorCamera.KEY_PHOTOFILE);
				
				if(null==strPhoto){
					Utils.amLog("wtf null strPhoto");
				}else{
					Utils.amLog(strPhoto);
				}
				
			}
			break;
			
		case ActivitySettings.REQEST_CODE:
			if(RESULT_OK == resultCode){
				boolean settingsChanged = data.getBooleanExtra(ActivitySettings.EXTRA_SETTINGS_CHANGED, false);
				Utils.amLog("Settings return changed flag:"+settingsChanged);
				if(settingsChanged){
					_serviceRef.checkGPSSettingChanged();
				}
			}
			break;
		}
		
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		_compassAdapter.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		_compassAdapter.onResume();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.action_load_data:
			_popSelectFolder.show();
			break;
			
		case R.id.action_gps:
			if(null!=_popLayersControl)
				_popGPSMarker.show();
			break;
			
		case R.id.action_layer_control:
			if(null!=_popLayersControl)
				_popLayersControl.show();
			break;
			
		case R.id.action_observating:
			if(null!=_popLayersControl)
				_jumpToFirstClass();
			break;
			
		case R.id.action_sample_collect:
			if(null!=_popLayersControl)
				_jumpToSampleCollect();
			break;
			
		case R.id.action_photo_manage:
			if(null!=_popLayersControl)
				_popPhotoManage.show();
			
			/*double scale = _mapView.getScale();
			String msg = "Scale:"+scale;
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();*/
			break;
			
		case R.id.action_settings:
			_gotoSettingPage();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//给弹出窗口的回调
	@Override
	public void onFolderSelected(File selectedFolder) {
		
		_loadProject(selectedFolder);
		
		LayerManager.initLayerManager(_project);
		
		//重新调整图层，移除以前的图层，添加现在的
		LayerManager.adjustLayerOnMap(_project, _mapView);
		
		//每次重新加载图层管理器后，需要重新初始化图层控制面板
		_popLayersControl = new PopLayersControl(this,_mapView);
		
		_panelObservating.setProject(_project);
		
		_popPhotoManage.setProject(_project);
		
		//通知服务变更了工作目录
		_notifyServiceWhenSelectedFolder(selectedFolder);
		
		
		//再重新获得gps跟踪db的只读访问器
		_gpsTrackReader  = new GPSTrackReader(selectedFolder);
		if(null==_gpsTrackReader){
			Utils.amLog("################################ WTF WTF WTF ################################");
		}else{
			Utils.amLog("################################ nice boat ################################");
		}
		
		LoadFeatureTask loadTask = new LoadFeatureTask();
		loadTask.execute();
	}
	

	@Override
	public void onLayerFilterChange(String[] selectedLayers) {
		// TODO Auto-generated method stub
		
	}
	
	public	void	onPhotoSelectFeature(int featureId){
		_panelObservating.photoManageSelectFeature(featureId);
	}
	
	@Override
	public	void	onGPSShow(){
		/*if(null!=_lastLocation){
			_mapView.centerAt(new Point(_lastLocation.getLongitude(),_lastLocation.getLatitude()), true);
		
			_locationListener._drawGPSMarker(_lastLocation);
			
		}*/
	}
	//给弹出窗口的回调  END
	
	
	@Override
	public void onSelectLayerClass(int classIndex) {
		_popLayersControl.setLayerClassVisibility(classIndex);
	}
	
	@Override
	public void onClose() {
		_popLayersControl.uncheckAll();
	}
	
	public 	void	onDisplayPhoto(int featureId){
		_popPhotoManage.displayPhoto(featureId);
	}
	
	@Override
	public void onTakePhoto() {
		
		Location lastLocation = _serviceRef.getLastLocation();
		int satelliteCount = _serviceRef.getSatelliteCount();
		
		//如果还未取得位置，提示用户
		if(null==lastLocation){
			Toast.makeText(this, R.string.toast_location_not_ready, Toast.LENGTH_SHORT).show();
			Intent	intent	 = new Intent(this,ActivitySensorCamera.class);
			intent.putExtra(ActivitySensorCamera.KEY_PHOTO_DIR, _project.getPhotoDir().getAbsolutePath());
			intent.putExtra(ActivitySensorCamera.KEY_LON, 0.0);
			intent.putExtra(ActivitySensorCamera.KEY_LAT, 0.0);
			intent.putExtra(ActivitySensorCamera.KEY_ALT, 0.0);
			intent.putExtra(ActivitySensorCamera.KEY_COUNT, 0);
			startActivityForResult(intent,ActivitySensorCamera.REQUEST_CODE);
			return ;
		}
		Intent	intent	 = new Intent(this,ActivitySensorCamera.class);
		intent.putExtra(ActivitySensorCamera.KEY_PHOTO_DIR, _project.getPhotoDir().getAbsolutePath());
		intent.putExtra(ActivitySensorCamera.KEY_LON, lastLocation.getLongitude());
		intent.putExtra(ActivitySensorCamera.KEY_LAT, lastLocation.getLatitude());
		intent.putExtra(ActivitySensorCamera.KEY_ALT, lastLocation.getAltitude());
		intent.putExtra(ActivitySensorCamera.KEY_COUNT, satelliteCount);
		startActivityForResult(intent,ActivitySensorCamera.REQUEST_CODE);
	}
	
	private void	_initWork(){
		
		_bindService();
		
		String loginAccount = getResources().getString(R.string.label_login_account);
		loginAccount +=AppPreference.getAccount(this);
		this.setTitle(loginAccount);
		
		Resources	res 	= this.getResources();
		_mapView			= (MapView)findViewById(R.id.feild_map);
		
		_popSelectFolder	= new PopSelectDirectory(this,_mapView,this,res.getString(R.string.label_select_data_folder));
		
		_popPhotoManage		= new PopPhotoManage(this,getWindow().getDecorView(),this);
		
		_popLoading			= new PopLoading(this,getWindow().getDecorView());
		
		_popGPSMarker		= new PopGPSMarker(this,_mapView,this);
		
		
		_panelObservating	= new PanelObservating(this, _mapView,
				(LinearLayout)findViewById(R.id.field_observating), 
				(LinearLayout)findViewById(R.id.field_draw_tool),
				this);
		
		_compassView		= (CompassView)findViewById(R.id.widget_compass);
		
		_compassAdapter		= new CompassAdapter();
			
	}
	
	
	private void	_loadProject(File projectFolder){
		
		//如果当前已经加载过，先释放工程
		if(null!=_project){
			
			_project.recycle();
			_project = null;
		}
		
		_project	= ProjectLoader.loadProject(this,projectFolder);
		
		if(null==_project){
			Utils.amLog("load project failed in folder:"+projectFolder.getAbsolutePath());
			Toast.makeText(this, R.string.toast_invalid_map, Toast.LENGTH_SHORT).show();
		}
	}
	

	
	private void	_jumpToSampleCollect(){
		boolean	 isShow = _panelObservating.isShow();
		
		if(isShow){
			_panelObservating.close();
		}else{
			_panelObservating.jumpToLayer("PSP");
		}
	}
	
	
	private void 	_jumpToFirstClass(){
		boolean	 isShow = _panelObservating.isShow();
		
		if(isShow){
			_panelObservating.close();
		}else{
			_panelObservating.jumpToLayer("WTF");
		}
	}
	
	
	private void _bindService(){
		Utils.amLog(" onStart and bind");
		Intent bindService = new Intent(this, GPSTrackService.class);
		bindService(bindService, _mConnection, Context.BIND_AUTO_CREATE);
	}
	
	private void	_notifyServiceWhenSelectedFolder(File folder){
		AppPreference.setProjectFolder(this, folder.getAbsolutePath());
		_serviceRef.checkGPSSettingChanged();
	}
	
	private void	_stopServiceWhenExitAndLogout(){
		Utils.amLog("exit app stop service");
		AppPreference.cleanProjectFolder(this);
		_serviceRef.exitApp();
	}
	
	private void	_gotoSettingPage(){
		Intent intent = new Intent(ActivityMain.this,ActivitySettings.class);
		startActivityForResult(intent, ActivitySettings.REQEST_CODE);
	}
	
	
	private boolean  _confirmExit(){
		new AlertDialog.Builder(this)  
		  
		.setTitle(getResources().getString(R.string.label_exit_title))  
  
		.setMessage("")  
		
		.setNeutralButton(getResources().getString(R.string.btn_exit_to_login), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				
				
				//通知服务退出
            	_stopServiceWhenExitAndLogout();
				
				//LayerManager.cleanUp(_mapView);
				Utils.amLog("--------back to login");
				Intent intent = new Intent(ActivityMain.this,ActivityLogin.class);
				startActivity(intent);
                finish();  
			}
		})
		.setNegativeButton(getResources().getString(R.string.btn_cancel),  
		        new DialogInterface.OnClickListener() {  
  		        public void onClick(DialogInterface dialog, int which) {  
  		        	
  		        	Utils.amLog("---------cancel");
		            }  
		        })  
  		.setPositiveButton(getResources().getString(R.string.btn_exit_app),  
		        new DialogInterface.OnClickListener() {  
  
		            public void onClick(DialogInterface dialog, int whichButton) {  
  
		            	//LayerManager.cleanUp(_mapView);
		            	//退出前先把打开的工程释放掉，关闭，释放数据库，便于马上使用数据
		            	if(null!=_project){
		            		_project.recycle();
		            	}
		            	
		            	
		            	//通知服务退出
		            	_stopServiceWhenExitAndLogout();
		            	//_stopLocationListening();
		                finish();  
		                Utils.amLog("---------exit");
		            }  
  
		        }).show();  
		
		return true;
	}


	
	
		
		
	private void _drawGPSMarker(Location location) {
		Utils.amLog("gggggggg wtf");
		GraphicsLayer gpsMarkerLayer = LayerManager.getGPSMarkerLayer();
		if (null != gpsMarkerLayer) {
			gpsMarkerLayer.removeAll();
			PictureMarkerSymbol markerSymbol = new PictureMarkerSymbol(
					getResources().getDrawable(R.drawable.img_marker));
			markerSymbol.setOffsetY(53);
			Point locationPoint = new Point(location.getLongitude(),
					location.getLatitude());
			Graphic markerGraphics = new Graphic(locationPoint, markerSymbol);
			gpsMarkerLayer.addGraphic(markerGraphics);

			Graphic point = new Graphic(locationPoint, new SimpleMarkerSymbol(
					Color.argb(255, 255, 0, 0), 4,
					SimpleMarkerSymbol.STYLE.CIRCLE));
			gpsMarkerLayer.addGraphic(point);
		}

	}
		
		
	
	private class LocationChangedNotify implements INotifyLocationChanged{

		@Override
		public void onLocationChanged(Location location) {
			_drawGPSMarker(location);
		}
		
	}
	
	private class CompassAdapter implements SensorEventListener{
		
		private float[] 		_aValues = new float[3];
		private float[] 		_mValues = new float[3];
		private SensorManager	_sensorManager;
		private int				_rotation;
		
		CompassAdapter(){
			_sensorManager 	= (SensorManager)getSystemService(Context.SENSOR_SERVICE);
			
			WindowManager	wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			_rotation = display.getRotation();
			
			_updateOrientation(new float[]{0,0,0});
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if(Sensor.TYPE_ACCELEROMETER == event.sensor.getType()){
				_aValues = event.values;
			}
			
			if(Sensor.TYPE_MAGNETIC_FIELD==event.sensor.getType()){
				_mValues = event.values;
			}
			
			_updateOrientation(_calculateOrientation());
		}
		
		
		public void	onPause(){
			_sensorManager.unregisterListener(this);
		}
		
		public	void	onResume(){
			Sensor 	accelerometer = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			
			Sensor	magField = _sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			
			int sensorDelay = AppPreference.getSensorDelay(ActivityMain.this);
			_sensorManager.registerListener(this, accelerometer, sensorDelay);
			_sensorManager.registerListener(this, magField, sensorDelay);
		}
		
		private void	_updateOrientation(float[] values){
			if(null != _compassView){
				_compassView.setBearing(values[0]);
				_compassView.setPitch(values[1]);
				_compassView.setRoll(values[2]);
				
				_compassView.invalidate();
			}
		}
		
		private float[] _calculateOrientation(){
			float[] values 	= new float[3];
			float[] inR 	= new float[9];
			float[]	outR	= new float[9];
			
			SensorManager.getRotationMatrix(inR, null, _aValues, _mValues);
			
			int x_axis 		= SensorManager.AXIS_X;
			int y_axis		= SensorManager.AXIS_Y;
			
			switch(_rotation){
			case Surface.ROTATION_90:
				x_axis = SensorManager.AXIS_Y;
				y_axis = SensorManager.AXIS_MINUS_X;
				break;
				
			case Surface.ROTATION_180:
				y_axis = SensorManager.AXIS_MINUS_Y;
				break;
				
			case Surface.ROTATION_270:
				x_axis = SensorManager.AXIS_MINUS_Y;
				y_axis = SensorManager.AXIS_X;
				break;
				
			}
			
			SensorManager.remapCoordinateSystem(inR, x_axis, y_axis, outR);
			
			SensorManager.getOrientation(outR, values);
			
			values[0] = (float) Math.toDegrees(values[0]);
			values[1] = (float) Math.toDegrees(values[1])-90;
			values[2] = (float) Math.toDegrees(values[2]);
			
			return values;
		}
		
	}


	
	private class	LoadFeatureTask extends AsyncTask<Object,Object,Object>{
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			_popLoading.hide();
		}

		@Override
		protected void onPreExecute() {
			_popLoading.show();
			super.onPreExecute();
		}

		@Override
		protected Object doInBackground(Object... params) {
			
			LayerManager.loadFeature(ActivityMain.this,_project);
			
			return null;
		}
	}

	


	


	

}

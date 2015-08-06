package com.gnc.dcqtech.activities;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.gnc.dcqtech.R;
import com.gnc.dcqtech.uicontroll.CameraPreview;
import com.gnc.dcqtech.utils.AppPreference;
import com.gnc.dcqtech.utils.Const;
import com.gnc.dcqtech.utils.GISTools;
import com.gnc.dcqtech.utils.GISTools.DMS;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;



public class ActivitySensorCamera extends Activity implements SensorEventListener,
OnSeekBarChangeListener, OnClickListener, Const {
	public	static final 	int 	REQUEST_CODE = 1033;
	
	public  static final 	String	KEY_FEATUREID = "featureid";//拍照时不用此字段，在转发广播时才附加
	
	public	static final	String	KEY_STATUS 	= "status";
	public	static final	String	KEY_PHOTOFILE= "photofile";
	public	static final 	String	KEY_BEARING	= "bearing";
	public	static final	String	KEY_PITCH	= "pitch";
	public	static final	String	KEY_ROLL	= "roll";
	public	static final	String	KEY_DATE	= "date";
	public	static final 	String	KEY_DISTANCE= "distance";
	public 	static final 	String	KEY_ENV_DES	= "envdes";
	public	static final	String	KEY_CC		= "cc";
	
	public	static final 	String	KEY_PHOTO_DIR= "photodir";
	public	static final 	String	KEY_LON		 = "lon";
	public  static final 	String	KEY_LAT		 = "lat";
	public	static final 	String	KEY_ALT		 = "alt";
	public  static final 	String	KEY_COUNT	 = "count";
	
	
	private float[] 		_aValues = new float[3];
	private float[] 		_mValues = new float[3];
	private File			_photoFolder;
	private SensorManager	_sensorManager;
	private CameraPreview	_preview;
	private int				_rotation;
    private Camera 			_camera;
	private EditText		_editBearing;
	private EditText		_editPitch;
	private EditText		_editRoll;
	private EditText		_editZoom;
	private	EditText		_editDistance;
	private EditText		_editCC;
	private EditText		_editEnvDes;
	private SeekBar			_seekBarZoom;
	private int 			_numberOfCameras;
    private int 			_cameraCurrentlyLocked;

    // The first rear facing camera
    private int 			_defaultCameraId;
    
    private boolean			_supportZoom;
    private int				_largestZoom;
    private int 			_smallestZoom;
    
    private	float			_photoBearing;
    private float			_photoPitch;
    private float			_photoRoll;
    private Date			_photoDate;
    private float			_transientBearing;
    private float			_transientPitch;
    private float			_transientRoll;
    private double			_photoLon;
    private double			_photoLat;
    private double			_photoAlt;
    private int				_satelliteCount;
    
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor_camera);
		_initWork();
	}

	
	
	@Override
	protected void onPause() {
		_sensorManager.unregisterListener(this);
		super.onPause();
		
		// Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        if (_camera != null) {
            _preview.setCamera(null);
            _camera.release();
            _camera = null;
        }
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		Sensor 	accelerometer = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		Sensor	magField = _sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		
		int sensorDelay = AppPreference.getSensorDelay(this);
		
		_sensorManager.registerListener(this, accelerometer, sensorDelay);
		_sensorManager.registerListener(this, magField, sensorDelay);
		
		// Open the default i.e. the first rear facing camera.
        _camera = Camera.open();
        
        _supportZoom = _camera.getParameters().isZoomSupported();
        if(_supportZoom){
        	_smallestZoom = 0;
        	_largestZoom	= _camera.getParameters().getMaxZoom();
        	
        	_seekBarZoom.setEnabled(true);
        	_seekBarZoom.setMax(_largestZoom);
        	_seekBarZoom.setProgress(_camera.getParameters().getZoom());
        	_editZoom.setText(String.valueOf(_camera.getParameters().getZoom()));
        }else{
        	_seekBarZoom.setEnabled(false);
        	_editZoom.setText(this.getResources().getString(R.string.label_sample_notsupport_zoom));
        }
        
        
        
        _cameraCurrentlyLocked = _defaultCameraId;
        _preview.setCamera(_camera);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
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
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if(fromUser){
			_editZoom.setText(String.valueOf(progress));
			Parameters params = _camera.getParameters();
			params.setZoom(progress);
			_camera.setParameters(params);
		}
	}



	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}



	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	
		
		
	}
	
	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.btn_cancel:
			finish();
			break;
			
		case R.id.btn_take_photo:
			_takePhoto();
			break;
		}
	}
	
	
	private void 	_initWork(){
		
		Intent	intent	= getIntent();
		
	    _photoLon		= intent.getDoubleExtra(KEY_LON, 0.0);
	    _photoLat		= intent.getDoubleExtra(KEY_LAT, 0.0);
	    _photoAlt		= intent.getDoubleExtra(KEY_ALT, 0.0);
	    _satelliteCount	= intent.getIntExtra(KEY_COUNT, 1);
		String  photoDir= intent.getStringExtra(KEY_PHOTO_DIR);
		
		_photoFolder	= new File(photoDir);
		if(!_photoFolder.exists())
			_photoFolder.mkdir();
		
		findViewById(R.id.btn_take_photo).setOnClickListener(this);
		findViewById(R.id.btn_cancel).setOnClickListener(this);
		
		EditText editLon= (EditText)findViewById(R.id.edit_sample_lon);
		EditText editLat= (EditText)findViewById(R.id.edit_sample_lat);
		EditText editAlt= (EditText)findViewById(R.id.edit_sample_altitude);
		EditText editCount= (EditText)findViewById(R.id.edit_sample_satellite_count);
		
		DMS	lonDMS = GISTools.toDMS(_photoLon);
		DMS latDMS = GISTools.toDMS(_photoLat);
		editLon.setText(lonDMS.format());
		editLat.setText(latDMS.format());
		/*editLon.setText(String.valueOf(_photoLon));
		editLat.setText(String.valueOf(_photoLat));*/
		editAlt.setText(String.valueOf(_photoAlt));
		editCount.setText(String.valueOf(_satelliteCount));
		
		_editBearing	= (EditText)findViewById(R.id.edit_sample_bearing);
		_editPitch		= (EditText)findViewById(R.id.edit_sample_pitch);
		_editRoll		= (EditText)findViewById(R.id.edit_sample_roll);
		_editZoom		= (EditText)findViewById(R.id.edit_sample_zoom);
		
		_editDistance	= (EditText)findViewById(R.id.edit_sample_distance);
		_editCC			= (EditText)findViewById(R.id.edit_sample_class_code);
		_editEnvDes		= (EditText)findViewById(R.id.edit_sample_env_des);
		
		_seekBarZoom	= (SeekBar)findViewById(R.id.seekBar_zoom);
		
		_seekBarZoom.setOnSeekBarChangeListener(this);
		
		_sensorManager 	= (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		
		WindowManager	wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		_rotation = display.getRotation();
		
		_updateOrientation(new float[]{0,0,0});
		
		_preview 	= (CameraPreview)findViewById(R.id.camera_preview);
		
		
		// Find the total number of cameras available
        _numberOfCameras = Camera.getNumberOfCameras();

        // Find the ID of the default camera
        CameraInfo cameraInfo = new CameraInfo();
            for (int i = 0; i < _numberOfCameras; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                    _defaultCameraId = i;
                }
            }
	}

	private void	_updateOrientation(float[] values){
			
		_transientBearing 	= values[0];
		_transientPitch		= values[2];
		_transientRoll		= values[1];
		
		if(_transientBearing<0){
			_transientBearing = 360+_transientBearing;
		}
		
		_transientPitch 	+=90;
		
		_transientPitch*=-1.0f;
		
		if(_transientBearing<0)
			_transientBearing = 360+_transientBearing;
		
		_transientBearing = (_transientBearing + 90)%360;
		
		
		_editBearing.setText(String.valueOf(_transientBearing));
		_editPitch.setText(String.valueOf(_transientPitch));
		_editRoll.setText(String.valueOf(_transientRoll));
	}
	
	private float[] _calculateOrientation(){
		float[] values 	= new float[3];
		float[] inR 	= new float[9];
		float[]	outR	= new float[9];
		
		
		
		SensorManager.getRotationMatrix(inR, null, _aValues, _mValues);
		
		
		/*int x_axis 		= SensorManager.AXIS_X;
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
		
		SensorManager.getOrientation(outR, values)**/
		SensorManager.getOrientation(inR, values);
		
		values[0] = (float) Math.toDegrees(values[0]);
		/*//转化为与磁北极的夹角
		if(values[0]>360)
			values[0] -= 360;*/
		values[1] = (float) Math.toDegrees(values[1]);
		

		
		values[2] = (float) Math.toDegrees(values[2]);
		
		return values;
	}
	
	@SuppressLint("SimpleDateFormat")
	private	File	_makeTempFile(){
		
		SimpleDateFormat sdf 	= new SimpleDateFormat(DATE_FORMAT);
		String		now			= sdf.format(_photoDate);
		File	result = new File(_photoFolder, now+".jpg");
		
		return result;
	}


	private void	_takePhoto(){
		//拍的瞬间，把各个瞬态存下来
		_photoBearing	= _transientBearing;
		_photoPitch		= _transientPitch;
		_photoRoll		= _transientRoll;
		_photoDate		= new Date();
		_camera.takePicture(new MyShutterCallback(), new MyRawCallback(), new MyJPEGCallback());
	}
	
	private class 	MyJPEGCallback		implements PictureCallback{

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			
			boolean	result = false;
			
			File tempFile = _makeTempFile();
			
			FileOutputStream	fos = null;
			
			try {
				fos	= new FileOutputStream(tempFile);
				fos.write(data);
				fos.close();
				result = true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			Intent	resultData = new Intent();
			
			resultData.putExtra(KEY_STATUS, result);
			//根据结果，让activity返回
			if(result){
				
				resultData.putExtra(KEY_PHOTOFILE, tempFile.getAbsolutePath());
				
				resultData.putExtra(KEY_BEARING, 	_photoBearing);
				resultData.putExtra(KEY_PITCH, 		_photoPitch);
				resultData.putExtra(KEY_ROLL, 		_photoRoll);
				resultData.putExtra(KEY_DATE, 		_photoDate);
			
				
				resultData.putExtra(KEY_LON, 		_photoLon);
				resultData.putExtra(KEY_LAT, 		_photoLat);
				resultData.putExtra(KEY_ALT, 		_photoAlt);
				resultData.putExtra(KEY_COUNT, 		_satelliteCount);
				
				String	strDistance = _editDistance.getText().toString();
				String	strCC		= _editCC.getText().toString();
				String	strEnvDes	= _editEnvDes.getText().toString();
				double	distance;
				try{
					distance = Double.parseDouble(strDistance);
				}catch(NumberFormatException e){
					distance = 0;
				}
				resultData.putExtra(KEY_DISTANCE, 	distance);
				resultData.putExtra(KEY_CC, 		strCC);
				resultData.putExtra(KEY_ENV_DES, 	strEnvDes);
			}
			
			setResult(RESULT_OK, resultData);
			finish();
		}
	}
	
	private class	MyShutterCallback	implements ShutterCallback{

		@Override
		public void onShutter() {
			
		}
	}
	
	private class	MyRawCallback		implements PictureCallback{

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
		
		}
	}
}

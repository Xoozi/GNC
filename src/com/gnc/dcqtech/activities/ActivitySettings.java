package com.gnc.dcqtech.activities;

import com.gnc.dcqtech.R;
import com.gnc.dcqtech.utils.AppPreference;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ActivitySettings extends Activity implements OnSeekBarChangeListener, OnClickListener {
	
	public static final String 		EXTRA_SETTINGS_CHANGED = "SettingsChanged";
	public static final int			REQEST_CODE = 1024;
	private	static	final int 		SENSOR_DELAY_COUNT = 4;
	
	private SeekBar		_seekSensor;
	private EditText	_editInterval;
	private CheckBox	_checkAutoTrack;
	
	private int			_seekPos;
	private float		_oldInterval;
	private boolean		_oldAutoTrack;
	
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		_seekPos = progress;
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onClick(View view) {
		if(R.id.btn_apply == view.getId()){
			_apply();
		}
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_settings);
		
		_initWork();
	}
	
	private void	_initWork(){
		_seekPos = AppPreference.getSensorDelay(this);
		_seekSensor = (SeekBar)findViewById(R.id.seekBar_sensor_delay);
		_seekSensor.setMax(SENSOR_DELAY_COUNT);
		_seekSensor.setProgress(_seekPos);
		_seekSensor.setOnSeekBarChangeListener(this);
		
		
		_oldInterval = AppPreference.getIntervalMins(this);
		_editInterval = (EditText)findViewById(R.id.edit_interval);
		_editInterval.setText(String.valueOf(_oldInterval));
		
		_oldAutoTrack = AppPreference.isAutoTrack(this);
		_checkAutoTrack = (CheckBox)findViewById(R.id.check_auto_track);
		_checkAutoTrack.setChecked(_oldAutoTrack);
		
		findViewById(R.id.btn_apply).setOnClickListener(this);
	}
	
	
	private void	_apply(){
		
		String interval= _editInterval.getText().toString();
		boolean autoTrack = _checkAutoTrack.isChecked();
		
		float     intervalMin ;
		
		try{
			intervalMin = Float.parseFloat(interval);
		}catch(NumberFormatException e){
			intervalMin = 1.0f;
		}
		
		if(intervalMin<AppPreference.PREFERENCE_MIN_INTERVAL){
			intervalMin = AppPreference.PREFERENCE_MIN_INTERVAL;
		}
		
		
		AppPreference.setIntervalMins(this, intervalMin);
		AppPreference.setIsAutoTrack(this, autoTrack);
		AppPreference.setSensorDelay(this, _seekPos);
		
		_editInterval.setText(""+AppPreference.getIntervalMins(this));
		
		boolean gpsSettingsChanged = true;
		
		if(_isFloatEqual(intervalMin, _oldInterval) && autoTrack==_oldAutoTrack){
			gpsSettingsChanged = false;
		}
		
		Intent data = new Intent();
		data.putExtra(EXTRA_SETTINGS_CHANGED, gpsSettingsChanged);
		setResult(Activity.RESULT_OK, data);
		finish();
	}
	
	private boolean _isFloatEqual(float f1, float f2){
		if(Math.abs(f1-f2)>0.001f)
			return false;
		else
			return true;
	}

	

}

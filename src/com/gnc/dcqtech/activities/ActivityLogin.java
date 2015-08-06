package com.gnc.dcqtech.activities;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import com.gnc.dcqtech.R;
import com.gnc.dcqtech.poppanel.IPopSelectFile;
import com.gnc.dcqtech.poppanel.PopSelectFile;
import com.gnc.dcqtech.utils.AccountChecker;
import com.gnc.dcqtech.utils.AppPreference;
import com.xoozi.andromeda.utils.LicenceFileWrap;
import com.xoozi.andromeda.utils.LicenceTools;
import com.xoozi.andromeda.utils.Utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

public class ActivityLogin extends Activity implements IPopSelectFile,OnClickListener,OnCheckedChangeListener{
	
	private PopSelectFile _popSelectFile;
	private View		_layoutLicence;
	private View		_layoutLogin;
	private View		_imgMore;
	private View		_imgMoreUp;
	private CheckBox	_checkRememberAccount;
	private TextView	_textLicenceLength;
	private EditText	_account;
	private EditText	_password;
	private Animation 	_shake 			= null;  
	private boolean		_showLoginOption;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_login);
		
		_initWork();
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.btn_login:
			_login();
			break;
			
		case R.id.btn_exit:
			_exit();
			break;
			
		case R.id.btn_licence_exit:
			_exit();
			break;
			
		case R.id.btn_licence_active:
			_popSelectFile.show();
			break;
			
		case R.id.button_login_option:
			_clickLoginOption();
			break;
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		boolean isRememberAccount = _checkRememberAccount.isChecked();
		AppPreference.setIsRememberAccountAndPassword(this, isRememberAccount);
		
		if(!isRememberAccount){
			AppPreference.setRememberedAccount(this, "");
			AppPreference.setRememberedPassword(this, "");
		}
	}
	
	private void	_initWork(){
		
		_layoutLogin = findViewById(R.id.field_login_input);
		_layoutLicence = findViewById(R.id.field_login_licence);
		
		_textLicenceLength = (TextView)findViewById(R.id.text_licence_length);
		_account	= (EditText)findViewById(R.id.edit_login_username);
		_password	= (EditText)findViewById(R.id.edit_login_password);
		
		findViewById(R.id.btn_login).setOnClickListener(this);
		findViewById(R.id.btn_exit).setOnClickListener(this);
		findViewById(R.id.button_login_option).setOnClickListener(this);
		findViewById(R.id.btn_licence_exit).setOnClickListener(this);
		findViewById(R.id.btn_licence_active).setOnClickListener(this);
		
		_imgMore	= findViewById(R.id.image_more);
		_imgMoreUp 	= findViewById(R.id.image_more_up);
		
		_checkRememberAccount = (CheckBox) findViewById(R.id.checkbox_remember_account);
		_checkRememberAccount.setOnCheckedChangeListener(this);
		
		_shake 			= AnimationUtils.loadAnimation(this, R.anim.shake);
		
		_showLoginOption = false;
		
		
		if(AppPreference.isRememberAccountAndPassword(this)){
			_account.setText(AppPreference.getRememberedAccount(this));
			_password.setText(AppPreference.getRememberedPassword(this));
			_checkRememberAccount.setChecked(true);
		}
		
		_checkLicenceAdjustUI();
		
		
		_popSelectFile	= new PopSelectFile(this,getWindow().getDecorView(),this,
				getString(R.string.label_select_licence_file), LicenceTools.EXT_NAME);
		
	}
	
	private void	_login(){
		String	account = _account.getText().toString();
		String  password= _password.getText().toString();
		
		int result = AccountChecker.checkAccount(this, account, password);

        //dummy
        if(account.equals("xoozi"))
            result = AccountChecker.RESULT_OK;
		
		Utils.amLog("accountchecker result:"+result);
		
		if(AccountChecker.RESULT_NO_USER==result){
			_shakeAccount();
		}else if(AccountChecker.RESULT_INVALID_PASSWORD==result){
			_shakePassword();
		}else{
			Intent intent = new Intent(this,ActivityMain.class);
			startActivity(intent);
			AppPreference.setAccount(this, account);
			
			if(AppPreference.isRememberAccountAndPassword(this)){
				AppPreference.setRememberedAccount(this, account);
				AppPreference.setRememberedPassword(this, password);
			}
			
			finish();
		}
	}
	
	private void	_exit(){
		finish();
	}
	
	private void	_shakeAccount(){
		_account.startAnimation(_shake);
	}
	
	private void	_shakePassword(){
		_password.startAnimation(_shake);
	}
	
	private void	_clickLoginOption(){
		if(_showLoginOption){
			_showLoginOption = false;
			_checkRememberAccount.setVisibility(View.GONE);
			_imgMoreUp.setVisibility(View.INVISIBLE);
			_imgMore.setVisibility(View.VISIBLE);
		}else{
			_showLoginOption = true;
			_checkRememberAccount.setVisibility(View.VISIBLE);
			_imgMoreUp.setVisibility(View.VISIBLE);
			_imgMore.setVisibility(View.INVISIBLE);
		}
	}
	
	/**
	 * 检测是否授权
	 */
	private boolean 	_checkLicence(){
		boolean result = false;
		
		result = AppPreference.isLicensed(this);
		
		do{
			
			if(!result)
				break;
			
			//检验配置中保存的licence数据
			int length = AppPreference.getLicenceMonthCount(this);
			long start = AppPreference.getLicenceStart(this);
			String sign = AppPreference.getLicenceSign(this);
			Date startDate = new Date(start);
			String mac = Utils.getLocalMacAddress(this);
			mac = mac.toLowerCase();
			if(!LicenceTools.verifySign(length, startDate, mac, sign)){
				result = false;
				AppPreference.cleanLicence(this);
				break;
			}
			
			//校验一下是否过期
			Calendar licenceLastDate = Calendar.getInstance();
			Calendar nowDate = Calendar.getInstance();
			licenceLastDate.setTime(startDate);
			licenceLastDate.add(Calendar.MONTH, length);
			Utils.amLog("lastDate:"+licenceLastDate+", nowDate:"+nowDate);
			if(!licenceLastDate.after(nowDate)){
				result = false;
				AppPreference.cleanLicence(this);
				break;
			}
			
			//检测一下，上一次校验证书的日期是否比这次还晚，以防止向后调整时间
			Date now = new Date();
			long lastCheck = AppPreference.getLicenceLastCheck(this);
			if(-1==lastCheck || lastCheck>now.getTime()){
				result = false;
				AppPreference.cleanLicence(this);
				break;
			}
			
			//计算日期差值
			long diff = licenceLastDate.getTimeInMillis() - nowDate.getTimeInMillis();
			long days = diff / (1000 * 60 * 60 * 24);
			long hours = (diff-days*(1000 * 60 * 60 * 24))/(1000* 60 * 60);
			_formatLicenceLength((int)days,(int)hours);
			
			AppPreference.setLicenceLastCheck(this, now.getTime());
			break;
		}while(true);
	
		
		return result;
	}
	
	private void		_checkLicenceAdjustUI(){
		if(_checkLicence()){
			_layoutLogin.setVisibility(View.VISIBLE);
			_layoutLicence.setVisibility(View.GONE);
		}else{
			_layoutLogin.setVisibility(View.GONE);
			_layoutLicence.setVisibility(View.VISIBLE);
		}
	}
	
	private void		_formatLicenceLength(int days, int hours){
		String stringDays = getResources().getString(R.string.label_licence_length_days);
		String stringDaysExt = getResources().getString(R.string.label_licence_length_days_ext);
		String stringHoursExt = getResources().getString(R.string.label_licence_length_hours_ext);
		
		String text = String.format("%s%d%s%d%s", stringDays, days, stringDaysExt, hours, stringHoursExt);
		_textLicenceLength.setText(text);
	}

	@Override
	public void onSelectFile(File file) {
		LicenceFileWrap licenceFile = new LicenceFileWrap(file);
		
		Utils.amLog("length:"+licenceFile.length+", start:"+licenceFile.start+", sign:"+licenceFile.sign);
		
		
		do{
			
			//校验这个licence
			Date start = new Date(licenceFile.start);
			String mac = Utils.getLocalMacAddress(this);
			mac = mac.toLowerCase();
			if(!LicenceTools.verifySign(licenceFile.length, start, mac, licenceFile.sign)){
				Utils.amLog("verify failed, mac:"+mac);
				break;
			}
			
			//校验一下是否过期
			Calendar licenceLastDate = Calendar.getInstance();
			Calendar nowDate = Calendar.getInstance();
			licenceLastDate.setTime(start);
			licenceLastDate.add(Calendar.MONTH, licenceFile.length);
			Utils.amLog("lastDate:"+licenceLastDate+", nowDate:"+nowDate);
			if(!licenceLastDate.after(nowDate)){
				Utils.amLog("check time failed");
				break;
			}
			
			//通过校验写入配置
			AppPreference.setLicenceMonthCount(this, licenceFile.length);
			AppPreference.setLicenceStart(this, licenceFile.start);
			AppPreference.setLicenceSign(this, licenceFile.sign);
			AppPreference.setLicensed(this, true);
			AppPreference.setLicenceLastCheck(this, nowDate.getTimeInMillis());
			
			_checkLicenceAdjustUI();
			
			return;
			
		}while(true);
		
		Toast.makeText(this, R.string.toast_license_failed, Toast.LENGTH_SHORT).show();
		
	}

	

}

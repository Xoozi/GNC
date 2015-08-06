package com.gnc.dcqtech.poppanel;

import android.content.Context;
import android.view.Gravity;
import android.view.View;

import com.gnc.dcqtech.R;
import com.xoozi.andromeda.uicontroller.PopPanelBase;
import com.xoozi.andromeda.uicontroller.PopPanelBase.PopMode;

public class PopLoading extends PopPanelBase {

	public PopLoading(Context context, View rootPanel) {
		super(context, rootPanel, PopPanelBase.PopMode.AT_LOCATION,false);
		
		_initWork();
	}
	
	protected void	_show(){
		
		//检查是否需要初始化
		if(null==_popMenu){
			_popMenu = _initPopView();
		}
		
		if(PopMode.DROP_DOWN == _popMode)
			_popMenu.showAsDropDown(_rootPanel);
		else
			_popMenu.showAtLocation(_rootPanel, Gravity.CENTER, 0,0);
	}
	
	public void show(){
		_show();
	}
	
	public void hide(){
		_hide();
	}
	
	@Override
	protected void _initWork() {
		_basePanel = _layoutInflater.inflate(R.layout.pop_loading, null);
		
		//_editGreek = (EditText) _basePanel.findViewById(R.id.edit_greek);
		
	}

}
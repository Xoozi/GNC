package com.gnc.dcqtech.poppanel;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

import com.gnc.dcqtech.R;
import com.gnc.dcqtech.layer.LayerManager;
import com.xoozi.andromeda.uicontroller.PopPanelBase;

public class PopGPSMarker extends PopPanelBase implements OnClickListener {
	
	private IPopPanelAction				_popPanelAction;//activity给弹出窗口的回调

	public PopGPSMarker(Context context, View rootPanel,IPopPanelAction popPanelAction) {
		super(context, rootPanel, PopPanelBase.PopMode.AT_LOCATION,true);
		_popPanelAction = popPanelAction;
		_initWork();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btn_show_gps_marker:
			_showGPSMarker();
			break;
			
		case R.id.btn_hide_gps_marker:
			_hideGPSMarkder();
			break;
		}
		
	}
	
	
	@Override
	protected void _initWork() {
		_basePanel = _layoutInflater.inflate(R.layout.pop_gps_marker, null);
		
		
		_basePanel.findViewById(R.id.btn_show_gps_marker).setOnClickListener(this);
		_basePanel.findViewById(R.id.btn_hide_gps_marker).setOnClickListener(this);
		
	}
	
	private	void	_showGPSMarker(){
		_hide();
		LayerManager.getGPSMarkerLayer().setVisible(true);
		_popPanelAction.onGPSShow();
	}
	
	private void	_hideGPSMarkder(){
		_hide();
		LayerManager.getGPSMarkerLayer().setVisible(false);
	}
	
	public	void	show(){
		_show();
	}

}

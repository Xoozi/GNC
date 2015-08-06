package com.gnc.dcqtech.uicontroll;

import android.content.Context;
import android.widget.LinearLayout;

import com.gnc.dcqtech.R;
import com.xoozi.andromeda.uicontroller.SelfInflaterPanel;

/**
 * wtf
 * @author xoozi
 *
 */
public class PanelCamera extends SelfInflaterPanel {

	public PanelCamera(Context context, LinearLayout baseLayout) {
		super(context, baseLayout);
		
		_initWork();
	}
	
	private void	_initWork(){
		initPanel(R.layout.activity_sensor_camera);
	}

}

package com.gnc.dcqtech.uicontroll;

import com.gnc.dcqtech.R;
import com.gnc.dcqtech.layer.Layer;
import com.gnc.dcqtech.layer.LayerManager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


public class CalloutOneItem {
	
	private LayerIcon			_layerIcon;	//图例icon
	private TextView			_layerName;		//图层名
	private TextView			_featureName;	//对象名
	
	private View				_baseView;
	
	public CalloutOneItem(Context context, int layerId, String cc){
		
		LayoutInflater layoutInflater	= LayoutInflater.from(context);
		_baseView= layoutInflater.inflate(R.layout.list_item_callout_one, null);
		
		_layerIcon	= (LayerIcon)_baseView.findViewById(R.id.img_callout_layer_icon);
		_layerName		= (TextView)_baseView.findViewById(R.id.text_callout_layer_name);
		_featureName	= (TextView)_baseView.findViewById(R.id.text_callout_feature_name);
		
		Layer	layer	= LayerManager.getLayerById(layerId);
		
		if(null!=layer){
			
			_layerIcon.setLayer(layer);
			_layerName.setTextColor(layer.getARGB());
			
			
			_layerName.setText(layer.getDisplayName());
		}
		
		_featureName.setText(cc);
	}
	
	
	public View getView(){
		return _baseView;
	}
	

}

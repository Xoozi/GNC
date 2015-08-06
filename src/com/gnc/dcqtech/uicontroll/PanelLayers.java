package com.gnc.dcqtech.uicontroll;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.gnc.dcqtech.R;
import com.gnc.dcqtech.layer.Layer;
import com.gnc.dcqtech.layer.LayerManager;
import com.xoozi.andromeda.uicontroller.SelfInflaterPanel;

/**
 * 图层画板
 * @author xoozi
 *
 */
public class PanelLayers extends SelfInflaterPanel  implements OnClickListener{

	private onSelectLayerListener		_onSelectLayer;
	

	public PanelLayers(Context context, LinearLayout baseLayout,onSelectLayerListener onSelectLayer) {
		super(context, baseLayout);
		_onSelectLayer = onSelectLayer;
		_initWork();
	}
	
	
	@Override
	public void onClick(View v) {
		Layer layer = (Layer)v.getTag();
		_onSelectLayer.onSelect(layer);
	}
	
	public	void	loadLayerClass(int index){
	
		
		_panelView.removeAllViews();
		
		List<Layer> layers = LayerManager.getLayerClass(index).getLayers();
		
		for(Layer layer:layers){
			

			
			LayerIcon	li = new LayerIcon(_context);
			li.setLayer(layer);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);  // , 1是可选写的
			lp.setMargins(5, 5, 5, 5); 
			li.setLayoutParams(lp); 
			
			li.setTag(layer);
			li.setClickable(true);
			li.setOnClickListener(this);
			
			_panelView.addView(li);
			
		}
	}
	
	
	private void	_initWork(){
		initPanel(R.layout.panel_layers);
	}


	public	interface	onSelectLayerListener{
		public void	onSelect(Layer layer);
	}
	

}

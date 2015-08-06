package com.gnc.dcqtech.poppanel;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.gnc.dcqtech.R;
import com.gnc.dcqtech.layer.Layer;
import com.gnc.dcqtech.layer.LayerClass;
import com.gnc.dcqtech.layer.LayerManager;
import com.xoozi.andromeda.uicontroller.PopPanelBase;
import com.xoozi.andromeda.utils.Utils;

/**
 * 图层控制面板
 * @author xoozi
 *
 */
public class PopLayersControl extends PopPanelBase implements OnClickListener{
	
	private CheckBox		_checkLabelVisibility;
	private List<CheckBox>	_checkBoxes = new ArrayList<CheckBox>();
	private ViewGroup		_layerClassesContent;
	private boolean			_labelVisibility = true;
	public PopLayersControl(Context context, View rootPanel) {
		super(context, rootPanel, PopPanelBase.PopMode.AT_LOCATION, true);
		_initWork();
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.btn_check_all:
			_checkAll();
			break;
			
		case R.id.btn_uncheck_all:
			uncheckAll();
			break;
			
		case R.id.checkbox_label_visibility:
			_checkLabelVisibilityWhenCick();
			break;
			
		default:
			_checkVisibilityWhenCick();
			break;
		}
		
		
	}
	
	public void	show(){
		_checkVisibilityBeforeShow();
		_show();
	}
	
	public	void	setLayerClassVisibility(int classIndex){
		uncheckAll();
		LayerClass lc = LayerManager.getLayerClass(classIndex);
		int classId = lc.getClassId();
		for(CheckBox checkBox: _checkBoxes){
			Layer layer = (Layer)checkBox.getTag();
			
			if(layer.getClassId()== classId){
				checkBox.setChecked(true);
			}
		}
		_checkVisibilityWhenCick();
	}
	
	@Override
	protected void _initWork() {
		_basePanel = _layoutInflater.inflate(R.layout.pop_layers_control, null);
		_layerClassesContent = (ViewGroup)_basePanel.findViewById(R.id.field_layer_classes);
		
		_checkLabelVisibility = (CheckBox) _basePanel.findViewById(R.id.checkbox_label_visibility);
		_checkLabelVisibility.setOnClickListener(this);
		_checkLabelVisibility.setChecked(_labelVisibility);
		
		_basePanel.findViewById(R.id.btn_check_all).setOnClickListener(this);
		_basePanel.findViewById(R.id.btn_uncheck_all).setOnClickListener(this);
		
		//按LayerClass 给它加载子视图
		int	layerClassCount = LayerManager.getLayerClassCount();
		for(int index=0; index<layerClassCount; index++){
			_loadLayerClassLayout(LayerManager.getLayerClass(index));
		}
	}
	
	/**
	 * 加载图层类别的子视图
	 * @param layerClass
	 */
	private	void	_loadLayerClassLayout(LayerClass layerClass){
		View layerClassListItem = _layoutInflater.inflate(R.layout.list_item_layer_class, null);
		
		TextView	layerClassName = (TextView) layerClassListItem.findViewById(R.id.text_layer_class);
		layerClassName.setText(layerClass.getClassName());
		
		ViewGroup	layerClassContent = (ViewGroup)layerClassListItem.findViewById(R.id.field_layer_class_content);
		
		List<Layer>	layers = layerClass.getLayers();
		for(Layer layer:layers){
			CheckBox	layerCell = new CheckBox(_context);
			layerCell.setText(layer.getDisplayName());
			layerCell.setTag(layer);
			layerCell.setTextColor(Color.argb(0xEE, 0xEE, 0xEE, 0xEE));
			layerCell.setTextSize(18);
			layerCell.setOnClickListener(this);
			layerClassContent.addView(layerCell);
			_checkBoxes.add(layerCell);
		}
		
		_layerClassesContent.addView(layerClassListItem);
	}
	
	private void	_checkLabelVisibilityWhenCick(){
		
		_labelVisibility = _checkLabelVisibility.isChecked();
		
		
		
		for(CheckBox checkBox: _checkBoxes){
			Layer layer = (Layer)checkBox.getTag();
			boolean visibility = checkBox.isChecked();
			
			//和_checkVisibilityWhenCick 不同，当label显示开关变更时只检查处于显示状态的图层
			if(layer.isVisible()){
				
				layer.setVisibility(visibility, _labelVisibility);
			}
		}
	}
	
	/**
	 * 任何一个checkbox被点击,都检查所有图层的可视性
	 */
	private void	_checkVisibilityWhenCick(){
		for(CheckBox checkBox: _checkBoxes){
			Layer layer = (Layer)checkBox.getTag();
			boolean visibility = checkBox.isChecked();
			
			if(visibility!=layer.isVisible()){
				
				layer.setVisibility(visibility, _labelVisibility);
			}
		}
	}
	
	/**
	 * 由于调绘面板等其他东西会改变图层可见性，所以本控件在显示前先检查一遍图层可见性，更改checkboxes的状态
	 */
	private void	_checkVisibilityBeforeShow(){
		for(CheckBox checkBox: _checkBoxes){
			Layer layer = (Layer)checkBox.getTag();
			boolean visibility = layer.isVisible();
			checkBox.setChecked(visibility);
		}
	}
	
	private void	_checkAll(){
		for(CheckBox checkBox: _checkBoxes){
			checkBox.setChecked(true);
		}
		_checkVisibilityWhenCick();
	}
	
	public void uncheckAll(){
		for(CheckBox checkBox: _checkBoxes){
			checkBox.setChecked(false);
		}
		_checkVisibilityWhenCick();
	}

}

package com.gnc.dcqtech.uicontroll;


import com.gnc.dcqtech.layer.Field;
import com.xoozi.andromeda.utils.Utils;

import android.content.Context;
import android.widget.LinearLayout;

public abstract class AttributeListItem extends LinearLayout{
	
	protected	Field					_field;
	protected 	String					_record;
	
	public AttributeListItem(Context context,Field field,String record){
		super(context);
		
		_field					= field;
		_record					= record;
		
		_loadLayout();
		_initWork();
	}
	
	
	/** 加载布局 供子类覆写
	 */
	protected	void	_loadLayout(){
		
	}
	
	/**
	 * 初始化工作 供子类覆写
	 */
	protected 	void	_initWork(){
		
		
	}

	
	/**
	 * 用于各种约束来设定record值
	 * @param record
	 */
	protected void setRecord(String record){
		_record = record;
	}
	
	public	String	getFieldName(){
		return _field.getFieldName();
	}
	
	
	/**
	 * 返回字符串形式的 record 供子类覆写
	 * @return
	 */
	protected	String	getRecord(){
		if(null!=_record)
			return _record;
		else
			return "";
	}
	
	
	
	public static final AttributeListItem	factoryALI(Context context,Field field, String record){
		AttributeListItem	result;
		
		//所有不可见的，都归为这一类，因为在采集端，其实不关心记录的类型，都是以字符串方式保存的
		//可见的需要分类的原因是为了不同数据类型间的UI不同
		if(!field.isVisibility()){
			
			Utils.amLog("AttributeListItemInvisiblility");
			result = new AttributeListItemInvisiblility(context,field,record);
		}else if(field.isSelect()){
			
			Utils.amLog("AttributeListItemSelect");
			result = new AttributeListItemSelect(context,field,record);
		}else if(field.getType().equalsIgnoreCase(Field.TYPE_C)){
			
			Utils.amLog("AttributeListItemText");
			result = new AttributeListItemText(context,field,record);
		}else if(field.getType().equalsIgnoreCase(Field.TYPE_F)||
				field.getType().equalsIgnoreCase(Field.TYPE_N)||
				field.getType().equalsIgnoreCase(Field.TYPE_D)){
			
			Utils.amLog("AttributeListItemNumber");
			result = new AttributeListItemNumber(context,field,record);
		}else{
			
			Utils.amLog("AttributeListItem null");
			result = null;
		}
		
		return result;
	}

}

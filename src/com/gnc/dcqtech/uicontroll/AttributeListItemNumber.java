package com.gnc.dcqtech.uicontroll;

import com.gnc.dcqtech.R;
import com.gnc.dcqtech.layer.Field;
import com.xoozi.andromeda.utils.Utils;

import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;


public class AttributeListItemNumber extends AttributeListItemText {

	public AttributeListItemNumber(Context context, Field field, String record) {
		super(context, field, record);
	}
	
	@Override
	protected void _loadLayout() {
		
		//由于安卓不支持代码设置只读EditText的缺陷，这里用布局来设置
		if(_field.isReadonly()){
			LayoutInflater.from(getContext()).inflate(R.layout.list_item_attribute_number_readonly, this);
		}else{
			LayoutInflater.from(getContext()).inflate(R.layout.list_item_attribute_number, this);
		}
		
	}
	

}
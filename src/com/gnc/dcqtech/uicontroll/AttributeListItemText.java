package com.gnc.dcqtech.uicontroll;

import com.gnc.dcqtech.R;
import com.gnc.dcqtech.layer.Field;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.TextView;


public class AttributeListItemText extends AttributeListItem {
	
	protected	TextView		_fieldName;
	protected	EditText		_editRecord;

	public AttributeListItemText(Context context, Field field, String record) {
		super(context,field, record);
	}
	
	
	@Override
	protected void _loadLayout() {
		
		//由于安卓不支持代码设置只读EditText的缺陷，这里用布局来设置
		if(_field.isReadonly()){
			LayoutInflater.from(getContext()).inflate(R.layout.list_item_attribute_text_readonly, this);
		}else{
			LayoutInflater.from(getContext()).inflate(R.layout.list_item_attribute_text, this);
		}
		
	}
	
	@Override
	protected void _initWork() {
		super._initWork();
		
		_fieldName	=	(TextView)findViewById(R.id.text_attribute_key);
		_editRecord	= 	(EditText)findViewById(R.id.edit_attribute_value);
		
		
		_fieldName.setText(_field.getDisplayName());
		
		
		//如果没有指定record（新建feature的情况）就检查是否有默认值
		if(null!=_record){
			_editRecord.setText(_record);
		}
		
		
	}
	
	
	@Override
	protected String getRecord() {
		_record = _editRecord.getText().toString();
		return _record;
	}
	
	@Override
	protected void setRecord(String record) {
		super.setRecord(record);
		_editRecord.setText(_record);
	}

}

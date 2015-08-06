package com.gnc.dcqtech.uicontroll;

import com.gnc.dcqtech.R;
import com.gnc.dcqtech.layer.Field;
import com.gnc.dcqtech.layer.Selection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;


public class AttributeListItemSelect extends AttributeListItem implements OnItemSelectedListener {
	
	protected	TextView		_fieldName;
	protected	Spinner			_spinnerRecord;
	protected 	Selection		_selection;
	protected	String			_selectedValue;

	public AttributeListItemSelect(Context context,Field field, String record) {
		super(context, field, record);
	}
	
	public void onItemSelected(AdapterView<?> arg0, View arg1, int selectedPos,
			long arg3) {
		_selectedValue = _selection.get(selectedPos).getValue();
	}


	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	protected void _loadLayout() {
		LayoutInflater.from(getContext()).inflate(R.layout.list_item_attribute_select, this); 
	}
	
	@Override
	protected void _initWork() {
		_selection		= 	_field.getSelectOparetor();
		_fieldName		=	(TextView)findViewById(R.id.text_attribute_key);
		_spinnerRecord	= 	(Spinner)findViewById(R.id.spinner_attribute_value);
		
		_spinnerRecord.setAdapter(new RecordSelectAdapter());
		_spinnerRecord.setOnItemSelectedListener(this);
		
		_fieldName.setText(_field.getDisplayName());
		
		if(null!=_record){
			int pos = _querySelection();
			if(-1!=pos){
				_spinnerRecord.setSelection(pos);
			}
		}
	}
	
	@Override
	protected String getRecord() {
		
		//保存时返回的值，应该是实际值，不是显示名
		return _selectedValue;
	}
	
	/**
	 * 根据record来选择项
	 * @return
	 */
	private int	_querySelection(){
		
		return _selection.findItem(_record);
	}
	
	
	private class RecordSelectAdapter extends BaseAdapter{
		private LayoutInflater		_layoutInflater;
		
		RecordSelectAdapter(){
			_layoutInflater = LayoutInflater.from(getContext());
		}
		
		public int getCount() {
			return _selection.count();
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if(null==convertView){
				convertView = _layoutInflater.inflate(R.layout.list_item_attribute_select_item, null);
			}
			
			TextView	value    	= (TextView)convertView.findViewById(R.id.text_value);
			TextView	displayName = (TextView)convertView.findViewById(R.id.text_displayname);
			
			value.setText(_selection.get(position).getValue());
			displayName.setText(_selection.get(position).getDisplayName());
			
			return convertView;
		}
		
	}


}

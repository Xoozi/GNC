package com.gnc.dcqtech.layer;

import java.util.ArrayList;
import java.util.List;

import com.gnc.dcqtech.project.ProjectDataBase.SelectionItemColumns;

import android.database.Cursor;


public class Selection{
	private List<SelectItem>	_selectItems = new ArrayList<SelectItem>();
	private String				_selectionName;
	
	Selection(String selectionName, Cursor data){
		_selectionName = selectionName;
		
		while(data.moveToNext()){
			String value 		= data.getString(data.getColumnIndexOrThrow(SelectionItemColumns.ITEM_VALUE));
			String displayName	= data.getString(data.getColumnIndexOrThrow(SelectionItemColumns.DISPLAY_NAME));
			_addSelectItem(value,displayName);
		}
	}
	
	
	
	public int count(){
		return _selectItems.size();
	}
	
	public	int findItem(String record){
		int	index = 0;
		for(SelectItem selectItem:_selectItems){
			if(selectItem.getValue().equals(record)){
				return index;
			}
			index++;
		}
		
		return -1;
	}
	
	public String getName(){
		return _selectionName;
	}
	
	public final SelectItem get(int index){
		return _selectItems.get(index);
	}
	
	private void _addSelectItem(String value, String displayName){
		SelectItem selectItem = new SelectItem(value, displayName);
		
		_selectItems.add(selectItem);
	}
	
	
	/**
	 * 选择项
	 * @author xoozi
	 *
	 */
	public class SelectItem{
		private String	_value;
		private String	_displayName;
		
		public SelectItem(String value, String displayName){
			_value 			= value;
			_displayName	= displayName;
		}
		
		public	final String getValue(){
			return _value;
		}
		
		public final String	getDisplayName(){
			return _displayName;
		}
	}
}

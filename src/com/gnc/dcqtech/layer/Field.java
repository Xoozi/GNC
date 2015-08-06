package com.gnc.dcqtech.layer;

import org.jdom.Element;


/**
 * 图层的属性Field
 * @author xoozi
 *
 */
public class Field {
	public static final String FIELD_TAG	= "Field";
	public static final String TYPE_C		= "C";
	public static final String TYPE_F		= "F";
	public static final String TYPE_D		= "D";
	public static final String TYPE_N		= "N";
	private static final String NAME		= "Name";
	private static final String DISPLAY_NAME= "DisplayName";
	private static final String	VISIBILITY	= "Visibility";
	private static final String TYPE		= "Type";
	private static final String	READONLY	= "Readonly";
	private static final String	VALUE		= "Value";
	private static final String	SELECT		= "Select";
	private static final String	TRUE		= "true";
	private static final String FALSE		= "fasle";
	

	private String	_name;
	private String  _displayName;
	private String	_type;
	private String	_value;
	private boolean	_readonly;
	private boolean	_visibility;
	private boolean	_select;
	
	private Selection	_selection;
	
	
	public Field(Element fieldConfig){
	
		_name			= fieldConfig.getAttributeValue(NAME);
		_displayName	= fieldConfig.getAttributeValue(DISPLAY_NAME);
		_type			= fieldConfig.getAttributeValue(TYPE);
		_value			= fieldConfig.getAttributeValue(VALUE, "");
		
		String	readonly	= fieldConfig.getAttributeValue(READONLY,FALSE);
		String	select		= fieldConfig.getAttributeValue(SELECT, FALSE);
		
		String	visibility	= fieldConfig.getAttributeValue(VISIBILITY, TRUE);
		
		if(visibility.equalsIgnoreCase(TRUE)){
			_visibility = true;
		}else{
			_visibility = false;
		}
		
		
		if(readonly.equalsIgnoreCase(TRUE)){
			_readonly = true;
		}else{
			_readonly = false;
		}
		
		
		_selection = LayerManager.getSelectionByName(select);
		
		if(null!=_selection){
			_select	= true;
		}else{
			_select = false;
		}
		
	}
	

	
	public boolean isReadonly(){
		return _readonly;
	}
	
	public boolean isVisibility(){
		return _visibility;
	}

	
	public boolean	isSelect(){
		return _select;
	}
	
	public final String getDisplayName(){
		if(null!=_displayName)
			return _displayName;
		else
			return _name;
	}
	
	public final String getFieldName(){
		return _name;
	}
	
	public final String getType(){
		return _type;
	}
	
	public final String getValue(){
		return _value;
	}

	
	public final Selection getSelectOparetor(){
		return _selection;
	}

}

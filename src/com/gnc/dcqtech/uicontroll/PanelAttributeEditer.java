package com.gnc.dcqtech.uicontroll;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.gnc.dcqtech.R;
import com.gnc.dcqtech.layer.Layer;
import com.gnc.dcqtech.layer.Record;
import com.xoozi.andromeda.uicontroller.SelfInflaterPanel;

import android.content.Context;
import android.util.Pair;
import android.widget.LinearLayout;


public class PanelAttributeEditer extends SelfInflaterPanel{

	private static final String CC = "CC";
	private	LinearLayout		_listAttribute;
	private	Layer				_currentLayer;
	
	private	List<AttributeListItem> _attributeListItem;

	public PanelAttributeEditer(Context context, LinearLayout baseLayout) {
		super(context, baseLayout);

		_initWork();
	}
	
	public	void	clean(){
		//移除所的属性行
		_listAttribute.removeAllViews();
				
		if(null!=_attributeListItem)
			_attributeListItem.clear();
	}
	
	public	void	loadData(Layer layer, String recordXml){
		_currentLayer = layer;
		
		//移除所的属性行
		_listAttribute.removeAllViews();
		
		if(null!=_attributeListItem)
			_attributeListItem.clear();
		
		//获取属性表项，过程包括了注册约束监听
		_attributeListItem = _currentLayer.getAttributeListItems(_context,recordXml);
				
		for(AttributeListItem attributeListItem:_attributeListItem){
					
		
			//忽略不可见的部分
			if(attributeListItem instanceof AttributeListItemInvisiblility)
				continue;
				_listAttribute.addView(attributeListItem);
			}
	}
	
	public	Pair<String,String>	getAttributeXmlForSave(){
		String cc = null;
		//用于输出xml的流
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
				
		//构建一个
		Element	recordList = new Element(Record.TAG_RECORD_LIST);
		Document myDocument = new Document(recordList);
		for(AttributeListItem ali:_attributeListItem){
			Element record = new Element(Record.TAG_RECORD);
			record.setAttribute(Record.ATTR_VALUE, ali.getRecord());
			recordList.addContent(record);
			if(CC.equals(ali.getFieldName()))
				cc = ali.getRecord();
		}
				
		try {
				XMLOutputter outputter = new XMLOutputter();
				outputter.output(myDocument, baos);
			} catch (java.io.IOException e) {
				    e.printStackTrace();
		}
		
		return new Pair<String, String>(baos.toString(),cc);
	}

	private void	_initWork(){
		initPanel(R.layout.panel_attribute_edit);
		_listAttribute	= (LinearLayout)_panelView.findViewById(R.id.field_attribute_list);
	}

	
	
}

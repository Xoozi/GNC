package com.gnc.dcqtech.uicontroll;

import com.gnc.dcqtech.layer.Field;

import android.content.Context;

/**
 * 隐式属性项，回调时只修改record值
 */
public class AttributeListItemInvisiblility extends AttributeListItem {

	public AttributeListItemInvisiblility(Context context,Field field,
			String record) {
		super(context,field, record);
	}

}
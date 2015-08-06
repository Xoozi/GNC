package com.gnc.dcqtech.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import com.gnc.dcqtech.project.Project;
import com.xoozi.andromeda.utils.MD5Tools;

import android.content.Context;

public class AccountChecker {
	
	public  static final int				RESULT_NO_USER = 2;
	public  static final int				RESULT_INVALID_PASSWORD = 1;
	public  static final int				RESULT_OK = 0;
	
	private static final String				TAG_USER = "User";
	private static final String				ATTR_NAME = "Name";
	private static final String				ATTR_PASSWORD = "Password";
	
	private static AccountChecker		_checker;
	
	private Map<String,String>			_accountTable;
	
	
	private AccountChecker(Context context){
		XmlOperator	usersDOM = Project.getUsersDOM(context);
		
		_accountTable	= new HashMap<String,String>();


		try{
			Element root = usersDOM.getRoot();
			List<Element> children = root.getChildren(TAG_USER);
			
			for(Element child : children){
				String name 	= child.getAttributeValue(ATTR_NAME);
				String md5		= child.getAttributeValue(ATTR_PASSWORD);
				_accountTable.put(name, md5);
			}
		}catch(Exception e){
			e.printStackTrace();
		}	
		
		usersDOM.Destory();
	}
	
	private	int	_checkInTable(String account, String password){
		String md5 = _accountTable.get(account);
		
		if(null==md5)
			return RESULT_NO_USER;
		
		String passwordMd5 = MD5Tools.getMD5String(password);
		
		if(passwordMd5.equals(md5))
			return RESULT_OK;
		else
			return RESULT_INVALID_PASSWORD;
	}
	
	public static	int	checkAccount(Context context,String account, String password){
		
		if(null==_checker){
			_checker = new AccountChecker(context);
		}
		
		return _checker._checkInTable(account, password);
	}

}

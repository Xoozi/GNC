package com.gnc.dcqtech.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;

import com.xoozi.andromeda.utils.Utils;

/**
 * xml文档对象模型操作简易封装
 * @author xoozi
 *
 */
public class XmlOperator
{
	private InputSource	_xmlSource;
	private InputStream	_xmlStream;
	private File 		_xmlFile;
	private Document 	_xmlDom;
	
	public XmlOperator(File xmlFile)
	{
		_xmlFile = xmlFile;
	}
	public XmlOperator(InputStream	xmlStream){
		_xmlStream = xmlStream;
	}
	public XmlOperator(InputSource xmlSource){
		_xmlSource = xmlSource;
	}
	
	private Document getXmlDom() throws JDOMException, IOException{
		if(_xmlDom == null)
		{
				SAXBuilder sax = new SAXBuilder();
			
				if(null!=_xmlFile)
					_xmlDom = sax.build(_xmlFile);
				else if(null!=_xmlStream)
					_xmlDom = sax.build(_xmlStream);
				else if(null!=_xmlSource)
					_xmlDom = sax.build(_xmlSource);
				else
					Utils.amLog("WTF no file no stream");
		}
		return _xmlDom;
	}
	
	public Element getRoot() throws JDOMException, IOException{
		
		Document xmlDom = getXmlDom();

		Element returnNode =  xmlDom.getRootElement();

		return returnNode;
		
	}
	
	public String	getXmlDocContent(){
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		String result=null;
		try {
			
		    XMLOutputter outputter = new XMLOutputter();
		    outputter.output(getXmlDom(), baos);
		    result = baos.toString();
		} catch (Exception e) {
		    e.printStackTrace();
		} 
		
		return result;
	}
	
	
	/**
	 * ��ָ��ĸ�ڵ��£��ҵ�ָ��name�Ľڵ�
	 * @param parentNode
	 * @param childNodeName
	 * @return ����element
	 */
	public Element getChildElement(Element parentNode, String childNodeName)
	{
		List<?> childList = parentNode.getChildren();
		Iterator<?> listIt = childList.iterator();
		
		Element returnNode = null;
		
		while (listIt.hasNext())
		{
			Element element = (Element) listIt.next();
			if(element.getName().equalsIgnoreCase(childNodeName))
			{
				returnNode = element;
				break;
			}
			
		}
		return returnNode;
	}
	
	/**
	 * ��ָ��ĸ�ڵ��£��ҵ�ָ��name�Ľڵ�
	 * @param parentNode
	 * @param childNodeName
	 * @return ����element
	 */
	public ArrayList<Element> getChildElements(Element parentNode, String childNodeName)
	{
		List<?> childList = parentNode.getChildren();
		Iterator<?> listIt = childList.iterator();
		
		ArrayList<Element> returnNodes = new ArrayList<Element>();
		
		while (listIt.hasNext())
		{
			Element element = (Element) listIt.next();
			if(element.getName().equalsIgnoreCase(childNodeName))
			{
				returnNodes.add(element);
			}
			
		}
		return returnNodes;
	}

	public void Destory()
	{
		_xmlDom =null;
		_xmlFile = null;
	}
}

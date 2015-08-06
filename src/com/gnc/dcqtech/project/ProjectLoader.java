package com.gnc.dcqtech.project;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.esri.android.map.TiledLayer;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.xoozi.andromeda.utils.Utils;

/**
 * 工程加载器
 * @author xoozi
 *
 */
public class ProjectLoader {
	private static final String	LC_FOLDER	= "Layers";
	private static final String CONF_FILE	= "conf.xml";
	private static final String DB_FILE		= "gnc.db3";
	

	public static Project  loadProject(Context context, File projectFolder){
		
		Project result = null;
		
		do{
			File	mapFolder = new File(projectFolder, LC_FOLDER);
			
			//如果没有符合规则的Layers目录，跳出返回null
			if(!mapFolder.exists()){
				Utils.amLog("map folder can't find");
				break;
			}
				
			
			
			//如果以Layers目录创建离线图层失败，也跳出返回null
			String mapURI;
			mapURI ="file://"+mapFolder.getAbsolutePath()+File.separator;
			TiledLayer offLineLayer ;
			try{
				offLineLayer = new ArcGISLocalTiledLayer(mapURI);
			}catch(Exception e){
				Utils.amLog("create tiledLayer failed"+e.toString());
				break;
			}
			
			int wkid = _getWKID(mapFolder);
			
			File	dbFile	= new File(projectFolder, DB_FILE);
			
			//如果不能创建db，错误比较严重，就不捕获了，直接抛出，在全局异常记录时捕获
			SQLiteDatabase dataBase = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.CREATE_IF_NECESSARY);
			
			
			result = new Project(context, projectFolder, offLineLayer, dataBase,wkid);
		}while(false);
		
	
		return result;
	}
	
	
	private static int	_getWKID(File mapDir){
		int result = 0;
		DocumentBuilder docBuilder=null;
		Document doc=null;
		try 
		{
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			
			docBuilder = docBuilderFactory.newDocumentBuilder();
		
			doc = docBuilder.parse(new File(mapDir, CONF_FILE));
			
			
			NodeList wktNode = doc.getElementsByTagName("WKID");
			
			
			
			if(wktNode != null)
			{
				result = Integer.parseInt(wktNode.item(0).getFirstChild().getNodeValue());
			}
			
		} catch (ParserConfigurationException e) 
		{
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		 
		
		
		return result;
	}
}

package com.gnc.dcqtech.project;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.jdom.Element;
import org.jdom.JDOMException;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Pair;

import com.esri.android.map.TiledLayer;
import com.esri.core.geometry.Geometry;
import com.gnc.dcqtech.activities.ActivitySensorCamera;
import com.gnc.dcqtech.layer.Layer;
import com.gnc.dcqtech.poppanel.PopPhotoManage;
import com.gnc.dcqtech.project.ProjectDataBase.FeatureColumns;
import com.gnc.dcqtech.project.ProjectDataBase.GPSPointsColumns;
import com.gnc.dcqtech.project.ProjectDataBase.LayerClassColumns;
import com.gnc.dcqtech.project.ProjectDataBase.LayersColumns;
import com.gnc.dcqtech.project.ProjectDataBase.PhotoColumns;
import com.gnc.dcqtech.project.ProjectDataBase.ProfileColumns;
import com.gnc.dcqtech.project.ProjectDataBase.SelectionColumns;
import com.gnc.dcqtech.project.ProjectDataBase.SelectionItemColumns;
import com.gnc.dcqtech.utils.AppPreference;
import com.gnc.dcqtech.utils.Const;
import com.gnc.dcqtech.utils.GISTools;
import com.gnc.dcqtech.utils.WKT;
import com.gnc.dcqtech.utils.XmlOperator;
import com.gnc.dcqtech.utils.GISTools.DMS;
import com.xoozi.andromeda.utils.Utils;

/**
 * 一份切片地图，连带采集数据和图片目录，合称为一个采集工程
 * @author xoozi
 *
 */
public class Project implements Const {
	
	private static final int 	THUMB_HEIGHT = 100;	//缩略图像素高度
	private static final String	PHOTO_FILE_PREFIX = "PH";	//保存照片的前缀
	private static final String	FOLDER_THUMB	= "Thumbnail";//缩略图目录
	
	private	static final String	FOLDER_CONFIG	= "Config";	//配置目录
	private static final String	FOLDER_LAYERS	= "Layers";	//图层目录
	private static final String	FOLDER_SELECT	= "Select";	//选择器目录
	private static final String	FOLDER_PHOTO	= "Photo";	//照片目录
	
	
	private	String			_account ;
	private	Context			_context;
	private TiledLayer		_offLineLayer;	//离线切片图层
	private File			_projectFolder;	//工程目录
	private File			_photoFolder;	//照片目录
	private File			_thumbFolder;	//缩略图目录
	private SQLiteDatabase	_dataBase;		//数据库访问工具
	private int				_wkid;
	
	
	/**
	 * 包权限的构造方法，强制用户通过ProjectLoader来构造
	 * @param projectFolder
	 * @param offLineLayer
	 * @param dataBase
	 */
	Project(Context context, File projectFolder, TiledLayer offLineLayer, SQLiteDatabase	dataBase, int wkid){
		_context		= context;
		_projectFolder	= projectFolder;
		_offLineLayer	= offLineLayer;
		_dataBase		= dataBase;
		_wkid			= wkid;
		_initWork();
	}
	
	/**
	 * 有效释放工程资源
	 */
	public	void		recycle(){
		
		_offLineLayer.recycle();
		_offLineLayer 	= null;
		_projectFolder	= null;
		_photoFolder	= null;
		_thumbFolder	= null;
		if(null!=_dataBase){
			_dataBase.close();
			_dataBase = null;
			SQLiteDatabase.releaseMemory();
		}
		System.gc();
	}
	
	/**
	 * 获取切片图层
	 * @return
	 */
	public	TiledLayer	getTiledLayer(){
		return _offLineLayer;
	}
	
	/**
	 * 获取照片目录
	 * @return
	 */
	public	File		getPhotoDir(){
		return _photoFolder;
	}
	
	public	File		getThumbDir(){
		return _thumbFolder;
	}
	
	public	float	getPhotoBearing(int featureId){
		
		float result = 0.0f;
		
		String	idString = String.valueOf(featureId);
		Cursor	data = _dataBase.query(PhotoColumns.TABLE_NAME, null, PhotoColumns.FEATURE_ID+"=?", 
				new String[]{idString}, null, null, null);
		
		if(data.moveToFirst()){
			result = (float)data.getDouble(data.getColumnIndexOrThrow(PhotoColumns.BEARING));
		}
		
		data.close();
		
		return result;
	}
	
	/**
	 * 查询所有要素  供加载用
	 * @return
	 */
	public	Cursor	queryAllFeature(){
		return _dataBase.query(FeatureColumns.TABLE_NAME, null, null, null, null, null, null);
	}
	
	
	/**
	 * 按id查询要素
	 * @param featureId
	 * @return
	 */
	public	Cursor	queryFeatureById(int featureId){
		String	idString = String.valueOf(featureId);
		Cursor	data = _dataBase.query(FeatureColumns.TABLE_NAME, null, FeatureColumns._ID+"=?", 
				new String[]{idString}, null, null, null);
		return data;
	}
	
	
	public	void	insertGPSTrace(Location location){
		
		ContentValues	values = new ContentValues();
		
		values.put(GPSPointsColumns.LON, location.getLongitude());
		values.put(GPSPointsColumns.LAT, location.getLatitude());
		values.put(GPSPointsColumns.ALT, location.getAltitude());
		values.put(GPSPointsColumns.GPSTIME, location.getTime());
		
		_dataBase.insert(GPSPointsColumns.TABLE_NAME, null, values);
	}
	
	/**
	 * 新增要素
	 * @param layer
	 * @param geo
	 * @param attributeXml
	 * @return
	 */
	public	long	insertFeature(Layer layer, Geometry geo, 
			String attributeXml, String cc, Intent pendingPhoto){
		
		long	featureId;
		UUID guid = UUID.randomUUID();
		Date createTime = new Date();
		
		
		ContentValues	values = new ContentValues();
		values.put(FeatureColumns.GUID, guid.toString());
		values.put(FeatureColumns.LAYER_ID, layer.getLayerId());
		values.put(FeatureColumns.FEATURE_TYPE, layer.getType());
		values.put(FeatureColumns.CC, cc);
		values.put(FeatureColumns.GEO_DATA, WKT.GeometryToWKT(geo));
		values.put(FeatureColumns.ATTR_DATA, attributeXml);
		values.put(FeatureColumns.AUTHOR, _account);
		values.put(FeatureColumns.CREATE_TIME, createTime.getTime());
		
		featureId =  _dataBase.insert(FeatureColumns.TABLE_NAME, null, values);
		
		insertPhoto((int)featureId, pendingPhoto);
		
		return featureId;
	}
	
	
	/**
	 * 删除要素，其实只是更新删除标记
	 * @param featureId
	 */
	public	void	deleteFeature(int featureId){
		ContentValues	values = new ContentValues();
		Date modifyTime = new Date();
		values.put(FeatureColumns.MODIFIER, _account);
		values.put(FeatureColumns.STATE, FeatureColumns.STATE_VALUE_DELETE);
		values.put(FeatureColumns.MODIFY_TIME, modifyTime.getTime());
		
		String	idString = String.valueOf(featureId);
		
		_dataBase.update(FeatureColumns.TABLE_NAME, values, FeatureColumns._ID+"=?", new String[]{idString});
	}
	
	/**
	 * 更新要素
	 * @param featureId
	 * @param geo
	 * @param attributeXml
	 * @param pendingPhoto
	 */
	public	void	updateFeature(int featureId, Geometry geo, 
			String attributeXml,  Intent pendingPhoto){
		
		Date modifyTime = new Date();
		ContentValues	values = new ContentValues();
		values.put(FeatureColumns.GEO_DATA, WKT.GeometryToWKT(geo));
		values.put(FeatureColumns.ATTR_DATA, attributeXml);
		values.put(FeatureColumns.MODIFIER, _account);
		values.put(FeatureColumns.MODIFY_TIME, modifyTime.getTime());
		
		String	idString = String.valueOf(featureId);
		
		_dataBase.update(FeatureColumns.TABLE_NAME, values, FeatureColumns._ID+"=?", new String[]{idString});
		
		insertPhoto((int)featureId, pendingPhoto);
	}
	
	public 	Cursor	queryPhoto(){
		return _dataBase.query(PhotoColumns.TABLE_NAME, null, null, null, null, null, null);
	}
	
	/**
	 * 删除指定要素名下的所有附属照片
	 * @param featureId
	 */
	public	void	deletePhoto(int featureId){
		
		String	idString = String.valueOf(featureId);
		Cursor	data = _dataBase.query(PhotoColumns.TABLE_NAME, null, PhotoColumns.FEATURE_ID+"=?", 
				new String[]{idString}, null, null, null);
		
		if(null!=data){
			
			while(data.moveToNext()){
				String photoName = data.getString(data.getColumnIndexOrThrow(PhotoColumns.PHOTO_NAME));
				int	   photoId	= data.getInt(data.getColumnIndexOrThrow(PhotoColumns._ID));
				
				String photoIdString = String.valueOf(photoId);
				
				//删除照片表的对应行
				_dataBase.delete(PhotoColumns.TABLE_NAME, PhotoColumns._ID+"=?", new String[]{photoIdString});
				
				File	photoFile = new File(_photoFolder, photoName);
				File	thumbFile = new File(_thumbFolder, photoName);
				if(photoFile.exists()){
					boolean deleteResult = photoFile.delete();
					Utils.amLog("delete photoFile:"+deleteResult);
				}
				if(thumbFile.exists()){
					boolean deleteResult = thumbFile.delete();
					Utils.amLog("delete thumbFile:"+deleteResult);
				}
				
				//删除完图片后，用本地广播通知照片管理面板，有新增的照片
				Utils.amLog("send delete photo broadcast");
				Intent intent = new Intent(PopPhotoManage.ACTION_DELETE_PHOTO);
				intent.putExtra(ActivitySensorCamera.KEY_FEATUREID, featureId);//附加上FeatureID
				LocalBroadcastManager lbm  = LocalBroadcastManager.getInstance(_context);
				lbm.sendBroadcast(intent);
			}
			
			data.close();
		}
	}
	
	@SuppressLint({ "SimpleDateFormat", "DefaultLocale" })
	public	void	insertPhoto(int featureId, Intent pendingPhoto){
		
		if(null==pendingPhoto)
			return;
		
		String	photoFile	= pendingPhoto.getStringExtra(ActivitySensorCamera.KEY_PHOTOFILE);
		String	cc			= pendingPhoto.getStringExtra(ActivitySensorCamera.KEY_CC);
		String	envDes		= pendingPhoto.getStringExtra(ActivitySensorCamera.KEY_ENV_DES);
		int		count		= pendingPhoto.getIntExtra(ActivitySensorCamera.KEY_COUNT, 0);
		double	bearing		= pendingPhoto.getFloatExtra(ActivitySensorCamera.KEY_BEARING, 0.0f);
		double	pitch		= pendingPhoto.getFloatExtra(ActivitySensorCamera.KEY_PITCH, 0.0f);
		double	roll		= pendingPhoto.getFloatExtra(ActivitySensorCamera.KEY_ROLL, 0.0f);
		double	lon			= pendingPhoto.getDoubleExtra(ActivitySensorCamera.KEY_LON, 0.0);
		double	lat			= pendingPhoto.getDoubleExtra(ActivitySensorCamera.KEY_LAT, 0.0);
		double	alt			= pendingPhoto.getDoubleExtra(ActivitySensorCamera.KEY_ALT, 0.0);
		double	distance	= pendingPhoto.getDoubleExtra(ActivitySensorCamera.KEY_DISTANCE, 0.0);
		Date	date		= (Date) pendingPhoto.getSerializableExtra(ActivitySensorCamera.KEY_DATE);
		
		Utils.amLog("dummy insert photo");
		Utils.amLog("photoFile:"+photoFile);
		Utils.amLog("distance:"+distance);
		Utils.amLog("cc:"+cc+", env:"+envDes);
		Utils.amLog("date:"+date);
		Utils.amLog("-----");
		Utils.amLog("s count;"+count);
		Utils.amLog("lon:"+lon);
		Utils.amLog("lat:"+lat);
		Utils.amLog("alt:"+alt);
		Utils.amLog("-----");
		Utils.amLog("bearing:"+bearing);
		Utils.amLog("pitch:"+pitch);
		Utils.amLog("roll:"+roll);
		
		
		//1先改名
		SimpleDateFormat sdf 	= new SimpleDateFormat("yyyyMMddHHmmss");
		String		dateString	= sdf.format(date);
		
		DMS	lonDMS = GISTools.toDMS(lon);
		DMS latDMS = GISTools.toDMS(lat);
		
		String saveFileName		= String.format("%s%s%s%s%03d.jpg",PHOTO_FILE_PREFIX,dateString,
				lonDMS.formatForFile(),latDMS.formatForFile(),(int)bearing);
		Utils.amLog("saveFileName:"+saveFileName);
		
		File filePending		= new File(photoFile);
		File filePhoto			= new File(_photoFolder,saveFileName);
		boolean ret = filePending.renameTo(filePhoto);
		
		//2生成缩略图
		Bitmap	bmThumb =  _getThumbnail(filePhoto);
		Utils.amLog("ret:"+ret+", bmThumb"+bmThumb);
		{
			//确保缩略图目录
			File  thumbFolder = new File(_projectFolder,FOLDER_THUMB);
			if(!thumbFolder.exists())
				thumbFolder.mkdir();
			//在缩略图目录下建立同名文件
			File  thumbPhoto  = new File(thumbFolder, saveFileName);
			//将缩略图转换成JPG
			ByteArrayOutputStream thumbStream = new ByteArrayOutputStream();       
            bmThumb.compress(Bitmap.CompressFormat.JPEG, 80, thumbStream);
            
            //缩略图存盘
			try {
				FileOutputStream fos	= new FileOutputStream(thumbPhoto);
				fos.write(thumbStream.toByteArray());
				fos.close();
				thumbStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//释放缩略图位图内存
			bmThumb.recycle();
		}
		
		
		//3裁剪相对文件名
		//String photoName = FOLDER_PHOTO+"/"+saveFileName;
		String photoName = saveFileName;
		Utils.amLog("photoName:"+photoName);
		
		
		//4入库
		
		ContentValues	values = new ContentValues();
		values.put(PhotoColumns.FEATURE_ID,featureId);
		values.put(PhotoColumns.PHOTO_NAME,photoName);
		values.put(PhotoColumns.CLASS_CODE,cc);
		values.put(PhotoColumns.ENV_DES,envDes);
		values.put(PhotoColumns.SCOUNT,count);
		values.put(PhotoColumns.BEARING,bearing);
		values.put(PhotoColumns.PITCH, pitch);
		values.put(PhotoColumns.ROLL, roll);
		values.put(PhotoColumns.LON, lon);
		values.put(PhotoColumns.LAT, lat);
		values.put(PhotoColumns.ALT, alt);
		values.put(PhotoColumns.DISTANCE, distance);
		values.put(PhotoColumns.CREATE_TIME, date.getTime());
		
		_dataBase.insert(PhotoColumns.TABLE_NAME, null, values);
		
		
		
		//插入完图片后，用本地广播通知照片管理面板，有新增的照片
		Utils.amLog("send add photo broadcast");
		pendingPhoto.setAction(PopPhotoManage.ACTION_ADD_PHOTO);
		pendingPhoto.putExtra(ActivitySensorCamera.KEY_PHOTOFILE, photoName); //同一个intent的其他数据都可以采用，只需要变更文件名
		pendingPhoto.putExtra(ActivitySensorCamera.KEY_FEATUREID, featureId);//附加上FeatureID
		LocalBroadcastManager lbm  = LocalBroadcastManager.getInstance(_context);
		lbm.sendBroadcast(pendingPhoto);
	}
	
	
	
	/**
	 * 查询图层类别
	 * @return
	 */
	public	List<Pair<String, Integer>>	queryLayerClasses(){
		Cursor	data = _dataBase.query(LayerClassColumns.TABLE_NAME, null, null, null, null, null, null);
		
		List<Pair<String, Integer>>	result = new ArrayList<Pair<String, Integer>>();
		
		while(data.moveToNext()){
			String name = data.getString(data.getColumnIndexOrThrow(LayerClassColumns.CLASS_NAME));
			int	   id	= data.getInt(data.getColumnIndexOrThrow(LayerClassColumns._ID));
			result.add(new Pair<String,Integer>(name,id));
		}
		
		data.close();
		
		return result;
	}

	
	public	Cursor	queryLayersWithClassId(int layerClassId){
		String	idString = String.valueOf(layerClassId);
		Cursor	data = _dataBase.query(LayersColumns.TABLE_NAME, null, LayersColumns.LAYER_CLASS_ID+"=?", 
				new String[]{idString}, null, null, null);
		return data;
	}
	
	public	List<Pair<String, Integer>> querySelection(){
		Cursor	data = _dataBase.query(SelectionColumns.TABLE_NAME, null, null, null, null, null, null);
		
		List<Pair<String, Integer>>	result = new ArrayList<Pair<String, Integer>>();
		
		while(data.moveToNext()){
			String name = data.getString(data.getColumnIndexOrThrow(SelectionColumns.SELECTION_NAME));
			int	   id	= data.getInt(data.getColumnIndexOrThrow(SelectionColumns._ID));
			result.add(new Pair<String,Integer>(name,id));
		}
		
		data.close();
		
		return result;
	}
	
	public	Cursor	querySelectionItemsWithSelectionId(int selectionId){
		String	idString = String.valueOf(selectionId);
		Cursor	data = _dataBase.query(SelectionItemColumns.TABLE_NAME, null, SelectionItemColumns.SELECTION_ID+"=?", 
				new String[]{idString}, null, null, null);
		return data;
	}
	
	
	
	/**
	 * 初始化工作
	 */
	private void	_initWork(){
		
		_account = AppPreference.getAccount(_context);
		
		
		//先尝试创建所有的表
		_dataBase.execSQL(ProfileColumns.CREATE_SQL);
		_dataBase.execSQL(LayerClassColumns.CREATE_SQL);
		_dataBase.execSQL(LayersColumns.CREATE_SQL);
		_dataBase.execSQL(SelectionColumns.CREATE_SQL);
		_dataBase.execSQL(SelectionItemColumns.CREATE_SQL);
		_dataBase.execSQL(FeatureColumns.CREATE_SQL);
		_dataBase.execSQL(GPSPointsColumns.CREATE_SQL);
		_dataBase.execSQL(PhotoColumns.CREATE_SQL);
		
		//然后检查Profile表，得知工程数据是否初始化
		if(!_checkProfileInited())
			_initProjectDB();
		
		//确保照片目录
		_photoFolder = new File(_projectFolder,FOLDER_PHOTO);
		if(!_photoFolder.exists())
			_photoFolder.mkdir();
		_thumbFolder = new File(_projectFolder,FOLDER_THUMB);
		if(!_thumbFolder.exists())
			_thumbFolder.mkdir();
		
	}
	
	/**
	 * 检查profile表是否初始化
	 * @return
	 */
	private boolean	_checkProfileInited(){
	
		boolean	result = false;
		
		do{
			Cursor	profileData = _dataBase.query(ProfileColumns.TABLE_NAME, null, null, null, null, null, null);
			
			if(null==profileData||profileData.getCount()<=0){
				break;
			}
			profileData.moveToFirst();
			try{
				int	inited		= profileData.getInt(profileData.getColumnIndexOrThrow(ProfileColumns.INITED));
				
				if(inited>0)
					result = true;
			}catch(Exception e){
				break;
			}finally{
				profileData.close();
			}
			
			
		}while(false);
	
		
		return result;
	}
	

	
	
	/**
	 * 设置profile初始化标志
	 */
	private void	_setProfileInited(){
		
		Date	now 		= new Date();
	
		ContentValues	values = new ContentValues();
		values.put(ProfileColumns.PROJECT_NAME, "anonymous");
		values.put(ProfileColumns.MAP_NAME, _projectFolder.getName());
		values.put(ProfileColumns.ENTERPRISE, "anonymous");
		values.put(ProfileColumns.PARK, "anonymous");
		values.put(ProfileColumns.AUTHOR, "anonymous");
		values.put(ProfileColumns.DESCRIPTION,"");
		values.put(ProfileColumns.INITED, 1);
		values.put(ProfileColumns.WKID, _wkid);
		values.put(ProfileColumns.CREATE_TIME, now.getTime());
		_dataBase.insert(ProfileColumns.TABLE_NAME, null, values);
		
	}
	
	
	/**
	 * 初始化数据库
	 */
	private void	_initProjectDB(){
		
		//检查project目录是否有配置存在
		File	configFolder = new File(_projectFolder,FOLDER_CONFIG);
		
		boolean	initResult = false;
		
		//根据情况分别以定制的配置或者assets目录的默认配置来初始化工程
		if(configFolder.exists()){
			initResult = _initProjectWithCustomConfig(configFolder);
		}else{
			initResult = _initProjectWithAssetConfig();
		}
		
		if(initResult){
			_setProfileInited();
			//toast报初始化成功
		}else{
			//toast报错
		}
		
	}
	
	/**
	 * 用外部的定制数据初始化
	 * @param configFolder
	 */
	private boolean	_initProjectWithCustomConfig(File configFolder){
		return false;
		
	}
	
	public	static XmlOperator	getUsersDOM(Context context){
		AssetManager am =  context.getAssets();
		XmlOperator domUsers = null;
		try {
			InputStream isUsers = am.open(FOLDER_CONFIG+"/"+"Users.xml");
			domUsers = new XmlOperator(isUsers);
			//isUsers.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return domUsers;
	}
	
	/**
	 * 用内部的assets数据初始化
	 */
	private boolean	_initProjectWithAssetConfig(){
		
		boolean	result = false;
		AssetManager am =  _context.getAssets();
		
		do{
			//先加载LayerClass.xml
			try {
				InputStream isLayerClass = am.open(FOLDER_CONFIG+"/"+"LayerClass.xml");
				XmlOperator domLayerClass = new XmlOperator(isLayerClass);
				_loadLayerClass(domLayerClass);
				domLayerClass.Destory();
				isLayerClass.close();
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
			
			
			//再加载Layers目录
			try {
				String	layersPath  = FOLDER_CONFIG + "/" + FOLDER_LAYERS;
				String[] layersList = am.list(layersPath);
				
				for(String layer:layersList){
					String layerFile = layersPath + "/" + layer;
					
					InputStream isLayer = am.open(layerFile);
					XmlOperator domLayer = new XmlOperator(isLayer);
					_loadLayer(domLayer);
					domLayer.Destory();
					isLayer.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
			
			//再加载Select目录
			try {
				String	selectPath  = FOLDER_CONFIG + "/" + FOLDER_SELECT;
				String[] selectList = am.list(selectPath);
				
				for(String select:selectList){
					String selectFile = selectPath + "/" + select;
					
					InputStream isSelect = am.open(selectFile);
					XmlOperator domSelect = new XmlOperator(isSelect);
					_loadSelect(domSelect);
					domSelect.Destory();
					isSelect.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
			
			
			result = true;
		}while(false);
		
		//。。这个不能关
		//am.close();
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private void	_loadLayerClass(XmlOperator xml) throws JDOMException, IOException{
		
		Element root = xml.getRoot();
		List<Element> children = root.getChildren(LayerClassDom.TAG_LAYER_CLASS);
		
		ContentValues	values = new ContentValues();

		try{
			for(Element child : children){
				String id 	= child.getAttributeValue(LayerClassDom.ATTR_ID);
				String name	= child.getAttributeValue(LayerClassDom.ATTR_NAME);
				values.clear();
				values.put(LayerClassColumns._ID, id);
				values.put(LayerClassColumns.CLASS_NAME, name);
				
				_dataBase.insert(LayerClassColumns.TABLE_NAME, null, values);
			}
		}catch(Exception e){
			e.printStackTrace();
		}	
	}
	
	
	private void	_loadLayer(XmlOperator xml) throws JDOMException, IOException{
		Element root = xml.getRoot();
		
		String	name 			= root.getAttributeValue(LayerDom.ATTR_NAME);
		String	type			= root.getAttributeValue(LayerDom.ATTR_TYPE);
		String	symbol			= root.getAttributeValue(LayerDom.ATTR_SYMBOL, "");
		String	displayName		= root.getAttributeValue(LayerDom.ATTR_DISPLAYNAME, "");
		String	annotation		= root.getAttributeValue(LayerDom.ATTR_ANNOTATION, "");
		int		classId			= Integer.parseInt(root.getAttributeValue(LayerDom.ATTR_CLASSID));
		int		size			= Integer.parseInt(root.getAttributeValue(LayerDom.ATTR_SIZE, "1"));
		int		alpha			= Integer.parseInt(root.getAttributeValue(LayerDom.ATTR_ALPHA, "255"));
		int		red				= Integer.parseInt(root.getAttributeValue(LayerDom.ATTR_RED, "255"));
		int		green			= Integer.parseInt(root.getAttributeValue(LayerDom.ATTR_GREEN, "255"));
		int		blue			= Integer.parseInt(root.getAttributeValue(LayerDom.ATTR_BLUE, "255"));
		int		photo			= Integer.parseInt(root.getAttributeValue(LayerDom.ATTR_WITH_PHOTO, "0"));
		String	xmlString		= xml.getXmlDocContent();
		
		
		ContentValues	values = new ContentValues();
		values.put(LayersColumns.NAME, name);
		values.put(LayersColumns.GEO_TYPE, type);
		values.put(LayersColumns.WITH_PHOTO, photo);
		values.put(LayersColumns.SYMBOL, symbol);
		values.put(LayersColumns.DISPLAY_NAME, displayName);
		values.put(LayersColumns.ANNOTATION, annotation);
		values.put(LayersColumns.LAYER_CLASS_ID, classId);
		values.put(LayersColumns.SIZE, size);
		values.put(LayersColumns.ALPHA, alpha);
		values.put(LayersColumns.RED, red);
		values.put(LayersColumns.GREEN, green);
		values.put(LayersColumns.BLUE, blue);
		values.put(LayersColumns.FIELDS, xmlString);
		_dataBase.insert(LayersColumns.TABLE_NAME, null, values);
	}
	
	@SuppressWarnings("unchecked")
	private void	_loadSelect(XmlOperator xml) throws JDOMException, IOException{
		Element root = xml.getRoot();
		
		String name	= root.getAttributeValue(SelectDom.ATTR_NAME);
		ContentValues	values = new ContentValues();
		values.put(SelectionColumns.SELECTION_NAME, name);
		int id = (int)_dataBase.insert(SelectionColumns.TABLE_NAME, null, values);
		
		List<Element> children = root.getChildren(SelectDom.TAG_ITEM);
		for(Element child : children){
			values.clear();
			
			String	value			= child.getAttributeValue(SelectDom.ATTR_VALUE);
			String	displayName		= child.getAttributeValue(SelectDom.ATTR_DISPALYNAME, "");
		
			
			values.put(SelectionItemColumns.ITEM_VALUE, value);
			values.put(SelectionItemColumns.DISPLAY_NAME, displayName);
			values.put(SelectionItemColumns.SELECTION_ID, id);
			_dataBase.insert(SelectionItemColumns.TABLE_NAME, null, values);
		}
	}
	
	
	/**
	 * 取得缩略图
	 * @param photoFile
	 * @return
	 */
	private	Bitmap	_getThumbnail(File photoFile){
		BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高
        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options); //此时返回bm为空
        options.inJustDecodeBounds = false;
         //计算缩放比
        int be = (int)(options.outHeight / (float)THUMB_HEIGHT);
        if (be <= 0)
            be = 1;
        options.inSampleSize = be;
        //重新读入图片
        bitmap=BitmapFactory.decodeFile(photoFile.getAbsolutePath(),options);
        
        return bitmap;
	}
	
	/**
	 * LayerClass xml 的各种标签
	 * @author xoozi
	 *
	 */
	private interface	LayerClassDom{
		public static final String	TAG_LAYER_CLASS = "LayerClass";
		public static final String	ATTR_ID			= "Id";
		public static final String	ATTR_NAME		= "Name";
	}

	/**
	 * Layer xml的各种标签
	 * @author xoozi
	 *
	 */
	private interface	LayerDom{
		public	static final String	ATTR_NAME		= "Name";
		public  static final String ATTR_DISPLAYNAME= "DisplayName";
		public  static final String	ATTR_ANNOTATION = "Annotation";
		public	static final String ATTR_CLASSID	= "ClassId";
		public  static final String ATTR_WITH_PHOTO = "WithPhoto";
		public	static final String ATTR_TYPE		= "Type";
		public	static final String ATTR_SIZE		= "Size";
		public	static final String ATTR_SYMBOL		= "Symbol";
		public	static final String ATTR_ALPHA		= "Alpha";
		public	static final String ATTR_RED		= "Red";
		public	static final String ATTR_GREEN		= "Green";
		public	static final String ATTR_BLUE		= "Blue";
				
	}
	
	/**
	 * Select xml的各种标签
	 * @author xoozi
	 *
	 */
	private interface 	SelectDom{
		public  static final String	TAG_ITEM		= "Item";
		public	static final String	ATTR_NAME		= "Name";
		public	static final String	ATTR_VALUE		= "Value";
		public  static final String	ATTR_DISPALYNAME= "DisplayName";
	}
	

}

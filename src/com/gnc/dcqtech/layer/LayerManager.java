package com.gnc.dcqtech.layer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.util.Pair;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.gnc.dcqtech.project.Project;
import com.gnc.dcqtech.project.ProjectDataBase.FeatureColumns;
import com.gnc.dcqtech.utils.WKT;
import com.xoozi.andromeda.utils.Utils;

/**
 * 图层管理器
 * @author xoozi
 *
 */
public class LayerManager {
	private static LayerManager 			_singleTon;
	private	static List<LayerClass>			_layerClasses 	= new ArrayList<LayerClass>();
	private static Map<String,Selection>	_selectionCache	= new HashMap<String,Selection>();
	
	private static GraphicsLayer			_editLayer;//编辑用图层
	private static GraphicsLayer			_gpsMarkerLayer ;//GPS标注图层
	//private static GraphicsLayer			_photoOrientationLayer ;
	
	
	/**
	 * 单件初始化
	 */
	public static void initLayerManager(Project project){
		if(null==_singleTon){
			_singleTon = new LayerManager(project);
		}else{
			_singleTon._initWork(project);
		}
	}
	
	
	/**
	 * 加载
	 * @param project
	 */
	public	static void	loadFeature(Context context,Project project){
		
	
		Cursor	data = project.queryAllFeature();
		
		while(data.moveToNext()){
			
			int	id 		= data.getInt(data.getColumnIndexOrThrow(FeatureColumns._ID));
			int	state 		= data.getInt(data.getColumnIndexOrThrow(FeatureColumns.STATE));
			
			if(FeatureColumns.STATE_VALUE_DELETE==state)
				continue;
			
			int	layerId	= data.getInt(data.getColumnIndexOrThrow(FeatureColumns.LAYER_ID));
			String	cc 	= data.getString(data.getColumnIndexOrThrow(FeatureColumns.CC));
			String  geo = data.getString(data.getColumnIndexOrThrow(FeatureColumns.GEO_DATA));
			
			Layer layer = LayerClass.getLayerById(layerId);
			
			if(null!=layer){
				layer.addFeature(context,project,id, WKT.WKTToGeometry(geo), cc);
			}
		}
		
		
		data.close();
	}
	
	/**
	 * 重新调整地图上的图层
	 * @param project
	 */
	public	static void	adjustLayerOnMap(Project project, MapView mapView){
		
		//先移除所有图层
		//mapView.removeAll();
		com.esri.android.map.Layer[] oldLayers = mapView.getLayers();
		Utils.amLog("layers count:"+oldLayers.length);
		for(com.esri.android.map.Layer layer: oldLayers){
			mapView.removeLayer(layer);
		}
		
		
		
		//然后先添加底图
		mapView.addLayer(project.getTiledLayer());
		
		
		
		//然后添加所有的GraphicsLayer, 依layerclass的顺序添加
		for(LayerClass layerClass:_layerClasses){
			
			List<Layer> layers = layerClass.getLayers();
			for(Layer layer:layers){
				
				layer.addLayerToMapView(mapView);
			}
		}
		
		
		
		//清理gps标注图层 
		if(null==_gpsMarkerLayer){
			_gpsMarkerLayer = new GraphicsLayer();
		}else{
			_gpsMarkerLayer.recycle();
			_gpsMarkerLayer = new GraphicsLayer();
		}
		mapView.addLayer(_gpsMarkerLayer);
		
		//最后，清理一下编辑图层，加在最上面
		if(null==_editLayer){
			_editLayer = new GraphicsLayer();
		}else{
			_editLayer.recycle();
			_editLayer = new GraphicsLayer();
		}
		mapView.addLayer(_editLayer);
		
	}
	
	
	/**
	 * 退出应用前清除
	 * @param mapView
	 */
	public	static void	cleanUp(MapView mapView){
		//先移除所有图层
		mapView.removeAll();
		
		
		//释放图层
		LayerClass.clean();
		
		//释放特殊图层
		_editLayer.recycle();
		
	}
	
	
	
	public	static GraphicsLayer	getEditLayer(){
		return _editLayer;
	}
	
	public	static GraphicsLayer	getGPSMarkerLayer(){
		return _gpsMarkerLayer;
	}

	
	
	/**
	 * 查询图层分类个数
	 * @return
	 */
	public	static int	getLayerClassCount(){
		return _layerClasses.size();
	}
	
	/**
	 * 获取图层分类
	 * @return
	 */
	public	static LayerClass	getLayerClass(int index){
		return _layerClasses.get(index);
	}
	
	public	static Layer		getLayerById(int id){
		return LayerClass.getLayerById(id);
	}
	
	
	/**
	 * 通过选择器名获取选择器
	 * @param name
	 * @return
	 */
	public	static Selection	getSelectionByName(String name){
		return _selectionCache.get(name);
	}
	
	/**
	 * 私用构造方法
	 * @param project
	 */
	private	LayerManager(Project project){
		_initWork(project);
	}
	
	private	static void	_clean(){
		_layerClasses.clear();
		_selectionCache.clear();
		LayerClass.clean();
		System.gc();
	}
	
	
	/**
	 * 私用初始化方法
	 * @param project
	 */
	private	void		_initWork(Project project){
		
		_clean();
		
		//从Project中查询选择器
		List<Pair<String,Integer>>	selections = project.querySelection();
				
		//挨个加载selection
		for(Pair<String,Integer> selection:selections){
			
			String	selectionName = selection.first;
			int		selectionId	  = selection.second;
			Cursor data = project.querySelectionItemsWithSelectionId(selectionId);
					
			Selection	aSelection = new Selection(selectionName,data);
					
			data.close();
					
			_selectionCache.put(selectionName, aSelection);
		}
		
		//从project中查询图层类别
		List<Pair<String,Integer>>	result = project.queryLayerClasses();
		
		//挨个加载图层类别
		for(Pair<String,Integer> layerClass:result){
			_layerClasses.add(new LayerClass(project, layerClass.first, layerClass.second));
		}
		
	}
}

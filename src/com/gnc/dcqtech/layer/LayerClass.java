package com.gnc.dcqtech.layer;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.util.SparseArray;

import com.gnc.dcqtech.project.Project;

public class LayerClass {
	
	private static SparseArray<Layer>   _layerCache = new SparseArray<Layer>();

	
	private Project				_project;
	private String				_layerClassName;
	private int					_layerClassId;
	private List<Layer> 		_layers = new ArrayList<Layer>();
	
	
	static Layer	getLayerById(int layerId){
		return _layerCache.get(layerId);
	}
	
	static SparseArray<Layer> getLayerCache(){
		return _layerCache;
	}
	
	static	void	clean(){
		int count = _layerCache.size();
		
		for(int index = 0;index<count; index++){
			_layerCache.get(_layerCache.keyAt(index)).clean();
		}
		_layerCache.clear();
	}
	
	LayerClass(Project project, String layerClassName, int layerClassId){
		_project		= project;
		_layerClassName	= layerClassName;
		_layerClassId	= layerClassId;
		_initWork();
	}
	
	public String	getClassName(){
		return _layerClassName;
	}
	
	public int		getClassId(){
		return _layerClassId;
	}
	
	public	List<Layer>	getLayers(){
		return _layers;
	}
	
	private void	_initWork(){
		//通过图层类别名字加载子图层
		Cursor	data	= _project.queryLayersWithClassId(_layerClassId);
	
		while(data.moveToNext()){
	
			Layer	child = new Layer(data);
			_layers.add(child);
			_layerCache.put(child.getLayerId(), child);
		}
		
		
		data.close();
	}

}

package com.gnc.dcqtech.uicontroll;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.esri.android.map.Callout;
import com.esri.android.map.CalloutStyle;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.Geometry.Type;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.Symbol;
import com.esri.android.map.MapOnTouchListener;
import com.gnc.dcqtech.R;
import com.gnc.dcqtech.layer.Layer;
import com.gnc.dcqtech.layer.LayerClass;
import com.gnc.dcqtech.layer.LayerManager;
import com.gnc.dcqtech.project.ProjectDataBase.FeatureColumns;
import com.gnc.dcqtech.utils.WKT;
import com.xoozi.andromeda.uicontroller.SelfInflaterPanel;
import com.xoozi.andromeda.utils.Utils;

public class PanelDrawTool extends SelfInflaterPanel implements OnCheckedChangeListener, OnClickListener{
	
	private IPanelDrawToolAction		_panelDrawToolAction;
	
	private LayerClass		_layerClass;
	private	Layer			_layer;
	private LayerIcon		_layerIcon;
	private Switch			_switchFreeDraw;
	private MapView			_mapView;
	private GraphicsLayer	_editLayer;
	
	//各个按钮
	private	Button			_btnGoback;
	private Button			_btnUnSelect;
	private Button			_btnDeleteLastNode;
	private Button			_btnClear;
	
	//地图触屏事件监听
	private DrawToolTouchListener _drawToolTouchListener ;
	
	//编辑策略
	private EditStrategy		_editStrategyNull 		= new EditStrategyNull(); 		//空策略
	private EditStrategy		_editStrategyPoint		= new EditStrategyPoint();		//点编辑策略
	private EditStrategy		_editStrategyLinestring = new EditStrategyLinestring();	//线编辑策略
	private EditStrategy		_editStrategyPolygon	= new EditStrategyPolygon(); 	//多边形编辑策略
	private EditStrategy		_currentEditStrategy    = _editStrategyNull;			//当前编辑策略
	
	//数据容器
	private List<Point> 		_vertexPoints 	= new ArrayList<Point>();	//当前编辑顶点列表
	private List<Point> 		_midPoints 		= new ArrayList<Point>();	//当前编辑中点列表
	private List<EditingStates> _editStatesList = new ArrayList<EditingStates>();//编辑状态列表，用于回退
	
	//几何编辑相关数据
	private FeatureEditStates   _featureEditStates		= new FeatureEditStates();

	public PanelDrawTool(Context context, LinearLayout baseLayout, IPanelDrawToolAction canBeSaved,MapView mapView) {
		super(context, baseLayout);
		_panelDrawToolAction = canBeSaved;
		_mapView = mapView;
		_initWork();
	}
	
	@Override
	public void onClick(View view) {
		switch(view.getId()){
		
		case R.id.btn_drawtools_go_back:
			_clickGoBack();
			break;
			
		case R.id.btn_drawtools_clear:
			_clearDrawTool();
			break;
			
		case R.id.btn_drawtools_delete_last_node:
			_clickDeleteLastNode();
			break;
			
		case R.id.btn_drawtools_unselect:
			_clickUnselect();
			break;
			
		case R.id.btn_drawtools_cancel:
			_selfCancel();
			
			break;
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		//根据自由笔的开关情况  修改绘制策略
		_featureEditStates.freeDraw = isChecked;
		_clearDrawTool();
		_adjustUI();
	}
	
	/**
	 * 外界劝退
	 */
	public	void	callCancel(){
		setVisibility(View.INVISIBLE);
		_clearDrawTool();
		
		//需要清空策略
		_currentEditStrategy = _editStrategyNull;
	}
	
	/**
	 * 自己退
	 */
	private void	_selfCancel(){
		setVisibility(View.INVISIBLE);
		_clearDrawTool();
		
		//需要清空策略
		_currentEditStrategy = _editStrategyNull;
		
		_panelDrawToolAction.onCancel();
	}
	
	public	void	setLayerClass(LayerClass layerClass){
		_layerClass = layerClass;
	}
	
	public	void	setLayer(Layer layer){
		
		Utils.amLog("wtttttfffff");
		_layer = layer;
		_layerIcon.setLayer(_layer);
		_editLayer = LayerManager.getEditLayer();
		
		setVisibility(View.VISIBLE);
		
		resetStatus();
	}
	
	public	void	editingEnd(){
		_selfCancel();
	}
	
	public	void	setEditLayer(Layer layer, String geoData){
		setLayer(layer);
		
		_loadGeometry(geoData);
	}
	
	public	void	setFeatureDisplay(Layer layer, String geoData){
		setLayer(layer);
		
		_loadGeometryDisplay(geoData);
	}
	
	public	Layer	getCurrentLayer(){
		return _layer;
	}
	
	/**
	 * 获取当前编辑的成果几何对象
	 * @return
	 */
	public	Geometry	getGeometry(){
		Geometry result = null;
		
		if(_currentEditStrategy == _editStrategyPoint){
			if(_vertexPoints.size()>=1)
				result = _vertexPoints.get(0);
		}else if(_currentEditStrategy == _editStrategyLinestring){
			
			MultiPath multipath=null;
			multipath = _currentEditStrategy == _editStrategyLinestring ? new Polyline(): new Polygon() ;
			multipath.startPath(_vertexPoints.get(0));
			for (int i = 1; i < _vertexPoints.size(); i++) {
				multipath.lineTo(_vertexPoints.get(i));
			}
			result = multipath;
		}else if(_currentEditStrategy == _editStrategyPolygon){
			
			MultiPath multipath=null;
			multipath = _currentEditStrategy == _editStrategyLinestring ? new Polyline(): new Polygon() ;
			multipath.startPath(_vertexPoints.get(0));
			for (int i = 1; i < _vertexPoints.size(); i++) {
				multipath.lineTo(_vertexPoints.get(i));
			}
			//最后让曲线闭合
			multipath.lineTo(_vertexPoints.get(0));
			result = multipath;
		}
		
		return result;
	}
	
	
	/**
	 * 判断当前的图形是否能保存,
	 * 空策略时直接false
	 * 或者类型是点，而顶点数小于1
	 * 或者类型是线，而顶点数小于2
	 * 或者类型是面，而顶点数小于3，返回false，
	 */
	private	boolean	_canSaveGeo(){
		
		boolean result = true;
		if(_currentEditStrategy == _editStrategyPoint){
			
			if(_vertexPoints.size()<1)
				result = false;
		}else if(_currentEditStrategy == _editStrategyLinestring){
			
			if(_vertexPoints.size()<2)
				result = false;
		}else if(_currentEditStrategy == _editStrategyPolygon){
			
			
			if(_vertexPoints.size()<3)
				result = false;
		}else if(_currentEditStrategy == _editStrategyNull){
			result = false;
		}
		
		return result;
	}
	
	
	private void _initWork(){
		initPanel(R.layout.panel_draw_tool);
		
		//设置一下callout的细节
				CalloutStyle calloutStyle = new CalloutStyle();
				calloutStyle.setBackgroundAlpha(0x40);
				calloutStyle.setBackgroundColor(Color.BLACK);
				Callout	callout = _mapView.getCallout();
				callout.setStyle(calloutStyle);
				callout.setMaxHeight(700);
				callout.setMaxWidth(700);
		
		_layerIcon	= (LayerIcon)_baseLayout.findViewById(R.id.layericon_current);
		
		_switchFreeDraw = (Switch) _baseLayout.findViewById(R.id.switch_free_draw);
		
		_switchFreeDraw.setOnCheckedChangeListener(this);
		
		_drawToolTouchListener = new DrawToolTouchListener(_context, _mapView);
		
		_mapView.setOnTouchListener(_drawToolTouchListener);
		
		_btnGoback			= (Button) _baseLayout.findViewById(R.id.btn_drawtools_go_back);
		_btnUnSelect		= (Button) _baseLayout.findViewById(R.id.btn_drawtools_unselect);
		_btnClear			= (Button) _baseLayout.findViewById(R.id.btn_drawtools_clear);
		_btnDeleteLastNode	= (Button) _baseLayout.findViewById(R.id.btn_drawtools_delete_last_node);
		
		_btnGoback.setOnClickListener(this);
		_btnUnSelect.setOnClickListener(this);
		_btnClear.setOnClickListener(this);
		_btnDeleteLastNode.setOnClickListener(this);
		
		_baseLayout.findViewById(R.id.btn_drawtools_cancel).setOnClickListener(this);
		
		_adjustUI();
		
		//初始化后把自己隐藏
		setVisibility(View.INVISIBLE);
	}
	
	/**
	 * 给父控件查询是否能保存的接口
	 */
	public	void	queryCanSave(){
		//向父控件传递能否保存几何对象的状态
		_panelDrawToolAction.onCheckCanBeSaved(_layer, _canSaveGeo());
	}
	
	/**
	 * 每次指定图层后，重置状态
	 */
	public void resetStatus(){
		Utils.amLog("resetStatus");
		_clearDrawTool();
		
		//每次重置，都默认关闭自由笔
		_switchFreeDraw.setChecked(false);
		
		//点模式不需要自由笔开关
		if(Layer.TYPE_VALUE_POINT.equals(_layer.getType())){
			_switchFreeDraw.setVisibility(View.GONE);
		}else{
			_switchFreeDraw.setVisibility(View.VISIBLE);
		}
		
		//根据图层的类型 选择编辑策略
		if(Layer.TYPE_VALUE_POINT.equals(_layer.getType())){
			_currentEditStrategy = _editStrategyPoint;
		}else if(Layer.TYPE_VALUE_LINESTRING.equals(_layer.getType())){
			_currentEditStrategy = _editStrategyLinestring;
		}else if(Layer.TYPE_VALUE_POLYGON.equals(_layer.getType())){
			_currentEditStrategy = _editStrategyPolygon;
		}
		
		//调整按钮状态
		_adjustUI();
	}
	
	/**
	 * 图形编辑：回退
	 */
	private void _clickGoBack(){
		_currentEditStrategy.onGoBack();
	}
	
	/**
	 * 图形编辑：删除最后添加节点
	 */
	private void _clickDeleteLastNode(){
		_currentEditStrategy.onDeleteLastNode();
	}
	
	/**
	 * 图像编辑，取消顶点选择
	 */
	private void _clickUnselect(){
		_currentEditStrategy.onUnselect();
	}
	
	/**
	 * 清除当前绘制
	 */
	private void	_clearDrawTool(){
		
		
		
		//清空状态列表
		_editStatesList.clear();
				
		_currentEditStrategy.onClear();
	}
	
	
	
	private void	_loadGeometry(String geoData){
		Geometry geometry = WKT.WKTToGeometry(geoData);
		
		Type geoType =  geometry.getType();
		
		//然后加载顶点
		if(geoType==Type.POINT){
	
			_vertexPoints.add((Point)geometry);
	
			//Graphic graphicMask = new Graphic(geometry,_getMaskSymbolObject());
			//_featureEditStates.editedMaskId = _displayLayer.addGraphic(graphicMask);
		}else if(geoType==Type.POLYLINE){
			
			MultiPath	path = (MultiPath)geometry;
			int	pointCount = path.getPointCount();
			for(int index=0;index<pointCount;index++){
				_vertexPoints.add(path.getPoint(index));
			}
		}else if(geoType==Type.POLYGON){
			
			MultiPath	path = (MultiPath)geometry;
			int	pointCount = path.getPointCount();
			for(int index=0;index<pointCount;index++){
				_vertexPoints.add(path.getPoint(index));
			}
		}
	
		//dummy 20130502 添加 存一个基本状态
		_editStatesList.add(new EditingStates(_vertexPoints, 
				_featureEditStates.isMidPointSelected,
				_featureEditStates.isVertexSelected, 
				_featureEditStates.selectPointIndex));
	
	
		//4refresh
		_refresh();
	}
	
	
	private void	_loadGeometryDisplay(String geoData){
		Geometry geometry = WKT.WKTToGeometry(geoData);
		
		Type geoType =  geometry.getType();
		
		//然后加载顶点
		if(geoType==Type.POINT){
	
			_vertexPoints.add((Point)geometry);
			_mapView.zoomToScale((Point)geometry, 3000);
			//Graphic graphicMask = new Graphic(geometry,_getMaskSymbolObject());
			//_featureEditStates.editedMaskId = _displayLayer.addGraphic(graphicMask);
		}else if(geoType==Type.POLYLINE){
			
			MultiPath	path = (MultiPath)geometry;
			int	pointCount = path.getPointCount();
			for(int index=0;index<pointCount;index++){
				_vertexPoints.add(path.getPoint(index));
			}
		}else if(geoType==Type.POLYGON){
			
			MultiPath	path = (MultiPath)geometry;
			int	pointCount = path.getPointCount();
			for(int index=0;index<pointCount;index++){
				_vertexPoints.add(path.getPoint(index));
			}
		}
	
		//dummy 20130502 添加 存一个基本状态
		_editStatesList.add(new EditingStates(_vertexPoints, 
				_featureEditStates.isMidPointSelected,
				_featureEditStates.isVertexSelected, 
				_featureEditStates.selectPointIndex));
	
	
		//4refresh
		_refresh();
	}
	
	private void	_hideCallout(){
		_mapView.getCallout().hide();
	}
	
	private void	_searchNearestGraphic(float x, float y){
		
		if(null!=_layerClass){
			List<Pair<Integer,Graphic>> result	= new ArrayList<Pair<Integer,Graphic>>();
			List<Layer>	  layers	= _layerClass.getLayers();
			
			//查找所属图层类别的所有图层
			for(Layer layer:layers){
		
				int[] graphicIDs  = layer.getGraphicIDs(x,y,30,3);
				for(int gid:graphicIDs){
					Graphic g = layer.getGraphic(gid);
					result.add(new Pair<Integer,Graphic>(gid,g));
				}
			}
			
			Utils.amLog("get graphic count:"+result.size());

			if(result.size()>0){
				_showCallout(result, x,y);
			}
		}
	}
	
	/**
	 * 在多个要素密集位置，显示一个带列表的气泡
	 * @param graphicIDs
	 * @param x
	 * @param y
	 */
	private void _showCallout(List<Pair<Integer,Graphic>> graphicsData,final float x, final float y){
		
		LinearLayout	myList = new LinearLayout(_context);
		myList.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams lpBase = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		
		LinearLayout.LayoutParams lpTxt = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		lpTxt.topMargin = 5;
		lpTxt.bottomMargin = 5;
		myList.setLayoutParams(lpBase);
		for(Pair<Integer,Graphic> gData :graphicsData){
			
			Graphic g		= gData.second;
			int		gId		= gData.first;
			int 	id 		= (Integer)g.getAttributeValue(FeatureColumns._ID);
			String cc 		= (String)g.getAttributeValue(FeatureColumns.CC);
			int	   layerId	= (Integer)g.getAttributeValue(FeatureColumns.LAYER_ID);
			do{
			
				
				CalloutOneItem callOutItem = new CalloutOneItem(_context, layerId,cc);
				View	callOutView = callOutItem.getView();
				
				callOutView.setTag(new Pair<Integer,Integer>(id,gId));
				callOutView.setOnClickListener(new OnClickListener() {
					
					@SuppressWarnings("unchecked")
					public void onClick(View view) {
						
						Pair<Integer,Integer>  awsome = (Pair<Integer,Integer>)view.getTag();
						_hideCallout();
						
						_panelDrawToolAction.startEditFeatrue(awsome);
					}
				});
				
				myList.addView(callOutView);
				
			}while(false);
		
		}
		
		TextView	hint = new TextView(_context);
		hint.setTextColor(Color.argb(0xEE, 0xEE, 0xEE, 0xEE));
		hint.setTextSize(23);
		hint.setText(R.string.label_select_to_edit);
		
		myList.addView(hint);
		
		Point pt ;
		pt = _mapView.toMapPoint(x,y);
		_mapView.getCallout().show(pt,myList);
	}
	
	/**
	 * 刷新editLayer的显示
	 */
	private void _refresh(){
		//移除editLayer上所有要素
		_editLayer.removeAll();
		
		//挨着画3类要素
		_renderPolyline();
		
		//自由笔模式，不画顶点和中点
		if(!_featureEditStates.freeDraw){
			_renderMidPoints();
			_renderVertices();
		}
		
		
		
		//按条件决定按钮状态
		_adjustUI();
		
		
	}
	
	/**
	 * 按条件调整ui
	 */
	private void 	_adjustUI(){
		
		
		
		
		//如果有点被选中，就使取消选择按钮可用，反之禁用
		if(_featureEditStates.isMidPointSelected ||
			_featureEditStates.isVertexSelected){
			_btnUnSelect.setEnabled(true);
		}else{
			_btnUnSelect.setEnabled(false);
		}
		
		//如果有保存编辑状态，就使回退按钮可用，反之禁用
		if(_editStatesList.size()>0){
			_btnGoback.setEnabled(true);
		}else{
			_btnGoback.setEnabled(false);
		}
		
		//如果有节点，就使删除最近添加节点按钮可用，反之禁用
		if(_vertexPoints.size()>0){
			
			_btnDeleteLastNode.setEnabled(true);
			_btnClear.setEnabled(true);
		}else{
			_btnDeleteLastNode.setEnabled(false);
			_btnClear.setEnabled(false);
		}
		
		//自由笔模式就要隐藏不必要的按钮
		if(_featureEditStates.freeDraw){
			_btnUnSelect.setVisibility(View.GONE);
			_btnGoback.setVisibility(View.GONE);
			_btnDeleteLastNode.setVisibility(View.GONE);
		}else{
			_btnUnSelect.setVisibility(View.VISIBLE);
			_btnGoback.setVisibility(View.VISIBLE);
			_btnDeleteLastNode.setVisibility(View.VISIBLE);
		}
		
		//点编辑模式，不需要这个按钮
		if(_currentEditStrategy==_editStrategyPoint){
			_btnDeleteLastNode.setVisibility(View.GONE);
		}else{
			_btnDeleteLastNode.setVisibility(View.VISIBLE);
		}
		
		//向父控件传递能否保存几何对象的状态
		_panelDrawToolAction.onCheckCanBeSaved(_layer, _canSaveGeo());
		
	}
	
	
	/**
	 * 在editLayer上绘制顶点
	 */
	private void _renderVertices(){
		int index;
		
		index = 0;
		
		for (Point pt : _vertexPoints) {
			
			 //如果是选中的顶点
			if (_featureEditStates.isVertexSelected && index == _featureEditStates.selectPointIndex){
				Graphic graphic = new Graphic(pt,new SimpleMarkerSymbol(Color.RED, 20,
						SimpleMarkerSymbol.STYLE.CIRCLE));
				
				_editLayer.addGraphic(graphic);
			}
			//没有选中时的最后一点
			else if (index == _vertexPoints.size() - 1){
				Graphic graphic = new Graphic(pt,new SimpleMarkerSymbol(Color.BLUE, 20,
						SimpleMarkerSymbol.STYLE.CIRCLE));			
				
				_editLayer.addGraphic(graphic);
				
	
				
			}
			//普通的顶点
			else{
				Graphic graphic = new Graphic(pt,new SimpleMarkerSymbol(Color.BLACK, 20,
						SimpleMarkerSymbol.STYLE.CIRCLE));
				
				_editLayer.addGraphic(graphic);
			}
			
			index++;
		}

	}
	
	/**
	 *  生成并绘制线段中点
	 */
	private void _renderMidPoints(){
		int index;
		Graphic graphic;
		
		//如果顶点数大于2，需要计算中点
		if (_vertexPoints.size() > 1) {
			_midPoints.clear();
			for (int i = 1; i < _vertexPoints.size(); i++) {
				Point p1 = _vertexPoints.get(i - 1);
				Point p2 = _vertexPoints.get(i);
				_midPoints.add(new Point((p1.getX() + p2.getX()) / 2,
						(p1.getY() + p2.getY()) / 2));
			}
			
			//如果是画多边形，需要将最后一点和第一点相连
			if (_currentEditStrategy == _editStrategyPolygon) { 
				Point p1 = _vertexPoints.get(0);
				Point p2 = _vertexPoints.get(_vertexPoints.size() - 1);
				_midPoints.add(new Point((p1.getX() + p2.getX()) / 2,
						(p1.getY() + p2.getY()) / 2));
			}
			
			index = 0;
			for (Point pt : _midPoints) {
				
				//画选中状态的中点
				if (_featureEditStates.isMidPointSelected && _featureEditStates.selectPointIndex == index)
					graphic = new Graphic(pt,new SimpleMarkerSymbol(Color.RED, 20,
							SimpleMarkerSymbol.STYLE.CIRCLE));
				else
					graphic = new Graphic(pt,new SimpleMarkerSymbol(Color.GREEN, 15,
							SimpleMarkerSymbol.STYLE.CIRCLE));
				_editLayer.addGraphic(graphic);
				index++;
			}
		}
	}
	
	/**
	 * 绘制线型要素各线段或者多边形要素的各边
	 */
	private void _renderPolyline(){
		
		//只有一个点，或者没有点的情况，不需要画边
		if (_vertexPoints.size() <= 1)
			return;
		Graphic graphic;		
		MultiPath multipath;
		
		if (_currentEditStrategy == _editStrategyLinestring)
			multipath = new Polyline();
		else
			multipath = new Polygon();
		multipath.startPath(_vertexPoints.get(0));
		for (int i = 1; i < _vertexPoints.size(); i++) {
			multipath.lineTo(_vertexPoints.get(i));
		}
		
		
		if (_currentEditStrategy == _editStrategyLinestring)
			graphic = new Graphic(multipath,_layer.getSymbolObject());
		else {
			graphic = new Graphic(multipath,_layer.getSymbolObject());
		}
		
		_editLayer.addGraphic(graphic);
	}
	
	/**
	 * 移动点
	 * @param point
	 */
	private void _movePoint(Point point) {
		
		//如果中点被选择，将它移到新位置并作为一个新的顶点
		if (_featureEditStates.isMidPointSelected) {
			_vertexPoints.add(_featureEditStates.selectPointIndex + 1, point);
			_editStatesList.add(new EditingStates(_vertexPoints, 
					_featureEditStates.isMidPointSelected,
					_featureEditStates.isVertexSelected, 
					_featureEditStates.selectPointIndex));
		} 
		//如果顶点被选中，更新对应位置的location
		else if (_featureEditStates.isVertexSelected) {
			ArrayList<Point> temp = new ArrayList<Point>();
			for (int i = 0; i < _vertexPoints.size(); i++) {
				if (i == _featureEditStates.selectPointIndex)
					temp.add(point);
				else
					temp.add(_vertexPoints.get(i));
			}
			_vertexPoints.clear();
			_vertexPoints.addAll(temp);
			_editStatesList.add(new EditingStates(_vertexPoints, 
					_featureEditStates.isMidPointSelected,
					_featureEditStates.isVertexSelected, 
					_featureEditStates.selectPointIndex));
		}
		_featureEditStates.isMidPointSelected = false; // back to the normal drawing mode.
		_featureEditStates.isVertexSelected = false;
	}
	
	/**
	 * 检查点列表的某个点是否被选中
	 * @param x
	 * @param y
	 * @param points1
	 * @param map
	 * @return
	 */
	private int _getSelectedIndex(double x, double y, List<Point> points1,
			MapView map) {

		if (points1 == null || points1.size() == 0)
			return -1;

		int index = -1;
		double distSQ_Small = Double.MAX_VALUE;
		for (int i = 0; i < points1.size(); i++) {
			Point p = map.toScreenPoint(points1.get(i));
			double diffx = p.getX() - x;
			double diffy = p.getY() - y;
			double distSQ = diffx * diffx + diffy * diffy;
			if (distSQ < distSQ_Small) {
				index = i;
				distSQ_Small = distSQ;
			}
		}

		if (distSQ_Small < (40 * 40)) {
			return index;
		}
		return -1;

	}
	
	/**
	 * 编辑状态封装
	 * @author xoozi
	 *
	 */
	private class EditingStates {
		List<Point> points = new ArrayList<Point>();
		boolean 	isMidPointSelected 	= false;
		boolean 	isVertexSelected 	= false;
		int 		insertPointIndex;

		public EditingStates(List<Point> aPoints, boolean aIsMidPointSelected,
				boolean aIsVertexSelected, int aInsertPointIndex) {
			points.addAll(aPoints);
			isMidPointSelected 		= aIsMidPointSelected;
			isVertexSelected 		= aIsVertexSelected;
			insertPointIndex	 	= aInsertPointIndex;
		}
	}
	
	/**
	 * 编辑策略接口
	 * @author xoozi
	 *
	 */
	private interface EditStrategy{
		public boolean onDragPointerMove(MotionEvent from, final MotionEvent to);
		public boolean onDragPointerUp(MotionEvent from, final MotionEvent to);
		public void onLongPress(MotionEvent point);
		public boolean onSingleTap(final MotionEvent e);
		
		public void 	onGoBack();
		public void 	onDeleteLastNode();
		public void 	onClear();
		public void 	onUnselect();
	}
	
	/**
	 * 空策略，用于没有选择任何功能时，这时长按会选择要素进行编辑
	 * @author xoozi
	 *
	 */
	private class EditStrategyNull implements EditStrategy{

		public boolean onDragPointerMove(MotionEvent from, MotionEvent to) {
			return false;
		}
		public boolean onDragPointerUp(MotionEvent from, MotionEvent to) {
			return false;
		}
		public void onLongPress(MotionEvent point) {
			Utils.amLog("long press search");
			_hideCallout();
			_searchNearestGraphic(point.getX(),point.getY());
		}
		public boolean onSingleTap(MotionEvent e) {
			_hideCallout();
			return false;
		}
		public void onGoBack() {
		}
		public void onDeleteLastNode() {
		}
		public void onClear() {
		}
		public void onUnselect(){
			
		}
	}
	
	
	
	/**
	 * 点编辑策略
	 * @author xoozi
	 *
	 */
	private class EditStrategyPoint implements EditStrategy{
		
		public boolean onDragPointerMove(MotionEvent from, MotionEvent to) {

			return false;
		}

		public boolean onDragPointerUp(MotionEvent from, MotionEvent to) {
			
			if(_featureEditStates.isShowMag){//如果是放大模式
				Point point = _mapView.toMapPoint(new Point(to.getX(), to.getY()));
				
				//清掉其他点
				_vertexPoints.clear();
				
				//加上点击位置的这个点
				_vertexPoints.add(point);
				
				
				//刷新编辑图层
				_refresh();
			}
			
			return false;
		}

		public void onLongPress(MotionEvent point) {

		}

		public boolean onSingleTap(MotionEvent e) {
			
			Point point = _mapView.toMapPoint(new Point(e.getX(), e.getY()));
			
			//清掉其他点
			_vertexPoints.clear();
			
			//加上点击位置的这个点
			_vertexPoints.add(point);
			
			//刷新编辑图层
			_refresh();
			
			return false;
		}

		public void onGoBack() {
			
			if(_editStatesList.size()>1){
				_editStatesList.remove(_editStatesList.size() - 1);
			}
			
			
			//清掉
			_vertexPoints.clear();
			
			//刷新编辑图层
			_refresh();
			
		}

		public void onDeleteLastNode() {
			
			//清掉
			_vertexPoints.clear();
			
			//刷新编辑图层
			_refresh();
			
		}

		public void onClear() {
			
			//清掉
			_vertexPoints.clear();
			
			//刷新编辑图层
			_refresh();
			
		}
		
		public void onUnselect(){
			
		}
		
	}
	
	/**
	 * 线编辑策略
	 * @author xoozi
	 *
	 */
	private class EditStrategyLinestring implements EditStrategy{

		public boolean onDragPointerMove(MotionEvent from, MotionEvent to) {
			
			
			if(_featureEditStates.freeDraw){
				
				Point point = _mapView.toMapPoint(new Point(to.getX(), to.getY()));
				_vertexPoints.add(point);
				_refresh();
				return true;
			}else{
				return false;
			}
			
		}

		public boolean onDragPointerUp(MotionEvent from, MotionEvent to) {
			
			if(_featureEditStates.freeDraw){
				
				return true;
			}else{
				if(_featureEditStates.isShowMag){//如果是拖动模式
					Point point = _mapView.toMapPoint(new Point(to.getX(), to.getY()));
					_movePoint(point);//移动选择点
					_refresh();
				}
				return false;
			}
			
			
		}

		public void onLongPress(MotionEvent point) {
			// TODO Auto-generated method stub
			
		}

		public boolean onSingleTap(MotionEvent e) {
			if(_featureEditStates.freeDraw){
				
				return true;
			}
			
			Point point = _mapView.toMapPoint(new Point(e.getX(), e.getY()));
			
			//如果单击时没有任何点被选中，就先检查用户的点击是否选择了某个点
			if(!_featureEditStates.isMidPointSelected && 
					!_featureEditStates.isVertexSelected){
				
				int indexMid = _getSelectedIndex(e.getX(),e.getY(),_midPoints,_mapView);
				if(-1!=indexMid){
					_featureEditStates.isMidPointSelected = true;
					_featureEditStates.selectPointIndex = indexMid;
				}else{
					int indexVertex = _getSelectedIndex(e.getX(),e.getY(),_vertexPoints,_mapView);
					
					if(-1!=indexVertex){
						_featureEditStates.isVertexSelected = true;
						_featureEditStates.selectPointIndex = indexVertex;
					}
				}
				
				
				//如果还是没选中，就增加一个顶点
				if(!_featureEditStates.isMidPointSelected && 
						!_featureEditStates.isVertexSelected){
					_vertexPoints.add(point);
					_editStatesList.add(new EditingStates(_vertexPoints, 
							_featureEditStates.isMidPointSelected,
							_featureEditStates.isVertexSelected, 
							_featureEditStates.selectPointIndex));
				}
				
			}else{//开始有点被选择，就直接Move它
				_movePoint(point);
			}
			
			_refresh();
			
			return false;
		}

		public void onGoBack() {
			
			if(_featureEditStates.freeDraw){
				
				return ;
			}
			
			if(_editStatesList.size()>1){
				_editStatesList.remove(_editStatesList.size() - 1);
				EditingStates state = _editStatesList.get(_editStatesList.size() - 1);
				_vertexPoints.clear();
				_vertexPoints.addAll(state.points);
				_featureEditStates.isMidPointSelected 	= state.isMidPointSelected;
				_featureEditStates.isVertexSelected 	= state.isVertexSelected;
				_featureEditStates.selectPointIndex 	= state.insertPointIndex;
				_refresh();
			}
		}

		public void onDeleteLastNode() {
			if(_featureEditStates.freeDraw){
				
				return ;
			}
			if(_featureEditStates.isVertexSelected && 
					_featureEditStates.selectPointIndex==_vertexPoints.size()-1){
				_featureEditStates.isVertexSelected = false;
				_featureEditStates.selectPointIndex = -1;
			}
			_vertexPoints.remove(_vertexPoints.size()-1);
			_refresh();
			
		}

		public void onClear() {
			
			_vertexPoints.clear();
			_featureEditStates.isVertexSelected 	= false;
			_featureEditStates.isMidPointSelected 	= false;
			_featureEditStates.selectPointIndex 	= -1;
			
			_refresh();
			
		}
		
		public void onUnselect(){
			if(_featureEditStates.freeDraw){
				
				return ;
			}
			_featureEditStates.isMidPointSelected 	= false;
			_featureEditStates.isVertexSelected 	= false;
			_featureEditStates.selectPointIndex	= -1;
			
			_refresh();
		}
		
	}
	
	/**
	 * 多边形编辑策略
	 * @author xoozi
	 *
	 */
	private class EditStrategyPolygon implements EditStrategy{

		public boolean onDragPointerMove(MotionEvent from, MotionEvent to) {
			
			if(_featureEditStates.freeDraw){
				
				Point point = _mapView.toMapPoint(new Point(to.getX(), to.getY()));
				_vertexPoints.add(point);
				_refresh();
				return true;
			}else{
				return false;
			}
		}

		public boolean onDragPointerUp(MotionEvent from, MotionEvent to) {
			
			if(_featureEditStates.freeDraw){
				
				return true;
			}else{
				if(_featureEditStates.isShowMag){//如果是拖动模式
					Point point = _mapView.toMapPoint(new Point(to.getX(), to.getY()));
					_movePoint(point);//移动选择点
					_refresh();
				}
				return false;
			}
		}

		public void onLongPress(MotionEvent point) {
			// TODO Auto-generated method stub
			
		}

		public boolean onSingleTap(MotionEvent e) {
			if(_featureEditStates.freeDraw){
				
				return true;
			}
			Point point = _mapView.toMapPoint(new Point(e.getX(), e.getY()));
			
			//如果单击时没有任何点被选中，就先检查用户的点击是否选择了某个点
			if(!_featureEditStates.isMidPointSelected && 
					!_featureEditStates.isVertexSelected){
				
				int indexMid = _getSelectedIndex(e.getX(),e.getY(),_midPoints,_mapView);
				if(-1!=indexMid){
					_featureEditStates.isMidPointSelected = true;
					_featureEditStates.selectPointIndex = indexMid;
				}else{
					int indexVertex = _getSelectedIndex(e.getX(),e.getY(),_vertexPoints,_mapView);
					
					if(-1!=indexVertex){
						_featureEditStates.isVertexSelected = true;
						_featureEditStates.selectPointIndex = indexVertex;
					}
				}
				
				
				//如果还是没选中，就增加一个顶点
				if(!_featureEditStates.isMidPointSelected && 
						!_featureEditStates.isVertexSelected){
					_vertexPoints.add(point);
					_editStatesList.add(new EditingStates(_vertexPoints, 
							_featureEditStates.isMidPointSelected,
							_featureEditStates.isVertexSelected, 
							_featureEditStates.selectPointIndex));
				}
				
			}else{//开始有点被选择，就直接Move它
				_movePoint(point);
			}
			
			_refresh();
			
			return false;
		}

		public void onGoBack() {
			if(_featureEditStates.freeDraw){
				
				return ;
			}
			
			if(_editStatesList.size()>1){
			
				_editStatesList.remove(_editStatesList.size() - 1);
				EditingStates state = _editStatesList.get(_editStatesList.size() - 1);
				_vertexPoints.clear();
				_vertexPoints.addAll(state.points);
				_featureEditStates.isMidPointSelected = state.isMidPointSelected;
				_featureEditStates.isVertexSelected = state.isVertexSelected;
				_featureEditStates.selectPointIndex = state.insertPointIndex;

				_refresh();
			}
		}

		public void onDeleteLastNode() {
			if(_featureEditStates.freeDraw){
				
				return ;
			}
			if(_featureEditStates.isVertexSelected && 
					_featureEditStates.selectPointIndex==_vertexPoints.size()-1){
				_featureEditStates.isVertexSelected = false;
				_featureEditStates.selectPointIndex = -1;
			}
			_vertexPoints.remove(_vertexPoints.size()-1);
			_refresh();
			
		}

		public void onClear() {
			_vertexPoints.clear();
			_featureEditStates.isVertexSelected = false;
			_featureEditStates.isMidPointSelected = false;
			_featureEditStates.selectPointIndex = -1;
			
			_refresh();
			
		}
		
		public void onUnselect(){
			if(_featureEditStates.freeDraw){
				
				return ;
			}
			_featureEditStates.isMidPointSelected 	= false;
			_featureEditStates.isVertexSelected 	 	= false;
			_featureEditStates.selectPointIndex		= -1;
			
			_refresh();
		}
		
	}
	
	
	/**
	 * 地图视图触屏事件监听
	 * @author xoozi
	 *
	 */
	class DrawToolTouchListener extends MapOnTouchListener{

		MapMagnifier _mag;
		Bitmap 		 _snapshot = null;
		
		
		public DrawToolTouchListener(Context context, MapView view) {
			super(context, view);
			_featureEditStates.isShowMag = false;
		}
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			
			//手指抬起时关掉放大镜
			if(event.getAction()==MotionEvent.ACTION_UP){
				if (null!=_mag&&_featureEditStates.isShowMag) {
					_mag.hide();
				}
			}
			
			
			//自由笔模式， 每次按下清除绘制
			if(MotionEvent.ACTION_DOWN==event.getAction()){
				if(_featureEditStates.freeDraw){
					_currentEditStrategy.onClear();
				}
			}
			
			
			
			return super.onTouch(v, event);
			/*
			if(FeatureEditor.MODE_BROWSE==_currentMode)
				return super.onTouch(v, event);
			else if(event.getPointerCount()<=1)
				return super.onTouch(v, event);
			else 
				return true;*/
		}
		
		@Override
		public boolean onDoubleTap(MotionEvent point) {
			
			
			if(_currentEditStrategy == _editStrategyNull){
				return super.onDoubleTap(point);
			}else{
				return true;
			}
		}

		@Override
		public void onLongPress(MotionEvent point) {
			if(_currentEditStrategy != _editStrategyNull){
				magnify(point);
				_featureEditStates.isShowMag = true;
			}
				
			_currentEditStrategy.onLongPress(point);
		}

		@Override
		public boolean onDragPointerMove(MotionEvent from, final MotionEvent to) {
			if (_featureEditStates.isShowMag) {
				magnify(to);
			}
			
			_currentEditStrategy.onDragPointerMove(from, to);
			
		
			if(_featureEditStates.isShowMag)
				return true;
			else{
				if(_currentEditStrategy.onDragPointerMove(from, to))
					return true;
				else
					return super.onDragPointerMove(from, to);
			}
			
		}

		@Override
		public boolean onDragPointerUp(MotionEvent from, final MotionEvent to) {
			
			_currentEditStrategy.onDragPointerUp(from, to);
			
			if (_featureEditStates.isShowMag) {
				if (_mag != null) {
					_mag.hide();
				}
				_mag.postInvalidate();
				_featureEditStates.isShowMag = false;
			}
			
			/*if(_currentEditStrategy == _editStrategyNull){
				return super.onDragPointerUp(from, to);
			}else{
				return true;
			}*/
			
			if(_currentEditStrategy.onDragPointerUp(from, to))
				return true;
			else
				return super.onDragPointerUp(from, to);
			
		}

		void magnify(MotionEvent to) {

			if(_mag==null){
				_mag = new MapMagnifier(_context, _mapView);
				_mapView.addView(_mag);
				_mag.prepareDrawingCacheAt(to.getX(), to.getY());
			}
			else{
				_mag.prepareDrawingCacheAt(to.getX(), to.getY());
			}


		}

		@Override
		public boolean onSingleTap(final MotionEvent e) {
			
			_currentEditStrategy.onSingleTap(e);

			if(_currentEditStrategy == _editStrategyNull){
				return super.onSingleTap(e);
			}else{
				return true;
			}
		}
		
	}
	
	/**
	 * 为免FeatureEditor看起来过于混乱，把状态信息封装一下
	 * @author xoozi
	 *
	 */
	private  class FeatureEditStates{
		boolean 			isMidPointSelected 		= false;		//某中点是否被选中
		boolean 			isVertexSelected 		= false;		//某顶点是否被选中
		boolean 		 	isShowMag 				= false;    	//是否放大模式
		boolean				isEditFeature 			= false;		//当前是在编辑现有要素还是在插入新要素
		boolean				freeDraw				= false;		//是否自由笔模式
		int 			 	selectPointIndex		= -1;			//选中点序号
		int					editedMaskId			= -1;			//编辑蒙板，用于标记正在编辑中的点的原始位置
		int					editedGraphicId			= -1;			//当前编辑图形ID
		String				editedFeatureCreator 	= null;			//当前编辑要素的原创者
	}
	
	
	public	interface	IPanelDrawToolAction{
		public	void	onCheckCanBeSaved(Layer layer, boolean canBeSaved);
		public	void	onCancel();
		public	void	startEditFeatrue(Pair<Integer,Integer> featureId_gId);
	}


}

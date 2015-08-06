package com.gnc.dcqtech.uicontroll;

import java.io.File;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.MapView;
import com.esri.core.geometry.Geometry;
import com.gnc.dcqtech.R;
import com.gnc.dcqtech.activities.ActivitySensorCamera;
import com.gnc.dcqtech.layer.Layer;
import com.gnc.dcqtech.layer.LayerClass;
import com.gnc.dcqtech.layer.LayerManager;
import com.gnc.dcqtech.poppanel.PopPhotoManage;
import com.gnc.dcqtech.project.Project;
import com.gnc.dcqtech.project.ProjectDataBase.FeatureColumns;
import com.gnc.dcqtech.uicontroll.PanelDrawTool.IPanelDrawToolAction;
import com.gnc.dcqtech.uicontroll.PanelLayers.onSelectLayerListener;
import com.xoozi.andromeda.uicontroller.SelfInflaterPanel;
import com.xoozi.andromeda.utils.Utils;

public class PanelObservating extends SelfInflaterPanel implements OnClickListener,OnItemSelectedListener,onSelectLayerListener, IPanelDrawToolAction{

	//private Intent					_pendingPhotoData;
	
	public	static final String		ACTION_DELETE_FEATURE = "com.gnc.dcqtech.uicontroll.PanelObservating.deleteFeature";
	
	private DeleteFeatureReceiver	_deleteFeatureReceiver = new DeleteFeatureReceiver();

	private	Spinner					_spinOca;
	private ObservatingClassAdapter	_oca;
	private PanelLayers				_layersPanel;
	private PanelAttributeEditer	_attrEditer;
	private PanelDrawTool			_drawTool;
	private TextView				_actionText;
	private IObservatingAction		_observatingAction;
	private LinearLayout 			_drawtoolLayout;
	private MapView					_mapView;
	private Button					_btnSave;
	private Button					_btnTakePhoto;
	private Button					_btnDisplayPhoto;
	private View					_btnPendingPhoto;
	private	View					_btnDelete;
	
	private FSMObservating			_fsmObservating;
	
	private boolean					_isShow = false;
	
	
	private Pair<Integer,Integer>	_asynArg = null;
	

	public PanelObservating(Context context, MapView mapView, LinearLayout baseLayout, LinearLayout drawtoolLayout,
			IObservatingAction observatingAction) {
		super(context, baseLayout);
		_observatingAction 	= observatingAction;
		_drawtoolLayout		= drawtoolLayout;
		_mapView			= mapView;
		_initWork();
		
	}
	
	@Override
	public void onClick(View v) {
		
		switch(v.getId()){
			case R.id.btn_save:
				_save();
				break;
				
			case R.id.btn_delete:
				_delete();
				break;
				
			case R.id.btn_close:
				close();
				break;
				
			case R.id.btn_take_photo:
				_tackPhoto();
				break;
				
			case R.id.btn_display_photo:
				_displayPhoto();
				break;
				
		}
		
	}
	
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		
		_fsmObservating.selectClass(position);
		
		if(null!=_asynArg){
			//使得调绘面板在指定的featrue上进入编辑模式
			_startDisplayFeatrue(_asynArg);
			_asynArg = null;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		
	}
	
	@Override
	public void onSelect(Layer layer) {
		_fsmObservating.selectLayer(layer);
	}
	
	///给DrawTool的回调接口
	@Override
	public void onCheckCanBeSaved(Layer layer, boolean canBeSaved) {
		
		
		if(null!=layer&&layer.withPhoto()){
			
			boolean isEditing = _fsmObservating._fsmEditingFeatureId_gId != null;
			if(isEditing){
				_btnSave.setEnabled(canBeSaved);
			}else{
				boolean	hasPendingPhotoData = null!=_fsmObservating._pendingPhotoData;
				_btnSave.setEnabled(canBeSaved&&hasPendingPhotoData);
			}
			
		}else{
			_btnSave.setEnabled(canBeSaved);
		}
	}
	
	@Override
	public void onCancel(){
		_fsmObservating.drawToolCancel();
	}
	
	
	@Override
	public void startEditFeatrue(Pair<Integer, Integer> featureId_gId) {
		_fsmObservating.startEditing(featureId_gId);
	}
	///给DrawTool的回调接口 END
	
	private void	_startDisplayFeatrue(Pair<Integer, Integer> featureId_gId){
		_fsmObservating.startDisplayFeatrue(featureId_gId);
	}
	

	
	
	/**
	 * 根据选择的featureid，跳转到对应的图层大类
	 * 将对应的graphic放大，居中显示
	 * 使指定的Feature变成编辑态
	 * @param featureId
	 */
	public	void	photoManageSelectFeature(int featureId){
		
		//根据featrueId查询到layerId
		Cursor	data	= _fsmObservating._fsmCurrentProject.queryFeatureById(featureId);
		if(data.moveToNext()){
			int		layerId	= data.getInt(data.getColumnIndexOrThrow(FeatureColumns.LAYER_ID));
			
			//根据layerId 获取layer，从layer中通过featrueId查询到graphicId, 从layer中查询到layerClassId
			Layer	layer = LayerManager.getLayerById(layerId);
			int layerClassId = layer.getClassId();
			
			
			//显示本调绘面板
			show(true);
			
			int gotoClass = layerClassId-1;
			
			
			//由于_spinOca.setSelection(layerClassId-1);是异步的
			//当在a layerclass选中的情况下 跳转到b layerclass
			//先调用_spinOca.setSelection(layerClassId-1); 后马上设置_startDisplayFeatrue
			//当_spinOca.setSelection(layerClassId-1);  引起的异步 onItemSelected被调用后，会清除
			//_startDisplayFeatrue设置的状态，所以这种情况下要把_startDisplayFeatrue推迟到 onItemSelected去调用
			if(_fsmObservating._fsmCurrentClass!=(gotoClass)){
				
				_asynArg = new Pair<Integer,Integer>(featureId, 0);
				
				//跳转到对应的layerClass
				_spinOca.setSelection(gotoClass);
			}else{
				
				//由于这时实际没调用选择class，当调绘面板本来就处于隐藏状态时，没有检查是否需要显示被选择的图层
				//需要加上这个补丁  这个方法里面两处破坏了状态机的完美性，但是目前没有太好的解决办法
				_observatingAction.onSelectLayerClass(gotoClass);
				
				//使得调绘面板在指定的featrue上进入编辑模式
				_startDisplayFeatrue(new Pair<Integer,Integer>(featureId, 0));
			}
		}
		
		data.close();
	}
	
	
	public	void	takePhotoReturn(Intent data){
		
		//分析一下拍照是否成功
		boolean	result = data.getBooleanExtra(ActivitySensorCamera.KEY_STATUS, false);
		
		if(!result){
			//失败， toast提醒用户
		}else{
			//把intent暂存起来，等待要素保存时一并处理
			_fsmObservating.whenPhotoReturn(data);
		}
	}
	
	
	public 	void 	jumpToLayer(String layerName){
		
		//_fsmObservating.whenInit(_fsmObservating._fsmCurrentProject);
		
		int	classCount = LayerManager.getLayerClassCount();
		
		OUT:
		for(int index=0; index<classCount; index++){
			LayerClass layerClass = LayerManager.getLayerClass(index);
			
			List<Layer> layers = layerClass.getLayers();
			
			for(Layer layer: layers){
				if(layerName.equals(layer.getName())){

					Utils.amLog("jumpToLayer index:"+index+", currentSel:"+_fsmObservating._fsmCurrentClass);
					
					//为了点击系统菜单做到隐藏/显示调绘面板到指定的类别，于是去掉这个跳转到不同类别的判断，
					//直接在下面调用_spinOca.setSelection(index);
					/*if(index != _fsmObservating._fsmCurrentClass){
						_spinOca.setSelection(index);
					}*/
					
					_fsmObservating.selectClass(index);
					_spinOca.setSelection(index);
					this.show(true);
					//_fsmObservating.selectLayer(layer);
					
					break OUT;
				}
			}
			if(index==(classCount-1)){
				this.show(true);
				_fsmObservating.selectClass(0);
				_spinOca.setSelection(0);
			}
				
		}
		
		
	}
	
	
	public	void	show(boolean show){
		if(show){
			_isShow = true;
			_baseLayout.setVisibility(View.VISIBLE);
		}else{
			_isShow = false;
			_baseLayout.setVisibility(View.INVISIBLE);
		}
	}
	
	public	boolean	isShow(){
		return _isShow;
	}
	
	public	void	setProject(Project project){
		_fsmObservating.whenInit(project);
	}
	
	
	private void	_initWork(){
		
		initPanel(R.layout.panel_observating);
		
		_actionText = (TextView) _baseLayout.findViewById(R.id.text_observating_action);
		
		_btnDisplayPhoto = (Button)_baseLayout.findViewById(R.id.btn_display_photo);
		_btnDisplayPhoto.setOnClickListener(this);
		
		_btnTakePhoto = (Button)_baseLayout.findViewById(R.id.btn_take_photo);
		_btnTakePhoto.setOnClickListener(this);
		_btnSave = (Button)_baseLayout.findViewById(R.id.btn_save);
		_btnSave.setOnClickListener(this);
		_baseLayout.findViewById(R.id.btn_close).setOnClickListener(this);
		
		
		_btnPendingPhoto = _baseLayout.findViewById(R.id.btn_pending_photo);
		_btnPendingPhoto.setOnClickListener(this);
		
		_btnDelete		 = _baseLayout.findViewById(R.id.btn_delete);
		_btnDelete.setOnClickListener(this);
		
		_spinOca	= (Spinner)_baseLayout.findViewById(R.id.spin_obervating_class);
		
		
		LinearLayout layersPanel = (LinearLayout) _baseLayout.findViewById(R.id.field_observating_layers);
		
		_layersPanel = new PanelLayers(_context,layersPanel,this);
		
		LinearLayout attrEditPanel = (LinearLayout) _baseLayout.findViewById(R.id.field_observating_layer_attr);
		
		_attrEditer  = new PanelAttributeEditer(_context,attrEditPanel);
		
		_drawTool	 = new PanelDrawTool(_context, _drawtoolLayout, this,_mapView);
		
		_fsmObservating = new FSMObservating();
		
		
		//注册接受删除要素广播的接收器
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(_context);
		lbm.registerReceiver(_deleteFeatureReceiver, new IntentFilter(ACTION_DELETE_FEATURE));
		
	
	}
	
	
	public void	close(){
		
		_fsmObservating.colseObservating();
	
	}
	
	private void	_save(){
		
		Layer layer = _drawTool.getCurrentLayer();
		
		Geometry geo = _drawTool.getGeometry();
		
		
		Pair<String,String> result = _attrEditer.getAttributeXmlForSave();
		String	attributeXml = result.first;
		String	cc		     = result.second;
	
		int	id;
		
		Pair<Integer, Integer>	editingFeatureId_gId = _fsmObservating._fsmEditingFeatureId_gId;
				
				
		if(null==editingFeatureId_gId){//新建
			id = (int)_fsmObservating._fsmCurrentProject.insertFeature(layer, geo, attributeXml, cc,_fsmObservating._pendingPhotoData);
		}else{
			//编辑
			id = editingFeatureId_gId.first;
			_fsmObservating._fsmCurrentProject.updateFeature(editingFeatureId_gId.first, geo, attributeXml, _fsmObservating._pendingPhotoData);
			
			//删除旧图形
			layer.removeFeaturePlus(editingFeatureId_gId.first);
		}
		
		layer.addFeature(_context, _fsmObservating._fsmCurrentProject,id, geo, cc);
		
		_fsmObservating.save();
	}
	
	
	private void	_delete(){
		Layer layer = _drawTool.getCurrentLayer();
		
		int featureId = _fsmObservating._fsmEditingFeatureId_gId.first;
		_fsmObservating._fsmCurrentProject.deleteFeature(featureId);
		//删除旧图形
		layer.removeFeaturePlus(_fsmObservating._fsmEditingFeatureId_gId.first);
		
		//如果是要素附带照片的图层，再同时删除照片
		if(layer.withPhoto()){
			_fsmObservating._fsmCurrentProject.deletePhoto(featureId);
		}
		
		_fsmObservating.save();
	}
	
	
	private Project _getProject(){
		return _fsmObservating._fsmCurrentProject;
	}
	
	
	private void	_tackPhoto(){
		_fsmObservating._cleanPendingPhoto();
		_observatingAction.onTakePhoto();
	}
	
	private void	_displayPhoto(){
		Utils.amLog("display photo");
		// 由于通过广播叫起popwindow会报token错误，先用回调来做这个事
		/*Intent intent = new Intent(PopPhotoManage.ACTION_DISPLAY_PHOTO);
		intent.putExtra(ActivitySensorCamera.KEY_FEATUREID, _fsmObservating._fsmEditingFeatureId_gId.first);//附加上FeatureID
		LocalBroadcastManager lbm  = LocalBroadcastManager.getInstance(_context);
		lbm.sendBroadcast(intent);*/
		
		_observatingAction.onDisplayPhoto(_fsmObservating._fsmEditingFeatureId_gId.first);
	}


	
	
	/**
	 * 调绘类别适配器
	 * @author xoozi
	 *
	 */
	private class ObservatingClassAdapter extends BaseAdapter{

		@Override
		public int getCount() {

			return LayerManager.getLayerClassCount();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			if (convertView == null){
				convertView = _layoutInflater.inflate(R.layout.spinner_item_observating_class, null);
			}
			
			TextView	observatingClass = (TextView)convertView.findViewById(R.id.text_class_name);
			observatingClass.setText(LayerManager.getLayerClass(position).getClassName());
			
			return convertView;
		}
		
	}


	/**
	 * 调绘面板接口，供顶层Activity实现
	 * @author xoozi
	 *
	 */
	public interface	IObservatingAction{
		public 	void	onSelectLayerClass(int classIndex);
		public  void	onClose();
		public	void	onTakePhoto();
		public 	void	onDisplayPhoto(int featrueId);
	}
	
	/**
	 * 删除要素的应用内部广播接收器
	 * @author xoozi
	 *
	 */
	private class	DeleteFeatureReceiver extends BroadcastReceiver{
		

		@Override
		public void onReceive(Context context, Intent intent) {
			Utils.amLog("receive broadcast delete feature");
			
			int	featureId = intent.getIntExtra(ActivitySensorCamera.KEY_FEATUREID, -1);
			
			Project project = _getProject();
			
			//根据featrueId查询到layerId
			Cursor	data	= project.queryFeatureById(featureId);
			if(data.moveToNext()){
				Utils.amLog("receive broadcast delete feature 2");
				int		layerId	= data.getInt(data.getColumnIndexOrThrow(FeatureColumns.LAYER_ID));
				
				//根据layerId 获取layer，从layer中通过featrueId查询到graphicId, 从layer中查询到layerClassId
				Layer	layer = LayerManager.getLayerById(layerId);
				//int graphicId = layer.getGraphicIdByFeatrueId(featureId);
				
				project.deleteFeature(featureId);
				project.deletePhoto(featureId);//照片的物理删除，统一放在这里
				
				//删除旧图形
				layer.removeFeaturePlus(featureId);
				
				_fsmObservating.save();//如果在编辑态，退出编辑态
			}
			Utils.amLog("receive broadcast delete feature 3");
			data.close();
			
		}
		
	}
	
	
	/**
	 * 调绘状态机
	 * @author xoozi
	 *
	 */
	class	FSMObservating{
		
		final	int				FSM_STATE_INIT				= 0;
		final	int				FSM_STATE_CLASS_OBSERVATING	= 1;
		final	int				FSM_STATE_LAYER_OBSERVATING = 2;
		final  	int  			FSM_STATE_FEATURE_EDITING	= 3;
		final	int				FSM_STATE_FEATURE_DISPLAY	= 4;
		final	int				FSM_STATE_CLOSED			= 5;
		
		private SparseArray<FSMStateEntry>	_stateList 		= new SparseArray<FSMStateEntry>();
		private int							_currentStateKey;
		private Intent						_pendingPhotoData;
		private Project						_fsmCurrentProject;	
		private int							_fsmCurrentClass;
		private Layer						_fsmCurrentLayer;
		private boolean						_fsmIsEditing;
		private boolean						_fsmPhotoSave;
		private Pair<Integer, Integer> 		_fsmEditingFeatureId_gId;
		
		FSMObservating(){
			_stateList.append(FSM_STATE_INIT, new FSMStateEntryInit());
			_stateList.append(FSM_STATE_CLASS_OBSERVATING, new FSMStateEntryClassObservating());
			_stateList.append(FSM_STATE_LAYER_OBSERVATING, new FSMStateEntryLayerObservating());
			_stateList.append(FSM_STATE_FEATURE_EDITING, new FSMStateEntryFeatureEditing());
			_stateList.append(FSM_STATE_FEATURE_DISPLAY, new FSMStateEntryFeatureDisplay());
			_stateList.append(FSM_STATE_CLOSED, new FSMStateEntryClosed());
			
			
			_currentStateKey = -1;
		}
		
		public	void	whenPhotoReturn(Intent photoData){
			_pendingPhotoData = photoData;
			_btnPendingPhoto.setVisibility(View.VISIBLE);
			_drawTool.queryCanSave();
		}
		
		/**
		 * 管理器外接口：当控件初始化时
		 */
		public	void	whenInit(Project project){
			_fsmCurrentProject = project;
			_setState(FSM_STATE_INIT);
		}
		
		/**
		 * 管理器外接口：选择调绘类
		 * @param classIndex
		 */
		public	void	selectClass(int classIndex){
			_fsmCurrentClass = classIndex;
			_setState(FSM_STATE_CLASS_OBSERVATING);
		}
		
		/**
		 * 管理器外接口：选择调绘图层
		 * @param layer
		 */
		public 	void	selectLayer(Layer layer){
			_fsmCurrentLayer = layer;
			_setState(FSM_STATE_LAYER_OBSERVATING);
		}
		
		/**
		 * 管理器外接口：开始要素编辑
		 * @param editingFeatureId_gId
		 */
		public	void	startEditing(Pair<Integer, Integer> 	editingFeatureId_gId){
			
			_fsmEditingFeatureId_gId	 = editingFeatureId_gId;
			_setState(FSM_STATE_FEATURE_EDITING);
		}
		
		
		public void		startDisplayFeatrue(Pair<Integer, Integer> 	editingFeatureId_gId){
			
			//为了循环进入编辑模式，需要把分段，清理前一状态后，再设置_fsmEditingFeatureId_gId，然后继续
			_setStatePre();
			_fsmEditingFeatureId_gId	 = editingFeatureId_gId;
			_setStatePost(FSM_STATE_FEATURE_DISPLAY);
		}
		
		/**
		 * 管理器外接口：关闭调绘
		 */
		public	void	colseObservating(){
			_setState(FSM_STATE_CLOSED);
		}
		
		/**
		 * 管理器外接口：drawtool放弃绘制
		 */
		public	void	drawToolCancel(){
			//回到当前图层所属类的状态
			selectClass(_fsmCurrentClass);
		}
		
		
		public	void	save(){
			_fsmPhotoSave = true;
			if(_fsmIsEditing)
				_saveEditingFeature();
			else
				_saveObservating();
		}
		/**
		 * 管理器私用方法：调绘保存
		 * @return
		 */
		private	void	_saveObservating(){
			
			selectClass(_fsmCurrentClass);
		}
		
		/**
		 * 管理器私用方法：要素编辑保存
		 */
		private	void	_saveEditingFeature(){
			selectClass(_fsmCurrentClass);
		}
		
		private	void	_cleanPendingPhoto(){
			
			if(null!=_pendingPhotoData){
				
				String	strPhoto 	= _pendingPhotoData.getStringExtra(ActivitySensorCamera.KEY_PHOTOFILE);
				File	photoFile 	= new File(strPhoto);
				photoFile.delete();
				_pendingPhotoData 	= null;
				_btnPendingPhoto.setVisibility(View.INVISIBLE);
				
				if(_fsmPhotoSave){
					Toast.makeText(_context, R.string.toast_save_pending_photo, Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(_context, R.string.toast_clean_pending_photo, Toast.LENGTH_SHORT).show();
				}
			}
			
			_fsmPhotoSave = false;
		}
		
		
		
		
		/**
		 * 管理器私用方法 设置状态
		 * @param state
		 */
		private	void	_setState(int state){
			FSMStateEntry	prevState = _stateList.get(_currentStateKey);
			
			if(null!=prevState)
				prevState.onExitState();
			
			_currentStateKey	= state;
			
			FSMStateEntry	currentState = _stateList.get(_currentStateKey);
			if(null!=currentState)
				currentState.onEnterState();
		}
		
		private void	_setStatePre(){
			FSMStateEntry	prevState = _stateList.get(_currentStateKey);
			
			if(null!=prevState){
				Utils.amLog("_setStatePre call onExitState");
				prevState.onExitState();
			}else{
				Utils.amLog("_setStatePre pass");
			}
				
		}
		
		private void	_setStatePost(int state){
			_currentStateKey	= state;
			
			FSMStateEntry	currentState = _stateList.get(_currentStateKey);
			if(null!=currentState)
				currentState.onEnterState();
		}
		
		
		/**
		 * 状态机状态实例
		 * @author xoozi
		 *
		 */
		 private abstract class 	FSMStateEntry{
			/**
			 * 当进入该状态时回调
			 */
			public	void	onEnterState(){}
			
			/**
			 * 当离开该状态时回调
			 */
			public	void	onExitState(){}
		}
		
		/**
		 * 状态机状态：初始状态
		 * @author xoozi
		 *
		 */
		private class FSMStateEntryInit extends FSMStateEntry{
		
			
			@Override
			public void onEnterState() {
				
				_oca	= new ObservatingClassAdapter();
				_spinOca.setAdapter(_oca);
				_spinOca.setOnItemSelectedListener(PanelObservating.this);
			}

			@Override
			public void onExitState() {
				
			}
		}
		
		/**
		 * 状态机状态：大类调绘
		 * @author xoozi
		 *
		 */
		private class FSMStateEntryClassObservating extends FSMStateEntry{
			
			
			
			@Override
			public void onEnterState() {
			
				//进入大类调绘制 后清除状态文字
				_actionText.setText("");
				
				//图层选择面板设定大类
				_layersPanel.loadLayerClass(_fsmCurrentClass);
				
				
				//回调顶层activity 将当前大类的图层设为可见 其他图层设为隐藏
				_observatingAction.onSelectLayerClass(_fsmCurrentClass);
				
				//设置绘图工具的图层大类，供其长按选择图层编辑用
				_drawTool.setLayerClass(LayerManager.getLayerClass(_fsmCurrentClass));
			}

			@Override
			public void onExitState() {
			
			}
		}
		
		
		/**
		 * 状态机状态：图层调绘
		 * @author xoozi
		 *
		 */
		private class FSMStateEntryLayerObservating extends FSMStateEntry{
			
		
			
			@Override
			public void onEnterState() {
				
				//进入图层调绘 更改状态文字
				_actionText.setText(_context.getResources().getString(R.string.label_observating_action)+_fsmCurrentLayer.getName());
				
				//加载属性编辑模板
				_attrEditer.loadData(_fsmCurrentLayer, null);
				
				//绘图工具，指定图层
				_drawTool.setLayer(_fsmCurrentLayer);
				
				//根据当前图层的withphoto属性来决定是否显示拍照按钮
				if(_fsmCurrentLayer.withPhoto()){
					_btnTakePhoto.setVisibility(View.VISIBLE);
				}else{
					_btnTakePhoto.setVisibility(View.GONE);
				}
				
			}

			@Override
			public void onExitState() {
			
				
				//清除属性编辑器
				_attrEditer.clean();
				
				//放弃绘图
				_drawTool.callCancel();
				
				//清除缓存的照片
				_cleanPendingPhoto();
				
				//拍照按钮隐藏
				_btnTakePhoto.setVisibility(View.GONE);	
			}
		}
		
		/**
		 * 状态机状态：要素编辑
		 * @author xoozi
		 *
		 */
		private class FSMStateEntryFeatureEditing extends FSMStateEntry{
			
		
			
			@Override
			public void onEnterState() {
				
				//根据选择编辑的要素id来加载要素数据
				Cursor	data	= _fsmCurrentProject.queryFeatureById(_fsmEditingFeatureId_gId.first);
				
				if(data.moveToNext()){
					String	geoData = data.getString(data.getColumnIndexOrThrow(FeatureColumns.GEO_DATA));
					String	attrData= data.getString(data.getColumnIndexOrThrow(FeatureColumns.ATTR_DATA));
					int		layerId	= data.getInt(data.getColumnIndexOrThrow(FeatureColumns.LAYER_ID));
					
					//通过要素数据中的图层id查询到要素所属图层
					_fsmCurrentLayer = LayerManager.getLayerById(layerId);
					
					//更改状态文字
					_actionText.setText(_context.getResources().getString(R.string.label_editing_action)+_fsmCurrentLayer.getName());
					
					//属性编辑器，加载属性模板和要素属性数据
					_attrEditer.loadData(_fsmCurrentLayer, attrData);
					
					
					//使删除按钮可见
					_btnDelete.setVisibility(View.VISIBLE);
					
					//如果是带照片的图层，使得浏览照片按钮可见
					if(_fsmCurrentLayer.withPhoto()){
						_btnDisplayPhoto.setVisibility(View.VISIBLE);
					}
					
					data.close();
			
					//绘图工具，指定图层 和编辑的几何数据
					_drawTool.setEditLayer(_fsmCurrentLayer, geoData);
				}
				
				
				//编辑标志
				_fsmIsEditing 				 = true; 
			}

			@Override
			public void onExitState() {
				
				//如果是带照片的图层，使得浏览照片按钮不可见
				if(_fsmCurrentLayer.withPhoto()){
					_btnDisplayPhoto.setVisibility(View.GONE);
				}
				
				//使删除按钮不可见
				_btnDelete.setVisibility(View.INVISIBLE);
				
				// 离开要素编辑 编辑标志 取消
				_fsmIsEditing 				= false;
				
				//缓存的 被编辑的要素id和图形id 清除
				_fsmEditingFeatureId_gId	= null; 
				
				//属性编辑器清除
				_attrEditer.clean();
				
				//绘图工具放弃绘制
				_drawTool.callCancel();
				
				//清除缓存图片
				_cleanPendingPhoto();
			}
		}
		
		
		/**
		 * 状态机状态：要素展示
		 * @author xoozi
		 *
		 */
		private class FSMStateEntryFeatureDisplay extends FSMStateEntry{
			
		
			
			@Override
			public void onEnterState() {
				
				//根据选择编辑的要素id来加载要素数据
				Cursor	data	= _fsmCurrentProject.queryFeatureById(_fsmEditingFeatureId_gId.first);
				
				if(data.moveToNext()){
					String	geoData = data.getString(data.getColumnIndexOrThrow(FeatureColumns.GEO_DATA));
					String	attrData= data.getString(data.getColumnIndexOrThrow(FeatureColumns.ATTR_DATA));
					int		layerId	= data.getInt(data.getColumnIndexOrThrow(FeatureColumns.LAYER_ID));
					
					//通过要素数据中的图层id查询到要素所属图层
					_fsmCurrentLayer = LayerManager.getLayerById(layerId);
					
					//更改状态文字
					_actionText.setText(_context.getResources().getString(R.string.label_editing_action)+_fsmCurrentLayer.getName());
					
					//属性编辑器，加载属性模板和要素属性数据
					_attrEditer.loadData(_fsmCurrentLayer, attrData);
					
					
					//使删除按钮可见
					_btnDelete.setVisibility(View.VISIBLE);
					
					//如果是带照片的图层，使得浏览照片按钮可见
					if(_fsmCurrentLayer.withPhoto()){
						_btnDisplayPhoto.setVisibility(View.VISIBLE);
					}
					
					data.close();
			
					//绘图工具，指定图层 和编辑的几何数据
					_drawTool.setFeatureDisplay(_fsmCurrentLayer, geoData);
				}
				
				
				//编辑标志
				_fsmIsEditing 				 = true; 
			}

			@Override
			public void onExitState() {
				
				//曾经用来跟踪photoManageSelectFeature 由于异步事件造成的莫名调用
				/*StackTraceElement dummy[] = Thread.currentThread().getStackTrace();
				 
				for(StackTraceElement wtf:dummy){
					Log.w("stackTrace",wtf.toString());
				}*/
				
				
				//如果是带照片的图层，使得浏览照片按钮不可见
				if(_fsmCurrentLayer.withPhoto()){
					_btnDisplayPhoto.setVisibility(View.GONE);
				}
				
				//使删除按钮不可见
				_btnDelete.setVisibility(View.INVISIBLE);
				
				// 离开要素编辑 编辑标志 取消
				_fsmIsEditing 				= false;
				
				//缓存的 被编辑的要素id和图形id 清除
				_fsmEditingFeatureId_gId	= null; 
				
				//属性编辑器清除
				_attrEditer.clean();
				
				//绘图工具放弃绘制
				_drawTool.callCancel();
				
				//清除缓存图片
				_cleanPendingPhoto();
			}
		}
		
		private class	FSMStateEntryClosed extends FSMStateEntry{

			@Override
			public void onEnterState() {
				//进入关闭状态
				
				//绘图工具放弃绘制
				_drawTool.callCancel();
				
				//关闭自身显示
				show(false);
				
				//通知顶层acitivity，调绘面板已经关闭，修改图层显示
				_observatingAction.onClose();
			}

			@Override
			public void onExitState() {
				
			}
			
		}
		
		
	}
	
	
	

	
}

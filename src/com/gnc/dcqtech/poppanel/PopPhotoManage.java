package com.gnc.dcqtech.poppanel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import com.gnc.dcqtech.R;
import com.gnc.dcqtech.activities.ActivityLogin;
import com.gnc.dcqtech.activities.ActivityMain;
import com.gnc.dcqtech.activities.ActivitySensorCamera;
import com.gnc.dcqtech.project.Project;
import com.gnc.dcqtech.project.ProjectDataBase.PhotoColumns;
import com.gnc.dcqtech.uicontroll.PanelObservating;
import com.xoozi.andromeda.uicontroller.PopPanelBase;
import com.xoozi.andromeda.utils.Utils;

public class PopPhotoManage extends PopPanelBase implements OnClickListener,OnItemClickListener,OnTouchListener {
	
	public static final String ACTION_DISPLAY_PHOTO = "com.gnc.dcqtech.poppanel.PopPhotoManage.displayPhoto";
	public static final String ACTION_ADD_PHOTO = "com.gnc.dcqtech.poppanel.PopPhotoManage.addPhoto";
	public static final String ACTION_DELETE_PHOTO = "com.gnc.dcqtech.poppanel.PopPhotoManage.deletePhoto";
	
	private static final int NONE = 0;  
	private static final int DRAG = 1;  
	private static final int ZOOM = 2;
	
	private IPopPanelAction				_popPanelAction;//activity给弹出窗口的回调
	
	private AddPhotoReceiver	  _addPhotoReceiver = new AddPhotoReceiver();
	private DisplayPhotoReceiver  _displayReceiver = new DisplayPhotoReceiver();
	private DeletePhotoReceiver	  _deletePhotoReceiver = new DeletePhotoReceiver();
	
	private SparseArray<PhotoCursorData>	_photoDataMap ;
	
	private PhotoThumbAdapter	  _photoAdapter = new PhotoThumbAdapter();
	
	private Project		_project;
	private File		_photoDir;
	private File		_thumbDir;
	private	ListView	_listPhotoThumb;
	
	
	
	private ImageView 	_imgviewDisplay;
	private Drawable 	_drawableDisplay;
	private Matrix 		_matrix;
	private Matrix 		_savedMatrix;
	private int 		_mode;  
	private PointF 		_start;  
    private PointF 		_mid;  
    private float 		_oldDist;  
    private boolean		_loaded=false;
	
	private SimpleDateFormat _sdf 	= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private int			_selectedPos = -1;
	

	private TextView	_textLon;
	private	TextView	_textLat;
	private TextView	_textAlt;
	private TextView	_textSatelliteCount;
	private TextView	_textPhotoDate;
	private TextView	_textBearing;
	private TextView	_textPitch;
	private TextView	_textRoll;
	private	TextView	_textDistance;
	private TextView	_textClassCode;
	private TextView	_textEnvDes;
	

	public PopPhotoManage(Context context, View rootPanel,IPopPanelAction popPanelAction) {
		super(context, rootPanel, PopPanelBase.PopMode.AT_LOCATION,true);
		_popPanelAction = popPanelAction;
		_initWork();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3) {
		_loadPhoto4Display(pos);
		_formatPhotoAttribute(pos);
		_selectedPos = pos;
		_photoAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View view) {
	
		switch(view.getId()){
		case R.id.btn_show_feature:
			_displayFeature();
			break;
			
		case R.id.btn_delete_photo:
			//没选中照片的情况下，忽略
			if(_selectedPos>=0){
				_confirmDelete();
			}
			break;
			
		}
	}
	
	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		
		Utils.amLog("size:"+event.getSize());
		
		if(!_loaded)
			return false;
	    if(event.getActionMasked()==MotionEvent.ACTION_POINTER_UP)
	    	Utils.amLog("多点操作");
	    switch(event.getActionMasked()){
	    
	    case MotionEvent.ACTION_DOWN:
	    	  _matrix.set(_imgviewDisplay.getImageMatrix());
	    	  _savedMatrix.set(_matrix);
	    	  _start.set(event.getX(),event.getY());
	    	  Utils.amLog("触摸了...");
	    	  _mode=DRAG;
	          break;
	          
	    case MotionEvent.ACTION_POINTER_DOWN:  //多点触控
	    	 _oldDist=this._spacing(event);
	        if(_oldDist>10f){
	        	Utils.amLog("oldDist"+_oldDist);
	        	_savedMatrix.set(_matrix);
	        	_midPoint(_mid,event);
	        	_mode=ZOOM;
	        }
	        break;
	    case MotionEvent.ACTION_POINTER_UP:
	    	_mode=NONE;
	        break;
	    case MotionEvent.ACTION_MOVE:
	    	if(_mode==DRAG){         //此实现图片的拖动功能...
	    		_matrix.set(_savedMatrix);
	    	    _matrix.postTranslate(event.getX()-_start.x, event.getY()-_start.y);
	    	}
	    	    else if(_mode==ZOOM){// 此实现图片的缩放功能...
	    	 float newDist=_spacing(event);
	    	 if(newDist>10){
	    		 _matrix.set(_savedMatrix);
	    		 float scale=newDist/_oldDist;
	    		 _matrix.postScale(scale, scale, _mid.x, _mid.y);	    		 
	    	 }
	    	    }
	    	break;
	    }
	    _imgviewDisplay.setImageMatrix(_matrix);
		return false;
	}
	
	protected PopupWindow	_initPopView(){
		
		PopupWindow	result = null;
		  
        result = new PopupWindow(_basePanel,ViewGroup.LayoutParams.MATCH_PARENT ,  
                ViewGroup.LayoutParams.MATCH_PARENT, true);  
        result.setFocusable(true);  
        result.setOutsideTouchable(true);   
        
        //之所以这样做，是因为对应PopWindow，setOutsideTouchable这些设置不管用
        //只要设置了backgroundDrawable，就支持域外点击
        //没有设置backgroundDrawable，就是模态的
        //可能是官方的bug，先这样用着吧
        if(_alowOutTouch){
        	result.setBackgroundDrawable(_context.getResources().getDrawable(R.drawable.shape_pop_panel));
        }
        
        result.setOnDismissListener(this);
		return result;
	}
	
	@Override
	protected void _initWork() {
		_basePanel = _layoutInflater.inflate(R.layout.pop_photo_manage, null);
		
		View	buttonDisplayFeature = _basePanel.findViewById(R.id.btn_show_feature);
		buttonDisplayFeature.setOnClickListener(this);
		
		View	buttonDeletePhoto = _basePanel.findViewById(R.id.btn_delete_photo);
		buttonDeletePhoto.setOnClickListener(this);
		
		_listPhotoThumb = (ListView) _basePanel.findViewById(R.id.listview_photo_thumb);
		_listPhotoThumb.setAdapter(_photoAdapter);
		_listPhotoThumb.setOnItemClickListener(this);
		
		_imgviewDisplay = (ImageView) _basePanel.findViewById(R.id.img_display);
		_imgviewDisplay.setOnTouchListener(this);
		
		_textLon = (TextView) _basePanel.findViewById(R.id.text_photo_lon);
		_textLat = (TextView) _basePanel.findViewById(R.id.text_photo_lat);
		_textAlt = (TextView) _basePanel.findViewById(R.id.text_photo_altitude);
		_textSatelliteCount = (TextView) _basePanel.findViewById(R.id.text_photo_satellite_count);
		_textPhotoDate = (TextView) _basePanel.findViewById(R.id.text_photo_date);
		_textBearing = (TextView) _basePanel.findViewById(R.id.text_photo_bearing);
		_textPitch = (TextView) _basePanel.findViewById(R.id.text_photo_pitch);
		_textRoll = (TextView) _basePanel.findViewById(R.id.text_photo_roll);
		_textDistance = (TextView) _basePanel.findViewById(R.id.text_photo_distance);
		_textClassCode = (TextView) _basePanel.findViewById(R.id.text_photo_class_code);
		_textEnvDes = (TextView) _basePanel.findViewById(R.id.text_photo_env_des);
		
		
		//注册添加照片和展示照片的广播接收器
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(_context);
		lbm.registerReceiver(_addPhotoReceiver, new IntentFilter(ACTION_ADD_PHOTO));
		lbm.registerReceiver(_displayReceiver, new IntentFilter(ACTION_DISPLAY_PHOTO));
		lbm.registerReceiver(_deletePhotoReceiver, new IntentFilter(ACTION_DELETE_PHOTO));
		
	}
	
	public	void	setProject(Project project){
		_project = project;
		_thumbDir = _project.getThumbDir();
		_photoDir = _project.getPhotoDir();
		_initPhotoList();
	}
	
	
	public void	show(){
		_show();
	}
	
	public	void	displayPhoto(int featureId){
		
		int position = _photoDataMap.indexOfKey(featureId);
		
		if(position>=0){
			
			_show();
			
			_displayPhoto(position);
		}
	}
	
	
	private void	_initPhotoList(){
		
		if(null!=_photoDataMap){
			
			_photoDataMap.clear();
			_photoDataMap = null;
		}
		
		_photoDataMap = new SparseArray<PhotoCursorData>();
		
		Cursor photoData = _project.queryPhoto();
		
		if(null!=photoData){
			
			while(photoData.moveToNext()){
				PhotoCursorData data = new PhotoCursorData();
				
				data.featureId = photoData.getInt(photoData.getColumnIndexOrThrow(PhotoColumns.FEATURE_ID));
				
				data.photoFile = photoData.getString(photoData.getColumnIndexOrThrow(PhotoColumns.PHOTO_NAME));
				data.classCode = photoData.getString(photoData.getColumnIndexOrThrow(PhotoColumns.CLASS_CODE));
				data.envDes = photoData.getString(photoData.getColumnIndexOrThrow(PhotoColumns.ENV_DES));
				
				data.satelliteCount = photoData.getInt(photoData.getColumnIndexOrThrow(PhotoColumns.SCOUNT));
				
				data.bearing = photoData.getDouble(photoData.getColumnIndexOrThrow(PhotoColumns.BEARING));
				data.pitch = photoData.getDouble(photoData.getColumnIndexOrThrow(PhotoColumns.PITCH));
				data.roll = photoData.getDouble(photoData.getColumnIndexOrThrow(PhotoColumns.ROLL));
				
				data.lon = photoData.getDouble(photoData.getColumnIndexOrThrow(PhotoColumns.LON));
				data.lat = photoData.getDouble(photoData.getColumnIndexOrThrow(PhotoColumns.LAT));
				data.alt = photoData.getDouble(photoData.getColumnIndexOrThrow(PhotoColumns.ALT));
				
				data.distance = photoData.getDouble(photoData.getColumnIndexOrThrow(PhotoColumns.DISTANCE));
				
				data.date = photoData.getLong(photoData.getColumnIndexOrThrow(PhotoColumns.CREATE_TIME));
				
				_photoDataMap.append(data.featureId, data);
				
			}
				
			photoData.close();
		}
		
		
	}
	
	/**
	 * 关闭照片管理面板，使指定的Feature变成编辑态
	 */
	private void	_displayFeature(){
		if(0<=_selectedPos){
			
			_popMenu.dismiss();
			_popPanelAction.onPhotoSelectFeature(_photoDataMap.valueAt(_selectedPos).featureId);
		}
	}
	
	private void	_displayPhoto(int position){
		_loadPhoto4Display(position);
		_formatPhotoAttribute(position);
		_selectedPos = position;
		_photoAdapter.notifyDataSetChanged();
		_listPhotoThumb.setSelection(position);
	}
	
	private void	_loadPhoto4Display(int position){
		
		_cleanDisplay();
		
		PhotoCursorData data = _photoDataMap.valueAt(position);
		
		File photoFile = new File(_photoDir,data.photoFile);
		
		
		if(photoFile.exists()){
			Utils.amLog("photo exist:"+photoFile.getAbsolutePath());
			_drawableDisplay = Drawable.createFromPath(photoFile.getAbsolutePath());
			
			if(null!=_drawableDisplay){
				_loaded = true;
				_imgviewDisplay.setImageDrawable(_drawableDisplay);
				_adjustImageDisplay();
			}else{
				_loaded = false;
			}
			
		}else{
			Utils.amLog("photo not exist");
			_loaded = false;
		}
	}
	
	private void _adjustImageDisplay(){
		
		//如果照片数据读出
		if(null!=_drawableDisplay){
			
			
			int drawableHeight 	= _drawableDisplay.getIntrinsicHeight();
			int drawableWidth	= _drawableDisplay.getIntrinsicWidth();
			
			int	imageViewHeight	= _imgviewDisplay.getHeight();
			int	imageViewWidth	= _imgviewDisplay.getWidth();
			
			float scaleFitHeight = ((float)imageViewHeight)/((float)drawableHeight);
			
			int deltaX = (imageViewWidth-drawableWidth)/2;
			int deltaY = (imageViewHeight-drawableHeight)/2;
			
			Utils.amLog("drawableHeight:"+drawableHeight+",drawableWidth:"+drawableWidth+
					",imageViewHeight:"+imageViewHeight+",imageViewWidth:"+imageViewWidth+
					",deltaX:"+deltaX+", deltaY:"+deltaY);
			
			Matrix matrix = new Matrix();
		    matrix.postTranslate(deltaX, deltaY);
		    matrix.postScale(scaleFitHeight, scaleFitHeight, imageViewWidth/2,imageViewHeight/2);
		    _imgviewDisplay.setImageMatrix(matrix);
		}
	}
	
	/**
	 * 清除显示
	 */
	private void _cleanDisplay(){
		
		_matrix			= new Matrix();
		_savedMatrix	= new Matrix();
		_mode			= NONE;  
		_start			= new PointF();  
	    _mid			= new PointF();  
	    _oldDist		= 1f; 
		
		if(null!=_drawableDisplay){
			_drawableDisplay = null;
			_imgviewDisplay.setImageDrawable(null);
		}
	}
	
	
	private	void	_formatPhotoAttribute(int position){
		PhotoCursorData data = _photoDataMap.valueAt(position);
		
		Date	photoDate = new Date();
		photoDate.setTime(data.date);
		String dateString = _sdf.format(photoDate);
		
		_textLon.setText(String.valueOf(data.lon));
		_textLat.setText(String.valueOf(data.lat));
		_textAlt.setText(String.valueOf(data.alt));
		_textSatelliteCount.setText(String.valueOf(data.satelliteCount));
		
		_textPhotoDate.setText(String.valueOf(dateString));
		
		_textBearing.setText(String.valueOf(data.bearing));
		_textPitch.setText(String.valueOf(data.pitch));
		_textRoll.setText(String.valueOf(data.roll));
		_textDistance.setText(String.valueOf(data.distance));
		
		_textClassCode.setText(String.valueOf(data.classCode));
		_textEnvDes.setText(String.valueOf(data.envDes));
	}

	
	private float _spacing(MotionEvent event) {  
        float x = event.getX(0) - event.getX(1);  
        float y = event.getY(0) - event.getY(1);  
        return (float) Math.sqrt(x * x + y * y);
     
	}  

 
	private void _midPoint(PointF point, MotionEvent event) {  
        float x = event.getX(0) + event.getX(1);  
        float y = event.getY(0) + event.getY(1);  
        point.set(x / 2, y / 2);  
    }  
	
	/**
	 * 被动地删除照片表项（由删除带照片的要素后，通知照片管理界面删除）
	 * 照片的物理删除在project里
	 * @param position
	 */
	private void	_deletePhotoPassively(int position){
		//如果删除的正好是选择的照片，就做下处理
		if(_selectedPos==position){
			_selectedPos = -1;
			_imgviewDisplay.setImageDrawable(null);
			_drawableDisplay = null;
		}
		
		//删除数据集中的对应项，通知列表更新
		_photoDataMap.removeAt(position);
		_photoAdapter.notifyDataSetChanged();
	}
	
	/**
	 * 主动地删除照片，由照片管理界面发起，删除照片后通知调绘面板删除要素
	 */
	private void	_deletePhotoActively(){
		//删数据集前，先把featureId取出来
		int	featureId = _photoDataMap.keyAt(_selectedPos);
		
		//先删除照片表项
		_deletePhotoPassively(_selectedPos);
		
		
		//删除完图片后，用本地广播通知调绘面板，删除要素
		Utils.amLog("send delete feature broadcast");
		Intent intent = new Intent(PanelObservating.ACTION_DELETE_FEATURE);
		intent.putExtra(ActivitySensorCamera.KEY_FEATUREID, featureId);//附加上FeatureID
		LocalBroadcastManager lbm  = LocalBroadcastManager.getInstance(_context);
		lbm.sendBroadcast(intent);
	}
	
	/**
	 * 确认删除照片
	 * @return
	 */
	private boolean  _confirmDelete(){
		
		Resources res = _context.getResources();
		
		new AlertDialog.Builder(_context)  
		  
		.setTitle(res.getString(R.string.label_delete_photo_title))  
  
		.setMessage("")  
		
		.setNegativeButton(res.getString(R.string.btn_cancel),  
		        new DialogInterface.OnClickListener() {  
  		        public void onClick(DialogInterface dialog, int which) {  
  		        	
  		        	Utils.amLog("---------cancel");
  		        	
		            }  
		        })  
  		.setPositiveButton(res.getString(R.string.btn_delete),  
		        new DialogInterface.OnClickListener() {  
  
		            public void onClick(DialogInterface dialog, int whichButton) {  
  
		                Utils.amLog("---------delete");
		                
		                _deletePhotoActively();
		            }  
  
		        }).show();  
		
		return true;
	}
	
	
	/**
	 * 添加图片的应用内部广播接收器
	 * @author xoozi
	 *
	 */
	private class AddPhotoReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			
			Utils.amLog("receive broadcast add photo");
			
			if(null==_photoDataMap)
				return;
			
			PhotoCursorData photoData = new PhotoCursorData();
			
			photoData.featureId = intent.getIntExtra(ActivitySensorCamera.KEY_FEATUREID, -1);
			photoData.photoFile	= intent.getStringExtra(ActivitySensorCamera.KEY_PHOTOFILE);
			photoData.classCode	= intent.getStringExtra(ActivitySensorCamera.KEY_CC);
			photoData.envDes	= intent.getStringExtra(ActivitySensorCamera.KEY_ENV_DES);
			photoData.satelliteCount = intent.getIntExtra(ActivitySensorCamera.KEY_COUNT, 0);
			photoData.bearing	= intent.getFloatExtra(ActivitySensorCamera.KEY_BEARING, 0.0f);
			photoData.pitch		= intent.getFloatExtra(ActivitySensorCamera.KEY_PITCH, 0.0f);
			photoData.roll		= intent.getFloatExtra(ActivitySensorCamera.KEY_ROLL, 0.0f);
			photoData.lon		= intent.getDoubleExtra(ActivitySensorCamera.KEY_LON, 0.0);
			photoData.lat		= intent.getDoubleExtra(ActivitySensorCamera.KEY_LAT, 0.0);
			photoData.alt		= intent.getDoubleExtra(ActivitySensorCamera.KEY_ALT, 0.0);
			photoData.distance	= intent.getDoubleExtra(ActivitySensorCamera.KEY_DISTANCE, 0.0);
			Date	date		= (Date) intent.getSerializableExtra(ActivitySensorCamera.KEY_DATE);
			photoData.date 		= date.getTime();
			
			
			_photoDataMap.append(photoData.featureId, photoData);
			
			_photoAdapter.notifyDataSetChanged();
		}
		
	}
	
	/**
	 * 删除照片的应用内部广播接收器
	 * @author xoozi
	 *
	 */
	private class	DeletePhotoReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			Utils.amLog("receive broadcast delete photo");
			
			int	featureId = intent.getIntExtra(ActivitySensorCamera.KEY_FEATUREID, -1);
			
		
			int index = _photoDataMap.indexOfKey(featureId);
			
			if(index>=0){
				
				_deletePhotoPassively(index);
			}
		}
		
	}
	
	/**
	 * 展示图片的应用内部广播接收器
	 * @author xoozi
	 *
	 */
	private class DisplayPhotoReceiver	extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			
			Utils.amLog("receive broadcast display photo");
			
			int featureId = intent.getIntExtra(ActivitySensorCamera.KEY_FEATUREID, -1);
			
			int position = _photoDataMap.indexOfKey(featureId);
			
			if(position>=0){
				
				//show();
				
				//_displayPhoto(position);
			}
		}
		
	}
	
	/**
	 * 照片缩略图列表适配器
	 * @author xoozi
	 *
	 */
	private class PhotoThumbAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			if(null==_photoDataMap)
				return 0;
			else
				return _photoDataMap.size();
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
				convertView = _layoutInflater.inflate(R.layout.list_item_photo_thumb, null);
			}
			
			PhotoCursorData data = _photoDataMap.valueAt(position);
			
			TextView photoName = (TextView)convertView.findViewById(R.id.text_photo_name);
			photoName.setText(data.photoFile);
			
			ImageView photoThumb = (ImageView)convertView.findViewById(R.id.img_photo_thumb);
			
			File imgFile = new File(_thumbDir, data.photoFile);
			
			
			if(imgFile.exists()){
				photoThumb.setImageDrawable(Drawable.createFromPath(imgFile.getAbsolutePath()));
			}else{
				photoThumb.setImageDrawable(_context.getResources().getDrawable(android.R.drawable.ic_menu_gallery));
			}
			
			if(_selectedPos == position){
				convertView.setBackgroundColor(Color.argb(0x80, 0xFF, 0xFF, 0xFF)); 
			}else{
				convertView.setBackgroundColor(Color.TRANSPARENT);  
			}
			
			return convertView;
		}
		
	}
	
	/**
	 * 照片数据封装
	 * @author xoozi
	 *
	 */
	private class PhotoCursorData{
		String	photoFile;
		String	classCode;
		String	envDes;
		int		satelliteCount;
		int		featureId;
		double	bearing;
		double	pitch;
		double	roll;
		double	lon;
		double	lat;
		double	alt;
		double	distance;
		long	date;
	}

	
	

}

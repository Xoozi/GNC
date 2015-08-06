package com.gnc.dcqtech.layer;


import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.Symbol;
import com.esri.core.symbol.TextSymbol;
import com.gnc.dcqtech.R;
import com.gnc.dcqtech.project.Project;
import com.gnc.dcqtech.project.ProjectDataBase.FeatureColumns;
import com.gnc.dcqtech.project.ProjectDataBase.LayersColumns;
import com.gnc.dcqtech.uicontroll.AttributeListItem;
import com.gnc.dcqtech.utils.XmlOperator;
import com.xoozi.andromeda.utils.Utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

public class Layer {
	public static final String	TYPE_VALUE_POINT 			= "POINT";		//类型值 点
	public static final String	TYPE_VALUE_LINESTRING		= "LINESTRING"; //类型值 线
	public static final String	TYPE_VALUE_POLYGON			= "POLYGON";	//类型值 面
	
	private static Drawable	ORIENTATION_IMG;
	
	private static float LABEL_SIZE = 15;
	

	protected String	_type;		//类型
	protected String	_symbol;	//记号风格
	protected String	_annotation;//标记格式
	protected String	_name;		//图层名
	protected String	_displayName;//图层显示名
	
	protected int		_layerId;	//图层id
	protected int		_classId;	//所属大类Id
	protected int		_size;		//尺寸，对于点类图形是半径，对于线状是线宽
	
	protected int		_alpha;		//alpha和三颜色分量
	protected int		_red;
	protected int		_green;		
	protected int		_blue;
	protected boolean	_withPhoto;
	
	
	private List<Field>	_fields = new ArrayList<Field>();
	private SparseArray<Integer> _graphicSet = new SparseArray<Integer>();
	
	private GraphicsLayer		_graphicLayer;
	
	private GraphicsLayer		_labelLayer;	//标准用图层
	private SparseArray<Integer> _labelGraphicSet;
	
	//当图层的withPhoto属性为true时，在要素图层下方铺上一层，用来标识照片方向的辅助图层
	private GraphicsLayer		_photoOrientationLayer ;
	private SparseArray<Integer> _photoOrientationGraphicSet ;
	
	Layer(Cursor data){
		_layerId	= data.getInt(data.getColumnIndexOrThrow(LayersColumns._ID));
		_type		= data.getString(data.getColumnIndexOrThrow(LayersColumns.GEO_TYPE));
		_symbol		= data.getString(data.getColumnIndexOrThrow(LayersColumns.SYMBOL));
		_annotation	= data.getString(data.getColumnIndexOrThrow(LayersColumns.ANNOTATION));
		_name		= data.getString(data.getColumnIndexOrThrow(LayersColumns.NAME));
		_displayName= data.getString(data.getColumnIndexOrThrow(LayersColumns.DISPLAY_NAME));
		_classId	= data.getInt(data.getColumnIndexOrThrow(LayersColumns.LAYER_CLASS_ID));
		_size		= data.getInt(data.getColumnIndexOrThrow(LayersColumns.SIZE));
		_alpha		= data.getInt(data.getColumnIndexOrThrow(LayersColumns.ALPHA));
		_red		= data.getInt(data.getColumnIndexOrThrow(LayersColumns.RED));
		_green		= data.getInt(data.getColumnIndexOrThrow(LayersColumns.GREEN));
		_blue		= data.getInt(data.getColumnIndexOrThrow(LayersColumns.BLUE));
		
		int photo	= data.getInt(data.getColumnIndexOrThrow(LayersColumns.WITH_PHOTO));
		if(0==photo){
			_withPhoto = false;
			
		}else{
			_withPhoto = true;
			_photoOrientationLayer = new GraphicsLayer();
			_photoOrientationLayer.setVisible(false);
			_photoOrientationLayer.setMinScale(12000);
			_photoOrientationGraphicSet  = new SparseArray<Integer>();
		}
		
		String	attrXml = data.getString(data.getColumnIndexOrThrow(LayersColumns.FIELDS));
		_initFields(attrXml);
		
		_graphicLayer = new GraphicsLayer();
		_graphicLayer.setVisible(false);
		
		
		_labelLayer = new GraphicsLayer();
		_labelLayer.setVisible(false);
		_labelLayer.setMinScale(3000);
		_labelGraphicSet = new SparseArray<Integer>();
	}
	
	void	clean(){
		_graphicSet.clear();
		_fields.clear();
		_graphicLayer.removeAll();
		_graphicLayer.recycle();
		
		
		_labelGraphicSet.clear();
		_labelLayer.removeAll();
		_labelLayer.recycle();
		
		if(_withPhoto){
			_photoOrientationGraphicSet.clear();
			_photoOrientationLayer.removeAll();
			_photoOrientationLayer.recycle();
		}
	}
	
	public	void	addFeature(Context context, Project project, int id,Geometry geo, String cc){
		
		
		Map<String, Object> attrs = new HashMap<String, Object>();
		attrs.put(FeatureColumns._ID, id);
		attrs.put(FeatureColumns.LAYER_ID, _layerId);
		attrs.put(FeatureColumns.CC, cc);
		
		Graphic graphic = new Graphic(geo,getSymbolObject(),attrs,null);
		
		
		int gid = _graphicLayer.addGraphic(graphic);
		
		//以备将来根据featureid 来查询图层中的某个graphic，以featureid为key将graphicid存入稀疏数组
		_graphicSet.append(id, gid);
		
		
		//对有CC属性的，给要素添加标注图像
		if(null!=cc && cc.length()>0){
			TextSymbol txtSym = new TextSymbol(LABEL_SIZE, cc,
					_getInvertRGB());
			Envelope  box  = new Envelope();
			
			geo.queryEnvelope(box);
			
			Point center = box.getCenter();

			Graphic labelGraphic = new Graphic(center, txtSym);
			
			int labelGid = _labelLayer.addGraphic(labelGraphic);
			
			_labelGraphicSet.append(id, labelGid);
		}
		
		
		//有照片的，添加照片方向指示图形
		if(_withPhoto){
			
			if(null==ORIENTATION_IMG){
				ORIENTATION_IMG = context.getResources().getDrawable(R.drawable.img_orientation_pp);
			}
			
			PictureMarkerSymbol  orientationSymbol   = new PictureMarkerSymbol(ORIENTATION_IMG);
			
			orientationSymbol.setAngle(project.getPhotoBearing(id));
			
			
			Graphic graphicOrientation = new Graphic(geo,orientationSymbol,attrs,null);
			
			
			int poGid = _photoOrientationLayer.addGraphic(graphicOrientation);
			
			_photoOrientationGraphicSet.append(id, poGid);
		}
		
	}
	
	
	public	void	removeFeaturePlus(int featureId){
		
		Integer	gid	= _graphicSet.get(featureId);
		
		if(null!=gid){
			_graphicLayer.removeGraphic(gid);
			_graphicSet.remove(featureId);
			
			Integer	labelGid = _labelGraphicSet.get(featureId);
			if(null!=labelGid){
				_labelLayer.removeGraphic(labelGid);
				_labelGraphicSet.remove(featureId);
			}
	
			if(_withPhoto){
				Integer	poGid = _photoOrientationGraphicSet.get(featureId);
				
				if(null!=poGid){
					_photoOrientationLayer.removeGraphic(poGid);
					_photoOrientationGraphicSet.remove(featureId);
				}
			}
			
		}
		
		
		
	}
	
	/*public	int		getGraphicIdByFeatrueId(int featureId){
		Integer gid = _graphicSet.get(featureId);
		
		if(null==gid)
			return -1;
		else
			return gid;
	}*/
	
	public	List<AttributeListItem>	getAttributeListItems(Context context, String recordXml){
		
		List<AttributeListItem> result = new ArrayList<AttributeListItem>();
		
		int index = 0;
		
		
		List<String> records = _getRecords(recordXml);
		
		//Utils.DCQLog("attributeXml:"+attributeXml+"size of records="+records.size());
		
		for(Field field:_fields){
			
			String record ;
			if(null==records){
				record = "";
			}else{
				try{
					record = records.get(index);
				}catch(IndexOutOfBoundsException e){
					record = "";
				}
			}
			
			if(0==record.length()){
				record = field.getValue();
			}
			
			
			AttributeListItem ali = AttributeListItem.factoryALI(context, field,record );
			result.add(ali);
			
			index++;
		}
		return result;
	}
	
	
	public 	boolean withPhoto(){
		return _withPhoto;
	}
	
	
	public	 String	getName(){
		return _name;
	}
	
	public	String	getDisplayName(){
		if(null!=_displayName && _displayName.length()>0){
			return _displayName;
		}else{
			return _name;
		}
	}
	
	public	int		getLayerId(){
		return _layerId;
	}
	
	public int		getClassId(){
		return _classId;
	}
	
	
	

	
	public final String	getType(){
		return _type;
	}
	
	
	public final int		getARGB(){
		return Color.argb(_alpha, _red, _green, _blue);
	}
	
	
	public final String		getSymbolString(){
		return _symbol;
	}
	
	public final int		getSize(){
		return _size;
	}
	
	public final List<Field> getFieldList(){
		return _fields;
	}
	
	public Symbol		getSymbolObject(){
		
		if(_type.equals(TYPE_VALUE_POINT)){
			return _getSymbolPoint();
		}else if(_type.equals(TYPE_VALUE_LINESTRING)){
			return _getSymbolPolyline();
		}else if(_type.equals(TYPE_VALUE_POLYGON)){
			return _getSymbolPolygon();
		}else{
			return null;
		}
	}
	
	public boolean			isVisible(){
		return _graphicLayer.isVisible();
	}
	
	public	int[]			getGraphicIDs(float x, float y, int tolerance, int numberOfResults){
		return _graphicLayer.getGraphicIDs(x, y, tolerance, numberOfResults);
	}
	
	public	Graphic			getGraphic(int graphicId){
		return _graphicLayer.getGraphic(graphicId);
	}
	
	public	void			addLayerToMapView(MapView mapView){
		if(_withPhoto){
			mapView.addLayer(_photoOrientationLayer);
		}
		mapView.addLayer(_graphicLayer);
		
		mapView.addLayer(_labelLayer);
	}
	
	
	public void			setVisibility(boolean visibility, boolean	labelVisibility){
		Utils.amLog("layer:"+_name+", set visibility:"+visibility);
		_graphicLayer.setVisible(visibility);
		if(_withPhoto){
			_photoOrientationLayer.setVisible(visibility);
		}
		_labelLayer.setVisible(visibility&&labelVisibility);
	}
	
	
	
	private List<String>	_getRecords(String recordXml){
		if(null==recordXml)
			return null;
		
		List<String> result = new ArrayList<String>();
		
		
		//创建一个新的字符串
        StringReader read = new StringReader(recordXml);
        //创建新的输入源SAX 解析器将使用 InputSource 对象来确定如何读取 XML 输入
        InputSource source = new InputSource(read);
        //创建一个新的SAXBuilder
        SAXBuilder sb = new SAXBuilder();

        //通过输入源构造一个Document
        Document doc;
		try {
			doc = sb.build(source);
			
			 //取的根元素
	        Element fieldRoot = doc.getRootElement();
	        
	        
	        @SuppressWarnings("unchecked")
			Iterator<Element> itr = (fieldRoot.getChildren(Record.TAG_RECORD)).iterator();
	        
			while(itr.hasNext()) {
				   Element 	fieldConfig 	= (Element)itr.next();
				   
				  
				   result.add(fieldConfig.getAttributeValue(Record.ATTR_VALUE, ""));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
			
			
		return result;
	}
	
	private Symbol 	_getSymbolPoint(){
		SimpleMarkerSymbol.STYLE	style;
		
		if(_symbol.equals(SimpleMarkerSymbol.STYLE.CIRCLE.name())){
			style = SimpleMarkerSymbol.STYLE.CIRCLE;
		}else if(_symbol.equals(SimpleMarkerSymbol.STYLE.CROSS.name())){
			style = SimpleMarkerSymbol.STYLE.CROSS;
		}else if(_symbol.equals(SimpleMarkerSymbol.STYLE.DIAMOND.name())){
			style = SimpleMarkerSymbol.STYLE.DIAMOND;
		}else if(_symbol.equals(SimpleMarkerSymbol.STYLE.SQUARE.name())){
			style = SimpleMarkerSymbol.STYLE.SQUARE;
		}else{
			style = SimpleMarkerSymbol.STYLE.X;
		}
		
		return new SimpleMarkerSymbol(getARGB(),_size,style);
	}
	
	private Symbol	_getSymbolPolyline(){
		
		SimpleLineSymbol.STYLE style;
		
		if(_symbol.equals(SimpleLineSymbol.STYLE.DASH.toString())){
			style = SimpleLineSymbol.STYLE.DASH;
		}else if(_symbol.equals(SimpleLineSymbol.STYLE.DASHDOT.toString())){
			style = SimpleLineSymbol.STYLE.DASHDOT;
		}else if(_symbol.equals(SimpleLineSymbol.STYLE.DASHDOTDOT.toString())){
			style = SimpleLineSymbol.STYLE.DASHDOTDOT;
		}else if(_symbol.equals(SimpleLineSymbol.STYLE.DOT.toString())){
			style = SimpleLineSymbol.STYLE.DOT;
		}else if(_symbol.equals(SimpleLineSymbol.STYLE.NULL.toString())){
			style = SimpleLineSymbol.STYLE.NULL;
		}else{
			style = SimpleLineSymbol.STYLE.SOLID;
		}
		
		return new SimpleLineSymbol(getARGB(), 
			_size, style);
	}
	
	private Symbol	_getSymbolPolygon(){
		return new SimpleFillSymbol(getARGB());
	}
	
	private int		_getRGB(){
		return Color.rgb(_red, _green, _blue);
	}
	
	private int		_getInvertRGB(){
		int	invertRed 	= 255-_red;
		int	invertGreen	= 255-_green;
		int invertBlue	= 255-_blue;
		return Color.rgb(invertRed, invertGreen, invertBlue);
	}
	
	@SuppressWarnings("unchecked")
	private void	_initFields(String attrXml){
        StringReader	reader 	= new StringReader(attrXml);
        InputSource 	is 		= new InputSource(reader);
        XmlOperator		xml	   	= new XmlOperator(is);
        
        try {
			Element			root	= xml.getRoot();
			List<Element>	fields  = root.getChildren(Field.FIELD_TAG);
			
			for(Element field : fields){
				_fields.add(new Field(field));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
        xml.Destory();
	}
	
	
	

}

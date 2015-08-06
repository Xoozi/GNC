package com.gnc.dcqtech.project;


/**
 * 定义工程数据库文件表结构的类
 * @author xoozi
 *
 */
public class ProjectDataBase {
	
	/**
	 * 工程Profile表的列
	 * @author xoozi
	 *
	 */
	public interface	ProfileColumns{
		public static final String	TABLE_NAME		= "Profile";
		
		public static final String	_ID				= "_id";
		public static final String	PROJECT_NAME	= "ProjectName";
		public static final String	INITED			= "Inited";
		public static final String	ENTERPRISE 		= "Enterprise";
		public static final String	PARK			= "Park";
		public static final String	AUTHOR			= "Author";
		public static final String	MODIFIER		= "Modifier";
		public static final String	CREATE_TIME		= "CreateTime";
		public static final String	MODIFY_TIME 	= "ModifyTime";
		public static final String	MAP_NAME		= "MapName";
		public static final String	WKID			= "WKID";
		public static final String	DESCRIPTION		= "Description";
		
		public static final String	CREATE_SQL	= "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME +
				"("+
				" "+_ID+			" INTEGER PRIMARY KEY," +
				" "+PROJECT_NAME+	" TEXT UNIQUE NOT NULL," +
				" "+INITED+			" INTEGER," +
				" "+WKID+			" INTEGER," +
				" "+ENTERPRISE+		" TEXT," +
				" "+PARK+			" TEXT," +
				" "+AUTHOR+			" TEXT," +
				" "+MODIFIER+		" TEXT," +
				" "+DESCRIPTION+	" TEXT," +
				" "+MAP_NAME+		" TEXT," +
				" "+CREATE_TIME+	" INTEGER,"+
				" "+MODIFY_TIME+	" INTEGER" +
				");";
	}
	
	/**
	 * 图层大类表的列
	 * @author xoozi
	 *
	 */
	public	interface	LayerClassColumns{
		public static final String	TABLE_NAME		= "LayerClass";
		
		
		public static final String	_ID				= "_id";			//主键
		public static final String	CLASS_NAME		= "ClassName";		//图层大类名
		
		public static final String	CREATE_SQL	= "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME +
				"("+
				" "+_ID+			" INTEGER PRIMARY KEY," +
				" "+CLASS_NAME+		" TEXT UNIQUE NOT NULL"+
				");";
	}
	
	
	/**
	 * 定义图层表的列
	 * @author xoozi
	 *
	 */
	public interface	LayersColumns{
		public static final String	TABLE_NAME		= "Layers";
		
		
		public static final String	_ID				= "_id";			//主键
		public static final String	LAYER_CLASS_ID	= "LayerClassId";	//所属图层大类id
		public static final String	NAME			= "Name";			//图层名
		public static final String	DISPLAY_NAME	= "DisplayName";	//显示名
		public static final String	GEO_TYPE		= "GeoType";		//几何类型
		public static final String	WITH_PHOTO		= "WithPhoto";		//图层是否可以关联照片 默认不关联
		public static final String	SYMBOL			= "Symbol";			//符号
		public static final String	ALPHA			= "Alpha";			//透明度
		public static final String	RED				= "Red";			//红色分量
		public static final String	GREEN			= "Green";			//绿色分量
		public static final String	BLUE			= "Blue";			//蓝色分量
		public static final String	SIZE			= "Size";			//尺寸，点的半径，线的宽度，面类型无视此字段
		public static final String	ANNOTATION		= "Annotation";		//标注格式[ATTR1]$[ATTR2] 样式的字符串$为分隔符 split中的元素
																		//为方括号包围的属性名 或者直接字符串 或者vbCrLf代表的换行回车
		public static final String	FIELDS			= "Fields";			//xml定义的属性域表
		
		public static final String	CREATE_SQL	= "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME +
				"("+
				" "+_ID+			" INTEGER PRIMARY KEY," +
				" "+LAYER_CLASS_ID+	" INTEGER"+
				" "+"REFERENCES "+LayerClassColumns.TABLE_NAME+"("+LayerClassColumns._ID+"),"+
				" "+NAME+			" TEXT UNIQUE NOT NULL,"+
				" "+DISPLAY_NAME+	" TEXT,"+
				" "+GEO_TYPE+		" TEXT,"+
				" "+WITH_PHOTO+		" INTEGER,"+
				" "+SYMBOL+			" TEXT,"+
				" "+ALPHA+			" INTEGER,"+
				" "+RED+			" INTEGER,"+
				" "+GREEN+			" INTEGER,"+
				" "+BLUE+			" INTEGER,"+
				" "+SIZE+			" INTEGER,"+
				" "+ANNOTATION+		" TEXT,"+
				" "+FIELDS+			" TEXT"+
				");";
		
	}
	
	/**
	 * 定义选择器表的列
	 * @author xoozi
	 *
	 */
	public interface SelectionColumns{
		public static final String	TABLE_NAME	= "Selections";
		
		public static final String	_ID				= "_id";			//主键
		public static final String	SELECTION_NAME	= "SelectionName";	//选择块名 在图层的Field表中以名字来引用
		
		public static final String	CREATE_SQL	= "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME +
				"("+
				" "+_ID+			" INTEGER PRIMARY KEY," +
				" "+SELECTION_NAME+	" TEXT UNIQUE NOT NULL"+
				");";
		
	}
	
	/**
	 * 定义选择器项表的列
	 * @author xoozi
	 *
	 */
	public	interface SelectionItemColumns{
		
		public static final String	TABLE_NAME	= "SelectionItems";
		
		public static final String	_ID				= "_id";			//主键
		public static final String	SELECTION_ID	= "SelectionId";	//关联的选择块id 外键
		public static final String	DISPLAY_NAME	= "DisplayName";	//选择器项的显示名
		public static final String  ITEM_VALUE		= "ItemValue";		//该项保存时填充到Record中的实际值
		
		public static final String	CREATE_SQL	= "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME +
				"("+
				" "+_ID+			" INTEGER PRIMARY KEY," +
				" "+SELECTION_ID+	" INTEGER"+
				" "+"REFERENCES "+SelectionColumns.TABLE_NAME+"("+SelectionColumns._ID+"),"+
				" "+DISPLAY_NAME+	" TEXT,"+
				" "+ITEM_VALUE+		" TEXT"+
				");";
		
	}
	
	/*现在资料尚不丰富，我不了解国标中定义的所有要素类别，故先不定这么细
	/**
	 * 定义要素类别表的列
	 * @author xoozi
	 *
	 *
	public interface FeatureClassColumns{
		public static final String	TABLE_NAME	= "FeatureClass";
		
		public static final String	_ID			= "_id";			//主键
		public static final String	LAYER_ID	= "LayerId";		//所属图层id
		public static final String	CLASS_CODE	= "ClassCode";		//类型码
		public static final String	CLASS_1		= "Class1";			//一类码
		public static final String	CLASS_2		= "Class2";			//二类码
		public static final String	CLASS_3		= "Class3";			//三类码
		
		public static final String	CREATE_SQL	= "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME +
				"("+
				" "+_ID+			" INTEGER PRIMARY KEY," +
				" "+LAYER_ID+		" INTEGER"+
				" "+"REFERENCES "+LayersColumns.TABLE_NAME+"("+LayersColumns._ID+"),"+
				" "+CLASS_CODE+		" INTEGER UNIQUE NOT NULL,"+
				" "+CLASS_1+		" INTEGER,"+
				" "+CLASS_2+		" INTEGER,"+
				" "+CLASS_3+		" INTEGER"+
				");";
	}*/
	
	
	/**
	 * 定义要素表的列
	 * @author xoozi
	 *
	 */
	public	interface FeatureColumns{
		
		public static final String	TABLE_NAME	= "Feature";
		
		public static final String	_ID			= "_id";				//主键
		public static final String	GUID		= "GUID";				//GUID
		public static final String	LAYER_ID	= "LayerId";			//所属图层id
		public static final String	STATE		= "State";				//要素状态
		public static final String	CC			= "CC";				 	//分类码
		public static final String	FEATURE_TYPE= "FeatureType";		//要素几何类型
		public static final String	GEO_DATA	= "GeoData";			//要素几何数据
		public static final String	ATTR_DATA	= "AttrData";			//要素属性数据
		public static final String	CREATE_TIME	= "CreateTime";
		public static final String	MODIFY_TIME	= "ModifyTime";
		public static final String	AUTHOR		= "Author";
		public static final String	MODIFIER	= "Modifier";
		
		
		public static final int		STATE_VALUE_NORMAL 	= 0;			//状态值 新建
		public static final int		STATE_VALUE_DELETE	= 1;			//状态值 删除标记
		public static final int		STATE_VALUE_MODIFY  = 2;			//状态值 修线
		public static final int		STATE_VALUE_DIVISION= 3;			//状态值 分割
		
		
		
		public static final String	CREATE_SQL	= "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME +
				"("+
				" "+_ID+			" INTEGER PRIMARY KEY," +
				" "+GUID+			" TEXT UNIQUE NOT NULL,"+
				" "+LAYER_ID+		" INTEGER"+
				" "+"REFERENCES "+LayersColumns.TABLE_NAME+"("+LayersColumns._ID+"),"+
				" "+STATE+			" INTEGER,"+
				" "+CC+				" TEXT,"+
				" "+FEATURE_TYPE+	" TEXT,"+
				" "+GEO_DATA+		" TEXT,"+
				" "+ATTR_DATA+		" TEXT,"+
				" "+CREATE_TIME+	" INTEGER,"+
				" "+MODIFY_TIME+	" INTEGER," +
				" "+AUTHOR+			" TEXT," +
				" "+MODIFIER+		" TEXT" +
				");";
	}
	
	
	
	
	/**
	 * 定义GPS轨迹点表的列
	 * @author xoozi
	 *
	 */
	public interface	GPSPointsColumns{
		public static final String	TABLE_NAME	= "GPSPoints";
		
		public static final String	_ID			= "_id";			//主键
		public static final String	LON			= "Lon";			//经度
		public static final String	LAT			= "Lat";			//纬度
		public static final String	ALT			= "Alt";			//高程
		public static final String	GPSTIME		= "GPSTime";		//GPS授时
		
		public static final String	CREATE_SQL	= "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME +
				"("+
				" "+_ID+			" INTEGER PRIMARY KEY," +
				" "+LON+			" DOUBLE,"+
				" "+LAT+			" DOUBLE,"+
				" "+ALT+			" DOUBLE,"+
				" "+GPSTIME+		" INTEGER" +
				");";
	}
	
	
	/**
	 * 定义照片表的列
	 * @author xoozi
	 *
	 */
	public interface	PhotoColumns{
		public static final String	TABLE_NAME	= "Photo";
		
		public static final String	_ID			= "_id";			//主键
		public static final String	FEATURE_ID	= "FeatureId";		//照片所属的要素id，如果是-1就代表是一个独立的采样点
		public static final String	PHOTO_NAME	= "PhotoName";		//图片名
		public static final String	LON			= "Lon";			//经度
		public static final String	LAT			= "Lat";			//纬度
		public static final String	ALT			= "Alt";			//高程
		public static final String	SCOUNT		= "SatelliteCount"; //卫星数
		public static final String	BEARING		= "Bearing";		//方位角
		public static final String	PITCH		= "Pitch";			//俯仰角
		public static final String	ROLL		= "Roll";			//横滚角
		public static final String	THUMB_DATA	= "ThumbData";		//缩略图
		public static final String	DISTANCE	= "Distance";		//拍摄距离
		public static final String	CLASS_CODE	= "ClassCode";		//分类代码
		public static final String	ENV_DES		= "EnvDes";			//环境描述
		public static final String	CREATE_TIME	= "CreateTime";
		public static final String	MODIFY_TIME	= "ModifyTime";
		public static final String	AUTHOR		= "Author";
		public static final String	MODIFIER	= "Modifier";
		
		public static final String	CREATE_SQL	= "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME +
				"("+
				" "+_ID+			" INTEGER PRIMARY KEY," +
				" "+FEATURE_ID+		" INTEGER ," +
				" "+PHOTO_NAME+		" TEXT UNIQUE NOT NULL," +
				" "+LON+			" DOUBLE,"+
				" "+LAT+			" DOUBLE,"+
				" "+ALT+			" DOUBLE,"+
				" "+SCOUNT+			" INTEGER,"+
				" "+BEARING+		" DOUBLE,"+
				" "+PITCH+			" DOUBLE,"+
				" "+ROLL+			" DOUBLE,"+
				" "+THUMB_DATA+		" BLOB ," +
				" "+DISTANCE+		" INTEGER ," +
				" "+CLASS_CODE+		" INTEGER ," +
				" "+ENV_DES+		" TEXT," +
				" "+CREATE_TIME+	" INTEGER,"+
				" "+MODIFY_TIME+	" INTEGER," +
				" "+AUTHOR+			" TEXT," +
				" "+MODIFIER+		" TEXT" +
				");";
		
	}

}

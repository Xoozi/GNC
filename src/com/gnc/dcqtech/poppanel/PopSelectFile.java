package com.gnc.dcqtech.poppanel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.gnc.dcqtech.R;
import com.xoozi.andromeda.uicontroller.PopPanelBase;
import com.xoozi.andromeda.utils.LicenceTools;
import com.xoozi.andromeda.utils.Utils;

public class PopSelectFile extends PopPanelBase implements OnItemClickListener{

	
	
	private static 	final String	LIST_MAP_KEY_NAME 		= "Name";
	private static 	final String	LIST_MAP_KEY_IMG  		= "Image";
	private static 	final String	LIST_MAP_KEY_FILE 		= "File";	
	private static 	final String	PARENT_PATH 			= "..";
	
	private IPopSelectFile				_popSelectFile;//activity给弹出窗口的回调
	private	ListView					_itemList;		//文件浏览列表控件
	private	File						_path;			//记录当前浏览目录
	private	File						_mntRoot;		//系统挂载 的文件根目录
	private	List<Map<String,Object>>	_fileDataList;	//数据源
	private	String						_title;			//标题
	private String						_extName;		//扩展名，如果为null或者空串 就不限制
	private TextView					_currentFolder;	//当前目录
	
	public PopSelectFile(Context context, View rootPanel,IPopSelectFile popSelectFile,String title, String extName) {
		super(context, rootPanel,PopPanelBase.PopMode.AT_LOCATION,true);
		_title = title;
		_extName = extName;
		_popSelectFile = popSelectFile;
		_initWork();
	}
	
	@Override
	protected void _initWork() {
		_basePanel = _layoutInflater.inflate(R.layout.pop_select_file, null);
		
		TextView	titleView = (TextView)_basePanel.findViewById(R.id.text_title);
		titleView.setText(_title);
		
		_currentFolder = (TextView)_basePanel.findViewById(R.id.text_current_folder);
		
		_itemList	= (ListView)_basePanel.findViewById(R.id.listview_folder_explorer);
	
		//这里我们用写死的/mnt目录，因为通过Environment只能取到/mnt/sdcard
		_mntRoot = Environment.getExternalStorageDirectory();
		
		_path = _mntRoot;
		
		refreshListItems(_path);
	}
	
	
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		if(0==position){
			_path = _mntRoot;
			refreshListItems(_path);
		}else if(1==position){
			_gotoParent();
		}else{
			
			File clickItem = (File) _fileDataList.get(position).get(LIST_MAP_KEY_FILE);
			
			if(clickItem.isDirectory()){
				_path = clickItem;
				refreshListItems(_path);
			}else{
				_hide();
				_popSelectFile.onSelectFile(clickItem);
			}
		}
	}
	
	public void	show(){
		_show();
	}
	
	
	/**
	 * 刷新当前路径的adapter
	 * @param path
	 */
	private void	refreshListItems(File path){
		_fileDataList	= buildListForSimpleAdapter(path);
		_currentFolder.setText(path.getAbsolutePath());
		SimpleAdapter	items = new SimpleAdapter(_context,_fileDataList,R.layout.list_item_select_folder,
				new String[]{LIST_MAP_KEY_NAME,LIST_MAP_KEY_IMG},
				new int[]{R.id.textFsExplorerName,R.id.imgFsExporerRow});
		
		_itemList.setAdapter(items);
		_itemList.setOnItemClickListener(this);
		_itemList.setSelection(0);
		
	}
	
	/**
	 * 返回父目录
	 */
	private void _gotoParent(){
		String	currentPath = _path.getAbsolutePath();
		String	rootPath	= _mntRoot.getAbsolutePath();
		
		if(currentPath.equalsIgnoreCase(rootPath)){
			Toast.makeText(_context, R.string.toast_fsexplorer_root_dir, Toast.LENGTH_SHORT).show();
		}else{
			File parent = _path.getParentFile();
			_path = parent;
			refreshListItems(_path);
		}
	}
	
	
	/**
	 * 构建指定目录的子目录adapter
	 * @param path
	 * @return
	 */
	private List<Map<String,Object>> buildListForSimpleAdapter(File path){
		
		File[]	files = path.listFiles();
		
		List<Map<String,Object>>	list	 = new ArrayList<Map<String,Object>>();
		
		Map<String,Object>	home	= new HashMap<String,Object>();
		home.put(LIST_MAP_KEY_NAME, _mntRoot.getName());
		home.put(LIST_MAP_KEY_IMG, R.drawable.img_fsexplorer_home);
		list.add(home);
		
		Map<String,Object>	parent	= new HashMap<String,Object>();
		parent.put(LIST_MAP_KEY_NAME, PARENT_PATH);
		parent.put(LIST_MAP_KEY_IMG, R.drawable.img_fsexplorer_upfolder);
		list.add(parent);
		
		//把目录集中显示在顶端 
		for(File file: files){
			if(file.isDirectory()){
	
				Map<String,Object>	map	= new HashMap<String,Object>();
				map.put(LIST_MAP_KEY_NAME, file.getName());
				map.put(LIST_MAP_KEY_IMG, R.drawable.img_fsexplorer_folder);
				map.put(LIST_MAP_KEY_FILE, file);
				list.add(map);
			}
		}
		
		//然后是文件
		for(File file: files){
			if(file.isFile() && _checkExtName(file)){
	
				Map<String,Object>	map	= new HashMap<String,Object>();
				map.put(LIST_MAP_KEY_NAME, file.getName());
				map.put(LIST_MAP_KEY_IMG, R.drawable.img_fsexplorer_file);
				map.put(LIST_MAP_KEY_FILE, file);
				list.add(map);
			}
		}
		
		return list;
	}
	
	private boolean _checkExtName(File file){
		if(null==_extName && _extName.length()==0){
			return true;
		}else{
			return LicenceTools.EXT_NAME.equals(Utils.getExtensionName(file.getName()));
		}
	}
	
}
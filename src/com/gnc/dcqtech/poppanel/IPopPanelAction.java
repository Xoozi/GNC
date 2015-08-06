package com.gnc.dcqtech.poppanel;

import java.io.File;

/**
 * 由Activity实现，响应弹出窗口回调的接口
 * @author xoozi
 *
 */
public interface IPopPanelAction {

	/**
	 * 当选择目录的操作返回
	 * @param selectedFolder
	 */
	public	void	onFolderSelected(File selectedFolder);
	
	
	/**
	 * 当图层显示过滤条件发生改变
	 * @param selectedLayers
	 */
	public	void	onLayerFilterChange(String[] selectedLayers);
	
	
	/**
	 * 当面板显示gps时，通知主窗口，如果lastlocation可用，就将此点居中
	 */
	public	void	onGPSShow();
	
	public	void	onPhotoSelectFeature(int featureId);
}

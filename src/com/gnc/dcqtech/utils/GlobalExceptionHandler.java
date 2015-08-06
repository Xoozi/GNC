package com.gnc.dcqtech.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Environment;


/**
 * 全局异常捕获器
 * @author xoozi
 *
 */
public class GlobalExceptionHandler implements UncaughtExceptionHandler{
	private static final String	ERROR_REPORT_DIR = "errorReports";
	private static final String	ERROR_REPORT_POSTFIX = "log";
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat 	_sdf 	= new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
	public void uncaughtException(Thread thread, Throwable ex) {
		StringWriter sw = new StringWriter();  
        PrintWriter pw = new PrintWriter(sw);  
        ex.printStackTrace(pw);  
        StringBuilder sb = new StringBuilder();  
        sb.append("Version code is ");  
        sb.append(Build.VERSION.SDK_INT + "\n");//设备的Android版本号  
        sb.append("Model is ");  
        sb.append(Build.MODEL+"\n");//设备型号  
        sb.append(sw.toString());  
        //准备开始保存
        {
        	File rootDir = Environment.getExternalStorageDirectory();
        	//在数据目录中找errorReportsDir， 如果不存在，就建立
        	File errorReportsDir = new File(rootDir, 
        			ERROR_REPORT_DIR);
    		if(!errorReportsDir.exists())
    			errorReportsDir.mkdir();
    		Date 		createTime 	= new Date();
    		String		createDate	= _sdf.format(createTime);
    		String		errorReportFileName = createDate+"."+ERROR_REPORT_POSTFIX;
    		File errorReportFile = new File(errorReportsDir, errorReportFileName);
    		
    		FileOutputStream fileOut=null;
    		OutputStreamWriter outStream = null;
    		try{
    			fileOut = new FileOutputStream(errorReportFile,false);
    			outStream = new OutputStreamWriter(fileOut);
    			outStream.write(sb.toString());
    		}catch (Exception e){
    		}
    		finally{		
    			try {
    				if(null!=outStream)
    					outStream.close();
    				
    				if(null!=fileOut)
    					fileOut.close();				
    			}catch (Exception e) {
    			}
    		}
        }

        
        //然后退出
        System.exit(10);
	}

}

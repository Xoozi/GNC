package com.gnc.dcqtech.utils;

/**
 * GIS小工具
 * @author xoozi
 *
 */
public class GISTools {

	/**
	 * 经纬度由小数转换至度分秒表示
	 * @param input
	 * @return
	 */
	public	static DMS toDMS(double input){
		
		input = Math.abs(input);
		
		DMS result = new DMS();
		
		result.d = (int)input;
		
		
		double mTemp = input-result.d;
		mTemp*=60;
		result.m = (int) mTemp;
		
		double sTemp = mTemp - result.m;
		sTemp*=60;
		result.s = (int) sTemp;
		return result;
	}
	
	
	
	
	/**
	 * 度分秒数据包装
	 * @author xoozi
	 *
	 */
	public static 	class DMS{
		int d;
		int m;
		int s;
		
		public String	format(){
			
			return String.format("%d°%02d′%02d\"",d,m,s);
		}
		
		public String	formatForFile(){
			return String.format("%d%02d%02d",d,m,s);
		}
		
	}
	
}

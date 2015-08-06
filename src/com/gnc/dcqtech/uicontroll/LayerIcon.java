package com.gnc.dcqtech.uicontroll;

import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.gnc.dcqtech.layer.Layer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.util.AttributeSet;
import android.view.View;


public class LayerIcon extends View{
	
	private	float		width;
	private float		height;
	
	private Layer		_layer;
	private Paint		_paint				= new Paint();
	private Paint		_background			= new Paint();
	private Paint		_paintText			= new Paint();
	private int			_textWidth;

	public LayerIcon(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
		_initWork();
	}

	public LayerIcon(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		_initWork();
	}	
	
	public LayerIcon(Context context) 
	{
		super(context);
		_initWork();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int	measuredWidth	= _measure(widthMeasureSpec);
		int	measuredHeight	= _measure(heightMeasureSpec);
		
		int	d 				= Math.min(measuredWidth, measuredHeight);
		
		setMeasuredDimension(d, d);
	}
	
	
	
	
	/**
	 * 设置当前显示图层图例
	 * @param layerLegend
	 */
	public void setLayer(Layer layer){
		_layer = layer;
		
	
		_paint.setColor(_layer.getARGB());

		
		//_background.setColor(_layerLegend.getInvertARGB());
		_background.setColor(Color.WHITE);
		
		String symbolString = _layer.getSymbolString();
		
		
		if(_layer.getType().equals(Layer.TYPE_VALUE_POINT)){
			

			
			if(symbolString.equals(SimpleMarkerSymbol.STYLE.SQUARE.toString())||
				symbolString.equals(SimpleMarkerSymbol.STYLE.CIRCLE.toString())||
				symbolString.equals(SimpleMarkerSymbol.STYLE.DIAMOND.toString())){
				_paint.setStyle(Paint.Style.FILL_AND_STROKE);
			}else{
				_paint.setStyle(Paint.Style.FILL_AND_STROKE);
				_paint.setStrokeWidth(5);
			}
			
		}else if(_layer.getType().equals(Layer.TYPE_VALUE_LINESTRING)){
			int size = _layer.getSize();
			_paint.setStyle(Paint.Style.STROKE);
			_paint.setStrokeWidth(size);
			
			_paint.setStrokeCap(Cap.ROUND);
			
	
			
			if(symbolString.equals(SimpleLineSymbol.STYLE.DOT.toString())){
				PathEffect effect = new DashPathEffect(new float[] { 0.4f*size, 2*size}, 1);
				_paint.setPathEffect(effect);
			}else if(symbolString.equals(SimpleLineSymbol.STYLE.DASH.toString())){
				PathEffect effect = new DashPathEffect(new float[] { 1.5f*size, 2*size}, 1);
				_paint.setPathEffect(effect);
			}else if(symbolString.equals(SimpleLineSymbol.STYLE.DASHDOT.toString())){
				PathEffect effect = new DashPathEffect(new float[] { 3*size, 2*size,size,2*size}, 1);
				_paint.setPathEffect(effect);
			}else if(symbolString.equals(SimpleLineSymbol.STYLE.DASHDOTDOT.toString())){
				PathEffect effect = new DashPathEffect(new float[] { 3*size, 2*size,size,2*size,size,2*size}, 1);
				_paint.setPathEffect(effect);
			}else{
				
				_paint.setPathEffect(null);
			}
			
			
		}else if(_layer.getType().equals(Layer.TYPE_VALUE_POLYGON)){
			_paint.setStyle(Paint.Style.FILL);
			_background.setColor(Color.argb(255, 255, 255, 255));
		}
		
		//请求重绘
		this.invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		_drawSymbol(canvas);
		super.onDraw(canvas);
	}
	
	private	int		_measure(int measureSpec){
		int	result = 0;			
		int	specSize	= MeasureSpec.getSize(measureSpec);
		
		if(specSize>100){
			result = 100;
		}else{
			result = specSize;
		}
		
		return result;
	}
	
	private void _initWork(){
		_paint.setAntiAlias(true);
		_background.setStyle(Paint.Style.FILL);
		_paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		_paintText.setColor(Color.argb(0xEE, 0x02, 0x4A, 0x68));
		_paintText.setFakeBoldText(true);
		_paintText.setSubpixelText(true);
		_paintText.setTextAlign(Align.LEFT);
	
		
	}
	
	private void _drawSymbol(Canvas canvas){
		if(null==_layer)
			return;
		
		width	= getMeasuredWidth();
		height	= getMeasuredHeight();
		
		int	size = (int)(height/3);
		_paintText.setTextSize(size);
		
		//先画背景
		//canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(),_background);
		canvas.drawARGB(0xEE, 0xEE, 0xEE, 0xEE);
		
		//再画点线面
		if(_layer.getType().equals(Layer.TYPE_VALUE_POINT)){
			_drawPoint(canvas);
		}else if(_layer.getType().equals(Layer.TYPE_VALUE_LINESTRING)){
			_drawLine(canvas);
		}else if(_layer.getType().equals(Layer.TYPE_VALUE_POLYGON)){
			_drawFill(canvas);
		}
		
		//最后画文字
		String	text = _layer.getName();

		_textWidth		= (int)_paintText.measureText(text);
		canvas.drawText(text, (width-_textWidth)/2, height, _paintText);
	}
	
	private void _drawPoint(Canvas canvas){
		
		String symbolString = _layer.getSymbolString();

		if(symbolString.equals(SimpleMarkerSymbol.STYLE.CIRCLE.name())){
	
			canvas.drawCircle(width/2, height/2, _layer.getSize(), _paint);
		}else if(symbolString.equals(SimpleMarkerSymbol.STYLE.CROSS.name())){
			
			canvas.drawLine(width*0.5f, height*0.25f, width*0.5f, height*0.75f, _paint);
			canvas.drawLine(width*0.25f, height*0.5f, width*0.75f, height*0.5f, _paint);
		}else if(symbolString.equals(SimpleMarkerSymbol.STYLE.X.name())){
			
			canvas.drawLine(width*0.25f, height*0.25f, width*0.75f, height*0.75f, _paint);
			canvas.drawLine(width*0.25f, height*0.75f, width*0.75f, height*0.25f, _paint);
		}else if(symbolString.equals(SimpleMarkerSymbol.STYLE.SQUARE.name())){
			
			canvas.drawRect(width*0.25f, height*0.25f, width*0.75f, width*0.75f, _paint);
		}else if(symbolString.equals(SimpleMarkerSymbol.STYLE.DIAMOND.name())){
			
			Path	diamond = new Path();
			diamond.setLastPoint(width*0.5f, height*0.25f);
			diamond.lineTo(width*0.75f, height*0.5f);
			diamond.lineTo(width*0.5f, height*0.75f);
			diamond.lineTo(width*0.25f, height*0.5f);
			diamond.close();
			canvas.drawPath(diamond, _paint);
			
		}
		
		
	}
	
	private void _drawLine(Canvas canvas){
		canvas.drawLine(0, 0, width, height, _paint);
	}
	
	private void _drawFill(Canvas canvas){
		canvas.drawRect(0.1f*width, 0.1f*height, 0.9f*width, 0.9f*height,_paint);
	}
}
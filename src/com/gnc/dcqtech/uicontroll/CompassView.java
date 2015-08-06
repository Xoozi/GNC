package com.gnc.dcqtech.uicontroll;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.graphics.Path.Direction;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

public class CompassView extends View {
	  
	  private enum CompassDirection { N, NNE, NE, ENE,
	    E, ESE, SE, SSE,
	    S, SSW, SW, WSW,
	    W, WNW, NW, NNW }
	  
	  private float bearing;

	  public void setBearing(float _bearing) {
	    bearing = _bearing;
	    sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
	  }

	  public float getBearing() {
	    return bearing;
	  }
	  
	  private float pitch;

	  public void setPitch(float _pitch) {
	    pitch = _pitch;
	    sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
	  }

	  public float getPitch() {
	    return pitch;
	  }

	  private float roll;

	  public void setRoll(float _roll) {
	    roll = _roll;
	    sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
	  }

	  public float getRoll() {
	    return roll;
	  }

	  private Paint backgroundPaint;
	  private Paint markerPaint;
	  private Paint textPaint;
	  private int textHeight;
	  
	  public CompassView(Context context) {
	    super(context);
	    initCompassView();
	  }

	  public CompassView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    initCompassView();
	  }

	  public CompassView(Context context,
	                     AttributeSet ats,
	                     int defaultStyle) {
	    super(context, ats, defaultStyle);
	    initCompassView();
	  }
	  
	  int markHeight = 10;
	  int arrowHeight = 30;

	   
	  protected void initCompassView() {
	    setFocusable(true);

	    // Get external resources
	    Resources r = this.getResources();
	    
	    backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    backgroundPaint.setColor(Color.argb(0x88, 0x69, 0x9b, 0x00));
	    backgroundPaint.setStyle(Paint.Style.FILL);


	    textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    textPaint.setColor(Color.argb(0xff, 0xff, 0xff, 0xff));
	    textPaint.setFakeBoldText(true);
	    textPaint.setSubpixelText(true);
	    textPaint.setTextAlign(Align.LEFT);
	    textPaint.setTextSize(15);

	    textHeight = (int)textPaint.measureText("yY");

	    markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    markerPaint.setColor(Color.argb(0xff, 0xff, 0xff, 0xff));
	    markerPaint.setAlpha(255);
	    markerPaint.setStrokeWidth(2);
	    markerPaint.setStyle(Paint.Style.STROKE);
	  }

	  
	  @Override
	  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    // The compass is a circle that fills as much space as possible.
	    // Set the measured dimensions by figuring out the shortest boundary,
	    // height or width.
	    int measuredWidth = measure(widthMeasureSpec);
	    int measuredHeight = measure(heightMeasureSpec);

	    int d = Math.min(measuredWidth, measuredHeight);

	    setMeasuredDimension(d, d);
	  }

	  private int measure(int measureSpec) {
	    int result = 0;

	    // Decode the measurement specifications.
	    int specMode = MeasureSpec.getMode(measureSpec);
	    int specSize = MeasureSpec.getSize(measureSpec);

	    if (specMode == MeasureSpec.UNSPECIFIED) {
	      // Return a default size of 200 if no bounds are specified.
	      result = 200;
	    } else {
	      // As you want to fill the available space
	      // always return the full available bounds.
	      result = specSize;
	    }
	    return result;
	  }
	  
	  @Override
	  protected void onDraw(Canvas canvas) {
	    float ringWidth = textHeight + 4;
	    
	    int height = getMeasuredHeight();
	    int width =getMeasuredWidth();

	    int px = width/2;
	    int py = height/2;
	    Point center = new Point(px, py);

	    int radius = Math.min(px, py)-2;

	    RectF boundingBox = new RectF(center.x - radius,
	                                  center.y - radius,
	                                  center.x + radius,
	                                  center.y + radius);

	    RectF innerBoundingBox = new RectF(center.x - radius + ringWidth,
	                                       center.y - radius + ringWidth,
	                                       center.x + radius - ringWidth,
	                                       center.y + radius - ringWidth);

	    float innerRadius = innerBoundingBox.height()/2;
	    Path outerRingPath = new Path();
	    outerRingPath.addOval(boundingBox, Direction.CW);

	    canvas.drawPath(outerRingPath,backgroundPaint);

	    

	    // Draw the arrow
	    Path rollArrow = new Path();
	    rollArrow.moveTo(center.x - 3, (int)innerBoundingBox.top + (markHeight+arrowHeight));
	    rollArrow.lineTo(center.x, (int)innerBoundingBox.top+markHeight+1);
	    rollArrow.moveTo(center.x + 3, innerBoundingBox.top + (markHeight+arrowHeight));
	    rollArrow.lineTo(center.x, innerBoundingBox.top+markHeight+1);
	    
	    canvas.drawPath(rollArrow, markerPaint);
	    
	   
	    canvas.save();
	    canvas.rotate(-1*(bearing), px, py);

	    // Should this be a double?
	    double increment = 22.5;

	    for (double i = 0; i < 360; i += increment) {
	      CompassDirection cd = CompassDirection.values()
	                            [(int)(i / 22.5)];
	      String headString = cd.toString();
	  
	      float headStringWidth = textPaint.measureText(headString);
	      PointF headStringCenter = 
	        new PointF(center.x - headStringWidth / 2,
	                   boundingBox.top + 1 + textHeight);
	  
	      //if (i % increment == 0)
	      if(i % 90==0)
	        canvas.drawText(headString,
	                        headStringCenter.x, headStringCenter.y,
	                        textPaint);
	      
	        canvas.drawLine(center.x, (int)innerBoundingBox.top,
	                        center.x, (int)innerBoundingBox.top + markHeight,
	                        markerPaint);
	  
	      canvas.rotate((int)increment, center.x, center.y);
	    }
	    canvas.restore();
	    
	   /* canvas.save();
	    canvas.rotate(bearing, center.x, center.y);
	    canvas.drawLine(center.x, center.y, center.x, center.y+arrowHeight, markerPaint);
	    canvas.restore();*/

	  }
	  
	  @Override
	  public boolean dispatchPopulateAccessibilityEvent(final AccessibilityEvent event) {
	    super.dispatchPopulateAccessibilityEvent(event);
	    if (isShown()) {
	      String bearingStr = String.valueOf(bearing);
	      if (bearingStr.length() > AccessibilityEvent.MAX_TEXT_LENGTH)
	        bearingStr = bearingStr.substring(0, AccessibilityEvent.MAX_TEXT_LENGTH);
	      
	      event.getText().add(bearingStr);
	      return true;
	    }
	    else
	      return false;
	  }
	}

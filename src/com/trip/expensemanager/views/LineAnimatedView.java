package com.trip.expensemanager.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.trip.expensemanager.R;

public class LineAnimatedView extends ProgressBar {

	private int iColor1, iColor2, iColor3;
	private Paint mAnimPaint1;
	private Paint mAnimPaint2;
	private Paint mAnimPaint3;
	private float mSeparatorWidth=2;
    private boolean scheduleNewFrame = true;
	private Runnable animator = new Runnable() {
		
	    @Override
	    public void run() {
	    	value=width/4;
	    	i=i+3;
	    	if(i>2 && i<=value){
				value=(int) i;
			} else if(i>value){
				i=0;
			}
	        if (!scheduleNewFrame) {
	            postDelayed(this, 15);
	        }
	        invalidate();
	    }
	};
	private int offset=1;
	private int width=0;
	private float i=0f;
	private int value;
//	private float prev=0f;

	public LineAnimatedView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.LineAnim,
				0, 0);

		try {
			iColor1 = a.getColor(R.styleable.LineAnim_color1, Color.BLUE);
			iColor2 = a.getColor(R.styleable.LineAnim_color2, Color.GREEN);
			iColor3 = a.getColor(R.styleable.LineAnim_color3, Color.RED);
		} finally {
			a.recycle();
		}

		init();
	}

	private void init() {
		mAnimPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
		mAnimPaint1.setColor(Color.BLUE);
		mAnimPaint1.setStyle(Style.STROKE);
		mAnimPaint1.setStrokeWidth(10);
		
		mAnimPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
		mAnimPaint2.setColor(iColor2);
		mAnimPaint2.setStyle(Style.STROKE);
		mAnimPaint2.setStrokeWidth(5);

		mAnimPaint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
		mAnimPaint3.setColor(iColor3);
		mAnimPaint3.setStyle(Style.STROKE);
		mAnimPaint3.setStrokeWidth(5);
	}

	public int getColor1() {
		return iColor1;
	}

	public void setColor1(int color) {
		iColor1=color;
		invalidate();
		requestLayout();
	}

	public int getColor2() {
		return iColor2;
	}

	public void setColor2(int color) {
		iColor2=color;
		invalidate();
		requestLayout();
	}

	public int getColor3() {
		return iColor3;
	}

	public void setColor3(int color) {
		iColor1=color;
		invalidate();
		requestLayout();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		width=getWidth();
		int prev=0;
		while (prev < width){
			canvas.drawLine(prev, 0, prev + value - mSeparatorWidth, 0, mAnimPaint1);
			prev = value + prev;
			value=width/4;
		}

		/*while (prev < width) {
			value = (float) value*2+offset;//Math.expm1(++i + offset);
			canvas.drawLine(prev, 0, prev + value - mSeparatorWidth, 0, mAnimPaint1);
			prev = value + prev;
		}*/
		if(scheduleNewFrame){
			scheduleNewFrame=false;
			removeCallbacks(animator);
			post(animator);
		}
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		removeCallbacks(animator);
	}

}

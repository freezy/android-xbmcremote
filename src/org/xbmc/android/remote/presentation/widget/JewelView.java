package org.xbmc.android.remote.presentation.widget;

import org.xbmc.android.remote.R;
import org.xbmc.api.type.ThumbSize;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class JewelView extends View {
	
	private Bitmap mPosterOverlay;
	private Bitmap mPoster;
	private Paint mPaint;
	private float mPosterAR;
	
	private int coverWidth, coverHeight;
	private int originalWidth, originalHeight;
	private int totalWidth, totalHeight;
	private float scaled;
	private int originalCoverHeight, originalCoverWidth;
	
	private final static String TAG = "JewelView"; 
	
	public JewelView(Context context) {
		super(context);
		init(context);
	}
	
	public JewelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		Log.d(TAG, "width = " + attrs.getAttributeValue("schemas.android.com/apk/res/android", "layout_width"));
		
	}
	
	public void setCover(int coverResource) {
		mPoster = BitmapFactory.decodeResource(getContext().getResources(), coverResource);
		mPosterAR = (float)mPoster.getHeight() / (float)mPoster.getWidth();
	}
	
	private final void init(Context context) {
		mPaint = new Paint();
		mPosterOverlay = BitmapFactory.decodeResource(context.getResources(), R.drawable.jewel_dvd);
		setPadding(3, 3, 3, 3);
		setCover(R.drawable.folder);
	}
	
	
	/**
     * @see android.view.View#measure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        
        final int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        final int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
        originalWidth = mPosterOverlay.getWidth();
        originalHeight = mPosterOverlay.getHeight();
        
        // reference is height, make width dependent on height.
        if (modeHeight == MeasureSpec.EXACTLY && modeWidth != MeasureSpec.EXACTLY) {
        	totalHeight = MeasureSpec.getSize(heightMeasureSpec);
        	originalCoverHeight = originalHeight - ThumbSize.scale(11 + 19);
        	originalCoverWidth = Math.round((float)originalCoverHeight / mPosterAR);
        	scaled = (float)totalHeight / (float)originalHeight;
        	coverHeight = Math.round((float)originalCoverHeight * scaled);
        	coverWidth = Math.round((float)originalCoverWidth * scaled);
        	totalWidth = coverWidth + Math.round((float)ThumbSize.scale(48 + 17) * scaled);

        	Log.d(TAG, "measure | by HEIGHT: " + MeasureSpec.getSize(widthMeasureSpec) + "x" + MeasureSpec.getSize(heightMeasureSpec) + " -> " + totalWidth + "x" + totalHeight);
//        	Log.d(TAG, "    --> cover resized 1 = " + originalCoverWidth + "x" + originalCoverHeight);
//        	Log.d(TAG, "    --> cover resized 2 = " + coverWidth + "x" + coverHeight);
        	setMeasuredDimension(totalWidth, totalHeight);
        } 
        // reference is width, make height dependent on width.
        else {
        	totalWidth = MeasureSpec.getSize(widthMeasureSpec);
            originalCoverWidth = originalWidth - ThumbSize.scale(48 + 17);
    		originalCoverHeight = Math.round((float)originalCoverWidth * mPosterAR);
    		scaled = (float)totalWidth / (float)originalWidth;
            coverHeight = Math.round((float)originalCoverHeight * scaled);
            coverWidth = Math.round((float)originalCoverWidth * scaled);
            totalHeight = coverHeight + Math.round((float)ThumbSize.scale(11 + 19) * scaled);
            
            Log.d(TAG, "measure | by WIDTH: " + MeasureSpec.getSize(widthMeasureSpec) + "x" + MeasureSpec.getSize(heightMeasureSpec) + " -> " + totalWidth + "x" + totalHeight);
    		
            setMeasuredDimension(totalWidth, totalHeight);
        }
        
        // fill_parent -> MeasureSpec.EXACTLY 
        // wrap_content -> MeasureSpec.AT_MOST
    }
    
    /**
     * Determines the width of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        Log.d(TAG, "width mode = " + specMode + ", width = " + specSize);
        
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result = mPosterOverlay.getWidth() + getPaddingLeft() + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

    /**
     * Determines the height of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        
        Log.d(TAG, "height mode = " + specMode + ", height = " + specSize);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // measure the height
            result = mPosterOverlay.getHeight() + getPaddingTop() + getPaddingBottom();
            final int originalWidth = mPosterOverlay.getWidth();
            final int originalCoverWidth = originalWidth - ThumbSize.scale(48 + 17);
    		final int originalCoverHeight = Math.round((float)originalCoverWidth * mPosterAR);
    		final float scaled = (float)getWidth() / (float)originalWidth;
            final int coverHeight = Math.round((float)originalCoverHeight * scaled);
            result = coverHeight + Math.round((float)ThumbSize.scale(11 + 19) * scaled);
            
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }
	
    /**
     * Render the text
     * 
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
    		
/*		final int originalWidth = mPosterOverlay.getWidth();
		final int originalHeight = mPosterOverlay.getHeight();

		final int totalWidth = getWidth();
		
		int originalCoverWidth = originalWidth - ThumbSize.scale(48 + 17);
		int originalCoverHeight = Math.round((float)originalCoverWidth * mPosterAR);
		
		final float scaled = (float)totalWidth / (float)originalWidth;
		
		final int coverWidth = Math.round((float)originalCoverWidth * scaled);
		final int coverHeight = Math.round((float)originalCoverHeight * scaled);
		
		final int totalHeight = coverHeight + Math.round((float)ThumbSize.scale(11 + 19) * scaled);*/
		
		final int pdnLeft = Math.round((float)ThumbSize.scale(48) * scaled);
		final int pdnTop  = Math.round((float)ThumbSize.scale(11) * scaled);

		Log.d(TAG, "original size old ar = " + originalWidth + "x" + originalHeight);
		Log.d(TAG, "new size old ar = " + totalWidth + "x" + getHeight());
		Log.d(TAG, "new size new ar = " + totalWidth + "x" + totalHeight);
		Log.d(TAG, "scaled = " + scaled);
		Log.d(TAG, "ar = " + mPosterAR);
		Log.d(TAG, "vertical padding = " + pdnTop + " " + Math.round((float)ThumbSize.scale(19) * scaled));
		Log.d(TAG, "cover original = " + mPoster.getWidth() + "x" + mPoster.getHeight());
		Log.d(TAG, "cover resized 1 = " + originalCoverWidth + "x" + originalCoverHeight);
		Log.d(TAG, "cover resized 2 = " + coverWidth + "x" + coverHeight);
		
		mPaint.setDither(true);
		mPaint.setColor(Color.GREEN);
		
		canvas.drawRect(new RectF(0, 0, getWidth(), getHeight()), mPaint);
		canvas.drawBitmap(mPoster, new Rect(0, 0, mPoster.getWidth(), mPoster.getHeight()), 
				new RectF(pdnLeft, pdnTop, pdnLeft + coverWidth, pdnTop + coverHeight), mPaint);
    	canvas.drawBitmap(mPosterOverlay, new Rect(0, 0, originalWidth, originalHeight), 
				new RectF(0, 0, totalWidth, totalHeight), mPaint);
		
	}
	
}
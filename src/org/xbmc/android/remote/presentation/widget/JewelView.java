package org.xbmc.android.remote.presentation.widget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xbmc.android.remote.R;
import org.xbmc.api.type.ThumbSize;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class JewelView extends View {
	
	private static final String ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android";

	private Bitmap mPosterOverlay;
	private Bitmap mPoster;
	private Paint mPaint;
	private float mPosterAR;

	private int coverWidth, coverHeight;
	private int originalWidth, originalHeight;
	private int totalWidth, totalHeight;
	private int specifiedWidth = 0;
	private int specifiedHeight = 0;
	private float scaled;
	private int originalCoverHeight, originalCoverWidth;
	private JewelType mType;
	
	public final static float AR_LANDSCAPE_SQUARE = 0.8f;
	public final static float AR_SQUARE_POSTER = 1.25f;
	public final static float AR_OVERLAY = 1.25f;

	private final static String TAG = "JewelView";
	
	public JewelView(Context context) {
		super(context);
		init(context);
	}

	public JewelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		
		// read layout width and height from xml element
		final Pattern p = Pattern.compile("([\\-\\d]+)");
		final String h = attrs.getAttributeValue(ANDROID_NAMESPACE, "layout_height");
		final String w = attrs.getAttributeValue(ANDROID_NAMESPACE, "layout_width");
		if (w != null) {
			try {
				Matcher matcher = p.matcher(w);
				if (matcher.find()) {
					specifiedWidth = Integer.parseInt(matcher.group(1));
				}
			} catch (Exception e) { }
			
		}
		if (h != null) {
			try {
				Matcher matcher = p.matcher(h);
				if (matcher.find()) {
					specifiedHeight = Integer.parseInt(matcher.group(1));
				}
			} catch (Exception e) { }
		}
	}

	public void setCover(int coverResource) {
//		Log.d(TAG, "setCover(" + coverResource + ")");
		setCover(BitmapFactory.decodeResource(getContext().getResources(), coverResource));
	}

	public void setCover(Bitmap cover) {
		mPoster = cover;
		mPosterAR = (float) cover.getHeight() / (float) cover.getWidth();
//		Log.d(TAG, "setCover(), AR = " + mPosterAR);
		mType = JewelType.get(mPosterAR);
		if (mType != null) {
			Log.i(TAG, "Set aspect ratio type for " + mPosterAR + " to " + mType);
			mPosterOverlay = BitmapFactory.decodeResource(getContext().getResources(), mType.overlayResource);
		} else {
			Log.w(TAG, "Unable to get aspect ratio type for " + mPosterAR);
		}
		requestLayout();
		invalidate();
	}

	private final void init(Context context) {
		mPaint = new Paint();
//		final int padding = ThumbSize.scale(3);
//		setPadding(padding, padding, padding, padding);
		setCover(R.drawable.default_jewel);
	}

	/**
	 * @see android.view.View#measure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		Log.d(TAG, "onMeasure(" + MeasureSpec.toString(widthMeasureSpec) + ", " + MeasureSpec.toString(heightMeasureSpec));

		if (mType == null) {
			setMeasuredDimension(0, 0);
			return;
		}
		
		final int modeWidth = specifiedWidth > 0 ? MeasureSpec.EXACTLY : MeasureSpec.getMode(widthMeasureSpec);
		final int modeHeight = specifiedHeight > 0 ? MeasureSpec.EXACTLY : MeasureSpec.getMode(heightMeasureSpec);
		originalWidth = mPosterOverlay.getWidth();
		originalHeight = mPosterOverlay.getHeight();
		
		if (modeHeight == modeWidth) {
			final float canvasAR = (float)MeasureSpec.getSize(heightMeasureSpec) / (float)MeasureSpec.getSize(widthMeasureSpec);
//			Log.d(TAG, "Patt, calculating from canvas AR (" + canvasAR + ")");
			if (mPosterAR > canvasAR * mType.overlayAR) {
				setMeasuredDimensionByHeight(MeasureSpec.getSize(heightMeasureSpec));
			} else {
				setMeasuredDimensionByWidth(MeasureSpec.getSize(widthMeasureSpec));
			}
		} else if (modeHeight == MeasureSpec.EXACTLY && modeWidth != MeasureSpec.EXACTLY && mPosterAR > AR_LANDSCAPE_SQUARE) {
			// reference is height, make width dependent on height.
			setMeasuredDimensionByHeight(specifiedHeight > 0 ? ThumbSize.scale(specifiedHeight) : MeasureSpec.getSize(heightMeasureSpec));
		} else {
			// reference is width, make height dependent on width.
			setMeasuredDimensionByWidth(specifiedWidth > 0 ? ThumbSize.scale(specifiedWidth) : MeasureSpec.getSize(widthMeasureSpec));
		}

		// fill_parent -> MeasureSpec.EXACTLY
		// wrap_content -> MeasureSpec.AT_MOST
	}
	
	private void setMeasuredDimensionByHeight(int height) {
		
		final Rect posterPosition = mType.posterPosition;
		
		totalHeight = height;
		originalCoverHeight = originalHeight - ThumbSize.scale(posterPosition.top + posterPosition.bottom);
		originalCoverWidth = Math.round((float) originalCoverHeight / mPosterAR);
		scaled = (float) totalHeight / (float) originalHeight;
		coverHeight = Math.round((float) originalCoverHeight * scaled);
		coverWidth = Math.round((float) originalCoverWidth * scaled);
		totalWidth = coverWidth + Math.round((float) ThumbSize.scale(posterPosition.left + posterPosition.right) * scaled);

//		Log.d(TAG, "Measuring by height (" + height + ") -> " + totalWidth + "x" + totalHeight);
		setMeasuredDimension(totalWidth, totalHeight + mType.bottomPadding);
	}
	
	private void setMeasuredDimensionByWidth(int width) {
		
		final Rect posterPosition = mType.posterPosition;
		
		totalWidth = width;
		originalCoverWidth = originalWidth - ThumbSize.scale(posterPosition.left + posterPosition.right);
		originalCoverHeight = Math.round((float) originalCoverWidth * mPosterAR);
		scaled = (float) totalWidth / (float) originalWidth;
		coverHeight = Math.round((float) originalCoverHeight * scaled);
		coverWidth = Math.round((float) originalCoverWidth * scaled);
		totalHeight = coverHeight + Math.round((float) ThumbSize.scale(posterPosition.top + posterPosition.bottom) * scaled);
		
//		Log.d(TAG, "Measuring by width (" + width + ") -> " + totalWidth + "x" + totalHeight);
		setMeasuredDimension(totalWidth, totalHeight + mType.bottomPadding);
	}

	/**
	 * Render the text
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {

		synchronized (this) {
			
			if (mType != null) {
				
//				Log.i(TAG, "Drawing " + mType);
	
				// Rect(left: 48, top: 11, right: 17, bottom: 19)
				final Rect posterPosition = mType.posterPosition;
				final int pdnLeft = Math.round((float) ThumbSize.scale(posterPosition.left) * scaled);
				final int pdnTop = Math.round((float) ThumbSize.scale(posterPosition.top) * scaled);
	
				mPaint.setDither(true);
				mPaint.setFilterBitmap(true);
				
				// draw actual poster
				canvas.drawBitmap(mPoster, new Rect(0, 0, mPoster.getWidth(), mPoster.getHeight()), 
					new Rect(pdnLeft, pdnTop, pdnLeft + coverWidth, pdnTop + coverHeight), mPaint); 
				// draw case overlay
				canvas.drawBitmap(mPosterOverlay, new Rect(0, 0, originalWidth, originalHeight), 
					new Rect(0, 0, totalWidth, totalHeight), mPaint);
			}
		}
	}

	private static class JewelType {

		private final static JewelType[] TYPES = { 
			//                     l   t   r   b
			new JewelType(new Rect(48, 11, 17, 19), AR_SQUARE_POSTER, 2000f, R.drawable.jewel_dvd, "Portrait (1:1.48)", 0, 1), 
			new JewelType(new Rect(41, 12, 17, 18), AR_LANDSCAPE_SQUARE, AR_SQUARE_POSTER, R.drawable.jewel_cd, "Cover (square)", 0, 1.105263157894737f), 
			new JewelType(new Rect(16, 11, 10, 34), 0.35f, AR_LANDSCAPE_SQUARE, R.drawable.jewel_tv, "Landscape (16:9)", ThumbSize.scale(30), 1)
		};

		public static final JewelType get(float ar) {
			for (JewelType jewelType : TYPES) {
				if (ar >= jewelType.minAR && ar < jewelType.maxAR) {
					return jewelType;
				}
			}
			return null;
		}

		public final Rect posterPosition;
		public final float minAR, maxAR;
		public final int overlayResource;
		public final String name;
		public final int bottomPadding;
		public final float overlayAR;

		public JewelType(Rect posterPosition, float minAR, float maxAR, int overlayResource, String name, int bottomPadding, float overlayAR) {
			this.posterPosition = posterPosition;
			this.minAR = minAR;
			this.maxAR = maxAR;
			this.overlayResource = overlayResource;
			this.name = name;
			this.bottomPadding = bottomPadding;
			this.overlayAR = overlayAR;
		}
		
		@Override
		public String toString() { 
			return name;
		}
	}

}
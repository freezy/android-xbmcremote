package org.xbmc.android.remote.presentation.widget;

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

	private Bitmap mPosterOverlay;
	private Bitmap mPoster;
	private Paint mPaint;
	private float mPosterAR;

	private int coverWidth, coverHeight;
	private int originalWidth, originalHeight;
	private int totalWidth, totalHeight;
	private float scaled;
	private int originalCoverHeight, originalCoverWidth;
	private JewelType mType;

	private final static String TAG = "JewelView";

	public JewelView(Context context) {
		super(context);
		init(context);
	}

	public JewelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public void setCover(int coverResource) {
		setCover(BitmapFactory.decodeResource(getContext().getResources(), coverResource));
	}

	public void setCover(Bitmap cover) {
		mPoster = cover;
		mPosterAR = (float) cover.getHeight() / (float) cover.getWidth();
		mType = JewelType.get(mPosterAR);
		if (mType != null) {
			Log.i(TAG, "Set aspect ratio type for " + mPosterAR + " to " + mType);
			mPosterOverlay = BitmapFactory.decodeResource(getContext().getResources(), mType.overlayResource);
		} else {
			Log.w(TAG, "Unable to get aspect ratio type for " + mPosterAR);
		}
		requestLayout();
	}

	private final void init(Context context) {
		mPaint = new Paint();
		setPadding(3, 3, 3, 3);
		setCover(R.drawable.default_jewel);
	}

	/**
	 * @see android.view.View#measure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		if (mType == null) {
			setMeasuredDimension(0, 0);
			return;
		}

		final Rect posterPosition = mType.posterPosition;
		final int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
		final int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
		originalWidth = mPosterOverlay.getWidth();
		originalHeight = mPosterOverlay.getHeight();

		// reference is height, make width dependent on height.
		if (modeHeight == MeasureSpec.EXACTLY && modeWidth != MeasureSpec.EXACTLY) {
			totalHeight = MeasureSpec.getSize(heightMeasureSpec);
			Log.d(TAG, "Measure by height: " + totalHeight);
			originalCoverHeight = originalHeight - ThumbSize.scale(posterPosition.top + posterPosition.bottom);
			originalCoverWidth = Math.round((float) originalCoverHeight / mPosterAR);
			scaled = (float) totalHeight / (float) originalHeight;
			coverHeight = Math.round((float) originalCoverHeight * scaled);
			coverWidth = Math.round((float) originalCoverWidth * scaled);
			totalWidth = coverWidth + Math.round((float) ThumbSize.scale(posterPosition.left + posterPosition.right) * scaled);

			setMeasuredDimension(totalWidth, totalHeight);
		}
		// reference is width, make height dependent on width.
		else {
			totalWidth = MeasureSpec.getSize(widthMeasureSpec);
			Log.d(TAG, "Measure by width: " + totalHeight);
			originalCoverWidth = originalWidth - ThumbSize.scale(posterPosition.left + posterPosition.right);
			originalCoverHeight = Math.round((float) originalCoverWidth * mPosterAR);
			scaled = (float) totalWidth / (float) originalWidth;
			coverHeight = Math.round((float) originalCoverHeight * scaled);
			coverWidth = Math.round((float) originalCoverWidth * scaled);
			totalHeight = coverHeight + Math.round((float) ThumbSize.scale(posterPosition.top + posterPosition.bottom) * scaled);

			setMeasuredDimension(totalWidth, totalHeight);
		}

		// fill_parent -> MeasureSpec.EXACTLY
		// wrap_content -> MeasureSpec.AT_MOST
	}

	/**
	 * Render the text
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {

		if (mType != null) {
			
			Log.i(TAG, "Drawing " + mType);

			// Rect(left: 48, top: 11, right: 17, bottom: 19)
			final Rect posterPosition = mType.posterPosition;
			final int pdnLeft = Math.round((float) ThumbSize.scale(posterPosition.left) * scaled);
			final int pdnTop = Math.round((float) ThumbSize.scale(posterPosition.top) * scaled);

//			Log.d(TAG, "original size old ar = " + originalWidth + "x" + originalHeight);
//			Log.d(TAG, "new size old ar = " + totalWidth + "x" + getHeight());
//			Log.d(TAG, "new size new ar = " + totalWidth + "x" + totalHeight);
//			Log.d(TAG, "scaled = " + scaled);
//			Log.d(TAG, "ar = " + mPosterAR);
//			Log.d(TAG, "vertical padding = " + pdnTop + " " + Math.round((float) ThumbSize.scale(posterPosition.bottom) * scaled));
//			Log.d(TAG, "cover original = " + mPoster.getWidth() + "x" + mPoster.getHeight());
//			Log.d(TAG, "cover resized 1 = " + originalCoverWidth + "x" + originalCoverHeight);
//			Log.d(TAG, "cover resized 2 = " + coverWidth + "x" + coverHeight);

			mPaint.setDither(true);

//			mPoster.setBounds(pdnLeft, pdnTop, pdnLeft + coverWidth, pdnTop + coverHeight);
//			mPoster.setFilterBitmap(true);
//			mPoster.draw(canvas);
//			mPosterOverlay.setBounds(0, 0, totalWidth, totalHeight);
//			mPosterOverlay.draw(canvas);
			
			// draw actual poster
			canvas.drawBitmap(mPoster, new Rect(0, 0, mPoster.getWidth(), mPoster.getHeight()), 
				new Rect(pdnLeft, pdnTop, pdnLeft + coverWidth, pdnTop + coverHeight), mPaint); 
			// draw case overlay
			canvas.drawBitmap(mPosterOverlay, new Rect(0, 0, originalWidth, originalHeight), 
				new Rect(0, 0, totalWidth, totalHeight), mPaint);
		}
	}

	private static class JewelType {

		private final static JewelType[] TYPES = { 
			new JewelType(new Rect(48, 11, 17, 19), 1.25f, 2000f, R.drawable.jewel_dvd, "Portrait (1:1.48)"), // 1:1.48 portrait
			new JewelType(new Rect(41, 12, 17, 18), 0.75f, 1.25f, R.drawable.jewel_cd, "Cover (square)")   // 1:1.0 square
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

		public JewelType(Rect posterPosition, float minAR, float maxAR, int overlayResource, String name) {
			this.posterPosition = posterPosition;
			this.minAR = minAR;
			this.maxAR = maxAR;
			this.overlayResource = overlayResource;
			this.name = name;
		}
		
		@Override
		public String toString() { 
			return name;
		}
	}

}
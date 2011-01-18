/*
 *      Copyright (C) 2005-2011 Team XBMC
 *      http://xbmc.org
 *
 *  This Program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2, or (at your option)
 *  any later version.
 *
 *  This Program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XBMC Remote; see the file license.  If not, write to
 *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *  http://www.gnu.org/copyleft/gpl.html
 *
 */

package org.xbmc.android.widget.gestureremote;

import org.xbmc.android.remote.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class GestureRemoteView extends View {
	
	private final static String TAG = "GestureRemoteView";
	
	public static final float PIXEL_SCALE = Resources.getSystem().getDisplayMetrics().density;
	
	private final static int ANIMATION_DURATION = 100;
	
	private final static int CURSOR_NEG_PADDING = (int)(17 * PIXEL_SCALE);
	private final static int CURSOR_POS_PADDING = (int)(30 * PIXEL_SCALE);
	
	private final static int SCROLL_ZONE_WIDTH = 62;
	private final static int LEFT_BORDER_WIDTH = 22;
	private final static int RIGHT_BORDER_WIDTH = 21;
	private final static int TOP_BORDER_HEIGHT = 11;
	private final static int BOTTOM_BORDER_HEIGHT = 19;
	
	private final static int BOX_LEFT_WIDTH = 101;
	private final static int BOX_RIGHT_WIDTH = 81;
	private final static int BOX_TOP_HEIGHT = 89;
	private final static int BOX_BOTTOM_HEIGHT = 96;
	
	private final static int REFERENCE_WIDTH = 320;
	private final static int REFERENCE_HEIGHT = 270;
	
	private final static Paint PAINT = new Paint();
	
	private final static boolean HORIZ = true;
	private final static boolean VERT = false;
	
	private IGestureListener mListener;
	private GestureRemoteCursor mCursor;
	private Point mDelta = new Point(0, 0);
	private Point mOrigin = mDelta;
	private boolean mIsDragging = false;
	private boolean mIsScrolling = false;
	
	private boolean mLastDirection = HORIZ;
	private int mLastZone = 0;
	private boolean mMoved = false;
	private double mLastScrollValue = 0.0;
	
	private final Bitmap mGestureOverlay;
	
	private final Bitmap mBorderRight;
	private final Bitmap mBorderLeft;
	private final Rect mBorderRightRect;
	private final Rect mBorderLeftRect;
	
	private final Rect mScrollerRect;
	private final Rect mGestureRect;

	private final Rect mTitleRect;
	private final Rect mInfoRect;
	private final Rect mMenuRect;
	private final Rect mBackRect;
	
	private int mMaxPosX = 0;
	private int mMaxPosY = 0;
	
	private final Point mCursorDim;
	private double[] mZones;
	
	private double mScaleWidth = 1;
	private double mScaleHeight = 1;
	
	private int mWidth = REFERENCE_WIDTH;
	private int mHeight = REFERENCE_HEIGHT;

	private int mCurrentButtonPressed = 0;

	public GestureRemoteView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFocusable(true); // necessary for getting the touch events
		mCursor = new GestureRemoteCursor(context, R.drawable.remote_gest_cursor, new Point(50, 20));
		mCursorDim = mCursor.getBitmapDimensions();
		setBackgroundResource(R.drawable.remote_xbox_gesturezone);
		mBorderLeft = BitmapFactory.decodeResource(getResources(), R.drawable.remote_gest_border_left);
		mBorderRight = BitmapFactory.decodeResource(getResources(), R.drawable.remote_gest_border_right);
		mGestureOverlay = BitmapFactory.decodeResource(getResources(), R.drawable.remote_xbox_gesturezone_dim);
		mBorderRightRect = new Rect();
		mBorderLeftRect = new Rect();
		mScrollerRect = new Rect();
		mGestureRect = new Rect();
		
		final int leftPosOfRightBox = REFERENCE_WIDTH - SCROLL_ZONE_WIDTH - BOX_RIGHT_WIDTH;
		final int rightPosOfRightBox = REFERENCE_WIDTH - SCROLL_ZONE_WIDTH;
		final int topPosOfBottomBox = REFERENCE_HEIGHT - BOX_BOTTOM_HEIGHT;
		
		mTitleRect = new Rect(0, 0, (int)(BOX_LEFT_WIDTH), (int)(BOX_TOP_HEIGHT));
		mInfoRect = new Rect((int)(leftPosOfRightBox), 0, (int)(rightPosOfRightBox), (int)(BOX_TOP_HEIGHT));
		mMenuRect = new Rect(0, (int)(topPosOfBottomBox), (int)(BOX_LEFT_WIDTH), (int)(REFERENCE_HEIGHT));
		mBackRect = new Rect((int)(leftPosOfRightBox), (int)(topPosOfBottomBox), (int)(rightPosOfRightBox), (int)(REFERENCE_HEIGHT));
	}
	
	public void setGestureListener(IGestureListener listener) {
		mListener = listener;
		mZones = listener.getZones();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		mWidth = MeasureSpec.getSize(widthMeasureSpec);
		mHeight = MeasureSpec.getSize(heightMeasureSpec);
		
		final double scaleHeight = (double)mHeight / (double)REFERENCE_HEIGHT;
		final double scaleWidth = (double)mWidth / (double)REFERENCE_WIDTH;

		setMeasuredDimension(mWidth, mHeight);
		
		if (scaleHeight != 1 || scaleWidth != 1) {
			
			Log.d(TAG, "Non-reference screen size detected. Scale width = " + scaleWidth + ", scale height = " + scaleHeight);
			
			final int leftPosOfRightBox = REFERENCE_WIDTH - SCROLL_ZONE_WIDTH - BOX_RIGHT_WIDTH;
			final int rightPosOfRightBox = REFERENCE_WIDTH - SCROLL_ZONE_WIDTH;
			final int topPosOfBottomBox = REFERENCE_HEIGHT - BOX_BOTTOM_HEIGHT;
			
			mTitleRect.right = (int)(BOX_LEFT_WIDTH * scaleWidth);
			mTitleRect.bottom = (int)(BOX_TOP_HEIGHT * scaleHeight);
			mInfoRect.left = (int)(leftPosOfRightBox * scaleWidth);
			mInfoRect.right = (int)(rightPosOfRightBox * scaleWidth);
			mInfoRect.bottom = (int)(BOX_TOP_HEIGHT * scaleHeight);
			mMenuRect.top = (int)(topPosOfBottomBox * scaleHeight);
			mMenuRect.right = (int)(BOX_LEFT_WIDTH * scaleWidth);
			mMenuRect.bottom = (int)(REFERENCE_HEIGHT * scaleHeight);
			mBackRect.left = (int)(leftPosOfRightBox * scaleWidth);
			mBackRect.top = (int)(topPosOfBottomBox * scaleHeight);
			mBackRect.right = (int)(rightPosOfRightBox * scaleWidth);
			mBackRect.bottom = (int)(REFERENCE_HEIGHT * scaleHeight);
		}
		mScaleWidth = scaleWidth;
		mScaleHeight = scaleHeight;
	}
	     
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mOrigin = new Point(
				LEFT_BORDER_WIDTH + (w - mCursorDim.x - SCROLL_ZONE_WIDTH - LEFT_BORDER_WIDTH) / 2, 
				TOP_BORDER_HEIGHT + (h - mCursorDim.y - TOP_BORDER_HEIGHT - BOTTOM_BORDER_HEIGHT) / 2);
		mCursor.setPosition(mOrigin);
		
		final double scaleWidth = mScaleWidth;
		final double scaleHeight = mScaleHeight;
		
		// define gesture rectangle
		mGestureRect.left = (int)(LEFT_BORDER_WIDTH * scaleWidth);
		mGestureRect.top = (int)(TOP_BORDER_HEIGHT * scaleHeight);
		mGestureRect.right = (int)(w - SCROLL_ZONE_WIDTH * scaleWidth);
		mGestureRect.bottom = (int)(h - BOTTOM_BORDER_HEIGHT * scaleHeight);
		
		// define fast scroller rectangle
		mScrollerRect.left = (int)(w - SCROLL_ZONE_WIDTH * scaleWidth);
		mScrollerRect.top = (int)(TOP_BORDER_HEIGHT * scaleHeight);
		mScrollerRect.right = w;
		mScrollerRect.bottom = (int)(h - BOTTOM_BORDER_HEIGHT * scaleHeight);

		final int lPad = (int)(22 * scaleWidth);
		final int tPad = (int)(-3 * scaleHeight);
		final int rPad = (int)(21 * scaleWidth);
		mBorderLeftRect.left = lPad;
		mBorderLeftRect.top = (h - mBorderLeft.getHeight()) / 2 + tPad;
		mBorderLeftRect.right = mBorderLeft.getWidth() + lPad;
		mBorderLeftRect.bottom = (h + mBorderLeft.getHeight()) / 2 + tPad;

		mBorderRightRect.left = w - mBorderRight.getWidth() - rPad;
		mBorderRightRect.top = (h - mBorderRight.getHeight()) / 2 + tPad;
		mBorderRightRect.right = w - rPad;
		mBorderRightRect.bottom = (h + mBorderRight.getHeight()) / 2 + tPad;
		
		mMaxPosX = (mGestureRect.width() - mCursorDim.x) / 2 + CURSOR_NEG_PADDING;
		mMaxPosY = (mGestureRect.height() - mCursorDim.y) / 2 + CURSOR_NEG_PADDING;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		switch (mCurrentButtonPressed) {
			case R.drawable.remote_xbox_gesture_title_down:
				canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), mCurrentButtonPressed), null, mTitleRect, null);
				break;
			case R.drawable.remote_xbox_gesture_menu_down:
				canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), mCurrentButtonPressed), null, mMenuRect, null);
				break;
			case R.drawable.remote_xbox_gesture_info_down:
				canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), mCurrentButtonPressed), null, mInfoRect, null);
				break;
			case R.drawable.remote_xbox_gesture_back_down:
				canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), mCurrentButtonPressed), null, mBackRect, null);
				break;
		}
		if (mCursor.backgroundFadePos > 0) {
			PAINT.setAlpha(mCursor.backgroundFadePos);
			canvas.drawBitmap(mGestureOverlay, null, new Rect(0, 0, mWidth, mHeight), PAINT);
		}
		canvas.drawBitmap(mCursor.getBitmap(), mCursor.getX(), mCursor.getY(), null);
		drawZones(canvas);
	}
	
	/**
	 * Paints the gesture zone, scrolling zone, acceleration zones
	 * with different colors. For debug purposes only.
	 * @param canvas
	 */
	private void drawZones(Canvas canvas) {
		final boolean drawZones = false;
		final boolean drawRects = false;
		if (drawRects) {
			PAINT.setColor(Color.BLUE);
			PAINT.setAlpha(50);
			canvas.drawRect(mScrollerRect, PAINT);
			PAINT.setColor(Color.RED);
			PAINT.setAlpha(50);
			canvas.drawRect(mGestureRect, PAINT);

			PAINT.setColor(Color.MAGENTA);
			PAINT.setAlpha(50);
			canvas.drawRect(mTitleRect, PAINT);
			PAINT.setColor(Color.CYAN);
			PAINT.setAlpha(50);
			canvas.drawRect(mInfoRect, PAINT);
			PAINT.setColor(Color.YELLOW);
			PAINT.setAlpha(50);
			canvas.drawRect(mMenuRect, PAINT);
			PAINT.setColor(Color.BLUE);
			PAINT.setAlpha(50);
			canvas.drawRect(mBackRect, PAINT);
		}
		
		if (drawZones) {
			for (int x = mGestureRect.left; x < mGestureRect.right + SCROLL_ZONE_WIDTH - RIGHT_BORDER_WIDTH; x++) {
				for (int y = mGestureRect.top; y < mGestureRect.bottom; y++) {
					Point centered = getCenteredPos(new Point(x - mCursorDim.x / 2, y - mCursorDim.y / 2));
					final int zone;
					if (Math.abs(centered.x) > Math.abs(centered.y)) {
						// horizontally moving
						final double pos = (double)centered.x / (double)mMaxPosX;
						zone = findZone(pos);
						PAINT.setColor(Color.MAGENTA);
					} else {
						// vertically moving
						final double pos = (double)centered.y / (double)mMaxPosY;
						zone = findZone(pos);
						PAINT.setColor(Color.CYAN);
					}				
					PAINT.setAlpha(10 * (Math.abs(zone) + 1));
					canvas.drawPoint(x, y, PAINT);
				}
			}
		}
	}
	
	/**
	 * Invalidates one of the four option buttons.
	 * @param buttonResId Button ID
	 */
	private void invalidateButton(int buttonResId) {
		switch (buttonResId) {
			case R.drawable.remote_xbox_gesture_title_down:
				invalidate(mTitleRect);
				break;
			case R.drawable.remote_xbox_gesture_menu_down:
				invalidate(mMenuRect);
				break;
			case R.drawable.remote_xbox_gesture_info_down:
				invalidate(mInfoRect);
				break;
			case R.drawable.remote_xbox_gesture_back_down:
				invalidate(mBackRect);
				break;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int eventaction = event.getAction();
		int x = (int)event.getX();
		int y = (int)event.getY();
		
		switch (eventaction) {
			/*
			 * Cursor is PRESSED
			 */
			case MotionEvent.ACTION_DOWN:
				final int cursorX = mCursor.getX();
				final int cursorY = mCursor.getY();
				mIsDragging = x + CURSOR_POS_PADDING > cursorX && x - CURSOR_POS_PADDING < cursorX + mCursorDim.x && y + CURSOR_POS_PADDING > cursorY && y - CURSOR_POS_PADDING < cursorY + mCursorDim.y;
				if (mScrollerRect.contains(x, y)) { // tapping on scroll bar?
					final Point target = getScrollTarget(x - mCursorDim.x / 2, y - mCursorDim.y / 2);
					GestureRemoteAnimation anim = new GestureRemoteAnimation(target, mCursor);
					anim.setDuration(ANIMATION_DURATION);
					anim.setFadeOut(true);
					startAnimation(anim);
					mIsScrolling = true;
					mDelta = new Point(mCursorDim.x / 2, mCursorDim.y / 2);
					if (mListener != null && target.y > mOrigin.y) {
						mListener.onScrollDown();
					} else if (mListener != null && target.y < mOrigin.y) {
						mListener.onScrollUp();
					}
//					invalidate(mGestureRect);
				} else if (mTitleRect.contains(x, y)) {
					mCurrentButtonPressed = R.drawable.remote_xbox_gesture_title_down;
					invalidateButton(R.drawable.remote_xbox_gesture_title_down);
					if (mListener != null) {
						mListener.onTitle();
					}
				} else if (mMenuRect.contains(x, y)) {
					mCurrentButtonPressed = R.drawable.remote_xbox_gesture_menu_down;
					invalidateButton(R.drawable.remote_xbox_gesture_menu_down);
					if (mListener != null) {
						mListener.onMenu();
					}
				} else if (mInfoRect.contains(x, y)) {
					mCurrentButtonPressed = R.drawable.remote_xbox_gesture_info_down;
					invalidateButton(R.drawable.remote_xbox_gesture_info_down);
					if (mListener != null) {
						mListener.onInfo();
					}
				} else if (mBackRect.contains(x, y)) {
					mCurrentButtonPressed = R.drawable.remote_xbox_gesture_back_down;
					invalidateButton(R.drawable.remote_xbox_gesture_back_down);
					if (mListener != null) {
						mListener.onBack();
					}
				} else {
					mDelta = new Point(x - cursorX, y - cursorY);
					mIsScrolling = false;
					mLastZone = 0;
				}
				mMoved = false;
				break;
				
			/*
			 * Cursor is MOVED
			 */
			case MotionEvent.ACTION_MOVE: 
				// DRAGGING
				if (mIsDragging) {
					final Point from = mCursor.getPosition();
					x -= mDelta.x;
					y -= mDelta.y;
					Point target = getGestureTarget(x, y);
					mCursor.setPosition(target);
					
					// calculate redraw aera
					invalidate(mCursor.getDirty(from));
					
					if (mListener != null) {
						Point centered = getCenteredPos(target);
						if (Math.abs(centered.x) > Math.abs(centered.y)) {
							// horizontally moving
							final double pos = (double)centered.x / (double)mMaxPosX;
							final int zone = findZone(pos);
							if (zone != mLastZone || (mLastDirection != HORIZ && zone != 0)) {
								mListener.onHorizontalMove(zone);
								mLastZone = zone;
								mLastDirection = HORIZ;
								if (zone != 0) {
									mMoved = true;
								}
							}
						} else {
							// vertically moving
							final double pos = (double)centered.y / (double)mMaxPosY;
							final int zone = findZone(pos);
							if (zone != mLastZone || (mLastDirection != VERT && zone != 0)) {
								Log.d("GestureRemoteView", "launching onVerticalMove() with pos = " + pos);
								mListener.onVerticalMove(zone);
								mLastZone = zone;
								mLastDirection = VERT;
								if (zone != 0) {
									mMoved = true;
								}
							}
						}
					}
				// SCROLLING
				} else if (mIsScrolling) {
					final Point from = mCursor.getPosition();
					x -= mDelta.x;
					y -= mDelta.y;
					Point target = getScrollTarget(x, y);
					mCursor.setPosition(target);
					
					// calculate redraw aera
					invalidate(mCursor.getDirty(from));

					if (mListener != null) {
						Point centered = getCenteredPos(target);
						final double pos = (double)centered.y / (double)mMaxPosY;
						if (pos != mLastScrollValue) {
							if (pos > 0) {
								if (mLastScrollValue > 0 != pos > 0 && mMoved) { // direction changed?
									mListener.onScrollUp(0);
								}
								if (pos != 0) {
									mListener.onScrollDown(pos);
								}
							} else {
								if (mLastScrollValue > 0 != pos > 0 && mMoved) { // direction changed?
									mListener.onScrollDown(0);
								}
								if (pos != 0) {
									mListener.onScrollUp(-pos);
								}
							}
							mLastScrollValue = pos;
						}
					}
					mMoved = true;
				}
				break;
				
			/*
			 * Cursor is RELEASED
			 */
			case MotionEvent.ACTION_UP:
				if (mIsDragging || mIsScrolling) {
					if (mListener != null && mIsDragging) {
						if (mLastZone == 0 && !mMoved) {
							mListener.onSelect();
						} else {
							if (mLastZone != 0 && mLastDirection == HORIZ) {
								mListener.onHorizontalMove(0);
							} else if (mLastZone != 0 && mLastDirection == VERT) {
								mListener.onVerticalMove(0);
							}
						}
						mLastZone = 0;
					}
					if (mListener != null && mIsScrolling) {
						if (mLastScrollValue > 0 && mMoved) {
							mListener.onScrollDown(0);
						} else if (mLastScrollValue < 0 && mMoved) {
							mListener.onScrollUp(0);
						}
					}
					GestureRemoteAnimation anim = new GestureRemoteAnimation(mOrigin, mCursor);
					anim.setDuration(ANIMATION_DURATION);
					if (mIsScrolling) {
						anim.setFadeIn(true);
					}
					this.startAnimation(anim);
					mIsDragging = false;
					mIsScrolling = false;
				}
				if (mCurrentButtonPressed != 0) {
					final int clearupBtn = mCurrentButtonPressed;
					mCurrentButtonPressed = 0;
					invalidateButton(clearupBtn);
				}
				break;
		}
		return true;
	}
	
	/**
	 * Returns the point where the cursor is finally rendered. It basically
	 * crops the movement to the predefined boundaries.
	 * @param x X-value where user dragged the cursor
	 * @param y Y-value where user dragged the cursor
	 * @return Point where cursor is finally rendered.
	 */
	private Point getGestureTarget(int x, int y) {
		int targetX, targetY;
		Rect rect = mGestureRect;
		if (x >= rect.left - CURSOR_NEG_PADDING) {
			if (x + mCursorDim.x < rect.right + CURSOR_NEG_PADDING + (SCROLL_ZONE_WIDTH - RIGHT_BORDER_WIDTH)) {
				targetX = x;
			} else {
				targetX = rect.right - mCursorDim.x + CURSOR_NEG_PADDING + (SCROLL_ZONE_WIDTH - RIGHT_BORDER_WIDTH);
			}
		} else {
			targetX = rect.left - CURSOR_NEG_PADDING;
		}
		if (y >= rect.top - CURSOR_NEG_PADDING) {
			if (y + mCursorDim.y < rect.bottom + CURSOR_NEG_PADDING) {
				targetY = y;
			} else {
				targetY = rect.bottom - mCursorDim.y + CURSOR_NEG_PADDING;
			}
		} else {
			targetY = rect.top - CURSOR_NEG_PADDING;
		}
		return new Point(targetX, targetY);
	}
	
	private Point getScrollTarget(int x, int y) {
		int targetX, targetY;
		Rect rect = mScrollerRect;
		// fix X and move only Y
		targetX = rect.left - CURSOR_NEG_PADDING;
		if (y >= rect.top - CURSOR_NEG_PADDING) {
			if (y + mCursorDim.y < rect.bottom + CURSOR_NEG_PADDING) {
				targetY = y;
			} else {
				targetY = rect.bottom - mCursorDim.y + CURSOR_NEG_PADDING;
			}
		} else {
			targetY = rect.top - CURSOR_NEG_PADDING;
		}
		return new Point(targetX, targetY);
	}
	
	/**
	 * Converts coordinates from Android's Origin (top left of the view) to 
	 * the center of the screen.
	 * @param from
	 * @return
	 */
	private Point getCenteredPos(Point from) {
		return new Point(
				from.x - mGestureRect.left - (mGestureRect.width() - mCursorDim.x) / 2,
				from.y - mGestureRect.top - (mGestureRect.height() - mCursorDim.y) / 2);
	}
	
	/**
	 * Returns the index of the zone defined for a position.
	 * @param pos
	 * @return
	 */
	private int findZone(double pos) {
		for (int i = 0; i < mZones.length; i++) {
			if (Math.abs(pos) <= mZones[i]) {
				return (pos > 0 ? 1 : - 1) * i;
			}
		}
		return (pos > 0 ? 1 : - 1) * mZones.length;
	}
}
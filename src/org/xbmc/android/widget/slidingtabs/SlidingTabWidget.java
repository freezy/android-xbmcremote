/*
 *      Copyright (C) 2005-2009 Team XBMC
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

package org.xbmc.android.widget.slidingtabs;

import org.xbmc.android.remote.R;
import org.xbmc.android.widget.slidingtabs.SlidingTabHost.SlidingTabSpec;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

/**
 * Displays the sliding tabs.
 * 
 * Displays a list of icons representing each page in the parent's tab
 * collection. The container object for this widget is
 * {@link org.xbmc.android.widget.slidingtabs.SlidingTabHost SlidingTabHost}. 
 * When the user selects a tab, this object sends a message to the parent 
 * container, SlidingTabHost, to tell it to switch the displayed page. You
 * typically won't use many methods directly on this object. The container 
 * SlidingTabHost is used to add labels, add the callback handler, and manage
 * callbacks. 
 * 
 * @author Team XBMC
 */
public class SlidingTabWidget extends LinearLayout {
	
	private final static int SCROLLBAR_HEIGHT = 42;
	private final static int SHADOW_PADDING = 18;
	
	private int mSeparationWidth;
	
	protected Context mContext;

	private OnTabSelectionChanged mSelectionChangedListener;
	private int mSelectedTab = 0;
	
	private final LinearLayout mOuterLayout;
	private final ImageButton mSlider;
	private final LinearLayout mInverseSliderBackground;
    private final LinearLayout mOverlayLayout;
    
    private final ImageView mOverlayIcon;
    private final TextView mOverlayText;
	
	private final SnapAnimation mSnapAnimation;
	
	private FrameLayout mTabContent;
	
	private int mNumTabs = 0;
	private int mInverseSliderWidth = 0;
	
	private int mSliderMoveWidth = 0;
	private int mBackMoveWidth = 0;
	float mBackMoveFactor = 0;

	
	public SlidingTabWidget(Context context) {
		this(context, null);
		mContext = context;
	}

	public SlidingTabWidget(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.slidingTabWidgetStyle);
		mContext = context;
	}

	public SlidingTabWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		
		mContext = context;
		setOrientation(LinearLayout.HORIZONTAL);
		
		// inflate widget layout from xml
		inflate(mContext, R.layout.slidingtab_widget, this);
		
        // set all the view references 
		mOuterLayout = (LinearLayout)findViewById(R.id.SlidingTabLinearLayoutOuter);
        mSlider = (ImageButton)findViewById(R.id.SlidingTabImageButtonSlider);
        mInverseSliderBackground = (LinearLayout)findViewById(R.id.SlidingTabLinearLayoutBackslider);
        
        mSnapAnimation = new SnapAnimation(mSlider, mInverseSliderBackground);
        mSlider.setOnTouchListener(new SliderOnTouchListener());
        
		// inflate and prepare the overlay
        final LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mOverlayLayout = (LinearLayout) inflater.inflate(R.layout.slidingtab_overlay, mTabContent, false);
		mOverlayIcon = (ImageView)mOverlayLayout.findViewById(R.id.slidingtab_overlay_image);
		mOverlayText = (TextView)mOverlayLayout.findViewById(R.id.slidingtab_overlay_label);
		
		// Deal with focus, as we don't want the focus to go by default
		// to a tab other than the current tab
		setFocusable(true);
//		setOnFocusChangeListener(this);
		
		setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
			
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_LEFT:
							if (mSelectedTab > 0) {
								snapTo(mSelectedTab - 1);
								setCurrentTab(mSelectedTab - 1);
							}
							return true;
						case KeyEvent.KEYCODE_DPAD_RIGHT:
							if (mSelectedTab < mNumTabs + 1) {
								snapTo(mSelectedTab + 1);
								setCurrentTab(mSelectedTab + 1);
							}
							return true;
					}
				} else {
					return true;
				}
				mTabContent.requestFocus(View.FOCUS_FORWARD);
				return mTabContent.dispatchKeyEvent(event);
			}
		});

		// apply the styled attributes
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlidingTabWidget, defStyle, 0);
		mSeparationWidth = a.getInt(R.styleable.SlidingTabWidget_separationWidth, 0);
		if (mSeparationWidth == 0) {
			mSeparationWidth = 75;
		}
		a.recycle();
	}

	/**
	 * Adds a new tab and sets the correct paddings defined by the
	 * separation width.
	 * @param newTab
	 */
	void addTab(SlidingTabSpec newTab) {
		
		BackgroundImage bgImg = new BackgroundImage(mContext, mNumTabs, newTab);
		
		final int drawableWidth = bgImg.getDrawable().getIntrinsicWidth();
		final int drawableHeight = bgImg.getDrawable().getIntrinsicHeight();
		final int hPadding = Math.round((float)(mSeparationWidth - drawableWidth) / 2.0f);
		final int vPadding = Math.round((float)(SCROLLBAR_HEIGHT - drawableHeight) / 2.0f); 
		
		bgImg.setOverlayIconResource(newTab.getBigIconResource());
		
		final int sliderWidth = mSlider.getBackground().getIntrinsicWidth() - 2 * SHADOW_PADDING;
		final int borderVPadding = (int) Math.floor((float)(sliderWidth - drawableWidth) / 2.0f);
		
		if (mNumTabs == 0) {
			bgImg.setPadding(borderVPadding, vPadding, hPadding, 0);
			mSlider.setImageResource(newTab.getActiveIconResource());
			mInverseSliderWidth += (borderVPadding + drawableWidth + hPadding);
		} else {
			if (mNumTabs > 1) {
				BackgroundImage lastTab = (BackgroundImage)mInverseSliderBackground.getChildAt(mNumTabs - 1);
				mInverseSliderWidth += (lastTab.getPaddingLeft() - lastTab.getPaddingRight());
				lastTab.setPadding(lastTab.getPaddingLeft(), lastTab.getPaddingTop(), lastTab.getPaddingLeft(), 0);
			}
			mInverseSliderWidth += (hPadding + drawableWidth + borderVPadding);
			bgImg.setPadding(hPadding, vPadding, borderVPadding, 0);
		}
		
		if (bgImg.getLayoutParams() == null) {
			final LinearLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
			lp.setMargins(0, 0, 0, 0);
			bgImg.setLayoutParams(lp);
		}
		

		// ensure you can navigate to the tab with the keyboard, and you can touch it
		bgImg.setFocusable(false);
		bgImg.setClickable(true);
		// make the buttons accessible by click directly.
		bgImg.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				updateLayoutDimensions();
				final BackgroundImage bgImg = (BackgroundImage)v;
				if (bgImg.getTabIndex() != mSelectedTab) {
					mSlider.setImageResource(bgImg.getSliderIconResource());
					snapTo(bgImg.getTabIndex());
					setCurrentTab(bgImg.getTabIndex());
				}
			}
		});
		
		mInverseSliderBackground.addView(bgImg);
		mNumTabs++;
		
/*		StringBuilder sb = new StringBuilder();
		int total = 0;
		for (int i = 0; i < mNumTabs; i++) {
			BackgroundImage img = (BackgroundImage)mInverseSliderBackground.getChildAt(i);
			total += img.getPaddingLeft();
			total += img.getDrawable().getIntrinsicWidth();
			total += img.getPaddingRight();
			sb.append("[" + img.getPaddingLeft() + "|" + img.getDrawable().getIntrinsicWidth() + "|" + img.getPaddingRight() + "] ");
		}
		mInverseSliderBackground.invalidate();
		Log.i("padding", total + ": " + sb.toString() + "(" + mInverseSliderWidth + ")");
*/
	}
	
	/**
	 * Automatically animates the slider (and background) to given tabindex.
	 * @param tabIndex
	 */
	void moveTo(int tabIndex) {
		updateLayoutDimensions();
		final BackgroundImage bgImg = (BackgroundImage)getChildTabViewAt(tabIndex);
		if (bgImg != null && tabIndex != mSelectedTab) {
			mSlider.setImageResource(bgImg.getSliderIconResource());
			snapTo(bgImg.getTabIndex());
			setCurrentTab(bgImg.getTabIndex());
		}
	}
	
	/**
	 * Moves the slider to a position
	 * @param pos Absolute position of outer-left edge of the slider (0 = left aligned)
	 */
	private void moveSlider(int pos) {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(pos, 0, 0, 0);
		mSlider.setLayoutParams(params);
	}
	
	/**
	 * Moves the background to a position. Won't move if all icons already visible.
	 * @param pos Relative position of all background icons.
	 */
	private void moveBackground(int pos) {
		// only move background if there are more icons than visible
		if (pos != 0 && mBackMoveFactor > 0) {
			mInverseSliderBackground.offsetLeftAndRight(pos);
			mInverseSliderBackground.invalidate();
		}
	}

	
	/**
	 * Moves the slider (and the background) to the correct position. Note that
	 * the actual content switch is not done in here and must be manually called:
	 * <pre>mSelectionChangedListener.onTabSelectionChanged(mCurrTabIndex, true);</pre>
	 * @param tabIndex Tab index (first tab = 0)
	 */
	private void snapTo(int tabIndex) {
		if (mBackMoveFactor > 0) { // background scrolls
			final float snapWidth; 
			if (mNumTabs > 1) {
				snapWidth = (float)mSliderMoveWidth / (float)(mNumTabs - 1);
			} else {
				snapWidth = mSliderMoveWidth;
			}
			final int snapPos = Math.round(tabIndex * snapWidth);
			final int backSnapPos =  -(int)Math.round((snapPos * mBackMoveFactor));
//			Log.i("snap", "Snapping to " + index + "(" + snapPos + " / " + backSnapPos + ")");
			mSnapAnimation.init(snapPos, backSnapPos);
		} else { // background doesn't scroll
			final BackgroundImage bgImg = (BackgroundImage)mInverseSliderBackground.getChildAt(tabIndex);
			final int snapPos = bgImg.getCenteredPosition() - Math.round((float)mSlider.getBackground().getIntrinsicWidth() / 2.0f) + SHADOW_PADDING;
			mSnapAnimation.init(snapPos, SnapAnimation.NO_ANIMATION);
		}
		mSnapAnimation.setDuration(100);
		mSlider.startAnimation(mSnapAnimation);
	}
	
	/**
	 * Updates layout dimensions. This should be done only once, though I 
	 * haven't found out yet how. Should be called when the layout is rendered.
	 */
	private void updateLayoutDimensions() {
		if (mSliderMoveWidth == 0) {
			mSliderMoveWidth = mOuterLayout.getWidth() - mSlider.getBackground().getIntrinsicWidth() + (2 * SHADOW_PADDING);
		}
		if (mBackMoveWidth == 0) {
			mBackMoveWidth = mInverseSliderWidth - mOuterLayout.getWidth() + mOuterLayout.getPaddingRight() + mOuterLayout.getPaddingLeft();// - (2 * SHADOW_PADDING);
		}
		if (mBackMoveFactor == 0) {
			mBackMoveFactor = (float)mBackMoveWidth / (float)mSliderMoveWidth;
		}
	}


	/**
	 * Returns background ImageButton at the given index.
	 * @param index The zero-based index of the tab indicator view to return
	 * @return the tab indicator view at the given index
	 */
	public View getChildTabViewAt(int index) {
		return mInverseSliderBackground.getChildAt(index);
	}

	/**
	 * Sets the reference to the tab content layer (needed for the overlay). 
	 * @param layout
	 */
	void setTabContent(FrameLayout layout) {
		mTabContent = layout;
	}
	
	/**
	 * Returns the number of tab indicator views.
	 * @return the number of tab indicator views.
	 */
	public int getTabCount() {
		return mInverseSliderBackground.getChildCount();
	}


	/**
	 * Sets the current tab.
	 * 
	 * Note that it doesn't move the slider, this must be done separately.
	 * However, it does switch the content.
	 * 
	 * @param tabIndex Which tab is going to be selected 
	 */
	public void setCurrentTab(int tabIndex) {
		if (tabIndex < 0 || tabIndex >= getTabCount()) {
			return;
		}
		mSelectionChangedListener.onTabSelectionChanged(tabIndex, true);
		mSelectedTab = tabIndex;
	}

	/**
	 * Sets the current tab and focuses the UI on it. This method makes sure
	 * that the focused tab matches the selected tab, normally at
	 * {@link #setCurrentTab}. Normally this would not be an issue if we go
	 * through the UI, since the UI is responsible for calling
	 * TabWidget.onFocusChanged(), but in the case where we are selecting the
	 * tab programmatically, we'll need to make sure focus keeps up.
	 * 
	 * @param index
	 *            The tab that you want focused (highlighted in orange) and
	 *            selected (tab brought to the front of the widget)
	 * 
	 * @see #setCurrentTab
	 */
	public void focusCurrentTab2(int index) {
		final int oldTab = mSelectedTab;

		// set the tab
		setCurrentTab(index);

		// change the focus if applicable.
		if (oldTab != index) {
//			getChildTabViewAt(index).requestFocus();
		}
	}

/*	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		int count = getTabCount();

		for (int i = 0; i < count; i++) {
			View child = getChildTabViewAt(i);
			child.setEnabled(enabled);
		}
	}*/
	
	
	/**
	 * Takes care of all the sliding stuff. 
	 */
	private class SliderOnTouchListener implements View.OnTouchListener {
		
    	private int mClickPos = 0;
        private int mCurrTabIndex = -1;
        private boolean mIsMoving = false;
        
		public boolean onTouch(View v, MotionEvent event) {
			
			updateLayoutDimensions();
			
			final int sliderRelPos = (int)event.getX() - mClickPos;
			final int sliderAbsPos = mSlider.getLeft() + sliderRelPos;
			
			final int bgAbsPos =  -(int)Math.round(((sliderAbsPos) * mBackMoveFactor));
			final int bgRelPos = bgAbsPos - mInverseSliderBackground.getLeft();
			
			switch (event.getAction()) {
			
				case MotionEvent.ACTION_DOWN:
					mClickPos = (int)event.getX();
					break;
					
				case MotionEvent.ACTION_UP:
					if (mIsMoving) {
						// remove overlay
						mTabContent.removeView(mOverlayLayout);
						
						// snap to selected position and change the tab contents
						snapTo(mCurrTabIndex);
						setCurrentTab(mCurrTabIndex);
						mIsMoving = false;
					}
					break;
					
				case MotionEvent.ACTION_MOVE:
					
					if (!mIsMoving) {
						// add overlay
						mTabContent.addView(mOverlayLayout);
					}
					mIsMoving = true;

					// only move within boundaries
					if (sliderAbsPos >= mOuterLayout.getLeft() + mOuterLayout.getPaddingLeft() &&
						sliderAbsPos <= mOuterLayout.getWidth() - mOuterLayout.getPaddingRight() - mSlider.getWidth() + (2 * SHADOW_PADDING)) {

						moveSlider(sliderAbsPos);
						moveBackground(bgRelPos);
						
						// find out nearest background icon
						final int slPos = mSlider.getLeft() + (int)Math.round((float)mSlider.getWidth() / 2.0f) - SHADOW_PADDING;
						int minVal = -1;
						BackgroundImage nearestTab = null;
						for (int i = 0; i < mNumTabs; i++) { // TODO some basic optimization wouldn't hurt.
							final BackgroundImage bgTab = (BackgroundImage)mInverseSliderBackground.getChildAt(i);
							final int bgPos = mInverseSliderBackground.getLeft() + bgTab.getCenteredPosition();
							if (Math.abs(bgPos - slPos) < minVal || minVal == -1) {
								minVal = Math.abs(bgPos - slPos);
								nearestTab = bgTab;
							}
						}
						// update overlay icons, text and slider icon on changes
						if (mCurrTabIndex == -1 || mCurrTabIndex != nearestTab.getTabIndex()) {
							mSlider.setImageResource(nearestTab.getSliderIconResource());
							mOverlayText.setText(nearestTab.getLabel());
							mOverlayIcon.setImageResource(nearestTab.getOverlayIconResource());
							mCurrTabIndex = nearestTab.getTabIndex();
						}
					}
				break;
			}
			return true;
		}
	}
	
	/**
	 * Used for the background icons. They additionally hold some resource
	 * references for direct access.
	 */
	private class BackgroundImage extends ImageButton {
		private final int mSliderIconResource;
		private final int mTabIndex;
		private final String mLabel; 
		private int mOverlayIconResource;
		public BackgroundImage(Context context, int tabIndex, SlidingTabSpec specs) {
			super(context);
			mTabIndex = tabIndex;
			mSliderIconResource = specs.getActiveIconResource();
			mLabel = specs.getLabel();
			setImageResource(specs.getInactiveIconResource());
			setBackgroundDrawable(null);
		}
		public void setOverlayIconResource(int res) {
			mOverlayIconResource = res;
		}
		public int getOverlayIconResource() {
			return mOverlayIconResource;
		}
		public int getSliderIconResource() {
			return mSliderIconResource;
		}
		public int getTabIndex() {
			return mTabIndex;
		}
		public String getLabel() {
			return mLabel;
		}
		public int getCenteredPosition() {
			return getLeft() + getPaddingLeft() + Math.round((float)getDrawable().getIntrinsicWidth() / 2.0f); 			
		}
	}

	
	/**
	 * Provides a way for {@link SlidingTabHost} to be notified that the user clicked
	 * on a tab indicator.
	 */
	void setTabSelectionListener(OnTabSelectionChanged listener) {
		mSelectionChangedListener = listener;
	}
	


	public void onFocusChange2(View v, boolean hasFocus) {
		if (v == this && hasFocus) {
			getChildTabViewAt(mSelectedTab).requestFocus();
			return;
		}

		if (hasFocus) {
			int i = 0;
			int numTabs = getTabCount();
			while (i < numTabs) {
				if (getChildTabViewAt(i) == v) {
					setCurrentTab(i);
					mSelectionChangedListener.onTabSelectionChanged(i, false);
					break;
				}
				i++;
			}
		}
	}

	/**
	 * Let {@link TabHost} know that the user clicked on a tab indicator.
	 */
	static interface OnTabSelectionChanged {
		/**
		 * Informs the TabHost which tab was selected. It also indicates if the
		 * tab was clicked/pressed or just focused into.
		 * 
		 * @param tabIndex
		 *            index of the tab that was selected
		 * @param clicked
		 *            whether the selection changed due to a touch/click or due
		 *            to focus entering the tab through navigation. Pass true if
		 *            it was due to a press/click and false otherwise.
		 */
		void onTabSelectionChanged(int tabIndex, boolean clicked);
	}

}

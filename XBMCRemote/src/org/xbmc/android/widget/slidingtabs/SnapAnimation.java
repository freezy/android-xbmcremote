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

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.LinearLayout;

/**
 * Makes the slider glide to the next tab.
 * 
 * @author Team XBMC
 */
class SnapAnimation extends Animation {
	
	private final ImageButton mSlider;
	private final LinearLayout mBackLayout;
	
	public static int NO_ANIMATION = -99999;
	
	private int mSliderSnapPosition;
	private int mBackSnapPosition;
	private int mInitialMargin;
	private int mInitialBackPosition;

	/**
	 * Constructor. Animation object can be declared final and re-used.
	 * 
	 * @param context Current context
	 * @param attrs   Attribute set when inflated from XML
	 * @param slider  Slider object
	 * @param bl      Background sliding layout
	 */
	public SnapAnimation(Context context, AttributeSet attrs, ImageButton slider, LinearLayout bl) {
		super(context, attrs);
		mBackLayout = bl;
		mSlider = slider;
	}

	/**
	 * Constructor. Animation object can be declared final and re-used.
	 *  
	 * @param slider  Slider object
	 * @param bl      Background sliding layout
	 */
	public SnapAnimation(ImageButton slider, LinearLayout bl) {
		mBackLayout = bl;
		mSlider = slider;
	}
	
	/**
	 * Initialization is needed before starting the application.
	 * @param sliderSnapPos To which position the slider should be moved
	 * @param backSnapPos   To which position the background should be moved
	 */
	public void init(int sliderSnapPos, int backSnapPos) {
		mSliderSnapPosition = sliderSnapPos;
		mBackSnapPosition = backSnapPos;
		mInitialMargin = ((LinearLayout.LayoutParams)mSlider.getLayoutParams()).leftMargin;
		mInitialBackPosition = mBackLayout.getLeft();
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		final int interPadding = mInitialMargin + Math.round((float)(mSliderSnapPosition - mInitialMargin) * interpolatedTime);
		final int interBackPosition = mInitialBackPosition + Math.round((float)(mBackSnapPosition - mInitialBackPosition) * interpolatedTime);
		final int interBackRelPosition = interBackPosition - mBackLayout.getLeft();
		if (interBackRelPosition != 0 && mBackSnapPosition != NO_ANIMATION) {
			mBackLayout.offsetLeftAndRight(interBackRelPosition);
			mBackLayout.invalidate();
		}
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(interPadding, 0, 0, 0);
		mSlider.setLayoutParams(params);
	}
}
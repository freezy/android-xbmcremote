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

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Makes the slider glide to the next tab.
 * 
 * @author Team XBMC
 */
class GestureRemoteAnimation extends Animation {
	
	private final Point mOrigin;
	private final Point mFrom;
	private final GestureRemoteCursor mCursor;
	
	private boolean mGestureZoneFadeOut = false;
	private boolean mGestureZoneFadeIn = false;

	/**
	 * Constructor. Animation object can be declared final and re-used.
	 * 
	 * @param context Current context
	 * @param attrs   Attribute set when inflated from XML
	 * @param origin  Coordinates of screen center
	 * @param cursor  Reference to cursor
	 */
	public GestureRemoteAnimation(Context context, AttributeSet attrs, Point origin, GestureRemoteCursor cursor) {
		super(context, attrs);
		mOrigin = origin;
		mCursor = cursor;
		mFrom = cursor.getPosition();
	}
	
	public void setFadeIn(boolean fadein) {
		mGestureZoneFadeIn = fadein;
	}
	
	public void setFadeOut(boolean fadeout) {
		mGestureZoneFadeOut = fadeout;
	}

	/**
	 * Constructor. Animation object can be declared final and re-used.
	 *  
	 * @param origin  Coordinates of screen center
	 * @param cursor  Reference to cursor
	 */
	public GestureRemoteAnimation(Point origin, GestureRemoteCursor cursor) {
		mOrigin = origin;
		mCursor = cursor;
		mFrom = cursor.getPosition();
	}
	

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		final Point to = new Point(
				mFrom.x - (int)((mFrom.x - mOrigin.x) * interpolatedTime), 
				mFrom.y - (int)((mFrom.y - mOrigin.y) * interpolatedTime)
			); 
		mCursor.setPosition(to);
		if (mGestureZoneFadeOut) {
			mCursor.backgroundFadePos = (int)(interpolatedTime * 255.0);
		}
		if (mGestureZoneFadeIn) {
			mCursor.backgroundFadePos = (int)((1 - interpolatedTime) * 255.0);
		}
		
	}
}
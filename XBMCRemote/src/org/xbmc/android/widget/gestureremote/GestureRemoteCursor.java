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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;

class GestureRemoteCursor {
	
	private Bitmap img; // the image of the ball
	private int coordX = 0; // the x coordinate at the canvas
	private int coordY = 0; // the y coordinate at the canvas
	private int id; // gives every ball his own id, for now not necessary
	private boolean goRight = true;
	private boolean goDown = true;
	
	public int backgroundFadePos = 0;
	
	private final int imgWidth, imgHeight;

	public GestureRemoteCursor(Context context, int drawable) {
		this(context, drawable, new Point(0, 0));
	}

	public GestureRemoteCursor(Context context, int drawable, Point point) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		img = BitmapFactory.decodeResource(context.getResources(), drawable);
		coordX = point.x;
		coordY = point.y;
		imgWidth = img.getWidth();
		imgHeight = img.getHeight();
	}
	
	public Point getBitmapDimensions() {
		return new Point(imgWidth, imgHeight);
	}

	void setX(int newValue) {
		coordX = newValue;
	}

	public int getX() {
		return coordX;
	}

	void setY(int newValue) {
		coordY = newValue;
	}

	public int getY() {
		return coordY;
	}
	
	public void setPosition(Point point) {
		coordX = point.x;
		coordY = point.y;
	}
	
	public Point getPosition() {
		return new Point(coordX, coordY);
	}

	public int getID() {
		return id;
	}

	public Bitmap getBitmap() {
		return img;
	}
	
	public String toString() {
		return "Cursor(" + coordX + ", " + coordY + ")";
	}
	
	/**
	 * Returns the dirty rectangle that needs to be redrawn.
	 * @param from Original position
	 * @return
	 */
	public Rect getDirty(Point from) {
		return new Rect(
				Math.min(coordX, from.x), 
				Math.min(coordY, from.y), 
				Math.max(coordX, from.x) + imgWidth, 
				Math.max(coordY, from.y) + imgHeight
			);
/*		if (coordX > from.x && coordY > from.y) {
			return new Rect(from.x, from.y, coordX + imgWidth, coordY + imgHeight);
		} else if (coordX > from.x && coordY <= from.y) {
			return new Rect(from.x, coordY, coordX + imgWidth, from.y + imgHeight);
		} else if (coordX <= from.x && coordY > from.y) {
			return new Rect(coordX, from.y, from.x + imgWidth, coordY + imgHeight);
		} else {
			return new Rect(coordX, coordY, from.x + imgWidth, from.y + imgHeight);
		}*/
	}

	public void moveBall(int goX, int goY) {
		// check the borders, and set the direction if a border has reached
		if (coordX > 270) {
			goRight = false;
		}
		if (coordX < 0) {
			goRight = true;
		}
		if (coordY > 400) {
			goDown = false;
		}
		if (coordY < 0) {
			goDown = true;
		}
		// move the x and y
		if (goRight) {
			coordX += goX;
		} else {
			coordX -= goX;
		}
		if (goDown) {
			coordY += goY;
		} else {
			coordY -= goY;
		}

	}

}

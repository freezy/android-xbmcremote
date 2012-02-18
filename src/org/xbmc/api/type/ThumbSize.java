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

package org.xbmc.api.type;

import android.content.res.Resources;

/**
 * Defines a thumb size. Sizes are:
 * <ul>
 * 	<li><code>SMALL</code></li>
 * 	<li><code>MEDIUM</code></li>
 * 	<li><code>BIG</code></li>
 * </ul>
 * 
 * @author Team XBMC
 */
public abstract class ThumbSize {
	
	public static final double POSTER_AR = 1.4799154334038054968287526427061;
	public static final double LANDSCAPE_AR = 0.5625;
	public static final double BANNER_AR = 0.1846965699208443;
	public static final double FULL_AR = 0.75;
	
	public static final int SMALL = 1;
	public static final int MEDIUM = 2;
	public static final int BIG = 3;
	public static final int SCREENWIDTH = 4;
	public static final int SCREENHEIGHT = 5;
	
	public static final float PIXEL_SCALE = Resources.getSystem().getDisplayMetrics().density;
	public static float SCREEN_SCALE = 1;
	
	public static int PIXELS_WIDTH = 0;
	public static int PIXELS_HEIGHT = 0;
	
	public static String getDir(int size) {
		switch (size) {
		case SMALL:
			return "/small";
		case MEDIUM:
			return "/medium";
		case BIG:
			return "/original";
		default:
			return "";
		}
	}
	
	public static void setScreenSize(int width, int height) {
		PIXELS_WIDTH = width;
		PIXELS_HEIGHT = height;
		SCREEN_SCALE = (((float)(width < height ? width : height)) / PIXEL_SCALE) / (float)320;
	}
	
	public static int getPixel(int size, boolean fixedSize) {
		return getPixel(size, fixedSize ? 1 : SCREEN_SCALE);
	}
	
	public static int getPixel(int size) {
		return getPixel(size, SCREEN_SCALE);
	}

	private static int getPixel(int size, float screenScale) {
		
		switch (size) {
			case SMALL:
				return (int)(50 * PIXEL_SCALE * screenScale);
			case MEDIUM:
				return (int)(105 * PIXEL_SCALE * screenScale);
			case BIG:
				return (int)(400 * PIXEL_SCALE * screenScale);
			case SCREENWIDTH:
				return PIXELS_WIDTH;
			case SCREENHEIGHT:
				return PIXELS_HEIGHT;
			default:
				return 0;
		}
	}
	
	public static int scale(int pixel) {
		return Math.round((float)pixel * PIXEL_SCALE);
	}
	
	/**
	 * Returns target dimensions of a bitmap. These are the dimensions the 
	 * picture will finally be cropped to.
	 * @param size      Which size
	 * @param mediaType Which media type
	 * @param x         Current image width
	 * @param y         Current image height
	 * @return
	 */
	public static Dimension getTargetDimension(int size, int mediaType, int x, int y) {
		switch (mediaType) {
		default:
		case MediaType.PICTURES:
		case MediaType.MUSIC: // always square
			return new Dimension(getPixel(size), getPixel(size));
		case MediaType.VIDEO:
		case MediaType.VIDEO_MOVIE:
		case MediaType.VIDEO_TVEPISODE:
		case MediaType.VIDEO_TVSEASON:
		case MediaType.VIDEO_TVSHOW:
			final double ar = ((double)x) / ((double)y);
			if (ar > 0.95 && ar < 1.05) { // square
				return new Dimension(getPixel(size), getPixel(size), Dimension.SQUARE);
			} else if (ar < 1) {			// portrait
				return new Dimension(getPixel(size), (int)(POSTER_AR * getPixel(size)), Dimension.PORTRAIT);
			} else if (ar < 1.5) { // landscape 4:3
				return new Dimension((int)((double)getPixel(size) / FULL_AR), getPixel(size), Dimension.LANDSCAPE);
			} else if (ar < 2) {			// landscape 16:9
				return new Dimension((int)((double)getPixel(size) / LANDSCAPE_AR), getPixel(size), Dimension.LANDSCAPE);
			} else if (ar > 5) {			// wide banner
				/* special case:
				 *  - small: banner width = screen width
				 *  - medium: banner width = screen height (for landscape display)
				 *  - large: 758x140 (original)
				 */				
				switch (size) {
				case ThumbSize.SMALL:
					return new Dimension(getPixel(ThumbSize.SCREENWIDTH), (int)((double)getPixel(ThumbSize.SCREENWIDTH) * BANNER_AR), Dimension.BANNER);
				case ThumbSize.MEDIUM:
					return new Dimension(getPixel(ThumbSize.SCREENHEIGHT), (int)((double)getPixel(ThumbSize.SCREENHEIGHT) * BANNER_AR), Dimension.BANNER);
				default:
				case ThumbSize.BIG:
					return new Dimension(758, 140, Dimension.BANNER);
				}
			} else {						// anything weird between wide banner and landscape 16:9
				return new Dimension((int)((double)getPixel(size) / LANDSCAPE_AR), getPixel(size), Dimension.UNKNOWN);
			}
		}
	}
	
	public static int[] values() {
		int[] values = { BIG, MEDIUM, SMALL }; 
		return values;
	}
	
	public static class Dimension {
		public static final int UNKNOWN = 0;
		public static final int PORTRAIT = 1;
		public static final int SQUARE = 2;
		public static final int LANDSCAPE = 3;
		public static final int BANNER = 4;
		public int x, y, format;
		Dimension(int x, int y, int format) {
			this(x, y);
			this.format = format;
		}
		Dimension(int x, int y) {
			this.x = x;
			this.y = y;
		}
		public String toString() {
			return "(" + x + "x" + y + ")";
		}
		public boolean equals(Dimension dim) {
			return dim.x == x && dim.y == y;
		}
	}
}

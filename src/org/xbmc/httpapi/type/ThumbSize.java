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

package org.xbmc.httpapi.type;

import android.content.res.Resources;

/**
 * Defines a thumb size. Sizes are:
 * <ul>
 * 	<li><code>SMALL</code></li>
 * 	<li><code>MEDIUM</code></li>
 * 	<li><code>BIG</code></li>
 * 	<li><code>SCREENWIDTH</code></li>
 * </ul>
 * 
 * @author Team XBMC
 */
public abstract class ThumbSize {
	
	public static final int SMALL = 1;
	public static final int MEDIUM = 2;
	public static final int BIG = 3;
	public static final int SCREENWIDTH = 4;
	
	private static final float sScale = Resources.getSystem().getDisplayMetrics().density;
	
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

	public static int getPixel(int size) {
		switch (size) {
			case SMALL:
				return (int)(50 * sScale);
			case MEDIUM:
				return (int)(103 * sScale);
			case BIG:
				return (int)(400 * sScale);
			case SCREENWIDTH:
				return (int)(320 * sScale);
			default:
				return 0;
		}
	}
	
	public static int[] values() {
		int[] values = { SMALL, MEDIUM, BIG }; 
		return values;
	}
}

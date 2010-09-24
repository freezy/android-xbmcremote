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

public abstract class SortType {
	
	public static final String ORDER_ASC = "ASC";
	public static final String ORDER_DESC = "DESC";
	
	public static final int ALBUM = 1;
	public static final int ARTIST = 2;
	public static final int TITLE = 3;
	public static final int FILENAME = 4;
	public static final int TRACK = 5;
	public static final int RATING = 6;
	public static final int YEAR = 7;
	public static final int EPISODE_NUM = 8;
	public static final int EPISODE_TITLE = 9;
	public static final int EPISODE_RATING = 10;
	
	public static final int DONT_SORT = -1;
}
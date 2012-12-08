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

package org.xbmc.api.business;

import org.xbmc.android.remote.business.Command;

public interface INotifiableManager {
	
	public static final String PREF_SORT_BY_PREFIX = "sort_by_";
	public static final String PREF_SORT_ORDER_PREFIX = "sort_order_";
	
	/* The idea of the sort keys is to remember different sort settings for
	 * each type. In your controller, make sure you run setSortKey() in the
	 * onCreate() method.
	 */
	public static final int PREF_SORT_KEY_ALBUM = 1;
	public static final int PREF_SORT_KEY_ARTIST = 2;
	public static final int PREF_SORT_KEY_SONG = 3;
	public static final int PREF_SORT_KEY_GENRE = 4;
	public static final int PREF_SORT_KEY_FILEMODE = 5;
	public static final int PREF_SORT_KEY_SHOW = 6;
	public static final int PREF_SORT_KEY_MOVIE = 7;
	public static final int PREF_SORT_KEY_EPISODE = 8;
	
	
	public void onFinish(DataResponse<?> response);
	public void onWrongConnectionState(int state, Command<?> cmd);
	public void onError(Exception e);
	public void onMessage(String message);
	public void onMessage(int code, String message);
	public void retryAll();
}

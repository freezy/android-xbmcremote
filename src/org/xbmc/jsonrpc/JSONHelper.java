/*
 *      Copyright (C) 2005-2010 Team XBMC
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

package org.xbmc.jsonrpc;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Wraps some calls in order to avoid JSON exceptions
 * @author Team XBMC
 */
public abstract class JSONHelper {
	
	public final static String getString(JSONObject obj, String key) {
		try {
			return obj.getString(key);
		} catch (JSONException e) {
			return null;
		}
	}
	public final static String getString(JSONObject obj, String key, String ifNullResult) {
		try {
			return obj.getString(key);
		} catch (JSONException e) {
			return ifNullResult;
		}
	}
	public final static int getInt(JSONObject obj, String key) {
		try {
			return obj.getInt(key);
		} catch (JSONException e) {
			return -1;
		}
	}
}
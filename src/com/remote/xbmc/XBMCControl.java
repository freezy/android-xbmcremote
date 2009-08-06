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

package com.remote.xbmc;

import org.xbmc.httpapi.XBMC;
import android.content.SharedPreferences;

public class XBMCControl {
	private static XBMC instance;
	
	public static final String PREFS_NAME = "XBMCRemotePrefsFile";
	
	static public XBMC getInstance(SharedPreferences settings) {
		if (instance == null)
		{
			String host = settings.getString("host", "");
			instance = new XBMC(host);
		}
		return instance;
	}

	public static void createInstance(XBMC xbmc) {
		instance = xbmc;
	}
}

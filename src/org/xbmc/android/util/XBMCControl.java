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

package org.xbmc.android.util;

import java.net.Inet4Address;
import org.xbmc.eventclient.XBMCClient;
import org.xbmc.httpapi.XBMC;
import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class XBMCControl {
	private static XBMC httpApiInstance;
	private static XBMCClient eventClientInstance;
	
	static public XBMC getHttpApiInstance(Activity activity) {
		if (httpApiInstance == null) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
			String host = prefs.getString("setting_ip", "");
			int port = prefs.getInt("setting_http_port", 80);
			String user = prefs.getString("setting_http_user", "");
			String pass = prefs.getString("setting_http_pass", "");
			if (port > 0 && user != null && user.length() > 0) {
				httpApiInstance = new XBMC(host, port, activity);
			} else if (user != null && user.length() > 0) {
				httpApiInstance = new XBMC(host, user, pass, activity);
			} else if (port > 0) {
				httpApiInstance = new XBMC(host, port, activity);
			} else {
				httpApiInstance = new XBMC(host, activity);
			}
		}
		return httpApiInstance;
	}
	
	static public XBMCClient getEventClientInstance(Activity activity) {
		if (eventClientInstance == null) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
			String host = prefs.getString("setting_ip", "");
			int port = prefs.getInt("setting_eventserver_port", 9777);
			try {
				eventClientInstance = new XBMCClient(Inet4Address.getByName(host), port, "Android XBMC Remote");
			} catch (Exception e) {
				return null;
			}
		}
		return eventClientInstance;
	}
}

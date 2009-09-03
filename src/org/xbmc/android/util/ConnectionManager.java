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
import org.xbmc.eventclient.EventClient;
import org.xbmc.httpapi.HttpClient;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

/**
 * Globally returns the control objects. 
 */
public class ConnectionManager {
	
	private static HttpClient sHttpApiInstance;
	private static EventClient sEventClientInstance;
	
	/**
	 * Returns an instance of the HTTP Client. Instantiation takes place only
	 * once, otherwise the first instance is returned.
	 * 
	 * @param activity
	 * @return Client for XBMC's HTTP API
	 */
	public static HttpClient getHttpApiInstance(Activity activity) {
		if (sHttpApiInstance == null) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
			String host = prefs.getString("setting_ip", "");
			int port = Integer.parseInt(prefs.getString("setting_http_port", "80"));
			String user = prefs.getString("setting_http_user", "");
			String pass = prefs.getString("setting_http_pass", "");
			if (port > 0 && user != null && user.length() > 0) {
				sHttpApiInstance = new HttpClient(host, port, activity);
			} else if (user != null && user.length() > 0) {
				sHttpApiInstance = new HttpClient(host, user, pass, activity);
			} else if (port > 0) {
				sHttpApiInstance = new HttpClient(host, port, activity);
			} else {
				sHttpApiInstance = new HttpClient(host, activity);
			}
		}
		return sHttpApiInstance;
	}
	
	/**
	 * Once instantiated with the activity we can use this one.
	 * @return Client for XBMC's HTTP API
	 */
	public static HttpClient getHttpApiInstance() {
		return sHttpApiInstance;
	}
	
	/**
	 * Returns an instance of the Event Server Client. Instantiation takes
	 * plave only once, otherwise the first instance is returned.
	 * 
	 * @param activity
	 * @return Client for XBMC's Event Server
	 */
	public static EventClient getEventClientInstance(Activity activity) {
		if (sEventClientInstance == null) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
			String host = prefs.getString("setting_ip", "");
			int port = Integer.parseInt(prefs.getString("setting_eventserver_port", "9777"));
			try {
				sEventClientInstance = new EventClient(Inet4Address.getByName(host), port, "Android XBMC Remote");
			} catch (Exception e) {
				return null;
			}
		}
		return sEventClientInstance;
	}
	
	/**
	 * Once instantiated with the activity we can use this one.
	 * @return Client for XBMC's Event Server
	 */
	public static EventClient getEventClientInstance() {
		return sEventClientInstance;
	}
	
	/**
	 * Checks whether the device is able to connect to the network
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connMgr.getActiveNetworkInfo();
		return(info!=null && info.isConnected());
	}
	
}

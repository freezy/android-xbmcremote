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

package org.xbmc.android.remote.data;

import org.xbmc.android.remote.presentation.controller.SettingsController;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IControlClient;
import org.xbmc.api.data.IInfoClient;
import org.xbmc.api.data.IMusicClient;
import org.xbmc.api.data.IVideoClient;
import org.xbmc.httpapi.HttpApi;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public abstract class ClientFactory {
	
	private static final int DEFAULT_TIMEOUT = 10000;
	private static final int DEFAULT_PORT = 80;
	
	private static HttpApi sHttpClient;
	
	public static IInfoClient getInfoClient(Context context, INotifiableManager manager) {
		return getHttpClient(context, manager).info;
	}
	
	public static IControlClient getControlClient(Context context, INotifiableManager manager) {
		return getHttpClient(context, manager).control;
	}
	
	public static IVideoClient getVideoClient(Context context, INotifiableManager manager) {
		return getHttpClient(context, manager).video;
	}
	
	public static IMusicClient getMusicClient(Context context, INotifiableManager manager) {
		return getHttpClient(context, manager).music;
	}
	
	/**
	 * Resets the client so it has to re-read the settings and recreate the instance.
	 */
	public static void resetClient() {
		sHttpClient = null;
	}
	
	
	/**
	 * Returns an instance of the HTTP Client. Instantiation takes place only
	 * once, otherwise the first instance is returned.
	 * 
	 * @param context Context needed for preferences. Use application context and not activity!
	 * @return Http client
	 */
	public static HttpApi getHttpClient(Context context, final INotifiableManager manager) {
		if (sHttpClient == null) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			int port = 0;
			try {
				port = Integer.parseInt(prefs.getString(SettingsController.SETTING_HTTP_PORT, String.valueOf(DEFAULT_PORT)));
			} catch (NumberFormatException e) {
				manager.onError(e);
				port = DEFAULT_PORT;
			}
			int timeout = 0;
			try {
				timeout = Integer.parseInt(prefs.getString(SettingsController.SETTING_HTTP_TIMEOUT, String.valueOf(DEFAULT_TIMEOUT)));
			} catch (NumberFormatException e) {
				manager.onError(e);
				timeout = DEFAULT_TIMEOUT;
			}
			String host = prefs.getString(SettingsController.SETTING_HTTP_HOST, "");
			String user = prefs.getString(SettingsController.SETTING_HTTP_USER, "");
			String pass = prefs.getString(SettingsController.SETTING_HTTP_PASS, "");
			
			if (!host.equals("")){
				if (port > 0 && user != null && user.length() > 0) {
					sHttpClient = new HttpApi(host, port, user, pass, timeout);
				} else if (user != null && user.length() > 0) {
					sHttpClient = new HttpApi(host, user, pass, timeout);
				} else if (port > 0) {
					sHttpClient = new HttpApi(host, port, timeout);
				} else {
					sHttpClient = new HttpApi(host, timeout);
				}
			} else {
				sHttpClient = new HttpApi(null, -1);
			}
			(new Thread("Init-Connection") {
				public void run() {
					sHttpClient.control.setResponseFormat(manager);
				}
			}).start();
		}
		return sHttpClient;
	}
}
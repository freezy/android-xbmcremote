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

import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IControlClient;
import org.xbmc.api.data.IInfoClient;
import org.xbmc.api.data.IMusicClient;
import org.xbmc.api.data.IVideoClient;
import org.xbmc.api.object.Host;
import org.xbmc.httpapi.HttpApi;

public abstract class ClientFactory {
	
	private static final int DEFAULT_TIMEOUT = 10000;
	
	private static HttpApi sHttpClient;
	
	public static IInfoClient getInfoClient(Host host, INotifiableManager manager) {
		return getHttpClient(host, manager).info;
	}
	
	public static IControlClient getControlClient(Host host, INotifiableManager manager) {
		return getHttpClient(host, manager).control;
	}
	
	public static IVideoClient getVideoClient(Host host, INotifiableManager manager) {
		return getHttpClient(host, manager).video;
	}
	
	public static IMusicClient getMusicClient(Host host, INotifiableManager manager) {
		return getHttpClient(host, manager).music;
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
	public static HttpApi getHttpClient(Host host, final INotifiableManager manager) {
		if (sHttpClient == null) {
			if (host != null && !host.addr.equals("")){
				sHttpClient = new HttpApi(host, DEFAULT_TIMEOUT);
			} else {
				sHttpClient = new HttpApi(null, -1);
			}
			
			// do some init stuff
			(new Thread("Init-Connection") {
				public void run() {
					sHttpClient.control.setResponseFormat(manager);
				}
			}).start();
		}
		return sHttpClient;
	}
}
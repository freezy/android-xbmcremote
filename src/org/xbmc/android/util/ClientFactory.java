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
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IControlClient;
import org.xbmc.api.data.IEventClient;
import org.xbmc.api.data.IInfoClient;
import org.xbmc.api.data.IMusicClient;
import org.xbmc.api.data.ITvShowClient;
import org.xbmc.api.data.IVideoClient;
import org.xbmc.api.object.Host;
import org.xbmc.eventclient.EventClient;
import org.xbmc.httpapi.HttpApi;
import org.xbmc.httpapi.WifiStateException;

import android.content.Context;

public abstract class ClientFactory {
	
	private static HttpApi sHttpClient;
	private static EventClient sEventClient;
	
	private static final String NAME = "Android XBMC Remote";
	
	public static IInfoClient getInfoClient(INotifiableManager manager, Context context) throws WifiStateException {
		if(context != null && HostFactory.host != null && HostFactory.host.wifi_only){
			final WifiHelper helper = WifiHelper.getInstance(context);
			final int state = helper.getWifiState();
			switch (state) {
			case WifiHelper.WIFI_STATE_DISABLED:
			case WifiHelper.WIFI_STATE_UNKNOWN:
			case WifiHelper.WIFI_STATE_ENABLED:
				throw new WifiStateException(state);
			}
		}
		return createHttpClient(manager).info;
	}
	
	public static IControlClient getControlClient(INotifiableManager manager, Context context) throws WifiStateException {
		if(context != null && HostFactory.host != null && HostFactory.host.wifi_only){
			final WifiHelper helper = WifiHelper.getInstance(context);
			final int state = helper.getWifiState();
			switch (state) {
			case WifiHelper.WIFI_STATE_DISABLED:
			case WifiHelper.WIFI_STATE_UNKNOWN:
			case WifiHelper.WIFI_STATE_ENABLED:
				throw new WifiStateException(state);
			}
		}
		return createHttpClient(manager).control;
	}
	
	public static IVideoClient getVideoClient(INotifiableManager manager, Context context) throws WifiStateException {
		if(context != null && HostFactory.host != null && HostFactory.host.wifi_only){
			final WifiHelper helper = WifiHelper.getInstance(context);
			final int state = helper.getWifiState();
			switch (state) {
			case WifiHelper.WIFI_STATE_DISABLED:
			case WifiHelper.WIFI_STATE_UNKNOWN:
				throw new WifiStateException(state);
			}
		}
		return createHttpClient(manager).video;
	}
	
	public static IMusicClient getMusicClient(INotifiableManager manager, Context context) throws WifiStateException {
		if(context != null && HostFactory.host != null && HostFactory.host.wifi_only){
			final WifiHelper helper = WifiHelper.getInstance(context);
			final int state = helper.getWifiState();
			switch (state) {
			case WifiHelper.WIFI_STATE_DISABLED:
			case WifiHelper.WIFI_STATE_UNKNOWN:
				throw new WifiStateException(state);
			}
		}
		return createHttpClient(manager).music;
	}
	
	public static ITvShowClient getTvShowClient(INotifiableManager manager, Context context) throws WifiStateException {
		if(context != null && HostFactory.host != null && HostFactory.host.wifi_only){
			final WifiHelper helper = WifiHelper.getInstance(context);
			final int state = helper.getWifiState();
			switch (state) {
			case WifiHelper.WIFI_STATE_DISABLED:
			case WifiHelper.WIFI_STATE_UNKNOWN:
				throw new WifiStateException(state);
			}
		}
		return createHttpClient(manager).shows;
	}
	
	public static IEventClient getEventClient(INotifiableManager manager) {
		return createEventClient(manager);
	}
	
	/**
	 * Resets the client so it has to re-read the settings and recreate the instance.
	 */
	public static void resetClient(Host host) {
		if (sHttpClient != null) {
			sHttpClient.setHost(host);
		}
		if (sEventClient != null) {
			try {
				if (host != null) {
					InetAddress addr = Inet4Address.getByName(host.addr);
					sEventClient.setHost(addr, host.esPort > 0 ? host.esPort : Host.DEFAULT_EVENTSERVER_PORT);
				} else {
					sEventClient.setHost(null, 0);
				}
			} catch (UnknownHostException e) { }
		}
	}

	/**
	 * Returns an instance of the HTTP Client. Instantiation takes place only
	 * once, otherwise the first instance is returned.
	 * 
	 * @param context Context needed for preferences. Use application context and not activity!
	 * @return Http client
	 */
	private static HttpApi createHttpClient(final INotifiableManager manager) {
		if (sHttpClient == null) {
			final Host host = HostFactory.host;
			if (host != null && !host.addr.equals("")){
				sHttpClient = new HttpApi(host, host.timeout >= 0 ? host.timeout : Host.DEFAULT_TIMEOUT);
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
	
	/**
	 * Returns an instance of the Event Server Client. Instantiation takes
	 * place only once, otherwise the first instance is returned.
	 * 
	 * @param context
	 * @return Client for XBMC's Event Server
	 */
	private static IEventClient createEventClient(final INotifiableManager manager) {
		if (sEventClient == null) {
			final Host host = HostFactory.host;
			if (host != null) {
				try {
					final InetAddress addr = Inet4Address.getByName(host.addr);
					sEventClient = new EventClient(addr, host.esPort > 0 ? host.esPort : Host.DEFAULT_EVENTSERVER_PORT, NAME);
				} catch (UnknownHostException e) {
					manager.onMessage("EventClient: Cannot parse address \"" + host.addr + "\".");
					sEventClient = new EventClient(NAME);
				}
			} else {
				manager.onMessage("EventClient: Failed to read host settings.");
				sEventClient = new EventClient(NAME);
			}
		}
		return sEventClient;
	}
}
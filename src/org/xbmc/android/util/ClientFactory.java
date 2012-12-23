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

import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IConnectionManager;
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
import android.util.Log;

public abstract class ClientFactory {

	public static int XBMC_REV = -1;

	public static final int MIN_JSONRPC_REV = 27770;
	public static final int MICROHTTPD_REV = 27770;
	public static final int THUMB_TO_VFS_REV = 29743;

	private static HttpApi sHttpClient;
	private static EventClient sEventClient;

	private static final String TAG = "ClientFactory";
	private static final String NAME = "Android XBMC Remote";
	
	public static IConnectionManager getConnectionManager(INotifiableManager manager,
			Context context) throws WifiStateException {
		
		assertWifiState(context);
		return createHttpClient(manager).getConnectionManager(context);
	}

	public static IInfoClient getInfoClient(INotifiableManager manager,
			Context context) throws WifiStateException {
		assertWifiState(context);
		return createHttpClient(manager).info;
	}

	public static IControlClient getControlClient(INotifiableManager manager,
			Context context) throws WifiStateException {
		assertWifiState(context);
		return createHttpClient(manager).control;
	}

	public static IVideoClient getVideoClient(INotifiableManager manager,
			Context context) throws WifiStateException {
		assertWifiState(context);
		return createHttpClient(manager).video;
	}

	public static IMusicClient getMusicClient(INotifiableManager manager,
			Context context) throws WifiStateException {
		assertWifiState(context);
		return createHttpClient(manager).music;
	}

	public static ITvShowClient getTvShowClient(INotifiableManager manager,
			Context context) throws WifiStateException {
		assertWifiState(context);
		return createHttpClient(manager).shows;
	}

	private static void assertWifiState(Context context)
			throws WifiStateException {
		if (context != null && HostFactory.host != null
				&& HostFactory.host.wifi_only) {
			final int state = WifiHelper.getInstance(context).getWifiState();
			if (state != WifiHelper.WIFI_STATE_CONNECTED) {
				throw new WifiStateException(state);
			}
		}
	}

	public static IEventClient getEventClient(INotifiableManager manager) {
		return createEventClient(manager);
	}

	/**
	 * Resets the client so it has to re-read the settings and recreate the
	 * instance.
	 * 
	 * @param host
	 *            New host settings, can be null.
	 */
	public static void resetClient(Host host) {
		
		ManagerFactory.resetClient();
		
		if (sHttpClient != null) {
			sHttpClient.setHost(host);
		} else {
			Log.w(TAG,
					"Not updating http client's host because no instance is set yet.");
		}
		Log.i(TAG, "Resetting client to "
				+ (host == null ? "<nullhost>" : host.addr));
		if (sEventClient != null) {
			try {
				if (host != null) {
					InetAddress addr = Inet4Address.getByName(host.addr);
					sEventClient.setHost(addr, host.esPort > 0 ? host.esPort
							: Host.DEFAULT_EVENTSERVER_PORT);
				} else {
					sEventClient.setHost(null, 0);
				}
			} catch (UnknownHostException e) {
				Log.e(TAG, "Unknown host: "
						+ (host == null ? "<nullhost>" : host.addr));
			}
		} else {
			Log.w(TAG,
					"Not updating event client's host because no instance is set yet.");
		}
	}

	/**
	 * Returns an instance of the HTTP Client. Instantiation takes place only
	 * once, otherwise the first instance is returned.
	 * 
	 * @param manager
	 *            Upper layer reference
	 * @return HTTP client
	 */
	private static HttpApi createHttpClient(final INotifiableManager manager) {
		final Host host = HostFactory.host;
		if (sHttpClient == null) {
			if (host != null && !host.addr.equals("")) {
				sHttpClient = new HttpApi(host,
						host.timeout >= 0 ? host.timeout : Host.DEFAULT_TIMEOUT);
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
	 * Returns an instance of the Event Server Client. Instantiation takes place
	 * only once, otherwise the first instance is returned.
	 * 
	 * @param manager
	 *            Upper layer reference
	 * @return Client for XBMC's Event Server
	 */
	private static IEventClient createEventClient(
			final INotifiableManager manager) {
		if (sEventClient == null) {
			final Host host = HostFactory.host;
			if (host != null) {
				try {
					final InetAddress addr = Inet4Address.getByName(host.addr);
					sEventClient = new EventClient(addr,
							host.esPort > 0 ? host.esPort
									: Host.DEFAULT_EVENTSERVER_PORT, NAME);
					Log.i(TAG, "EventClient created on " + addr);
				} catch (UnknownHostException e) {
					manager.onMessage("EventClient: Cannot parse address \""
							+ host.addr + "\".");
					Log.e(TAG, "EventClient: Cannot parse address \""
							+ host.addr + "\".");
					sEventClient = new EventClient(NAME);
				}
			} else {
				manager.onMessage("EventClient: Failed to read host settings.");
				Log.e(TAG, "EventClient: Failed to read host settings.");
				sEventClient = new EventClient(NAME);
			}
		}
		return sEventClient;
	}
}
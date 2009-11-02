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

import java.io.IOException;
import java.net.Inet4Address;
import java.util.Collection;
import java.util.HashSet;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.xbmc.eventclient.EventClient;
import org.xbmc.httpapi.HttpClient;

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
	private static Collection<ServiceInfo> sServiceInfo = new HashSet<ServiceInfo>();
	
	/**
	 * Performs zeroconf lookup for the hostname and XBMC's services.
	 * Stores a static collection of all previously looked up services,
	 * and returns from that cache if it exists.
	 * 
	 * @param type The zeroconf connection type
	 * @param host The hostname to lookup
	 * @return {@link ServiceInfo} The details of the first matching host
	 */
	public static ServiceInfo getZeroconfServiceInfo(String type, String host) {
		// Zeroconf addresses always end with .local.
		// So, if it ends just with ".local", add a period to it.
		if (host.endsWith(".local")) {
			host = host.concat(".");
		}

		// If it doesn't have the ".local" suffix at all, add it,
		// assuming the user typed just the hostname.
		if (!host.endsWith(".local.")) {
			host = host.concat("local.");
		}

		// See if we already looked that specific service and type up
		if (!sServiceInfo.isEmpty()) {
			for (ServiceInfo si: sServiceInfo) {
				if (si.getType() == type && 
						si.getServer().compareToIgnoreCase(host) == 0) {
					return si;
				}
			}
		}

		// If this is the first lookup, try to find something that matches\
		// the query, and save it in the collection for future use.
		ServiceInfo hostInfo = null;
		try {
			JmDNS jmdns = JmDNS.create();
			int iAttempt = 0;

			while (hostInfo == null && iAttempt < 60) {
				ServiceInfo[] infos = jmdns.list(type);
				for (int i=0; i < infos.length; i++) {
					if (infos[i].getServer().compareToIgnoreCase(host) == 0) {
						hostInfo = infos[i];
						break;
					}
				}

				if (hostInfo == null) {               
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						break;
					}
					iAttempt++;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Add the host information to the collection, for future reference
		sServiceInfo.add(hostInfo);
		return hostInfo;
	}
	
	/**
	 * Returns an instance of the HTTP Client. Instantiation takes place only
	 * once, otherwise the first instance is returned.
	 * 
	 * @param context
	 * @return Client for XBMC's HTTP API
	 */
	public static HttpClient getHttpClient(Context context) {
		if (sHttpApiInstance == null) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			String prefHost = prefs.getString("setting_ip", "10.10.10.10");

			// Define the variables for the connection
			String host = null;
			int port = 0;
			
			// If we need to use zeroconf, query it, and use that for the connection information
			// [phreezie] removed for now, please do it async, test, and when youre 100% sure test again, then re-enable. ;)
/*			if (prefs.getBoolean("setting_mdns", false)) {
				ServiceInfo mdnsHost = ConnectionManager.getZeroconfServiceInfo(
						"_xbmc-web._tcp.local.", prefHost);

				// In case the mdns lookup has failed, return a null				
				if (mdnsHost != null) {
					host = mdnsHost.getHostAddress();
					port = mdnsHost.getPort();
				} else {
					return null;
				}
			} else {*/
				host = prefHost;
				port = Integer.parseInt(prefs.getString("setting_http_port", "80"));
//			}
			
			String user = prefs.getString("setting_http_user", "");
			String pass = prefs.getString("setting_http_pass", "");
			int timeout = 10000;
			try {
				timeout = Integer.parseInt(prefs.getString("setting_socket_timeout", "10000"));
			} catch (ClassCastException e) {}
			
			if (port > 0 && user != null && user.length() > 0) {
				sHttpApiInstance = new HttpClient(host, port, user, pass, timeout, new ErrorHandler(context));
			} else if (user != null && user.length() > 0) {
				sHttpApiInstance = new HttpClient(host, user, pass, timeout, new ErrorHandler(context));
			} else if (port > 0) {
				sHttpApiInstance = new HttpClient(host, port, timeout, new ErrorHandler(context));
			} else {
				sHttpApiInstance = new HttpClient(host, timeout, new ErrorHandler(context));
			}
		}
		return sHttpApiInstance;
	}
	
	/**
	 * Forces the next HTTP Client access to recreate the client and re-read the settings. 
	 */
	public static void resetClient() {
		if (sHttpApiInstance != null) {
			sHttpApiInstance = null;
		}
		if (sEventClientInstance != null) {
			sEventClientInstance = null;
		}
	}
	
	/**
	 * Returns an instance of the Event Server Client. Instantiation takes
	 * place only once, otherwise the first instance is returned.
	 * 
	 * @param context
	 * @return Client for XBMC's Event Server
	 */
	public static EventClient getEventClient(Context context) {
		if (sEventClientInstance == null) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			
			String prefHost = prefs.getString("setting_ip", "10.10.10.10");

			String host = null;
			int port = 0;
			
/*			if (prefs.getBoolean("setting_mdns", false)) {
				ServiceInfo mdnsHost = ConnectionManager.getZeroconfServiceInfo(
						"_xbmc-events._udp.local.", prefHost);
				
				// In case the mdns lookup has failed, return a null 
				if (mdnsHost != null) {
					host = mdnsHost.getHostAddress();
					port = mdnsHost.getPort();
				} else {
					return null;
				}
			} else {*/
				host = prefHost;
				port = Integer.parseInt(prefs.getString("setting_eventserver_port", "9777"));
//			}
			
			try {
				sEventClientInstance = new EventClient(Inet4Address.getByName(host), port, "Android XBMC Remote");
			} catch (Exception e) {
				return null;
			}
		}
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
		return info != null && info.isConnected();
	}
}
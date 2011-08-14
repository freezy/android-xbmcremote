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

import org.xbmc.android.remote.business.NowPlayingPollerThread;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Globally returns the control objects. 
 * 
 * This class will soon retire once the event client is properly moved to the
 * data layer
 */
public class ConnectionFactory {
	
//	private static Collection<ServiceInfo> sServiceInfo = new HashSet<ServiceInfo>();
	private static NowPlayingPollerThread sNowPlayingPoller;
	
	/**
	 * Performs zeroconf lookup for the hostname and XBMC's services.
	 * Stores a static collection of all previously looked up services,
	 * and returns from that cache if it exists.
	 * 
	 * @param type The zeroconf connection type
	 * @param host The hostname to lookup
	 * @return {@link ServiceInfo} The details of the first matching host
	 *
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
						Log.e("ConnectionManager", Log.getStackTraceString(e));
						break;
					}
					iAttempt++;
				}
			}
		} catch (IOException e) {
			//e.printStackTrace();
			Log.e("ConnectionManager", Log.getStackTraceString(e));
		}

		// Add the host information to the collection, for future reference
		sServiceInfo.add(hostInfo);
		return hostInfo;
	}*/
	
	/**
	 * Returns an instance of the NowPlaying Poller . Instantiation takes place only
	 * once, otherwise the first instance is returned.
	 * 
	 * @param context
	 * @return A reference to the NowPlaying Poller
	 */
	public static NowPlayingPollerThread getNowPlayingPoller(Context context) {
		if (sNowPlayingPoller == null) {
			sNowPlayingPoller = new NowPlayingPollerThread(context);
			sNowPlayingPoller.start();
		}
		if (!sNowPlayingPoller.isAlive()){
			sNowPlayingPoller = new NowPlayingPollerThread(context);
			sNowPlayingPoller.start();			
		}
		return sNowPlayingPoller;
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
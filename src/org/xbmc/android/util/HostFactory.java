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

import java.util.ArrayList;

import org.xbmc.android.remote.business.provider.HostProvider;
import org.xbmc.android.remote.business.provider.HostProvider.Hosts;
import org.xbmc.api.object.Host;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;

/**
 * Helper class that keeps 
 *   a) a reference to the currently used host
 *   b) all the code between the provider and the settings stuff
 * 
 * @author Team XBMC
 */
public abstract class HostFactory {
	
	/**
	 * The currently used host
	 */
	public static Host host = null;
	
	/**
	 * The setting that remembers which host has been used last
	 */
	public static final String SETTING_HOST_ID = "setting_host_id";
	
	/**
	 * Returns all hosts
	 * @param activity Reference to activity
	 * @return List of all hosts
	 */
	public static ArrayList<Host> getHosts(Context context) {
		Cursor cur = context.getContentResolver().query(HostProvider.Hosts.CONTENT_URI, 
				null,       // All
				null,       // Which rows to return (all rows)
				null,       // Selection arguments (none)
				HostProvider.Hosts.NAME + " ASC"); // Put the results in ascending order by name
		ArrayList<Host> hosts = new ArrayList<Host>();
		try {
			if (cur.moveToFirst()) {
				final int idCol = cur.getColumnIndex(HostProvider.Hosts._ID);
				final int nameCol = cur.getColumnIndex(HostProvider.Hosts.NAME);
				final int hostCol = cur.getColumnIndex(HostProvider.Hosts.ADDR);
				final int portCol = cur.getColumnIndex(HostProvider.Hosts.PORT);
				final int userCol = cur.getColumnIndex(HostProvider.Hosts.USER);
				final int passCol = cur.getColumnIndex(HostProvider.Hosts.PASS);
				final int esPortCol = cur.getColumnIndex(HostProvider.Hosts.ESPORT);
				final int timeoutCol = cur.getColumnIndex(HostProvider.Hosts.TIMEOUT);
				final int wifiOnlyCol = cur.getColumnIndex(HostProvider.Hosts.WIFI_ONLY);
				final int accessPointCol = cur.getColumnIndex(HostProvider.Hosts.ACCESS_POINT);
				final int macAddrCol = cur.getColumnIndex(HostProvider.Hosts.MAC_ADDR);
				final int wolPortCol = cur.getColumnIndex(HostProvider.Hosts.WOL_PORT);
				final int wolWaitCol = cur.getColumnIndex(HostProvider.Hosts.WOL_WAIT);
				do {
					final Host host = new Host();
					host.id = cur.getInt(idCol);
					host.name = cur.getString(nameCol);
					host.addr = cur.getString(hostCol);
					host.port = cur.getInt(portCol);
					host.user = cur.getString(userCol);
					host.pass = cur.getString(passCol);
					host.esPort = cur.getInt(esPortCol);
					host.timeout = cur.getInt(timeoutCol);
					host.access_point = cur.getString(accessPointCol);
					host.mac_addr = cur.getString(macAddrCol);
					host.wifi_only = cur.getInt(wifiOnlyCol)==1; //stored as 1 = true and 0 = false in sqlite
					host.wol_port = cur.getInt(wolPortCol);
					host.wol_wait = cur.getInt(wolWaitCol);
					hosts.add(host);
				} while (cur.moveToNext());
			}
		} finally {
			cur.close();
		}
		return hosts;
	}
	
	/**
	 * Adds a host to the database.
	 * @param context Reference to context
	 * @param host Host to add
	 */
	public static void addHost(Context context, Host host) {
		ContentValues values = new ContentValues();
		values.put(HostProvider.Hosts.NAME, host.name);
		values.put(HostProvider.Hosts.ADDR, host.addr);
		values.put(HostProvider.Hosts.PORT, host.port);
		values.put(HostProvider.Hosts.USER, host.user);
		values.put(HostProvider.Hosts.PASS, host.pass);
		values.put(HostProvider.Hosts.ESPORT, host.esPort);
		values.put(HostProvider.Hosts.TIMEOUT, host.timeout);
		values.put(HostProvider.Hosts.WIFI_ONLY, host.wifi_only?1:0);
		values.put(HostProvider.Hosts.MAC_ADDR, host.mac_addr);
		values.put(HostProvider.Hosts.ACCESS_POINT, host.access_point);
		values.put(HostProvider.Hosts.WOL_PORT, host.wol_port);
		values.put(HostProvider.Hosts.WOL_WAIT, host.wol_wait);
		context.getContentResolver().insert(HostProvider.Hosts.CONTENT_URI, values);
	}
	
	/**
	 * Updates a host
	 * @param context Reference to context
	 * @param host Host to update
	 */
	public static void updateHost(Context context, Host host) {
		ContentValues values = new ContentValues();
		values.put(HostProvider.Hosts.NAME, host.name);
		values.put(HostProvider.Hosts.ADDR, host.addr);
		values.put(HostProvider.Hosts.PORT, host.port);
		values.put(HostProvider.Hosts.USER, host.user);
		values.put(HostProvider.Hosts.PASS, host.pass);
		values.put(HostProvider.Hosts.ESPORT, host.esPort);
		values.put(HostProvider.Hosts.TIMEOUT, host.timeout);
		values.put(HostProvider.Hosts.WIFI_ONLY, host.wifi_only?1:0);
		values.put(HostProvider.Hosts.MAC_ADDR, host.mac_addr);
		values.put(HostProvider.Hosts.ACCESS_POINT, host.access_point);
		values.put(HostProvider.Hosts.WOL_PORT, host.wol_port);
		values.put(HostProvider.Hosts.WOL_WAIT, host.wol_wait);
		context.getContentResolver().update(HostProvider.Hosts.CONTENT_URI, values, HostProvider.Hosts._ID + "=" + host.id, null);
	}
	
	/**
	 * Deletes a host.
	 * @param activity Reference to activity
	 * @param host Host to delete
	 * @return
	 */
	public static void deleteHost(Context context, Host host) {
		Uri hostUri = ContentUris.withAppendedId(Hosts.CONTENT_URI, host.id);
		context.getContentResolver().delete(hostUri, null, null);
	}
	
	/**
	 * Saves the host to the preference file.
	 * @param context
	 * @param addr
	 */
	public static void saveHost(Context context, Host h) {
		SharedPreferences.Editor ed = PreferenceManager.getDefaultSharedPreferences(context).edit();
		if (h != null) {
			ed.putInt(SETTING_HOST_ID, h.id);
		} else {
			ed.putInt(SETTING_HOST_ID, 0);
		}
		ed.commit();
		host = h;
		ClientFactory.resetClient(h);
	}
	
	/**
	 * Reads the preferences and returns the currently set host. If there is no
	 * preference set, return the first host. If there is no host set, return
	 * null.
	 * @param activity Reference to current activity
	 * @return Current host
	 */
	public static void readHost(Context context) {
		int hostId = PreferenceManager.getDefaultSharedPreferences(context).getInt(SETTING_HOST_ID, -1);
		if (hostId < 0) {
			host = getHost(context);
		} else {
			host = getHost(context, hostId);
		}
	}
	
	/**
	 * Returns a host based on its database ID.
	 * @param activity Reference to activity
	 * @param id Host database ID
	 * @return
	 */
	private static Host getHost(Context context, int id) {
		Uri hostUri = ContentUris.withAppendedId(Hosts.CONTENT_URI, id);
		Cursor cur = context.getContentResolver().query(hostUri, null, null, null, null);
		try {
			if (cur.moveToFirst()) {
				final Host host = new Host();
				host.id = cur.getInt(cur.getColumnIndex(HostProvider.Hosts._ID));
				host.name = cur.getString(cur.getColumnIndex(HostProvider.Hosts.NAME));
				host.addr = cur.getString(cur.getColumnIndex(HostProvider.Hosts.ADDR));
				host.port = cur.getInt(cur.getColumnIndex(HostProvider.Hosts.PORT));
				host.user = cur.getString(cur.getColumnIndex(HostProvider.Hosts.USER));
				host.pass = cur.getString(cur.getColumnIndex(HostProvider.Hosts.PASS));
				host.esPort = cur.getInt(cur.getColumnIndex(HostProvider.Hosts.ESPORT));
				host.timeout = cur.getInt(cur.getColumnIndex(HostProvider.Hosts.TIMEOUT));
				host.wifi_only = cur.getInt(cur.getColumnIndex(HostProvider.Hosts.WIFI_ONLY))==1;
				host.access_point = cur.getString(cur.getColumnIndex(HostProvider.Hosts.ACCESS_POINT));
				host.mac_addr = cur.getString(cur.getColumnIndex(HostProvider.Hosts.MAC_ADDR)); 
				host.wol_port = cur.getInt(cur.getColumnIndex(HostProvider.Hosts.WOL_PORT));
				host.wol_wait = cur.getInt(cur.getColumnIndex(HostProvider.Hosts.WOL_WAIT));
				return host;
			}
		} finally {
			cur.close();
		}
		return null;
	}
	
	/**
	 * Returns the first host found. This is useful if hosts are defined but
	 * the settings have been erased and need to be reset to a host. If nothing
	 * found, return null.
	 * @param activity Reference to activity
	 * @return First host found or null if hosts table empty.
	 */
	private static Host getHost(Context context) {
		ArrayList<Host> hosts = getHosts(context);
		if (hosts.size() > 0) {
			return hosts.get(0);
		} else {
			return null;
		}
	}
}
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

package org.xbmc.android.remote.presentation.preference;

import java.io.Serializable;
import java.util.ArrayList;

import org.xbmc.android.remote.presentation.preference.HostProvider.Hosts;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Just a data container for connection data of an XBMC instance
 * 
 * @author Team XBMC
 */
public class Host implements Serializable {
	/**
	 * Database ID
	 */
	public int id;
	/**
	 * Name (description/label) of the host
	 */
	public String name;
	/**
	 * IP address or host name of the host
	 */
	public String host;
	/**
	 * HTTP API Port
	 */
	public int port;
	/**
	 * User name of in case of HTTP authentication
	 */
	public String user;
	/**
	 * Password of in case of HTTP authentication
	 */
	public String pass;
	
	/**
	 * Returns a host based on its database ID.
	 * @param activity Reference to activity
	 * @param id Host database ID
	 * @return
	 */
	public static Host getHost(Activity activity, int id) {
		Uri hostUri = ContentUris.withAppendedId(Hosts.CONTENT_URI, id);
		Cursor cur = activity.managedQuery(hostUri, null, null, null, null);
		if (cur.moveToFirst()) {
			final Host host = new Host();
			host.id = cur.getInt(cur.getColumnIndex(HostProvider.Hosts._ID));
			host.name = cur.getString(cur.getColumnIndex(HostProvider.Hosts.NAME));
			host.host = cur.getString(cur.getColumnIndex(HostProvider.Hosts.HOST));
			host.port = cur.getInt(cur.getColumnIndex(HostProvider.Hosts.PORT));
			host.user = cur.getString(cur.getColumnIndex(HostProvider.Hosts.USER));
			host.pass = cur.getString(cur.getColumnIndex(HostProvider.Hosts.PASS));
			return host;
		}
		return null;
	}
	
	/**
	 * Returns all hosts
	 * @param activity Reference to activity
	 * @return List of all hosts
	 */
	public static ArrayList<Host> getHosts(Activity activity) {
		Cursor cur = activity.managedQuery(HostProvider.Hosts.CONTENT_URI, 
				null,       // All
				null,       // Which rows to return (all rows)
				null,       // Selection arguments (none)
				HostProvider.Hosts.NAME + " ASC"); // Put the results in ascending order by name
		ArrayList<Host> hosts = new ArrayList<Host>();
		if (cur.moveToFirst()) {
			final int idCol = cur.getColumnIndex(HostProvider.Hosts._ID);
			final int nameCol = cur.getColumnIndex(HostProvider.Hosts.NAME);
			final int hostCol = cur.getColumnIndex(HostProvider.Hosts.HOST);
			final int portCol = cur.getColumnIndex(HostProvider.Hosts.PORT);
			final int userCol = cur.getColumnIndex(HostProvider.Hosts.USER);
			final int passCol = cur.getColumnIndex(HostProvider.Hosts.PASS);
			do {
				final Host host = new Host();
				host.id = cur.getInt(idCol);
				host.name = cur.getString(nameCol);
				host.host = cur.getString(hostCol);
				host.port = cur.getInt(portCol);
				host.user = cur.getString(userCol);
				host.pass = cur.getString(passCol);
				hosts.add(host);
			} while (cur.moveToNext());
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
		values.put(HostProvider.Hosts.HOST, host.host);
		values.put(HostProvider.Hosts.PORT, host.port);
		values.put(HostProvider.Hosts.USER, host.user);
		values.put(HostProvider.Hosts.PASS, host.pass);
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
		values.put(HostProvider.Hosts.HOST, host.host);
		values.put(HostProvider.Hosts.PORT, host.port);
		values.put(HostProvider.Hosts.USER, host.user);
		values.put(HostProvider.Hosts.PASS, host.pass);
		context.getContentResolver().update(HostProvider.Hosts.CONTENT_URI, values, HostProvider.Hosts._ID + "=" + host.id, null);
	}
	
	/**
	 * Something readable
	 */
	public String toString() {
		return host + ":" + port;
	}
	
	public String getSummary() {
		return toString();
	}

	
	private static final long serialVersionUID = 7886482294339161092L;
	
}
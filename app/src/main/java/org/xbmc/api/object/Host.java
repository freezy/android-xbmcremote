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

package org.xbmc.api.object;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Just a data container for connection data of an XBMC instance
 *
 * @author Team XBMC
 */
public class Host implements Serializable {
	public static final int DEFAULT_HTTP_PORT = 8080;
	/**
	 * HTTP API Port
	 */
	public int port = DEFAULT_HTTP_PORT;
	public static final int DEFAULT_EVENTSERVER_PORT = 9777;
	/**
	 * Event server port
	 */
	public int esPort = DEFAULT_EVENTSERVER_PORT;
	public static final int DEFAULT_TIMEOUT = 5000;
	/**
	 * TCP socket read timeout in milliseconds
	 */
	public int timeout = DEFAULT_TIMEOUT;
	public static final int DEFAULT_WOL_WAIT = 40;
	/**
	 * The time to wait after sending WOL
	 */
	public int wol_wait = DEFAULT_WOL_WAIT;
	public static final int DEFAULT_WOL_PORT = 9;
	/**
	 * The port to send the WOL to
	 */
	public int wol_port = DEFAULT_WOL_PORT;
	private static final String TAG = "Host";
	private static final long serialVersionUID = 7886482294339161092L;
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
	public String addr;
	/**
	 * User name of in case of HTTP authentication
	 */
	public String user;
	/**
	 * Password of in case of HTTP authentication
	 */
	public String pass;
	/**
	 * If this host is only available through wifi
	 */
	public boolean wifi_only = false;
	/**
	 * If wifi only is true there might be an access point specified to connect to
	 */
	public String access_point;
	/**
	 * The MAC address of this host
	 */
	public String mac_addr;

	/**
	 * Something readable
	 */
	public String toString() {
		return addr + ":" + port;
	}

	public String getSummary() {
		return toString();
	}

	public String toJson() {
		try {
			JSONObject json = new JSONObject();
			json.put("name", name);
			json.put("addr", addr);
			json.put("port", port);
			json.put("user", user);
			json.put("pass", pass);
			json.put("esPort", esPort);
			json.put("timeout", timeout);
			json.put("wifi_only", wifi_only);
			json.put("access_point", access_point);
			json.put("mac_addr", mac_addr);
			json.put("wol_wait", wol_wait);
			json.put("wol_port", wol_port);
			return json.toString();
		} catch (JSONException e) {
			Log.e(TAG, "Error in toJson", e);
			return "";
		}
	}

}
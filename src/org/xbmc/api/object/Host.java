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

import java.io.Serializable;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;
import org.xbmc.android.jsonrpc.config.HostConfig;

import android.util.Log;

/**
 * Just a data container for connection data of an XBMC instance
 * 
 * @author Team XBMC
 */
public class Host implements Serializable {
	private static final String TAG = "Host";
	
	public static final int DEFAULT_HTTP_PORT = 8080;
	public static final int DEFAULT_JSON_PORT = 9090;
	public static final int DEFAULT_EVENTSERVER_PORT = 9777;
	public static final int DEFAULT_TIMEOUT = 5000;
	public static final int DEFAULT_WOL_WAIT = 40;
	public static final int DEFAULT_WOL_PORT = 9;
	
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
	 * HTTP API Port
	 */
	public int port = DEFAULT_HTTP_PORT;
	/**
	 * JSON API Port
	 */
	public int jsonPort = DEFAULT_JSON_PORT;
	
	/**
	 * User name of in case of HTTP authentication
	 */
	public String user;
	/**
	 * Password of in case of HTTP authentication
	 */
	public String pass;
	/**
	 * Event server port
	 */
	public int esPort = DEFAULT_EVENTSERVER_PORT;
	/**
	 * TCP socket read timeout in milliseconds
	 */
	public int timeout = DEFAULT_TIMEOUT;
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
	 * The time to wait after sending WOL
	 */
	public int wol_wait = DEFAULT_WOL_WAIT;
	/**
	 * The port to send the WOL to
	 */
	public int wol_port = DEFAULT_WOL_PORT;
	
	/**
	 * Whether or not to use the JSON api
	 */
	public boolean jsonApi = false;
	
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
			json.put("jsonPort", jsonPort);
			json.put("jsonApi", jsonApi);
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
	
	public HostConfig toHostConfig() {
		return new HostConfig(addr, port, jsonPort, user, pass);
	}
	
	public int getTimeout() {
		return this.timeout >= 0 ? this.timeout : Host.DEFAULT_TIMEOUT;		
	}
	
	public String getVfsUrl(String path) {
		String specialPath = "vfs";
		return "http://" + addr + ":" + port + "/" + specialPath + "/" + URLEncoder.encode(path);
	}
	
	private static final long serialVersionUID = 7886482294339161092L;
	
}
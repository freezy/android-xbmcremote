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

package org.xbmc.jsonrpc;

import org.xbmc.api.object.Host;
import org.xbmc.jsonrpc.client.InfoClient;
import org.xbmc.jsonrpc.client.MusicClient;

/**
 * Wrapper class for JSON-RPC clients. The idea is to separate the loads of
 * API method we're going to have into separate classes. The ClientFactory class
 * instantiates them and keeps them in a central place.
 * 
 * @author Team XBMC
 */
public class JsonRpc {
	
	
	/**
	 * Use this client for anything system related
	 */
	public final InfoClient info;
	
	/**
	 * Use this client for anything music related
	 */
	public final MusicClient music;
	
	/**
	 * Use this client for anything video related
	 *
	public final VideoClient video;
	
	/**
	 * Use this client for anything media controller related
	 *
	public final ControlClient control;
	
	/**
	 * Use this client for anything tv show related
	 *
	public final TvShowClient shows;
	
	/**
	 * Construct with all paramaters
	 * @param host    Connection data of the host
	 * @param timeout Read timeout
	 */
	public JsonRpc(Host host, int timeout) {
		Connection connection;
		if (host != null) {
			connection = Connection.getInstance(host.addr, host.port);
			connection.setAuth(host.user, host.pass);
		} else {
			connection = Connection.getInstance(null, 0);
		}
		connection.setTimeout(timeout);
		info = new InfoClient(connection);
		music = new MusicClient(connection);
/*		video = new VideoClient(connection);
		control = new ControlClient(connection);
		shows = new TvShowClient(connection);*/
	}
	
	/**
	 * Updates host info on all clients
	 * @param host
	 */
	public void setHost(Host host) {
		info.setHost(host);
		music.setHost(host);
/*		video.setHost(host);
		control.setHost(host);
		shows.setHost(host);*/
	}
}
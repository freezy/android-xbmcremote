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

package org.xbmc.httpapi;

import java.util.ArrayList;
import java.util.PriorityQueue;

import org.xbmc.httpapi.type.MediaType;

import android.content.Context;


public class HttpClient {
	HttpApiConnection instance;
	PriorityQueue<Message> messenger;
	
	public HttpClient(String host) {
		this(host, -1, null, null);
	}
	
	public HttpClient(String host, int port) {
		this(host, port, null, null);
	}

	public HttpClient(String host, String username, String password) {
		this(host, -1, username, password);
	}

	public HttpClient(String host, int port, String username, String password) {		
		messenger = new PriorityQueue<Message>();
		instance = new HttpApiConnection(host, port, username, password, messenger);
	}
	
	public ArrayList<Item> getShares(MediaType type) {
		ArrayList<String> stringList = instance.getList("GetShares", type.toString());
		ArrayList<Item> returnList = new ArrayList<Item>();
		for (String share : stringList) {
			String[] sl = share.split(";");
			if (sl.length < 2)
				continue;
			returnList.add(new SimpleItem(sl[0], sl[1]));
		}
		
		return returnList;
	}
	
	public ArrayList<String> getDirectory(String path, Mask mask) {
		if (mask.equals(Mask.All))
			return getDirectory(path);
		else
			return getDirectory(path, mask.toString());
	}
	
	public ArrayList<String> getDirectory(String path) {
		return instance.getList("getDirectory", path);
	}
	
	public ArrayList<String> getDirectory(String path, String mask) {
		return instance.getList("getDirectory", path + ";" + mask);
	}
	
	public String getCurrentPlaylist() {
		return instance.getString("GetCurrentPlaylist");
	}
	
	public MediaControl getMediaControls() {
		return new MediaControl(instance, messenger);
	}
	
	public VideoDatabase getVideoDatabase() {
		return new VideoDatabase(instance, messenger);
	}
	
	public MusicDatabase getMusicDatabase() {
		return new MusicDatabase(instance, messenger);
	}
	
	public boolean isAvailable() {
		return instance.isAvailable();
	}

	public PriorityQueue<Message> getMessenger() {
		return messenger;
	}
	
	public boolean hasMessages() {
		return !messenger.isEmpty();
	}
}

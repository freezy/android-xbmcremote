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

import org.xbmc.httpapi.client.ControlClient;
import org.xbmc.httpapi.client.InfoClient;
import org.xbmc.httpapi.client.MusicClient;
import org.xbmc.httpapi.client.VideoClient;
import org.xbmc.httpapi.type.DirectoryMask;
import org.xbmc.httpapi.type.MediaType;

import android.app.Activity;
import android.content.Context;

/**
 * Wrapper class for our HTTP clients
 * 
 * @author Team XBMC
 */
public class HttpClient {
	
	public final InfoClient info;
	public final MusicClient music;
	public final VideoClient video;
	public final ControlClient control;
	
	private final Connection mConnection;

	public HttpClient(String host, IErrorHandler errorHandler) {
		this(host, -1, null, null, errorHandler);
	}
	
	public HttpClient(String host, int port, IErrorHandler errorHandler) {
		this(host, port, null, null, errorHandler);
	}

	public HttpClient(String host, String username, String password, IErrorHandler errorHandler) {
		this(host, -1, username, password, errorHandler);
	}

	public HttpClient(String host, int port, String username, String password, IErrorHandler errorHandler) {		
		mConnection= new Connection(host, port, username, password, errorHandler);
		info = new InfoClient(mConnection);
		music = new MusicClient(mConnection);
		video = new VideoClient(mConnection);
		control = new ControlClient(mConnection);
	}

	public PriorityQueue<Message> getMessenger() {
		return null;//messenger;
	}
	
	public boolean hasMessages() {
		return false;// !messenger.isEmpty();
	}
}

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

package org.xbmc.android.backend.httpapi;

import org.xbmc.android.util.ConnectionManager;
import org.xbmc.httpapi.client.MusicClient;

import android.os.Handler;
import android.os.Looper;

/**
 * Spawned on first access, then looping. Takes all HTTP API commands and
 * synchronously returns the result.
 * 
 * @author Team XBMC
 */
abstract class HttpApiAbstractThread extends Thread {
	protected Handler mHandler;
	
	protected HttpApiAbstractThread(String name) {
		super(name);
	}
	

	public void run() {
		Looper.prepare();
		mHandler = new Handler();
		Looper.loop();
	}
	
	/**
	 * Returns the MusicClient class
	 * @param handler
	 * @return
	 */
	protected MusicClient music(HttpApiHandler<?> handler) {
		return ConnectionManager.getHttpClient(handler.getActivity()).music;
	}	
	
	/**
	 * Calls the UI thread's callback code.
	 * @param handler
	 */
	protected void done(HttpApiHandler<?> handler) {
		handler.getActivity().runOnUiThread(handler);
	}
	
	protected static void waitForStartup(HttpApiAbstractThread thread) {
		while (thread.mHandler == null) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
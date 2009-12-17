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

package org.xbmc.android.remote.business;

import java.io.IOException;

import org.xbmc.android.util.ClientFactory;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.presentation.INotifiableController;

/**
 * Super class of the wrappers, keeps common code.
 * 
 * @author Team XBMC
 */
public class EventClientManager implements INotifiableManager, IEventClientManager {
	
	protected static final String TAG = "EventClientManager";
	protected static final Boolean DEBUG = false;
	
	private INotifiableController mController = null;
	
	public void setController(INotifiableController controller) {
		mController = controller;
	}

	public void onMessage(int code, String message) {
		onMessage(message);
	}

	public void sendAction(String actionmessage) throws IOException {
		ClientFactory.getEventClient(this).sendAction(actionmessage);
	}

	public void sendButton(short code, boolean repeat, boolean down, boolean queue, short amount, byte axis) throws IOException {
		ClientFactory.getEventClient(this).sendButton(code, repeat, down, queue, amount, axis);
	}

	public void sendButton(String mapName, String buttonName, boolean repeat, boolean down, boolean queue, short amount, byte axis) throws IOException {
		ClientFactory.getEventClient(this).sendButton(mapName, buttonName, repeat, down, queue, amount, axis);
	}

	public void sendLog(byte loglevel, String logmessage) throws IOException {
		ClientFactory.getEventClient(this).sendLog(loglevel, logmessage);
	}

	public void sendMouse(int x, int y) throws IOException {
		ClientFactory.getEventClient(this).sendMouse(x, y);
	}

	public void sendNotification(String title, String message) throws IOException {
		ClientFactory.getEventClient(this).sendNotification(title, message);
	}

	public void sendNotification(String title, String message, byte icontype, byte[] icondata) throws IOException {
		ClientFactory.getEventClient(this).sendNotification(title, message, icontype, icondata);
	}
	
	public void onError(Exception e) {
		if (mController != null) {
			mController.onError(e);
		}
	}

	public void onMessage(String message) {
		if (mController != null) {
			mController.onMessage(message);
		}
	}
}

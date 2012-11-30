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

import org.xbmc.android.util.HostFactory;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.business.IInfoManager;
import org.xbmc.api.business.IMusicManager;
import org.xbmc.api.business.ITvShowManager;
import org.xbmc.api.business.IVideoManager;
import org.xbmc.api.presentation.INotifiableController;

import android.util.Log;

public abstract class ManagerFactory {

	private static EventClientManager sEventClientManager = null;

	private enum ApiType {
		HTTP, JSON_FRODO
	}

	private static ApiType getApiType(INotifiableController controller) {
		
		if(HostFactory.host !=null && HostFactory.host.jsonApi) {
			Log.e("ManagerFactory", "JSON API");
			return ApiType.JSON_FRODO;
		}
		Log.e("ManagerFactory", "HTTP API");
		return ApiType.HTTP;

	}

	public static IInfoManager getInfoManager(INotifiableController controller) {
		switch (getApiType(controller)) {
		case JSON_FRODO:
			org.xbmc.android.remote.business.cm.InfoManager manager = new org.xbmc.android.remote.business.cm.InfoManager();
			manager.setController(controller);
			return manager;
		case HTTP:
			return ManagerThread.info(controller);
		}
		return null;
	}

	public static IControlManager getControlManager(
			INotifiableController controller) {
		switch (getApiType(controller)) {
		case JSON_FRODO:
			org.xbmc.android.remote.business.cm.ControlManager manager = new org.xbmc.android.remote.business.cm.ControlManager();
			manager.setController(controller);
			return manager;
		case HTTP:
			return ManagerThread.control(controller);
		}
		return null;
	}

	public static IVideoManager getVideoManager(INotifiableController controller) {
		switch (getApiType(controller)) {
		case JSON_FRODO:
			org.xbmc.android.remote.business.cm.VideoManager manager = new org.xbmc.android.remote.business.cm.VideoManager();
			manager.setController(controller);
			return manager;
		case HTTP:
			return ManagerThread.video(controller);
		}
		return null;
	}

	public static ITvShowManager getTvManager(INotifiableController controller) {
		switch (getApiType(controller)) {
		case JSON_FRODO:
			org.xbmc.android.remote.business.cm.TvShowManager manager = new org.xbmc.android.remote.business.cm.TvShowManager();
			manager.setController(controller);
			return manager;
		case HTTP:
			return ManagerThread.shows(controller);
		}
		return null;
	}

	public static IMusicManager getMusicManager(INotifiableController controller) {
		switch (getApiType(controller)) {
		case JSON_FRODO:
			org.xbmc.android.remote.business.cm.MusicManager manager = new org.xbmc.android.remote.business.cm.MusicManager();
			manager.setController(controller);
			return manager;
		case HTTP:
			return ManagerThread.music(controller);
		}
		return null;
	}

	public static IEventClientManager getEventClientManager(
			INotifiableController controller) {
		if (sEventClientManager == null) {
			sEventClientManager = new EventClientManager();
		}
		sEventClientManager.setController(controller);
		return sEventClientManager;
	}
}
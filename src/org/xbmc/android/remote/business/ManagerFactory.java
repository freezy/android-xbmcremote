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

import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.business.IInfoManager;
import org.xbmc.api.business.IMusicManager;
import org.xbmc.api.business.IVideoManager;
import org.xbmc.api.presentation.INotifiableController;

import android.content.Context;

public abstract class ManagerFactory {
	
	private static EventClientManager sEventClientManager = null;
	
	public static IInfoManager getInfoManager(Context context, INotifiableController controller) {
		IInfoManager im = ManagerThread.info(context);
		if (controller != null) {
			im.setController(controller);
		}
		return im;
	}
	
	public static IControlManager getControlManager(Context context, INotifiableController controller) {
		IControlManager cm = ManagerThread.control(context);
		if (controller != null) {
			cm.setController(controller);
		}
		return cm;
	}
	
	public static IVideoManager getVideoManager(Context context, INotifiableController controller) {
		IVideoManager vm = ManagerThread.video(context);
		if (controller != null) {
			vm.setController(controller);
		}
		return vm;
	}
	
	public static IMusicManager getMusicManager(Context context, INotifiableController controller) {
		IMusicManager mm = ManagerThread.music(context);
		if (controller != null) {
			mm.setController(controller);
		}
		return mm;
	}
	
	public static IEventClientManager getEventClientManager(Context context, INotifiableController controller) {
		if (sEventClientManager == null) {
			sEventClientManager = new EventClientManager();
		}
		if (controller != null) {
			sEventClientManager.setController(controller);
		}
		return sEventClientManager;
	}
}
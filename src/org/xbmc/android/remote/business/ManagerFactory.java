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
import org.xbmc.api.business.IProfileManager;
import org.xbmc.api.business.ITvShowManager;
import org.xbmc.api.business.IVideoManager;
import org.xbmc.api.presentation.INotifiableController;

public abstract class ManagerFactory {
	
	private static EventClientManager sEventClientManager = null;
	
	public static IInfoManager getInfoManager(INotifiableController controller) {
		return ManagerThread.info(controller);
	}
	public static IControlManager getControlManager(INotifiableController controller) {
		return ManagerThread.control(controller);
	}
	public static IVideoManager getVideoManager(INotifiableController controller) {
		return ManagerThread.video(controller);
	}
	public static ITvShowManager getTvManager(INotifiableController controller) {
		return ManagerThread.shows(controller);
	}
	public static IMusicManager getMusicManager(INotifiableController controller) {
		return ManagerThread.music(controller);
	}
	public static IProfileManager getProfileManager(INotifiableController controller) {
		return ManagerThread.profile(controller);
	}
	public static IEventClientManager getEventClientManager(INotifiableController controller) {
		if (sEventClientManager == null) {
			sEventClientManager = new EventClientManager();
		}
		sEventClientManager.setController(controller);
		return sEventClientManager;
	}
}
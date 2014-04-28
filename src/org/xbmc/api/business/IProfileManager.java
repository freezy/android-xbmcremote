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

package org.xbmc.api.business;

import java.util.ArrayList;

import org.xbmc.api.object.Profile;

import android.content.Context;

/**
 * This is the interface between the presentation layer and the business layer.
 * All the controller of the presentation layer gets to see is this interface.
 *  
 * @author Team XBMC
 */
public interface IProfileManager extends IManager {

	/**
	 * Gets all profiles from database
	 * @param response Response object
	 */
	public void getProfiles(final DataResponse<ArrayList<Profile>> response, final Context context);
	
	/**
	 * Gets the current active profile
	 * @param response Response object
	 */
	public void GetCurrentProfile(final DataResponse<String> response, final Context context);

	/**
	 * Loads a new profile
	 * @param response Response object
	 * @param profileName The new profile to load
	 * @param profilePassword The password for the new profile
	 */
	public void loadProfile(final DataResponse<Boolean> response, final String profileName, final String profilePassword, final Context context);

	/**
	 * Put in here everything that has to be cleaned up after leaving an activity.
	 */
	public void postActivity();
}
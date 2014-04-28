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

package org.xbmc.api.data;

import java.util.ArrayList;

import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.object.Profile;

import android.graphics.Bitmap;


/**
 * This is the interface between the business layer and the presentation layer.
 * All the business layer gets to see is this interface.
 *  
 * @author Team XBMC
 */
public interface IProfileClient extends IClient {
	
	/**
	 * Gets all profiles from database
	 * @return All profiles
	 */
	public ArrayList<Profile> getProfiles(INotifiableManager manager);
	

	/**
	 * Gets the current active profile
	 * @return Current active profile name
	 */
	public String getCurrentProfile(INotifiableManager manager);

	/**
	 * Loads a new profile
	 * @param profileName The new profile to load
	 * @param profilePassword The password for the new profile
	 * @return True on success, false otherwise.
	 */
	public boolean loadProfile(INotifiableManager manager, String profileName, String profilePassword);

	/**
	 * Returns a cover as bitmap
	 * @param cover
	 * @return Cover
	 */
	public Bitmap getCover(INotifiableManager manager, ICoverArt cover, int size);
}
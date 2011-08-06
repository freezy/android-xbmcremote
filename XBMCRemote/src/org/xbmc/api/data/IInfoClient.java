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

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.object.FileLocation;
import org.xbmc.api.type.DirectoryMask;


/**
 * This is the interface between the business layer and the presentation layer.
 * All the business layer gets to see is this interface.
 *  
 * @author Team XBMC
 */
public interface IInfoClient extends IClient {
	
	/**
	 * Returns the contents of a directory
	 * @param path    Path to the directory
	 * @param mask    Mask to filter
	 * @param offset  Offset (0 for none)
	 * @param limit   Limit (0 for none)
	 * @return
	 */
	public ArrayList<FileLocation> getDirectory(INotifiableManager manager, String path, DirectoryMask mask, int offset, int limit, int mediaType);
	
	/**
	 * Returns all the contents of a directory
	 * @param path    Path to the directory
	 * @return
	 */
	public ArrayList<FileLocation> getDirectory(INotifiableManager manager, String path, int mediaType);

	
	/**
	 * Returns all defined shares of a media type
	 * @param mediaType Media type
	 * @return
	 */
	public ArrayList<FileLocation> getShares(INotifiableManager manager, int mediaType);
	
	/**
	 * Returns URI of the currently playing's thumbnail.
	 * @return
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public String getCurrentlyPlayingThumbURI(INotifiableManager manager) throws MalformedURLException, URISyntaxException;
	
	/**
	 * Returns any system info variable, see {@link org.xbmc.api.info.SystemInfo}
	 * @param field Field to return
	 * @return
	 */
	public String getSystemInfo(INotifiableManager manager, int field);
	
	/**
	 * Returns a boolean GUI setting
	 * @param field
	 * @return
	 */
	public boolean getGuiSettingBool(INotifiableManager manager, int field);

	/**
	 * Returns an integer GUI setting
	 * @param field
	 * @return
	 */
	public int getGuiSettingInt(INotifiableManager manager, int field);
	
	/**
	 * Returns a boolean GUI setting
	 * @param field
	 * @param value Value
	 * @return
	 */
	public boolean setGuiSettingBool(INotifiableManager manager, int field, boolean value);
	
	/**
	 * Returns an integer GUI setting
	 * @param field
	 * @param value Value
	 * @return
	 */
	public boolean setGuiSettingInt(INotifiableManager manager, int field, int value);
	
	/**
	 * Returns any music info variable see {@link org.xbmc.http.info.MusicInfo}
	 * @param field Field to return
	 * @return
	 */
	public String getMusicInfo(INotifiableManager manager, int field);

	/**
	 * Returns any video info variable see {@link org.xbmc.http.info.VideoInfo}
	 * @param field Field to return
	 * @return
	 */
	public String getVideoInfo(INotifiableManager manager, int field);

}
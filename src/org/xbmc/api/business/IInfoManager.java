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

import org.xbmc.api.object.FileLocation;
import org.xbmc.api.type.DirectoryMask;

import android.content.Context;

/**
 * This is the interface between the presentation layer and the business layer.
 * All the controller of the presentation layer gets to see is this interface.
 *  
 * @author Team XBMC
 */
public interface IInfoManager extends IManager {
	
	/**
	 * Returns any system info variable, see {@link org.xbmc.api.info.SystemInfo}
	 * @param response Response object
	 * @param field Field to return
	 */
	public void getSystemInfo(final DataResponse<String> response, final int field, final Context context);
	
	/**
	 * Returns all defined shares of a media type
	 * @param response Response object
	 * @param mediaType Media type
	 */
	public void getShares(final DataResponse<ArrayList<FileLocation>> response, final int mediaType, final Context context);
	
	/**
	 * Returns the contents of a directory
	 * @param response Response object
	 * @param path     Path to the directory
	 * @param mask     Mask to filter
	 * @param offset   Offset (0 for none)
	 * @param limit    Limit (0 for none)
	 * @return
	 */
	public void getDirectory(final DataResponse<ArrayList<FileLocation>> response, final String path, final DirectoryMask mask, final int offset, final int limit, final Context context);
	
	/**
	 * Returns the contents of a directory
	 * @param response Response object
	 * @param path     Path to the directory
	 * @return
	 */
	public void getDirectory(final DataResponse<ArrayList<FileLocation>> response, final String path, final Context context);
	
	/**
	 * Returns the gui setting of XBMC
	 * @param response Response object
	 * @param setting  see {@link org.xbmc.api.info.GuiSettings} for all settings you can query.
	 * @param context 
	 */
	public void getGuiSettingInt(final DataResponse<Integer> response, final int setting, final Context context);
	
	/**
	 * Returns the gui setting of XBMC
	 * @param response Response object
	 * @param setting  see {@link org.xbmc.api.info.GuiSettings} for all settings you can query.
	 * @param context 
	 */
	public void getGuiSettingBool(final DataResponse<Boolean> response, final int setting, final Context context);
	
}
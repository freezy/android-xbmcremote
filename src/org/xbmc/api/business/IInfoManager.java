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
import org.xbmc.httpapi.type.DirectoryMask;

/**
 * This is the interface between the presentation layer and the business layer.
 * All the controller of the presentation layer gets to see is this interface.
 *  
 * @author Team XBMC
 */
public interface IInfoManager {
	
	/**
	 * Returns any system info variable, see {@link org.xbmc.httpapi.info.SystemInfo}
	 * @param response Response object
	 * @param field Field to return
	 */
	public void getSystemInfo(final DataResponse<String> response, final int field);
	
	/**
	 * Returns all defined shares of a media type
	 * @param response Response object
	 * @param mediaType Media type
	 */
	public void getShares(final DataResponse<ArrayList<FileLocation>> response, final int mediaType);
	
	/**
	 * Returns the contents of a directory
	 * @param response Response object
	 * @param path     Path to the directory
	 * @param mask     Mask to filter
	 * @param offset   Offset (0 for none)
	 * @param limit    Limit (0 for none)
	 * @return
	 */
	public void getDirectory(final DataResponse<ArrayList<FileLocation>> response, final String path, final DirectoryMask mask, final int offset, final int limit);
	
	/**
	 * Returns the contents of a directory
	 * @param response Response object
	 * @param path     Path to the directory
	 * @return
	 */
	public void getDirectory(final DataResponse<ArrayList<FileLocation>> response, final String path);
	
}
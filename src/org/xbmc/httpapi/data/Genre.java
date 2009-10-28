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

package org.xbmc.httpapi.data;

import java.io.Serializable;


/**
 * Genre is basically a name and an ID.
 * 
 * @author Team XBMC
 */
public class Genre implements Serializable, NamedResource {

	/**
	 * Constructor
	 * @param id		Database ID
	 * @param name		Album name
	 * @param artist	Artist
	 */
	public Genre(int id, String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * Returns database ID.
	 * @return
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Something descriptive
	 */
	public String toString() {
		return "[" + this.id + "] " + this.name;
	}
	
	/**
	 * Database ID
	 */
	public int id;
	/**
	 * Genre name
	 */
	public String name;
	
	public String getShortName() {
		return this.name;
	}
	
	private static final long serialVersionUID = 9073064679039418773L;
}

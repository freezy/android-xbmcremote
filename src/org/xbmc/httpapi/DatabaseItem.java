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

package org.xbmc.httpapi;

public class DatabaseItem {
	private String ID, nameID, data;
	@SuppressWarnings("unused")
	private String thumbID;
	
	public DatabaseItem(String ID, String nameID, String data) {
		this(ID, nameID, data, "");
	}
	
	public DatabaseItem(String ID, String nameID, String data, String thumbID) {
		this.ID = ID;
		this.nameID = nameID;
		this.data = data;
		this.thumbID = thumbID;
	}
	
	public String getData() {
		return data;
	}

	public String formatSQL() {
		return nameID + "='" + ID + "'";
	}
	
	public String toString() {
		return data;
	}
}

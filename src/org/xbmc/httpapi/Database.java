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

import java.util.ArrayList;

public abstract class Database {
	Connection instance;
	String queryString;

	public Database(Connection instance, String queryString) {
		this.instance = instance;
		this.queryString = queryString;
	}

/*	protected ArrayList<DatabaseItem> getMergedList(String nameID, String sqlQuery) {
		ArrayList<String> list = instance.getList(queryString, sqlQuery);
		ArrayList<DatabaseItem> returnList = new ArrayList<DatabaseItem>();
		
		for (String s : list) {
			if (s.startsWith("<field>")) {
				s = s.substring(7);
				String[] data = s.split("<field>");
				if (data.length >= 2)
					returnList.add(new DatabaseItem(data[0], nameID,  data[1], data.length > 2 ? data[2] : ""));
			}
		}
		
		return returnList;
	}
	*/
	
	/**
	 * Removes the trailing "</field>" string from the value
	 * @param value
	 * @return Trimmed value
	 */
	protected static String trim(String value) {
		return value.substring(0, value.length() - 8);
	}	
	
	/**
	 * Removes the trailing "</field>" string from the value and tries to
	 * parse an integer from it. On error, returns -1.
	 * @param value
	 * @return Parsed integer from field value
	 */
	protected static int trimInt(String value) {
		String trimmed = trim(value);
		if (trimmed.length() > 0) {
			try {
				return Integer.parseInt(trimmed);
			} catch (NumberFormatException e) {
				return -1;
			}
		} else {
			return -1;
		}
	}
}
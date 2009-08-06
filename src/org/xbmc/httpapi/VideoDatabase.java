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
import java.util.PriorityQueue;

public class VideoDatabase extends Database {
	protected VideoDatabase(HttpApiConnection instance, PriorityQueue<Message> messenger) {
		super(instance, "QueryVideoDatabase");
	}
	
	/**
	 * Get all TVShows in database
	 * @return list of TV Show names
	 */
	public ArrayList<DatabaseItem> getTVShows() {
		return getMergedList("idShow", "SELECT idShow, c00 from tvshow ORDER BY c00");
	}
	
	/**
	 * Get all Movies in database
	 * @return list of Movie names
	 */
	public ArrayList<DatabaseItem> getMovies() {
		return getMergedList("idMovie", "SELECT idMovie, c00 from movie ORDER BY c00");
	}
}

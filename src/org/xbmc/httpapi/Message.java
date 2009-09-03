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

import java.util.Date;

import org.xbmc.httpapi.type.LogType;

public class Message implements Comparable<Message> {
	private LogType level;
	private String message;
	private Date time;

	public Message(LogType level, String message) {
		this.level = level;
		this.message = message;
		time = new Date();
	}
	
	public String toString() {
		return level.toString() + ": " + message;
	}
	
	public int compareTo(Message arg) {
		if (arg.level == level)
			return time.compareTo(arg.time);
		else
			return arg.level.compareTo(level);
	}
}

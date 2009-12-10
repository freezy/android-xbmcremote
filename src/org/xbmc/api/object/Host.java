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

package org.xbmc.api.object;

import java.io.Serializable;

/**
 * Just a data container for connection data of an XBMC instance
 * 
 * @author Team XBMC
 */
public class Host implements Serializable {
	/**
	 * Database ID
	 */
	public int id;
	/**
	 * Name (description/label) of the host
	 */
	public String name;
	/**
	 * IP address or host name of the host
	 */
	public String addr;
	/**
	 * HTTP API Port
	 */
	public int port;
	/**
	 * User name of in case of HTTP authentication
	 */
	public String user;
	/**
	 * Password of in case of HTTP authentication
	 */
	public String pass;
	
	/**
	 * Something readable
	 */
	public String toString() {
		return addr + ":" + port;
	}
	
	public String getSummary() {
		return toString();
	}
	
	private static final long serialVersionUID = 7886482294339161092L;
	
}
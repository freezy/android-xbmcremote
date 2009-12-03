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

package org.xbmc.api.info;

/**
 * Describes a play status, which can be:
 * <ul>
 * 	<li>Stopped</li>
 * 	<li>Paused</li>
 * 	<li>Playing</li>
 * </ul>
 * 
 * @author Team XBMC
 */
public abstract class PlayStatus {
	public static final int STOPPED = 0;
	public static final int PAUSED = 1;
	public static final int PLAYING = 2;
	
	public static int parse(String response) {
		if (response.contains("PlayStatus:Paused") || response.equals("Paused")) {
			return PlayStatus.PAUSED;
		} else if (response.contains("PlayStatus:Playing") || response.equals("Playing")) {
			return PlayStatus.PLAYING;
		} else {
			return PlayStatus.STOPPED;
		}
	}
}
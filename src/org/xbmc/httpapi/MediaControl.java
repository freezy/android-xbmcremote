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

import java.util.Queue;

import org.xbmc.httpapi.type.SeekType;

public class MediaControl {
	private HttpApiConnection instance;
	
	protected MediaControl(HttpApiConnection instance, Queue<Message> messenger) {
		this.instance = instance;
	}
	
	public int getPercentage() {
		return instance.getInt("GetPercentage");
	}
	
	public int getVolume() {
		return instance.getInt("GetVolume");
	}
	
	public boolean setVolume(int volume) {
		return instance.executeBooleanResponseCommand("SetVolume(" + volume + ")");
	}
	
	public boolean seek(SeekType type, int progress) {
		if (type.compareTo(SeekType.absolute) == 0)
			return instance.executeBooleanResponseCommand("SeekPercentage", new Integer(progress).toString());
		else
			return instance.executeBooleanResponseCommand("SeekPercentageRelative", new Integer(progress).toString());
	}
	
	public boolean mute() {
		return instance.executeBooleanResponseCommand("Mute");
	}
	
	public boolean pause() {
		return instance.executeBooleanResponseCommand("Pause");
	}
	
	public boolean playFile(String url) {
		return instance.executeBooleanResponseCommand("PlayFile", url);
	}
	
	public boolean playNext() {
		return instance.executeBooleanResponseCommand("PlayNext");
	}
	
	public boolean playPrevious() {
		return instance.executeBooleanResponseCommand("PlayPrev");
	}
	
	public boolean stop() {
		return instance.executeBooleanResponseCommand("Stop");
	}
	
	public boolean navUp() {
		return instance.executeBooleanResponseCommand("Action", "3");
	}

	public boolean navDown() {
		return instance.executeBooleanResponseCommand("Action", "4");
	}
	
	public boolean navLeft() {
		return instance.executeBooleanResponseCommand("Action", "1");
	}
	
	public boolean navRight() {
		return instance.executeBooleanResponseCommand("Action", "2");
	}
	
	public boolean navSelect() {
		return instance.executeBooleanResponseCommand("Action", "7");
	}
}

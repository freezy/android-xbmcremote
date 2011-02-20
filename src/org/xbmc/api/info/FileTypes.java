/*
 *      Copyright (C) 2005-2011 Team XBMC
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
 * Defines known media file types and some helper methods.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class FileTypes {
	
	/**
	 * Audio file extensions
	 */
	public final static String[] AUDIO = { "ac3", "flac", "m4a", "mp3", "mid", "ogg", "wav" };
	
	/**
	 * Playlist file extensions
	 */
	public final static String[] PLAYLIST = { "m3u", "pls" };
	
	/**
	 * Video extensions
	 */
	public final static String[] VIDEO = { "avi", "flv", "mkv", "mov", "mp4", "mpg", "mpeg", "ts", "wmv", "vob" };
	
	/**
	 * Image extensions
	 */
	public final static String[] PICTURE = { "bmp", "gif", "jpeg", "jpg", "png", "tbn" };
	
	/**
	 * Returns true if extensions is of type audio.
	 * @param extension Extension to check, without "."
	 * @return true if audio extension, false otherwise.
	 */
	public static boolean isAudio(String extension) {
		return is(AUDIO, extension);
	}
	
	/**
	 * Returns true if extensions is of type audio.
	 * @param extension Extension to check, without "."
	 * @return true if audio extension, false otherwise.
	 */
	public static boolean isAudioOrPlaylist(String extension) {
		return is(AUDIO, extension) || is(PLAYLIST, extension);
	}

	/**
	 * Returns true if extensions is of type video.
	 * @param extension Extension to check, without "."
	 * @return true if video extension, false otherwise.
	 */
	public static boolean isVideo(String extension) {
		return is(VIDEO, extension);
	}
	
	/**
	 * Returns true if extensions is of type playlist.
	 * @param extension Extension to check, without "."
	 * @return true if playlist extension, false otherwise.
	 */
	public static boolean isPicture(String extension) {
		return is(PICTURE, extension);
	}

	/**
	 * Returns true if extensions is of type picture.
	 * @param extension Extension to check, without "."
	 * @return true if picture extension, false otherwise.
	 */
	public static boolean isPlaylist(String extension) {
		return is(PLAYLIST, extension);
	}
	
	/**
	 * Returns the file extension of a file name or path in lower case.
	 * @param filenameOrPath File name or path
	 * @return File extension without "."
	 */
	public static String getExtension(String filenameOrPath) {
		return filenameOrPath.substring(filenameOrPath.lastIndexOf(".") + 1).toLowerCase();
	}
	
	private static boolean is(String[] arr, String extension) {
		for (String audioExt : arr) {
			if (audioExt.equals(extension)) {
				return true;
			}
		}
		return false;
	}
}

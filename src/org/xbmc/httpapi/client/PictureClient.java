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

package org.xbmc.httpapi.client;

import java.util.HashMap;

import org.xbmc.api.data.IControlClient.ICurrentlyPlaying;
import org.xbmc.api.data.IControlClient.PlayStatus;
import org.xbmc.api.type.MediaType;

/**
 * Takes care of Picture/SlideShow stuff
 * 
 * @author Team XBMC
 *
 */
public class PictureClient {

	public static final String TAG = "PictureClient";
	
	static ICurrentlyPlaying getCurrentlyPlaying(final HashMap<String, String> map) {
		return new ICurrentlyPlaying() {
			private static final long serialVersionUID = 5036994329211476713L;
			
			public int getMediaType() {
				return MediaType.PICTURES;
			}

			public String getAlbum() {
				String[] path = map.get("Filename").split("/");
				return path[path.length - 2];
			}

			public String getArtist() {
				return "Image";
			}

			public int getDuration() {
				return 0;
			}

			public String getFilename() {
				return map.get("Filename");
			}

			public float getPercentage() {
				// TODO Auto-generated method stub
				return 0;
			}

			public PlayStatus getPlayStatus() {
				return PlayStatus.Playing;
			}

			public int getPlaylistPosition() {
				// TODO Auto-generated method stub
				return 0;
			}

			public int getTime() {
				return 0;
			}

			public String getTitle() {
				String[] path = map.get("Filename").split("/");
				return path[path.length - 1];
				//return map.get("Filename").substring(map.get("Filename").lastIndexOf("/") + 1);
			}

			public boolean isPlaying() {
				return true;
			}

			public int getHeight() {
				return parseHeight(map.get("Resolution"));
			}

			public int getWidth() {
				return parseWidth(map.get("Resolution"));
			}
			
			private int parseHeight(String resolution){
				String[] xy = resolution.split("x");
				if (xy.length != 2)
					return 0;
				try{
					return Integer.parseInt(xy[1].trim());
				} catch (NumberFormatException e) {
					return 0;
				}
			}
			private int parseWidth(String resolution){
				String[] xy = resolution.split("x");
				if (xy.length != 2)
					return 0;
				try{
					return Integer.parseInt(xy[0].trim());
				} catch (NumberFormatException e) {
					return 0;
				}
			}			
		};
	}
}
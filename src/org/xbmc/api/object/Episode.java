/*
 *      Copyright (C) 2005-2010 Team XBMC
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

import java.util.ArrayList;

import org.xbmc.android.util.Crc32;
import org.xbmc.api.type.MediaType;

public class Episode implements ICoverArt {
	
	public final static String TAG = "Episode";
	
	/**
	 * Database primary key
	 */
	public int id;

	/**
	 * Local path of this episode (without file name)
	 */
	public String localPath;
	
	/**
	 * File name of this episode
	 */
	public String fileName;
	
	/**
	 * Title of this episode
	 */
	public String title;
	/**
	 * Plotsummary
	 */
	public String plot;
	/**
	 * Rating of this episode
	 */
	public double rating = 0.0;
	/**
	 * Writer of this episode
	 */
	public String writer;
	public String firstAired;
	
	/**
	 * Number of watched, -1 if not set.
	 */
	public int numWatched = -1;
	public String director;
	public int season;
	
	/**
	 * Number of this episode within the season
	 */
	public int episode;
	
	/**
	 * Title of the TV Show
	 */
	public String showTitle;
		
	public ArrayList<Actor> actors = null;
	
	public Episode(int id, String title, String plot, double rating, String writer, String firstAired,
			int numWatched, String director, int season, int episode, String localPath, String fileName, String showTitle) {
		this.id = id;
		this.title = title;
		this.plot = plot;
		this.rating = rating;
		this.writer = writer;
		this.firstAired = firstAired;
		this.numWatched = numWatched;
		this.director = director;
		this.season = season;
		this.episode = episode;
		this.localPath = localPath;
		this.showTitle = showTitle;
		this.fileName = fileName;
	}

	public long getCrc() {
		if (fileName.contains("://"))
		   return  Crc32.computeLowerCase(fileName);
		else 
			return  Crc32.computeLowerCase(localPath + fileName);			
	}

	/**
	 * Returns CRC for episode thumb. From FileItem.cpp(2597):
	 * <pre>
	 * 	CStdString strCRC;
	 *	strCRC.Format("%sepisode%i",GetVideoInfoTag()->m_strFileNameAndPath.c_str(),GetVideoInfoTag()->m_iEpisode);
	 *	return GetCachedThumb(strCRC,g_settings.GetVideoThumbFolder(),true);
	 * </pre>
	 */
	public int getFallbackCrc() {
		if (fileName.contains("://"))
			return Crc32.computeLowerCase(fileName + "episode" + episode);
		else
			return Crc32.computeLowerCase(localPath +fileName + "episode" + episode);
	}

	public int getId() {
		return id;
	}

	public int getMediaType() {
		return MediaType.VIDEO_TVEPISODE;
	}

	public String getName() {
		return season + "x" + episode + ": " + title;
	}

	/**
	 * Returns the path XBMC needs to play the episode. This can either
	 * localPath + filename or filename only (in case of stacks) 
	 * @return
	 */
	public String getPath() {
		   if (fileName.contains("://")) {
			   return fileName;
		   } 
		   else {
				return localPath + fileName;
			}
	}
	private static final long serialVersionUID = 5317212562013683169L;	
}

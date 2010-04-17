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
	
	public String localPath;
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
	public boolean watched;
	public String director;
	public int season;
	/**
	 * Number of this episode within the season
	 */
	public int episode;
	
	public ArrayList<Actor> actors = null;
	
	public Episode(int id, String title, String plot, double rating, String writer, String firstAired,
			boolean watched, String director, int season, int episode, String localPath) {
		this.id = id;
		this.title = title;
		this.plot = plot;
		this.rating = rating;
		this.writer = writer;
		this.firstAired = firstAired;
		this.watched = watched;
		this.director = director;
		this.season = season;
		this.episode = episode;
		this.localPath = localPath;
	}

	public long getCrc() {
		return Crc32.computeLowerCase(localPath);
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
		return Crc32.computeLowerCase(localPath + "episode" + episode);
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

	public String getPath() {
		return localPath;
	}
	private static final long serialVersionUID = 5317212562013683169L;	
}

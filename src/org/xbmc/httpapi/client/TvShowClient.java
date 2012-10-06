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

package org.xbmc.httpapi.client;

import java.util.ArrayList;
import java.util.HashMap;

import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.ITvShowClient;
import org.xbmc.api.data.IControlClient.ICurrentlyPlaying;
import org.xbmc.api.info.PlayStatus;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Episode;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.Host;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.object.Season;
import org.xbmc.api.object.TvShow;
import org.xbmc.api.type.MediaType;
import org.xbmc.api.type.SortType;
import org.xbmc.httpapi.Connection;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * TV show client for HTTP API.
 * <br /><pre>
 * DB Fields:
 * 	- c00: title
 * 	- c01: summary
 * 	- c02, c03: empty
 * 	- c04: rating
 * 	- c05: first aired (YYYY-mm-dd)
 * 	- c06: thumb urls (found online thumbs, in xml)
 * 	- c07: empty
 * 	- c08: genres (separated by "/")
 * 	- c09: empty
 * 	- c10: tvdb urls
 * 	- c11: fanart urls
 *	- c12: tvdb show id
 *	- c13: content rating
 *	- c14: network name
 *	- c15-c20: empty
 *	-> plus strPath, totalCount, watchedCount, watched
 * </pre>
 * <pre>
 * Episode DB Fields:
 * idEpisode 	 integer 	 Primary Key
 * c00: Episode Title
 * c01:	Plot Summary
 * c02:	[unknown]
 * c03: Rating
 * c04: Writer
 * c05:	First Aired
 * c06:	Thumbnail URL
 * c07:	[unknown]
 * c08: Has the episode been watched?
 * c09: [unknown]
 * c10: Director
 * c11: [unknown]
 * c12: Season
 * c13: Episode Number
 * idFile: Foreign key to the files table
 * </pre>
 * @author Team XBMC
 */
public class TvShowClient extends Client implements ITvShowClient {

	private static final String TAG = "TvShowClient";

	public TvShowClient(Connection connection) {
		super(connection);
	}
	
	public ArrayList<TvShow> getTvShows(INotifiableManager manager, int sortBy, String sortOrder, boolean hideWatched) {
		StringBuilder sb = new StringBuilder();
		
		// don't fetch summary for list view
		sb.append("SELECT tvshow.idShow, tvshow.c00, \"\" AS c01, ROUND(tvshow.c04, 2), tvshow.c05, tvshow.c08, tvshow.c13, tvshow.c14,"); 
		sb.append("		paths.strPath,");
		sb.append("		counts.totalcount AS totalCount,");
		sb.append("		counts.watchedcount AS watchedCount,");
		sb.append("		counts.totalcount = counts.watchedcount AS watched,");
		sb.append("		art.url AS artUrl");
		sb.append("	FROM (");
		sb.append("		select min(tvshow.idShow) as idShow, tvshow.c00 from tvshow");
		sb.append("		group by tvshow.c00");
		sb.append("	) showNames");
		sb.append("	LEFT OUTER join tvshow on showNames.idShow = tvshow.idShow");
		sb.append("	LEFT OUTER join (");
		sb.append("		SELECT min(tvshow.idShow) as idShow, tvshow.c00, count(1) AS totalcount, count(files.playCount) AS watchedcount");
		sb.append(" 	FROM tvshow");
		sb.append("		JOIN episode ON episode.idShow = tvshow.idShow");
		sb.append("		JOIN files ON files.idFile = episode.idFile");
		sb.append("		GROUP BY tvshow.c00");
		sb.append("	) counts ON tvshow.idShow = counts.idShow");
		sb.append("	LEFT OUTER join (");
		sb.append("		SELECT tvshow.idShow, strPath ");
		sb.append("		FROM tvshow");
		sb.append("		JOIN tvshowlinkpath ON tvshow.idShow = tvshowlinkpath.idShow");
		sb.append("		JOIN path ON path.idpath = tvshowlinkpath.idPath");
		sb.append("		WHERE path.idPath in (SELECT max(idPath) FROM tvshowlinkpath GROUP BY idShow)");
		sb.append("	)  paths on tvshow.idShow = paths.idShow ");
		sb.append(" LEFT OUTER JOIN ");
		sb.append("		art ON art.media_id=tvshow.idShow AND art.media_type='tvshow' AND art.type='thumb'");
		if (hideWatched) {
			sb.append(" WHERE counts.totalcount > counts.watchedcount ");
		}
		sb.append(showsOrderBy(sortBy, sortOrder));
		Log.i(TAG, sb.toString());
		
				
/*		sb.append("SELECT tvshow.idShow, c00, c01, c04, c05, c08, c13, c14, strPath FROM tvshow, path, tvshowlinkpath");
		sb.append(" WHERE tvshow.idShow = tvshowlinkpath.idShow");
		sb.append(" AND path.idPath = tvshowlinkpath.idPath");*/
		return parseShows(mConnection.query("QueryVideoDatabase", sb.toString(), manager));
	}
	
	/**
	 * Gets all tv show actors from database
	 * @return All tv show actors
	 */
	public ArrayList<Actor> getTvShowActors(INotifiableManager manager) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT actors.idActor, strActor FROM actors, actorlinktvshow");
		sb.append(" WHERE actorlinktvshow.idActor = actors.idActor");
		sb.append(" ORDER BY upper(strActor), strActor");
		return VideoClient.parseActors(mConnection.query("QueryVideoDatabase", sb.toString(), manager));
	}
	
	/**
	 * Gets all tv show genres from database
	 * @return All tv show genres
	 */
	public ArrayList<Genre> getTvShowGenres(INotifiableManager manager) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT idGenre, strGenre FROM genre");
		sb.append(" WHERE idGenre IN (SELECT idGenre FROM genrelinktvshow)");
		sb.append(" ORDER BY upper(strGenre)");
		return VideoClient.parseGenres(mConnection.query("QueryVideoDatabase", sb.toString(), manager));
	}
	
	/**
	 * Gets all tv shows with the specified actor
	 * @param manager
	 * @param actor
	 * @return
	 */
	public ArrayList<TvShow> getTvShows(INotifiableManager manager, Actor actor, int sortBy, String sortOrder, boolean hideWatched) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT tvshow.idShow, tvshow.c00, \"\" AS c01, ROUND(tvshow.c04, 2), tvshow.c05, tvshow.c08, tvshow.c13, tvshow.c14,"); 
		sb.append("		paths.strPath,");
		sb.append("		counts.totalcount AS totalCount,");
		sb.append("		counts.watchedcount AS watchedCount,");
		sb.append("		counts.totalcount = counts.watchedcount AS watched,");
		sb.append("		art.url AS artUrl");
		sb.append("	FROM (");
		sb.append("		select min(tvshow.idShow) as idShow, tvshow.c00 from tvshow");
		sb.append("		group by tvshow.c00");
		sb.append("	) showNames");
		sb.append("	LEFT OUTER join tvshow on showNames.idShow = tvshow.idShow");
		sb.append("	LEFT OUTER join (");
		sb.append("		SELECT min(tvshow.idShow) as idShow, tvshow.c00, count(1) AS totalcount, count(files.playCount) AS watchedcount");
		sb.append(" 	FROM tvshow");
		sb.append("		JOIN episode ON episode.idShow = tvshow.idShow");
		sb.append("		JOIN files ON files.idFile = episode.idFile");
		sb.append("		GROUP BY tvshow.c00");
		sb.append("	) counts ON tvshow.idShow = counts.idShow");
		sb.append("	LEFT OUTER join (");
		sb.append("		SELECT tvshow.idShow, strPath ");
		sb.append("		FROM tvshow");
		sb.append("		JOIN tvshowlinkpath ON tvshow.idShow = tvshowlinkpath.idShow");
		sb.append("		JOIN path ON path.idpath = tvshowlinkpath.idPath");
		sb.append("		WHERE path.idPath in (SELECT max(idPath) FROM tvshowlinkpath GROUP BY idShow)");
		sb.append("	)  paths on tvshow.idShow = paths.idShow ");
		sb.append("	LEFT OUTER join (");
		sb.append("		SELECT min(tvshow.idShow) as idShow, actorlinktvshow.idActor FROM tvshow");
		sb.append("		LEFT OUTER JOIN actorlinktvshow ON tvshow.idShow = actorlinktvshow.idShow");
		sb.append("		GROUP BY tvshow.c00, actorlinktvshow.idActor");
		sb.append("	) actors on tvshow.idShow = actors.idShow");
		sb.append(" LEFT OUTER JOIN ");
		sb.append("		art ON art.media_id=tvshow.idShow AND art.media_type='tvshow' AND art.type='thumb'");
		sb.append("	WHERE actors.idActor = ");
		sb.append(actor.id);
		if (hideWatched) {
			sb.append(" AND counts.totalcount > counts.watchedcount ");
		}
		sb.append(showsOrderBy(sortBy, sortOrder));
		
		Log.i(TAG, sb.toString());
		return parseShows(mConnection.query("QueryVideoDatabase", sb.toString(), manager));
	}
	
	/**
	 * Gets all tv shows for the specified genre
	 * 
	 */
	public ArrayList<TvShow> getTvShows(INotifiableManager manager, Genre genre, int sortBy, String sortOrder, boolean hideWatched) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT tvshow.idShow, tvshow.c00, \"\" AS c01, ROUND(tvshow.c04, 2), tvshow.c05, tvshow.c08, tvshow.c13, tvshow.c14,"); 
		sb.append("		paths.strPath,");
		sb.append("		counts.totalcount AS totalCount,");
		sb.append("		counts.watchedcount AS watchedCount,");
		sb.append("		counts.totalcount = counts.watchedcount AS watched,");
		sb.append("		art.url AS artUrl");
		sb.append("	FROM (");
		sb.append("		select min(tvshow.idShow) as idShow, tvshow.c00 from tvshow");
		sb.append("		group by tvshow.c00");
		sb.append("	) showNames");
		sb.append("	LEFT OUTER join tvshow on showNames.idShow = tvshow.idShow");
		sb.append("	LEFT OUTER join (");
		sb.append("		SELECT min(tvshow.idShow) as idShow, tvshow.c00, count(1) AS totalcount, count(files.playCount) AS watchedcount");
		sb.append(" 	FROM tvshow");
		sb.append("		JOIN episode ON episode.idShow = tvshow.idShow");
		sb.append("		JOIN files ON files.idFile = episode.idFile");
		sb.append("		GROUP BY tvshow.c00");
		sb.append("	) counts ON tvshow.idShow = counts.idShow");
		sb.append("	LEFT OUTER join (");
		sb.append("		SELECT tvshow.idShow, strPath ");
		sb.append("		FROM tvshow");
		sb.append("		JOIN tvshowlinkpath ON tvshow.idShow = tvshowlinkpath.idShow");
		sb.append("		JOIN path ON path.idpath = tvshowlinkpath.idPath");
		sb.append("		WHERE path.idPath in (SELECT max(idPath) FROM tvshowlinkpath GROUP BY idShow)");
		sb.append("	)  paths on tvshow.idShow = paths.idShow ");
		sb.append("	LEFT OUTER join (");
		sb.append("		SELECT min(tvshow.idShow) as idShow, genrelinktvshow.idGenre FROM tvshow");
		sb.append("		LEFT OUTER JOIN genrelinktvshow ON tvshow.idShow = genrelinktvshow.idShow");
		sb.append("		GROUP BY tvshow.c00, genrelinktvshow.idGenre");
		sb.append("	) genres on tvshow.idShow = genres.idShow");
		sb.append(" LEFT OUTER JOIN ");
		sb.append("		art ON art.media_id=tvshow.idShow AND art.media_type='tvshow' AND art.type='thumb'");
		sb.append("	WHERE genres.idGenre = ");
		sb.append(genre.id);
		if (hideWatched) {
			sb.append(" AND counts.totalcount > counts.watchedcount ");
		}
		sb.append(showsOrderBy(sortBy, sortOrder));
		
		Log.i(TAG, sb.toString());
		return parseShows(mConnection.query("QueryVideoDatabase", sb.toString(), manager));
	}
	
	/**
	 * Gets all seasons for the specified show
	 * @param manager
	 * @param show
	 * @return
	 */
	public ArrayList<Season> getSeasons(INotifiableManager manager, TvShow show, boolean hideWatched) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT c12, art.url from (");
		sb.append("	SELECT episode.c12, tvshow.idshow ");
		sb.append("	FROM tvshow ");
		sb.append("		LEFT OUTER JOIN episode ON episode.idShow = tvshow.idShow ");
		if (hideWatched) {
			sb.append(" LEFT OUTER JOIN files ON files.idFile = episode.idFile ");
		}
		sb.append("	WHERE tvshow.c00 = (SELECT c00 FROM tvshow WHERE tvshow.idShow = ");
		sb.append(show.id);
		sb.append("	) ");
		if (hideWatched) {
			sb.append(" AND (files.playCount IS NULL OR files.playCount = 0) ");
		}
		sb.append("GROUP BY episode.c12, tvshow.c00 ");
		sb.append(") q");
		sb.append(" LEFT OUTER JOIN seasons ON seasons.idShow=q.idShow AND seasons.season=q.c12");
		sb.append(" LEFT OUTER JOIN art ON art.media_id=seasons.idSeason AND art.media_type='season' AND art.type='thumb'");
		sb.append(" where not q.c12 is null ");
		sb.append("ORDER BY q.c12+0");
		
		return parseSeasons(mConnection.query("QueryVideoDatabase", sb.toString(), manager), show);
	}
	
	/**
	 * Gets all seasons for all shows
	 * @param manager
	 * @param show
	 * @return
	 */
	public ArrayList<Season> getSeasons(INotifiableManager manager, int sortBy, String sortOrder, boolean hideWatched) {
		ArrayList<TvShow> shows = getTvShows(manager, sortBy, sortOrder, hideWatched);
		HashMap<Integer, TvShow> showMap = new HashMap<Integer, TvShow>();
		for (TvShow tvShow : shows) {
			showMap.put(tvShow.id, tvShow);
		}
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT min(tvshow.idShow) as idShow, episode.c12, art.url ");
		sb.append("FROM tvshow ");
		sb.append("	LEFT OUTER JOIN episode ON episode.idShow = tvshow.idShow ");
		if (hideWatched) {
			sb.append(" LEFT OUTER JOIN files ON files.idFile = episode.idFile ");
		}
		sb.append(" LEFT OUTER JOIN seasons ON seasons.idShow=tvshow.idShow AND seasons.season=episode.c12");
		sb.append(" LEFT OUTER JOIN art ON art.media_id=seasons.idSeason AND art.media_type='season' AND art.type='thumb'");
		sb.append("WHERE NOT episode.c12 is null ");
		if (hideWatched) {
			sb.append(" AND (files.playCount IS NULL OR files.playCount = 0) ");
		}
		sb.append("GROUP BY episode.c12, tvshow.c00 ");
		sb.append("ORDER BY tvshow.idShow, episode.c12+0");
		
		return parseSeasons(mConnection.query("QueryVideoDatabase", sb.toString(), manager), showMap);
	}
	
	/**
	 * Gets all Episodes for the specified show
	 * @param manager
	 * @param show
	 * @return
	 */
	public ArrayList<Episode> getEpisodes(INotifiableManager manager, TvShow show, int sortBy, String sortOrder, boolean hideWatched) {
		return getEpisodes(manager, show, null, sortBy, sortOrder, hideWatched);
	}
	
	/**
	 * Gets all Episodes for the specified season
	 * @param manager
	 * @param season
	 * @return
	 */
	public ArrayList<Episode> getEpisodes(INotifiableManager manager, Season season, int sortBy, String sortOrder, boolean hideWatched) {
		return getEpisodes(manager, season.show, season, sortBy, sortOrder, hideWatched);
	}
	
	/**
	 * Gets all Episodes for all shows
	 * @param manager
	 * @return
	 */
	public ArrayList<Episode> getEpisodes(INotifiableManager manager, int sortBy, String sortOrder, boolean hideWatched) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT idEpisode, c00, \"\" AS c01, ROUND(c03, 2), c04, c05, c06, playCount, c10, c12, c13, strPath, strFileName, strTitle, art.url");
		sb.append(" FROM episodeview LEFT OUTER JOIN art ON art.media_id=episodeview.idEpisode AND art.media_type='episode' AND art.type='thumb'");
		if (hideWatched) {
			sb.append(" WHERE (playCount IS NULL OR playCount = 0) ");
		}
		sb.append(showsOrderBy(sortBy, sortOrder));
		return parseEpisodes(mConnection.query("QueryVideoDatabase", sb.toString(), manager));
	}
	
	/**
	 * Gets all Episodes for the specified show and season
	 * @param manager
	 * @param show
	 * @param season
	 * @return
	 */
	public ArrayList<Episode> getEpisodes(INotifiableManager manager, TvShow show, Season season, int sortBy, String sortOrder, boolean hideWatched) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT idEpisode, c00, \"\" AS c01, ROUND(c03, 2), c04, c05, c06, playCount, c10, c12, c13, strPath, strFileName, strTitle, art.url");
		sb.append(" FROM episodeview  LEFT OUTER JOIN art ON art.media_id=episodeview.idEpisode AND art.media_type='episode' AND art.type='thumb'");
		sb.append(" WHERE idShow in ( select idShow from tvshow where c00 in (select c00 from tvshow where idShow = ");
		sb.append(show.id);
		sb.append("))");
		if (hideWatched) {
			sb.append(" AND (playCount IS NULL OR playCount = 0) ");
		}
		if(season != null) {
			sb.append(" AND (c12 = ");
			sb.append(season.number);
			sb.append(" OR (c12 = 0 AND (c15 = 0 OR c15 = ");
			sb.append(season.number);
			sb.append(")))");
		}
		sb.append(showsOrderBy(sortBy, sortOrder));
		return parseEpisodes(mConnection.query("QueryVideoDatabase", sb.toString(), manager));
	}
	
	public TvShow updateTvShowDetails(INotifiableManager manager, TvShow show) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT c01");
		sb.append("  FROM tvshow");
		sb.append("  WHERE tvshow.idShow = ");
		sb.append(show.id);
		show = parseTvShowDetails(mConnection.query("QueryVideoDatabase", sb.toString(), manager), show);
		//parse actors of the show
		sb = new StringBuilder();
		sb.append("SELECT actors.idActor, strActor, art.url, strRole");
		sb.append(" FROM actors LEFT JOIN art ON art.media_id=actors.idActor AND art.media_type='actor' AND art.type='thumb', actorlinktvshow");
		sb.append(" WHERE actors.idActor = actorlinktvshow.idActor");
		sb.append(" AND actorlinktvshow.idShow =");
		sb.append(show.getId());
		show.actors = VideoClient.parseActorRoles(mConnection.query("QueryVideoDatabase", sb.toString(), manager));		
		return show;
	}
	
	private TvShow parseTvShowDetails(String response, TvShow show) {
		String[] fields = response.split("<field>");
		try {
			show.summary = Connection.trim(fields[1]);
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println("response = " + response);
			e.printStackTrace();
		}
		return show;
	}
	
	public Episode updateEpisodeDetails(INotifiableManager manager, Episode episode) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT c01 ");
		sb.append(" FROM episodeview ");
		sb.append(" WHERE idEpisode=");
		sb.append(episode.id);
		episode = parseEpisodeDetails(mConnection.query("QueryVideoDatabase", sb.toString(), manager), episode);
		sb = new StringBuilder();
		sb.append("SELECT actors.idActor, strActor, art.url, strRole");
		sb.append(" FROM actors LEFT OUTER JOIN art ON art.media_id=actors.idActor AND art.media_type='actor' AND art.type='thumb', actorlinkepisode");
		sb.append(" WHERE actors.idActor = actorlinkepisode.idActor");
		sb.append(" AND actorlinkepisode.idEpisode =");
		sb.append(episode.id);
		episode.actors = VideoClient.parseActorRoles(mConnection.query("QueryVideoDatabase", sb.toString(), manager));
		return episode;
	}
	
	private Episode parseEpisodeDetails(String response, Episode episode) {
		String[] fields = response.split("<field>");
		try {
			episode.plot = Connection.trim(fields[1]);
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println("response = " + response);
			e.printStackTrace();
		}
		return episode;
	}
	
	static ICurrentlyPlaying getCurrentlyPlaying(final HashMap<String, String> map) {
		return new ICurrentlyPlaying() {
			private static final long serialVersionUID = 5036994329211476714L;
			public String getTitle() {
				return map.get("Show Title");
			}
			public int getTime() {
				return parseTime(map.get("Time"));
			}
			public int getPlayStatus() {
				return PlayStatus.parse(map.get("PlayStatus"));
			}
			public int getPlaylistPosition() {
				return Integer.parseInt(map.get("VideoNo"));
			}
			//Workarond for bug in Float.valueOf(): http://code.google.com/p/android/issues/detail?id=3156
			public float getPercentage() {
				try{
					return Integer.valueOf(map.get("Percentage"));
				} catch (NumberFormatException e) { }
				return Float.valueOf(map.get("Percentage"));
			}
			public String getFilename() {
				return map.get("Filename");
			}
			public int getDuration() {
				return parseTime(map.get("Duration"));
			}
			public String getArtist() {
				if(Integer.valueOf(map.get("Season")) == 0) {
					return "Specials / Episode " + map.get("Episode");
				}
				else {
					return "Season " + map.get("Season") + " / Episode " + map.get("Episode");
				}
			}
			public String getAlbum() {
				return map.get("Title");
			}
			public int getMediaType() {
				return MediaType.VIDEO_TVSHOW;
			}
			public boolean isPlaying() {
				return PlayStatus.parse(map.get("PlayStatus")) == PlayStatus.PLAYING;
			}
			public int getHeight() {
				return 0;
			}
			public int getWidth() {
				return 0;
			}
			private int parseTime(String time) {
				String[] s = time.split(":");
				if (s.length == 2) {
					return Integer.parseInt(s[0]) * 60 + Integer.parseInt(s[1]);
				} else if (s.length == 3) {
					return Integer.parseInt(s[0]) * 3600 + Integer.parseInt(s[1]) * 60 + Integer.parseInt(s[2]);
				} else {
					return 0;
				}
			}
		};
	}
	
	/**
	 * Returns a pre-resized movie cover. Pre-resizing is done in a way that
	 * the bitmap at least as large as the specified size but not larger than
	 * the double.
	 * @param manager Postback manager
	 * @param cover Cover object
	 * @param size Minmal size to pre-resize to.
	 * @return Thumbnail bitmap
	 */
	public Bitmap getCover(INotifiableManager manager, ICoverArt cover, int size) {
		return getCover(manager, cover, size, TvShow.getThumbUri(cover), TvShow.getFallbackThumbUri(cover));
	}
	
	/*
	 * public int id;1 
	public String title;2
	public String plot;3
	public double rating = 0.0;
	public String writer;
	public String firstAired;
	public boolean watched;
	public String director;
	public int season;
	public int episode;
	 */
	/**
	* Episode DB Fields:
		 * idEpisode 	 integer 	 Primary Key
		 * c00: Episode Title
		 * c01:	Plot Summary
		 * c03: Rating
		 * c04: Writer
		 * c05:	First Aired
		 * c06:	Thumbnail URL
		 * c08: Has the episode been watched?
		 * c10: Director
		 * c12: Season
		 * c13: Episode Number
		 * idFile: Foreign key to the files table
		 */
	protected ArrayList<Episode> parseEpisodes(String response) {
		ArrayList<Episode> episodes = new ArrayList<Episode>();
		String[] fields = response.split("<field>");
		try {
			for(int row = 1; row < fields.length; row += 15) {
				episodes.add(new Episode(Connection.trimInt(fields[row]),
						Connection.trim(fields[row + 1]),
						Connection.trim(fields[row + 2]),
						Connection.trimDouble(fields[row + 3]),
						Connection.trim(fields[row + 4]),
						Connection.trim(fields[row + 5]),
						Connection.trimInt(fields[row + 7]),
						Connection.trim(fields[row + 8]),
						Connection.trimInt(fields[row + 9]),
						Connection.trimInt(fields[row + 10]),
						Connection.trim(fields[row + 11]),
						Connection.trim(fields[row + 12]),
						Connection.trim(fields[row + 13]),
						Connection.trim(fields[row + 14])
					));
			}
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println("response = " + response);
			e.printStackTrace();
		}

		return episodes;
	}

	protected ArrayList<Season> parseSeasons(String response, TvShow show) {
		ArrayList<Season> seasons = new ArrayList<Season>();
		String[] fields = response.split("<field>");
		try {
			for( int row = 1; row < fields.length; row += 2) {
				seasons.add(new Season(Connection.trimInt(fields[row]), false, show, Connection.trim(fields[row+1])));
			}
		}catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println("response = " + response);
			e.printStackTrace();
		}
		return seasons;
	}
	
	protected ArrayList<Season> parseSeasons(String response, HashMap<Integer, TvShow> showMap) {
		ArrayList<Season> seasons = new ArrayList<Season>();
		String[] fields = response.split("<field>");
		try {
			for (int row = 1; row < fields.length; row += 3) {
				final int showId = Connection.trimInt(fields[row]);
				if (showMap.containsKey(showId)) {
					seasons.add(new Season(Connection.trimInt(fields[row + 1]), false, showMap.get(showId), Connection.trim(fields[row + 2])));
				}
			}
		}catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println("response = " + response);
			e.printStackTrace();
		}
		return seasons;
	}
	
	protected ArrayList<TvShow> parseShows(String response) {
		ArrayList<TvShow> shows = new ArrayList<TvShow>();
		String[] fields = response.split("<field>");
		try {
			for (int row = 1; row < fields.length; row += 13) {
				shows.add(new TvShow(
						Connection.trimInt(fields[row]),
						Connection.trim(fields[row + 1]),
						Connection.trim(fields[row + 2]),
						Connection.trimDouble(fields[row + 3]),
						Connection.trim(fields[row + 4]),
						Connection.trim(fields[row + 5]),
						Connection.trim(fields[row + 6]),
						Connection.trim(fields[row + 7]),
						Connection.trim(fields[row + 8]),
						Connection.trimInt(fields[row + 9]),
						Connection.trimInt(fields[row + 10]),
						Connection.trimBoolean(fields[row + 11]),
						Connection.trim(fields[row + 12])
				));
			}
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println("response = " + response);
			e.printStackTrace();
		}
		return shows;
	}
	
	private String showsOrderBy(int sortBy, String sortOrder) {
		switch (sortBy) {
			default:
				return " ORDER BY lower(tvshow.c00) asc";
			case SortType.TITLE:
				return " ORDER BY CASE WHEN tvshow.c15 IS NULL OR tvshow.c15 = '' THEN replace(lower(tvshow.c00),'the ','') ELSE replace(lower(tvshow.c15),'the ','') END " + sortOrder;
			case SortType.YEAR:
				return " ORDER BY tvshow.c05 " + sortOrder + ", CASE WHEN tvshow.c15 IS NULL OR tvshow.c15 = '' THEN lower(tvshow.c00) ELSE lower(tvshow.c15) END " + sortOrder;
			case SortType.RATING:
				return " ORDER BY ROUND(tvshow.c04, 2) " + sortOrder;
			case SortType.EPISODE_NUM:
				return " ORDER BY episodeview.c12+0 " + sortOrder + ", episodeview.c13+0 " + sortOrder;
			case SortType.EPISODE_TITLE:
				return " ORDER BY episodeview.c00 " + sortOrder;
			case SortType.EPISODE_RATING:
				return " ORDER BY episodeview.c03 " + sortOrder;
		}
	}

	/**
	 * Updates host info on the connection.
	 * @param host
	 */
	public void setHost(Host host) {
		mConnection.setHost(host);
	}

	public ArrayList<Episode> getRecentlyAddedEpisodes(
			INotifiableManager manager, boolean hideWatched) {
		// TODO Auto-generated method stub
		return null;
	}
}

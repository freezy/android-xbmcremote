package org.xbmc.httpapi.client;

import java.util.ArrayList;

import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.ITvShowClient;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Episode;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.Host;
import org.xbmc.api.object.Season;
import org.xbmc.api.object.TvShow;
import org.xbmc.httpapi.Connection;

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
public class TvShowClient implements ITvShowClient {

	private static final String TAG = "TvShowClient";

	private Connection mConnection;

	public TvShowClient(Connection connection) {
		this.mConnection = connection;
	}
	
	public ArrayList<TvShow> getTvShows(INotifiableManager manager) {
		StringBuilder sb = new StringBuilder();
		
		// don't fetch summary for list view
		sb.append("SELECT tvshow.idShow, tvshow.c00, \"\" AS c01, tvshow.c04, tvshow.c05, tvshow.c08, tvshow.c13, tvshow.c14, ");
		sb.append("    path.strPath AS strPath,");
		sb.append("    counts.totalcount AS totalCount,");
		sb.append("    counts.watchedcount AS watchedCount,");
		sb.append("    counts.totalcount = counts.watchedcount AS watched");
		sb.append("  FROM tvshow");
		sb.append("  JOIN tvshowlinkpath ON tvshow.idShow = tvshowlinkpath.idShow");
		sb.append("  JOIN path ON path.idpath = tvshowlinkpath.idPath ");
		sb.append("  LEFT OUTER join (");
		sb.append("     SELECT tvshow.idShow AS idShow, count(1) AS totalcount, count(files.playCount) AS watchedcount");
		sb.append("     FROM tvshow");
		sb.append("     JOIN tvshowlinkepisode ON tvshow.idShow = tvshowlinkepisode.idShow");
		sb.append("     JOIN episode ON episode.idEpisode = tvshowlinkepisode.idEpisode");
		sb.append("     JOIN files ON files.idFile = episode.idFile");
		sb.append("     GROUP BY tvshow.idShow");
		sb.append("  )");
		sb.append("  counts ON tvshow.idShow = counts.idShow");
		Log.i(TAG, sb.toString());
				
				
/*		sb.append("SELECT tvshow.idShow, c00, c01, c04, c05, c08, c13, c14, strPath FROM tvshow, path, tvshowlinkpath");
		sb.append(" WHERE tvshow.idShow = tvshowlinkpath.idShow");
		sb.append(" AND path.idPath = tvshowlinkpath.idPath");*/
		//sb.append(showsOrderBy(sortBy, sortOrder));
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
	 * Gets all movie genres from database
	 * @return All movie genres
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
	public ArrayList<TvShow> getTvShows(INotifiableManager manager, Actor actor) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT tvshow.idShow, tvshow.c00, \"\" AS c01, tvshow.c04, tvshow.c05, tvshow.c08, tvshow.c13, tvshow.c14, ");
		sb.append("    path.strPath AS strPath,");
		sb.append("    counts.totalcount AS totalCount,");
		sb.append("    counts.watchedcount AS watchedCount,");
		sb.append("    counts.totalcount = counts.watchedcount AS watched");
		sb.append("  FROM tvshow");
		sb.append("  JOIN tvshowlinkpath ON tvshow.idShow = tvshowlinkpath.idShow");
		sb.append("  JOIN path ON path.idpath = tvshowlinkpath.idPath ");
		sb.append("  LEFT OUTER join (");
		sb.append("     SELECT tvshow.idShow AS idShow, count(1) AS totalcount, count(files.playCount) AS watchedcount");
		sb.append("     FROM tvshow");
		sb.append("     JOIN tvshowlinkepisode ON tvshow.idShow = tvshowlinkepisode.idShow");
		sb.append("     JOIN episode ON episode.idEpisode = tvshowlinkepisode.idEpisode");
		sb.append("     JOIN files ON files.idFile = episode.idFile");
		sb.append("     GROUP BY tvshow.idShow");
		sb.append("  )");
		sb.append("  counts ON tvshow.idShow = counts.idShow");
		sb.append("  WHERE tvshow.idShow IN (Select idShow from actorlinktvshow where idActor = ");
		sb.append(actor.id);
		sb.append(" )");
		Log.i(TAG, sb.toString());
		return parseShows(mConnection.query("QueryVideoDatabase", sb.toString(), manager));
	}
	
	/**
	 * Gets all tv shows for the specified genre
	 * 
	 */
	public ArrayList<TvShow> getTvShows(INotifiableManager manager, Genre genre) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT tvshow.idShow, tvshow.c00, \"\" AS c01, tvshow.c04, tvshow.c05, tvshow.c08, tvshow.c13, tvshow.c14, ");
		sb.append("    path.strPath AS strPath,");
		sb.append("    counts.totalcount AS totalCount,");
		sb.append("    counts.watchedcount AS watchedCount,");
		sb.append("    counts.totalcount = counts.watchedcount AS watched");
		sb.append("  FROM tvshow");
		sb.append("  JOIN tvshowlinkpath ON tvshow.idShow = tvshowlinkpath.idShow");
		sb.append("  JOIN path ON path.idpath = tvshowlinkpath.idPath ");
		sb.append("  LEFT OUTER join (");
		sb.append("     SELECT tvshow.idShow AS idShow, count(1) AS totalcount, count(files.playCount) AS watchedcount");
		sb.append("     FROM tvshow");
		sb.append("     JOIN tvshowlinkepisode ON tvshow.idShow = tvshowlinkepisode.idShow");
		sb.append("     JOIN episode ON episode.idEpisode = tvshowlinkepisode.idEpisode");
		sb.append("     JOIN files ON files.idFile = episode.idFile");
		sb.append("     GROUP BY tvshow.idShow");
		sb.append("  )");
		sb.append("  counts ON tvshow.idShow = counts.idShow");
		sb.append("  WHERE tvshow.idShow in (Select idShow from genrelinktvshow where idgenre = ");
		sb.append(genre.id);
		sb.append(") ");
		Log.i(TAG, sb.toString());
		return parseShows(mConnection.query("QueryVideoDatabase", sb.toString(), manager));
	}
	
	/**
	 * Gets all seasons for the specified show
	 * @param manager
	 * @param show
	 * @return
	 */
	public ArrayList<Season> getSeasons(INotifiableManager manager, TvShow show) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT episode.c12,path.strPath,tvshow.c00,tvshow.c08,count(1),");
		sb.append("count(files.playCount) FROM episode JOIN tvshowlinkepisode ");
		sb.append("ON tvshowlinkepisode.idEpisode=episode.idEpisode join tvshow ");
		sb.append("ON tvshow.idShow=tvshowlinkepisode.idShow JOIN files on files.idFile=episode.idFile  ");
		sb.append("JOIN tvshowlinkpath on tvshowlinkpath.idShow = tvshow.idShow ");
		sb.append("JOIN path on path.idPath = tvshowlinkpath.idPath where tvshow.idShow = 1  group by episode.c12");
		return null;
	}
	
	/**
	 * Gets all Episodes for the specified show
	 * @param manager
	 * @param show
	 * @return
	 */
	public ArrayList<Episode> getEpisodes(INotifiableManager manager, TvShow show) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT idEpisode, c00, \"\" AS c01, c03, c04, c05, c06, c08, c10, c12, c13, idFile");
		sb.append(" FROM episode WHEre idEpisode in (select idEpisode FROM tvshowlinkepisode WHERE ");
		sb.append(" idShow = ");
		sb.append(show.id);
		sb.append(" )");
		sb.append(" GROUP BY c12 ORDER BY c12, c13");
		return parseEpisodes(mConnection.query("QueryVideoDatabase", sb.toString(), manager));
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
			for(int row = 1; row < fields.length; row += 12) {
				episodes.add(new Episode(Connection.trimInt(fields[row]),
						Connection.trim(fields[row + 1]),
						Connection.trim(fields[row + 2]),
						Connection.trimDouble(fields[row + 3]),
						Connection.trim(fields[row + 4]),
						Connection.trim(fields[row + 5]),
						Connection.trimBoolean(fields[row + 7]),
						Connection.trim(fields[row + 8]),
						Connection.trimInt(fields[row + 9]),
						Connection.trimInt(fields[row + 10])));
			}
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println("response = " + response);
			e.printStackTrace();
		}
		return episodes;
	}

	
	protected ArrayList<TvShow> parseShows(String response) {
		ArrayList<TvShow> shows = new ArrayList<TvShow>();
		String[] fields = response.split("<field>");
		try {
			for (int row = 1; row < fields.length; row += 12) {
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
						Connection.trimBoolean(fields[row + 11])
				));
			}
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println("response = " + response);
			e.printStackTrace();
		}
		return shows;
	}
	
/*	private String showsOrderBy(int sortBy, String sortOrder) {
		switch (sortBy) {
			default:
			case SortType.TITLE:
				return " ORDER BY lower(c00) " + sortOrder;
//			case SortType.YEAR:
//				return " ORDER BY c07 " + sortOrder + ", lower(c00) " + sortOrder;
			case SortType.RATING:
				return " ORDER BY c04" + sortOrder;
		}
	}*/

	/**
	 * Updates host info on the connection.
	 * @param host
	 */
	public void setHost(Host host) {
		mConnection.setHost(host);
	}
}

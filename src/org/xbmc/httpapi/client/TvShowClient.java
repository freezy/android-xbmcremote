package org.xbmc.httpapi.client;

import java.util.ArrayList;

import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.ITvShowClient;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.Host;
import org.xbmc.api.object.TvShow;
import org.xbmc.api.type.SortType;
import org.xbmc.httpapi.Connection;

import android.util.Log;

/**
 * TV show client for HTTP API.
 * 
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
 *  - c12: tvdb show id
 *  - c13: content rating
 *  - c14: network name
 *  - c15-c20: empty
 *  plus strPath, totalCount, watchedCount, watched
 * 
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
	
	public ArrayList<TvShow> getTvShows(INotifiableManager manager, Genre genre) {
		StringBuilder sb = new StringBuilder();
		sb.append("select idShow, c00, c01, c04, c05, c08, c13, c14 from tvshow ");
		sb.append("where idShow in (Select idShow from genrelinktvshow where idgenre =");
		sb.append(genre.id);
		return parseShows(mConnection.query("QueryVideoDatabase", sb.toString(), manager));
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
	
	private String showsOrderBy(int sortBy, String sortOrder) {
		switch (sortBy) {
			default:
			case SortType.TITLE:
				return " ORDER BY lower(c00) " + sortOrder;
//			case SortType.YEAR:
//				return " ORDER BY c07 " + sortOrder + ", lower(c00) " + sortOrder;
			case SortType.RATING:
				return " ORDER BY c04" + sortOrder;
		}
	}

	/**
	 * Updates host info on the connection.
	 * @param host
	 */
	public void setHost(Host host) {
		mConnection.setHost(host);
	}
}

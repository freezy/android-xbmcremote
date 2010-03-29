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

public class TvShowClient implements ITvShowClient {

	private Connection mConnection;

	public TvShowClient(Connection connection) {
		this.mConnection = connection;
	}
	
	public ArrayList<TvShow> getTvShows(INotifiableManager manager, int sortBy, String sortOrder) {
		StringBuilder sb = new StringBuilder();
		sb.append("select idShow, c00, c01, c04, c05, c08, c13, c14 from tvshow ");
		sb.append(showsOrderBy(sortBy, sortOrder));
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
			for (int row = 1; row < fields.length; row += 9) {
				shows.add( new TvShow(
						Connection.trimInt(fields[row]),
						Connection.trim(fields[row + 1]),
						Connection.trim(fields[row +2]),
						Connection.trimDouble(fields[row + 3]),
						Connection.trim(fields[row + 4]),
						Connection.trim(fields[row + 5]),
						Connection.trim(fields[row + 6]),
						Connection.trim(fields[row + 7])
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

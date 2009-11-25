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

import java.util.ArrayList;
import java.util.HashMap;

import org.xbmc.httpapi.Connection;
import org.xbmc.httpapi.client.ControlClient.PlayStatus;
import org.xbmc.httpapi.data.Actor;
import org.xbmc.httpapi.data.Genre;
import org.xbmc.httpapi.data.ICoverArt;
import org.xbmc.httpapi.data.Movie;
import org.xbmc.httpapi.type.MediaType;
import org.xbmc.httpapi.type.SortType;

import android.util.Log;

/**
 * Takes care of everything related to the video database.
 * 
 * @author Team XBMC
 */
public class VideoClient {
	
	private final Connection mConnection;

	/**
	 * Class constructor needs reference to HTTP client connection
	 * @param connection
	 */
	public VideoClient(Connection connection) {
		mConnection = connection;
	}
	
	/**
	 * Gets all movies from database
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All movies
	 */
	public ArrayList<Movie> getMovies(int sortBy, String sortOrder) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT idMovie, c00, c07, strPath, strFileName, c15, c11, c14, c05");
		sb.append(" FROM movieview WHERE movieview.idmovie NOT IN (SELECT idmovie FROM setlinkmovie)");
		sb.append(moviesOrderBy(sortBy, sortOrder));
		return parseMovies(mConnection.query("QueryVideoDatabase", sb.toString()));
	}
	
	/**
	 * Gets all movies with an actor from database
	 * @param actor Display only movies with this actor.
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All movies
	 */
	public ArrayList<Movie> getMovies(Actor actor, int sortBy, String sortOrder) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT idMovie, c00, c07, strPath, strFileName, c15, c11, c14, c05");
		sb.append(" FROM movieview");
		sb.append(" WHERE movieview.idmovie IN (");
		sb.append("   SELECT DISTINCT idMovie ");
		sb.append("   FROM actorlinkmovie ");
		sb.append("   WHERE idActor =");
		sb.append(actor.id);
		sb.append(" )");
		sb.append(moviesOrderBy(sortBy, sortOrder));
		return parseMovies(mConnection.query("QueryVideoDatabase", sb.toString()));
	}
	
	/**
	 * Gets all movies of a genre from database
	 * @param genre Display only movies of this genre.
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All movies
	 */
	public ArrayList<Movie> getMovies(Genre genre, int sortBy, String sortOrder) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT idMovie, c00, c07, strPath, strFileName, c15, c11, c14, c05");
		sb.append(" FROM movieview");
		sb.append(" WHERE movieview.idmovie IN (");
		sb.append("   SELECT DISTINCT idMovie ");
		sb.append("   FROM genrelinkmovie ");
		sb.append("   WHERE idGenre =");
		sb.append(genre.id);
		sb.append(" )");
		sb.append(moviesOrderBy(sortBy, sortOrder));
		return parseMovies(mConnection.query("QueryVideoDatabase", sb.toString()));
	}
	
	/**
	 * Gets all movies from database
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return Updated movie
	 */
	public Movie updateMovieDetails(Movie movie) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT c03, c01, c04, c18, c12, c19");
		sb.append(" FROM movieview WHERE movieview.idmovie = ");
		sb.append(movie.getId());
		parseMovieDetails(mConnection.query("QueryVideoDatabase", sb.toString()), movie);
		sb = new StringBuilder();
		sb.append("SELECT actors.idActor, strActor, strRole");
		sb.append(" FROM actors, actorlinkmovie");
		sb.append(" WHERE actors.idActor = actorlinkmovie.idActor");
		sb.append(" AND actorlinkmovie.idMovie =");
		sb.append(movie.getId());
		movie.actors = parseActorRoles(mConnection.query("QueryVideoDatabase", sb.toString()));
		return movie;
	}
	
	/**
	 * Gets all actors from database. Use {@link getMovieActors()} and
	 * {@link getTvActors()} for filtered actors. 
	 * @return All actors
	 */
	public ArrayList<Actor> getActors() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT idActor, strActor FROM actors");
		sb.append(" ORDER BY upper(strActor), strActor");
		return parseActors(mConnection.query("QueryVideoDatabase", sb.toString()));
	}
	
	/**
	 * Gets all movie actors from database
	 * @return All movie actors
	 */
	public ArrayList<Actor> getMovieActors() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT actors.idActor, strActor FROM actors, actorlinkmovie");
		sb.append(" WHERE actorlinkmovie.idActor = actors.idActor");
		sb.append(" ORDER BY upper(strActor), strActor");
		return parseActors(mConnection.query("QueryVideoDatabase", sb.toString()));
	}
	
	/**
	 * Gets all movie actors from database
	 * @return All movie actors
	 */
	public ArrayList<Actor> getTvShowActors() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT actors.idActor, strActor FROM actors, actorlinktvshow");
		sb.append(" WHERE actorlinktvshow.idActor = actors.idActor");
		sb.append(" ORDER BY upper(strActor), strActor");
		return parseActors(mConnection.query("QueryVideoDatabase", sb.toString()));
	}
	
	/**
	 * Gets all movie genres from database
	 * @return All movie genres
	 */
	public ArrayList<Genre> getMovieGenres() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT idGenre, strGenre FROM genre");
		sb.append(" WHERE idGenre IN (SELECT idGenre FROM genrelinkmovie)");
		sb.append(" ORDER BY upper(strGenre)");
		return parseGenres(mConnection.query("QueryVideoDatabase", sb.toString()));
	}
	
	/**
	 * Returns movie thumbnail as base64-encoded string
	 * @param cover
	 * @return Base64-encoded content of thumb
	 */
	public String getCover(ICoverArt cover) {
		final String data = mConnection.query("FileDownload", Movie.getThumbUri(cover));
		if (data.length() > 0) {
			return data;
		} else {
			final String url = Movie.getFallbackThumbUri(cover);
			if (url != null) {
				Log.i("VideoClient", "*** Downloaded cover has size null, retrying with fallback:");
				return mConnection.query("FileDownload", url);
			} else {
				return "";
			}
		}
	}

	/**
	 * Converts query response from HTTP API to a list of Movie objects. Each
	 * row must return the following attributes in the following order:
	 * <ol>
	 * 	<li><code>idMovie</code></li>
	 * 	<li><code>c00</code></li> (title)
	 * 	<li><code>c07</code></li> (year)
	 * 	<li><code>strPath</code></li>
	 * 	<li><code>strFileName</code></li>
	 * 	<li><code>c15</code></li> (director)
	 * 	<li><code>c11</code></li> (runtime)
	 * 	<li><code>c14</code></li> (genres)
	 * 	<li><code>c05</code></li> (rating)
	 * </ol> 
	 * @param response
	 * @return List of movies
	 */
	private ArrayList<Movie> parseMovies(String response) {
		ArrayList<Movie> movies = new ArrayList<Movie>();
		String[] fields = response.split("<field>");
		try {
			for (int row = 1; row < fields.length; row += 9) {
				movies.add(new Movie( // int id, String title, int year, String path, String filename, String director, String runtime, String genres
						Connection.trimInt(fields[row]), 
						Connection.trim(fields[row + 1]), 
						Connection.trimInt(fields[row + 2]),
						Connection.trim(fields[row + 3]),
						Connection.trim(fields[row + 4]),
						Connection.trim(fields[row + 5]),
						Connection.trim(fields[row + 6]),
						Connection.trim(fields[row + 7]),
						Connection.trimDouble(fields[row + 8])
				));
			}
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println("response = " + response);
			e.printStackTrace();
		}
		return movies;
	}
	
	/**
	 * Updates a movie object with some more details. Fields must be the following (in this order):
	 * <ol>
	 * 	<li><code>c03</code></li> (tagline)
	 * 	<li><code>c01</code></li> (plot)
	 * 	<li><code>c04</code></li> (number of votes)
	 * 	<li><code>c18</code></li> (studio)
	 * 	<li><code>c12</code></li> (parental rating)
	 * 	<li><code>c19</code></li> (trailer)
	 * </ol> 
	 * @param response
	 * @param movie 
	 * @return Updated movie object
	 */
	private Movie parseMovieDetails(String response, Movie movie) {
		String[] fields = response.split("<field>");
		try {
			movie.tagline = Connection.trim(fields[1]);
			movie.plot = Connection.trim(fields[2]);
			movie.numVotes = Connection.trimInt(fields[3]);
			movie.studio = Connection.trim(fields[4]);
			movie.rated = Connection.trim(fields[5]);
			movie.trailerUrl = Connection.trim(fields[6]);
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println("response = " + response);
			e.printStackTrace();
		}
		return movie;
	}
	

	/**
	 * Converts query response from HTTP API to a list of Actor objects. Each
	 * row must return the following columns in the following order:
	 * <ol>
	 * 	<li><code>idActor</code></li>
	 * 	<li><code>strActor</code></li>
	 * </ol>
	 * @param response
	 * @return List of Actors
	 */
	private ArrayList<Actor> parseActors(String response) {
		ArrayList<Actor> actors = new ArrayList<Actor>();
		String[] fields = response.split("<field>");
		try { 
			for (int row = 1; row < fields.length; row += 2) { 
				actors.add(new Actor(
						Connection.trimInt(fields[row]), 
						Connection.trim(fields[row + 1])
				));
			}
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println("response = " + response);
			e.printStackTrace();
		}
		return actors;		
	}
	
	/**
	 * Converts query response from HTTP API to a list of Actor objects with 
	 * roles attached. Each row must return the following columns in the 
	 * following order:
	 * <ol>
	 * 	<li><code>idActor</code></li>
	 * 	<li><code>strActor</code></li>
	 * 	<li><code>strRole</code></li>
	 * </ol>
	 * @param response
	 * @return List of Actors
	 */
	private ArrayList<Actor> parseActorRoles(String response) {
		ArrayList<Actor> actors = new ArrayList<Actor>();
		String[] fields = response.split("<field>");
		try { 
			for (int row = 1; row < fields.length; row += 3) { 
				actors.add(new Actor(
						Connection.trimInt(fields[row]), 
						Connection.trim(fields[row + 1]),
						Connection.trim(fields[row + 2])
				));
			}
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println("response = " + response);
			e.printStackTrace();
		}
		return actors;		
	}
	
	/**
	 * Converts query response from HTTP API to a list of Genre objects. Each
	 * row must return the following columns in the following order:
	 * <ol>
	 * 	<li><code>idGenre</code></li>
	 * 	<li><code>strGenre</code></li>
	 * </ol>
	 * @param response
	 * @return List of Genres
	 */
	private ArrayList<Genre> parseGenres(String response) {
		ArrayList<Genre> genres = new ArrayList<Genre>();
		String[] fields = response.split("<field>");
		try { 
			for (int row = 1; row < fields.length; row += 2) { 
				genres.add(new Genre(
					Connection.trimInt(fields[row]), 
					Connection.trim(fields[row + 1])
				));
			}
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println("response = " + response);
			e.printStackTrace();
		}
		return genres;		
	}

	
	/**
	 * Returns an SQL String of given sort options of movies query
	 * @param sortBy    Sort field
	 * @param sortOrder Sort order
	 * @return SQL "ORDER BY" string
	 */
	private String moviesOrderBy(int sortBy, String sortOrder) {
		switch (sortBy) {
			default:
			case SortType.TITLE:
				return " ORDER BY lower(c00) " + sortOrder;
			case SortType.YEAR:
				return " ORDER BY c07 " + sortOrder + ", lower(c00) " + sortOrder;
			case SortType.RATING:
				return " ORDER BY c05" + sortOrder;
		}
	}
	
	public static ControlClient.ICurrentlyPlaying getCurrentlyPlaying(final HashMap<String, String> map) {
		return new ControlClient.ICurrentlyPlaying() {
			private static final long serialVersionUID = 5036994329211476713L;
			public String getTitle() {
				return map.get("Tagline");
			}
			public int getTime() {
				return parseTime(map.get("Time"));
			}
			public PlayStatus getPlayStatus() {
				return PlayStatus.parse(map.get("PlayStatus"));
			}
			public int getPlaylistPosition() {
				return Integer.parseInt(map.get("VideoNo"));
			}
			public float getPercentage() {
				return Float.valueOf(map.get("Percentage"));
			}
			public String getFilename() {
				return map.get("Filename");
			}
			public int getDuration() {
				return parseTime(map.get("Duration"));
			}
			public String getArtist() {
				return map.get("Genre");
			}
			public String getAlbum() {
				return map.get("Title");
			}
			public int getMediaType() {
				return MediaType.VIDEO;
			}
			public boolean isPlaying() {
				return PlayStatus.parse(map.get("PlayStatus")).equals(PlayStatus.Playing);
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
}
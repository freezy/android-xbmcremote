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

package org.xbmc.jsonrpc.client;

import java.util.ArrayList;
import java.util.Iterator;

import org.codehaus.jackson.JsonNode;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IVideoClient;
import org.xbmc.api.data.IControlClient.ICurrentlyPlaying;
import org.xbmc.api.info.PlayStatus;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.Host;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.object.Movie;
import org.xbmc.api.type.MediaType;
import org.xbmc.api.type.SortType;
import org.xbmc.jsonrpc.Connection;
import android.graphics.Bitmap;

/**
 * Takes care of everything related to the video database.
 * 
 * @author Team XBMC
 */
public class VideoClient extends Client implements IVideoClient {
	
	public static final String TAG = "VideoClient";

	public static final int PLAYLIST_ID = 1;
	
	public static final int PLAYLIST_LIMIT = 100;
	
	/**
	 * Class constructor needs reference to HTTP client connection
	 * @param connection
	 */
	public VideoClient(Connection connection) {
		super(connection);
	}
	
	/**
	 * Updates host info on the connection.
	 * @param host
	 */
	public void setHost(Host host) {
		mConnection.setHost(host);
	}
	
	/**
	 * Gets all movies from database
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All movies
	 */
	public ArrayList<Movie> getMovies(INotifiableManager manager, int sortBy, String sortOrder, boolean hideWatched) {
		return getMovies(manager, obj(), sortBy, sortOrder, hideWatched);
	}
	
	public ArrayList<Movie> getMovies(INotifiableManager manager, ObjNode obj, int sortBy, String sortOrder, boolean hideWatched) {
		
		obj = sort(obj.p(PARAM_PROPERTIES, arr().add("director").add("file").add("genre").add("imdbnumber").add("playcount").add("rating").add("runtime").add("thumbnail").add("year")), sortBy, sortOrder);
		
		final ArrayList<Movie> movies = new ArrayList<Movie>();
		final JsonNode result = mConnection.getJson(manager, "VideoLibrary.GetMovies", obj);
		final JsonNode jsonMovies = result.get("movies");
		if(jsonMovies != null){
			for (Iterator<JsonNode> i = jsonMovies.getElements(); i.hasNext();) {
				JsonNode jsonMovie = (JsonNode)i.next();
				
				int playcount =getInt(jsonMovie, "playcount");
				if(playcount > 0 && hideWatched)
					continue;
				
				int runtime = getInt(jsonMovie, "runtime");
				String formatted_runtime = "";
				if(runtime != -1){						
					if(runtime >= 3600)
						formatted_runtime = (runtime / 3600) + "hr ";
					formatted_runtime += ((runtime % 3600) / 60) + "min";					
				}
				
				
				movies.add(new Movie(
					getInt(jsonMovie, "movieid"),
					getString(jsonMovie, "label"),
					getInt(jsonMovie, "year"),
					"",
					getString(jsonMovie, "file"),
					getString(jsonMovie, "director"),
					formatted_runtime,
					getString(jsonMovie, "genre"),
					getDouble(jsonMovie, "rating"),
					playcount,
					getString(jsonMovie, "imdbnumber"),
					getString(jsonMovie, "thumbnail")
				));
			}
		}
		return movies;
	}
	
	/**
	 * Gets movies from database with offset
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return Movies with offset
	 */
	public ArrayList<Movie> getMovies(INotifiableManager manager, int sortBy, String sortOrder, int offset, boolean hideWatched) {
		
		return getMovies(manager, obj().p("limits", obj().p("start", 0)), sortBy, sortOrder, hideWatched);
	}
	
	/**
	 * Gets all movies with an actor from database
	 * @param actor Display only movies with this actor.
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All movies with an actor
	 */
	public ArrayList<Movie> getMovies(INotifiableManager manager, Actor actor, int sortBy, String sortOrder, boolean hideWatched) {
		return getMovies(manager, obj().p("filter", obj().p("actor", actor.name)), sortBy, sortOrder, hideWatched);
	}
	
	/**
	 * Gets all movies of a genre from database
	 * @param genre Display only movies of this genre.
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All movies of a genre
	 */
	public ArrayList<Movie> getMovies(INotifiableManager manager, Genre genre, int sortBy, String sortOrder, boolean hideWatched) {
		
		return getMovies(manager, obj().p("filter", obj().p("genreid", genre.id)), sortBy, sortOrder, hideWatched);
	}
	
	/**
	 * Gets all movies from database
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return Updated movie
	 */
	public Movie updateMovieDetails(INotifiableManager manager, Movie movie) {
		
		ObjNode obj = obj().p("movieid", movie.getId()).p(PARAM_PROPERTIES, arr().add("cast").add("mpaa").add("plot").add("studio").add("tagline").add("trailer").add("votes"));
		
		final JsonNode result = mConnection.getJson(manager, "VideoLibrary.GetMovieDetails", obj);
		final JsonNode jsonMovie = result.get("moviedetails");
		movie.tagline = getString(jsonMovie, "tagline");
		movie.plot = getString(jsonMovie, "plot");
		movie.numVotes = getInt(jsonMovie, "votes");
		movie.studio = getString(jsonMovie, "studio");
		movie.rated = getString(jsonMovie, "mpaa");
		movie.trailerUrl = getString(jsonMovie, "trailer");	
		
		
		ArrayList<Actor> actors = new ArrayList<Actor>();
		final JsonNode jsonCast = jsonMovie.get("cast");
		for (Iterator<JsonNode> i = jsonCast.getElements(); i.hasNext();) {
			JsonNode jsonActor = (JsonNode)i.next();
			actors.add(new Actor(
					getInt(jsonActor,"actorid"),
					getString(jsonActor, "name"),
					getString(jsonActor, "thumbnail"),
					getString(jsonActor, "role")
					));
		}
			
		movie.actors = actors;
		
		return movie;
	}
	
	/**
	 * Gets all actors from database. Use {@link getMovieActors()} and
	 * {@link getTvActors()} for filtered actors. 
	 * @return All actors
	 */
	public ArrayList<Actor> getActors(INotifiableManager manager) {
		//TODO
		return new ArrayList<Actor>();
	}
	
	/**
	 * Gets all movie actors from database
	 * @return All movie actors
	 */
	public ArrayList<Actor> getMovieActors(INotifiableManager manager) {
		//TODO
		return new ArrayList<Actor>();
	}
	
	/**
	 * Gets all movie actors from database
	 * @return All movie actors
	 */
	public ArrayList<Actor> getTvShowActors(INotifiableManager manager) {
		return new ArrayList<Actor>();//parseActors(mConnection.query("QueryVideoDatabase", sb.toString(), manager));
	}
	
	/**
	 * Gets all movie genres from database
	 * @return All movie genres
	 */
	
	public ArrayList<Genre> getGenres(INotifiableManager manager, String type) {
		ObjNode obj = sort(obj().p("type", type), SortType.TITLE, "descending");
		
		final ArrayList<Genre> genres = new ArrayList<Genre>();
		final JsonNode result = mConnection.getJson(manager, "VideoLibrary.GetGenres", obj);
		final JsonNode jsonGenres = result.get("genres");
		if(jsonGenres != null){
			for (Iterator<JsonNode> i = jsonGenres.getElements(); i.hasNext();) {
				JsonNode jsonGenre = (JsonNode)i.next();
				genres.add(new Genre(
					getInt(jsonGenre, "genreid"),
					getString(jsonGenre, "label")
				));
			}
		}
		return genres;
	}
	

	public ArrayList<Genre> getMovieGenres(INotifiableManager manager) {
		return getGenres(manager, "movie");
	}
	
	/**
	 * Gets all tv show genres from database
	 * @return All tv show genres
	 */
	public ArrayList<Genre> getTvShowGenres(INotifiableManager manager) {
		return getGenres(manager, "tvshow");
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
		String url = null;
		if(Movie.getThumbUri(cover) != ""){
			final JsonNode dl = mConnection.getJson(manager, "Files.PrepareDownload", obj().p("path", Movie.getThumbUri(cover)));
			if(dl != null){
				JsonNode details = dl.get("details");
				if(details != null)
					url = mConnection.getUrl(getString(details, "path"));
			}
		}
		return getCover(manager, cover, size, url);
	}
	
	
	static ICurrentlyPlaying getCurrentlyPlaying(final JsonNode player, final JsonNode item) {
		
		return new ICurrentlyPlaying() {
			private static final long serialVersionUID = 5036994329211476714L;
			public String getTitle() {
				String title =getString(item, "title");
				if (title != null && !title.equals(""))
					return title;
				String[] path = getString(item, "file").replaceAll("\\\\", "/").split("/");
				return path[path.length - 1];
			}
			public int getTime() {
				return ControlClient.parseTime(player.get("time"));
			}
			public int getPlayStatus() {
				return getInt(player, "speed");
			}
			public int getPlaylistPosition() {
				return getInt(player, "position");
			}
			//Workarond for bug in Float.valueOf(): http://code.google.com/p/android/issues/detail?id=3156
			public float getPercentage() {
				try{
					return getInt(player, "percentage");
				} catch (NumberFormatException e) { }
				return (float)getDouble(player, "percentage");
			}
			public String getFilename() {
				return getString(item, "file");
			}
			public int getDuration() {
				return ControlClient.parseTime(player.get("totaltime"));
			}
			public String getArtist() {
				return getString(item, "genre");
			}
			public String getAlbum() {
				String title = getString(item, "tagline");
				if (title != null)
					return title;
				String path = getString(item, "file").replaceAll("\\\\", "/");
				return path.substring(0, path.lastIndexOf("/"));
			}
			public int getMediaType() {
				return MediaType.VIDEO;
			}
			public boolean isPlaying() {
				return getInt(player, "speed") == PlayStatus.PLAYING;
			}
			public int getHeight() {
				return 0;
			}
			public int getWidth() {
				return 0;
			}
		};
	}

	/**
	 * Retrieves the currently playing video number in the playlist.
	 * @return Number of items in the playlist
	 */
	public int getPlaylistPosition(INotifiableManager manager) {
		final JsonNode active = mConnection.getJson(manager, "Player.GetActivePlayers", null).get(0);			
		return mConnection.getInt(manager, "Player.GetProperties", obj().p("playerid", getInt(active, "playerid")).p(PARAM_PROPERTIES, arr().add("position")), "position");
	}
	
	/**
	 * Sets the media at playlist position to be the next item to be played.
	 * @param position New position, starting with 0.
	 * @return True on success, false otherwise.
	 */
	public boolean setPlaylistPosition(INotifiableManager manager, int position) {
		int playerid = getActivePlayerId(manager);
		if(playerid == -1)
			return mConnection.getString(manager, "Player.Open", obj().p("item", obj().p("playlistid", PLAYLIST_ID).p("position", position))).equals("OK");
		else
			return mConnection.getString(manager, "Player.GoTo", obj().p("playerid", getActivePlayerId(manager)).p("position", position)).equals("OK");
	}
	
	/**
	 * Returns the first {@link PLAYLIST_LIMIT} videos of the playlist. 
	 * @return Videos in the playlist.
	 */
	public ArrayList<String> getPlaylist(INotifiableManager manager) {
		JsonNode jsonItems = mConnection.getJson(manager, "PlayList.GetItems", obj().p("playlistid", PLAYLIST_ID).p("limits", obj().p("start", 0).p("end", PLAYLIST_LIMIT)).p("properties", arr().add("file")));
		JsonNode jsonSongs = jsonItems.get("items");
		final ArrayList<String> files = new ArrayList<String>();
		if (jsonSongs != null) {
			for (Iterator<JsonNode> i = jsonSongs.getElements(); i.hasNext();) {
				JsonNode jsonSong = (JsonNode)i.next();
				files.add(getString(jsonSong, "file"));
			}
		}
		return files;
	}
	
	/**
	 * Removes media from the current playlist. It is not possible to remove the media if it is currently being played.
	 * @param position Complete path (including filename) of the media to be removed.
	 * @return True on success, false otherwise.
	 */
	public boolean removeFromPlaylist(INotifiableManager manager, String path) {
		return mConnection.getString(manager, "Playlist.Remove", obj().p("playlistid", PLAYLIST_ID).p("position", "position")).equals("OK");
	}
}

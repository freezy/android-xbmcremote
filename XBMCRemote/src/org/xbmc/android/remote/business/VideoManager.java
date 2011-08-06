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

package org.xbmc.android.remote.business;

import java.util.ArrayList;

import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.business.ISortableManager;
import org.xbmc.api.business.IVideoManager;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.Movie;
import org.xbmc.api.type.SortType;
import org.xbmc.httpapi.WifiStateException;

import android.content.Context;

/**
 * Asynchronously wraps the {@link org.xbmc.httpapi.client.VideoClient} class.
 * 
 * @author Team XBMC
 */
public class VideoManager extends AbstractManager implements IVideoManager, ISortableManager, INotifiableManager {
	
	/**
	 * Updates the movie object with additional data (plot, cast, etc)
	 * @param response Response object
	 * @param movie Movie
	 */
	public void updateMovieDetails(final DataResponse<Movie> response, final Movie movie, final Context context) {
		mHandler.post(new Command<Movie>(response, this) {
			@Override
			public void doRun() throws Exception {
				response.value = video(context).updateMovieDetails(VideoManager.this, movie);
			}
		});
	}
	
	/**
	 * Gets all movies from database
	 * @param response Response object
	 */
	public void getMovies(final DataResponse<ArrayList<Movie>> response, final Context context) {
		mHandler.post(new Command<ArrayList<Movie>>(response, this) {
			@Override
			public void doRun() throws Exception { 
				response.value = video(context).getMovies(VideoManager.this, getSortBy(SortType.TITLE), getSortOrder(), getHideWatched(context));
			}
		});
	}
	
	/**
	 * SYNCHRONOUSLY gets all movies from database
	 * @return All movies in database
	 */
	public ArrayList<Movie> getMovies(final Context context) {
		try {
			return video(context).getMovies(VideoManager.this, getSortBy(SortType.TITLE), getSortOrder(), getHideWatched(context));
		} catch (WifiStateException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * SYNCHRONOUSLY gets movies from database with offset
	 * @return Movies in database with offset
	 */
	public ArrayList<Movie> getMovies(final Context context, int offset) {
		try {
			return video(context).getMovies(VideoManager.this, getSortBy(SortType.TITLE), getSortOrder(), offset, getHideWatched(context));
		} catch (WifiStateException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Gets all movies with an actor from database
	 * @param response Response object
	 * @param actor Actor
	 */
	public void getMovies(final DataResponse<ArrayList<Movie>> response, final Actor actor, final Context context) {
		mHandler.post(new Command<ArrayList<Movie>>(response, this) {
			@Override
			public void doRun() throws Exception { 
				response.value = video(context).getMovies(VideoManager.this, actor, getSortBy(SortType.TITLE), getSortOrder(), getHideWatched(context));
			}
		});
	}

	/**
	 * Gets all movies of a genre from database
	 * @param response Response object
	 * @param genre Genre
	 */
	public void getMovies(final DataResponse<ArrayList<Movie>> response, final Genre genre, final Context context) {
		mHandler.post(new Command<ArrayList<Movie>>(response, this) {
			@Override
			public void doRun() throws Exception { 
				response.value = video(context).getMovies(VideoManager.this, genre, getSortBy(SortType.TITLE), getSortOrder(), getHideWatched(context));
			}
		});
	}
	
	/**
	 * Gets all actors from database. Use {@link getMovieActors()} and
	 * {@link getTvActors()} for filtered actors. 
	 * @param response Response object
	 */
	public void getActors(final DataResponse<ArrayList<Actor>> response, final Context context) {
		mHandler.post(new Command<ArrayList<Actor>>(response, this) {
			@Override
			public void doRun() throws Exception { 
				response.value = video(context).getActors(VideoManager.this);
			}
		});
	}
	
	/**
	 * SYNCHRONOUSLY gets all actors from database. Use {@link getMovieActors()} and
	 * {@link getTvActors()} for filtered actors.
	 * @return All actors 
	 */
	public ArrayList<Actor> getActors(final Context context) {
		try {
			return video(context).getActors(VideoManager.this);
		} catch (WifiStateException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Gets all movie actors from database
	 * @param response Response object
	 */
	public void getMovieActors(final DataResponse<ArrayList<Actor>> response, final Context context) {
		mHandler.post(new Command<ArrayList<Actor>>(response, this) {
			@Override
			public void doRun() throws Exception { 
				response.value = video(context).getMovieActors(VideoManager.this);
			}
		});
	}

	/**
	 * Gets all TV show actors from database
	 * @param response Response object
	 */
	public void getTvShowActors(final DataResponse<ArrayList<Actor>> response, final Context context) {
		mHandler.post(new Command<ArrayList<Actor>>(response, this) {
			@Override
			public void doRun() throws Exception { 
				response.value = video(context).getTvShowActors(VideoManager.this);
			}
		});
	}
	
	/**
	 * Gets all movie genres from database
	 * @param response Response object
	 */
	public void getMovieGenres(final DataResponse<ArrayList<Genre>> response, final Context context) {
		mHandler.post(new Command<ArrayList<Genre>>(response, this) {
			@Override
			public void doRun() throws Exception { 
				response.value = video(context).getMovieGenres(VideoManager.this);
			}
		});
	}

	public void onWrongConnectionState(int state) {
		// TODO Auto-generated method stub
		
	}

	public void getTvShowGenres(DataResponse<ArrayList<Genre>> response,
			final Context context) {
		mHandler.post(new Command<ArrayList<Genre>>(response, this) {
			@Override
			public void doRun() throws Exception { 
				mResponse.value = video(context).getTvShowGenres(VideoManager.this);
			}
		});
	}
	
	/**
	 * Sets the media at playlist position to be the next item to be played.
	 * @param response Response object
	 * @param position Position, starting with 0.
	 */
	public void setPlaylistVideo(final DataResponse<Boolean> response, final int position, final Context context) {
		mHandler.post(new Command<Boolean>(response, this) {
			public void doRun() throws Exception{ 
				response.value = video(context).setPlaylistPosition(VideoManager.this, position);
			}
		});
	}
	
	/**
	 * Returns an array of videos on the playlist. Empty array if nothing is playing.
	 * @param response Response object
	 */
	public void getPlaylist(final DataResponse<ArrayList<String>> response, final Context context) {
		mHandler.post(new Command<ArrayList<String>>(response, this) {
			public void doRun() throws Exception{ 
				response.value = video(context).getPlaylist(VideoManager.this);
				final String firstEntry = response.value.get(0);
				if (firstEntry != null && firstEntry.equals("[Empty]")) {
					response.value = new ArrayList<String>();
				} 
			}
		});
	}
	
	/**
	 * Returns the position of the currently playing video in the playlist. First position is 0.
	 * @param response Response object
	 */
	public void getPlaylistPosition(final DataResponse<Integer> response, final Context context) {
		mHandler.post(new Command<Integer>(response, this) {
			public void doRun() throws Exception{ 
				response.value = video(context).getPlaylistPosition(VideoManager.this);
			}
		});
	}
	
	/**
	 * Removes media from the current playlist. It is not possible to remove the media if it is currently being played.
	 * @param position Complete path (including filename) of the media to be removed.
	 * @return True on success, false otherwise.
	 */
	public void removeFromPlaylist(final DataResponse<Boolean> response, final String path, final Context context) {
		mHandler.post(new Command<Boolean>(response, this) {
			public void doRun() throws Exception{ 
				response.value = video(context).removeFromPlaylist(VideoManager.this, path);
			}
		});
	}
}
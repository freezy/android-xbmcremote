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
import org.xbmc.api.object.FileLocation;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.Movie;
import org.xbmc.api.type.SortType;
import org.xbmc.httpapi.WifiStateException;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Asynchronously wraps the {@link org.xbmc.httpapi.client.VideoClient} class.
 * 
 * @author Team XBMC
 */
public class VideoManager extends AbstractManager implements IVideoManager, ISortableManager, INotifiableManager {

	private SharedPreferences mPref;
	private int mCurrentSortKey;
	
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
				response.value = video(context).getMovies(VideoManager.this, getSortBy(SortType.TITLE), getSortOrder());
			}
		});
	}
	
	/**
	 * SYNCHRONOUSLY gets all movies from database
	 * @return All movies in database
	 */
	public ArrayList<Movie> getMovies(final Context context) {
		try {
			return video(context).getMovies(VideoManager.this, getSortBy(SortType.TITLE), getSortOrder());
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
				response.value = video(context).getMovies(VideoManager.this, actor, getSortBy(SortType.TITLE), getSortOrder());
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
				response.value = video(context).getMovies(VideoManager.this, genre, getSortBy(SortType.TITLE), getSortOrder());
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
	
	/**
	 * Sets the static reference to the preferences object. Used to obtain
	 * current sort values.
	 * @param pref
	 */
	public void setPreferences(SharedPreferences pref) {
		mPref = pref;
	}
	
	/**
	 * Sets which kind of view is currently active.
	 * @param sortKey
	 */
	public void setSortKey(int sortKey) {
		mCurrentSortKey = sortKey;
	}
	
	/**
	 * Returns currently saved "sort by" value. If the preference was not set yet, or
	 * if the current sort key is not set, return default value.
	 * @param type Default value
	 * @return Sort by field
	 */
	private int getSortBy(int type) {
		if (mPref != null) {
			return mPref.getInt(AbstractManager.PREF_SORT_BY_PREFIX + mCurrentSortKey, type);
		}
		return type;
	}
	
	/**
	 * Returns currently saved "sort by" value. If the preference was not set yet, or
	 * if the current sort key is not set, return "ASC".
	 * @return Sort order
	 */
	private String getSortOrder() {
		if (mPref != null) {
			return mPref.getString(AbstractManager.PREF_SORT_ORDER_PREFIX + mCurrentSortKey, SortType.ORDER_ASC);
		}
		return SortType.ORDER_ASC;
	}

	public void onWrongConnectionState(int state) {
		// TODO Auto-generated method stub
		
	}
}
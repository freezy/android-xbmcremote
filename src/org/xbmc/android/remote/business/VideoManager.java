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

import org.xbmc.httpapi.data.Actor;
import org.xbmc.httpapi.data.Genre;
import org.xbmc.httpapi.data.Movie;
import org.xbmc.httpapi.type.SortType;

import android.content.SharedPreferences;

/**
 * Asynchronously wraps the {@link org.xbmc.httpapi.client.VideoClient} class.
 * 
 * @author Team XBMC
 */
public class VideoManager extends AbstractManager {

	private static SharedPreferences sPref;
	private static int sCurrentSortKey;
	
	/**
	 * Updates the movie object with additional data (plot, cast, etc)
	 * @param handler Callback handler
	 */
	public void updateMovieDetails(final DataResponse<Movie> handler, final Movie movie) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = video(handler).updateMovieDetails(movie);
				done(handler);
			}
		});
	}
	
	/**
	 * Gets all movies from database
	 * @param handler Callback handler
	 */
	public void getMovies(final DataResponse<ArrayList<Movie>> handler) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = video(handler).getMovies(getSortBy(SortType.TITLE), getSortOrder());
				done(handler);
			}
		});
	}
	
	/**
	 * Gets all movies with an actor from database
	 * @param handler Callback handler
	 */
	public void getMovies(final DataResponse<ArrayList<Movie>> handler, final Actor actor) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = video(handler).getMovies(actor, getSortBy(SortType.TITLE), getSortOrder());
				done(handler);
			}
		});
	}

	/**
	 * Gets all movies of a genre from database
	 * @param handler Callback handler
	 */
	public void getMovies(final DataResponse<ArrayList<Movie>> handler, final Genre genre) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = video(handler).getMovies(genre, getSortBy(SortType.TITLE), getSortOrder());
				done(handler);
			}
		});
	}
	
	/**
	 * Gets all actors from database. Use {@link getMovieActors()} and
	 * {@link getTvActors()} for filtered actors. 
	 * @param handler Callback handler
	 */
	public void getActors(final DataResponse<ArrayList<Actor>> handler) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = video(handler).getActors();
				done(handler);
			}
		});
	}
	
	/**
	 * Gets all movie actors from database
	 * @param handler Callback handler
	 */
	public void getMovieActors(final DataResponse<ArrayList<Actor>> handler) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = video(handler).getMovieActors();
				done(handler);
			}
		});
	}

	/**
	 * Gets all TV show actors from database
	 * @param handler Callback handler
	 */
	public void getTvShowActors(final DataResponse<ArrayList<Actor>> handler) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = video(handler).getTvShowActors();
				done(handler);
			}
		});
	}
	
	/**
	 * Gets all movie genres from database
	 * @param handler Callback handler
	 */
	public void getMovieGenres(final DataResponse<ArrayList<Genre>> handler) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = video(handler).getMovieGenres();
				done(handler);
			}
		});
	}
	
	/**
	 * Sets the static reference to the preferences object. Used to obtain
	 * current sort values.
	 * @param pref
	 */
	public static void setPreferences(SharedPreferences pref) {
		sPref = pref;
	}
	
	/**
	 * Sets which kind of view is currently active.
	 * @param sortKey
	 */
	public static void setSortKey(int sortKey) {
		sCurrentSortKey = sortKey;
	}
	
	/**
	 * Returns currently saved "sort by" value. If the preference was not set yet, or
	 * if the current sort key is not set, return default value.
	 * @param type Default value
	 * @return Sort by field
	 */
	private static int getSortBy(int type) {
		if (sPref != null) {
			return sPref.getInt(AbstractManager.PREF_SORT_BY_PREFIX + sCurrentSortKey, type);
		}
		return type;
	}
	
	/**
	 * Returns currently saved "sort by" value. If the preference was not set yet, or
	 * if the current sort key is not set, return "ASC".
	 * @return Sort order
	 */
	private static String getSortOrder() {
		if (sPref != null) {
			return sPref.getString(AbstractManager.PREF_SORT_ORDER_PREFIX + sCurrentSortKey, SortType.ORDER_ASC);
		}
		return SortType.ORDER_ASC;
	}
}
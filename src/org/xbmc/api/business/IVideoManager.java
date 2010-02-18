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

package org.xbmc.api.business;

import java.util.ArrayList;

import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.Movie;

import android.content.Context;

/**
 * This is the interface between the presentation layer and the business layer.
 * All the controller of the presentation layer gets to see is this interface.
 *  
 * @author Team XBMC
 */
public interface IVideoManager extends IManager {

	/**
	 * Updates the movie object with additional data (plot, cast, etc)
	 * @param response Response object
	 */
	public void updateMovieDetails(final DataResponse<Movie> response, final Movie movie, final Context context);
	
	/**
	 * Gets all movies from database
	 * @param response Response object
	 */
	public void getMovies(final DataResponse<ArrayList<Movie>> response, final Context context);
	
	/**
	 * SYNCHRONOUSLY gets all movies from database
	 * @return All movies in database
	 */
	public ArrayList<Movie> getMovies(final Context context);
	
	/**
	 * Gets all movies with an actor from database
	 * @param response Response object
	 * @param actor Actor
	 */
	public void getMovies(final DataResponse<ArrayList<Movie>> response, final Actor actor, final Context context);
	/**
	 * Gets all movies of a genre from database
	 * @param response Response object
	 * @param genre Genre
	 */
	public void getMovies(final DataResponse<ArrayList<Movie>> response, final Genre genre, final Context context);
	
	/**
	 * Gets all actors from database. Use {@link getMovieActors()} and
	 * {@link getTvActors()} for filtered actors. 
	 * @param response Response object
	 */
	public void getActors(final DataResponse<ArrayList<Actor>> response, final Context context);
	
	/**
	 * SYNCHRONOUSLY gets all actors from database.
	 * @return All actors 
	 */
	public ArrayList<Actor> getActors(final Context context);
	
	/**
	 * Gets all movie actors from database
	 * @param response Response object
	 */
	public void getMovieActors(final DataResponse<ArrayList<Actor>> response, final Context context);

	/**
	 * Gets all TV show actors from database
	 * @param response Response object
	 */
	public void getTvShowActors(final DataResponse<ArrayList<Actor>> response, final Context context);
	
	/**
	 * Gets all movie genres from database
	 * @param response Response object
	 */
	public void getMovieGenres(final DataResponse<ArrayList<Genre>> response, final Context context);
	
	/**
	 * Put in here everything that has to be cleaned up after leaving an activity.
	 */
	public void postActivity();
	
}
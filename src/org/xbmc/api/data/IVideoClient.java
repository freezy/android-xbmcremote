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

package org.xbmc.api.data;

import java.util.ArrayList;

import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.object.Movie;


/**
 * This is the interface between the business layer and the presentation layer.
 * All the business layer gets to see is this interface.
 *  
 * @author Team XBMC
 */
public interface IVideoClient {
	
	/**
	 * Gets all movies from database
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All movies
	 */
	public ArrayList<Movie> getMovies(int sortBy, String sortOrder);
	
	/**
	 * Gets all movies with an actor from database
	 * @param actor Display only movies with this actor.
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All movies with an actor
	 */
	public ArrayList<Movie> getMovies(Actor actor, int sortBy, String sortOrder);
	
	/**
	 * Gets all movies of a genre from database
	 * @param genre Display only movies of this genre.
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All movies of a genre
	 */
	public ArrayList<Movie> getMovies(Genre genre, int sortBy, String sortOrder);
	
	/**
	 * Gets all movies from database
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return Updated movie
	 */
	public Movie updateMovieDetails(Movie movie);
	
	/**
	 * Gets all actors from database. Use {@link getMovieActors()} and
	 * {@link getTvActors()} for filtered actors. 
	 * @return All actors
	 */
	public ArrayList<Actor> getActors();
	
	/**
	 * Gets all movie actors from database
	 * @return All movie actors
	 */
	public ArrayList<Actor> getMovieActors();
	
	/**
	 * Gets all movie actors from database
	 * @return All movie actors
	 */
	public ArrayList<Actor> getTvShowActors();
	
	/**
	 * Gets all movie genres from database
	 * @return All movie genres
	 */
	public ArrayList<Genre> getMovieGenres();
	
	/**
	 * Returns movie thumbnail as base64-encoded string
	 * @param cover
	 * @return Base64-encoded content of thumb
	 */
	public String getCover(ICoverArt cover);
}
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

import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.object.Movie;

import android.graphics.Bitmap;


/**
 * This is the interface between the business layer and the presentation layer.
 * All the business layer gets to see is this interface.
 *  
 * @author Team XBMC
 */
public interface IVideoClient extends IClient {
	
	/**
	 * Gets all movies from database
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All movies
	 */
	public ArrayList<Movie> getMovies(INotifiableManager manager, int sortBy, String sortOrder);
	
	/**
	 * Gets all movies with an actor from database
	 * @param actor Display only movies with this actor.
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All movies with an actor
	 */
	public ArrayList<Movie> getMovies(INotifiableManager manager, Actor actor, int sortBy, String sortOrder);
	
	/**
	 * Gets all movies of a genre from database
	 * @param genre Display only movies of this genre.
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All movies of a genre
	 */
	public ArrayList<Movie> getMovies(INotifiableManager manager, Genre genre, int sortBy, String sortOrder);
	
	/**
	 * Gets all movies from database
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return Updated movie
	 */
	public Movie updateMovieDetails(INotifiableManager manager, Movie movie);
	
	/**
	 * Gets all actors from database. Use {@link getMovieActors()} and
	 * {@link getTvActors()} for filtered actors. 
	 * @return All actors
	 */
	public ArrayList<Actor> getActors(INotifiableManager manager);
	
	/**
	 * Gets all movie actors from database
	 * @return All movie actors
	 */
	public ArrayList<Actor> getMovieActors(INotifiableManager manager);
	
	/**
	 * Gets all movie actors from database
	 * @return All movie actors
	 */
	public ArrayList<Actor> getTvShowActors(INotifiableManager manager);
	
	/**
	 * Gets all movie genres from database
	 * @return All movie genres
	 */
	public ArrayList<Genre> getMovieGenres(INotifiableManager manager);
	
	/**
	 * Gets all tv show genres from the database
	 * @return All tv show genres
	 */
	public ArrayList<Genre> getTvShowGenres(INotifiableManager manager);
	
	/**
	 * Returns a cover as bitmap
	 * @param cover
	 * @return Cover
	 */
	public Bitmap getCover(INotifiableManager manager, ICoverArt cover, int size);
	 
}
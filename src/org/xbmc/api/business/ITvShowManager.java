package org.xbmc.api.business;

import java.util.ArrayList;

import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Episode;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.Season;
import org.xbmc.api.object.TvShow;

import android.content.Context;

public interface ITvShowManager extends IManager{

	public void getTvShows(DataResponse<ArrayList<TvShow>> response, Context context);
	public void getTvShowActors(DataResponse<ArrayList<Actor>> response, Context context) ;
	public void getTvShowGenres(DataResponse<ArrayList<Genre>> response, Context context);
	public void getTvShows(DataResponse<ArrayList<TvShow>> response, Genre genre, Context context);
	
	/**
	 * Gets all tv shows with the specified actor
	 * @param manager
	 * @param actor
	 */
	public void getTvShows(DataResponse<ArrayList<TvShow>> response, Actor actor, Context context);
	
	/**
	 * Gets all Episodes for the specified show
	 * @param show
	 */
	public void getEpisodes(DataResponse<ArrayList<Episode>> response, TvShow show, Context context) ;
	
	/**
	 * Gets all seasons for the specified show
	 * @param show
	 */
	public void getSeasons(DataResponse<ArrayList<Season>> response, TvShow show, Context context);
}

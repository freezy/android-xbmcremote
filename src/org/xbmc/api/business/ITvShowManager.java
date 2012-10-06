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
	public ArrayList<TvShow> getTvShows(Context context);
	public ArrayList<Season> getAllSeasons(Context context);
	public ArrayList<Episode> getAllEpisodes(Context context);
	
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
	 * Gets all Episodes for the specified show and season
	 * @param response
	 * @param show
	 * @param season
	 * @param context
	 */
	public void getEpisodes(DataResponse<ArrayList<Episode>> response, TvShow show, Season season, Context context) ;
	
	/**
	 * Gets all Episodes for the specified season
	 * @param response
	 * @param season
	 * @param context
	 */
	public void getEpisodes(DataResponse<ArrayList<Episode>> response, Season season, Context context) ;
	
	/**
	 * Gets all recently added episodes
	 * @param response
	 * @param context
	 */
	public void getRecentlyAddedEpisodes(DataResponse<ArrayList<Episode>> response, Context context) ;
	
	/**
	 * Gets all seasons for the specified show
	 * @param show
	 */
	public void getSeasons(DataResponse<ArrayList<Season>> response, TvShow show, Context context);
	
	/**
	 * Updates the given Episode with 
	 * @param response
	 * @param episode
	 * @param context
	 */
	public void updateEpisodeDetails(DataResponse<Episode> response, Episode episode, Context context);
	
	/**
	 * Updates the give TvShow
	 * @param response
	 * @param show
	 */
	public void updateTvShowDetails(DataResponse<TvShow> response, TvShow show, Context context);
	
	/**
	 * Put in here everything that has to be cleaned up after leaving an activity.
	 */
	public void postActivity();
}

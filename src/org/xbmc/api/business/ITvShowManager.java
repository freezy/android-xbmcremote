package org.xbmc.api.business;

import java.util.ArrayList;

import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Episode;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.object.Season;
import org.xbmc.api.object.TvShow;
import org.xbmc.httpapi.WifiStateException;

import android.content.Context;
import android.graphics.Bitmap;

public interface ITvShowManager extends IManager{
	
	public void getTvShows(DataResponse<ArrayList<TvShow>> response, Context context);
	public void getTvShowActors(DataResponse<ArrayList<Actor>> response, Context context) ;
	public void getTvShowGenres(DataResponse<ArrayList<Genre>> response, Context context);
	public void getTvShows(DataResponse<ArrayList<TvShow>> response, Genre genre, Context context);
	public void getAllSeasons(DataResponse<ArrayList<Season>> response, Context context);
	public void getAllEpisodes(DataResponse<ArrayList<Episode>> response, Context context);
	
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
	
	/**
	 * Does the actual downloading of images into various caches
	 * @param response
	 * @param cover
	 * @param thumbSize
	 * @param context
	 */
	public void downloadCover(final DataResponse<Bitmap> response, ICoverArt cover, int thumbSize, Context context) throws WifiStateException;
	
}

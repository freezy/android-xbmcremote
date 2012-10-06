package org.xbmc.api.data;

import java.util.ArrayList;

import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Episode;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.object.Season;
import org.xbmc.api.object.TvShow;

import android.graphics.Bitmap;

public interface ITvShowClient extends IClient {

	public ArrayList<TvShow> getTvShows(INotifiableManager manager, int sortBy, String sortOrder, boolean hideWatched);
	public ArrayList<Actor> getTvShowActors(INotifiableManager manager) ;
	public ArrayList<Genre> getTvShowGenres(INotifiableManager manager);
	
	/**
	 * Gets all tv shows with the specified genre
	 * @param manager
	 * @param genre
	 * @return
	 */
	public ArrayList<TvShow> getTvShows(INotifiableManager manager, Genre genre, int sortBy, String sortOrder, boolean hideWatched);
	
	/**
	 * Gets all tv shows with the specified actor
	 * @param manager
	 * @param actor
	 * @return
	 */
	public ArrayList<TvShow> getTvShows(INotifiableManager manager, Actor actor, int sortBy, String sortOrder, boolean hideWatched);
	
	/**
	 * Gets all Episodes for the specified show
	 * @param manager
	 * @param show
	 * @return
	 */
	public ArrayList<Episode> getEpisodes(INotifiableManager manager, TvShow show, int sortBy, String sortOrder, boolean hideWatched) ;
	
	/**
	 * Gets all Episodes for the specified season
	 * @param manager
	 * @param season
	 * @return
	 */
	public ArrayList<Episode> getEpisodes(INotifiableManager manager, Season season, int sortBy, String sortOrder, boolean hideWatched) ;
	
	/**
	 * Gets all Episodes for the specified show and season
	 * @param manager
	 * @param show
	 * @param season
	 * @return
	 */
	public ArrayList<Episode> getEpisodes(INotifiableManager manager, TvShow show, Season season, int sortBy, String sortOrder, boolean hideWatched) ;
	
	/**
	 * Gets all episodes from all shows
	 * @param manager
	 * @return
	 */
	public ArrayList<Episode> getEpisodes(INotifiableManager manager, int sortBy, String sortOrder, boolean hideWatched);
	
	/**
	 * Gets recently added episodes
	 * @param manager
	 * @return
	 */
	public ArrayList<Episode> getRecentlyAddedEpisodes(INotifiableManager manager, boolean hideWatched) ;
	/**
	 * Gets all seasons for the specified show
	 * @param manager
	 * @param show
	 * @return
	 */
	public ArrayList<Season> getSeasons(INotifiableManager manager, TvShow show, boolean hideWatched);
	
	/**
	 * Gets all seasons from all shows
	 * @param manager
	 * @return
	 */
	public ArrayList<Season> getSeasons(INotifiableManager manager, int sortBy, String sortOrder, boolean hideWatched);
	
	/**
	 * Returns a cover as bitmap
	 * @param cover
	 * @return Cover
	 */
	public Bitmap getCover(INotifiableManager manager, ICoverArt cover, int size);
	
	/**
	 * Updates the episode with plot and actors
	 * @param manager
	 * @param episode
	 * @return
	 */
	public Episode updateEpisodeDetails(INotifiableManager manager, Episode episode);
	
	/**
	 * Updates the show with summary
	 * @param manager
	 * @param show
	 * @return
	 */
	public TvShow updateTvShowDetails(INotifiableManager manager, TvShow show);

}

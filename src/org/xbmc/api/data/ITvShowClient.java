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

	public ArrayList<TvShow> getTvShows(INotifiableManager manager);
	public ArrayList<Actor> getTvShowActors(INotifiableManager manager) ;
	public ArrayList<Genre> getTvShowGenres(INotifiableManager manager);
	public ArrayList<TvShow> getTvShows(INotifiableManager manager, Genre genre);
	
	/**
	 * Gets all tv shows with the specified actor
	 * @param manager
	 * @param actor
	 * @return
	 */
	public ArrayList<TvShow> getTvShows(INotifiableManager manager, Actor actor);
	
	/**
	 * Gets all Episodes for the specified show
	 * @param manager
	 * @param show
	 * @return
	 */
	public ArrayList<Episode> getEpisodes(INotifiableManager manager, TvShow show) ;
	
	/**
	 * Gets all Episodes for the specified season
	 * @param manager
	 * @param season
	 * @return
	 */
	public ArrayList<Episode> getEpisodes(INotifiableManager manager, Season season) ;
	
	/**
	 * Gets all Episodes for the specified show and season
	 * @param manager
	 * @param show
	 * @param season
	 * @return
	 */
	public ArrayList<Episode> getEpisodes(INotifiableManager manager, TvShow show, Season season) ;
	
	/**
	 * Gets all seasons for the specified show
	 * @param manager
	 * @param show
	 * @return
	 */
	public ArrayList<Season> getSeasons(INotifiableManager manager, TvShow show);
	
	
	/**
	 * Returns a cover as bitmap
	 * @param cover
	 * @return Cover
	 */
	public Bitmap getCover(INotifiableManager manager, ICoverArt cover, int size);

}

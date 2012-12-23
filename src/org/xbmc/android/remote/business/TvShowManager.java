package org.xbmc.android.remote.business;

import java.util.ArrayList;

import org.xbmc.android.util.ClientFactory;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.business.ISortableManager;
import org.xbmc.api.business.ITvShowManager;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Episode;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.object.Season;
import org.xbmc.api.object.TvShow;
import org.xbmc.api.type.SortType;
import org.xbmc.httpapi.WifiStateException;

import android.content.Context;
import android.graphics.Bitmap;

public class TvShowManager extends AbstractManager implements ITvShowManager,
		ISortableManager, INotifiableManager {

	/**
	 * Gets all tv shows actors from database
	 * @param response Response object
	 */
	public void getTvShowActors(final DataResponse<ArrayList<Actor>> response, final Context context) {
		mHandler.post(new Command<ArrayList<Actor>>(response, this) {
			@Override
			public void doRun() throws Exception {
				response.value = shows(context).getTvShowActors(TvShowManager.this);
			}
		});
	}

	/**
	 * Gets all tv show genres from database
	 * @param response Response object
	 */
	public void getTvShowGenres(final DataResponse<ArrayList<Genre>> response, final Context context) {
		mHandler.post(new Command<ArrayList<Genre>>(response, this) {
			@Override
			public void doRun() throws Exception {
				response.value = shows(context).getTvShowGenres(TvShowManager.this);
			}
		});
	}

	/**
	 * Gets all tv shows from database
	 * @param response Response object
	 */
	public void getTvShows(final DataResponse<ArrayList<TvShow>> response, final Context context) {
		mHandler.post(new Command<ArrayList<TvShow>>(response, this) {
			@Override
			public void doRun() throws Exception {
				response.value = shows(context).getTvShows(TvShowManager.this, getSort(SortType.TITLE), getHideWatched(context));
			}
		});
	}
	
	/**
	 * gets all tv show seasons from database
	 * @return All tv show seasons in database
	 */
	public void getAllSeasons(DataResponse<ArrayList<Season>> response, final Context context) {
		
		mHandler.post(new Command<ArrayList<Season>>(response, this) {
			@Override
			public void doRun() throws Exception {
				mResponse.value = shows(context).getSeasons(TvShowManager.this, getSort(SortType.TITLE), getHideWatched(context));
			}
		});
	}
	
	/**
	 * gets all tv show episodes from database
	 * @return All tv show episodes in database
	 */
	public void getAllEpisodes(DataResponse<ArrayList<Episode>> response, final Context context) {
		
		mHandler.post(new Command<ArrayList<Episode>>(response, this) {
			@Override
			public void doRun() throws Exception {

				mResponse.value =  shows(context).getEpisodes(TvShowManager.this, getSort(SortType.EPISODE_NUM), getHideWatched(context));
			}
		});
	}

	/**
	 * Gets all tv shows of a genre from database
	 * @param response Response object
	 * @param genre Genre of the tv shows
	 */
	public void getTvShows(final DataResponse<ArrayList<TvShow>> response, final Genre genre, final Context context) {
		mHandler.post(new Command<ArrayList<TvShow>>(response, this) {
			@Override
			public void doRun() throws Exception {
				response.value = shows(context).getTvShows(TvShowManager.this, genre, getSort(SortType.TITLE), getHideWatched(context));
			}
		});
	}

	/**
	 * Gets all tv shows of an actor from database
	 * @param response Response object
	 * @param actor Actor of the tv shows
	 */
	public void getTvShows(DataResponse<ArrayList<TvShow>> response, final Actor actor, final Context context) {
		mHandler.post(new Command<ArrayList<TvShow>>(response, this) {
			@Override
			public void doRun() throws Exception {
				mResponse.value = shows(context).getTvShows(TvShowManager.this, actor, getSort(SortType.TITLE), getHideWatched(context));
			}
		});
	}

	/**
	 * Gets all episodes of a tv show from database
	 * @param response Response object
	 * @param show TvShow the returning episodes belong to
	 */
	public void getEpisodes(DataResponse<ArrayList<Episode>> response,
			final TvShow show, final Context context) {
		mHandler.post(new Command<ArrayList<Episode>>(response, this) {
			@Override
			public void doRun() throws Exception {
				mResponse.value = shows(context).getEpisodes(TvShowManager.this, show, getSort(SortType.EPISODE_NUM), getHideWatched(context));
			}
		});
	}

	/**
	 * Gets all seasons of a tv show from database
	 * @param response Response object
	 * @param show TvShow the returning seasons belong to
	 */
	public void getSeasons(DataResponse<ArrayList<Season>> response,
			final TvShow show, final Context context) {
		mHandler.post(new Command<ArrayList<Season>>(response, this) {
			@Override
			public void doRun() throws Exception {
				mResponse.value = shows(context).getSeasons(TvShowManager.this, show, getHideWatched(context));
			}
		});
	}

	/**
	 * Gets all episodes of a season of a tv show from database
	 * @param response Response object
	 * @param show TvShow the returning episodes belong to
	 * @param season Season the returning episodes belong to
	 */
	public void getEpisodes(DataResponse<ArrayList<Episode>> response,
			final TvShow show, final Season season, final Context context) {
		mHandler.post(new Command<ArrayList<Episode>>(response, this) {
			@Override
			public void doRun() throws Exception {
				mResponse.value = shows(context).getEpisodes(TvShowManager.this, show, season, getSort(SortType.EPISODE_NUM), getHideWatched(context));
			}
		});
		
	}

	/**
	 * Updates the episode object with additional data from the episodeview table
	 * @param response Response object
	 * @param episode Episode to update
	 */
	public void updateEpisodeDetails(DataResponse<Episode> response, final Episode episode, final Context context) {
		mHandler.post(new Command<Episode>(response, this) {
			@Override
			public void doRun() throws Exception {
				mResponse.value = shows(context).updateEpisodeDetails(TvShowManager.this, episode);
			}
		});
	}

	/**
	 * Updates the show object with additional data from the tvshow table
	 * @param response Response object
	 * @param show TvShow to update
	 */
	public void updateTvShowDetails(DataResponse<TvShow> response, final TvShow show, final Context context) {
		mHandler.post(new Command<TvShow>(response, this) {
			@Override
			public void doRun() throws Exception {
				mResponse.value = shows(context).updateTvShowDetails(TvShowManager.this, show);
			}
		});
	}
	
	public void downloadCover(DataResponse<Bitmap> response, ICoverArt cover,
			int thumbSize, Context context) throws WifiStateException {
		response.value = ClientFactory.getTvShowClient(this, context).getCover(this, cover,
				thumbSize);
		
	}
}

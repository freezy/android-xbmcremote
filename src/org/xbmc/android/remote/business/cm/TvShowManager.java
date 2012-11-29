package org.xbmc.android.remote.business.cm;

import java.util.ArrayList;
import java.util.List;

import org.xbmc.android.jsonrpc.api.AbstractCall;
import org.xbmc.android.jsonrpc.api.call.VideoLibrary;
import org.xbmc.android.jsonrpc.api.model.VideoModel.TVShowDetail;
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
import org.xbmc.httpapi.WifiStateException;

import android.content.Context;
import android.graphics.Bitmap;

public class TvShowManager extends AbstractManager implements ITvShowManager,
		ISortableManager, INotifiableManager {

	public void getTvShows(DataResponse<ArrayList<TvShow>> response,
			Context context) {

		call(new VideoLibrary.GetTVShows(null, getSort(TVShowDetail.TITLE),
				(VideoLibrary.GetTVShows.FilterGenreId) null,
				TVShowDetail.TITLE, TVShowDetail.RATING,
				TVShowDetail.PREMIERED, TVShowDetail.GENRE, TVShowDetail.MPAA,
				TVShowDetail.STUDIO, TVShowDetail.FILE, TVShowDetail.EPISODE,
				TVShowDetail.WATCHEDEPISODES, TVShowDetail.PLOT),
				new ApiHandler<ArrayList<TvShow>, TVShowDetail>() {

					@Override
					public ArrayList<TvShow> handleResponse(
							AbstractCall<TVShowDetail> apiCall) {
						List<TVShowDetail> tvShowDetails = apiCall.getResults();

						ArrayList<TvShow> result = new ArrayList<TvShow>();
						for (TVShowDetail tvShow : tvShowDetails) {
							result.add(new TvShow(tvShow));
						}
						return result;
					}
				}, response, context);
	}

	public void getTvShowActors(DataResponse<ArrayList<Actor>> response,
			Context context) {
		// TODO: Add to API

	}

	public void getTvShowGenres(DataResponse<ArrayList<Genre>> response,
			Context context) {
		// TODO: Add to API
	}

	public void getTvShows(DataResponse<ArrayList<TvShow>> response,
			Genre genre, Context context) {
		// TODO Auto-generated method stub

	}

	public ArrayList<TvShow> getTvShows(Context context) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Season> getAllSeasons(Context context) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Episode> getAllEpisodes(Context context) {
		// TODO Auto-generated method stub
		return null;
	}

	public void getTvShows(DataResponse<ArrayList<TvShow>> response,
			Actor actor, Context context) {
		// TODO Auto-generated method stub

	}

	public void getEpisodes(DataResponse<ArrayList<Episode>> response,
			TvShow show, Context context) {
		// TODO Auto-generated method stub

	}

	public void getEpisodes(DataResponse<ArrayList<Episode>> response,
			TvShow show, Season season, Context context) {
		// TODO Auto-generated method stub

	}

	public void getEpisodes(DataResponse<ArrayList<Episode>> response,
			Season season, Context context) {
		// TODO Auto-generated method stub

	}

	public void getSeasons(DataResponse<ArrayList<Season>> response,
			TvShow show, Context context) {
		// TODO Auto-generated method stub

	}

	public void updateEpisodeDetails(DataResponse<Episode> response,
			Episode episode, Context context) {
		// TODO Auto-generated method stub

	}

	public void updateTvShowDetails(DataResponse<TvShow> response, TvShow show,
			Context context) {
		// TODO Auto-generated method stub

	}

	public void downloadCover(DataResponse<Bitmap> response, ICoverArt cover,
			int thumbSize, Context context) throws WifiStateException {
		response.value = getCover(cover, thumbSize, TvShow.getThumbUri(cover),
				TvShow.getFallbackThumbUri(cover));

	}

}

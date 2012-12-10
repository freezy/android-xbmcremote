package org.xbmc.android.remote.business.cm;

import java.util.ArrayList;
import java.util.List;

import org.xbmc.android.jsonrpc.api.AbstractCall;
import org.xbmc.android.jsonrpc.api.call.VideoLibrary;
import org.xbmc.android.jsonrpc.api.model.LibraryModel;
import org.xbmc.android.jsonrpc.api.model.LibraryModel.GenreDetail;
import org.xbmc.android.jsonrpc.api.model.VideoModel;
import org.xbmc.android.jsonrpc.api.model.VideoModel.Cast;
import org.xbmc.android.jsonrpc.api.model.VideoModel.EpisodeDetail;
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
import android.util.Log;

public class TvShowManager extends AbstractManager implements ITvShowManager,
		ISortableManager, INotifiableManager {

	public void getTvShows(DataResponse<ArrayList<TvShow>> response,
			Context context) {

		call(new VideoLibrary.GetTVShows(null, getSort(TVShowDetail.TITLE),
				(VideoLibrary.GetTVShows.FilterGenreId) null,
				TVShowDetail.TITLE, TVShowDetail.RATING,
				TVShowDetail.PREMIERED, TVShowDetail.GENRE, TVShowDetail.MPAA,
				TVShowDetail.STUDIO, TVShowDetail.FILE, TVShowDetail.EPISODE,
				TVShowDetail.WATCHEDEPISODES, TVShowDetail.PLOT,
				TVShowDetail.THUMBNAIL),
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
		// Currently FRODO doesn't support Actor listings.
		response.value = new ArrayList<Actor>();
		onFinish(response);
	}

	public void getTvShowGenres(DataResponse<ArrayList<Genre>> response,
			Context context) {
		call(new VideoLibrary.GetGenres("tvshow", null,
				getSort(GenreDetail.LABEL), GenreDetail.THUMBNAIL),
				new ApiHandler<ArrayList<Genre>, LibraryModel.GenreDetail>() {
					@Override
					public ArrayList<Genre> handleResponse(
							AbstractCall<GenreDetail> apiCall) {
						List<GenreDetail> genreDetails = apiCall.getResults();

						ArrayList<Genre> result = new ArrayList<Genre>();
						for (GenreDetail genreDetail : genreDetails) {
							result.add(new Genre(genreDetail));
						}
						return result;
					}
				}, response, context);

	}

	public void getTvShows(DataResponse<ArrayList<TvShow>> response,
			Genre genre, Context context) {
		call(new VideoLibrary.GetTVShows(null, getSort(TVShowDetail.TITLE),
				new VideoLibrary.GetTVShows.FilterGenreId(genre.getId()),
				TVShowDetail.TITLE, TVShowDetail.RATING,
				TVShowDetail.PREMIERED, TVShowDetail.GENRE, TVShowDetail.MPAA,
				TVShowDetail.STUDIO, TVShowDetail.FILE, TVShowDetail.EPISODE,
				TVShowDetail.WATCHEDEPISODES, TVShowDetail.PLOT,
				TVShowDetail.THUMBNAIL),
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

	public ArrayList<TvShow> getTvShows(Context context) {
		// TODO Remove me
		return null;
	}

	public ArrayList<Season> getAllSeasons(Context context) {
		// TODO Remove me
		return null;
	}

	public ArrayList<Episode> getAllEpisodes(Context context) {
		// TODO Remove me
		return null;
	}

	public void getTvShows(DataResponse<ArrayList<TvShow>> response,
			Actor actor, Context context) {
		call(new VideoLibrary.GetTVShows(null, getSort(TVShowDetail.TITLE),
				new VideoLibrary.GetTVShows.FilterActor(actor.getName()),
				TVShowDetail.TITLE, TVShowDetail.RATING,
				TVShowDetail.PREMIERED, TVShowDetail.GENRE, TVShowDetail.MPAA,
				TVShowDetail.STUDIO, TVShowDetail.FILE, TVShowDetail.EPISODE,
				TVShowDetail.WATCHEDEPISODES, TVShowDetail.PLOT,
				TVShowDetail.THUMBNAIL),
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

	public void getEpisodes(DataResponse<ArrayList<Episode>> response,
			TvShow show, Context context) {

		call(new VideoLibrary.GetEpisodes(show.getId(),
				getSort(TVShowDetail.TITLE), VideoModel.EpisodeDetail.TITLE,
				VideoModel.EpisodeDetail.PLOT, VideoModel.EpisodeDetail.RATING,
				VideoModel.EpisodeDetail.WRITER,
				VideoModel.EpisodeDetail.FIRSTAIRED,
				VideoModel.EpisodeDetail.DIRECTOR,
				VideoModel.EpisodeDetail.EPISODE,
				VideoModel.EpisodeDetail.FILE,
				VideoModel.EpisodeDetail.SHOWTITLE,
				VideoModel.EpisodeDetail.THUMBNAIL),
				new ApiHandler<ArrayList<Episode>, VideoModel.EpisodeDetail>() {

					@Override
					public ArrayList<Episode> handleResponse(
							AbstractCall<EpisodeDetail> apiCall) {
						List<VideoModel.EpisodeDetail> episodeDetails = apiCall
								.getResults();

						ArrayList<Episode> result = new ArrayList<Episode>();
						for (VideoModel.EpisodeDetail episode : episodeDetails) {
							result.add(new Episode(episode));
						}
						return result;

					}

				}, response, context);

	}

	public void getEpisodes(DataResponse<ArrayList<Episode>> response,
			TvShow show, Season season, Context context) {
		call(new VideoLibrary.GetEpisodes(show.getId(), season.number,
				getSort(VideoModel.EpisodeDetail.EPISODE),
				VideoModel.EpisodeDetail.TITLE, VideoModel.EpisodeDetail.PLOT,
				VideoModel.EpisodeDetail.RATING,
				VideoModel.EpisodeDetail.WRITER,
				VideoModel.EpisodeDetail.FIRSTAIRED,
				VideoModel.EpisodeDetail.DIRECTOR,
				VideoModel.EpisodeDetail.EPISODE,
				VideoModel.EpisodeDetail.FILE,
				VideoModel.EpisodeDetail.SHOWTITLE,
				VideoModel.EpisodeDetail.THUMBNAIL),
				new ApiHandler<ArrayList<Episode>, VideoModel.EpisodeDetail>() {

					@Override
					public ArrayList<Episode> handleResponse(
							AbstractCall<EpisodeDetail> apiCall) {
						List<VideoModel.EpisodeDetail> episodeDetails = apiCall
								.getResults();

						Log.e(TAG, "Found " + episodeDetails.size()
								+ " episodes");
						ArrayList<Episode> result = new ArrayList<Episode>();
						for (VideoModel.EpisodeDetail episode : episodeDetails) {
							result.add(new Episode(episode));
						}
						return result;

					}

				}, response, context);

	}

	public void getSeasons(DataResponse<ArrayList<Season>> response,
			final TvShow show, Context context) {
		call(new VideoLibrary.GetSeasons(show.getId(),
				getSort(VideoModel.SeasonDetail.SEASON),
				VideoModel.SeasonDetail.SEASON,
				VideoModel.SeasonDetail.WATCHEDEPISODES,
				VideoModel.SeasonDetail.TVSHOWID,
				VideoModel.SeasonDetail.THUMBNAIL),
				new ApiHandler<ArrayList<Season>, VideoModel.SeasonDetail>() {

					@Override
					public ArrayList<Season> handleResponse(
							AbstractCall<VideoModel.SeasonDetail> apiCall) {
						List<VideoModel.SeasonDetail> seasonDetails = apiCall
								.getResults();

						ArrayList<Season> result = new ArrayList<Season>();
						for (VideoModel.SeasonDetail season : seasonDetails) {
							result.add(new Season(season, show));
						}
						return result;

					}

				}, response, context);

	}

	public void updateEpisodeDetails(DataResponse<Episode> response,
			final Episode episode, Context context) {
		call(new VideoLibrary.GetEpisodeDetails(episode.getId(),
				EpisodeDetail.CAST, EpisodeDetail.PLOT, EpisodeDetail.DIRECTOR,
				EpisodeDetail.WRITER),
				new ApiHandler<Episode, EpisodeDetail>() {

					@Override
					public Episode handleResponse(
							AbstractCall<EpisodeDetail> apiCall) {
						EpisodeDetail episodeDetail = apiCall.getResult();
						List<Cast> cast = episodeDetail.cast;
						for (Cast member : cast) {
							episode.actors.add(new Actor(member));
						}
						episode.plot = episodeDetail.plot;
						episode.director = episodeDetail.director;
						episode.writer = episodeDetail.writer;
						return episode;
					}
				}, response, context);

	}

	public void updateTvShowDetails(DataResponse<TvShow> response,
			final TvShow show, Context context) {
		call(new VideoLibrary.GetTVShowDetails(show.getId(), TVShowDetail.CAST),
				new ApiHandler<TvShow, TVShowDetail>() {

					@Override
					public TvShow handleResponse(
							AbstractCall<TVShowDetail> apiCall) {
						TVShowDetail tvShowDetail = apiCall.getResult();
						List<Cast> cast = tvShowDetail.cast;
						for (Cast member : cast) {
							show.actors.add(new Actor(member));
						}
						return show;
					}
				}, response, context);
	}

	public void downloadCover(DataResponse<Bitmap> response, ICoverArt cover,
			int thumbSize, Context context) throws WifiStateException {
		response.value = getCover(cover, thumbSize, TvShow.getThumbUri(cover),
				TvShow.getFallbackThumbUri(cover));

	}

	public void getAllEpisodes(DataResponse<ArrayList<Episode>> response,
			Context context) {
		call(new VideoLibrary.GetEpisodes(getSort(TVShowDetail.TITLE),
				VideoModel.EpisodeDetail.TITLE, VideoModel.EpisodeDetail.PLOT,
				VideoModel.EpisodeDetail.RATING,
				VideoModel.EpisodeDetail.WRITER,
				VideoModel.EpisodeDetail.FIRSTAIRED,
				VideoModel.EpisodeDetail.DIRECTOR,
				VideoModel.EpisodeDetail.EPISODE,
				VideoModel.EpisodeDetail.FILE,
				VideoModel.EpisodeDetail.SHOWTITLE,
				VideoModel.EpisodeDetail.THUMBNAIL),
				new ApiHandler<ArrayList<Episode>, VideoModel.EpisodeDetail>() {

					@Override
					public ArrayList<Episode> handleResponse(
							AbstractCall<EpisodeDetail> apiCall) {
						List<VideoModel.EpisodeDetail> episodeDetails = apiCall
								.getResults();

						ArrayList<Episode> result = new ArrayList<Episode>();
						for (VideoModel.EpisodeDetail episode : episodeDetails) {
							result.add(new Episode(episode));
						}
						return result;

					}

				}, response, context);
	}

	public void getAllSeasons(DataResponse<ArrayList<Season>> response,
			Context context) {
		// currently we can't pull all seasons via the API
		response.value = new ArrayList<Season>();
		onFinish(response);
	}

}

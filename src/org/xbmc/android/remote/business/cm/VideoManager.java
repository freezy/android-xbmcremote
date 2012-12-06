package org.xbmc.android.remote.business.cm;

import java.util.ArrayList;
import java.util.List;

import org.xbmc.android.jsonrpc.api.AbstractCall;
import org.xbmc.android.jsonrpc.api.call.VideoLibrary;
import org.xbmc.android.jsonrpc.api.call.VideoLibrary.GetMovies.FilterActor;
import org.xbmc.android.jsonrpc.api.model.LibraryModel;
import org.xbmc.android.jsonrpc.api.model.LibraryModel.GenreDetail;
import org.xbmc.android.jsonrpc.api.model.VideoModel.Cast;
import org.xbmc.android.jsonrpc.api.model.VideoModel.MovieDetail;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.business.ISortableManager;
import org.xbmc.api.business.IVideoManager;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.object.Movie;
import org.xbmc.httpapi.WifiStateException;

import android.content.Context;
import android.graphics.Bitmap;

public class VideoManager extends AbstractManager implements IVideoManager,
		ISortableManager, INotifiableManager {

	public void updateMovieDetails(DataResponse<Movie> response, final Movie movie,
			Context context) {
		call(new VideoLibrary.GetMovieDetails(movie.getId(),
				MovieDetail.STUDIO, MovieDetail.PLOT, MovieDetail.MPAA, MovieDetail.CAST),
				new ApiHandler<Movie, MovieDetail>() {

					@Override
					public Movie handleResponse(
							AbstractCall<MovieDetail> apiCall) {
						MovieDetail movieDetail = apiCall.getResult();
						movie.studio = movieDetail.studio;
						movie.plot = movieDetail.plot;
						movie.rated = movieDetail.mpaa;
						
						List<Cast> cast = movieDetail.cast;
						for(Cast member : cast) {
							movie.actors.add(new Actor(member));
						}
						return movie;
					}
				}, response, context);


	}

	public void getMovies(DataResponse<ArrayList<Movie>> response,
			Context context) {
		call(new VideoLibrary.GetMovies(getSort(MovieDetail.TITLE),
				MovieDetail.TITLE, MovieDetail.YEAR, MovieDetail.PLAYCOUNT,
				MovieDetail.FILE, MovieDetail.DIRECTOR, MovieDetail.RUNTIME,
				MovieDetail.GENRE, MovieDetail.RATING, MovieDetail.IMDBNUMBER,
				MovieDetail.THUMBNAIL),
				new ApiHandler<ArrayList<Movie>, MovieDetail>() {

					@Override
					public ArrayList<Movie> handleResponse(
							AbstractCall<MovieDetail> apiCall) {
						List<MovieDetail> movieDetails = apiCall.getResults();

						ArrayList<Movie> result = new ArrayList<Movie>();
						for (MovieDetail movie : movieDetails) {
							result.add(new Movie(movie));
						}
						return result;
					}
				}, response, context);

	}

	public ArrayList<Movie> getMovies(Context context) {
		// FIXME: fix covers
		return null;
	}

	public void getMovies(DataResponse<ArrayList<Movie>> response, Actor actor,
			Context context) {
		call(new VideoLibrary.GetMovies(null, getSort(MovieDetail.TITLE),
				new FilterActor(actor.name), MovieDetail.TITLE,
				MovieDetail.YEAR, MovieDetail.PLAYCOUNT, MovieDetail.FILE,
				MovieDetail.DIRECTOR, MovieDetail.RUNTIME, MovieDetail.GENRE,
				MovieDetail.RATING, MovieDetail.IMDBNUMBER,
				MovieDetail.THUMBNAIL),
				new ApiHandler<ArrayList<Movie>, MovieDetail>() {

					@Override
					public ArrayList<Movie> handleResponse(
							AbstractCall<MovieDetail> apiCall) {
						List<MovieDetail> movieDetails = apiCall.getResults();

						ArrayList<Movie> result = new ArrayList<Movie>();
						for (MovieDetail movie : movieDetails) {
							result.add(new Movie(movie));
						}
						return result;
					}
				}, response, context);

	}

	public void getMovies(DataResponse<ArrayList<Movie>> response, Genre genre,
			Context context) {
		call(new VideoLibrary.GetMovies(null, getSort(MovieDetail.TITLE),
				new VideoLibrary.GetMovies.FilterGenreId(genre.id),
				MovieDetail.TITLE, MovieDetail.YEAR, MovieDetail.PLAYCOUNT,
				MovieDetail.FILE, MovieDetail.DIRECTOR, MovieDetail.RUNTIME,
				MovieDetail.GENRE, MovieDetail.RATING, MovieDetail.IMDBNUMBER,
				MovieDetail.THUMBNAIL),
				new ApiHandler<ArrayList<Movie>, MovieDetail>() {

					@Override
					public ArrayList<Movie> handleResponse(
							AbstractCall<MovieDetail> apiCall) {
						List<MovieDetail> movieDetails = apiCall.getResults();

						ArrayList<Movie> result = new ArrayList<Movie>();
						for (MovieDetail movie : movieDetails) {
							result.add(new Movie(movie));
						}
						return result;
					}
				}, response, context);

	}

	public void getActors(DataResponse<ArrayList<Actor>> response,Context context) {
		response.value = new ArrayList<Actor>();
		onFinish(response);

	}

	public void getMovieActors(DataResponse<ArrayList<Actor>> response,
			Context context) {
		// currently we cannot return this in Frodo
		response.value = new ArrayList<Actor>();
		onFinish(response);
	}

	public void getMovieGenres(DataResponse<ArrayList<Genre>> response,
			Context context) {
		call(new VideoLibrary.GetGenres("movie", null,
				getSort(LibraryModel.GenreDetail.TITLE)),
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

	public void setPlaylistVideo(DataResponse<Boolean> response, int position,
			Context context) {
		setPlaylist(PLAYLIST_VIDEO, response, position, context);
	}

	public void removeFromPlaylist(DataResponse<Boolean> response,
			int position, Context context) {
		removeFromPlaylist(PLAYLIST_VIDEO, response, position, context);
	}

	public void getPlaylist(DataResponse<ArrayList<String>> response,
			Context context) {
		getPlaylist(PLAYLIST_VIDEO, response, context);
	}

	public void getPlaylistPosition(DataResponse<Integer> response,
			Context context) {
		getPlaylistPosition(PLAYLIST_VIDEO, response, context);
	}

	public void downloadCover(DataResponse<Bitmap> response, ICoverArt cover,
			int thumbSize, Context context) throws WifiStateException {

		response.value = getCover(cover, thumbSize, Movie.getThumbUri(cover),
				Movie.getFallbackThumbUri(cover));
	}

}

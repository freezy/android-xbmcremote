package org.xbmc.android.remote.business.cm;

import java.util.ArrayList;

import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IVideoManager;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.object.Movie;
import org.xbmc.httpapi.WifiStateException;

import android.content.Context;
import android.graphics.Bitmap;

public class VideoManager extends AbstractManager implements IVideoManager {

	public void updateMovieDetails(DataResponse<Movie> response, Movie movie,
			Context context) {
		// TODO Auto-generated method stub

	}

	public void getMovies(DataResponse<ArrayList<Movie>> response,
			Context context) {
		// TODO Auto-generated method stub

	}

	public ArrayList<Movie> getMovies(Context context) {
		// TODO Auto-generated method stub
		return null;
	}

	public void getMovies(DataResponse<ArrayList<Movie>> response, Actor actor,
			Context context) {
		// TODO Auto-generated method stub

	}

	public void getMovies(DataResponse<ArrayList<Movie>> response, Genre genre,
			Context context) {
		// TODO Auto-generated method stub

	}

	public void getActors(DataResponse<ArrayList<Actor>> response,
			Context context) {
		// TODO Auto-generated method stub

	}

	public ArrayList<Actor> getActors(Context context) {
		// TODO Auto-generated method stub
		return null;
	}

	public void getMovieActors(DataResponse<ArrayList<Actor>> response,
			Context context) {
		// TODO Auto-generated method stub

	}

	public void getTvShowActors(DataResponse<ArrayList<Actor>> response,
			Context context) {
		// TODO Auto-generated method stub

	}

	public void getMovieGenres(DataResponse<ArrayList<Genre>> response,
			Context context) {
		// TODO Auto-generated method stub

	}

	public void getTvShowGenres(DataResponse<ArrayList<Genre>> response,
			Context context) {
		// TODO Auto-generated method stub

	}

	public void setPlaylistVideo(DataResponse<Boolean> response, int position,
			Context context) {
		// TODO Auto-generated method stub

	}

	public void removeFromPlaylist(DataResponse<Boolean> response, int position,
			Context context) {
		// TODO Auto-generated method stub

	}

	public void getPlaylist(DataResponse<ArrayList<String>> response,
			Context context) {
		// TODO Auto-generated method stub

	}

	public void getPlaylistPosition(DataResponse<Integer> response,
			Context context) {
		// TODO Auto-generated method stub
	}
	
	public void downloadCover(DataResponse<Bitmap> response, ICoverArt cover,
			int thumbSize, Context context) throws WifiStateException {
		
		response.value = getCover(cover, thumbSize, Movie.getThumbUri(cover),
				Movie.getFallbackThumbUri(cover));
	}
	

}

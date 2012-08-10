package org.xbmc.jsonrpc.client;

import java.util.ArrayList;

import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IVideoClient;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.Host;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.object.Movie;
import org.xbmc.jsonrpc.Connection;

import android.graphics.Bitmap;

public class VideoClient extends Client implements IVideoClient {

	public VideoClient(Connection connection) {
		super(connection);
		// TODO Auto-generated constructor stub
	}

	public void setHost(Host host) {
		// TODO Auto-generated method stub

	}

	public ArrayList<Movie> getMovies(INotifiableManager manager, int sortBy,
			String sortOrder, boolean hideWatched) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Movie> getMovies(INotifiableManager manager, int sortBy,
			String sortOrder, int offset, boolean hideWatched) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Movie> getMovies(INotifiableManager manager, Actor actor,
			int sortBy, String sortOrder, boolean hideWatched) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Movie> getMovies(INotifiableManager manager, Genre genre,
			int sortBy, String sortOrder, boolean hideWatched) {
		// TODO Auto-generated method stub
		return null;
	}

	public Movie updateMovieDetails(INotifiableManager manager, Movie movie) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Actor> getActors(INotifiableManager manager) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Actor> getMovieActors(INotifiableManager manager) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Actor> getTvShowActors(INotifiableManager manager) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Genre> getMovieGenres(INotifiableManager manager) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Genre> getTvShowGenres(INotifiableManager manager) {
		// TODO Auto-generated method stub
		return null;
	}

	public Bitmap getCover(INotifiableManager manager, ICoverArt cover, int size) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getPlaylistPosition(INotifiableManager manager) {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean setPlaylistPosition(INotifiableManager manager, int position) {
		// TODO Auto-generated method stub
		return false;
	}

	public ArrayList<String> getPlaylist(INotifiableManager manager) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean removeFromPlaylist(INotifiableManager manager, String path) {
		// TODO Auto-generated method stub
		return false;
	}

}

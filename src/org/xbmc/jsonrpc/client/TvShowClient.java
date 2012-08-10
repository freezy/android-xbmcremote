package org.xbmc.jsonrpc.client;

import java.util.ArrayList;

import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.ITvShowClient;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Episode;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.Host;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.object.Season;
import org.xbmc.api.object.TvShow;
import org.xbmc.jsonrpc.Connection;

import android.graphics.Bitmap;

public class TvShowClient extends Client implements ITvShowClient {

	public TvShowClient(Connection connection) {
		super(connection);
	}

	public void setHost(Host host) {
		// TODO Auto-generated method stub
		
	}

	public ArrayList<TvShow> getTvShows(INotifiableManager manager, int sortBy,
			String sortOrder, boolean hideWatched) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Actor> getTvShowActors(INotifiableManager manager) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Genre> getTvShowGenres(INotifiableManager manager) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<TvShow> getTvShows(INotifiableManager manager,
			Genre genre, int sortBy, String sortOrder, boolean hideWatched) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<TvShow> getTvShows(INotifiableManager manager,
			Actor actor, int sortBy, String sortOrder, boolean hideWatched) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Episode> getEpisodes(INotifiableManager manager,
			TvShow show, int sortBy, String sortOrder, boolean hideWatched) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Episode> getEpisodes(INotifiableManager manager,
			Season season, int sortBy, String sortOrder, boolean hideWatched) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Episode> getEpisodes(INotifiableManager manager,
			TvShow show, Season season, int sortBy, String sortOrder,
			boolean hideWatched) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Episode> getEpisodes(INotifiableManager manager,
			int sortBy, String sortOrder, boolean hideWatched) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Season> getSeasons(INotifiableManager manager,
			TvShow show, boolean hideWatched) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Season> getSeasons(INotifiableManager manager, int sortBy,
			String sortOrder, boolean hideWatched) {
		// TODO Auto-generated method stub
		return null;
	}

	public Bitmap getCover(INotifiableManager manager, ICoverArt cover, int size) {
		// TODO Auto-generated method stub
		return null;
	}

	public Episode updateEpisodeDetails(INotifiableManager manager,
			Episode episode) {
		// TODO Auto-generated method stub
		return null;
	}

	public TvShow updateTvShowDetails(INotifiableManager manager, TvShow show) {
		// TODO Auto-generated method stub
		return null;
	}

}

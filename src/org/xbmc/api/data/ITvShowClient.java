package org.xbmc.api.data;

import java.util.ArrayList;

import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.TvShow;

public interface ITvShowClient extends IClient {

	public ArrayList<TvShow> getTvShows(INotifiableManager manager, int sortBy, String sortOrder);
	public ArrayList<Actor> getTvShowActors(INotifiableManager manager) ;
	public ArrayList<Genre> getTvShowGenres(INotifiableManager manager);
	public ArrayList<TvShow> getTvShows(INotifiableManager manager, Genre genre);

}

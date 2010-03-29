package org.xbmc.api.business;

import java.util.ArrayList;

import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.TvShow;

import android.content.Context;

public interface ITvShowManager extends IManager{

	public void getTvShows(DataResponse<ArrayList<TvShow>> response, int sortBy, String sortOrder, Context context);
	public void getTvShowActors(DataResponse<ArrayList<Actor>> response, Context context) ;
	public void getTvShowGenres(DataResponse<ArrayList<Genre>> response, Context context);
	public void getTvShows(DataResponse<ArrayList<TvShow>> response, Genre genre, Context context);
}

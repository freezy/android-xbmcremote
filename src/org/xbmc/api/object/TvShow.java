package org.xbmc.api.object;

import java.util.List;

public class TvShow {

	public int id;
	public String title;
	public String summary;
	public double rating = 0.0;
	public String firstAired;
	public String contentRating;
	public String network;
	public String genre;
	
	public List<Season> seasons = null;
	public List<Actor> actors = null;
	
	public TvShow(int id, String title, String summary, double rating, String firstAired, 
			String contentRating, String network, String genre) {
		this.id = id;
		this.title = title;
		this.summary = summary;
		this.rating = rating;
		this.firstAired = firstAired;
		this.contentRating = contentRating;
		this.network = network;
		this.genre = genre;
	}
	
}

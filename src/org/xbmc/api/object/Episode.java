package org.xbmc.api.object;

public class Episode {

	public int id;
	public String title;
	public String plot;
	public double rating = 0.0;
	public String writer;
	public String firstAired;
	public boolean watched;
	public String director;
	public int season;
	public int episode;
	
	public Episode(int id, String title, String plot, double rating, String writer, String firstAired,
			boolean watched, String director, int season, int episode){
		this.id = id;
		this.title = title;
		this.plot = plot;
		this.rating = rating;
		this.writer = writer;
		this.firstAired = firstAired;
		this.watched = watched;
		this.director = director;
		this.season = season;
		this.episode = episode;
	}
}

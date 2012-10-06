/*
 *      Copyright (C) 2005-2010 Team XBMC
 *      http://xbmc.org
 *
 *  This Program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2, or (at your option)
 *  any later version.
 *
 *  This Program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XBMC Remote; see the file license.  If not, write to
 *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *  http://www.gnu.org/copyleft/gpl.html
 *
 */

package org.xbmc.jsonrpc.client;

import java.util.ArrayList;
import java.util.Iterator;

import org.codehaus.jackson.JsonNode;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.ITvShowClient;
import org.xbmc.api.data.IControlClient.ICurrentlyPlaying;
import org.xbmc.api.info.PlayStatus;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Episode;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.Host;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.object.Season;
import org.xbmc.api.object.TvShow;
import org.xbmc.api.type.MediaType;
import org.xbmc.api.type.SortType;
import org.xbmc.jsonrpc.Connection;
import android.graphics.Bitmap;

/**
 * TV show client for JSON RPC.
 * <br /><pre>
 * </pre>
 * @author Team XBMC
 */
public class TvShowClient extends Client implements ITvShowClient {

	public TvShowClient(Connection connection) {
		super(connection);
	}
	

	public ArrayList<TvShow> getTvShows(INotifiableManager manager, int sortBy, String sortOrder, boolean hideWatched) {
		return getTvShows(manager, obj(), sortBy, sortOrder, hideWatched);
	}	
	public ArrayList<TvShow> getTvShows(INotifiableManager manager, ObjNode obj, int sortBy, String sortOrder, boolean hideWatched) {
		
		sort(obj.p(PARAM_PROPERTIES, arr().add("plot").add("rating").add("premiered").add("genre").add("mpaa").add("studio").add("file").add("episode").add("playcount").add("thumbnail")), sortBy, sortOrder);
		
		final ArrayList<TvShow> tvshows = new ArrayList<TvShow>();
		final JsonNode result = mConnection.getJson(manager, "VideoLibrary.GetTvShows", obj);
		if(result.size() > 0){
			final JsonNode jsonShows = result.get("tvshows");
			for (Iterator<JsonNode> i = jsonShows.getElements(); i.hasNext();) {
				JsonNode jsonShow = (JsonNode)i.next();
				
				int playcount =getInt(jsonShow, "playcount");
				if(playcount > 0 && hideWatched)
					continue;
				
				tvshows.add(new TvShow(
					getInt(jsonShow, "tvshowid"),
					getString(jsonShow, "label"),
					getString(jsonShow, "plot"),
					getDouble(jsonShow, "rating"),
					getString(jsonShow, "premiered"),
					getString(jsonShow, "genre"),
					getString(jsonShow, "mpaa"),
					getString(jsonShow, "studio"),
					getString(jsonShow, "file"),
					getInt(jsonShow, "episode"),
					playcount,
					getInt(jsonShow, "playcount") > 0,
					getString(jsonShow, "thumbnail")
				));
			}
		}
		return tvshows;
	}
	
	/**
	 * Gets all tv show actors from database
	 * @return All tv show actors
	 */
	public ArrayList<Actor> getTvShowActors(INotifiableManager manager) {
		//TODO
		return new ArrayList<Actor>();
	}
	
	/**
	 * Gets all tv show genres from database
	 * @return All tv show genres
	 */
	public ArrayList<Genre> getTvShowGenres(INotifiableManager manager) {
		ObjNode obj = sort(obj().p("type", "tvshow"), SortType.TITLE, "descending");
		
		final ArrayList<Genre> genres = new ArrayList<Genre>();
		final JsonNode result = mConnection.getJson(manager, "VideoLibrary.GetGenres", obj);
		if(result.size() > 0){
			final JsonNode jsonGenres = result.get("genres");
			for (Iterator<JsonNode> i = jsonGenres.getElements(); i.hasNext();) {
				JsonNode jsonGenre = (JsonNode)i.next();
				genres.add(new Genre(
					getInt(jsonGenre, "genreid"),
					getString(jsonGenre, "label")
				));
			}
		}
		return genres;
	}
	
	/**
	 * Gets all tv shows with the specified actor
	 * @param manager
	 * @param actor
	 * @return
	 */
	public ArrayList<TvShow> getTvShows(INotifiableManager manager, Actor actor, int sortBy, String sortOrder, boolean hideWatched) {
		return getTvShows(manager, obj().p("filter",  obj().p("actor", actor.name)), sortBy, sortOrder, hideWatched);
	}
	
	/**
	 * Gets all tv shows for the specified genre
	 * 
	 */
	public ArrayList<TvShow> getTvShows(INotifiableManager manager, Genre genre, int sortBy, String sortOrder, boolean hideWatched) {
		return getTvShows(manager, obj().p("filter",  obj().p("genreid", genre.id)), sortBy, sortOrder, hideWatched);
	}
	
	/**
	 * Gets all seasons for the specified show
	 * @param manager
	 * @param show
	 * @return
	 */
	public ArrayList<Season> getSeasons(INotifiableManager manager, TvShow show, boolean hideWatched) {
		
		ObjNode obj = sort(obj().p("tvshowid", show.id).p(PARAM_PROPERTIES, arr().add("season").add("playcount").add("thumbnail")), SortType.TITLE, "ascending");
		
		final ArrayList<Season> seasons = new ArrayList<Season>();
		final JsonNode result = mConnection.getJson(manager, "VideoLibrary.GetSeasons", obj);
		if(result.size() > 0){
			final JsonNode jsonSeasons = result.get("seasons");
			for (Iterator<JsonNode> i = jsonSeasons.getElements(); i.hasNext();) {
				
				JsonNode jsonShow = (JsonNode)i.next();
				
				int playcount =getInt(jsonShow, "playcount");
				if(playcount > 0 && hideWatched)
					continue;
				
				seasons.add(new Season(
					getInt(jsonShow, "season"),
					playcount > 0,
					show,
					getString(jsonShow, "thumbnail")
				));
			}
		}
		return seasons;
	}
	
	/**
	 * Gets all seasons for all shows
	 * @param manager
	 * @param show
	 * @return
	 */
	public ArrayList<Season> getSeasons(INotifiableManager manager, int sortBy, String sortOrder, boolean hideWatched) {
		ArrayList<TvShow> shows = getTvShows(manager, sortBy, sortOrder, hideWatched);
		ArrayList<Season> seasons = new ArrayList<Season>();
		for (TvShow tvShow : shows) {
			seasons.addAll(getSeasons(manager, tvShow, hideWatched));
		}
		return seasons;
	}
	
	/**
	 * Gets all Episodes for the specified show
	 * @param manager
	 * @param show
	 * @return
	 */
	public ArrayList<Episode> getEpisodes(INotifiableManager manager, TvShow show, int sortBy, String sortOrder, boolean hideWatched) {
		return getEpisodes(manager, show, null, sortBy, sortOrder, hideWatched);
	}
	
	/**
	 * Gets all Episodes for the specified season
	 * @param manager
	 * @param season
	 * @return
	 */
	public ArrayList<Episode> getEpisodes(INotifiableManager manager, Season season, int sortBy, String sortOrder, boolean hideWatched) {
		return getEpisodes(manager, season.show, season, sortBy, sortOrder, hideWatched);
	}
	
	public ArrayList<Episode> getRecentlyAddedEpisodes(INotifiableManager manager, boolean hideWatched) {
		
		ObjNode obj = obj().p(PARAM_PROPERTIES, arr().add("title").add("plot").add("rating").add("writer").add("firstaired").add("playcount").add("director").add("season").add("episode").add("file").add("showtitle").add("thumbnail"));
		
		final ArrayList<Episode> episodes = new ArrayList<Episode>();
		final JsonNode result = mConnection.getJson(manager, "VideoLibrary.GetRecentlyAddedEpisodes", obj);
		if(result.size() > 0){
			final JsonNode jsonEpisodes = result.get("episodes");
			for (Iterator<JsonNode> i = jsonEpisodes.getElements(); i.hasNext();) {
				JsonNode jsonEpisode = (JsonNode)i.next();
				
				int playcount =getInt(jsonEpisode, "playcount");
				if(playcount > 0 && hideWatched)
					continue;
				
				episodes.add(new Episode(
					getInt(jsonEpisode, "episodeid"),
					getString(jsonEpisode, "title"),
					getString(jsonEpisode, "plot"),
					getDouble(jsonEpisode, "rating"),
					getString(jsonEpisode, "writer"),
					getString(jsonEpisode, "firstaired"),
					playcount,
					getString(jsonEpisode, "director"),
					getInt(jsonEpisode, "season"),
					getInt(jsonEpisode, "episode"),
					"",
					getString(jsonEpisode, "file"),
					getString(jsonEpisode, "showtitle"),
					getString(jsonEpisode, "thumbnail")
				));
			}
		}
		return episodes;
	}
	
	/**
	 * Gets all Episodes for all shows
	 * @param manager
	 * @return
	 */
	public ArrayList<Episode> getEpisodes(INotifiableManager manager, int sortBy, String sortOrder, boolean hideWatched){
		return getEpisodes(manager, obj(), sortBy, sortOrder, hideWatched);
	}
	public ArrayList<Episode> getEpisodes(INotifiableManager manager, ObjNode obj, int sortBy, String sortOrder, boolean hideWatched) {
		
		obj = sort(obj.p(PARAM_PROPERTIES, arr().add("title").add("plot").add("rating").add("writer").add("firstaired").add("playcount").add("director").add("season").add("episode").add("file").add("showtitle").add("thumbnail")), sortBy, sortOrder);
		
		final ArrayList<Episode> episodes = new ArrayList<Episode>();
		final JsonNode result = mConnection.getJson(manager, "VideoLibrary.GetEpisodes", obj);
		if(result.size() > 0){
			final JsonNode jsonEpisodes = result.get("episodes");
			for (Iterator<JsonNode> i = jsonEpisodes.getElements(); i.hasNext();) {
				JsonNode jsonEpisode = (JsonNode)i.next();
				
				int playcount =getInt(jsonEpisode, "playcount");
				if(playcount > 0 && hideWatched)
					continue;
				
				episodes.add(new Episode(
					getInt(jsonEpisode, "episodeid"),
					getString(jsonEpisode, "title"),
					getString(jsonEpisode, "plot"),
					getDouble(jsonEpisode, "rating"),
					getString(jsonEpisode, "writer"),
					getString(jsonEpisode, "firstaired"),
					playcount,
					getString(jsonEpisode, "director"),
					getInt(jsonEpisode, "season"),
					getInt(jsonEpisode, "episode"),
					"",
					getString(jsonEpisode, "file"),
					getString(jsonEpisode, "showtitle"),
					getString(jsonEpisode, "thumbnail")
				));
			}
		}
		return episodes;
	}
	
	/**
	 * Gets all Episodes for the specified show and season
	 * @param manager
	 * @param show
	 * @param season
	 * @return
	 */
	public ArrayList<Episode> getEpisodes(INotifiableManager manager, TvShow show, Season season, int sortBy, String sortOrder, boolean hideWatched) {
		return getEpisodes(manager, obj().p("tvshowid", show.id).p("season", season.number), sortBy, sortOrder, hideWatched);
	}
	
	public TvShow updateTvShowDetails(INotifiableManager manager, TvShow show) {
		
		ObjNode obj = obj().p("tvshowid", show.id).p(PARAM_PROPERTIES, arr().add("cast"));
		
		final ArrayList<Actor> actors = new ArrayList<Actor>();
		final JsonNode result = mConnection.getJson(manager, "VideoLibrary.GetTvShowDetails", obj);
		if(result.size() > 0){
			final JsonNode jsonCast = result.get("tvshowdetails").get("cast");
			for (Iterator<JsonNode> i = jsonCast.getElements(); i.hasNext();) {
				JsonNode jsonActor = (JsonNode)i.next();
				actors.add(new Actor(
						getInt(jsonActor,"actorid"),
						getString(jsonActor, "name"),
						getString(jsonActor, "thumbnail"),
						getString(jsonActor, "role")
				));
			}
			show.actors = actors;
		}
		return show;
	}
	
	public Episode updateEpisodeDetails(INotifiableManager manager, Episode episode) {
		ObjNode obj = obj().p("episodeid", episode.id).p(PARAM_PROPERTIES, arr().add("cast"));
		
		final ArrayList<Actor> actors = new ArrayList<Actor>();
		final JsonNode result = mConnection.getJson(manager, "VideoLibrary.GetEpisodeDetails", obj);
		if(result.size() > 0){
			final JsonNode jsonCast = result.get("episodedetails").get("cast");
			for (Iterator<JsonNode> i = jsonCast.getElements(); i.hasNext();) {
				JsonNode jsonActor = (JsonNode)i.next();
				actors.add(new Actor(
						getInt(jsonActor,"actorid"),
						getString(jsonActor, "name"),
						getString(jsonActor, "thumbnail"),
						getString(jsonActor, "role")
				));
			}
			episode.actors = actors;
		}
		return episode;
	}
	
	static ICurrentlyPlaying getCurrentlyPlaying(final JsonNode player, final JsonNode item) {
		return new ICurrentlyPlaying() {
			private static final long serialVersionUID = 5036994329211476714L;
			public String getTitle() {
				return getString(item, "showtitle");
			}
			public int getTime() {
				return ControlClient.parseTime(player.get("time"));
			}
			public int getPlayStatus() {
				return getInt(player, "speed");
			}
			public int getPlaylistPosition() {
				return getInt(player, "position");
			}
			//Workarond for bug in Float.valueOf(): http://code.google.com/p/android/issues/detail?id=3156
			public float getPercentage() {
				try{
					return getInt(player, "percentage");
				} catch (NumberFormatException e) { }
				return (float)getDouble(player, "percentage");
			}
			public String getFilename() {
				return getString(item, "file");
			}
			public int getDuration() {
				return ControlClient.parseTime(player.get("totaltime"));
			}
			public String getArtist() {
				if(getInt(item, "season") == 0) {
					return "Specials / Episode " + getInt(item, "episode");
				}
				else {
					return "Season " + getInt(item, "season") + " / Episode " + getInt(item, "episode");
				}
			}
			public String getAlbum() {
				return getString(item, "title");
			}
			public int getMediaType() {
				return MediaType.VIDEO_TVSHOW;
			}
			public boolean isPlaying() {
				return getInt(player, "speed") == PlayStatus.PLAYING;
			}
			public int getHeight() {
				return 0;
			}
			public int getWidth() {
				return 0;
			}
		};
	}
	
	/**
	 * Returns a pre-resized movie cover. Pre-resizing is done in a way that
	 * the bitmap at least as large as the specified size but not larger than
	 * the double.
	 * @param manager Postback manager
	 * @param cover Cover object
	 * @param size Minmal size to pre-resize to.
	 * @return Thumbnail bitmap
	 */
	public Bitmap getCover(INotifiableManager manager, ICoverArt cover, int size) {
		
		String url = null;
		if(!TvShow.getThumbUri(cover).equals("")){
			final JsonNode dl = mConnection.getJson(manager, "Files.PrepareDownload", obj().p("path", TvShow.getThumbUri(cover)));
			if(dl != null){
				JsonNode details = dl.get("details");
				if(details != null)
					url = mConnection.getUrl(getString(details, "path"));
			}
		}
		return getCover(manager, cover, size, url);
	}
	

	/**
	 * Updates host info on the connection.
	 * @param host
	 */
	public void setHost(Host host) {
		mConnection.setHost(host);
	}
}

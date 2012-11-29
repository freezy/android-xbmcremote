package org.xbmc.jsonrpc.client;

import java.util.ArrayList;
import java.util.Iterator;

import org.codehaus.jackson.JsonNode;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.ITvShowClient;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Episode;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.object.Season;
import org.xbmc.api.object.TvShow;
import org.xbmc.api.type.Sort;
import org.xbmc.jsonrpc.Connection;

import android.graphics.Bitmap;

public class TvShowClient extends Client implements ITvShowClient {

	public TvShowClient(Connection connection) {
		super(connection);
	}

	public ArrayList<TvShow> getTvShows(INotifiableManager manager, Sort sort,
			boolean hideWatched) {
		// TODO: handle filters
		return getTvShows(manager, obj(), sort, hideWatched);
	}

	public ArrayList<Actor> getTvShowActors(INotifiableManager manager) {
		// TODO: Add back in actor support
		return new ArrayList<Actor>();
	}

	public ArrayList<Genre> getTvShowGenres(INotifiableManager manager) {
		// TODO: unused? This also exists in VideoClient
		return new ArrayList<Genre>();

	}

	public ArrayList<TvShow> getTvShows(INotifiableManager manager,
			Genre genre, Sort sort, boolean hideWatched) {
		// ObjNode obj = obj().p("filter", obj().p("field",
		// "genre").p("operator", "is").p("value", genre.getId()));
		return getTvShows(manager, obj(), sort, hideWatched);
	}

	public ArrayList<TvShow> getTvShows(INotifiableManager manager,
			Actor actor, Sort sort, boolean hideWatched) {
		// ObjNode obj = obj().p("filter", obj().p("field",
		// "actor").p("operator", "is").p("value", actor.getId()));
		return getTvShows(manager, obj(), sort, hideWatched);
	}

	public ArrayList<TvShow> getTvShows(INotifiableManager manager,
			ObjNode obj, Sort sort, boolean hideWatched) {
//		obj.p("properties",
//				arr().add("title").add("rating").add("premiered").add("genre")
//						.add("mpaa").add("studio").add("file").add("episode"));
//		obj = sort(obj, sort);
//		return parseTvShows(mConnection.getJson(manager,
//				"VideoLibrary.GetTvShows", obj));
			return null;
	}

	public TvShow getTvShow(INotifiableManager manager, int tvshowid) {
//		ObjNode obj = obj().p("tvshowid", tvshowid);
//		obj.p("properties",
//				arr().add("title").add("rating").add("premiered").add("genre")
//						.add("mpaa").add("studio").add("file").add("episode"));
//
//		JsonNode result = mConnection.getJson(manager,
//				"VideoLibrary.GetTvShowDetails", obj);
//		JsonNode tvshowdetails = result.get("tvshowdetails");
//		if (tvshowdetails == null) {
//			return null;
//		}
//		return parseTvShow(tvshowdetails);
		return null;

	}

	private ArrayList<TvShow> parseTvShows(JsonNode result) {
		ArrayList<TvShow> tvShows = new ArrayList<TvShow>();
		final JsonNode jsonTvShows = result.get("tvshows");
		if (jsonTvShows == null) {
			return tvShows;
		}
		for (Iterator<JsonNode> i = jsonTvShows.getElements(); i.hasNext();) {
			JsonNode jsonTvShow = (JsonNode) i.next();
			tvShows.add(parseTvShow(jsonTvShow));
		}

		return tvShows;
	}

	private TvShow parseTvShow(JsonNode jsonTvShow) {
		return new TvShow(getInt(jsonTvShow, "tvshowid"), getString(jsonTvShow,
				"title"), "", getDouble(jsonTvShow, "rating"), getString(
				jsonTvShow, "premiered"), getString(jsonTvShow, "genre"),
				getString(jsonTvShow, "mpaa"), getString(jsonTvShow, "studio"),
				getString(jsonTvShow, "file"), getInt(jsonTvShow, "episode"),
				0, false/* currently don't handle watched episodes */
		);
	}

	public ArrayList<Episode> getEpisodes(INotifiableManager manager,
			TvShow show, Sort sort, boolean hideWatched) {
		// TODO: we can no longer pull down all episodes
		// this is only used for coverart caching
		return new ArrayList<Episode>();
	}

	public ArrayList<Episode> getEpisodes(INotifiableManager manager,
			Season season, Sort sort, boolean hideWatched) {
		// TODO we can not pull down epsiodes for a season without the TV show
		return new ArrayList<Episode>();
	}

	public ArrayList<Episode> getEpisodes(INotifiableManager manager,
			TvShow show, Season season, Sort sort, boolean hideWatched) {
//		ObjNode obj = obj();
//		obj.p("tvshowid", show.getId());
//		obj.p("season", season.getNumber());
//		obj.p("properties",
//				arr().add("title").add("plot").add("rating").add("writer")
//						.add("firstaired").add("director").add("season")
//						.add("episode").add("file").add("showtitle"));
//		obj = sort(obj, sort);
//		return parseEpisodes(mConnection.getJson(manager,
//				"VideoLibrary.GetEpisodes", obj));
		return null;
	}

	private ArrayList<Episode> parseEpisodes(JsonNode result) {
		ArrayList<Episode> episodes = new ArrayList<Episode>();
		final JsonNode jsonEpisodes = result.get("episodes");
		if (jsonEpisodes == null) {
			return episodes;
		}
		for (Iterator<JsonNode> i = jsonEpisodes.getElements(); i.hasNext();) {
			JsonNode jsonEpisode = (JsonNode) i.next();
			String fullpath = getString(jsonEpisode, "file").replace("\\", "/");
			String localPath = fullpath.substring(0, fullpath.lastIndexOf('/'));
			String filename = fullpath.replace(localPath, "");
			episodes.add(new Episode(getInt(jsonEpisode, "episodeid"),
					getString(jsonEpisode, "title"), getString(jsonEpisode,
							"plot"), getDouble(jsonEpisode, "rating"),
					getString(jsonEpisode, "writer"), getString(jsonEpisode,
							"firstaired"), 0,
					getString(jsonEpisode, "director"), getInt(jsonEpisode,
							"season"), getInt(jsonEpisode, "episode"),
					localPath, filename, getString(jsonEpisode, "showtitle")));
		}

		return episodes;
	}

	public ArrayList<Episode> getEpisodes(INotifiableManager manager,
			Sort sort, boolean hideWatched) {
		// TODO we can't do this without a TV show and season any longer
		// we mainly do this for thumbnail caching, though
		return new ArrayList<Episode>();
	}

	public ArrayList<Season> getSeasons(INotifiableManager manager,
			TvShow show, boolean hideWatched) {
		ObjNode obj = obj().p("tvshowid", show.getId());
		return getSeasons(manager, obj, hideWatched);
	}

	public ArrayList<Season> getSeasons(INotifiableManager manager, Sort sort,
			boolean hideWatched) {
		// TODO we can't do this without a TV show any longer
		// we mainly do this for thumbnail caching, though
		return new ArrayList<Season>();
	}

	public ArrayList<Season> getSeasons(INotifiableManager manager,
			ObjNode obj, boolean hideWatched) {
//		obj.p("properties", arr().add("season").add("tvshowid"));
//		return parseSeasons(manager,
//				mConnection.getJson(manager, "VideoLibrary.GetSeasons", obj));
		return null;
	}

	private ArrayList<Season> parseSeasons(INotifiableManager manager,
			JsonNode result) {
		ArrayList<Season> seasons = new ArrayList<Season>();
		final JsonNode jsonSeasons = result.get("seasons");
		if (jsonSeasons == null) {
			return seasons;
		}
		for (Iterator<JsonNode> i = jsonSeasons.getElements(); i.hasNext();) {
			JsonNode jsonSeason = (JsonNode) i.next();
			seasons.add(new Season(getInt(jsonSeason, "season"), false,
					getTvShow(manager, getInt(jsonSeason, "tvshowid"))));
		}

		return seasons;
	}

	public Bitmap getCover(INotifiableManager manager, ICoverArt cover, int size) {
		return getCover(manager, cover, size, TvShow.getThumbUri(cover),
				TvShow.getFallbackThumbUri(cover));
	}

	public Episode updateEpisodeDetails(INotifiableManager manager,
			Episode episode) {
		// TODO: Fill in more detail
		return episode;
	}

	public TvShow updateTvShowDetails(INotifiableManager manager, TvShow show) {
		// TODO: Fill in more detail
		return show;
	}

}

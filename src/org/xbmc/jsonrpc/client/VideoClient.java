package org.xbmc.jsonrpc.client;

import java.util.ArrayList;
import java.util.Iterator;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IControlClient.ICurrentlyPlaying;
import org.xbmc.api.data.IVideoClient;
import org.xbmc.api.info.PlayStatus;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.object.Movie;
import org.xbmc.api.type.MediaType;
import org.xbmc.api.type.Sort;
import org.xbmc.jsonrpc.Connection;

import android.graphics.Bitmap;
import android.util.Log;

public class VideoClient extends Client implements IVideoClient {

	public VideoClient(Connection connection) {
		super(connection);
		// TODO Auto-generated constructor stub
	}

	private ArrayList<Movie> parseMovies(JsonNode result, boolean hideWatched) {

		ArrayList<Movie> movies = new ArrayList<Movie>();
		final JsonNode jsonMovies = result.get("movies");
		if (jsonMovies == null) {
			return movies;
		}

		for (Iterator<JsonNode> i = jsonMovies.getElements(); i.hasNext();) {
			JsonNode jsonMovie = (JsonNode) i.next();
			int playcount = getInt(jsonMovie, "playcount");
			if (playcount > 0 && hideWatched) {
				continue;
			}
			String fullpath = getString(jsonMovie, "file").replace("\\", "/");
			String localPath = fullpath.substring(0, fullpath.lastIndexOf('/'));
			String filename = fullpath.replace(localPath, "");
			
			// FIXME: Ugly backwards compatability hack
			String genre = getString(jsonMovie, "genre");
			if(jsonMovie.get("genre") != null && jsonMovie.get("genre") instanceof ArrayNode) {
				ArrayNode genreArrayNode = (ArrayNode) jsonMovie.get("genre");
				
				genre = "";
				for(int j = 0; j < genreArrayNode.size(); j++) {
					genre += genreArrayNode.get(j).getTextValue();
					if(j < genreArrayNode.size() - 1) {
						genre += " / ";
					}
				}
			}
			
			movies.add(new Movie(getInt(jsonMovie, "movieid"), getString(
					jsonMovie, "label"), getInt(jsonMovie, "year"), localPath,
					filename, getString(jsonMovie, "director"), getString(
							jsonMovie, "runtime"),
					genre, getDouble(jsonMovie,
							"rating"), playcount, getString(jsonMovie,
							"imdbnumber")));
		}

		return movies;
	}

	public ArrayList<Movie> getMovies(INotifiableManager manager, Sort sort,
			boolean hideWatched) {

//		return parseMovies(
//				mConnection.getJson(
//						manager,
//						"VideoLibrary.GetMovies",
//						sort(obj().p(
//								"properties",
//								arr().add("playcount").add("year").add("file")
//										.add("director").add("runtime")
//										.add("genre").add("rating")
//										.add("imdbnumber")), sort)),
//				hideWatched);
		return null;
	}

	public ArrayList<Movie> getMovies(INotifiableManager manager, Actor actor,
			Sort sort, boolean hideWatched) {
		return new ArrayList<Movie>();
	}

	public ArrayList<Movie> getMovies(INotifiableManager manager, Genre genre,
			Sort sort, boolean hideWatched) {
		// unfortunately no ability to query by genres currently
//		ArrayList<Movie> movies = parseMovies(
//				mConnection.getJson(
//						manager,
//						"VideoLibrary.GetMovies",
//						sort(obj().p(
//								"properties",
//								arr().add("playcount").add("year").add("file")
//										.add("director").add("runtime")
//										.add("genre").add("rating")
//										.add("imdbnumber")), sort)),
//				hideWatched);
//		for (Iterator<Movie> i = movies.iterator(); i.hasNext();) {
//			Movie movie = i.next();
//			if (!movie.isGenre(genre)) {
//				i.remove();
//			}
//		}
//		return movies;
		return null;
	}

	public Movie updateMovieDetails(INotifiableManager manager, Movie movie) {

//		ObjNode obj = obj().p("movieid", movie.getId()).p(
//				"properties",
//				arr().add("tagline").add("plot").add("votes").add("studio")
//						.add("mpaa").add("trailer"));
//		JsonNode result = mConnection.getJson(manager,
//				"VideoLibrary.GetMovieDetails", obj);
//		JsonNode movieDetails = result.get("moviedetails");
//		if (movieDetails == null) {
//			return movie;
//		}
//		Log.e("VideoClient", result.toString());
//		movie.tagline = getString(movieDetails, "tagline");
//		movie.plot = getString(movieDetails, "plot");
//		movie.numVotes = getInt(movieDetails, "votes");
//		
//		JsonNode node = movieDetails.get("studio");
//		if(node instanceof ArrayNode) {
//			ArrayNode arrNode = (ArrayNode) node;
//			if(arrNode.size() > 0) {
//				movie.studio = arrNode.get(0).getTextValue();
//			} else {
//				movie.studio = "";
//			}
//		} else {
//			movie.studio = getString(movieDetails, "studio");
//		}
//		movie.rated = getString(movieDetails, "mpaa");
//		movie.trailerUrl = getString(movieDetails, "trailer");
//		return movie;
		return null;
	}

	public ArrayList<Actor> getActors(INotifiableManager manager) {
		// TODO, we can't lookup actors currently
		return new ArrayList<Actor>();
	}

	public ArrayList<Actor> getMovieActors(INotifiableManager manager) {
		// TODO, we can't lookup actors currently
		return new ArrayList<Actor>();
	}

	public ArrayList<Actor> getTvShowActors(INotifiableManager manager) {
		// TODO, we can't lookup actors currently
		return new ArrayList<Actor>();
	}

	public ArrayList<Genre> getMovieGenres(INotifiableManager manager, Sort sort) {

		JsonNode result = mConnection.getJson(manager,
				"VideoLibrary.GetGenres", sort(obj().p("type", "movie"), sort));

		return parseGenres(result);

	}

	public ArrayList<Genre> getTvShowGenres(INotifiableManager manager) {
		// we can't lookup genres currently
		return new ArrayList<Genre>();
	}

	public Bitmap getCover(INotifiableManager manager, ICoverArt cover, int size) {
		return getCover(manager, cover, size, Movie.getThumbUri(cover),
				Movie.getFallbackThumbUri(cover));
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

	public boolean removeFromPlaylist(INotifiableManager manager, int position) {
		return false;
	}

	static ICurrentlyPlaying getCurrentlyPlaying(final Integer currentPlayer,
			final JsonNode item, final JsonNode props) {

		return new ICurrentlyPlaying() {
			private static final long serialVersionUID = 5036994329211476714L;

			public String getTitle() {
				return item.get("label").getTextValue();
			}

			public int getTime() {
				return parseTime(props.get("time").getTextValue());
			}

			public int getPlayStatus() {
				return PlayStatus.parse(currentPlayer, props.get("speed")
						.getIntValue());
			}

			public int getPlaylistPosition() {
				return props.get("position").getIntValue();
			}

			public float getPercentage() {
				return props.get("percentage").getIntValue();
			}

			public String getFilename() {
				return item.get("file").getTextValue();
			}

			public int getDuration() {
				if (item.get("duration") == null) {
					return 0;
				}
				return parseTime(item.get("duration").getTextValue());
			}

			public String getArtist() {
				if (item.get("genre") == null) {
					return "";
				}
				return item.get("genre").getTextValue();
			}

			public String getAlbum() {
				String title = item.get("tagline").getTextValue();
				if (title != null)
					return title;
				String path = item.get("file").getTextValue()
						.replaceAll("\\\\", "/");
				return path.substring(0, path.lastIndexOf("/"));
			}

			public int getMediaType() {
				return MediaType.VIDEO;
			}

			public boolean isPlaying() {
				return props.get("speed").getIntValue() == PlayStatus.PLAYING;
			}

			public int getHeight() {
				return 0;
			}

			public int getWidth() {
				return 0;
			}

			private int parseTime(String time) {
				String[] s = time.split(":");
				if (s.length == 2) {
					return Integer.parseInt(s[0]) * 60 + Integer.parseInt(s[1]);
				} else if (s.length == 3) {
					return Integer.parseInt(s[0]) * 3600
							+ Integer.parseInt(s[1]) * 60
							+ Integer.parseInt(s[2]);
				} else {
					return 0;
				}
			}
			public String getThumbnail() {
				return props.get("thumbnail").getTextValue();
			}
		};
	}
}

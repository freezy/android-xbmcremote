package org.xbmc.jsonrpc.client;

import java.util.ArrayList;
import java.util.Iterator;

import org.codehaus.jackson.JsonNode;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IControlClient.ICurrentlyPlaying;
import org.xbmc.api.data.IVideoClient;
import org.xbmc.api.info.PlayStatus;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.Host;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.object.Movie;
import org.xbmc.api.type.MediaType;
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
	
	private ArrayList<Movie> parseMovies(JsonNode result, boolean hideWatched) {
		
		ArrayList<Movie> movies = new ArrayList<Movie>();
		final JsonNode jsonMovies = result.get("movies");
		if(jsonMovies == null) {
			return movies;
		}
		
		for (Iterator<JsonNode> i = jsonMovies.getElements(); i.hasNext();) {
			JsonNode jsonMovie = (JsonNode)i.next();
			int playcount = getInt(jsonMovie, "playcount");
			if(playcount > 0 && hideWatched) {
				continue;
			}
			movies.add(new Movie(
					getInt(jsonMovie, "movieid"),
					getString(jsonMovie, "title"),
					getInt(jsonMovie, "year"),
					getString(jsonMovie, "file"),
					getString(jsonMovie, "file"),
					getString(jsonMovie, "director"),
					getString(jsonMovie, "runtime"),
					getString(jsonMovie, "genre"),
					getDouble(jsonMovie, "rating"),
					playcount,
					getString(jsonMovie, "imdbnumber")
					));			
		}
		
		return movies;
	}
		

	public ArrayList<Movie> getMovies(INotifiableManager manager, int sortBy,
			String sortOrder, boolean hideWatched) {
		
		return parseMovies(mConnection.getJson(manager, "VideoLibrary.GetMovies", sort(obj().p("properties", 
				arr().add("playcount").add("year").add("file").add("director").add("runtime").add("genre").add("rating")
				.add("imdbnumber")), sortBy, sortOrder, true)), hideWatched);
	}

	public ArrayList<Movie> getMovies(INotifiableManager manager, int sortBy,
			String sortOrder, int offset, boolean hideWatched) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Movie> getMovies(INotifiableManager manager, Actor actor,
			int sortBy, String sortOrder, boolean hideWatched) {
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
	
	static ICurrentlyPlaying getCurrentlyPlaying(final Integer currentPlayer, final JsonNode item, final JsonNode props) {
		
		return new ICurrentlyPlaying() {
			private static final long serialVersionUID = 5036994329211476714L;
			public String getTitle() {
				return item.get("label").getTextValue();
			}
			public int getTime() {
				return parseTime(props.get("time").getTextValue());
			}
			public int getPlayStatus() {
				return PlayStatus.parse(currentPlayer, props.get("speed").getIntValue());
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
				return parseTime(item.get("duration").getTextValue());
			}
			public String getArtist() {
				return item.get("genre").getTextValue();
			}
			public String getAlbum() {
				String title = item.get("tagline").getTextValue();
				if (title != null)
					return title;
				String path = item.get("file").getTextValue().replaceAll("\\\\", "/");
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
					return Integer.parseInt(s[0]) * 3600 + Integer.parseInt(s[1]) * 60 + Integer.parseInt(s[2]);
				} else {
					return 0;
				}
			}
		};
	}	

}

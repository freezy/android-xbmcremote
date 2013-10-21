/*
 *      Copyright (C) 2005-2009 Team XBMC
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
import org.xbmc.api.data.IControlClient;
import org.xbmc.api.data.IMusicClient;
import org.xbmc.api.data.IControlClient.ICurrentlyPlaying;
import org.xbmc.api.info.PlayStatus;
import org.xbmc.api.object.Album;
import org.xbmc.api.object.Artist;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.Host;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.object.Song;
import org.xbmc.api.type.MediaType;
import org.xbmc.api.type.SortType;
import org.xbmc.jsonrpc.Connection;

import android.graphics.Bitmap;

/**
 * Takes care of every music related stuff, notably the music database.
 * 
 * @author Team XBMC
 */
public class MusicClient extends Client implements IMusicClient {
	
	public static final String TAG = "MusicClient";
	
	public static final int VIEW_ALBUMS = 1;
	public static final int VIEW_SONGS = 2;
	
	public static final int PLAYLIST_ID = 0;
	public static final String LIBRARY_TYPE = "songs";
	
	public static final int PLAYLIST_LIMIT = 100;
	
	/**
	 * Class constructor needs reference to HTTP client connection
	 * @param connection
	 */
	public MusicClient(Connection connection) {
		super(connection);
	}
	
	/**
	 * Updates host info on the connection.
	 * @param host
	 */
	public void setHost(Host host) {
		mConnection.setHost(host);
	}

	
	/**
	 * Adds an album to the current playlist.
	 * @param album Album
	 * @return True on success, false otherwise.
	 */
	public boolean addToPlaylist(INotifiableManager manager, Album album, int sortBy, String sortOrder) {
		return mConnection.getString(manager, "Playlist.Add", obj().p("playlistid", PLAYLIST_ID).p("item", obj().p("albumid", album.id))).equals("OK");
	}

	/**
	 * Adds all songs from an artist to the current playlist.
	 * @param artist Artist
	 * @return True on success, false otherwise.
	 */
	public boolean addToPlaylist(INotifiableManager manager, Artist artist, int sortBy, String sortOrder) {
		return mConnection.getString(manager, "Playlist.Add", obj().p("playlistid", PLAYLIST_ID).p("item", obj().p("artist", artist.id))).equals("OK");
	}

	/**
	 * Adds all songs from a genre to the current playlist.
	 * @param genre Genre
	 * @return True on success, false otherwise.
	 */
	public boolean addToPlaylist(INotifiableManager manager, Genre genre, int sortBy, String sortOrder) {
		return mConnection.getString(manager, "Playlist.Add", obj().p("playlistid", PLAYLIST_ID).p("item", obj().p("genreid", genre.id))).equals("OK");
	}

	/**
	 * Adds songs of a genre from an artist to the current playlist.
	 * @param artist Artist
	 * @param genre Genre
	 * @return True on success, false otherwise.
	 */
	public boolean addToPlaylist(INotifiableManager manager, Artist artist, Genre genre, int sortBy, String sortOrder) {
		return mConnection.getString(manager, "Playlist.Add", obj().p("playlistid", PLAYLIST_ID).p("item", obj().p("genreid", genre.id).p("artistid", artist.id))).equals("OK");
	}
	
	/**
	 * Adds a song to the current playlist.
	 * @param song Song to add
	 * @return True on success, false otherwise.
	 */
	public boolean addToPlaylist(INotifiableManager manager, Song song) {
		return mConnection.getString(manager, "Playlist.Add", obj().p("playlistid", PLAYLIST_ID).p("item", obj().p("songid", song.id))).equals("OK");
	}
	
	/**
	 * Returns how many items are in the playlist.
	 * @return Number of items in the playlist
	 */
	public int getPlaylistSize(INotifiableManager manager) {
		return mConnection.getInt(manager, "Playlist.GetProperties", obj().p("playlistid", PLAYLIST_ID).p(PARAM_PROPERTIES, arr().add("size")), "size");
	}
	
	/**
	 * Retrieves the currently playing song number in the playlist.
	 * @return Number of items in the playlist
	 */
	public int getPlaylistPosition(INotifiableManager manager) {		
		return mConnection.getInt(manager, "Player.GetItem", obj().p("playerid", getActivePlayerId(manager)).p(PARAM_PROPERTIES, arr().add("position")), "position");
	}
	
	/**
	 * Sets the media at playlist position position to be the next item to be played.
	 * @param position New position, starting with 0.
	 * @return True on success, false otherwise.
	 */
	public boolean setPlaylistPosition(INotifiableManager manager, int position) {
		int playerid = getActivePlayerId(manager);
		if(playerid == -1)
			return mConnection.getString(manager, "Player.Open", obj().p("item", obj().p("playlistid", PLAYLIST_ID).p("position", position))).equals("OK");
		else
			return mConnection.getString(manager, "Player.GoTo", obj().p("playerid", getActivePlayerId(manager)).p("position", position)).equals("OK");
	}
	
	/**
	 * Removes media from the current playlist. It is not possible to remove the media if it is currently being played.
	 * @param position Position to remove, starting with 0.
	 * @return True on success, false otherwise.
	 */
	public boolean removeFromPlaylist(INotifiableManager manager, int position) {
		return mConnection.getString(manager, "Playlist.Remove", obj().p("playlistid", PLAYLIST_ID).p("position", position)).equals("OK");
	}
	
	/**
	 * Removes media from the current playlist. It is not possible to remove the media if it is currently being played.
	 * @param position Complete path (including filename) of the media to be removed.
	 * @return True on success, false otherwise.
	 */
	public boolean removeFromPlaylist(INotifiableManager manager, String path) {
		
		JsonNode playlistitems = mConnection.getJson(manager, "Playlist.GetItems", obj().p("playlistid", PLAYLIST_ID).p(PARAM_PROPERTIES, arr().add("position").add("file")));
		for (Iterator<JsonNode> i = playlistitems.getElements(); i.hasNext();) {
			JsonNode jsonItem = (JsonNode)i.next();
			if(getString(jsonItem,"file").toLowerCase().equals(path.toLowerCase()))
				return mConnection.getString(manager, "Playlist.Remove", obj().p("playlistid", PLAYLIST_ID).p("position", getInt(jsonItem,"position"))).equals("OK");
		}
		
		return false;
	}
	
	/**
	 * Returns the first {@link PLAYLIST_LIMIT} songs of the playlist. 
	 * @return Songs in the playlist.
	 */
	public ArrayList<String> getPlaylist(INotifiableManager manager) {
		JsonNode jsonItems = mConnection.getJson(manager, "PlayList.GetItems", obj().p("playlistid", PLAYLIST_ID).p("limits", obj().p("start", 0).p("end", PLAYLIST_LIMIT)).p("properties", arr().add("file")));
		final JsonNode jsonSongs = jsonItems.get("items");
		final ArrayList<String> files = new ArrayList<String>();
		if (jsonSongs != null) {
			for (Iterator<JsonNode> i = jsonSongs.getElements(); i.hasNext();) {
				JsonNode jsonSong = (JsonNode)i.next();
				files.add(getString(jsonSong, "file"));
			}
		}
		return files;
	}
	
	/**
	 * Clears current playlist
	 * @return True on success, false otherwise.
	 */
	public boolean clearPlaylist(INotifiableManager manager) {
		return mConnection.getString(manager, "Playlist.Clear", obj().p("playlistid", PLAYLIST_ID)).equals("OK");
	}
	
	/**
	 * Adds a song to the current playlist and plays it.
	 * @param song Song
	 * @return True on success, false otherwise.
	 */
	public boolean play(INotifiableManager manager, Song song) {
		
		int size = mConnection.getInt(manager, "Playlist.GetProperties", obj().p("playlistid", PLAYLIST_ID), "size");
		
		if(addToPlaylist(manager, song))
			return setPlaylistPosition(manager, size);
		else
			return false;
	}
	
	public boolean play(INotifiableManager manager){
		return mConnection.getString(manager, "Player.Open", obj().p("item", obj().p("playlistid", PLAYLIST_ID).p("position", 0))).equals("OK");
	}
	
	/**
	 * Plays an album. Playlist is previously cleared.
	 * @param album Album to play
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return True on success, false otherwise.
	 */
	public boolean play(INotifiableManager manager, Album album, int sortBy, String sortOrder) {
		if(clearPlaylist(manager))
			if(addToPlaylist(manager, album, sortBy, sortOrder))
				return play(manager);
			else
				return false;
		else
			return false;		
	}
	
	/**
	 * Plays all songs of a genre. Playlist is previously cleared.
	 * @param genre Genre
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return True on success, false otherwise.
	 */
	public boolean play(INotifiableManager manager, Genre genre, int sortBy, String sortOrder) {
		if(clearPlaylist(manager))
			if(addToPlaylist(manager, genre, sortBy, sortOrder))
				return play(manager);
			else
				return false;
		else
			return false;		
	}
	
	/**
	 * Plays all songs from an artist. Playlist is previously cleared.
	 * @param artist Artist
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return True on success, false otherwise.
	 */
	public boolean play(INotifiableManager manager, Artist artist, int sortBy, String sortOrder) {
		if(clearPlaylist(manager))
			if(addToPlaylist(manager, artist, sortBy, sortOrder))
				return play(manager);
			else
				return false;
		else
			return false;		
	}
	
	/**
	 * Plays songs of a genre from an artist. Playlist is previously cleared.
	 * @param artist Artist
	 * @param genre Genre
	 * @return True on success, false otherwise.
	 */
	public boolean play(INotifiableManager manager, Artist artist, Genre genre) {
		if(clearPlaylist(manager))
			if(addToPlaylist(manager, artist, genre, SortType.TITLE, "descending"))
				return play(manager);
			else
				return false;
		else
			return false;		
	}

	/**
	 * Starts playing/showing the next media/image in the current playlist
	 * or, if currently showing a slidshow, the slideshow playlist.
	 * @return True on success, false otherwise.
	 */
	public boolean playNext(INotifiableManager manager) {
		return mConnection.getString(manager, "Player.GoNext", obj().p("playerid", getActivePlayerId(manager))).equals("OK");
	}

	/**
	 * Starts playing/showing the previous media/image in the current playlist
	 * or, if currently showing a slidshow, the slideshow playlist.
	 * @return True on success, false otherwise.
	 */
	public boolean playPrev(INotifiableManager manager) {
		return mConnection.getString(manager, "Player.GoPrevious", obj().p("playerid", getActivePlayerId(manager))).equals("OK");
	}
	
	/**
	 * Sets the media at playlist position position to be the next item to be 
	 * played. Position starts at 0, so SetPlaylistSong(5) sets the position
	 * to the 6th song in the playlist.
	 * @param pos Position
	 * @return true on success, false otherwise.
	 */
	public boolean playlistSetSong(INotifiableManager manager, int pos) {
		return setPlaylistPosition(manager, pos);
	}
	
	/**
	 * Sets current playlist to "0"
	 * @return True on success, false otherwise.
	 */
	public boolean setCurrentPlaylist(INotifiableManager manager) {
		return mConnection.getString(manager, "Player.Open", obj().p("item", obj().p("playlistid", PLAYLIST_ID))).equals("OK");
	}
	
	
	/**
	 * Gets all albums from database
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All albums
	 */
	public ArrayList<Album> getAlbums(INotifiableManager manager, int SortBy, String sortOrder){
		return getAlbums(manager, obj(), SortBy, sortOrder);
	}
	
	private ArrayList<Album> getAlbums(INotifiableManager manager, ObjNode obj, int sortBy, String sortOrder) {
		
		obj = sort(obj.p(PARAM_PROPERTIES, arr().add("artist").add("year").add("thumbnail")), sortBy, sortOrder);
		
		final ArrayList<Album> albums = new ArrayList<Album>();
		final JsonNode result = mConnection.getJson(manager, "AudioLibrary.GetAlbums", obj);
		final JsonNode jsonAlbums = result.get("albums");
		if(jsonAlbums != null){
			for (Iterator<JsonNode> i = jsonAlbums.getElements(); i.hasNext();) {
				JsonNode jsonAlbum = (JsonNode)i.next();
				albums.add(new Album(
					getInt(jsonAlbum, "albumid"), 
					getString(jsonAlbum, "label"), 
					getString(jsonAlbum, "artist"), 
					getInt(jsonAlbum, "year"), 
					getString(jsonAlbum, "thumbnail", "") 
				));
			}
		}
		return albums;
	}
	
	/**
	 * Gets all albums with given artist IDs
	 * @param artistIDs Array of artist IDs
	 * @return All compilation albums
	 */
	public ArrayList<Album> getAlbums(INotifiableManager manager, ArrayList<Integer> artistIDs) {
		
		final ArrayList<Album> albums = new ArrayList<Album>();
		for(int id : artistIDs){
			albums.addAll(getAlbums(manager, obj().p("filter", obj().p("artistid", id)), SortType.TITLE, "descending"));
		}
		
		return albums;
	}


	/**
	 * Gets all albums of an artist from database
	 * @param artist Artist
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return Albums with an artist
	 */
	public ArrayList<Album> getAlbums(INotifiableManager manager, Artist artist, int sortBy, String sortOrder) {
		return getAlbums(manager, obj().p("filter", obj().p("artistid", artist.id)), sortBy, sortOrder);
	}

	/**
	 * Gets all albums of with at least one song in a genre
	 * @param genre Genre
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return Albums of a genre
	 */
	public ArrayList<Album> getAlbums(INotifiableManager manager, Genre genre, int sortBy, String sortOrder) {
		return getAlbums(manager, obj().p("filter", obj().p("genreid", genre.id)), sortBy, sortOrder);
	}
	
	/**
	 * Gets all artists from database
	 * @param albumArtistsOnly If set to true, hide artists who appear only on compilations.
	 * @return All albums
	 */
	public ArrayList<Artist> getArtists(INotifiableManager manager, ObjNode obj, boolean albumArtistsOnly) {
		
		obj.p(PARAM_PROPERTIES, arr().add("thumbnail")).p("albumartistsonly", albumArtistsOnly);
		obj = sort(obj, SortType.ARTIST, "descending");
		final ArrayList<Artist> artists = new ArrayList<Artist>();
		final JsonNode result = mConnection.getJson(manager, "AudioLibrary.GetArtists", obj);
		if(result != null){
			final JsonNode jsonArtists = result.get("artists");
			for (Iterator<JsonNode> i = jsonArtists.getElements(); i.hasNext();) {
				JsonNode jsonArtist = (JsonNode)i.next();
				artists.add(new Artist(
					getInt(jsonArtist, "artistid"), 
					getString(jsonArtist, "label"), 
					getString(jsonArtist, "thumbnail", "") 
				));
			}
		}
		return artists;
	}
	
	public ArrayList<Artist> getArtists(INotifiableManager manager, boolean albumArtistsOnly) {
		return getArtists(manager, obj(), albumArtistsOnly);
	}

	/**
	 * Gets all artists with at least one song of a genre.
	 * @param genre Genre
	 * @param albumArtistsOnly If set to true, hide artists who appear only on compilations.
	 * @return Albums with a genre
	 */
	public ArrayList<Artist> getArtists(INotifiableManager manager, Genre genre, boolean albumArtistsOnly) {
		return getArtists(manager, obj().p("filter", obj().p("genreid", genre.id)), albumArtistsOnly);
	}
	
	/**
	 * Gets all genres from database
	 * @return All genres
	 */
	public ArrayList<Genre> getGenres(INotifiableManager manager) {
		
		
		final ArrayList<Genre> genres = new ArrayList<Genre>();
		final JsonNode result = mConnection.getJson(manager, "AudioLibrary.GetGenres", sort(obj(), SortType.TITLE, "descending"));
		final JsonNode jsonGenres = result.get("genres");
		for (Iterator<JsonNode> i = jsonGenres.getElements(); i.hasNext();) {
			JsonNode jsonGenre = (JsonNode)i.next();
			genres.add(new Genre(
				getInt(jsonGenre, "genreid"), 
				getString(jsonGenre, "label") 
			));
		}
		return genres;
	}
	
	/**
	 * Updates the album object with additional data from the albuminfo table
	 * @param album
	 * @return Updated album
	 */
	public Album updateAlbumInfo(INotifiableManager manager, Album album) {
		
		final JsonNode result = mConnection.getJson(manager, "AudioLibrary.GetAlbumDetails", obj().p("albumid", album.id).p("properties", arr().add("genre").add("albumlabel").add("rating")));
		final JsonNode jsonAlbum = result.get("albumdetails");
		album.genres = getString(jsonAlbum, "genre");
		album.label = getString(jsonAlbum, "albumlabel");
		album.rating = getInt(jsonAlbum, "rating");
		return album;
	}
	
	/**
	 * Updates the artist object with additional data from the artistinfo table
	 * @param artist
	 * @return Updated artist
	 */
	public Artist updateArtistInfo(INotifiableManager manager, Artist artist) {
		
		final JsonNode result = mConnection.getJson(manager, "AudioLibrary.GetArtistDetails", obj().p("artistid", artist.id).p("properties", arr().add("born").add("formed").add("mood").add("style").add("description")));
		final JsonNode jsonArtist = result.get("artistdetails");
		artist.born = getString(jsonArtist, "born");
		artist.formed = getString(jsonArtist, "formed");
		artist.moods = getString(jsonArtist, "mood");
		artist.styles = getString(jsonArtist, "style");
		artist.biography = getString(jsonArtist, "description");
		
		return artist;
	}
	
	/**
	 * Returns a list containing tracks of a certain condition.
	 * @param sqlCondition SQL condition which tracks to return
	 * @return Found tracks
	 **/
	private ArrayList<Song> getSongs(INotifiableManager manager, ObjNode obj) {
		final ArrayList<Song> songs = new ArrayList<Song>();
		final JsonNode result = mConnection.getJson(manager, "AudioLibrary.GetSongs", obj);
		final JsonNode jsonAlbums = result.get("songs");
		for (Iterator<JsonNode> i = jsonAlbums.getElements(); i.hasNext();) {
			JsonNode jsonSong = (JsonNode)i.next();
			songs.add(new Song(
				getInt(jsonSong, "songid"), 
				getString(jsonSong, "label"), 
				getString(jsonSong, "artist"), 
				getString(jsonSong, "album"), 
				getInt(jsonSong, "track"),
				getInt(jsonSong, "duration"),
				getString(jsonSong, ""),
				getString(jsonSong, "file"),
				getString(jsonSong, "thumbnail", "") 
			));
		}
		return songs;
	}
	
	private ArrayList<Song> getSongs(INotifiableManager manager, ObjNode obj, int sortBy, String sortOrder) {
		return getSongs(manager, sort(obj.p(PARAM_PROPERTIES, arr().add("artist").add("album").add("track").add("duration").add("file").add("thumbnail")), sortBy, sortOrder));
	}
	

	/**
	 * Returns a list containing all tracks of an album. The list is sorted by filename.
	 * @param album Album
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.	 
	 * @return All tracks of an album
	 */
	public ArrayList<Song> getSongs(INotifiableManager manager, Album album, int sortBy, String sortOrder) {
		return getSongs(manager, obj().p("filter", obj().p("albumid", album.id)), sortBy, sortOrder);
	}

	/**
	 * Returns a list containing all tracks of an artist. The list is sorted by album name, filename.
	 * @param artist Artist
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All tracks of the artist
	 */
	public ArrayList<Song> getSongs(INotifiableManager manager, Artist artist, int sortBy, String sortOrder) {
		return getSongs(manager, obj().p("filter", obj().p("artistid", artist.id)), sortBy, sortOrder);
	}
	
	/**
	 * Returns a list containing all tracks of a genre. The list is sorted by artist, album name, filename.
	 * @param genre Genre
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All tracks of the genre
	 */
	public ArrayList<Song> getSongs(INotifiableManager manager, Genre genre, int sortBy, String sortOrder) {
		return getSongs(manager, obj().p("genreid", genre.id), sortBy, sortOrder);
	}
	
	/**
	 * Returns a list containing all tracks of a genre AND and artist. The list is sorted by 
	 * artist, album name, filename.
	 * @param genre Genre
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All tracks of the genre
	 */
	public ArrayList<Song> getSongs(INotifiableManager manager, Artist artist, Genre genre, int sortBy, String sortOrder) {
		return getSongs(manager, obj().p("filter", obj().p("artistid", artist.id).p("genreid", genre.id)), sortBy, sortOrder);
	}
	
	/**
	 * Returns a pre-resized album cover. Pre-resizing is done in a way that
	 * the bitmap at least as large as the specified size but not larger than
	 * the double.
	 * @param manager Postback manager
	 * @param cover Cover object
	 * @param size Minmal size to pre-resize to.
	 * @return Thumbnail bitmap
	 */
	public Bitmap getCover(INotifiableManager manager, ICoverArt cover, int size) {
		String url = null;
		if(Album.getThumbUri(cover) != ""){
			final JsonNode dl = mConnection.getJson(manager, "Files.PrepareDownload", obj().p("path", Album.getThumbUri(cover)));
			if(dl != null){
				JsonNode details = dl.get("details");
				if(details != null)
					url = mConnection.getUrl(getString(details, "path"));
			}
		}
		return getCover(manager, cover, size, url);
	}
	
	/**
	 * Returns a list containing all artist IDs that stand for "compilation".
	 * Best case scenario would be only one ID for "Various Artists", though
	 * there are also just "V.A." or "VA" naming conventions.
	 * @return List of compilation artist IDs
	 */
	public ArrayList<Integer> getCompilationArtistIDs(INotifiableManager manager) {
		
		ArrayList<Artist> artists = getArtists(manager, sort(obj(), SortType.ARTIST, "ascending"), true);
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for(Artist artist : artists){
			if(artist.name.toLowerCase().equals("various artists") || artist.name.toLowerCase().equals("v.a.") || artist.name.toLowerCase().equals("va"))
				ids.add(artist.id);
		}
		
		return ids;
		
	}
	
			
	static ICurrentlyPlaying getCurrentlyPlaying(final JsonNode player, final JsonNode item) {
		return new IControlClient.ICurrentlyPlaying() {
			private static final long serialVersionUID = 5036994329211476714L;
			public String getTitle() {
				return getString(item, "title");
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
				return getInt(item, "duration");
			}
			public String getArtist() {
				return getString(item, "artist");
			}
			public String getAlbum() {
				return getString(item, "album");
			}
			public int getMediaType() {
				return MediaType.MUSIC;
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
}
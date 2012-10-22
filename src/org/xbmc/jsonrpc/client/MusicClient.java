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
import org.xbmc.android.remote.business.MusicManager;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IControlClient;
import org.xbmc.api.data.IControlClient.ICurrentlyPlaying;
import org.xbmc.api.data.IMusicClient;
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
import android.util.Log;

/**
 * Takes care of every music related stuff, notably the music database.
 * 
 * @author Team XBMC
 */
public class MusicClient extends Client implements IMusicClient {
	
	public static final String TAG = "MusicClient";
	
	public static final int VIEW_ALBUMS = 1;
	public static final int VIEW_SONGS = 2;
	
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
		
		return mConnection.getBoolean(manager, "Playlist.Add", obj().p("playlistid", PLAYLIST_MUSIC).p("item", obj().p("albumid", album.getId())));
	}

	/**
	 * Adds all songs from an artist to the current playlist.
	 * @param artist Artist
	 * @return True on success, false otherwise.
	 */
	public boolean addToPlaylist(INotifiableManager manager, Artist artist, int sortBy, String sortOrder) {
		return mConnection.getBoolean(manager, "Playlist.Add", obj().p("playlistid", PLAYLIST_MUSIC).p("item", obj().p("artistid", artist.getId())));
	}

	/**
	 * Adds all songs from a genre to the current playlist.
	 * @param genre Genre
	 * @return True on success, false otherwise.
	 */
	public boolean addToPlaylist(INotifiableManager manager, Genre genre, int sortBy, String sortOrder) {
		return mConnection.getBoolean(manager, "Playlist.Add", obj().p("playlistid", PLAYLIST_MUSIC).p("item", obj().p("genreid", genre.getId())));
	}

	/**
	 * Adds songs of a genre from an artist to the current playlist.
	 * @param artist Artist
	 * @param genre Genre
	 * @return True on success, false otherwise.
	 */
	public boolean addToPlaylist(INotifiableManager manager, Artist artist, Genre genre, int sortBy, String sortOrder) {
		return mConnection.getBoolean(manager, "Playlist.Add", obj().p("playlistid", PLAYLIST_MUSIC).p("item", obj().p("artistid", artist.getId()).p("genreid", genre.getId())));
	}
	
	/**
	 * Adds a song to the current playlist.
	 * @param song Song to add
	 * @return True on success, false otherwise.
	 */
	public boolean addToPlaylist(INotifiableManager manager, Song song) {
		return mConnection.getBoolean(manager, "Playlist.Add", obj().p("playlistid", PLAYLIST_MUSIC).p("item", obj().p("songid", song.getId())));
	}
	
	/**
	 * Returns how many items are in the playlist.
	 * @return Number of items in the playlist
	 */
	public int getPlaylistSize(INotifiableManager manager) {
		JsonNode result = mConnection.getJson(manager, "Playlist.GetItems", obj().p("playlistid", PLAYLIST_MUSIC));
		JsonNode items = result.get("items");
		if(items == null) {
			return 0;
		}
		return items.size();
	}
	
	/**
	 * Retrieves the currently playing song number in the playlist.
	 * @return Number of items in the playlist
	 */
	public int getPlaylistPosition(INotifiableManager manager) {
		Integer player = getActivePlayerId(manager, MediaType.MUSIC);
		if(player == null) {
			return -1;
		}
		JsonNode result = mConnection.getJson(manager, "Player.GetProperties", obj().p("playerid", player).p("properties", arr().add("position")));
		return result.get("position").getIntValue();
	}
	
	/**
	 * Sets the media at playlist position position to be the next item to be played.
	 * @param position New position, starting with 0.
	 * @return True on success, false otherwise.
	 */
	public boolean setPlaylistPosition(INotifiableManager manager, int position) {
		Integer player = getActivePlayerId(manager, MediaType.MUSIC); 
		if(player == null) {
			return open(manager, PLAYLIST_MUSIC, position);
		}
		return mConnection.getBoolean(manager, "Player.GoTo", obj().p("position", position).p("playerid", player));
	}
	
	public boolean open(INotifiableManager manager, int player) {
		return open(manager, player, 0);
	}
	
	public boolean open(INotifiableManager manager, int player, int position) {
		
		return mConnection.getBoolean(manager, "Player.Open", obj().p("item", obj().p("playlistid", player).p("position", position)));
	}
	
	/**
	 * Removes media from the current playlist. It is not possible to remove the media if it is currently being played.
	 * @param position Position to remove, starting with 0.
	 * @return True on success, false otherwise.
	 */
	public boolean removeFromPlaylist(INotifiableManager manager, int position) {
		return false; //mConnection.getBoolean(manager, "RemoveFromPlaylist", PLAYLIST_ID + ";" + position);
	}
	
	/**
	 * Removes media from the current playlist. It is not possible to remove the media if it is currently being played.
	 * @param position Complete path (including filename) of the media to be removed.
	 * @return True on success, false otherwise.
	 */
	public boolean removeFromPlaylist(INotifiableManager manager, String path) {
		return false; //mConnection.getBoolean(manager, "RemoveFromPlaylist", PLAYLIST_ID + ";" + path);
	}
	
	/**
	 * Returns the first {@link PLAYLIST_LIMIT} songs of the playlist. 
	 * @return Songs in the playlist.
	 */
	public ArrayList<String> getPlaylist(INotifiableManager manager) {
		ArrayList<String> playlistItems = new ArrayList<String>();
		JsonNode result = mConnection.getJson(manager, "Playlist.GetItems", obj().p("playlistid", PLAYLIST_MUSIC));
		JsonNode items = result.get("items");
		if(items == null || items.size() == 0) {
			playlistItems.add(MusicManager.EMPTY_PLAYLIST_ITEM);
			return playlistItems;
		}
		for (Iterator<JsonNode> i = items.getElements(); i.hasNext();) {
			JsonNode item = (JsonNode)i.next();
			playlistItems.add(item.get("label").getTextValue());
		}
		return playlistItems;
	}
	
	/**
	 * Clears current playlist
	 * @return True on success, false otherwise.
	 */
	public boolean clearPlaylist(INotifiableManager manager) {
		return mConnection.getBoolean(manager, "Playlist.Clear", obj().p("playlistid", PLAYLIST_MUSIC));
	}
	
	/**
	 * Adds a song to the current playlist and plays it.
	 * @param song Song
	 * @return True on success, false otherwise.
	 */
	public boolean play(INotifiableManager manager, Song song) {
		return play(manager, obj().p("item", getSongsCondition(song)));
	}
	
	/**
	 * Plays an album. Playlist is previously cleared.
	 * @param album Album to play
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return True on success, false otherwise.
	 */
	public boolean play(INotifiableManager manager, Album album, int sortBy, String sortOrder) {
		// ignoring sort as not applicable in jsonrpc API
		return play(manager, obj().p("item", getSongsCondition(album)));
	}
	
	/**
	 * Plays all songs of a genre. Playlist is previously cleared.
	 * @param genre Genre
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return True on success, false otherwise.
	 */
	public boolean play(INotifiableManager manager, Genre genre, int sortBy, String sortOrder) {
		return false; //play(manager, getSongsCondition(genre).append(songsOrderBy(sortBy, sortOrder)));
	}
	
	/**
	 * Plays all songs from an artist. Playlist is previously cleared.
	 * @param artist Artist
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return True on success, false otherwise.
	 */
	public boolean play(INotifiableManager manager, Artist artist, int sortBy, String sortOrder) {
		return false; //play(manager, getSongsCondition(artist).append(songsOrderBy(sortBy, sortOrder)));
	}
	
	/**
	 * Plays songs of a genre from an artist. Playlist is previously cleared.
	 * @param artist Artist
	 * @param genre Genre
	 * @return True on success, false otherwise.
	 */
	public boolean play(INotifiableManager manager, Artist artist, Genre genre) {
		return false; //play(manager, getSongsCondition(artist, genre).append(songsOrderBy(SortType.ARTIST, SortType.ORDER_ASC)));
	}

	/**
	 * Plays all songs fetched by a SQL condition.
	 * @param sqlCondition SQL Condition
	 * @return True on success, false otherwise.
	 */
	private boolean play(INotifiableManager manager, ObjNode node) {
		clearPlaylist(manager);
		node.p("playlistid", PLAYLIST_MUSIC);
		if(mConnection.getBoolean(manager, "Playlist.Add", node)) {
			setCurrentPlaylist(manager);
			return playNext(manager);
		}
		return false;
	}
	
	/**
	 * Starts playing/showing the next media/image in the current playlist
	 * or, if currently showing a slidshow, the slideshow playlist.
	 * @return True on success, false otherwise.
	 */
	public boolean playNext(INotifiableManager manager) {
		Integer player = getActivePlayerId(manager, MediaType.MUSIC);
		if(player != null) {
			return mConnection.getBoolean(manager, "Player.GoNext", obj().p("playerid", player));
		}
		return mConnection.getBoolean(manager, "Player.Open", obj().p("item", obj().p("playlistid", PLAYLIST_MUSIC)));
		
	}

	/**
	 * Starts playing/showing the previous media/image in the current playlist
	 * or, if currently showing a slidshow, the slideshow playlist.
	 * @return True on success, false otherwise.
	 */
	public boolean playPrev(INotifiableManager manager) {
		return false; //mConnection.getBoolean(manager, "PlayPrev");
	}
	
	/**
	 * Sets the media at playlist position position to be the next item to be 
	 * played. Position starts at 0, so SetPlaylistSong(5) sets the position
	 * to the 6th song in the playlist.
	 * @param pos Position
	 * @return true on success, false otherwise.
	 */
	public boolean playlistSetSong(INotifiableManager manager, int pos) {
		return false; //mConnection.getBoolean(manager, "SetPlaylistSong", String.valueOf(pos));
	}
	
	/**
	 * Sets current playlist to "0"
	 * @return True on success, false otherwise.
	 */
	public boolean setCurrentPlaylist(INotifiableManager manager) {
		return false; //mConnection.getBoolean(manager, "SetCurrentPlaylist", PLAYLIST_ID);
	}
	
	
	/**
	 * Gets all albums from database
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All albums
	 */
	public ArrayList<Album> getAlbums(INotifiableManager manager, int sortBy, String sortOrder) {
		// TODO: Make ignore article configurable
		return parseAlbums(mConnection.getJson(manager, "AudioLibrary.GetAlbums",
				sort(obj().p("properties", arr().add("artist").add("year")), sortBy, sortOrder, true)));
	}
	
	private ArrayList<Album> parseAlbums(JsonNode result) {
		final ArrayList<Album> albums = new ArrayList<Album>();
		final JsonNode jsonAlbums = result.get("albums");
		if(jsonAlbums == null) {
			return albums;
		}
		for (Iterator<JsonNode> i = jsonAlbums.getElements(); i.hasNext();) {
			JsonNode jsonAlbum = (JsonNode)i.next();
			albums.add(new Album(
				getInt(jsonAlbum, "albumid"), 
				getString(jsonAlbum, "label"), 
				getString(jsonAlbum, "artist"), 
				getInt(jsonAlbum, "year"), 
				getString(jsonAlbum, "thumbnail", "NONE") 
			));
		}
		return albums;
	}
	
	
	/**
	 * Gets all albums with given artist IDs
	 * @param artistIDs Array of artist IDs
	 * @return All compilation albums
	 */
	public ArrayList<Album> getAlbums(INotifiableManager manager, ArrayList<Integer> artistIDs, int sortBy, String sortOrder) {
		
		// TODO: Make ignore article configurable
		final ArrayList<Album> albums = new ArrayList<Album>();
		for (Integer id : artistIDs) {
			ObjNode node = sort(obj().p("artistid", id), sortBy, sortOrder, true);
			albums.addAll(parseAlbums(mConnection.getJson(manager, "AudioLibrary.GetAlbums", node)));
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
		
		// TODO: Make ignore article configurable
		ObjNode node = sort(obj().p("artistid", artist.getId()), sortBy, sortOrder, true);
		return parseAlbums(mConnection.getJson(manager, "AudioLibrary.GetAlbums", node));
	}

	/**
	 * Gets all albums of with at least one song in a genre
	 * @param genre Genre
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return Albums of a genre
	 */
	public ArrayList<Album> getAlbums(INotifiableManager manager, Genre genre, int sortBy, String sortOrder) {
		
		// TODO: Make ignore article configurable		
		ObjNode node = sort(obj().p("genreid", genre.getId()), sortBy, sortOrder, true);
		return parseAlbums(mConnection.getJson(manager, "AudioLibrary.GetAlbums", node));
	}
	
	private ArrayList<Artist> parseArtists(JsonNode result) {
		ArrayList<Artist> artists = new ArrayList<Artist>();
		final JsonNode jsonArtists = result.get("artists");
		if(jsonArtists == null) {
			return artists;
		}
		for (Iterator<JsonNode> i = jsonArtists.getElements(); i.hasNext();) {
			JsonNode jsonArtist = (JsonNode)i.next();
			artists.add(new Artist(
					getInt(jsonArtist, "artistid"), 
					getString(jsonArtist, "label") 
					));			
		}
		
		return artists;
	}
	
	
	/**
	 * Gets all albums from database
	 * @param albumArtistsOnly If set to true, hide artists who appear only on compilations.
	 * @return All albums
	 */
	public ArrayList<Artist> getArtists(INotifiableManager manager, boolean albumArtistsOnly) {
		
		// TODO: add ignore article as setting
		ObjNode node = sort(obj().p("albumartistsonly", albumArtistsOnly), SortType.ARTIST, SortType.ORDER_ASC, true);
		return parseArtists(mConnection.getJson(manager, "AudioLibrary.GetArtists", node));
	}

	/**
	 * Gets all artists with at least one song of a genre.
	 * @param genre Genre
	 * @param albumArtistsOnly If set to true, hide artists who appear only on compilations.
	 * @return Albums with a genre
	 */
	public ArrayList<Artist> getArtists(INotifiableManager manager, Genre genre, boolean albumArtistsOnly) {
		
		// TODO: add ignore article as setting
		ObjNode node = sort(obj().p("albumartistsonly", albumArtistsOnly).p("genreid", genre.getId()), SortType.ARTIST, SortType.ORDER_ASC, true);
		return parseArtists(mConnection.getJson(manager, "AudioLibrary.GetArtists", node));
	}
	
	/**
	 * Gets all genres from database
	 * @return All genres
	 */
	public ArrayList<Genre> getGenres(INotifiableManager manager) {
		// TODO: add ignore article as setting
		ObjNode node = sort(obj(), SortType.GENRE, SortType.ORDER_ASC, true);
		return parseGenres(mConnection.getJson(manager, "AudioLibrary.GetGenres", node));
	}
	
	/**
	 * Updates the album object with additional data from the albuminfo table
	 * @param album
	 * @return Updated album
	 */
	public Album updateAlbumInfo(INotifiableManager manager, Album album) {
		ObjNode obj = obj().p("albumid", album.getId()).p("properties", arr()
				.add("genre").add("rating"));
		JsonNode result = mConnection.getJson(manager, "AudioLibrary.GetAlbumDetails", obj);
		JsonNode albumDetails = result.get("albumdetails");
		if(albumDetails == null) {
			return album;
		}
		Log.e("MusicClient", result.toString());
		album.genres = getString(albumDetails, "genre");
		album.label = getString(albumDetails, "label");
		album.rating = getInt(albumDetails, "rating");
		return album;
	}
	
	/**
	 * Updates the artist object with additional data from the artistinfo table
	 * @param artist
	 * @return Updated artist
	 */
	public Artist updateArtistInfo(INotifiableManager manager, Artist artist) {

		ObjNode obj = obj().p("artistid", artist.getId()).p("properties", arr().add("born").add("formed")
				.add("genre").add("mood").add("style").add("description"));
		JsonNode result = mConnection.getJson(manager, "AudioLibrary.GetArtistDetails", obj);
		JsonNode artistDetails = result.get("artistdetails");
		if(artistDetails == null) {
			return artist;
		}
		Log.e("MusicClient", result.toString());
		artist.born = getString(artistDetails, "born");
		artist.formed = getString(artistDetails, "formed");
		artist.genres = getString(artistDetails, "genre");
		artist.moods = getString(artistDetails, "mood");
		artist.styles = getString(artistDetails, "style");
		artist.biography = getString(artistDetails, "description");
		return artist;
	}
	
	/**
	 * Returns a list containing tracks of a certain condition.
	 * @param sqlCondition SQL condition which tracks to return
	 * @return Found tracks
	 */
	public ArrayList<Song> getSongs(INotifiableManager manager, ObjNode obj, int sortBy, String sortOrder) {
		obj.p("properties", arr().add("duration").add("artist").add("album").add("track").add("file").add("thumbnail"));
		obj = sort(obj, sortBy, sortOrder, true);
		return parseSongs(mConnection.getJson(manager, "AudioLibrary.GetSongs", obj));
	}
	
	private ArrayList<Song> parseSongs(JsonNode result) {
		final ArrayList<Song> songs = new ArrayList<Song>();

		final JsonNode jsonSongs = result.get("songs");
		if(jsonSongs == null) {
			return songs;
		}
		for (Iterator<JsonNode> i = jsonSongs.getElements(); i.hasNext();) {
			JsonNode jsonSong = (JsonNode)i.next();
			
			songs.add(new Song(
					getInt(jsonSong, "songid"), 
				getString(jsonSong, "label"), 
				getString(jsonSong, "artist"), 
				getString(jsonSong, "album"),
				getInt(jsonSong, "track"),
				getInt(jsonSong, "duration"),
				getString(jsonSong, "file"),
				getString(jsonSong, "thumbnail", "NONE") 
			));
		}
		return songs;		
	}
	
	/**
	 * Returns a hash map containing tracks of a certain condition.
	 * @param sqlCondition SQL condition which tracks to return
	 * @return Found tracks
	 *
	private HashMap<Integer, Song> getSongsAsHashMap(StringBuilder sqlCondition) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT idSong, strTitle, strArtist, strAlbum, iTrack, iDuration, strPath, strFileName, strThumb");
		sb.append(" FROM songview WHERE ");
		sb.append(sqlCondition);
		sb.append(" ORDER BY iTrack, strFileName");
		parseSongsAsHashMap(mConnection.query("QueryMusicDatabase", sb.toString()));
	}*/
	
	/**
	 * Returns the SQL condition that returns all songs of a song.
	 * @param song Song
	 * @return SQL string
	 */
	private ObjNode getSongsCondition(Song song) {
		return obj().p("songid", song.getId());
	}

	/**
	 * Returns the SQL condition that returns all songs of an album.
	 * @param album Album
	 * @return SQL string
	 */
	private ObjNode getSongsCondition(Album album) {
		return obj().p("albumid", album.getId());
	}

	/**
	 * Returns the SQL condition that returns all songs of an artist.
	 * @param artist Artist
	 * @return SQL string
	 */
	private ObjNode getSongsCondition(Artist artist) {
		return obj().p("artistid", artist.getId());
	}	
	
	/**
	 * Returns the SQL condition that returns all songs of a genre.
	 * @param genre Genre
	 * @return SQL string
	 */
	private ObjNode getSongsCondition(Genre genre) {
		return obj().p("genreid", genre.getId());
	}
	
	/**
	 * Returns the SQL condition that returns all songs of a genre AND an artist.
	 * @param artist Artist
	 * @param genre Genre
	 * @return SQL string
	 */
	private ObjNode getSongsCondition(Artist artist, Genre genre) {
		return obj().p("genreid", genre.getId()).p("artistid", artist.getId());
	}
	
	/**
	 * Returns a list containing all tracks of an album. The list is sorted by filename.
	 * @param album Album
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.	 
	 * @return All tracks of an album
	 */
	public ArrayList<Song> getSongs(INotifiableManager manager, Album album, int sortBy, String sortOrder) {
		return getSongs(manager, getSongsCondition(album), sortBy, sortOrder);
	}

	/**
	 * Returns a list containing all tracks of an artist. The list is sorted by album name, filename.
	 * @param artist Artist
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All tracks of the artist
	 */
	public ArrayList<Song> getSongs(INotifiableManager manager, Artist artist, int sortBy, String sortOrder) {
		return getSongs(manager, getSongsCondition(artist), sortBy, sortOrder);
	}
	
	/**
	 * Returns a list containing all tracks of a genre. The list is sorted by artist, album name, filename.
	 * @param genre Genre
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All tracks of the genre
	 */
	public ArrayList<Song> getSongs(INotifiableManager manager, Genre genre, int sortBy, String sortOrder) {
		return getSongs(manager, getSongsCondition(genre), sortBy, sortOrder);
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
		return getSongs(manager, getSongsCondition(artist, genre), sortBy, sortOrder);
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
		if(cover instanceof Album) {
			return getCover(manager, cover, size, Album.getThumbUri(cover), Album.getFallbackThumbUri(cover));
		}
		else if(cover instanceof Artist) {
			return getCover(manager, cover, size, Artist.getThumbUri(cover), Artist.getFallbackThumbUri(cover));
		}
		throw new RuntimeException("Unsupported cover type");
	}
	
	/**
	 * Returns a list containing all artist IDs that stand for "compilation".
	 * Best case scenario would be only one ID for "Various Artists", though
	 * there are also just "V.A." or "VA" naming conventions.
	 * @return List of compilation artist IDs
	 */
	public ArrayList<Integer> getCompilationArtistIDs(INotifiableManager manager) {
		
		// TODO: Should this feature potentially be removed?
		ArrayList<Integer> ids = new ArrayList<Integer>();
		
		ArrayList<Artist> artists = getArtists(manager, true);
		for(Artist artist : artists) {
			if(artist.getName() != null) {
				String name = artist.getName().toLowerCase();
				if(name.startsWith("various artists") || name.startsWith("v.a.") || name.equals("va")) {
					ids.add(artist.getId());
				}
			}
		}
		return ids;
	}
	

	/**
	 * Returns an SQL String of given sort options of songs query
	 * @param sortBy    Sort field
	 * @param sortOrder Sort order
	 * @return SQL "ORDER BY" string
	 */
	private String songsOrderBy(int sortBy, String sortOrder) {
		switch (sortBy) {
			case SortType.ALBUM:
				return " ORDER BY lower(strAlbum) " + sortOrder + ", iTrack " + sortOrder;
			case SortType.ARTIST:
				return " ORDER BY lower(strArtist) " + sortOrder + ", lower(strAlbum) " + sortOrder + ", iTrack " + sortOrder;
			case SortType.TITLE:
				return " ORDER BY lower(strTitle)" + sortOrder;
			case SortType.FILENAME:
				return " ORDER BY lower(strFileName)" + sortOrder;
			default:
			case SortType.TRACK:
				return " ORDER BY iTrack " + sortOrder + ", lower(strFileName) " + sortOrder;
			case SortType.DONT_SORT:
				return "";
		}
	}

	/**
	 * Converts query response from HTTP API to a list of Album objects. Each
	 * row must return the following attributes in the following order:
	 * <ol>
	 * 	<li><code>idAlbum</code></li>
	 * 	<li><code>strAlbum</code></li>
	 * 	<li><code>strArtist</code></li>
	 * 	<li><code>iYear</code></li>
	 * 	<li><code>strThumb</code></li>
	 * </ol> 
	 * @param response
	 * @return List of albums
	 *
	private ArrayList<Album> parseAlbums(String response) {
		ArrayList<Album> albums = new ArrayList<Album>();
		String[] fields = response.split("<field>");
		try {
			for (int row = 1; row < fields.length; row += 5) {
				albums.add(new Album(
						Connection.trimInt(fields[row]), 
						Connection.trim(fields[row + 1]), 
						Connection.trim(fields[row + 2]),
						Connection.trimInt(fields[row + 3]),
						Connection.trim(fields[row + 4])
				));
			}
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println("response = " + response);
			e.printStackTrace();
		}
		return albums;
		return null;
	}*/
	
	/**
	 * Updates an album with info from HTTP API query response. One row is 
	 * expected, with the following columns:
	 * <ol>
	 * 	<li><code>strGenre</code></li>
	 * 	<li><code>strExtraGenres</code></li>
	 * 	<li><code>iYear</code></li>
	 * 	<li><code>strLabel</code></li>
	 * 	<li><code>iRating</code></li>
	 * </ol>  
	 * @param album
	 * @param response
	 * @return Updated album
	 *
	private Album parseAlbumInfo(Album album, String response) {
		String[] fields = response.split("<field>");
		try {
			if (Connection.trim(fields[2]).length() > 0) {
				album.genres = Connection.trim(fields[1]) + Connection.trim(fields[2]);
			}	
			if (Connection.trim(fields[3]).length() > 0) {
				album.label = Connection.trim(fields[4]);
			}
			if (Connection.trim(fields[4]).length() > 0) {
				album.rating = Connection.trimInt(fields[5]);
			}
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println("response = " + response);
			e.printStackTrace();
		}
		return null; //album;
	}*/
	
	/**
	 * Converts query response from HTTP API to a list of Song objects. Each
	 * row must return the following columns in the following order:
	 * <ol>
	 * 	<li><code>idSong</code></li>
	 * 	<li><code>strTitle</code></li>
	 * 	<li><code>strArtist</code></li>
	 * 	<li><code>strAlbum</code></li>
	 * 	<li><code>iTrack</code></li>
	 * 	<li><code>iDuration</code></li>
	 * 	<li><code>strPath</code></li>
	 * 	<li><code>strFileName</code></li>
	 * 	<li><code>strThumb</code></li>
	 * </ol> 
	 * @param response
	 * @return List of Songs
	 *
	private ArrayList<Song> parseSongs(String response) {
		ArrayList<Song> songs = new ArrayList<Song>();
		String[] fields = response.split("<field>");
		try { 
			for (int row = 1; row < fields.length; row += 9) { 
				songs.add(new Song( // int id, String title, String artist, String album, int track, int duration, String path, String filename, String thumbPath
						Connection.trimInt(fields[row]),
						Connection.trim(fields[row + 1]), 
						Connection.trim(fields[row + 2]), 
						Connection.trim(fields[row + 3]), 
						Connection.trimInt(fields[row + 4]), 
						Connection.trimInt(fields[row + 5]), 
						Connection.trim(fields[row + 6]),
						Connection.trim(fields[row + 7]), 
						Connection.trim(fields[row + 8]) 
				));
			}
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println("response = " + response);
			e.printStackTrace();
		}
		return null; //songs;		
	}*/
	
	/**
	 * Converts query response from HTTP API to a list of Song objects. Each
	 * row must return the following columns in the following order:
	 * <ol>
	 * 	<li><code>idSong</code></li>
	 * 	<li><code>strTitle</code></li>
	 * 	<li><code>strArtist</code></li>
	 * 	<li><code>strAlbum</code></li>
	 * 	<li><code>iTrack</code></li>
	 * 	<li><code>iDuration</code></li>
	 * 	<li><code>strPath</code></li>
	 * 	<li><code>strFileName</code></li>
	 * 	<li><code>strThumb</code></li>
	 * </ol> 
	 * @param response
	 * @return List of Songs
	 *
	private HashMap<Integer, Song> parseSongsAsHashMap(String response) {
		HashMap<Integer, Song> songs = new HashMap<Integer, Song>();
		String[] fields = response.split("<field>");
		try { 
			for (int row = 1; row < fields.length; row += 9) { 
				songs.put(Connection.trimInt(fields[row]),
					new Song( // int id, String title, String artist, String album, int track, int duration, String path, String filename, String thumbPath
						Connection.trimInt(fields[row]),
						Connection.trim(fields[row + 1]), 
						Connection.trim(fields[row + 2]), 
						Connection.trim(fields[row + 3]), 
						Connection.trimInt(fields[row + 4]), 
						Connection.trimInt(fields[row + 5]), 
						Connection.trim(fields[row + 6]),
						Connection.trim(fields[row + 7]), 
						Connection.trim(fields[row + 8])
					)
				);
			}
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println("response = " + response);
			e.printStackTrace();
		}
		return songs;		
	}*/
	
	/**
	 * Converts query response from HTTP API to a list of integer values.
	 * @param response
	 * @return
	 *
	private ArrayList<Integer> parseIntArray(String response) {
		ArrayList<Integer> array = new ArrayList<Integer>();
		String[] fields = response.split("<field>");
		try { 
			for (int row = 1; row < fields.length; row += 9) {
				array.add(Connection.trimInt(fields[row]));
			}
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println("response = " + response);
			e.printStackTrace();
		}
		return null; //array;
	}*/
	
	/**
	 * Converts query response from HTTP API to a list of Artist objects. Each
	 * row must return the following columns in the following order:
	 * <ol>
	 * 	<li><code>idArtist</code></li>
	 * 	<li><code>strArtist</code></li>
	 * </ol>
	 * @param response
	 * @return List of Artists
	 *
	private ArrayList<Artist> parseArtists(String response) {
		ArrayList<Artist> artists = new ArrayList<Artist>();
		String[] fields = response.split("<field>");
		try { 
			for (int row = 1; row < fields.length; row += 2) { 
				artists.add(new Artist(
						Connection.trimInt(fields[row]), 
						Connection.trim(fields[row + 1])
				));
			}
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println("response = " + response);
			e.printStackTrace();
		}
		return null; //artists;		
	}*/
	
	/**
	 * Converts query response from HTTP API to a list of Genre objects. Each
	 * row must return the following columns in the following order:
	 * <ol>
	 * 	<li><code>idGenre</code></li>
	 * 	<li><code>strGenre</code></li>
	 * </ol>
	 * @param response
	 * @return List of Genres
	 *
	private ArrayList<Genre> parseGenres(String response) {
		ArrayList<Genre> genres = new ArrayList<Genre>();
		String[] fields = response.split("<field>");
		try { 
			for (int row = 1; row < fields.length; row += 2) { 
				genres.add(new Genre(
						Connection.trimInt(fields[row]), 
						Connection.trim(fields[row + 1])
				));
			}
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println("response = " + response);
			e.printStackTrace();
		}
		return null; //genres;		
	}*/
	
	static ICurrentlyPlaying getCurrentlyPlaying(final Integer currentPlayer, final JsonNode item, final JsonNode props) {
		return new IControlClient.ICurrentlyPlaying() {
			private static final long serialVersionUID = 5036994329211476714L;
			public String getTitle() {
				return item.get("label").getTextValue();
			}
			public int getTime() {
				return parseTime(props.get("time"));
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
				return item.get("duration").getIntValue();
			}
			public String getArtist() {
				String albumArtist = item.get("albumartist").getTextValue();
				if(!"".equals(albumArtist)) {
					return albumArtist;
				}
				return item.get("artist").getTextValue();
			}
			public String getAlbum() {
				return item.get("album").getTextValue();
			}
			public int getMediaType() {
				return MediaType.MUSIC;
			}
			public boolean isPlaying() {
				return props.get("speed").getIntValue() > 0;
			}
			public int getHeight() {
				return 0;
			}
			public int getWidth() {
				return 0;
			}
			private int parseTime(JsonNode time) {
				int hours = time.get("hours").getIntValue();
				//int milliseconds = time.get("milliseconds").getIntValue();
				int minutes = time.get("minutes").getIntValue();
				int seconds = time.get("seconds").getIntValue();
				
				return (hours * 3600) + (minutes * 60) + seconds;
			}
		};
	}	
}
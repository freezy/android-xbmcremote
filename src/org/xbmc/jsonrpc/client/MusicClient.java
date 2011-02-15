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
import java.util.HashMap;
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
	
	public static final String PLAYLIST_ID = "0";
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
		return mConnection.getBoolean(manager, "AddToPlayListFromDB", LIBRARY_TYPE + ";" + getSongsCondition(album) + songsOrderBy(sortBy, sortOrder));
	}

	/**
	 * Adds all songs from an artist to the current playlist.
	 * @param artist Artist
	 * @return True on success, false otherwise.
	 */
	public boolean addToPlaylist(INotifiableManager manager, Artist artist, int sortBy, String sortOrder) {
		return mConnection.getBoolean(manager, "AddToPlayListFromDB", LIBRARY_TYPE + ";" + getSongsCondition(artist) + songsOrderBy(sortBy, sortOrder));
	}

	/**
	 * Adds all songs from a genre to the current playlist.
	 * @param genre Genre
	 * @return True on success, false otherwise.
	 */
	public boolean addToPlaylist(INotifiableManager manager, Genre genre, int sortBy, String sortOrder) {
		return mConnection.getBoolean(manager, "AddToPlayListFromDB", LIBRARY_TYPE + ";" + getSongsCondition(genre) + songsOrderBy(sortBy, sortOrder));
	}

	/**
	 * Adds songs of a genre from an artist to the current playlist.
	 * @param artist Artist
	 * @param genre Genre
	 * @return True on success, false otherwise.
	 */
	public boolean addToPlaylist(INotifiableManager manager, Artist artist, Genre genre, int sortBy, String sortOrder) {
		return mConnection.getBoolean(manager, "AddToPlayListFromDB", LIBRARY_TYPE + ";" + getSongsCondition(artist, genre) + songsOrderBy(sortBy, sortOrder));
	}
	
	/**
	 * Adds a song to the current playlist.
	 * @param song Song to add
	 * @return True on success, false otherwise.
	 */
	public boolean addToPlaylist(INotifiableManager manager, Song song) {
		return mConnection.getBoolean(manager, "AddToPlayList", song.path + ";" + PLAYLIST_ID);
	}
	
	/**
	 * Returns how many items are in the playlist.
	 * @return Number of items in the playlist
	 */
	public int getPlaylistSize(INotifiableManager manager) {
		return mConnection.getInt(manager, "GetPlaylistLength", PLAYLIST_ID);
	}
	
	/**
	 * Retrieves the currently playing song number in the playlist.
	 * @return Number of items in the playlist
	 */
	public int getPlaylistPosition(INotifiableManager manager) {
		return 0;//mConnection.getInt(manager, "GetPlaylistSong");
	}
	
	/**
	 * Sets the media at playlist position position to be the next item to be played.
	 * @param position New position, starting with 0.
	 * @return True on success, false otherwise.
	 */
	public boolean setPlaylistPosition(INotifiableManager manager, int position) {
		return false; //mConnection.getBoolean(manager, "SetPlaylistSong", String.valueOf(position));
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
		return null; //mConnection.getArray(manager, "GetPlaylistContents", PLAYLIST_ID);
		
		
		/*
		final ArrayList<String> nodes = mConnection.getArray("GetDirectory", "playlistmusic://");
		final ArrayList<String> ids = new ArrayList<String>();
		final int playlistPosition = getPlaylistPosition();
		int i = 0;
		for (String node : nodes) {
			ids.add(node.substring(node.lastIndexOf('/') + 1, node.lastIndexOf('.')));
			if (++i > PLAYLIST_LIMIT + playlistPosition) {
				break;
			}
		}
		StringBuilder sql = new StringBuilder();
		sql.append("idSong IN (");
		int j = 0;
		for (String id : ids) {
			sql.append(id);
			if (++j < i) {
				sql.append(',');
			}
		}
		sql.append(")");
		final HashMap<Integer, Song> unsortedSongs = getSongsAsHashMap(sql);
		final ArrayList<Song> sortedSongs = new ArrayList<Song>();
		
		for (String node : nodes) {
			try {
				final int id = Integer.parseInt(node.substring(node.lastIndexOf('/') + 1, node.lastIndexOf('.')));
				sortedSongs.add(unsortedSongs.get(id));
			} catch (NumberFormatException e) { 
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			}
		}
		return sortedSongs;*/
	}
	
	/**
	 * Clears current playlist
	 * @return True on success, false otherwise.
	 */
	public boolean clearPlaylist(INotifiableManager manager) {
		return false; //mConnection.getBoolean(manager, "ClearPlayList", PLAYLIST_ID);
	}
	
	/**
	 * Adds a song to the current playlist and plays it.
	 * @param song Song
	 * @return True on success, false otherwise.
	 */
	public boolean play(INotifiableManager manager, Song song) {
		return false; //play(manager, getSongsCondition(song));
	}
	
	/**
	 * Plays an album. Playlist is previously cleared.
	 * @param album Album to play
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return True on success, false otherwise.
	 */
	public boolean play(INotifiableManager manager, Album album, int sortBy, String sortOrder) {
		return false; //play(manager, getSongsCondition(album).append(songsOrderBy(sortBy, sortOrder)));
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
	 *
	private boolean play(INotifiableManager manager, StringBuilder sqlCondition) {
		clearPlaylist(manager);
		mConnection.getBoolean(manager, "AddToPlayListFromDB", LIBRARY_TYPE + ";" + sqlCondition.toString());
		setCurrentPlaylist(manager);
		return false; //playNext(manager);
	}*/
	
	/**
	 * Starts playing/showing the next media/image in the current playlist
	 * or, if currently showing a slidshow, the slideshow playlist.
	 * @return True on success, false otherwise.
	 */
	public boolean playNext(INotifiableManager manager) {
		return false; //mConnection.getBoolean(manager, "PlayNext");
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
		return getAlbums(manager, sort(obj().p(PARAM_FIELDS, arr().add("artist").add("year")), sortBy, sortOrder));
	}
	
	private ArrayList<Album> getAlbums(INotifiableManager manager, ObjNode obj) {
		final ArrayList<Album> albums = new ArrayList<Album>();
		final JsonNode result = mConnection.getJson(manager, "MusicLibrary.GetAlbums", obj);
		final JsonNode jsonAlbums = result.get("albums");
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
	public ArrayList<Album> getAlbums(INotifiableManager manager, ArrayList<Integer> artistIDs) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT idAlbum, strAlbum, strArtist, iYear, strThumb");
		sb.append(" FROM albumview WHERE albumview.strAlbum <> ''");
		sb.append(" AND idArtist IN (");
		int n = 0;
		for (Integer id : artistIDs) {
			sb.append(id);
			n++;
			if (artistIDs.size() < n) {
				sb.append(", ");
			}
		}
		sb.append(")");
		return null; //parseAlbums(mConnection.query("QueryMusicDatabase", sb.toString(), manager));
	}


	/**
	 * Gets all albums of an artist from database
	 * @param artist Artist
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return Albums with an artist
	 */
	public ArrayList<Album> getAlbums(INotifiableManager manager, Artist artist, int sortBy, String sortOrder) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT idAlbum, strAlbum, strArtist, iYear, strThumb");
		sb.append(" FROM albumview");
		sb.append(" WHERE albumview.strAlbum <> ''");
		sb.append(" AND idArtist = " + artist.id);
//		sb.append(albumsOrderBy(sortBy, sortOrder));
		return null; //parseAlbums(mConnection.query("QueryMusicDatabase", sb.toString(), manager));
	}

	/**
	 * Gets all albums of with at least one song in a genre
	 * @param genre Genre
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return Albums of a genre
	 */
	public ArrayList<Album> getAlbums(INotifiableManager manager, Genre genre, int sortBy, String sortOrder) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT idAlbum, strAlbum, strArtist, iYear, strThumb");
		sb.append("  FROM albumview");
		sb.append("  WHERE albumview.strAlbum <> ''");
		sb.append("  AND (idAlbum IN ("); 
		sb.append("        SELECT song.idAlbum FROM song"); 			
		sb.append("        JOIN exgenresong ON song.idSong = exgenresong.idSong"); 			
		sb.append("        WHERE exgenresong.idGenre =  " + genre.id);
		sb.append("  ) OR idAlbum IN (");
		sb.append("        SELECT DISTINCT idAlbum");
		sb.append("        FROM song");
		sb.append("        WHERE idGenre = " + genre.id);
		sb.append("  ))");
//		sb.append(albumsOrderBy(sortBy, sortOrder));
		return null; //parseAlbums(mConnection.query("QueryMusicDatabase", sb.toString(), manager));
	}
	
	/**
	 * Gets all albums from database
	 * @param albumArtistsOnly If set to true, hide artists who appear only on compilations.
	 * @return All albums
	 */
	public ArrayList<Artist> getArtists(INotifiableManager manager, boolean albumArtistsOnly) {
		StringBuilder sb = new StringBuilder();
		if (albumArtistsOnly) {
			sb.append("SELECT idArtist, strArtist ");
			sb.append("  FROM artist");
			sb.append("  WHERE (");
			sb.append("    idArtist IN (");
			sb.append("      SELECT album.idArtist");
			sb.append("      FROM album");
			sb.append("    ) OR idArtist IN (");
			sb.append("      SELECT exartistalbum.idArtist");
			sb.append("      FROM exartistalbum");
			sb.append("      JOIN album ON album.idAlbum = exartistalbum.idAlbum");
			sb.append("      WHERE album.strExtraArtists != ''");
			sb.append("    )");
			sb.append(") AND artist.strArtist != ''");
		} else {
			sb.append("SELECT idArtist, strArtist FROM artist");
		}
		sb.append(" ORDER BY upper(strArtist), strArtist");
		return null; //parseArtists(mConnection.query("QueryMusicDatabase", sb.toString(), manager));
	}

	/**
	 * Gets all artists with at least one song of a genre.
	 * @param genre Genre
	 * @param albumArtistsOnly If set to true, hide artists who appear only on compilations.
	 * @return Albums with a genre
	 */
	public ArrayList<Artist> getArtists(INotifiableManager manager, Genre genre, boolean albumArtistsOnly) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT idArtist, strArtist ");
		sb.append("  FROM artist");
		sb.append("  WHERE (idArtist IN (");
		sb.append("    SELECT DISTINCT s.idArtist");
		sb.append("    FROM exgenresong AS g, song AS s");
		sb.append("    WHERE g.idGenre = " + genre.id);
		sb.append("    AND g.idSong = s.idSong");
		sb.append("  ) OR idArtist IN (");
		sb.append("    SELECT DISTINCT idArtist");
		sb.append("     FROM song");
		sb.append("     WHERE idGenre = " + genre.id);
		sb.append("  ))");
		if (albumArtistsOnly) {
			sb.append("  AND (");
			sb.append("    idArtist IN (");
			sb.append("      SELECT album.idArtist");
			sb.append("      FROM album");
			sb.append("    ) OR idArtist IN (");
			sb.append("      SELECT exartistalbum.idArtist");
			sb.append("      FROM exartistalbum");
			sb.append("      JOIN album ON album.idAlbum = exartistalbum.idAlbum");
			sb.append("      WHERE album.strExtraArtists != ''");
			sb.append("    )");
			sb.append("  ) AND artist.strArtist != ''");
		}		
		sb.append(" ORDER BY upper(strArtist), strArtist");
		return null; //parseArtists(mConnection.query("QueryMusicDatabase", sb.toString(), manager));
	}
	
	/**
	 * Gets all genres from database
	 * @return All genres
	 */
	public ArrayList<Genre> getGenres(INotifiableManager manager) {
		return null; //parseGenres(mConnection.query("QueryMusicDatabase", "SELECT idGenre, strGenre FROM genre ORDER BY upper(strGenre), strGenre", manager));
	}
	
	/**
	 * Updates the album object with additional data from the albuminfo table
	 * @param album
	 * @return Updated album
	 */
	public Album updateAlbumInfo(INotifiableManager manager, Album album) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT g.strGenre, a.strExtraGenres, ai.strLabel, ai.iRating");
		sb.append("  FROM album a, genre g");
		sb.append("  LEFT JOIN albuminfo AS ai ON ai.idAlbumInfo = a.idAlbum");
		sb.append("  WHERE a.idGenre = g.idGenre");
		sb.append("  AND a.idAlbum = " + album.id);
		return null; //parseAlbumInfo(album, mConnection.query("QueryMusicDatabase", sb.toString(), manager));
	}
	
	/**
	 * Returns a list containing tracks of a certain condition.
	 * @param sqlCondition SQL condition which tracks to return
	 * @return Found tracks
	 *
	private ArrayList<Song> getSongs(INotifiableManager manager, StringBuilder sqlCondition, int sortBy, String sortOrder) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT idSong, strTitle, strArtist, strAlbum, iTrack, iDuration, strPath, strFileName, strThumb");
		sb.append(" FROM songview WHERE ");
		sb.append(sqlCondition);
		sb.append(songsOrderBy(sortBy, sortOrder));
		
		return null; //parseSongs(mConnection.query("QueryMusicDatabase", sb.toString(), manager));
	}*/
	
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
	 *
	private StringBuilder getSongsCondition(Song song) {
		final StringBuilder sb = new StringBuilder();
		sb.append("idSong = ");
		sb.append(song.id);
		return sb;
	}*/

	/**
	 * Returns the SQL condition that returns all songs of an album.
	 * @param album Album
	 * @return SQL string
	 */
	private StringBuilder getSongsCondition(Album album) {
		final StringBuilder sb = new StringBuilder();
		sb.append("idAlbum = ");
		sb.append(album.id);
		return sb;
	}

	/**
	 * Returns the SQL condition that returns all songs of an artist.
	 * @param artist Artist
	 * @return SQL string
	 */
	private StringBuilder getSongsCondition(Artist artist) {
		final StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append("  idArtist = ");
		sb.append(artist.id);
		sb.append("  OR idSong IN (");
		sb.append("     SELECT exartistsong.idSong");
		sb.append("     FROM exartistsong");
		sb.append("     WHERE exartistsong.idArtist = ");
		sb.append(artist.id);
		sb.append("  ) OR idSong IN (");
		sb.append("     SELECT song.idSong");
		sb.append("     FROM song");
		sb.append("     JOIN album ON song.idAlbum = album.idAlbum");
		sb.append("     WHERE album.idArtist = ");
		sb.append(artist.id);
		sb.append("  ) OR idSong IN (");
		sb.append("     SELECT song.idSong");
		sb.append("     FROM song");
		sb.append("     JOIN exartistalbum ON song.idAlbum = exartistalbum.idAlbum");
		sb.append("     JOIN album ON song.idAlbum = album.idAlbum");
		sb.append("     WHERE exartistalbum.idArtist = ");
		sb.append(artist.id);
		sb.append("     AND album.strExtraArtists != ''");
		sb.append("  )");
		sb.append(")");
		return sb;
	}	
	
	/**
	 * Returns the SQL condition that returns all songs of a genre.
	 * @param genre Genre
	 * @return SQL string
	 */
	private StringBuilder getSongsCondition(Genre genre) {
		final StringBuilder sb = new StringBuilder();
		sb.append("idGenre = ");
		sb.append(genre.id);
		sb.append("  OR idSong IN (");
		sb.append("    SELECT exgenresong.idSong FROM exgenresong WHERE exgenresong.idGenre = ");
		sb.append(genre.id);
		sb.append(")");
		return sb;
	}

	/**
	 * Returns the SQL condition that returns all songs of a genre AND an artist.
	 * @param artist Artist
	 * @param genre Genre
	 * @return SQL string
	 */
	private StringBuilder getSongsCondition(Artist artist, Genre genre) {
		final StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append("  idArtist = ");
		sb.append(artist.id);
		sb.append("  OR idSong IN (");
		sb.append("     SELECT exartistsong.idSong");
		sb.append("     FROM exartistsong");
		sb.append("     WHERE exartistsong.idArtist = ");
		sb.append(artist.id);
		sb.append("  ) OR idSong IN (");
		sb.append("     SELECT song.idSong");
		sb.append("     FROM song");
		sb.append("     JOIN album ON song.idAlbum = album.idAlbum");
		sb.append("     WHERE album.idArtist = ");
		sb.append(artist.id);
		sb.append("  ) OR idSong IN (");
		sb.append("     SELECT song.idSong");
		sb.append("     FROM song");
		sb.append("     JOIN exartistalbum ON song.idAlbum = exartistalbum.idAlbum");
		sb.append("     JOIN album ON song.idAlbum = album.idAlbum");
		sb.append("     WHERE exartistalbum.idArtist = ");
		sb.append(artist.id);
		sb.append("     AND album.strExtraArtists != ''");
		sb.append("  )");
		sb.append(") AND (");
		sb.append("  idGenre = ");
		sb.append(genre.id);
		sb.append("  OR idSong IN (");
		sb.append("     SELECT exgenresong.idSong FROM exgenresong WHERE exgenresong.idGenre = ");
		sb.append(genre.id);
		sb.append("  )");
		sb.append(")");
		return sb;
	}
	
	/**
	 * Returns a list containing all tracks of an album. The list is sorted by filename.
	 * @param album Album
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.	 
	 * @return All tracks of an album
	 */
	public ArrayList<Song> getSongs(INotifiableManager manager, Album album, int sortBy, String sortOrder) {
		return null; //getSongs(manager, getSongsCondition(album), sortBy, sortOrder);
	}

	/**
	 * Returns a list containing all tracks of an artist. The list is sorted by album name, filename.
	 * @param artist Artist
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All tracks of the artist
	 */
	public ArrayList<Song> getSongs(INotifiableManager manager, Artist artist, int sortBy, String sortOrder) {
		return null; //getSongs(manager, getSongsCondition(artist), sortBy, sortOrder);
	}
	
	/**
	 * Returns a list containing all tracks of a genre. The list is sorted by artist, album name, filename.
	 * @param genre Genre
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All tracks of the genre
	 */
	public ArrayList<Song> getSongs(INotifiableManager manager, Genre genre, int sortBy, String sortOrder) {
		return null; //getSongs(manager, getSongsCondition(genre), sortBy, sortOrder);
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
		return null; //getSongs(manager, getSongsCondition(artist, genre), sortBy, sortOrder);
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
		return getCover(manager, cover, size, Album.getThumbUri(cover), Album.getFallbackThumbUri(cover));
	}
	
	/**
	 * Returns a list containing all artist IDs that stand for "compilation".
	 * Best case scenario would be only one ID for "Various Artists", though
	 * there are also just "V.A." or "VA" naming conventions.
	 * @return List of compilation artist IDs
	 */
	public ArrayList<Integer> getCompilationArtistIDs(INotifiableManager manager) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT idArtist");
		sb.append("  FROM artist");
		sb.append("  WHERE lower(strArtist) LIKE 'various artists%'");
		sb.append("  OR lower(strArtist) LIKE 'v.a.%'");
		sb.append("  OR lower(strArtist) = 'va'");
		return null; //parseIntArray(mConnection.query("QueryMusicDatabase", sb.toString(), manager));
		
	}
	
	/**
	 * Returns an SQL String of given sort options of albums query
	 * @param sortBy    Sort field
	 * @param sortOrder Sort order
	 * @return SQL "ORDER BY" string
	 */
	private static ObjNode sort(ObjNode params, int sortBy, String sortOrder) {
		final String order = sortOrder.equals(SortType.ORDER_DESC) ? "descending" : "ascending";
		switch (sortBy) {
			default:
			case SortType.ALBUM:
				params.p("sortmethod", "label").p("sortorder", order);
				break;
			case SortType.ARTIST:
				params.p("sortmethod", "artist").p("sortorder", order);
				break;
			case SortType.TRACK:
				params.p("sortmethod", "track").p("sortorder", order);
				break;
		}
		return params;
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
	
	static ICurrentlyPlaying getCurrentlyPlaying(final HashMap<String, String> map) {
		return new IControlClient.ICurrentlyPlaying() {
			private static final long serialVersionUID = 5036994329211476714L;
			public String getTitle() {
				return map.get("Title");
			}
			public int getTime() {
				return parseTime(map.get("Time"));
			}
			public int getPlayStatus() {
				return PlayStatus.parse(map.get("PlayStatus"));
			}
			public int getPlaylistPosition() {
				return Integer.parseInt(map.get("SongNo"));
			}
			//Workarond for bug in Float.valueOf(): http://code.google.com/p/android/issues/detail?id=3156
			public float getPercentage() {
				try{
					return Integer.valueOf(map.get("Percentage"));
				} catch (NumberFormatException e) { }
				return Float.valueOf(map.get("Percentage"));
			}
			public String getFilename() {
				return map.get("Filename");
			}
			public int getDuration() {
				return parseTime(map.get("Duration"));
			}
			public String getArtist() {
				return map.get("Artist");
			}
			public String getAlbum() {
				return map.get("Album");
			}
			public int getMediaType() {
				return MediaType.MUSIC;
			}
			public boolean isPlaying() {
				return PlayStatus.parse(map.get("PlayStatus")) == PlayStatus.PLAYING;
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
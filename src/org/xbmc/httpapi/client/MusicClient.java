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

package org.xbmc.httpapi.client;

import java.util.ArrayList;
import java.util.Collections;

import org.xbmc.httpapi.Connection;
import org.xbmc.httpapi.data.Album;
import org.xbmc.httpapi.data.Artist;
import org.xbmc.httpapi.data.Genre;
import org.xbmc.httpapi.data.ICoverArt;
import org.xbmc.httpapi.data.Song;

import android.util.Log;

/**
 * Takes care of every music related stuff, notably the music database.
 * 
 * @author Team XBMC
 */
public class MusicClient {
	
	// those are the musicdb://n/ keys.
	public static final int MUSICDB_GENRE           = 1;
	public static final int MUSICDB_ARTIST          = 2;
	public static final int MUSICDB_ALBUM           = 3;
	public static final int MUSICDB_SONG            = 4;
	public static final int MUSICDB_TOP100          = 5;
	public static final int MUSICDB_RECENTLY_ADDED  = 6;
	public static final int MUSICDB_RECENTLY_PLAYED = 7;
	public static final int MUSICDB_COMPILATION     = 8;
	public static final int MUSICDB_YEARS           = 9;
	
	public static final String PLAYLIST_ID = "0";
	
	private final Connection mConnection;

	/**
	 * Class constructor needs reference to HTTP client connection
	 * @param connection
	 */
	public MusicClient(Connection connection) {
		mConnection = connection;
	}
	
	/**
	 * Adds an album to the current playlist.
	 * @param album
	 * @return first song of the album
	 */
	public Song addToPlaylist(Album album) {
//		return mConnection.getBoolean("AddToPlayList", "musicdb://" + MUSICDB_ALBUM + "/" + album.id);
		final ArrayList<Song> songs = getSongs(album);
		Song firstSong = null;
		for (Song song : songs) {
			if (firstSong == null) {
				firstSong = song;
			}
			addToPlaylist(song);
		}
		return firstSong;
	}

	/**
	 * Adds all songs from an artist to the current playlist.
	 * @param artist
	 * @return first song of all added songs
	 */
	public Song addToPlaylist(Artist artist) {
		final ArrayList<Song> songs = getSongs(artist);
		Song firstSong = null;
		for (Song song : songs) {
			if (firstSong == null) {
				firstSong = song;
			}
			addToPlaylist(song);
		}
		return firstSong;
	}

	/**
	 * Adds songs of a genre from an artist to the current playlist.
	 * @param artist
	 * @return first song of all added songs
	 */
	public Song addToPlaylist(Artist artist, Genre genre) {
		final ArrayList<Song> songs = getSongs(artist, genre);
		Song firstSong = null;
		for (Song song : songs) {
			if (firstSong == null) {
				firstSong = song;
			}
			addToPlaylist(song);
		}
		return firstSong;
	}
	
	/**
	 * Adds a song to the current playlist.
	 * @param song
	 * @return true on success, false otherwise.
	 */
	public boolean addToPlaylist(Song song) {
		return mConnection.getBoolean("AddToPlayList", song.path + ";" + PLAYLIST_ID);
	}
	
	/**
	 * Returns how many items are in the playlist.
	 * @return
	 */
	public int getPlaylistSize() {
		return mConnection.getInt("GetPlaylistLength", PLAYLIST_ID);
	}
	
	/**
	 * Clears current playlist
	 * @return true on success, false otherwise.
	 */
	public boolean clearPlaylist() {
		return mConnection.getBoolean("ClearPlayList", PLAYLIST_ID);
	}
	
	/**
	 * Plays an album. Playlist is previously cleared.
	 * @param album
	 * @return true on success, false otherwise.
	 */
	public boolean play(Album album) {
		final ArrayList<Song> songs = getSongs(album);
		clearPlaylist();
		int n = 0;
		for (Song song : songs) {
			addToPlaylist(song);
			if (n == 0) {
				play(song);
			}
			n++;
		}
		return true;
	}
	
	/**
	 * Plays all songs from an artist. Playlist is previously cleared.
	 * @param artist
	 * @return true on success, false otherwise.
	 */
	public boolean play(Artist artist) {
		final ArrayList<Song> songs = getSongs(artist);
		clearPlaylist();
		int n = 0;
		for (Song song : songs) {
			addToPlaylist(song);
			if (n == 0) {
				play(song);
			}
			n++;
		}
		return true;
	}
	
	/**
	 * Plays songs of a genre from an artist. Playlist is previously cleared.
	 * @param artist
	 * @param genre
	 * @return true on success, false otherwise.
	 */
	public boolean play(Artist artist, Genre genre) {
		final ArrayList<Song> songs = getSongs(artist, genre);
		clearPlaylist();
		int n = 0;
		for (Song song : songs) {
			addToPlaylist(song);
			if (n == 0) {
				play(song);
			}
			n++;
		}
		return true;
	}
	
	/**
	 * Adds a song to the current playlist.
	 * @param song
	 * @return true on success, false otherwise.
	 */
	public boolean play(Song song) {
		return mConnection.getBoolean("PlayFile", song.path + ";" + PLAYLIST_ID);
	}
	
	/**
	 * Gets all albums from database
	 * @return All albums
	 */
	public ArrayList<Album> getAlbums() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT a.idAlbum, a.strAlbum, i.strArtist, a.iYear, (");
		sb.append("   SELECT p.strPath");
		sb.append("   FROM song AS s, path AS p");
		sb.append("   WHERE s.idPath = p.idPath");
		sb.append("   AND s.idAlbum = a.idAlbum");
		sb.append("   LIMIT 1");
		sb.append("  )");
		sb.append("  FROM album AS a, artist AS i");
		sb.append("  WHERE a.idArtist = i.idArtist");
		sb.append("  ORDER BY i.strArtist ASC");
//		sb.append("  LIMIT 300"); // let's keep it at 300 for now
		return parseAlbums(mConnection.query("QueryMusicDatabase", sb.toString()));
	}

	/**
	 * Gets all albums of an artist from database
	 * @return Albums with an artist
	 */
	public ArrayList<Album> getAlbums(Artist artist) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT DISTINCT a.idAlbum, a.strAlbum, i.strArtist, a.iYear, (");
		sb.append("   SELECT p.strPath");
		sb.append("   FROM song AS s, path AS p");
		sb.append("   WHERE s.idPath = p.idPath");
		sb.append("   AND s.idAlbum = a.idAlbum");
		sb.append("   LIMIT 1");
		sb.append("  )");
		sb.append("  FROM song AS s, album AS a, artist as i");
		sb.append("  WHERE s.idArtist = " + artist.id);
		sb.append("  AND s.idAlbum = a.idAlbum");
		sb.append("  AND i.idArtist = s.idArtist");
		sb.append("  ORDER BY a.strAlbum ASC");
		return parseAlbums(mConnection.query("QueryMusicDatabase", sb.toString()));
	}

	/**
	 * Gets all albums of with at least one song in a genre
	 * @return Albums of a genre
	 */
	public ArrayList<Album> getAlbums(Genre genre) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT DISTINCT alb.idAlbum, alb.strAlbum, art.strArtist, alb.iYear, (");
		sb.append("   SELECT p.strPath");
		sb.append("   FROM song AS s, path AS p");
		sb.append("   WHERE s.idPath = p.idPath");
		sb.append("   AND s.idAlbum = alb.idAlbum");
		sb.append("   LIMIT 1");
		sb.append("  )");
		sb.append("  FROM artist as art, album as alb");
		sb.append("  WHERE art.idArtist = alb.idArtist");
		sb.append("  AND (alb.idAlbum IN (");
		sb.append("        SELECT DISTINCT s.idAlbum");
		sb.append("        FROM exgenresong AS g, song AS s");
		sb.append("        WHERE g.idGenre = " + genre.id);
		sb.append("        AND g.idSong = s.idSong");
		sb.append("  ) OR alb.idAlbum IN (");
		sb.append("        SELECT DISTINCT idAlbum");
		sb.append("        FROM song");
		sb.append("        WHERE idGenre = " + genre.id);
		sb.append("  ))");
		sb.append("  ORDER BY alb.strAlbum");
		return parseAlbums(mConnection.query("QueryMusicDatabase", sb.toString()));
	}
	
	/**
	 * Gets all albums from database
	 * @return All albums
	 */
	public ArrayList<Artist> getArtists() {
		return parseArtists(mConnection.query("QueryMusicDatabase", "SELECT idArtist, strArtist FROM artist ORDER BY strArtist"));
	}

	/**
	 * Gets all artists with at least one song of a genre.
	 * @return Albums with a genre
	 */
	public ArrayList<Artist> getArtists(Genre genre) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT idArtist, strArtist FROM artist");
		sb.append("   WHERE idArtist IN (");
		sb.append("         SELECT DISTINCT s.idArtist");
		sb.append("         FROM exgenresong AS g, song AS s");
		sb.append("         WHERE g.idGenre = " + genre.id);
		sb.append("         AND g.idSong = s.idSong");
		sb.append("   ) OR idArtist IN (");
		sb.append("        SELECT DISTINCT idArtist");
		sb.append("         FROM song");
		sb.append("         WHERE idGenre = " + genre.id);
		sb.append("   )");
		sb.append("   ORDER BY strArtist");
		return parseArtists(mConnection.query("QueryMusicDatabase", sb.toString()));
	}
	
	/**
	 * Gets all genres from database
	 * @return All genres
	 */
	public ArrayList<Genre> getGenres() {
		return parseGenres(mConnection.query("QueryMusicDatabase", "SELECT idGenre, strGenre FROM genre ORDER BY strGenre"));
	}
	
	/**
	 * Updates the album object with additional data from the albuminfo table
	 * @param album
	 * @return Updated album
	 */
	public Album updateAlbumInfo(Album album) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT g.strGenre, a.strExtraGenres, ai.strLabel, ai.iRating");
		sb.append("  FROM album a, genre g");
		sb.append("  LEFT JOIN albuminfo AS ai ON ai.idAlbumInfo = a.idAlbum");
		sb.append("  WHERE a.idGenre = g.idGenre");
		sb.append("  AND a.idAlbum = " + album.id);
		return parseAlbumInfo(album, mConnection.query("QueryMusicDatabase", sb.toString()));
	}
	
	/**
	 * Returns a list containing all tracks of an album. The list is sorted by filename.
	 * @param album
	 * @return Tracks of an album
	 */
	public ArrayList<Song> getSongs(Album album) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT s.strTitle, art.StrArtist, alb.strAlbum, s.iTrack, s.iDuration, p.strPath, s.strFileName");
		sb.append("  FROM song AS s, path AS p, artist AS art, album AS alb");
		sb.append("  WHERE s.idPath = p.idPath");
		sb.append("  AND s.idArtist = art.idArtist");
		sb.append("  AND s.idAlbum = alb.idAlbum");
		sb.append("  AND s.idAlbum = " + album.id);
		sb.append("  ORDER BY s.iTrack, s.strFileName");
		return parseSongs(mConnection.query("QueryMusicDatabase", sb.toString()));
	}
	
	/**
	 * Returns a list containing all tracks of an artist. The list is sorted by album name, filename.
	 * @param artist Artist
	 * @return All tracks of the artist
	 */
	public ArrayList<Song> getSongs(Artist artist) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT s.strTitle, art.StrArtist, alb.strAlbum, s.iTrack, s.iDuration, p.strPath, s.strFileName");
		sb.append("  FROM song AS s, path AS p, artist art, album AS alb");
		sb.append("  WHERE s.idPath = p.idPath");
		sb.append("  AND s.idArtist = art.idArtist");
		sb.append("  AND s.idAlbum = alb.idAlbum");
		sb.append("  AND s.idArtist = " + artist.id);
		sb.append("  ORDER BY alb.strAlbum, s.iTrack, s.strFileName");
		return parseSongs(mConnection.query("QueryMusicDatabase", sb.toString()));
	}
	
	/**
	 * Returns a list containing all tracks of a genre. The list is sorted by artist, album name, filename.
	 * @param genre Genre
	 * @return All tracks of the genre
	 */
	public ArrayList<Song> getSongs(Genre genre) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT s.strTitle, art.StrArtist, alb.strAlbum, s.iTrack, s.iDuration, p.strPath, s.strFileName");
		sb.append("  FROM song AS s, path AS p, artist art, album AS alb");
		sb.append("  WHERE s.idPath = p.idPath");
		sb.append("  AND s.idArtist = art.idArtist");
		sb.append("  AND s.idAlbum = alb.idAlbum");
		sb.append("  AND s.idGenre = " + genre.id);
		sb.append("  ORDER BY art.StrArtist, alb.strAlbum, s.iTrack, s.strFileName");
		return parseSongs(mConnection.query("QueryMusicDatabase", sb.toString()));
	}
	
	/**
	 * Returns a list containing all tracks of a genre AND and artist. The list is sorted by 
	 * artist, album name, filename.
	 * @param genre Genre
	 * @return All tracks of the genre
	 */
	public ArrayList<Song> getSongs(Artist artist, Genre genre) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT s.strTitle, art.StrArtist, alb.strAlbum, s.iTrack, s.iDuration, p.strPath, s.strFileName");
		sb.append("  FROM song AS s, path AS p, artist art, album AS alb");
		sb.append("  WHERE s.idPath = p.idPath");
		sb.append("  AND s.idArtist = art.idArtist");
		sb.append("  AND s.idAlbum = alb.idAlbum");
		sb.append("  AND s.idGenre = " + genre.id);
		sb.append("  AND s.idArtist = " + artist.id);
		sb.append("  ORDER BY art.StrArtist, alb.strAlbum, s.iTrack, s.strFileName");
		return parseSongs(mConnection.query("QueryMusicDatabase", sb.toString()));
	}
	
	/**
	 * Returns album thumbnail as base64-encoded string
	 * @param album
	 * @return Base64-encoded content of thumb
	 */
	public String getAlbumThumb(ICoverArt art) {
		final String data = mConnection.query("FileDownload", Album.getThumbUri(art));
		if (data.length() > 0) {
			return data;
		} else {
			Log.i("MusicClient", "*** Downloaded cover has size null, retrying with fallback:");
			return mConnection.query("FileDownload", Album.getFallbackThumbUri(art));
		}
	}

	/**
	 * Converts query response from HTTP API to a list of Album objects. Each
	 * row must return the following attributes in the following order:
	 * <ol>
	 * 	<li><code>idAlbum</code></li>
	 * 	<li><code>strAlbum</code></li>
	 * 	<li><code>strArtist</code></li>
	 * 	<li><code>path to album</code></li>
	 * </ol>
	 * @param response
	 * @return List of albums
	 */
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
	}
	
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
	 */
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
		return album;
	}
	
	/**
	 * Converts query response from HTTP API to a list of Song objects. Each
	 * row must return the following columns in the following order:
	 * <ol>
	 * 	<li><code>strTitle</code></li>
	 * 	<li><code>strArtist</code></li>
	 * 	<li><code>iTrack</code></li>
	 * 	<li><code>iDuration</code></li>
	 * 	<li><code>strPath</code></li>
	 * 	<li><code>strFileName</code></li>
	 * </ol>
	 * @param response
	 * @return List of Songs
	 */
	private ArrayList<Song> parseSongs(String response) {
		ArrayList<Song> songs = new ArrayList<Song>();
		String[] fields = response.split("<field>");
		try { 
			for (int row = 1; row < fields.length; row += 7) { 
				songs.add(new Song( // String title, String artist, String album, int track, int duration, String path
						Connection.trim(fields[row]), 
						Connection.trim(fields[row + 1]), 
						Connection.trim(fields[row + 2]), 
						Connection.trimInt(fields[row + 3]), 
						Connection.trimInt(fields[row + 4]), 
						Connection.trim(fields[row + 5]),
						Connection.trim(fields[row + 6]) 
				));
			}
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println("response = " + response);
			e.printStackTrace();
		}
		Collections.sort(songs);
		return songs;		
	}
	
	/**
	 * Converts query response from HTTP API to a list of Artist objects. Each
	 * row must return the following columns in the following order:
	 * <ol>
	 * 	<li><code>idArtist</code></li>
	 * 	<li><code>strArtist</code></li>
	 * </ol>
	 * @param response
	 * @return List of Artists
	 */
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
		return artists;		
	}
	
	/**
	 * Converts query response from HTTP API to a list of Genre objects. Each
	 * row must return the following columns in the following order:
	 * <ol>
	 * 	<li><code>idGenre</code></li>
	 * 	<li><code>strGenre</code></li>
	 * </ol>
	 * @param response
	 * @return List of Genres
	 */
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
		return genres;		
	}
}

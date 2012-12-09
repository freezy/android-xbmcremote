package org.xbmc.android.remote.business.cm;

import java.util.ArrayList;
import java.util.List;

import org.xbmc.android.jsonrpc.api.AbstractCall;
import org.xbmc.android.jsonrpc.api.call.AudioLibrary;
import org.xbmc.android.jsonrpc.api.call.Player;
import org.xbmc.android.jsonrpc.api.call.Playlist;
import org.xbmc.android.jsonrpc.api.model.AudioModel.AlbumDetail;
import org.xbmc.android.jsonrpc.api.model.AudioModel.ArtistDetail;
import org.xbmc.android.jsonrpc.api.model.AudioModel.SongDetail;
import org.xbmc.android.jsonrpc.api.model.LibraryModel.GenreDetail;
import org.xbmc.android.jsonrpc.api.model.ListModel;
import org.xbmc.android.jsonrpc.api.model.ListModel.AlbumFilter;
import org.xbmc.android.jsonrpc.api.model.ListModel.AlbumFilterRule;
import org.xbmc.android.jsonrpc.api.model.ListModel.FilterRule.Value;
import org.xbmc.android.jsonrpc.api.model.ListModel.SongFilter;
import org.xbmc.android.jsonrpc.api.model.ListModel.SongFilterRule;
import org.xbmc.android.jsonrpc.api.model.PlaylistModel;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IMusicManager;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.business.ISortableManager;
import org.xbmc.api.object.Album;
import org.xbmc.api.object.Artist;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.object.Song;
import org.xbmc.httpapi.WifiStateException;

import android.content.Context;
import android.graphics.Bitmap;

public class MusicManager extends AbstractManager implements IMusicManager,
		ISortableManager, INotifiableManager {

	public void getCompilations(DataResponse<ArrayList<Album>> response,
			Context context) {

		List<AlbumFilter> artistFilters = new ArrayList<AlbumFilter>();
		// (String operator, Value value, String field)
		artistFilters.add(new AlbumFilter(
				new AlbumFilterRule("startswith", new Value("various artists"),
						AlbumFilterRule.Field.ALBUMARTIST)));
		artistFilters.add(new AlbumFilter(new AlbumFilterRule("startswith",
				new Value("v.a."), AlbumFilterRule.Field.ALBUMARTIST)));
		artistFilters.add(new AlbumFilter(new AlbumFilterRule("is", new Value(
				"va"), AlbumFilterRule.Field.ALBUMARTIST)));

		call(new AudioLibrary.GetAlbums(null, getSort(AlbumDetail.TITLE),
				new ListModel.AlbumFilter(new AlbumFilter.Or(artistFilters)),
				AlbumDetail.ARTIST, AlbumDetail.YEAR, AlbumDetail.THUMBNAIL),
				new ApiHandler<ArrayList<Album>, AlbumDetail>() {

					@Override
					public ArrayList<Album> handleResponse(
							AbstractCall<AlbumDetail> apiCall) {
						List<AlbumDetail> albumDetails = apiCall.getResults();

						ArrayList<Album> result = new ArrayList<Album>();
						for (AlbumDetail albumDetail : albumDetails) {
							result.add(new Album(albumDetail));
						}
						return result;
					}
				}, response, context);
	}

	public void getAlbums(final DataResponse<ArrayList<Album>> response,
			Context context) {

		call(new AudioLibrary.GetAlbums(null, getSort(AlbumDetail.TITLE),
				(ListModel.AlbumFilter) null, AlbumDetail.ARTIST,
				AlbumDetail.YEAR, AlbumDetail.THUMBNAIL),
				new ApiHandler<ArrayList<Album>, AlbumDetail>() {

					@Override
					public ArrayList<Album> handleResponse(
							AbstractCall<AlbumDetail> apiCall) {
						List<AlbumDetail> albumDetails = apiCall.getResults();

						ArrayList<Album> result = new ArrayList<Album>();
						for (AlbumDetail albumDetail : albumDetails) {
							result.add(new Album(albumDetail));
						}
						return result;
					}
				}, response, context);
	}

	public ArrayList<Album> getAlbums(Context context) {
		// FIXME: getcovers needs to work properly
		throw new RuntimeException(
				"Synchronous calls are incompatible with new API");
	}

	public void getAlbums(DataResponse<ArrayList<Album>> response,
			Artist artist, Context context) {
		call(new AudioLibrary.GetAlbums(null, getSort(AlbumDetail.TITLE),
				new AudioLibrary.GetAlbums.FilterArtistId(artist.getId()),
				AlbumDetail.ARTIST, AlbumDetail.YEAR, AlbumDetail.THUMBNAIL),
				new ApiHandler<ArrayList<Album>, AlbumDetail>() {

					@Override
					public ArrayList<Album> handleResponse(
							AbstractCall<AlbumDetail> apiCall) {
						List<AlbumDetail> albumDetails = apiCall.getResults();

						ArrayList<Album> result = new ArrayList<Album>();
						for (AlbumDetail albumDetail : albumDetails) {
							result.add(new Album(albumDetail));
						}
						return result;
					}
				}, response, context);
	}

	public void getAlbums(DataResponse<ArrayList<Album>> response, Genre genre,
			Context context) {
		call(new AudioLibrary.GetAlbums(null, getSort(AlbumDetail.TITLE),
				new AudioLibrary.GetAlbums.FilterGenreId(genre.getId()),
				AlbumDetail.ARTIST, AlbumDetail.YEAR, AlbumDetail.THUMBNAIL),
				new ApiHandler<ArrayList<Album>, AlbumDetail>() {

					@Override
					public ArrayList<Album> handleResponse(
							AbstractCall<AlbumDetail> apiCall) {
						List<AlbumDetail> albumDetails = apiCall.getResults();

						ArrayList<Album> result = new ArrayList<Album>();
						for (AlbumDetail albumDetail : albumDetails) {
							result.add(new Album(albumDetail));
						}
						return result;
					}
				}, response, context);
	}

	public void getSongs(DataResponse<ArrayList<Song>> response, Album album,
			Context context) {
		call(new AudioLibrary.GetSongs(null, getSort(SongDetail.TRACK),
				new AudioLibrary.GetSongs.FilterAlbumId(album.getId()),
				SongDetail.ARTIST, SongDetail.TITLE, SongDetail.ALBUM,
				SongDetail.TRACK, SongDetail.DISC, SongDetail.DURATION,
				SongDetail.FILE, SongDetail.THUMBNAIL),
				new ApiHandler<ArrayList<Song>, SongDetail>() {

					@Override
					public ArrayList<Song> handleResponse(
							AbstractCall<SongDetail> apiCall) {
						List<SongDetail> songDetails = apiCall.getResults();

						ArrayList<Song> result = new ArrayList<Song>();
						for (SongDetail songDetail : songDetails) {
							result.add(new Song(songDetail));
						}
						return result;
					}
				}, response, context);

	}

	public void getSongs(DataResponse<ArrayList<Song>> response, Artist artist,
			Context context) {
		call(new AudioLibrary.GetSongs(null, getSort(SongDetail.TRACK),
				new AudioLibrary.GetSongs.FilterArtistId(artist.getId()),
				SongDetail.ARTIST, SongDetail.TITLE, SongDetail.ALBUM,
				SongDetail.TRACK, SongDetail.DISC, SongDetail.DURATION,
				SongDetail.FILE, SongDetail.THUMBNAIL),
				new ApiHandler<ArrayList<Song>, SongDetail>() {

					@Override
					public ArrayList<Song> handleResponse(
							AbstractCall<SongDetail> apiCall) {
						List<SongDetail> songDetails = apiCall.getResults();

						ArrayList<Song> result = new ArrayList<Song>();
						for (SongDetail songDetail : songDetails) {
							result.add(new Song(songDetail));
						}
						return result;
					}
				}, response, context);
	}

	public void getSongs(DataResponse<ArrayList<Song>> response, Genre genre,
			Context context) {
		call(new AudioLibrary.GetSongs(null, getSort(SongDetail.TRACK),
				new AudioLibrary.GetSongs.FilterGenreId(genre.getId()),
				SongDetail.ARTIST, SongDetail.TITLE, SongDetail.ALBUM,
				SongDetail.TRACK, SongDetail.DISC, SongDetail.DURATION,
				SongDetail.FILE, SongDetail.THUMBNAIL),
				new ApiHandler<ArrayList<Song>, SongDetail>() {

					@Override
					public ArrayList<Song> handleResponse(
							AbstractCall<SongDetail> apiCall) {
						List<SongDetail> songDetails = apiCall.getResults();

						ArrayList<Song> result = new ArrayList<Song>();
						for (SongDetail songDetail : songDetails) {
							result.add(new Song(songDetail));
						}
						return result;
					}
				}, response, context);

	}

	public void getArtists(DataResponse<ArrayList<Artist>> response,
			Context context) {
		// FIXME: Parametize this
		Boolean albumArtistsOnly = Boolean.TRUE;
		call(new AudioLibrary.GetArtists(albumArtistsOnly, null,
				getSort(ArtistDetail.LABEL),
				(AudioLibrary.GetArtists.FilterGenreId) null,
				ArtistDetail.THUMBNAIL),
				new ApiHandler<ArrayList<Artist>, ArtistDetail>() {

					@Override
					public ArrayList<Artist> handleResponse(
							AbstractCall<ArtistDetail> apiCall) {
						List<ArtistDetail> artistDetails = apiCall.getResults();

						ArrayList<Artist> result = new ArrayList<Artist>();
						for (ArtistDetail artistDetail : artistDetails) {
							result.add(new Artist(artistDetail));
						}
						return result;
					}
				}, response, context);

	}

	public void getArtists(DataResponse<ArrayList<Artist>> response,
			Genre genre, Context context) {
		// FIXME: Parametize this
		Boolean albumArtistsOnly = Boolean.TRUE;
		call(new AudioLibrary.GetArtists(albumArtistsOnly, null,
				getSort(ArtistDetail.LABEL),
				new AudioLibrary.GetArtists.FilterGenreId(genre.getId()),
				ArtistDetail.THUMBNAIL),
				new ApiHandler<ArrayList<Artist>, ArtistDetail>() {

					@Override
					public ArrayList<Artist> handleResponse(
							AbstractCall<ArtistDetail> apiCall) {
						List<ArtistDetail> artistDetails = apiCall.getResults();

						ArrayList<Artist> result = new ArrayList<Artist>();
						for (ArtistDetail artistDetail : artistDetails) {
							result.add(new Artist(artistDetail));
						}
						return result;
					}
				}, response, context);
	}

	public void getGenres(DataResponse<ArrayList<Genre>> response,
			Context context) {
		call(new AudioLibrary.GetGenres(null, getSort(ArtistDetail.LABEL),
				ArtistDetail.THUMBNAIL),
				new ApiHandler<ArrayList<Genre>, GenreDetail>() {
					@Override
					public ArrayList<Genre> handleResponse(
							AbstractCall<GenreDetail> apiCall) {
						List<GenreDetail> genreDetails = apiCall.getResults();

						ArrayList<Genre> result = new ArrayList<Genre>();
						for (GenreDetail genreDetail : genreDetails) {
							result.add(new Genre(genreDetail));
						}
						return result;
					}
				}, response, context);

	}

	public void addToPlaylist(DataResponse<Boolean> response, Album album,
			Context context) {

		call(new Playlist.Add(PLAYLIST_MUSIC, new PlaylistModel.Item(
				new PlaylistModel.Item.Albumid(album.getId()))),
				new ApiHandler<Boolean, String>() {
					@Override
					public Boolean handleResponse(AbstractCall<String> apiCall) {
						return "OK".equals(apiCall.getResult());
					}
				}, response, context);
	}

	public void addToPlaylist(DataResponse<Boolean> response, Genre genre,
			Context context) {
		call(new Playlist.Add(PLAYLIST_MUSIC, new PlaylistModel.Item(
				new PlaylistModel.Item.Genreid(genre.getId()))),
				new ApiHandler<Boolean, String>() {
					@Override
					public Boolean handleResponse(AbstractCall<String> apiCall) {
						return "OK".equals(apiCall.getResult());
					}
				}, response, context);
	}

	public void addToPlaylist(DataResponse<Boolean> response, Song song,
			Context context) {
		call(new Playlist.Add(PLAYLIST_MUSIC, new PlaylistModel.Item(
				new PlaylistModel.Item.Songid(song.getId()))),
				new ApiHandler<Boolean, String>() {
					@Override
					public Boolean handleResponse(AbstractCall<String> apiCall) {
						return "OK".equals(apiCall.getResult());
					}
				}, response, context);
	}

	public void addToPlaylist(DataResponse<Boolean> response, Album album,
			Song song, Context context) {
		call(new Playlist.Add(PLAYLIST_MUSIC, new PlaylistModel.Item(
				new PlaylistModel.Item.Albumid(album.getId()))),
				new ApiHandler<Boolean, String>() {
					@Override
					public Boolean handleResponse(AbstractCall<String> apiCall) {
						return "OK".equals(apiCall.getResult());
					}
				}, response, context);

	}

	public void addToPlaylist(DataResponse<Boolean> response, Artist artist,
			Context context) {

		call(new Playlist.Add(PLAYLIST_MUSIC, new PlaylistModel.Item(
				new PlaylistModel.Item.Artistid(artist.getId()))),
				new ApiHandler<Boolean, String>() {
					@Override
					public Boolean handleResponse(AbstractCall<String> apiCall) {
						return "OK".equals(apiCall.getResult());
					}
				}, response, context);
	}

	public void addToPlaylist(final DataResponse<Boolean> response,
			Artist artist, Genre genre, final Context context) {

		List<SongFilter> songFilters = new ArrayList<SongFilter>();
		// SongFilterRule(String operator, Value value, String field)
		songFilters.add(new SongFilter(new SongFilterRule("is", new Value(
				Integer.toString(artist.getId())), "artist")));
		songFilters.add(new SongFilter(new SongFilterRule("is", new Value(
				Integer.toString(genre.getId())), "genre")));
		ListModel.SongFilter filter = new ListModel.SongFilter(
				new ListModel.SongFilter.And(songFilters));

		callRaw(new AudioLibrary.GetSongs(null, filter),
				new ApiHandler<Boolean, SongDetail>() {

					@Override
					public Boolean handleResponse(
							AbstractCall<SongDetail> apiCall) {
						List<SongDetail> songDetails = apiCall.getResults();

						int currentSong = 1;
						ArrayList<Song> result = new ArrayList<Song>();
						for (SongDetail songDetail : songDetails) {
							final boolean lastSong = currentSong == songDetails
									.size();
							callRaw(new Playlist.Add(PLAYLIST_MUSIC,
									new PlaylistModel.Item(
											new PlaylistModel.Item.Songid(
													songDetail.songid))),
									new ApiHandler<Boolean, String>() {
										@Override
										public Boolean handleResponse(
												AbstractCall<String> apiCall) {
											boolean result = "OK"
													.equals(apiCall.getResult());
											if (lastSong) {
												response.value = result;
												MusicManager.this
														.onFinish(response);
											}
											return result;
										}
									}, context);
							currentSong++;
						}

						return Boolean.TRUE;
					}
				}, context);
	}

	public void setPlaylistSong(DataResponse<Boolean> response, int position,
			Context context) {
		setPlaylist(PLAYLIST_MUSIC, response, position, context);
	}

	public void removeFromPlaylist(DataResponse<Boolean> response,
			int position, Context context) {
		removeFromPlaylist(PLAYLIST_MUSIC, response, position, context);
	}

	public void getPlaylist(DataResponse<ArrayList<String>> response,
			Context context) {
		getPlaylist(PLAYLIST_MUSIC, response, context);
	}

	public void getPlaylistPosition(DataResponse<Integer> response,
			Context context) {
		getPlaylistPosition(PLAYLIST_MUSIC, response, context);
	}

	public void play(DataResponse<Boolean> response, Album album,
			Context context) {
		call(new Player.Open(new PlaylistModel.Item(
				new PlaylistModel.Item.Albumid(album.getId()))),
				new ApiHandler<Boolean, String>() {
					@Override
					public Boolean handleResponse(AbstractCall<String> apiCall) {
						return "OK".equals(apiCall.getResult());
					}
				}, response, context);
	}

	public void play(DataResponse<Boolean> response, Genre genre,
			Context context) {
		call(new Player.Open(new PlaylistModel.Item(
				new PlaylistModel.Item.Genreid(genre.getId()))),
				new ApiHandler<Boolean, String>() {
					@Override
					public Boolean handleResponse(AbstractCall<String> apiCall) {
						return "OK".equals(apiCall.getResult());
					}
				}, response, context);

	}

	public void play(DataResponse<Boolean> response, Song song, Context context) {
		call(new Player.Open(new PlaylistModel.Item(
				new PlaylistModel.Item.Songid(song.getId()))),
				new ApiHandler<Boolean, String>() {
					@Override
					public Boolean handleResponse(AbstractCall<String> apiCall) {
						return "OK".equals(apiCall.getResult());
					}
				}, response, context);

	}

	public void play(final DataResponse<Boolean> response, final Album album,
			final Song song, final Context context) {

		callRaw(new AudioLibrary.GetSongs(null, getSort(SongDetail.TRACK),
				new AudioLibrary.GetSongs.FilterAlbumId(album.getId()),
				SongDetail.ARTIST, SongDetail.TITLE, SongDetail.ALBUM,
				SongDetail.TRACK, SongDetail.DISC, SongDetail.DURATION,
				SongDetail.FILE, SongDetail.THUMBNAIL),
				new ApiHandler<Boolean, SongDetail>() {

					@Override
					public Boolean handleResponse(
							AbstractCall<SongDetail> apiCall) {
						List<SongDetail> songDetails = apiCall.getResults();

						int songCount = 0;
						boolean foundStart = false;
						for (SongDetail songDetail : songDetails) {
							songCount++;
							if (songDetail.songid == song.getId()) {
								callRaw(new Player.Open(new PlaylistModel.Item(
										new PlaylistModel.Item.Songid(
												songDetail.songid))),
										new ApiHandler<Boolean, String>() {
											@Override
											public Boolean handleResponse(
													AbstractCall<String> apiCall) {
												return "OK".equals(apiCall
														.getResult());
											}
										}, context);
								foundStart = true;
							} else if (foundStart
									&& songDetails.size() == songCount) {
								call(new Playlist.Add(PLAYLIST_MUSIC,
										new PlaylistModel.Item(
												new PlaylistModel.Item.Songid(
														songDetail.songid))),
										new ApiHandler<Boolean, String>() {
											@Override
											public Boolean handleResponse(
													AbstractCall<String> apiCall) {
												return "OK".equals(apiCall
														.getResult());
											}
										}, response, context);

							} else if (foundStart) {
								callRaw(new Playlist.Add(PLAYLIST_MUSIC,
										new PlaylistModel.Item(
												new PlaylistModel.Item.Songid(
														songDetail.songid))),
										new ApiHandler<Boolean, String>() {
											@Override
											public Boolean handleResponse(
													AbstractCall<String> apiCall) {
												return "OK".equals(apiCall
														.getResult());
											}
										}, context);
							}
						}
						return Boolean.TRUE;
					}
				}, context);

	}

	public void play(DataResponse<Boolean> response, Artist artist,
			Context context) {
		call(new Player.Open(new PlaylistModel.Item(
				new PlaylistModel.Item.Artistid(artist.getId()))),
				new ApiHandler<Boolean, String>() {
					@Override
					public Boolean handleResponse(AbstractCall<String> apiCall) {
						return "OK".equals(apiCall.getResult());
					}
				}, response, context);
	}

	public void play(final DataResponse<Boolean> response, Artist artist,
			Genre genre, final Context context) {

		List<SongFilter> songFilters = new ArrayList<SongFilter>();
		// SongFilterRule(String operator, Value value, String field)
		songFilters.add(new SongFilter(new SongFilterRule("is", new Value(
				Integer.toString(artist.getId())), "artist")));
		songFilters.add(new SongFilter(new SongFilterRule("is", new Value(
				Integer.toString(genre.getId())), "genre")));
		ListModel.SongFilter filter = new ListModel.SongFilter(
				new ListModel.SongFilter.And(songFilters));

		callRaw(new AudioLibrary.GetSongs(null, filter),
				new ApiHandler<Boolean, SongDetail>() {

					@Override
					public Boolean handleResponse(
							AbstractCall<SongDetail> apiCall) {
						List<SongDetail> songDetails = apiCall.getResults();

						int currentSong = 1;
						ArrayList<Song> result = new ArrayList<Song>();
						for (SongDetail songDetail : songDetails) {
							final boolean lastSong = currentSong == songDetails
									.size();
							if (currentSong == 1) {
								callRaw(new Player.Open(new PlaylistModel.Item(
										new PlaylistModel.Item.Songid(
												songDetail.songid))),
										new ApiHandler<Boolean, String>() {
											@Override
											public Boolean handleResponse(
													AbstractCall<String> apiCall) {
												boolean result = "OK"
														.equals(apiCall
																.getResult());
												if (lastSong) {
													response.value = result;
													MusicManager.this
															.onFinish(response);
												}
												return result;
											}
										}, context);

								continue;
							}
							callRaw(new Playlist.Add(PLAYLIST_MUSIC,
									new PlaylistModel.Item(
											new PlaylistModel.Item.Songid(
													songDetail.songid))),
									new ApiHandler<Boolean, String>() {
										@Override
										public Boolean handleResponse(
												AbstractCall<String> apiCall) {
											boolean result = "OK"
													.equals(apiCall.getResult());
											if (lastSong) {
												response.value = result;
												MusicManager.this
														.onFinish(response);
											}
											return result;
										}
									}, context);
							currentSong++;
						}

						return Boolean.TRUE;
					}
				}, context);
	}

	public void updateAlbumInfo(DataResponse<Album> response,
			final Album album, Context context) {
		call(new AudioLibrary.GetAlbumDetails(album.getId(), AlbumDetail.GENRE,
				AlbumDetail.RATING, AlbumDetail.YEAR),
				new ApiHandler<Album, AlbumDetail>() {
					@Override
					public Album handleResponse(
							AbstractCall<AlbumDetail> apiCall) {
						AlbumDetail detail = apiCall.getResult();
						album.genres = detail.genre;
						album.rating = detail.rating;
						album.year = detail.year;
						return album;
					}
				}, response, context);
	}

	public void updateArtistInfo(DataResponse<Artist> response,
			final Artist artist, Context context) {
		call(new AudioLibrary.GetArtistDetails(artist.getId(),
				ArtistDetail.BORN, ArtistDetail.FORMED, ArtistDetail.GENRE,
				ArtistDetail.MOOD, ArtistDetail.STYLE, ArtistDetail.DESCRIPTION),
				new ApiHandler<Artist, ArtistDetail>() {
					@Override
					public Artist handleResponse(
							AbstractCall<ArtistDetail> apiCall) {
						ArtistDetail detail = apiCall.getResult();
						artist.born = detail.born;
						artist.formed = detail.formed;
						artist.genres = detail.genre;
						artist.moods = detail.mood;
						artist.styles = detail.style;
						artist.biography = detail.description;
						return artist;
					}
				}, response, context);
	}

	public void downloadCover(DataResponse<Bitmap> response, ICoverArt cover,
			int thumbSize, Context context) throws WifiStateException {
		if (cover instanceof Album) {
			response.value = getCover(cover, thumbSize,
					Album.getThumbUri(cover), Album.getFallbackThumbUri(cover));
			return;
		} else if (cover instanceof Artist) {
			response.value = getCover(cover, thumbSize,
					Artist.getThumbUri(cover),
					Artist.getFallbackThumbUri(cover));
			return;
		}
		throw new RuntimeException("Unsupported cover type");

	}

}

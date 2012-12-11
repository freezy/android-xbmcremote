package org.xbmc.android.remote.business.cm;

import java.util.ArrayList;
import java.util.List;

import org.xbmc.android.jsonrpc.api.AbstractCall;
import org.xbmc.android.jsonrpc.api.call.AudioLibrary;
import org.xbmc.android.jsonrpc.api.call.Player;
import org.xbmc.android.jsonrpc.api.call.Player.GetActivePlayers;
import org.xbmc.android.jsonrpc.api.call.Player.GetActivePlayers.GetActivePlayersResult;
import org.xbmc.android.jsonrpc.api.call.Player.Seek;
import org.xbmc.android.jsonrpc.api.call.Playlist;
import org.xbmc.android.jsonrpc.api.call.VideoLibrary;
import org.xbmc.android.jsonrpc.api.model.GlobalModel.Time;
import org.xbmc.android.jsonrpc.api.model.ListModel;
import org.xbmc.android.jsonrpc.api.model.ListModel.AllItems;
import org.xbmc.android.jsonrpc.api.model.ListModel.BaseItem;
import org.xbmc.android.jsonrpc.api.model.PlayerModel;
import org.xbmc.android.jsonrpc.api.model.PlayerModel.PropertyValue;
import org.xbmc.android.jsonrpc.api.model.PlaylistModel;
import org.xbmc.android.util.StringUtil;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.data.IControlClient;
import org.xbmc.api.data.IControlClient.ICurrentlyPlaying;
import org.xbmc.api.info.PlayStatus;
import org.xbmc.api.type.MediaType;
import org.xbmc.api.type.SeekType;

import android.content.Context;
import android.util.Log;

public class ControlManager extends AbstractManager implements IControlManager {

	public void playFile(DataResponse<Boolean> response, String filename,
			Context context) {
		call(new Player.Open(new PlaylistModel.Item(
				new PlaylistModel.Item.File(filename))),
				new ApiHandler<Boolean, String>() {
					@Override
					public Boolean handleResponse(AbstractCall<String> apiCall) {
						return "OK".equals(apiCall.getResult());
					}
				}, response, context);
	}

	public void playFolder(DataResponse<Boolean> response, final String foldername,
			String playlistType, Context context) {
		call(new Player.Open(new PlaylistModel.Item(
				new PlaylistModel.Item.Directory(foldername))),
				new ApiHandler<Boolean, String>() {
					@Override
					public Boolean handleResponse(AbstractCall<String> apiCall) {
						return "OK".equals(apiCall.getResult());
					}
				}, response, context);


	}

	public void queueFolder(DataResponse<Boolean> response, String foldername,
			String playlistType, Context context) {
		call(new Playlist.Add(Integer.parseInt(playlistType), new PlaylistModel.Item(
				new PlaylistModel.Item.Directory(foldername))),
				new ApiHandler<Boolean, String>() {
					@Override
					public Boolean handleResponse(AbstractCall<String> apiCall) {
						return "OK".equals(apiCall.getResult());
					}
				}, response, context);


	}

	public void playUrl(DataResponse<Boolean> response, String url,
			Context context) {
		playFile(response, url, context);
	}

	public void seek(final DataResponse<Boolean> response, SeekType type,
			final int progress, final Context context) {

		callRaw(new Player.GetActivePlayers(),
				new ApiHandler<Integer, GetActivePlayers.GetActivePlayersResult>() {
					@Override
					public Integer handleResponse(
							AbstractCall<GetActivePlayersResult> apiCall) {
						ArrayList<GetActivePlayersResult> results = apiCall
								.getResults();
						if (results.size() == 0) {
							return 0;
						}
						GetActivePlayersResult result = results.get(results.size() - 1);
						final int playerid = result.playerid;
						call(new Player.Seek(playerid, (double) progress),
								new ApiHandler<Boolean, Seek.SeekResult>() {
									@Override
									public Boolean handleResponse(
											AbstractCall<Seek.SeekResult> apiCall) {

										// Seek.SeekResult result =
										// apiCall.getResule();
										// result.
										// TODO: This could update the position
										// directly
										return Boolean.TRUE;
									}
								}, response, context);
						return result.playerid;
					}
				}, context);

	}

	public void updateLibrary(DataResponse<Boolean> response, String mediaType,
			Context context) {

		if ("music".equals(mediaType)) {
			call(new AudioLibrary.Scan(""), new ApiHandler<Boolean, String>() {

				@Override
				public Boolean handleResponse(AbstractCall<String> apiCall) {
					return "OK".equals(apiCall.getResult());
				}
			}, response, context);

		} else if ("pictures".equals(mediaType)) {
			
			//?

			
		} else if ("video".equals(mediaType)) {
			call(new VideoLibrary.Scan(""), new ApiHandler<Boolean, String>() {

				@Override
				public Boolean handleResponse(AbstractCall<String> apiCall) {
					return "OK".equals(apiCall.getResult());
				}
			}, response, context);

		}

	}

	public void showPicture(DataResponse<Boolean> response, String filename,
			Context context) {
		playFile(response, filename, context);
	}

	public void getCurrentlyPlaying(
			final DataResponse<ICurrentlyPlaying> response,
			final Context context) {

		callRaw(new Player.GetActivePlayers(),
				new ApiHandler<Integer, GetActivePlayers.GetActivePlayersResult>() {
					@Override
					public Integer handleResponse(
							AbstractCall<GetActivePlayersResult> apiCall) {
						ArrayList<GetActivePlayersResult> results = apiCall
								.getResults();
						if (results.size() == 0) {
							return 0;
						}
						GetActivePlayersResult result = results.get(results.size() - 1);
						final int playerid = result.playerid;
						callRaw(new Player.GetProperties(playerid, "time",
								"speed", "position", "percentage"),
								new ApiHandler<Boolean, PlayerModel.PropertyValue>() {
									@Override
									public Boolean handleResponse(
											AbstractCall<PropertyValue> apiCall) {
										final PropertyValue propertyValue = apiCall
												.getResult();
										call(new Player.GetItem(playerid,
												BaseItem.ALBUMARTIST,
												BaseItem.FILE,
												BaseItem.DURATION,
												BaseItem.ARTIST,
												BaseItem.ALBUM,
												BaseItem.THUMBNAIL,
												BaseItem.SHOWTITLE,
												BaseItem.SEASON,
												BaseItem.EPISODE,
												BaseItem.FANART,
												BaseItem.RUNTIME,
												BaseItem.TAGLINE,
												BaseItem.TITLE,
												BaseItem.GENRE),
												new ApiHandler<ICurrentlyPlaying, ListModel.AllItems>() {
													@Override
													public ICurrentlyPlaying handleResponse(
															AbstractCall<AllItems> apiCall) {

														return getCurrentlyPlaying(
																apiCall.getResult(),
																propertyValue);

													}
												}, response, context);
										return Boolean.TRUE;
									}

								}, context);

						return result.playerid;
					}
				}, context);

	}

	private ICurrentlyPlaying getCurrentlyPlaying(
			final ListModel.AllItems item, final PropertyValue propertyValue) {
		if (item == null)
			return IControlClient.NOTHING_PLAYING;
		if (item.file != null && item.file.contains("Nothing Playing")) {
			return IControlClient.NOTHING_PLAYING;
		} else {
			return new IControlClient.ICurrentlyPlaying() {
				private static final long serialVersionUID = 5036994329211476714L;

				public String getTitle() {
					if ("song".equals(item.type)) {
						return item.label;
					} else if ("episode".equals(item.type)) {
						return item.showtitle;
					} else if ("movie".equals(item.type)) {
						return item.title;
					}
					String[] path = item.file.split("/");
					return path[path.length - 1];
				}

				public int getTime() {
					return parseTime(propertyValue.time);
				}

				public int getPlayStatus() {
					return PlayStatus.parse(propertyValue.speed);
				}

				public int getPlaylistPosition() {
					return propertyValue.position;
				}

				public float getPercentage() {
					return propertyValue.percentage.floatValue();
				}

				public String getFilename() {
					return item.file;
				}

				public int getDuration() {
					return item.runtime;
				}

				public String getArtist() {
					if ("song".equals(item.type)) {
						List<String> albumArtist = item.albumartist;
						if (albumArtist.size() > 0) {
							return albumArtist.get(0);
						}
						if (item.artist.size() > 0) {
							return item.artist.get(0);
						}
					} else if ("episode".equals(item.type)) {
						if (Integer.valueOf(item.season) == 0) {
							return "Specials / Episode " + item.episode;
						} else {
							return "Season " + item.season + " / Episode "
									+ item.episode;
						}
					} else if ("movie".equals(item.type)) {
						return StringUtil.join(" / ", item.genre);
					} else if ("picture".equals(item.type)) {
						return "Image";
					}
					return "";
				}

				public String getAlbum() {
					if ("song".equals(item.type)) {
						return item.album;
					} else if ("episode".equals(item.type)) {
						return item.title;
					} else if ("movie".equals(item.type)) {
						String title = item.tagline;
						if (title != null) {
							return title;
						}
					}
					String[] path = item.file.replaceAll("\\\\", "/").split("/");
					return path[path.length - 2];
				}

				public int getMediaType() {
					return MediaType.MUSIC;
				}

				public boolean isPlaying() {
					return propertyValue.speed > 0;
				}

				public int getHeight() {
					return 0;
				}

				public int getWidth() {
					return 0;
				}

				private int parseTime(Time time) {
					int hours = time.hours;
					// int milliseconds =
					// time.get("milliseconds").getIntValue();
					int minutes = time.minutes;
					int seconds = time.seconds;

					return (hours * 3600) + (minutes * 60) + seconds;
				}

				public String getThumbnail() {
					return item.thumbnail;
				}
				
				public String getFanart() {
					return item.fanart;
				}
			};

		}
	}

	/**
	 * getPlaylistId only returns Music or Video currently so it's useful for playlist management
	 */
	public void getPlaylistId(DataResponse<Integer> response, Context context) {
		call(new Player.GetActivePlayers(),
				new ApiHandler<Integer, GetActivePlayers.GetActivePlayersResult>() {
					@Override
					public Integer handleResponse(
							AbstractCall<GetActivePlayersResult> apiCall) {
						ArrayList<GetActivePlayersResult> results = apiCall
								.getResults();
						if (results.size() == 0) {
							return 0;
						}
						GetActivePlayersResult result = results.get(0);
						return result.playerid;
					}
				}, response, context);
	}

	public void setPlaylistId(DataResponse<Boolean> response, int id,
			Context context) {
		response.value = Boolean.TRUE;
		onFinish(response);
	}

	public void setPlaylistPos(final DataResponse<Boolean> response, final int position,
			final Context context) {
		getPlaylistId(new DataResponse<Integer>() {
			@Override
			public void run() {
				call(new Player.GoTo(value, position),
						new ApiHandler<Boolean, String>() {
							@Override
							public Boolean handleResponse(AbstractCall<String> apiCall) {
								return "OK".equals(apiCall.getResult());
							}
						}, response, context);
			}
		}, context);
	}

	public void clearPlaylist(DataResponse<Boolean> response,
			String playlistId, Context context) {
		call(new Playlist.Clear(Integer.parseInt(playlistId)),
				new ApiHandler<Boolean, String>() {
					@Override
					public Boolean handleResponse(AbstractCall<String> apiCall) {
						return "OK".equals(apiCall.getResult());
					}
				}, response, context);
	}

	public void setGuiSetting(DataResponse<Boolean> response, int setting,
			String value, Context context) {
		// TODO Auto-generated method stub

	}

	public void getVolume(DataResponse<Integer> response, Context context) {
		// TODO Auto-generated method stub

	}

	public void sendText(DataResponse<Boolean> response, String text,
			Context context) {
		// TODO Auto-generated method stub

	}

}

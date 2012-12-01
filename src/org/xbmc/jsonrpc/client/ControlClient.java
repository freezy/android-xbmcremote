package org.xbmc.jsonrpc.client;

import org.codehaus.jackson.JsonNode;
import org.xbmc.android.jsonrpc.api.call.Playlist;
import org.xbmc.android.jsonrpc.api.model.PlaylistModel.Item;
import org.xbmc.android.jsonrpc.api.model.PlaylistModel.Item.File;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IControlClient;
import org.xbmc.api.info.PlayStatus;
import org.xbmc.api.type.SeekType;
import org.xbmc.jsonrpc.Connection;

import android.util.Log;

public class ControlClient extends Client implements IControlClient {

	public ControlClient(Connection connection) {
		super(connection);
	}

	public boolean addToPlaylist(INotifiableManager manager, String fileOrFolder) {
		return mConnection.getBoolean(manager, "Playlist.Add", obj().p("playlistid", 0).p("item", obj().p("file", fileOrFolder)));
	}

	public boolean playFile(INotifiableManager manager, String filename) {
		JsonNode status = mConnection.getJson(manager, "Player.Open", obj().p("item", obj().p("file", filename)));
		return "OK".equals(status.getTextValue());
	}

	public boolean playNext(INotifiableManager manager) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean playPrevious(INotifiableManager manager) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean pause(INotifiableManager manager) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean stop(INotifiableManager manager) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean playUrl(INotifiableManager manager, String url) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean setVolume(INotifiableManager manager, int volume) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean seek(INotifiableManager manager, SeekType type, int progress) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean sendText(INotifiableManager manager, String text) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean mute(INotifiableManager manager) {
		// TODO Auto-generated method stub
		return false;
	}

	public int getPercentage(INotifiableManager manager) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getVolume(INotifiableManager manager) {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean navUp(INotifiableManager manager) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean navDown(INotifiableManager manager) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean navLeft(INotifiableManager manager) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean navRight(INotifiableManager manager) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean navSelect(INotifiableManager manager) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean updateLibrary(INotifiableManager manager, String mediaType) {
		Log.d("ControlClient", "Scanning audio library");
		mConnection.getJson(manager, "AudioLibrary.Scan");
		Log.d("ControlClient", "Scanning video library");
		mConnection.getJson(manager, "VideoLibrary.Scan");
		return true;
	}

	public boolean showPicture(INotifiableManager manager, String filename) {
		return playFile(manager, filename);
	}

	public boolean broadcast(INotifiableManager manager, String message) {
		// TODO Auto-generated method stub
		return false;
	}

	public int getBroadcast(INotifiableManager manager) {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean setBroadcast(INotifiableManager manager, int port, int level) {
		// TODO Auto-generated method stub
		return false;
	}

	public int getPlayState(INotifiableManager manager) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getPlaylistId(INotifiableManager manager) {
		return 0;
	}

	public boolean setCurrentPlaylist(INotifiableManager manager,
			String playlistId) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean setPlaylistId(INotifiableManager manager, int id) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean setPlaylistPos(INotifiableManager manager, int position) {
		Integer player = getActivePlayerId(manager);
		if(player == null) {
			play(manager);
			player = getActivePlayerId(manager);
		}
		
		mConnection.getJson(manager, "Player.GoTo", obj().p("position", position).p("playerid", player));
		
		return false;
	}
	
	public int play(INotifiableManager manager) {
		// this should probably choose the playlist based upon the manager type
		Integer player = getActivePlayerId(manager);
		if(player == null) {
			player = Client.PLAYLIST_MUSIC;
		}
		
		
		JsonNode status = mConnection.getJson(manager, "Player.PlayPause", obj().p("playerid", player));
		Log.e("ControlClient", status.toString());
		return PlayStatus.parse(player, status.getIntValue());
	}

	public boolean clearPlaylist(INotifiableManager manager, String playlistId) {
		
		return mConnection.getBoolean(manager, "Playlist.Clear", obj().p("playlistid", playlistId));
	}

	public ICurrentlyPlaying getCurrentlyPlaying(INotifiableManager manager) {
		
//		JsonNode player = getActivePlayer(manager);
//		if(player == null) {
//			return NOTHING_PLAYING;
//		}
//		Integer playerid = player.get("playerid").getIntValue();
//		JsonNode result = mConnection.getJson(manager, "Player.GetItem", obj().p("playerid", playerid).p("properties", arr().add("album").add("albumartist").add("duration").add("file").add("thumbnail").add("tagline").add("artist")));
//		if(result == null) {
//			return NOTHING_PLAYING;
//		}
//		
//		JsonNode item = result.get("item");
//		if(item == null) {
//			return NOTHING_PLAYING;
//		}
//		
//		// currently streams don't work properly
//		JsonNode file = item.get("file");
//		if(file == null) {
//			return PLAYING_UNKNOWN;
//		}
//		
//		String type = player.get("type").getTextValue();
//		
//		
//	    if("audio".equals(type)) {
//	    	JsonNode properties = mConnection.getJson(manager, "Player.GetProperties", obj().p("playerid", playerid).p("properties", arr().add("time").add("speed").add("position").add("percentage")));
//	    	return MusicClient.getCurrentlyPlaying(playerid, item, properties);
//	    }
//	    else if("video".equals(type)) {
//	    	JsonNode properties = mConnection.getJson(manager, "Player.GetProperties", obj().p("playerid", playerid).p("properties", arr().add("time").add("speed").add("position").add("percentage")));
//	    	return VideoClient.getCurrentlyPlaying(playerid, item, properties);
//	    }
//	    else if("picture".equals(type)) {
//	    	return PictureClient.getCurrentlyPlaying(playerid, item, obj());
//	    }
	    return NOTHING_PLAYING;
	}
	
	public boolean setGuiSetting(INotifiableManager manager, int setting,
			String value) {
		// TODO Auto-generated method stub
		return false;
	}

}

package org.xbmc.jsonrpc.client;

import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IControlClient;
import org.xbmc.api.object.Host;
import org.xbmc.api.type.SeekType;
import org.xbmc.jsonrpc.Connection;

public class ControlClient extends Client implements IControlClient {

	public ControlClient(Connection connection) {
		super(connection);
	}

	public void setHost(Host host) {
		// TODO Auto-generated method stub
		
	}

	public boolean addToPlaylist(INotifiableManager manager, String fileOrFolder) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean playFile(INotifiableManager manager, String filename) {
		// TODO Auto-generated method stub
		return false;
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
		// TODO Auto-generated method stub
		return false;
	}

	public boolean showPicture(INotifiableManager manager, String filename) {
		// TODO Auto-generated method stub
		return false;
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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return false;
	}

	public boolean clearPlaylist(INotifiableManager manager, String playlistId) {
		// TODO Auto-generated method stub
		return false;
	}

	public ICurrentlyPlaying getCurrentlyPlaying(INotifiableManager manager) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean setGuiSetting(INotifiableManager manager, int setting,
			String value) {
		// TODO Auto-generated method stub
		return false;
	}

}

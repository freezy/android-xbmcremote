package org.xbmc.jsonrpc.client;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IInfoClient;
import org.xbmc.api.object.FileLocation;
import org.xbmc.api.object.Host;
import org.xbmc.api.type.DirectoryMask;
import org.xbmc.jsonrpc.Connection;

/**
 * The InfoClient basically takes care of everything else not covered by the
 * other clients (music, video and control). That means its tasks are bound to
 * system related stuff like directory listing and so on. 
 * 
 * @author Team XBMC
 */
public class InfoClient implements IInfoClient {
	
	private final Connection mConnection;
	
	/**
	 * Class constructor needs reference to HTTP client connection
	 * @param connection
	 */
	public InfoClient(Connection connection) {
		mConnection = connection;
	}

	/**
	 * Updates host info on the connection.
	 * @param host
	 */
	public void setHost(Host host) {
		mConnection.setHost(host);
	}
	
	/**
	 * Returns the contents of a directory
	 * @param path    Path to the directory
	 * @param mask    Mask to filter
	 * @param offset  Offset (0 for none)
	 * @param limit   Limit (0 for none)
	 * @return
	 */
	public ArrayList<FileLocation> getDirectory(INotifiableManager manager, String path, DirectoryMask mask, int offset, int limit) {
/*		final ArrayList<FileLocation> directories = new ArrayList<FileLocation>();
		try {
			final JSONObject result = mConnection.getJson(manager, "Files.GetDirectory", new JSONObject().put("type", "files").put("directory", path));
			final JSONArray jsonShares = result.getJSONArray("directories");
			for (int i = 0; i < jsonShares.length(); i++) {
				JSONObject jsonShare = (JSONObject)jsonShares.get(i);
				directories.add(new FileLocation(jsonShare.getString("label"), jsonShare.getString("file")));
			}
			return directories;
		} catch (JSONException e) {
			manager.onError(e);
		}
		return directories;*/
		return null;
	}
	
	/**
	 * Returns all the contents of a directory
	 * @param path    Path to the directory
	 * @return
	 */
	public ArrayList<FileLocation> getDirectory(INotifiableManager manager, String path) {
		return getDirectory(manager, path, null, 0, 0);
	}

	
	/**
	 * Returns all defined shares of a media type
	 * @param mediaType Media type
	 * @return
	 */
	public ArrayList<FileLocation> getShares(INotifiableManager manager, int mediaType) {
/*		final ArrayList<FileLocation> shares = new ArrayList<FileLocation>();
		try {
			final JSONObject result = mConnection.getJson(manager, "Files.GetShares", new JSONObject().put("type", "video"));
			final JSONArray jsonShares = result.getJSONArray("shares");
			for (int i = 0; i < jsonShares.length(); i++) {
				JSONObject jsonShare = (JSONObject)jsonShares.get(i);
				shares.add(new FileLocation(jsonShare.getString("label"), jsonShare.getString("file")));
			}
			return shares;
		} catch (JSONException e) {
			manager.onError(e);
		}
		return shares;*/
		return null;
	}
	
	/**
	 * @TODO Implement for JSON-RPC
	 */
	public String getCurrentlyPlayingThumbURI(INotifiableManager manager) throws MalformedURLException, URISyntaxException {
		/*
		ArrayList<String> array = mConnection.getArray(manager, "GetCurrentlyPlaying", "");
		for (String s : array) {
			if (s.startsWith("Thumb")) {
				return mConnection.getUrl("FileDownload", s.substring(6));
			}
		}*/
		return null;
	}
	
	/**
	 * Returns any system info variable, see {@link org.xbmc.api.info.SystemInfo}
	 * @TODO Wait for JSON-RPC implementation
	 * @param field Field to return
	 * @return
	 */
	public String getSystemInfo(INotifiableManager manager, int field) {
		return mConnection.getString(manager, "JSONRPC.Version", "version");
	}
	
	/**
	 * Returns a boolean GUI setting
	 * @TODO Wait for JSON-RPC implementation
	 * @param field
	 * @return
	 */
	public boolean getGuiSettingBool(INotifiableManager manager, int field) {
		//return mConnection.getBoolean(manager, "GetGuiSetting", GuiSettings.MusicLibrary.getType(field) + ";" + GuiSettings.MusicLibrary.getName(field));
		return false;
	}

	/**
	 * Returns an integer GUI setting
	 * @TODO Wait for JSON-RPC implementation
	 * @param field
	 * @return
	 */
	public int getGuiSettingInt(INotifiableManager manager, int field) {
		//return mConnection.getInt(manager, "GetGuiSetting", GuiSettings.MusicLibrary.getType(field) + ";" + GuiSettings.MusicLibrary.getName(field));
		return 0;
	}
	
	/**
	 * Returns any music info variable see {@link org.xbmc.http.info.MusicInfo}
	 * @TODO Wait for JSON-RPC implementation
	 * @param field Field to return
	 * @return
	 */
	public String getMusicInfo(INotifiableManager manager, int field) {
		//return mConnection.getString(manager, "GetMusicLabel", String.valueOf(field));
		return "";
	}

	/**
	 * Returns any video info variable see {@link org.xbmc.http.info.VideoInfo}
	 * @TODO Wait for JSON-RPC implementation
	 * @param field Field to return
	 * @return
	 */
	public String getVideoInfo(INotifiableManager manager, int field) {
		//return mConnection.getString(manager, "GetVideoLabel", String.valueOf(field));
		return "";
	}
}
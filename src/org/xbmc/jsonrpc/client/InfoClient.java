package org.xbmc.jsonrpc.client;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;

import org.codehaus.jackson.JsonNode;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IInfoClient;
import org.xbmc.api.object.FileLocation;
import org.xbmc.api.object.Host;
import org.xbmc.api.type.DirectoryMask;
import org.xbmc.api.type.MediaType;
import org.xbmc.jsonrpc.Connection;

/**
 * The InfoClient basically takes care of everything else not covered by the
 * other clients (music, video and control). That means its tasks are bound to
 * system related stuff like directory listing and so on. 
 * 
 * @author Team XBMC
 */
public class InfoClient extends Client implements IInfoClient {
	
	/**
	 * Class constructor needs reference to HTTP client connection
	 * @param connection
	 */
	public InfoClient(Connection connection) {
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
	 * Returns the contents of a directory
	 * @param path    Path to the directory
	 * @param mask    Mask to filter
	 * @param offset  Offset (0 for none)
	 * @param limit   Limit (0 for none)
	 * @return
	 */
	public ArrayList<FileLocation> getDirectory(INotifiableManager manager, String path, DirectoryMask mask, int offset, int limit,  final int mMediaType) {
		final ArrayList<FileLocation> dirs = new ArrayList<FileLocation>();
		final JsonNode jsonDirs = mConnection.getJson(manager, "Files.GetDirectory", obj().put("type", "files").put("directory", path), "directories");
		for (Iterator<JsonNode> i = jsonDirs.getElements(); i.hasNext();) {
			JsonNode jsonDir = (JsonNode)i.next();
			dirs.add(new FileLocation(getString(jsonDir, "label"), getString(jsonDir, "file")));
		}
		return dirs;
	}
	
	/**
	 * Returns all the contents of a directory
	 * @param path    Path to the directory
	 * @return
	 */
	public ArrayList<FileLocation> getDirectory(INotifiableManager manager, String path, int mMediaType) {
		return getDirectory(manager, path, null, 0, 0, mMediaType);
	}

	
	/**
	 * Returns all defined shares of a media type
	 * @param mediaType Media type
	 * @return
	 */
	public ArrayList<FileLocation> getShares(INotifiableManager manager, int mediaType) {
		final ArrayList<FileLocation> shares = new ArrayList<FileLocation>();
		final JsonNode jsonShares = mConnection.getJson(manager, "Files.GetSources", obj().put("type", "video")).get("shares");
		for (Iterator<JsonNode> i = jsonShares.getElements(); i.hasNext();) {
			JsonNode jsonShare = (JsonNode)i.next();
			shares.add(new FileLocation(getString(jsonShare, "label"), getString(jsonShare, "file")));
		}
		return shares;
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
	 * Returns a boolean GUI setting
	 * @param field
	 * @param value Value
	 * @return
	 */
	public boolean setGuiSettingBool(INotifiableManager manager, int field, boolean value) {
		// return mConnection.getBoolean(manager, "SetGuiSetting", GuiSettings.getType(field) + ";" + GuiSettings.getName(field) + ";" + value);
		return false;
	}
	
	/**
	 * Returns an integer GUI setting
	 * @param field
	 * @param value Value
	 * @return
	 */
	public boolean setGuiSettingInt(INotifiableManager manager, int field, int value) {
		// return mConnection.getBoolean(manager, "SetGuiSetting", GuiSettings.getType(field) + ";" + GuiSettings.getName(field) + ";" + value);
		return false;
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
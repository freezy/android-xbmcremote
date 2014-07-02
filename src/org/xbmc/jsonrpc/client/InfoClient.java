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
import org.xbmc.api.type.SortType;
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
		final JsonNode jsonDirs = mConnection.getJson(manager, "Files.GetDirectory", sort(obj().p("media", "files").p("directory", path), SortType.ALBUM, "descending"), "files");
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
		final JsonNode jsonShares = mConnection.getJson(manager, "Files.GetSources", obj().p("media", MediaType.getName(mediaType)));
		if(jsonShares != null && jsonShares.get("sources") != null) {
			for (Iterator<JsonNode> i = jsonShares.get("sources").getElements(); i.hasNext();) {
				JsonNode jsonShare = (JsonNode)i.next();
				shares.add(new FileLocation(getString(jsonShare, "label"), getString(jsonShare, "file")));
			}
		}
		return shares;
	}
	
	/**
	 * @TODO Implement for JSON-RPC
	 */
	public String getCurrentlyPlayingThumbURI(INotifiableManager manager) throws MalformedURLException, URISyntaxException {
		int playerid = getActivePlayerId(manager);
		if(playerid == -1)
			return null;
		
		final JsonNode item = mConnection.getJson(manager, "Player.GetItem", obj().p("playerid", playerid).p(PARAM_PROPERTIES, arr().add("thumbnail"))).get("item");

		JsonNode dl = null;
		if(getString(item, "thumbnail") != null && !getString(item, "thumbnail").equals(""))
			dl = mConnection.getJson(manager, "Files.PrepareDownload", obj().p("path", getString(item, "thumbnail")));
		if(dl != null){
			JsonNode details = dl.get("details");
			if(details != null)
				return mConnection.getUrl(getString(details, "path"));
			else
				return null;
		}
		else
			return null;		
	}
	
	/**
	 * Returns any system info variable, see {@link org.xbmc.api.info.SystemInfo}
	 * @TODO Wait for JSON-RPC implementation
	 * @param field Field to return
	 * @return
	 */
	public String getSystemInfo(INotifiableManager manager, int field) {
		JsonNode version = mConnection.getJson(manager, "Application.GetProperties", obj().p(PARAM_PROPERTIES, arr().add("version"))).get("version");
		return getInt(version, "major") + "." + getInt(version, "minor") + " " + getString(version, "tag") + "\nGit: " + getString(version, "revision"); 
	}
	
	/**
	 * Returns a boolean GUI setting
	 * @TODO Wait for JSON-RPC implementation
	 * @param field
	 * @return
	 */
	public boolean getGuiSettingBool(INotifiableManager manager, int field) {
		//TODO
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
		//TODO
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
		//TODO
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
		//TODO 
		//return mConnection.getBoolean(manager, "SetGuiSetting", GuiSettings.getType(field) + ";" + GuiSettings.getName(field) + ";" + value);
		return false;
	}
	
	/**
	 * Returns any music info variable see {@link org.xbmc.http.info.MusicInfo}
	 * @TODO Wait for JSON-RPC implementation
	 * @param field Field to return
	 * @return
	 */
	public String getMusicInfo(INotifiableManager manager, int field) {
		//TODO
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
		//TODO
		//return mConnection.getString(manager, "GetVideoLabel", String.valueOf(field));
		return "";
	}
}
package org.xbmc.jsonrpc.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;

import org.codehaus.jackson.JsonNode;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IInfoClient;
import org.xbmc.api.object.FileLocation;
import org.xbmc.api.type.DirectoryMask;
import org.xbmc.api.type.MediaType;
import org.xbmc.api.type.Sort;
import org.xbmc.jsonrpc.Connection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

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
	 * 
	 * @param connection
	 */
	public InfoClient(Connection connection) {
		super(connection);
	}

	/**
	 * Returns the contents of a directory
	 * 
	 * @param path
	 *            Path to the directory
	 * @param mask
	 *            Mask to filter
	 * @param offset
	 *            Offset (0 for none)
	 * @param limit
	 *            Limit (0 for none)
	 * @return
	 */
	public ArrayList<FileLocation> getDirectory(INotifiableManager manager,
			String path, DirectoryMask mask, int offset, int limit,
			final int mMediaType, Sort sort) {
		final ArrayList<FileLocation> dirs = new ArrayList<FileLocation>();
		final JsonNode jsonDirs = mConnection.getJson(
				manager,
				"Files.GetDirectory",
				sort(obj().p("media", "files").p("directory", path),
						sort), "files");
		for (Iterator<JsonNode> i = jsonDirs.getElements(); i.hasNext();) {
			JsonNode jsonDir = (JsonNode) i.next();
			dirs.add(new FileLocation(getString(jsonDir, "label"), getString(
					jsonDir, "file")));
		}
		return dirs;*/
	}

	/**
	 * Returns all the contents of a directory
	 * 
	 * @param path
	 *            Path to the directory
	 * @return
	 */
	public ArrayList<FileLocation> getDirectory(INotifiableManager manager,
			String path, int mMediaType, Sort sort) {
		return getDirectory(manager, path, null, 0, 0, mMediaType, sort);
	}

	/**
	 * Returns all defined shares of a media type
	 * 
	 * @param mediaType
	 *            Media type
	 * @return
	 */
	public ArrayList<FileLocation> getShares(INotifiableManager manager,
			int mediaType) {
		String media = "music";
		switch (mediaType) {
		case MediaType.VIDEO:
			media = "video";
			break;
		case MediaType.PICTURES:
			media = "pictures";
			break;
		}

		final ArrayList<FileLocation> shares = new ArrayList<FileLocation>();
		final JsonNode jsonShares = mConnection.getJson(manager,
				"Files.GetSources", obj().put("media", media)).get("sources");
		for (Iterator<JsonNode> i = jsonShares.getElements(); i.hasNext();) {
			JsonNode jsonShare = (JsonNode) i.next();
			shares.add(new FileLocation(getString(jsonShare, "label"),
					getString(jsonShare, "file")));
		}
		return shares;*/
	}

	/**
	 * @TODO Implement for JSON-RPC
	 */
	public String getCurrentlyPlayingThumbURI(INotifiableManager manager)
			throws MalformedURLException, URISyntaxException {
		Integer player = getActivePlayerId(manager);
		if (player == null) {
			return null;
		}

		JsonNode result = mConnection.getJson(manager, "Player.GetItem", obj()
				.p("playerid", player).p("properties", arr().add("thumbnail")));
		JsonNode item = result.get("item");
		if (item == null) {
			return null;
		}
		String specialPath = item.get("thumbnail").getTextValue();
		return mConnection.getVfsPath(specialPath);
	}

	/**
	 * Returns any system info variable, see
	 * {@link org.xbmc.api.info.SystemInfo}
	 * 
	 * @TODO Wait for JSON-RPC implementation
	 * @param field
	 *            Field to return
	 * @return
	 */
	public String getSystemVersion(INotifiableManager manager) {
		// get and cache the api version while we're here
		getAPIVersion(manager);
		
		ObjNode obj = obj().p("properties", arr().add("version"));
		JsonNode result = mConnection.getJson(manager, "Application.GetProperties", obj);
		JsonNode version = result.get("version");
		if(version == null) {
			return "Unknown";
		}
		return version.get("major").getValueAsText() + "." + version.get("minor").getValueAsText() + " " + version.get("revision").getValueAsText();
	}
	
	public int getAPIVersion(INotifiableManager manager) {
		return super.getAPIVersion(manager);
	}

	/**
	 * Returns a boolean GUI setting
	 * 
	 * @TODO Wait for JSON-RPC implementation
	 * @param field
	 * @return
	 */
	public boolean getGuiSettingBool(INotifiableManager manager, int field) {
		// return mConnection.getBoolean(manager, "GetGuiSetting",
		// GuiSettings.MusicLibrary.getType(field) + ";" +
		// GuiSettings.MusicLibrary.getName(field));
		return false;
	}

	/**
	 * Returns an integer GUI setting
	 * 
	 * @TODO Wait for JSON-RPC implementation
	 * @param field
	 * @return
	 */
	public int getGuiSettingInt(INotifiableManager manager, int field) {
		// return mConnection.getInt(manager, "GetGuiSetting",
		// GuiSettings.MusicLibrary.getType(field) + ";" +
		// GuiSettings.MusicLibrary.getName(field));
		return 0;
	}

	/**
	 * Returns a boolean GUI setting
	 * 
	 * @param field
	 * @param value
	 *            Value
	 * @return
	 */
	public boolean setGuiSettingBool(INotifiableManager manager, int field,
			boolean value) {
		// return mConnection.getBoolean(manager, "SetGuiSetting",
		// GuiSettings.getType(field) + ";" + GuiSettings.getName(field) + ";" +
		// value);
		return false;
	}

	/**
	 * Returns an integer GUI setting
	 * 
	 * @param field
	 * @param value
	 *            Value
	 * @return
	 */
	public boolean setGuiSettingInt(INotifiableManager manager, int field,
			int value) {
		// return mConnection.getBoolean(manager, "SetGuiSetting",
		// GuiSettings.getType(field) + ";" + GuiSettings.getName(field) + ";" +
		// value);
		return false;
	}

	/**
	 * Returns any music info variable see {@link org.xbmc.http.info.MusicInfo}
	 * 
	 * @TODO Wait for JSON-RPC implementation
	 * @param field
	 *            Field to return
	 * @return
	 */
	public String getMusicInfo(INotifiableManager manager, int field) {
		// return mConnection.getString(manager, "GetMusicLabel",
		// String.valueOf(field));
		return "";
	}

	/**
	 * Returns any video info variable see {@link org.xbmc.http.info.VideoInfo}
	 * 
	 * @TODO Wait for JSON-RPC implementation
	 * @param field
	 *            Field to return
	 * @return
	 */
	public String getVideoInfo(INotifiableManager manager, int field) {
		// return mConnection.getString(manager, "GetVideoLabel",
		// String.valueOf(field));
		return "";
	}

	public Bitmap download(String downloadURI) throws MalformedURLException,
			URISyntaxException, IOException {
		return BitmapFactory.decodeStream(mConnection.getInputStream(downloadURI));
	}

}

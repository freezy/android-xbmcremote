package org.xbmc.httpapi.client;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.xbmc.android.util.ErrorHandler;
import org.xbmc.api.data.IInfoClient;
import org.xbmc.api.object.FileLocation;
import org.xbmc.httpapi.Connection;
import org.xbmc.httpapi.info.GuiSettings;
import org.xbmc.httpapi.type.DirectoryMask;
import org.xbmc.httpapi.type.MediaType;

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
	 * Returns the contents of a directory
	 * @param path    Path to the directory
	 * @param mask    Mask to filter
	 * @param offset  Offset (0 for none)
	 * @param limit   Limit (0 for none)
	 * @return
	 */
	public ArrayList<FileLocation> getDirectory(String path, DirectoryMask mask, int offset, int limit) {
		final ArrayList<String> result = mConnection.getArray("GetDirectory", 
			path + ";" +
			(mask != null ? mask.toString() : " ") + ";" + 
			(offset > 0 ? offset : " ") + ";" +
			(limit > 0 ? limit : " ")
		);
		final ArrayList<FileLocation> files = new ArrayList<FileLocation>();
		for (String file : result) {
			files.add(new FileLocation(file));
		}
		return files;
	}
	
	/**
	 * Returns all the contents of a directory
	 * @param path    Path to the directory
	 * @return
	 */
	public ArrayList<FileLocation> getDirectory(String path) {
		return getDirectory(path, null, 0, 0);
	}

	
	/**
	 * Returns all defined shares of a media type
	 * @param mediaType Media type
	 * @return
	 */
	public ArrayList<FileLocation> getShares(int mediaType) {
		final ArrayList<String> result = mConnection.getArray("GetShares", MediaType.getName(mediaType));
		final ArrayList<FileLocation> shares = new ArrayList<FileLocation>();
		for (String share : result) {
			shares.add(new FileLocation(share));
		}
		return shares;
	}
	
	public String getCurrentlyPlayingThumbURI() throws MalformedURLException, URISyntaxException {
		ArrayList<String> array = mConnection.getArray("GetCurrentlyPlaying", "");
		for (String s : array) {
			if (s.startsWith("Thumb")) {
				return mConnection.generateQuery("FileDownload", s.substring(6));
			}
		}
		return null;
	}
	
	/**
	 * Returns any system info variable, see {@link org.xbmc.httpapi.info.SystemInfo}
	 * @param field Field to return
	 * @return
	 */
	public String getSystemInfo(int field) {
		if(mConnection.isConnected())
			return mConnection.getString("GetSystemInfo", String.valueOf(field));
		
		new ErrorHandler().handle(new ConnectException() );
		return "";
	}
	
	/**
	 * Returns a boolean GUI setting
	 * @param field
	 * @return
	 */
	public boolean getGuiSettingBool(int field) {
		return mConnection.getBoolean("GetGuiSetting", GuiSettings.MusicLibrary.getType(field) + ";" + GuiSettings.MusicLibrary.getName(field));
	}

	/**
	 * Returns an integer GUI setting
	 * @param field
	 * @return
	 */
	public int getGuiSettingInt(int field) {
		return mConnection.getInt("GetGuiSetting", GuiSettings.MusicLibrary.getType(field) + ";" + GuiSettings.MusicLibrary.getName(field));
	}
	
	/**
	 * Returns any music info variable see {@link org.xbmc.http.info.MusicInfo}
	 * @param field Field to return
	 * @return
	 */
	public String getMusicInfo(int field) {
		return mConnection.getString("GetMusicLabel", String.valueOf(field));
	}

	/**
	 * Returns any video info variable see {@link org.xbmc.http.info.VideoInfo}
	 * @param field Field to return
	 * @return
	 */
	public String getVideoInfo(int field) {
		return mConnection.getString("GetVideoLabel", String.valueOf(field));
	}
}
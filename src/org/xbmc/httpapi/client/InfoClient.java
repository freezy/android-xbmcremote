package org.xbmc.httpapi.client;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import org.xbmc.httpapi.Connection;
import org.xbmc.httpapi.data.MediaLocation;
import org.xbmc.httpapi.type.DirectoryMask;
import org.xbmc.httpapi.type.MediaType;

/**
 * The InfoClient basically takes care of everything else not covered by the
 * other clients (music, video and control). That means its tasks are bound to
 * system related stuff like directory listing and so on. 
 * 
 * @author Team XBMC
 */
public class InfoClient {
	
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
	public ArrayList<MediaLocation> getDirectory(String path, DirectoryMask mask, int offset, int limit) {
		final ArrayList<String> result = mConnection.getArray("GetDirectory", 
			path + ";" +
			(mask != null ? mask.toString() : " ") + ";" + 
			(offset > 0 ? offset : " ") + ";" +
			(limit > 0 ? limit : " ")
		);
		final ArrayList<MediaLocation> files = new ArrayList<MediaLocation>();
		for (String file : result) {
			files.add(new MediaLocation(file));
		}
		return files;
	}
	
	/**
	 * Returns all the contents of a directory
	 * @param path    Path to the directory
	 * @return
	 */
	public ArrayList<MediaLocation> getDirectory(String path) {
		return getDirectory(path, null, 0, 0);
	}

	
	/**
	 * Returns all defined shares of a media type
	 * @param type Media type
	 * @return
	 */
	public ArrayList<MediaLocation> getShares(MediaType type) {
		final ArrayList<String> result = mConnection.getArray("GetShares", type.toString());
		final ArrayList<MediaLocation> shares = new ArrayList<MediaLocation>();
		for (String share : result) {
			shares.add(new MediaLocation(share));
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
		return mConnection.getString("GetSystemInfo", String.valueOf(field));
	}

	public String getMusicInfo(int field) {
		return mConnection.getString("GetMusicLabel", String.valueOf(field));
	}

}
package org.xbmc.httpapi.client;

import java.util.ArrayList;

import org.xbmc.httpapi.Connection;
import org.xbmc.httpapi.data.MediaLocation;
import org.xbmc.httpapi.type.DirectoryMask;
import org.xbmc.httpapi.type.MediaType;

public class InfoClient {
	
	private final Connection mConnection;
	
	/**
	 * Class constructor needs reference to HTTP client connection
	 * @param connection
	 */
	public InfoClient(Connection connection) {
		mConnection = connection;
	}
	
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
	
	
	public ArrayList<MediaLocation> getDirectory(String path) {
		return getDirectory(path, null, 0, 0);
	}

	
	public ArrayList<MediaLocation> getShares(MediaType type) {
		final ArrayList<String> result = mConnection.getArray("GetShares", type.toString());
		final ArrayList<MediaLocation> shares = new ArrayList<MediaLocation>();
		for (String share : result) {
			shares.add(new MediaLocation(share));
		}
		return shares;
	}
	
	
	public String getSystemInfo(int field) {
		return mConnection.getString("GetSystemInfo", String.valueOf(field));
	}

}
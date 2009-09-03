package org.xbmc.httpapi.data;

import org.xbmc.httpapi.Connection;

public class MediaLocation {
	public String name, path;
	public boolean isDirectory;
	
	/**
	 * Class constructor with already parsed data
	 * @param name Display name
	 * @param path Path incl filename
	 */
	public MediaLocation(String name, String path) {
		this.name = name;
		this.path = path;
		this.isDirectory = path.endsWith("/") || path.endsWith("\\");
	}
	
	/**
	 * Parses name and path from raw line.
	 * @param line raw line, either path only ort name and path, separated by Connection.VALUE_SEP.
	 */
	public MediaLocation(String line) {
		if (line.endsWith(".m3u\\") || line.endsWith(".m3u/")) {
			line = line.substring(0, line.length() - 1);
		}
		if (line.contains(Connection.VALUE_SEP)) {
			final String[] s = line.split(Connection.VALUE_SEP);
			name = s[0];
			path = s[1];
			isDirectory = path.endsWith("/") || path.endsWith("\\");
		} else {
			String trimmed = line.replaceAll("\\\\", "/"); // path without trailing "/"
			isDirectory = trimmed.endsWith("/");
			path = line;
			if (isDirectory) {
				trimmed = trimmed.substring(0, trimmed.lastIndexOf("/")); 
				name = trimmed.substring(trimmed.lastIndexOf("/") + 1);
			} else {
				name = trimmed.substring(trimmed.lastIndexOf("/") + 1);
			}
		}
	}
}
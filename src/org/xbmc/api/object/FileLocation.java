package org.xbmc.api.object;

import java.net.URLDecoder;

import org.xbmc.httpapi.Connection;

public class FileLocation implements INamedResource {
	
	public String name, path, displayPath;
	public boolean isDirectory;
	public boolean isArchive = false;
	public boolean isMultipath = false;
	
	/**
	 * Class constructor with already parsed data
	 * @param name Display name
	 * @param path Path incl filename
	 */
	public FileLocation(String name, String path) {
		this.name = name;
		this.path = path;
		isDirectory = path.endsWith("/") || path.endsWith("\\");
		isArchive = isDirectory && path.startsWith("rar://") || path.startsWith("zip://");
		isMultipath = path.startsWith("multipath://");
		
		if (isArchive || isMultipath) {
			displayPath = URLDecoder.decode(path).replaceAll("\\\\", "/");
		} else {
			displayPath = path.replaceAll("\\\\", "/");
		}
	}
	
	public String getShortName(){
		return this.name;
	}
	
	/**
	 * Parses name and path from raw line.
	 * @param line raw line, either path only or name and path, separated by Connection.VALUE_SEP.
	 */
	public FileLocation(String line) {
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
			}
			name = trimmed.substring(trimmed.lastIndexOf("/") + 1);
		}
		if (path.startsWith("rar://") || path.startsWith("zip://")) {
			final String decoded;
			if (isDirectory) {
				decoded = URLDecoder.decode(path.substring(0, path.length() - 1)).replaceAll("\\\\", "/");
				isArchive = true;
			} else {
				decoded = URLDecoder.decode(path).replaceAll("\\\\", "/");
				isArchive = false;
			}
			name = decoded.substring(decoded.lastIndexOf("/") + 1);
			displayPath = URLDecoder.decode(path).replaceAll("\\\\", "/");

		} else if (path.startsWith("multipath://")) {
			displayPath = URLDecoder.decode(path).replaceAll("\\\\", "/");
			isMultipath = true;
			
		} else {
			displayPath = path.replaceAll("\\\\", "/");
		}
		
	}
}
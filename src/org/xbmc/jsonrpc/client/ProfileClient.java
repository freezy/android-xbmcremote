/*
 *      Copyright (C) 2005-2009 Team XBMC
 *      http://xbmc.org
 *
 *  This Program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2, or (at your option)
 *  any later version.
 *
 *  This Program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XBMC Remote; see the file license.  If not, write to
 *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *  http://www.gnu.org/copyleft/gpl.html
 *
 */

package org.xbmc.jsonrpc.client;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;

import org.codehaus.jackson.JsonNode;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IProfileClient;
import org.xbmc.api.object.Host;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.object.Profile;
import org.xbmc.jsonrpc.Connection;
import android.graphics.Bitmap;

/**
 * Takes care of everything related to the profile database.
 * 
 * @author Team XBMC
 */
public class ProfileClient extends Client implements IProfileClient {
	
	/**
	 * Class constructor needs reference to HTTP client connection
	 * @param connection
	 */
	public ProfileClient(Connection connection) {
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
	 * Gets all profiles from database
	 * @return All profiles
	 */
	public ArrayList<Profile> getProfiles(INotifiableManager manager) {
		ObjNode obj = obj().p(PARAM_PROPERTIES, arr().add("thumbnail").add("lockmode"));
		
		final ArrayList<Profile> profiles = new ArrayList<Profile>();
		final JsonNode result = mConnection.getJson(manager, "Profiles.GetProfiles", obj);
		final JsonNode jsonProfiles = result.get("profiles");
		if(jsonProfiles != null){
			for (Iterator<JsonNode> i = jsonProfiles.getElements(); i.hasNext();) {
				JsonNode jsonProfile = (JsonNode)i.next();
				
				profiles.add(new Profile(
					getInt(jsonProfile, "movieid"),
					getString(jsonProfile, "label"),
					getInt(jsonProfile, "lockmode"),
					getString(jsonProfile, "thumbnail")
				));
			}
		}
		return profiles;
	}
	
	/**
	 * Gets the current active profile
	 * @return The current active profile
	 */
	public String getCurrentProfile(INotifiableManager manager) {
		final ObjNode obj = obj().p(PARAM_PROPERTIES, arr());
		final JsonNode result = mConnection.getJson(manager, "Profiles.GetCurrentProfile", obj);
		return getString(result, "label");
	}

	/**
	 * Loads a new profile
	 * @param profileName The new profile name
	 * @param profilePassword The profile password
	 * @return True on success, false otherwise.
	 */
	public boolean loadProfile(INotifiableManager manager, String profileName, String password) {
		ObjNode obj = obj().p("profile", profileName).p("prompt", false);
		if (password != null) {
			obj = obj.p("password", obj().p("value", md5(password)).p("encryption", "md5"));
		}
		return mConnection.getString(manager, "Profiles.LoadProfile", obj).equals("OK");
	}

	/**
	 * Returns a pre-resized profile cover. Pre-resizing is done in a way that
	 * the bitmap at least as large as the specified size but not larger than
	 * the double.
	 * @param manager Postback manager
	 * @param cover Cover object
	 * @param size Minmal size to pre-resize to.
	 * @return Thumbnail bitmap
	 */
	public Bitmap getCover(INotifiableManager manager, ICoverArt cover, int size) {
		String url = null;
		if(Profile.getThumbUri(cover) != ""){
			final JsonNode dl = mConnection.getJson(manager, "Files.PrepareDownload", obj().p("path", Profile.getThumbUri(cover)));
			if(dl != null){
				JsonNode details = dl.get("details");
				if(details != null)
					url = mConnection.getUrl(getString(details, "path"));
			}
		}
		return getCover(manager, cover, size, url);
	}
	
	/**
	 * Converts string to an MD5 string
	 * @param s A string to convert
	 * @return The converted string
	 */
	private static String md5(String s) {
	    try {
	        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
	        digest.update(s.getBytes());
	        byte messageDigest[] = digest.digest();

	        return bytesToHex(messageDigest);

	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    return "";
	}
	
	/**
	 * Converts list of bytes to HEX string
	 * @param bytes A list of bytes to convert
	 * @return The converted list of bytes
	 */
	private static String bytesToHex(byte[] bytes) {
		final char[] hexArray = "0123456789ABCDEF".toCharArray();
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
}

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

package org.xbmc.httpapi;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.PriorityQueue;

import org.xbmc.android.util.Base64;
import org.xbmc.httpapi.type.LogType;

import android.content.Context;

class HttpApiConnection {
	
	private String mBaseURL;
	private PriorityQueue<Message> mMessenger;
	
	public HttpApiConnection(String host, int port, String username, String password, PriorityQueue<Message> messenger) {
		mBaseURL = "http://";
		if (username != null) {
			mBaseURL += username + (password != null ? ":" + password : "") + "@";
		}
		mBaseURL += host;
		if (port > 0)
			mBaseURL += ":" + port;
		mMessenger = messenger;
	}

	private URL formatQueryString(String method, String parameter) throws MalformedURLException, URISyntaxException {
		String encodedParameter = URLEncoder.encode(parameter);
		return new URL(mBaseURL + "/xbmcCmds/xbmcHttp?command=" + method + "(" + encodedParameter + ")");
	}
	
	public ArrayList<String> getList(String method, String parameter) {
		try {
			String response = executeCommand(method, parameter);
			System.out.println("<response>\n" + response + "\n</response>");
			return parseList(response);
		} catch (IOException e) {
			return new ArrayList<String>();
		}
	}
	
	private ArrayList<String> parseList(String response) {
		if (response == null || response.length() == 0 || response.contains("Error")) {
			mMessenger.offer(new Message(LogType.error, "ERROR in response"));
			return new ArrayList<String>();
		}
		
		String[] rows = response.split("<li>");
		
		ArrayList<String> returnList = new ArrayList<String>();
		
		for (String row : rows) {
			System.out.println("parsing: " + row);
			String verify = row.trim().toLowerCase();
			if (verify.length() > 0 && !verify.contains("error"))
				returnList.add(row.trim());
		}
		return returnList;
	}
	
	public boolean isAvailable() {
		return true;
	}
	

	public String getString(String method) {
		return getString(method, "");
	}
	
	public String getString(String method, String parameter) {
		try {
			String result = executeCommand(method, parameter);
			if (result.startsWith("<html>")) {
				return result.substring(6, result.length() - 6 - 7);
			} else {
				return result;
			}
		} catch (IOException e) {
			return "";
		} 
//		ArrayList<String> stringList = getList(method, parameter);
//		return stringList.size() > 0 ? stringList.get(0) : "";
	}
	
	public int getInt(String method) {
		return getInt(method, "");
	}
	
	public int getInt(String method, String parameter) {
		try {
			return Integer.parseInt(getString(method, parameter));
		} catch (NumberFormatException e) {
			mMessenger.offer(new Message(LogType.warning, "Parse exception: " + e.getMessage()));
			return 0;
		}
	}
	
	public boolean executeBooleanResponseCommand(String method) {
		return executeBooleanResponseCommand(method, "");
	}
	
	public boolean executeBooleanResponseCommand(String method, String parameter) {
		try {
			String s = executeCommand(method, parameter);
			boolean b = s.matches("OK");
			return b;
		} catch (IOException e) {
			mMessenger.offer(new Message(LogType.error, e.getMessage()));
			return false;
		}
	}
	private String executeCommand(String method, String parameter) throws IOException {
		return executeCommand(method, parameter, null);
	}
	
	private String executeCommand(String method, String parameter, String dumpToFile) throws IOException {
		try {
			URL query = formatQueryString(method, parameter);
			URLConnection conn = query.openConnection();
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()), 8192);
			StringBuilder sb = new StringBuilder();
			String line = "";
			System.out.println("HTTP: start (" + query + ")");
			while ((line = rd.readLine()) != null) {    
				sb.append(line);
			}
			System.out.println("HTTP: end (" + query + ")");
			rd.close();
			String response = sb.toString();
			
			if (dumpToFile == null) {
				return response.replace("<html>", "").replace("</html>", "");
			} else {
				// for now we write to the internal media.
				// TODO look for sdcard and store it there (then we don't need the context anymore)
//				FileOutputStream wr = context.openFileOutput(dumpToFile, Context.MODE_WORLD_READABLE);
//				wr.write(Base64.decode(response.replace("<html>", "").replace("</html>", "")));
//				wr.close();
				return dumpToFile;
			}
			  
		} catch (MalformedURLException e) {
			mMessenger.offer(new Message(LogType.error, "Malformed URL Exception: " + e.getMessage()));
			throw new IOException("Malformed URL Exception " + e.getMessage());
		} catch (URISyntaxException e) {
			String message = "URL Encode Exception: " + e.getMessage();
			mMessenger.offer(new Message(LogType.error, message));
			throw new IOException("URL Encode Exception " + e.getMessage());
		}
	}
	
	/**
	 * Will download a given file from the XBMC server, this method will return a full array so it's not memory efficient on large files
	 * @param fileName to download
	 * @return location of the file saved (locally)
	 * @throws IOException
	 */
	public String download(String uri, String saveTo) {
//		try {
//			try {
//				long size = context.openFileInput(saveTo).getChannel().size();
//				if (size > 0) {
//					return saveTo;
//				} else {
//					return executeCommand("FileDownload", uri, saveTo);
//				}
				return null; // TODO fix
//			} catch (FileNotFoundException e) {
//				return executeCommand("FileDownload", uri, saveTo);
//			}
//		} catch (IOException e) {
//			return null;
//		}
	}
}

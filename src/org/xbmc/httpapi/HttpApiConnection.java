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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.PriorityQueue;

class HttpApiConnection {
	private String baseURL;
	private PriorityQueue<Message> messenger;
	
	public HttpApiConnection(String host, int port, String username, String password, PriorityQueue<Message> messenger) {
		baseURL = "http://";
		if (username != null) {
			baseURL += username + (password != null ? ":" + password : "") + "@";
		}
	
		baseURL += host;
		if (port > 0)
			baseURL += ":" + port;
		
		this.messenger = messenger;
	}

	private URL formatQueryString(String method, String parameter) throws MalformedURLException, URISyntaxException {
		String encodedParameter = URLEncoder.encode(parameter);
		return new URL(baseURL + "/xbmcCmds/xbmcHttp?command=" + method + "(" + encodedParameter + ")");
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
			messenger.offer(new Message(UrgancyLevel.error, "ERROR in response"));
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
		ArrayList<String> stringList = getList(method, parameter);
		
		return stringList.size() > 0 ? stringList.get(0) : "";
	}
	
	public int getInt(String method) {
		return getInt(method, "");
	}
	
	public int getInt(String method, String parameter) {
		try {
			return Integer.parseInt(getString(method, parameter));
		} catch (NumberFormatException e) {
			messenger.offer(new Message(UrgancyLevel.warning, "Parse exception: " + e.getMessage()));
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
			messenger.offer(new Message(UrgancyLevel.error, e.getMessage()));
			return false;
		}
	}
	
	private String executeCommand(String method, String parameter) throws IOException {
		try {
			URL query = formatQueryString(method, parameter);
			URLConnection conn = query.openConnection();
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			  
			StringBuilder sb = new StringBuilder();
			String line = "";
			while ((line = rd.readLine()) != null)      
				sb.append(line);

			rd.close();
			String response = sb.toString();
			return response.replace("<html>", "").replace("</html>", "");
		} catch (MalformedURLException e) {
			messenger.offer(new Message(UrgancyLevel.error, "Malformed URL Exception: " + e.getMessage()));
			throw new IOException("Malformed URL Exception " + e.getMessage());
		} catch (URISyntaxException e) {
			String message = "URL Encode Exception: " + e.getMessage();
			messenger.offer(new Message(UrgancyLevel.error, message));
			throw new IOException("URL Encode Exception " + e.getMessage());
		}
	}
	
	/**
	 * Will download a given file on XBMC server, this method will return a full array so it's not memory efficient on large files
	 * @param fileName to download
	 * @return char array of file
	 * @throws IOException
	 */
	// TODO Test if this actually work
	public char[] FileDownload(String fileName) throws IOException {
		String data = executeCommand("FileDownload", fileName);
		return data.toCharArray();
	}
}

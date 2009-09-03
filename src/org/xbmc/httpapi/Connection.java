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
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Keeps track of the HTTP connection and contains basic helper methods for
 * repeated String transformations 
 */
public class Connection {
	
	public static final String LINE_SEP = "<li>";
	public static final String VALUE_SEP = ";";
	
	private static final int CONNECTION_TIMEOUT = 10000; // in milliseconds
	
	private static final String XBMC_HTTP_BOOTSTRAP =  "/xbmcCmds/xbmcHttp";
	private String mBaseURL;
	private IErrorHandler mErrorHandler;
	
	public Connection(String host, int port, String username, String password, IErrorHandler errorHandler) {
		mBaseURL = "http://";
		if (username != null) {
			mBaseURL += username + (password != null ? ":" + password : "") + "@";
		}
		mBaseURL += host;
		if (port != 80)
			mBaseURL += ":" + port;
		mErrorHandler = errorHandler;
//		setResponseFormat();
	}
	
	public String query(String method, String parameters) {
		try {
			final URL query = formatQueryString(method, parameters);
			final URLConnection uc = query.openConnection();
			uc.setConnectTimeout(CONNECTION_TIMEOUT);
			uc.setReadTimeout(CONNECTION_TIMEOUT);
			
			final BufferedReader rd = new BufferedReader(new InputStreamReader(uc.getInputStream()), 8192);
			final StringBuilder sb = new StringBuilder();

			System.out.println("HTTP: start (" + query + ")");
			String line = "";
			while ((line = rd.readLine()) != null) {    
				sb.append(line);
			}
			System.out.println("HTTP: end (" + query + ")");
			
			rd.close();
			return sb.toString().replace("<html>", "").replace("</html>", "");
			
		} catch (Exception e) {
			mErrorHandler.handle(e);
			return "";
		}
	}
	
	public String getString(String method, String parameters) {
		return this.query(method, parameters).replaceAll(LINE_SEP, "").trim();
	}
	
	public int getInt(String method, String parameters) {
		try {
			return Integer.parseInt(getString(method, parameters));
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	public int getInt(String method) {
		return getInt(method, "");
	}

	public boolean getBoolean(String method, String parameters) {
		try {
			assertBoolean(method, parameters);
			return true;
		} catch (WrongDataFormatException e) {
			return false;
		}
	}
	
	public boolean getBoolean(String method) {
		return getBoolean(method, "");
	}
	
	public ArrayList<String> getArray(String method, String parameters) {
		final String[] rows = query(method, parameters).split(LINE_SEP);
		final ArrayList<String> result = new ArrayList<String>();
		for (String row : rows) {
			if (row.length() > 0) {
				result.add(row.trim());
			}
		}
		return result;
	}
	
	public void assertBoolean(String method, String parameters) throws WrongDataFormatException {
		final String ret = query(method, parameters);
		if (!ret.contains("OK")) {
			throw new WrongDataFormatException("OK", ret);
		}
	}
	
	private void setResponseFormat() {
		try {
			assertBoolean("SetResponseFormat", "WebHeader;false");
			assertBoolean("SetResponseFormat", "WebFooter;false");
			assertBoolean("SetResponseFormat", "Header;");
			assertBoolean("SetResponseFormat", "Footer;");
			assertBoolean("SetResponseFormat", "OpenTag;");
			assertBoolean("SetResponseFormat", "OpenRecordSet;");
			assertBoolean("SetResponseFormat", "OpenRecord;");
			assertBoolean("SetResponseFormat", "CloseRecord;\n");
			assertBoolean("SetResponseFormat", "CloseField;|");
		} catch (WrongDataFormatException e) {
			mErrorHandler.handle(e);
		}
	}

	private URL formatQueryString(String method, String parameter) throws MalformedURLException, URISyntaxException {
		String encodedParameter = URLEncoder.encode(parameter);
		return new URL(mBaseURL + XBMC_HTTP_BOOTSTRAP + "?command=" + method + "(" + encodedParameter + ")");
	}
}
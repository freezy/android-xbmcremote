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
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpException;

/**
 * Keeps track of the HTTP connection and contains basic helper methods for
 * repeated String transformations 
 * 
 *  @author Team XBMC
 */
public class Connection {

    public class MyAuthenticator extends Authenticator {
    	private String username;
    	private char[] password;
    	public static final int MAX_RETRY = 5;
    	private int retry_count = 0;
    	
        public MyAuthenticator(String username, String password) {
    		this.username = username;
    		this.password = password!=null?password.toCharArray():new char[0];
		}

		// This method is called when a password-protected URL is accessed
        protected PasswordAuthentication getPasswordAuthentication() {
        	if(retry_count < MAX_RETRY) {
        		retry_count ++;
        		return new PasswordAuthentication(username, password);
        	}
        	return null;
        }
        // has to be called after each successful connection!!!
        public void resetCounter() {
        	retry_count = 0;
        }
    }
	
	public static final String LINE_SEP = "<li>";
	public static final String VALUE_SEP = ";";
	
	private static final int CONNECTION_TIMEOUT = 10000; // in milliseconds
	
	private static final String XBMC_HTTP_BOOTSTRAP =  "/xbmcCmds/xbmcHttp";
	private String mBaseURL;
	private IErrorHandler mErrorHandler;
	private boolean settingsOK = false;
	private URLConnection uc = null;
	private MyAuthenticator auth = null;
	
	/**
	 * Class constructor sets host data and error handler.
	 * @param host         XBMC host or IP address
	 * @param port         HTTP API port
	 * @param username     HTTP user name
	 * @param password     HTTP password
	 * @param errorHandler Error handler
	 */
	public Connection(String host, int port, String username, String password, IErrorHandler errorHandler) {
		auth = new MyAuthenticator(username, password);
		Authenticator.setDefault(auth);
		
		if (!host.equals("") && port > 0) {
			mBaseURL = "http://";
			if (username != null) {
				mBaseURL += username + /*(password != null ? ":" + password : "") + */"@";
			}
			mBaseURL += host;
			if (port != 80)
				mBaseURL += ":" + port;
//			setResponseFormat();
			settingsOK = true;
		}
		mErrorHandler = errorHandler;
	}
	/**
	 * Executes an HTTP API method and returns the result as untrimmed string.
	 * @param method      Name of the method to run
	 * @param parameters  Parameters of the method, separated by ";"
	 * @return Result
	 */
	public String query(String method, String parameters) {
		try {
			if (!settingsOK) {
				throw new NoSettingsException();
			}
			final URL query = formatQueryString(method, parameters);
//			final URLConnection uc = query.openConnection();
			uc = query.openConnection();
			uc.setConnectTimeout(CONNECTION_TIMEOUT);
			uc.setReadTimeout(CONNECTION_TIMEOUT);

			//connection successful, reset retry counter!
			auth.resetCounter();
			
			final BufferedReader rd = new BufferedReader(new InputStreamReader(uc.getInputStream()), 8192);
			final StringBuilder sb = new StringBuilder();

			System.out.println("HTTP: start " + URLDecoder.decode(query.toString()));
			String line = "";
			while ((line = rd.readLine()) != null) {    
				sb.append(line);
			}
			System.out.println("HTTP: end " + URLDecoder.decode(query.toString()));
			
			rd.close();
			uc = null;
			return sb.toString().replace("<html>", "").replace("</html>", "");
			
		} catch (Exception e) {
			try {
				if(((HttpURLConnection)uc).getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
					e = new HttpException(Integer.toString(HttpURLConnection.HTTP_UNAUTHORIZED));
				}
			} catch (IOException e1) {
				//do nothing, just tried to check the response code
			}
			mErrorHandler.handle(e);
			return "";
		}
	}
	
	/**
	 * Executes an HTTP API method and returns the result as string.
	 * @param method      Name of the method to run
	 * @param parameters  Parameters of the method, separated by ";"
	 * @return Result
	 */
	public String getString(String method, String parameters) {
		return this.query(method, parameters).replaceAll(LINE_SEP, "").trim();
	}
	
	/**
	 * Executes an HTTP API method and returns the result as string.
	 * @param method      Name of the method to run
	 * @return Result
	 */
	public String getString(String method) {
		return getString(method, "");
	}
	
	/**
	 * Executes an HTTP API method and returns the result as integer.
	 * @param method      Name of the method to run
	 * @param parameters  Parameters of the method, separated by ";"
	 * @return Result
	 */
	public int getInt(String method, String parameters) {
		try {
			return Integer.parseInt(getString(method, parameters));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * Executes an HTTP API method without parameter and returns the result as
	 * integer.
	 * @param method      Name of the method to run
	 * @return Result
	 */
	public int getInt(String method) {
		return getInt(method, "");
	}

	/**
	 * Executes an HTTP API method and returns the result as boolean.
	 * @param method      Name of the method to run
	 * @param parameters  Parameters of the method, separated by ";"
	 * @return Result
	 */
	public boolean getBoolean(String method, String parameters) {
		try {
			assertBoolean(method, parameters);
			return true;
		} catch (WrongDataFormatException e) {
			return false;
		}
	}
	
	/**
	 * Executes an HTTP API method and returns the result as boolean.
	 * @param method      Name of the method to run
	 * @param parameters  Parameters of the method, separated by ";"
	 * @return Result
	 */
	public boolean getBoolean(String method) {
		return getBoolean(method, "");
	}
	
	/**
	 * Executes an HTTP API method without parameter and returns the result in
	 * a list of strings.
	 * @param method      Name of the method to run
	 * @return Result
	 */
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
	
	/**
	 * Executes an HTTP API method and makes sure the result is OK (or something
	 * like that)
	 * @param method      Name of the method to run
	 * @param parameters  Parameters of the method, separated by ";"
	 * @throws WrongDataFormatException If not "OK"
	 */
	public void assertBoolean(String method, String parameters) throws WrongDataFormatException {
		final String ret = query(method, parameters);
		if (!ret.contains("OK")) {
			throw new WrongDataFormatException("OK", ret);
		}
	}
	
	/**
	 * Sets the correct response format
	 * @deprecated doesn't really work
	 */
/*	private void setResponseFormat() {
		try {
			assertBoolean("SetResponseFormat", "WebHeader;false");
			assertBoolean("SetResponseFormat", "WebFooter;false");
			assertBoolean("SetResponseFormat", "Header; ");
			assertBoolean("SetResponseFormat", "Footer; ");
			assertBoolean("SetResponseFormat", "OpenTag; ");
			assertBoolean("SetResponseFormat", "OpenRecordSet; ");
			assertBoolean("SetResponseFormat", "OpenRecord; ");
			assertBoolean("SetResponseFormat", "CloseRecord;\n");
			assertBoolean("SetResponseFormat", "CloseField;|");
		} catch (WrongDataFormatException e) {
			mErrorHandler.handle(e);
		}
	}*/
	
	/**
	 * Creates the API URL
	 * @param method     Name of the method to run
	 * @param parameter  Parameters of the method, separated by ";"
	 * @return           HTTP API URL
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	private URL formatQueryString(String method, String parameter) throws MalformedURLException, URISyntaxException {
		return new URL(generateQuery(method, parameter));
	}
	
	public String generateQuery(String method, String parameter) {
		String encodedParameter = URLEncoder.encode(parameter);
		return mBaseURL + XBMC_HTTP_BOOTSTRAP + "?command=" + method + "(" + encodedParameter + ")";
	}

	/**
	 * Removes the trailing "</field>" string from the value
	 * @param value
	 * @return Trimmed value
	 */
	public static String trim(String value) {
		return value.substring(0, value.length() - 8);
	}	
	
	/**
	 * Removes the trailing "</field>" string from the value and tries to
	 * parse an integer from it. On error, returns -1.
	 * @param value
	 * @return Parsed integer from field value
	 */
	public static int trimInt(String value) {
		String trimmed = trim(value);
		if (trimmed.length() > 0) {
			try {
				return Integer.parseInt(trimmed);
			} catch (NumberFormatException e) {
				return -1;
			}
		} else {
			return -1;
		}
	}
	/**
	 * Check connection to a XBMC HttpApi session
	 * @return
	 * TODO Might need implementation change to not mess with response format
	 */
	public boolean isConnected() {
		try {
			assertBoolean("SetResponseFormat", "");
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public String getBaseURL() {
		return mBaseURL;
	}
}
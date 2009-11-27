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
import java.util.HashMap;

import org.apache.http.HttpException;

import android.util.Log;

/**
 * Keeps track of the HTTP connection and contains basic helper methods for
 * repeated String transformations 
 * 
 *  @author Team XBMC
 */
public class Connection {

	public static final String LINE_SEP = "<li>";
	public static final String VALUE_SEP = ";";
	public static final String PAIR_SEP = ":";
	
	public static final String TAG = "Connection";
	
	/**
	 * Connection timeout in milliseconds. That's just the CONNECTION timeout. 
	 * Read timeout is set by preferences.
	 */
	private static final int CONNECTION_TIMEOUT = 5000; 
	
	private static final String XBMC_HTTP_BOOTSTRAP =  "/xbmcCmds/xbmcHttp";
	private String mBaseURL;
	private IErrorHandler mErrorHandler;
	private boolean settingsOK = false;
	private int mTimeout;
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
	public Connection(String host, int port, String username, String password, int timeout, IErrorHandler errorHandler) {
		auth = new MyAuthenticator(username, password);
		Authenticator.setDefault(auth);
		mErrorHandler = errorHandler;
		if (!host.equals("") && port > 0) {
			mBaseURL = "http://";
			if (username != null) {
				mBaseURL += username + /*(password != null ? ":" + password : "") + */"@";
			}
			mBaseURL += host;
			if (port != 80) {
				mBaseURL += ":" + port;
			}
			mTimeout = timeout;
			settingsOK = true;
			if(isConnected())
				setResponseFormat();
		}
		else
			mErrorHandler.handle(new NoSettingsException());
	}
	
	/**
	 * Executes an HTTP API method and returns the result as untrimmed string.
	 * @param method      Name of the method to run
	 * @param parameters  Parameters of the method, separated by ";"
	 * @return Result
	 */
	public synchronized String query(String method, String parameters) {
		try {
			if (!settingsOK) {
				throw new NoSettingsException();
			}
			final URL query = formatQueryString(method, parameters);
			final String debugUrl = URLDecoder.decode(query.toString());
			final String debugTag = debugUrl.substring(debugUrl.indexOf("?command=") + 9);
			Log.i(TAG, "START: " + debugUrl);

			
//			final URLConnection uc = query.openConnection();
//			final long connectionTimer = System.currentTimeMillis();
			uc = query.openConnection();
			uc.setConnectTimeout(CONNECTION_TIMEOUT);
			uc.setReadTimeout(mTimeout);
			
			//Log.i(TAG, "CONNECTED: " + (System.currentTimeMillis() - connectionTimer) + "ms (" + debugTag + ")");

			//connection successful, reset retry counter!
			auth.resetCounter();
			
			final long responseTimer = System.currentTimeMillis();
			final BufferedReader rd = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			final StringBuilder sb = new StringBuilder();
			
			String line = "";
			while ((line = rd.readLine()) != null) {    
				sb.append(line);
			}
			Log.i(TAG, "DONE: " + (System.currentTimeMillis() - responseTimer) + "ms (" + debugTag + ")");
			
			rd.close();
			uc = null;
			return sb.toString().replace("<html>", "").replace("</html>", "");
			
		} catch (Exception e) {
			try {
				if(uc != null) {
					if(((HttpURLConnection)uc).getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
						e = new HttpException(Integer.toString(HttpURLConnection.HTTP_UNAUTHORIZED));
					}
				}
			} catch (Exception e1) {
				//do nothing, just tried to check the response code
			}
//			mIsConnected = false;
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
		return query(method, parameters).replaceAll(LINE_SEP, "").trim();
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
			return assertBoolean(method, parameters);
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
	 * Executes an HTTP API method and returns the result in a list of strings.
	 * @param method      Name of the method to run
	 * @param parameters  Parameters of the method, separated by ";"
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
	 * Executes an HTTP API method and returns the result as a list of 
	 * key => value pairs
	 * @param method      Name of the method to run
	 * @param parameters  Parameters of the method, separated by ";"
	 * @return
	 */
	public HashMap<String, String> getPairs(String method, String parameters) {
		final String[] rows = query(method, parameters).split(LINE_SEP);
		final HashMap<String, String> result = new HashMap<String, String>();
		for (String row : rows) {
			final String[] pair = row.split(PAIR_SEP, 2);
			if (pair.length == 1) {
				result.put(pair[0].trim(), "");
			} else if (pair.length == 2 && pair[0].trim().length() > 0) {
				result.put(pair[0].trim(), pair[1].trim());
			}
		}
		return result;
	}

	/**
	 * Executes an HTTP API method without parameter and returns the result as
	 * a list of key => value pairs
	 * @param method      Name of the method to run
	 * @return
	 */
	public HashMap<String, String> getPairs(String method) {
		return getPairs(method, "");
	}
	
	/**
	 * Executes an HTTP API method and makes sure the result is OK (or something
	 * like that)
	 * @param method      Name of the method to run
	 * @param parameters  Parameters of the method, separated by ";"
	 * @throws WrongDataFormatException If not "OK"
	 */
	public boolean assertBoolean(String method, String parameters) throws WrongDataFormatException {
		final String ret = query(method, parameters);
		if (ret.contains("OK") || ret.contains("true") || ret.contains("True") || ret.contains("TRUE")) {
			return true;
		} else if (ret.contains("false") || ret.contains("False") || ret.contains("FALSE")) {
			return false;
		} else {
			throw new WrongDataFormatException("OK", ret);
		}
	}
	
	/**
	 * Executes an HTTP API method and makes sure the result is OK (or something
	 * like that)
	 * @param method      Name of the method to run
	 * @throws WrongDataFormatException If not "OK"
	 */
	public boolean assertBoolean(String method) throws WrongDataFormatException {
		return assertBoolean(method, "");
	}
	
	/**
	 * Sets the correct response format to default values
	 */
	private boolean setResponseFormat() {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("WebHeader;true;");
			sb.append("WebFooter;true;");
			sb.append("Header; ;");
			sb.append("Footer; ;");
			sb.append("OpenTag;");sb.append(LINE_SEP);sb.append(";");
			sb.append("CloseTag;\n;");
			sb.append("CloseFinalTag;false");
			assertBoolean("SetResponseFormat", sb.toString());
			
			sb = new StringBuilder();
			sb.append("OpenRecordSet; ;");
			sb.append("CloseRecordSet; ;");
			sb.append("OpenRecord; ;");
			sb.append("CloseRecord; ;");
			sb.append("OpenField;<field>;");
			sb.append("CloseField;</field>");
			assertBoolean("SetResponseFormat", sb.toString());

			return true;
//			mIsConnected = true;
		} catch (WrongDataFormatException e) {
			return false;
//			mIsConnected = false;
		}
	}
	
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
		StringBuilder sb = new StringBuilder();
		sb.append(mBaseURL);
		sb.append(XBMC_HTTP_BOOTSTRAP);
		sb.append("?command=");
		sb.append(method);
		sb.append("(");
		sb.append(encodedParameter);
		sb.append(")");
//		return mBaseURL + XBMC_HTTP_BOOTSTRAP + "?command=" + method + "(" + encodedParameter + ")";
		return sb.toString();
	}

	/**
	 * Removes the trailing "</field>" string from the value
	 * @param value
	 * @return Trimmed value
	 */
	public static String trim(String value) {
		return value.replace("</record>", "").substring(0, value.length() - 8);
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
				return Integer.parseInt(trimmed.replace(",", ""));
			} catch (NumberFormatException e) {
				return -1;
			}
		} else {
			return -1;
		}
	}

	/**
	 * Removes the trailing "</field>" string from the value and tries to
	 * parse a double from it. On error, returns -1.0.
	 * @param value
	 * @return Parsed double from field value
	 */
	public static double trimDouble(String value) {
		String trimmed = trim(value);
		if (trimmed.length() > 0) {
			try {
				return Double.parseDouble(trimmed);
			} catch (NumberFormatException e) {
				return -1.0;
			}
		} else {
			return -1.0;
		}
	}

	/**
	 * Check connection to a XBMC HttpApi session
	 * @return
	 * TODO Might need implementation change to not mess with response format
	 */
	public synchronized boolean isConnected() {
		try{
			final URL query = formatQueryString("WebServerStatus", "");
			uc = query.openConnection();
			uc.setConnectTimeout(CONNECTION_TIMEOUT);
			uc.setReadTimeout(mTimeout);
			
			auth.resetCounter();
			
			final BufferedReader rd = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			final StringBuilder sb = new StringBuilder();
			
			String line = "";
			while ((line = rd.readLine()) != null) {    
				sb.append(line);
			}
			
			rd.close();
			uc = null;
			
			final String ret = sb.toString().replace("<html>", "").replace("</html>", "");
			if (ret.contains("On"))
				return true;
			
		} catch (MalformedURLException e){
			return false;
		} catch (URISyntaxException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return false;
	}
	
	public String getBaseURL() {
		return mBaseURL;
	}
	

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
	
}
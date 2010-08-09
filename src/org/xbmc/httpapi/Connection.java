/*
 *      Copyright (C) 2005-2010 Team XBMC
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpException;
import org.xbmc.android.util.ClientFactory;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.object.Host;

import android.util.Log;


/**
 * Singleton class. Will be instantiated only once
 * 
 * @author Team XBMC
 */
public class Connection {

	private static final String TAG = "Connection";
	private static final String XBMC_HTTP_BOOTSTRAP =  "/xbmcCmds/xbmcHttp";
	private static final String XBMC_MICROHTTPD_THUMB_BOOTSTRAP =  "/thumb/";
	private static final String XBMC_MICROHTTPD_VFS_BOOTSTRAP =  "/vfs/";
	private static final int SOCKET_CONNECTION_TIMEOUT = 5000;
	
	/**
	 * Singleton class instance
	 */
	private static Connection sConnection;
	
	/**
	 * Complete URL without any attached command parameters, for instance:
	 * <code>http://192.168.0.10:8080</code>
	 */
	private String mUrlSuffix;
	
	/**
	 * Socket read timeout (connection timeout is default)
	 */
	private int mSocketReadTimeout = 0;
	
	/**
	 * Performs HTTP Authentication
	 */
	private HttpAuthenticator mAuthenticator = null;
	
	/**
	 * Use getInstance() for public class instantiation
	 * @param host XBMC host
	 * @param port HTTP API port
	 */
	private Connection(String host, int port) {
		setHost(host, port);
	}
	
	/**
	 * Returns the singleton instance of this connection. Note that host and 
	 * port settings are only looked at the first time. Use {@link setHost()}
	 * if you want to update these parameters.
	 * @param host XBMC host
	 * @param port HTTP API port
	 * @return Connection instance
	 */
	public static Connection getInstance(String host, int port) {
		if (sConnection == null) {
			sConnection = new Connection(host, port);
		}
		if (sConnection.mUrlSuffix == null) {
			sConnection.setHost(host, port);
		}
		return sConnection;
	}
	
	/**
	 * Updates host info of the connection instance
	 * @param host
	 */
	public void setHost(Host host) {
		if (host == null) {
			setHost(null, 0);
		} else {
			setHost(host.addr, host.port);
			setAuth(host.user, host.pass);
		}
	}
	
	/**
	 * Updates host and port parameters of the connection instance.
	 * @param host Host or IP address of the host
	 * @param port Port the HTTP API is listening to
	 */
	public void setHost(String host, int port) {
		if (host == null || port <= 0) {
			mUrlSuffix = null;
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("http://");
			sb.append(host);
			sb.append(":");
			sb.append(port);
			mUrlSuffix = sb.toString();
		}
	}
	
	/**
	 * Sets authentication info
	 * @param user HTTP API username
	 * @param pass HTTP API password
	 */
	public void setAuth(String user, String pass) {
		if (pass != null && pass.length() > 0) {
			mAuthenticator = new HttpAuthenticator(user, pass);
			Authenticator.setDefault(mAuthenticator);
		} else {
			mAuthenticator = null;
			Authenticator.setDefault(null);
		}
	}
	
	/**
	 * Sets socket read timeout (connection timeout has constant value)
	 * @param timeout Read timeout in milliseconds.
	 */
	public void setTimeout(int timeout) {
		if (timeout > 0) {
			mSocketReadTimeout = timeout;
		}
	}
	
	/**
	 * Returns the full URL of an HTTP API request
	 * @param command    Name of the command to execute
	 * @param parameters Parameters, separated by ";".
	 * @return Absolute URL to HTTP API
	 */
	public String getUrl(String command, String parameters) {
		// create url
		StringBuilder sb = new StringBuilder(mUrlSuffix);
		sb.append(XBMC_HTTP_BOOTSTRAP);
		sb.append("?command=");
		sb.append(command);
		sb.append("(");
		sb.append(URLEncoder.encode(parameters));
		sb.append(")");
		return sb.toString();
	}
	
	/**
	 * Returns an input stream pointing to a HTTP API command.
	 * @param command    Name of the command to execute
	 * @param parameters Parameters, separated by ";".
	 * @param manager    Reference back to business layer
	 * @return
	 */
	public InputStream getThumbInputStream(String command, String parameters, INotifiableManager manager) {
		URLConnection uc = null;
		try {
			if (mUrlSuffix == null) {
				throw new NoSettingsException();
			}
			if (mAuthenticator != null) {
				mAuthenticator.resetCounter();
			}
			URL url = new URL(getUrl(command, parameters));
			uc = url.openConnection();
			uc.setConnectTimeout(SOCKET_CONNECTION_TIMEOUT);
			uc.setReadTimeout(mSocketReadTimeout);
			Log.i(TAG, "Preparing input stream from " + url);
			return uc.getInputStream();
		} catch (MalformedURLException e) {
			manager.onError(e);
		} catch (IOException e) {
			manager.onError(e);
		} catch (NoSettingsException e) {
			manager.onError(e);
		}
		return null;
	}
	
	/**
	 * Returns an input stream pointing to a HTTP API command.
	 * @param command    Name of the command to execute
	 * @param parameters Parameters, separated by ";".
	 * @param manager    Reference back to business layer
	 * @return
	 */
	public InputStream getThumbInputStreamForMicroHTTPd(String thumb, INotifiableManager manager) throws FileNotFoundException {
		URLConnection uc = null;
		try {
			if (mUrlSuffix == null) {
				throw new NoSettingsException();
			}
			if (mAuthenticator != null) {
				mAuthenticator.resetCounter();
			}
			final URL url;
			if (ClientFactory.XBMC_REV > 0 && ClientFactory.XBMC_REV >= ClientFactory.THUMB_TO_VFS_REV) {
				url = new URL(mUrlSuffix + XBMC_MICROHTTPD_VFS_BOOTSTRAP + URLEncoder.encode(thumb));
			} else {
				url = new URL(mUrlSuffix + XBMC_MICROHTTPD_THUMB_BOOTSTRAP + thumb + ".jpg");
			}
			Log.i(TAG, "Preparing input stream from " + url + " for microhttpd..");
			uc = url.openConnection();
			uc.setConnectTimeout(SOCKET_CONNECTION_TIMEOUT);
			uc.setReadTimeout(mSocketReadTimeout);
			return uc.getInputStream();
		} catch (FileNotFoundException e) {
			throw e;
		} catch (MalformedURLException e) {
			manager.onError(e);
		} catch (IOException e) {
			manager.onError(e);
		} catch (NoSettingsException e) {
			manager.onError(e);
		}
		return null;
	}
	
	/**
	 * Executes a query.
	 * @param command    Name of the command to execute
	 * @param parameters Parameters, separated by ";".
	 * @param manager    Reference back to business layer
	 * @return HTTP response string.
	 */
	public String query(String command, String parameters, INotifiableManager manager) {
		URLConnection uc = null;
		try {
			if (mUrlSuffix == null) {
				throw new NoSettingsException();
			}
			if (mAuthenticator != null) {
				mAuthenticator.resetCounter();
			}
			URL url = new URL(getUrl(command, parameters));
			uc = url.openConnection();
			uc.setConnectTimeout(SOCKET_CONNECTION_TIMEOUT);
			uc.setReadTimeout(mSocketReadTimeout);
			
			final String debugUrl = URLDecoder.decode(url.toString());
			Log.i(TAG, debugUrl);
			
			final BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()), 8192);
			final StringBuilder response = new StringBuilder();
			String line;

			while ((line = in.readLine()) != null) {
				response.append(line);
			}
			in.close();
			return response.toString().replace("<html>", "").replace("</html>", "");
			
		} catch (MalformedURLException e) {
			manager.onError(e);
		} catch (IOException e) {
			int responseCode = -1;
			try {
				responseCode = ((HttpURLConnection)uc).getResponseCode();
			} catch (IOException e1) { } // do nothing, getResponse code failed so treat as default i/o exception.
			if (uc != null && responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				manager.onError(new HttpException(Integer.toString(HttpURLConnection.HTTP_UNAUTHORIZED)));
			} else {
				manager.onError(e);
			}
		} catch (NoSettingsException e) {
			manager.onError(e);
		}
		return "";
	}
	
	/**
	 * Executes an HTTP API method and returns the result as string.
	 * @param method      Name of the method to run
	 * @param parameters  Parameters of the method, separated by ";"
	 * @return Result
	 */
	public String getString(INotifiableManager manager, String method, String parameters) {
		return query(method, parameters, manager).replaceAll(LINE_SEP, "").trim();
	}
	
	/**
	 * Executes an HTTP API method and returns the result as string.
	 * @param method      Name of the method to run
	 * @return Result
	 */
	public String getString(INotifiableManager manager, String method) {
		return getString(manager, method, "");
	}
	
	/**
	 * Executes an HTTP API method and returns the result as integer.
	 * @param method      Name of the method to run
	 * @param parameters  Parameters of the method, separated by ";"
	 * @return Result
	 */
	public int getInt(INotifiableManager manager, String method, String parameters) {
		try {
			return Integer.parseInt(getString(manager, method, parameters));
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
	public int getInt(INotifiableManager manager, String method) {
		return getInt(manager, method, "");
	}
	
	/**
	 * Executes an HTTP API method and makes sure the result is OK (or something
	 * like that)
	 * @param method      Name of the method to run
	 * @param parameters  Parameters of the method, separated by ";"
	 * @throws WrongDataFormatException If not "OK"
	 */
	public boolean assertBoolean(INotifiableManager manager, String method, String parameters) throws WrongDataFormatException {
		final String ret = query(method, parameters, manager);
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
	public boolean assertBoolean(INotifiableManager manager, String method) throws WrongDataFormatException {
		return assertBoolean(manager, method, "");
	}
	
	/**
	 * Executes an HTTP API method and returns the result as boolean.
	 * @param method      Name of the method to run
	 * @param parameters  Parameters of the method, separated by ";"
	 * @return Result
	 */
	public boolean getBoolean(INotifiableManager manager, String method, String parameters) {
		try {
			return assertBoolean(manager, method, parameters);
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
	public boolean getBoolean(INotifiableManager manager, String method) {
		return getBoolean(manager, method, "");
	}
	
	/**
	 * Executes an HTTP API method and returns the result in a list of strings.
	 * @param method      Name of the method to run
	 * @param parameters  Parameters of the method, separated by ";"
	 */
	public ArrayList<String> getArray(INotifiableManager manager, String method, String parameters) {
		final String[] rows = query(method, parameters, manager).split(LINE_SEP);
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
	public HashMap<String, String> getPairs(INotifiableManager manager, String method, String parameters) {
		final String[] rows = query(method, parameters, manager).split(LINE_SEP);
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
	public HashMap<String, String> getPairs(INotifiableManager manager, String method) {
		return getPairs(manager, method, "");
	}
	
	/**
	 * Removes the trailing "</field>" string from the value
	 * @param value
	 * @return Trimmed value
	 */
	public static String trim(String value) {
		return new String(value.replace("</record>", "").replace("<record>", "").replace("</field>", "").toCharArray());
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
	 * Removes the trailing "</field>" string from the value and tries to
	 * parse a boolean from it.
	 * @param value
	 * @return Parsed boolean from field value
	 */
	public static boolean trimBoolean(String value) {
		if (value.startsWith("0") || value.toLowerCase().startsWith("false")) {
			return false;
		}
		if (value.startsWith("1") || value.toLowerCase().startsWith("true")) {
			return true;
		}
		return false;
	}
	
	/**
	 * HTTP Authenticator.
	 * 
	 * @author Team XBMC
	 */
    public class HttpAuthenticator extends Authenticator {
    	public static final int MAX_RETRY = 5;
    	
    	private final String mUser;
    	private final char[] mPass;
    	private int mRetryCount = 0;
    	
        public HttpAuthenticator(String user, String pass) {
    		mUser = user;
    		mPass = pass != null ? pass.toCharArray() : new char[0];
		}

        /**
         * This method is called when a password-protected URL is accessed
         */
        protected PasswordAuthentication getPasswordAuthentication() {
        	if (mRetryCount < MAX_RETRY) {
        		mRetryCount++;
        		return new PasswordAuthentication(mUser, mPass);
        	}
        	return null;
        }
        
        /**
         * This method has to be called after each successful connection!!!
         */
        public void resetCounter() {
        	mRetryCount = 0;
        }
    }
	
	public static final String LINE_SEP = "<li>";
	public static final String VALUE_SEP = ";";
	public static final String PAIR_SEP = ":";
}

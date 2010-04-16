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

package org.xbmc.jsonrpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpException;
import org.json.JSONException;
import org.json.JSONObject;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.object.Host;
import org.xbmc.httpapi.NoSettingsException;


/**
 * Singleton class. Will be instantiated only once and contains mostly help
 * 
 * @author Team XBMC
 */
public class Connection {

//	private static final String TAG = "Connection-JsonRpc";
	private static final String XBMC_JSONRPC_BOOTSTRAP = "/jsonrpc";
	private static final int SOCKET_CONNECTION_TIMEOUT = 5000;
	
	/**
	 * Singleton class instance
	 */
	private static Connection sConnection;
	
	/**
	 * Complete URL without any attached command parameters, for instance:
	 * <code>http://192.168.0.10:8080/jsonrpc</code>
	 */
	private String mUrl;
	
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
	 * @param port Port
	 */
	private Connection(String host, int port) {
		setHost(host, port);
	}
	
	/**
	 * Returns the singleton instance of this connection. Note that host and 
	 * port settings are only looked at the first time. Use {@link setHost()}
	 * if you want to update these parameters.
	 * @param host XBMC host
	 * @param port HTTP API / JSON-RPC port (it's the same)
	 * @return Connection instance
	 */
	public static Connection getInstance(String host, int port) {
		if (sConnection == null) {
			sConnection = new Connection(host, port);
		}
		if (sConnection.mUrl == null) {
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
	 * @param port HTTP port
	 */
	public void setHost(String host, int port) {
		if (host == null || port <= 0) {
			mUrl = null;
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("http://");
			sb.append(host);
			sb.append(":");
			sb.append(port);
			sb.append(XBMC_JSONRPC_BOOTSTRAP);
			mUrl = sb.toString();
		}
	}
	
	/**
	 * Sets authentication info
	 * @param user Username
	 * @param pass Password
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
	
	public InputStream getInputStream(String url, INotifiableManager manager) {
		try {
			final URL u = new URL(url);
			URLConnection uc;
			uc = u.openConnection();
			uc.setConnectTimeout(SOCKET_CONNECTION_TIMEOUT);
			uc.setReadTimeout(mSocketReadTimeout);
			return uc.getInputStream();
		} catch (IOException e) {
			manager.onError(e);
		}
		return null;
	}
	
	/**
	 * Executes a query.
	 * @param command    Name of the command to execute
	 * @param parameters Parameters
	 * @param manager    Reference back to business layer
	 * @return Parsed JSON object, empty object on error.
	 */
	public JSONObject query(String command, JSONObject parameters, INotifiableManager manager) {
		URLConnection uc = null;
		try {
			if (mUrl == null) {
				throw new NoSettingsException();
			}
			if (mAuthenticator != null) {
				mAuthenticator.resetCounter();
			}
			final URL url = new URL(mUrl);
			uc = url.openConnection();
			uc.setConnectTimeout(SOCKET_CONNECTION_TIMEOUT);
			uc.setReadTimeout(mSocketReadTimeout);
			uc.setDoOutput(true);
			
			final OutputStreamWriter wr = new OutputStreamWriter(uc.getOutputStream());
			final JSONObject data = new JSONObject()
				.put("jsonrpc", "2.0")
				.put("method", command)
				.put("id", "1");
			if (parameters != null) {
				data.put("params", parameters);
			} else {
				data.put("params", new JSONObject());
			}
			
			// POST data
			wr.write(data.toString());
			wr.flush();
			
			final BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()), 8192);
			final StringBuilder response = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
				response.append(line);
			}
			wr.close();
			in.close();
			
			final JSONObject jsonResponse = new JSONObject(response.toString());
			return jsonResponse;
			
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
		} catch (JSONException e) {
			manager.onError(e);
		}
		return new JSONObject();
	}
	
	/**
	 * Executes a JSON-RPC command and returns the result as JSON object.
	 * @param manager     Upper layer reference for error posting
	 * @param method      Name of the method to run
	 * @param parameters  Parameters of the method
	 * @return Result
	 */
	public JSONObject getJson(INotifiableManager manager, String method, JSONObject parameters) {
		try {
			return query(method, parameters, manager).getJSONObject(RESULT_FIELD);
		} catch (JSONException e) {
			manager.onError(e);
			return new JSONObject();
		}
	}
	
	/**
	 * Executes a JSON-RPC command without parameters and returns the result as
	 * JSON object.
	 * @param manager     Upper layer reference for error posting
	 * @param method      Name of the method to run
	 * @return Result
	 */
	public JSONObject getJson(INotifiableManager manager, String method) {
		try {
			return query(method, null, manager).getJSONObject(RESULT_FIELD);
		} catch (JSONException e) {
			manager.onError(e);
			return new JSONObject();
		}
	}
	
	/**
	 * Executes an JSON-RPC method and returns the result from a field as string.
	 * @param manager     Upper layer reference for error posting
	 * @param method      Name of the method to run
	 * @param parameters  Parameters of the method, separated by ";"
	 * @param returnField Name of the field to return
	 * @return Result
	 */
	public String getString(INotifiableManager manager, String method, JSONObject parameters, String returnField) {
		try {
			return query(method, parameters, manager).getJSONObject(RESULT_FIELD).getString(returnField);
		} catch (JSONException e) {
			manager.onError(e);
			return "";
		}
	}
	
	/**
	 * Executes an JSON-RPC method without parameter and returns the result 
	 * from a field as string.
	 * @param manager     Upper layer reference for error posting
	 * @param method      Name of the method to run
	 * @param returnField Name of the field to return
	 * @return Result as string
	 */
	public String getString(INotifiableManager manager, String method, String returnField) {
		return getString(manager, method, null, returnField);
	}
	
	/**
	 * Executes an JSON-RPC method and returns the result from a field as integer.
	 * @param manager     Upper layer reference for error posting
	 * @param method      Name of the method to run
	 * @param parameters  Parameters of the method, separated by ";"
	 * @param returnField Name of the field to return
	 * @return Result as integer
	 */
	public int getInt(INotifiableManager manager, String method, JSONObject parameters, String returnField) {
		try {
			return Integer.parseInt(getString(manager, method, parameters, returnField));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * Executes an JSON-RPC method without parameter and returns the result 
	 * from a field as integer.
	 * @param manager     Upper layer reference for error posting
	 * @param method      Name of the method to run
	 * @param returnField Name of the field to return
	 * @return Result as integer
	 */
	public int getInt(INotifiableManager manager, String method, String returnField) {
		return getInt(manager, method, null, returnField);
	}
	
	/**
	 * Executes an JSON-RPC method and returns the result from a field as boolean.
	 * @param manager     Upper layer reference for error posting
	 * @param method      Name of the method to run
	 * @param parameters  Parameters of the method, separated by ";"
	 * @param returnField Name of the field to return
	 * @return Result as boolean
	 */
	public boolean getBoolean(INotifiableManager manager, String method, JSONObject parameters, String returnField) {
		return getString(manager, method, parameters, returnField).equals("true");
	}
	
	/**
	 * Executes an JSON-RPC method without parameters and returns the result 
	 * from a field as boolean.
	 * @param manager     Upper layer reference for error posting
	 * @param method      Name of the method to run
	 * @param returnField Name of the field to return
	 * @return Result as boolean
	 */
	public boolean getBoolean(INotifiableManager manager, String method, String returnField) {
		return getBoolean(manager, method, null, returnField);
	}
	
	/**
	 * HTTP Authenticator.
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
	
	public static final String RESULT_FIELD = "result";
}

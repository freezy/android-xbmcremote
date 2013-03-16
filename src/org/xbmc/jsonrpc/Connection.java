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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.http.HttpException;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.xbmc.android.util.Base64;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.object.Host;
import org.xbmc.httpapi.NoSettingsException;
import org.xbmc.jsonrpc.client.Client;

import android.util.Log;


/**
 * Singleton class. Will be instantiated only once and contains mostly help
 * 
 * @author Team XBMC
 */
public class Connection {

	private static final String TAG = "Connection-JsonRpc";
	
	private static final String XBMC_JSONRPC_BOOTSTRAP = "/jsonrpc";
	private static final int SOCKET_CONNECTION_TIMEOUT = 5000;
	
	/**
	 * Singleton class instance
	 */
	private static Connection sConnection;
	
	/**
	 * Complete URL without any attached command parameters, for instance:
	 * <code>http://192.168.0.10:8080</code>
	 */
	private String mUrlSuffix;;
	
	/**
	 * Socket read timeout (connection timeout is default)
	 */
	private int mSocketReadTimeout = 0;
	
	/**
	 * Holds the base64 encoded user/pass for http authentication
	 */
	private String authEncoded = null;

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
	 * @param port HTTP port
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
	 * @param user Username
	 * @param pass Password
	 */
	public void setAuth(String user, String pass) {
		if (user != null && pass != null) {
			String auth = user + ":" + pass;
			authEncoded = Base64.encodeBytes(auth.getBytes()).toString();
		} else {
			authEncoded = null;
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
	
	public InputStream getThumbInputStream(String thumb, INotifiableManager manager) throws FileNotFoundException {
		URLConnection uc = null;
		try {
			if (mUrlSuffix == null) {
				throw new NoSettingsException();
			}
			URL url = new URL(thumb);
			Log.i(TAG, "Preparing input stream from " + thumb + " for microhttpd..");
			uc = getUrlConnection(url);
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
	 * Returns the full URL of an HTTP API request
	 * @param command    Name of the command to execute
	 * @param parameters Parameters, separated by ";".
	 * @return Absolute URL to HTTP API
	 */
	public String getUrl(String path) {
		// create url
		StringBuilder sb = new StringBuilder(mUrlSuffix);
		sb.append("/");
		sb.append(path);
		return sb.toString();
	}
	
	public byte[] download(String pathToDownload) throws IOException, URISyntaxException {
		try {
			final URL url = new URL(pathToDownload);
			final URLConnection uc = getUrlConnection(url);
			
			final InputStream is = uc.getInputStream();
			final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			
			byte[] data = new byte[8192];
			int nRead;
			while ((nRead = is.read(data, 0, data.length)) != -1) {
				  buffer.write(data, 0, nRead);
				}

			return buffer.toByteArray();
			
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Create a new URLConnection with the request headers set, including authentication.
	 *
	 * @param url The request url
	 * @return URLConnection
	 * @throws IOException
	 */
	private URLConnection getUrlConnection(URL url) throws IOException {
		final URLConnection uc = url.openConnection();
		uc.setConnectTimeout(SOCKET_CONNECTION_TIMEOUT);
		uc.setReadTimeout(mSocketReadTimeout);
		uc.setRequestProperty("Connection", "close");

		if (authEncoded != null) {
			uc.setRequestProperty("Authorization", "Basic " + authEncoded);
		}

		return uc;
	}
	
	/**
	 * Executes a query.
	 * @param command    Name of the command to execute
	 * @param parameters Parameters
	 * @param manager    Reference back to business layer
	 * @return Parsed JSON object, empty object on error.
	 */
	public JsonNode query(String command, JsonNode parameters, INotifiableManager manager) {
		URLConnection uc = null;
		try {
			final ObjectMapper mapper = Client.MAPPER;

			if (mUrlSuffix == null) {
				throw new NoSettingsException();
			}

			final URL url = new URL(mUrlSuffix + XBMC_JSONRPC_BOOTSTRAP);
			uc = url.openConnection();
			uc.setConnectTimeout(SOCKET_CONNECTION_TIMEOUT);
			uc.setReadTimeout(mSocketReadTimeout);
			if (authEncoded != null) {
				uc.setRequestProperty("Authorization", "Basic " + authEncoded);
			}
			uc.setRequestProperty("Content-Type", "application/json");
			uc.setDoOutput(true);
			
			final ObjectNode data = Client.obj()
				.p("jsonrpc", "2.0")
				.p("method", command)
				.p("id", "1");
			if (parameters != null) {
				data.put("params", parameters);
			}
			
			final JsonFactory jsonFactory = new JsonFactory();
			final JsonGenerator jg = jsonFactory.createJsonGenerator(uc.getOutputStream(), JsonEncoding.UTF8);
			jg.setCodec(mapper);

			// POST data
			jg.writeTree(data);
			jg.flush();
			
			final JsonParser jp = jsonFactory.createJsonParser(uc.getInputStream());
			jp.setCodec(mapper);
			final JsonNode ret = jp.readValueAs(JsonNode.class);
			return ret;
			
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
		return new ObjectNode(null);
	}
	
	/**
	 * Executes a JSON-RPC command and returns the result as JSON object.
	 * @param manager     Upper layer reference for error posting
	 * @param method      Name of the method to run
	 * @param parameters  Parameters of the method
	 * @return Result
	 */
	public JsonNode getJson(INotifiableManager manager, String method, JsonNode parameters) {
		try {
			final JsonNode response = query(method, parameters, manager);
			final JsonNode result = response.get(RESULT_FIELD);
			if (result == null) {
				if (response.get(ERROR_FIELD) == null) {
					throw new Exception("Weird JSON response, could not parse error.");
				}else if(!response.get(ERROR_FIELD).get("message").getTextValue().equals("Invalid params.")){
					throw new Exception(response.get(ERROR_FIELD).get("message").getTextValue());
				}else{
					Log.d(TAG, "Request returned Invalid Params.");
				}
			} else {
				return response.get(RESULT_FIELD);
			}
		} catch (Exception e) {
			manager.onError(e);
		}
		return Client.obj();
	}
	
	public JsonNode getJson(INotifiableManager manager, String method, JsonNode parameters, String resultField) {
		try {
			final JsonNode response = getJson(manager, method, parameters);
			final JsonNode result = response.get(resultField);
			if (result == null) {
				throw new Exception("Could not find field \"" + resultField + "\" as return value.");
			} else {
				return response.get(resultField);
			}
		} catch (Exception e) {
			manager.onError(e);
		}
		return Client.obj();
	}
	
	/**
	 * Executes a JSON-RPC command without parameters and returns the result as
	 * JSON object.
	 * @param manager     Upper layer reference for error posting
	 * @param method      Name of the method to run
	 * @return Result
	 */
	public JsonNode getJson(INotifiableManager manager, String method) {
		return query(method, null, manager).get(RESULT_FIELD);
	}
	
	/**
	 * Executes an JSON-RPC method and returns the result from a field as string.
	 * @param manager     Upper layer reference for error posting
	 * @param method      Name of the method to run
	 * @param parameters  Parameters of the method, separated by ";"
	 * @param returnField Name of the field to return
	 * @return Result
	 */
	public String getString(INotifiableManager manager, String method, ObjectNode parameters, String returnField) {
		final JsonNode result = (JsonNode)query(method, parameters, manager).get(RESULT_FIELD);
		if(returnField == null)
			return result == null ? "" : result.getValueAsText();
		else
			return result == null ? "" : result.get(returnField).getValueAsText();
	}
	
		
	public String getString(INotifiableManager manager, String method, ObjectNode parameters) {
		return getString(manager, method, parameters, null);
	}
	
	/**
	 * Executes an JSON-RPC method and returns the result from a field as integer.
	 * @param manager     Upper layer reference for error posting
	 * @param method      Name of the method to run
	 * @param parameters  Parameters of the method, separated by ";"
	 * @param returnField Name of the field to return
	 * @return Result as integer
	 */
	public int getInt(INotifiableManager manager, String method, ObjectNode parameters, String returnField) {
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
	public boolean getBoolean(INotifiableManager manager, String method, ObjectNode parameters, String returnField) {
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
	public boolean getBoolean(INotifiableManager manager, String method, ObjectNode parameters) {
		return getBoolean(manager, method, parameters, null);
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
	public static final String ERROR_FIELD = "error";
}

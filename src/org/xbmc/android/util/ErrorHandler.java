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

package org.xbmc.android.util;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import org.apache.http.HttpException;
import org.xbmc.android.remote.activity.SettingsActivity;
import org.xbmc.httpapi.IErrorHandler;
import org.xbmc.httpapi.NoNetworkException;
import org.xbmc.httpapi.NoSettingsException;
import org.xbmc.httpapi.WrongDataFormatException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;

/**
 * The idea of the error handler is to have something global which is called
 * when an exception occurs. Basically it implements a <code>handle()</code>
 * method which will then further treat the error, depending on the kind of
 * exception that was thrown.
 * <br />
 * For now, we pop up an alert dialog with some message. Since the pop up will
 * be opened on the page the current activity refers to (and not on the current
 * page), we'll have to use the <code>setActivity()</code> method in our
 * activities.
 * <br />
 * Anyway this is how it is for now, maybe later we could change it to a less
 * obtrusive message.
 * 
 * @author Team XBMC
 */
public class ErrorHandler implements IErrorHandler {
	
	private static Context sContext;
	
	/**
	 * An activity is needed for the (potential) GUI stuff.
	 * @param activity
	 */
	public ErrorHandler(Context context) {
		sContext = context;
	}
	
	public static void setActivity(Context context) {
		sContext = context;
	}
	
	/**
	 * Handles an exception.
	 * @param exception Exception to handle.
	 */
	public void handle(Exception exception) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(sContext);
		try {
			throw exception;
		} catch (NoSettingsException e) {
			builder.setTitle("No Settings detected");
			builder.setMessage(e.getMessage());
			builder.setNeutralButton("Settings", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					sContext.startActivity(new Intent(sContext, SettingsActivity.class));
				}
			});
		} catch (NoNetworkException e) {
			builder.setTitle("No Network");
			builder.setMessage(e.getMessage());
			builder.setCancelable(true);
			builder.setNeutralButton("Settings", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					sContext.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
				}
			});
		} catch (WrongDataFormatException e) {
			builder.setTitle("Internal Error");
			builder.setMessage("Wrong data from HTTP API; expected '" + e.getExpected() + "', got '" + e.getReceived() + "'.");
		} catch (SocketTimeoutException e) {
			builder.setTitle("Socket Timeout");
			builder.setMessage("Make sure XBMC webserver is enabled and XBMC is running.");
			builder.setNeutralButton("Settings", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					sContext.startActivity(new Intent(sContext, SettingsActivity.class));
				}
			});
		} catch (ConnectException e) {
			builder.setTitle("Connection Refused");
			builder.setMessage("Make sure XBMC webserver is enabled and XBMC is running.");
			builder.setNeutralButton("Settings", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					sContext.startActivity(new Intent(sContext, SettingsActivity.class));
				}
			});
		} catch (IOException e) {
			if (e.getMessage().startsWith("Network unreachable")) {
				builder.setTitle("No network");
				builder.setMessage("XBMC Remote needs local network access. Please make sure that your wireless network is activated. You can click on the Settings button below to directly access your network settings.");
				builder.setNeutralButton("Settings", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						sContext.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
					}
				});
			} else {
				builder.setTitle("Unknown I/O Exception");
				builder.setMessage(e.getMessage().toString());
			}
		} catch (HttpException e) {
			if (e.getMessage().startsWith("401")) {
				builder.setTitle("HTTP 401: Unauthorized");
				builder.setMessage("The supplied username and/or password is incorrect. Please check your settings.");
				builder.setNeutralButton("Settings", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						sContext.startActivity(new Intent(sContext, SettingsActivity.class));
					}
				});
			}
		} catch (Exception e) {
			builder.setTitle("Exception");
			builder.setMessage(e.getStackTrace().toString());
		} finally {
			
			exception.printStackTrace();
			
			builder.setCancelable(true);
			builder.setNegativeButton("Close", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			
			final AlertDialog alert = builder.create();
			try {
				alert.show();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
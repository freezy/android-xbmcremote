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

package org.xbmc.android.remote.presentation.controller;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import org.apache.http.HttpException;
import org.xbmc.android.remote.presentation.activity.SettingsActivity;
import org.xbmc.android.util.HostFactory;
import org.xbmc.httpapi.NoNetworkException;
import org.xbmc.httpapi.NoSettingsException;
import org.xbmc.httpapi.WrongDataFormatException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.widget.Toast;

/**
 * Every controller should extend this class. Takes care of the messages.
 * 
 * @author Team XBMC
 */
public abstract class AbstractController {
	
	protected Activity mActivity;
	private boolean mDialogShowing = false;
	
	public void onCreate(Activity activity) {
		mActivity = activity;
		HostFactory.readHost(activity.getApplicationContext());
	}
	
	public void onError(Exception exception) {
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		try {
			throw exception;
		} catch (NoSettingsException e) {
			builder.setTitle("No hosts detected");
			builder.setMessage(e.getMessage());
			builder.setNeutralButton("Settings", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					mActivity.startActivity(new Intent(mActivity, SettingsActivity.class));
					mDialogShowing = false;
				}
			});
		} catch (NoNetworkException e) {
			builder.setTitle("No Network");
			builder.setMessage(e.getMessage());
			builder.setCancelable(true);
			builder.setNeutralButton("Settings", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					mActivity.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
					mDialogShowing = false;
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
					mActivity.startActivity(new Intent(mActivity, SettingsActivity.class));
					mDialogShowing = false;
				}
			});
		} catch (ConnectException e) {
			builder.setTitle("Connection Refused");
			builder.setMessage("Make sure XBMC webserver is enabled and XBMC is running.");
			builder.setNeutralButton("Settings", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					mActivity.startActivity(new Intent(mActivity, SettingsActivity.class));
					mDialogShowing = false;
				}
			});
		} catch (IOException e) {
			if (e.getMessage().startsWith("Network unreachable")) {
				builder.setTitle("No network");
				builder.setMessage("XBMC Remote needs local network access. Please make sure that your wireless network is activated. You can click on the Settings button below to directly access your network settings.");
				builder.setNeutralButton("Settings", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mActivity.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
						mDialogShowing = false;
					}
				});
			} else {
				builder.setTitle("I/O Exception (" + e.getClass().getCanonicalName() + ")");
				builder.setMessage(e.getMessage().toString());
			}
		} catch (HttpException e) {
			if (e.getMessage().startsWith("401")) {
				builder.setTitle("HTTP 401: Unauthorized");
				builder.setMessage("The supplied username and/or password is incorrect. Please check your settings.");
				builder.setNeutralButton("Settings", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mActivity.startActivity(new Intent(mActivity, SettingsActivity.class));
						mDialogShowing = false;
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
					mDialogShowing = false;
//					ConnectionManager.resetClient();
				}
			});
			
			mActivity.runOnUiThread(new Runnable() {
				public void run() {
					final AlertDialog alert = builder.create();
					try {
						if (!mDialogShowing) {
							alert.show();
							mDialogShowing = true;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	public void onMessage(final String message) {
		mActivity.runOnUiThread(new Runnable() {
			public void run() {
				Toast toast = Toast.makeText(mActivity, message, Toast.LENGTH_LONG);
				toast.show();
			}
		});
	}

	public void runOnUI(Runnable action) {
		if (mActivity != null) {
			mActivity.runOnUiThread(action);
		}
	}
	
	public void onActivityPause() {
		mActivity = null;
	}

	public void onActivityResume(Activity activity) {
		mActivity = activity;
	}
}
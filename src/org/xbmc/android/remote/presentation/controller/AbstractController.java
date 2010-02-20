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
import org.xbmc.android.remote.business.Command;
import org.xbmc.android.remote.presentation.activity.SettingsActivity;
import org.xbmc.android.util.HostFactory;
import org.xbmc.android.util.WifiHelper;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.object.Host;
import org.xbmc.httpapi.NoNetworkException;
import org.xbmc.httpapi.NoSettingsException;
import org.xbmc.httpapi.WrongDataFormatException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.widget.Toast;

/**
 * Every controller should extend this class. Takes care of the messages.
 * 
 * @author Team XBMC
 */
public abstract class AbstractController {
	
	protected Activity mActivity;
	private boolean mDialogShowing = false;
	public static final int MAX_WAIT_FOR_WIFI = 20;
	private Thread mWaitForWifi;
	public void onCreate(Activity activity) {
		mActivity = activity;
		HostFactory.readHost(activity.getApplicationContext());
	}
	
	public void onWrongConnectionState(int state, final INotifiableManager manager, final Command<?> source) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		switch (state) {
		case WifiHelper.WIFI_STATE_DISABLED:
			builder.setTitle("Wifi disabled");
			builder.setMessage("This host is Wifi only. Should I activate Wifi?");
			builder.setNeutralButton("Activate Wifi", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					final ProgressDialog pd = new ProgressDialog(mActivity);
					pd.setCancelable(true);
					pd.setTitle("Activating Wifi");
					pd.setMessage("Please wait while Wifi is activated.");
					pd.show();
					(new Thread() {
						public void run() {
							final WifiHelper helper = WifiHelper.getInstance(mActivity);
							helper.enableWifi(true);
							int wait = 0;
							while(wait <= MAX_WAIT_FOR_WIFI * 1000 && helper.getWifiState() != WifiHelper.WIFI_STATE_ENABLED) {
								try {
									sleep(500);
									wait += 500;
								} catch (InterruptedException e) {}
							}
							manager.retryAll();
							pd.cancel();
							mDialogShowing = false;
							
						}
					}).start();
				}
			});
			showDialog(builder);
			break;
		case WifiHelper.WIFI_STATE_ENABLED:
			final Host host = HostFactory.host;
			final WifiHelper helper = WifiHelper.getInstance(mActivity);
			final String msg; 
			if(host != null && host.access_point != null && !host.access_point.equals("")) {
				helper.connect(host);
				msg = "Connecting to " + host.access_point +  ". Please wait";
			} else {
				msg = "Waiting for Wifi to connect to your LAN."; 
			}
			final ProgressDialog pd = new ProgressDialog(mActivity);
			pd.setCancelable(true);
			pd.setTitle("Connecting");
			pd.setMessage(msg);
			mWaitForWifi = new Thread() {
				public void run() {
					mDialogShowing = true;
					pd.show();
					(new Thread( ) {
						public void run() {
							int wait = 0;
							while(wait <= MAX_WAIT_FOR_WIFI * 1000 && helper.getWifiState() != WifiHelper.WIFI_STATE_CONNECTED) {
								try {
									sleep(500);
									wait += 500;
								} catch (InterruptedException e) {}
							}
							pd.cancel();
							mDialogShowing = false;
						}
					}).start();
					pd.setOnDismissListener(new OnDismissListener() {
						public void onDismiss(DialogInterface dialog) {
							if(helper.getWifiState() != WifiHelper.WIFI_STATE_CONNECTED) {
								builder.setTitle("Wifi doesn't seem to connect");
								builder.setMessage("You can open the Wifi settings or wait "+ MAX_WAIT_FOR_WIFI +" seconds");
								builder.setNeutralButton("Wifi Settings", new OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										mDialogShowing = false;
									}
								});
								builder.setCancelable(true);
								builder.setNegativeButton("Wait", new OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										mDialogShowing = false;
										mActivity.runOnUiThread(mWaitForWifi); //had to make the Thread a field because of this line
									}
								});
								
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
						}
						
					});
				}
			};
			mActivity.runOnUiThread(mWaitForWifi);
		}
		
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
			
			
		}
	}
	
	protected void showDialog(final AlertDialog.Builder builder) {
		builder.setCancelable(true);
		builder.setNegativeButton("Close", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				mDialogShowing = false;
//				ConnectionManager.resetClient();
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
//		mActivity = null;
	}

	public void onActivityResume(Activity activity) {
		mActivity = activity;
	}
}
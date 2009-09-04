package org.xbmc.android.util;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.xbmc.android.remote.activity.SettingsActivity;
import org.xbmc.httpapi.IErrorHandler;
import org.xbmc.httpapi.NoNetworkException;
import org.xbmc.httpapi.WrongDataFormatException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;

public class ErrorHandler implements IErrorHandler {
	
	private static Activity sActivity;
	
	public ErrorHandler(Activity activity) {
		sActivity = activity;
	}
	
	public static void setActivity(Activity activity) {
		sActivity = activity;
	}
	
	public void handle(Exception exception) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(sActivity);
		try {
			throw exception;
		} catch (NoNetworkException e) {
			builder.setTitle("No Network");
			builder.setMessage(e.getMessage());
			builder.setCancelable(true);
			builder.setNeutralButton("Settings", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					sActivity.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
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
					sActivity.startActivity(new Intent(sActivity, SettingsActivity.class));
				}
			});
		} catch (IOException e) {
			builder.setTitle("Unknown I/O Exception");
			builder.setMessage(e.getMessage().toString());
		} catch (Exception e) {
			builder.setTitle("Exception");
			builder.setMessage(e.getStackTrace().toString());
		} finally {
			
			builder.setCancelable(true);
			builder.setNegativeButton("Close", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			final AlertDialog alert = builder.create();
			alert.show();
		}
	}
}
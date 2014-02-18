package org.xbmc.android.remote.presentation.appwidget;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.presentation.controller.AppWidgetRemoteController;
import org.xbmc.android.util.HostFactory;
import org.xbmc.eventclient.ButtonCodes;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * Base class for the remote controller widget based on RemoteController
 * activity in portrait mode.
 * 
 * @author Heikki Hämäläinen
 * 
 */
public class RemoteControllerWidget extends AppWidgetProvider {

	public static final String EXTRA_ITEM = "com.example.android.stackwidget.EXTRA_ITEM";
	public static final String ACTION_WIDGET_CONTROL = "org.xbmc.android.remote.WIDGET_CONTROL";
	public static final String URI_SCHEME = "remote_controller_widget";

	private int mWidgetLayoutId = R.layout.widget_xbox;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// If the app is not initialized this should cause it to try connect to
		// the latest host and we also avoid noSettings exceptions
		HostFactory.readHost(context);
		
		ComponentName thisWidget = new ComponentName(context,
				RemoteControllerWidget.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		// Loop for all widgets
		for (int widgetId : allWidgetIds) {
			RemoteViews remoteView = new RemoteViews(context.getPackageName(), mWidgetLayoutId);
			attachPendingIntents(context, remoteView, widgetId);
		}
	}

	/**
	 * Attaches pending intents to widget buttons
	 * 
	 * @param context
	 * @param remoteView
	 * @param widgetId
	 */
	private void attachPendingIntents(Context context, RemoteViews remoteView,
			int widgetId) {
		// First row
		AppWidgetRemoteController.setupWidgetButton(remoteView,
				R.id.RemoteXboxWidgetImgBtnDisplay, context, this,
				ButtonCodes.REMOTE_DISPLAY, widgetId, URI_SCHEME,
				ACTION_WIDGET_CONTROL);

		// Third row
		AppWidgetRemoteController.setupWidgetButton(remoteView,
				R.id.RemoteXboxWidgetImgBtnSeekBack, context, this,
				ButtonCodes.REMOTE_REVERSE, widgetId, URI_SCHEME,
				ACTION_WIDGET_CONTROL);
		
		AppWidgetRemoteController.setupWidgetButton(remoteView,
				R.id.RemoteXboxWidgetImgBtnPlay, context, this,
				ButtonCodes.REMOTE_PLAY, widgetId, URI_SCHEME,
				ACTION_WIDGET_CONTROL);
		AppWidgetRemoteController.setupWidgetButton(remoteView,
				R.id.RemoteXboxWidgetImgBtnSeekForward, context, this,
				ButtonCodes.REMOTE_FORWARD, widgetId, URI_SCHEME,
				ACTION_WIDGET_CONTROL);

		// Fourth row
		AppWidgetRemoteController.setupWidgetButton(remoteView,
				R.id.RemoteXboxWidgetImgBtnPrevious, context, this,
				ButtonCodes.REMOTE_SKIP_MINUS, widgetId, URI_SCHEME,
				ACTION_WIDGET_CONTROL);
		AppWidgetRemoteController.setupWidgetButton(remoteView,
				R.id.RemoteXboxWidgetImgBtnStop, context, this,
				ButtonCodes.REMOTE_STOP, widgetId, URI_SCHEME,
				ACTION_WIDGET_CONTROL);
		AppWidgetRemoteController.setupWidgetButton(remoteView,
				R.id.RemoteXboxWidgetImgBtnPause, context, this,
				ButtonCodes.REMOTE_PAUSE, widgetId, URI_SCHEME,
				ACTION_WIDGET_CONTROL);
		AppWidgetRemoteController.setupWidgetButton(remoteView,
				R.id.RemoteXboxWidgetImgBtnNext, context, this,
				ButtonCodes.REMOTE_SKIP_PLUS, widgetId, URI_SCHEME,
				ACTION_WIDGET_CONTROL);

		// Fifth row
		AppWidgetRemoteController.setupWidgetButton(remoteView,
				R.id.RemoteXboxWidgetImgBtnTitle, context, this,
				ButtonCodes.REMOTE_TITLE, widgetId, URI_SCHEME,
				ACTION_WIDGET_CONTROL);
		AppWidgetRemoteController.setupWidgetButton(remoteView,
				R.id.RemoteXboxWidgetImgBtnUp, context, this,
				ButtonCodes.REMOTE_UP, widgetId, URI_SCHEME,
				ACTION_WIDGET_CONTROL);
		AppWidgetRemoteController.setupWidgetButton(remoteView,
				R.id.RemoteXboxWidgetImgBtnInfo, context, this,
				ButtonCodes.REMOTE_INFO, widgetId, URI_SCHEME,
				ACTION_WIDGET_CONTROL);

		// Sixth row
		AppWidgetRemoteController.setupWidgetButton(remoteView,
				R.id.RemoteXboxWidgetImgBtnLeft, context, this,
				ButtonCodes.REMOTE_LEFT, widgetId, URI_SCHEME,
				ACTION_WIDGET_CONTROL);
		AppWidgetRemoteController.setupWidgetButton(remoteView,
				R.id.RemoteXboxWidgetImgBtnSelect, context, this,
				ButtonCodes.REMOTE_SELECT, widgetId, URI_SCHEME,
				ACTION_WIDGET_CONTROL);
		AppWidgetRemoteController.setupWidgetButton(remoteView,
				R.id.RemoteXboxWidgetImgBtnRight, context, this,
				ButtonCodes.REMOTE_RIGHT, widgetId, URI_SCHEME,
				ACTION_WIDGET_CONTROL);

		// Seventh row
		AppWidgetRemoteController.setupWidgetButton(remoteView,
				R.id.RemoteXboxWidgetImgBtnMenu, context, this,
				ButtonCodes.REMOTE_MENU, widgetId, URI_SCHEME,
				ACTION_WIDGET_CONTROL);
		AppWidgetRemoteController.setupWidgetButton(remoteView,
				R.id.RemoteXboxWidgetImgBtnDown, context, this,
				ButtonCodes.REMOTE_DOWN, widgetId, URI_SCHEME,
				ACTION_WIDGET_CONTROL);
		AppWidgetRemoteController.setupWidgetButton(remoteView,
				R.id.RemoteXboxWidgetImgBtnBack, context, this,
				ButtonCodes.REMOTE_BACK, widgetId, URI_SCHEME,
				ACTION_WIDGET_CONTROL);

		AppWidgetManager.getInstance(context).updateAppWidget(widgetId,
				remoteView);
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		final String action = intent.getAction();

		if (action.equals(ACTION_WIDGET_CONTROL)) {

			Bundle extras = intent.getExtras();
			if (extras.containsKey(AppWidgetRemoteController.COMMAND)) {
				Log.i("onReceive", "Send Key");
				// The xbmc app may be dead so we need to initialize the host
				// settings via Host Factory
				HostFactory.readHost(context);
				AppWidgetRemoteController mRemoteController = new AppWidgetRemoteController(context);
				mRemoteController.sendButton(extras.getString(AppWidgetRemoteController.COMMAND));
			}
			if (extras.containsKey(AppWidgetRemoteController.ERROR_MESSAGE)) {
				// Error is most probably connection refused or socket timeout
				// TODO Different error, different message
				
				Toast.makeText(context, context.getString(extras.getInt(AppWidgetRemoteController.ERROR_MESSAGE)),
						Toast.LENGTH_SHORT).show();
			}
		}

		super.onReceive(context, intent);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
		int minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

		// https://developer.android.com/guide/practices/ui_guidelines/widget_design.html#cellstable
		if (minHeight < 230) {
			mWidgetLayoutId = R.layout.widget_xbox_small;
		} else {
			mWidgetLayoutId = R.layout.widget_xbox;
		}

		RemoteViews remoteView = new RemoteViews(context.getPackageName(), mWidgetLayoutId);
		attachPendingIntents(context, remoteView, appWidgetId);

		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
	}
}

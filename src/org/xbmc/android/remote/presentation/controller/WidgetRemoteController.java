package org.xbmc.android.remote.presentation.controller;

import java.net.ConnectException;

import org.xbmc.android.remote.business.Command;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.widget.RemoteControllerWidget;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.presentation.INotifiableController;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;

import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class WidgetRemoteController extends RemoteController implements
		INotifiableController {
	private static final String COMMAND = "command";
	private Context context;
	
	public WidgetRemoteController(Context context) {
		super(context);
		this.context = context;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onWrongConnectionState(int state, INotifiableManager manager,
			Command<?> source) {
		// TODO Auto-generated method stub
		Log.w("onWrongConnectionState","Jep");
		super.onWrongConnectionState(state, manager, source);
	}

	@Override
	public void onError(Exception exception) {
		
		if (context != null){
				Log.w("onCatch","Jep");
				Intent active = new Intent(context, RemoteControllerWidget.class);				
				active.setAction(RemoteControllerWidget.ACTION_WIDGET_CONTROL);
				// active.setData(Uri.parse(active.toUri(Intent.URI_INTENT_SCHEME)));
				active.putExtra("ERROR", exception.getClass().toString());
		        context.sendBroadcast(active);
		}
	}
	
	@Override
	public void onMessage(final String message){
		Log.w("onMessage",message);
	}
	
	
	
	/**
	 * 
	 * @param viewId
	 * @param buttonCode
	 * @param widgetId
	 */
	public void setupWidgetButton(RemoteViews remoteView, int viewId, Context context, Object caller,  String buttonCode, int widgetId, String uri, String action){
	
        Intent active = new Intent(context, caller.getClass());
        active.setAction(action);
        active.putExtra(COMMAND, buttonCode);
        // Make this pending intent unique to prevent updating other intents
        Uri data = Uri.withAppendedPath(Uri.parse(uri + "://widget/id/#"+buttonCode), String.valueOf(widgetId));	
		active.setData(data);
        remoteView.setOnClickPendingIntent(viewId, PendingIntent.getBroadcast(context, 0, active, PendingIntent.FLAG_UPDATE_CURRENT));
	}
	
}

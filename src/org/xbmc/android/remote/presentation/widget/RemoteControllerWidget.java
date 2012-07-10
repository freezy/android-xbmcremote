package org.xbmc.android.remote.presentation.widget;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.presentation.controller.WidgetRemoteController;
import org.xbmc.android.util.HostFactory;
import org.xbmc.eventclient.ButtonCodes;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;


public class RemoteControllerWidget extends AppWidgetProvider {

	
	public static final String EXTRA_ITEM = "com.example.android.stackwidget.EXTRA_ITEM";
	public static final String ACTION_WIDGET_CONTROL = "org.xbmc.android.remote.WIDGET_CONTROL";
	public static final String URI_SCHEME = "remote_controller_widget";
	private static final String COMMAND = "command";
	private WidgetRemoteController mRemoteController;
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		HostFactory.readHost(context);
		mRemoteController = new WidgetRemoteController(context);
		
		//ManagerFactory.getEventClientManager(mRemoteController);
		
//		mConfigurationManager = ConfigurationManager.getInstance(this);
		// Get all ids
		ComponentName thisWidget = new ComponentName(context,
				RemoteControllerWidget.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		// get orientation
		
		
		for (int widgetId : allWidgetIds) {
			// Create some random data
			
			
			RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_xbox);
			attachPendingIntents(context, remoteView, widgetId);	
			
		}

		
	}
	/**
	 * Attaches pending intents to widget buttons
	 * @param context
	 * @param remoteView
	 * @param widgetId
	 */
	  private void attachPendingIntents(Context context, RemoteViews remoteView,
			int widgetId) {
		  // First row 
		  mRemoteController.setupWidgetButton(remoteView, R.id.RemoteXboxWidgetImgBtnDisplay,context, this, ButtonCodes.REMOTE_DISPLAY, widgetId, URI_SCHEME,ACTION_WIDGET_CONTROL);
		  
		  // Third row
		  mRemoteController.setupWidgetButton(remoteView, R.id.RemoteXboxWidgetImgBtnSeekBack,context, this, ButtonCodes.REMOTE_REVERSE, widgetId,URI_SCHEME, ACTION_WIDGET_CONTROL);
		  mRemoteController.setupWidgetButton(remoteView, R.id.RemoteXboxWidgetImgBtnPlay,context, this, ButtonCodes.REMOTE_PLAY, widgetId,URI_SCHEME, ACTION_WIDGET_CONTROL);
		  mRemoteController.setupWidgetButton(remoteView, R.id.RemoteXboxWidgetImgBtnSeekForward,context, this, ButtonCodes.REMOTE_FORWARD, widgetId,URI_SCHEME, ACTION_WIDGET_CONTROL);
		   
		  // Fourth row		  
		  mRemoteController.setupWidgetButton(remoteView, R.id.RemoteXboxWidgetImgBtnPrevious,context, this, ButtonCodes.REMOTE_SKIP_MINUS, widgetId,URI_SCHEME,ACTION_WIDGET_CONTROL);
		  mRemoteController.setupWidgetButton(remoteView, R.id.RemoteXboxWidgetImgBtnPlay,context, this, ButtonCodes.REMOTE_STOP, widgetId,URI_SCHEME,ACTION_WIDGET_CONTROL);		  
		  mRemoteController.setupWidgetButton(remoteView, R.id.RemoteXboxWidgetImgBtnPause,context, this, ButtonCodes.REMOTE_PAUSE, widgetId,URI_SCHEME,ACTION_WIDGET_CONTROL);		  
		  mRemoteController.setupWidgetButton(remoteView, R.id.RemoteXboxWidgetImgBtnNext,context, this, ButtonCodes.REMOTE_SKIP_PLUS, widgetId,URI_SCHEME,ACTION_WIDGET_CONTROL);
		  
		  // Fifth row
		  mRemoteController.setupWidgetButton(remoteView, R.id.RemoteXboxWidgetImgBtnTitle,context, this, ButtonCodes.REMOTE_TITLE, widgetId,URI_SCHEME,ACTION_WIDGET_CONTROL);
		  mRemoteController.setupWidgetButton(remoteView, R.id.RemoteXboxWidgetImgBtnUp,context, this, ButtonCodes.REMOTE_UP, widgetId,URI_SCHEME,ACTION_WIDGET_CONTROL);
		  mRemoteController.setupWidgetButton(remoteView, R.id.RemoteXboxWidgetImgBtnInfo,context, this, ButtonCodes.REMOTE_INFO, widgetId,URI_SCHEME,ACTION_WIDGET_CONTROL);
		  
		  // Sixth row		  		  
		  mRemoteController.setupWidgetButton(remoteView, R.id.RemoteXboxWidgetImgBtnLeft,context, this, ButtonCodes.REMOTE_LEFT, widgetId,URI_SCHEME,ACTION_WIDGET_CONTROL);
		  mRemoteController.setupWidgetButton(remoteView, R.id.RemoteXboxWidgetImgBtnSelect,context, this, ButtonCodes.REMOTE_SELECT, widgetId,URI_SCHEME,ACTION_WIDGET_CONTROL);		  
		  mRemoteController.setupWidgetButton(remoteView, R.id.RemoteXboxWidgetImgBtnRight,context, this, ButtonCodes.REMOTE_RIGHT, widgetId,URI_SCHEME,ACTION_WIDGET_CONTROL);
		  
		  // Seventh row
		  mRemoteController.setupWidgetButton(remoteView, R.id.RemoteXboxWidgetImgBtnMenu,context, this, ButtonCodes.REMOTE_MENU, widgetId,URI_SCHEME,ACTION_WIDGET_CONTROL);
		  mRemoteController.setupWidgetButton(remoteView, R.id.RemoteXboxWidgetImgBtnDown,context, this, ButtonCodes.REMOTE_DOWN, widgetId,URI_SCHEME,ACTION_WIDGET_CONTROL);
		  mRemoteController.setupWidgetButton(remoteView, R.id.RemoteXboxWidgetImgBtnBack,context, this, ButtonCodes.REMOTE_BACK, widgetId,URI_SCHEME,ACTION_WIDGET_CONTROL);
		  
		  AppWidgetManager.getInstance(context).updateAppWidget(widgetId, remoteView);
	}

	@Override
	    public void onReceive(Context context, Intent intent) {
			Log.i("onReceive","alkaa");
			if (intent.getAction() != null){
				//Log.i("Tänne",intent.getAction());
				// Log.i("INTENT", intent.toString());
			}
			
			
			final String action = intent.getAction();
			 
	        if (action.equals(ACTION_WIDGET_CONTROL)){
	        	Log.i("onReceive","Action");
	        	
	        	Bundle b = intent.getExtras();
	        	if (b.containsKey(COMMAND)){
	        		Log.i("onReceive","Send Key");
	        		// ManagerThread.get();
	        		HostFactory.readHost(context);
	        		mRemoteController = new WidgetRemoteController(context);
	        		// ManagerFactory.getEventClientManager(mRemoteController);
	        		mRemoteController.sendButton(b.getString(COMMAND));
	        		
					
	        	}
	        	if (b.containsKey("ERROR")){
	        		
	        		Log.i("onReceive","error");
	        		Toast.makeText(context, b.getString("ERROR"), Toast.LENGTH_SHORT).show();
					
	        	}
	        	
	        }
	       
	        super.onReceive(context, intent);
	    }

}
		
/*
 *      WTFPL - http://sam.zoy.org/wtfpl/
 *      Sylvain Galand - http://slvn.fr
 */


package org.xbmc.android.remote.appwidget;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.service.RemoteCommandService;
import org.xbmc.android.remote.presentation.activity.HomeActivity;
import org.xbmc.eventclient.ButtonCodes;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class RemoteAppWidgetProvider extends AppWidgetProvider {
	
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	    	
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.remote_widget_layout);
            
            // Set the XBMC app shortcut
            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, HomeActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.widget_app, pendingIntent);
            
            // Set all pending Intent for common buttons
            setIntent( views, context, R.id.widget_play, 	ButtonCodes.KEYBOARD_PLAY_PAUSE );
            setIntent( views, context, R.id.widget_up,		ButtonCodes.REMOTE_UP );
            setIntent( views, context, R.id.widget_stop,	ButtonCodes.REMOTE_STOP );
            setIntent( views, context, R.id.widget_left,	ButtonCodes.REMOTE_LEFT );
            setIntent( views, context, R.id.widget_action,	ButtonCodes.REMOTE_SELECT );
            setIntent( views, context, R.id.widget_right,	ButtonCodes.REMOTE_RIGHT );
            setIntent( views, context, R.id.widget_down,	ButtonCodes.REMOTE_DOWN );
            setIntent( views, context, R.id.widget_back,	ButtonCodes.REMOTE_BACK );
            
            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
            
            // Start service
            context.startService(new Intent(context, RemoteCommandService.class));
        }
    }
    
    @Override
    public void onDisabled(Context context) {
        context.stopService(new Intent(context, RemoteCommandService.class));
        super.onDisabled(context);
    }
    
    private void setIntent(RemoteViews views, Context context, int id, String action) {
    	views.setOnClickPendingIntent(id,
    			getPendingIntent(context, id, action));
    }
    
    private PendingIntent getPendingIntent(Context context, int id, String action) {
        Intent intent = new Intent(context, RemoteCommandService.class);
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
        intent.putExtra(RemoteCommandService.BUTTON_COMMAND, action);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
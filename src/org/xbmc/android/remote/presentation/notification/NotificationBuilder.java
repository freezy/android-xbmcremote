package org.xbmc.android.remote.presentation.notification;

import org.xbmc.android.remote.presentation.activity.NowPlayingActivity;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;

public class NotificationBuilder {
    /**
     * Return the richest NotificationBuilder that will work on this platform.
     */
    public static NotificationBuilder getInstance(Context context) {
        if (Integer.valueOf(Build.VERSION.SDK) >= Build.VERSION_CODES.JELLY_BEAN) {
            return new BigPictureNotificationBuilder(context);
        }

        if (Integer.valueOf(Build.VERSION.SDK) >= Build.VERSION_CODES.HONEYCOMB) {
            return new LargeIconNotificationBuilder(context);
        }

        return new NotificationBuilder(context);
    }

    protected final PendingIntent mIntent;
    protected final Context mContext;

    protected NotificationBuilder(Context context) {
        mContext = context;
        final Intent actintent = new Intent(mContext, NowPlayingActivity.class);
        actintent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        mIntent = PendingIntent.getActivity(mContext, 0, actintent, 0);
    }

    /**
     * Create a simple notification. Subclasses may take advantage of newer APIs.
     * @param title
     * @param text
     * @param icon The id of a drawable to be used as the small icon. Will display on all platforms.
     * @param thumb A bitmap representing the currently playing item. Ignored on lower API levels.
     * @return
     */
    public Notification build(String title, String text, int icon, Bitmap thumb) {
        Notification notification = new Notification(icon, title, System.currentTimeMillis());
        notification.setLatestEventInfo(mContext, title, text, mIntent);
        return finalize(notification);
    }

    /**
     * Perform modifications to a notification that apply to all API levels. All definitions of
     * buildNotification should call this before returning.
     */
    protected Notification finalize(Notification notification) {
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        return notification;
    }
}

package org.xbmc.android.remote.presentation.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;

@TargetApi(11)
/**
 * On Honeycomb (level 11) and up, we can include a thumbnail in addition to an icon.
 */
public class LargeIconNotificationBuilder extends NotificationBuilder {
    protected LargeIconNotificationBuilder(Context context) {
        super(context);
    }

    protected Notification.Builder getBuilder(String title, String text, int icon, Bitmap thumb) {
        Notification.Builder largeIconNotificationBuilder = new Notification.Builder(mContext)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(icon)
            .setLargeIcon(thumb)
            .setContentIntent(mIntent);
        return largeIconNotificationBuilder;
    }

    @Override
    public Notification build(String title, String text, int icon, Bitmap thumb) {
        Notification.Builder builder = getBuilder(title, text, icon, thumb);
        Notification notification = builder.getNotification();
        return finalize(notification);
    }
}

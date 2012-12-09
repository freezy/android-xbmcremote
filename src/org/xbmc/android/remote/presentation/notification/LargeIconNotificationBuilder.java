package org.xbmc.android.remote.presentation.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.content.res.Resources;
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
        // Left to its own devices, Notification.Builder will crop the thumbnail
        // to the size of the view largeIcon goes into. This is rarely an
        // informative image, so we crop the largest possible square from the
        // center of the thumbnail and use that.
        Bitmap scaledThumb = null;
        if (thumb != null) {
            Resources resources = mContext.getResources();
            int iconWidth = resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
            int iconHeight = resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
            int thumbWidth = thumb.getWidth();
            int thumbHeight = thumb.getHeight();
            if (thumbWidth > thumbHeight) {
                iconWidth = (int) (((float) iconHeight / thumbHeight) * thumbWidth);
            } else {
                iconHeight = (int) (((float) iconWidth / thumbWidth) * thumbHeight);
            }
            scaledThumb = Bitmap.createScaledBitmap(thumb, iconWidth, iconHeight, false);
        }
        Notification.Builder largeIconNotificationBuilder = new Notification.Builder(mContext)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(icon)
            .setLargeIcon(scaledThumb)
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

package org.xbmc.android.remote.presentation.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

@TargetApi(16)
/**
 * On Jelly Bean (level 16) and up, we can build on a large icon notification to expand the
 * thumbnail and expose playback controls.
 */
public class BigPictureNotificationBuilder extends LargeIconNotificationBuilder {

    protected BigPictureNotificationBuilder(Context context) {
        super(context);
    }

    @Override
    public Notification build(String title, String text, int icon, Bitmap thumb) {
        Notification.Builder superBuilder = super.getBuilder(title, text, icon, thumb);
        if(thumb == null){
	        superBuilder
	        	.setSmallIcon(icon)
	        	.setContentText(text);
	        return finalize(superBuilder.build());
        }
        else{
	        Notification  notification = new Notification.BigPictureStyle(superBuilder)
	            .bigPicture(thumb)
	            .bigLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
	            .setSummaryText(text)
	            .build();
	        return finalize(notification);
        }
    }
}

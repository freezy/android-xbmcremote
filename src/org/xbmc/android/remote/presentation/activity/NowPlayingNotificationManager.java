package org.xbmc.android.remote.presentation.activity;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.NowPlayingPollerThread;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.api.data.IControlClient.ICurrentlyPlaying;
import org.xbmc.api.data.IControlClient.PlayStatus;
import org.xbmc.httpapi.type.MediaType;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

public class NowPlayingNotificationManager implements OnSharedPreferenceChangeListener {

	private Context mContext = null;
	private static NowPlayingNotificationManager mInstance = null;
	private static boolean mEnabled = true;
	public static final int NOW_PLAYING_ID = 0;
	
	private NowPlayingNotificationManager(Context context) {
		mContext = context;
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mEnabled = prefs.getBoolean("setting_show_notification", false);
		prefs.registerOnSharedPreferenceChangeListener(this);
	}
	
	public static NowPlayingNotificationManager getInstance(Context context) {
		if(mInstance == null)
			mInstance = new NowPlayingNotificationManager(context);
		return mInstance;
	}

	
	public void startNotificating() {
		if(mEnabled)
			ConnectionManager.getNowPlayingPoller(mContext).subscribe(mPollingHandler);
	}
	
	public void stopNotificating() {
		ConnectionManager.getNowPlayingPoller(mContext).unSubscribe(mPollingHandler);
		removeNotification();
	}
	
	
	
	public void showPausedNotification(String artist, String title) {
		if((artist == null || artist.equals("") ) && (title == null || title.equals("")))
			removeNotification();
		else
			showNotification(artist, title, "Paused on XBMC",R.drawable.notif_pause);
	}
	
	public void showPlayingNotification(String artist, String title) {
		if((artist == null || artist.equals("") ) && (title == null || title.equals("")))
			removeNotification();
		else
			showNotification(artist, title, "Now playing on XBMC", R.drawable.notif_play);
	}
	
	public void showNotification(String artist, String title, String text, int icon) {
		Notification notification = buildNotification(artist + " - " + title, text, icon);
		final String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(ns);
		notificationManager.notify(NOW_PLAYING_ID, notification);
	}
	
	public void removeNotification() {
		final String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(ns);
		notificationManager.cancel(NOW_PLAYING_ID);
	}
	
	private Notification buildNotification(String title, String text, int icon) {
		final Intent actintent = new Intent(mContext, NowPlayingActivity.class);
		final PendingIntent intent = PendingIntent.getActivity(mContext, 0, actintent, 0);
		final Notification notification = new Notification(icon, title, System.currentTimeMillis());
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.setLatestEventInfo(mContext, title, text, intent);
		return notification;
	}
	
	public void showSlideshowNotification(String fileName, String folder) {
		Notification notification = buildNotification(folder + "/" + fileName, "Slideshow on XBMC", R.drawable.notif_pic);
		final String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(ns);
		notificationManager.notify(NOW_PLAYING_ID, notification);
	}
	
	private final Handler mPollingHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case NowPlayingPollerThread.MESSAGE_TRACK_CHANGED:
			case NowPlayingPollerThread.MESSAGE_PLAYSTATE_CHANGED:
				ICurrentlyPlaying curr = (ICurrentlyPlaying)msg.getData().get(NowPlayingPollerThread.BUNDLE_CURRENTLY_PLAYING);
				
				final int mediaType = curr.getMediaType();
				switch(mediaType){
					case MediaType.PICTURES:
						showSlideshowNotification(curr.getTitle(), curr.getAlbum());
						break;
					default:
						PlayStatus status = curr.getPlayStatus();
						if(status == PlayStatus.Playing) {
							showPlayingNotification(curr.getArtist(), curr.getTitle());
						}else if(status == PlayStatus.Paused) {
							showPausedNotification(curr.getArtist(), curr.getTitle());
						}else if(status == PlayStatus.Stopped) {
							removeNotification();
						}
						break;
				}
				break;
			case NowPlayingPollerThread.MESSAGE_CONNECTION_ERROR:
				removeNotification();
				break;
			case NowPlayingPollerThread.MESSAGE_RECONFIGURE:
				
				new Thread(){
					public void run(){
						try{
							Thread.sleep(1000);
						} catch(InterruptedException e) {
							Log.e("NowPlayingNotificationManager", Log.getStackTraceString(e));
						}
						startNotificating();
					}
				}.start();
			}
		}
	};

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals("setting_show_notification")) {
			boolean newState = sharedPreferences.getBoolean(key, true);
			if(newState){
				mEnabled = true;
				startNotificating();
			}
			else {
				mEnabled = false;
				stopNotificating();
			}
			
		}
	}
}

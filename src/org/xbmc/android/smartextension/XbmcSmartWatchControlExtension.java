/*
 *      Copyright (C) 2012 Cedric Priscal
 *
 *  This Program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2, or (at your option)
 *  any later version.
 *
 *  This Program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XBMC Remote; see the file license.  If not, write to
 *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *  http://www.gnu.org/copyleft/gpl.html
 *
 */

package org.xbmc.android.smartextension;

import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.business.NowPlayingPollerThread;
import org.xbmc.android.util.ConnectionFactory;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.data.IControlClient.ICurrentlyPlaying;
import org.xbmc.api.info.PlayStatus;
import org.xbmc.eventclient.ButtonCodes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlTouchEvent;
import com.sonyericsson.extras.liveware.sdk.R;

public class XbmcSmartWatchControlExtension extends ControlExtension implements
		Callback {

	// DEBUG
	private static final String LOG_TAG = "XbmcSmartWatchControlExtension";
	// Connection with XBMC
	private final IEventClientManager mEventClientManager;
	private final IControlManager mControlManager;
	// PLAYING STATUS
	private Handler mNowPlayingHandler;
	private int mLastPosition;
	private int mPlayStatus;
	private int mPlayListId;
	//private String mTitle;
	//private String mArtist;
	//private String mAlbum;
	//private int mProgressPosition;
	// GUI
	private final int mWidth;
	private final int mHeight;
	private final LinearLayout mLayout;
	private final Bitmap mBackground;
	private final Canvas mCanvas;
	// private final TextView mTitleView;
	private final ImageView mCoverView;
	private final LayoutParams mLayoutParams;

	public XbmcSmartWatchControlExtension(Context context,
			String hostAppPackageName) {
		super(context, hostAppPackageName);
		mNowPlayingHandler = new Handler(this);
		mControlManager = ManagerFactory.getControlManager(null);
		mEventClientManager = ManagerFactory.getEventClientManager(null);
		mWidth = context.getResources().getDimensionPixelSize(
				R.dimen.smart_watch_control_width);
		mHeight = context.getResources().getDimensionPixelSize(
				R.dimen.smart_watch_control_height);
		mLayoutParams = new LayoutParams(mWidth, mHeight);

		// Create background bitmap for drawing.
		mBackground = Bitmap.createBitmap(mWidth, mHeight,
				Bitmap.Config.RGB_565);
		// Set default density to avoid scaling.
		mBackground.setDensity(DisplayMetrics.DENSITY_DEFAULT);
		mCanvas = new Canvas(mBackground);

		mLayout = new LinearLayout(context);
		// mTitleView = new TextView(context);
		mCoverView = new ImageView(context);
		mCoverView.setImageResource(R.drawable.coverbox_back);
		mCoverView.setLayoutParams(mLayoutParams);

		mLayout.setLayoutParams(mLayoutParams);
		mLayout.addView(mCoverView);
		// mLayout.addView(mTitleView);
	}

	@Override
	public void onResume() {
		setScreenState(Control.Intents.SCREEN_STATE_ON);
		refresh();
	}

	@Override
	public void onPause() {
	}

	@Override
	public void onStart() {
		new Thread("nowplaying-spawning") {
			@Override
			public void run() {
				ConnectionFactory.getNowPlayingPoller(
						mContext.getApplicationContext()).subscribe(
						mNowPlayingHandler);
			}
		}.start();
	}

	@Override
	public void onStop() {
		ConnectionFactory.getNowPlayingPoller(mContext.getApplicationContext())
				.unSubscribe(mNowPlayingHandler);
	}

	private void refresh() {
		mLayout.measure(mWidth, mHeight);
		mLayout.layout(0, 0, mLayout.getMeasuredWidth(),
				mLayout.getMeasuredHeight());
		mBackground.eraseColor(0);
		mLayout.draw(mCanvas);

		showBitmap(mBackground);
	}

	@Override
	public void onTouch(ControlTouchEvent event) {
		switch (event.getAction()) {
		case Control.Intents.TOUCH_ACTION_RELEASE:
			// Play/Pause
			switch (mPlayStatus) {
			case PlayStatus.PLAYING:
				mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_PAUSE,
						false, true, true, (short) 0, (byte) 0);
				break;
			case PlayStatus.PAUSED:
				mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_PLAY,
						false, true, true, (short) 0, (byte) 0);
				break;
			case PlayStatus.STOPPED:
				final DataResponse<Boolean> doNothing = new DataResponse<Boolean>();
				mControlManager.setPlaylistId(doNothing, mPlayListId < 0 ? 0
						: mPlayListId, mContext.getApplicationContext());
				mControlManager.setPlaylistPos(doNothing, mLastPosition < 0 ? 0
						: mLastPosition, mContext.getApplicationContext());
				break;
			}
			break;
		case Control.Intents.TOUCH_ACTION_LONGPRESS:
			// Stop
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_STOP, false, true, true,
					(short) 0, (byte) 0);
			setScreenState(Control.Intents.SCREEN_STATE_AUTO);
			break;
		}
	}

	@Override
	public void onSwipe(int direction) {
		switch (direction) {
		case Control.Intents.SWIPE_DIRECTION_RIGHT:
			// forward
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_RIGHT, false, true, true,
					(short) 0, (byte) 0);
			break;

		case Control.Intents.SWIPE_DIRECTION_LEFT:
			// backward
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_LEFT, false, true, true,
					(short) 0, (byte) 0);
			break;

		case Control.Intents.SWIPE_DIRECTION_UP:
			// Previous element in playlist
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_SKIP_MINUS, false, true, true,
					(short) 0, (byte) 0);
			break;

		case Control.Intents.SWIPE_DIRECTION_DOWN:
			// Next element in playlist
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_SKIP_PLUS, false, true, true,
					(short) 0, (byte) 0);
			break;
		}
	}

	public boolean handleMessage(Message msg) {

		final Bundle data = msg.getData();
		final ICurrentlyPlaying currentlyPlaying = (ICurrentlyPlaying) data
				.getSerializable(NowPlayingPollerThread.BUNDLE_CURRENTLY_PLAYING);

		Log.d(LOG_TAG, "handleMessage: " + msg.what);

		switch (msg.what) {

		case NowPlayingPollerThread.MESSAGE_PROGRESS_CHANGED:
			mPlayStatus = currentlyPlaying.getPlayStatus();
			//mProgressPosition = Math.round(currentlyPlaying.getPercentage());
			if (currentlyPlaying.isPlaying()) {
				Log.d(LOG_TAG, "PROGRESS_CHANGED => play");
			} else {
				Log.d(LOG_TAG, "PROGRESS_CHANGED => stop/pause");
			}
			refresh();
			return true;

		case NowPlayingPollerThread.MESSAGE_PLAYLIST_ITEM_CHANGED:
			//mTitle = currentlyPlaying.getTitle();
			//mArtist = currentlyPlaying.getArtist();
			//mAlbum = currentlyPlaying.getAlbum();
			mLastPosition = data
					.getInt(NowPlayingPollerThread.BUNDLE_LAST_PLAYPOSITION);
			refresh();
			return true;

		case NowPlayingPollerThread.MESSAGE_PLAYSTATE_CHANGED:
			mPlayListId = data
					.getInt(NowPlayingPollerThread.BUNDLE_LAST_PLAYLIST);
			refresh();
			return true;

		case NowPlayingPollerThread.MESSAGE_COVER_CHANGED:
			Bitmap b = ConnectionFactory.getNowPlayingPoller(
					mContext.getApplicationContext()).getNowPlayingCover();
			if (b != null) {
				mCoverView.setImageBitmap(b);
			} else {
				mCoverView.setImageResource(R.drawable.coverbox_back);
			}
			refresh();
			return true;

		case NowPlayingPollerThread.MESSAGE_CONNECTION_ERROR:
			mPlayStatus = PlayStatus.UNKNOWN;
			Log.w(LOG_TAG, "Received connection error from poller!");
			refresh();
			return true;

		case NowPlayingPollerThread.MESSAGE_RECONFIGURE:
			mPlayStatus = PlayStatus.UNKNOWN;
			new Thread() {
				public void run() {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						Log.e(LOG_TAG, Log.getStackTraceString(e));
					}
					ConnectionFactory.getNowPlayingPoller(
							mContext.getApplicationContext()).subscribe(
							mNowPlayingHandler);
				}
			}.start();
			return true;

		default:
			return false;
		}
	}

}

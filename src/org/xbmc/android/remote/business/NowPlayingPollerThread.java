/*
 *      Copyright (C) 2005-2009 Team XBMC
 *      http://xbmc.org
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

package org.xbmc.android.remote.business;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashSet;

import org.xbmc.android.util.ClientFactory;
import org.xbmc.android.util.HostFactory;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IControlClient;
import org.xbmc.api.data.IControlClient.ICurrentlyPlaying;
import org.xbmc.api.data.IInfoClient;
import org.xbmc.api.info.PlayStatus;
import org.xbmc.api.object.Host;
import org.xbmc.jsonrpc.Connection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Activities (and other stuff) can subscribe to this thread in order to obtain
 * real-time "Now playing" information. The thread will send relevant messages
 * to all subscribers. If there are no subscriptions, nothing is polled.
 *
 * Please remember to unsubscribe (e.g. onPause()) in order to avoid unnecessary
 * polling.
 *
 * @author Team XBMC
 */
public class NowPlayingPollerThread extends Thread {

	private static final String TAG = "NowPlayingPollerThread";

	public static final String BUNDLE_CURRENTLY_PLAYING = "CurrentlyPlaying";
	public static final String BUNDLE_LAST_PLAYLIST = "LastPlaylist";
	public static final String BUNDLE_LAST_PLAYPOSITION = "LastPlayPosition";

	public static final int MESSAGE_CONNECTION_ERROR = 1;
	public static final int MESSAGE_RECONFIGURE = 2;
	public static final int MESSAGE_PROGRESS_CHANGED = 666;
	public static final int MESSAGE_PLAYLIST_ITEM_CHANGED = 667;
	public static final int MESSAGE_COVER_CHANGED = 668;
	public static final int MESSAGE_PLAYSTATE_CHANGED = 669;

	private IInfoClient mInfo;
	private IControlClient mControl;
	private final HashSet<Handler> mSubscribers;

	private String mCoverPath;
	private Bitmap mCover;

	private int mPlayList = -1;
	private int mPosition = -1;

	/**
	 * Since this one is kinda of its own, we use a stub as manager.
	 * @TODO create some toats or at least logs instead of empty on* methods.
	 */
	private final INotifiableManager mManagerStub;

	public NowPlayingPollerThread(final Context context){
		mManagerStub = new INotifiableManager() {
			public void onMessage(int code, String message) { }
			public void onMessage(String message) { }
			public void onError(Exception e) {
				// XXX link to context will eventually change if activity which created the thread changes (java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare())
				//Toast toast = Toast.makeText(context, "Poller Error: " + e.getMessage(), Toast.LENGTH_LONG);
				//toast.show();
				if (e.getMessage() != null) {
					Log.e(TAG, e.getMessage());
				}
				e.printStackTrace();
			}
			public void onFinish(DataResponse<?> response) {
			}
			public void onWrongConnectionState(int state, Command<?> cmd) {
			}
			public void retryAll() {
			}
		};
		try {
			mControl = ClientFactory.getControlClient(mManagerStub, context);
		} catch (Exception e2) {
			mControl = null;
		}
		try {
			mInfo = ClientFactory.getInfoClient(mManagerStub, context);
		} catch (Exception e1) {
			mInfo = null;
		}
		mSubscribers = new HashSet<Handler>();
	}

	public void subscribe(Handler handler) {
		// update handler on the state of affairs
		final ICurrentlyPlaying currPlaying = mControl.getCurrentlyPlaying(mManagerStub);
		sendSingleMessage(handler, MESSAGE_PROGRESS_CHANGED, currPlaying);
		sendSingleMessage(handler, MESSAGE_PLAYLIST_ITEM_CHANGED, currPlaying);
		sendSingleMessage(handler, MESSAGE_COVER_CHANGED, currPlaying);

		synchronized (mSubscribers) {
			mSubscribers.add(handler);
		}
	}

	public void unSubscribe(Handler handler){
		synchronized (mSubscribers) {
			mSubscribers.remove(handler);
		}
	}

	public Bitmap getNowPlayingCover(){
		return mCover;
	}

	/**
	 * @return True if the stored cover art was updated.
	 */
	private boolean updateNowPlayingCover() {
		try {
			String downloadURI = mInfo.getCurrentlyPlayingThumbURI(mManagerStub);
			if (downloadURI == null || downloadURI.length() == 0) {
				mCover = null;
				String oldCoverPath = mCoverPath;
				mCoverPath = null;
				// If we had previously been handing out a thumbnail, clients
				// need to update to the lack of one.
				return oldCoverPath != null;
			}
			if (!downloadURI.equals(mCoverPath)) {
				mCoverPath = downloadURI;
				byte[] buffer = download(downloadURI);
				if (buffer == null || buffer.length == 0)
					mCover = null;
				else
					mCover = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
				return true;
			}
		} catch (MalformedURLException e) {
			Log.e(TAG, Log.getStackTraceString(e));
		} catch (URISyntaxException e) {
			Log.e(TAG, Log.getStackTraceString(e));
		} catch (IOException e) {
			Log.e(TAG, Log.getStackTraceString(e));
		}
		return false;
	}

	public void sendMessage(int what, ICurrentlyPlaying curr) {
		synchronized (mSubscribers) {
			for (Handler handler : mSubscribers) {
				sendSingleMessage(handler, what, curr);
			}
		}
	}

	private void sendSingleMessage(Handler handler, int what, ICurrentlyPlaying curr) {
		Message msg = Message.obtain(handler);
		msg.what = what;
		Bundle bundle = msg.getData();
		bundle.putSerializable(BUNDLE_CURRENTLY_PLAYING, curr);
		bundle.putInt(BUNDLE_LAST_PLAYLIST, mPlayList);
		bundle.putInt(BUNDLE_LAST_PLAYPOSITION, mPosition);
		msg.setTarget(handler);
		handler.sendMessage(msg);
	}

	public void sendEmptyMessage(int what) {
		synchronized (mSubscribers) {
			for (Handler handler : mSubscribers) {
				handler.sendEmptyMessage(what);
			}
		}
	}

	public void run() {
		String lastPos = "-1";
		int lastPlayStatus = PlayStatus.UNKNOWN;
		int currentPlayStatus = PlayStatus.UNKNOWN;
		int currentMediaType = 0;
		IControlClient control = mControl; // use local reference for faster access
		HashSet<Handler> subscribers;
		while (!isInterrupted() ) {
			synchronized (mSubscribers) {
				subscribers = new HashSet<Handler>(mSubscribers);
			}
			if (subscribers.size() > 0){
				/*				if (!control.isConnected()) {
					sendEmptyMessage(MESSAGE_CONNECTION_ERROR);
				} else {*/
				ICurrentlyPlaying currPlaying;
				try{
					currPlaying = control.getCurrentlyPlaying(mManagerStub);
				} catch(Exception e) {
					e.printStackTrace();
					sendEmptyMessage(MESSAGE_CONNECTION_ERROR);
					return;
				}
				currentPlayStatus = currPlaying.getPlayStatus();
				String currentPos = currPlaying.getTitle() + currPlaying.getDuration();

				// send changed status
				if (currentPlayStatus == PlayStatus.PLAYING) {
					sendMessage(MESSAGE_PROGRESS_CHANGED, currPlaying);
					boolean coverChanged = updateNowPlayingCover();
					if (coverChanged) {
						sendMessage(MESSAGE_COVER_CHANGED, currPlaying);
					}
				}

				// play state changed?
				if ((currentPlayStatus != lastPlayStatus) || (currentMediaType != currPlaying.getMediaType())) {
					currentMediaType = currPlaying.getMediaType();

					if (currentPlayStatus == PlayStatus.PLAYING) {
						mPlayList = control.getPlaylistId(mManagerStub);
						sendMessage(MESSAGE_PLAYSTATE_CHANGED, currPlaying);
					} else {
						sendMessage(MESSAGE_PLAYSTATE_CHANGED, currPlaying);
						sendMessage(MESSAGE_PROGRESS_CHANGED, currPlaying);
					}
					boolean coverChanged = updateNowPlayingCover();
					if (coverChanged) {
						sendMessage(MESSAGE_COVER_CHANGED, currPlaying);
					}
				}

				// play position changed?
				if (!lastPos.equals(currentPos)) {
					lastPos = currentPos;

					if (currPlaying.getPlaylistPosition() >= 0) {
						mPosition = currPlaying.getPlaylistPosition();
					}
					sendMessage(MESSAGE_PLAYLIST_ITEM_CHANGED, currPlaying);

					boolean coverChanged = updateNowPlayingCover();
					if (coverChanged) {
						sendMessage(MESSAGE_COVER_CHANGED, currPlaying);
					}
				}
			}
			else{
				this.interrupt();
			}
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				sendEmptyMessage(MESSAGE_RECONFIGURE);
				return;
			}
			lastPlayStatus = currentPlayStatus;
		}
	}

	private byte[] download(String pathToDownload) throws IOException, URISyntaxException {
		Connection connection;
		final Host host = HostFactory.host;
			if (host != null) {
				connection = Connection.getInstance(host.addr, host.port);
				connection.setAuth(host.user, host.pass);
			} else {
				connection = Connection.getInstance(null, 0);
			}

			return connection.download(pathToDownload);
	}
}

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

package org.xbmc.android.remote.presentation.controller;

import java.io.IOException;

import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.business.NowPlayingPollerThread;
import org.xbmc.android.remote.presentation.activity.NowPlayingActivity;
import org.xbmc.android.remote.presentation.activity.PlaylistActivity;
import org.xbmc.android.util.ConnectionFactory;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.data.IControlClient.ICurrentlyPlaying;
import org.xbmc.api.info.PlayStatus;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.api.type.SeekType;
import org.xbmc.eventclient.ButtonCodes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class NowPlayingController extends AbstractController implements INotifiableController, IController, Callback  {
	
	private static final String TAG = "NowPlayingController";
	
	private IControlManager mControlManager;
	private NowPlayingActivity mNowPlayingActivity;
	private Handler mNowPlayingHandler;
	private IEventClientManager mEventClientManager;
	private int mPlayStatus = PlayStatus.UNKNOWN;
	private int mPlayListId = -1;
	private int mLastPosition = -1;
	
	public NowPlayingController(NowPlayingActivity activity) {
		super.onCreate(activity);
		mNowPlayingActivity = activity;
		mControlManager = ManagerFactory.getControlManager(this);
		mEventClientManager = ManagerFactory.getEventClientManager(this);
		mNowPlayingHandler = new Handler(this);
	}
	
	/**
	 * This is called from the thread with a message containing updated
	 * info of what's currently playing.
	 * @param msg Message object containing currently playing info
	 */
	public synchronized boolean handleMessage(Message msg) {
		
		final Bundle data = msg.getData();
		final ICurrentlyPlaying currentlyPlaying = (ICurrentlyPlaying)data.getSerializable(NowPlayingPollerThread.BUNDLE_CURRENTLY_PLAYING);

		switch (msg.what) {
			case NowPlayingPollerThread.MESSAGE_PROGRESS_CHANGED: 
				mPlayStatus = currentlyPlaying.getPlayStatus();
				mNowPlayingActivity.setProgressPosition(Math.round(currentlyPlaying.getPercentage()));
				if (currentlyPlaying.isPlaying()) {
					mNowPlayingActivity.updateProgress(currentlyPlaying.getDuration(), currentlyPlaying.getTime());
				} else {
					mNowPlayingActivity.clear();
				}
				return true;
			
			case NowPlayingPollerThread.MESSAGE_TRACK_CHANGED:
				mNowPlayingActivity.updateInfo(currentlyPlaying.getArtist(), currentlyPlaying.getAlbum(), currentlyPlaying.getTitle());
				mLastPosition = data.getInt(NowPlayingPollerThread.BUNDLE_LAST_PLAYPOSITION);
		  	  	return true;

			case NowPlayingPollerThread.MESSAGE_PLAYSTATE_CHANGED:
				mPlayListId = data.getInt(NowPlayingPollerThread.BUNDLE_LAST_PLAYLIST);
				return true;
		  	  	
			case NowPlayingPollerThread.MESSAGE_COVER_CHANGED:
				// TODO: FIX!!
				mNowPlayingActivity.updateCover(ConnectionFactory.getNowPlayingPoller(mActivity).getNowPlayingCover());
				return true;
				
			case NowPlayingPollerThread.MESSAGE_CONNECTION_ERROR:
				mPlayStatus = PlayStatus.UNKNOWN;
				Log.w(TAG,"Received connection error from poller!");
				return true;
				
			case NowPlayingPollerThread.MESSAGE_RECONFIGURE:
				mPlayStatus = PlayStatus.UNKNOWN;
				new Thread(){
					public void run(){
						try{
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							Log.e(TAG, Log.getStackTraceString(e));
						}
						ConnectionFactory.getNowPlayingPoller(mActivity.getApplicationContext()).subscribe(mNowPlayingHandler);					
					}
				}.start();
				return true;
			default:
				return false;
		}
	}
	
	public void setupButtons(SeekBar seekbar, View prev, View stop, View playpause, View next, View playlist) {
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser && !seekBar.isInTouchMode())
					seek(progress);
			}
			public void onStartTrackingTouch(SeekBar seekBar) { }
			public void onStopTrackingTouch(SeekBar seekBar) {
				seek(seekBar.getProgress());
			}
		});
		
		// setup buttons
		prev.setOnClickListener(new OnRemoteAction(ButtonCodes.REMOTE_SKIP_MINUS));
		stop.setOnClickListener(new OnRemoteAction(ButtonCodes.REMOTE_STOP));
		next.setOnClickListener(new OnRemoteAction(ButtonCodes.REMOTE_SKIP_PLUS));
		playpause.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					switch (mPlayStatus) {
						case PlayStatus.PLAYING:
							mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_PAUSE, false, true, true, (short)0, (byte)0);
							break;
						case PlayStatus.PAUSED:
							mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_PLAY, false, true, true, (short)0, (byte)0);
							break;
						case PlayStatus.STOPPED:
							final DataResponse<Boolean> doNothing = new DataResponse<Boolean>();
							mControlManager.setPlaylistId(doNothing, mPlayListId < 0 ? 0 : mPlayListId, mActivity.getApplicationContext());
							mControlManager.setPlaylistPos(doNothing, mLastPosition < 0 ? 0 : mLastPosition, mActivity.getApplicationContext());
							break;
					}
				} catch (IOException e) { }
			}
		});
		
		// playlist button
		playlist.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mActivity.startActivity(new Intent(mActivity, PlaylistActivity.class));
			}
		});
	}
	public void seek(int progress) {
		mControlManager.seek(new DataResponse<Boolean>(), SeekType.absolute, progress, mActivity.getApplicationContext());
	}
	

	/**
	 * Handles the push- release button code. Switches image of the pressed
	 * button, vibrates and executes command.
	 */
	private class OnRemoteAction implements OnClickListener {
		private final String mAction;
		public OnRemoteAction(String action) {
			mAction = action;
		}
		public void onClick(View v) {
			try {
				mEventClientManager.sendButton("R1", mAction, false, true, true, (short)0, (byte)0);
			} catch (IOException e) { }
		}
	}
	
	public void onActivityPause() {
		ConnectionFactory.getNowPlayingPoller(mActivity.getApplicationContext()).unSubscribe(mNowPlayingHandler);
		if (mControlManager != null) {
			mControlManager.setController(null);
		}
		super.onActivityPause();
	}

	public void onActivityResume(Activity activity) {
		super.onActivityResume(activity);
		ConnectionFactory.getNowPlayingPoller(activity.getApplicationContext()).subscribe(mNowPlayingHandler);
		if (mControlManager != null) {
			mControlManager.setController(this);
		}
	}
}
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

package org.xbmc.android.remote.activity;

import java.io.IOException;

import org.xbmc.android.backend.httpapi.NowPlayingPollerThread;
import org.xbmc.android.remote.R;
import org.xbmc.android.remote.guilogic.MusicPlaylistLogic;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.android.util.ErrorHandler;
import org.xbmc.eventclient.ButtonCodes;
import org.xbmc.eventclient.EventClient;
import org.xbmc.httpapi.client.ControlClient.ICurrentlyPlaying;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;

public class PlaylistActivity extends Activity implements Callback {
	
	private MusicPlaylistLogic mMusicPlaylistLogic;
	private EventClient mClient;
	
	private Handler mNowPlayingHandler;
	private NowPlayingPollerThread mNowPlayingPoller;
	
	private ImageButton mPlayPauseView;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ErrorHandler.setActivity(this);
       	setContentView(R.layout.playlist);
        
       	mClient = ConnectionManager.getEventClient(this);
       	
 	  	mMusicPlaylistLogic = new MusicPlaylistLogic();
 	  	mMusicPlaylistLogic.findTitleView(findViewById(R.id.playlist_outer_layout));
 	  	mMusicPlaylistLogic.onCreate(this, (ListView)findViewById(R.id.playlist_list));
 	  	
 	  	mPlayPauseView = (ImageButton)findViewById(R.id.MediaPlayPauseButton);
 	  	mNowPlayingHandler = new Handler(this);
 	  	
 	  	// setup buttons
		findViewById(R.id.MediaPreviousButton).setOnClickListener(new OnRemoteAction(ButtonCodes.REMOTE_SKIP_MINUS));
		findViewById(R.id.MediaStopButton).setOnClickListener(new OnRemoteAction(ButtonCodes.REMOTE_STOP));
		findViewById(R.id.MediaPlayPauseButton).setOnClickListener(new OnRemoteAction(ButtonCodes.REMOTE_PAUSE));
		findViewById(R.id.MediaNextButton).setOnClickListener(new OnRemoteAction(ButtonCodes.REMOTE_SKIP_PLUS));
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
			case NowPlayingPollerThread.MESSAGE_NOW_PLAYING_PROGRESS: 
				if (currentlyPlaying.isPlaying()) {
					mPlayPauseView.setBackgroundResource(R.drawable.now_playing_pause);
				} else {
					mPlayPauseView.setBackgroundResource(R.drawable.now_playing_play);
				}
				return true;
			default:
				return false;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
  	  	mNowPlayingPoller = ConnectionManager.getNowPlayingPoller(this);
  	  	mNowPlayingPoller.subscribe(mNowPlayingHandler);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mNowPlayingPoller.unSubscribe(mNowPlayingHandler);
	}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMusicPlaylistLogic.onCreateOptionsMenu(menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mMusicPlaylistLogic.onOptionsItemSelected(item);
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		mMusicPlaylistLogic.onCreateContextMenu(menu, v, menuInfo);
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		mMusicPlaylistLogic.onContextItemSelected(item);
		return super.onContextItemSelected(item);
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
				mClient.sendButton("R1", mAction, false, true, true, (short)0, (byte)0);
			} catch (IOException e) { }
		}
	}
}
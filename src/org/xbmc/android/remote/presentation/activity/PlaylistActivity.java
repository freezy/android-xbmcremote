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

package org.xbmc.android.remote.presentation.activity;

import java.io.IOException;

import org.xbmc.android.backend.httpapi.NowPlayingPollerThread;
import org.xbmc.android.remote.ConfigurationManager;
import org.xbmc.android.remote.R;
import org.xbmc.android.remote.presentation.controller.MusicPlaylistController;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.api.data.IControlClient.ICurrentlyPlaying;
import org.xbmc.api.object.Song;
import org.xbmc.eventclient.ButtonCodes;
import org.xbmc.eventclient.EventClient;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class PlaylistActivity extends Activity implements Callback {

	public static final int MESSAGE_PLAYLIST_SIZE = 701;
	public static final String BUNDLE_PLAYLIST_SIZE = "playlist_size";

	private MusicPlaylistController mMusicPlaylistLogic;
	private EventClient mClient;

	private Handler mNowPlayingHandler;
	private NowPlayingPollerThread mNowPlayingPoller;

	private ImageButton mPlayPauseView;
	private TextView mLabel1;
	private TextView mLabel2;

	private ConfigurationManager mConfigurationManager;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playlist);
		
		// remove nasty top fading edge
		FrameLayout topFrame = (FrameLayout)findViewById(android.R.id.content);
		topFrame.setForeground(null);

		// cache references for faster access
		mPlayPauseView = (ImageButton) findViewById(R.id.MediaPlayPauseButton);
		mLabel1 = (TextView) findViewById(R.id.playlist_textfield_upper);
		mLabel2 = (TextView) findViewById(R.id.playlist_textfield_lower);

		mClient = ConnectionManager.getEventClient(this);
		mNowPlayingHandler = new Handler(this);

		// create and associate logic
		mMusicPlaylistLogic = new MusicPlaylistController();
		mMusicPlaylistLogic.findTitleView(findViewById(R.id.playlist_outer_layout));
		mMusicPlaylistLogic.findMessageView(findViewById(R.id.playlist_outer_layout));
		mMusicPlaylistLogic.onCreate(this, (ListView) findViewById(R.id.playlist_list));
		mMusicPlaylistLogic.subscribe(mNowPlayingHandler);

		// setup buttons
		findViewById(R.id.MediaPreviousButton).setOnClickListener(new OnRemoteAction(ButtonCodes.REMOTE_SKIP_MINUS));
		findViewById(R.id.MediaStopButton).setOnClickListener(new OnRemoteAction(ButtonCodes.REMOTE_STOP));
		findViewById(R.id.MediaPlayPauseButton).setOnClickListener(new OnRemoteAction(ButtonCodes.REMOTE_PAUSE));
		findViewById(R.id.MediaNextButton).setOnClickListener(new OnRemoteAction(ButtonCodes.REMOTE_SKIP_PLUS));
		
		mConfigurationManager = ConfigurationManager.getInstance(this);
		mConfigurationManager.initKeyguard();
	}

	/**
	 * This is called from the thread with a message containing updated info of
	 * what's currently playing.
	 * 
	 * @param msg
	 *            Message object containing currently playing info
	 */
	public synchronized boolean handleMessage(Message msg) {
		final Bundle data = msg.getData();
		final ICurrentlyPlaying currentlyPlaying = (ICurrentlyPlaying) data.getSerializable(NowPlayingPollerThread.BUNDLE_CURRENTLY_PLAYING);
		switch (msg.what) {
		case NowPlayingPollerThread.MESSAGE_PROGRESS_CHANGED:
			if (currentlyPlaying.isPlaying()) {
				mLabel1.setText(Song.getDuration(currentlyPlaying.getTime() + 1));
				mPlayPauseView.setBackgroundResource(R.drawable.now_playing_pause);
			} else {
				mLabel1.setText("--:--");
				mPlayPauseView.setBackgroundResource(R.drawable.now_playing_play);
			}
			return true;
			
		case PlaylistActivity.MESSAGE_PLAYLIST_SIZE:
			final int size = msg.getData().getInt(BUNDLE_PLAYLIST_SIZE);
			mLabel2.setText(size == 0 ? "empty" : size + " tracks");
			return true;
			
		case NowPlayingPollerThread.MESSAGE_TRACK_CHANGED:
			mMusicPlaylistLogic.onTrackChanged(currentlyPlaying);
			return true;
			
		default:
			return false;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mConfigurationManager.onActivityResume(this);
		mNowPlayingPoller = ConnectionManager.getNowPlayingPoller(this);
		mNowPlayingPoller.subscribe(mNowPlayingHandler);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mConfigurationManager.onActivityPause();
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
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		EventClient client = ConnectionManager.getEventClient(this);	
		try {
			switch (keyCode) {
				case KeyEvent.KEYCODE_VOLUME_UP:
					client.sendButton("R1", ButtonCodes.REMOTE_VOLUME_PLUS, false, true, true, (short)0, (byte)0);
					return true;
				case KeyEvent.KEYCODE_VOLUME_DOWN:
					client.sendButton("R1", ButtonCodes.REMOTE_VOLUME_MINUS, false, true, true, (short)0, (byte)0);
					return true;
			}
		} catch (IOException e) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
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
				mClient.sendButton("R1", mAction, false, true, true, (short) 0, (byte) 0);
			} catch (IOException e) {
			}
		}
	}
}
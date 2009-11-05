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
import java.net.SocketTimeoutException;

import org.xbmc.android.backend.httpapi.NowPlayingPollerThread;
import org.xbmc.android.remote.R;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.android.util.ErrorHandler;
import org.xbmc.eventclient.ButtonCodes;
import org.xbmc.eventclient.EventClient;
import org.xbmc.httpapi.client.ControlClient;
import org.xbmc.httpapi.client.ControlClient.ICurrentlyPlaying;
import org.xbmc.httpapi.data.Song;
import org.xbmc.httpapi.type.MediaType;
import org.xbmc.httpapi.type.SeekType;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class NowPlayingActivity extends Activity implements Callback {
	
	private ControlClient mControl;
	private EventClient mClient;

	private Handler mNowPlayingHandler;
	private NowPlayingPollerThread mNowPlayingPoller;
	
	private TextView mAlbumView;
	private TextView mArtistView;
	private TextView mSongTitleView;
	private TextView mCounterLeftView;
	private TextView mCounterRightView;
	private ImageButton mPlayPauseView;
	private SeekBar mSeekBar;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ErrorHandler.setActivity(this);
       	setContentView(R.layout.nowplaying);
        
  	  	mControl = ConnectionManager.getHttpClient(this).control;
  	  	mClient = ConnectionManager.getEventClient(this);
  	  	
		mSeekBar = (SeekBar) findViewById(R.id.NowPlayingProgress);
		mArtistView = (TextView) findViewById(R.id.ArtistTextView);
		mAlbumView = (TextView) findViewById(R.id.AlbumTextView);
		mSongTitleView = (TextView) findViewById(R.id.SongTextView);
		mCounterLeftView = (TextView)findViewById(R.id.now_playing_counter_left);
		mCounterRightView = (TextView)findViewById(R.id.now_playing_counter_right);
		mPlayPauseView = (ImageButton)findViewById(R.id.MediaPlayPauseButton);
  	  	
		// remove nasty top fading edge
		FrameLayout topFrame = (FrameLayout)findViewById(android.R.id.content);
		topFrame.setForeground(null);
  	  	
		// set titlebar text
  	  	((TextView)findViewById(R.id.titlebar_text)).setText("Now playing");
  	  	
  	  	mNowPlayingHandler = new Handler(this);

  	  	setupButtons();
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
	
	private void setupButtons() {
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser && !seekBar.isInTouchMode())
					mControl.seek(SeekType.absolute, progress);
			}
			public void onStartTrackingTouch(SeekBar seekBar) { }
			public void onStopTrackingTouch(SeekBar seekBar) {
				mControl.seek(SeekType.absolute, seekBar.getProgress());
			}
		});
		
		// setup buttons
		findViewById(R.id.MediaPreviousButton).setOnClickListener(new OnRemoteAction(ButtonCodes.REMOTE_SKIP_MINUS));
		findViewById(R.id.MediaStopButton).setOnClickListener(new OnRemoteAction(ButtonCodes.REMOTE_STOP));
		findViewById(R.id.MediaPlayPauseButton).setOnClickListener(new OnRemoteAction(ButtonCodes.REMOTE_PAUSE));
		findViewById(R.id.MediaNextButton).setOnClickListener(new OnRemoteAction(ButtonCodes.REMOTE_SKIP_PLUS));
		
		// playlist button
		findViewById(R.id.MediaPlaylistButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(NowPlayingActivity.this, PlaylistActivity.class));
			}
		});
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
			if (!mSeekBar.isPressed()) {
				mSeekBar.setProgress(Math.round(currentlyPlaying.getPercentage()));
			}
			if (currentlyPlaying.isPlaying()) {
				mSeekBar.setEnabled(true);
				mCounterLeftView.setText(Song.getDuration(currentlyPlaying.getTime() + 1));
				mCounterRightView.setText("-" + Song.getDuration(currentlyPlaying.getDuration() - currentlyPlaying.getTime() - 1));
				mPlayPauseView.setBackgroundResource(R.drawable.now_playing_pause);
			} else {
				mSeekBar.setEnabled(false);
				mCounterLeftView.setText("");
				mCounterRightView.setText("");
				mPlayPauseView.setBackgroundResource(R.drawable.now_playing_play);
			}
			return true;
		
		case NowPlayingPollerThread.MESSAGE_ARTIST_TEXT_VIEW:
			mArtistView.setText(currentlyPlaying.getArtist());
	  	  	mAlbumView.setText(currentlyPlaying.getAlbum());
	  	  	mSongTitleView.setText(currentlyPlaying.getTitle());
	  	  	return true;
	  	  	
		case NowPlayingPollerThread.MESSAGE_COVER_IMAGE:
			final ImageView cover = (ImageView) findViewById(R.id.CoverImage);
			cover.setImageDrawable(mNowPlayingPoller.getNowPlayingCover());
			return true;
			
		case NowPlayingPollerThread.MESSAGE_CONNECTION_ERROR:
			ErrorHandler handler = new ErrorHandler(this);
			handler.handle(new SocketTimeoutException());
			setResult(NowPlayingPollerThread.MESSAGE_CONNECTION_ERROR);
			finish();
			return true;
			
		default:
			return false;
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, "Music");
		menu.add(0, 2, 0, "Video");
		menu.add(0, 3, 0, "Pictures").setIcon(android.R.drawable.ic_menu_camera);
		
		menu.add(0, 5, 0, "Remote");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent myIntent = null;
		
		switch (item.getItemId()) {
		case 1:
			myIntent = new Intent(this, FileListActivity.class);
			myIntent.putExtra("shareType", MediaType.music.toString());
			break;
		case 2:
			myIntent = new Intent(this, FileListActivity.class);
			myIntent.putExtra("shareType", MediaType.video.toString());
			break;
		case 3:
			myIntent = new Intent(this, FileListActivity.class);
			myIntent.putExtra("shareType", MediaType.pictures.toString());
			break;
		case 5:
			myIntent = new Intent(this, RemoteActivity.class);
			break;
		}
		
		if (myIntent != null) {
			startActivity(myIntent);
			return true;
		}
		return false;
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
				mClient.sendButton("R1", mAction, false, true, true, (short)0, (byte)0);
			} catch (IOException e) { }
		}
	}
}
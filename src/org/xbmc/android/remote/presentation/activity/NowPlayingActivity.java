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

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.controller.NowPlayingController;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.object.Song;
import org.xbmc.eventclient.ButtonCodes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class NowPlayingActivity extends Activity {
	
	private TextView mAlbumView;
	private TextView mArtistView;
	private TextView mSongTitleView;
	private TextView mCounterLeftView;
	private TextView mCounterRightView;
	private ImageButton mPlayPauseView;
	private SeekBar mSeekBar;
	
	private ConfigurationManager mConfigurationManager;
	private NowPlayingController mNowPlayingController;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       	setContentView(R.layout.nowplaying);
       	
       	mNowPlayingController = new NowPlayingController(this);
        	
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

  	  	mConfigurationManager = ConfigurationManager.getInstance(this);
		mConfigurationManager.initKeyguard();
  	  	
  	  	mNowPlayingController.setupButtons(mSeekBar,
  	  		findViewById(R.id.MediaPreviousButton),
  	  		findViewById(R.id.MediaStopButton),
  	  		findViewById(R.id.MediaPlayPauseButton),
  	  		findViewById(R.id.MediaNextButton),
  	  		findViewById(R.id.MediaPlaylistButton)
  	  	);
  	  	
	}
	
	public void setProgressPosition(int pos) {
		if (!mSeekBar.isPressed()) {
			mSeekBar.setProgress(pos);
		}		
	}
	
	public void updateInfo(String artist, String album, String title) {
		mArtistView.setText(artist);
  	  	mAlbumView.setText(album);
  	  	mSongTitleView.setText(title);		
	}
	
	public void updateProgress(int duration, int time) {
		mSeekBar.setEnabled(duration != 0);
		mCounterLeftView.setText(Song.getDuration(time + 1));
		mCounterRightView.setText(duration == 0 ? "unknown" : "-" + Song.getDuration(duration - time - 1));
		mPlayPauseView.setBackgroundResource(R.drawable.now_playing_pause);		
	}
	
	public void updateCover(Drawable cover) {
		final ImageView img = (ImageView) findViewById(R.id.CoverImage);
		img.setImageDrawable(cover);
	}
	
	public void clear() {
		mSeekBar.setEnabled(false);
		mCounterLeftView.setText("");
		mCounterRightView.setText("");
		mPlayPauseView.setBackgroundResource(R.drawable.now_playing_play);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		IEventClientManager client = ManagerFactory.getEventClientManager(mNowPlayingController);
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

	@Override
	protected void onResume() {
		super.onResume();
		mNowPlayingController.onActivityResume(this);
		mConfigurationManager.onActivityResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mNowPlayingController.onActivityPause();
		mConfigurationManager.onActivityPause();
		if(isTaskRoot()){
			Intent intent = new Intent(NowPlayingActivity.this, HomeActivity.class );
			NowPlayingActivity.this.startActivity(intent);
		}
	}
}
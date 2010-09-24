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
import org.xbmc.android.util.KeyTracker;
import org.xbmc.android.util.OnLongPressBackKeyTracker;
import org.xbmc.android.util.KeyTracker.Stage;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.object.Song;
import org.xbmc.api.type.MediaType;
import org.xbmc.eventclient.ButtonCodes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Build.VERSION;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
	private KeyTracker mKeyTracker;
	
	private static final int MENU_REMOTE = 303;
	
	public NowPlayingActivity() {
		if(Integer.parseInt(VERSION.SDK) < 5) {
			mKeyTracker = new KeyTracker(new OnLongPressBackKeyTracker() {
	
				@Override
				public void onLongPressBack(int keyCode, KeyEvent event,
						Stage stage, int duration) {
					onKeyLongPress(keyCode, event);
				}
		
				@Override
				public void onShortPressBack(int keyCode, KeyEvent event,
						Stage stage, int duration) {
					callSuperOnKeyDown(keyCode, event);
				}
				
			});
		};
	}	
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       	setContentView(R.layout.nowplaying);
       	
       	mNowPlayingController = new NowPlayingController(this, new Handler());
        	
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
	
	public void updateCover(Drawable cover, int mediaType) {
		
		ImageView cdCaseImage = (ImageView) findViewById(R.id.CDCaseImage);
		ImageView dvdCaseImage = (ImageView) findViewById(R.id.DVDCaseImage);
		ImageView cdImage = (ImageView) findViewById(R.id.CDCoverImage);
		ImageView dvdImage = (ImageView) findViewById(R.id.DVDCoverImage);
		ImageView episodeImage = (ImageView) findViewById(R.id.EpisodeImage);
		RelativeLayout cdView = (RelativeLayout) findViewById(R.id.CDView);
		RelativeLayout dvdView = (RelativeLayout) findViewById(R.id.DVDView);		
		
		switch (mediaType) {
			case MediaType.MUSIC:
				cdCaseImage.setImageResource(R.drawable.coverbox_front);
				cdView.setVisibility(View.VISIBLE);
				dvdView.setVisibility(View.GONE);
				episodeImage.setVisibility(View.GONE);
				
				if (cover != null) {
					cdImage.setImageDrawable(cover);
				} else {
					cdImage.setImageResource(R.drawable.coverbox_back);
				}
				break;
			case MediaType.VIDEO_MOVIE:
			case MediaType.VIDEO:
				dvdCaseImage.setImageResource(R.drawable.jewel_dvd);
				dvdView.setVisibility(View.VISIBLE);
				cdView.setVisibility(View.GONE);
				episodeImage.setVisibility(View.GONE);
				
				if (cover != null) {
					dvdImage.setImageDrawable(cover);
				} else {
					dvdImage.setImageResource(R.drawable.jewel_dvd_back);
				}
				break;
			case MediaType.VIDEO_TVEPISODE:
			case MediaType.VIDEO_TVSEASON:
			case MediaType.VIDEO_TVSHOW:
				episodeImage.setVisibility(View.VISIBLE);
				dvdView.setVisibility(View.GONE);
				cdView.setVisibility(View.GONE);
				
				if (cover != null) {
					episodeImage.setImageDrawable(cover);
				} else {
					episodeImage.setImageBitmap(null);
				}
				break;
			default:				
				cdCaseImage.setImageResource(R.drawable.coverbox_front);
				cdImage.setImageResource(R.drawable.coverbox_back);
				cdView.setVisibility(View.VISIBLE);
				dvdView.setVisibility(View.GONE);
				episodeImage.setVisibility(View.GONE);
				break;
		}
	}
	
	public void clear() {
		mSeekBar.setEnabled(false);
		mCounterLeftView.setText("");
		mCounterRightView.setText("");
		mPlayPauseView.setBackgroundResource(R.drawable.now_playing_play);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();		
		menu.add(0, MENU_REMOTE, 0, "Remote control").setIcon(R.drawable.menu_remote);
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
			case MENU_REMOTE:
				final Intent intent = new Intent(this, RemoteActivity.class);
				intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				return true;
		}
		return super.onOptionsItemSelected(item);
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
			client.setController(null);
			return false;
		}
		client.setController(null);
		boolean handled =  (mKeyTracker != null)?mKeyTracker.doKeyDown(keyCode, event):false;
		return handled || super.onKeyDown(keyCode, event);
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
	
	protected void callSuperOnKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
	}
	
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		Intent intent = new Intent(NowPlayingActivity.this, HomeActivity.class);
		intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		return true;
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean handled = (mKeyTracker != null)?mKeyTracker.doKeyUp(keyCode, event):false;
		return handled || super.onKeyUp(keyCode, event);
	}
}
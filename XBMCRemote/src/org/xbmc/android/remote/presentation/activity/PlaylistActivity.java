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
import org.xbmc.android.remote.presentation.controller.PlaylistController;
import org.xbmc.android.util.KeyTracker;
import org.xbmc.android.util.OnLongPressBackKeyTracker;
import org.xbmc.android.util.KeyTracker.Stage;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.eventclient.ButtonCodes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Build.VERSION;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class PlaylistActivity extends Activity {

	private PlaylistController mPlaylistController;

	private ImageButton mPlayPauseView;
	private TextView mLabel1;
	private TextView mLabel2;

	private ConfigurationManager mConfigurationManager;

	private KeyTracker mKeyTracker;
	
	public PlaylistActivity() {
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
		}
	}
	
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

		// create and associate logic
		mPlaylistController = new PlaylistController();
		mPlaylistController.findTitleView(findViewById(R.id.playlist_outer_layout));
		mPlaylistController.findMessageView(findViewById(R.id.playlist_outer_layout));
		mPlaylistController.onCreate(this, new Handler(), (ListView) findViewById(R.id.playlist_list));

		// setup buttons
		mPlaylistController.setupButtons(
			findViewById(R.id.MediaPreviousButton), 
			findViewById(R.id.MediaStopButton), 
			findViewById(R.id.MediaPlayPauseButton), 
			findViewById(R.id.MediaNextButton));
		
		mConfigurationManager = ConfigurationManager.getInstance(this);
	}
	
	public void setTime(String time) {
		mLabel1.setText(time);
		mPlayPauseView.setBackgroundResource(R.drawable.now_playing_pause);
	}
	
	public void setNumItems(String numItems) {
		mLabel2.setText(numItems);
	}
	
	public void clear() {
		mLabel1.setText("--:--");
		mPlayPauseView.setBackgroundResource(R.drawable.now_playing_play);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mPlaylistController.onActivityResume(this);
		mConfigurationManager.onActivityResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mPlaylistController.onActivityPause();
		mConfigurationManager.onActivityPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mPlaylistController.onCreateOptionsMenu(menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mPlaylistController.onOptionsItemSelected(item);
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		mPlaylistController.onCreateContextMenu(menu, v, menuInfo);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		mPlaylistController.onContextItemSelected(item);
		return super.onContextItemSelected(item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		IEventClientManager client = ManagerFactory.getEventClientManager(mPlaylistController);
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
	
	protected void callSuperOnKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
	}
	
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		Intent intent = new Intent(PlaylistActivity.this, HomeActivity.class);
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
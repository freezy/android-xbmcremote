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

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.controller.NowPlayingController;
import org.xbmc.android.remote.presentation.controller.RemoteController;
import org.xbmc.android.remote.presentation.widget.JewelView;
import org.xbmc.android.util.KeyTracker;
import org.xbmc.android.util.KeyTracker.Stage;
import org.xbmc.android.util.OnLongPressBackKeyTracker;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.object.Song;
import org.xbmc.api.type.ThumbSize;
import org.xbmc.eventclient.ButtonCodes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class NowPlayingActivity extends Activity {

	private TextView mTopTitleView;
	private TextView mBottomTitleView;
	private TextView mBottomSubtitleView;
	private TextView mCounterLeftView;
	private TextView mCounterRightView;
	private ImageButton mPlayPauseView;
	private SeekBar mSeekBar;

	private ConfigurationManager mConfigurationManager;
	private NowPlayingController mNowPlayingController;
	private KeyTracker mKeyTracker;

	private boolean mMonitorMode = false;

	private static final int MENU_REMOTE = 303;
	private static final int MENU_MONITOR_MODE = 304;

	public NowPlayingActivity() {
		if (Integer.parseInt(VERSION.SDK) < 5) {
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
		;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set display size
		final Display display = getWindowManager().getDefaultDisplay();
		ThumbSize.setScreenSize(display.getWidth(), display.getHeight());

		setContentView(R.layout.nowplaying);

		mNowPlayingController = new NowPlayingController(this, new Handler());

		mSeekBar = (SeekBar) findViewById(R.id.now_playing_progessbar);
		mTopTitleView = (TextView) findViewById(R.id.now_playing_top_title);
		mBottomTitleView = (TextView) findViewById(R.id.now_playing_bottom_title);
		mBottomSubtitleView = (TextView) findViewById(R.id.now_playing_bottom_subtitle);
		mCounterLeftView = (TextView) findViewById(R.id.now_playing_counter_left);
		mCounterRightView = (TextView) findViewById(R.id.now_playing_counter_right);
		mPlayPauseView = (ImageButton) findViewById(R.id.MediaPlayPauseButton);

		// JewelView jewelCase =
		// (JewelView)findViewById(R.id.now_playing_jewelcase);

		// RelativeLayout.LayoutParams layoutParams = new
		// RelativeLayout.LayoutParams(arg0, arg1)
		// layoutParams.height = 140;
		// layoutParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT;

		// / layoutParams.addRule(RelativeLayout.ABOVE,
		// R.id.now_playing_bottombar);
		// jewelCase.setLayoutParams(layoutParams);

		// remove nasty top fading edge
		FrameLayout topFrame = (FrameLayout) findViewById(android.R.id.content);
		topFrame.setForeground(null);

		// set titlebar text
		((TextView) findViewById(R.id.titlebar_text)).setText("Now playing");

		mConfigurationManager = ConfigurationManager.getInstance(this);

		mNowPlayingController.setupButtons(mSeekBar,
				findViewById(R.id.MediaPreviousButton),
				findViewById(R.id.MediaStopButton),
				findViewById(R.id.MediaPlayPauseButton),
				findViewById(R.id.MediaNextButton),
				findViewById(R.id.MediaPlaylistButton));

	}

	public void setProgressPosition(int pos) {
		if (!mSeekBar.isPressed()) {
			mSeekBar.setProgress(pos);
		}
	}

	public void updateInfo(String topTitle, String bottomTitme,
			String bottomSubtitle) {
		mTopTitleView.setText(topTitle);
		mBottomTitleView.setText(bottomTitme);
		mBottomSubtitleView.setText(bottomSubtitle);
	}

	public void updateProgress(int duration, int time) {
		mSeekBar.setEnabled(duration != 0);

		mCounterLeftView.setVisibility(View.VISIBLE);
		mCounterLeftView.setText(Song.getDuration(time + 1));
		mCounterRightView.setVisibility(View.VISIBLE);
		mCounterRightView.setText(duration == 0 ? "unknown" : "-"
				+ Song.getDuration(duration - time - 1));
		mPlayPauseView.setBackgroundResource(R.drawable.now_playing_pause);
	}

	public void updateCover(Bitmap cover, int mediaType) {
		final JewelView jewelCase = (JewelView) findViewById(R.id.now_playing_jewelcase);
		if (cover == null) {
			jewelCase.setCover(R.drawable.coverbox_back);
		} else {
			jewelCase.setCover(cover);
		}
	}

	@SuppressLint("")
	public void updateFanart(Bitmap fanart, int mediaType) {
		final ImageView background = (ImageView) findViewById(R.id.fanart);
		if (fanart == null) {
			return;
		}
		background.setImageBitmap(fanart);
	}

	public void clear() {
		mSeekBar.setEnabled(false);
		mCounterLeftView.setVisibility(View.GONE);
		mCounterRightView.setVisibility(View.GONE);
		mPlayPauseView.setBackgroundResource(R.drawable.now_playing_play);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(0, MENU_REMOTE, 0, "Remote control").setIcon(
				R.drawable.menu_remote);
		menu.add(0, MENU_MONITOR_MODE, 0, "Monitor mode").setIcon(
				R.drawable.menu_view);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case MENU_REMOTE:
			final Intent intent;
			if (getSharedPreferences("global", Context.MODE_PRIVATE).getInt(
					RemoteController.LAST_REMOTE_PREFNAME, -1) == RemoteController.LAST_REMOTE_GESTURE) {
				intent = new Intent(this, GestureRemoteActivity.class);
			} else {
				intent = new Intent(this, RemoteActivity.class);
			}
			intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(intent);
			return true;
		case MENU_MONITOR_MODE:
			switchMonitorMode();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		IEventClientManager client = ManagerFactory
				.getEventClientManager(mNowPlayingController);
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
			client.sendButton("R1", ButtonCodes.REMOTE_VOLUME_PLUS, false,
					true, true, (short) 0, (byte) 0);
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			client.sendButton("R1", ButtonCodes.REMOTE_VOLUME_MINUS, false,
					true, true, (short) 0, (byte) 0);
			return true;
		case KeyEvent.KEYCODE_SEARCH:
			switchMonitorMode();
			return true;
			/*
			 * case KeyEvent.KEYCODE_PAGE_UP: switchMonitorMode(); return true;
			 */
		}
		client.setController(null);
		boolean handled = (mKeyTracker != null) ? mKeyTracker.doKeyDown(
				keyCode, event) : false;
		return handled || super.onKeyDown(keyCode, event);
	}

	private void switchMonitorMode() {
		mMonitorMode = !mMonitorMode;
		handleLayout();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mNowPlayingController.onActivityResume(this);
		mConfigurationManager.onActivityResume(this);
		handleLayout();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mNowPlayingController.onActivityPause();
		mConfigurationManager.onActivityPause();
	}

	private void handleLayout() {
		if (mMonitorMode) {
			// TODO
		} else {
			// TODO
		}
	}

	protected void callSuperOnKeyDown(int keyCode, KeyEvent event) {
		onBackPressed();
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
		boolean handled = (mKeyTracker != null) ? mKeyTracker.doKeyUp(keyCode,
				event) : false;
		return handled || super.onKeyUp(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		if (isTaskRoot()) {
			Intent intent = new Intent(NowPlayingActivity.this,
					HomeActivity.class);
			NowPlayingActivity.this.startActivity(intent);
		}

		finish();

		return;
	}
}
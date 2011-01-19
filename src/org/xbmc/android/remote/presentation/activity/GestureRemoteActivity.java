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
import org.xbmc.android.remote.presentation.controller.RemoteController;
import org.xbmc.android.widget.gestureremote.GestureRemoteView;
import org.xbmc.eventclient.ButtonCodes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Gesture mode. Let's see how this works out...
 * 
 * @author Team XBMC
 */
public class GestureRemoteActivity extends Activity {
	
	private final static String TAG = "GestureRemoteActivity";
	
	private ConfigurationManager mConfigurationManager;
	private RemoteController mRemoteController;
	
	private static final int MENU_NOW_PLAYING = 401;
	private static final int MENU_SWITCH_BUTTONS = 402;
//	private static final int MENU_SWITCH_MOUSE = 403;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Display d = getWindowManager().getDefaultDisplay();
		final int w = d.getWidth();
		final int h = d.getHeight();
		final double ar = w > h ? (double) w / (double) h : (double) h / (double) w;
		if (ar > 1.6) {
			Log.i(TAG, "AR = " + ar + ", using extended layout.");
			setContentView(R.layout.remote_gesture_extended);
		} else {
			Log.i(TAG, "AR = " + ar + ", normal layout.");
			setContentView(R.layout.remote_gesture);
		}
		
		// remove nasty top fading edge
		FrameLayout topFrame = (FrameLayout)findViewById(android.R.id.content);
		topFrame.setForeground(null);
		
		GestureRemoteView view = (GestureRemoteView)findViewById(R.id.RemoteXboxGestureZone);	
		mRemoteController = new RemoteController(getApplicationContext());
		view.setGestureListener(mRemoteController.startGestureThread(this.getApplicationContext()));
		
		mConfigurationManager = ConfigurationManager.getInstance(this);
		// mConfigurationManager.initKeyguard(true);
		setupButtons();
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		return mRemoteController.onTrackballEvent(event);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return mRemoteController.onKeyDown(keyCode, event) ? true : super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		getSharedPreferences("global", Context.MODE_PRIVATE).edit().putInt(RemoteController.LAST_REMOTE_PREFNAME, RemoteController.LAST_REMOTE_GESTURE).commit();
		mRemoteController.onActivityResume(this);
		mConfigurationManager.onActivityResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mConfigurationManager.onActivityPause();
		mRemoteController.onActivityPause();
	}

	/**
	 * Assigns the button events to the views.
	 */
	private void setupButtons() {
		// display
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnDisplay), ButtonCodes.REMOTE_DISPLAY);
		
		// seek back
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnSeekBack), ButtonCodes.REMOTE_REVERSE);
		// play
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnPlay), ButtonCodes.REMOTE_PLAY);
		// seek forward
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnSeekForward), ButtonCodes.REMOTE_FORWARD);

		// previous
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnPrevious), ButtonCodes.REMOTE_SKIP_MINUS);
		// stop
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnStop), ButtonCodes.REMOTE_STOP);
		// pause
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnPause), ButtonCodes.REMOTE_PAUSE);
		// next
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnNext), ButtonCodes.REMOTE_SKIP_PLUS);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_SWITCH_BUTTONS, 0, "Switch to buttons").setIcon(R.drawable.menu_remote);
		menu.add(0, MENU_NOW_PLAYING, 0, "Now playing").setIcon(R.drawable.menu_nowplaying);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
			case MENU_NOW_PLAYING:
				intent = new Intent(this, NowPlayingActivity.class);
				break;
			case MENU_SWITCH_BUTTONS:
				intent = new Intent(this, RemoteActivity.class);
				intent.addFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
				break;
		}
		if (intent != null) {
			startActivity(intent);
			return true;
		}
		return false;
	}
}
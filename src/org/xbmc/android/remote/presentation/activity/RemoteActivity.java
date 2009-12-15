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
import org.xbmc.android.util.ConnectionFactory;
import org.xbmc.eventclient.ButtonCodes;
import org.xbmc.eventclient.EventClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;

/**
 * Activity for remote control. At the moment that's the good ol' Xbox remote
 * control, more to come...
 * 
 * @author Team XBMC
 */
public class RemoteActivity extends Activity {
	private static final int MENU_NOW_PLAYING = 401;
	
	private Vibrator mVibrator;
	private EventClient mClient;
	
	private ConfigurationManager mConfigurationManager;
	
	/**
	 * timestamp since last trackball use.
	 */
	private long mTimestamp = 0;


    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.remote_xbox);
		
		// remove nasty top fading edge
		FrameLayout topFrame = (FrameLayout)findViewById(android.R.id.content);
		topFrame.setForeground(null);
		
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		mClient = ConnectionFactory.getEventClient(this);
        
		mConfigurationManager = ConfigurationManager.getInstance(this);
		mConfigurationManager.initKeyguard(true);
		
		WindowManager wm = getWindowManager(); 
        Display d = wm.getDefaultDisplay();
        
		if (d.getWidth() > d.getHeight())
			setupButtonsLandscape();
		else
			setupButtonsPortrait();
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN)
			return keyboardAction(ButtonCodes.KEYBOARD_ENTER);
		else{
			// check when the last trackball move happened to avoid too speedy selections
			long newstamp = System.currentTimeMillis(); 
			if(newstamp - mTimestamp > 300){
				mTimestamp = newstamp;
				if (Math.abs(event.getX()) > 0.15f) {
					return keyboardAction(event.getX() < 0 ? ButtonCodes.KEYBOARD_LEFT : ButtonCodes.KEYBOARD_RIGHT);
				}else if (Math.abs(event.getY()) > 0.15f){
					return keyboardAction(event.getY() < 0 ? ButtonCodes.KEYBOARD_UP : ButtonCodes.KEYBOARD_DOWN);
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		char key = (char)event.getUnicodeChar();
		if (key > 'A' && key < 'z')
			return keyboardAction("" + key);
		
		try {
			switch (keyCode) {
				case KeyEvent.KEYCODE_VOLUME_UP:
					mClient.sendButton("R1", ButtonCodes.REMOTE_VOLUME_PLUS, false, true, true, (short)0, (byte)0);
					return true;
				case KeyEvent.KEYCODE_VOLUME_DOWN:
					mClient.sendButton("R1", ButtonCodes.REMOTE_VOLUME_MINUS, false, true, true, (short)0, (byte)0);
					return true;
				case KeyEvent.KEYCODE_DPAD_DOWN:
					mClient.sendButton("R1", ButtonCodes.REMOTE_DOWN, false, true, true, (short)0, (byte)0);
					return true;
				case KeyEvent.KEYCODE_DPAD_UP:
					mClient.sendButton("R1", ButtonCodes.REMOTE_UP, false, true, true, (short)0, (byte)0);
					return true;
				case KeyEvent.KEYCODE_DPAD_LEFT:
					mClient.sendButton("R1", ButtonCodes.REMOTE_LEFT, false, true, true, (short)0, (byte)0);
					return true;
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					mClient.sendButton("R1", ButtonCodes.REMOTE_RIGHT, false, true, true, (short)0, (byte)0);
					return true;
				case KeyEvent.KEYCODE_DPAD_CENTER:
					mClient.sendButton("R1", ButtonCodes.REMOTE_ENTER, false, true, true, (short)0, (byte)0);
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
		mConfigurationManager.onActivityResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mConfigurationManager.onActivityPause();
	}
	
	
	/**
	 * Sends a keyboard event
	 * @param button
	 * @return
	 */
	private boolean keyboardAction(String button) {
		try {
			mClient.sendButton("KB", button, false, true, true, (short)0, (byte)0);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Checks the preferences if vibration on click is activated.
	 * @return True if vibration activated, false otherwise.
	 */
	private boolean isVibrationSet(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		return prefs.getBoolean("setting_vibrate_on_touch", true);
	}
	
	/**
	 * Handles the push- release button code. Switches image of the pressed
	 * button, vibrates and executes command.
	 */
	private class OnRemoteAction implements OnTouchListener {
		private final String mAction;
		public OnRemoteAction(String action) {
			mAction = action;
		}
		public boolean onTouch(View v, MotionEvent event) {
			try {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (isVibrationSet()) {
						mVibrator.vibrate(45);
					}
					mClient.sendButton("R1", mAction, true, true, true, (short)0, (byte)0);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					mClient.sendButton("R1", mAction, false, false, true, (short)0, (byte)0);
				}
			} catch (IOException e) {
				return false;
			}
			return false;
		}
	}
	
	/**
	 * Shortcut for adding the listener class to the button
	 * @param resourceButton       Resource ID of the button
	 * @param action               Action string
	 * @param resourceButtonUp     Resource ID of the button up image
	 * @param resourceButtonDown   Resource ID of the button down image
	 */
	private void setupButton(int resourceButton, String action) {
		final View btn = findViewById(resourceButton);
		if (btn != null) {
			findViewById(resourceButton).setOnTouchListener(new OnRemoteAction(action));
		}
	}
	
	/**
	 * Assigns the button events to the views.
	 */
	private void setupButtonsPortrait() {
		
		// display
		setupButton(R.id.RemoteXboxImgBtnDisplay, ButtonCodes.REMOTE_DISPLAY);
		
		setupButton(R.id.RemoteXboxImgBtnVideo, ButtonCodes.REMOTE_MY_VIDEOS);
		setupButton(R.id.RemoteXboxImgBtnMusic, ButtonCodes.REMOTE_MY_MUSIC);
		setupButton(R.id.RemoteXboxImgBtnImages, ButtonCodes.REMOTE_MY_PICTURES);
		setupButton(R.id.RemoteXboxImgBtnTv, ButtonCodes.REMOTE_MY_TV);
		
		// seek back
		setupButton(R.id.RemoteXboxImgBtnSeekBack, ButtonCodes.REMOTE_REVERSE);
		// play
		setupButton(R.id.RemoteXboxImgBtnPlay, ButtonCodes.REMOTE_PLAY);
		// seek forward
		setupButton(R.id.RemoteXboxImgBtnSeekForward, ButtonCodes.REMOTE_FORWARD);

		// previous
		setupButton(R.id.RemoteXboxImgBtnPrevious, ButtonCodes.REMOTE_SKIP_MINUS);
		// stop
		setupButton(R.id.RemoteXboxImgBtnStop, ButtonCodes.REMOTE_STOP);
		// pause
		setupButton(R.id.RemoteXboxImgBtnPause, ButtonCodes.REMOTE_PAUSE);
		// next
		setupButton(R.id.RemoteXboxImgBtnNext, ButtonCodes.REMOTE_SKIP_PLUS);
		
		// title
		setupButton(R.id.RemoteXboxImgBtnTitle, ButtonCodes.REMOTE_TITLE);
		// up
		setupButton(R.id.RemoteXboxImgBtnUp, ButtonCodes.REMOTE_UP);
		// info
		setupButton(R.id.RemoteXboxImgBtnInfo, ButtonCodes.REMOTE_INFO);
		
		// left
		setupButton(R.id.RemoteXboxImgBtnLeft, ButtonCodes.REMOTE_LEFT);
		// select
		setupButton(R.id.RemoteXboxImgBtnSelect, ButtonCodes.REMOTE_SELECT);
		// right
		setupButton(R.id.RemoteXboxImgBtnRight, ButtonCodes.REMOTE_RIGHT);
		
		// menu
		setupButton(R.id.RemoteXboxImgBtnMenu, ButtonCodes.REMOTE_MENU);
		// down
		setupButton(R.id.RemoteXboxImgBtnDown, ButtonCodes.REMOTE_DOWN);
		// back 
		setupButton(R.id.RemoteXboxImgBtnBack, ButtonCodes.REMOTE_BACK);
	}
	
	/**
	 * Assigns the button events to the views.
	 */
	private void setupButtonsLandscape() {
		
		setupButtonsPortrait();
		
		// tv
		setupButton(R.id.RemoteXboxImgBtnTv, ButtonCodes.REMOTE_MY_TV);
		// music
		setupButton(R.id.RemoteXboxImgBtnMusic, ButtonCodes.REMOTE_MY_MUSIC);
		// pictures
		setupButton(R.id.RemoteXboxImgBtnPictures, ButtonCodes.REMOTE_MY_PICTURES);
		// videos
		setupButton(R.id.RemoteXboxImgBtnVideos, ButtonCodes.REMOTE_MY_VIDEOS);
		// settings
		setupButton(R.id.RemoteXboxImgBtnPower, ButtonCodes.REMOTE_POWER);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_NOW_PLAYING, 0, "Now playing").setIcon(R.drawable.menu_nowplaying);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent myIntent = null;
		switch (item.getItemId()) {
		case MENU_NOW_PLAYING:
			myIntent = new Intent(this, NowPlayingActivity.class);
			break;
		}
		if (myIntent != null) {
			startActivity(myIntent);
			return true;
		}
		return false;
	}
}
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

import org.xbmc.android.remote.R;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.android.util.ErrorHandler;
import org.xbmc.eventclient.ButtonCodes;
import org.xbmc.eventclient.EventClient;
import org.xbmc.httpapi.type.MediaType;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
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
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * Activity for remote control. At the moment that's the good ol' Xbox remote
 * control, more to come...
 * 
 * @author Team XBMC
 */
public class RemoteActivity extends Activity implements OnSharedPreferenceChangeListener {
	private static final int MENU_NOW_PLAYING = 401;
	
	private Vibrator mVibrator;
	private EventClient mClient;
	private KeyguardManager.KeyguardLock mKeyguardLock = null;
	private boolean mDisableKeyguard = false;
	
	/**
	 * timestamp since last trackball use.
	 */
	private long mTimestamp = 0;


    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ErrorHandler.setActivity(this);
		setContentView(R.layout.remote_xbox);
		
		// remove nasty top fading edge
		FrameLayout topFrame = (FrameLayout)findViewById(android.R.id.content);
		topFrame.setForeground(null);
		
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		mClient = ConnectionManager.getEventClient(this);
        
    	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	String disableKeyguardString = prefs.getString("setting_disable_keyguard", "0");
    	mDisableKeyguard = ( disableKeyguardString.equals("1") || disableKeyguardString.equals("2") );
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		WindowManager wm = getWindowManager(); 
        Display d = wm.getDefaultDisplay();
        
		if (d.getWidth() > d.getHeight())
			setupButtonsLandscape();
		else
			setupButtonsPortrait();
	}

    @Override
    protected void onResume(){
    	super.onResume();
    	if(mDisableKeyguard) {
    		KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);
			mKeyguardLock = keyguardManager.newKeyguardLock("RemoteActivityKeyguardLock");
			mKeyguardLock.disableKeyguard();
    	}
    }
    
	@Override
	protected void onPause() {
		super.onPause();
		if (mKeyguardLock != null){
			mKeyguardLock.reenableKeyguard();
			mKeyguardLock = null;
		}
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
		private final int mUp, mDown;
		public OnRemoteAction(String action, int up, int down) {
			mAction = action;
			mUp = up;
			mDown = down;
		}
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (isVibrationSet())
					mVibrator.vibrate(45);
				try {
					mClient.sendButton("R1", mAction, true, true, true, (short)0, (byte)0);
				} catch (IOException e) {
					return false;
				}
				((Button)v).setBackgroundResource(mDown);
				return true;
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				try {
					mClient.sendButton("R1", mAction, false, false, true, (short)0, (byte)0);
				} catch (IOException e) {
					return false;
				}
				((Button)v).setBackgroundResource(mUp);
				return true;
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
	private void setupButton(int resourceButton, String action, int resourceButtonUp, int resourceButtonDown) {
		findViewById(resourceButton).setOnTouchListener(new OnRemoteAction(action, resourceButtonUp, resourceButtonDown));		
	}
	
	/**
	 * Assigns the button events to the views.
	 */
	private void setupButtonsPortrait() {
		
		// display
		setupButton(R.id.RemoteXboxImgBtnDisplay, ButtonCodes.REMOTE_DISPLAY, R.drawable.remote_xbox_display, R.drawable.remote_xbox_display_down);
		
		// seek back
		setupButton(R.id.RemoteXboxImgBtnSeekBack, ButtonCodes.REMOTE_REVERSE, R.drawable.remote_xbox_seek_back, R.drawable.remote_xbox_seek_back_down);
		// play
		setupButton(R.id.RemoteXboxImgBtnPlay, ButtonCodes.REMOTE_PLAY, R.drawable.remote_xbox_play, R.drawable.remote_xbox_play_down);
		// seek forward
		setupButton(R.id.RemoteXboxImgBtnSeekForward, ButtonCodes.REMOTE_FORWARD, R.drawable.remote_xbox_seek_forward, R.drawable.remote_xbox_seek_forward_down);

		// previous
		setupButton(R.id.RemoteXboxImgBtnPrevious, ButtonCodes.REMOTE_SKIP_MINUS, R.drawable.remote_xbox_previous, R.drawable.remote_xbox_previous_down);
		// stop
		setupButton(R.id.RemoteXboxImgBtnStop, ButtonCodes.REMOTE_STOP, R.drawable.remote_xbox_stop, R.drawable.remote_xbox_stop_down);
		// pause
		setupButton(R.id.RemoteXboxImgBtnPause, ButtonCodes.REMOTE_PAUSE, R.drawable.remote_xbox_pause, R.drawable.remote_xbox_pause_down);
		// next
		setupButton(R.id.RemoteXboxImgBtnNext, ButtonCodes.REMOTE_SKIP_PLUS, R.drawable.remote_xbox_next, R.drawable.remote_xbox_next_down);
		
		// title
		setupButton(R.id.RemoteXboxImgBtnTitle, ButtonCodes.REMOTE_TITLE, R.drawable.remote_xbox_title, R.drawable.remote_xbox_title_down);
		// up
		setupButton(R.id.RemoteXboxImgBtnUp, ButtonCodes.REMOTE_UP, R.drawable.remote_xbox_up, R.drawable.remote_xbox_up_down);
		// info
		setupButton(R.id.RemoteXboxImgBtnInfo, ButtonCodes.REMOTE_INFO, R.drawable.remote_xbox_info, R.drawable.remote_xbox_info_down);
		
		// left
		setupButton(R.id.RemoteXboxImgBtnLeft, ButtonCodes.REMOTE_LEFT, R.drawable.remote_xbox_left, R.drawable.remote_xbox_left_down);
		// select
		setupButton(R.id.RemoteXboxImgBtnSelect, ButtonCodes.REMOTE_SELECT, R.drawable.remote_xbox_select, R.drawable.remote_xbox_select_down);
		// right
		setupButton(R.id.RemoteXboxImgBtnRight, ButtonCodes.REMOTE_RIGHT, R.drawable.remote_xbox_right, R.drawable.remote_xbox_right_down);
		
		// menu
		setupButton(R.id.RemoteXboxImgBtnMenu, ButtonCodes.REMOTE_MENU, R.drawable.remote_xbox_menu, R.drawable.remote_xbox_menu_down);
		// down
		setupButton(R.id.RemoteXboxImgBtnDown, ButtonCodes.REMOTE_DOWN, R.drawable.remote_xbox_down, R.drawable.remote_xbox_down_down);
		// back 
		setupButton(R.id.RemoteXboxImgBtnBack, ButtonCodes.REMOTE_BACK, R.drawable.remote_xbox_back, R.drawable.remote_xbox_back_down);
	}
	
	/**
	 * Assigns the button events to the views.
	 */
	private void setupButtonsLandscape() {
		
		// display
		setupButton(R.id.RemoteXboxImgBtnDisplay, ButtonCodes.REMOTE_DISPLAY, R.drawable.remote_xbox_landscape_display, R.drawable.remote_xbox_landscape_display_down);
		
		// seek back
		setupButton(R.id.RemoteXboxImgBtnSeekBack, ButtonCodes.REMOTE_REVERSE, R.drawable.remote_xbox_landscape_seek_back, R.drawable.remote_xbox_landscape_seek_back_down);
		// play
		setupButton(R.id.RemoteXboxImgBtnPlay, ButtonCodes.REMOTE_PLAY, R.drawable.remote_xbox_landscape_play, R.drawable.remote_xbox_landscape_play_down);
		// seek forward
		setupButton(R.id.RemoteXboxImgBtnSeekForward, ButtonCodes.REMOTE_FORWARD, R.drawable.remote_xbox_landscape_seek_forward, R.drawable.remote_xbox_landscape_seek_forward_down);
		
		// previous
		setupButton(R.id.RemoteXboxImgBtnPrevious, ButtonCodes.REMOTE_SKIP_MINUS, R.drawable.remote_xbox_landscape_previous, R.drawable.remote_xbox_landscape_previous_down);
		// stop
		setupButton(R.id.RemoteXboxImgBtnStop, ButtonCodes.REMOTE_STOP, R.drawable.remote_xbox_landscape_stop, R.drawable.remote_xbox_landscape_stop_down);
		// pause
		setupButton(R.id.RemoteXboxImgBtnPause, ButtonCodes.REMOTE_PAUSE, R.drawable.remote_xbox_landscape_pause, R.drawable.remote_xbox_landscape_pause_down);
		// next
		setupButton(R.id.RemoteXboxImgBtnNext, ButtonCodes.REMOTE_SKIP_PLUS, R.drawable.remote_xbox_landscape_next, R.drawable.remote_xbox_landscape_next_down);
		
		// title
		setupButton(R.id.RemoteXboxImgBtnTitle, ButtonCodes.REMOTE_TITLE, R.drawable.remote_xbox_landscape_title, R.drawable.remote_xbox_landscape_title_down);
		// up
		setupButton(R.id.RemoteXboxImgBtnUp, ButtonCodes.REMOTE_UP, R.drawable.remote_xbox_landscape_up, R.drawable.remote_xbox_landscape_up_down);
		// info
		setupButton(R.id.RemoteXboxImgBtnInfo, ButtonCodes.REMOTE_INFO, R.drawable.remote_xbox_landscape_info, R.drawable.remote_xbox_landscape_info_down);
		
		// left
		setupButton(R.id.RemoteXboxImgBtnLeft, ButtonCodes.REMOTE_LEFT, R.drawable.remote_xbox_landscape_left, R.drawable.remote_xbox_landscape_left_down);
		// select
		setupButton(R.id.RemoteXboxImgBtnSelect, ButtonCodes.REMOTE_SELECT, R.drawable.remote_xbox_landscape_select, R.drawable.remote_xbox_landscape_select_down);
		// right
		setupButton(R.id.RemoteXboxImgBtnRight, ButtonCodes.REMOTE_RIGHT, R.drawable.remote_xbox_landscape_right, R.drawable.remote_xbox_landscape_right_down);
		
		// menu
		setupButton(R.id.RemoteXboxImgBtnMenu, ButtonCodes.REMOTE_MENU, R.drawable.remote_xbox_landscape_menu, R.drawable.remote_xbox_landscape_menu_down);
		// down
		setupButton(R.id.RemoteXboxImgBtnDown, ButtonCodes.REMOTE_DOWN, R.drawable.remote_xbox_landscape_down, R.drawable.remote_xbox_landscape_down_down);
		// back 
		setupButton(R.id.RemoteXboxImgBtnBack, ButtonCodes.REMOTE_BACK, R.drawable.remote_xbox_landscape_back, R.drawable.remote_xbox_landscape_back_down);
		
		/* now those are the special keys */
		// tv
		setupButton(R.id.RemoteXboxImgBtnTv, ButtonCodes.REMOTE_MY_TV, R.drawable.remote_xbox_landscape_tv, R.drawable.remote_xbox_landscape_tv_down);
		// music
		setupButton(R.id.RemoteXboxImgBtnMusic, ButtonCodes.REMOTE_MY_MUSIC, R.drawable.remote_xbox_landscape_music, R.drawable.remote_xbox_landscape_music_down);
		// pictures
		setupButton(R.id.RemoteXboxImgBtnPictures, ButtonCodes.REMOTE_MY_PICTURES, R.drawable.remote_xbox_landscape_pictures, R.drawable.remote_xbox_landscape_pictures_down);
		// videos
		setupButton(R.id.RemoteXboxImgBtnVideos, ButtonCodes.REMOTE_MY_VIDEOS, R.drawable.remote_xbox_landscape_videos, R.drawable.remote_xbox_landscape_videos_down);
		// settings
		setupButton(R.id.RemoteXboxImgBtnPower, ButtonCodes.REMOTE_POWER, R.drawable.remote_xbox_landscape_power, R.drawable.remote_xbox_landscape_power_down);
		
		
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
			myIntent.putExtra("shareType", MediaType.music.toString());
			break;
		}
		if (myIntent != null) {
			startActivity(myIntent);
			return true;
		}
		return false;
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals("setting_disable_keyguard")) {
			String disableKeyguardString = sharedPreferences.getString(key, "0");
			boolean disableKeyguardState = ( disableKeyguardString.equals("1") || disableKeyguardString.equals("2") );
			if (disableKeyguardState != mDisableKeyguard){
				if (disableKeyguardState) {
					if(this.hasWindowFocus()  ) {
		    			KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);
						mKeyguardLock = keyguardManager.newKeyguardLock("RemoteActivityKeyguardLock");
						mKeyguardLock.disableKeyguard();
					}
				}
				else {
					if(this.hasWindowFocus()) {
						if (mKeyguardLock != null) {
							mKeyguardLock.reenableKeyguard();
						}
						mKeyguardLock = null;
					}
				}
				mDisableKeyguard = disableKeyguardState;
			}
		}
	}
}
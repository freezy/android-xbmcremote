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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;

/**
 * Activity for remote control. At the moment that's the good ol' Xbox remote
 * control, more to come...
 * 
 * TODO: Horizontal orientation
 * TODO: "display" button missing. Looking for an alternative, also the "S" button is quite important for skins like Aeon.
 * 
 * @author Team XBMC
 */
public class RemoteActivity extends Activity {
	
	private Vibrator mVibrator;
	private EventClient mClient;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ErrorHandler.setActivity(this);
        setContentView(R.layout.remote_xbox);
        
        mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
  	  	mClient = ConnectionManager.getEventClient(this);

		setupButtons();
    }
    
	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN)
			return keyboardAction(ButtonCodes.KEYBOARD_ENTER);
		else if (Math.abs(event.getX()) > 0.1f)
			return keyboardAction(event.getX() < 0 ? ButtonCodes.KEYBOARD_LEFT : ButtonCodes.KEYBOARD_RIGHT);
		else if (Math.abs(event.getY()) > 0.1f)
			return keyboardAction(event.getY() < 0 ? ButtonCodes.KEYBOARD_UP : ButtonCodes.KEYBOARD_DOWN);
		return false;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		char key = (char)event.getUnicodeChar();
		
		if (key > 'A' && key < 'z')
			return keyboardAction("" + key);
		
		try {
			if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
				mClient.sendButton("R1", ButtonCodes.REMOTE_VOLUME_PLUS, false, true, true, (short)0, (byte)0);
				return true;
			}
			if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
				mClient.sendButton("R1", ButtonCodes.REMOTE_VOLUME_MINUS, false, true, true, (short)0, (byte)0);
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
				mVibrator.vibrate(45);
				try {
					mClient.sendButton("R1", mAction, true, true, true, (short)0, (byte)0);
				} catch (IOException e) {
					return false;
				}
				((ImageButton)v).setImageResource(mDown);
				return true;
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				try {
					mClient.sendButton("R1", mAction, false, false, true, (short)0, (byte)0);
				} catch (IOException e) {
					return false;
				}
				((ImageButton)v).setImageResource(mUp);
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
	private void setupButtons() {
		
		// seek back
		setupButton(R.id.RemoteXboxImgBtnSeekBack, ButtonCodes.REMOTE_REVERSE, R.drawable.remote_xbox_play, R.drawable.remote_xbox_play_down);
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
		setupButton(R.id.RemoteXboxImgBtnMenu, ButtonCodes.REMOTE_MENU, R.drawable.remote_xbox_menu_down, R.drawable.remote_xbox_menu_down);
		// down
		setupButton(R.id.RemoteXboxImgBtnDown, ButtonCodes.REMOTE_DOWN, R.drawable.remote_xbox_down_down, R.drawable.remote_xbox_down);
		// back 
		setupButton(R.id.RemoteXboxImgBtnBack, ButtonCodes.REMOTE_BACK, R.drawable.remote_xbox_back, R.drawable.remote_xbox_back_down);
		
	}
}
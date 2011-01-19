package org.xbmc.android.remote.presentation.controller;

import java.io.IOException;

import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.eventclient.ButtonCodes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class GestureController extends AbstractController implements INotifiableController, IController {
	
	IEventClientManager mEventClientManager;
	private long mTimestamp = 0;
	
	public GestureController(Context context) {
		mEventClientManager = ManagerFactory.getEventClientManager(this);
	}
	
	/**
	 * Sends a keyboard event
	 * @param button
	 * @return
	 */
	private boolean keyboardAction(String button) {
		try {
			mEventClientManager.sendButton("KB", button, false, true, true, (short)0, (byte)0);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	
	public boolean onTrackballEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN)
			return keyboardAction(ButtonCodes.KEYBOARD_ENTER);
		else{
			// check when the last trackball move happened to avoid too speedy selections
			long newstamp = System.currentTimeMillis(); 
			if (newstamp - mTimestamp > 300){
				mTimestamp = newstamp;
				if (Math.abs(event.getX()) > 0.15f) {
					return keyboardAction(event.getX() < 0 ? ButtonCodes.KEYBOARD_LEFT : ButtonCodes.KEYBOARD_RIGHT);
				} else if (Math.abs(event.getY()) > 0.15f){
					return keyboardAction(event.getY() < 0 ? ButtonCodes.KEYBOARD_UP : ButtonCodes.KEYBOARD_DOWN);
				}
			}
		}
		return false;
	}
	
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		char key = (char)event.getUnicodeChar();
		if (key > 'A' && key < 'z')
			return keyboardAction("" + key);
		try {
			switch (keyCode) {
				case KeyEvent.KEYCODE_VOLUME_UP:
					mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_VOLUME_PLUS, false, true, true, (short)0, (byte)0);
					return true;
				case KeyEvent.KEYCODE_VOLUME_DOWN:
					mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_VOLUME_MINUS, false, true, true, (short)0, (byte)0);
					return true;
				case KeyEvent.KEYCODE_DPAD_DOWN:
					mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_DOWN, false, true, true, (short)0, (byte)0);
					return true;
				case KeyEvent.KEYCODE_DPAD_UP:
					mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_UP, false, true, true, (short)0, (byte)0);
					return true;
				case KeyEvent.KEYCODE_DPAD_LEFT:
					mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_LEFT, false, true, true, (short)0, (byte)0);
					return true;
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_RIGHT, false, true, true, (short)0, (byte)0);
					return true;
				case KeyEvent.KEYCODE_DPAD_CENTER:
					mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_ENTER, false, true, true, (short)0, (byte)0);
					return true;
				default: 
					return false;
			}
		} catch (IOException e) {
			return false;
		}
	}

	public void onActivityPause() {
		mEventClientManager.setController(null);
		super.onActivityPause();
	}

	public void onActivityResume(Activity activity) {
		super.onActivityResume(activity);
		mEventClientManager.setController(this);
	}
}
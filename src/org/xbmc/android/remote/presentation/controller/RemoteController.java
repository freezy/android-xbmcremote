package org.xbmc.android.remote.presentation.controller;

import java.io.IOException;

import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.eventclient.ButtonCodes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

public class RemoteController extends AbstractController implements INotifiableController, IController {
	
	IEventClientManager mEventClientManager;
	
	/**
	 * timestamp since last trackball use.
	 */
	private long mTimestamp = 0;
	private final Vibrator mVibrator;
	private final boolean mDoVibrate;
	
	public RemoteController(Context context) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		mEventClientManager = ManagerFactory.getEventClientManager(this);
		mVibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		mDoVibrate = prefs.getBoolean("setting_vibrate_on_touch", true);
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

	/**
	 * Shortcut for adding the listener class to the button
	 * @param resourceButton       Resource ID of the button
	 * @param action               Action string
	 * @param resourceButtonUp     Resource ID of the button up image
	 * @param resourceButtonDown   Resource ID of the button down image
	 */
	public void setupButton(View btn, String action) {
		if (btn != null) {
			btn.setOnTouchListener(new OnRemoteAction(action));
			((Button)btn).setSoundEffectsEnabled(true);
			((Button)btn).setClickable(true);
		}
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
					if (mDoVibrate) {
						mVibrator.vibrate(30);
					}
					mEventClientManager.sendButton("R1", mAction, true, true, true, (short)0, (byte)0);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					v.playSoundEffect(AudioManager.FX_KEY_CLICK);
					mEventClientManager.sendButton("R1", mAction, false, false, true, (short)0, (byte)0);
				}
			} catch (IOException e) {
				return false;
			}
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
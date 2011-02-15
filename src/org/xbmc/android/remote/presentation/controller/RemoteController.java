/*
 *      Copyright (C) 2005-2011 Team XBMC
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

package org.xbmc.android.remote.presentation.controller;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.activity.GestureRemoteActivity;
import org.xbmc.android.remote.presentation.activity.NowPlayingActivity;
import org.xbmc.android.widget.gestureremote.IGestureListener;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.business.IInfoManager;
import org.xbmc.api.info.GuiSettings;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.eventclient.ButtonCodes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

public class RemoteController extends AbstractController implements INotifiableController, IController {

	public static final int LAST_REMOTE_BUTTON = 0;
	public static final int LAST_REMOTE_GESTURE = 1;
	public static final String LAST_REMOTE_PREFNAME = "last_remote_type";
	
	private static final int MENU_NOW_PLAYING = 401;
	private static final int MENU_XBMC_EXIT = 402;
	private static final int MENU_XBMC_S = 403;
//	private static final int MENU_SWITCH_MOUSE = 404;
	private static final int MENU_SWITCH_GESTURE = 405;

	
	private static final int DPAD_DOWN_MIN_DELTA_TIME = 100;
	private static final int MOTION_EVENT_MIN_DELTA_TIME = 250;
	private static final float MOTION_EVENT_MIN_DELTA_POSITION = 0.15f;
	
	private static final long VIBRATION_LENGTH = 45;
	
	IEventClientManager mEventClientManager;
	IInfoManager mInfoManager;
	IControlManager mControl;
	GestureThread mGestureThread;
	
	/**
	 * timestamp since last trackball use.
	 */
	private long mTimestamp = 0;
	private final Vibrator mVibrator;
	private final boolean mDoVibrate;
	
	private int mEventServerInitialDelay = 750;
	
	private Timer tmrKeyPress;
	
	final SharedPreferences prefs;
	
	public RemoteController(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);

		mControl = ManagerFactory.getControlManager(this);
		mInfoManager = ManagerFactory.getInfoManager(this);
		mEventClientManager = ManagerFactory.getEventClientManager(this);
		mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		mDoVibrate = prefs.getBoolean("setting_vibrate_on_touch", true);
		mInfoManager.getGuiSettingInt(new DataResponse<Integer>() {
//			@Override
//			public void run() {
//				mHandler.post(new Runnable() {
//					public void run() {
//						mEventServerInitialDelay = value;
//						Log.i("RemoteController", "Saving previous value " + GuiSettings.getName(GuiSettings.Services.EVENT_SERVER_INITIAL_DELAY) + " = " + value);
//					}
//				});
//			}
		}, GuiSettings.Services.EVENT_SERVER_INITIAL_DELAY, context);
	}

	public IGestureListener startGestureThread(final Context context) {
		mGestureThread = new GestureThread(mEventClientManager);
		IGestureListener listener = new IGestureListener() {
			private boolean mScrolling = false;
			public double[] getZones() {
				double[] ret = { 0.13, 0.25, 0.5, 0.75 };
				return ret;
			}
			public void onHorizontalMove(int value) {
				Log.d(TAG, "onHorizontalMove(" + value + ")");
				if (value == 0) {
					if (mGestureThread != null) {
						mGestureThread.quit();
						mGestureThread = null;
					}
				} else {
					if (mGestureThread == null) {
						mGestureThread = new GestureThread(mEventClientManager);
					}
					mGestureThread.setLevel(value, value > 0 ? GestureThread.ACTION_RIGHT : GestureThread.ACTION_LEFT);
				}
			}
			public void onVerticalMove(int value) {
				Log.d(TAG, "onVerticalMove(" + value + ")");
				if (value == 0) {
					if (mGestureThread != null) {
						mGestureThread.quit();
						mGestureThread = null;
					}
				} else {
					if (mGestureThread == null) {
						mGestureThread = new GestureThread(mEventClientManager);
					}
					mGestureThread.setLevel(value, value > 0 ? GestureThread.ACTION_DOWN : GestureThread.ACTION_UP);
				}
			}
			private void scroll(String button, double amount) {
				try {
					if (amount != 0) {
						if (!mScrolling) {
							Log.i(TAG, "Setting " + GuiSettings.getName(GuiSettings.Services.EVENT_SERVER_INITIAL_DELAY) + " = " + 25);
							mInfoManager.setGuiSettingInt(new DataResponse<Boolean>(), GuiSettings.Services.EVENT_SERVER_INITIAL_DELAY, 25, context);
						}
						mEventClientManager.sendButton("XG", button, true, true, false, (short)(amount * 65535), (byte)0);
						mScrolling = true;
					} else {
						mEventClientManager.sendButton("XG", button, false, false, false, (short)0, (byte)0);
						Log.i(TAG, "Restoring " + GuiSettings.getName(GuiSettings.Services.EVENT_SERVER_INITIAL_DELAY) + " = " + mEventServerInitialDelay);
						mInfoManager.setGuiSettingInt(new DataResponse<Boolean>(), GuiSettings.Services.EVENT_SERVER_INITIAL_DELAY, mEventServerInitialDelay, context);
						mScrolling = false;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			public void onScrollDown(double amount) {
				Log.d(TAG, "onScrollDown(" + amount + ")");
				scroll(ButtonCodes.GAMEPAD_RIGHT_ANALOG_TRIGGER, amount);
			}
			public void onScrollUp(double amount) {
				Log.d(TAG, "onScrollUp(" + amount + ")");
				scroll(ButtonCodes.GAMEPAD_LEFT_ANALOG_TRIGGER, amount);
			}
			public void onSelect() {
				try {
					mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_SELECT, false, true, false, (short)0, (byte)0);
				} catch (IOException e) { }
			}
			public void onScrollDown() {
				Log.d(TAG, "onScrollDown()");
				try {
					mEventClientManager.sendButton("KB", ButtonCodes.KEYBOARD_PAGEDOWN, false, true, true, (short)0, (byte)0);
				} catch (IOException e) { }
			}
			public void onScrollUp() {
				Log.d(TAG, "onScrollUp()");
				try {
					mEventClientManager.sendButton("KB", ButtonCodes.KEYBOARD_PAGEUP, false, true, true, (short)0, (byte)0);
				} catch (IOException e) { }
			}
			public void onBack() {
				try {
					mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_BACK, false, true, true, (short)0, (byte)0);
				} catch (IOException e) { }
			}
			public void onInfo() {
				try {
					mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_INFO, false, true, true, (short)0, (byte)0);
				} catch (IOException e) { }
			}
			public void onMenu() {
				try {
					mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_MENU, false, true, true, (short)0, (byte)0);
				} catch (IOException e) { }
			}
			public void onTitle() {
				try {
					mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_TITLE, false, true, true, (short)0, (byte)0);
				} catch (IOException e) { }
			}
		};
		return listener;
	}
	
	private static class GestureThread extends Thread {
		public static final int ACTION_UP = 1;
		public static final int ACTION_RIGHT = 2;
		public static final int ACTION_DOWN = 3;
		public static final int ACTION_LEFT = 4;
		private final IEventClientManager mEventClient;
		private boolean mQuit = false;
		private int mLevel = 0;
		private int[] mSpeed = { 0, 800, 400, 200, 100, 50, 0 };
		private int mAction = 0;
		public GestureThread(IEventClientManager eventClient) {
			super("RemoteController.GestureThread");
			mEventClient = eventClient;
		}
		@Override
		public void run() {
			Log.i("GestureThread", "STARTING...");
			while (!mQuit) {
				try {
					switch (mAction) {
						case ACTION_UP:
							mEventClient.sendButton("R1", ButtonCodes.REMOTE_UP, false, true, true, (short)0, (byte)0);
							break;
						case ACTION_RIGHT:
							mEventClient.sendButton("R1", ButtonCodes.REMOTE_RIGHT, false, true, true, (short)0, (byte)0);
							break;
						case ACTION_DOWN:
							mEventClient.sendButton("R1", ButtonCodes.REMOTE_DOWN, false, true, true, (short)0, (byte)0);
							break;
						case ACTION_LEFT:
							mEventClient.sendButton("R1", ButtonCodes.REMOTE_LEFT, false, true, true, (short)0, (byte)0);
							break;
					}
//					Log.i("GestureThread", "action: " + mAction);
					Thread.sleep(mSpeed[Math.abs(mLevel)]);
				} catch (InterruptedException e) {
					mQuit = true;
				} catch (IOException e1) {
					mQuit = true;
				}

			}
		}
		public synchronized void setLevel(int level, int action) {
			mLevel = level;
			mAction = action;
			if (!isAlive()) {
				start();
			}
		}
		public synchronized void quit() {
			Log.i("GestureThread", "QUITTING.");
			mQuit = true;
		}
	}
	
	public void showVolume() {
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
					return onDirectionalPadDown(keyCode);
				case KeyEvent.KEYCODE_DPAD_UP:
					return onDirectionalPadDown(keyCode);
				case KeyEvent.KEYCODE_DPAD_LEFT:
					return onDirectionalPadDown(keyCode);
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					return onDirectionalPadDown(keyCode);
				case KeyEvent.KEYCODE_DPAD_CENTER:
					return onDirectionalPadDown(keyCode);
				default: 
					return false;
			}
		} catch (IOException e) {
			return false;
		}
	}
	
	private boolean onDirectionalPadDown(int keyCode){
			long newstamp = System.currentTimeMillis();
			if (newstamp - mTimestamp > DPAD_DOWN_MIN_DELTA_TIME){
				mTimestamp = newstamp;
				try{
					switch (keyCode) {
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
			return true;
	} 
	
	public boolean onTrackballEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN)
			return keyboardAction(ButtonCodes.KEYBOARD_ENTER);
		else{
			// check when the last trackball move happened to avoid too speedy selections
			long newstamp = System.currentTimeMillis();
			if (newstamp - mTimestamp > MOTION_EVENT_MIN_DELTA_TIME){
				mTimestamp = newstamp;
				if (Math.abs(event.getX()) > MOTION_EVENT_MIN_DELTA_POSITION) {
					return keyboardAction(event.getX() < 0 ? ButtonCodes.KEYBOARD_LEFT : ButtonCodes.KEYBOARD_RIGHT);
				} else if (Math.abs(event.getY()) > MOTION_EVENT_MIN_DELTA_POSITION){
					return keyboardAction(event.getY() < 0 ? ButtonCodes.KEYBOARD_UP : ButtonCodes.KEYBOARD_DOWN);
				}
			}
		}
		return true;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_NOW_PLAYING, 0, "Now playing").setIcon(R.drawable.menu_nowplaying);
		menu.add(0, MENU_SWITCH_GESTURE, 0, "Gesture mode").setIcon(R.drawable.menu_gesture_mode);
		menu.add(0, MENU_XBMC_EXIT, 0, "Exit XBMC").setIcon(R.drawable.menu_xbmc_exit);
		menu.add(0, MENU_XBMC_S, 0, "Press \"S\"").setIcon(R.drawable.menu_xbmc_s);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			Intent intent = null;
			switch (item.getItemId()) {
				case MENU_NOW_PLAYING:
					intent = new Intent(mActivity, NowPlayingActivity.class);
					break;
				case MENU_SWITCH_GESTURE:
					intent = new Intent(mActivity, GestureRemoteActivity.class);
					intent.addFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
					break;
				case MENU_XBMC_EXIT:
					mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_POWER, false, true, true, (short)0, (byte)0);
					break;
				case MENU_XBMC_S:
					mEventClientManager.sendButton("KB", "S", false, true, true, (short)0, (byte)0);
					break;
			}
			if (intent != null) {
				intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				mActivity.startActivity(intent);
				return true;
			}
		} catch (IOException e) {
			return false;
		}
		return true;
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
					Log.d(TAG, "onTouch - ACTION_DOWN");
					if (mDoVibrate) {
						mVibrator.vibrate(VIBRATION_LENGTH);
					}
					mEventClientManager.sendButton("R1", mAction, !prefs.getBoolean("setting_send_repeats", false), true, true, (short)0, (byte)0);									
					
					if (prefs.getBoolean("setting_send_repeats", false) && !prefs.getBoolean("setting_send_single_click", false)) {
															
						if (tmrKeyPress != null) {
							tmrKeyPress.cancel();						
						}
						
						int RepeatDelay = Integer.parseInt(prefs.getString("setting_repeat_rate", "250"));
						
						tmrKeyPress = new Timer();
						tmrKeyPress.schedule(new KeyPressTask(mAction), RepeatDelay, RepeatDelay);					
					}
					
					
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					Log.d(TAG, "onTouch - ACTION_UP");
					v.playSoundEffect(AudioManager.FX_KEY_CLICK);
					mEventClientManager.sendButton("R1", mAction, false, false, true, (short)0, (byte)0);
					
					if (tmrKeyPress != null) {
						tmrKeyPress.cancel();						
					}					
				}
			} catch (IOException e) {
				return false;
			}
			return false;
		}			
	}
	
	private class KeyPressTask extends TimerTask {
		
		private String mKeyPressAction = "";
		
		public KeyPressTask(String mAction) {
			mKeyPressAction = mAction;
		}

		public void run() {
			try {
				if (mKeyPressAction.length() > 0){
					mEventClientManager.sendButton("R1", mKeyPressAction, false, true, true, (short)0, (byte)0);
				}				
			} catch (IOException e) {
				return;
			}
		}
	}
	
	public void onActivityPause() {
		mEventClientManager.setController(null);
		mInfoManager.setController(null);
		if (mGestureThread != null) {
			mGestureThread.quit();
		}
		super.onActivityPause();
	}

	public void onActivityResume(Activity activity) {
		super.onActivityResume(activity);
		mHandler = new Handler();
		mEventClientManager.setController(this);
		mInfoManager.setController(this);
	}
}
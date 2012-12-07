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
import org.xbmc.android.util.KeyTracker;
import org.xbmc.android.util.KeyTracker.Stage;
import org.xbmc.android.util.OnLongPressBackKeyTracker;
import org.xbmc.api.type.ThumbSize;
import org.xbmc.eventclient.ButtonCodes;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ViewFlipper;

/**
 * Activity for remote control. At the moment that's the good ol' Xbox remote
 * control, more to come...
 * 
 * @author Team XBMC
 */
public class RemoteActivity extends Activity {

	private final static String TAG = "RemoteActivity";

	private ConfigurationManager mConfigurationManager;
	private RemoteController mRemoteController;

	private KeyTracker mKeyTracker;

	private ViewFlipper mViewFlipper;

	private View mRemoteView, mGestureView, mMousePadView;

	private float mOldTouchValue;

	public RemoteActivity() {
		if(Integer.parseInt(VERSION.SDK) < 5) {
			mKeyTracker = new KeyTracker(new OnLongPressBackKeyTracker() {
	
				@Override
				public void onLongPressBack(int keyCode, KeyEvent event, Stage stage, int duration) {
					onKeyLongPress(keyCode, event);
				}
	
				@Override
				public void onShortPressBack(int keyCode, KeyEvent event, Stage stage, int duration) {
					RemoteActivity.super.onKeyDown(keyCode, event);
				}
			});
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Display d = getWindowManager().getDefaultDisplay();
		// set display size
		ThumbSize.setScreenSize(d.getWidth(), d.getHeight());	
		final int w = d.getWidth();
		final int h = d.getHeight();
		final double ar = w > h ? (double) w / (double) h : (double) h / (double) w;
		if (ar > 1.6) {
			Log.i(TAG, "AR = " + ar + ", using extended layout.");
			setContentView(R.layout.remote_xbox_extended);
		} else {
			Log.i(TAG, "AR = " + ar + ", normal layout.");
			setContentView(R.layout.remote_xbox);
		}
		

//		mViewFlipper = (ViewFlipper) findViewById(R.id.remote_flipper);

		if (mViewFlipper != null) {
			mRemoteView = mViewFlipper.getChildAt(0);
			mMousePadView = mViewFlipper.getChildAt(1);
			mGestureView = mViewFlipper.getChildAt(2);
			mViewFlipper.setDisplayedChild(0); // mRemoteView
		}

		// mRemoteView.setVisibility(View.VISIBLE);
		// mMousePadView.setVisibility(View.VISIBLE);
		// mGestureView.setVisibility(View.VISIBLE);

		// remove nasty top fading edge
		FrameLayout topFrame = (FrameLayout) findViewById(android.R.id.content);
		topFrame.setForeground(null);
		mRemoteController = new RemoteController(getApplicationContext());

		mConfigurationManager = ConfigurationManager.getInstance(this);
		// mConfigurationManager.initKeyguard(true);


		setupButtons();
	}

	@Override
	public Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		return mRemoteController.onCreateDialog(id, this);
	}

	@Override
	public void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		mRemoteController.onPrepareDialog(id, dialog);
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		return mRemoteController.onTrackballEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean handled = (mKeyTracker != null)?mKeyTracker.doKeyDown(keyCode, event):false;
		return handled || mRemoteController.onKeyDown(keyCode, event)
				|| super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		getSharedPreferences("global", Context.MODE_PRIVATE).edit().putInt(RemoteController.LAST_REMOTE_PREFNAME, RemoteController.LAST_REMOTE_BUTTON).commit();
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
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnDisplay),ButtonCodes.REMOTE_DISPLAY);

		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnVideo), ButtonCodes.REMOTE_MY_VIDEOS);
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnMusic), ButtonCodes.REMOTE_MY_MUSIC);
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnImages), ButtonCodes.REMOTE_MY_PICTURES);
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnTv), ButtonCodes.REMOTE_MY_TV);

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

		// title
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnTitle), ButtonCodes.REMOTE_TITLE);
		// up
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnUp), ButtonCodes.REMOTE_UP);
		// info
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnInfo), ButtonCodes.REMOTE_INFO);

		// left
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnLeft), ButtonCodes.REMOTE_LEFT);
		// select
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnSelect), ButtonCodes.REMOTE_SELECT);
		// right
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnRight), ButtonCodes.REMOTE_RIGHT);

		// menu
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnMenu), ButtonCodes.REMOTE_MENU);
		// down
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnDown), ButtonCodes.REMOTE_DOWN);
		// back
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnBack), ButtonCodes.REMOTE_BACK);

		// videos
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnVideo), ButtonCodes.REMOTE_MY_VIDEOS);
		// music
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnMusic), ButtonCodes.REMOTE_MY_MUSIC);
		// pictures
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnImages), ButtonCodes.REMOTE_MY_PICTURES);
		// tv
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnTv), ButtonCodes.REMOTE_MY_TV);
		// settings
		mRemoteController.setupButton(findViewById(R.id.RemoteXboxImgBtnPower), ButtonCodes.REMOTE_POWER);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return mRemoteController.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return mRemoteController.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean handled = (mKeyTracker != null)?mKeyTracker.doKeyUp(keyCode, event):false;
		return handled || super.onKeyUp(keyCode, event);
	}

	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		Intent intent = new Intent(RemoteActivity.this, HomeActivity.class);
		intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		return true;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent touchEvent) {
		// ignore all that on hdpi displays
		if (mViewFlipper == null) {
			return false;
		}
		
		// determine the current view and
		// who is to the right and to the left.
		final View currentView = mViewFlipper.getCurrentView();
		final View leftView, rightView;

		if (currentView == mRemoteView) {
			Log.d("current layout: ", "remote");
			leftView = mGestureView;
			rightView = mMousePadView;
		} else if (currentView == mMousePadView) {
			Log.d("current layout: ", "mousepad");
			leftView = mRemoteView;
			rightView = mGestureView;
		} else if (currentView == mGestureView) {
			Log.d("current layout: ", "gesture");
			leftView = mMousePadView;
			rightView = mRemoteView;
		}
		// This shouldn't happen unless someone adds another view
		// inside the ViewFlipper
		else {
			leftView = null;
			rightView = null;
		}

		switch (touchEvent.getAction()) {
			case MotionEvent.ACTION_DOWN: 
				// freezy: the mousepad seems to always flicker
				// at the start of the move action (i.e. action_down)
				// so i tried this but it doesn't seem to work.
				// thats the only thing i can think of that keeps this
				// feature from being 100%
				/*
				 * if(currentView != mMousePadView) {
				 * mMousePadView.setVisibility(View.INVISIBLE); }
				 */
				mOldTouchValue = touchEvent.getX();
			break;
		
			case MotionEvent.ACTION_UP: 
				float currentX = touchEvent.getX();
	
				if (mOldTouchValue < currentX) {
					mViewFlipper.setInAnimation(AnimationHelper.inFromLeftAnimation());
					mViewFlipper.setOutAnimation(AnimationHelper.outToRightAnimation());
					mViewFlipper.showPrevious();
				}
				if (mOldTouchValue > currentX) {
					mViewFlipper.setInAnimation(AnimationHelper.inFromRightAnimation());
					mViewFlipper.setOutAnimation(AnimationHelper.outToLeftAnimation());
					mViewFlipper.showNext();
				}
	
			break;
		
			case MotionEvent.ACTION_MOVE: 
				leftView.setVisibility(View.VISIBLE);
				rightView.setVisibility(View.VISIBLE);
	
				Log.d("current layout:", "left: "
						+ Integer.toString(currentView.getLeft()) + " right: "
						+ Integer.toString(currentView.getRight()));
				Log.d("previous layout:", "left: "
						+ Integer.toString(leftView.getLeft()) + " right: "
						+ Integer.toString(leftView.getRight()));
				Log.d("next layout:", "left: "
						+ Integer.toString(rightView.getLeft()) + " right: "
						+ Integer.toString(rightView.getRight()));
	
				// move the current view to the left or right.
				currentView.layout((int) (touchEvent.getX() - mOldTouchValue),
						currentView.getTop(),
						(int) (touchEvent.getX() - mOldTouchValue) + 320,
						currentView.getBottom());
	
				// place this view just left of the currentView
				leftView.layout(currentView.getLeft() - 320, leftView.getTop(),
						currentView.getLeft(), leftView.getBottom());
	
				// place this view just right of the currentView
				rightView.layout(currentView.getRight(), rightView.getTop(),
						currentView.getRight() + 320, rightView.getBottom());
			break;
		
		}
		return false;
	}

	public static class AnimationHelper {
		public static Animation inFromRightAnimation() {
			Animation inFromRight = new TranslateAnimation(
					Animation.RELATIVE_TO_PARENT, +1.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f);
			inFromRight.setDuration(350);
			inFromRight.setInterpolator(new AccelerateInterpolator());
			return inFromRight;
		}

		public static Animation outToLeftAnimation() {
			Animation outtoLeft = new TranslateAnimation(
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, -1.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f);
			outtoLeft.setDuration(350);
			outtoLeft.setInterpolator(new AccelerateInterpolator());
			return outtoLeft;
		}

		// for the next movement
		public static Animation inFromLeftAnimation() {
			Animation inFromLeft = new TranslateAnimation(
					Animation.RELATIVE_TO_PARENT, -1.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f);
			inFromLeft.setDuration(350);
			inFromLeft.setInterpolator(new AccelerateInterpolator());
			return inFromLeft;
		}

		public static Animation outToRightAnimation() {
			Animation outtoRight = new TranslateAnimation(
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, +1.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f);
			outtoRight.setDuration(350);
			outtoRight.setInterpolator(new AccelerateInterpolator());
			return outtoRight;
		}
	}
}

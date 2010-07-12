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
import org.xbmc.android.remote.presentation.controller.HomeController;
import org.xbmc.android.remote.presentation.controller.HomeController.ProgressThread;
import org.xbmc.android.util.ImportUtilities;
import org.xbmc.android.util.OnLongPressBackKeyTracker;
import org.xbmc.android.util.KeyTracker.Stage;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.type.ThumbSize;
import org.xbmc.eventclient.ButtonCodes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

public class HomeActivity extends Activity {
	
	private static final String TAG = "HomeActivity";

	private static final int MENU_ABOUT = 1;
	private static final int MENU_SETTINGS = 2;
	private static final int MENU_EXIT = 3;
	private static final int MENU_SWITCH_XBMC = 5;
//	private static final int MENU_INPUT_TEXT = 6;
	public static final int MENU_COVER_DOWNLOAD = 4;
	public static final int MENU_COVER_DOWNLOAD_MUSIC = 41;
	public static final int MENU_COVER_DOWNLOAD_MOVIES = 42;
	public static final int MENU_COVER_DOWNLOAD_ACTORS = 43;
	public static final int MENU_COVER_PURGE_CACHE = 44;
	public static final int MENU_COVER_DOWNLOAD_TVSHOWS = 45;
	public static final int MENU_COVER_DOWNLOAD_TVSEASONS = 46;
	
	private ConfigurationManager mConfigurationManager;
	private HomeController mHomeController;
	
	private IEventClientManager mEventClientManager;

	private ProgressThread mProgressThread;
    private ProgressDialog mProgressDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		
		// set display size
		final Display display = getWindowManager().getDefaultDisplay(); 
		ThumbSize.setScreenSize(display.getWidth(), display.getHeight());
		
		final Button versionButton = (Button)findViewById(R.id.home_version_button);
		final GridView menuGrid = (GridView)findViewById(R.id.HomeItemGridView);
		mHomeController = new HomeController(this, menuGrid);
		mHomeController.setupVersionHandler(versionButton, menuGrid);
		
		mEventClientManager = ManagerFactory.getEventClientManager(mHomeController);
		mConfigurationManager = ConfigurationManager.getInstance(this);
		
		versionButton.setText("Connecting...");
		versionButton.setOnClickListener(mHomeController.getOnHostChangeListener());
		
		((Button)findViewById(R.id.home_about_button)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(HomeActivity.this, AboutActivity.class));
			}
		});
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		menu.add(0, MENU_SWITCH_XBMC, 0, "Switch XBMC").setIcon(R.drawable.menu_switch);
		SubMenu downloadMenu = menu.addSubMenu(0, MENU_COVER_DOWNLOAD, 0, "Download Covers").setIcon(R.drawable.menu_download);
		menu.add(0, MENU_ABOUT, 0, "About").setIcon(R.drawable.menu_about);
		menu.add(0, MENU_SETTINGS, 0, "Settings").setIcon(R.drawable.menu_settings);
		menu.add(0, MENU_EXIT, 0, "Exit").setIcon(R.drawable.menu_exit);
//		menu.add(0, MENU_INPUT_TEXT, 0, "Send Text");
		
		downloadMenu.add(2, MENU_COVER_DOWNLOAD_MOVIES, 0, "Movie Posters");
		downloadMenu.add(2, MENU_COVER_DOWNLOAD_MUSIC, 0, "Album Covers");
		downloadMenu.add(2, MENU_COVER_DOWNLOAD_TVSHOWS, 0, "TV Show Banners");
		downloadMenu.add(2, MENU_COVER_DOWNLOAD_TVSEASONS, 0, "TV Season Posters");
		downloadMenu.add(2, MENU_COVER_DOWNLOAD_ACTORS, 0, "Actor Shots");
		downloadMenu.add(2, MENU_COVER_PURGE_CACHE, 0, "Clear Cache");
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ABOUT:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		case MENU_SWITCH_XBMC:
			mHomeController.openHostChanger();
			return true;
		case MENU_SETTINGS:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case MENU_EXIT:
			Log.i(TAG, "Exiting XBMC Remote.");
			NowPlayingNotificationManager.getInstance(getBaseContext()).removeNotification();
			android.os.Process.killProcess(android.os.Process.myPid());
//			System.exit(0);
			return true;
//		case MENU_INPUT_TEXT:
//			startActivity(new Intent(this, KeyboardAndMouseActivity.class));
//			return true;
		case MENU_COVER_DOWNLOAD_MOVIES:
		case MENU_COVER_DOWNLOAD_MUSIC:
		case MENU_COVER_DOWNLOAD_ACTORS:
		case MENU_COVER_DOWNLOAD_TVSHOWS:
		case MENU_COVER_DOWNLOAD_TVSEASONS:
			showDialog(item.getItemId());
			return true;
		case MENU_COVER_PURGE_CACHE:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("All downloaded covers, thumbs and posters will be deleted. Are you really sure you want to do this?");
			builder.setCancelable(false);
			builder.setPositiveButton("Absolutely.", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					ImportUtilities.purgeCache();
					Toast.makeText(HomeActivity.this, "Cache purged.", Toast.LENGTH_SHORT).show();
				}
			});
			builder.setNegativeButton("Please, no!", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			builder.create().show();
			return true;
		}
		return false;
	}
	
	@Override
	public Dialog onCreateDialog(int id) {
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage("");
		mProgressDialog.setProgress(0);
		mProgressDialog.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				mProgressThread.cancel();
			}
		});
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		return mProgressDialog;
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		final ProgressDialog d = (ProgressDialog)dialog;
		d.setProgress(0);
		switch (id) {
			case MENU_COVER_DOWNLOAD_MOVIES:
				d.setMessage("Downloading movie posters...");
				mProgressThread = mHomeController.new ProgressThread(mHandler, MENU_COVER_DOWNLOAD_MOVIES, d);
	            break;
			case MENU_COVER_DOWNLOAD_MUSIC:
				d.setMessage("Downloading album covers...");
				mProgressThread = mHomeController.new ProgressThread(mHandler, MENU_COVER_DOWNLOAD_MUSIC, d);
				break;
			case MENU_COVER_DOWNLOAD_ACTORS:
				d.setMessage("Downloading actor thumbs...");
				mProgressThread = mHomeController.new ProgressThread(mHandler, MENU_COVER_DOWNLOAD_ACTORS, d);
				break;
			case MENU_COVER_DOWNLOAD_TVSHOWS:
				d.setMessage("Downloading TV show banners.");
				mProgressThread = mHomeController.new ProgressThread(mHandler, MENU_COVER_DOWNLOAD_TVSHOWS, d);
				break;
			case MENU_COVER_DOWNLOAD_TVSEASONS:
				d.setMessage("Downloading TV season posters.");
				mProgressThread = mHomeController.new ProgressThread(mHandler, MENU_COVER_DOWNLOAD_TVSEASONS, d);
				break;
			default:
				return;
		}
		mProgressThread.start();
	}

	@Override
	public void onResume(){
		super.onResume();
		mHomeController.onActivityResume(this);
        mConfigurationManager.onActivityResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mHomeController.onActivityPause();
		mConfigurationManager.onActivityPause();
		mEventClientManager.setController(null);
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		try {
			switch (keyCode) {
				case KeyEvent.KEYCODE_VOLUME_UP:
					mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_VOLUME_PLUS, false, true, true, (short)0, (byte)0);
					return true;
				case KeyEvent.KEYCODE_VOLUME_DOWN:
					mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_VOLUME_MINUS, false, true, true, (short)0, (byte)0);
					return true;
				case KeyEvent.KEYCODE_BACK:
					if(OnLongPressBackKeyTracker.lastStage == Stage.LONG_REPEAT) {
						return true;
					}
			}
		} catch (IOException e) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if(OnLongPressBackKeyTracker.lastStage == Stage.LONG_REPEAT) {
				OnLongPressBackKeyTracker.lastStage = Stage.SHORT_REPEAT;
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			mHomeController.onHandleMessage(msg, mProgressDialog, mProgressThread);
		}
	};
}
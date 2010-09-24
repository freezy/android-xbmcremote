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
import org.xbmc.android.remote.presentation.controller.ListController;
import org.xbmc.android.util.KeyTracker;
import org.xbmc.android.util.OnLongPressBackKeyTracker;
import org.xbmc.android.util.KeyTracker.Stage;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.eventclient.ButtonCodes;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Build.VERSION;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.FrameLayout;

public abstract class AbsListActivity extends Activity {

	private static final int MENU_NOW_PLAYING = 501;
	private static final int MENU_REMOTE = 502;

	protected ConfigurationManager mConfigurationManager;

	ListController mListController;
	private KeyTracker mKeyTracker;
	
	public AbsListActivity() {
		if(Integer.parseInt(VERSION.SDK) < 5) {
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
	}
	
	protected void setupLists(int layoutResId) {
		setContentView(layoutResId);
		// remove nasty top fading edge
		FrameLayout topFrame = (FrameLayout)findViewById(android.R.id.content);
		topFrame.setForeground(null);
		mListController = (ListController)getLastNonConfigurationInstance();
		if (mListController == null) {
			mListController = (ListController) getIntent().getSerializableExtra(ListController.EXTRA_LIST_CONTROLLER);
			mListController.findTitleView(findViewById(R.id.blanklist_outer_layout));
			mListController.findMessageView(findViewById(R.id.blanklist_outer_layout));
			mListController.onCreate(this, new Handler(), (AbsListView)findViewById(R.id.blanklist_list));
		}
		mConfigurationManager = ConfigurationManager.getInstance(this);
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
//		// TODO Auto-generated method stub
//		return super.onRetainNonConfigurationInstance();
		return mListController;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		mListController.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		mListController.onContextItemSelected(item);
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_NOW_PLAYING, 0, "Now playing").setIcon(R.drawable.menu_nowplaying);
		mListController.onCreateOptionsMenu(menu);
		menu.add(0, MENU_REMOTE, 0, "Remote control").setIcon(R.drawable.menu_remote);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mListController.onOptionsItemSelected(item);
		switch (item.getItemId()) {
			case MENU_REMOTE:
				startActivity(new Intent(this, RemoteActivity.class));
				return true;
			case MENU_NOW_PLAYING:
				startActivity(new Intent(this, NowPlayingActivity.class));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		IEventClientManager client = ManagerFactory.getEventClientManager(mListController);
		try {
			switch (keyCode) {
				case KeyEvent.KEYCODE_VOLUME_UP:
					client.sendButton("R1", ButtonCodes.REMOTE_VOLUME_PLUS, false, true, true, (short) 0, (byte) 0);
					return true;
				case KeyEvent.KEYCODE_VOLUME_DOWN:
					client.sendButton("R1", ButtonCodes.REMOTE_VOLUME_MINUS, false, true, true, (short) 0, (byte) 0);
					return true;
			}
		} catch (IOException e) {
			client.setController(null);
			return false;
		}
		client.setController(null);
		boolean handled =  (mKeyTracker != null)?mKeyTracker.doKeyDown(keyCode, event):false;
		return handled || super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mListController.onActivityResume(this);
		mConfigurationManager.onActivityResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mConfigurationManager.onActivityPause();
		mListController.onActivityPause();
	}
	
	protected void callSuperOnKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
	}
	
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		Intent intent = new Intent(AbsListActivity.this, HomeActivity.class);
		intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		return true;
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean handled = (mKeyTracker != null)?mKeyTracker.doKeyUp(keyCode, event):false;
		return handled || super.onKeyUp(keyCode, event);
	}
}
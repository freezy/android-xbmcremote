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
import org.xbmc.android.remote.presentation.controller.ListController;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.eventclient.ButtonCodes;
import org.xbmc.eventclient.EventClient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.FrameLayout;
import android.widget.ListView;

public class ListActivity extends Activity {

	private static final int MENU_NOW_PLAYING = 501;
	private static final int MENU_REMOTE = 502;

	private ConfigurationManager mConfigurationManager;

	ListController mListController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.blanklist);
		
		// remove nasty top fading edge
		FrameLayout topFrame = (FrameLayout)findViewById(android.R.id.content);
		topFrame.setForeground(null);

		mListController = (ListController) getIntent().getSerializableExtra(ListController.EXTRA_LIST_CONTROLLER);

		mListController.findTitleView(findViewById(R.id.blanklist_outer_layout));
		mListController.findMessageView(findViewById(R.id.blanklist_outer_layout));
		mListController.onCreate(this, (ListView) findViewById(R.id.blanklist_list));

		mConfigurationManager = ConfigurationManager.getInstance(this);
		mConfigurationManager.initKeyguard();
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
		EventClient client = ConnectionManager.getEventClient(this);
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
}
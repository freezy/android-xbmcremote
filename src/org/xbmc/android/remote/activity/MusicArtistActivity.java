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
import org.xbmc.android.remote.guilogic.AlbumListLogic;
import org.xbmc.android.remote.guilogic.SongListLogic;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.android.util.ErrorHandler;
import org.xbmc.android.widget.slidingtabs.SlidingTabActivity;
import org.xbmc.android.widget.slidingtabs.SlidingTabHost;
import org.xbmc.android.widget.slidingtabs.SlidingTabHost.OnTabChangeListener;
import org.xbmc.eventclient.ButtonCodes;
import org.xbmc.eventclient.EventClient;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.FrameLayout;
import android.widget.ListView;

public class MusicArtistActivity extends SlidingTabActivity  {

	private SlidingTabHost mTabHost;
	private AlbumListLogic mAlbumLogic;
	private SongListLogic mSongLogic;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		ErrorHandler.setActivity(this);
		setContentView(R.layout.musicartist);
		
		// remove nasty top fading edge
		FrameLayout topFrame = (FrameLayout)findViewById(android.R.id.content);
		topFrame.setForeground(null);
		
		mTabHost = getTabHost();

		mTabHost.addTab(mTabHost.newTabSpec("musictab1", "Albums", R.drawable.st_album_on, R.drawable.st_album_off).setBigIcon(R.drawable.st_album_over).setContent(R.id.albumlist_outer_layout));
		mTabHost.addTab(mTabHost.newTabSpec("musictab2", "Songs", R.drawable.st_artist_on, R.drawable.st_artist_off).setBigIcon(R.drawable.st_artist_over).setContent(R.id.songlist_outer_layout));
		mTabHost.setCurrentTab(0);
		
		mAlbumLogic = new AlbumListLogic();
		mAlbumLogic.findTitleView(findViewById(R.id.albumlist_outer_layout));
		mAlbumLogic.onCreate(this, (ListView)findViewById(R.id.albumlist_list)); // first tab can be updated now.

		mSongLogic = new SongListLogic();
		mSongLogic.findTitleView(findViewById(R.id.songlist_outer_layout));
		
		mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				if (tabId.equals("musictab1")) {
					mAlbumLogic.onCreate(MusicArtistActivity.this, (ListView)findViewById(R.id.albumlist_list));
				}
				if (tabId.equals("musictab2")) {
					mSongLogic.onCreate(MusicArtistActivity.this, (ListView)findViewById(R.id.songlist_list));
				}
			}
		});
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		switch (mTabHost.getCurrentTab()) {
			case 0:
				mAlbumLogic.onCreateContextMenu(menu, v, menuInfo);
				break;
			case 1:
				mSongLogic.onCreateContextMenu(menu, v, menuInfo);
				break;
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (mTabHost.getCurrentTab()) {
		case 0:
			mAlbumLogic.onContextItemSelected(item);
			break;
		case 1:
			mSongLogic.onContextItemSelected(item);
			break;
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		EventClient client = ConnectionManager.getEventClient(this);	
		try {
			switch (keyCode) {
				case KeyEvent.KEYCODE_VOLUME_UP:
					client.sendButton("R1", ButtonCodes.REMOTE_VOLUME_PLUS, false, true, true, (short)0, (byte)0);
					return true;
				case KeyEvent.KEYCODE_VOLUME_DOWN:
					client.sendButton("R1", ButtonCodes.REMOTE_VOLUME_MINUS, false, true, true, (short)0, (byte)0);
					return true;
			}
		} catch (IOException e) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}
}

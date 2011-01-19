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
import org.xbmc.android.remote.presentation.controller.AlbumListController;
import org.xbmc.android.remote.presentation.controller.ArtistListController;
import org.xbmc.android.remote.presentation.controller.RemoteController;
import org.xbmc.android.remote.presentation.controller.SongListController;
import org.xbmc.android.widget.slidingtabs.SlidingTabActivity;
import org.xbmc.android.widget.slidingtabs.SlidingTabHost;
import org.xbmc.android.widget.slidingtabs.SlidingTabHost.OnTabChangeListener;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.eventclient.ButtonCodes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.FrameLayout;
import android.widget.ListView;

public class MusicGenreActivity extends SlidingTabActivity  {

	private SlidingTabHost mTabHost;
	private ArtistListController mArtistController;
	private AlbumListController mAlbumController;
	private SongListController mSongController;
	
	private static final int MENU_NOW_PLAYING = 201;
	private static final int MENU_REMOTE = 202;
	
	private ConfigurationManager mConfigurationManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.musicgenre);
		
		// remove nasty top fading edge
		FrameLayout topFrame = (FrameLayout)findViewById(android.R.id.content);
		topFrame.setForeground(null);
		
		mTabHost = getTabHost();

		mTabHost.addTab(mTabHost.newTabSpec("genretab_artists", "Artists", R.drawable.st_artist_on, R.drawable.st_artist_off).setBigIcon(R.drawable.st_artist_over).setContent(R.id.artistlist_outer_layout));
		mTabHost.addTab(mTabHost.newTabSpec("genretab_albums", "Albums", R.drawable.st_album_on, R.drawable.st_album_off).setBigIcon(R.drawable.st_album_over).setContent(R.id.albumlist_outer_layout));
		mTabHost.addTab(mTabHost.newTabSpec("genretab_songs", "Songs", R.drawable.st_song_on, R.drawable.st_song_off).setBigIcon(R.drawable.st_song_over).setContent(R.id.songlist_outer_layout));
		mTabHost.setCurrentTab(0);
		
		final Handler handler = new Handler();
		mArtistController = new ArtistListController();
		mArtistController.findTitleView(findViewById(R.id.artistlist_outer_layout));
		mArtistController.findMessageView(findViewById(R.id.artistlist_outer_layout));
		mArtistController.onCreate(this, handler, (ListView)findViewById(R.id.artistlist_list)); // first tab can be updated now.

		mAlbumController = new AlbumListController();
		mAlbumController.findTitleView(findViewById(R.id.albumlist_outer_layout));
		mAlbumController.findMessageView(findViewById(R.id.albumlist_outer_layout));

		mSongController = new SongListController();
		mSongController.findTitleView(findViewById(R.id.songlist_outer_layout));
		mSongController.findMessageView(findViewById(R.id.songlist_outer_layout));
		
		mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				if (tabId.equals("genretab_artists")) {
					mArtistController.onCreate(MusicGenreActivity.this, handler, (ListView)findViewById(R.id.artistlist_list));
				}
				if (tabId.equals("genretab_albums")) {
					mAlbumController.onCreate(MusicGenreActivity.this, handler, (ListView)findViewById(R.id.albumlist_list));
				}
				if (tabId.equals("genretab_songs")) {
					mSongController.onCreate(MusicGenreActivity.this, handler, (ListView)findViewById(R.id.songlist_list));
				}
			}
		});
		
		mConfigurationManager = ConfigurationManager.getInstance(this);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(0, MENU_NOW_PLAYING, 0, "Now playing").setIcon(R.drawable.menu_nowplaying);
		switch (mTabHost.getCurrentTab()) {
			case 0:
				mArtistController.onCreateOptionsMenu(menu);
				break;
			case 1:
				mAlbumController.onCreateOptionsMenu(menu);
				break;
			case 2:
				mSongController.onCreateOptionsMenu(menu);
				break;
		}
		menu.add(0, MENU_REMOTE, 0, "Remote control").setIcon(R.drawable.menu_remote);
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		// first, process individual menu events
		switch (mTabHost.getCurrentTab()) {
		case 0:
			mArtistController.onOptionsItemSelected(item);
			break;
		case 1:
			mAlbumController.onOptionsItemSelected(item);
			break;
		case 2:
			mSongController.onOptionsItemSelected(item);
			break;
		}
		
		// then the generic ones.
		switch (item.getItemId()) {
		case MENU_REMOTE:
			final Intent intent;
			if (getSharedPreferences("global", Context.MODE_PRIVATE).getInt(RemoteController.LAST_REMOTE_PREFNAME, -1) == RemoteController.LAST_REMOTE_GESTURE) {
				intent = new Intent(this, GestureRemoteActivity.class);
			} else {
				intent = new Intent(this, RemoteActivity.class);
			}
			intent.addFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(intent);
			return true;
		case MENU_NOW_PLAYING:
			startActivity(new Intent(this,  NowPlayingActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		switch (mTabHost.getCurrentTab()) {
			case 0:
				mArtistController.onCreateContextMenu(menu, v, menuInfo);
				break;
			case 1:
				mAlbumController.onCreateContextMenu(menu, v, menuInfo);
				break;
			case 2:
				mSongController.onCreateContextMenu(menu, v, menuInfo);
				break;
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (mTabHost.getCurrentTab()) {
		case 0:
			mArtistController.onContextItemSelected(item);
			break;
		case 1:
			mAlbumController.onContextItemSelected(item);
			break;
		case 2:
			mSongController.onContextItemSelected(item);
			break;
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		IEventClientManager client = ManagerFactory.getEventClientManager(mArtistController);
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
			client.setController(null);
			return false;
		}
		client.setController(null);
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mArtistController.onActivityResume(this);
		mAlbumController.onActivityResume(this);
		mSongController.onActivityResume(this);
		mConfigurationManager.onActivityResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mArtistController.onActivityPause();
		mAlbumController.onActivityPause();
		mSongController.onActivityPause();
		mConfigurationManager.onActivityPause();
	}
}

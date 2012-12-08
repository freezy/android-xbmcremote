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
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.controller.AlbumListController;
import org.xbmc.android.remote.presentation.controller.ArtistListController;
import org.xbmc.android.remote.presentation.controller.FileListController;
import org.xbmc.android.remote.presentation.controller.MusicGenreListController;
import org.xbmc.android.remote.presentation.controller.RemoteController;
import org.xbmc.android.widget.slidingtabs.SlidingTabActivity;
import org.xbmc.android.widget.slidingtabs.SlidingTabHost;
import org.xbmc.android.widget.slidingtabs.SlidingTabHost.OnTabChangeListener;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.eventclient.ButtonCodes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ListView;

public class MusicLibraryActivity extends SlidingTabActivity implements ViewTreeObserver.OnGlobalLayoutListener {

	private SlidingTabHost mTabHost;
	private AlbumListController mAlbumController;
	private ArtistListController mArtistController;
	private MusicGenreListController mGenreController;
	private AlbumListController mCompilationsController;
	private FileListController mFileController;
	
	private static final int MENU_NOW_PLAYING = 301;
	private static final int MENU_UPDATE_LIBRARY = 302;
	private static final int MENU_REMOTE = 303;
	
	private static final String PREF_REMEMBER_TAB = "setting_remember_last_tab";
	private static final String LAST_MUSIC_TAB_ID = "last_music_tab_id";
	
    private ConfigurationManager mConfigurationManager;
    private Handler mHandler;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.musiclibrary);
		
		// remove nasty top fading edge
		FrameLayout topFrame = (FrameLayout)findViewById(android.R.id.content);
		topFrame.setForeground(null);
		
		mTabHost = getTabHost();
		
		// add the tabs
		mTabHost.addTab(mTabHost.newTabSpec("tab_albums", "Albums", R.drawable.st_album_on, R.drawable.st_album_off).setBigIcon(R.drawable.st_album_over).setContent(R.id.albumlist_outer_layout));
		mTabHost.addTab(mTabHost.newTabSpec("tab_artists", "Artists", R.drawable.st_artist_on, R.drawable.st_artist_off).setBigIcon(R.drawable.st_artist_over).setContent(R.id.artists_outer_layout));
		mTabHost.addTab(mTabHost.newTabSpec("tab_genres", "Genres", R.drawable.st_genre_on, R.drawable.st_genre_off).setBigIcon(R.drawable.st_genre_over).setContent(R.id.genres_outer_layout));
		mTabHost.addTab(mTabHost.newTabSpec("tab_compilations", "Compilations", R.drawable.st_va_on, R.drawable.st_va_off).setBigIcon(R.drawable.st_va_over).setContent(R.id.compilations_outer_layout));
		mTabHost.addTab(mTabHost.newTabSpec("tab_files", "File Mode", R.drawable.st_filemode_on, R.drawable.st_filemode_off).setBigIcon(R.drawable.st_filemode_over).setContent(R.id.filelist_outer_layout));
		
		mTabHost.getViewTreeObserver().addOnGlobalLayoutListener(this);

		// assign the gui logic to each tab
		mHandler = new Handler();
		mAlbumController = new AlbumListController();
		mAlbumController.findTitleView(findViewById(R.id.albumlist_outer_layout));
		mAlbumController.findMessageView(findViewById(R.id.albumlist_outer_layout));
//		mAlbumController.setGrid((GridView)findViewById(R.id.albumlist_grid));

		mFileController = new FileListController();
		mFileController.findTitleView(findViewById(R.id.filelist_outer_layout));
		mFileController.findMessageView(findViewById(R.id.filelist_outer_layout));
		
		mArtistController = new ArtistListController();
		mArtistController.findTitleView(findViewById(R.id.artists_outer_layout));
		mArtistController.findMessageView(findViewById(R.id.artists_outer_layout));

		mGenreController = new MusicGenreListController();
		mGenreController.findTitleView(findViewById(R.id.genres_outer_layout));
		mGenreController.findMessageView(findViewById(R.id.genres_outer_layout));

		mCompilationsController = new AlbumListController();
		mCompilationsController.findTitleView(findViewById(R.id.compilations_outer_layout));
		mCompilationsController.findMessageView(findViewById(R.id.compilations_outer_layout));
		mCompilationsController.setCompilationsOnly(true);
		
		mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				initTab(tabId);
				
				final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				if (prefs.getBoolean(PREF_REMEMBER_TAB, false)) {
					getSharedPreferences("global", Context.MODE_PRIVATE).edit().putString(LAST_MUSIC_TAB_ID, tabId).commit();
				}
			}
		});
		
		mConfigurationManager = ConfigurationManager.getInstance(this);
	}
	
	public void onGlobalLayout() {
        mTabHost.getViewTreeObserver().removeGlobalOnLayoutListener(this);
		
		String lastTab = "tab_albums";
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		if (prefs.getBoolean(PREF_REMEMBER_TAB, false)) {
			lastTab = (getSharedPreferences("global", Context.MODE_PRIVATE).getString(LAST_MUSIC_TAB_ID, "tab_albums"));
			mTabHost.selectTabByTag(lastTab);
		}
		
		initTab(lastTab);
	}
	
	private void initTab(String tabId) {
		if (tabId.equals("tab_albums")) {
			mAlbumController.onCreate(MusicLibraryActivity.this, mHandler, (ListView)findViewById(R.id.albumlist_list));
		}
		if (tabId.equals("tab_files")) {
			mFileController.onCreate(MusicLibraryActivity.this, mHandler, (ListView)findViewById(R.id.filelist_list));
		}
		if (tabId.equals("tab_artists")) {
			mArtistController.onCreate(MusicLibraryActivity.this, mHandler, (ListView)findViewById(R.id.artists_list));
		}
		if (tabId.equals("tab_genres")) {
			mGenreController.onCreate(MusicLibraryActivity.this, mHandler, (ListView)findViewById(R.id.genres_list));
		}
		if (tabId.equals("tab_compilations")) {
			mCompilationsController.onCreate(MusicLibraryActivity.this, mHandler, (ListView)findViewById(R.id.compilations_list));
		}
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(0, MENU_NOW_PLAYING, 0, "Now playing").setIcon(R.drawable.menu_nowplaying);
		switch (mTabHost.getCurrentTab()) {
			case 0:
				mAlbumController.onCreateOptionsMenu(menu);
				break;
			case 1:
				mArtistController.onCreateOptionsMenu(menu);
				break;
			case 2:
				mGenreController.onCreateOptionsMenu(menu);
				break;
			case 3:
				mCompilationsController.onCreateOptionsMenu(menu);
				break;
			case 4:
				mFileController.onCreateOptionsMenu(menu);
				break;
		}
		menu.add(0, MENU_UPDATE_LIBRARY, 0, "Update Library").setIcon(R.drawable.menu_refresh);
		menu.add(0, MENU_REMOTE, 0, "Remote control").setIcon(R.drawable.menu_remote);
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		// first, process individual menu events
		switch (mTabHost.getCurrentTab()) {
		case 0:
			mAlbumController.onOptionsItemSelected(item);
			break;
		case 1:
			mArtistController.onOptionsItemSelected(item);
			break;
		case 2:
			mGenreController.onOptionsItemSelected(item);
			break;
		case 3:
			mCompilationsController.onOptionsItemSelected(item);
			break;
		case 4:
			mFileController.onOptionsItemSelected(item);
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
		case MENU_UPDATE_LIBRARY:
			mAlbumController.updateLibrary();
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
				mAlbumController.onCreateContextMenu(menu, v, menuInfo);
				break;
			case 1:
				mArtistController.onCreateContextMenu(menu, v, menuInfo);
				break;
			case 2:
				mGenreController.onCreateContextMenu(menu, v, menuInfo);
				break;
			case 3:
				mCompilationsController.onCreateContextMenu(menu, v, menuInfo);
				break;
			case 4:
				mFileController.onCreateContextMenu(menu, v, menuInfo);
				break;
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (mTabHost.getCurrentTab()) {
		case 0:
			mAlbumController.onContextItemSelected(item);
			break;
		case 1:
			mArtistController.onContextItemSelected(item);
			break;
		case 2:
			mGenreController.onContextItemSelected(item);
			break;
		case 3:
			mCompilationsController.onContextItemSelected(item);
			break;
		case 4:
			mFileController.onContextItemSelected(item);
			break;
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		IEventClientManager client = ManagerFactory.getEventClientManager(mAlbumController);
		switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_UP:
				client.sendButton("R1", ButtonCodes.REMOTE_VOLUME_PLUS, false, true, true, (short)0, (byte)0);
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				client.sendButton("R1", ButtonCodes.REMOTE_VOLUME_MINUS, false, true, true, (short)0, (byte)0);
				return true;
		}
		client.setController(null);
		return super.onKeyDown(keyCode, event);
	}
	

	@Override
	protected void onResume() {
		super.onResume();
		mAlbumController.onActivityResume(this);
		mArtistController.onActivityResume(this);
		mGenreController.onActivityResume(this);
		mCompilationsController.onActivityResume(this);
		mFileController.onActivityResume(this);
		mConfigurationManager.onActivityResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mAlbumController.onActivityPause();
		mArtistController.onActivityPause();
		mGenreController.onActivityPause();
		mCompilationsController.onActivityPause();
		mFileController.onActivityPause();
		mConfigurationManager.onActivityPause();
	}
}

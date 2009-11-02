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
import org.xbmc.android.remote.guilogic.ArtistListLogic;
import org.xbmc.android.remote.guilogic.FileListLogic;
import org.xbmc.android.remote.guilogic.GenreListLogic;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.android.util.ErrorHandler;
import org.xbmc.android.widget.slidingtabs.SlidingTabActivity;
import org.xbmc.android.widget.slidingtabs.SlidingTabHost;
import org.xbmc.android.widget.slidingtabs.SlidingTabHost.OnTabChangeListener;
import org.xbmc.eventclient.ButtonCodes;
import org.xbmc.eventclient.EventClient;

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

public class MusicLibraryActivity extends SlidingTabActivity  {

	private SlidingTabHost mTabHost;
	private AlbumListLogic mAlbumLogic;
	private ArtistListLogic mArtistLogic;
	private GenreListLogic mGenreLogic;
	private AlbumListLogic mCompilationsLogic;
	private FileListLogic mFileLogic;
	
	private static final int MENU_NOW_PLAYING = 101;
	private static final int MENU_REMOTE = 102;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ErrorHandler.setActivity(this);
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
		mTabHost.setCurrentTab(0);

		// assign the gui logic to each tab
		mAlbumLogic = new AlbumListLogic();
		mAlbumLogic.findTitleView(findViewById(R.id.albumlist_outer_layout));
		mAlbumLogic.onCreate(this, (ListView)findViewById(R.id.albumlist_list)); // first tab can be updated now.

		mFileLogic = new FileListLogic();
		mFileLogic.findTitleView(findViewById(R.id.filelist_outer_layout));
		
		mArtistLogic = new ArtistListLogic();
		mArtistLogic.findTitleView(findViewById(R.id.artists_outer_layout));

		mGenreLogic = new GenreListLogic();
		mGenreLogic.findTitleView(findViewById(R.id.genres_outer_layout));

		mCompilationsLogic = new AlbumListLogic();
		mCompilationsLogic.findTitleView(findViewById(R.id.compilations_outer_layout));
		mCompilationsLogic.setCompilationsOnly(true);

		mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				
				if (tabId.equals("tab_albums")) {
					mAlbumLogic.onCreate(MusicLibraryActivity.this, (ListView)findViewById(R.id.albumlist_list));
				}
				if (tabId.equals("tab_files")) {
					mFileLogic.onCreate(MusicLibraryActivity.this, (ListView)findViewById(R.id.filelist_list));
				}
				if (tabId.equals("tab_artists")) {
					mArtistLogic.onCreate(MusicLibraryActivity.this, (ListView)findViewById(R.id.artists_list));
				}
				if (tabId.equals("tab_genres")) {
					mGenreLogic.onCreate(MusicLibraryActivity.this, (ListView)findViewById(R.id.genres_list));
				}
				if (tabId.equals("tab_compilations")) {
					mCompilationsLogic.onCreate(MusicLibraryActivity.this, (ListView)findViewById(R.id.compilations_list));
				}
			}
		});
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(0, MENU_NOW_PLAYING, 0, "Now playing").setIcon(R.drawable.menu_nowplaying);
		switch (mTabHost.getCurrentTab()) {
			case 0:
				mAlbumLogic.onCreateOptionsMenu(menu);
				break;
			case 1:
				mArtistLogic.onCreateOptionsMenu(menu);
				break;
			case 2:
				mGenreLogic.onCreateOptionsMenu(menu);
				break;
			case 3:
				mCompilationsLogic.onCreateOptionsMenu(menu);
				break;
			case 4:
				mFileLogic.onCreateOptionsMenu(menu);
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
			mAlbumLogic.onOptionsItemSelected(item);
			break;
		case 1:
			mArtistLogic.onOptionsItemSelected(item);
			break;
		case 2:
			mGenreLogic.onOptionsItemSelected(item);
			break;
		case 3:
			mCompilationsLogic.onOptionsItemSelected(item);
			break;
		case 4:
			mFileLogic.onOptionsItemSelected(item);
			break;
		}
		
		// then the generic ones.
		switch (item.getItemId()) {
		case MENU_REMOTE:
			startActivity(new Intent(this, RemoteActivity.class));
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
				mAlbumLogic.onCreateContextMenu(menu, v, menuInfo);
				break;
			case 1:
				mArtistLogic.onCreateContextMenu(menu, v, menuInfo);
				break;
			case 2:
				mGenreLogic.onCreateContextMenu(menu, v, menuInfo);
				break;
			case 3:
				mCompilationsLogic.onCreateContextMenu(menu, v, menuInfo);
				break;
			case 4:
				mFileLogic.onCreateContextMenu(menu, v, menuInfo);
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
			mArtistLogic.onContextItemSelected(item);
			break;
		case 2:
			mGenreLogic.onContextItemSelected(item);
			break;
		case 3:
			mCompilationsLogic.onContextItemSelected(item);
			break;
		case 4:
			mFileLogic.onContextItemSelected(item);
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

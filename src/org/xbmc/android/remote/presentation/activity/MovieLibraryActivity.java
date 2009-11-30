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
import org.xbmc.android.remote.presentation.controller.ActorListController;
import org.xbmc.android.remote.presentation.controller.FileListController;
import org.xbmc.android.remote.presentation.controller.MovieGenreListController;
import org.xbmc.android.remote.presentation.controller.MovieListController;
import org.xbmc.android.util.ConnectionManager;
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

public class MovieLibraryActivity extends SlidingTabActivity  {

	private SlidingTabHost mTabHost;
	
	private MovieListController mMovieController;
	private ActorListController mActorController;
	private MovieGenreListController mGenresController;
	private FileListController mFileController;
	
	private static final int MENU_NOW_PLAYING = 301;
	private static final int MENU_UPDATE_LIBRARY = 302;
	private static final int MENU_REMOTE = 303;
	
    private ConfigurationManager mConfigurationManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.movielibrary);
		
		// remove nasty top fading edge
		FrameLayout topFrame = (FrameLayout)findViewById(android.R.id.content);
		topFrame.setForeground(null);
		
		mTabHost = getTabHost();
		
		// add the tabs
		mTabHost.addTab(mTabHost.newTabSpec("tab_movies", "Movies", R.drawable.st_movie_on, R.drawable.st_movie_off).setBigIcon(R.drawable.st_movie_over).setContent(R.id.movielist_outer_layout));
		mTabHost.addTab(mTabHost.newTabSpec("tab_actors", "Actors", R.drawable.st_actor_on, R.drawable.st_actor_off).setBigIcon(R.drawable.st_actor_over).setContent(R.id.actorlist_outer_layout));
		mTabHost.addTab(mTabHost.newTabSpec("tab_genres", "Genres", R.drawable.st_genre_on, R.drawable.st_genre_off).setBigIcon(R.drawable.st_genre_over).setContent(R.id.genrelist_outer_layout));
		mTabHost.addTab(mTabHost.newTabSpec("tab_files", "File Mode", R.drawable.st_filemode_on, R.drawable.st_filemode_off).setBigIcon(R.drawable.st_filemode_over).setContent(R.id.filelist_outer_layout));
		mTabHost.setCurrentTab(0);

		// assign the gui logic to each tab
		mMovieController = new MovieListController();
		mMovieController.findTitleView(findViewById(R.id.movielist_outer_layout));
		mMovieController.findMessageView(findViewById(R.id.movielist_outer_layout));
		mMovieController.onCreate(this, (ListView)findViewById(R.id.movielist_list)); // first tab can be updated now.

		mActorController = new ActorListController(ActorListController.TYPE_MOVIE);
		mActorController.findTitleView(findViewById(R.id.actorlist_outer_layout));
		mActorController.findMessageView(findViewById(R.id.actorlist_outer_layout));

		mGenresController = new MovieGenreListController();
		mGenresController.findTitleView(findViewById(R.id.genrelist_outer_layout));
		mGenresController.findMessageView(findViewById(R.id.genrelist_outer_layout));

		mFileController = new FileListController();
		mFileController.findTitleView(findViewById(R.id.filelist_outer_layout));
		mFileController.findMessageView(findViewById(R.id.filelist_outer_layout));
		
		mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				
				if (tabId.equals("tab_movies")) {
					mMovieController.onCreate(MovieLibraryActivity.this, (ListView)findViewById(R.id.movielist_list));
				}
				if (tabId.equals("tab_actors")) {
					mActorController.onCreate(MovieLibraryActivity.this, (ListView)findViewById(R.id.actorlist_list));
				}
				if (tabId.equals("tab_genres")) {
					mGenresController.onCreate(MovieLibraryActivity.this, (ListView)findViewById(R.id.genrelist_list));
				}
				if (tabId.equals("tab_files")) {
					mFileController.onCreate(MovieLibraryActivity.this, (ListView)findViewById(R.id.filelist_list));
				}
			}
		});
		mConfigurationManager = ConfigurationManager.getInstance(this);
		mConfigurationManager.initKeyguard();
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(0, MENU_NOW_PLAYING, 0, "Now playing").setIcon(R.drawable.menu_nowplaying);
		switch (mTabHost.getCurrentTab()) {
			case 0:
				mMovieController.onCreateOptionsMenu(menu);
				break;
			case 1:
				mActorController.onCreateOptionsMenu(menu);
				break;
			case 2:
				mGenresController.onCreateOptionsMenu(menu);
				break;
			case 3:
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
			mMovieController.onOptionsItemSelected(item);
			break;
		case 1:
			mActorController.onOptionsItemSelected(item);
			break;
		case 2:
			mGenresController.onOptionsItemSelected(item);
			break;
		case 3:
			mFileController.onOptionsItemSelected(item);
			break;
		}
		
		// then the generic ones.
		switch (item.getItemId()) {
			case MENU_REMOTE:
				startActivity(new Intent(this, RemoteActivity.class));
				return true;
			case MENU_UPDATE_LIBRARY:
				mMovieController.refreshMovieLibrary(this);
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
				mMovieController.onCreateContextMenu(menu, v, menuInfo);
				break;
			case 1:
				mActorController.onCreateContextMenu(menu, v, menuInfo);
				break;
			case 2:
				mGenresController.onCreateContextMenu(menu, v, menuInfo);
				break;
			case 3:
				mFileController.onCreateContextMenu(menu, v, menuInfo);
				break;
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (mTabHost.getCurrentTab()) {
		case 0:
			mMovieController.onContextItemSelected(item);
			break;
		case 1:
			mActorController.onContextItemSelected(item);
			break;
		case 2:
			mGenresController.onContextItemSelected(item);
			break;
		case 3:
			mFileController.onContextItemSelected(item);
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
	

	@Override
	protected void onResume() {
		super.onResume();
		mMovieController.onActivityResume(this);
		mFileController.onActivityResume(this);
		mConfigurationManager.onActivityResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mMovieController.onActivityPause();
		mFileController.onActivityPause();
		mConfigurationManager.onActivityPause();
	}
}

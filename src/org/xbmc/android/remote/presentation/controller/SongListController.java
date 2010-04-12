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

package org.xbmc.android.remote.presentation.controller;

import java.util.ArrayList;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.AbstractManager;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.widget.ThreeLabelsItemView;
import org.xbmc.android.util.ImportUtilities;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IMusicManager;
import org.xbmc.api.business.ISortableManager;
import org.xbmc.api.object.Album;
import org.xbmc.api.object.Artist;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.Song;
import org.xbmc.api.type.SortType;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class SongListController extends ListController implements IController {
	
	public static final int ITEM_CONTEXT_QUEUE = 1;
	public static final int ITEM_CONTEXT_PLAY = 2;
	public static final int ITEM_CONTEXT_INFO = 3;
	
	public static final int MENU_PLAY_ALL = 1;
	public static final int MENU_SORT = 2;
	public static final int MENU_SORT_BY_ALBUM_ASC = 31;
	public static final int MENU_SORT_BY_ALBUM_DESC = 32;
	public static final int MENU_SORT_BY_ARTIST_ASC = 33;
	public static final int MENU_SORT_BY_ARTIST_DESC = 34;
	public static final int MENU_SORT_BY_TITLE_ASC = 35;
	public static final int MENU_SORT_BY_TITLE_DESC = 36;
	public static final int MENU_SORT_BY_FILENAME_ASC = 37;
	public static final int MENU_SORT_BY_FILENAME_DESC = 38;
	
	private Album mAlbum;
	private Artist mArtist;
	private Genre mGenre;
	
	private IMusicManager mMusicManager;
	
	private boolean mLoadCovers = false;
	
	public void onCreate(Activity activity, AbsListView list) {
		
		mMusicManager = ManagerFactory.getMusicManager(this);
		
		((ISortableManager)mMusicManager).setSortKey(AbstractManager.PREF_SORT_KEY_SONG);
		((ISortableManager)mMusicManager).setPreferences(activity.getPreferences(Context.MODE_PRIVATE));
		
		final String sdError = ImportUtilities.assertSdCard();
		mLoadCovers = sdError == null;
		
		if (!isCreated()) {
			super.onCreate(activity, list);
			
			if (!mLoadCovers) {
				Toast toast = Toast.makeText(activity, sdError + " Displaying place holders only.", Toast.LENGTH_LONG);
				toast.show();
			}
			
			mAlbum = (Album)mActivity.getIntent().getSerializableExtra(EXTRA_ALBUM);
			mArtist = (Artist)mActivity.getIntent().getSerializableExtra(EXTRA_ARTIST);
			mGenre = (Genre)mActivity.getIntent().getSerializableExtra(EXTRA_GENRE);
			mActivity.registerForContextMenu(mList);
			
			mFallbackBitmap = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.icon_song);
			setupIdleListener();
			
			mList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final Song song = (Song)mList.getAdapter().getItem(((ThreeLabelsItemView)view).position);
					if (mAlbum == null) {
						mMusicManager.play(new QueryResponse(
							mActivity, 
							"Playing \"" + song.title + "\" by " + song.artist + "...", 
							"Error playing song!",
							true
						), song, mActivity.getApplicationContext());
					} else {
						mMusicManager.play(new QueryResponse(
							mActivity, 
							"Playing album \"" + song.album + "\" starting with song \"" + song.title + "\" by " + song.artist + "...",
							"Error playing song!",
							true
						), mAlbum, song, mActivity.getApplicationContext());
					}
				}
			});
					
			mList.setOnKeyListener(new ListControllerOnKeyListener<Song>());
			fetch();
		}
	}
	
	private void fetch() {
		final Album album = mAlbum; 
		final Genre genre = mGenre; 
		final Artist artist = mArtist; 
		showOnLoading();
		if (album != null) {
			setTitle("Songs...");
			mMusicManager.getSongs(new DataResponse<ArrayList<Song>>() {
				public void run() {
					setTitle(album.name);
					if (value.size() > 0) {
						mList.setAdapter(new SongAdapter(mActivity, value));
					} else {
						setNoDataMessage("No songs found", R.drawable.icon_song_dark);
					}
				}
			}, album, mActivity.getApplicationContext());
			
		} else if (artist != null) {
			setTitle(artist.name + " - Songs...");
			mMusicManager.getSongs(new DataResponse<ArrayList<Song>>() {
				public void run() {
					if (value.size() > 0) {
						setTitle(artist.name + " - Songs (" + value.size() + ")");
						mList.setAdapter(new SongAdapter(mActivity, value));
					} else {
						setTitle(artist.name + " - Songs");
						setNoDataMessage("No songs found.", R.drawable.icon_song_dark);
					}
				}
			}, artist, mActivity.getApplicationContext());
			
		} else if (genre != null) {
			setTitle(genre.name + " - Songs...");
			mMusicManager.getSongs(new DataResponse<ArrayList<Song>>() {
				public void run() {
					if (value.size() > 0) {
						setTitle(genre.name + " - Songs (" + value.size() + ")");
						mList.setAdapter(new SongAdapter(mActivity, value));
					} else {
						setTitle(genre.name + " - Songs");
						setNoDataMessage("No songs found.", R.drawable.icon_song_dark);
					}
				}
			}, genre, mActivity.getApplicationContext());
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// be aware that this must be explicitly called by your activity!
		final ThreeLabelsItemView view = (ThreeLabelsItemView)((AdapterContextMenuInfo)menuInfo).targetView;
		menu.setHeaderTitle(((Song)mList.getItemAtPosition(view.getPosition())).title);
		menu.add(0, ITEM_CONTEXT_QUEUE, 1, "Queue Song");
		menu.add(0, ITEM_CONTEXT_PLAY, 2, "Play Song");
	}
	
	public void onContextItemSelected(MenuItem item) {
		// be aware that this must be explicitly called by your activity!
		final Song song = (Song)mList.getAdapter().getItem(((ThreeLabelsItemView)((AdapterContextMenuInfo)item.getMenuInfo()).targetView).position);
		switch (item.getItemId()) {
			case ITEM_CONTEXT_QUEUE:
				if (mAlbum == null) {
					mMusicManager.addToPlaylist(new QueryResponse(mActivity, "Song added to playlist.", "Error adding song!"), song, mActivity.getApplicationContext());
				} else {
					mMusicManager.addToPlaylist(new QueryResponse(mActivity, "Playlist empty, added whole album.", "Song added to playlist."), mAlbum, song, mActivity.getApplicationContext());
				}
				break;
			case ITEM_CONTEXT_PLAY:
				if (mAlbum == null) {
					mMusicManager.play(new QueryResponse(
						mActivity, 
						"Playing \"" + song.title + "\" by " + song.artist + "...", 
						"Error playing song!",
						true
					), song, mActivity.getApplicationContext());
				} else {
					mMusicManager.play(new QueryResponse(
						mActivity, 
						"Playing album \"" + song.album + "\" starting with song \"" + song.title + "\" by " + song.artist + "...",
						"Error playing song!",
						true
					), mAlbum, song, mActivity.getApplicationContext());
				}
				break;
			default:
				return;
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_PLAY_ALL, 0, "Play all").setIcon(R.drawable.menu_song);
		SubMenu sortMenu = menu.addSubMenu(0, MENU_SORT, 0, "Sort").setIcon(R.drawable.menu_sort);
		sortMenu.add(2, MENU_SORT_BY_ALBUM_ASC, 0, "by Album ascending");
		sortMenu.add(2, MENU_SORT_BY_ALBUM_DESC, 0, "by Album descending");
		sortMenu.add(2, MENU_SORT_BY_ARTIST_ASC, 0, "by Artist ascending");
		sortMenu.add(2, MENU_SORT_BY_ARTIST_DESC, 0, "by Artist descending");
		sortMenu.add(2, MENU_SORT_BY_TITLE_ASC, 0, "by Title ascending");
		sortMenu.add(2, MENU_SORT_BY_TITLE_DESC, 0, "by Title descending");
		sortMenu.add(2, MENU_SORT_BY_FILENAME_ASC, 0, "by Filename ascending");
		sortMenu.add(2, MENU_SORT_BY_FILENAME_DESC, 0, "by Filename descending");
	}
	
	@Override
	public void onOptionsItemSelected(MenuItem item) {
		final SharedPreferences.Editor ed;
		switch (item.getItemId()) {
		case MENU_PLAY_ALL:
			final Album album = mAlbum;
			final Genre genre = mGenre;
			final Artist artist = mArtist;
			if (album != null) {
				mMusicManager.play(new QueryResponse(
						mActivity, 
						"Playing all songs of album " + album.name + " by " + album.artist + "...", 
						"Error playing songs!",
						true
					), album, mActivity.getApplicationContext());			
			} else if (artist != null) {
				mMusicManager.play(new QueryResponse(
						mActivity, 
						"Playing all songs from " + artist.name + "...", 
						"Error playing songs!",
						true
					), artist, mActivity.getApplicationContext());
			} else if (genre != null) {
				mMusicManager.play(new QueryResponse(
						mActivity, 
						"Playing all songs of genre " + genre.name + "...", 
						"Error playing songs!",
						true
					), genre, mActivity.getApplicationContext());
			}
			break;
		case MENU_SORT_BY_ALBUM_ASC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_SONG, SortType.ALBUM);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_SONG, SortType.ORDER_ASC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_ALBUM_DESC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_SONG, SortType.ALBUM);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_SONG, SortType.ORDER_DESC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_ARTIST_ASC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_SONG, SortType.ARTIST);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_SONG, SortType.ORDER_ASC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_ARTIST_DESC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_SONG, SortType.ARTIST);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_SONG, SortType.ORDER_DESC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_TITLE_ASC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_SONG, SortType.TITLE);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_SONG, SortType.ORDER_ASC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_TITLE_DESC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_SONG, SortType.TITLE);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_SONG, SortType.ORDER_DESC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_FILENAME_ASC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_SONG, SortType.FILENAME);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_SONG, SortType.ORDER_ASC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_FILENAME_DESC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_SONG, SortType.FILENAME);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_SONG, SortType.ORDER_DESC);
			ed.commit();
			fetch();
			break;
		}
	}
	
	private class SongAdapter extends ArrayAdapter<Song> {
		SongAdapter(Activity activity, ArrayList<Song> items) {
			super(activity, 0, items);
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			final ThreeLabelsItemView view;
			if (convertView == null) {
				view = new ThreeLabelsItemView(mActivity, mMusicManager, parent.getWidth(), mFallbackBitmap, mList.getSelector());
			} else {
				view = (ThreeLabelsItemView)convertView;
			}
			
			final Song song = getItem(position);
			view.reset();
			view.position = position;
			view.title = song.title;
			if (mAlbum != null) {
				view.subtitle = song.artist;
			} else if (mArtist != null) {
				view.subtitle = song.album;
			} else if (mGenre != null) {
				view.subtitle = song.artist;
			}
			view.subsubtitle = song.getDuration();
			
			if (mLoadCovers) {
				view.getResponse().load(song, !mPostScrollLoader.isListIdle());
			}
			return view;
		}
	}
	
	public void onActivityPause() {
		if (mMusicManager != null) {
			mMusicManager.setController(null);
			mMusicManager.postActivity();
		}
		super.onActivityPause();
	}

	public void onActivityResume(Activity activity) {
		super.onActivityResume(activity);
		if (mMusicManager != null) {
			mMusicManager.setController(this);
		}
	}
	
	private static final long serialVersionUID = 755529227668553163L;
}

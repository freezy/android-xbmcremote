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
import org.xbmc.android.remote.business.ManagerThread;
import org.xbmc.android.remote.presentation.controller.holder.ThreeHolder;
import org.xbmc.android.remote.presentation.drawable.CrossFadeDrawable;
import org.xbmc.api.business.DataResponse;
import org.xbmc.httpapi.data.Album;
import org.xbmc.httpapi.data.Artist;
import org.xbmc.httpapi.data.Genre;
import org.xbmc.httpapi.data.Song;
import org.xbmc.httpapi.type.SortType;
import org.xbmc.httpapi.type.ThumbSize;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class SongListController extends ListController {
	
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
	
	private boolean mLoadCovers = false;
	
	public void onCreate(Activity activity, ListView list) {
		
		ManagerThread.music().setSortKey(AbstractManager.PREF_SORT_KEY_SONG);
		ManagerThread.music().setPreferences(activity.getPreferences(Context.MODE_PRIVATE));
		
		mLoadCovers = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		
		if (!isCreated()) {
			super.onCreate(activity, list);
			
			if (!mLoadCovers) {
				Toast toast = Toast.makeText(activity, "Your SD card is not mounted. You'll need it for caching covers. Displaying place holders only.", Toast.LENGTH_LONG);
				toast.show();
			}
			
			mAlbum = (Album)mActivity.getIntent().getSerializableExtra(EXTRA_ALBUM);
			mArtist = (Artist)mActivity.getIntent().getSerializableExtra(EXTRA_ARTIST);
			mGenre = (Genre)mActivity.getIntent().getSerializableExtra(EXTRA_GENRE);
			mActivity.registerForContextMenu(mList);
			
			mFallbackBitmap = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.icon_song);
			setupIdleListener();
			
			mList.setOnItemClickListener(new OnItemClickListener() {
				@SuppressWarnings("unchecked")
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final Song song = ((ThreeHolder<Song>)view.getTag()).holderItem;
					if (mAlbum == null) {
						ManagerThread.music().play(new QueryHandler(
							mActivity, 
							"Playing \"" + song.title + "\" by " + song.artist + "...", 
							"Error playing song!",
							true
						), song);
					} else {
						ManagerThread.music().play(new QueryHandler(
							mActivity, 
							"Playing album \"" + song.album + "\" starting with song \"" + song.title + "\" by " + song.artist + "...",
							"Error playing song!",
							true
						), mAlbum, song);
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
		if (album != null) {
			setTitle("Songs...");
			ManagerThread.music().getSongs(new DataResponse<ArrayList<Song>>(mActivity) {
				public void run() {
					setTitle(album.name);
					if (value.size() > 0) {
						mList.setAdapter(new SongAdapter(mActivity, value));
					} else {
						setNoDataMessage("No songs found", R.drawable.icon_song_dark);
					}
				}
			}, album);
			
		} else if (artist != null) {
			setTitle(artist.name + " - Songs...");
			ManagerThread.music().getSongs(new DataResponse<ArrayList<Song>>(mActivity) {
				public void run() {
					if (value.size() > 0) {
						setTitle(artist.name + " - Songs (" + value.size() + ")");
						mList.setAdapter(new SongAdapter(mActivity, value));
					} else {
						setTitle(artist.name + " - Songs");
						setNoDataMessage("No songs found.", R.drawable.icon_song_dark);
					}
				}
			}, artist);
			
		} else if (genre != null) {
			setTitle(genre.name + " - Songs...");
			ManagerThread.music().getSongs(new DataResponse<ArrayList<Song>>(mActivity) {
				public void run() {
					if (value.size() > 0) {
						setTitle(genre.name + " - Songs (" + value.size() + ")");
						mList.setAdapter(new SongAdapter(mActivity, value));
					} else {
						setTitle(genre.name + " - Songs");
						setNoDataMessage("No songs found.", R.drawable.icon_song_dark);
					}
				}
			}, genre);
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// be aware that this must be explicitly called by your activity!
		final ThreeHolder<Song> holder = (ThreeHolder<Song>)((AdapterContextMenuInfo)menuInfo).targetView.getTag();
		menu.setHeaderTitle(holder.holderItem.title);
		menu.add(0, ITEM_CONTEXT_QUEUE, 1, "Queue Song");
		menu.add(0, ITEM_CONTEXT_PLAY, 2, "Play Song");
	}
	
	@SuppressWarnings("unchecked")
	public void onContextItemSelected(MenuItem item) {
		// be aware that this must be explicitly called by your activity!
		final ThreeHolder<Song> holder = (ThreeHolder<Song>)((AdapterContextMenuInfo)item.getMenuInfo()).targetView.getTag();
		switch (item.getItemId()) {
			case ITEM_CONTEXT_QUEUE:
				if (mAlbum == null) {
					ManagerThread.music().addToPlaylist(new QueryHandler(mActivity, "Song added to playlist.", "Error adding song!"), holder.holderItem);
				} else {
					ManagerThread.music().addToPlaylist(new QueryHandler(mActivity, "Playlist empty, added whole album.", "Song added to playlist."), mAlbum, holder.holderItem);
				}
				break;
			case ITEM_CONTEXT_PLAY:
				final Song song = holder.holderItem;
				if (mAlbum == null) {
					ManagerThread.music().play(new QueryHandler(
						mActivity, 
						"Playing \"" + song.title + "\" by " + song.artist + "...", 
						"Error playing song!",
						true
					), song);
				} else {
					ManagerThread.music().play(new QueryHandler(
						mActivity, 
						"Playing album \"" + song.album + "\" starting with song \"" + song.title + "\" by " + song.artist + "...",
						"Error playing song!",
						true
					), mAlbum, song);
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
				ManagerThread.music().play(new QueryHandler(
						mActivity, 
						"Playing all songs of album " + album.name + " by " + album.artist + "...", 
						"Error playing songs!",
						true
					), album);			
			} else if (artist != null) {
				ManagerThread.music().play(new QueryHandler(
						mActivity, 
						"Playing all songs from " + artist.name + "...", 
						"Error playing songs!",
						true
					), artist);
			} else if (genre != null) {
				ManagerThread.music().play(new QueryHandler(
						mActivity, 
						"Playing all songs of genre " + genre.name + "...", 
						"Error playing songs!",
						true
					), genre);
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
		private Activity mActivity;
		private final LayoutInflater mInflater;
		SongAdapter(Activity activity, ArrayList<Song> items) {
			super(activity, R.layout.listitem_three, items);
			mActivity = activity;
			mInflater = LayoutInflater.from(activity);
		}
		@SuppressWarnings("unchecked")
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View row;
			ThreeHolder<Song> holder;
			
			if (convertView == null) {
				
				row = mInflater.inflate(R.layout.listitem_three, null);
				holder = new ThreeHolder<Song>(
					(ImageView)row.findViewById(R.id.MusicItemImageViewArt),
					(TextView)row.findViewById(R.id.MusicItemTextViewTitle),
					(TextView)row.findViewById(R.id.MusicItemTextViewSubtitle),
					(TextView)row.findViewById(R.id.MusicItemTextViewSubSubtitle)
				);
				row.setTag(holder);
				
				CrossFadeDrawable transition = new CrossFadeDrawable(mFallbackBitmap, null);
				transition.setCrossFadeEnabled(true);
				holder.transition = transition;
				holder.defaultCover = R.drawable.icon_song;
				
			} else {
				row = convertView;
				holder = (ThreeHolder<Song>)convertView.getTag();
			}
			final Song song = getItem(position);
			
			holder.titleView.setText(song.title);
			if (mAlbum != null) {
				holder.subtitleView.setText(song.artist);
			} else if (mArtist != null) {
				holder.subtitleView.setText(song.album);
			} else if (mGenre != null) {
				holder.subtitleView.setText(song.artist);
			}
			holder.subsubtitleView.setText(song.getDuration());
			holder.holderItem = song;
			holder.id = song.getId();
			holder.coverItem = song;
			if (mLoadCovers) {
				holder.iconView.setImageResource(R.drawable.icon_song_dark);
				holder.tempBind = true;
				ManagerThread.music().getCover(holder.getCoverDownloadHandler(mActivity, mPostScrollLoader), song, ThumbSize.SMALL);
			} else {
				holder.iconView.setImageResource(R.drawable.icon_song);
			}
			return row;
		}
	}
	
	private static final long serialVersionUID = 755529227668553163L;
}

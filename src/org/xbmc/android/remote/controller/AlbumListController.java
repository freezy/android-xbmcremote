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

package org.xbmc.android.remote.controller;

import java.util.ArrayList;

import org.xbmc.android.backend.httpapi.HttpApiHandler;
import org.xbmc.android.backend.httpapi.HttpApiThread;
import org.xbmc.android.backend.httpapi.MusicWrapper;
import org.xbmc.android.backend.httpapi.Wrapper;
import org.xbmc.android.remote.R;
import org.xbmc.android.remote.activity.DialogFactory;
import org.xbmc.android.remote.activity.ListActivity;
import org.xbmc.android.remote.controller.holder.OneHolder;
import org.xbmc.android.remote.controller.holder.ThreeHolder;
import org.xbmc.android.remote.drawable.CrossFadeDrawable;
import org.xbmc.httpapi.data.Album;
import org.xbmc.httpapi.data.Artist;
import org.xbmc.httpapi.data.Genre;
import org.xbmc.httpapi.type.SortType;
import org.xbmc.httpapi.type.ThumbSize;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class AlbumListController extends ListController {
	
	public static final int ITEM_CONTEXT_QUEUE = 1;
	public static final int ITEM_CONTEXT_PLAY = 2;
	public static final int ITEM_CONTEXT_INFO = 3;
	
	public static final int MENU_PLAY_ALL = 1;
	public static final int MENU_SORT = 2;
	public static final int MENU_SORT_BY_ARTIST_ASC = 21;
	public static final int MENU_SORT_BY_ARTIST_DESC = 22;
	public static final int MENU_SORT_BY_ALBUM_ASC = 23;
	public static final int MENU_SORT_BY_ALBUM_DESC = 24;
	public static final int MENU_SWITCH_VIEW = 3;
	
	private static final int VIEW_LIST = 1;
	private static final int VIEW_GRID = 2;
	
	private int mCurrentView = VIEW_LIST;
	
	
	private Artist mArtist;
	private Genre mGenre;
	private boolean mCompilationsOnly = false;

	private GridView mGrid = null;
	
	/**
	 * Defines if only compilations should be listed.
	 * @param co True if compilations only should be listed, false otherwise.
	 */
	public void setCompilationsOnly(boolean co) {
		mCompilationsOnly = co;
	}
	
	/**
	 * If grid reference is set, albums can be displayed as wall view.
	 * @param grid Reference to GridView
	 */
	public void setGrid(GridView grid) {
		mGrid = grid;
	}
	
	public void onCreate(Activity activity, ListView list) {
		
		MusicWrapper.setSortKey(Wrapper.PREF_SORT_KEY_ALBUM);
		MusicWrapper.setPreferences(activity.getPreferences(Context.MODE_PRIVATE));
		
		if (!isCreated()) {
			super.onCreate(activity, list);
			
			mArtist = (Artist)mActivity.getIntent().getSerializableExtra(ListController.EXTRA_ARTIST);
			mGenre = (Genre)mActivity.getIntent().getSerializableExtra(ListController.EXTRA_GENRE);
			activity.registerForContextMenu(mList);
			
			mFallbackBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.icon_album_grey);
			setupIdleListener();
			
//			ImportUtilities.purgeCache();
			
			mList.setOnItemClickListener(new OnItemClickListener() {
				@SuppressWarnings("unchecked")
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent nextActivity;
					ThreeHolder<Album> holder = (ThreeHolder<Album>)view.getTag();
					nextActivity = new Intent(view.getContext(), ListActivity.class);
					nextActivity.putExtra(ListController.EXTRA_LIST_LOGIC, new SongListController());
					nextActivity.putExtra(ListController.EXTRA_ALBUM, holder.holderItem);
					mActivity.startActivity(nextActivity);
				}
			});
			mList.setOnKeyListener(new ListLogicOnKeyListener<Album>());
			fetch();
		}
	}
	
	private void setAdapter(ArrayList<Album> value) {
		switch (mCurrentView) {
			case VIEW_LIST:
				mList.setAdapter(new AlbumAdapter(mActivity, value));
				mList.setVisibility(View.VISIBLE);
				if (mGrid != null) {
					mGrid.setVisibility(View.GONE);
				}
				break;
			case VIEW_GRID:
				if (mGrid != null) {
					mGrid.setAdapter(new AlbumGridAdapter(mActivity, value));
					mGrid.setVisibility(View.VISIBLE);
					mList.setVisibility(View.GONE);
				} else {
					mList.setVisibility(View.VISIBLE);
					mList.setAdapter(new AlbumAdapter(mActivity, value));
				}
			break;
		}
	}
	
	private void fetch() {
		final Artist artist = mArtist;
		final Genre genre = mGenre;
		if (artist != null) {						// albums of an artist
			setTitle(artist.name + " - Albums...");
			HttpApiThread.music().getAlbums(new HttpApiHandler<ArrayList<Album>>(mActivity) {
				public void run() {
					if (value.size() > 0) {
						setTitle(artist.name + " - Albums (" + value.size() + ")");
						setAdapter(value);
					} else {
						setTitle(artist.name + " - Albums");
						setNoDataMessage("No albums found.", R.drawable.icon_album_dark);
					}
				}
			}, artist);
			
		} else if (genre != null) {					// albums of a genre
			setTitle(genre.name + " - Albums...");
			HttpApiThread.music().getAlbums(new HttpApiHandler<ArrayList<Album>>(mActivity) {
				public void run() {
					if (value.size() > 0) {
						setTitle(genre.name + " - Albums (" + value.size() + ")");
						setAdapter(value);
					} else {
						setTitle(genre.name + " - Albums");
						setNoDataMessage("No albums found.", R.drawable.icon_album_dark);
					}
				}
			}, genre);
			
		} else {
			if (mCompilationsOnly) {				// compilations
				setTitle("Compilations...");
				HttpApiThread.music().getCompilations(new HttpApiHandler<ArrayList<Album>>(mActivity) {
					public void run() {
						if (value.size() > 0) {
							setTitle("Compilations (" + value.size() + ")");
							setAdapter(value);
						} else {
							setTitle("Compilations");
							setNoDataMessage("No compilations found.", R.drawable.icon_album_dark);
						}
					}
				});
			} else {
				setTitle("Albums...");				// all albums
				HttpApiThread.music().getAlbums(new HttpApiHandler<ArrayList<Album>>(mActivity) {
					public void run() {
						if (value.size() > 0) {
							setTitle("Albums (" + value.size() + ")");
							setAdapter(value);
						} else {
							setTitle("Albums");
							setNoDataMessage("No Albums found.", R.drawable.icon_album_dark);
						}
					}
				});
			}
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		final ThreeHolder<Album> holder = (ThreeHolder<Album>)((AdapterContextMenuInfo)menuInfo).targetView.getTag();
		menu.setHeaderTitle(holder.holderItem.name);
		menu.add(0, ITEM_CONTEXT_QUEUE, 1, "Queue Album");
		menu.add(0, ITEM_CONTEXT_PLAY, 2, "Play Album");
		menu.add(0, ITEM_CONTEXT_INFO, 3, "View Details");
	}
	
	@SuppressWarnings("unchecked")
	public void onContextItemSelected(MenuItem item) {
		final ThreeHolder<Album> holder = (ThreeHolder<Album>)((AdapterContextMenuInfo)item.getMenuInfo()).targetView.getTag();
		final Album album = holder.holderItem;
		switch (item.getItemId()) {
			case ITEM_CONTEXT_QUEUE:
				HttpApiThread.music().addToPlaylist(new QueryHandler(
						mActivity, 
						"Adding album \"" + album.name + "\" by " + album.artist + " to playlist...", 
						"Error adding album!"
					), album);
				break;
			case ITEM_CONTEXT_PLAY:
				HttpApiThread.music().play(new QueryHandler(
						mActivity, 
						"Playing album \"" + album.name + "\" by " + album.artist + "...", 
						"Error playing album!",
						true
					), album);
				break;
			case ITEM_CONTEXT_INFO:
				DialogFactory.getAlbumDetail(mActivity, album).show();
				break;
			default:
				return;
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu) {
		if (mArtist != null || mGenre != null) {
			menu.add(0, MENU_PLAY_ALL, 0, "Play all").setIcon(R.drawable.menu_album);
		}
		SubMenu sortMenu = menu.addSubMenu(0, MENU_SORT, 0, "Sort").setIcon(R.drawable.menu_sort);
		sortMenu.add(2, MENU_SORT_BY_ALBUM_ASC, 0, "by Album ascending");
		sortMenu.add(2, MENU_SORT_BY_ALBUM_DESC, 0, "by Album descending");
		sortMenu.add(2, MENU_SORT_BY_ARTIST_ASC, 0, "by Artist ascending");
		sortMenu.add(2, MENU_SORT_BY_ARTIST_DESC, 0, "by Artist descending");
//		menu.add(0, MENU_SWITCH_VIEW, 0, "Switch view");
	}
	
	@Override
	public void onOptionsItemSelected(MenuItem item) {
		final SharedPreferences.Editor ed;
		switch (item.getItemId()) {
		case MENU_PLAY_ALL:
			final Artist artist = mArtist;
			final Genre genre = mGenre;
			if (artist != null && genre == null) {
				HttpApiThread.music().play(new QueryHandler(
						mActivity, 
						"Playing all albums by " + artist.name + "...", 
						"Error playing songs!",
						true
					), genre);			
			} else if (genre != null && artist == null) {
				HttpApiThread.music().play(new QueryHandler(
						mActivity, 
						"Playing all albums of genre " + genre.name + "...", 
						"Error playing songs!",
						true
					), genre);
			} else if (genre != null && artist != null) {
				HttpApiThread.music().play(new QueryHandler(
						mActivity, 
						"Playing all songs of genre " + genre.name + " by " + artist.name + "...", 
						"Error playing songs!",
						true
					), artist, genre);
			}
			break;
		case MENU_SORT_BY_ALBUM_ASC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(Wrapper.PREF_SORT_BY_PREFIX + Wrapper.PREF_SORT_KEY_ALBUM, SortType.ALBUM);
			ed.putString(Wrapper.PREF_SORT_ORDER_PREFIX + Wrapper.PREF_SORT_KEY_ALBUM, SortType.ORDER_ASC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_ALBUM_DESC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(Wrapper.PREF_SORT_BY_PREFIX + Wrapper.PREF_SORT_KEY_ALBUM, SortType.ALBUM);
			ed.putString(Wrapper.PREF_SORT_ORDER_PREFIX + Wrapper.PREF_SORT_KEY_ALBUM, SortType.ORDER_DESC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_ARTIST_ASC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(Wrapper.PREF_SORT_BY_PREFIX + Wrapper.PREF_SORT_KEY_ALBUM, SortType.ARTIST);
			ed.putString(Wrapper.PREF_SORT_ORDER_PREFIX + Wrapper.PREF_SORT_KEY_ALBUM, SortType.ORDER_ASC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_ARTIST_DESC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(Wrapper.PREF_SORT_BY_PREFIX + Wrapper.PREF_SORT_KEY_ALBUM, SortType.ARTIST);
			ed.putString(Wrapper.PREF_SORT_ORDER_PREFIX + Wrapper.PREF_SORT_KEY_ALBUM, SortType.ORDER_DESC);
			ed.commit();
			fetch();
			break;
		case MENU_SWITCH_VIEW:
			mCurrentView = (mCurrentView % 2) + 1;
			fetch();
			break;
		}
	}
	
	private class AlbumAdapter extends ArrayAdapter<Album> {
		private Activity mActivity;
		private final LayoutInflater mInflater;
		AlbumAdapter(Activity activity, ArrayList<Album> items) {
			super(activity, R.layout.listitem_three, items);
			mActivity = activity;
			mInflater = LayoutInflater.from(activity);
		}
		
		@SuppressWarnings("unchecked")
		public View getView(int position, View convertView, ViewGroup parent) {
			
			final View row;
			final ThreeHolder<Album> holder;
			
			if (convertView == null) {

				row = mInflater.inflate(R.layout.listitem_three, null);
				holder = new ThreeHolder<Album>(
					(ImageView)row.findViewById(R.id.MusicItemImageViewArt),
					(TextView)row.findViewById(R.id.MusicItemTextViewTitle),
					(TextView)row.findViewById(R.id.MusicItemTextViewSubtitle),
					(TextView)row.findViewById(R.id.MusicItemTextViewSubSubtitle)
				);
				row.setTag(holder);
				
				CrossFadeDrawable transition = new CrossFadeDrawable(mFallbackBitmap, null);
				transition.setCrossFadeEnabled(true);
				holder.transition = transition;
				
			} else {
				row = convertView;
				holder = (ThreeHolder<Album>)convertView.getTag();
			}
			
			final Album album = getItem(position);
			holder.holderItem = album;
			holder.coverItem = album;
			holder.id = album.getCrc();
			
			holder.titleView.setText(album.name);
			holder.subtitleView.setText(album.artist);
			holder.subsubtitleView.setText(album.year > 0 ? String.valueOf(album.year) : "");
			holder.iconView.setImageResource(R.drawable.icon_album_grey);
			holder.setTemporaryBind(true);
		
			HttpApiThread.music().getAlbumCover(holder.getCoverDownloadHandler(mActivity, mPostScrollLoader), album, ThumbSize.SMALL);
			return row;
		}
	}
	
	private class AlbumGridAdapter extends ArrayAdapter<Album> {
		private Activity mActivity;
		AlbumGridAdapter(Activity activity, ArrayList<Album> items) {
			super(activity, R.layout.listitem_three, items);
			mActivity = activity;
		}
		
		@SuppressWarnings("unchecked")
		public View getView(int position, View convertView, ViewGroup parent) {
			
			final ImageView row;
			final OneHolder<Album> holder;
			
			if (convertView == null) {
				
				row = new ImageView(mActivity);
				holder = new OneHolder<Album>(row, null);
				row.setTag(holder);
				
				CrossFadeDrawable transition = new CrossFadeDrawable(mFallbackBitmap, null);
				transition.setCrossFadeEnabled(true);
				holder.transition = transition;
				
			} else {
				row = (ImageView)convertView;
				holder = (OneHolder<Album>)convertView.getTag();
			}
			
			final Album album = getItem(position);
			holder.holderItem = album;
			holder.coverItem = album;
			holder.id = album.getCrc();
			
			row.setImageResource(R.drawable.icon_album_grey);
			holder.setTemporaryBind(true);
			
			HttpApiThread.music().getAlbumCover(holder.getCoverDownloadHandler(mActivity, mPostScrollLoader), album, ThumbSize.MEDIUM);
			return row;
		}
	}
	
	private static final long serialVersionUID = 1088971882661811256L;
}

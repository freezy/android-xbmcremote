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
import org.xbmc.android.remote.presentation.activity.DialogFactory;
import org.xbmc.android.remote.presentation.activity.ListActivity;
import org.xbmc.android.remote.presentation.widget.ThreeLabelsItemView;
import org.xbmc.android.util.ImportUtilities;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.IMusicManager;
import org.xbmc.api.business.ISortableManager;
import org.xbmc.api.object.Album;
import org.xbmc.api.object.Artist;
import org.xbmc.api.object.Genre;
import org.xbmc.api.type.SortType;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

/**
 * TODO Once we move to 1.6+, waste the deprecated code. 
 */
public class AlbumListController extends ListController implements IController {
	
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
	
	private IMusicManager mMusicManager;
	private IControlManager mControlManager;
	
	private boolean mCompilationsOnly = false;
	private boolean mLoadCovers = false;

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
		
		mMusicManager = ManagerFactory.getMusicManager(this);
		mControlManager = ManagerFactory.getControlManager(this);
		
		((ISortableManager)mMusicManager).setSortKey(AbstractManager.PREF_SORT_KEY_ALBUM);
		((ISortableManager)mMusicManager).setPreferences(activity.getPreferences(Context.MODE_PRIVATE));
		
		final String sdError = ImportUtilities.assertSdCard();
		mLoadCovers = sdError == null;

		
		if (!isCreated()) {
			super.onCreate(activity, list);

			if (!mLoadCovers) {
				Toast toast = Toast.makeText(activity, sdError + " Displaying place holders only.", Toast.LENGTH_LONG);
				toast.show();
			}
			
			mArtist = (Artist)activity.getIntent().getSerializableExtra(ListController.EXTRA_ARTIST);
			mGenre = (Genre)activity.getIntent().getSerializableExtra(ListController.EXTRA_GENRE);
			activity.registerForContextMenu(mList);
			
			mFallbackBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.icon_album_dark);
			setupIdleListener();
			
			mList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent nextActivity;
					final Album album = (Album)mList.getAdapter().getItem(((ThreeLabelsItemView)view).position);
					nextActivity = new Intent(view.getContext(), ListActivity.class);
					nextActivity.putExtra(ListController.EXTRA_LIST_CONTROLLER, new SongListController());
					nextActivity.putExtra(ListController.EXTRA_ALBUM, album);
					mActivity.startActivity(nextActivity);
				}
			});
			mList.setOnKeyListener(new ListControllerOnKeyListener<Album>());
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
	
	public void updateLibrary() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setMessage("Are you sure you want XBMC to rescan your music library?")
			.setCancelable(false)
			.setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					mControlManager.updateLibrary(new DataResponse<Boolean>() {
						public void run() {
							final String message;
							if (value) {
								message = "Music library updated has been launched.";
							} else {
								message = "Error launching music library update.";
							}
							Toast toast = Toast.makeText(mActivity, message, Toast.LENGTH_SHORT);
							toast.show();
						}
					}, "music");
				}
			})
			.setNegativeButton("Uh, no.", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
		builder.create().show();

	}
	
	private void fetch() {
		final Artist artist = mArtist;
		final Genre genre = mGenre;
		if (artist != null) {						// albums of an artist
			setTitle(artist.name + " - Albums...");
			showOnLoading();
			mMusicManager.getAlbums(new DataResponse<ArrayList<Album>>() {
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
			showOnLoading();
			mMusicManager.getAlbums(new DataResponse<ArrayList<Album>>() {
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
				showOnLoading();
				mMusicManager.getCompilations(new DataResponse<ArrayList<Album>>() {
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
				showOnLoading();
				mMusicManager.getAlbums(new DataResponse<ArrayList<Album>>() {
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
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		final ThreeLabelsItemView view = (ThreeLabelsItemView)((AdapterContextMenuInfo)menuInfo).targetView;
		menu.setHeaderTitle(((Album)mList.getItemAtPosition(view.getPosition())).name);
		menu.add(0, ITEM_CONTEXT_QUEUE, 1, "Queue Album");
		menu.add(0, ITEM_CONTEXT_PLAY, 2, "Play Album");
		menu.add(0, ITEM_CONTEXT_INFO, 3, "View Details");
	}
	
	public void onContextItemSelected(MenuItem item) {
		final Album album = (Album)mList.getAdapter().getItem(((ThreeLabelsItemView)((AdapterContextMenuInfo)item.getMenuInfo()).targetView).position);
		switch (item.getItemId()) {
			case ITEM_CONTEXT_QUEUE:
				mMusicManager.addToPlaylist(new QueryResponse(
						mActivity, 
						"Adding album \"" + album.name + "\" by " + album.artist + " to playlist...", 
						"Error adding album!"
					), album);
				break;
			case ITEM_CONTEXT_PLAY:
				mMusicManager.play(new QueryResponse(
						mActivity, 
						"Playing album \"" + album.name + "\" by " + album.artist + "...", 
						"Error playing album!",
						true
					), album);
				break;
			case ITEM_CONTEXT_INFO:
				DialogFactory.getAlbumDetail(mMusicManager, mActivity, album).show();
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
//		menu.add(0, MENU_SWITCH_VIEW, 0, "Switch view").setIcon(R.drawable.menu_view);
	}
	
	@Override
	public void onOptionsItemSelected(MenuItem item) {
		final SharedPreferences.Editor ed;
		switch (item.getItemId()) {
		case MENU_PLAY_ALL:
			final Artist artist = mArtist;
			final Genre genre = mGenre;
			if (artist != null && genre == null) {
				mMusicManager.play(new QueryResponse(
						mActivity, 
						"Playing all albums by " + artist.name + "...", 
						"Error playing songs!",
						true
					), genre);			
			} else if (genre != null && artist == null) {
				mMusicManager.play(new QueryResponse(
						mActivity, 
						"Playing all albums of genre " + genre.name + "...", 
						"Error playing songs!",
						true
					), genre);
			} else if (genre != null && artist != null) {
				mMusicManager.play(new QueryResponse(
						mActivity, 
						"Playing all songs of genre " + genre.name + " by " + artist.name + "...", 
						"Error playing songs!",
						true
					), artist, genre);
			}
			break;
		case MENU_SORT_BY_ALBUM_ASC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_ALBUM, SortType.ALBUM);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_ALBUM, SortType.ORDER_ASC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_ALBUM_DESC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_ALBUM, SortType.ALBUM);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_ALBUM, SortType.ORDER_DESC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_ARTIST_ASC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_ALBUM, SortType.ARTIST);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_ALBUM, SortType.ORDER_ASC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_ARTIST_DESC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_ALBUM, SortType.ARTIST);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_ALBUM, SortType.ORDER_DESC);
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
		AlbumAdapter(Activity activity, ArrayList<Album> items) {
			super(activity, 0, items);
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			final ThreeLabelsItemView view;
			if (convertView == null) {
				view = new ThreeLabelsItemView(mActivity, mMusicManager, parent.getWidth(), mFallbackBitmap);
			} else {
				view = (ThreeLabelsItemView)convertView;
			}
			
			final Album album = getItem(position);
			view.reset();
			view.position = position;
			view.title = album.name;
			view.subtitle = album.artist;
			view.subsubtitle = album.year > 0 ? String.valueOf(album.year) : "";
			
			if (mLoadCovers) {
				view.getResponse().load(album);
			}
			return view;
		}
	}
	
	private class AlbumGridAdapter extends ArrayAdapter<Album> {
		AlbumGridAdapter(Activity activity, ArrayList<Album> items) {
			super(activity, 0, items);
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			/*
			final ImageView row;
			final OneHolder<Album> holder;
			
			if (convertView == null) {
				row = new ImageView(mActivity);
				
				holder = new OneHolder<Album>(row, null);
				row.setTag(holder);
				
				CrossFadeDrawable transition = new CrossFadeDrawable(mFallbackBitmap, null);
				transition.setCrossFadeEnabled(true);
				holder.transition = transition;
				holder.defaultCover = R.drawable.icon_album_big;
			} else {
				row = (ImageView)convertView;
				holder = (OneHolder<Album>)convertView.getTag();
			}
			
			final Album album = getItem(position);
			holder.holderItem = album;
			holder.coverItem = album;
			holder.id = album.getCrc();
			
			if (mLoadCovers) {
				row.setImageResource(R.drawable.icon_album_dark_big);
				holder.tempBind = true;
				mMusicManager.getCover(holder.getCoverDownloadHandler(mPostScrollLoader), album, ThumbSize.MEDIUM);
			} else {
				row.setImageResource(R.drawable.icon_album);
			}*/
			return convertView/*row*/;
		}
	}

	public void onActivityPause() {
		if (mMusicManager != null) {
			mMusicManager.setController(null);
			mMusicManager.postActivity();
		}
		if (mControlManager != null) {
			mControlManager.setController(null);
		}
		super.onActivityPause();
	}

	public void onActivityResume(Activity activity) {
		super.onActivityResume(activity);
		if (mMusicManager != null) {
			mMusicManager.setController(this);
		}
		if (mControlManager != null) {
			mControlManager.setController(this);
		}
	}
	
	private static final long serialVersionUID = 1088971882661811256L;
}

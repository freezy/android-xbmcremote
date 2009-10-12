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

package org.xbmc.android.remote.guilogic;

import java.util.ArrayList;

import org.xbmc.android.backend.httpapi.HttpApiHandler;
import org.xbmc.android.backend.httpapi.HttpApiThread;
import org.xbmc.android.remote.R;
import org.xbmc.android.remote.activity.DialogFactory;
import org.xbmc.android.remote.activity.ListActivity;
import org.xbmc.android.remote.drawable.CrossFadeDrawable;
import org.xbmc.android.util.ImportUtilities;
import org.xbmc.android.widget.FastScrollView;
import org.xbmc.android.widget.IdleListDetector;
import org.xbmc.android.widget.ImageLoaderIdleListener;
import org.xbmc.httpapi.data.Album;
import org.xbmc.httpapi.data.Artist;
import org.xbmc.httpapi.data.Genre;
import org.xbmc.httpapi.data.Song;
import org.xbmc.httpapi.type.CacheType;
import org.xbmc.httpapi.type.ThumbSize;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class AlbumListLogic extends ListLogic {
	
	public static final int ITEM_CONTEXT_QUEUE = 1;
	public static final int ITEM_CONTEXT_PLAY = 2;
	public static final int ITEM_CONTEXT_INFO = 3;
	
	private Artist mArtist;
	private Genre mGenre;
	
	private static Bitmap mFallbackBitmap;
	
//	private static final ImageMemCache mCache = new ImageMemCache();
	private IdleListDetector mImageLoader;
	
	public void onCreate(Activity activity, ListView list) {
		if (!isCreated()) {
			super.onCreate(activity, list);
			
			mArtist = (Artist)mActivity.getIntent().getSerializableExtra(ListLogic.EXTRA_ARTIST);
			mGenre = (Genre)mActivity.getIntent().getSerializableExtra(ListLogic.EXTRA_GENRE);
			mActivity.registerForContextMenu(mList);
			
			mFallbackBitmap = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.icon_album_grey);
//			mCache.setFallback(mActivity.getResources(), R.drawable.icon_album_grey);
			
//			ImportUtilities.purgeCache();
			
			mList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent nextActivity;
					AlbumViewHolder holder = (AlbumViewHolder)view.getTag();
					nextActivity = new Intent(view.getContext(), ListActivity.class);
					nextActivity.putExtra(ListLogic.EXTRA_LIST_LOGIC, new SongListLogic());
					nextActivity.putExtra(ListLogic.EXTRA_ALBUM, holder.album);
					mActivity.startActivity(nextActivity);
				}
			});
					
			if (mArtist != null) {
				setTitle(mArtist.name + " - Albums...");
				HttpApiThread.music().getAlbums(new HttpApiHandler<ArrayList<Album>>(mActivity) {
					public void run() {
						setTitle(mArtist.name + " - Albums (" + value.size() + ")");
						//mList.setAdapter(new AlbumAdapter(mActivity, value));
					}
				}, mArtist);
				
			} else if (mGenre != null) {
				setTitle(mGenre.name + " - Albums...");
				HttpApiThread.music().getAlbums(new HttpApiHandler<ArrayList<Album>>(mActivity) {
					public void run() {
						setTitle(mGenre.name + " - Albums (" + value.size() + ")");
						//mList.setAdapter(new AlbumAdapter(mActivity, value));
					}
				}, mGenre);
				
			} else {
				setTitle("Albums...");
				HttpApiThread.music().getAlbums(new HttpApiHandler<ArrayList<Album>>(mActivity) {
					public void run() {
						setTitle("Albums (" + value.size() + ")");
						mList.setAdapter(new AlbumAdapter(mActivity, value));
						
						/* Hook up the mechanism to load images only when the list "slows"
						 * down. */
						ImageLoaderIdleListener idleListener = new ImageLoaderIdleListener(mActivity, mList);
						mImageLoader = new IdleListDetector(idleListener);
						FastScrollView fastScroller = (FastScrollView)mList.getParent();
						fastScroller.setOnIdleListDetector(mImageLoader);
						
					}
				});
			}
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		final AlbumViewHolder holder = (AlbumViewHolder)((AdapterContextMenuInfo)menuInfo).targetView.getTag();
		menu.setHeaderTitle(holder.album.name);
		menu.add(0, ITEM_CONTEXT_QUEUE, 1, "Queue Album");
		menu.add(0, ITEM_CONTEXT_PLAY, 2, "Play Album");
		menu.add(0, ITEM_CONTEXT_INFO, 3, "View Details");
	}
	
	public void onContextItemSelected(MenuItem item) {
		final AlbumViewHolder holder = (AlbumViewHolder)((AdapterContextMenuInfo)item.getMenuInfo()).targetView.getTag();
		switch (item.getItemId()) {
			case ITEM_CONTEXT_QUEUE:
				HttpApiThread.music().addToPlaylist(new HttpApiHandler<Song>(mActivity), holder.album);
				break;
			case ITEM_CONTEXT_PLAY:
				HttpApiThread.music().play(new HttpApiHandler<Boolean>(mActivity), holder.album);
				break;
			case ITEM_CONTEXT_INFO:
				DialogFactory.getAlbumDetail(mActivity, holder.album).show();
				break;
			default:
				return;
		}
	}

	private static class AlbumViewHolder implements ImageLoaderIdleListener.ImageLoaderHolder {
		String crc;
		ImageView iconView;
		TextView titleView;
		TextView subtitleView;
		TextView subsubtitleView;
		int id = 0;
		Album album;
		boolean tempBind;
		CrossFadeDrawable transition;
//		final CharArrayBuffer albumBuffer = new CharArrayBuffer(64);
//		final CharArrayBuffer artistBuffer = new CharArrayBuffer(64);
		public Album getCover() { return album; }
		public String getItemId() { return crc; }
		public boolean isTemporaryBind() { return tempBind; }
		public void setTemporaryBind(boolean temp) { tempBind = temp; }
		public ImageView getImageLoaderView() { return iconView; }
		public CrossFadeDrawable getTransitionDrawable() { return transition; }
	}
	
	private class AlbumAdapter extends ArrayAdapter<Album> {
		private Activity mActivity;
		private final LayoutInflater mInflater;
		AlbumAdapter(Activity activity, ArrayList<Album> items) {
			super(activity, R.layout.listitem_three, items);
			mActivity = activity;
			mInflater = LayoutInflater.from(activity);
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			
			final View row;
			final AlbumViewHolder holder;
			
			if (convertView == null) {
				row = mInflater.inflate(R.layout.listitem_three, null);

				holder = new AlbumViewHolder();
				row.setTag(holder);
				
				holder.titleView = (TextView)row.findViewById(R.id.MusicItemTextViewTitle);
				holder.subtitleView = (TextView)row.findViewById(R.id.MusicItemTextViewSubtitle);
				holder.subsubtitleView = (TextView)row.findViewById(R.id.MusicItemTextViewSubSubtitle);
				holder.iconView = (ImageView)row.findViewById(R.id.MusicItemImageViewArt);

				CrossFadeDrawable transition = new CrossFadeDrawable(mFallbackBitmap, null);
				transition.setCrossFadeEnabled(true);
				holder.transition = transition;
				
			} else {
				row = convertView;
				holder = (AlbumViewHolder)convertView.getTag();
			}
/*			
			final Album album = this.getItem(position);
			holder.album = album;
			
			if (mImageLoader.isListIdle() == true) {
				FastBitmapDrawable d = mCache.fetchFromXbmc2(mActivity, album);
				holder.albumArtwork.setImageDrawable(d);
				holder.setTemporaryBind(false);
			} else {
				holder.albumArtwork.setImageDrawable(mCache.getFallback());
				holder.setTemporaryBind(true);
			}
			
			if (mImageLoader.isListIdle() == true) {
				FastBitmapDrawable d = mCache.fetchFromXbmc2(mActivity, album);
				holder.albumArtwork.setImageDrawable(d);
				holder.setTemporaryBind(false);
			} else {
				holder.albumArtwork.setImageDrawable(mCache.getFallback());
				holder.setTemporaryBind(true);
			}
			
			holder.artistName.setText(album.artist);
			holder.albumName.setText(album.name);*/
			
			
			final Album album = this.getItem(position);
			holder.album = album;
			holder.id = album.getCrc();
//			Log.i("AlbumListLogic", "GOT, VIEW, setting holder.id = " + holder.id);
			
			holder.titleView.setText(album.name);
			holder.subtitleView.setText(album.artist);
			holder.subsubtitleView.setText(album.year > 0 ? String.valueOf(album.year) : "");
			holder.iconView.setImageResource(R.drawable.icon_album_grey);
			holder.setTemporaryBind(true);
			
			HttpApiThread.music().getAlbumCover(new HttpApiHandler<Bitmap>(mActivity, holder.id) {
				public void run() {
//					Log.i("AlbumListLogic", "BACK, tag on iconview = " + calledBackIdg + ", holder.id = " + holder.id);
					if (mTag == holder.id) {
						if (value == null) {
							holder.iconView.setImageResource(R.drawable.icon_album);
						} else {
							// only "fade" if cover was downloaded.
							if (mCacheType.equals(CacheType.network)) {
								CrossFadeDrawable transition = holder.getTransitionDrawable();
								transition.setEnd(value);
								holder.getImageLoaderView().setImageDrawable(transition);
								transition.startTransition(500);
							} else {
								holder.iconView.setImageBitmap(value);
							}
							holder.setTemporaryBind(false);
						}
					} else {
//						Log.i("AlbumListLogic", "*** SKIPPING UPDATE: mTag = " + mTag + ", holder.id = " + holder.id);
					}
				}
				public boolean postCache() {
					if (mImageLoader.isListIdle()) {
//						Log.i("AlbumListLogic", "### LOADING: idleing!");
						return true;
					} else {
//						Log.i("AlbumListLogic", "### SKIPPING: scrolling!");
						return false;
					}
				}
			}, album, ThumbSize.small);
			return row;
		}
	}
	
	private static final long serialVersionUID = 1088971882661811256L;
}

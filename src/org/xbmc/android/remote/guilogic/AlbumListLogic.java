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
import org.xbmc.android.remote.guilogic.holder.ThreeHolder;
import org.xbmc.android.widget.FastScrollView;
import org.xbmc.android.widget.IdleListDetector;
import org.xbmc.android.widget.ImageLoaderIdleListener;
import org.xbmc.httpapi.data.Album;
import org.xbmc.httpapi.data.Artist;
import org.xbmc.httpapi.data.Genre;
import org.xbmc.httpapi.data.Song;
import org.xbmc.httpapi.type.ThumbSize;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
	
	private IdleListDetector mImageLoader;
	
	public void onCreate(Activity activity, ListView list) {
		if (!isCreated()) {
			super.onCreate(activity, list);
			
			mArtist = (Artist)mActivity.getIntent().getSerializableExtra(ListLogic.EXTRA_ARTIST);
			mGenre = (Genre)mActivity.getIntent().getSerializableExtra(ListLogic.EXTRA_GENRE);
			mActivity.registerForContextMenu(mList);
			
			mFallbackBitmap = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.icon_album_grey);
			
//			ImportUtilities.purgeCache();
			
			mList.setOnItemClickListener(new OnItemClickListener() {
				@SuppressWarnings("unchecked")
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent nextActivity;
					ThreeHolder<Album> holder = (ThreeHolder<Album>)view.getTag();
					nextActivity = new Intent(view.getContext(), ListActivity.class);
					nextActivity.putExtra(ListLogic.EXTRA_LIST_LOGIC, new SongListLogic());
					nextActivity.putExtra(ListLogic.EXTRA_ALBUM, holder.getItem());
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
	@SuppressWarnings("unchecked")
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		final ThreeHolder<Album> holder = (ThreeHolder<Album>)((AdapterContextMenuInfo)menuInfo).targetView.getTag();
		menu.setHeaderTitle(holder.getItem().name);
		menu.add(0, ITEM_CONTEXT_QUEUE, 1, "Queue Album");
		menu.add(0, ITEM_CONTEXT_PLAY, 2, "Play Album");
		menu.add(0, ITEM_CONTEXT_INFO, 3, "View Details");
	}
	
	@SuppressWarnings("unchecked")
	public void onContextItemSelected(MenuItem item) {
		final ThreeHolder<Album> holder = (ThreeHolder<Album>)((AdapterContextMenuInfo)item.getMenuInfo()).targetView.getTag();
		final Album album = holder.getItem();
		switch (item.getItemId()) {
			case ITEM_CONTEXT_QUEUE:
				HttpApiThread.music().addToPlaylist(new HttpApiHandler<Song>(mActivity), album);
				break;
			case ITEM_CONTEXT_PLAY:
				HttpApiThread.music().play(new HttpApiHandler<Boolean>(mActivity), album);
				break;
			case ITEM_CONTEXT_INFO:
				DialogFactory.getAlbumDetail(mActivity, album).show();
				break;
			default:
				return;
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
			holder.setItem(album);
			holder.id = album.getCrc();
			
			holder.setText(album.name, album.artist, album.year > 0 ? String.valueOf(album.year) : "");
			holder.setImageResource(R.drawable.icon_album_grey);
			holder.setTemporaryBind(true);
		
			HttpApiThread.music().getAlbumCover(holder.getCoverDownloadHandler(mActivity, mImageLoader), album, ThumbSize.small);
			return row;
		}
	}
	
	private static final long serialVersionUID = 1088971882661811256L;
}

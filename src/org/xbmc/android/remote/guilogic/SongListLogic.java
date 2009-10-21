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
import org.xbmc.android.remote.drawable.CrossFadeDrawable;
import org.xbmc.android.remote.guilogic.holder.ThreeHolder;
import org.xbmc.httpapi.data.Album;
import org.xbmc.httpapi.data.Artist;
import org.xbmc.httpapi.data.Genre;
import org.xbmc.httpapi.data.Song;
import org.xbmc.httpapi.type.ThumbSize;

import android.app.Activity;
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

public class SongListLogic extends ListLogic {
	
	public static final int ITEM_CONTEXT_QUEUE = 1;
	public static final int ITEM_CONTEXT_PLAY = 2;
	public static final int ITEM_CONTEXT_INFO = 3;
	
	private Album mAlbum;
	private Artist mArtist;
	private Genre mGenre;
	
	public void onCreate(Activity activity, ListView list) {
		if (!isCreated()) {
			
			super.onCreate(activity, list);
			
			mAlbum = (Album)mActivity.getIntent().getSerializableExtra(EXTRA_ALBUM);
			mArtist = (Artist)mActivity.getIntent().getSerializableExtra(EXTRA_ARTIST);
			mGenre = (Genre)mActivity.getIntent().getSerializableExtra(EXTRA_GENRE);
			mActivity.registerForContextMenu(mList);
			
			mFallbackBitmap = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.icon_music);
			setupIdleListener();
			
			mList.setOnItemClickListener(new OnItemClickListener() {
				@SuppressWarnings("unchecked")
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final ThreeHolder<Song> holder = (ThreeHolder<Song>)view.getTag();
					HttpApiThread.music().play(new HttpApiHandler<Boolean>((Activity)view.getContext()), holder.getHolderItem());
				}
			});
					
			if (mAlbum != null) {
				setTitle("Songs...");
				HttpApiThread.music().getSongs(new HttpApiHandler<ArrayList<Song>>(mActivity) {
					public void run() {
						setTitle(mAlbum.name);
						mList.setAdapter(new SongAdapter(mActivity, value));
					}
				}, mAlbum);
				
			} else if (mArtist != null) {
				setTitle(mArtist.name + " - Songs...");
				HttpApiThread.music().getSongs(new HttpApiHandler<ArrayList<Song>>(mActivity) {
					public void run() {
						setTitle(mArtist.name + " - Songs (" + value.size() + ")");
						mList.setAdapter(new SongAdapter(mActivity, value));
					}
				}, mArtist);
				
			} else if (mGenre != null) {
				setTitle(mGenre.name + " - Songs...");
				HttpApiThread.music().getSongs(new HttpApiHandler<ArrayList<Song>>(mActivity) {
					public void run() {
						setTitle(mGenre.name + " - Songs (" + value.size() + ")");
						mList.setAdapter(new SongAdapter(mActivity, value));
					}
				}, mGenre);
			}
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// be aware that this must be explicitly called by your activity!
		final ThreeHolder<Song> holder = (ThreeHolder<Song>)((AdapterContextMenuInfo)menuInfo).targetView.getTag();
		menu.setHeaderTitle(holder.getHolderItem().title);
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
					HttpApiThread.music().addToPlaylist(new HttpApiHandler<Boolean>(mActivity), holder.getHolderItem());
				} else {
					HttpApiThread.music().addToPlaylist(new HttpApiHandler<Boolean>(mActivity), mAlbum, holder.getHolderItem());
				}
				break;
			case ITEM_CONTEXT_PLAY:
				if (mAlbum == null) {
					HttpApiThread.music().play(new HttpApiHandler<Boolean>(mActivity), holder.getHolderItem());
				} else {
					HttpApiThread.music().play(new HttpApiHandler<Boolean>(mActivity), mAlbum, holder.getHolderItem());
				}
				break;
			default:
				return;
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
			} else {
				row = convertView;
				holder = (ThreeHolder<Song>)convertView.getTag();
			}
			final Song song = getItem(position);
			Album album = null;
			
			if (mAlbum != null) {
				album = mAlbum;
				holder.setText(song.title, song.artist, song.getDuration());
			} else if (mArtist != null) {
				album = new Album(-1, song.album, song.albumArtist, -1);
				holder.setText(song.title, song.album, song.getDuration());
			} else if (mGenre != null) {
				album = new Album(-1, song.album, song.albumArtist, -1);
				holder.setText(song.title, song.album, song.getDuration());
			}
			holder.setHolderItem(song);
			if (album != null) {
				holder.id = album.getId();
				holder.setCoverItem(album);
				holder.setImageResource(R.drawable.icon_album_grey);
				holder.setTemporaryBind(true);
				HttpApiThread.music().getAlbumCover(holder.getCoverDownloadHandler(mActivity, mPostScrollLoader), album, ThumbSize.small);
			}
			return row;
		}
	}
	
	private static final long serialVersionUID = 755529227668553163L;
}

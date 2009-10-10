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
import org.xbmc.httpapi.data.Album;
import org.xbmc.httpapi.data.Artist;
import org.xbmc.httpapi.data.Genre;
import org.xbmc.httpapi.data.Song;
import org.xbmc.httpapi.type.ThumbSize;

import android.app.Activity;
import android.graphics.Bitmap;
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
			
			mList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Song song = (Song)view.getTag();
					HttpApiThread.music().play(new HttpApiHandler<Boolean>((Activity)view.getContext()), song);
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
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		final Song song = (Song)((AdapterContextMenuInfo)menuInfo).targetView.getTag();
		menu.setHeaderTitle(song.title);
		menu.add(0, ITEM_CONTEXT_QUEUE, 1, "Queue Song");
		menu.add(0, ITEM_CONTEXT_PLAY, 2, "Play Song");
	}
	
	public void onContextItemSelected(MenuItem item) {
		final Song song = (Song)((AdapterContextMenuInfo)item.getMenuInfo()).targetView.getTag();
		HttpApiThread.music().addToPlaylist(new HttpApiHandler<Boolean>(mActivity), song);
		switch (item.getItemId()) {
			case ITEM_CONTEXT_QUEUE:
				HttpApiThread.music().addToPlaylist(new HttpApiHandler<Boolean>(mActivity), song);
				break;
			case ITEM_CONTEXT_PLAY:
				HttpApiThread.music().play(new HttpApiHandler<Boolean>(mActivity), song);
				break;
			default:
				return;
		}
	}
	
	private class SongAdapter extends ArrayAdapter<Song> {
		private Activity mActivity;
		SongAdapter(Activity activity, ArrayList<Song> items) {
			super(activity, R.layout.listitem_three, items);
			mActivity = activity;
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			View row;
			if (convertView == null) {
				LayoutInflater inflater = mActivity.getLayoutInflater();
				row = inflater.inflate(R.layout.listitem_three, null);
			} else {
				row = convertView;
			}
			final Song song = this.getItem(position);
			row.setTag(song);
			final TextView title = (TextView)row.findViewById(R.id.MusicItemTextViewTitle);
			final TextView subtitle = (TextView)row.findViewById(R.id.MusicItemTextViewSubtitle);
			final TextView subsubtitle = (TextView)row.findViewById(R.id.MusicItemTextViewSubSubtitle);
			final ImageView icon = (ImageView)row.findViewById(R.id.MusicItemImageViewArt);
			title.setText(song.title);
//			title.setText(song.track + " " + song.title);
			subsubtitle.setText(song.getDuration());
			
			if (mAlbum != null) {
				subtitle.setText(song.artist);
				HttpApiThread.music().getAlbumCover(new HttpApiHandler<Bitmap>(mActivity, mAlbum) {
					public void run() {
						if (value == null) {
							icon.setImageResource(R.drawable.icon_music);
						} else {
							icon.setImageBitmap(value);
						}
					}
				}, mAlbum, ThumbSize.small);
			} else if (mArtist != null) {
				subtitle.setText(song.album);
				icon.setImageResource(R.drawable.icon_music);
			} else if (mGenre != null) {
				subtitle.setText(song.artist);
				icon.setImageResource(R.drawable.icon_music);
			}
			return row;
		}
	}
	
	private static final long serialVersionUID = 755529227668553163L;
}

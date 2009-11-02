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
import org.xbmc.android.remote.guilogic.holder.OneHolder;
import org.xbmc.httpapi.client.MusicClient;
import org.xbmc.httpapi.data.Song;

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

public class MusicPlaylistLogic extends ListLogic {
	
	public static final int ITEM_CONTEXT_PLAY = 1;
	public static final int ITEM_CONTEXT_REMOVE = 2;
	
	private int mPlaylistPosition = -1;
	
	public void onCreate(final Activity activity, final ListView list) {
		if (!isCreated()) {
			super.onCreate(activity, list);
			
			activity.registerForContextMenu(mList);
			
			mFallbackBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.icon_music);
			setupIdleListener();
			
			HttpApiThread.music().getPlaylistPosition(new HttpApiHandler<Integer>(activity) {
				public void run() {
					MusicPlaylistLogic.this.mPlaylistPosition = value;
				}
			});
			
	  	  	HttpApiThread.music().getPlaylist(new HttpApiHandler<ArrayList<String>>(activity) {
	  	  		public void run() {
	  	  			final ArrayList<PrimitivePlaylistItem> items = new ArrayList<PrimitivePlaylistItem>();
	  	  			int i = 0;
	  	  			for (String path : value) {
	  	  				items.add(new PrimitivePlaylistItem(path, i++));
					}
					setTitle("Music playlist (" + (value.size() > MusicClient.PLAYLIST_LIMIT ? "(" + MusicClient.PLAYLIST_LIMIT + "+" : value.size()) + ")" );
					mList.setAdapter(new SongAdapter(activity, items));
					if (mPlaylistPosition > 0) {
						mList.setSelection(mPlaylistPosition);
					}
	  	  		}
	  	  	});
			
			mList.setOnItemClickListener(new OnItemClickListener() {
				@SuppressWarnings("unchecked")
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final OneHolder<PrimitivePlaylistItem> holder = (OneHolder<PrimitivePlaylistItem>)view.getTag();
					HttpApiThread.music().setPlaylistSong(new HttpApiHandler<Boolean>((Activity)view.getContext()), holder.getHolderItem().position);
				}
			});
					
			mList.setOnKeyListener(new ListLogicOnKeyListener<Song>());
			setTitle("Music playlist...");

		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// be aware that this must be explicitly called by your activity!
		final OneHolder<PrimitivePlaylistItem>holder = (OneHolder<PrimitivePlaylistItem>)((AdapterContextMenuInfo)menuInfo).targetView.getTag();
		menu.setHeaderTitle(holder.getHolderItem().filename);
		menu.add(0, ITEM_CONTEXT_PLAY, 1, "Play");
		menu.add(0, ITEM_CONTEXT_REMOVE, 2, "Remove");
	}
	
	@SuppressWarnings("unchecked")
	public void onContextItemSelected(MenuItem item) {
		// be aware that this must be explicitly called by your activity!
		final OneHolder<PrimitivePlaylistItem> holder = (OneHolder<PrimitivePlaylistItem>)((AdapterContextMenuInfo)item.getMenuInfo()).targetView.getTag();
		
		switch (item.getItemId()) {
			case ITEM_CONTEXT_PLAY:
				HttpApiThread.music().setPlaylistSong(new HttpApiHandler<Boolean>(mActivity), holder.getHolderItem().position);
				break;
			case ITEM_CONTEXT_REMOVE:
				HttpApiThread.music().removeFromPlaylist(new HttpApiHandler<Boolean>(mActivity), holder.getHolderItem().path);
				break;
			default:
				return;
		}
	}
	
	private class SongAdapter extends ArrayAdapter<PrimitivePlaylistItem> {
		private Activity mActivity;
		private final LayoutInflater mInflater;
		SongAdapter(Activity activity, ArrayList<PrimitivePlaylistItem> items) {
			super(activity, R.layout.listitem_oneliner, items);
			mActivity = activity;
			mInflater = LayoutInflater.from(activity);
		}
		@SuppressWarnings("unchecked")
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View row;
			OneHolder<PrimitivePlaylistItem> holder;
			
			if (convertView == null) {
				
				row = mInflater.inflate(R.layout.listitem_oneliner, null);
				holder = new OneHolder<PrimitivePlaylistItem>(
					(ImageView)row.findViewById(R.id.MusicItemImageViewArt),
					(TextView)row.findViewById(R.id.MusicItemTextViewTitle)
				);
				row.setTag(holder);
			} else {
				row = convertView;
				holder = (OneHolder<PrimitivePlaylistItem>)convertView.getTag();
			}
			final PrimitivePlaylistItem item = getItem(position);
			
			holder.setImageResource(item.position == mPlaylistPosition ? R.drawable.icon_play : R.drawable.icon_music);
			holder.setHolderItem(item);
			holder.id = item.position;
			holder.setText(item.filename);
			return row;
		}
	}
	
	private static class PrimitivePlaylistItem {
		public final String path;
		public final String filename;
		public final int position;
		public PrimitivePlaylistItem(String path, int position) {
			this.path = path;
			this.filename = path.substring(path.replaceAll("\\\\", "/").lastIndexOf('/') + 1);
			this.position = position;
		}
	}
	
	private static final long serialVersionUID = 755529227668553163L;
}

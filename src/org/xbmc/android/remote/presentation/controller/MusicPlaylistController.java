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
import java.util.HashMap;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.ManagerThread;
import org.xbmc.android.remote.presentation.activity.PlaylistActivity;
import org.xbmc.android.remote.presentation.controller.holder.OneHolder;
import org.xbmc.api.business.DataResponse;
import org.xbmc.httpapi.client.MusicClient;
import org.xbmc.httpapi.client.ControlClient.ICurrentlyPlaying;
import org.xbmc.httpapi.data.NamedResource;
import org.xbmc.httpapi.data.Song;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

public class MusicPlaylistController extends ListController {
	
	public static final String TAG = "MusicPlaylistLogic";
	
	public static final int ITEM_CONTEXT_PLAY = 1;
	public static final int ITEM_CONTEXT_REMOVE = 2;
	
	private int mCurrentPosition = -1;
	private Handler mHandler;
	private SongAdapter mSongAdapter;
	
	public void onCreate(final Activity activity, final ListView list) {
		if (!isCreated()) {
			super.onCreate(activity, list);
			
			activity.registerForContextMenu(mList);
			
			mFallbackBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.icon_song);
			setupIdleListener();
			
			ManagerThread.music().getPlaylistPosition(new DataResponse<Integer>(activity) {
				public void run() {
					MusicPlaylistController.this.mCurrentPosition = value;
				}
			});
			
	  	  	ManagerThread.music().getPlaylist(new DataResponse<ArrayList<String>>(activity) {
	  	  		public void run() {
	  	  			if (value.size() > 0) {
		  	  			final ArrayList<PlaylistItem> items = new ArrayList<PlaylistItem>();
		  	  			int i = 0;
		  	  			for (String path : value) {
		  	  				items.add(new PlaylistItem(path, i++));
						}
						setTitle("Music playlist (" + (value.size() > MusicClient.PLAYLIST_LIMIT ? MusicClient.PLAYLIST_LIMIT + "+" : value.size()) + ")" );
						mSongAdapter = new SongAdapter(activity, items);
						mList.setAdapter(mSongAdapter);
						if (mCurrentPosition >= 0) {
							mList.setSelection(mCurrentPosition);
						}
					} else {
						setTitle("Music playlist");
						setNoDataMessage("No tracks in playlist.", R.drawable.icon_playlist_dark);
					}

	  	  		}
	  	  	});
			
			mList.setOnItemClickListener(new OnItemClickListener() {
				@SuppressWarnings("unchecked")
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final OneHolder<PlaylistItem> holder = (OneHolder<PlaylistItem>)view.getTag();
					ManagerThread.music().setPlaylistSong(new DataResponse<Boolean>((Activity)view.getContext()), holder.holderItem.position);
				}
			});
					
			mList.setOnKeyListener(new ListControllerOnKeyListener<Song>());
			setTitle("Music playlist...");

		}
	}
	
	public void subscribe(Handler handler) {
		mHandler = handler;
	}
	
	public void onTrackChanged(ICurrentlyPlaying newSong) {
		final SongAdapter adapter = mSongAdapter;
		if (adapter != null) {
			final int currentPos = mCurrentPosition;
			int newPos = newSong.getPlaylistPosition();
			OneHolder<PlaylistItem> holder = adapter.getHolderAtPosition(currentPos);
			if (currentPos >= 0 && holder != null) {
				holder.iconView.setImageResource(R.drawable.icon_song_light);
			} else {
				Log.i(TAG, "NOT resetting previous icon at position " + currentPos);
			}
			holder = adapter.getHolderAtPosition(newPos);
			mCurrentPosition = newPos;
			if (holder != null) {
				holder.iconView.setImageResource(R.drawable.icon_play);
			} else {
				mList.setSelection(newPos);
			}
			Log.i(TAG, "New playing position is at " + newPos);
		}
	}
	
	@Override
//	@SuppressWarnings("unchecked")
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// be aware that this must be explicitly called by your activity!
/*		final OneHolder<PlaylistItem>holder = (OneHolder<PlaylistItem>)((AdapterContextMenuInfo)menuInfo).targetView.getTag();
		menu.setHeaderTitle(holder.holderItem.filename);
		menu.add(0, ITEM_CONTEXT_PLAY, 1, "Play");
		menu.add(0, ITEM_CONTEXT_REMOVE, 2, "Remove");*/
	}
	
	@SuppressWarnings("unchecked")
	public void onContextItemSelected(MenuItem item) {
		// be aware that this must be explicitly called by your activity!
		final OneHolder<PlaylistItem> holder = (OneHolder<PlaylistItem>)((AdapterContextMenuInfo)item.getMenuInfo()).targetView.getTag();
		
		switch (item.getItemId()) {
			case ITEM_CONTEXT_PLAY:
				ManagerThread.music().setPlaylistSong(new DataResponse<Boolean>(mActivity), holder.holderItem.position);
				break;
			case ITEM_CONTEXT_REMOVE:
				ManagerThread.music().removeFromPlaylist(new DataResponse<Boolean>(mActivity), holder.holderItem.path);
				break;
			default:
				return;
		}
	}
	
	private class SongAdapter extends ArrayAdapter<PlaylistItem> {
		private final LayoutInflater mInflater;
		private final HashMap<Integer, OneHolder<PlaylistItem>> mItemPositions = new HashMap<Integer, OneHolder<PlaylistItem>>();
		SongAdapter(Activity activity, ArrayList<PlaylistItem> items) {
			super(activity, R.layout.listitem_oneliner, items);
			mInflater = LayoutInflater.from(activity);
			Handler handler = mHandler;
			if (handler != null) {
				Message msg = Message.obtain();
	  	  		Bundle bundle = msg.getData();
	  	  		bundle.putInt(PlaylistActivity.BUNDLE_PLAYLIST_SIZE, items.size());
	  	  		msg.what = PlaylistActivity.MESSAGE_PLAYLIST_SIZE;	
	  	  		handler.sendMessage(msg);
			}
		}
		
		@SuppressWarnings("unchecked")
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View row;
			OneHolder<PlaylistItem> holder;
			final PlaylistItem item = getItem(position);
			
			if (convertView == null) {
				row = mInflater.inflate(R.layout.listitem_oneliner, null);
				holder = new OneHolder<PlaylistItem>(
					(ImageView)row.findViewById(R.id.MusicItemImageViewArt),
					(TextView)row.findViewById(R.id.MusicItemTextViewTitle)
				);
				holder.defaultCover = R.drawable.icon_song_light;
				row.setTag(holder);
			} else {
				row = convertView;
				holder = (OneHolder<PlaylistItem>)convertView.getTag();
				mItemPositions.remove(holder.holderItem.position);
			}
			mItemPositions.put(item.position, holder);
			
			holder.iconView.setImageResource(item.position == mCurrentPosition ? R.drawable.icon_play : R.drawable.icon_song_light);
			holder.holderItem = item;
			holder.id = item.position;
			holder.titleView.setText(item.filename);
			return row;
		}
		
		public OneHolder<PlaylistItem> getHolderAtPosition(int position) {
			if (mItemPositions.containsKey(position)) {
				return mItemPositions.get(position);
			}
			return null;
		}
	}
	
	private static class PlaylistItem implements NamedResource{
		public final String path;
		public final String filename;
		public final int position;
		public PlaylistItem(String path, int position) {
			this.path = path;
			this.filename = path.substring(path.replaceAll("\\\\", "/").lastIndexOf('/') + 1);
			this.position = position;
		}
		public String getShortName() {
			return filename;
		}
	}
	
	private static final long serialVersionUID = 755529227668553163L;
}

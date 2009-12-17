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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.business.NowPlayingPollerThread;
import org.xbmc.android.remote.presentation.activity.PlaylistActivity;
import org.xbmc.android.remote.presentation.controller.holder.OneHolder;
import org.xbmc.android.util.ConnectionFactory;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.business.IMusicManager;
import org.xbmc.api.data.IControlClient.ICurrentlyPlaying;
import org.xbmc.api.info.PlayStatus;
import org.xbmc.api.object.INamedResource;
import org.xbmc.api.object.Song;
import org.xbmc.eventclient.ButtonCodes;
import org.xbmc.httpapi.client.MusicClient;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class MusicPlaylistController extends ListController implements IController, Callback {
	
	public static final String TAG = "MusicPlaylistLogic";
	
	public static final int ITEM_CONTEXT_PLAY = 1;
	public static final int ITEM_CONTEXT_REMOVE = 2;
	
	public static final int MESSAGE_PLAYLIST_SIZE = 701;
	public static final String BUNDLE_PLAYLIST_SIZE = "playlist_size";
	
	private PlaylistActivity mPlaylistActivity;
	private Handler mNowPlayingHandler;
	private SongAdapter mSongAdapter;
	
	private IControlManager mControlManager;
	private IMusicManager mMusicManager;
	private IEventClientManager mEventClient;
	
	private int mPlayStatus = PlayStatus.UNKNOWN;
	private int mPlayListId = -1;
	private int mCurrentPosition = -1;
	private int mLastPosition = -1;
	
	public void onCreate(final PlaylistActivity activity, final ListView list) {
		
		mPlaylistActivity = activity;
		mMusicManager = ManagerFactory.getMusicManager(this);
		mControlManager = ManagerFactory.getControlManager(this);
		mEventClient = ManagerFactory.getEventClientManager(this);
		mNowPlayingHandler = new Handler(this);
		
		if (!isCreated()) {
			super.onCreate(activity, list);
			
			activity.registerForContextMenu(mList);
			
			mFallbackBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.icon_song);
			setupIdleListener();
			
			mMusicManager.getPlaylistPosition(new DataResponse<Integer>() {
				public void run() {
					mCurrentPosition = value;
				}
			});
			
			mMusicManager.getPlaylist(new DataResponse<ArrayList<String>>() {
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
					final DataResponse<Boolean> doNothing = new DataResponse<Boolean>();
					mControlManager.setPlaylistId(doNothing, mPlayListId < 0 ? 0 : mPlayListId);
					mMusicManager.setPlaylistSong(doNothing, holder.holderItem.position);
				}
			});
					
			mList.setOnKeyListener(new ListControllerOnKeyListener<Song>());
			setTitle("Music playlist...");

		}
	}

	/**
	 * This is called from the thread with a message containing updated info of
	 * what's currently playing.
	 * 
	 * @param msg
	 *            Message object containing currently playing info
	 */
	public synchronized boolean handleMessage(Message msg) {
		final Bundle data = msg.getData();
		final ICurrentlyPlaying currentlyPlaying = (ICurrentlyPlaying) data.getSerializable(NowPlayingPollerThread.BUNDLE_CURRENTLY_PLAYING);
		switch (msg.what) {
		case NowPlayingPollerThread.MESSAGE_PROGRESS_CHANGED:
			mPlayStatus = currentlyPlaying.getPlayStatus();
			if (currentlyPlaying.isPlaying()) {
				mPlaylistActivity.setTime(Song.getDuration(currentlyPlaying.getTime() + 1));
			} else {
				mPlaylistActivity.clear();
			}
			return true;

		case NowPlayingPollerThread.MESSAGE_TRACK_CHANGED:
			mLastPosition = data.getInt(NowPlayingPollerThread.BUNDLE_LAST_PLAYPOSITION);
			onTrackChanged(currentlyPlaying);
			return true;
			
		case NowPlayingPollerThread.MESSAGE_PLAYSTATE_CHANGED:
			mPlayListId = data.getInt(NowPlayingPollerThread.BUNDLE_LAST_PLAYLIST);
			return true;
			
		case MESSAGE_PLAYLIST_SIZE:
			final int size = msg.getData().getInt(BUNDLE_PLAYLIST_SIZE);
			mPlaylistActivity.setNumTracks(size == 0 ? "empty" : size + " tracks");
			return true;
			
		case NowPlayingPollerThread.MESSAGE_CONNECTION_ERROR:
		case NowPlayingPollerThread.MESSAGE_RECONFIGURE:
			mPlayStatus = PlayStatus.UNKNOWN;
			return true;
			
		default:
			return false;
		}
	}
	
	public void setupButtons(View prev, View stop, View playpause, View next) {

		// setup buttons
		prev.setOnClickListener(new OnRemoteAction(ButtonCodes.REMOTE_SKIP_MINUS));
		stop.setOnClickListener(new OnRemoteAction(ButtonCodes.REMOTE_STOP));
		next.setOnClickListener(new OnRemoteAction(ButtonCodes.REMOTE_SKIP_PLUS));
		playpause.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					switch (mPlayStatus) {
						case PlayStatus.PLAYING:
							mEventClient.sendButton("R1", ButtonCodes.REMOTE_PAUSE, false, true, true, (short)0, (byte)0);
							break;
						case PlayStatus.PAUSED:
							mEventClient.sendButton("R1", ButtonCodes.REMOTE_PLAY, false, true, true, (short)0, (byte)0);
							break;
						case PlayStatus.STOPPED:
							final DataResponse<Boolean> doNothing = new DataResponse<Boolean>();
							mControlManager.setPlaylistId(doNothing, mPlayListId < 0 ? 0 : mPlayListId);
							mControlManager.setPlaylistPos(doNothing, mLastPosition < 0 ? 0 : mLastPosition);
							break;
					}
				} catch (IOException e) { }
			}
		});
	}
	

	/**
	 * Handles the push- release button code. Switches image of the pressed
	 * button, vibrates and executes command.
	 */
	private class OnRemoteAction implements OnClickListener {
		private final String mAction;

		public OnRemoteAction(String action) {
			mAction = action;
		}

		public void onClick(View v) {
			try {
				mEventClient.sendButton("R1", mAction, false, true, true, (short) 0, (byte) 0);
			} catch (IOException e) {
			}
		}
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
				mMusicManager.setPlaylistSong(new DataResponse<Boolean>(), holder.holderItem.position);
				break;
			case ITEM_CONTEXT_REMOVE:
				mMusicManager.removeFromPlaylist(new DataResponse<Boolean>(), holder.holderItem.path);
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
			Handler handler = mNowPlayingHandler;
			if (handler != null) {
				Message msg = Message.obtain();
	  	  		Bundle bundle = msg.getData();
	  	  		bundle.putInt(BUNDLE_PLAYLIST_SIZE, items.size());
	  	  		msg.what = MESSAGE_PLAYLIST_SIZE;	
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
	
	private static class PlaylistItem implements INamedResource{
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
	
	public void onActivityPause() {
		ConnectionFactory.getNowPlayingPoller(mActivity.getApplicationContext()).unSubscribe(mNowPlayingHandler);
		if (mMusicManager != null) {
			mMusicManager.setController(null);
		}
		if (mControlManager != null) {
			mControlManager.setController(null);
		}
		if (mEventClient != null) {
			mEventClient.setController(null);
		}
		super.onActivityPause();
	}

	public void onActivityResume(Activity activity) {
		super.onActivityResume(activity);
		ConnectionFactory.getNowPlayingPoller(activity.getApplicationContext()).subscribe(mNowPlayingHandler);
		if (mEventClient != null) {
			mEventClient.setController(this);
		}
		if (mMusicManager != null) {
			mMusicManager.setController(this);
		}
		if (mControlManager != null) {
			mControlManager.setController(this);
		}
	}
	
	private static final long serialVersionUID = 755529227668553163L;
}

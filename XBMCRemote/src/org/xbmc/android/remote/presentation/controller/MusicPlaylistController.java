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
import org.xbmc.android.remote.presentation.widget.OneLabelItemView;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
	
	private static Bitmap sPlayingBitmap;
	
	public void onCreate(final PlaylistActivity activity, Handler handler, final AbsListView list) {
		
		mPlaylistActivity = activity;
		mMusicManager = ManagerFactory.getMusicManager(this);
		mControlManager = ManagerFactory.getControlManager(this);
		mEventClient = ManagerFactory.getEventClientManager(this);
		mNowPlayingHandler = new Handler(this);
		
		if (!isCreated()) {
			super.onCreate(activity, handler, list);
			
			activity.registerForContextMenu(mList);
			
			mFallbackBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.icon_song_light);
			sPlayingBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.icon_play);
			
			mMusicManager.getPlaylistPosition(new DataResponse<Integer>() {
				public void run() {
					mCurrentPosition = value;
				}
			}, mActivity.getApplicationContext());
			
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
	  	  	}, mActivity.getApplicationContext());
			mList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final PlaylistItem item = (PlaylistItem)mList.getAdapter().getItem(((OneLabelItemView)view).position);
					final DataResponse<Boolean> doNothing = new DataResponse<Boolean>();
					mControlManager.setPlaylistId(doNothing, mPlayListId < 0 ? 0 : mPlayListId, mActivity.getApplicationContext());
					mMusicManager.setPlaylistSong(doNothing, item.position, mActivity.getApplicationContext());
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

		case NowPlayingPollerThread.MESSAGE_PLAYLIST_ITEM_CHANGED:
			mLastPosition = data.getInt(NowPlayingPollerThread.BUNDLE_LAST_PLAYPOSITION);
			onTrackChanged(currentlyPlaying);
			return true;
			
		case NowPlayingPollerThread.MESSAGE_PLAYSTATE_CHANGED:
			mPlayListId = data.getInt(NowPlayingPollerThread.BUNDLE_LAST_PLAYLIST);
			return true;
			
		case MESSAGE_PLAYLIST_SIZE:
			final int size = msg.getData().getInt(BUNDLE_PLAYLIST_SIZE);
			mPlaylistActivity.setNumItems(size == 0 ? "empty" : size + " tracks");
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
							mControlManager.setPlaylistId(doNothing, mPlayListId < 0 ? 0 : mPlayListId, mActivity.getApplicationContext());
							mControlManager.setPlaylistPos(doNothing, mLastPosition < 0 ? 0 : mLastPosition, mActivity.getApplicationContext());
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
			final int newPos = newSong.getPlaylistPosition();
			
			// clear previous song's icon
			OneLabelItemView view = adapter.getViewAtPosition(currentPos);
			if (currentPos >= 0 && view != null) {
				view.setCover(BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.icon_song_light));
				Log.i(TAG, "Resetting previous icon at position " + currentPos + " (" + view.title + ")");
			} else {
				Log.i(TAG, "NOT resetting previous icon at position " + currentPos);
			}
			
			// set new song's play icon
			view = adapter.getViewAtPosition(newPos);
			mCurrentPosition = newPos;
			if (view != null) {
				view.setCover(BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.icon_play));
			} else {
				mList.setSelection(newPos);
			}
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// be aware that this must be explicitly called by your activity!
/*		final OneHolder<PlaylistItem>holder = (OneHolder<PlaylistItem>)((AdapterContextMenuInfo)menuInfo).targetView.getTag();
		menu.setHeaderTitle(holder.holderItem.filename);
		menu.add(0, ITEM_CONTEXT_PLAY, 1, "Play");
		menu.add(0, ITEM_CONTEXT_REMOVE, 2, "Remove");*/
	}
	
	public void onContextItemSelected(MenuItem item) {
		// be aware that this must be explicitly called by your activity!
		final PlaylistItem playlistItem = (PlaylistItem)mList.getAdapter().getItem(((OneLabelItemView)((AdapterContextMenuInfo)item.getMenuInfo()).targetView).position);
		switch (item.getItemId()) {
			case ITEM_CONTEXT_PLAY:
				mMusicManager.setPlaylistSong(new DataResponse<Boolean>(), playlistItem.position, mActivity.getApplicationContext());
				break;
			case ITEM_CONTEXT_REMOVE:
				mMusicManager.removeFromPlaylist(new DataResponse<Boolean>(), playlistItem.path, mActivity.getApplicationContext());
				break;
			default:
				return;
		}
	}
	
	private class SongAdapter extends ArrayAdapter<PlaylistItem> {
		private final HashMap<Integer, OneLabelItemView> mItemPositions = new HashMap<Integer, OneLabelItemView>();
		SongAdapter(Activity activity, ArrayList<PlaylistItem> items) {
			super(activity, 0, items);
			Handler handler = mNowPlayingHandler;
			if (handler != null) {
				Message msg = Message.obtain();
	  	  		Bundle bundle = msg.getData();
	  	  		bundle.putInt(BUNDLE_PLAYLIST_SIZE, items.size());
	  	  		msg.what = MESSAGE_PLAYLIST_SIZE;	
	  	  		handler.sendMessage(msg);
			}
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			final OneLabelItemView view;
			if (convertView == null) {
				view = new OneLabelItemView(mActivity, parent.getWidth(), mFallbackBitmap, mList.getSelector(), true);
			} else {
				view = (OneLabelItemView)convertView;
				mItemPositions.remove(view.position);
			}
			final PlaylistItem item = this.getItem(position);
			view.reset();
			view.position = position;
			view.title = item.filename;
			if (position == mCurrentPosition) {
				view.setCover(sPlayingBitmap);
			} else {
				view.setCover(mFallbackBitmap);
			}
			mItemPositions.put(view.position, view);
			return view;
		}
		public OneLabelItemView getViewAtPosition(int position) {
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
			mMusicManager.postActivity();
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

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
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.activity.ListActivity;
import org.xbmc.android.remote.presentation.activity.NowPlayingActivity;
import org.xbmc.android.remote.presentation.widget.OneLabelItemView;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.IInfoManager;
import org.xbmc.api.info.FileTypes;
import org.xbmc.api.object.FileLocation;
import org.xbmc.api.type.MediaType;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class FileListController extends ListController implements IController {
	
	public static final int MESSAGE_HANDLE_DATA = 1;
	public static final int MESSAGE_CONNECTION_ERROR = 2;
	private static final int ITEM_CONTEXT_QUEUE = 0;
	private static final int ITEM_CONTEXT_PLAY = 1;
	
	private HashMap<String, FileLocation> mFileItems;
	private volatile String mGettingUrl;
	private int mMediaType = MediaType.UNKNOWN;
	
	// from ListActivity.java
	protected ListAdapter mAdapter;
	
	private IInfoManager mInfoManager;
	private IControlManager mControlManager;
	
	public FileListController() {}
	
	public FileListController(int mediaType) {
		mMediaType = mediaType;
	}
	
	public void onCreate(Activity activity, Handler handler, AbsListView list) {

		mInfoManager = ManagerFactory.getInfoManager(this);
		mControlManager = ManagerFactory.getControlManager(this);
		
		if (!isCreated()) {
			super.onCreate(activity, handler, list);
			
			if (mMediaType == MediaType.UNKNOWN) {
				mMediaType = mActivity.getIntent().getIntExtra(EXTRA_SHARE_TYPE, MediaType.MUSIC);
			}
			mFallbackBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.icon_file);
			
			final String path = mActivity.getIntent().getStringExtra(EXTRA_PATH);
			final String displayPath = mActivity.getIntent().getStringExtra(EXTRA_DISPLAY_PATH);
			fillUp(path == null ? "" : path, displayPath);
	
			activity.registerForContextMenu(mList);
			mList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if(isLoading()) return;
					if (mFileItems == null)
						return;
	
					FileLocation item = mFileItems.get(((FileLocation)parent.getAdapter().getItem(position)).name);
					if (item.isDirectory) {
						Intent nextActivity = new Intent(mActivity, ListActivity.class);
						nextActivity.putExtra(ListController.EXTRA_LIST_CONTROLLER, new FileListController());
						nextActivity.putExtra(ListController.EXTRA_SHARE_TYPE, mMediaType);
						nextActivity.putExtra(ListController.EXTRA_PATH, item.path);
						nextActivity.putExtra(ListController.EXTRA_DISPLAY_PATH, item.displayPath);
						mActivity.startActivity(nextActivity);
					} else {
						switch(item.mediaType) {
							case 0:
								break;
							
							case MediaType.PICTURES:
								mControlManager.showPicture(new DataResponse<Boolean>() {
									public void run() {
										if (value) {
											mActivity.startActivity(new Intent(mActivity, NowPlayingActivity.class));
										}
									}
								}, item.path, mActivity.getApplicationContext());
								break;
							default:
								mControlManager.playFile(new DataResponse<Boolean>() {
									public void run() {
										if (value) {
											mActivity.startActivity(new Intent(mActivity, NowPlayingActivity.class));
										}
									}
								}, item.path, mActivity.getApplicationContext());
						}
					}
				}
			});
			mList.setOnKeyListener(new ListControllerOnKeyListener<FileLocation>());
		}
	}
	
	private class FileItemAdapter extends ArrayAdapter<FileLocation> {
		FileItemAdapter(Activity activity, ArrayList<FileLocation> items) {
			super(activity, 0, items);
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			
			final OneLabelItemView view;
			if (convertView == null) {
				view = new OneLabelItemView(mActivity, parent.getWidth(), mFallbackBitmap, mList.getSelector(), true);
			} else {
				view = (OneLabelItemView)convertView;
			}
			final FileLocation fileItem = this.getItem(position);
			view.reset();
			view.position = position;
			view.title = fileItem.name;
			final Resources res = mActivity.getResources();
			if (fileItem.isArchive) {
				view.setCover(BitmapFactory.decodeResource(res, R.drawable.icon_zip));
				view.setCover(BitmapFactory.decodeResource(res, R.drawable.icon_zip));
			} else if (fileItem.isDirectory) {
				if (fileItem.path.startsWith("shout://")) {
					view.setCover(BitmapFactory.decodeResource(res, R.drawable.icon_shoutcast));
				} else if (fileItem.path.startsWith("lastfm://")) {
					view.setCover(BitmapFactory.decodeResource(res, R.drawable.icon_lastfm));
				} else {
					view.setCover(BitmapFactory.decodeResource(res, R.drawable.icon_folder));
				}
			} else {
				final String ext = FileTypes.getExtension(fileItem.path);
				if (FileTypes.isAudio(ext)) {
					view.setCover(BitmapFactory.decodeResource(res, R.drawable.icon_song));
				} else if (FileTypes.isVideo(ext)) {
					view.setCover(BitmapFactory.decodeResource(res, R.drawable.icon_video));
				} else if (FileTypes.isPicture(ext)) {
					view.setCover(BitmapFactory.decodeResource(res, R.drawable.icon_picture));
				} else if (FileTypes.isPlaylist(ext)) {
					view.setCover(BitmapFactory.decodeResource(res, R.drawable.icon_playing));
				} else {
					view.setCover(BitmapFactory.decodeResource(res, R.drawable.icon_file));
				}
			}
			return view;
		}
	}
	
	
	private void fillUp(final String url, final String displayPath) {
		if (mGettingUrl != null)
			return;
		
		mGettingUrl = url;
		mFileItems = null;
		mList.setTextFilterEnabled(false);
		setTitle("Loading...");
		showOnLoading();
		DataResponse<ArrayList<FileLocation>> mediaListHandler = new DataResponse<ArrayList<FileLocation>>() {
			public void run() {
				setTitle(url.equals("") ? "/" : displayPath);
				if (value.size() > 0) {
					mFileItems = new HashMap<String, FileLocation>();
					for (FileLocation item : value) {
						mFileItems.put(item.name, item);
					}
					setListAdapter(new FileItemAdapter(mActivity, value));
				} else {
					setNoDataMessage("No files found.", R.drawable.icon_folder_dark);
				}
			}
		};
		
		if (mGettingUrl.length() == 0) {
			mInfoManager.getShares(mediaListHandler, mMediaType, mActivity.getApplicationContext());
		} else {
			mInfoManager.getDirectory(mediaListHandler, mGettingUrl, mActivity.getApplicationContext(), mMediaType);
		}
	}
	
    /**
     * Provide the cursor for the list view.
     */
    public void setListAdapter(ListAdapter adapter) {
        synchronized (this) {
            mAdapter = adapter;
            mList.setAdapter(adapter);
        }
    }

	@Override
	public void onContextItemSelected(MenuItem item) {
		// be aware that this must be explicitly called by your activity!
		final FileLocation loc = (FileLocation) mList.getAdapter().getItem(((OneLabelItemView)((AdapterContextMenuInfo)item.getMenuInfo()).targetView).position);
		switch(item.getItemId()) {
		case ITEM_CONTEXT_QUEUE:
			mControlManager.queueFolder(new QueryResponse(mActivity, "Queueing folder " + loc.path, "Error queueing folder."), loc.path, MediaType.getPlaylistType(mMediaType), mActivity);
			break;
		case ITEM_CONTEXT_PLAY:
			mControlManager.playFolder(new QueryResponse(mActivity, "Playing folder " + loc.path, "Error playint folder."), loc.path, MediaType.getPlaylistType(mMediaType), mActivity);
			break;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// be aware that this must be explicitly called by your activity!
		Log.d("FileListController", "Create Context Menu");
		final OneLabelItemView view = (OneLabelItemView)((AdapterContextMenuInfo)menuInfo).targetView;
		menu.setHeaderTitle(((FileLocation)mList.getItemAtPosition(view.getPosition())).name);
		menu.add(0, ITEM_CONTEXT_QUEUE, 1, "Queue Folder");
		menu.add(0, ITEM_CONTEXT_PLAY, 2, "Play Folder");
	}
	
	public void onActivityPause() {
		if (mInfoManager != null) {
			mInfoManager.setController(null);
		}
		if (mControlManager != null) {
			mControlManager.setController(null);
		}
		super.onActivityPause();
	}

	public void onActivityResume(Activity activity) {
		if (mInfoManager != null) {
			mInfoManager.setController(this);
		}
		if (mControlManager != null) {
			mControlManager.setController(this);
		}
		super.onActivityResume(activity);
	}
	
	private static final long serialVersionUID = -3883887349523448733L;
}

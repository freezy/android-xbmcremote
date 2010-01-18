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
import org.xbmc.api.object.FileLocation;
import org.xbmc.api.type.MediaType;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class FileListController extends ListController implements IController {
	
	public static final int MESSAGE_HANDLE_DATA = 1;
	public static final int MESSAGE_CONNECTION_ERROR = 2;
	
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
	
	public void onCreate(Activity activity, ListView list) {
		
		mInfoManager = ManagerFactory.getInfoManager(this);
		mControlManager = ManagerFactory.getControlManager(this);
		
		if (!isCreated()) {
			super.onCreate(activity, list);
			
			if (mMediaType == MediaType.UNKNOWN) {
				mMediaType = mActivity.getIntent().getIntExtra(EXTRA_SHARE_TYPE, MediaType.MUSIC);
			}
			
			final String path = mActivity.getIntent().getStringExtra(EXTRA_PATH);
			final String displayPath = mActivity.getIntent().getStringExtra(EXTRA_DISPLAY_PATH);
			fillUp(path == null ? "" : path, displayPath);
	
			mList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
								}, item.path);
								break;
								
							default:
								mControlManager.playFile(new DataResponse<Boolean>() {
									public void run() {
										if (value) {
											mActivity.startActivity(new Intent(mActivity, NowPlayingActivity.class));
										}
									}
								}, item.path);									
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
				view = new OneLabelItemView(mActivity, R.drawable.icon_artist);
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
				view.setCover(BitmapFactory.decodeResource(res, R.drawable.icon_folder));
			} else {
				final String ext = fileItem.name.substring(fileItem.name.lastIndexOf(".") + 1).toLowerCase();
				if (fileItem.mediaType == MediaType.MUSIC) {
					view.setCover(BitmapFactory.decodeResource(res, R.drawable.icon_song));
				} else if (fileItem.mediaType == MediaType.MUSIC) {
					view.setCover(BitmapFactory.decodeResource(res, R.drawable.icon_video));
				} else if (fileItem.mediaType == MediaType.PICTURES) {
					view.setCover(BitmapFactory.decodeResource(res, R.drawable.icon_picture));
				} else if (ext.equals("m3u")) {
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
			mInfoManager.getShares(mediaListHandler, mMediaType);
		} else {
			mInfoManager.getDirectory(mediaListHandler, mGettingUrl);
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
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// be aware that this must be explicitly called by your activity!
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

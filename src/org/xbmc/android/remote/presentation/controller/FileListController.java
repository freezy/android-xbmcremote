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
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.IInfoManager;
import org.xbmc.api.object.FileLocation;
import org.xbmc.api.type.MediaType;

import android.app.Activity;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
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
		
		mInfoManager = ManagerFactory.getInfoManager(activity.getApplicationContext(), this);
		mControlManager = ManagerFactory.getControlManager(activity.getApplicationContext(), this);
		
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
						nextActivity.putExtra(ListController.EXTRA_LIST_LOGIC, new FileListController());
						nextActivity.putExtra(ListController.EXTRA_SHARE_TYPE, mMediaType);
						nextActivity.putExtra(ListController.EXTRA_PATH, item.path);
						nextActivity.putExtra(ListController.EXTRA_DISPLAY_PATH, item.displayPath);
						mActivity.startActivity(nextActivity);
					} else {
						mControlManager.playFile(new DataResponse<Boolean>() {
							public void run() {
								if (value) {
									mActivity.startActivity(new Intent(mActivity, NowPlayingActivity.class));
								}
							}
						}, item.path);
					}
				}
			});
			mList.setOnKeyListener(new ListControllerOnKeyListener<FileLocation>());
		}
	}
	
	private class FileItemAdapter extends ArrayAdapter<FileLocation> {
		private final LayoutInflater mInflater; 
		FileItemAdapter(Activity activity, ArrayList<FileLocation> items) {
			super(activity, R.layout.listitem_oneliner, items);
			mInflater = activity.getLayoutInflater();
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			View row;
			if (convertView == null) {
				row = mInflater.inflate(R.layout.listitem_oneliner, null);
			} else {
				row = convertView;
			}
			final FileLocation fileItem = this.getItem(position);
			row.setTag(fileItem);
			final TextView title = (TextView)row.findViewById(R.id.MusicItemTextViewTitle);
			final ImageView icon = (ImageView)row.findViewById(R.id.MusicItemImageViewArt);
			title.setText(fileItem.name);
			if (fileItem.isArchive) {
				icon.setImageResource(R.drawable.icon_zip);
			} else if (fileItem.isDirectory) {
				icon.setImageResource(R.drawable.icon_folder);
			} else {
				final String ext = fileItem.name.substring(fileItem.name.lastIndexOf(".") + 1).toLowerCase();
				if (ext.equals("mp3") || ext.equals("ogg")) {
					icon.setImageResource(R.drawable.icon_song);
				} else if (ext.equals("avi") || ext.equals("mov") || ext.equals("flv") || ext.equals("mkv") || ext.equals("wmv") || ext.equals("mp4")) {
					icon.setImageResource(R.drawable.icon_video);
				} else if (ext.equals("jpg") || ext.equals("jpeg") || ext.equals("bmp") || ext.equals("gif") || ext.equals("png") || ext.equals("tbn")) {
					icon.setImageResource(R.drawable.icon_picture);
				} else if (ext.equals("m3u")) {
					icon.setImageResource(R.drawable.icon_playing);
				} else {
					icon.setImageResource(R.drawable.icon_file);
				}
			}
			return row;
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
	}

	public void onActivityResume(Activity activity) {
		if (mInfoManager != null) {
			mInfoManager.setController(this);
		}
		if (mControlManager != null) {
			mControlManager.setController(this);
		}
	}
	
	private static final long serialVersionUID = -3883887349523448733L;
}

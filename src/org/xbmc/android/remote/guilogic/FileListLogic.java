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
import java.util.HashMap;

import org.xbmc.android.backend.httpapi.HttpApiHandler;
import org.xbmc.android.backend.httpapi.HttpApiThread;
import org.xbmc.android.remote.R;
import org.xbmc.android.remote.activity.ListActivity;
import org.xbmc.android.remote.activity.NowPlayingActivity;
import org.xbmc.httpapi.data.MediaLocation;
import org.xbmc.httpapi.type.MediaType;

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

public class FileListLogic extends ListLogic {
	
	public static final int MESSAGE_HANDLE_DATA = 1;
	public static final int MESSAGE_CONNECTION_ERROR = 2;
	
	private HashMap<String, MediaLocation> mFileItems;
	private volatile String mGettingUrl;
	private MediaType mMediaType;
	
	// from ListActivity.java
	protected ListAdapter mAdapter;
	
	public void onCreate(Activity activity, ListView list) {
		if (!isCreated()) {
			super.onCreate(activity, list);
			
			final String st = mActivity.getIntent().getStringExtra(EXTRA_SHARE_TYPE);
			mMediaType = st != null ? MediaType.valueOf(st) : MediaType.music;
			final String path = mActivity.getIntent().getStringExtra(EXTRA_PATH);
			fillUp(path == null ? "" : path);
	
			mList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if (mFileItems == null)
						return;
	
					MediaLocation item = mFileItems.get(((MediaLocation)parent.getAdapter().getItem(position)).name);
					if (item.isDirectory) {
						Intent nextActivity = new Intent(mActivity, ListActivity.class);
						nextActivity.putExtra(ListLogic.EXTRA_LIST_LOGIC, new FileListLogic());
						nextActivity.putExtra(ListLogic.EXTRA_SHARE_TYPE, mMediaType.toString());
						nextActivity.putExtra(ListLogic.EXTRA_PATH, item.path);
						mActivity.startActivity(nextActivity);
					} else {
						HttpApiThread.control().playFile(new HttpApiHandler<Boolean>(mActivity) {
							public void run() {
								if (value) {
									mActivity.startActivity(new Intent(mActivity, NowPlayingActivity.class));
								}
							}
						}, item.path);
					}
				}
			});
		}
	}
	
	private class FileItemAdapter extends ArrayAdapter<MediaLocation> {
		private Activity mActivity;
		FileItemAdapter(Activity activity, ArrayList<MediaLocation> items) {
			super(activity, R.layout.listitem_three, items);
			mActivity = activity;
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			View row;
			if (convertView == null) {
				LayoutInflater inflater = mActivity.getLayoutInflater();
				row = inflater.inflate(R.layout.listitem_oneliner, null);
			} else {
				row = convertView;
			}
			final MediaLocation fileItem = this.getItem(position);
			row.setTag(fileItem);
			final TextView title = (TextView)row.findViewById(R.id.MusicItemTextViewTitle);
			final ImageView icon = (ImageView)row.findViewById(R.id.MusicItemImageViewArt);
			title.setText(fileItem.name);
			icon.setImageResource(R.drawable.icon_folder);
			return row;
		}
	}
	
	
	private void fillUp(final String url) {
		if (mGettingUrl != null)
			return;
		
		mGettingUrl = url;
		mFileItems = null;
		mList.setTextFilterEnabled(false);
		setTitle("Loading...");
		
		HttpApiHandler<ArrayList<MediaLocation>> mediaListHandler = new HttpApiHandler<ArrayList<MediaLocation>>(mActivity) {
			public void run() {
				setTitle(url.equals("") ? "/" : url);
				mFileItems = new HashMap<String, MediaLocation>();
				for (MediaLocation item : value) {
					mFileItems.put(item.name, item);
				}
				setListAdapter(new FileItemAdapter(mActivity, value));
				mList.setTextFilterEnabled(true);
			}
		};
		
		if (mGettingUrl.length() == 0) {
			HttpApiThread.info().getShares(mediaListHandler, mMediaType);
		} else {
			HttpApiThread.info().getDirectory(mediaListHandler, mGettingUrl);
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
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	}
	private static final long serialVersionUID = -3883887349523448733L;
}

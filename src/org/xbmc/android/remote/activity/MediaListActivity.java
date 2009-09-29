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

package org.xbmc.android.remote.activity;

import java.util.ArrayList;
import java.util.HashMap;

import org.xbmc.android.util.ConnectionManager;
import org.xbmc.android.util.ErrorHandler;
import org.xbmc.httpapi.HttpClient;
import org.xbmc.httpapi.data.MediaLocation;
import org.xbmc.httpapi.type.MediaType;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

public class MediaListActivity extends ListActivity implements Callback, Runnable {
	private final HttpClient mClient = ConnectionManager.getHttpClient(this);

	private HashMap<String, MediaLocation> fileItems;
	private volatile String gettingUrl;
	private MediaType mMediaType;
	private Handler mediaListHandler;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mediaListHandler = new Handler(this);
		ErrorHandler.setActivity(this);
		final String st = getIntent().getStringExtra("shareType");
		mMediaType = st != null ? MediaType.valueOf(st) : MediaType.music;
		final String path = getIntent().getStringExtra("path");
		fillUp(path == null ? "" : path);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (fileItems == null)
			return;

		MediaLocation item = fileItems.get(l.getAdapter().getItem(position));
		if (item.isDirectory) {
			Intent myIntent = new Intent(this, MediaListActivity.class);
			myIntent.putExtra("shareType", mMediaType.toString());
			myIntent.putExtra("path", item.path);
			startActivityForResult(myIntent, 0);
		} else {
			if (mClient.control.playFile(item.path)) {
				startActivityForResult(new Intent(v.getContext(), NowPlayingActivity.class), 0);
			}
		}
	}

	private void fillUp(String url) {
		if (gettingUrl != null)
			return;
		
		gettingUrl = url;
		fileItems = null;
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[]{ "Loading..." }));
		getListView().setTextFilterEnabled(false);
		Thread thread = new Thread(this);
		thread.start();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		if (mMediaType.equals(MediaType.music))
			menu.add(0, 0, 0, "Change view").setIcon(android.R.drawable.ic_menu_view);
		if (!mMediaType.equals(MediaType.music))
			menu.add(0, 1, 0, "Music");
		if (!mMediaType.equals(MediaType.video))
			menu.add(0, 2, 0, "Video");
		if (!mMediaType.equals(MediaType.pictures))
			menu.add(0, 3, 0, "Pictures").setIcon(android.R.drawable.ic_menu_camera);
		
		menu.add(0, 4, 0, "Now Playing").setIcon(android.R.drawable.ic_media_play);
		menu.add(0, 5, 0, "Remote");
		
		if (mMediaType.equals(MediaType.music) || mMediaType.equals(MediaType.video))
			menu.add(0, 6, 0, "Update Library");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent myIntent = null;
		
		switch (item.getItemId()) {
		case 0:
			myIntent = new Intent(this, AlbumGridActivity.class);
			break;
		case 1:
			myIntent = new Intent(this, MediaListActivity.class);
			myIntent.putExtra("shareType", MediaType.music.toString());
			break;
		case 2:
			myIntent = new Intent(this, MediaListActivity.class);
			myIntent.putExtra("shareType", MediaType.video.toString());
			break;
		case 3:
			myIntent = new Intent(this, MediaListActivity.class);
			myIntent.putExtra("shareType", MediaType.pictures.toString());
			break;
		case 4:
			myIntent = new Intent(this, NowPlayingActivity.class);
			break;
		case 5:
			myIntent = new Intent(this, RemoteActivity.class);
			break;
		case 6:
			mClient.control.updateLibrary(mMediaType.toString());
			break;
		}
		
		if (myIntent != null) {
			startActivity(myIntent);
			return true;
		}
		return false;
	}

	public boolean handleMessage(Message msg) {
		if (msg.what == 1) {
			Bundle data = msg.getData();
			
			setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data.getStringArrayList("items")));
			getListView().setTextFilterEnabled(true);

			return true;
		}
		return false;
	}

	public void run() {
		ArrayList<MediaLocation> dir;
		if (gettingUrl.length() == 0) {
			dir = mClient.info.getShares(mMediaType);
		} else {
			dir =  mClient.info.getDirectory(gettingUrl);
		}
		
		ArrayList<String> presentationList = new ArrayList<String>();
		fileItems = new HashMap<String, MediaLocation>();

		for (MediaLocation item : dir) {
			presentationList.add(item.name);
			fileItems.put(item.name, item);
		}
		
		Message msg = Message.obtain(mediaListHandler, 1);
		Bundle data = msg.getData();
		data.putStringArrayList("items", presentationList);
		mediaListHandler.sendMessage(msg);
	}
}
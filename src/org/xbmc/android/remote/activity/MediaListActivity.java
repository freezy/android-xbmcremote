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
import java.util.List;
import java.util.Stack;

import org.xbmc.android.util.ConnectionManager;
import org.xbmc.android.util.ErrorHandler;
import org.xbmc.httpapi.HttpClient;
import org.xbmc.httpapi.data.MediaLocation;
import org.xbmc.httpapi.type.MediaType;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;

public class MediaListActivity extends ListActivity {
	
	private final Stack<String> mHistory = new Stack<String>();;
	private final HttpClient mClient = ConnectionManager.getHttpClient(this);

	private List<MediaLocation> fileItems;
	private String currentUrl;
	private MediaType mMediaType;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ErrorHandler.setActivity(this);
		final String st = getIntent().getStringExtra("shareType");
		mMediaType = st != null ? MediaType.valueOf(st) : MediaType.music;
		fillUp("");
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		MediaLocation item = fileItems.get(position);
		if (item.isDirectory) {
			mHistory.add(currentUrl);
			fillUp(item.path);
		} else {
			if (mClient.control.playFile(item.path)) {
				startActivityForResult(new Intent(v.getContext(), NowPlayingActivity.class), 0);
			}
		}
	}

	private void fillUp(String url) {
		final List<String> presentationList = new ArrayList<String>();
		if (url.length() == 0) {
			fileItems = mClient.info.getShares(mMediaType);
		} else {
			fileItems =  mClient.info.getDirectory(url);
		}
		for (MediaLocation item : fileItems) {
			presentationList.add(item.name);
		}
		currentUrl = url;
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, presentationList));
		getListView().setTextFilterEnabled(true);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && !mHistory.isEmpty()) {
			fillUp(mHistory.pop());
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}
}
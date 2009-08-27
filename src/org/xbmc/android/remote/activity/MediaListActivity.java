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

import org.xbmc.android.remote.util.XBMCControl;
import org.xbmc.httpapi.Item;
import org.xbmc.httpapi.MediaType;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;

public class MediaListActivity extends ListActivity {
	private List<FileItem> fileItems;
	private Stack<String> history;
	private String currentUrl;
	MediaType mediaType;
	
	private class FileItem
	{
		public String name, url;
		public boolean isDirectory;
		
		public FileItem(String url) {
			this.url = url;
			this.isDirectory = url.endsWith("/");
			
			String s = isDirectory ? url.substring(0, url.length() - 1) : url;
			name = s.substring(s.lastIndexOf('/') + 1);
		}
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		String s = intent.getStringExtra("shareType");
		mediaType = s != null ? MediaType.valueOf(s) : MediaType.music;
		history = new Stack<String>();
		fillUp("");
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		FileItem item = fileItems.get(position);
		if (item.isDirectory)
		{
			history.add(currentUrl);
			fillUp(item.url);
		}
		else
		{
			XBMCControl.getHttpApiInstance(this).getMediaControls().playFile(item.url);
            Intent myIntent = new Intent(v.getContext(), NowPlayingActivity.class);
            startActivityForResult(myIntent, 0);
		}
	}

	private void fillUp(String url) {
		List<String> presentationList = new ArrayList<String>();
		fileItems = new ArrayList<FileItem>();
		
		if (url.length() == 0)
		{
			for (Item item : XBMCControl.getHttpApiInstance(this).getShares(mediaType))
			{
				presentationList.add(item.getName());
				fileItems.add(new FileItem(item.getURL()));
			}
		}
		else
		{
			for (String s : XBMCControl.getHttpApiInstance(this).getDirectory(url))
			{
				FileItem currentItem = new FileItem(s);
				fileItems.add(currentItem);
				presentationList.add(currentItem.name);
			}
		}	

		currentUrl = url;
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, presentationList));
		getListView().setTextFilterEnabled(true);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && !history.isEmpty())
		{
			fillUp(history.pop());
			return true;
		}
		else
			return super.onKeyDown(keyCode, event);
	}
}
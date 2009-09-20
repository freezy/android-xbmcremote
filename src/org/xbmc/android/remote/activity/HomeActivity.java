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

import org.xbmc.android.remote.R;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.httpapi.HttpClient;
import org.xbmc.httpapi.NoNetworkException;
import org.xbmc.httpapi.info.SystemInfo;
import org.xbmc.httpapi.type.MediaType;

import android.app.Activity;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.AvoidXfermode;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class HomeActivity extends Activity implements Callback, Runnable {
	Handler homeHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		
		final ArrayList<HomeItem> homeItems = new ArrayList<HomeItem>();
		
		homeItems.add(new HomeItem(R.drawable.home_remote, "Remote Control", "Use as", new View.OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(v.getContext(), RemoteActivity.class);
				startActivityForResult(myIntent, 0);
			}
		}));
		homeItems.add(new HomeItem(R.drawable.home_music, "Music", "Listen to", new View.OnClickListener() {
			public void onClick(View v) {
//				Intent myIntent = new Intent(v.getContext(), AlbumGridActivity.class);
				Intent myIntent = new Intent(v.getContext(), MediaListActivity.class);
				myIntent.putExtra("shareType", MediaType.music.toString());
				startActivityForResult(myIntent, 0);
			}
		}));
		homeItems.add(new HomeItem(R.drawable.home_video, "Videos", "Watch your", new View.OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(v.getContext(), MediaListActivity.class);
				myIntent.putExtra("shareType", MediaType.video.toString());
				startActivityForResult(myIntent, 0);
			}
		}));
		homeItems.add(new HomeItem(R.drawable.home_pictures, "Pictures", "Browse your", new View.OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(v.getContext(), MediaListActivity.class);
				myIntent.putExtra("shareType", MediaType.pictures.toString());
				startActivityForResult(myIntent, 0);
			}
		}));
		homeItems.add(new HomeItem(R.drawable.home_playing, "Now Playing", "See what's", new View.OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(v.getContext(), NowPlayingActivity.class);
				startActivityForResult(myIntent, 0);
			}
		}));

		final GridView lv = (GridView)findViewById(R.id.ItemListView);
		lv.setAdapter(new HomeAdapter(this, homeItems));
		
        homeHandler = new Handler(this);
        ((TextView) findViewById(R.id.HomeVersionTextView)).setText("Connecting...");
        Thread connectThread = new Thread(this);
        connectThread.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, "Settings").setIcon(R.drawable.icon_menu_settings);
		menu.add(0, 2, 0, "Exit").setIcon(R.drawable.icon_menu_exit);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case 2:
			this.finish();
			return true;
		}
		return false;
	}

	public boolean handleMessage(Message msg) {
		if (msg.what == 1) {
			((TextView) findViewById(R.id.HomeVersionTextView)).setText(msg.getData().get("version").toString());
			return true;
		}
		
		return false;
	}

	public void run() {
		String version = "";
		try {
			if (!ConnectionManager.isNetworkAvailable(this)) {
				throw new NoNetworkException();
			}
			HttpClient client = ConnectionManager.getHttpClient(this);
			version = client.isConnected() ? ("XBMC " + client.info.getSystemInfo(SystemInfo.SYSTEM_BUILD_VERSION)) : "Connection error, check your setttings!";

		} catch (Exception e) {
			version = "Connection error, check your setttings!";
		}
		
		Message msg = new Message();
		msg.what = 1;
		Bundle bundle = new Bundle();
		bundle.putString("version", version);
		msg.setData(bundle);
		homeHandler.sendMessage(msg);
	}
	
	
	private class HomeItem {
		public final int icon;
		public final String title, subtitle;
		public final  View.OnClickListener onClick;
		
		public HomeItem(int icon, String title, String subtitle, View.OnClickListener onClick) {
			this.icon = icon;
			this.title = title;
			this.subtitle = subtitle;
			this.onClick = onClick;
		}
	}
	
	private class HomeAdapter extends ArrayAdapter<HomeItem> {
		private Activity mActivity;
		HomeAdapter(Activity activity, ArrayList<HomeItem> items) {
			super(activity, R.layout.home_item, items);
			mActivity = activity;
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			View row;
			if (convertView == null) {
				LayoutInflater inflater = mActivity.getLayoutInflater();
				row = inflater.inflate(R.layout.home_item, null);
			} else {
				row = convertView;
			}
			HomeItem item = this.getItem(position);
			TextView title = (TextView)row.findViewById(R.id.TitleTextView);
			TextView subtitle = (TextView)row.findViewById(R.id.SubtitleTextView);
			ImageView icon = (ImageView)row.findViewById(R.id.IconImageView);
			title.setText(item.title);
			subtitle.setText(item.subtitle);
			icon.setImageResource(item.icon);
			row.setOnClickListener(item.onClick);
			return row;
		}
	}
	
}

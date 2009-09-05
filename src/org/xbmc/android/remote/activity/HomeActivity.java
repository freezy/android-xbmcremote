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

import org.xbmc.android.remote.R;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.httpapi.HttpClient;
import org.xbmc.httpapi.NoNetworkException;
import org.xbmc.httpapi.info.SystemInfo;
import org.xbmc.httpapi.type.MediaType;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class HomeActivity extends Activity implements Callback, Runnable {
	Handler homeHandler;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
        WindowManager wm = getWindowManager(); 
        Display d = wm.getDefaultDisplay();

        if (d.getWidth() > d.getHeight())
        	setContentView(R.layout.main_landscape);
        else
        	setContentView(R.layout.main_portrait);

        homeHandler = new Handler(this);
        ((TextView) findViewById(R.id.HomeVersionTextView)).setText("Connecting...");
        Thread connectThread = new Thread(this);
        connectThread.start();

		final Button GoMusicButton = (Button) findViewById(R.id.GoMusicButton);
		GoMusicButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
//				Intent myIntent = new Intent(v.getContext(), AlbumGridActivity.class);
				Intent myIntent = new Intent(v.getContext(), MediaListActivity.class);
				myIntent.putExtra("shareType", MediaType.music.toString());
				startActivityForResult(myIntent, 0);
			}
		});

		final Button GoVideosButton = (Button) findViewById(R.id.GoVideosButton);
		GoVideosButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(v.getContext(), MediaListActivity.class);
				myIntent.putExtra("shareType", MediaType.video.toString());
				startActivityForResult(myIntent, 0);
			}
		});

		final Button GoPicturesButton = (Button) findViewById(R.id.GoPicturesButton);
		GoPicturesButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(v.getContext(), MediaListActivity.class);
				myIntent.putExtra("shareType", MediaType.pictures.toString());
				startActivityForResult(myIntent, 0);
			}
		});

		final Button GoRemoteButton = (Button) findViewById(R.id.GoRemoteButton);
		GoRemoteButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(v.getContext(), RemoteActivity.class);
				startActivityForResult(myIntent, 0);
			}
		});

		final Button GoNowPlayingButton = (Button) findViewById(R.id.GoNowPlayingButton);
		GoNowPlayingButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(v.getContext(), NowPlayingActivity.class);
				startActivityForResult(myIntent, 0);
			}
		});
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
}

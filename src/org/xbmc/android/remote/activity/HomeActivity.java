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
import org.xbmc.android.util.WakeOnLan;
import org.xbmc.httpapi.HttpClient;
import org.xbmc.httpapi.NoNetworkException;
import org.xbmc.httpapi.info.SystemInfo;
import org.xbmc.httpapi.type.MediaType;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeActivity extends Activity implements Callback, Runnable {
	Handler homeHandler;
	Thread connectThread;
	HomeAdapter homeMenu;
	HomeAdapter offlineMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		final ArrayList<HomeItem> homeItems = new ArrayList<HomeItem>();
		final ArrayList<HomeItem> offlineItems = new ArrayList<HomeItem>();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        homeHandler = new Handler(this);
        Message connectMsg = new Message();        
        connectMsg.what = 3;

        final HomeItem remote = new HomeItem(R.drawable.home_remote, "Remote Control", "Use as", new View.OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(v.getContext(), RemoteActivity.class);
				startActivityForResult(myIntent, 0);
			}
		});
        
		homeItems.add(remote);
		offlineItems.add(remote);
		
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
		
		final String wolMac = prefs.getString("setting_wol", "");
		final int wolWait = Integer.parseInt(prefs.getString("setting_wol_wait", "40"));

		offlineItems.add(new HomeItem(R.drawable.home_reconnect, "Again", "Try to connect", new View.OnClickListener() {
			public void onClick(View v) {
		        Message connectMsg = new Message();
		        connectMsg.what = 3;
				Bundle connectText = new Bundle();
				connectText.putString("message", "Reconnecting...");
				connectMsg.setData(connectText);
				homeHandler.sendMessage(connectMsg);
			}
		}));
		
		if (wolMac.compareTo("") != 0) {
			offlineItems.add(new HomeItem(R.drawable.home_power, "Power On", "Turn your XBMC's", new View.OnClickListener() {
				public void onClick(View v) {
					WakeOnLan wol = new WakeOnLan();
					if (wol.sendMagicPacket(wolMac)) { // If succeeded in sending the magic packet, begin the countdown
						WoLCounter counter = new WoLCounter(wolWait * 1000,1000);
						counter.start();
					}
				}
			}));
		}

		homeMenu = new HomeAdapter(this, homeItems);
		offlineMenu = new HomeAdapter(this, offlineItems);
		((GridView)findViewById(R.id.HomeItemGridView)).setAdapter(offlineMenu);
		
        ((TextView) findViewById(R.id.HomeVersionTextView)).setText("Connecting...");
        
		homeHandler.sendMessage(connectMsg);
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
			System.exit(0);
			return true;
		}
		return false;
	}

	public boolean handleMessage(Message msg) {
		if (msg.what == 1) { // Connection succeeded
			((TextView) findViewById(R.id.HomeVersionTextView)).setText(msg.getData().get("version").toString());
			((GridView)findViewById(R.id.HomeItemGridView)).setAdapter(homeMenu);

			return true;
		} else if (msg.what == 2) { // Connection failed - show offline menu
			((TextView) findViewById(R.id.HomeVersionTextView)).setText(msg.getData().get("version").toString());
			((GridView)findViewById(R.id.HomeItemGridView)).setAdapter(offlineMenu);

			return true;
		} else if (msg.what == 3) { // Attempt to connect
			// If we aren't already trying to connect, get the string from the message
			// And try to start the connection thread
			if (connectThread == null || !connectThread.isAlive()) {
				String connectMsg = msg.getData().getString("message");
				if (connectMsg == null) {
					connectMsg = "Connecting...";
				}
				((TextView) findViewById(R.id.HomeVersionTextView)).setText(connectMsg);				
				connectThread = new Thread(this);
				connectThread.start();				
			}			
			return true;
		}
		
		return false;
	}

	public void run() {
		String version = "";
		int msgType = 1;
		try {
			if (!ConnectionManager.isNetworkAvailable(this)) {
				throw new NoNetworkException();
			}
			HttpClient client = ConnectionManager.getHttpClient(this);

			if (client.isConnected()) {
				version = "XBMC " + client.info.getSystemInfo(SystemInfo.SYSTEM_BUILD_VERSION);
			} else {
				version = "Connection error, check your setttings!";
				msgType = 2;
			}
		} catch (Exception e) {
			version = "Connection error, check your setttings!";
			msgType = 2;
		}
		
		Message msg = new Message();
		msg.what = msgType;
		Bundle bundle = new Bundle();
		bundle.putString("version", version);
		msg.setData(bundle);
		homeHandler.sendMessage(msg);
	}
		
	private class HomeItem {
		public final int icon;
		public final String title, subtitle;
		public final View.OnClickListener onClick;
		
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
	
	public class WoLCounter extends CountDownTimer {
		private TextView textCount;
		
		public WoLCounter(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);

			textCount = ((TextView) findViewById(R.id.HomeVersionTextView));
		}
		
		@Override
		public void onFinish() {
			Message connectMsg = new Message();
			Bundle connectText = new Bundle();
			connectText.putString("message", "Attempting to reconnect...");
			connectMsg.setData(connectText);			
			connectMsg.what = 3;
			homeHandler.sendMessage(connectMsg);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			textCount.setText("Waiting for " + millisUntilFinished/1000 + " more seconds...");						
		}
	}
}

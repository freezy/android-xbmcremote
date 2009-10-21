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

import java.io.IOException;
import java.util.ArrayList;

import org.xbmc.android.backend.httpapi.HttpApiHandler;
import org.xbmc.android.backend.httpapi.HttpApiThread;
import org.xbmc.android.remote.R;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.android.util.ErrorHandler;
import org.xbmc.android.util.WakeOnLan;
import org.xbmc.eventclient.ButtonCodes;
import org.xbmc.eventclient.EventClient;
import org.xbmc.httpapi.info.SystemInfo;
import org.xbmc.httpapi.type.MediaType;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class HomeActivity extends Activity implements OnItemClickListener {

	private static final int HOME_ACTION_REMOTE = 0;
	private static final int HOME_ACTION_MUSIC = 1;
	private static final int HOME_ACTION_VIDEOS = 2;
	private static final int HOME_ACTION_PICTURES = 3;
	private static final int HOME_ACTION_NOWPLAYING = 4;
	private static final int HOME_ACTION_RECONNECT = 5;
	private static final int HOME_ACTION_WOL = 6;
	
	private static final int MENU_ABOUT = 1;
	private static final int MENU_SETTINGS = 2;
	private static final int MENU_EXIT = 3;
	

	private HomeAdapter mHomeMenu;
	private HomeAdapter mOfflineMenu;
	
	HttpApiHandler<String> mUpdateVersionHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		
		ErrorHandler.setActivity(this);

		final ArrayList<HomeItem> homeItems = new ArrayList<HomeItem>();
		final ArrayList<HomeItem> offlineItems = new ArrayList<HomeItem>();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		mUpdateVersionHandler = new HttpApiHandler<String>(this) {
			public void run() {
				if (!ConnectionManager.isNetworkAvailable(mActivity)) {
					((TextView) findViewById(R.id.HomeVersionTextView)).setText("No network");
				}
				if (!value.equals("")) {
					((TextView) findViewById(R.id.HomeVersionTextView)).setText("XBMC " + value);
					((GridView)findViewById(R.id.HomeItemGridView)).setAdapter(mHomeMenu);
				} else {
					((TextView) findViewById(R.id.HomeVersionTextView)).setText("Check Settings and retry");
					((GridView)findViewById(R.id.HomeItemGridView)).setAdapter(mOfflineMenu);
				}
			}
		};
		
        final HomeItem remote = new HomeItem(HOME_ACTION_REMOTE, R.drawable.icon_remote, "Remote Control", "Use as");
        
		homeItems.add(remote);
		offlineItems.add(remote);
		
		homeItems.add(new HomeItem(HOME_ACTION_MUSIC, R.drawable.icon_music, "Music", "Listen to"));
		homeItems.add(new HomeItem(HOME_ACTION_VIDEOS, R.drawable.icon_video, "Videos", "Watch your"));
		homeItems.add(new HomeItem(HOME_ACTION_PICTURES, R.drawable.icon_pictures, "Pictures", "Browse your"));
		homeItems.add(new HomeItem(HOME_ACTION_NOWPLAYING, R.drawable.icon_playing, "Now Playing", "See what's"));
		
		offlineItems.add(new HomeItem(HOME_ACTION_RECONNECT, R.drawable.icon_reconnect, "Connect", "Try again to"));

		final String wolMac = prefs.getString("setting_wol", "");
		if (wolMac.compareTo("") != 0)
			offlineItems.add(new HomeItem(HOME_ACTION_WOL, R.drawable.icon_power, "Power On", "Turn your XBMC's"));

		mHomeMenu = new HomeAdapter(this, homeItems);
		mOfflineMenu = new HomeAdapter(this, offlineItems);
		setHomeAdapter(mOfflineMenu);
		
        ((TextView) findViewById(R.id.HomeVersionTextView)).setText("Connecting...");
        HttpApiThread.info().getSystemInfo(mUpdateVersionHandler, SystemInfo.SYSTEM_BUILD_VERSION);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ABOUT, 0, "About").setIcon(R.drawable.menu_about);
		menu.add(0, MENU_SETTINGS, 0, "Settings").setIcon(R.drawable.icon_menu_settings);
		menu.add(0, MENU_EXIT, 0, "Exit").setIcon(R.drawable.icon_menu_exit);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ABOUT:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		case MENU_SETTINGS:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case MENU_EXIT:
			System.exit(0);
			return true;
		}
		return false;
	}


	private void setHomeAdapter(HomeAdapter adapter) {
		final GridView gridView = ((GridView)findViewById(R.id.HomeItemGridView));
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(this);
		gridView.setSelected(true);
		gridView.setSelection(0);
	}
	
	public void onItemClick(AdapterView<?> listView, View v, int position, long ID) {
		HomeItem item = (HomeItem)listView.getAdapter().getItem(position);
		
		switch (item.ID) {
		case HOME_ACTION_REMOTE:
			startActivityForResult(new Intent(v.getContext(), RemoteActivity.class), 0);
			break;
		case HOME_ACTION_MUSIC:
//			startActivityForResult(createMediaIntent(MediaType.music, v), 0);
			startActivity(new Intent(v.getContext(), MusicLibraryActivity.class));
//			startActivityForResult(new Intent(v.getContext(), MediaTabContainerActivity.class), 0);
			break;
		case HOME_ACTION_VIDEOS:

/*			Intent nextActivity = new Intent(v.getContext(), ListActivity.class);
			nextActivity.putExtra(ListLogic.EXTRA_LIST_LOGIC, new FileListLogic());
			nextActivity.putExtra(ListLogic.EXTRA_SHARE_TYPE, MediaType.video.toString());
			nextActivity.putExtra(ListLogic.EXTRA_PATH, "");
			startActivity(nextActivity);*/

			startActivityForResult(createMediaIntent(MediaType.video, v), 0);
			break;
		case HOME_ACTION_PICTURES:
			startActivityForResult(createMediaIntent(MediaType.pictures, v), 0);
			break;
		case HOME_ACTION_NOWPLAYING:
			startActivityForResult(new Intent(v.getContext(), NowPlayingActivity.class), 0);
			break;
		case HOME_ACTION_RECONNECT:
			((TextView) findViewById(R.id.HomeVersionTextView)).setText("Reconnecting...");
			HttpApiThread.info().getSystemInfo(mUpdateVersionHandler, SystemInfo.SYSTEM_BUILD_VERSION);
			break;
		case HOME_ACTION_WOL:
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			final String wolMac = prefs.getString("setting_wol", "");
			final int wolWait = Integer.parseInt(prefs.getString("setting_wol_wait", "40"));
			
			WakeOnLan wol = new WakeOnLan();
			if (wol.sendMagicPacket(wolMac)) { // If succeeded in sending the magic packet, begin the countdown
				WoLCounter counter = new WoLCounter(wolWait * 1000,1000);
				counter.start();
			}
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		EventClient client = ConnectionManager.getEventClient(this);	
		try {
			switch (keyCode) {
				case KeyEvent.KEYCODE_VOLUME_UP:
					client.sendButton("R1", ButtonCodes.REMOTE_VOLUME_PLUS, false, true, true, (short)0, (byte)0);
					return true;
				case KeyEvent.KEYCODE_VOLUME_DOWN:
					client.sendButton("R1", ButtonCodes.REMOTE_VOLUME_MINUS, false, true, true, (short)0, (byte)0);
					return true;
			}
		} catch (IOException e) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	private Intent createMediaIntent(MediaType mediaType, View v) {
		Intent myIntent = new Intent(v.getContext(), FileListActivity.class);
		myIntent.putExtra("shareType", mediaType.toString());
		return myIntent;
	}
	
	private class HomeItem {
		public final int ID, icon;
		public final String title, subtitle;
		
		public HomeItem(int ID, int icon, String title, String subtitle) {
			this.ID = ID;
			this.icon = icon;
			this.title = title;
			this.subtitle = subtitle;
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
			((TextView) findViewById(R.id.HomeVersionTextView)).setText("Attempting to reconnect...");
			HttpApiThread.info().getSystemInfo(mUpdateVersionHandler, SystemInfo.SYSTEM_BUILD_VERSION);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			textCount.setText("Waiting for " + millisUntilFinished/1000 + " more seconds...");						
		}
	}
}

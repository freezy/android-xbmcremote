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
import java.util.Observable;
import java.util.Observer;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.activity.HostSettingsActivity;
import org.xbmc.android.remote.presentation.activity.ListActivity;
import org.xbmc.android.remote.presentation.activity.MovieLibraryActivity;
import org.xbmc.android.remote.presentation.activity.MusicLibraryActivity;
import org.xbmc.android.remote.presentation.activity.NowPlayingActivity;
import org.xbmc.android.remote.presentation.activity.NowPlayingNotificationManager;
import org.xbmc.android.remote.presentation.activity.RemoteActivity;
import org.xbmc.android.util.ConnectionFactory;
import org.xbmc.android.util.HostFactory;
import org.xbmc.android.util.WakeOnLan;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IInfoManager;
import org.xbmc.api.info.SystemInfo;
import org.xbmc.api.object.Host;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.api.type.MediaType;
import org.xbmc.httpapi.BroadcastListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class HomeController extends AbstractController implements INotifiableController, IController, Observer {
	
	private static final int HOME_ACTION_REMOTE = 0;
	private static final int HOME_ACTION_MUSIC = 1;
	private static final int HOME_ACTION_VIDEOS = 2;
	private static final int HOME_ACTION_PICTURES = 3;
	private static final int HOME_ACTION_NOWPLAYING = 4;
	private static final int HOME_ACTION_RECONNECT = 5;
	private static final int HOME_ACTION_WOL = 6;
	
	private IInfoManager mInfoManager;
	
	private DataResponse<String> mUpdateVersionHandler;
	
	private HomeAdapter mHomeMenu;
	private HomeAdapter mOfflineMenu;
	
	public HomeController(Activity activity, GridView menuGrid) {
		super.onCreate(activity);
		mInfoManager = ManagerFactory.getInfoManager(activity.getApplicationContext(), this);
		setupMenuItems(menuGrid);
		
//		BroadcastListener bcl = BroadcastListener.getInstance(ConnectionManager.getHttpClient(this));
//		bcl.addObserver(this);
	}
	
	public View.OnClickListener getOnHostChangeListener() {
		return new OnClickListener() {
			public void onClick(View v) {
				// granted, this is butt-ugly. better ideas, be my guest.
				final ArrayList<Host> hosts = HostFactory.getHosts(mActivity.getApplicationContext());
				final HashMap<Integer, Host> hostMap = new HashMap<Integer, Host>();
				final CharSequence[] names = new CharSequence[hosts.size()];
				int i = 0;
				for (Host host : hosts) {
					names[i] = host.name;
					hostMap.put(i, host);
					i++;
				}
				if (hosts.size() > 0) {
					AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
					builder.setTitle("Pick your XBMC!");
					builder.setItems(names, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							final Host host = hostMap.get(which);
							if (HostFactory.host != null && HostFactory.host.id == host.id) {
								Toast.makeText(mActivity.getApplicationContext(), "You've picked the same host as the current.", Toast.LENGTH_SHORT).show();
							} else {
								HostFactory.saveHost(mActivity.getApplicationContext(), host);
								Toast.makeText(mActivity.getApplicationContext(), "Changed host to " + host.toString() + ".", Toast.LENGTH_SHORT).show();
								mInfoManager.getSystemInfo(mUpdateVersionHandler, SystemInfo.SYSTEM_BUILD_VERSION);
							}
						}
					});
					AlertDialog dialog = builder.create();
					dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
					dialog.show();
				} else {
					Toast.makeText(mActivity.getApplicationContext(), "No XBMC hosts defined, please do that first.", Toast.LENGTH_LONG).show();
					Intent intent = new Intent(mActivity, HostSettingsActivity.class);
					mActivity.startActivity(intent);
				}
			}
		};
	}
	
	public void setupVersionHandler(final Button versionTextView, final GridView homeItemGrid) {
		mUpdateVersionHandler = new DataResponse<String>() {
			public void run() {
				if (!ConnectionFactory.isNetworkAvailable(mActivity.getApplicationContext())) {
					versionTextView.setText("No network");
				}
				if (!value.equals("")) {
					versionTextView.setText("XBMC " + value);
					homeItemGrid.setAdapter(mHomeMenu);
					NowPlayingNotificationManager.getInstance(mActivity.getApplicationContext()).startNotificating();
				} else {
					versionTextView.setText("Check Settings and retry");
					homeItemGrid.setAdapter(mOfflineMenu);
				}
			}
		};		
	}
	
	private void setupMenuItems(GridView menuGrid) {
		final HomeItem remote = new HomeItem(HOME_ACTION_REMOTE, R.drawable.icon_home_remote, "Remote Control", "Use as");
		final ArrayList<HomeItem> homeItems = new ArrayList<HomeItem>();
		homeItems.add(new HomeItem(HOME_ACTION_MUSIC, R.drawable.icon_home_music, "Music", "Listen to"));
		homeItems.add(new HomeItem(HOME_ACTION_VIDEOS, R.drawable.icon_home_movie, "Movies", "Watch your"));
		homeItems.add(new HomeItem(HOME_ACTION_PICTURES, R.drawable.icon_home_picture, "Pictures", "Browse your"));
		homeItems.add(new HomeItem(HOME_ACTION_NOWPLAYING, R.drawable.icon_home_playing, "Now Playing", "See what's"));
		homeItems.add(remote);
			
		final ArrayList<HomeItem> offlineItems = new ArrayList<HomeItem>();
		offlineItems.add(remote);
		offlineItems.add(new HomeItem(HOME_ACTION_RECONNECT, R.drawable.icon_home_reconnect, "Connect", "Try again to"));
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext());
		final String wolMac = prefs.getString("setting_wol", "");
		if (wolMac.compareTo("") != 0)
			offlineItems.add(new HomeItem(HOME_ACTION_WOL, R.drawable.icon_home_power, "Power On", "Turn your XBMC's"));
		
		mHomeMenu = new HomeAdapter(mActivity, homeItems);
		mOfflineMenu = new HomeAdapter(mActivity, offlineItems);
		
		setHomeAdapter(menuGrid, mOfflineMenu);

	}
	
	private void setHomeAdapter(GridView menuGrid, HomeAdapter adapter) {
		menuGrid.setAdapter(adapter);
		menuGrid.setOnItemClickListener(getHomeMenuOnClickListener());
		menuGrid.setSelected(true);
		menuGrid.setSelection(0);
	}
	
	private OnItemClickListener getHomeMenuOnClickListener() {
		return new OnItemClickListener() {
			public void onItemClick(AdapterView<?> listView, View v, int position, long ID) {
				HomeItem item = (HomeItem)listView.getAdapter().getItem(position);
				switch (item.ID) {
					case HOME_ACTION_REMOTE:
						mActivity.startActivity(new Intent(v.getContext(), RemoteActivity.class));
						break;
					case HOME_ACTION_MUSIC:
						mActivity.startActivity(new Intent(v.getContext(), MusicLibraryActivity.class));
						break;
					case HOME_ACTION_VIDEOS:
						mActivity.startActivity(new Intent(v.getContext(), MovieLibraryActivity.class));
						break;
					case HOME_ACTION_PICTURES:
						Intent intent = new Intent(v.getContext(), ListActivity.class);
						intent.putExtra(ListController.EXTRA_LIST_CONTROLLER, new FileListController());
						intent.putExtra(ListController.EXTRA_SHARE_TYPE, MediaType.PICTURES);
						intent.putExtra(ListController.EXTRA_PATH, "");
						mActivity.startActivity(intent);
						break;
					case HOME_ACTION_NOWPLAYING:
						mActivity.startActivity(new Intent(v.getContext(), NowPlayingActivity.class));
						break;
					case HOME_ACTION_RECONNECT:
						((Button)mActivity.findViewById(R.id.home_version_button)).setText("Reconnecting...");
						mInfoManager.getSystemInfo(mUpdateVersionHandler, SystemInfo.SYSTEM_BUILD_VERSION);
						break;
					case HOME_ACTION_WOL:
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext());
						final String wolMac = prefs.getString("setting_wol", "");
						final int wolWait = Integer.parseInt(prefs.getString("setting_wol_wait", "40"));
						final int wolPort = Integer.parseInt(prefs.getString("setting_wol_port", "9"));
						
						WakeOnLan wol = new WakeOnLan();
						if (wol.sendMagicPacket(wolMac, wolPort)) { // If succeeded in sending the magic packet, begin the countdown
							WolCounter counter = new WolCounter(wolWait * 1000,1000);
							counter.start();
						}
						break;
				}
			}
		};
	}
	
	public void update(Observable observable, Object data) {
		if (data instanceof BroadcastListener.Event) {
			BroadcastListener.Event event = (BroadcastListener.Event)data;
			switch (event.id) {
				case BroadcastListener.EVENT_ON_PROGRESS_CHANGED:
					Log.i("broadcast", "EVENT_ON_PROGRESS_CHANGED: " + event.getInt(0));
					break;
				default:
					Log.i("broadcast", "EVENT: " + event.id + ", int = " + event.getInt(0));
					break;
			}
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
	
	public class WolCounter extends CountDownTimer {
		private Button textCount;
		
		public WolCounter(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);

			textCount = ((Button)mActivity.findViewById(R.id.home_version_button));
		}
		
		@Override
		public void onFinish() {
			((Button)mActivity.findViewById(R.id.home_version_button)).setText("Attempting to reconnect...");
			mInfoManager.getSystemInfo(mUpdateVersionHandler, SystemInfo.SYSTEM_BUILD_VERSION);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			textCount.setText("Waiting for " + millisUntilFinished/1000 + " more seconds...");						
		}
	}

	public void onActivityPause() {
		mInfoManager.setController(null);
	}

	public void onActivityResume(Activity activity) {
		mInfoManager.setController(this);
		mInfoManager.getSystemInfo(mUpdateVersionHandler, SystemInfo.SYSTEM_BUILD_VERSION);
	}
}
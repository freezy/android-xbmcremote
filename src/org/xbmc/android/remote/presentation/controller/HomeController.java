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
import org.xbmc.android.remote.presentation.activity.HomeActivity;
import org.xbmc.android.remote.presentation.activity.HostSettingsActivity;
import org.xbmc.android.remote.presentation.activity.AbsListActivity;
import org.xbmc.android.remote.presentation.activity.MovieLibraryActivity;
import org.xbmc.android.remote.presentation.activity.MusicLibraryActivity;
import org.xbmc.android.remote.presentation.activity.NowPlayingActivity;
import org.xbmc.android.remote.presentation.activity.NowPlayingNotificationManager;
import org.xbmc.android.remote.presentation.activity.RemoteActivity;
import org.xbmc.android.remote.presentation.activity.TvShowLibraryActivity;
import org.xbmc.android.util.ConnectionFactory;
import org.xbmc.android.util.HostFactory;
import org.xbmc.android.util.WakeOnLan;
import org.xbmc.android.util.WifiHelper;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IInfoManager;
import org.xbmc.api.business.IMusicManager;
import org.xbmc.api.business.IVideoManager;
import org.xbmc.api.info.SystemInfo;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Album;
import org.xbmc.api.object.Host;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.object.Movie;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.api.type.MediaType;
import org.xbmc.api.type.ThumbSize;
import org.xbmc.httpapi.BroadcastListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
	private static final int HOME_ACTION_TVSHOWS = 7;
	
	
	private IInfoManager mInfoManager;
	
	private static final String TAG = "HomeController";
	private static final boolean DEBUG = false;
	
	private DataResponse<String> mUpdateVersionHandler;
	
	private HomeAdapter mHomeMenu;
	private HomeAdapter mOfflineMenu;
	
	private int mCoverDownloadOffset = 0;
	
	private WolCounter mWolCounter;
	
	private final HomeItem mHomeWol = new HomeItem(HOME_ACTION_WOL, R.drawable.icon_home_power, "Power On", "Turn your XBMC's");
    
	public HomeController(Activity activity, GridView menuGrid) {
		super.onCreate(activity);
		mInfoManager = ManagerFactory.getInfoManager(this);
		setupMenuItems(menuGrid);
//		BroadcastListener bcl = BroadcastListener.getInstance(ConnectionManager.getHttpClient(this));
//		bcl.addObserver(this);
	}
	
	public View.OnClickListener getOnHostChangeListener() {
		return new OnClickListener() {
			public void onClick(View v) {
				openHostChanger();
			}
		};
	}
	
	/**
	 * Opens the host changer popup.
	 */
	public void openHostChanger() {
		
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
						final GridView menuGrid = (GridView)mActivity.findViewById(R.id.HomeItemGridView);
						resetupOfflineMenuItems();
						setHomeAdapter(menuGrid, mOfflineMenu);
						final Button versionButton = (Button)mActivity.findViewById(R.id.home_version_button);
						versionButton.setText("Connecting...");
						Toast.makeText(mActivity.getApplicationContext(), "Changed host to " + host.toString() + ".", Toast.LENGTH_SHORT).show();
						mInfoManager.getSystemInfo(mUpdateVersionHandler, SystemInfo.SYSTEM_BUILD_VERSION, mActivity.getApplicationContext());
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
	
	public void setupVersionHandler(final Button versionTextView, final GridView homeItemGrid) {
		mUpdateVersionHandler = new DataResponse<String>() {
			public void run() {
				if (!mPaused) {
					if (!ConnectionFactory.isNetworkAvailable(mActivity.getApplicationContext())) {
						versionTextView.setText("No network");
					}
					if (!value.equals("")) {
						if(mWolCounter != null) mWolCounter.cancel();
						versionTextView.setText("XBMC " + value);
						homeItemGrid.setAdapter(mHomeMenu);
						NowPlayingNotificationManager.getInstance(mActivity.getApplicationContext()).startNotificating();
					} else {
						versionTextView.setText("Check Settings and retry");
						homeItemGrid.setAdapter(mOfflineMenu);
					}
				}
			}
		};		
	}
	
	private void setupMenuItems(GridView menuGrid) {
		final HomeItem remote = new HomeItem(HOME_ACTION_REMOTE, R.drawable.icon_home_remote, "Remote Control", "Use as");
		final ArrayList<HomeItem> homeItems = new ArrayList<HomeItem>();
		homeItems.add(new HomeItem(HOME_ACTION_MUSIC, R.drawable.icon_home_music, "Music", "Listen to"));
		homeItems.add(new HomeItem(HOME_ACTION_VIDEOS, R.drawable.icon_home_movie, "Movies", "Watch your"));
		homeItems.add(new HomeItem(HOME_ACTION_TVSHOWS, R.drawable.icon_home_tv, "TV Shows", "Watch your"));
		homeItems.add(new HomeItem(HOME_ACTION_PICTURES, R.drawable.icon_home_picture, "Pictures", "Browse your"));
		homeItems.add(new HomeItem(HOME_ACTION_NOWPLAYING, R.drawable.icon_home_playing, "Now Playing", "See what's"));
		homeItems.add(remote);
			
		final ArrayList<HomeItem> offlineItems = new ArrayList<HomeItem>();
		offlineItems.add(remote);
		offlineItems.add(new HomeItem(HOME_ACTION_RECONNECT, R.drawable.icon_home_reconnect, "Connect", "Try again to"));
		
//		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext());
//		final String wolMac = prefs.getString("setting_wol", "");
		if (HostFactory.host != null && !"".equals(HostFactory.host.mac_addr))
			offlineItems.add(mHomeWol);
		
		mHomeMenu = new HomeAdapter(mActivity, homeItems);
		mOfflineMenu = new HomeAdapter(mActivity, offlineItems);
		
		setHomeAdapter(menuGrid, mOfflineMenu);
	}
	
	/**
	 * Due to host changing we need to resetup the offline items with checking of WOL prefs and WiFi activation.
	 * @param menuGrid
	 */
	private void resetupOfflineMenuItems() {
		mOfflineMenu.remove(mHomeWol);
		if (HostFactory.host != null && !"".equals(HostFactory.host.mac_addr))
			mOfflineMenu.add(mHomeWol);
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
					case HOME_ACTION_TVSHOWS:
						mActivity.startActivity(new Intent(v.getContext(), TvShowLibraryActivity.class));
						break;
					case HOME_ACTION_PICTURES:
						Intent intent = new Intent(v.getContext(), AbsListActivity.class);
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
						mInfoManager.getSystemInfo(mUpdateVersionHandler, SystemInfo.SYSTEM_BUILD_VERSION, mActivity.getApplicationContext());
						break;
					case HOME_ACTION_WOL:
						final Host host = HostFactory.host;
						WakeOnLan wol = new WakeOnLan();
						if (wol.sendMagicPacket(host.mac_addr, host.wol_port)) { // If succeeded in sending the magic packet, begin the countdown
							if(mWolCounter != null) mWolCounter.cancel();
							mWolCounter = new WolCounter(host.wol_wait * 1000,1000);
							mWolCounter.start();
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
			mInfoManager.getSystemInfo(mUpdateVersionHandler, SystemInfo.SYSTEM_BUILD_VERSION, mActivity.getApplicationContext());
			mWolCounter = null;
		}

		@Override
		public void onTick(long millisUntilFinished) {
			textCount.setText("Waiting for " + millisUntilFinished/1000 + " more seconds...");						
		}
	}
	
	public class ProgressThread extends Thread {
		
		private Handler mHandlerOut;
		private Handler mHandlerIn;
		private int mTotal;
		private int mPosition;
		
		public final static int MSG_NEXT = 0;
		public final static int MSG_QUIT = 1;
		
		public final static String DATA_TYPE = "type";
		public final static String DATA_TOTAL = "total";
		public final static String DATA_POSITION = "pos";
		public final static String DATA_COVER = "cover";
		
		private final int mType;
		private final ProgressDialog mProgressDialog;
		
		public ProgressThread(Handler h, int type, ProgressDialog progressDialog) {
			super("Cover download progress Thread");
			mHandlerOut = h;
			mType = type;
			mProgressDialog = progressDialog;
		}
		
		public Handler getHandlerIn() {
			return mHandlerIn;
		}
		
		public ArrayList<ICoverArt> getCovers() {
			switch (mType) {
				case HomeActivity.MENU_COVER_DOWNLOAD_MOVIES:
					final IVideoManager vm = ManagerFactory.getVideoManager(HomeController.this);
					final ArrayList<Movie> movies = vm.getMovies(mActivity.getApplicationContext(), mCoverDownloadOffset);
					return new ArrayList<ICoverArt>(movies);
				case HomeActivity.MENU_COVER_DOWNLOAD_MUSIC:
					final IMusicManager mm = ManagerFactory.getMusicManager(HomeController.this);
					final ArrayList<Album> albums = mm.getAlbums(mActivity.getApplicationContext());
					return new ArrayList<ICoverArt>(albums);
				case HomeActivity.MENU_COVER_DOWNLOAD_ACTORS:
					final IVideoManager vm2 = ManagerFactory.getVideoManager(HomeController.this);
					final ArrayList<Actor> actors = vm2.getActors(mActivity.getApplicationContext());
					return new ArrayList<ICoverArt>(actors);
				default:
					return null;
			}
		}
		
		public void run() {
			if (DEBUG) Log.i(TAG, "[ProgressThread] Starting progress thread.");
			final ArrayList<ICoverArt> covers = getCovers();
			mTotal = covers.size();
			mPosition = 0;
			mProgressDialog.setMax(covers.size());
			boolean started = false;
			final WifiLock lock;
			if(HostFactory.host.wifi_only) {
				lock = WifiHelper.getInstance(mActivity).getNewWifiLock("BatchDownloader");
				lock.acquire();
			} else
				lock = null;
			Looper.prepare();
			mHandlerIn = new Handler() {
				public void handleMessage(Message msgIn) {
					switch (msgIn.what) {
						case MSG_NEXT:
							if (DEBUG) Log.i(TAG, "[ProgressThread] New message received, posting back new cover.");
							Message msgOut = mHandlerOut.obtainMessage();
							Bundle b = new Bundle();
							b.putInt(DATA_TOTAL, mTotal);
							b.putInt(DATA_POSITION, mPosition);
							b.putInt(DATA_TYPE, mType);
							if (mPosition < mTotal) {
								b.putSerializable(DATA_COVER, covers.get(mPosition));
							}
							msgOut.setData(b);
							mHandlerOut.sendMessage(msgOut);
							mPosition++;
						break;
						case MSG_QUIT:
							if (DEBUG) Log.i(TAG, "[ProgressThread] Exiting.");
							Looper.myLooper().quit();
							break;
					}
				}
			};
			if (!started) {
				started = true;
				Message msgStart = mHandlerOut.obtainMessage();
				Bundle b = new Bundle();
				b.putInt(DATA_TYPE, mType);
				msgStart.what = MSG_NEXT;
				msgStart.setData(b);
				mHandlerIn.sendMessage(msgStart);
				if (DEBUG) Log.i(TAG, "[ProgressThread] Not started, kicking on....");
			}
			Looper.loop();
			if(lock != null) lock.release();
		}
	}
	
	public void onHandleMessage(Message msg, ProgressDialog progressDialog, final ProgressThread progressThread) {
		int total = msg.getData().getInt(ProgressThread.DATA_TOTAL);
		int position = msg.getData().getInt(ProgressThread.DATA_POSITION);
		int type = msg.getData().getInt(ProgressThread.DATA_TYPE);
		if (total > 0) {
			progressDialog.setProgress(position);
			if (position < total) {
				final ICoverArt cover = (ICoverArt)msg.getData().getSerializable(ProgressThread.DATA_COVER); 
				if (DEBUG) Log.i(TAG, "New download message received for position " + position + ": " + cover.getName());
				ManagerFactory.getMusicManager(HomeController.this).getCover(new DataResponse<Bitmap>() {
					public void run() {
						if (DEBUG) Log.i(TAG, "Cover Downloaded, sending new (empty) message to progress thread.");
						progressThread.getHandlerIn().sendEmptyMessage(ProgressThread.MSG_NEXT);
					}
				}, cover, ThumbSize.BIG, null, mActivity.getApplicationContext());
			} else {
				mActivity.dismissDialog(type);
				progressThread.getHandlerIn().sendEmptyMessage(ProgressThread.MSG_QUIT);
				Toast toast = Toast.makeText(mActivity, total + " posters downloaded.", Toast.LENGTH_SHORT);
				toast.show();
			}
		} else {
			mActivity.dismissDialog(type);
			Toast toast = Toast.makeText(mActivity, "No posters downloaded, libary empty?", Toast.LENGTH_LONG);
			toast.show();
		}	
		mCoverDownloadOffset = position;
	}

	public void onActivityPause() {
		mInfoManager.setController(null);
		super.onActivityPause();
	}

	public void onActivityResume(Activity activity) {
		super.onActivityResume(activity);
		mInfoManager.setController(this);
		mInfoManager.getSystemInfo(mUpdateVersionHandler, SystemInfo.SYSTEM_BUILD_VERSION, mActivity.getApplicationContext());
	}
	
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("coverOffset", mCoverDownloadOffset);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	mCoverDownloadOffset = savedInstanceState.getInt("coverOffset");   
    }

}
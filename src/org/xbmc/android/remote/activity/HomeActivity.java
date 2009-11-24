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
import java.util.Observable;
import java.util.Observer;

import org.xbmc.android.backend.httpapi.HttpApiHandler;
import org.xbmc.android.backend.httpapi.HttpApiThread;
import org.xbmc.android.remote.ConfigurationManager;
import org.xbmc.android.remote.R;
import org.xbmc.android.remote.controller.FileListController;
import org.xbmc.android.remote.controller.ListController;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.android.util.ErrorHandler;
import org.xbmc.android.util.WakeOnLan;
import org.xbmc.eventclient.ButtonCodes;
import org.xbmc.eventclient.EventClient;
import org.xbmc.httpapi.BroadcastListener;
import org.xbmc.httpapi.client.MusicClient;
import org.xbmc.httpapi.client.VideoClient;
import org.xbmc.httpapi.data.Actor;
import org.xbmc.httpapi.data.Album;
import org.xbmc.httpapi.data.ICoverArt;
import org.xbmc.httpapi.data.Movie;
import org.xbmc.httpapi.info.SystemInfo;
import org.xbmc.httpapi.type.MediaType;
import org.xbmc.httpapi.type.SortType;
import org.xbmc.httpapi.type.ThumbSize;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class HomeActivity extends Activity implements OnItemClickListener, Observer {

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
	private static final int MENU_COVER_DOWNLOAD = 4;
	private static final int MENU_COVER_DOWNLOAD_MUSIC = 41;
	private static final int MENU_COVER_DOWNLOAD_MOVIES = 42;
	private static final int MENU_COVER_DOWNLOAD_ACTORS = 43;
	
	private static final String TAG = "HomeActivity";
	private static final boolean DEBUG = false;
	
	private ProgressThread mProgressThread;
    private ProgressDialog mProgressDialog;
	
	private HomeAdapter mHomeMenu;
	private HomeAdapter mOfflineMenu;
	
	private ConfigurationManager mConfigurationManager;
	
	private EventClient mClient;
	HttpApiHandler<String> mUpdateVersionHandler;
	
	public void update(Observable obj, Object arg) {
		if (arg instanceof BroadcastListener.Event) {
			BroadcastListener.Event event = (BroadcastListener.Event)arg;
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


	@Override
	public void onCreate(Bundle savedInstanceState) {
		
//		BroadcastListener bcl = BroadcastListener.getInstance(ConnectionManager.getHttpClient(this));
//		bcl.addObserver(this);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		mClient = ConnectionManager.getEventClient(this);
		ErrorHandler.setActivity(this);

		final ArrayList<HomeItem> homeItems = new ArrayList<HomeItem>();
		final ArrayList<HomeItem> offlineItems = new ArrayList<HomeItem>();
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		mUpdateVersionHandler = new HttpApiHandler<String>(this) {
			public void run() {
				if (!ConnectionManager.isNetworkAvailable(mActivity)) {
					((TextView) findViewById(R.id.HomeVersionTextView)).setText("No network");
				}
				if (!value.equals("")) {
					((TextView) findViewById(R.id.HomeVersionTextView)).setText("XBMC " + value);
					((GridView)findViewById(R.id.HomeItemGridView)).setAdapter(mHomeMenu);
					NowPlayingNotificationManager.getInstance(getBaseContext()).startNotificating();
				} else {
					((TextView) findViewById(R.id.HomeVersionTextView)).setText("Check Settings and retry");
					((GridView)findViewById(R.id.HomeItemGridView)).setAdapter(mOfflineMenu);
				}
			}
		};
		
//		ImportUtilities.purgeCache();
		
        final HomeItem remote = new HomeItem(HOME_ACTION_REMOTE, R.drawable.icon_home_remote, "Remote Control", "Use as");
        
		homeItems.add(remote);
		offlineItems.add(remote);
		
		homeItems.add(new HomeItem(HOME_ACTION_MUSIC, R.drawable.icon_home_music, "Music", "Listen to"));
		homeItems.add(new HomeItem(HOME_ACTION_VIDEOS, R.drawable.icon_home_movie, "Movies", "Watch your"));
		homeItems.add(new HomeItem(HOME_ACTION_PICTURES, R.drawable.icon_home_picture, "Pictures", "Browse your"));
		homeItems.add(new HomeItem(HOME_ACTION_NOWPLAYING, R.drawable.icon_home_playing, "Now Playing", "See what's"));
		
		offlineItems.add(new HomeItem(HOME_ACTION_RECONNECT, R.drawable.icon_home_reconnect, "Connect", "Try again to"));

		final String wolMac = prefs.getString("setting_wol", "");
		if (wolMac.compareTo("") != 0)
			offlineItems.add(new HomeItem(HOME_ACTION_WOL, R.drawable.icon_home_power, "Power On", "Turn your XBMC's"));

		mConfigurationManager = ConfigurationManager.getInstance(this);
		mConfigurationManager.initKeyguard();
		
		mHomeMenu = new HomeAdapter(this, homeItems);
		mOfflineMenu = new HomeAdapter(this, offlineItems);
		setHomeAdapter(mOfflineMenu);
		
        ((TextView) findViewById(R.id.HomeVersionTextView)).setText("Connecting...");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		menu.add(0, MENU_ABOUT, 0, "About").setIcon(R.drawable.menu_about);
		menu.add(0, MENU_SETTINGS, 0, "Settings").setIcon(R.drawable.menu_settings);
		SubMenu downloadMenu = menu.addSubMenu(0, MENU_COVER_DOWNLOAD, 0, "Download Covers").setIcon(R.drawable.menu_download);
		menu.add(0, MENU_EXIT, 0, "Exit").setIcon(R.drawable.menu_exit);
		
		downloadMenu.add(2, MENU_COVER_DOWNLOAD_MOVIES, 0, "Movie Posters");
		downloadMenu.add(2, MENU_COVER_DOWNLOAD_MUSIC, 0, "Album Covers");
		downloadMenu.add(2, MENU_COVER_DOWNLOAD_ACTORS, 0, "Actor Shots");
		
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
			NowPlayingNotificationManager.getInstance(getBaseContext()).removeNotification();
			System.exit(0);
			return true;
		case MENU_COVER_DOWNLOAD_MOVIES:
		case MENU_COVER_DOWNLOAD_MUSIC:
		case MENU_COVER_DOWNLOAD_ACTORS:
			showDialog(item.getItemId());
			return true;
		}
		return false;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		mProgressDialog = new ProgressDialog(HomeActivity.this);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		switch (id) {
			case MENU_COVER_DOWNLOAD_MOVIES:
				mProgressDialog.setMessage("Downloading movie posters...");
				mProgressThread = new ProgressThread(mHandler, MENU_COVER_DOWNLOAD_MOVIES);
	            break;
			case MENU_COVER_DOWNLOAD_MUSIC:
				mProgressDialog.setMessage("Downloading album covers...");
				mProgressThread = new ProgressThread(mHandler, MENU_COVER_DOWNLOAD_MUSIC);
				break;
			case MENU_COVER_DOWNLOAD_ACTORS:
				mProgressDialog.setMessage("Downloading actor thumbs...");
				mProgressThread = new ProgressThread(mHandler, MENU_COVER_DOWNLOAD_ACTORS);
				break;
			default:
				return null;
		}
		mProgressThread.start();
		return mProgressDialog;
	}

	@Override
	public void onResume(){
		super.onResume();
        HttpApiThread.info().getSystemInfo(mUpdateVersionHandler, SystemInfo.SYSTEM_BUILD_VERSION);
        mConfigurationManager.onActivityResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mConfigurationManager.onActivityPause();
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
			startActivity(new Intent(v.getContext(), MusicLibraryActivity.class));
			break;
		case HOME_ACTION_VIDEOS:
			Intent intent = new Intent(v.getContext(), MovieLibraryActivity.class);
			intent.putExtra(ListController.EXTRA_SHARE_TYPE, MediaType.VIDEO);
			startActivity(intent);
//			startActivity(createMediaIntent(MediaType.video, v));
			break;
		case HOME_ACTION_PICTURES:
			startActivity(createMediaIntent(MediaType.PICTURES, v));
			break;
		case HOME_ACTION_NOWPLAYING:
			startActivity(new Intent(v.getContext(), NowPlayingActivity.class));
			break;
		case HOME_ACTION_RECONNECT:
			((TextView) findViewById(R.id.HomeVersionTextView)).setText("Reconnecting...");
			HttpApiThread.info().getSystemInfo(mUpdateVersionHandler, SystemInfo.SYSTEM_BUILD_VERSION);
			break;
		case HOME_ACTION_WOL:
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			final String wolMac = prefs.getString("setting_wol", "");
			final int wolWait = Integer.parseInt(prefs.getString("setting_wol_wait", "40"));
			final int wolPort = Integer.parseInt(prefs.getString("setting_wol_port", "9"));
			
			WakeOnLan wol = new WakeOnLan();
			if (wol.sendMagicPacket(wolMac, wolPort)) { // If succeeded in sending the magic packet, begin the countdown
				WoLCounter counter = new WoLCounter(wolWait * 1000,1000);
				counter.start();
			}
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		try {
			switch (keyCode) {
				case KeyEvent.KEYCODE_VOLUME_UP:
					mClient.sendButton("R1", ButtonCodes.REMOTE_VOLUME_PLUS, false, true, true, (short)0, (byte)0);
					return true;
				case KeyEvent.KEYCODE_VOLUME_DOWN:
					mClient.sendButton("R1", ButtonCodes.REMOTE_VOLUME_MINUS, false, true, true, (short)0, (byte)0);
					return true;
			}
		} catch (IOException e) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	private Intent createMediaIntent(int mediaType, View v) {
		Intent nextActivity = new Intent(v.getContext(), ListActivity.class);
		nextActivity.putExtra(ListController.EXTRA_LIST_LOGIC, new FileListController());
		nextActivity.putExtra(ListController.EXTRA_SHARE_TYPE, mediaType);
		nextActivity.putExtra(ListController.EXTRA_PATH, "");
		return nextActivity;
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
	

	final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			int total = msg.getData().getInt(ProgressThread.DATA_TOTAL);
			int position = msg.getData().getInt(ProgressThread.DATA_POSITION);
			int type = msg.getData().getInt(ProgressThread.DATA_TYPE);
			if (total > 0) {
				mProgressDialog.setProgress(position);
				if (position < total) {
					final ICoverArt cover = (ICoverArt)msg.getData().getSerializable(ProgressThread.DATA_COVER); 
					if (DEBUG) Log.i(TAG, "New download message received for position " + position + ": " + cover.getName());
					HttpApiThread.video().getCover(new HttpApiHandler<Bitmap>(HomeActivity.this) {
						public void run() {
							if (DEBUG) Log.i(TAG, "Cover Downloaded, sending new (empty) message to progress thread.");
							mProgressThread.getHandlerIn().sendEmptyMessage(ProgressThread.MSG_NEXT);
						}
					}, cover, ThumbSize.BIG);
				} else {
					dismissDialog(type);
					mProgressThread.getHandlerIn().sendEmptyMessage(ProgressThread.MSG_QUIT);
					Toast toast = Toast.makeText(HomeActivity.this, total + " posters downloaded.", Toast.LENGTH_SHORT);
					toast.show();
				}
			} else {
				dismissDialog(type);
				Toast toast = Toast.makeText(HomeActivity.this, "No posters downloaded, libary empty?", Toast.LENGTH_LONG);
				toast.show();
			}
		}
	};


	private class ProgressThread extends Thread {
		
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
		
		ProgressThread(Handler h, int type) {
			super("Cover download progress Thread");
			mHandlerOut = h;
			mType = type;
		}
		
		public Handler getHandlerIn() {
			return mHandlerIn;
		}
		
		private ArrayList<ICoverArt> getCovers() {
			switch (mType) {
				case MENU_COVER_DOWNLOAD_MOVIES:
					final VideoClient vc = ConnectionManager.getHttpClient(HomeActivity.this).video;
					final ArrayList<Movie> movies = vc.getMovies(SortType.DONT_SORT, SortType.ORDER_ASC);
					return new ArrayList<ICoverArt>(movies);
				case MENU_COVER_DOWNLOAD_MUSIC:
					final MusicClient mc = ConnectionManager.getHttpClient(HomeActivity.this).music;
					final ArrayList<Album> albums = mc.getAlbums(SortType.DONT_SORT, SortType.ORDER_ASC);
					return new ArrayList<ICoverArt>(albums);
				case MENU_COVER_DOWNLOAD_ACTORS:
					final VideoClient vc2 = ConnectionManager.getHttpClient(HomeActivity.this).video;
					final ArrayList<Actor> actors = vc2.getActors();
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
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

package org.xbmc.android.remote.presentation.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.presentation.controller.HomeController;
import org.xbmc.android.remote.presentation.preference.Host;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.eventclient.ButtonCodes;
import org.xbmc.eventclient.EventClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

public class HomeActivity extends Activity {

	private static final int MENU_ABOUT = 1;
	private static final int MENU_SETTINGS = 2;
	private static final int MENU_EXIT = 3;
//	private static final int MENU_COVER_DOWNLOAD = 4;
	private static final int MENU_COVER_DOWNLOAD_MUSIC = 41;
	private static final int MENU_COVER_DOWNLOAD_MOVIES = 42;
	private static final int MENU_COVER_DOWNLOAD_ACTORS = 43;
	
	private static final String TAG = "HomeActivity";
	private static final boolean DEBUG = false;
	
	private ProgressThread mProgressThread;
    private ProgressDialog mProgressDialog;
	
	private ConfigurationManager mConfigurationManager;
	private HomeController mHomeController;
	
	private EventClient mClient;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		
		final Button versionButton = (Button)findViewById(R.id.home_version_button);
		final GridView menuGrid = (GridView)findViewById(R.id.HomeItemGridView);
		mHomeController = new HomeController(this, menuGrid);

		mClient = ConnectionManager.getEventClient(this);
		
		mHomeController.setupVersionHandler(versionButton, menuGrid);
		
		mConfigurationManager = ConfigurationManager.getInstance(this);
		mConfigurationManager.initKeyguard();
		
//		ImportUtilities.purgeCache();
		
		versionButton.setText("Connecting...");
		versionButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// granted, this is butt-ugly. better ideas, be my guest.
				final ArrayList<Host> hosts = Host.getHosts(HomeActivity.this);
				final HashMap<Integer, Host> hostMap = new HashMap<Integer, Host>();
				final CharSequence[] names = new CharSequence[hosts.size()];
				int i = 0;
				for (Host host : hosts) {
					names[i] = host.name;
					hostMap.put(i, host);
					i++;
				}
				if (hosts.size() > 0) {
					AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
					builder.setTitle("Choose your XBMC!");
					builder.setItems(names, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							final Host host = hostMap.get(which);
							Toast.makeText(getApplicationContext(), host.name, Toast.LENGTH_SHORT).show();
						}
					});
					AlertDialog dialog = builder.create();
					dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
					dialog.show();
				} else {
					Toast.makeText(getApplicationContext(), "No XBMC hosts defined, please do that first.", Toast.LENGTH_LONG).show();
					Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
					intent.putExtra(SettingsActivity.JUMP_TO, SettingsActivity.JUMP_TO_INSTANCES);
					startActivity(intent);
				}

			}
		});
		
		((Button)findViewById(R.id.home_about_button)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(HomeActivity.this, AboutActivity.class));
			}
		});
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		menu.add(0, MENU_ABOUT, 0, "About").setIcon(R.drawable.menu_about);
		menu.add(0, MENU_SETTINGS, 0, "Settings").setIcon(R.drawable.menu_settings);
//		SubMenu downloadMenu = menu.addSubMenu(0, MENU_COVER_DOWNLOAD, 0, "Download Covers").setIcon(R.drawable.menu_download);
		menu.add(0, MENU_EXIT, 0, "Exit").setIcon(R.drawable.menu_exit);
		
//		downloadMenu.add(2, MENU_COVER_DOWNLOAD_MOVIES, 0, "Movie Posters");
//		downloadMenu.add(2, MENU_COVER_DOWNLOAD_MUSIC, 0, "Album Covers");
//		downloadMenu.add(2, MENU_COVER_DOWNLOAD_ACTORS, 0, "Actor Shots");
		
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
		mHomeController.onActivityResume(this);
        mConfigurationManager.onActivityResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mHomeController.onActivityPause();
		mConfigurationManager.onActivityPause();
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
/* TODO adapt to new model
 * 					ManagerThread.music().getCover(new DataResponse<Bitmap>() {
						public void run() {
							if (DEBUG) Log.i(TAG, "Cover Downloaded, sending new (empty) message to progress thread.");
							mProgressThread.getHandlerIn().sendEmptyMessage(ProgressThread.MSG_NEXT);
						}
					}, cover, ThumbSize.BIG);*/
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
/*			switch (mType) {
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
			}*/
			return null;
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

}
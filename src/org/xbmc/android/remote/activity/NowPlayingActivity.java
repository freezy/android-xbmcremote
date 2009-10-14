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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.xbmc.android.remote.R;
import org.xbmc.android.util.Base64;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.android.util.ErrorHandler;
import org.xbmc.eventclient.ButtonCodes;
import org.xbmc.eventclient.EventClient;
import org.xbmc.httpapi.client.ControlClient;
import org.xbmc.httpapi.client.InfoClient;
import org.xbmc.httpapi.client.ControlClient.ICurrentlyPlaying;
import org.xbmc.httpapi.data.Song;
import org.xbmc.httpapi.type.MediaType;
import org.xbmc.httpapi.type.SeekType;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class NowPlayingActivity extends Activity implements Callback {
	
	public static final int MESSAGE_COVER_IMAGE = 668;
	public static final int MESSAGE_ARTIST_TEXT_VIEW = 667;
	public static final int MESSAGE_NOW_PLAYING_PROGRESS = 666;
	public static final int MESSAGE_CONNECTION_ERROR = 1;
	
	public static final String BUNDLE_CURRENTLY_PLAYING = "CurrentlyPlaying";
	
	private InfoClient mInfo;
	private ControlClient mControl;
	private EventClient mClient;
	
	private Handler mNowPlayingHandler;
	private UpdateThread mUpdateThread;

	private String mLastPos = "-1";
	private String mCoverPath;
	private Drawable mCover;
	
	private TextView mAlbumView;
	private TextView mArtistView;
	private TextView mSongTitleView;
	private TextView mCounterLeftView;
	private TextView mCounterRightView;
	private ImageButton mPlayPauseView;
	private SeekBar mSeekBar;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ErrorHandler.setActivity(this);
        WindowManager wm = getWindowManager(); 
        Display d = wm.getDefaultDisplay();
        requestWindowFeature(Window.FEATURE_NO_TITLE);

//        if (d.getWidth() > d.getHeight())
 //       	setContentView(R.layout.nowplaying_landscape);
  //      else
       	setContentView(R.layout.nowplaying_portrait);
        
  	  	mControl = ConnectionManager.getHttpClient(this).control;
  	  	mInfo = ConnectionManager.getHttpClient(this).info;
  	  	mClient = ConnectionManager.getEventClient(this);
  	  	
		mSeekBar = (SeekBar) findViewById(R.id.NowPlayingProgress);
		mArtistView = (TextView) findViewById(R.id.ArtistTextView);
		mAlbumView = (TextView) findViewById(R.id.AlbumTextView);
		mSongTitleView = (TextView) findViewById(R.id.SongTextView);
		mCounterLeftView = (TextView)findViewById(R.id.now_playing_counter_left);
		mCounterRightView = (TextView)findViewById(R.id.now_playing_counter_right);
		mPlayPauseView = (ImageButton)findViewById(R.id.MediaPlayPauseButton);
		
  	  	
		// remove nasty top fading edge
		FrameLayout topFrame = (FrameLayout)findViewById(android.R.id.content);
		topFrame.setForeground(null);
  	  	
		// set titlebar text
  	  	((TextView)findViewById(R.id.titlebar_text)).setText("Now playing");
  	  	
  	  	mNowPlayingHandler = new Handler(this);
  	  	setupButtons();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		mUpdateThread = new UpdateThread(getResources());
		mUpdateThread.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		mUpdateThread.interrupt();
	}
	
	private void setupButtons() {
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser && !seekBar.isInTouchMode())
					mControl.seek(SeekType.absolute, progress);
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				mControl.seek(SeekType.absolute, seekBar.getProgress());
			}
		});
		
		// previous
		setupButton(R.id.MediaPreviousButton, ButtonCodes.REMOTE_SKIP_MINUS, R.drawable.now_playing_previous, R.drawable.now_playing_previous_down);
		// stop
		setupButton(R.id.MediaStopButton, ButtonCodes.REMOTE_STOP, R.drawable.now_playing_stop, R.drawable.now_playing_stop_down);
		// pause
		setupButton(R.id.MediaPlayPauseButton, ButtonCodes.REMOTE_PAUSE, R.drawable.now_playing_pause, R.drawable.now_playing_pause_down);
		// next
		setupButton(R.id.MediaNextButton, ButtonCodes.REMOTE_SKIP_PLUS, R.drawable.now_playing_next, R.drawable.now_playing_next_down);

	}
	
	/**
	 * Shortcut for adding the listener class to the button
	 * @param resourceButton       Resource ID of the button
	 * @param action               Action string
	 * @param resourceButtonUp     Resource ID of the button up image
	 * @param resourceButtonDown   Resource ID of the button down image
	 */
	private void setupButton(int resourceButton, String action, int resourceButtonUp, int resourceButtonDown) {
		findViewById(resourceButton).setOnTouchListener(new OnRemoteAction(action, resourceButtonUp, resourceButtonDown));		
	}
	

	
	Thread updateViewThread;
	
	public synchronized boolean handleMessage(Message msg) {
		
		final Bundle data = msg.getData();
		final ICurrentlyPlaying currentlyPlaying = (ICurrentlyPlaying)data.getSerializable(BUNDLE_CURRENTLY_PLAYING);

		switch (msg.what) {
		case MESSAGE_NOW_PLAYING_PROGRESS: 
			mSeekBar.setProgress(Math.round(currentlyPlaying.getPercentage()));
			
			if (currentlyPlaying.isPlaying()) {
				mSeekBar.setEnabled(true);
				mCounterLeftView.setText(Song.getDuration(currentlyPlaying.getTime() + 1));
				mCounterRightView.setText("-" + Song.getDuration(currentlyPlaying.getDuration() - currentlyPlaying.getTime() - 1));
				setupButton(R.id.MediaPlayPauseButton, ButtonCodes.REMOTE_PAUSE, R.drawable.now_playing_play, R.drawable.now_playing_play_down);
				mPlayPauseView.setBackgroundResource(R.drawable.now_playing_pause);
			} else {
				mSeekBar.setEnabled(false);
				mCounterLeftView.setText("");
				mCounterRightView.setText("");
				setupButton(R.id.MediaPlayPauseButton, ButtonCodes.REMOTE_PAUSE, R.drawable.now_playing_pause, R.drawable.now_playing_pause_down);
				mPlayPauseView.setBackgroundResource(R.drawable.now_playing_play);
			}
			return true;
		
		case MESSAGE_ARTIST_TEXT_VIEW:

			mArtistView.setText(currentlyPlaying.getArtist());
	  	  	mAlbumView.setText(currentlyPlaying.getAlbum());
	  	  	mSongTitleView.setText(currentlyPlaying.getTitle());
	  	  	
	  	  	return true;
	  	  	
		case MESSAGE_COVER_IMAGE:
			final ImageView cover = (ImageView) findViewById(R.id.CoverImage);
			cover.setImageDrawable(mCover);
			return true;
			
		case MESSAGE_CONNECTION_ERROR:
			ErrorHandler handler = new ErrorHandler(this);
			handler.handle(new SocketTimeoutException());
			setResult(MESSAGE_CONNECTION_ERROR);
			finish();
			return true;
			
		default:
			return false;
		}
	}
	
	private synchronized void setCover(Drawable cover) {
		mCover = cover;
		mNowPlayingHandler.sendEmptyMessage(MESSAGE_COVER_IMAGE);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, "Music");
		menu.add(0, 2, 0, "Video");
		menu.add(0, 3, 0, "Pictures").setIcon(android.R.drawable.ic_menu_camera);
		
		menu.add(0, 5, 0, "Remote");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent myIntent = null;
		
		switch (item.getItemId()) {
		case 1:
			myIntent = new Intent(this, FileListActivity.class);
			myIntent.putExtra("shareType", MediaType.music.toString());
			break;
		case 2:
			myIntent = new Intent(this, FileListActivity.class);
			myIntent.putExtra("shareType", MediaType.video.toString());
			break;
		case 3:
			myIntent = new Intent(this, FileListActivity.class);
			myIntent.putExtra("shareType", MediaType.pictures.toString());
			break;
		case 5:
			myIntent = new Intent(this, RemoteActivity.class);
			break;
		}
		
		if (myIntent != null) {
			startActivity(myIntent);
			return true;
		}
		return false;
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


	private byte[] download(String pathToDownload) {
		try {
			final URL url = new URL(pathToDownload);
			final URLConnection uc = url.openConnection();
			
			final BufferedReader rd = new BufferedReader(new InputStreamReader(uc.getInputStream()), 8192);
			
			final StringBuilder sb = new StringBuilder();
			String line = "";
			while ((line = rd.readLine()) != null) {    
				sb.append(line);
			}
			
			rd.close();
			return Base64.decode(sb.toString().replace("<html>", "").replace("</html>", ""));
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Handles the push- release button code. Switches image of the pressed
	 * button, vibrates and executes command.
	 */
	private class OnRemoteAction implements OnTouchListener {
		private final String mAction;
		private final int mUp, mDown;
		public OnRemoteAction(String action, int up, int down) {
			mAction = action;
			mUp = up;
			mDown = down;
		}
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				try {
					mClient.sendButton("R1", mAction, true, true, true, (short)0, (byte)0);
				} catch (IOException e) {
					return false;
				}
				((ImageButton)v).setBackgroundResource(mDown);
				return true;
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				try {
					mClient.sendButton("R1", mAction, false, false, true, (short)0, (byte)0);
				} catch (IOException e) {
					return false;
				}
				((ImageButton)v).setBackgroundResource(mUp);
				return true;
			}
			return false;
		}
	}
	
	
	private class UpdateThread extends Thread {
		
		private Resources mResources;
		
		public UpdateThread(Resources resources) {
			mResources = resources;
		}
		
		public void run() {
			while (!isInterrupted()) {
				Message msg = new Message();
				Bundle bundle = msg.getData();
				if (!mControl.isConnected()) {
					msg.what = MESSAGE_CONNECTION_ERROR;
					bundle.putSerializable(BUNDLE_CURRENTLY_PLAYING, null);
					mNowPlayingHandler.sendMessage(msg);
				} else {
					
					final ICurrentlyPlaying currPlaying = mControl.getCurrentlyPlaying();
					bundle.putSerializable(BUNDLE_CURRENTLY_PLAYING, currPlaying);

					msg.what = MESSAGE_NOW_PLAYING_PROGRESS;
					
					String currentPos = currPlaying.getTitle() + currPlaying.getDuration();
					mNowPlayingHandler.sendMessage(msg);
					
					if (!mLastPos.equals(currentPos)) {
						mLastPos = currentPos;
						msg = new Message();
						bundle = msg.getData();
						bundle.putSerializable(BUNDLE_CURRENTLY_PLAYING, currPlaying);
				  	  	msg.what = MESSAGE_ARTIST_TEXT_VIEW;
				  	  	
						mNowPlayingHandler.sendMessage(msg);
						
						try {				
							String downloadURI = mInfo.getCurrentlyPlayingThumbURI();
			
							if (downloadURI != null && downloadURI.length() > 0) {
								if (!downloadURI.equals(mCoverPath)) {
						  	  		mCoverPath = downloadURI;
//						  	  		setCover(mResources.getDrawable(R.drawable.cover_downloading));
			
						  	  		byte[] buffer = download(downloadURI);
			
						  	  		if (buffer == null || buffer.length == 0)
						  	  			setCover(null);
						  	  		else
						  	  			setCover(new BitmapDrawable(BitmapFactory.decodeByteArray(buffer, 0, buffer.length)));
								}
							} else {
								mCoverPath = null;
								setCover(null);
							}
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (URISyntaxException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					return;
				}
			}
			
		}
	}
		
}

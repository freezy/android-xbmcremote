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
import android.view.View;
import android.view.WindowManager;
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
	
	private Handler mNowPlayingHandler;
	private UpdateThread mUpdateThread;

	private String mLastPos = "-1";
	private String mCoverPath;
	private Drawable mCover;
	
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
						  	  		setCover(mResources.getDrawable(R.drawable.cover_downloading));
			
						  	  		byte[] buffer = download(downloadURI);
			
						  	  		if (buffer == null || buffer.length == 0)
						  	  			setCover(mResources.getDrawable(R.drawable.nocover));
						  	  		else
						  	  			setCover(new BitmapDrawable(BitmapFactory.decodeByteArray(buffer, 0, buffer.length)));
								}
							} else {
								mCoverPath = null;
								setCover(mResources.getDrawable(R.drawable.nocover));
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
			}
			
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				return;
			}
		}
	}
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ErrorHandler.setActivity(this);
        WindowManager wm = getWindowManager(); 
        Display d = wm.getDefaultDisplay();

        if (d.getWidth() > d.getHeight())
        	setContentView(R.layout.nowplaying_landscape);
        else
        	setContentView(R.layout.nowplaying_portrait);
        
  	  	mControl = ConnectionManager.getHttpClient(this).control;
  	  	mInfo = ConnectionManager.getHttpClient(this).info;
  	  	
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
		final SeekBar seekBar = (SeekBar) findViewById(R.id.NowPlayingProgress);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

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
  	  	
        final ImageButton PlayPrevButton = (ImageButton) findViewById(R.id.MediaPreviousButton);
		PlayPrevButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mControl.playPrevious();
			}
		});
		final ImageButton PlayButton = (ImageButton) findViewById(R.id.MediaPlayPauseButton);
		PlayButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mControl.pause();
			}
		});
		
		final ImageButton PlayNextButton = (ImageButton) findViewById(R.id.MediaNextButton);
		PlayNextButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mControl.playNext();
			}
		});
	}

	Thread updateViewThread;
	
	public synchronized boolean handleMessage(Message msg) {
		
		final Bundle data = msg.getData();
		final ICurrentlyPlaying currentlyPlaying = (ICurrentlyPlaying)data.getSerializable(BUNDLE_CURRENTLY_PLAYING);

		switch (msg.what) {
		case MESSAGE_NOW_PLAYING_PROGRESS: 
			final SeekBar seekBar = (SeekBar) findViewById(R.id.NowPlayingProgress);
			seekBar.setProgress(Math.round(currentlyPlaying.getPercentage()));
			
			final ImageButton PlayPauseButton = (ImageButton) findViewById(R.id.MediaPlayPauseButton);
			boolean isPlaying = data.getBoolean("isplaying");
			PlayPauseButton.setImageResource(isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
			return true;
		
		case MESSAGE_ARTIST_TEXT_VIEW:
			final TextView artist = (TextView) findViewById(R.id.ArtistTextView);
			final TextView album = (TextView) findViewById(R.id.AlbumTextView);
			final TextView song = (TextView) findViewById(R.id.SongTextView);

			artist.setText(currentlyPlaying.getArtist());
	  	  	album.setText(currentlyPlaying.getAlbum());
	  	  	song.setText(currentlyPlaying.getTitle());
	  	  	
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
}

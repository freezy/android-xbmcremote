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
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.xbmc.android.remote.R;
import org.xbmc.android.util.Base64;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.android.util.DownloadCallback;
import org.xbmc.android.util.DownloadThread;
import org.xbmc.android.util.ErrorHandler;
import org.xbmc.httpapi.client.ControlClient;
import org.xbmc.httpapi.client.InfoClient;
import org.xbmc.httpapi.client.InfoClient.CurrentlyPlaying;
import org.xbmc.httpapi.info.MusicInfo;
import org.xbmc.httpapi.type.SeekType;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class NowPlayingActivity extends Activity implements Callback, Runnable {
	private ControlClient control;
	private InfoClient info;
	private Handler nowPlayingHandler;
	private String lastPos = "-1";
	private String mCoverPath;
	private Drawable mCover;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ErrorHandler.setActivity(this);
        WindowManager wm = getWindowManager(); 
        Display d = wm.getDefaultDisplay();

        if (d.getWidth() > d.getHeight())
        	setContentView(R.layout.nowplaying_landscape);
        else
        	setContentView(R.layout.nowplaying_portrait);
        
  	  	control = ConnectionManager.getHttpClient(this).control;
  	  	info = ConnectionManager.getHttpClient(this).info;
  	  	
  	  	nowPlayingHandler = new Handler(this);
  	  	setupButtons();
  	  	nowPlayingHandler.sendEmptyMessage(1);
	}

	private void setupButtons() {
		final SeekBar seekBar = (SeekBar) findViewById(R.id.NowPlayingProgress);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser && !seekBar.isInTouchMode())
					control.seek(SeekType.absolute, progress);
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				control.seek(SeekType.absolute, seekBar.getProgress());
			}
		});
  	  	
        final ImageButton PlayPrevButton = (ImageButton) findViewById(R.id.MediaPreviousButton);
		PlayPrevButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (control.playPrevious())
					nowPlayingHandler.sendEmptyMessage(1);
			}
		});
		final ImageButton PlayButton = (ImageButton) findViewById(R.id.MediaPlayPauseButton);
		PlayButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (control.pause())
					nowPlayingHandler.sendEmptyMessage(1);
			}
		});
		
		final ImageButton PlayNextButton = (ImageButton) findViewById(R.id.MediaNextButton);
		PlayNextButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (control.playNext())
					nowPlayingHandler.sendEmptyMessage(1);
			}
		});
	}

	Thread updateViewThread;
	
	public boolean handleMessage(Message msg) {
		if (msg.what == 1) {
			if (updateViewThread == null || !updateViewThread.isAlive()) {
				updateViewThread = new Thread(this);
				updateViewThread.start();
			}
			nowPlayingHandler.sendEmptyMessageDelayed(1, 1000);
			return true;
		} else if (msg.what == 666) {
			Bundle data = msg.getData();
			final SeekBar seekBar = (SeekBar) findViewById(R.id.NowPlayingProgress);
			seekBar.setProgress(data.getInt("progress"));
			
			final ImageButton PlayPauseButton = (ImageButton) findViewById(R.id.MediaPlayPauseButton);
			boolean isPlaying = data.getBoolean("isplaying");
			PlayPauseButton.setImageResource(isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
			return true;
		} else if (msg.what == 667) {
			Bundle data = msg.getData();
			final TextView artist = (TextView) findViewById(R.id.ArtistTextView);
			String sa =data.getString("artist"); 
	  	  	artist.setText(data.getString("artist"));
	  	  	
	  	  	final TextView album = (TextView) findViewById(R.id.AlbumTextView);
	  	  	String sal = data.getString("album");
	  	  	album.setText(data.getString("album"));
	  	  	
	  	  	final TextView song = (TextView) findViewById(R.id.SongTextView);
	  	  	String st = data.getString("title");
	  	  	song.setText(data.getString("title"));
	  	  	return true;
		} else if (msg.what == 668) {
			final ImageView cover = (ImageView) findViewById(R.id.CoverImage);
			cover.setImageDrawable(mCover);
			return true;
		}
		return false;
	}

	public void run() {
		Message msg = new Message();
		Bundle bundle = msg.getData();
		msg.what = 666;
		
		bundle.putInt("progress", control.getPercentage());
		
		CurrentlyPlaying currPlaying = info.getCurrentlyPlaying();
		bundle.putBoolean("isplaying", currPlaying != null && currPlaying.isPlaying);
		
		String currentPos = info.getMusicInfo(MusicInfo.MUSICPLAYER_TITLE) + info.getMusicInfo(MusicInfo.MUSICPLAYER_DURATION);
		nowPlayingHandler.sendMessage(msg);
		
		if (!lastPos.equals(currentPos)) {
			lastPos = currentPos;
			msg = new Message();
			bundle = msg.getData();
	  	  	msg.what = 667;
			
	  	  	String album = info.getMusicInfo(MusicInfo.MUSICPLAYER_ALBUM);
	  	  	String artist = info.getMusicInfo(MusicInfo.MUSICPLAYER_ARTIST);
	  	  	String title = info.getMusicInfo(MusicInfo.MUSICPLAYER_TITLE);
	  	  	
			bundle.putString("album", album);
			bundle.putString("artist", artist);
			bundle.putString("title", title);
			
			nowPlayingHandler.sendMessage(msg);
			
			try {				
				String downloadURI = info.getCurrentlyPlayingThumbURI();

				if (downloadURI != null && downloadURI.length() > 0) {
					if (!downloadURI.equals(mCoverPath)) {
			  	  		mCover = this.getResources().getDrawable(R.drawable.cover_downloading);
			  	  		mCoverPath = downloadURI;
			  	  		nowPlayingHandler.sendEmptyMessage(668);
			  	  		
				  	  	/*Thread downloadThread = new DownloadThread(new String[] { downloadURI }, this);
				  	  	downloadThread.start();*/
			  	  		
			  	  		byte[] buffer = download(downloadURI);

			  	  		if (buffer != null) {
					  	  	mCover = new BitmapDrawable(BitmapFactory.decodeByteArray(buffer, 0, buffer.length));
					  	  	nowPlayingHandler.sendEmptyMessage(668);
			  	  		}
					}
				} else {
					mCover = this.getResources().getDrawable(R.drawable.nocover);
					mCoverPath = null;
					nowPlayingHandler.sendEmptyMessage(668);
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

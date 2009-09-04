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
import org.xbmc.android.util.ErrorHandler;
import org.xbmc.httpapi.client.ControlClient;
import org.xbmc.httpapi.client.InfoClient;
import org.xbmc.httpapi.info.MusicInfo;
import org.xbmc.httpapi.type.SeekType;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Handler.Callback;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class NowPlayingActivity extends Activity implements Callback {
	private ControlClient control;
	private InfoClient info;
	private Handler progressHandler;
	
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
  	  	
  	  	setupButtons();
  	  	updatePlayingInfo();
  	  	progressHandler = new Handler(this);
  	  	progressHandler.sendEmptyMessageDelayed(1234, 1000);
	}

	private void setupPlayingInfo() {
		final TextView artist = (TextView) findViewById(R.id.ArtistTextView);
  	  	artist.setText(info.getMusicInfo(MusicInfo.MUSICPLAYER_ARTIST));
  	  	
  	  	final TextView album = (TextView) findViewById(R.id.AlbumTextView);
  	  	album.setText(info.getMusicInfo(MusicInfo.MUSICPLAYER_ALBUM));
  	  	
  	  	final TextView song = (TextView) findViewById(R.id.SongTextView);
  	  	song.setText(info.getMusicInfo(MusicInfo.MUSICPLAYER_TITLE));
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
				control.playPrevious();
				updatePlayingInfo();
			}
		});
		final ImageButton PlayButton = (ImageButton) findViewById(R.id.MediaPlayPauseButton);
		PlayButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				control.pause();
			}
		});
		
		final ImageButton PlayNextButton = (ImageButton) findViewById(R.id.MediaNextButton);
		PlayNextButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				control.playNext();
				updatePlayingInfo();
			}
		});
	}

	public boolean handleMessage(Message msg) {
		if (msg.what == 1234) {
			updatePlayingInfo();
			progressHandler.sendEmptyMessageDelayed(1234, 1000);
			return true;
		}
		return false;
	}

	private int lastPos = -1;
	
	private void updatePlayingInfo() {
		final SeekBar seekBar = (SeekBar) findViewById(R.id.NowPlayingProgress);
		int progress = control.getPercentage();
		seekBar.setProgress(progress);
		
		int currentPos = Integer.parseInt(info.getMusicInfo(MusicInfo.MUSICPLAYER_PLAYLISTPOS));
		if (currentPos != lastPos) {
			setupPlayingInfo();
			lastPos = currentPos;
		}
	}
}

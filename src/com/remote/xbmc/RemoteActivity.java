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

package com.remote.xbmc;

import java.io.IOException;

import org.xbmc.httpapi.MediaControl;
import org.xbmc.httpapi.Message;
import org.xbmc.httpapi.UrgancyLevel;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class RemoteActivity extends Activity {
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		char key = (char)event.getUnicodeChar();
		
		if (key > 'A' && key < 'z')
		{
			try
			{
				XBMCControl.getEventClientInstance(this).sendButton("KB", "" + key, false, true, true, (short)0, (byte)0);
				return true;
			} catch (IOException e) {
				XBMCControl.getHttpApiInstance(this).getMessenger().add(new Message(UrgancyLevel.error, e.getMessage()));
			}
		}
		
		return super.onKeyDown(keyCode, event);
	}

	private MediaControl control;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remotemain);

  	  	control = XBMCControl.getHttpApiInstance(this).getMediaControls();
  	  	
		setupMediaControlButtons();
		setupNavButtons();
    }
    
	private void setupNavButtons() {
		final Button NavSelectButton = (Button) findViewById(R.id.NavSelectButton);
		NavSelectButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					XBMCControl.getEventClientInstance(RemoteActivity.this).sendButton("KB", "enter", false, true, true, (short)0, (byte)0);
				} catch (IOException e) { }
			}
		});
		
		final ImageButton NavUpButton = (ImageButton) findViewById(R.id.NavUpButton);
		NavUpButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					XBMCControl.getEventClientInstance(RemoteActivity.this).sendButton("KB", "up", false, true, true, (short)0, (byte)0);
				} catch (IOException e) { }
			}
		});
		final ImageButton NavDownButton = (ImageButton) findViewById(R.id.NavDownButton);
		NavDownButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					XBMCControl.getEventClientInstance(RemoteActivity.this).sendButton("KB", "down", false, true, true, (short)0, (byte)0);
				} catch (IOException e) { }
			}
		});
		final ImageButton NavLeftButton = (ImageButton) findViewById(R.id.NavLeftButton);
		NavLeftButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					XBMCControl.getEventClientInstance(RemoteActivity.this).sendButton("KB", "left", false, true, true, (short)0, (byte)0);
				} catch (IOException e) { }
			}
		});
		final ImageButton NavRightButton = (ImageButton) findViewById(R.id.NavRightButton);
		NavRightButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					XBMCControl.getEventClientInstance(RemoteActivity.this).sendButton("KB", "right", false, true, true, (short)0, (byte)0);
				} catch (IOException e) { }
			}
		});
	}

	private void setupMediaControlButtons() {
/*		final ImageButton PlayPrevButton = (ImageButton) findViewById(R.id.MediaPreviousButton);
		PlayPrevButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				control.playPrevious();
			}
		});
		final ImageButton PlayButton = (ImageButton) findViewById(R.id.MediaPlayButton);
		PlayButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				control.pause();
			}
		});
		
		final ImageButton StopButton = (ImageButton) findViewById(R.id.MediaStopButton);
		StopButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				control.stop();
			}
		});
		
		final ImageButton PlayNextButton = (ImageButton) findViewById(R.id.MediaNextButton);
		PlayNextButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				control.playNext();
			}
		});*/
	}
}
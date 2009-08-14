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
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class RemoteActivity extends Activity {
	MediaControl control;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remotemain);
        
        control = XBMCControl.getHttpApiInstance(this).getMediaControls();
  	  	
		setupButtons();
    }

	private void setupButtons() {
		
		final Button GoBackButton = (Button) findViewById(R.id.GoBackButton);
		GoBackButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				KeyboardAction("backspace");
			}
		});
		final ImageButton PlayPrevButton = (ImageButton) findViewById(R.id.MediaPreviousButton);
		PlayPrevButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				control.playPrevious();
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
			}
		});
		
	    final Button GoNowPlayingButton = (Button) findViewById(R.id.GoNowPlayingButton);
	    GoNowPlayingButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), LogViewer.class);
                startActivityForResult(myIntent, 0);
			}
		});
	}
	
	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN)
			return KeyboardAction("enter");
		else if (Math.abs(event.getX()) > 0.1f)
			return KeyboardAction(event.getX() < 0 ? "left" : "right");
		else if (Math.abs(event.getY()) > 0.1f)
			return KeyboardAction(event.getY() < 0 ? "up" : "down");
		
		return false;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		char key = (char)event.getUnicodeChar();
		
		if (key > 'A' && key < 'z')
			return KeyboardAction("" + key);
		
		return super.onKeyDown(keyCode, event);
	}
	
	private boolean KeyboardAction(String button) {
		try {
			XBMCControl.getEventClientInstance(RemoteActivity.this).sendButton("KB", button, false, true, true, (short)0, (byte)0);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
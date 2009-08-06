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

import org.xbmc.httpapi.MediaControl;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class RemoteActivity extends Activity {
	private MediaControl control;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remotemain);

  	  	control = XBMCControl.getInstance(getSharedPreferences(XBMCControl.PREFS_NAME, 0)).getMediaControls();
  	  	
		setupMediaControlButtons();
		setupNavButtons();
    }
    
	private void setupNavButtons() {
		final Button NavSelectButton = (Button) findViewById(R.id.NavSelectButton);
		NavSelectButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				control.navSelect();
			}
		});
		
		final ImageButton NavUpButton = (ImageButton) findViewById(R.id.NavUpButton);
		NavUpButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				control.navUp();
			}
		});
		final ImageButton NavDownButton = (ImageButton) findViewById(R.id.NavDownButton);
		NavDownButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				control.navDown();
			}
		});
		final ImageButton NavLeftButton = (ImageButton) findViewById(R.id.NavLeftButton);
		NavLeftButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				control.navLeft();
			}
		});
		final ImageButton NavRightButton = (ImageButton) findViewById(R.id.NavRightButton);
		NavRightButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				control.navRight();
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
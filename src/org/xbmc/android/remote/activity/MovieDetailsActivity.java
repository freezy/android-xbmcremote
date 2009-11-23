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

import org.xbmc.android.backend.httpapi.HttpApiHandler;
import org.xbmc.android.backend.httpapi.HttpApiThread;
import org.xbmc.android.remote.ConfigurationManager;
import org.xbmc.android.remote.R;
import org.xbmc.android.remote.controller.ListController;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.android.util.ErrorHandler;
import org.xbmc.eventclient.ButtonCodes;
import org.xbmc.eventclient.EventClient;
import org.xbmc.httpapi.data.Movie;
import org.xbmc.httpapi.type.ThumbSize;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MovieDetailsActivity extends Activity {
	
    private ConfigurationManager mConfigurationManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.moviedetails);
		ErrorHandler.setActivity(this);
		
		// remove nasty top fading edge
		FrameLayout topFrame = (FrameLayout)findViewById(android.R.id.content);
		topFrame.setForeground(null);
		
		final Movie movie = (Movie)getIntent().getSerializableExtra(ListController.EXTRA_MOVIE);
		((TextView)findViewById(R.id.titlebar_text)).setText(movie.getName());
		
		final ImageView posterView = ((ImageView)findViewById(R.id.moviedetails_poster));
		((TextView)findViewById(R.id.moviedetails_director)).setText(movie.director);
		((TextView)findViewById(R.id.moviedetails_genre)).setText(movie.genres);
		((TextView)findViewById(R.id.moviedetails_runtime)).setText(movie.runtime);
		((TextView)findViewById(R.id.moviedetails_rating)).setText(String.valueOf(movie.rating));
		
		mConfigurationManager = ConfigurationManager.getInstance(this);
		mConfigurationManager.initKeyguard();
		
		// load the cover
		HttpApiThread.video().getCover(new HttpApiHandler<Bitmap>(this) {
			public void run() {
				if (value == null) {
					posterView.setImageResource(R.drawable.nocover);
				} else {
					posterView.setImageBitmap(value);
				}
			}
		}, movie, ThumbSize.BIG);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mConfigurationManager.onActivityResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mConfigurationManager.onActivityPause();
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
}
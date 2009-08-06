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

import org.xbmc.httpapi.MediaType;
import org.xbmc.httpapi.XBMC;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class HomeActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
	    final Button GoMusicButton = (Button) findViewById(R.id.GoMusicButton);
	    GoMusicButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), MediaListActivity.class);
                myIntent.putExtra("shareType", MediaType.music.toString());
                startActivityForResult(myIntent, 0);
			}
		});
	    
	    final Button GoVideosButton = (Button) findViewById(R.id.GoVideosButton);
	    GoVideosButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), MediaListActivity.class);
                myIntent.putExtra("shareType", MediaType.video.toString());
                startActivityForResult(myIntent, 0);
			}
		});
	    
	    final Button GoPicturesButton = (Button) findViewById(R.id.GoPicturesButton);
	    GoPicturesButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), MediaListActivity.class);
                myIntent.putExtra("shareType", MediaType.pictures.toString());
                startActivityForResult(myIntent, 0);
			}
		});
	    
	    final Button GoRemoteButton = (Button) findViewById(R.id.GoRemoteButton);
	    GoRemoteButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), RemoteActivity.class);
                startActivityForResult(myIntent, 0);
			}
		});
	    
	    final Button GoNowPlayingButton = (Button) findViewById(R.id.GoNowPlayingButton);
	    GoNowPlayingButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), NowPlayingActivity.class);
                startActivityForResult(myIntent, 0);
			}
		});
	    
		SharedPreferences settings = getSharedPreferences(XBMCControl.PREFS_NAME, 0);
		String host = settings.getString("host", "");
		
		final EditText HostText = (EditText) findViewById(R.id.HostText);
		HostText.setText(host);
	}
	
	@Override
	protected void onPause() {
		super.onStop();
		
		final EditText HostText = (EditText) findViewById(R.id.HostText);
		String host = HostText.getText().toString();

		XBMCControl.createInstance(new XBMC(host));
		SharedPreferences settings = getSharedPreferences(XBMCControl.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("host", host);
		
		editor.commit();
	}
}

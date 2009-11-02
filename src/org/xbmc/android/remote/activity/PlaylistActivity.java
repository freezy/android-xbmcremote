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
import org.xbmc.android.remote.guilogic.MusicPlaylistLogic;
import org.xbmc.android.util.ErrorHandler;

import android.app.Activity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;

public class PlaylistActivity extends Activity {
	
	private MusicPlaylistLogic mMusicPlaylistLogic;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ErrorHandler.setActivity(this);
       	setContentView(R.layout.playlist);
        
 	  	mMusicPlaylistLogic = new MusicPlaylistLogic();
 	  	mMusicPlaylistLogic.findTitleView(findViewById(R.id.playlist_outer_layout));
 	  	mMusicPlaylistLogic.onCreate(this, (ListView)findViewById(R.id.playlist_list));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMusicPlaylistLogic.onCreateOptionsMenu(menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mMusicPlaylistLogic.onOptionsItemSelected(item);
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		mMusicPlaylistLogic.onCreateContextMenu(menu, v, menuInfo);
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		mMusicPlaylistLogic.onContextItemSelected(item);
		return super.onContextItemSelected(item);
	}
}
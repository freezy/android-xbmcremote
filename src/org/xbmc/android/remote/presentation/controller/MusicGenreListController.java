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

package org.xbmc.android.remote.presentation.controller;

import java.util.ArrayList;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.activity.MusicGenreActivity;
import org.xbmc.android.remote.presentation.widget.OneLabelItemView;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IMusicManager;
import org.xbmc.api.object.Genre;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class MusicGenreListController extends ListController implements IController {
	
	public static final int ITEM_CONTEXT_QUEUE = 1;
	public static final int ITEM_CONTEXT_PLAY = 2;
	
	private IMusicManager mMusicManager;
	
	public void onCreate(Activity activity, ListView list) {
		
		mMusicManager = ManagerFactory.getMusicManager(this);
		
		if (!isCreated()) {
			super.onCreate(activity, list);
			
			mActivity.registerForContextMenu(mList);
			mList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent nextActivity;
					Genre genre = (Genre)mList.getAdapter().getItem(((OneLabelItemView)view).position);
					nextActivity = new Intent(view.getContext(), MusicGenreActivity.class);
					nextActivity.putExtra(ListController.EXTRA_GENRE, genre);
					nextActivity.putExtra(ListController.EXTRA_LIST_CONTROLLER, new SongListController());
					mActivity.startActivity(nextActivity);
				}
			});
			mFallbackBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.icon_genre);
			setTitle("Genres...");
			showOnLoading();
			mMusicManager.getGenres(new DataResponse<ArrayList<Genre>>() {
				public void run() {
					if (value.size() > 0) {
						setTitle("Genres (" + value.size() + ")");
						mList.setAdapter(new GenreAdapter(mActivity, value));
					} else {
						setTitle("Genres");
						setNoDataMessage("No genres found.", R.drawable.icon_genre_dark);
					}
				}
			}, mActivity.getApplicationContext());
			
			mList.setOnKeyListener(new ListControllerOnKeyListener<Genre>());
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// be aware that this must be explicitly called by your activity!
		final Genre genre = (Genre)((AdapterContextMenuInfo)menuInfo).targetView.getTag();
		menu.setHeaderTitle(genre.name);
		menu.add(0, ITEM_CONTEXT_QUEUE, 1, "Queue " + genre.name + " songs");
		menu.add(0, ITEM_CONTEXT_PLAY, 2, "Play " + genre.name + " songs");
	}
	
	public void onContextItemSelected(MenuItem item) {
		// be aware that this must be explicitly called by your activity!
		final Genre genre = (Genre)((AdapterContextMenuInfo)item.getMenuInfo()).targetView.getTag();
		switch (item.getItemId()) {
			case ITEM_CONTEXT_QUEUE:
				mMusicManager.addToPlaylist(new QueryResponse(
						mActivity, 
						"Adding all songs of genre " + genre.name + " to playlist...", 
						"Error adding songs!"
					), genre, mActivity.getApplicationContext());
				break;
			case ITEM_CONTEXT_PLAY:
				mMusicManager.play(new QueryResponse(
						mActivity, 
						"Playing all songs of genre " + genre.name + "...", 
						"Error playing songs!",
						true
					), genre, mActivity.getApplicationContext());
				break;
		}
	}
	
	private class GenreAdapter extends ArrayAdapter<Genre> {
		GenreAdapter(Activity activity, ArrayList<Genre> items) {
			super(activity, 0, items);
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			final OneLabelItemView view;
			if (convertView == null) {
				view = new OneLabelItemView(mActivity, parent.getWidth(), mFallbackBitmap, mList.getSelector());
			} else {
				view = (OneLabelItemView)convertView;
			}
			final Genre genre = this.getItem(position);
			view.reset();
			view.position = position;
			view.title = genre.name;
			return view;
		}
	}
	private static final long serialVersionUID = 4360738733222799619L;

	public void onActivityPause() {
		if (mMusicManager != null) {
			mMusicManager.setController(null);
			mMusicManager.postActivity();
		}
		super.onActivityPause();
	}

	public void onActivityResume(Activity activity) {
		super.onActivityResume(activity);
		if (mMusicManager != null) {
			mMusicManager.setController(this);
		}
	}
}

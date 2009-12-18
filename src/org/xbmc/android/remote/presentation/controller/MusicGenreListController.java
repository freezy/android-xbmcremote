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
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IMusicManager;
import org.xbmc.api.object.Genre;

import android.app.Activity;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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
					Genre genre = (Genre)view.getTag();
					nextActivity = new Intent(view.getContext(), MusicGenreActivity.class);
					nextActivity.putExtra(ListController.EXTRA_GENRE, genre);
					nextActivity.putExtra(ListController.EXTRA_LIST_CONTROLLER, new SongListController());
					mActivity.startActivity(nextActivity);
				}
			});

			setTitle("Genres...");
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
			});
			
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
					), genre);
				break;
			case ITEM_CONTEXT_PLAY:
				mMusicManager.play(new QueryResponse(
						mActivity, 
						"Playing all songs of genre " + genre.name + "...", 
						"Error playing songs!",
						true
					), genre);
				break;
		}
	}
	
	private class GenreAdapter extends ArrayAdapter<Genre> {
		private Activity mActivity;
		GenreAdapter(Activity activity, ArrayList<Genre> items) {
			super(activity, R.layout.listitem_oneliner, items);
			mActivity = activity;
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			View row;
			if (convertView == null) {
				LayoutInflater inflater = mActivity.getLayoutInflater();
				row = inflater.inflate(R.layout.listitem_oneliner, null);
			} else {
				row = convertView;
			}
			final Genre genre = this.getItem(position);
			row.setTag(genre);
			final TextView title = (TextView)row.findViewById(R.id.MusicItemTextViewTitle);
			final ImageView icon = (ImageView)row.findViewById(R.id.MusicItemImageViewArt);
			title.setText(genre.name);
			icon.setImageResource(R.drawable.icon_genre);
			
			return row;
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

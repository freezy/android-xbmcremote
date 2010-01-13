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
import org.xbmc.android.remote.presentation.activity.ListActivity;
import org.xbmc.android.remote.presentation.widget.OneLabelItemView;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IVideoManager;
import org.xbmc.api.object.Genre;

import android.app.Activity;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class MovieGenreListController extends ListController implements IController {
	
	private IVideoManager mVideoManager;
	
	public void onCreate(Activity activity, ListView list) {
		
		mVideoManager = ManagerFactory.getVideoManager(this);
		
		if (!isCreated()) {
			super.onCreate(activity, list);
			
			mList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent nextActivity = new Intent(view.getContext(), ListActivity.class);
					nextActivity.putExtra(ListController.EXTRA_LIST_CONTROLLER, new MovieListController());
					nextActivity.putExtra(ListController.EXTRA_GENRE, (Genre)view.getTag());
					mActivity.startActivity(nextActivity);
				}
			});

			setTitle("Movie genres...");
			mVideoManager.getMovieGenres(new DataResponse<ArrayList<Genre>>() {
				public void run() {
					if (value.size() > 0) {
						setTitle("Movie genres (" + value.size() + ")");
						mList.setAdapter(new GenreAdapter(mActivity, value));
					} else {
						setTitle("Movie genres");
						setNoDataMessage("No genres found.", R.drawable.icon_genre_dark);
					}
				}
			});
			
			mList.setOnKeyListener(new ListControllerOnKeyListener<Genre>());
		}
	}
	
	private class GenreAdapter extends ArrayAdapter<Genre> {
		GenreAdapter(Activity activity, ArrayList<Genre> items) {
			super(activity, 0, items);
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			final OneLabelItemView view;
			if (convertView == null) {
				view = new OneLabelItemView(mActivity, R.drawable.icon_genre);
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
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// no context menu here
	}
	@Override
	public void onContextItemSelected(MenuItem item) {
		// no context menu here
	}
	
	public void onActivityPause() {
		if (mVideoManager != null) {
			mVideoManager.setController(null);
		}
		super.onActivityPause();
	}
	
	public void onActivityResume(Activity activity) {
		super.onActivityResume(activity);
		if (mVideoManager != null) {
			mVideoManager.setController(this);
		}
	}
}
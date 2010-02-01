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
import org.xbmc.android.util.ImportUtilities;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IVideoManager;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Artist;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ActorListController extends ListController implements IController {
	
	public static final int TYPE_ALL = 1;
	public static final int TYPE_MOVIE = 2;
	public static final int TYPE_TVSHOW = 3;
	public static final int TYPE_EPISODE = 4;
	
	private boolean mLoadCovers = false;
	private final int mType;
	
	private IVideoManager mVideoManager;
	
	public ActorListController(int type) {
		mType = type;
	}
	
	public void onCreate(Activity activity, ListView list) {
		
		mVideoManager = ManagerFactory.getVideoManager(this);
		
		if (!isCreated()) {
			super.onCreate(activity, list);
			final String sdError = ImportUtilities.assertSdCard();
			mLoadCovers = sdError == null;
			if (!mLoadCovers) {
				Toast toast = Toast.makeText(activity, sdError + " Displaying place holders only.", Toast.LENGTH_LONG);
				toast.show();
			}
			mFallbackBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.person_black_small);
			setupIdleListener();
			
			mList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent nextActivity;
					final Actor actor = (Actor)mList.getAdapter().getItem(((OneLabelItemView)view).position);
					nextActivity = new Intent(view.getContext(), ListActivity.class);
					nextActivity.putExtra(ListController.EXTRA_LIST_CONTROLLER, new MovieListController());
					nextActivity.putExtra(ListController.EXTRA_ACTOR, actor);
					mActivity.startActivity(nextActivity);
				}
			});
			
			mList.setOnKeyListener(new ListControllerOnKeyListener<Artist>());
			switch (mType) {
			case TYPE_ALL:
				setTitle("Actors...");
				mVideoManager.getActors(new DataResponse<ArrayList<Actor>>() {
					public void run() {
						if (value.size() > 0) {
							setTitle("Actors (" + value.size() + ")");
							mList.setAdapter(new ActorAdapter(mActivity, value));
						} else {
							setTitle("Actors");
							setNoDataMessage("No actors found.", R.drawable.icon_artist_dark);
						}
					}
				});
				break;
			case TYPE_MOVIE:
				setTitle("Movie Actors...");
				mVideoManager.getMovieActors(new DataResponse<ArrayList<Actor>>() {
					public void run() {
						if (value.size() > 0) {
							setTitle("Movie actors (" + value.size() + ")");
							mList.setAdapter(new ActorAdapter(mActivity, value));
						} else {
							setTitle("Movie actors");
							setNoDataMessage("No actors found.", R.drawable.icon_artist_dark);
						}
					}
				});
				break;
			case TYPE_TVSHOW:
				setTitle("TV Actors...");
				mVideoManager.getTvShowActors(new DataResponse<ArrayList<Actor>>() {
					public void run() {
						if (value.size() > 0) {
							setTitle("TV show actors (" + value.size() + ")");
							mList.setAdapter(new ActorAdapter(mActivity, value));
						} else {
							setTitle("TV show actors");
							setNoDataMessage("No actors found.", R.drawable.icon_artist_dark);
						}
					}
				});
				break;
			case TYPE_EPISODE:
				break;
			}
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu) {
	}
	
	private class ActorAdapter extends ArrayAdapter<Actor> {
		ActorAdapter(Activity activity, ArrayList<Actor> items) {
			super(activity, 0, items);
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			final OneLabelItemView view;
			if (convertView == null) {
				view = new OneLabelItemView(mActivity, mVideoManager, parent.getWidth(), mFallbackBitmap);
			} else {
				view = (OneLabelItemView)convertView;
			}
			final Actor actor = this.getItem(position);
			view.reset();
			view.position = position;
			view.title = actor.name;
			
			if (mLoadCovers) {
				view.getResponse().load(actor);
			}
			return view;
		}
	}
	private static final long serialVersionUID = 4360738733222799619L;

	@Override
	public void onContextItemSelected(MenuItem item) {
		// No context menus in here.
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// No context menus in here.
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

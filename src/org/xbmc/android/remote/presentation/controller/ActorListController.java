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
import org.xbmc.android.remote.business.DataResponse;
import org.xbmc.android.remote.business.ManagerThread;
import org.xbmc.android.remote.presentation.activity.ListActivity;
import org.xbmc.android.remote.presentation.controller.holder.OneHolder;
import org.xbmc.android.remote.presentation.drawable.CrossFadeDrawable;
import org.xbmc.httpapi.data.Actor;
import org.xbmc.httpapi.data.Artist;
import org.xbmc.httpapi.type.ThumbSize;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ActorListController extends ListController {
	
	public static final int TYPE_ALL = 1;
	public static final int TYPE_MOVIE = 2;
	public static final int TYPE_TVSHOW = 3;
	public static final int TYPE_EPISODE = 4;
	
	private boolean mLoadCovers = false;
	private final int mType;
	
	public ActorListController(int type) {
		mType = type;
	}
	
	public void onCreate(Activity activity, ListView list) {
		if (!isCreated()) {
			super.onCreate(activity, list);
			
			mLoadCovers = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
			
			if (!mLoadCovers) {
				Toast toast = Toast.makeText(activity, "Your SD card is not mounted. You'll need it for caching thumbs. Displaying place holders only.", Toast.LENGTH_LONG);
				toast.show();
			}
			mFallbackBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.person_black_small);
			setupIdleListener();
			
			mList.setOnItemClickListener(new OnItemClickListener() {
				@SuppressWarnings("unchecked")
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent nextActivity;
					OneHolder<Actor> holder = (OneHolder<Actor>)view.getTag();
					nextActivity = new Intent(view.getContext(), ListActivity.class);
					nextActivity.putExtra(ListController.EXTRA_LIST_LOGIC, new MovieListController());
					nextActivity.putExtra(ListController.EXTRA_ACTOR, holder.holderItem);
					mActivity.startActivity(nextActivity);
				}
			});
			
			mList.setOnKeyListener(new ListControllerOnKeyListener<Artist>());
			switch (mType) {
			case TYPE_ALL:
				setTitle("Actors...");
				ManagerThread.video().getActors(new DataResponse<ArrayList<Actor>>(mActivity) {
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
				ManagerThread.video().getMovieActors(new DataResponse<ArrayList<Actor>>(mActivity) {
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
				ManagerThread.video().getTvShowActors(new DataResponse<ArrayList<Actor>>(mActivity) {
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
		private Activity mActivity;
		ActorAdapter(Activity activity, ArrayList<Actor> items) {
			super(activity, R.layout.listitem_oneliner, items);
			mActivity = activity;
		}
		@SuppressWarnings("unchecked")
		public View getView(int position, View convertView, ViewGroup parent) {
			View row;
			final OneHolder<Actor> holder;
			
			if (convertView == null) {
				LayoutInflater inflater = mActivity.getLayoutInflater();
				row = inflater.inflate(R.layout.listitem_oneliner, null);
				holder = new OneHolder<Actor>(
						(ImageView)row.findViewById(R.id.MusicItemImageViewArt),
						(TextView)row.findViewById(R.id.MusicItemTextViewTitle)
					);
				row.setTag(holder);
				CrossFadeDrawable transition = new CrossFadeDrawable(mFallbackBitmap, null);
				transition.setCrossFadeEnabled(true);
				holder.transition = transition;
				holder.defaultCover = R.drawable.person_black_small;
					
			} else {
				row = convertView;
				holder = (OneHolder<Actor>)convertView.getTag();
			}
			final Actor actor = this.getItem(position);
			holder.holderItem = actor;
			holder.coverItem = actor;
			holder.id = actor.getCrc();
			
			holder.titleView.setText(actor.name);
			holder.iconView.setImageResource(holder.defaultCover);
			
			if (mLoadCovers) {
				holder.tempBind = true;
				ManagerThread.video().getCover(holder.getCoverDownloadHandler(mActivity, mPostScrollLoader), actor, ThumbSize.SMALL);
			}
			return row;
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
}

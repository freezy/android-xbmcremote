/*
 *      Copyright (C) 2005-2010 Team XBMC
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
import org.xbmc.android.remote.presentation.widget.GridPosterItemView;
import org.xbmc.android.util.ImportUtilities;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.ITvShowManager;
import org.xbmc.api.object.Movie;
import org.xbmc.api.object.Season;
import org.xbmc.api.object.TvShow;
import org.xbmc.api.type.ThumbSize;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class SeasonListController extends ListController implements IController {
	
	public static final int ITEM_CONTEXT_BROWSE = 1;
	
	public static final int MENU_PLAY_ALL = 1;
	public static final int MENU_SORT = 2;
	public static final int MENU_SORT_BY_TITLE_ASC = 21;
	public static final int MENU_SORT_BY_TITLE_DESC = 22;
	public static final int MENU_SORT_BY_YEAR_ASC = 23;
	public static final int MENU_SORT_BY_YEAR_DESC = 24;
	public static final int MENU_SORT_BY_RATING_ASC = 25;
	public static final int MENU_SORT_BY_RATING_DESC = 26;
	
	private TvShow mShow;
	
	private ITvShowManager mTvManager;
	private IControlManager mControlManager;
	
	private boolean mLoadCovers = false;
	
	public void onCreate(Activity activity, Handler handler, AbsListView list) {
		
		mTvManager = ManagerFactory.getTvManager(this);
		mControlManager = ManagerFactory.getControlManager(this);
		
		final String sdError = ImportUtilities.assertSdCard();
		mLoadCovers = sdError == null;
		
		if (!isCreated()) {
			super.onCreate(activity, handler, list);

			if (!mLoadCovers) {
				Toast toast = Toast.makeText(activity, sdError + " Displaying place holders only.", Toast.LENGTH_LONG);
				toast.show();
			}
			
			mShow = (TvShow)activity.getIntent().getSerializableExtra(ListController.EXTRA_TVSHOW);
			
			activity.registerForContextMenu(mList);
			
			mFallbackBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.default_season);
			setupIdleListener(ThumbSize.MEDIUM);
			
			mList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if(isLoading()) return;
					final Season season = (Season)mList.getAdapter().getItem(((GridPosterItemView)view).position);
					Intent nextActivity = new Intent(view.getContext(), ListActivity.class);
					nextActivity.putExtra(ListController.EXTRA_SEASON, season);
					nextActivity.putExtra(ListController.EXTRA_LIST_CONTROLLER, new EpisodeListController());
					mActivity.startActivity(nextActivity);
				}
			});
			mList.setOnKeyListener(new ListControllerOnKeyListener<Movie>());
			fetch();
		}
	}
	
	private void fetch() {
		final TvShow show = mShow;
		showOnLoading();
		if (show != null) {
			setTitle(show.title + " - Seasons");
			mTvManager.getSeasons(new DataResponse<ArrayList<Season>>() {
				public void run() {
					if(value.size() > 0) {
						setTitle(show.title + " - Seasons (" + value.size() + ")");
						mList.setAdapter(new SeasonAdapter(mActivity, value));
					} else {
						setNoDataMessage("No seasons found.", R.drawable.icon_movie_dark);
					}
				}
			}, show, mActivity.getApplicationContext());
		}
	}
	
	/**
	 * Shows a dialog and refreshes the movie library if user confirmed.
	 * @param activity
	 */
	public void refreshMovieLibrary(final Activity activity) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage("Are you sure you want XBMC to rescan your movie library?")
			.setCancelable(false)
			.setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					mControlManager.updateLibrary(new DataResponse<Boolean>() {
						public void run() {
							final String message;
							if (value) {
								message = "Movie library updated has been launched.";
							} else {
								message = "Error launching movie library update.";
							}
							Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
							toast.show();
						}
					}, "video", mActivity.getApplicationContext());
				}
			})
			.setNegativeButton("Uh, no.", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
		builder.create().show();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		final GridPosterItemView view = (GridPosterItemView)((AdapterContextMenuInfo)menuInfo).targetView;
		menu.setHeaderTitle(view.title);
		menu.add(0, ITEM_CONTEXT_BROWSE, 1, "Browse Season");
	}
	
	public void onContextItemSelected(MenuItem item) {
		//final Season season = (Season)mList.getAdapter().getItem(((GridPosterItemView)view).position);
		final Season season = (Season)mList.getAdapter().getItem(((GridPosterItemView)((AdapterContextMenuInfo)item.getMenuInfo()).targetView).position);
		switch (item.getItemId()) {
			case ITEM_CONTEXT_BROWSE:
				Intent nextActivity = new Intent(mActivity, ListActivity.class);
				nextActivity.putExtra(ListController.EXTRA_SEASON, season);
				nextActivity.putExtra(ListController.EXTRA_LIST_CONTROLLER, new EpisodeListController());
				mActivity.startActivity(nextActivity);
				break;
			default:
				return;
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu) {
//		if (mActor != null || mGenre != null) {
//			menu.add(0, MENU_PLAY_ALL, 0, "Play all").setIcon(R.drawable.menu_album);
//		}
		SubMenu sortMenu = menu.addSubMenu(0, MENU_SORT, 0, "Sort").setIcon(R.drawable.menu_sort);
		sortMenu.add(2, MENU_SORT_BY_TITLE_ASC, 0, "by Title ascending");
		sortMenu.add(2, MENU_SORT_BY_TITLE_DESC, 0, "by Title descending");
		sortMenu.add(2, MENU_SORT_BY_YEAR_ASC, 0, "by Year ascending");
		sortMenu.add(2, MENU_SORT_BY_YEAR_DESC, 0, "by Year descending");
		sortMenu.add(2, MENU_SORT_BY_RATING_ASC, 0, "by Rating ascending");
		sortMenu.add(2, MENU_SORT_BY_RATING_DESC, 0, "by Rating descending");
//		menu.add(0, MENU_SWITCH_VIEW, 0, "Switch view").setIcon(R.drawable.menu_view);
		createShowHideWatchedToggle(menu);
	}
	
	@Override
	public void onOptionsItemSelected(MenuItem item) {
//		final SharedPreferences.Editor ed;
		switch (item.getItemId()) {
		case MENU_PLAY_ALL:
			break;
		case MENU_SORT_BY_TITLE_ASC:
		case MENU_SORT_BY_TITLE_DESC:
		case MENU_SORT_BY_YEAR_ASC:
		case MENU_SORT_BY_YEAR_DESC:
		case MENU_SORT_BY_RATING_ASC:
		case MENU_SORT_BY_RATING_DESC:
			break;
		default:
			super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected void refreshList() {
		hideMessage();
		fetch();
	}
	
	private class SeasonAdapter extends ArrayAdapter<Season> {
		SeasonAdapter(Activity activity, ArrayList<Season> items) {
			super(activity, 0, items);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final GridPosterItemView view;
			if (convertView == null) {
				view = new GridPosterItemView(mActivity, mTvManager, parent.getWidth(), mFallbackBitmap, mList.getSelector(), false);
			} else {
				view = (GridPosterItemView)convertView;
			}
			
			final Season season = getItem(position);
			view.reset();
			view.position = position;
			view.title = "Season " + season.number;
			
			if (mLoadCovers) {
				view.getResponse().load(season, ThumbSize.MEDIUM, !mPostScrollLoader.isListIdle());
			}
			return view;
		}
	}
	
	private static final long serialVersionUID = 1088971882661811256L;

	public void onActivityPause() {
		if (mTvManager != null) {
			mTvManager.setController(null);
//			mVideoManager.postActivity();
		}
		if (mControlManager != null) {
			mControlManager.setController(null);
		}
		super.onActivityPause();
	}

	public void onActivityResume(Activity activity) {
		super.onActivityResume(activity);
		if (mTvManager != null) {
			mTvManager.setController(this);
		}
		if (mControlManager != null) {
			mControlManager.setController(this);
		}
	}

}

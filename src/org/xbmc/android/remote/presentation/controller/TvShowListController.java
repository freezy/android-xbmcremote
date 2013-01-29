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
import org.xbmc.android.remote.business.AbstractManager;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.activity.GridActivity;
import org.xbmc.android.remote.presentation.activity.ListActivity;
import org.xbmc.android.remote.presentation.activity.TvShowDetailsActivity;
import org.xbmc.android.remote.presentation.widget.FiveLabelsItemView;
import org.xbmc.android.remote.presentation.widget.FlexibleItemView;
import org.xbmc.android.util.ImportUtilities;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.ISortableManager;
import org.xbmc.api.business.ITvShowManager;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.TvShow;
import org.xbmc.api.type.SortType;
import org.xbmc.api.type.ThumbSize;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Toast;

public class TvShowListController extends ListController implements IController {
	
	private static final int mThumbSize = ThumbSize.SMALL;
	public static final int ITEM_CONTEXT_BROWSE = 1;
	public static final int ITEM_CONTEXT_INFO = 2;
	public static final int ITEM_CONTEXT_IMDB = 3;
	
	public static final int MENU_PLAY_ALL = 1;
	public static final int MENU_SORT = 2;
	public static final int MENU_SORT_BY_TITLE_ASC = 21;
	public static final int MENU_SORT_BY_TITLE_DESC = 22;
	public static final int MENU_SORT_BY_YEAR_ASC = 23;
	public static final int MENU_SORT_BY_YEAR_DESC = 24;
	public static final int MENU_SORT_BY_RATING_ASC = 25;
	public static final int MENU_SORT_BY_RATING_DESC = 26;
	public static final int MENU_RECENT_EPISODES = 27;
	
	private Actor mActor;
	private Genre mGenre;
	
	private ITvShowManager mTvManager;
	private IControlManager mControlManager;
	
	private boolean mLoadCovers = false;
	
	private static Bitmap mWatchedBitmap;
	
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
			
			mActor = (Actor)mActivity.getIntent().getSerializableExtra(ListController.EXTRA_ACTOR);
			mGenre = (Genre)mActivity.getIntent().getSerializableExtra(ListController.EXTRA_GENRE);
			activity.registerForContextMenu(mList);
			
			mFallbackBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.default_tvshow);
			mWatchedBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.check_mark);
			setupIdleListener();
			
			mList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if(isLoading()) return;
					final TvShow show = (TvShow)mList.getAdapter().getItem(((FiveLabelsItemView)view).position);
					Intent nextActivity = new Intent(view.getContext(), GridActivity.class);
					nextActivity.putExtra(ListController.EXTRA_TVSHOW, show);
					nextActivity.putExtra(ListController.EXTRA_LIST_CONTROLLER, new SeasonListController());
					mActivity.startActivity(nextActivity);
				}
			});
			mList.setOnKeyListener(new ListControllerOnKeyListener<TvShow>());
			fetch();
		}
	}
	
	private void fetch() {
		// tv show and episode both are using the same manager so set the sort key here
		((ISortableManager)mTvManager).setSortKey(AbstractManager.PREF_SORT_KEY_SHOW);
		((ISortableManager)mTvManager).setPreferences(mActivity.getPreferences(Context.MODE_PRIVATE));

		final String title = mActor != null ? mActor.name + " - " : mGenre != null ? mGenre.name + " - " : "" + "TV Shows";
		DataResponse<ArrayList<TvShow>> response = new DataResponse<ArrayList<TvShow>>() {
			public void run() {
				if (value.size() > 0) {
					setTitle(title + " (" + value.size() + ")");
					((AdapterView<ListAdapter>) mList).setAdapter(new TvShowAdapter(mActivity, value));
				} else {
					setTitle(title);
					setNoDataMessage("No TV shows found.", R.drawable.icon_movie_dark);
				}
			}
		};
		
		showOnLoading();
		setTitle(title + "...");		
		if (mActor != null) {						// TV Shows with a certain actor
			mTvManager.getTvShows(response, mActor, mActivity.getApplicationContext());
		} else if (mGenre != null) {					// TV Shows of a genre
			mTvManager.getTvShows(response, mGenre, mActivity.getApplicationContext());
		} else {									// all TV Shows
			mTvManager.getTvShows(response, mActivity.getApplicationContext());
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
		final FiveLabelsItemView view = (FiveLabelsItemView)((AdapterContextMenuInfo)menuInfo).targetView;
		menu.setHeaderTitle(view.title);
		menu.add(0, ITEM_CONTEXT_BROWSE, 1, "Browse TV Show");
		menu.add(0, ITEM_CONTEXT_INFO, 2, "View Details");
		menu.add(0, ITEM_CONTEXT_IMDB, 3, "Open IMDb");
	}
	
	public void onContextItemSelected(MenuItem item) {
		final TvShow show = (TvShow)mList.getAdapter().getItem(((FiveLabelsItemView)((AdapterContextMenuInfo)item.getMenuInfo()).targetView).position);
		switch (item.getItemId()) {
			case ITEM_CONTEXT_BROWSE:
				Intent browseActivity = new Intent(mActivity, GridActivity.class);
				browseActivity.putExtra(ListController.EXTRA_TVSHOW, show);
				browseActivity.putExtra(ListController.EXTRA_LIST_CONTROLLER, new SeasonListController());
				mActivity.startActivity(browseActivity);
				break;
			case ITEM_CONTEXT_INFO:
				Intent nextActivity = new Intent(mActivity, TvShowDetailsActivity.class);
				nextActivity.putExtra(ListController.EXTRA_TVSHOW, show);
				mActivity.startActivity(nextActivity);
				break;
			case ITEM_CONTEXT_IMDB:
				Intent intentIMDb = new Intent(Intent.ACTION_VIEW, Uri.parse("imdb:///find?s=tt&q=" + show.getName()));
				if (mActivity.getPackageManager().resolveActivity(intentIMDb, PackageManager.MATCH_DEFAULT_ONLY) == null)
				{
					intentIMDb = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.imdb.com/search/title?title=" + show.getShortName() + "&title_type=tv_series&release_date=" + show.firstAired));
				}
				mActivity.startActivity(intentIMDb);
				break;
			default:
				return;
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu) {
		if (mActor != null || mGenre != null) {
			menu.add(0, MENU_PLAY_ALL, 0, "Play all").setIcon(R.drawable.menu_album);
		}
		SubMenu sortMenu = menu.addSubMenu(0, MENU_SORT, 0, "Sort").setIcon(R.drawable.menu_sort);
		sortMenu.add(2, MENU_SORT_BY_TITLE_ASC, 0, "by Title ascending");
		sortMenu.add(2, MENU_SORT_BY_TITLE_DESC, 0, "by Title descending");
		sortMenu.add(2, MENU_SORT_BY_YEAR_ASC, 0, "by Year ascending");
		sortMenu.add(2, MENU_SORT_BY_YEAR_DESC, 0, "by Year descending");
		sortMenu.add(2, MENU_SORT_BY_RATING_ASC, 0, "by Rating ascending");
		sortMenu.add(2, MENU_SORT_BY_RATING_DESC, 0, "by Rating descending");
		sortMenu.add(2, MENU_RECENT_EPISODES, 0, "Recent Episodes");
//		menu.add(0, MENU_SWITCH_VIEW, 0, "Switch view").setIcon(R.drawable.menu_view);
		createShowHideWatchedToggle(menu);
	}
	
	@Override
	public void onOptionsItemSelected(MenuItem item) {
		final SharedPreferences.Editor ed;
		switch (item.getItemId()) {
		case MENU_PLAY_ALL:
			break;
		case MENU_SORT_BY_TITLE_ASC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_SHOW, SortType.TITLE);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_SHOW, SortType.ORDER_ASC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_TITLE_DESC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_SHOW, SortType.TITLE);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_SHOW, SortType.ORDER_DESC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_YEAR_ASC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_SHOW, SortType.YEAR);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_SHOW, SortType.ORDER_ASC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_YEAR_DESC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_SHOW, SortType.YEAR);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_SHOW, SortType.ORDER_DESC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_RATING_ASC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_SHOW, SortType.RATING);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_SHOW, SortType.ORDER_ASC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_RATING_DESC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_SHOW, SortType.RATING);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_SHOW, SortType.ORDER_DESC);
			ed.commit();
			fetch();
			break;
		case MENU_RECENT_EPISODES:
			Intent recentEpisodesActivity = new Intent(mActivity, ListActivity.class);
			recentEpisodesActivity.putExtra(ListController.EXTRA_LIST_CONTROLLER, new EpisodeListController());
			mActivity.startActivity(recentEpisodesActivity);
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
	
	private class TvShowAdapter extends ArrayAdapter<TvShow> {
		TvShowAdapter(Activity activity, ArrayList<TvShow> items) {
			super(activity, 0, items);
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			final FlexibleItemView view;
			if (convertView == null) {
				view = new FlexibleItemView(mActivity, mTvManager, parent.getWidth(), mFallbackBitmap,
						mList.getSelector(), false);
			} else {
				view = (FlexibleItemView) convertView;
			}

			final TvShow show = getItem(position);
			view.reset();
			view.position = position;
			view.posterOverlay = show.watched ? mWatchedBitmap : null;
			view.title = show.title;
			view.subtitle = show.genre;
			view.subtitleRight = show.firstAired != null ? show.firstAired : "";
			view.bottomtitle = show.numEpisodes + " episodes";
			view.bottomright = String.valueOf(((float) Math.round(show.rating * 10)) / 10);

			if (mLoadCovers) {
				if (mTvManager.coverLoaded(show, mThumbSize)) {
					view.setCover(mTvManager.getCoverSync(show, mThumbSize));
				} else {
					view.setCover(null);
					view.getResponse().load(show, !mPostScrollLoader.isListIdle());
				}
			}
			return view;
		}
	}
	
	private static final long serialVersionUID = 1088971882661811256L;

	public void onActivityPause() {
		if (mTvManager != null) {
			mTvManager.setController(null);
			mTvManager.postActivity();
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

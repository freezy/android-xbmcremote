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
import org.xbmc.android.remote.business.ManagerThread;
import org.xbmc.android.remote.presentation.activity.GridActivity;
import org.xbmc.android.remote.presentation.activity.MovieDetailsActivity;
import org.xbmc.android.remote.presentation.activity.NowPlayingActivity;
import org.xbmc.android.remote.presentation.widget.FiveLabelsItemView;
import org.xbmc.android.remote.presentation.widget.FlexibleItemView;
import org.xbmc.android.util.ImportUtilities;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.ITvShowManager;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.Movie;
import org.xbmc.api.object.TvShow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class TvShowListController extends ListController implements IController {
	
	public static final int ITEM_CONTEXT_PLAY = 1;
	public static final int ITEM_CONTEXT_INFO = 2;
	
	public static final int MENU_PLAY_ALL = 1;
	public static final int MENU_SORT = 2;
	public static final int MENU_SORT_BY_TITLE_ASC = 21;
	public static final int MENU_SORT_BY_TITLE_DESC = 22;
	public static final int MENU_SORT_BY_YEAR_ASC = 23;
	public static final int MENU_SORT_BY_YEAR_DESC = 24;
	public static final int MENU_SORT_BY_RATING_ASC = 25;
	public static final int MENU_SORT_BY_RATING_DESC = 26;
	
	private Actor mActor;
	private Genre mGenre;
	
	private ITvShowManager mTvManager;
	private IControlManager mControlManager;
	
	private boolean mLoadCovers = false;
	
	public void onCreate(Activity activity, ListView list) {
		
		mTvManager = ManagerFactory.getTvManager(this);
		mControlManager = ManagerFactory.getControlManager(this);
		
		ManagerThread.video(this).setSortKey(AbstractManager.PREF_SORT_KEY_ALBUM);
		ManagerThread.video(this).setPreferences(activity.getPreferences(Context.MODE_PRIVATE));
		final String sdError = ImportUtilities.assertSdCard();
		mLoadCovers = sdError == null;
		
		if (!isCreated()) {
			super.onCreate(activity, list);

			if (!mLoadCovers) {
				Toast toast = Toast.makeText(activity, sdError + " Displaying place holders only.", Toast.LENGTH_LONG);
				toast.show();
			}
			
			mActor = (Actor)mActivity.getIntent().getSerializableExtra(ListController.EXTRA_ACTOR);
			mGenre = (Genre)mActivity.getIntent().getSerializableExtra(ListController.EXTRA_GENRE);
			activity.registerForContextMenu(mList);
			
			mFallbackBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.poster);
			setupIdleListener();
			
			mList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final TvShow show = (TvShow)mList.getAdapter().getItem(((FiveLabelsItemView)view).position);
					Intent nextActivity = new Intent(view.getContext(), GridActivity.class);
					nextActivity.putExtra(ListController.EXTRA_TVSHOW, show);
					nextActivity.putExtra(ListController.EXTRA_LIST_CONTROLLER, new SeasonListController());
					mActivity.startActivity(nextActivity);
				}
			});
			mList.setOnKeyListener(new ListControllerOnKeyListener<Movie>());
			fetch();
		}
	}
	
	private void fetch() {
		final Actor actor = mActor;
		final Genre genre = mGenre;
		showOnLoading();
		if (actor != null) {						// TV Shows with a certain actor
			setTitle(actor.name + " - TV Shows...");
			mTvManager.getTvShows(new DataResponse<ArrayList<TvShow>>() {
				public void run() {
					if (value.size() > 0) {
						setTitle(actor.name + " - TV Shows (" + value.size() + ")");
						mList.setAdapter(new TvShowAdapter(mActivity, value));
					} else {
						setTitle(actor.name + " - TV Shows");
						setNoDataMessage("No movies found.", R.drawable.icon_movie_dark);
					}
				}
			}, actor, mActivity.getApplicationContext());
			
		} else if (genre != null) {					// TV Shows of a genre
			setTitle(genre.name + " - TV Shows...");
			mTvManager.getTvShows(new DataResponse<ArrayList<TvShow>>() {
				public void run() {
					if (value.size() > 0) {
						setTitle(genre.name + " - TV Shows (" + value.size() + ")");
						mList.setAdapter(new TvShowAdapter(mActivity, value));
					} else {
						setTitle(genre.name + " - TV Shows");
						setNoDataMessage("No tv shows found.", R.drawable.icon_movie_dark);
					}
				}
			}, genre, mActivity.getApplicationContext());
		} else {
			setTitle("TV Shows...");				// all TV Shows
			mTvManager.getTvShows(new DataResponse<ArrayList<TvShow>>() {
				public void run() {
					if (value.size() > 0) {
						setTitle("TV Shows (" + value.size() + ")");
						mList.setAdapter(new TvShowAdapter(mActivity, value));
					} else {
						setTitle("TV Shows");
						setNoDataMessage("No movies found.", R.drawable.icon_movie_dark);
					}
				}
			}, mActivity.getApplicationContext());
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
		menu.add(0, ITEM_CONTEXT_PLAY, 1, "Play Movie");
		menu.add(0, ITEM_CONTEXT_INFO, 2, "View Details");
	}
	
	public void onContextItemSelected(MenuItem item) {
		final Movie movie = (Movie)mList.getAdapter().getItem(((FiveLabelsItemView)((AdapterContextMenuInfo)item.getMenuInfo()).targetView).position);
		switch (item.getItemId()) {
			case ITEM_CONTEXT_PLAY:
				mControlManager.playFile(new DataResponse<Boolean>() {
					public void run() {
						if (value) {
							mActivity.startActivity(new Intent(mActivity, NowPlayingActivity.class));
						}
					}
				}, movie.getPath(), mActivity.getApplicationContext());
				break;
			case ITEM_CONTEXT_INFO:
				Intent nextActivity = new Intent(mActivity, MovieDetailsActivity.class);
				nextActivity.putExtra(ListController.EXTRA_MOVIE, movie);
				mActivity.startActivity(nextActivity);
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
//		menu.add(0, MENU_SWITCH_VIEW, 0, "Switch view").setIcon(R.drawable.menu_view);
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
		}
	}
	
	private class TvShowAdapter extends ArrayAdapter<TvShow> {
		TvShowAdapter(Activity activity, ArrayList<TvShow> items) {
			super(activity, 0, items);
		}
		public View getView(int position, View convertView, ViewGroup parent) {

			final FlexibleItemView view;
			if (convertView == null) {
				view = new FlexibleItemView(mActivity, mTvManager, parent.getWidth(), mFallbackBitmap, mList.getSelector());
			} else {
				view = (FlexibleItemView)convertView;
			}
			
			final TvShow show = getItem(position);
			view.reset();
			view.position = position;
			view.title = show.title;
			view.subtitle = show.genre;
			view.subtitleRight = show.firstAired!=null?show.firstAired:"";
			view.bottomtitle = show.numEpisodes + " episodes";
			view.bottomright = String.valueOf(((float)Math.round(show.rating *10))/ 10);
			
			if (mLoadCovers) {
				view.getResponse().load(show, !mPostScrollLoader.isListIdle());
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

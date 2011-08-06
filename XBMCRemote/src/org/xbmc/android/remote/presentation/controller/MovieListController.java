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
import org.xbmc.android.remote.presentation.activity.MovieDetailsActivity;
import org.xbmc.android.remote.presentation.activity.NowPlayingActivity;
import org.xbmc.android.remote.presentation.widget.FiveLabelsItemView;
import org.xbmc.android.util.ImportUtilities;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.ISortableManager;
import org.xbmc.api.business.IVideoManager;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.Movie;
import org.xbmc.api.type.SortType;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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

public class MovieListController extends ListController implements IController {
	
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
	public static final int MENU_SORT_BY_DATE_ADDED_ASC = 27;
	public static final int MENU_SORT_BY_DATE_ADDED_DESC = 28;
	
	
	private Actor mActor;
	private Genre mGenre;
	
	private IVideoManager mVideoManager;
	private IControlManager mControlManager;
	
	private boolean mLoadCovers = false;

	private static Bitmap mWatchedBitmap;
	
	public void onCreate(Activity activity, Handler handler, AbsListView list) {
		
		mVideoManager = ManagerFactory.getVideoManager(this);
		mControlManager = ManagerFactory.getControlManager(this);
		
		((ISortableManager)mVideoManager).setSortKey(AbstractManager.PREF_SORT_KEY_MOVIE);
		((ISortableManager)mVideoManager).setPreferences(activity.getPreferences(Context.MODE_PRIVATE));
		
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
			
			mFallbackBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.default_poster);
			mWatchedBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.check_mark);
			setupIdleListener();
			
			mList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if(isLoading()) return;
					final Movie movie = (Movie)mList.getAdapter().getItem(((FiveLabelsItemView)view).position);
					Intent nextActivity = new Intent(view.getContext(), MovieDetailsActivity.class);
					nextActivity.putExtra(ListController.EXTRA_MOVIE, movie);
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
		if (actor != null) {						// movies with a certain actor
			setTitle(actor.name + " - Movies...");
			mVideoManager.getMovies(new DataResponse<ArrayList<Movie>>() {
				public void run() {
					if (value.size() > 0) {
						setTitle(actor.name + " - Movies (" + value.size() + ")");
						mList.setAdapter(new MovieAdapter(mActivity, value));
					} else {
						setTitle(actor.name + " - Movies");
						setNoDataMessage("No movies found.", R.drawable.icon_movie_dark);
					}
				}
			}, actor, mActivity.getApplicationContext());
			
		} else if (genre != null) {					// movies of a genre
			setTitle(genre.name + " - Movies...");
			mVideoManager.getMovies(new DataResponse<ArrayList<Movie>>() {
				public void run() {
					if (value.size() > 0) {
						setTitle(genre.name + " - Movies (" + value.size() + ")");
						mList.setAdapter(new MovieAdapter(mActivity, value));
					} else {
						setTitle(genre.name + " - Movies");
						setNoDataMessage("No movies found.", R.drawable.icon_movie_dark);
					}
				}
			}, genre, mActivity.getApplicationContext());
		} else {
			setTitle("Movies...");				// all movies
			mVideoManager.getMovies(new DataResponse<ArrayList<Movie>>() {
				public void run() {
					if (value.size() > 0) {
						setTitle("Movies (" + value.size() + ")");
						mList.setAdapter(new MovieAdapter(mActivity, value));
					} else {
						setTitle("Movies");
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
		sortMenu.add(2, MENU_SORT_BY_DATE_ADDED_ASC, 0, "by Date Added ascending");
		sortMenu.add(2, MENU_SORT_BY_DATE_ADDED_DESC, 0, "by Date Added descending");
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
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.TITLE);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.ORDER_ASC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_TITLE_DESC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.TITLE);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.ORDER_DESC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_YEAR_ASC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.YEAR);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.ORDER_ASC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_YEAR_DESC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.YEAR);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.ORDER_DESC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_RATING_ASC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.RATING);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.ORDER_ASC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_RATING_DESC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.RATING);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.ORDER_DESC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_DATE_ADDED_ASC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.DATE_ADDED);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.ORDER_ASC);
			ed.commit();
			fetch();
			break;
		case MENU_SORT_BY_DATE_ADDED_DESC:
			ed = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.DATE_ADDED);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.ORDER_DESC);
			ed.commit();
			fetch();
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
	
	private class MovieAdapter extends ArrayAdapter<Movie> {
		MovieAdapter(Activity activity, ArrayList<Movie> items) {
			super(activity, 0, items);
		}
		public View getView(int position, View convertView, ViewGroup parent) {

			final FiveLabelsItemView view;
			if (convertView == null) {
				view = new FiveLabelsItemView(mActivity, mVideoManager, parent.getWidth(), mFallbackBitmap, mList.getSelector(), false);
			} else {
				view = (FiveLabelsItemView)convertView;
			}
			
			final Movie movie = getItem(position);
			view.reset();
			view.position = position;
			view.posterOverlay = movie.numWatched > 0 ? mWatchedBitmap : null;
			view.title = movie.title;
			view.subtitle = movie.genres;
			view.subtitleRight = movie.year > 0 ? String.valueOf(movie.year) : "";
			view.bottomtitle = movie.runtime;
			view.bottomright = String.valueOf(movie.rating);
			
			if (mLoadCovers) {
				view.getResponse().load(movie, !mPostScrollLoader.isListIdle());
			}
			return view;
		}
	}
	
	private static final long serialVersionUID = 1088971882661811256L;

	public void onActivityPause() {
		if (mVideoManager != null) {
			mVideoManager.setController(null);
			mVideoManager.postActivity();
		}
		if (mControlManager != null) {
			mControlManager.setController(null);
		}
		super.onActivityPause();
	}

	public void onActivityResume(Activity activity) {
		super.onActivityResume(activity);
		if (mVideoManager != null) {
			mVideoManager.setController(this);
		}
		if (mControlManager != null) {
			mControlManager.setController(this);
		}
	}

}

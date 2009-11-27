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

package org.xbmc.android.remote.controller;

import java.util.ArrayList;

import org.xbmc.android.backend.httpapi.HttpApiHandler;
import org.xbmc.android.backend.httpapi.HttpApiThread;
import org.xbmc.android.backend.httpapi.MusicWrapper;
import org.xbmc.android.backend.httpapi.Wrapper;
import org.xbmc.android.remote.R;
import org.xbmc.android.remote.activity.MovieDetailsActivity;
import org.xbmc.android.remote.activity.NowPlayingActivity;
import org.xbmc.android.remote.controller.holder.MovieHolder;
import org.xbmc.android.remote.drawable.CrossFadeDrawable;
import org.xbmc.httpapi.data.Actor;
import org.xbmc.httpapi.data.Artist;
import org.xbmc.httpapi.data.Genre;
import org.xbmc.httpapi.data.Movie;
import org.xbmc.httpapi.type.ThumbSize;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class MovieListController extends ListController {
	
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
	private boolean mLoadCovers = false;
	
	public void onCreate(Activity activity, ListView list) {
		
		MusicWrapper.setSortKey(Wrapper.PREF_SORT_KEY_ALBUM);
		MusicWrapper.setPreferences(activity.getPreferences(Context.MODE_PRIVATE));
		mLoadCovers = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		
		if (!isCreated()) {
			super.onCreate(activity, list);

			if (!mLoadCovers) {
				Toast toast = Toast.makeText(activity, "Your SD card is not mounted. You'll need it for caching covers. Displaying place holders only.", Toast.LENGTH_LONG);
				toast.show();
			}
			
			mActor = (Actor)mActivity.getIntent().getSerializableExtra(ListController.EXTRA_ACTOR);
			mGenre = (Genre)mActivity.getIntent().getSerializableExtra(ListController.EXTRA_GENRE);
			activity.registerForContextMenu(mList);
			
			mFallbackBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.poster);
			setupIdleListener();
			
//			ImportUtilities.purgeCache();
			
			mList.setOnItemClickListener(new OnItemClickListener() {
				@SuppressWarnings("unchecked")
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					MovieHolder<Movie> holder = (MovieHolder<Movie>)view.getTag();
					Intent nextActivity = new Intent(view.getContext(), MovieDetailsActivity.class);
					nextActivity.putExtra(ListController.EXTRA_MOVIE, holder.holderItem);
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
		if (actor != null) {						// movies with a certain actor
			setTitle(actor.name + " - Movies...");
			HttpApiThread.video().getMovies(new HttpApiHandler<ArrayList<Movie>>(mActivity) {
				public void run() {
					if (value.size() > 0) {
						setTitle(actor.name + " - Movies (" + value.size() + ")");
						mList.setAdapter(new MovieAdapter(mActivity, value));
					} else {
						setTitle(actor.name + " - Movies");
						setNoDataMessage("No movies found.", R.drawable.icon_movie_dark);
					}
				}
			}, actor);
			
		} else if (genre != null) {					// movies of a genre
			setTitle(genre.name + " - Movies...");
			HttpApiThread.video().getMovies(new HttpApiHandler<ArrayList<Movie>>(mActivity) {
				public void run() {
					if (value.size() > 0) {
						setTitle(genre.name + " - Movies (" + value.size() + ")");
						mList.setAdapter(new MovieAdapter(mActivity, value));
					} else {
						setTitle(genre.name + " - Movies");
						setNoDataMessage("No movies found.", R.drawable.icon_movie_dark);
					}
				}
			}, genre);
		} else {
			setTitle("Movies...");				// all movies
			HttpApiThread.video().getMovies(new HttpApiHandler<ArrayList<Movie>>(mActivity) {
				public void run() {
					if (value.size() > 0) {
						setTitle("Movies (" + value.size() + ")");
						mList.setAdapter(new MovieAdapter(mActivity, value));
					} else {
						setTitle("Movies");
						setNoDataMessage("No movies found.", R.drawable.icon_movie_dark);
					}
				}
			});
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		final MovieHolder<Movie> holder = (MovieHolder<Movie>)((AdapterContextMenuInfo)menuInfo).targetView.getTag();
		menu.setHeaderTitle(holder.holderItem.title);
		menu.add(0, ITEM_CONTEXT_PLAY, 1, "Play Movie");
		menu.add(0, ITEM_CONTEXT_INFO, 2, "View Details");
	}
	
	@SuppressWarnings("unchecked")
	public void onContextItemSelected(MenuItem item) {
		final MovieHolder<Movie> holder = (MovieHolder<Movie>)((AdapterContextMenuInfo)item.getMenuInfo()).targetView.getTag();
		final Movie movie = holder.holderItem;
		switch (item.getItemId()) {
			case ITEM_CONTEXT_PLAY:
				HttpApiThread.control().playFile(new HttpApiHandler<Boolean>(mActivity) {
					public void run() {
						if (value) {
							mActivity.startActivity(new Intent(mActivity, NowPlayingActivity.class));
						}
					}
				}, movie.getPath());
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
			final Artist artist = mActor;
			final Genre genre = mGenre;
			if (artist != null && genre == null) {
				HttpApiThread.music().play(new QueryHandler(
						mActivity, 
						"Playing all albums by " + artist.name + "...", 
						"Error playing songs!",
						true
					), genre);			
			} else if (genre != null && artist == null) {
				HttpApiThread.music().play(new QueryHandler(
						mActivity, 
						"Playing all albums of genre " + genre.name + "...", 
						"Error playing songs!",
						true
					), genre);
			} else if (genre != null && artist != null) {
				HttpApiThread.music().play(new QueryHandler(
						mActivity, 
						"Playing all songs of genre " + genre.name + " by " + artist.name + "...", 
						"Error playing songs!",
						true
					), artist, genre);
			}
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
	
	private class MovieAdapter extends ArrayAdapter<Movie> {
		private Activity mActivity;
		private final LayoutInflater mInflater;
		MovieAdapter(Activity activity, ArrayList<Movie> items) {
			super(activity, R.layout.listitem_three, items);
			mActivity = activity;
			mInflater = LayoutInflater.from(activity);
		}
		
		@SuppressWarnings("unchecked")
		public View getView(int position, View convertView, ViewGroup parent) {
			
			final View row;
			final MovieHolder<Movie> holder;
			
			if (convertView == null) {

				row = mInflater.inflate(R.layout.listitem_movie, null);
				holder = new MovieHolder<Movie>(
					(ImageView)row.findViewById(R.id.listitem_image),
					(TextView)row.findViewById(R.id.listitem_title),
					(TextView)row.findViewById(R.id.listitem_subtitle),
					(TextView)row.findViewById(R.id.listitem_subtitle_right),
					(TextView)row.findViewById(R.id.listitem_bottom_line),
					(TextView)row.findViewById(R.id.listitem_bottom_right)
				);
				row.setTag(holder);
				
				CrossFadeDrawable transition = new CrossFadeDrawable(mFallbackBitmap, null);
				transition.setCrossFadeEnabled(true);
				holder.transition = transition;
				holder.defaultCover = R.drawable.poster;
				
			} else {
				row = convertView;
				holder = (MovieHolder<Movie>)convertView.getTag();
			}
			
			final Movie movie = getItem(position);
			holder.holderItem = movie;
			holder.coverItem = movie;
			holder.id = movie.getCrc();
			
			holder.titleView.setText(movie.title);
			holder.subtitleView.setText(movie.genres);
			holder.subtitleRightView.setText(movie.year > 0 ? String.valueOf(movie.year) : "");
			holder.bottomView.setText(movie.runtime);
			holder.bottomRightView.setText(String.valueOf(movie.rating));
			
			if (mLoadCovers) {
				holder.tempBind = true;
				holder.iconView.setImageResource(R.drawable.poster);
				HttpApiThread.video().getCover(holder.getCoverDownloadHandler(mActivity, mPostScrollLoader), movie, ThumbSize.SMALL);
			} else {
				holder.iconView.setImageResource(R.drawable.poster);
			}		
			
			return row;
		}
	}
	
	private static final long serialVersionUID = 1088971882661811256L;
}

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

import java.io.Serializable;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.presentation.activity.NowPlayingActivity;
import org.xbmc.android.widget.FastScrollView;
import org.xbmc.android.widget.IdleListDetector;
import org.xbmc.android.widget.IdleListener;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.api.type.ThumbSize;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public abstract class ListController extends AbstractController implements Serializable, INotifiableController {
	
	public static final String EXTRA_LIST_CONTROLLER = "ListController"; 
	public static final String EXTRA_MOVIE = "movie";
	public static final String EXTRA_TVSHOW = "tvshow";
	public static final String EXTRA_SEASON = "season";
	public static final String EXTRA_EPISODE = "episode";
	public static final String EXTRA_ALBUM = "album"; 
	public static final String EXTRA_ARTIST = "artist";
	public static final String EXTRA_ACTOR = "actor";
	public static final String EXTRA_GENRE = "genre";
	public static final String EXTRA_SHARE_TYPE = "shareType"; 
	public static final String EXTRA_PATH = "path"; 
	public static final String EXTRA_DISPLAY_PATH = "display_path"; 

	private static final int MENU_SHOWHIDE_WATCHED = 51;
	private static final String PREF_HIDE_WATCHED = "HideWatched";
	
	protected AbsListView mList;
	
	private TextView mTitleView;
	private ViewGroup mMessageGroup;
	private TextView mMessageText;
	private boolean hideWatched;
	private boolean isCreated = false;
	
	protected static Bitmap mFallbackBitmap;
	protected IdleListDetector mPostScrollLoader;

	public void onCreate(Activity activity, Handler handler, AbsListView list) {
		super.onCreate(activity, handler);
		mList = list;
		mActivity = activity;
		SharedPreferences sp = mActivity.getSharedPreferences("global", Context.MODE_PRIVATE);
		hideWatched = sp.getBoolean(PREF_HIDE_WATCHED, false);
		isCreated = true;
//		list.setOnScrollListener(new ScrollManager(ThumbSize.SMALL));
	}

	/**
	 * Default listener is small
	 * @return
	 */
	protected IdleListener setupIdleListener() {
		return setupIdleListener(ThumbSize.SMALL);
	}
	
	/**
	 * Hook up the mechanism to load images only when the list "slows" down.
	 */
	protected IdleListener setupIdleListener(int thumbSize) {
		IdleListener idleListener = new IdleListener(mList, thumbSize);
		mPostScrollLoader = new IdleListDetector(idleListener);
		FastScrollView fastScroller = (FastScrollView)mList.getParent();
		fastScroller.setOnIdleListDetector(mPostScrollLoader);
		return idleListener;
	}
	
	public abstract void onContextItemSelected(MenuItem item);
	public abstract void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo);
	
	public void onCreateOptionsMenu(Menu menu) { }
	
	protected void createShowHideWatchedToggle(Menu menu) {
		configureShowHideWatchedToggleOption(menu.add(0, MENU_SHOWHIDE_WATCHED, 0, ""), hideWatched);
	}
	
	public void onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == MENU_SHOWHIDE_WATCHED) {
			SharedPreferences sp = mActivity.getSharedPreferences("global", Context.MODE_PRIVATE);
			hideWatched = !(sp.getBoolean(PREF_HIDE_WATCHED, false));
			sp.edit().putBoolean(PREF_HIDE_WATCHED, hideWatched).commit();
			configureShowHideWatchedToggleOption(item, hideWatched);
			refreshList();
		}
	}
	
	private MenuItem configureShowHideWatchedToggleOption(MenuItem item, boolean hideWatched) {
		if (hideWatched) {
			return item.setTitle("Show Watched").setIcon(R.drawable.menu_show_watched);
		} else {
			return item.setTitle("Hide Watched").setIcon(R.drawable.menu_hide_watched);
		}
	}
	
	protected void refreshList() { }
	
	public void findTitleView(View parent) {
		mTitleView = (TextView)parent.findViewById(R.id.titlebar_text);	
	}
	
	public void findMessageView(View parent) {
		mMessageGroup = (ViewGroup)parent.findViewById(R.id.listmessage);	
		mMessageText = (TextView)parent.findViewById(R.id.listmessage_text);	
		mMessageGroup.setVisibility(View.GONE);
	}
	
	protected void setTitle(final String title) {
		if (mTitleView != null) {
			mHandler.post(new Runnable() {
				public void run() {
					mTitleView.setText(title);
				}
			});
		}
	}
	
	protected boolean isCreated() {
		return isCreated;
	}
	
	protected void setNoDataMessage(final String message, final int imageResource) {
		if (mMessageGroup != null) {
			mHandler.post(new Runnable() {
				public void run() {
					mMessageText.setText(message);
					mMessageText.setCompoundDrawablesWithIntrinsicBounds(imageResource, 0, 0, 0);
					mList.setAdapter(null);
					mMessageGroup.setVisibility(View.VISIBLE);
				}
			});
		}
	}
	
	protected void hideMessage() {
		if (mMessageGroup != null) {
			mMessageGroup.setVisibility(View.GONE);
		}
	}
	
	protected void showOnLoading() {
		mHandler.post(new Runnable() {
			public void run() {
				mList.setAdapter(new LoadingAdapter(mActivity));
				mList.setVisibility(View.VISIBLE);
			}
		});
	}
	
	protected boolean isLoading() {
		return mList.getAdapter() instanceof LoadingAdapter;
	}
	
	@Override
	public void onActivityResume(Activity activity) {
		super.onActivityResume(activity);
		if (isCreated()) {
			SharedPreferences sp = mActivity.getSharedPreferences("global", Context.MODE_PRIVATE);
			boolean hideWatched = sp.getBoolean(PREF_HIDE_WATCHED, false);
			if (hideWatched != this.hideWatched) {
				this.hideWatched = hideWatched;
				refreshList();
			}
		}
	}
	
	protected class QueryResponse extends DataResponse<Boolean> {
		private final String mSuccessMessage;
		private final String mErrorMessage;
		private final boolean mGotoNowPlaying;
		public QueryResponse(Activity activity, String successMessage, String errorMessage) {
			super();
			mSuccessMessage = successMessage;
			mErrorMessage = errorMessage;
			mGotoNowPlaying = false;
		}
		public QueryResponse(Activity activity, String successMessage, String errorMessage, boolean gotoNowPlaying) {
			super();
			mSuccessMessage = successMessage;
			mErrorMessage = errorMessage;
			mGotoNowPlaying = gotoNowPlaying;
		}
		public void run() {
			Toast.makeText(mActivity, value ? mSuccessMessage :  mErrorMessage, Toast.LENGTH_LONG).show();
			if (value && mGotoNowPlaying) {
				mActivity.startActivity(new Intent(mActivity, NowPlayingActivity.class));
			}
		}
	}

	private static final long serialVersionUID = 2903701184005613570L;
	
	private class LoadingAdapter extends ArrayAdapter<String> {
		View row;
		public LoadingAdapter(Activity act) {
			super(act, R.layout.loadinglistentry);
			add("dummy");
			row = LayoutInflater.from(mActivity).inflate(R.layout.loadinglistentry, null);
			((TextView)row.findViewById(R.id.loading_text)).setText("Loading...");
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			
			return row;
		}
	}
}

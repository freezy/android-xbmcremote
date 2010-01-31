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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public abstract class ListController extends AbstractController implements Serializable, INotifiableController {
	
	public static final String EXTRA_LIST_CONTROLLER = "ListController"; 
	public static final String EXTRA_MOVIE = "movie";
	public static final String EXTRA_ALBUM = "album"; 
	public static final String EXTRA_ARTIST = "artist";
	public static final String EXTRA_ACTOR = "actor";
	public static final String EXTRA_GENRE = "genre";
	public static final String EXTRA_SHARE_TYPE = "shareType"; 
	public static final String EXTRA_PATH = "path"; 
	public static final String EXTRA_DISPLAY_PATH = "display_path"; 
	
	protected ListView mList;
	
	private TextView mTitleView;
	private ViewGroup mMessageGroup;
	private TextView mMessageText;
	private boolean isCreated = false;
	
	protected static Bitmap mFallbackBitmap;
	protected IdleListDetector mPostScrollLoader;
	
	public void onCreate(Activity activity, ListView list) {
		super.onCreate(activity);
		mList = list;
		mActivity = activity;
		isCreated = true;
	}
	
	/**
	 * Hook up the mechanism to load images only when the list "slows" down.
	 */
	protected IdleListener setupIdleListener() {
		IdleListener idleListener = new IdleListener(mList);
		mPostScrollLoader = new IdleListDetector(idleListener);
		FastScrollView fastScroller = (FastScrollView)mList.getParent();
		fastScroller.setOnIdleListDetector(mPostScrollLoader);
		return idleListener;
	}
	
	public abstract void onContextItemSelected(MenuItem item);
	public abstract void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo);
	
	public void onCreateOptionsMenu(Menu menu) { }
	public void onOptionsItemSelected(MenuItem item) { }
	
	public void findTitleView(View parent) {
		mTitleView = (TextView)parent.findViewById(R.id.titlebar_text);	
	}
	
	public void findMessageView(View parent) {
		mMessageGroup = (ViewGroup)parent.findViewById(R.id.listmessage);	
		mMessageText = (TextView)parent.findViewById(R.id.listmessage_text);	
		mMessageGroup.setVisibility(View.GONE);
	}
	
	protected void setTitle(String title) {
		if (mTitleView != null) {
			mTitleView.setText(title);
		}
	}
	
	protected boolean isCreated() {
		return isCreated;
	}
	
	protected void setNoDataMessage(String message, int imageResource) {
		if (mMessageGroup != null) {
			mMessageText.setText(message);
			mMessageText.setCompoundDrawablesWithIntrinsicBounds(imageResource, 0, 0, 0);
			mList.setAdapter(null);
			mMessageGroup.setVisibility(View.VISIBLE);
		}
	}
	
	protected void showOnLoading() {
		mList.setAdapter(new LoadingAdapter(mActivity));
		mList.setVisibility(View.VISIBLE);
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

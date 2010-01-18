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

package org.xbmc.android.widget.slidingtabs;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.presentation.activity.HomeActivity;
import org.xbmc.android.remote.presentation.activity.MovieLibraryActivity;
import org.xbmc.android.remote.presentation.activity.PlaylistActivity;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import org.xbmc.android.util.KeyTracker;
import org.xbmc.android.util.OnLongPressBackKeyTracker;
import org.xbmc.android.util.KeyTracker.Stage;
import org.xbmc.android.util.KeyTracker.State;

public class SlidingTabActivity extends ActivityGroup {
	
	private SlidingTabHost mTabHost;
	private String mDefaultTab = null;
	private int mDefaultTabIndex = -1;
	private KeyTracker mKeyTracker = null;

	public SlidingTabActivity() { 
		mKeyTracker = new KeyTracker(new OnLongPressBackKeyTracker() {

			@Override
			public void onLongPressBack(int keyCode, KeyEvent event,
					Stage stage, int duration) {
				Intent intent = new Intent(SlidingTabActivity.this, HomeActivity.class);
				intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
			}

			@Override
			public void onShortPressBack(int keyCode, KeyEvent event,
					Stage stage, int duration) {
				callSuperOnKeyDown(keyCode, event);
			}
			
		});
	}
	
	protected void callSuperOnKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
	}

	/**
	 * Sets the default tab that is the first tab highlighted.
	 * 
	 * @param tag
	 *            the name of the default tab
	 */
	public void setDefaultTab(String tag) {
		mDefaultTab = tag;
		mDefaultTabIndex = -1;
	}

	/**
	 * Sets the default tab that is the first tab highlighted.
	 * 
	 * @param index
	 *            the index of the default tab
	 */
	public void setDefaultTab(int index) {
		mDefaultTab = null;
		mDefaultTabIndex = index;
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		ensureTabHost();
		String cur = state.getString("currentTab");
		if (cur != null) {
			mTabHost.setCurrentTabByTag(cur);
		}
		if (mTabHost.getCurrentTab() < 0) {
			if (mDefaultTab != null) {
				mTabHost.setCurrentTabByTag(mDefaultTab);
			} else if (mDefaultTabIndex >= 0) {
				mTabHost.setCurrentTab(mDefaultTabIndex);
			}
		}
	}

	@Override
	protected void onPostCreate(Bundle icicle) {
		super.onPostCreate(icicle);

		ensureTabHost();

		if (mTabHost.getCurrentTab() == -1) {
			mTabHost.setCurrentTab(0);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		String currentTabTag = mTabHost.getCurrentTabTag();
		if (currentTabTag != null) {
			outState.putString("currentTab", currentTabTag);
		}
	}

	/**
	 * Updates the screen state (current list and other views) when the content
	 * changes.
	 * 
	 *@see Activity#onContentChanged()
	 */
	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mTabHost = (SlidingTabHost) findViewById(R.id.slidingtabhost);

		if (mTabHost == null) {
			throw new RuntimeException("Your content must have a TabHost whose id attribute is " + "'android.R.id.tabhost'");
		}
		mTabHost.setup(getLocalActivityManager());
	}

	private void ensureTabHost() {
		if (mTabHost == null) {
			this.setContentView(R.id.slidingtabhost);
		}
	}

	@Override
	protected void onChildTitleChanged(Activity childActivity, CharSequence title) {
		// Dorky implementation until we can have multiple activities running.
		if (getLocalActivityManager().getCurrentActivity() == childActivity) {
			View tabView = mTabHost.getCurrentTabView();
			if (tabView != null && tabView instanceof TextView) {
				((TextView) tabView).setText(title);
			}
		}
	}

	/**
	 * Returns the {@link TabHost} the activity is using to host its tabs.
	 * 
	 * @return the {@link TabHost} the activity is using to host its tabs.
	 */
	public SlidingTabHost getTabHost() {
		ensureTabHost();
		return mTabHost;
	}

	/**
	 * Returns the {@link TabWidget} the activity is using to draw the actual
	 * tabs.
	 * 
	 * @return the {@link TabWidget} the activity is using to draw the actual
	 *         tabs.
	 */
	public SlidingTabWidget getTabWidget() {
		return mTabHost.getTabWidget();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean handled =  mKeyTracker.doKeyDown(keyCode, event);
		return handled || super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean handled = mKeyTracker.doKeyUp(keyCode, event);
		return handled || super.onKeyUp(keyCode, event);
	}
}
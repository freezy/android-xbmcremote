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

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.KeyguardManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

public class SlidingTabActivity extends ActivityGroup implements OnSharedPreferenceChangeListener{
	private SlidingTabHost mTabHost;
	private String mDefaultTab = null;
	private int mDefaultTabIndex = -1;

    private boolean mDisableKeyguard = false;
    private KeyguardManager.KeyguardLock mKeyguardLock = null;
	
	public SlidingTabActivity() {
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
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	String disableKeyguardString = prefs.getString("setting_disable_keyguard", "0");
    	mDisableKeyguard = ( disableKeyguardString.equals("2") );
    	prefs.registerOnSharedPreferenceChangeListener(this);
    }

   @Override
   protected void onResume(){
	   super.onResume();
	   if(mDisableKeyguard) {
		   KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);
		   mKeyguardLock = keyguardManager.newKeyguardLock("RemoteActivityKeyguardLock");
		   mKeyguardLock.disableKeyguard();
	   }
   }
    
	@Override
	protected void onPause() {
		super.onPause();
		if (mKeyguardLock != null){
			mKeyguardLock.reenableKeyguard();
			mKeyguardLock = null;
		}
	}
    
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals("setting_disable_keyguard")) {
			String disableKeyguardString = sharedPreferences.getString(key, "0");
			boolean disableKeyguardState = ( disableKeyguardString.equals("2") );
			if (disableKeyguardState != mDisableKeyguard){
				if (disableKeyguardState) {
					if(this.hasWindowFocus()  ) {
		    			KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);
						mKeyguardLock = keyguardManager.newKeyguardLock("RemoteActivityKeyguardLock");
						mKeyguardLock.disableKeyguard();
					}
				}
				else {
					if(this.hasWindowFocus()) {
						if (mKeyguardLock != null) {
							mKeyguardLock.reenableKeyguard();
						}
						mKeyguardLock = null;
					}
				}
				mDisableKeyguard = disableKeyguardState;
			}
		}
	}
}

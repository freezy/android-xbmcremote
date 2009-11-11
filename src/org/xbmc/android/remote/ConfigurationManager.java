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

package org.xbmc.android.remote;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class ConfigurationManager implements OnSharedPreferenceChangeListener {

	public final static String PREF_KEYGUARD_DISABLED = "setting_disable_keyguard";

	public final static String KEYGUARD_STATUS_ENABLED = "0";
	public final static String KEYGUARD_STATUS_REMOTE_ONLY = "1";
	public final static String KEYGUARD_STATUS_ALL = "2";

	public final static String KEYGUARD_TAG = "xbmc_remote_keyguard_lock";

	private static ConfigurationManager sInstance;

	private Activity mActivity;

	private boolean mKeyguardDisabled = false;
	private boolean mKeyguardFromRemote = false;
	private KeyguardManager.KeyguardLock mKeyguardLock = null;

	private ConfigurationManager(Activity activity) {
		mActivity = activity;
	}

	public static ConfigurationManager getInstance(Activity activity) {
		if (sInstance == null) {
			sInstance = new ConfigurationManager(activity);
		} else {
			sInstance.mActivity = activity;
		}
		return sInstance;
	}

	public void initKeyguard() {
		initKeyguard(false);
	}

	public void initKeyguard(boolean fromRemoteControl) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
		mKeyguardFromRemote = fromRemoteControl;
		mKeyguardDisabled = isKeyguardDisabled(prefs);
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if (key.equals(PREF_KEYGUARD_DISABLED)) {
			boolean disableKeyguardState = isKeyguardDisabled(prefs);
			if (disableKeyguardState != mKeyguardDisabled) {
				if (disableKeyguardState) {
					if (mActivity.hasWindowFocus()) {
						KeyguardManager keyguardManager = (KeyguardManager) mActivity.getSystemService(Activity.KEYGUARD_SERVICE);
						mKeyguardLock = keyguardManager.newKeyguardLock(KEYGUARD_TAG);
						mKeyguardLock.disableKeyguard();
					}
				} else {
					if (mActivity.hasWindowFocus()) {
						if (mKeyguardLock != null) {
							mKeyguardLock.reenableKeyguard();
						}
						mKeyguardLock = null;
					}
				}
				mKeyguardDisabled = disableKeyguardState;
			}
		}
	}
	
	public void onActivityResume(Activity activity) {
		if (mKeyguardDisabled) {
			KeyguardManager keyguardManager = (KeyguardManager) activity.getSystemService(Activity.KEYGUARD_SERVICE);
			mKeyguardLock = keyguardManager.newKeyguardLock(KEYGUARD_TAG);
			mKeyguardLock.disableKeyguard();
		}
		mActivity = activity;
	}
	
	public void onActivityPause() {
		if (mKeyguardLock != null){
			mKeyguardLock.reenableKeyguard();
			mKeyguardLock = null;
		}
	}
	
	private boolean isKeyguardDisabled(SharedPreferences prefs) {
		String disableKeyguardString = prefs.getString(PREF_KEYGUARD_DISABLED, KEYGUARD_STATUS_ENABLED);
		if (!mKeyguardFromRemote) {
			return disableKeyguardString.equals(KEYGUARD_STATUS_ALL);
		} else {
			return disableKeyguardString.equals(KEYGUARD_STATUS_REMOTE_ONLY) || disableKeyguardString.equals(KEYGUARD_STATUS_ALL);
		}
	}

}
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

package org.xbmc.android.remote.activity;

import java.io.IOException;
import java.util.Hashtable;

import org.xbmc.android.remote.R;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.eventclient.ButtonCodes;
import org.xbmc.eventclient.EventClient;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.KeyEvent;

/**
 * The XBMC remote's preferences page. This is a little special since we want
 * the actual value of the setting in the summary text. It can be set using the
 * %value% place holder.
 * <p>
 * For that we needed to cache the original (with %value% intact) summaries,
 * which then will be replaced upon change or application start. 
 * 
 * @author Team XBMC
 */
public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	public final static String SUMMARY_VALUE_PLACEHOLDER = "%value%";
	
	private final Hashtable<String, String> mSummaries = new Hashtable<String, String>();
	
    private boolean mDisableKeyguard = false;
    private KeyguardManager.KeyguardLock mKeyguardLock = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		PreferenceScreen ps = getPreferenceScreen();
		String disableKeyguardString = ps.getSharedPreferences().getString("setting_disable_keyguard", "0");
		mDisableKeyguard = ( disableKeyguardString.equals("2") );
		// save original summaries to variable for later update
		mSummaries.clear();
		for (String key : ps.getSharedPreferences().getAll().keySet()) {
			Preference pref = ps.findPreference(key);
			if (pref != null && pref.getSummary() != null) {
				mSummaries.put(key, pref.getSummary().toString());
			}
		}
		
		updateSummaries();
	}
	
	/**
	 * Updates summaries of all known keys with the updated value.
	 */
	private void updateSummaries() {
		PreferenceScreen ps = getPreferenceScreen();
		for (String key : ps.getSharedPreferences().getAll().keySet()) {
			Preference pref = ps.findPreference(key);
			if (pref != null && pref.getSummary() != null) {
				String summary = pref.getSummary().toString();
				if (summary.contains(SUMMARY_VALUE_PLACEHOLDER)) {
					pref.setSummary(summary.replaceAll(SUMMARY_VALUE_PLACEHOLDER, ps.getSharedPreferences().getString(key, "<not set>")));
				}
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateSummaries();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	   	if(mDisableKeyguard) {
	   		KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);
	   		mKeyguardLock = keyguardManager.newKeyguardLock("RemoteActivityKeyguardLock");
	   		mKeyguardLock.disableKeyguard();
	   	}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		if (mKeyguardLock != null){
			mKeyguardLock.reenableKeyguard();
			mKeyguardLock = null;
		}
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference pref = getPreferenceScreen().findPreference(key);
		String origSummary = mSummaries.get(key);
		if (origSummary != null && origSummary.contains(SUMMARY_VALUE_PLACEHOLDER)) {
			pref.setSummary(origSummary.replaceAll(SUMMARY_VALUE_PLACEHOLDER, sharedPreferences.getString(key, "")));
		}
		if (key.equals("setting_ip") || key.equals("setting_http_port") || key.equals("setting_eventserver_port") || key.equals("setting_http_user") || key.equals("setting_http_pass")) {
			ConnectionManager.resetClient();
		}
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
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		EventClient client = ConnectionManager.getEventClient(this);	
		try {
			switch (keyCode) {
				case KeyEvent.KEYCODE_VOLUME_UP:
					client.sendButton("R1", ButtonCodes.REMOTE_VOLUME_PLUS, false, true, true, (short)0, (byte)0);
					return true;
				case KeyEvent.KEYCODE_VOLUME_DOWN:
					client.sendButton("R1", ButtonCodes.REMOTE_VOLUME_MINUS, false, true, true, (short)0, (byte)0);
					return true;
			}
		} catch (IOException e) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}


}

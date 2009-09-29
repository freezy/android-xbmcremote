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

import java.util.Hashtable;

import org.xbmc.android.remote.R;
import org.xbmc.android.util.ConnectionManager;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		PreferenceScreen ps = getPreferenceScreen();
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
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference pref = getPreferenceScreen().findPreference(key);
		String origSummary = mSummaries.get(key);
		if (origSummary != null && origSummary.contains(SUMMARY_VALUE_PLACEHOLDER)) {
			pref.setSummary(origSummary.replaceAll(SUMMARY_VALUE_PLACEHOLDER, sharedPreferences.getString(key, "")));
		}
		if (key.equals("setting_ip") || key.equals("setting_http_port") || key.equals("setting_eventserver_port")) {
			ConnectionManager.resetClient();
		}
	}

}

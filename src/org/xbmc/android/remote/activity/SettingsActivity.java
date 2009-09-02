package org.xbmc.android.remote.activity;

import java.util.Hashtable;
import java.util.prefs.Preferences;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import org.xbmc.android.remote.R;

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
		if (origSummary.contains(SUMMARY_VALUE_PLACEHOLDER)) {
			pref.setSummary(origSummary.replaceAll(SUMMARY_VALUE_PLACEHOLDER, sharedPreferences.getString(key, "")));
		}
	}

}

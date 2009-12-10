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

import java.util.Hashtable;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.activity.SettingsActivity;
import org.xbmc.android.util.HostFactory;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.object.Host;
import org.xbmc.api.presentation.INotifiableController;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

public class SettingsController extends AbstractController implements INotifiableController, IController, OnSharedPreferenceChangeListener, OnPreferenceChangeListener {
	
	public static final String SETTING_HTTP_HOST = "setting_ip";
	public static final String SETTING_HTTP_PORT = "setting_http_port";
	public static final String SETTING_HTTP_USER = "setting_http_user";
	public static final String SETTING_HTTP_PASS = "setting_http_pass";
	public static final String SETTING_HTTP_TIMEOUT = "setting_socket_timeout";
	public static final String SETTING_ES_PORT = "setting_eventserver_port";
	
	private IControlManager mControlManager;
	private PreferenceActivity mPreferenceActivity;		
	private final Hashtable<String, String> mSummaries = new Hashtable<String, String>();
	
	public SettingsController(PreferenceActivity activity) {
		mPreferenceActivity = activity;
		super.onCreate(activity);
		mControlManager = ManagerFactory.getControlManager(activity.getApplicationContext(), this);
		
		activity.addPreferencesFromResource(R.xml.preferences);
		activity.getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		
		PreferenceScreen ps = activity.getPreferenceScreen();
		// save original summaries to variable for later update
		mSummaries.clear();
		for (String key : ps.getSharedPreferences().getAll().keySet()) {
			Preference pref = ps.findPreference(key);
			if (pref != null && pref.getSummary() != null) {
				mSummaries.put(key, pref.getSummary().toString());
			}
		}
		
		updateSummaries();
		updateHosts(activity);
	}
	
	public void updateHosts(PreferenceActivity activity) {
		PreferenceScreen hosts = (PreferenceScreen)activity.getPreferenceScreen().findPreference("setting_instances");
		HostPreference addItem = (HostPreference)activity.getPreferenceScreen().findPreference("setting_add_host");
		hosts.removeAll();
		
		for (Host host : HostFactory.getHosts(activity.getApplicationContext())) {
			HostPreference pref = new HostPreference(activity);
			pref.setTitle(host.name);
			pref.setSummary(host.getSummary());
			pref.setHost(host);
			pref.setKey(HostPreference.ID_PREFIX + host.id);
			pref.setOnPreferenceChangeListener(this);
			hosts.addPreference(pref);
		}
		hosts.addPreference(addItem);
	}
	
	/**
	 * Updates summaries of all known keys with the updated value.
	 */
	public void updateSummaries() {
		PreferenceScreen ps = mPreferenceActivity.getPreferenceScreen();
		for (String key : ps.getSharedPreferences().getAll().keySet()) {
			Preference pref = ps.findPreference(key);
			if (pref != null && pref.getSummary() != null) {
				String summary = pref.getSummary().toString();
				if (summary.contains(SettingsActivity.SUMMARY_VALUE_PLACEHOLDER)) {
					pref.setSummary(summary.replaceAll(SettingsActivity.SUMMARY_VALUE_PLACEHOLDER, ps.getSharedPreferences().getString(key, "<not set>")));
				}
			}
		}
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.i("SettingsActivity", "onSharedPreferenceChanged(" + key + ")");
		Preference pref = mPreferenceActivity.getPreferenceScreen().findPreference(key);
		String origSummary = mSummaries.get(key);
		if (origSummary != null && origSummary.contains(SettingsActivity.SUMMARY_VALUE_PLACEHOLDER)) {
			pref.setSummary(origSummary.replaceAll(SettingsActivity.SUMMARY_VALUE_PLACEHOLDER, sharedPreferences.getString(key, "")));
		}
		if (key.equals(SETTING_HTTP_HOST) || key.equals(SETTING_HTTP_PORT) || key.equals(SETTING_ES_PORT) || key.equals(SETTING_HTTP_USER) || key.equals(SETTING_HTTP_PASS)) {
			mControlManager.resetClient();
		}
	}
	
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		Log.i("SettingsActivity", "onPreferenceChange(" + newValue + ")");
		if (preference instanceof HostPreference) {
			((HostPreference)preference).setTitle(((Host)newValue).name);
			((HostPreference)preference).setSummary(((Host)newValue).getSummary());
			updateHosts(mPreferenceActivity);
			return true;
		}
		return false;
	}
	
	public void onActivityPause() {
		if (mControlManager != null) {
			mControlManager.setController(null);
		}
	}

	public void onActivityResume(Activity activity) {
		if (mControlManager != null) {
			mControlManager.setController(this);
		}
	}
	
}
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

import java.util.ArrayList;
import java.util.Hashtable;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.presentation.activity.SettingsActivity;
import org.xbmc.android.util.HostFactory;
import org.xbmc.api.object.Host;
import org.xbmc.api.presentation.INotifiableController;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class SettingsController extends AbstractController implements INotifiableController, IController, OnSharedPreferenceChangeListener {
	
	public static final int MENU_ADD_HOST = 1;
	public static final int MENU_EXIT = 2;
	
	private PreferenceActivity mPreferenceActivity;		
	private final Hashtable<String, String> mSummaries = new Hashtable<String, String>();
	
	public SettingsController(PreferenceActivity activity, Handler handler) {
		mPreferenceActivity = activity;
		super.onCreate(activity, handler);
	}
	
	/**
	 * Used in SettingsActivity in order to update the %value% placeholder in 
	 * the summaries.
	 * @param activity Reference to activity
	 */
	public void registerOnSharedPreferenceChangeListener(PreferenceActivity activity) {
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
	}
	
	/**
	 * Creates the preference screen that contains all the listed hosts.
	 * @param root Root node
	 * @param activity Reference to activity
	 * @return 
	 */
	public PreferenceScreen createHostsPreferences(PreferenceScreen root, Activity activity) {
		final ArrayList<Host> hosts = HostFactory.getHosts(activity.getApplicationContext());
		if (hosts.size() > 0) {
			for (Host host : hosts) {
				HostPreference pref = new HostPreference(activity);
				pref.setTitle(host.name);
				pref.setSummary(host.getSummary());
				pref.setHost(host);
				pref.setKey(HostPreference.ID_PREFIX + host.id);
				root.addPreference(pref);
			}
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setMessage("No hosts defined. In order to add hosts, press \"Menu\" and choose \"Add host\".");
			builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			builder.create().show();
		}
		return root;
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
	
	/**
	 * Used in order to replace the %value% placeholders with real values.
	 */
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.i("SettingsActivity", "onSharedPreferenceChanged(" + key + ")");
		Preference pref = mPreferenceActivity.getPreferenceScreen().findPreference(key);
		String origSummary = mSummaries.get(key);
		if (origSummary != null && origSummary.contains(SettingsActivity.SUMMARY_VALUE_PLACEHOLDER)) {
			pref.setSummary(origSummary.replaceAll(SettingsActivity.SUMMARY_VALUE_PLACEHOLDER, sharedPreferences.getString(key, "")));
		}
	}

	public void onCreateOptionsMenu(Menu menu) {
		menu.addSubMenu(0, MENU_ADD_HOST, 0, "Add Host").setIcon(R.drawable.menu_add_host);
		menu.addSubMenu(0, MENU_EXIT, 0, "Exit").setIcon(R.drawable.menu_exit);
	}
	
	public void onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ADD_HOST:
				HostPreference pref = new HostPreference(mActivity);
				pref.setTitle("New XBMC Host");
				pref.create(mPreferenceActivity.getPreferenceManager());
				mPreferenceActivity.getPreferenceScreen().addPreference(pref);
				break;
			case MENU_EXIT:
				System.exit(0);
				break;
		}
	}
	
	public void onActivityPause() {
		super.onActivityPause();
	}
	
	public void onActivityResume(Activity activity) {
		super.onActivityResume(activity);
		final ArrayList<Host> hosts = HostFactory.getHosts(activity.getApplicationContext()); 
		if (hosts.size() == 1) {
			final Host host = hosts.get(0);
			Log.i(TAG, "Setting host to " + (host == null ? "<null>" : host.addr) + ".");
			HostFactory.saveHost(activity.getApplicationContext(), host);
		}
		if (hosts.size() == 0) {
			Log.i(TAG, "Resetting host to <null>.");
			HostFactory.saveHost(activity.getApplicationContext(), null);
		}
	}	
}
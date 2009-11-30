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

package org.xbmc.android.remote.presentation.activity;

import java.io.IOException;
import java.util.Hashtable;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.controller.AbstractController;
import org.xbmc.android.remote.presentation.controller.IController;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.eventclient.ButtonCodes;
import org.xbmc.eventclient.EventClient;

import android.app.Activity;
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
public class SettingsActivity extends PreferenceActivity {
	
	public final static String SUMMARY_VALUE_PLACEHOLDER = "%value%";
	
	private ConfigurationManager mConfigurationManager;
	private SettingsController mSettingsController;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSettingsController = new SettingsController(this);
		mConfigurationManager = ConfigurationManager.getInstance(this);
		mConfigurationManager.initKeyguard();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mSettingsController.updateSummaries();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(mSettingsController);
		mConfigurationManager.onActivityResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(mSettingsController);
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
	
	public static class SettingsController extends AbstractController implements INotifiableController, IController, OnSharedPreferenceChangeListener {
		
		public static final String SETTING_HTTP_HOST = "setting_ip";
		public static final String SETTING_HTTP_PORT = "setting_http_port";
		public static final String SETTING_HTTP_USER = "setting_http_user";
		public static final String SETTING_HTTP_PASS = "setting_http_pass";
		public static final String SETTING_HTTP_TIMEOUT = "setting_socket_timeout";
		public static final String SETTING_ES_PORT = "setting_eventserver_port";
		
		private IControlManager mControlManager;
		private PreferenceActivity mPreferenceActivity;		
		private final Hashtable<String, String> mSummaries = new Hashtable<String, String>();
		
		SettingsController(PreferenceActivity activity) {
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
					if (summary.contains(SUMMARY_VALUE_PLACEHOLDER)) {
						pref.setSummary(summary.replaceAll(SUMMARY_VALUE_PLACEHOLDER, ps.getSharedPreferences().getString(key, "<not set>")));
					}
				}
			}
		}
		
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			Preference pref = mPreferenceActivity.getPreferenceScreen().findPreference(key);
			String origSummary = mSummaries.get(key);
			if (origSummary != null && origSummary.contains(SUMMARY_VALUE_PLACEHOLDER)) {
				pref.setSummary(origSummary.replaceAll(SUMMARY_VALUE_PLACEHOLDER, sharedPreferences.getString(key, "")));
			}
			if (key.equals(SETTING_HTTP_HOST) || key.equals(SETTING_HTTP_PORT) || key.equals(SETTING_ES_PORT) || key.equals(SETTING_HTTP_USER) || key.equals(SETTING_HTTP_PASS)) {
				mControlManager.resetClient();
			}
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
}
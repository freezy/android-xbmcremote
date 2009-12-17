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

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.controller.SettingsController;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.eventclient.ButtonCodes;

import android.os.Bundle;
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
	public final static String JUMP_TO = "jump_to";
	public final static int JUMP_TO_INSTANCES = 1;
	
	private ConfigurationManager mConfigurationManager;
	private SettingsController mSettingsController;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		mSettingsController = new SettingsController(this);
		mSettingsController.registerOnSharedPreferenceChangeListener(this);
		mConfigurationManager = ConfigurationManager.getInstance(this);
		mConfigurationManager.initKeyguard();
		final int jumpTo = getIntent().getIntExtra(JUMP_TO, 0);
		switch (jumpTo) {
			case JUMP_TO_INSTANCES:
				setPreferenceScreen((PreferenceScreen)getPreferenceScreen().findPreference("setting_instances"));
				break;
			default:
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mSettingsController.onActivityResume(this);
		mSettingsController.updateSummaries();
		mConfigurationManager.onActivityResume(this);
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(mSettingsController);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(mSettingsController);
		mSettingsController.onActivityPause();
		mConfigurationManager.onActivityPause();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		IEventClientManager client = ManagerFactory.getEventClientManager(mSettingsController);	
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
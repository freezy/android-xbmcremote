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

import org.xbmc.android.remote.presentation.controller.SettingsController;
import org.xbmc.android.util.ConnectionFactory;
import org.xbmc.eventclient.ButtonCodes;
import org.xbmc.eventclient.EventClient;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Because we can't add menus to child PreferenceScreens, we're forced to 
 * create a separate activity for it.
 * 
 * @author Team XBMC
 */
public class HostSettingsActivity extends PreferenceActivity {
	
	private ConfigurationManager mConfigurationManager;
	private SettingsController mSettingsController;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("XBMC Hosts");
		mSettingsController = new SettingsController(this);
		mConfigurationManager = ConfigurationManager.getInstance(this);
		mConfigurationManager.initKeyguard();
		setPreferenceScreen(mSettingsController.createHostsPreferences(getPreferenceManager().createPreferenceScreen(this), this));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mSettingsController.onCreateOptionsMenu(menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		mSettingsController.onMenuItemSelected(featureId, item);
		return super.onMenuItemSelected(featureId, item);
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
		EventClient client = ConnectionFactory.getEventClient(this);	
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
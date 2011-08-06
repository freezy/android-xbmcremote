/*
 *      Copyright (C) 2005-2011 Team XBMC
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

package org.xbmc.android.remote.business.receiver;

import org.xbmc.android.remote.presentation.activity.HomeActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Received when XBMC starts
 * 
 * @author Team-XBMC (thanks larry)
 */
public class AutoStartReceiver extends BroadcastReceiver {

	private static final String TAG = "AutoStartReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		// Received intent only when the system boot is completed
		Log.d(TAG, "onReceiveIntent");

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean startupOnBoot = sharedPreferences.getBoolean("setting_startup_onboot", false);
		if (startupOnBoot) {
			Intent activityIntent = new Intent(context, HomeActivity.class);
			activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(activityIntent);
		}
	}

}

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


import org.xbmc.android.remote.presentation.controller.MediaIntentController;

import android.app.Activity;
import android.os.Bundle;

/**
 * @author Team XBMC
 *
 */
public class MediaIntentActivity extends Activity {

	public static final String ACTION = "android.intent.action.VIEW";
	
	private ConfigurationManager mConfigurationManager;
	private MediaIntentController mMediaIntentController;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
  	  	mConfigurationManager = ConfigurationManager.getInstance(this);
		mConfigurationManager.initKeyguard();
		mMediaIntentController = new MediaIntentController(this);
		mMediaIntentController.setupStatusHandler();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mMediaIntentController.onActivityPause();
		mConfigurationManager.onActivityPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mMediaIntentController.onActivityResume(this);
		mConfigurationManager.onActivityResume(this);
	}

}

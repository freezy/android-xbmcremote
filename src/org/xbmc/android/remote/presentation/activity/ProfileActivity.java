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

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.presentation.activity.AbsListActivity;
import org.xbmc.android.remote.presentation.controller.ProfileListController;
import org.xbmc.android.remote.presentation.controller.RemoteController;

/**
 * Activity for remote control. At the moment that's the good ol' Xbox remote
 * control, more to come...
 * 
 * @author Team XBMC
 */
public class ProfileActivity extends ListActivity {

	private ProfileListController mProfileListController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mProfileListController = (ProfileListController)mListController;
	}
	
	@Override
	public Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		return mProfileListController.onCreateDialog(id, this);
	}

	@Override
	public void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		mProfileListController.onPrepareDialog(id, dialog);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mProfileListController.onActivityResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mProfileListController.onActivityPause();
	}

}

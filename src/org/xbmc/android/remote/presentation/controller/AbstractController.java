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

import android.app.Activity;
import android.widget.Toast;

/**
 * Every controller should extend this class. Takes care of the messages.
 * 
 * @author Team XBMC
 */
public abstract class AbstractController {
	
	protected Activity mActivity;
	
	public void onCreate(Activity activity) {
		mActivity = activity;
	}
	
	public void onError(String message) {
		Toast toast = Toast.makeText(mActivity, "MESSAGE FROM DOWN THERE: " + message, Toast.LENGTH_LONG);
		toast.show();
	}

	public void onMessage(String message) {
		Toast toast = Toast.makeText(mActivity, "MESSAGE FROM DOWN THERE: " + message, Toast.LENGTH_LONG);
		toast.show();
	}

	public void runOnUI(Runnable action) {
		mActivity.runOnUiThread(action);
	}
}
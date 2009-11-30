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

import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.api.type.SeekType;

import android.app.Activity;
import android.widget.Toast;

public class NowPlayingController implements INotifiableController, IController {
	
	private final Activity mActivity;
	private IControlManager mControlManager;
	
	public NowPlayingController(Activity activity) {
		mActivity = activity;
		mControlManager = ManagerFactory.getControlManager(activity.getApplicationContext(), this);
	}
	
	public void playUrl(String url) {
		mControlManager.playUrl(new DataResponse<Boolean>(), url);
	}
	
	public void seek(int progress) {
		mControlManager.seek(new DataResponse<Boolean>(), SeekType.absolute, progress);
	}
	
	public void onError(String message) {
		Toast toast = Toast.makeText(mActivity, "ERROR FROM DOWN THERE: " + message, Toast.LENGTH_LONG);
		toast.show();
	}

	public void onMessage(String message) {
		Toast toast = Toast.makeText(mActivity, "MESSAGE FROM DOWN THERE: " + message, Toast.LENGTH_LONG);
		toast.show();
	}

	public void runOnUI(Runnable action) {
		mActivity.runOnUiThread(action);
	}

	public void onActivityPause() {
		mControlManager.setController(null);
	}

	public void onActivityResume(Activity activity) {
		mControlManager.setController(this);
	}
}
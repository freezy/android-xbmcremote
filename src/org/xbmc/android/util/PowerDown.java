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

package org.xbmc.android.util;

import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.IEventClientManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class PowerDown {
	IEventClientManager mEventClientManager;
	private Activity mActivity;
	private final IControlManager cm;

	public PowerDown(IControlManager cm) {
		this.cm = cm;
	}

	public void ShowDialog(final Activity activity) {
		mActivity = activity;
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setMessage("Are you sure you want to power off?").setCancelable(false);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				this.Down();
				// dialog.cancel();
			}

			private void Down() {
				cm.powerOff(new DataResponse<Boolean>(), activity.getApplicationContext());
			}
		}).setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
}

/*
 *      Copyright (C) 2012 Cedric Priscal
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

package org.xbmc.android.smartextension;

import android.content.ContentValues;
import android.content.Context;

import com.sonyericsson.extras.liveware.aef.registration.Registration;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation;
import com.sonyericsson.extras.liveware.sdk.R;

public class XbmcExtensionRegistrationInformation extends
		RegistrationInformation {

	final Context mContext;

	protected XbmcExtensionRegistrationInformation(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("context == null");
		}
		mContext = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sonyericsson.extras.liveware.extension.util.registration.
	 * RegistrationInformation#getRequiredNotificationApiVersion()
	 */
	@Override
	public int getRequiredNotificationApiVersion() {
		return API_NOT_REQUIRED;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sonyericsson.extras.liveware.extension.util.registration.
	 * RegistrationInformation#getExtensionRegistrationConfiguration()
	 */
	@Override
	public ContentValues getExtensionRegistrationConfiguration() {
		ContentValues values = new ContentValues();

		values.put(Registration.ExtensionColumns.NAME,
				mContext.getString(R.string.app_name));
		values.put(Registration.ExtensionColumns.EXTENSION_KEY,
				XbmcExtensionService.EXTENSION_KEY);
		values.put(Registration.ExtensionColumns.HOST_APP_ICON_URI,
				ExtensionUtils.getUriString(mContext, org.xbmc.android.remote.R.drawable.icon));
		values.put(Registration.ExtensionColumns.EXTENSION_ICON_URI,
				ExtensionUtils.getUriString(mContext, org.xbmc.android.remote.R.drawable.ext_icon));
		values.put(
				Registration.ExtensionColumns.EXTENSION_ICON_URI_BLACK_WHITE,
				ExtensionUtils.getUriString(mContext, org.xbmc.android.remote.R.drawable.ext_icon_bw));
		values.put(Registration.ExtensionColumns.NOTIFICATION_API_VERSION,
				getRequiredNotificationApiVersion());
		values.put(Registration.ExtensionColumns.PACKAGE_NAME,
				mContext.getPackageName());

		return values;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sonyericsson.extras.liveware.extension.util.registration.
	 * RegistrationInformation#getRequiredWidgetApiVersion()
	 */
	@Override
	public int getRequiredWidgetApiVersion() {
		return API_NOT_REQUIRED;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sonyericsson.extras.liveware.extension.util.registration.
	 * RegistrationInformation#getRequiredControlApiVersion()
	 */
	@Override
	public int getRequiredControlApiVersion() {
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sonyericsson.extras.liveware.extension.util.registration.
	 * RegistrationInformation#getRequiredSensorApiVersion()
	 */
	@Override
	public int getRequiredSensorApiVersion() {
		return API_NOT_REQUIRED;
	}

    @Override
    public boolean isDisplaySizeSupported(int width, int height) {
        return true;
    }
}

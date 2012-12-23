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

import android.util.Log;

import com.sonyericsson.extras.liveware.extension.util.ExtensionService;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation;

public class XbmcExtensionService extends ExtensionService {

	public static final String EXTENSION_KEY = "org.xbmc.android.smartextension";
	private static final String LOG_TAG = "XbmcExtensionService";

	public XbmcExtensionService() {
		super(EXTENSION_KEY);
		Log.d(LOG_TAG, "Starting XbmcExtensionService");
	}

	/* (non-Javadoc)
	 * @see com.sonyericsson.extras.liveware.extension.util.ExtensionService#getRegistrationInformation()
	 */
	@Override
	protected RegistrationInformation getRegistrationInformation() {
		return new XbmcExtensionRegistrationInformation(this);
	}

	/* (non-Javadoc)
	 * @see com.sonyericsson.extras.liveware.extension.util.ExtensionService#keepRunningWhenConnected()
	 */
	@Override
	protected boolean keepRunningWhenConnected() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.sonyericsson.extras.liveware.extension.util.ExtensionService#createControlExtension()
	 */
	@Override
	public ControlExtension createControlExtension(String hostAppPackageName) {
		return new XbmcSmartWatchControlExtension(this, hostAppPackageName);
	}

}

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

package org.xbmc.android.remote.business;

import java.util.ArrayList;

import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IInfoManager;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.object.FileLocation;
import org.xbmc.api.type.SortType;
import org.xbmc.httpapi.WifiStateException;

import android.content.Context;
import android.util.Log;


/**
 * Asynchronously wraps the {@link org.xbmc.httpapi.client.InfoClient} class.
 * 
 * @author Team XBMC
 */
public class InfoManager extends AbstractManager implements IInfoManager, INotifiableManager {
	
	/**
	 * Returns any system info variable, see {@link org.xbmc.api.info.SystemInfo}
	 * @param response Response object
	 * @param field Field to return
	 */
	public void getSystemVersion(final DataResponse<String> response, final Context context) {
		mHandler.post(new Command<String>(response, this){
			@Override
			public void doRun() throws Exception {
				response.value = info(context).getSystemVersion(InfoManager.this);
			}
			
		});
	}
	
	/**
	 * SYNCHRONOUSLY gets API Version (since we need this for layout capabilities)
	 */
	public int getAPIVersion(final Context context) {
		try {
			return info(context).getAPIVersion(InfoManager.this);
		} catch(WifiStateException e) {
			Log.e("InfoManager", e.getMessage(), e);
		}
		return 0;
	}
	
	/**
	 * Returns all defined shares of a media type
	 * @param response Response object
	 * @param mediaType Media type
	 */
	public void getShares(final DataResponse<ArrayList<FileLocation>> response, final int mediaType, final Context context) {
		mHandler.post(new Command<ArrayList<FileLocation>>(response, this) {
			@Override
			public void doRun() throws Exception {
				response.value = info(context).getShares(InfoManager.this, mediaType);
			}
		});
	}
	
	/**
	 * Returns the contents of a directory
	 * @param response Response object
	 * @param path     Path to the directory
	 * @return
	 */
	public void getDirectory(final DataResponse<ArrayList<FileLocation>> response, final String path, final Context context, final int mediaType) {
		mHandler.post(new Command<ArrayList<FileLocation>>(response, this){
			@Override
			public void doRun() throws Exception {
				response.value = info(context).getDirectory(InfoManager.this, path, mediaType, getSort(SortType.FILENAME));
			}
			
		});
	}
	
	/**
	 * Returns the gui setting of XBMC
	 * @param response Response object
	 * @param setting  see {@link org.xbmc.api.info.GuiSettings} for all settings you can query.
	 * @param context 
	 */
	public void getGuiSettingBool(final DataResponse<Boolean> response, final int setting, final Context context) {
		mHandler.post(new Command<Boolean>(response, this) {
			@Override
			public void doRun() throws Exception {
				response.value = info(context).getGuiSettingBool(InfoManager.this, setting);
			}
		});
	}
	
	/**
	 * Returns the gui setting of XBMC
	 * @param response Response object
	 * @param setting  see {@link org.xbmc.api.info.GuiSettings} for all settings you can query.
	 * @param context 
	 */
	public void getGuiSettingInt(final DataResponse<Integer> response, final int setting, final Context context) {
		mHandler.post(new Command<Integer>(response, this) {
			@Override
			public void doRun() throws Exception {
				response.value = info(context).getGuiSettingInt(InfoManager.this, setting);
			}
		});
	}
	
	/**
	 * NOT YET IMPLEMENTED! Returns the gui setting of XBMC 
	 * @param response Response object
	 * @param setting  see {@link org.xbmc.api.info.GuiSettings} for all settings you can query.
	 * @param context 
	 */
	public void getGuiSettingString(final DataResponse<String> response, final int setting) {
		
	}
	
	/**
	 * Sets an integer GUI setting
	 * @param response Response object
	 * @param field Field to return (see GuiSettings.java)
	 * @param val Integer value to set
	 */
	public void setGuiSettingInt(final DataResponse<Boolean> response, final int field, final int val, final Context context) {
		mHandler.post(new Command<Boolean>(response, this) {
			@Override
			public void doRun() throws Exception { 
				response.value = info(context).setGuiSettingInt(InfoManager.this, field, val);
			}
		});
	}	
}

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

package org.xbmc.api.business;

import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.presentation.INotifiableController;

import android.graphics.Bitmap;

public interface IManager {
	
	/**
	 * Sets the current controller object. Must be set on each activity's onResume().
	 * @param controller Controller object
	 */
	public void setController(INotifiableController controller);
	
	/**
	 * Returns bitmap of any cover. Note that the callback is done by the
	 * helper methods below.
	 * @param response Response object
	 */
	public void getCover(final DataResponse<Bitmap> response, final ICoverArt cover, final int thumbSize, Bitmap defaultCover);

}
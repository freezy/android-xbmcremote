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

package org.xbmc.android.remote.presentation.controller.holder;

import android.widget.ImageView;
import android.widget.TextView;

public class MovieHolder<T> extends AbstractHolder {

	public long id = 0;

	public final TextView titleView;
	public final TextView subtitleView;
	public final TextView subtitleRightView;
	public final TextView bottomView;
	public final TextView bottomRightView;
	
	public T holderItem;

	public MovieHolder(ImageView icon, TextView title, TextView subtitle, TextView subtitleRight, TextView bottom, TextView bottomRight) {
		iconView = icon;
		titleView = title;
		subtitleView = subtitle;
		subtitleRightView = subtitleRight;
		bottomView = bottom;
		bottomRightView = bottomRight;
	}
}
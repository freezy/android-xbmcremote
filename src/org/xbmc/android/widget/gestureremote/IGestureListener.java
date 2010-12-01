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

package org.xbmc.android.widget.gestureremote;

public interface IGestureListener {
	public void onVerticalMove(int zoneIndex);
	public void onHorizontalMove(int zoneIndex);
	public void onSelect();
	public void onScrollUp(double amount);
	public void onScrollDown(double amount);
	public void onScrollUp();
	public void onScrollDown();
	public void onMenu();
	public void onTitle();
	public void onInfo();
	public void onBack();
	public double[] getZones();
}
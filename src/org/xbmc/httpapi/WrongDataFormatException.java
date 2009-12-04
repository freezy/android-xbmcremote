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

package org.xbmc.httpapi;

/**
 * Throw this exception if API response is something different than expected.
 * 
 * @author Team XBMC
 */
public class WrongDataFormatException extends Exception {
	private static final long serialVersionUID = 42438942451326636L;
	private String mExpected;
	private String mReceived;
	public WrongDataFormatException(String expected, String received) {
		super("Wrong data format, expected '" + expected + "', got '" + received + "'.");
		mExpected = expected;
		mReceived = received;
	}
	public String getExpected() {
		return mExpected;
	}
	public String getReceived() {
		return mReceived;
	}
}
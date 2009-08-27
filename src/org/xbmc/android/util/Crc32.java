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

package org.xbmc.android.util;
import java.io.UnsupportedEncodingException;

/**
 * This is basically a transcript from XBMC's Crc32.cpp. It avoids having to
 * query for thumb names by the HTTP API.
 * @author freezy <phreezie@gmail.com>
 */
public class Crc32 {
	
	private int mCrc;

	public Crc32() {
		reset();
	}
	public String getHexValue() {
		return String.format("%08x", mCrc);
	}
	public void reset() {
		mCrc = 0xFFFFFFFF;
	}
	public void compute(byte[] buffer) {
		int count = buffer.length;
		while (count-- > 0) {
			compute(buffer[buffer.length - count - 1]);
		}
	}
	public void compute(byte value) {
		mCrc ^= (value << 24);
		for (int i = 0; i < 8; i++) {
			if ((mCrc & 0x80000000) != 0) {
				mCrc = (mCrc << 1) ^ 0x04C11DB7;
			} else {
				mCrc <<= 1;
			}
		}
	}
	public void compute(String strValue) {
		try {
			compute(strValue.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			compute(strValue.getBytes());
		}
	}
	public void computeFromLowerCase(String strValue) {
		compute(strValue.toLowerCase());
	}
}

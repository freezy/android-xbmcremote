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
	
	public static int compute(byte[] buffer, int crc) {
		int count = buffer.length;
		while (count-- > 0) {
			crc = compute(buffer[buffer.length - count - 1], crc);
		}
		return crc;
	}
	public static int compute(byte value, int crc) {
		crc ^= (value << 24);
		for (int i = 0; i < 8; i++) {
			if ((crc & 0x80000000) != 0) {
				crc = (crc << 1) ^ 0x04C11DB7;
			} else {
				crc <<= 1;
			}
		}
		return crc;
	}
	public static int compute(String strValue) {
		try {
			return compute(strValue.getBytes("UTF-8"), 0xFFFFFFFF);
		} catch (UnsupportedEncodingException e) {
			return compute(strValue.getBytes(), 0xFFFFFFFF);
		}
	}
	public static String computeAsHex(String strValue) {
		return String.format("%08x", compute(strValue));
	}
}

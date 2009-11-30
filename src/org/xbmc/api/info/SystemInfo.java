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

package org.xbmc.api.info;

public class SystemInfo {
	public static final int SYSTEM_TEMPERATURE_UNITS    = 106;
	public static final int SYSTEM_PROGRESS_BAR         = 107;
	public static final int SYSTEM_LANGUAGE             = 108;
	public static final int SYSTEM_TIME                 = 110;
	public static final int SYSTEM_DATE                 = 111;
	public static final int SYSTEM_CPU_TEMPERATURE      = 112;
	public static final int SYSTEM_GPU_TEMPERATURE      = 113;
	public static final int SYSTEM_FAN_SPEED            = 114;
	public static final int SYSTEM_FREE_SPACE_C         = 115;
//	public static final int SYSTEM_FREE_SPACE_D         = 116; // 116 is reserved for space on D
	public static final int SYSTEM_FREE_SPACE_E         = 117;
	public static final int SYSTEM_FREE_SPACE_F         = 118;
	public static final int SYSTEM_FREE_SPACE_G         = 119;
	public static final int SYSTEM_BUILD_VERSION        = 120;
	public static final int SYSTEM_BUILD_DATE           = 121;
	public static final int SYSTEM_ETHERNET_LINK_ACTIVE = 122;
	public static final int SYSTEM_FPS                  = 123;
	public static final int SYSTEM_ALWAYS_TRUE          = 125;  // useful for <visible fade="10" start="hidden">true</visible>, to fade in a control
	public static final int SYSTEM_ALWAYS_FALSE         = 126;  // used for <visible fade="10">false</visible>, to fade out a control (ie not particularly useful!)
	public static final int SYSTEM_MEDIA_DVD            = 127;
	public static final int SYSTEM_DVDREADY             = 128;
	public static final int SYSTEM_HAS_ALARM            = 129;
	public static final int SYSTEM_SCREEN_MODE          = 132;
	public static final int SYSTEM_SCREEN_WIDTH         = 133;
	public static final int SYSTEM_SCREEN_HEIGHT        = 134;
	public static final int SYSTEM_CURRENT_WINDOW       = 135;
	public static final int SYSTEM_CURRENT_CONTROL      = 136;
	public static final int SYSTEM_DVD_LABEL            = 138;
	public static final int SYSTEM_HAS_DRIVE_F          = 139;
	public static final int SYSTEM_HASLOCKS             = 140;
	public static final int SYSTEM_ISMASTER             = 141;
	public static final int SYSTEM_TRAYOPEN             = 142;
	public static final int SYSTEM_ALARM_POS            = 144;
	public static final int SYSTEM_LOGGEDON             = 145;
	public static final int SYSTEM_PROFILENAME          = 146;
	public static final int SYSTEM_PROFILETHUMB         = 147;
	public static final int SYSTEM_HAS_LOGINSCREEN      = 148;
	public static final int SYSTEM_HAS_DRIVE_G          = 149;
	public static final int SYSTEM_HDD_SMART            = 150;
	public static final int SYSTEM_HDD_TEMPERATURE      = 151;
	public static final int SYSTEM_HDD_MODEL            = 152;
	public static final int SYSTEM_HDD_SERIAL           = 153;
	public static final int SYSTEM_HDD_FIRMWARE         = 154;
	public static final int SYSTEM_HDD_PASSWORD         = 156;
	public static final int SYSTEM_HDD_LOCKSTATE        = 157;
	public static final int SYSTEM_HDD_LOCKKEY          = 158;
	public static final int SYSTEM_INTERNET_STATE       = 159;
}

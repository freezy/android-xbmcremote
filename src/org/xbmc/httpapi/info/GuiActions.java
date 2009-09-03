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

package org.xbmc.httpapi.info;

public class GuiActions {
	
	public static int ACTION_NONE                  =  0;
	public static int ACTION_MOVE_LEFT             =  1;
	public static int ACTION_MOVE_RIGHT            =  2;
	public static int ACTION_MOVE_UP               =  3;
	public static int ACTION_MOVE_DOWN             =  4;
	public static int ACTION_PAGE_UP               =  5;
	public static int ACTION_PAGE_DOWN             =  6;
	public static int ACTION_SELECT_ITEM           =  7;
	public static int ACTION_HIGHLIGHT_ITEM        =  8;
	public static int ACTION_PARENT_DIR            =  9;
	public static int ACTION_PREVIOUS_MENU         = 10;
	public static int ACTION_SHOW_INFO             = 11;

	public static int ACTION_PAUSE                 = 12;
	public static int ACTION_STOP                  = 13;
	public static int ACTION_NEXT_ITEM             = 14;
	public static int ACTION_PREV_ITEM             = 15;
	public static int ACTION_FORWARD               = 16; // Can be used to specify specific action in a window, Playback control is handled in ACTION_PLAYER_*
	public static int ACTION_REWIND                = 17; // Can be used to specify specific action in a window, Playback control is handled in ACTION_PLAYER_*

	public static int ACTION_SHOW_GUI              = 18; // toggle between GUI and movie or GUI and visualisation.
	public static int ACTION_ASPECT_RATIO          = 19; // toggle quick-access zoom modes. Can b used in videoFullScreen.zml window id=2005
	public static int ACTION_STEP_FORWARD          = 20; // seek +1% in the movie. Can b used in videoFullScreen.xml window id=2005
	public static int ACTION_STEP_BACK             = 21; // seek -1% in the movie. Can b used in videoFullScreen.xml window id=2005
	public static int ACTION_BIG_STEP_FORWARD      = 22; // seek +10% in the movie. Can b used in videoFullScreen.xml window id=2005
	public static int ACTION_BIG_STEP_BACK         = 23; // seek -10% in the movie. Can b used in videoFullScreen.xml window id=2005
	public static int ACTION_SHOW_OSD              = 24; // show/hide OSD. Can b used in videoFullScreen.xml window id=2005
	public static int ACTION_SHOW_SUBTITLES        = 25; // turn subtitles on/off. Can b used in videoFullScreen.xml window id=2005
	public static int ACTION_NEXT_SUBTITLE         = 26; // switch to next subtitle of movie. Can b used in videoFullScreen.xml window id=2005
	public static int ACTION_SHOW_CODEC            = 27; // show information about file. Can b used in videoFullScreen.xml window id=2005 and in slideshow.xml window id=2007
	public static int ACTION_NEXT_PICTURE          = 28; // show next picture of slideshow. Can b used in slideshow.xml window id=2007
	public static int ACTION_PREV_PICTURE          = 29; // show previous picture of slideshow. Can b used in slideshow.xml window id=2007
	public static int ACTION_ZOOM_OUT              = 30; // zoom in picture during slideshow. Can b used in slideshow.xml window id=2007
	public static int ACTION_ZOOM_IN               = 31; // zoom out picture during slideshow. Can b used in slideshow.xml window id=2007
	public static int ACTION_TOGGLE_SOURCE_DEST    = 32; // used to toggle between source view and destination view. Can be used in myfiles.xml window id=3
	public static int ACTION_SHOW_PLAYLIST         = 33; // used to toggle between current view and playlist view. Can b used in all mymusic xml files
	public static int ACTION_QUEUE_ITEM            = 34; // used to queue a item to the playlist. Can b used in all mymusic xml files
	public static int ACTION_REMOVE_ITEM           = 35; // not used anymore
	public static int ACTION_SHOW_FULLSCREEN       = 36; // not used anymore
	public static int ACTION_ZOOM_LEVEL_NORMAL     = 37; // zoom 1x picture during slideshow. Can b used in slideshow.xml window id=2007
	public static int ACTION_ZOOM_LEVEL_1          = 38; // zoom 2x picture during slideshow. Can b used in slideshow.xml window id=2007
	public static int ACTION_ZOOM_LEVEL_2          = 39; // zoom 3x picture during slideshow. Can b used in slideshow.xml window id=2007
	public static int ACTION_ZOOM_LEVEL_3          = 40; // zoom 4x picture during slideshow. Can b used in slideshow.xml window id=2007
	public static int ACTION_ZOOM_LEVEL_4          = 41; // zoom 5x picture during slideshow. Can b used in slideshow.xml window id=2007
	public static int ACTION_ZOOM_LEVEL_5          = 42; // zoom 6x picture during slideshow. Can b used in slideshow.xml window id=2007
	public static int ACTION_ZOOM_LEVEL_6          = 43; // zoom 7x picture during slideshow. Can b used in slideshow.xml window id=2007
	public static int ACTION_ZOOM_LEVEL_7          = 44; // zoom 8x picture during slideshow. Can b used in slideshow.xml window id=2007
	public static int ACTION_ZOOM_LEVEL_8          = 45; // zoom 9x picture during slideshow. Can b used in slideshow.xml window id=2007
	public static int ACTION_ZOOM_LEVEL_9          = 46; // zoom 10x picture during slideshow. Can b used in slideshow.xml window id=2007

	public static int ACTION_CALIBRATE_SWAP_ARROWS = 47; // select next arrow. Can b used in: settingsScreenCalibration.xml windowid=11
	public static int ACTION_CALIBRATE_RESET       = 48; // reset calibration to defaults. Can b used in: settingsScreenCalibration.xml windowid=11/settingsUICalibration.xml windowid=10
	public static int ACTION_ANALOG_MOVE           = 49; // analog thumbstick move. Can b used in: slideshow.xml window id=2007/settingsScreenCalibration.xml windowid=11/settingsUICalibration.xml windowid=10
	public static int ACTION_ROTATE_PICTURE        = 50; // rotate current picture during slideshow. Can b used in slideshow.xml window id=2007
	public static int ACTION_CLOSE_DIALOG          = 51; // action for closing the dialog. Can b used in any dialog
	public static int ACTION_SUBTITLE_DELAY_MIN    = 52; // Decrease subtitle/movie Delay.  Can b used in videoFullScreen.xml window id=2005
	public static int ACTION_SUBTITLE_DELAY_PLUS   = 53; // Increase subtitle/movie Delay.  Can b used in videoFullScreen.xml window id=2005
	public static int ACTION_AUDIO_DELAY_MIN       = 54; // Increase avsync delay.  Can b used in videoFullScreen.xml window id=2005
	public static int ACTION_AUDIO_DELAY_PLUS      = 55; // Decrease avsync delay.  Can b used in videoFullScreen.xml window id=2005
	public static int ACTION_AUDIO_NEXT_LANGUAGE   = 56; // Select next language in movie.  Can b used in videoFullScreen.xml window id=2005
	public static int ACTION_CHANGE_RESOLUTION     = 57; // switch 2 next resolution. Can b used during screen calibration settingsScreenCalibration.xml windowid=11
}
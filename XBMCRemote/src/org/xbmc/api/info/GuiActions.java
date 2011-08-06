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

public abstract class GuiActions {
	
	public static final int ACTION_NONE                  =  0;
	public static final int ACTION_MOVE_LEFT             =  1;
	public static final int ACTION_MOVE_RIGHT            =  2;
	public static final int ACTION_MOVE_UP               =  3;
	public static final int ACTION_MOVE_DOWN             =  4;
	public static final int ACTION_PAGE_UP               =  5;
	public static final int ACTION_PAGE_DOWN             =  6;
	public static final int ACTION_SELECT_ITEM           =  7;
	public static final int ACTION_HIGHLIGHT_ITEM        =  8;
	public static final int ACTION_PARENT_DIR            =  9;
	public static final int ACTION_PREVIOUS_MENU         = 10;
	public static final int ACTION_SHOW_INFO             = 11;

	public static final int ACTION_PAUSE                 = 12;
	public static final int ACTION_STOP                  = 13;
	public static final int ACTION_NEXT_ITEM             = 14;
	public static final int ACTION_PREV_ITEM             = 15;
	public static final int ACTION_FORWARD               = 16; // Can be used to specify specific action in a window, Playback control is handled in ACTION_PLAYER_*
	public static final int ACTION_REWIND                = 17; // Can be used to specify specific action in a window, Playback control is handled in ACTION_PLAYER_*

	public static final int ACTION_SHOW_GUI              = 18; // toggle between GUI and movie or GUI and visualisation.
	public static final int ACTION_ASPECT_RATIO          = 19; // toggle quick-access zoom modes. Can b used in videoFullScreen.zml window id=2005
	public static final int ACTION_STEP_FORWARD          = 20; // seek +1% in the movie. Can b used in videoFullScreen.xml window id=2005
	public static final int ACTION_STEP_BACK             = 21; // seek -1% in the movie. Can b used in videoFullScreen.xml window id=2005
	public static final int ACTION_BIG_STEP_FORWARD      = 22; // seek +10% in the movie. Can b used in videoFullScreen.xml window id=2005
	public static final int ACTION_BIG_STEP_BACK         = 23; // seek -10% in the movie. Can b used in videoFullScreen.xml window id=2005
	public static final int ACTION_SHOW_OSD              = 24; // show/hide OSD. Can b used in videoFullScreen.xml window id=2005
	public static final int ACTION_SHOW_SUBTITLES        = 25; // turn subtitles on/off. Can b used in videoFullScreen.xml window id=2005
	public static final int ACTION_NEXT_SUBTITLE         = 26; // switch to next subtitle of movie. Can b used in videoFullScreen.xml window id=2005
	public static final int ACTION_SHOW_CODEC            = 27; // show information about file. Can b used in videoFullScreen.xml window id=2005 and in slideshow.xml window id=2007
	public static final int ACTION_NEXT_PICTURE          = 28; // show next picture of slideshow. Can b used in slideshow.xml window id=2007
	public static final int ACTION_PREV_PICTURE          = 29; // show previous picture of slideshow. Can b used in slideshow.xml window id=2007
	public static final int ACTION_ZOOM_OUT              = 30; // zoom in picture during slideshow. Can b used in slideshow.xml window id=2007
	public static final int ACTION_ZOOM_IN               = 31; // zoom out picture during slideshow. Can b used in slideshow.xml window id=2007
	public static final int ACTION_TOGGLE_SOURCE_DEST    = 32; // used to toggle between source view and destination view. Can be used in myfiles.xml window id=3
	public static final int ACTION_SHOW_PLAYLIST         = 33; // used to toggle between current view and playlist view. Can b used in all mymusic xml files
	public static final int ACTION_QUEUE_ITEM            = 34; // used to queue a item to the playlist. Can b used in all mymusic xml files
	public static final int ACTION_REMOVE_ITEM           = 35; // not used anymore
	public static final int ACTION_SHOW_FULLSCREEN       = 36; // not used anymore
	public static final int ACTION_ZOOM_LEVEL_NORMAL     = 37; // zoom 1x picture during slideshow. Can b used in slideshow.xml window id=2007
	public static final int ACTION_ZOOM_LEVEL_1          = 38; // zoom 2x picture during slideshow. Can b used in slideshow.xml window id=2007
	public static final int ACTION_ZOOM_LEVEL_2          = 39; // zoom 3x picture during slideshow. Can b used in slideshow.xml window id=2007
	public static final int ACTION_ZOOM_LEVEL_3          = 40; // zoom 4x picture during slideshow. Can b used in slideshow.xml window id=2007
	public static final int ACTION_ZOOM_LEVEL_4          = 41; // zoom 5x picture during slideshow. Can b used in slideshow.xml window id=2007
	public static final int ACTION_ZOOM_LEVEL_5          = 42; // zoom 6x picture during slideshow. Can b used in slideshow.xml window id=2007
	public static final int ACTION_ZOOM_LEVEL_6          = 43; // zoom 7x picture during slideshow. Can b used in slideshow.xml window id=2007
	public static final int ACTION_ZOOM_LEVEL_7          = 44; // zoom 8x picture during slideshow. Can b used in slideshow.xml window id=2007
	public static final int ACTION_ZOOM_LEVEL_8          = 45; // zoom 9x picture during slideshow. Can b used in slideshow.xml window id=2007
	public static final int ACTION_ZOOM_LEVEL_9          = 46; // zoom 10x picture during slideshow. Can b used in slideshow.xml window id=2007

	public static final int ACTION_CALIBRATE_SWAP_ARROWS = 47; // select next arrow. Can b used in: settingsScreenCalibration.xml windowid=11
	public static final int ACTION_CALIBRATE_RESET       = 48; // reset calibration to defaults. Can b used in: settingsScreenCalibration.xml windowid=11/settingsUICalibration.xml windowid=10
	public static final int ACTION_ANALOG_MOVE           = 49; // analog thumbstick move. Can b used in: slideshow.xml window id=2007/settingsScreenCalibration.xml windowid=11/settingsUICalibration.xml windowid=10
	public static final int ACTION_ROTATE_PICTURE        = 50; // rotate current picture during slideshow. Can b used in slideshow.xml window id=2007
	public static final int ACTION_CLOSE_DIALOG          = 51; // action for closing the dialog. Can b used in any dialog
	public static final int ACTION_SUBTITLE_DELAY_MIN    = 52; // Decrease subtitle/movie Delay.  Can b used in videoFullScreen.xml window id=2005
	public static final int ACTION_SUBTITLE_DELAY_PLUS   = 53; // Increase subtitle/movie Delay.  Can b used in videoFullScreen.xml window id=2005
	public static final int ACTION_AUDIO_DELAY_MIN       = 54; // Increase avsync delay.  Can b used in videoFullScreen.xml window id=2005
	public static final int ACTION_AUDIO_DELAY_PLUS      = 55; // Decrease avsync delay.  Can b used in videoFullScreen.xml window id=2005
	public static final int ACTION_AUDIO_NEXT_LANGUAGE   = 56; // Select next language in movie.  Can b used in videoFullScreen.xml window id=2005
	public static final int ACTION_CHANGE_RESOLUTION     = 57; // switch 2 next resolution. Can b used during screen calibration settingsScreenCalibration.xml windowid=11
	
	public static final int REMOTE_0                     = 58;  // remote keys 0-9. are used by multiple windows
	public static final int REMOTE_1                     = 59;  // for example in videoFullScreen.xml window id=2005 you can
	public static final int REMOTE_2                     = 60;  // enter time (mmss) to jump to particular point in the movie
	public static final int REMOTE_3                     = 61;
	public static final int REMOTE_4                     = 62;  // with spincontrols you can enter 3digit number to quickly set
	public static final int REMOTE_5                     = 63;  // spincontrol to desired value
	public static final int REMOTE_6                     = 64;
	public static final int REMOTE_7                     = 65;
	public static final int REMOTE_8                     = 66;
	public static final int REMOTE_9                     = 67;

	public static final int ACTION_PLAY                  = 68;  // Unused at the moment
	public static final int ACTION_OSD_SHOW_LEFT         = 69;  // Move left in OSD. Can b used in videoFullScreen.xml window id=2005
	public static final int ACTION_OSD_SHOW_RIGHT        = 70;  // Move right in OSD. Can b used in videoFullScreen.xml window id=2005
	public static final int ACTION_OSD_SHOW_UP           = 71;  // Move up in OSD. Can b used in videoFullScreen.xml window id=2005
	public static final int ACTION_OSD_SHOW_DOWN         = 72;  // Move down in OSD. Can b used in videoFullScreen.xml window id=2005
	public static final int ACTION_OSD_SHOW_SELECT       = 73;  // toggle/select option in OSD. Can b used in videoFullScreen.xml window id=2005
	public static final int ACTION_OSD_SHOW_VALUE_PLUS   = 74;  // increase value of current option in OSD. Can b used in videoFullScreen.xml window id=2005
	public static final int ACTION_OSD_SHOW_VALUE_MIN    = 75;  // decrease value of current option in OSD. Can b used in videoFullScreen.xml window id=2005
	public static final int ACTION_SMALL_STEP_BACK       = 76;  // jumps a few seconds back during playback of movie. Can b used in videoFullScreen.xml window id=2005

	public static final int ACTION_PLAYER_FORWARD        = 77;  // FF in current file played. global action, can be used anywhere
	public static final int ACTION_PLAYER_REWIND         = 78;  // RW in current file played. global action, can be used anywhere
	public static final int ACTION_PLAYER_PLAY           = 79;  // Play current song. Unpauses song and sets playspeed to 1x. global action, can be used anywhere

	public static final int ACTION_DELETE_ITEM           = 80;  // delete current selected item. Can be used in myfiles.xml window id=3 and in myvideoTitle.xml window id=25
	public static final int ACTION_COPY_ITEM             = 81;  // copy current selected item. Can be used in myfiles.xml window id=3
	public static final int ACTION_MOVE_ITEM             = 82;  // move current selected item. Can be used in myfiles.xml window id=3
	public static final int ACTION_SHOW_MPLAYER_OSD      = 83;  // toggles mplayers OSD. Can be used in videofullscreen.xml window id=2005
	public static final int ACTION_OSD_HIDESUBMENU       = 84;  // removes an OSD sub menu. Can be used in videoOSD.xml window id=2901
	public static final int ACTION_TAKE_SCREENSHOT       = 85;  // take a screenshot
	public static final int ACTION_RENAME_ITEM           = 87;  // rename item

	public static final int ACTION_VOLUME_UP             = 88;
	public static final int ACTION_VOLUME_DOWN           = 89;
	public static final int ACTION_MUTE                  = 91;

	public static final int ACTION_MOUSE                 = 90;

	public static final int ACTION_MOUSE_CLICK           = 100;
	public static final int ACTION_MOUSE_LEFT_CLICK      = 100;
	public static final int ACTION_MOUSE_RIGHT_CLICK     = 101;
	public static final int ACTION_MOUSE_MIDDLE_CLICK    = 102;
	public static final int ACTION_MOUSE_XBUTTON1_CLICK  = 103;
	public static final int ACTION_MOUSE_XBUTTON2_CLICK  = 104;

	public static final int ACTION_MOUSE_DOUBLE_CLICK            = 105;
	public static final int ACTION_MOUSE_LEFT_DOUBLE_CLICK       = 105;
	public static final int ACTION_MOUSE_RIGHT_DOUBLE_CLICK      = 106;
	public static final int ACTION_MOUSE_MIDDLE_DOUBLE_CLICK     = 107;
	public static final int ACTION_MOUSE_XBUTTON1_DOUBLE_CLICK   = 108;
	public static final int ACTION_MOUSE_XBUTTON2_DOUBLE_CLICK   = 109;

	public static final int ACTION_BACKSPACE           = 110;
	public static final int ACTION_SCROLL_UP           = 111;
	public static final int ACTION_SCROLL_DOWN         = 112;
	public static final int ACTION_ANALOG_FORWARD      = 113;
	public static final int ACTION_ANALOG_REWIND       = 114;

	public static final int ACTION_MOVE_ITEM_UP        = 115;  // move item up in playlist
	public static final int ACTION_MOVE_ITEM_DOWN      = 116;  // move item down in playlist
	public static final int ACTION_CONTEXT_MENU        = 117;  // pops up the context menu


	// stuff for virtual keyboard shortcuts
	public static final int ACTION_SHIFT                   = 118;
	public static final int ACTION_SYMBOLS                 = 119;
	public static final int ACTION_CURSOR_LEFT             = 120;
	public static final int ACTION_CURSOR_RIGHT            = 121;

	public static final int ACTION_BUILT_IN_FUNCTION       = 122;

	public static final int ACTION_SHOW_OSD_TIME           = 123; // displays current time, can be used in videoFullScreen.xml window id=2005
	public static final int ACTION_ANALOG_SEEK_FORWARD     = 124; // seeks forward, and displays the seek bar.
	public static final int ACTION_ANALOG_SEEK_BACK        = 125; // seeks backward, and displays the seek bar.

	public static final int ACTION_VIS_PRESET_SHOW         = 126;
	public static final int ACTION_VIS_PRESET_LIST         = 127;
	public static final int ACTION_VIS_PRESET_NEXT         = 128;
	public static final int ACTION_VIS_PRESET_PREV         = 129;
	public static final int ACTION_VIS_PRESET_LOCK         = 130;
	public static final int ACTION_VIS_PRESET_RANDOM       = 131;
	public static final int ACTION_VIS_RATE_PRESET_PLUS    = 132;
	public static final int ACTION_VIS_RATE_PRESET_MINUS   = 133;

	public static final int ACTION_SHOW_VIDEOMENU          = 134;
	public static final int ACTION_ENTER                   = 135;

	public static final int ACTION_INCREASE_RATING         = 136;
	public static final int ACTION_DECREASE_RATING         = 137;

	public static final int ACTION_NEXT_SCENE              = 138; // switch to next scene/cutpoint in movie
	public static final int ACTION_PREV_SCENE              = 139; // switch to previous scene/cutpoint in movie

	public static final int ACTION_NEXT_LETTER             = 140; // jump through a list or container by letter
	public static final int ACTION_PREV_LETTER             = 141;

	public static final int ACTION_JUMP_SMS2               = 142; // jump direct to a particular letter using SMS-style input
	public static final int ACTION_JUMP_SMS3               = 143;
	public static final int ACTION_JUMP_SMS4               = 144;
	public static final int ACTION_JUMP_SMS5               = 145;
	public static final int ACTION_JUMP_SMS6               = 146;
	public static final int ACTION_JUMP_SMS7               = 147;
	public static final int ACTION_JUMP_SMS8               = 148;
	public static final int ACTION_JUMP_SMS9               = 149;

	public static final int ACTION_FILTER_CLEAR            = 150;
	public static final int ACTION_FILTER_SMS2             = 151;
	public static final int ACTION_FILTER_SMS3             = 152;
	public static final int ACTION_FILTER_SMS4             = 153;
	public static final int ACTION_FILTER_SMS5             = 154;
	public static final int ACTION_FILTER_SMS6             = 155;
	public static final int ACTION_FILTER_SMS7             = 156;
	public static final int ACTION_FILTER_SMS8             = 157;
	public static final int ACTION_FILTER_SMS9             = 158;

	public static final int ACTION_FIRST_PAGE              = 159;
	public static final int ACTION_LAST_PAGE               = 160;

	public static final int ACTION_AUDIO_DELAY             = 161;
	public static final int ACTION_SUBTITLE_DELAY          = 162;

	public static final int ACTION_PASTE                   = 180;
	public static final int ACTION_NEXT_CONTROL            = 181;
	public static final int ACTION_PREV_CONTROL            = 182;
	public static final int ACTION_CHANNEL_SWITCH          = 183;

	public static final int ACTION_TOGGLE_FULLSCREEN       = 199; // switch 2 desktop resolution
	public static final int ACTION_TOGGLE_WATCHED          = 200; // Toggle watched status (videos)
	public static final int ACTION_SCAN_ITEM               = 201; // scan item
	public static final int ACTION_TOGGLE_DIGITAL_ANALOG   = 202; // switch digital <-> analog
	public static final int ACTION_RELOAD_KEYMAPS          = 203; // reloads CButtonTranslator's keymaps
	public static final int ACTION_GUIPROFILE_BEGIN        = 204; // start the GUIControlProfiler running
}
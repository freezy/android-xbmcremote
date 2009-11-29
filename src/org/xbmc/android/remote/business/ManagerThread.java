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

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * Spawned on first access, then looping. Takes all HTTP API commands and
 * synchronously returns the result.
 * 
 * @author Team XBMC
 */
public class ManagerThread extends Thread {

	private static ManagerThread sManagerThread;
	private Handler mHandler;
	
	private final InfoManager mInfoManager;
	private final ControlManager mControlManager;
	private final MusicManager mMusicManager;
	private final VideoManager mVideoManager;
	
	private ManagerThread() {
		super("ManagerThread");
		mInfoManager = new InfoManager();
		mControlManager = new ControlManager();
		mMusicManager = new MusicManager();
		mVideoManager = new VideoManager();
	}
	public static ManagerThread get() {
		if (sManagerThread == null) {
			sManagerThread = new ManagerThread();
			sManagerThread.start();
			// thread must be entirely started
			while (sManagerThread.mHandler == null) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return sManagerThread;
	}
	
	public void run() {
		Looper.prepare();
		mHandler = new Handler();
		mInfoManager.setHandler(mHandler);
		mControlManager.setHandler(mHandler);
		mMusicManager.setHandler(mHandler);
		mVideoManager.setHandler(mHandler);
		Looper.loop();
	}
	public static InfoManager info(Context context) {
		InfoManager im = get().mInfoManager;
		im.setContext(context);
		return im;
	}
	public static ControlManager control(Context context) {
		ControlManager cm = get().mControlManager;
		cm.setContext(context);
		return cm;
	}
	public static MusicManager music(Context context) {
		MusicManager mm = get().mMusicManager;
		mm.setContext(context);
		return mm;
	}
	public static VideoManager video(Context context) {
		VideoManager vm = get().mVideoManager;
		vm.setContext(context);
		return vm;
	}
}
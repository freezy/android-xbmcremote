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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Observable;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.xbmc.httpapi.client.ControlClient.ICurrentlyPlaying;

public class BroadcastListener extends Observable implements Runnable {
	
	public static final int EVENT_ERROR               = -1;
	public static final int EVENT_UNKNOWN             = 0;
	public static final int EVENT_STARTUP             = 1;
	public static final int EVENT_SHUTDOWN            = 2;
	public static final int EVENT_ON_ACTION           = 3;
	public static final int EVENT_ON_PLAYBACK_STARTED = 4;
	public static final int EVENT_ON_PLAYBACK_ENDED   = 5;
	public static final int EVENT_ON_PLAYBACK_STOPPED = 6;
	public static final int EVENT_ON_PLAYBACK_PAUSED  = 7;
	public static final int EVENT_ON_PLAYBACK_RESUMED = 8;
	public static final int EVENT_ON_QUEUE_NEXT_ITEM  = 9;
	public static final int EVENT_ON_MEDIA_CHANGED    = 10;
	public static final int EVENT_ON_PLAYPOSITON_CHANGED = 11;
	public static final int EVENT_AVAILABLE              = 100;
	public static final int EVENT_TIMEOUT                = 101;

	private static final String THREAD_NAME = "BroadcastListener";
	private static final String TIMER_NAME  = "BroadcastTimer";
	private static final String BCAST_PING  = "OnXbmcRemoteTest";
	
	private static final int TIMEOUT = 10;
	private static final int DEFAULT_PORT = 8278;
	private static final int BUFFER_LENGTH = 256;
	private static final int BCAST_LEVEL = 2;
	private static final String BCAST_ADDR = "255.255.255.255";
	
	private static BroadcastListener sInstance;
	private static Thread sThread;
	private static final Timer sTimer = new Timer(TIMER_NAME);
	
	private final HttpClient mHttpClient;
	
	private boolean mIsListening = false;
	private boolean mIsAvailable = false;
	private int mPort = 0;
	
	public static BroadcastListener getInstance(HttpClient httpClient) {
		if (sInstance == null) {
			sInstance = new BroadcastListener(httpClient);
			sThread = new Thread(sInstance, THREAD_NAME);
		}
		return sInstance;
	}
	
	private BroadcastListener(HttpClient httpClient) {
		mHttpClient = httpClient;
		init();
	}
	
	public void init() {
		(new Thread(THREAD_NAME + "-INIT") {
			public void run() {
				final int port = mHttpClient.control.getBroadcast();
				if (port == 0 || port == DEFAULT_PORT) {
					Random rnd = new Random();
					final int rndPort = (rnd.nextInt() % 22768) + 10000;
					if (!mHttpClient.control.setBroadcast(rndPort, BCAST_LEVEL)) {
						mIsAvailable = false;
						mPort = 0;
						return;
					}
					mPort = rndPort;
				} else {
					mPort = port;
				}
				// now we have a port, launch the listener thread
				sThread.start();
				int n = 0;
				while (!mIsAvailable || !mIsListening) {
					mHttpClient.control.broadcast(BCAST_PING);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) { }
					if (n++ > TIMEOUT) {
						dispatch(EVENT_TIMEOUT);
						break;
					}
				}
			}
		}).start();
	}

	public void run() {
		try {
			
			byte[] b = new byte[BUFFER_LENGTH];
			DatagramPacket packet = new DatagramPacket(b, b.length);
			DatagramSocket socket = new DatagramSocket(mPort, InetAddress.getByName(BCAST_ADDR));
			
			while (true) {
				mIsListening = true;
				// blocks until a datagram is received
				socket.receive(packet); 
				String received = new String(packet.getData(), 0, packet.getLength());
				// must reset length field
				packet.setLength(b.length);
				handle(received.replace("<b>", "").replace("</b>", ""));
				
				System.err.println("Received " + packet.getLength() + " bytes from " + packet.getAddress() + ": " + received);
			}
		} catch (SocketException e) {
			e.printStackTrace();
			String[] params = { e.getMessage() };
			dispatch(EVENT_ERROR, params);
		} catch (IOException e) {
			e.printStackTrace();
			String[] params = { e.getMessage() };
			dispatch(EVENT_ERROR, params);
		}
	}
	
	private void dispatch(int event, String[] params) {
		setChanged();
		notifyObservers(new Event(event, params));
	}
	private void dispatch(int event) {
		String[] params = {};
		dispatch(event, params);
	}
	
	private void handle(String response) {
		String param = "";
		String params[] = {};
		int event = EVENT_UNKNOWN;
		if (response.contains(":")) {
			param = response.substring(response.indexOf(":"), response.lastIndexOf(";"));
		}
		if (response.startsWith("StartUp")) {
			event = EVENT_STARTUP;
		} else if (response.startsWith("ShutDown")) {
			event = EVENT_SHUTDOWN;
		} else if (response.startsWith("OnAction")) {
			event = EVENT_ON_ACTION;
			params[0] = param;
		} else if (response.startsWith("OnPlayBackStarted")) {
			event = EVENT_ON_PLAYBACK_STARTED;
			ICurrentlyPlaying currPlaying = mHttpClient.control.getCurrentlyPlaying();
			sTimer.schedule(new BroadcastListener.Counter(currPlaying.getTime(), currPlaying.getDuration()), 0L, 1000L);
		} else if (response.startsWith("OnPlayBackStopped")) {
			event = EVENT_ON_PLAYBACK_STOPPED;
			sTimer.cancel();
		} else if (response.startsWith("OnPlayBackEnded")) {
			event = EVENT_ON_PLAYBACK_ENDED;
			sTimer.cancel();
		} else if (response.startsWith("OnQueueNextItem")) {
			event = EVENT_ON_QUEUE_NEXT_ITEM;
		} else if (response.startsWith("MediaChanged")) {
			event = EVENT_ON_MEDIA_CHANGED;
			params = param.split("<li>");
		} else if (response.startsWith(BCAST_PING)) {
			event = EVENT_AVAILABLE;
			mIsAvailable = true;
		}
		dispatch(event, params);
	}
	
	private class Counter extends TimerTask {
		private int mStart, mEnd;
		Counter(int start, int end) {
			mStart = start;
			mEnd = end;
		}
		@Override
		public void run() {
			String[] params = { String.valueOf(mStart++) };
			dispatch(EVENT_ON_PLAYPOSITON_CHANGED, params);
			if (mStart > mEnd) {
				this.cancel();
			}
		}
	}
	
	public static class Event {
		public final int id;
		public final String[] params;
		public Event(int event, String[] params) {
			this.id = event;
			this.params = params;
		}
		public int getInt(int fallback) {
			try {
				return Integer.valueOf(params[0]);
			} catch (NumberFormatException e) {
				return fallback; 
			}
		}
	}
}
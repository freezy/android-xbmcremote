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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;

import org.xbmc.android.remote.data.ClientFactory;
import org.xbmc.android.util.Base64;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IControlClient;
import org.xbmc.api.data.IInfoClient;
import org.xbmc.api.data.IControlClient.ICurrentlyPlaying;
import org.xbmc.api.data.IControlClient.PlayStatus;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Activities (and other stuff) can subscribe to this thread in order to obtain
 * real-time "Now playing" information. The thread will send relevant messages
 * to all subscribers. If there are no subscriptions, nothing is polled.
 *
 * Please remember to unsubscribe (e.g. onPause()) in order to avoid unnecessary
 * polling.
 * 
 * @author Team XBMC
 */
public class NowPlayingPollerThread extends Thread {
	
	public static final String BUNDLE_CURRENTLY_PLAYING = "CurrentlyPlaying";
	public static final int MESSAGE_CONNECTION_ERROR = 1;
	public static final int MESSAGE_RECONFIGURE = 2;
	public static final int MESSAGE_PROGRESS_CHANGED = 666;
	public static final int MESSAGE_TRACK_CHANGED = 667;
	public static final int MESSAGE_COVER_CHANGED = 668;
	public static final int MESSAGE_PLAYSTATE_CHANGED = 669;

	private final IInfoClient mInfo;
	private final IControlClient mControl;
	private final HashSet<Handler> mSubscribers;
	
	private String mCoverPath;
	private Drawable mCover;
	
	/**
	 * Since this one is kinda of its own, we use a stub as manager.
	 * @TODO create some toats or at least logs instead of empty on* methods.
	 */
	private final INotifiableManager mManagerStub;
	
	public NowPlayingPollerThread(Context context){
  	  	mManagerStub = new INotifiableManager() {
			public void onMessage(int code, String message) { }
			public void onMessage(String message) { }
			public void onError(Exception e) { }
		};
		mControl = ClientFactory.getControlClient(context, mManagerStub);
  	  	mInfo = ClientFactory.getInfoClient(context, mManagerStub);
  	  	mSubscribers = new HashSet<Handler>();
	}
	
	public synchronized void subscribe(Handler handler){
		//update handler on the state of affairs
		Message msg = Message.obtain(handler);
		Bundle bundle = msg.getData();
		IControlClient control = mControl; // local access is much faster
/*		if (!control.isConnected()){
			msg.what = MESSAGE_CONNECTION_ERROR;
			bundle.putSerializable(BUNDLE_CURRENTLY_PLAYING, null);
			handler.sendMessage(msg);
		} else {*/
			final ICurrentlyPlaying currPlaying = control.getCurrentlyPlaying(mManagerStub);
			msg.what = MESSAGE_PROGRESS_CHANGED;
			bundle = msg.getData();
			bundle.putSerializable(BUNDLE_CURRENTLY_PLAYING, currPlaying);
			handler.sendMessage(msg);

	  		msg = Message.obtain(handler);
  	  		bundle = msg.getData();
  	  		bundle.putSerializable(BUNDLE_CURRENTLY_PLAYING, currPlaying);
  	  		msg.what = NowPlayingPollerThread.MESSAGE_TRACK_CHANGED;	
  	  		handler.sendMessage(msg);
  	  		
  			msg = Message.obtain(handler);
  	  		handler.sendEmptyMessage(MESSAGE_COVER_CHANGED);
//		}
		mSubscribers.add(handler);
	}
	
	public synchronized void unSubscribe(Handler handler){
		mSubscribers.remove(handler);
	}
	
	public synchronized Drawable getNowPlayingCover(){
		return mCover;
	}
	
	public synchronized void sendMessage(int what, ICurrentlyPlaying curr) {
		HashSet<Handler> subscribers = mSubscribers;
		Message msg;
		Bundle bundle;
		for (Handler handler : subscribers) {
			msg = Message.obtain(handler);
			msg.what = what;
			bundle = msg.getData();
			bundle.putSerializable(BUNDLE_CURRENTLY_PLAYING, curr);
			msg.setTarget(handler);
			handler.sendMessage(msg);
		}	
	}

	public synchronized void sendEmptyMessage(int what) {
		HashSet<Handler> subscribers = mSubscribers;
		for (Handler handler : subscribers) {
			handler.sendEmptyMessage(what);
		}	
	}
	
	public void run() {
		String lastPos = "-1";
		PlayStatus lastPlayStatus = null;
		IControlClient control = mControl; // use local reference for faster access
		HashSet<Handler> subscribers = mSubscribers;
		while (!isInterrupted() ) {
			if(subscribers.size() > 0){
/*				if (!control.isConnected()) {
					sendEmptyMessage(MESSAGE_CONNECTION_ERROR);
				} else {*/
					ICurrentlyPlaying currPlaying;
					try{
						 currPlaying = control.getCurrentlyPlaying(mManagerStub);
					} catch(Exception e) {
						sendEmptyMessage(MESSAGE_CONNECTION_ERROR);
						return;
					}
					
					sendMessage(MESSAGE_PROGRESS_CHANGED, currPlaying);
					
					String currentPos = currPlaying.getTitle() + currPlaying.getDuration();
					PlayStatus currentPlayStatus = currPlaying.getPlayStatus();
					
					if (currentPlayStatus != lastPlayStatus) {
						sendMessage(MESSAGE_PLAYSTATE_CHANGED, currPlaying);
					}
					
					if (!lastPos.equals(currentPos)) {
						lastPos = currentPos;
						
						sendMessage(MESSAGE_TRACK_CHANGED, currPlaying);
			  	  		
			  	  		try {				
			  	  			String downloadURI = mInfo.getCurrentlyPlayingThumbURI(mManagerStub);
			  	  			if (downloadURI != null && downloadURI.length() > 0) {
			  	  				if (!downloadURI.equals(mCoverPath)) {
			  	  					mCoverPath = downloadURI;
			  	  					
			  	  					byte[] buffer = download(downloadURI);
			  	  					
			  	  					if (buffer == null || buffer.length == 0)
			  	  						mCover = null;
			  	  					else 
			  	  						mCover = new BitmapDrawable(BitmapFactory.decodeByteArray(buffer, 0, buffer.length));

			  	  					for (Handler handler : subscribers) {
		  				  	  			handler.sendEmptyMessage(MESSAGE_COVER_CHANGED);
		  				  	  		}	
			  	  				}
			  	  			} else {
			  	  				mCover = null;
			  	  				if (mCoverPath != null){
			  	  					for (Handler handler : subscribers) {
		  				  	  			handler.sendEmptyMessage(MESSAGE_COVER_CHANGED);
		  				  	  		}			  	  					
			  	  				}
			  	  				mCoverPath = null;
			  	  			}
			  	  		} catch (MalformedURLException e) {
			  	  			//e.printStackTrace();
			  	  		Log.e("NowPlayingPollerThread", Log.getStackTraceString(e));
			  	  		} catch (URISyntaxException e) {
			  	  			//e.printStackTrace();
			  	  		Log.e("NowPlayingPollerThread", Log.getStackTraceString(e));
			  	  		}
					}
				}
//			}
			try {
				sleep(1000);
			}
			catch (InterruptedException e) {
				sendEmptyMessage(MESSAGE_RECONFIGURE);
				return;
			}
		}
	}
	
	private byte[] download(String pathToDownload) {
		try {
			final URL url = new URL(pathToDownload);
			final URLConnection uc = url.openConnection();
			
			final BufferedReader rd = new BufferedReader(new InputStreamReader(uc.getInputStream()), 8192);
			
			final StringBuilder sb = new StringBuilder();
			String line = "";
			while ((line = rd.readLine()) != null) {    
				sb.append(line);
			}
			
			rd.close();
			return Base64.decode(sb.toString().replace("<html>", "").replace("</html>", ""));
		} catch (Exception e) {
			return null;
		}
	}
}

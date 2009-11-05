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

package org.xbmc.android.backend.httpapi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xbmc.android.util.Base64;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.httpapi.client.ControlClient;
import org.xbmc.httpapi.client.InfoClient;
import org.xbmc.httpapi.client.ControlClient.ICurrentlyPlaying;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * @author Team XBMC
 *
 */
public class NowPlayingPollerThread extends Thread {
	
	private String mLastPos = "-1";
	private InfoClient mInfo;
	private ControlClient mControl;
	private Set<Handler> mSubscribers;
	private String mCoverPath;
	private Drawable mCover;
	public static final String BUNDLE_CURRENTLY_PLAYING = "CurrentlyPlaying";
	public static final int MESSAGE_CONNECTION_ERROR = 1;
	public static final int MESSAGE_NOW_PLAYING_PROGRESS = 666;
	public static final int MESSAGE_ARTIST_TEXT_VIEW = 667;
	public static final int MESSAGE_COVER_IMAGE = 668;
	
	public NowPlayingPollerThread(Context context){
  	  	mControl = ConnectionManager.getHttpClient(context).control;
  	  	mInfo = ConnectionManager.getHttpClient(context).info;
  	  	mSubscribers = new HashSet<Handler>();
	}
	
	public synchronized void subscribe(Handler handler){
		//update handler on the state of affairs
		Message msg = Message.obtain(handler);
		Bundle bundle = msg.getData();
		if (!mControl.isConnected()){
			msg.what = NowPlayingPollerThread.MESSAGE_CONNECTION_ERROR;
			bundle.putSerializable(NowPlayingPollerThread.BUNDLE_CURRENTLY_PLAYING, null);
			handler.sendMessage(msg);
		} else {
			final ICurrentlyPlaying currPlaying = mControl.getCurrentlyPlaying();
			msg.what = NowPlayingPollerThread.MESSAGE_NOW_PLAYING_PROGRESS;
			bundle = msg.getData();
			bundle.putSerializable(NowPlayingPollerThread.BUNDLE_CURRENTLY_PLAYING, currPlaying);
			handler.sendMessage(msg);
			
	  		msg = Message.obtain(handler);
  	  		bundle = msg.getData();
  	  		bundle.putSerializable(NowPlayingPollerThread.BUNDLE_CURRENTLY_PLAYING, currPlaying);
  	  		msg.what = NowPlayingPollerThread.MESSAGE_ARTIST_TEXT_VIEW;	
  	  		handler.sendMessage(msg);
  	  		
  			msg = Message.obtain(handler);
  	  		handler.sendEmptyMessage(NowPlayingPollerThread.MESSAGE_COVER_IMAGE);
		}
		mSubscribers.add(handler);
	}
	
	public synchronized void unSubscribe(Handler handler){
		mSubscribers.remove(handler);
	}
	
	public synchronized Drawable getNowPlayingCover(){
		return mCover;
	}
	
	public void run() {
		Message msg = null;
		Bundle bundle = null;
		Handler handler = null;
		while (!isInterrupted()) {
			if(mSubscribers.size() > 0){
				if (!mControl.isConnected()) {
					for(Iterator<Handler> it = mSubscribers.iterator();it.hasNext();){
						handler = it.next();
						msg = Message.obtain(handler);
						msg.what = NowPlayingPollerThread.MESSAGE_CONNECTION_ERROR;
						bundle = msg.getData();
						bundle.putSerializable(NowPlayingPollerThread.BUNDLE_CURRENTLY_PLAYING, null);
						handler.sendMessage(msg);
					}				
				} else {
					
					final ICurrentlyPlaying currPlaying = mControl.getCurrentlyPlaying();
					for(Iterator<Handler> it = mSubscribers.iterator();it.hasNext();){
						handler = it.next();
						msg = Message.obtain(handler);
						msg.what = NowPlayingPollerThread.MESSAGE_NOW_PLAYING_PROGRESS;
						bundle = msg.getData();
						bundle.putSerializable(NowPlayingPollerThread.BUNDLE_CURRENTLY_PLAYING, currPlaying);
						handler.sendMessage(msg);
					}

					String currentPos = currPlaying.getTitle() + currPlaying.getDuration();
					
					if (!mLastPos.equals(currentPos)) {
						mLastPos = currentPos;
			  	  		for(Iterator<Handler> it = mSubscribers.iterator();it.hasNext();){
			  	  			handler = it.next();
			  	  			msg = Message.obtain(handler);
			  	  			bundle = msg.getData();
			  	  			bundle.putSerializable(NowPlayingPollerThread.BUNDLE_CURRENTLY_PLAYING, currPlaying);
			  	  			msg.what = NowPlayingPollerThread.MESSAGE_ARTIST_TEXT_VIEW;	
			  	  			handler.sendMessage(msg);
			  	  		}
			  	  		
			  	  		try {				
			  	  			String downloadURI = mInfo.getCurrentlyPlayingThumbURI();
			  	  			Log.i("POLLER", "downloadURI: " + downloadURI);
			  	  			if (downloadURI != null && downloadURI.length() > 0) {
			  	  				if (!downloadURI.equals(mCoverPath)) {
			  	  					mCoverPath = downloadURI;
			  	  					
			  	  					byte[] buffer = download(downloadURI);
			  	  					
			  	  					if (buffer == null || buffer.length == 0)
			  	  						mCover = null;
			  	  					else 
			  	  						mCover = new BitmapDrawable(BitmapFactory.decodeByteArray(buffer, 0, buffer.length));

		  				  	  		for(Iterator<Handler> it = mSubscribers.iterator();it.hasNext();){
		  				  	  			handler = it.next();
		  				  	  			msg = Message.obtain(handler);
		  				  	  			handler.sendEmptyMessage(NowPlayingPollerThread.MESSAGE_COVER_IMAGE);
		  				  	  		}	
			  	  				}
			  	  			} else {
			  	  				mCover = null;
			  	  				if (mCoverPath != null){
		  				  	  		for(Iterator<Handler> it = mSubscribers.iterator();it.hasNext();){
		  				  	  			handler = it.next();
		  				  	  			msg = Message.obtain(handler);
		  				  	  			handler.sendEmptyMessage(NowPlayingPollerThread.MESSAGE_COVER_IMAGE);
		  				  	  		}			  	  					
			  	  				}
			  	  				mCoverPath = null;
			  	  			}
			  	  		} catch (MalformedURLException e) {
			  	  			// TODO Auto-generated catch block
			  	  			e.printStackTrace();
			  	  		} catch (URISyntaxException e) {
			  	  			// TODO Auto-generated catch block
						e.printStackTrace();
			  	  		}
					}
				}
			}
			try {
				sleep(1000);
			} catch (InterruptedException e) {
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

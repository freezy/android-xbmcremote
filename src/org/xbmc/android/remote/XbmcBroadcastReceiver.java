package org.xbmc.android.remote;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.xbmc.android.util.ConnectionManager;
import org.xbmc.android.util.SmsMmsMessage;
import org.xbmc.android.util.SmsPopupUtils;
import org.xbmc.eventclient.ButtonCodes;
import org.xbmc.eventclient.EventClient;
import org.xbmc.eventclient.Packet;
import org.xbmc.httpapi.HttpClient;
import org.xbmc.httpapi.client.ControlClient.ICurrentlyPlaying;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;

public class XbmcBroadcastReceiver extends BroadcastReceiver {

	
	/** 
	 * The Action fired by the Android-System when a SMS was received.
	 */
	private static final String SMS_RECVEICED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
	
	private static final int PLAY_STATE_NONE = -1;
	private static final int PLAY_STATE_PAUSED = 0;
	
	private static int PLAY_STATE = PLAY_STATE_NONE;
	public XbmcBroadcastReceiver() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		EventClient client = getEventClient();
		HttpClient http = ConnectionManager.getHttpClient();
		SharedPreferences prefs = context.getSharedPreferences("XBMCRemotePrefsFile", Context.MODE_WORLD_WRITEABLE);
		// currently no new connection to the event server is opened
		if(client != null) {
			try {
				if(action.equals(android.telephony.TelephonyManager.ACTION_PHONE_STATE_CHANGED)
						&& prefs.getBoolean("settings_show_call", true)) {
					String extra = intent.getStringExtra(android.telephony.TelephonyManager.EXTRA_STATE);
					if(extra.equals(android.telephony.TelephonyManager.EXTRA_STATE_RINGING)) {
						// someone is calling, we get all infos and pause the playback
	                    String number = intent.getStringExtra(android.telephony.TelephonyManager.EXTRA_INCOMING_NUMBER);
	                    String id = SmsPopupUtils.getPersonIdFromPhoneNumber(context, number);
	                    String callername = SmsPopupUtils.getPersonName(context, id, number);
	                    // Bitmap isn't supported by the event server, so we have to compress it
	                    Bitmap pic;
	                    if(id != null)
	                    	pic = Contacts.People.loadContactPhoto(context, 
	                    		Uri.withAppendedPath(Contacts.People.CONTENT_URI, id), R.drawable.icon, null);
	                    else
	                    	pic = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
	                    ByteArrayOutputStream os = new ByteArrayOutputStream();
	                    pic.compress(Bitmap.CompressFormat.PNG, 0, os);
	                    
	                    // if xbmc is playing something, we pause it. without the check paused playback would resume
	                    final ICurrentlyPlaying cp = http.control.getCurrentlyPlaying();
	                    if (http != null && http.isConnected() && http.info != null && cp != null ){
	                    	if (cp.isPlaying()){
	                    		client.sendButton("R1", ButtonCodes.REMOTE_PAUSE, true, true, true, (short)0, (byte)0);
	                    		client.sendButton("R1", ButtonCodes.REMOTE_PAUSE, false, false, true, (short)0, (byte)0);
	                    		PLAY_STATE = PLAY_STATE_PAUSED;
	                    	}
	                    }
	                    
	                    client.sendNotification(callername,"calling", Packet.ICON_PNG, os.toByteArray());
	               }
	               if(extra.equals(android.telephony.TelephonyManager.EXTRA_STATE_IDLE))
	               {
	            	   // phone state changed to idle, so if we paused the playback before, we resume it now
	            	   if(PLAY_STATE == PLAY_STATE_PAUSED) {
		            	   client.sendButton("R1", ButtonCodes.REMOTE_PLAY, true, true, true, (short)0, (byte)0);
		            	   client.sendButton("R1", ButtonCodes.REMOTE_PLAY, false, false, true, (short)0, (byte)0);
	            	   }
	            	   PLAY_STATE = PLAY_STATE_NONE;
	               }
				}else if(action.equals(SMS_RECVEICED_ACTION) && prefs.getBoolean("setting_show_sms", true)) {
					// sms received. extract msg, contact and pic and show it on the tv
					Bundle bundle = intent.getExtras();
		            if (bundle != null) {
		            	SmsMmsMessage msg = SmsMmsMessage.getSmsfromPDUs(context, (Object[])bundle.get("pdus"));
		            	Bitmap pic = msg.getContactPhoto();
		            	if(pic != null) {
		            		ByteArrayOutputStream os = new ByteArrayOutputStream();
		            		pic.compress(Bitmap.CompressFormat.PNG, 0, os);
		            		client.sendNotification("SMS Received from "+msg.getContactName(), msg.getMessageBody(),
		            				Packet.ICON_PNG, os.toByteArray());
		            	} else {
		            		client.sendNotification("SMS Received from "+msg.getContactName(), msg.getMessageBody());
		            	}
		            }
				}
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @return the active <code>EventClient</client> or creates a new one
	 */
	public EventClient getEventClient() {
		EventClient client = EventClient.getInstance();
		
		return client;
	}

}

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

package org.xbmc.android.remote.business.receiver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.android.util.SmsMmsMessage;
import org.xbmc.android.util.SmsPopupUtils;

import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IControlClient.ICurrentlyPlaying;

import org.xbmc.eventclient.ButtonCodes;
import org.xbmc.eventclient.EventClient;
import org.xbmc.eventclient.Packet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Contacts;

public class AndroidBroadcastReceiver extends BroadcastReceiver {

	/**
	 * The Action fired by the Android-System when a SMS was received.
	 */
	private static final String SMS_RECVEICED_ACTION = "android.provider.Telephony.SMS_RECEIVED";

	private static final int PLAY_STATE_NONE = -1;
	private static final int PLAY_STATE_PAUSED = 0;

	private static int sPlayState = PLAY_STATE_NONE;

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		final EventClient client = ConnectionManager.getEventClient(context);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		// currently no new connection to the event server is opened
		if (client != null) {
			try {
				if (action.equals(android.telephony.TelephonyManager.ACTION_PHONE_STATE_CHANGED) && prefs.getBoolean("setting_show_call", false)) {

					String extra = intent.getStringExtra(android.telephony.TelephonyManager.EXTRA_STATE);
					if (extra.equals(android.telephony.TelephonyManager.EXTRA_STATE_RINGING)) {
						// someone is calling, we get all infos and pause the
						// playback
						String number = null;
						String id = null;
						String callername = null;
						number = intent.getStringExtra(android.telephony.TelephonyManager.EXTRA_INCOMING_NUMBER);
						if (number != null) {
							id = SmsPopupUtils.getPersonIdFromPhoneNumber(context, number);
							callername = SmsPopupUtils.getPersonName(context, id, number);
						} else
							callername = "Unknown Number";
						// Bitmap isn't supported by the event server, so we
						// have to compress it
						Bitmap pic;
						if (id != null)
							pic = Contacts.People.loadContactPhoto(context, Uri.withAppendedPath(Contacts.People.CONTENT_URI, id), R.drawable.icon, null);
						else
							pic = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						pic.compress(Bitmap.CompressFormat.PNG, 0, os);

						// if xbmc is playing something, we pause it. without
						// the check paused playback would resume
						final IControlManager cm = ManagerFactory.getControlManager(context, null);
						cm.getCurrentlyPlaying(new DataResponse<ICurrentlyPlaying>() {
							public void run() {
								if (value != null && value.isPlaying()) {
									try {
										client.sendButton("R1", ButtonCodes.REMOTE_PAUSE, false, true, true, (short) 0, (byte) 0);
									} catch (IOException e) {
										((INotifiableManager)cm).onError(e);
									}
									sPlayState = PLAY_STATE_PAUSED;
								}
							}
						});
						client.sendNotification(callername, "calling", Packet.ICON_PNG, os.toByteArray());
						
					} else if (extra.equals(android.telephony.TelephonyManager.EXTRA_STATE_IDLE)) {
						
						// phone state changed to idle, so if we paused the
						// playback before, we resume it now
						if (sPlayState == PLAY_STATE_PAUSED) {
							client.sendButton("R1", ButtonCodes.REMOTE_PLAY, true, true, true, (short) 0, (byte) 0);
							client.sendButton("R1", ButtonCodes.REMOTE_PLAY, false, false, true, (short) 0, (byte) 0);
						}
						sPlayState = PLAY_STATE_NONE;
					}
				} else if (action.equals(SMS_RECVEICED_ACTION) && prefs.getBoolean("setting_show_sms", false)) {
					if (client != null) {
						// sms received. extract msg, contact and pic and show
						// it on the tv
						Bundle bundle = intent.getExtras();
						if (bundle != null) {
							SmsMmsMessage msg = SmsMmsMessage.getSmsfromPDUs(context, (Object[]) bundle.get("pdus"));
							Bitmap pic = msg.getContactPhoto();
							if (pic != null) {
								ByteArrayOutputStream os = new ByteArrayOutputStream();
								pic.compress(Bitmap.CompressFormat.PNG, 0, os);
								client.sendNotification("SMS Received from " + msg.getContactName(), msg.getMessageBody(), Packet.ICON_PNG, os.toByteArray());
							} else
								client.sendNotification("SMS Received from " + msg.getContactName(), msg.getMessageBody());
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

}

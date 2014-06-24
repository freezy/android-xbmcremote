/*
 *      Copyright (C) 2005-2011 Team XBMC
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import org.xbmc.android.remote.business.Command;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.controller.RemoteController;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.eventclient.ButtonCodes;

/**
 * @author John Pulford
 */
public class RemoteControlReceiver extends BroadcastReceiver implements INotifiableController {

    @Override
    public void onReceive(Context context, Intent intent) {
        IEventClientManager mEventClientManager = ManagerFactory.getEventClientManager(this);

        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            String keyPressAction = "";
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == event.getKeyCode()) {
                // Handle key press.
                keyPressAction = ButtonCodes.REMOTE_PLAY;
            }
            if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == event.getKeyCode()) {
                // Handle key press.
                keyPressAction = ButtonCodes.REMOTE_REVERSE;
            }
            if (KeyEvent.KEYCODE_MEDIA_NEXT == event.getKeyCode()) {
                // Handle key press.
                keyPressAction = ButtonCodes.REMOTE_FORWARD;
            }
            if (keyPressAction.length() > 0) {
                mEventClientManager.sendButton("R1", keyPressAction, false, true, true, (short)0, (byte)0);
            }
        }
    }


    // Not convinced these methods need to do anything in this instance
    // but someone with a longer history with the project may have a better idea.
    @Override
    public void onWrongConnectionState(int state, INotifiableManager manager, Command<?> source) {

    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onMessage(String message) {

    }

    @Override
    public void runOnUI(Runnable action) {

    }
}
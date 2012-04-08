/*
 *      WTFPL - http://sam.zoy.org/wtfpl/
 *      Sylvain Galand - http://slvn.fr
 */

package org.xbmc.android.remote.business.service;

import org.xbmc.android.remote.business.Command;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.presentation.INotifiableController;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

public class RemoteCommandService extends Service implements INotifiableController {
	
	public static final String BUTTON_COMMAND = "button_command";
	
	private IEventClientManager mEventClientManager;
	private Handler mHandler;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Get EventClickManager
		mEventClientManager = ManagerFactory.getEventClientManager(this);
		
		// Define new handler to handle toast message in UI thread
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
			}
		};
	}
	
	@Override
	public void onDestroy() {
		
		// Remove this as controller
		if (mEventClientManager != null)
		{
			mEventClientManager.setController(null);
		}
		
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	// This is the old onStart method that will be called on the pre-2.0
	// platform.  On 2.0 or later we override onStartCommand() so this
	// method will not be called.
	@Override
	public void onStart(Intent intent, int startId) {
	    handleCommand(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    handleCommand(intent);
	    // We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
	    return START_STICKY;
	}

	/**
	 * Handle the intent send by button from the AppWidget
	 * Retrieve the command and send the button event
	 * @param intent
	 */
	private void handleCommand(final Intent intent) {
		if (intent != null) {
			final String action =	intent.getStringExtra(BUTTON_COMMAND);
			new Thread() {
				@Override
				public void run() {
					super.run();
					if (!TextUtils.isEmpty(action)) {
						// Send a complete action on a button (down + up)
						mEventClientManager.sendButton("R1", action, false, true,  true, (short)0, (byte)0);
						mEventClientManager.sendButton("R1", action, false, false, true, (short)0, (byte)0);
					}
				}
			}.start();
		}
	}

	public void onWrongConnectionState(int state, INotifiableManager manager, Command<?> source) {
		// Should not happen
	}

	public void onError(Exception e) {
		// Should not happen
	}

	public void onMessage(final String message) {
		
		if (!TextUtils.isEmpty(message)) {
			
			// If message not empty send it to the handler
		   	Message msg = new Message();
		   	msg.obj = message;
		   	mHandler.sendMessage(msg);
		   	
		}
		
	}

	public void runOnUI(Runnable action) {
		// Should not happen
	}
}

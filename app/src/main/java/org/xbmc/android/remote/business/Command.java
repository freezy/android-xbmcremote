package org.xbmc.android.remote.business;

import android.util.Log;

import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.httpapi.WifiStateException;

/**
 * Class to asynchronous execute backend stuff and send the result back to the GUI.
 * Holds some extras to check how often this request failed and catch all to not have the backend crashing
 * and force closing the app.
 *
 * @author till
 */
public abstract class Command<T> implements Runnable {

	public static final int MAX_RETRY = 5;
	public final INotifiableManager mManager;
	public final DataResponse<T> mResponse;
	// TODO Disable this when not needed anymore
	public final StackTraceElement mCaller;
	public int mRetryCount = 0;
	public long mStarted = 0;

	public Command(DataResponse<T> response, INotifiableManager manager) {
		mManager = manager;
		mResponse = response;
		mStarted = System.currentTimeMillis();
		mCaller = new Throwable().fillInStackTrace().getStackTrace()[2];
	}

	public void run() {
		try {
			mRetryCount++;
			Log.d("Command", "Running command counter: " + mRetryCount);
			if (mRetryCount > MAX_RETRY) return;
			doRun();
			Log.i(mCaller.getClassName(), "*** " + mCaller.getMethodName() + ": " + (System.currentTimeMillis() -
					mStarted) + "ms");

			mManager.onFinish(mResponse);
		} catch (WifiStateException e) {
			mManager.onWrongConnectionState(e.getState(), this);
		} catch (Exception e) {
			mManager.onError(e);
		}
	}

	public abstract void doRun() throws Exception;

}

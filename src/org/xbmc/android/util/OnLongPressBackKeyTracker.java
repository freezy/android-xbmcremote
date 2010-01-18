package org.xbmc.android.util;

import org.xbmc.android.remote.presentation.activity.HomeActivity;
import org.xbmc.android.util.KeyTracker.OnKeyTracker;
import org.xbmc.android.util.KeyTracker.Stage;
import org.xbmc.android.util.KeyTracker.State;

import android.content.Intent;
import android.view.KeyEvent;

public abstract class OnLongPressBackKeyTracker implements OnKeyTracker {

	private Stage lastStage = Stage.SHORT_REPEAT;

	public State onKeyTracker(int keyCode, KeyEvent event, Stage stage,
			int duration) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (stage == KeyTracker.Stage.LONG_REPEAT) {
            	// here we have the long pressed back button
            	lastStage = stage;
                return KeyTracker.State.KEEP_TRACKING;
            }else if (stage == KeyTracker.Stage.UP) {
            	if(lastStage == Stage.LONG_REPEAT) {
            		lastStage = Stage.SHORT_REPEAT;
            		onLongPressBack(keyCode, event, stage, duration);
            		return KeyTracker.State.DONE_TRACKING;
            	}
            	onShortPressBack(keyCode, event, stage, duration);
            	return KeyTracker.State.NOT_TRACKING;
            }
            return KeyTracker.State.KEEP_TRACKING;
        }
        return KeyTracker.State.NOT_TRACKING;
	}
	
	public abstract void onLongPressBack(int keyCode, KeyEvent event, Stage stage, int duration);
	
	public abstract void onShortPressBack(int keyCode, KeyEvent event, Stage stage, int duration);

}

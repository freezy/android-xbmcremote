package org.xbmc.android.remote.presentation.activity;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.xbmc.android.remote.R;

import android.app.Application;

@ReportsCrashes(formKey = "", // will not be used
mailTo = "tim.thomas.austin@gmail.com", mode = ReportingInteractionMode.TOAST, resToastText = R.string.crash_toast_text)
public class RemoteApplication extends Application {
	@Override
	public void onCreate() {
		ACRA.init(this);
		super.onCreate();
	}
}

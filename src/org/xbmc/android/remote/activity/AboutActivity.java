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

package org.xbmc.android.remote.activity;

import org.xbmc.android.remote.R;
import org.xbmc.android.util.ErrorHandler;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutActivity extends Activity implements OnSharedPreferenceChangeListener{
	
    private boolean mDisableKeyguard = false;
    private KeyguardManager.KeyguardLock mKeyguardLock = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		ErrorHandler.setActivity(this);
		try {
			final String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			final int versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
			((TextView)findViewById(R.id.about_version)).setText("v" + versionName);
			((TextView)findViewById(R.id.about_revision)).setText("Revision " + versionCode);
			TextView message = (TextView)findViewById(R.id.about_url_message);
			
			message.setText(Html.fromHtml("Visit our project page at <a href=\"http://code.google.com/p/android-xbmcremote\">Google Code</a>."));
			message.setMovementMethod(LinkMovementMethod.getInstance());
		} catch (NameNotFoundException e) {
			((TextView)findViewById(R.id.about_version)).setText("Error reading version");
		}
		
	      final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	      String disableKeyguardString = prefs.getString("setting_disable_keyguard", "0");
	      mDisableKeyguard = ( disableKeyguardString.equals("2") );
	      prefs.registerOnSharedPreferenceChangeListener(this);
	}
	
	   @Override
	   protected void onResume(){
	   	super.onResume();
	   	if(mDisableKeyguard) {
	   		KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);
	       mKeyguardLock = keyguardManager.newKeyguardLock("RemoteActivityKeyguardLock");
	       mKeyguardLock.disableKeyguard();
	   	}
	  }
	    
		@Override
		protected void onPause() {
			super.onPause();
			if (mKeyguardLock != null){
				mKeyguardLock.reenableKeyguard();
				mKeyguardLock = null;
			}
		}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals("setting_disable_keyguard")) {
			String disableKeyguardString = sharedPreferences.getString(key, "0");
			boolean disableKeyguardState = ( disableKeyguardString.equals("2") );
			if (disableKeyguardState != mDisableKeyguard){
				if (disableKeyguardState) {
					if(this.hasWindowFocus()  ) {
		    			KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);
						mKeyguardLock = keyguardManager.newKeyguardLock("RemoteActivityKeyguardLock");
						mKeyguardLock.disableKeyguard();
					}
				}
				else {
					if(this.hasWindowFocus()) {
						if (mKeyguardLock != null) {
							mKeyguardLock.reenableKeyguard();
						}
						mKeyguardLock = null;
					}
				}
				mDisableKeyguard = disableKeyguardState;
			}
		}
	}
}
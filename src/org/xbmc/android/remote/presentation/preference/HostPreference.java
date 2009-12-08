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

package org.xbmc.android.remote.presentation.preference;

import org.xbmc.android.remote.R;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * One of those contains name, host, port, user and pass of an XBMC instance.
 * 
 * @author Team XBMC
 */
public class HostPreference extends DialogPreference {
	
	private EditText mNameView;
	private EditText mHostView;
	private EditText mPortView;
	private EditText mUserView;
	private EditText mPassView;
	
	private String mName;
	private String mHost;
	private int mPort;
	private String mUser;
	private String mPass;

	public HostPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.preference_host);
	}
	
	public void setName(String name) {
		final boolean wasBlocking = shouldDisableDependents();
		mName = name;
		
		persistString(name);
		
		final boolean isBlocking = shouldDisableDependents();
		if (isBlocking != wasBlocking) {
			notifyDependencyChange(isBlocking);
		}
	}
	
	public void setHost(String host, int port) {
		final boolean wasBlocking = shouldDisableDependents();
		mHost = host;
		mPort = port;
		
		persistString(host);
		persistInt(port);
		
		final boolean isBlocking = shouldDisableDependents();
		if (isBlocking != wasBlocking) {
			notifyDependencyChange(isBlocking);
		}
	}
	
	public void setAuth(String user, String pass) {
		final boolean wasBlocking = shouldDisableDependents();
		mUser = user;
		mPass = pass;
		
		persistString(user);
		persistString(pass);
		
		final boolean isBlocking = shouldDisableDependents();
		if (isBlocking != wasBlocking) {
			notifyDependencyChange(isBlocking);
		}
	}
	
	public String getName() {
		return mName;
	}
	public String getHost() {
		return mHost;
	}
	public int getPort() {
		return mPort;
	}
	public String getUser() {
		return mUser;
	}
	public String getPass() {
		return mPass;
	}
	
	@Override
	protected View onCreateDialogView() {
		final ViewGroup parent = (ViewGroup)super.onCreateDialogView();
		mNameView = (EditText)parent.findViewById(R.id.pref_name);
		mHostView = (EditText)parent.findViewById(R.id.pref_host);
		mPortView = (EditText)parent.findViewById(R.id.pref_port);
		mUserView = (EditText)parent.findViewById(R.id.pref_user);
		mPassView = (EditText)parent.findViewById(R.id.pref_pass);
		return parent;
	}
	
	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		mNameView.setText(mName);
		mHostView.setText(mHost);
		mPortView.setText(String.valueOf(mPort));
		mUserView.setText(mUser);
		mPassView.setText(mPass);
	}
	
	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		if (isPersistent()) {
			// No need to save instance state since it's persistent
			return superState;
		}

		final SavedState myState = new SavedState(superState);
		myState.name = mName;
		myState.host = mHost;
		myState.port = mPort;
		myState.user = mUser;
		myState.pass = mPass;
		return myState;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
//			String value = mEditText.getText().toString();
//			if (callChangeListener(value)) {
//				setText(value);
//			}
		}
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state == null || !state.getClass().equals(SavedState.class)) {
			// Didn't save state for us in onSaveInstanceState
			super.onRestoreInstanceState(state);
			return;
		}
		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());
		setName(myState.name);
		setHost(myState.host, myState.port);
		setAuth(myState.user, myState.pass);
	}
	
	private static class SavedState extends BaseSavedState {
		
		public final static String BUNDLE_NAME = "name";
		public final static String BUNDLE_HOST = "host";
		public final static String BUNDLE_PORT = "port";
		public final static String BUNDLE_USER = "user";
		public final static String BUNDLE_PASS = "pass";
		
		String name, host, user, pass;
		int port;

		public SavedState(Parcel source) {
			super(source);
			final Bundle bundle = source.readBundle();
			name = bundle.getString(BUNDLE_NAME);
			host = bundle.getString(BUNDLE_HOST);
			user = bundle.getString(BUNDLE_USER);
			pass = bundle.getString(BUNDLE_PASS);
			port = bundle.getInt(BUNDLE_PORT);
		}
		
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			final Bundle bundle = dest.readBundle();
			bundle.putString(BUNDLE_NAME, name);
			bundle.putString(BUNDLE_HOST, host);
			bundle.putString(BUNDLE_USER, user);
			bundle.putString(BUNDLE_PASS, pass);
			bundle.putInt(BUNDLE_PORT, port);
			dest.writeBundle(bundle);
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}
	}
}
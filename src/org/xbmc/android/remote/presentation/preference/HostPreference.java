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
	
	private Host mHost;

	public HostPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.preference_host);
	}
	
	public void setHost(Host host) {
		final boolean wasBlocking = shouldDisableDependents();
		mHost = host;
		
//		persistString(name);
		
		final boolean isBlocking = shouldDisableDependents();
		if (isBlocking != wasBlocking) {
			notifyDependencyChange(isBlocking);
		}
	}
	
	public Host getHost() {
		return mHost;
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
		final Host host = mHost;
		if (host != null) {
			mNameView.setText(host.name);
			mHostView.setText(host.host);
			mPortView.setText(String.valueOf(host.port));
			mUserView.setText(host.user);
			mPassView.setText(host.pass);
		}
	}
	
	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		if (isPersistent()) {
			// No need to save instance state since it's persistent
			return superState;
		}

		final SavedState myState = new SavedState(superState);
		myState.host = mHost;
		return myState;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			Host newHost = new Host();
			newHost.name = mNameView.getText().toString();
			newHost.host = mHostView.getText().toString();
			try {
				newHost.port = Integer.parseInt(mPortView.getText().toString());
			} catch (NumberFormatException e) {
				newHost.port = 0;
			}
			newHost.user = mUserView.getText().toString();
			newHost.pass = mPassView.getText().toString();
			
			if (callChangeListener(newHost)) {
				setHost(newHost);
			}
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
		setHost(myState.host);
	}
	
	private static class SavedState extends BaseSavedState {
		
		Host host;

		public SavedState(Parcel source) {
			super(source);
			host = (Host)source.readSerializable();
		}
		
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeSerializable(host);
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}
	}
}
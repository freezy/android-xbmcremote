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

package org.xbmc.android.remote.presentation.controller;

import java.util.ArrayList;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.widget.FiveLabelsItemView;
import org.xbmc.android.util.ImportUtilities;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.IProfileManager;
import org.xbmc.api.object.Profile;
import org.xbmc.api.type.ThumbSize;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Toast;

public class ProfileListController extends ListController implements IController {

	private static final int mThumbSize = ThumbSize.SMALL;
	
	public static final int DIALOG_PASSWORD = 500;
	
	private IProfileManager mProfileManager;
	private IControlManager mControlManager;
	private String mCurrentProfile; // The current active profile name
	private String mClickedProfile; // The clicked profile name (Needed in the password dialog)
	
	private boolean mLoadCovers = false;
	// Loading a new profile might take some time, let's try to reconnect more than once.
	public static final int MAX_NUMBER_OF_TRIES = 5;
	private byte mNumberOfTries = 0; // 

	public void onCreate(Activity activity, Handler handler, AbsListView list) {
		
		mProfileManager = ManagerFactory.getProfileManager(this);
		mControlManager = ManagerFactory.getControlManager(this);
		
		final String sdError = ImportUtilities.assertSdCard();
		mLoadCovers = sdError == null;
		
		if (!isCreated()) {
			super.onCreate(activity, handler, list);

			if (!mLoadCovers) {
				Toast toast = Toast.makeText(activity, sdError + " Displaying place holders only.", Toast.LENGTH_LONG);
				toast.show();
			}
			
			activity.registerForContextMenu(mList);
			
			mFallbackBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.default_profile);
			setupIdleListener();
			
			mList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if(isLoading()) return;
					final Profile profile = (Profile)mList.getAdapter().getItem(((FiveLabelsItemView)view).position);
					mClickedProfile = profile.title;
					if (profile.lockmode == 0) { // No password required
						mProfileManager.loadProfile(new DataResponse<Boolean>(), profile.title, null, mActivity.getApplicationContext());
						refreshList();
					} else {
						showDialog(DIALOG_PASSWORD);
					}
				}
			});
			mList.setOnKeyListener(new ListControllerOnKeyListener<Profile>());
			fetch();
		}
	}
	
	private void fetch() {
		final String title = "Profiles";
		DataResponse<String> response1 = new DataResponse<String>() { // Holds the current active profile
			public void run() {
				mCurrentProfile = value;
			}
		};
		DataResponse<ArrayList<Profile>> response2 = new DataResponse<ArrayList<Profile>>() { // Holds the list of all the profiles
			public void run() {
				setTitle(title + " (" + value.size() + ")");
				((AdapterView<ListAdapter>) mList).setAdapter(new ProfileAdapter(mActivity, value));
			}
		};
		
		showOnLoading();
		setTitle(title + "...");
		mProfileManager.GetCurrentProfile(response1, mActivity.getApplicationContext());
		mProfileManager.getProfiles(response2, mActivity.getApplicationContext());
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) { } // No context menu
	
	@Override
	public void onContextItemSelected(MenuItem item) { } // No context menu
	
	@Override
	protected void refreshList() {
		mNumberOfTries = 0;
		hideMessage();
		fetch();
	}
	
	@Override
	public void onError(Exception exception) {
		if (mActivity == null) {
			return;
		}
		try {
			throw exception;
		} catch (Exception e) {
			mNumberOfTries++;
			if (mNumberOfTries < MAX_NUMBER_OF_TRIES) {
				fetch(); // Let's try to fetch it more than once
			} else {
				super.onError(exception);
			}
		}
	}
	
	private class ProfileAdapter extends ArrayAdapter<Profile> {
		ProfileAdapter(Activity activity, ArrayList<Profile> items) {
			super(activity, 0, items);
		}
		public View getView(int position, View convertView, ViewGroup parent) {

			final FiveLabelsItemView view;
			if (convertView == null) {
				view = new FiveLabelsItemView(mActivity, mProfileManager, parent.getWidth(), mFallbackBitmap, mList.getSelector(), false);
			} else {
				view = (FiveLabelsItemView)convertView;
			}
			
			final Profile profile = getItem(position);
			view.reset();
			view.position = position;
			view.title = profile.title;
			view.subtitle = profile.lockmode > 0 ? "Protected" : "Public";
			view.subtitleRight = profile.title.equals(mCurrentProfile) ? "Active" : "";

			if (mLoadCovers) {
				if(mProfileManager.coverLoaded(profile, mThumbSize)){
					view.setCover(mProfileManager.getCoverSync(profile, mThumbSize));
				}else{
					view.setCover(null);
					view.getResponse().load(profile, !mPostScrollLoader.isListIdle());
				}
			}
			return view;
		}
	}
	
	private static final long serialVersionUID = -8661723658167343976L;

	public void onActivityPause() {
		if (mProfileManager != null) {
			mProfileManager.setController(null);
			mProfileManager.postActivity();
		}
		if (mControlManager != null) {
			mControlManager.setController(null);
		}
		super.onActivityPause();
	}

	public void onActivityResume(Activity activity) {
		super.onActivityResume(activity);
		if (mProfileManager != null) {
			mProfileManager.setController(this);
		}
		if (mControlManager != null) {
			mControlManager.setController(this);
		}
	}

	// Need Context passed in because this can be called at times when mActivity is null.
	public Dialog onCreateDialog(int id, final Context context) {
		Dialog dialog;
		switch(id) {
		case DIALOG_PASSWORD:
			dialog = new Dialog(context);
			dialog.setContentView(R.layout.profile_password);
			Button sendbutton = (Button) dialog.findViewById(R.id.password_button_send);
			sendbutton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					EditText text = (EditText) v.getRootView().findViewById(R.id.password_text);
					mProfileManager.loadProfile(new DataResponse<Boolean>(), mClickedProfile, text.getText().toString(), mActivity.getApplicationContext());
					dismissDialog(DIALOG_PASSWORD);
					refreshList();
				}
			});
			Button cancelbutton = (Button) dialog.findViewById(R.id.password_button_cancel);
			cancelbutton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dismissDialog(DIALOG_PASSWORD);
				}
			});
			break;
		default:
			dialog = null;
		}
		return dialog;
	}
	
	public void onPrepareDialog (int id, Dialog dialog) {
		dialog.setTitle(mClickedProfile);
		EditText text = (EditText) dialog.findViewById(R.id.password_text);
		text.setText("");
	}

}

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
import java.util.Hashtable;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.presentation.activity.HostSettingsActivity;
import org.xbmc.android.remote.presentation.activity.SettingsActivity;
import org.xbmc.android.util.HostFactory;
import org.xbmc.api.object.Host;
import org.xbmc.api.presentation.INotifiableController;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class SettingsController extends AbstractController implements INotifiableController, IController, OnSharedPreferenceChangeListener {
	private static final String TAG = "SettingsController";
	
	public static final int MENU_ADD_HOST = 1;
	public static final int MENU_ADD_HOST_WIZARD = 3;
	public static final int MENU_ADD_FROM_BARCODE = 4;
	public static final int MENU_GENERATE_BARCODE = 5;
	
	public static final int REQUEST_SCAN_BARCODE = 1;
	
	private PreferenceActivity mPreferenceActivity;		
	private final Hashtable<String, String> mSummaries = new Hashtable<String, String>();
	
	public SettingsController(PreferenceActivity activity, Handler handler) {
		mPreferenceActivity = activity;
		super.onCreate(activity, handler);
	}
	
	/**
	 * Used in SettingsActivity in order to update the %value% placeholder in 
	 * the summaries.
	 * @param activity Reference to activity
	 */
	public void registerOnSharedPreferenceChangeListener(PreferenceActivity activity) {
		activity.getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		PreferenceScreen ps = activity.getPreferenceScreen();
		// save original summaries to variable for later update
		mSummaries.clear();
		for (String key : ps.getSharedPreferences().getAll().keySet()) {
			Preference pref = ps.findPreference(key);
			if (pref != null && pref.getSummary() != null) {
				mSummaries.put(key, pref.getSummary().toString());
			}
		}
		updateSummaries();
	}
	
	/**
	 * Creates the preference screen that contains all the listed hosts.
	 * @param root Root node
	 * @param activity Reference to activity
	 * @return 
	 */
	public PreferenceScreen createHostsPreferences(PreferenceScreen root, Activity activity) {
		final ArrayList<Host> hosts = HostFactory.getHosts(activity.getApplicationContext());
		if (hosts.size() > 0) {
			for (Host host : hosts) {
				HostPreference pref = new HostPreference(activity);
				pref.setTitle(host.name);
				pref.setSummary(host.getSummary());
				pref.setHost(host);
				pref.setKey(HostPreference.ID_PREFIX + host.id);
				root.addPreference(pref);
			}
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setMessage("No hosts defined. In order to add hosts, press \"Menu\" and choose \"Add host\".");
			builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			builder.create().show();
		}
		return root;
	}
	
	/**
	 * Updates summaries of all known keys with the updated value.
	 */
	public void updateSummaries() {
		PreferenceScreen ps = mPreferenceActivity.getPreferenceScreen();
		for (String key : ps.getSharedPreferences().getAll().keySet()) {
			Preference pref = ps.findPreference(key);
			if (pref != null && pref.getSummary() != null) {
				String summary = pref.getSummary().toString();
				if (summary.contains(SettingsActivity.SUMMARY_VALUE_PLACEHOLDER)) {
					pref.setSummary(summary.replaceAll(SettingsActivity.SUMMARY_VALUE_PLACEHOLDER, ps.getSharedPreferences().getString(key, "<not set>")));
				}
			}
		}
	}
	
	/**
	 * Used in order to replace the %value% placeholders with real values.
	 */
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.i("SettingsActivity", "onSharedPreferenceChanged(" + key + ")");
		Preference pref = mPreferenceActivity.getPreferenceScreen().findPreference(key);
		String origSummary = mSummaries.get(key);
		if (origSummary != null && origSummary.contains(SettingsActivity.SUMMARY_VALUE_PLACEHOLDER)) {
			pref.setSummary(origSummary.replaceAll(SettingsActivity.SUMMARY_VALUE_PLACEHOLDER, sharedPreferences.getString(key, "")));
		}
	}

	public void onCreateOptionsMenu(Menu menu) {
		menu.addSubMenu(0, MENU_ADD_HOST, 0, "Add Host").setIcon(R.drawable.menu_add_host);
		menu.addSubMenu(0, MENU_ADD_HOST_WIZARD, 0, "Host Wizard").setIcon(R.drawable.menu_add_host);
		menu.addSubMenu(0, MENU_ADD_FROM_BARCODE, 0, mPreferenceActivity.getString(R.string.add_from_barcode)).setIcon(R.drawable.menu_qr_code);
		menu.addSubMenu(0, MENU_GENERATE_BARCODE, 0, mPreferenceActivity.getString(R.string.generate_barcode)).setIcon(R.drawable.menu_qr_code);
	}
	
	public void onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ADD_HOST:
				HostPreference pref = new HostPreference(mActivity);
				pref.setTitle("New XBMC Host");
				pref.create(mPreferenceActivity.getPreferenceManager());
				mPreferenceActivity.getPreferenceScreen().addPreference(pref);
				break;
			case MENU_ADD_FROM_BARCODE:
				final Intent scanIntent = new Intent("com.google.zxing.client.android.SCAN");
				scanIntent.putExtra("SCAN_MODE", "QR_CODE_MODE");
				
				if (mPreferenceActivity.getPackageManager().queryIntentActivities(scanIntent, 0).size() == 0) {
					showBarcodeUnsupportedDialog();
					break;
				}
				
				mPreferenceActivity.startActivityForResult(scanIntent, REQUEST_SCAN_BARCODE);
				break;
			case MENU_GENERATE_BARCODE:
				final Intent intent = new Intent("com.google.zxing.client.android.ENCODE");
				intent.putExtra("ENCODE_TYPE", "TEXT_TYPE");
				
				if (mPreferenceActivity.getPackageManager().queryIntentActivities(intent, 0).size() == 0) {
					showBarcodeUnsupportedDialog();
					break;
				}
				
				final ArrayList<Host> hosts = HostFactory.getHosts(mPreferenceActivity);
				
				if (hosts.size() == 0) {
					AlertDialog.Builder builder = new AlertDialog.Builder(mPreferenceActivity);
					builder.setMessage(mPreferenceActivity.getString(R.string.hosts_required));
					builder.setPositiveButton(mPreferenceActivity.getString(R.string.ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					builder.create().show();
					break;
				} else if (hosts.size() == 1) {
					Host host = hosts.get(0);
					intent.putExtra("ENCODE_DATA", host.toJson());
					mPreferenceActivity.startActivity(intent);
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(mPreferenceActivity);
					builder.setTitle(mPreferenceActivity.getString(R.string.pick_host));
					String[] names = new String[hosts.size()];
					for (int i = 0, size = hosts.size(); i < size; i++) {
						names[i] = hosts.get(i).name;
					}
					builder.setItems(names, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Host host = hosts.get(which);
							intent.putExtra("ENCODE_DATA", host.toJson());
							mPreferenceActivity.startActivity(intent);
						}
					});
					builder.setCancelable(true);
					builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
						public void onCancel(DialogInterface dialog) {
							dialog.dismiss();
						}
					});
					builder.create().show();
				}
				break;
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_SCAN_BARCODE) {
			if (resultCode == Activity.RESULT_OK) {
				String result = data.getStringExtra("SCAN_RESULT");
				Host host = HostFactory.getHostFromJson(result);
				if (host == null) {
					AlertDialog.Builder builder = new AlertDialog.Builder(mPreferenceActivity);
					builder.setMessage(mPreferenceActivity.getString(R.string.host_scan_error));
					builder.setPositiveButton(mPreferenceActivity.getString(R.string.ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					builder.create().show();
				} else {
					HostFactory.addHost(mPreferenceActivity, host);
					Toast.makeText(mPreferenceActivity, mPreferenceActivity.getString(R.string.added_host, host.name), Toast.LENGTH_SHORT).show();
					
					// Is there a better way to refresh the preferences screen?
					mPreferenceActivity.startActivity(new Intent(mPreferenceActivity.getBaseContext(), HostSettingsActivity.class));
					mPreferenceActivity.finish();
				}
			}
		}
	}
	
	private void showBarcodeUnsupportedDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mPreferenceActivity);
		builder.setMessage(mPreferenceActivity.getString(R.string.barcode_scanner_required));
		builder.setPositiveButton(mPreferenceActivity.getString(R.string.yes), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("market://search?q=pname:com.google.zxing.client.android"));
				mPreferenceActivity.startActivity(intent);
			}
		});
		builder.setNegativeButton(mPreferenceActivity.getString(R.string.no), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.create().show();
	}
	
	public void onActivityPause() {
		super.onActivityPause();
	}
	
	public void onActivityResume(Activity activity) {
		super.onActivityResume(activity);
		final ArrayList<Host> hosts = HostFactory.getHosts(activity.getApplicationContext()); 
		if (hosts.size() == 1) {
			final Host host = hosts.get(0);
			Log.i(TAG, "Setting host to " + (host == null ? "<null>" : host.addr) + ".");
			HostFactory.saveHost(activity.getApplicationContext(), host);
		}
		if (hosts.size() == 0) {
			Log.i(TAG, "Resetting host to <null>.");
			HostFactory.saveHost(activity.getApplicationContext(), null);
		}
	}	
}
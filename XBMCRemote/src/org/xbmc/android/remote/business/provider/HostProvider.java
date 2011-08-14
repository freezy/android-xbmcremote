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

package org.xbmc.android.remote.business.provider;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class HostProvider extends ContentProvider {

	private static final String TAG = "HostProvider";

	public static final String AUTHORITY = "org.xbmc.android.provider.remote";

	private static final int DATABASE_VERSION = 4;
	private static final String DATABASE_NAME = "xbmc_hosts.db";
	private static final String HOSTS_TABLE_NAME = "hosts";

	private static HashMap<String, String> sHostsProjectionMap;

	private static final int HOSTS = 1;
	private static final int HOST_ID = 2;

	private static final UriMatcher sUriMatcher;
	
	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + HOSTS_TABLE_NAME + " (" 
					+ Hosts._ID + " INTEGER PRIMARY KEY," 
					+ Hosts.NAME + " TEXT," 
					+ Hosts.ADDR + " TEXT,"
					+ Hosts.PORT + " INTEGER," 
					+ Hosts.USER + " TEXT," 
					+ Hosts.PASS + " TEXT," 
					+ Hosts.ESPORT + " INTEGER," 
					+ Hosts.TIMEOUT + " INTEGER," 
					+ Hosts.WIFI_ONLY + " INTEGER,"
					+ Hosts.ACCESS_POINT + " TEXT,"
					+ Hosts.MAC_ADDR + " TEXT,"
					+ Hosts.WOL_PORT + " INTEGER,"
					+ Hosts.WOL_WAIT + " INTEGER"
					+ ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			String altertable;
			switch (oldVersion) {
			case 2:
				Log.d(TAG, "Upgrading database from version 2 to 3");
				altertable = "ALTER TABLE " + HOSTS_TABLE_NAME + " ADD COLUMN " + Hosts.WIFI_ONLY 
				+ " INTEGER DEFAULT 0;";
				db.execSQL(altertable);
				Log.d(TAG, "executed: " + altertable);
				altertable = "ALTER TABLE " + HOSTS_TABLE_NAME + " ADD COLUMN " + Hosts.ACCESS_POINT 
				+ " TEXT;";
				db.execSQL(altertable);
				Log.d(TAG, "executed: " + altertable);
				altertable = "ALTER TABLE " + HOSTS_TABLE_NAME + " ADD COLUMN " + Hosts.MAC_ADDR 
				+ " TEXT;";
				db.execSQL(altertable);
				Log.d(TAG, "executed: " + altertable);
			case 3:
				Log.d(TAG, "Upgrading database from version 3 to 4");
				 altertable = "ALTER TABLE " + HOSTS_TABLE_NAME + " ADD COLUMN " + Hosts.WOL_PORT
				+ " INTEGER;";
				 db.execSQL(altertable);
				 Log.d(TAG, "executed: " + altertable);
				 altertable = "ALTER TABLE " + HOSTS_TABLE_NAME + " ADD COLUMN " + Hosts.WOL_WAIT
				 + " INTEGER;";
				 db.execSQL(altertable);
				 Log.d(TAG, "executed: " + altertable);
				 
				//WARNING!!! ADD A break; BEFORE THE DEFAULT BLOCK OF THE DATABASE WILL BE DROPPED!!! 
				 break;
			default: 
				Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
				db.execSQL("DROP TABLE IF EXISTS " + HOSTS_TABLE_NAME);
				onCreate(db);
					
			}
		}
	}

	private DatabaseHelper mOpenHelper;

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch (sUriMatcher.match(uri)) {
		case HOSTS:
			qb.setTables(HOSTS_TABLE_NAME);
			qb.setProjectionMap(sHostsProjectionMap);
			break;

		case HOST_ID:
			qb.setTables(HOSTS_TABLE_NAME);
			qb.setProjectionMap(sHostsProjectionMap);
			qb.appendWhere(Hosts._ID + "=" + uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = Hosts.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		// Get the database and run the query
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Log.d(TAG, "SQLite database version: " + db.getVersion());
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

		// Tell the cursor what uri to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case HOSTS:
			return Hosts.CONTENT_TYPE;

		case HOST_ID:
			return Hosts.CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		// Validate the requested uri
		if (sUriMatcher.match(uri) != HOSTS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		if (values.containsKey(Hosts.NAME) == false) {
			Resources r = Resources.getSystem();
			values.put(Hosts.NAME, r.getString(android.R.string.untitled));
		}

		if (values.containsKey(Hosts.ADDR) == false) {
			values.put(Hosts.ADDR, "");
		}
		if (values.containsKey(Hosts.PORT) == false) {
			values.put(Hosts.PORT, 0);
		}
		if (values.containsKey(Hosts.USER) == false) {
			values.put(Hosts.USER, "");
		}
		if (values.containsKey(Hosts.PASS) == false) {
			values.put(Hosts.PASS, "");
		}
		if (values.containsKey(Hosts.ESPORT) == false) {
			values.put(Hosts.ESPORT, 0);
		}
		if (values.containsKey(Hosts.TIMEOUT) == false) {
			values.put(Hosts.TIMEOUT, -1);
		}
		if (values.containsKey(Hosts.WIFI_ONLY) == false) {
			values.put(Hosts.WIFI_ONLY, 0);
		}
		if (values.containsKey(Hosts.ACCESS_POINT) == false) {
			values.put(Hosts.ACCESS_POINT, "");
		}
		if (values.containsKey(Hosts.MAC_ADDR) == false) {
			values.put(Hosts.MAC_ADDR, "");
		}
		if (values.containsKey(Hosts.WOL_PORT) == false) {
			values.put(Hosts.WOL_PORT, 0);
		}
		if (values.containsKey(Hosts.WOL_WAIT) == false) {
			values.put(Hosts.WOL_WAIT, 0);
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = db.insert(HOSTS_TABLE_NAME, Hosts.ADDR, values);
		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(Hosts.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case HOSTS:
			count = db.delete(HOSTS_TABLE_NAME, where, whereArgs);
			break;

		case HOST_ID:
			String hostId = uri.getPathSegments().get(1);
			count = db.delete(HOSTS_TABLE_NAME, Hosts._ID + "=" + hostId + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case HOSTS:
			count = db.update(HOSTS_TABLE_NAME, values, where, whereArgs);
			break;

		case HOST_ID:
			String hostId = uri.getPathSegments().get(1);
			count = db.update(HOSTS_TABLE_NAME, values, Hosts._ID + "=" + hostId + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, "hosts", HOSTS);
		sUriMatcher.addURI(AUTHORITY, "hosts/#", HOST_ID);

		sHostsProjectionMap = new HashMap<String, String>();
		sHostsProjectionMap.put(Hosts._ID, Hosts._ID);
		sHostsProjectionMap.put(Hosts.NAME, Hosts.NAME);
		sHostsProjectionMap.put(Hosts.ADDR, Hosts.ADDR);
		sHostsProjectionMap.put(Hosts.PORT, Hosts.PORT);
		sHostsProjectionMap.put(Hosts.USER, Hosts.USER);
		sHostsProjectionMap.put(Hosts.PASS, Hosts.PASS);
		sHostsProjectionMap.put(Hosts.ESPORT, Hosts.ESPORT);
		sHostsProjectionMap.put(Hosts.TIMEOUT, Hosts.TIMEOUT);
		sHostsProjectionMap.put(Hosts.WIFI_ONLY, Hosts.WIFI_ONLY);
		sHostsProjectionMap.put(Hosts.ACCESS_POINT, Hosts.ACCESS_POINT);
		sHostsProjectionMap.put(Hosts.MAC_ADDR, Hosts.MAC_ADDR);
		sHostsProjectionMap.put(Hosts.WOL_PORT, Hosts.WOL_PORT);
		sHostsProjectionMap.put(Hosts.WOL_WAIT, Hosts.WOL_WAIT);
	}

	/**
	 * Notes table
	 */
	public static final class Hosts implements BaseColumns {

		// This class cannot be instantiated
		private Hosts() {
		}

		/**
		 * The name of the host (as in label/title)
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String NAME = "name";

		/**
		 * The address or IP of the host
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String ADDR = "address";

		/**
		 * The note itself
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String PORT = "http_port";

		/**
		 * The user name if HTTP Auth is used
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String USER = "user";

		/**
		 * The password if HTTP Auth is used
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String PASS = "pass";
		
		/**
		 * The event server port
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String ESPORT = "esport";
		
		/**
		 * The socket read timeout in milliseconds
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String TIMEOUT = "timeout";
		
		/**
		 * If this connection is for wireless lan only
		 * <P>
		 * Type: BOOLEAN
		 * </P>
		 */
		public static final String WIFI_ONLY = "wifi_only";
		
		/**
		 * If WIFI_ONLY is set this may or may not include an access point name
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String ACCESS_POINT = "access_point";
		
		/**
		 * The MAC address of this host
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String MAC_ADDR = "mac_addr";

		/**
		 * The time in seconds to wait after sending WOL paket
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String WOL_WAIT = "wol_wait";
		
		/**
		 * The port the WOL packet should be send to
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String WOL_PORT = "wol_port";

		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + HOSTS_TABLE_NAME);

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.xbmc.host";

		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
		 * note.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.xbmc.host";

		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = NAME + " ASC";

	}
}
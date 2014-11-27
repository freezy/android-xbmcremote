package org.xbmc.android.remote.business;

import android.content.SharedPreferences;

import org.xbmc.api.business.ISortableManager;
import org.xbmc.api.type.SortType;

public class SortPreferenceMedia implements ISortableManager {
    public static final String PREF_SORT_BY_PREFIX = "sort_by_";
    public static final String PREF_SORT_ORDER_PREFIX = "sort_order_";

    /* The idea of the sort keys is to remember different sort settings for
     * each type. In your controller, make sure you run setSortKey() in the
	 * onCreate() method.
	 */
    public static final int PREF_SORT_KEY_ALBUM = 1;
    public static final int PREF_SORT_KEY_ARTIST = 2;
    public static final int PREF_SORT_KEY_SONG = 3;
    public static final int PREF_SORT_KEY_GENRE = 4;
    public static final int PREF_SORT_KEY_FILEMODE = 5;
    public static final int PREF_SORT_KEY_SHOW = 6;
    public static final int PREF_SORT_KEY_MOVIE = 7;
    public static final int PREF_SORT_KEY_EPISODE = 8;

    public SharedPreferences mPref;
    public int mCurrentSortKey;

    /**
     * Sets the static reference to the preferences object. Used to obtain
     * current sort values.
     *
     * @param pref
     */
    public void setPreferences(SharedPreferences pref) {
        mPref = pref;
    }

    /**
     * Sets which kind of view is currently active.
     *
     * @param sortKey
     */
    public void setSortKey(int sortKey) {
        mCurrentSortKey = sortKey;
    }

    /**
     * Returns currently saved "sort by" value. If the preference was not set yet, or
     * if the current sort key is not set, return default value.
     *
     * @param type Default value
     * @return Sort by field
     */
    public int getSortBy(int type) {
        if (mPref != null) {
            return mPref.getInt(PREF_SORT_BY_PREFIX + mCurrentSortKey, type);
        }
        return type;
    }

    /**
     * Returns currently saved "sort by" value. If the preference was not set yet, or
     * if the current sort key is not set, return "ASC".
     *
     * @return Sort order
     */
    public String getSortOrder() {
        if (mPref != null) {
            return mPref.getString(PREF_SORT_ORDER_PREFIX + mCurrentSortKey, SortType.ORDER_ASC);
        }
        return SortType.ORDER_ASC;
    }
}
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
import org.xbmc.android.remote.presentation.activity.DialogFactory;
import org.xbmc.android.remote.presentation.activity.MusicArtistActivity;
import org.xbmc.android.remote.presentation.widget.OneLabelItemView;
import org.xbmc.android.util.ImportUtilities;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IMusicManager;
import org.xbmc.api.object.Artist;
import org.xbmc.api.object.Genre;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class ArtistListController extends ListController implements IController {
	
	public static final int ITEM_CONTEXT_QUEUE = 1;
	public static final int ITEM_CONTEXT_PLAY = 2;
	public static final int ITEM_CONTEXT_QUEUE_GENRE = 3;
	public static final int ITEM_CONTEXT_PLAY_GENRE = 4;
	public static final int ITEM_CONTEXT_INFO = 5;
	
	private boolean mLoadCovers = false;
	private Genre mGenre;
	private IMusicManager mMusicManager;
	
	public void onCreate(Activity activity, Handler handler, AbsListView list) {
		
		mMusicManager = ManagerFactory.getMusicManager(this);
		
		final String sdError = ImportUtilities.assertSdCard();
		mLoadCovers = sdError == null;
		
		if (!isCreated()) {
			super.onCreate(activity, handler, list);
			
			if (!mLoadCovers) {
				Toast toast = Toast.makeText(activity, sdError + " Displaying place holders only.", Toast.LENGTH_LONG);
				toast.show();
			}
			
			mGenre = (Genre)mActivity.getIntent().getSerializableExtra(ListController.EXTRA_GENRE);
			
			mFallbackBitmap = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.icon_artist);
			setupIdleListener();
			
			activity.registerForContextMenu(mList);
			mList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if(isLoading()) return;
					Intent nextActivity;
					Artist artist = (Artist)mList.getAdapter().getItem(((OneLabelItemView)view).position);
					nextActivity = new Intent(view.getContext(), MusicArtistActivity.class);
					nextActivity.putExtra(ListController.EXTRA_LIST_CONTROLLER, new AlbumListController());
					nextActivity.putExtra(ListController.EXTRA_ARTIST, artist);
					mActivity.startActivity(nextActivity);
				}
			});
			
			mList.setOnKeyListener(new ListControllerOnKeyListener<Artist>());			
			
			if (mGenre != null) {
				setTitle(mGenre.name + " - Artists...");
				showOnLoading();
				mMusicManager.getArtists(new DataResponse<ArrayList<Artist>>() {
					public void run() {
						if (value.size() > 0) {
							setTitle(mGenre.name + " - Artists (" + value.size() + ")");
							mList.setAdapter(new ArtistAdapter(mActivity, value));
						} else {
							setTitle(mGenre.name + " - Artists");
							setNoDataMessage("No artists found.", R.drawable.icon_artist_dark);
						}
					}
				}, mGenre, mActivity.getApplicationContext());
			} else {
				setTitle("Artists...");
				showOnLoading();
				mMusicManager.getArtists(new DataResponse<ArrayList<Artist>>() {
					public void run() {
						if (value.size() > 0) {
							setTitle("Artists (" + value.size() + ")");
							mList.setAdapter(new ArtistAdapter(mActivity, value));
						} else {
							setTitle("Artists");
							setNoDataMessage("No artists found.", R.drawable.icon_artist_dark);
						}
					}
				}, mActivity.getApplicationContext());
			}
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// be aware that this must be explicitly called by your activity!
		final Artist artist = (Artist)mList.getAdapter().getItem(((OneLabelItemView)(((AdapterContextMenuInfo)menuInfo).targetView)).position);
		menu.setHeaderTitle(artist.name);
		menu.add(0, ITEM_CONTEXT_QUEUE, 1, "Queue all songs from Artist");
		menu.add(0, ITEM_CONTEXT_PLAY, 2, "Play all songs from Artist");
		if (mGenre != null) {
			menu.add(0, ITEM_CONTEXT_QUEUE_GENRE, 3, "Queue only " + mGenre.name + " from Artist");
			menu.add(0, ITEM_CONTEXT_PLAY_GENRE, 4, "Play only " + mGenre.name + " from Artist");
		}
		menu.add(0, ITEM_CONTEXT_INFO, 5, "View Details");
	}
	
	public void onContextItemSelected(MenuItem item) {
		// be aware that this must be explicitly called by your activity!
		final Artist artist = (Artist)mList.getAdapter().getItem(((OneLabelItemView)((AdapterContextMenuInfo)item.getMenuInfo()).targetView).position);
		switch (item.getItemId()) {
			case ITEM_CONTEXT_QUEUE:
				mMusicManager.addToPlaylist(new QueryResponse(
						mActivity, 
						"Adding all songs by " + artist.name + " to playlist...", 
						"Error adding songs!"
					), artist, mActivity.getApplicationContext());
				break;
			case ITEM_CONTEXT_PLAY:
				mMusicManager.play(new QueryResponse(
						mActivity, 
						"Playing all songs by " + artist.name + "...", 
						"Error playing songs!",
						true
					), artist, mActivity.getApplicationContext());
				break;
			case ITEM_CONTEXT_QUEUE_GENRE:
				mMusicManager.addToPlaylist(new QueryResponse(
						mActivity, 
						"Adding all songs of genre " + mGenre.name + " by " + artist.name + " to playlist...", 
						"Error adding songs!"
					), artist, mGenre, mActivity.getApplicationContext());
				break;
			case ITEM_CONTEXT_PLAY_GENRE:
				mMusicManager.play(new QueryResponse(
						mActivity, 
						"Playing all songs of genre " + mGenre.name + " by " + artist.name + "...", 
						"Error playing songs!",
						true
					), artist, mGenre, mActivity.getApplicationContext());
				break;
			case ITEM_CONTEXT_INFO:
				DialogFactory.getArtistDetail(mMusicManager, mActivity, artist, mActivity.getApplicationContext()).show();
				break;
			default:
				return;
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu) {
	}
	
	private class ArtistAdapter extends ArrayAdapter<Artist> {
		ArtistAdapter(Activity activity, ArrayList<Artist> items) {
			super(activity, 0, items);
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			final OneLabelItemView view;
			if (convertView == null) {
				view = new OneLabelItemView(mActivity, mMusicManager, parent.getWidth(), mFallbackBitmap, mList.getSelector(), true);
			} else {
				view = (OneLabelItemView)convertView;
			}
			final Artist artist = this.getItem(position);
			view.reset();
			view.position = position;
			view.title = artist.name;
			
			if (mLoadCovers) {
				view.getResponse().load(artist, !mPostScrollLoader.isListIdle());
			}
			return view;
		}
	}
	private static final long serialVersionUID = 4360738733222799619L;

	
	public void onActivityPause() {
		if (mMusicManager != null) {
			mMusicManager.setController(null);
			mMusicManager.postActivity();
		}
		super.onActivityPause();
	}

	public void onActivityResume(Activity activity) {
		super.onActivityResume(activity);
		if (mMusicManager != null) {
			mMusicManager.setController(this);
		}
	}
}

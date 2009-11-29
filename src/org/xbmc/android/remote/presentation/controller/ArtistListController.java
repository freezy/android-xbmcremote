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
import org.xbmc.android.remote.presentation.activity.MusicArtistActivity;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IMusicManager;
import org.xbmc.api.object.Artist;
import org.xbmc.api.object.Genre;

import android.app.Activity;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class ArtistListController extends ListController implements IController {
	
	public static final int ITEM_CONTEXT_QUEUE = 1;
	public static final int ITEM_CONTEXT_PLAY = 2;
	public static final int ITEM_CONTEXT_QUEUE_GENRE = 3;
	public static final int ITEM_CONTEXT_PLAY_GENRE = 4;
	
	private Genre mGenre;
	private IMusicManager mMusicManager;
	
	public void onCreate(Activity activity, ListView list) {
		
		mMusicManager = ManagerFactory.getMusicManager(activity.getApplicationContext(), this);
		
		if (!isCreated()) {
			super.onCreate(activity, list);
			
			mGenre = (Genre)mActivity.getIntent().getSerializableExtra(ListController.EXTRA_GENRE);
			
			mActivity.registerForContextMenu(mList);
			mList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent nextActivity;
					Artist artist = (Artist)view.getTag();
					nextActivity = new Intent(view.getContext(), MusicArtistActivity.class);
					nextActivity.putExtra(ListController.EXTRA_LIST_LOGIC, new AlbumListController());
					nextActivity.putExtra(ListController.EXTRA_ARTIST, artist);
					mActivity.startActivity(nextActivity);
				}
			});
			
			mList.setOnKeyListener(new ListControllerOnKeyListener<Artist>());
			
			if (mGenre != null) {
				setTitle(mGenre.name + " - Artists...");
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
				}, mGenre);
			} else {
				setTitle("Artists...");
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
				});
			}
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// be aware that this must be explicitly called by your activity!
		final Artist artist = (Artist)((AdapterContextMenuInfo)menuInfo).targetView.getTag();
		menu.setHeaderTitle(artist.name);
		menu.add(0, ITEM_CONTEXT_QUEUE, 1, "Queue all songs from Artist");
		menu.add(0, ITEM_CONTEXT_PLAY, 2, "Play all songs from Artist");
		if (mGenre != null) {
			menu.add(0, ITEM_CONTEXT_QUEUE_GENRE, 3, "Queue only " + mGenre.name + " from Artist");
			menu.add(0, ITEM_CONTEXT_PLAY_GENRE, 4, "Play only " + mGenre.name + " from Artist");
			
		}
	}
	
	public void onContextItemSelected(MenuItem item) {
		// be aware that this must be explicitly called by your activity!
		final Artist artist = (Artist)((AdapterContextMenuInfo)item.getMenuInfo()).targetView.getTag();
		switch (item.getItemId()) {
			case ITEM_CONTEXT_QUEUE:
				mMusicManager.addToPlaylist(new QueryResponse(
						mActivity, 
						"Adding all songs by " + artist.name + " to playlist...", 
						"Error adding songs!"
					), artist);
				break;
			case ITEM_CONTEXT_PLAY:
				mMusicManager.play(new QueryResponse(
						mActivity, 
						"Playing all songs by " + artist.name + "...", 
						"Error playing songs!",
						true
					), artist);
				break;
			case ITEM_CONTEXT_QUEUE_GENRE:
				mMusicManager.addToPlaylist(new QueryResponse(
						mActivity, 
						"Adding all songs of genre " + mGenre.name + " by " + artist.name + " to playlist...", 
						"Error adding songs!"
					), artist, mGenre);
				break;
			case ITEM_CONTEXT_PLAY_GENRE:
				mMusicManager.play(new QueryResponse(
						mActivity, 
						"Playing all songs of genre " + mGenre.name + " by " + artist.name + "...", 
						"Error playing songs!",
						true
					), artist, mGenre);
				break;
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu) {
	}
	
	private class ArtistAdapter extends ArrayAdapter<Artist> {
		private Activity mActivity;
		ArtistAdapter(Activity activity, ArrayList<Artist> items) {
			super(activity, R.layout.listitem_oneliner, items);
			mActivity = activity;
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			View row;
			if (convertView == null) {
				LayoutInflater inflater = mActivity.getLayoutInflater();
				row = inflater.inflate(R.layout.listitem_oneliner, null);
			} else {
				row = convertView;
			}
			final Artist artist = this.getItem(position);
			row.setTag(artist);
			final TextView title = (TextView)row.findViewById(R.id.MusicItemTextViewTitle);
			final ImageView icon = (ImageView)row.findViewById(R.id.MusicItemImageViewArt);
			title.setText(artist.name);
			icon.setImageResource(R.drawable.icon_artist);
			return row;
		}
	}
	private static final long serialVersionUID = 4360738733222799619L;

	
	public void onActivityPause() {
		if (mMusicManager != null) {
			mMusicManager.setController(null);
		}
	}

	public void onActivityResume(Activity activity) {
		if (mMusicManager != null) {
			mMusicManager.setController(this);
		}
	}
}

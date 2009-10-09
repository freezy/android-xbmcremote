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

package org.xbmc.android.remote.guilogic;

import java.util.ArrayList;

import org.xbmc.android.backend.httpapi.HttpApiHandler;
import org.xbmc.android.backend.httpapi.HttpApiThread;
import org.xbmc.android.remote.R;
import org.xbmc.android.remote.activity.MusicGenreActivity;
import org.xbmc.httpapi.data.Artist;
import org.xbmc.httpapi.data.Genre;
import org.xbmc.httpapi.data.Song;

import android.app.Activity;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
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

public class GenreListLogic extends ListLogic {
	
	public static final int ITEM_CONTEXT_QUEUE = 1;
	public static final int ITEM_CONTEXT_PLAY = 2;
	
	public void onCreate(Activity activity, ListView list) {
		if (!isCreated()) {
			super.onCreate(activity, list);
			
			mActivity.registerForContextMenu(mList);
			mList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent nextActivity;
					Genre genre = (Genre)view.getTag();
					nextActivity = new Intent(view.getContext(), MusicGenreActivity.class);
					nextActivity.putExtra(ListLogic.EXTRA_GENRE, genre);
					nextActivity.putExtra(ListLogic.EXTRA_LIST_LOGIC, new SongListLogic());
					mActivity.startActivity(nextActivity);
				}
			});

			setTitle("Genres...");
			HttpApiThread.music().getGenres(new HttpApiHandler<ArrayList<Genre>>(mActivity) {
				public void run() {
					setTitle("Genres (" + value.size() + ")");
					mList.setAdapter(new GenreAdapter(mActivity, value));
				}
			});
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		final Artist artist = (Artist)((AdapterContextMenuInfo)menuInfo).targetView.getTag();
		menu.setHeaderTitle(artist.name);
		menu.add(0, ITEM_CONTEXT_QUEUE, 1, "Queue all songs");
		menu.add(0, ITEM_CONTEXT_PLAY, 2, "Play all songs");
	}
	
	public void onContextItemSelected(MenuItem item) {
		final Artist artist = (Artist)((AdapterContextMenuInfo)item.getMenuInfo()).targetView.getTag();
		switch (item.getItemId()) {
			case ITEM_CONTEXT_QUEUE:
				HttpApiThread.music().addToPlaylist(new HttpApiHandler<Song>(mActivity), artist);
				break;
			case ITEM_CONTEXT_PLAY:
				HttpApiThread.music().play(new HttpApiHandler<Boolean>(mActivity), artist);
				break;
		}
	}
	
	private class GenreAdapter extends ArrayAdapter<Genre> {
		private Activity mActivity;
		GenreAdapter(Activity activity, ArrayList<Genre> items) {
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
			final Genre genre = this.getItem(position);
			row.setTag(genre);
			final TextView title = (TextView)row.findViewById(R.id.MusicItemTextViewTitle);
			final ImageView icon = (ImageView)row.findViewById(R.id.MusicItemImageViewArt);
			title.setText(genre.name);
			icon.setImageResource(R.drawable.icon_genre);
			
			return row;
		}
	}
	private static final long serialVersionUID = 4360738733222799619L;
}

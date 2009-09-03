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

import java.io.File;
import java.util.ArrayList;

import org.xbmc.android.remote.R;
import org.xbmc.android.util.ErrorHandler;
import org.xbmc.android.util.ImageLoader;
import org.xbmc.android.util.ImportUtilities;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.httpapi.client.MusicClient;
import org.xbmc.httpapi.data.Album;
import org.xbmc.httpapi.data.Song;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * The "album wall" activity. Let's see if we get it scrolling smoothly with
 * more than 2 items :)
 *  
 * @author freezy <phreezie@gmail.com>
 */
public class AlbumGridActivity extends Activity {
	
	public static Bitmap coverQueued, coverDownloading, coverError, coverInit;
	private final MusicClient mdb = ConnectionManager.getHttpClient(this).music;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ErrorHandler.setActivity(this);
		setContentView(R.layout.albumgrid);
		final GridView gridview = (GridView)findViewById(R.id.albumgrid_grid);
		
		Resources res = getResources();
		coverDownloading = BitmapFactory.decodeResource(res, R.drawable.cover_downloading);
		coverQueued = BitmapFactory.decodeResource(res, R.drawable.cover_queued);
		coverError = BitmapFactory.decodeResource(res, R.drawable.cover_error);
		coverInit = BitmapFactory.decodeResource(res, R.drawable.cover_init);
		
		// fetch the albums so we can give it to the adapter
		final ArrayList<Album> albums = mdb.getAlbums(); 
		
		// TODO this is going to need some cleanup..
		final AlbumAdapter adapter = new AlbumAdapter(this, albums);
		ImageLoader.imageCacheLoader = new ImageLoader();
		ImageLoader.imageNetworkLoader = new ImageLoader();
		final ImageLoader cacheLoader = ImageLoader.imageCacheLoader; 
		final ImageLoader xbmcLoader = ImageLoader.imageNetworkLoader; 
		if (!cacheLoader.isAlive()) {
			cacheLoader.start();
		}
		if (!xbmcLoader.isAlive()) {
			xbmcLoader.start();
		}
//		ImportUtilities.purgeCache();
		ImageLoader.setAdapter(adapter);
		gridview.setAdapter(adapter);
	}
	
	/**
	 * Returns the popup view which displays the big album cover along with
	 * year and genre
	 * @param album
	 * @return
	 */
	public static View.OnClickListener getAlbumOnClickListener(final Album album) {
		return new View.OnClickListener() {
			public void onClick(View v) {
				
				Dialog dialog = new Dialog(v.getContext());
				dialog.setContentView(R.layout.albuminfo);
				
				ConnectionManager.getHttpClient().music.updateAlbumInfo(album);
				dialog.setTitle(album.name);

				// get controls
				TextView artistText = (TextView) dialog.findViewById(R.id.album_artistname);
				ImageView cover = (ImageView)dialog.findViewById(R.id.album_cover);
				TextView genresText = (TextView) dialog.findViewById(R.id.album_genres);
				TextView yearText = (TextView) dialog.findViewById(R.id.album_year);
				
				// update content
				artistText.setText(album.artist);
				if (album.year > 0) {
					yearText.setText(String.valueOf(album.year));
				} else {
					yearText.setVisibility(View.GONE);
				}
				if (album.genres != null) {
					genresText.setText(album.genres);
				} else {
					genresText.setVisibility(View.GONE);
				}
				
		        final File file = new File(ImportUtilities.getCacheDirectory(album.getArtFolder(), ImageLoader.DIR_BIG), album.getCrc());
		        if (file.exists()) {
		        	cover.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
		        }
				cover.setOnClickListener(getTracksOnClickListener(album));
				
				dialog.show();
			}
		};
	}
	
	/**
	 * Returns the tracklisting popup
	 * @param album
	 * @return
	 */
	public static View.OnClickListener getTracksOnClickListener(final Album album) {
		return new View.OnClickListener() {
			public void onClick(View v) {
				Dialog dialog = new Dialog(v.getContext());
				dialog.setContentView(R.layout.albumtracks);
				dialog.setTitle(album.name);

				// get controls
				TextView artistText = (TextView) dialog.findViewById(R.id.album_artistname);
				ImageView cover = (ImageView)dialog.findViewById(R.id.album_cover);
				TextView numTrackText = (TextView) dialog.findViewById(R.id.album_numtracks);
				TextView yearText = (TextView) dialog.findViewById(R.id.album_year);
				TableLayout trackTable = (TableLayout) dialog.findViewById(R.id.album_tracktable);

				// update content
				artistText.setText(album.artist);
				if (album.year > 0) {
					yearText.setText(String.valueOf(album.year));
				} else {
					yearText.setVisibility(View.GONE);
				}
				
		        final File file = new File(ImportUtilities.getCacheDirectory(album.getArtFolder(), ImageLoader.DIR_SMALL), album.getCrc());
		        if (file.exists()) {
		        	cover.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
		        }
		        
				trackTable.setScrollContainer(true);
				
				ArrayList<Song> songs = ConnectionManager.getHttpClient().music.getSongs(album);
				numTrackText.setText(songs.size() + " Tracks");
				
				
				for (Song song: songs) {
					TableRow tr = new TableRow(v.getContext());
					
					TextView txtTrack = new TextView(v.getContext());
					txtTrack.setText(String.valueOf(song.track));
					txtTrack.setGravity(Gravity.RIGHT);
					txtTrack.setWidth(20);
					txtTrack.setPadding(0, 0, 5, 0);
					txtTrack.setTextSize(TypedValue.COMPLEX_UNIT_PX, 10);
					
					TextView txtTitle = new TextView(v.getContext());
					if (album.isVA()) {
						txtTitle.setText(song.artist + " - " + song.title);
					} else {
						txtTitle.setText(song.title);
					}
					txtTitle.setWidth(200);
					
					TextView txtDuration = new TextView(v.getContext());
					txtDuration.setText(song.getDuration());
					txtDuration.setGravity(Gravity.RIGHT);
					
					tr.addView(txtTrack);
					tr.addView(txtTitle);
					tr.addView(txtDuration);
					
					trackTable.addView(tr);
				}
				dialog.show();
			}
		};		
	}
}
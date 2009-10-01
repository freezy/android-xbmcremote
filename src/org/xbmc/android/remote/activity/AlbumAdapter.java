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

import java.util.ArrayList;

import org.xbmc.android.util.ImageLoader;
import org.xbmc.httpapi.data.Album;
import org.xbmc.httpapi.data.ICoverArt;
import org.xbmc.httpapi.type.ThumbSize;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Adapter class for the album grid view
 * 
 * @author Team XBMC
 */
public class AlbumAdapter extends BaseAdapter {

	final private Context mContext;
	final private ArrayList<Album> mAlbums;

	/**
	 * Class constructor sets context and data
	 * @param c       Application context
	 * @param albums  Albums to render
	 */
	public AlbumAdapter(Context c, ArrayList<Album> albums) {
		mContext = c;
		mAlbums = albums;
	}
	
	/**
	 * Switches old image with new image, but only after verifying that the
	 * image view is still associated to the correct album.
	 * @param bitmap   New bitmap to load
	 * @param art      Album instance to verify
	 * @param image    Image view to update
	 */
	public void onImageReady(Bitmap bitmap, ICoverArt art, ImageView image) {
		image.setImageBitmap(bitmap);
		AlbumHolder holder = (AlbumHolder)image.getTag();
		if (holder.thumbCrc.equals(art.getCrc())) {
			if (bitmap == null) {
				image.setImageBitmap(AlbumGridActivity.coverError);
			} else {
				try {
					image.setImageBitmap(bitmap);
					image.setOnClickListener(AlbumGridActivity.getAlbumOnClickListener((Album)art));
				} catch (Exception e) {
					System.err.println("ERROR: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * That's the method called by the grid view.
	 * @param position     Position of the album to render
	 * @param convertView  ImageView object of recycled view
	 * @param parent       Parent view
	 * @return ImageView   Created/updated view
	 */
	public View getView(final int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		
		final Album album = mAlbums.get(position); 
		final AlbumHolder holder;
		try {
			if (convertView == null) {
				imageView = new ImageView(mContext);
				holder = new AlbumHolder();
				imageView.setLayoutParams(new GridView.LayoutParams(ThumbSize.medium.getPixel(), ThumbSize.medium.getPixel()));
//				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(0, 0, 1, 1);
			} else {
				imageView = (ImageView)convertView;
				holder = (AlbumHolder)imageView.getTag();
			}

			holder.thumbCrc = album.getCrc();
			imageView.setTag(holder);
			
			final Bitmap bitmap = ImageLoader.loadCachedCover(imageView, album, ThumbSize.medium);
			if (bitmap != null) {
				onImageReady(bitmap, album, imageView);
			} else {
//				onImageReady(AlbumGridActivity.coverInit, album, imageView);
			}
			
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println(e.getStackTrace());
			imageView = null;
		}

		return imageView;
	}
	
	/**
	 * Returns number of albums
	 */
	public int getCount() {
		return mAlbums.size();
	}
	
	/**
	 * Returns an album at a position
	 */
	public Object getItem(int position) {
		return mAlbums.get(position);
	}
	
	/**
	 * Returns ID of an album
	 */
	public long getItemId(int position) {
		return mAlbums.get(position).id;
	}	
}

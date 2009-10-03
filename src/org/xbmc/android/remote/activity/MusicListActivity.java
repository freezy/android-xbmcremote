
package org.xbmc.android.remote.activity;

import java.io.IOException;
import java.util.ArrayList;

import org.xbmc.android.backend.httpapi.HttpApiHandler;
import org.xbmc.android.backend.httpapi.HttpApiThread;
import org.xbmc.android.remote.R;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.android.util.ErrorHandler;
import org.xbmc.eventclient.ButtonCodes;
import org.xbmc.eventclient.EventClient;
import org.xbmc.httpapi.data.Album;
import org.xbmc.httpapi.data.Song;
import org.xbmc.httpapi.type.ListType;
import org.xbmc.httpapi.type.ThumbSize;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class MusicListActivity extends ListActivity {
	
	public static final int ITEM_CONTEXT_QUEUE = 1;
	public static final int ITEM_CONTEXT_PLAY = 2;
	public static final int ITEM_CONTEXT_INFO = 3;
	
	public static final String EXTRA_LIST_TYPE = "listType"; 
	public static final String EXTRA_ALBUM = "album"; 
	
	private ListType mListType;
	private Album mAlbum;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		ErrorHandler.setActivity(this);
		setContentView(R.layout.music_list);

		final String mt = getIntent().getStringExtra(EXTRA_LIST_TYPE);
		final TextView titleView = (TextView)findViewById(R.id.MusicListTextViewTitle);
		mListType = mt != null ? ListType.valueOf(mt) : ListType.albums;
		mAlbum = (Album)getIntent().getSerializableExtra(EXTRA_ALBUM);
		
		final ListView list = (ListView)findViewById(android.R.id.list);
		registerForContextMenu(list);
		
		// depending on list type, fetch albums or songs
		switch (mListType) {
			case albums:
				titleView.setText("Albums...");
				HttpApiThread.music().getAlbums(new HttpApiHandler<ArrayList<Album>>(this) {
					public void run() {
						titleView.setText("Albums (" + value.size() + ")");
						list.setAdapter(new AlbumAdapter(mActivity, value));
					}
				});
				break;
			case songs:
				titleView.setText("Songs...");
				if (mAlbum != null) {
					HttpApiThread.music().getSongs(new HttpApiHandler<ArrayList<Song>>(this) {
						public void run() {
							titleView.setText(mAlbum.name);
							list.setAdapter(new SongAdapter(mActivity, value));
						}
					}, mAlbum);
				}
				break;
			default:
				break;
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, ITEM_CONTEXT_QUEUE, 1, "Queue " + mListType.getSingular());
		menu.add(0, ITEM_CONTEXT_PLAY, 2, "Play " + mListType.getSingular());
		String title = "";
		switch (mListType) {
			case albums:
				final Album album = (Album)((AdapterContextMenuInfo)menuInfo).targetView.getTag();
				title = album.name;
				menu.add(0, ITEM_CONTEXT_INFO, 3, "View Details");
				break;
			case songs:
				final Song song = (Song)((AdapterContextMenuInfo)menuInfo).targetView.getTag();
				title = song.title;
				break;
		}
		menu.setHeaderTitle(title);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (mListType) {
			case albums:
				final Album album = (Album)((AdapterContextMenuInfo)item.getMenuInfo()).targetView.getTag();
				switch (item.getItemId()) {
					case ITEM_CONTEXT_QUEUE:
						HttpApiThread.music().addToPlaylist(new HttpApiHandler<Song>(this), album);
						break;
					case ITEM_CONTEXT_PLAY:
						HttpApiThread.music().play(new HttpApiHandler<Boolean>(this), album);
						break;
					case ITEM_CONTEXT_INFO:
						DialogFactory.getAlbumDetail(this, album).show();
						break;
					default:
						return super.onContextItemSelected(item);
				}
			break;
			case songs:
				final Song song = (Song)((AdapterContextMenuInfo)item.getMenuInfo()).targetView.getTag();
				HttpApiThread.music().addToPlaylist(new HttpApiHandler<Boolean>(this), song);
				switch (item.getItemId()) {
					case ITEM_CONTEXT_QUEUE:
						HttpApiThread.music().addToPlaylist(new HttpApiHandler<Boolean>(this), song);
						break;
					case ITEM_CONTEXT_PLAY:
						HttpApiThread.music().play(new HttpApiHandler<Boolean>(this), song);
						break;
					case ITEM_CONTEXT_INFO:
						break;
					default:
						return super.onContextItemSelected(item);
				}
			break;
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent nextActivity;
		switch (mListType) {
			case albums:
				Album album = (Album)v.getTag();
				nextActivity = new Intent(this, MediaTabContainerActivity.class);
				
				nextActivity.putExtras(getIntent().getExtras());
				
				nextActivity.putExtra(EXTRA_LIST_TYPE, ListType.songs.toString());
				nextActivity.putExtra(EXTRA_ALBUM, album);
				startActivity(nextActivity);
			break;
			case songs:
				Song song = (Song)v.getTag();
				HttpApiThread.music().play(new HttpApiHandler<Boolean>(this), song);
			break;
		}
	}

	
	private class SongAdapter extends ArrayAdapter<Song> {
		private Activity mActivity;
		SongAdapter(Activity activity, ArrayList<Song> items) {
			super(activity, R.layout.music_item, items);
			mActivity = activity;
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			View row;
			if (convertView == null) {
				LayoutInflater inflater = mActivity.getLayoutInflater();
				row = inflater.inflate(R.layout.music_item, null);
			} else {
				row = convertView;
			}
			final Song song = this.getItem(position);
			row.setTag(song);
			final TextView title = (TextView)row.findViewById(R.id.MusicItemTextViewTitle);
			final TextView subtitle = (TextView)row.findViewById(R.id.MusicItemTextViewSubtitle);
			final TextView subsubtitle = (TextView)row.findViewById(R.id.MusicItemTextViewSubSubtitle);
			final ImageView icon = (ImageView)row.findViewById(R.id.MusicItemImageViewArt);
			title.setText(song.track + " " + song.title);
			subtitle.setText(song.artist);
			subsubtitle.setText(song.getDuration());
			
			HttpApiThread.music().getAlbumCover(new HttpApiHandler<Bitmap>(mActivity, mAlbum) {
				public void run() {
					if (value == null) {
						icon.setImageResource(R.drawable.home_music);
					} else {
						icon.setImageBitmap(value);
					}
				}
			}, mAlbum, ThumbSize.small);
			return row;
		}
	}
	
	private class AlbumAdapter extends ArrayAdapter<Album> {
		private Activity mActivity;
		AlbumAdapter(Activity activity, ArrayList<Album> items) {
			super(activity, R.layout.music_item, items);
			mActivity = activity;
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			View row;
			if (convertView == null) {
				LayoutInflater inflater = mActivity.getLayoutInflater();
				row = inflater.inflate(R.layout.music_item, null);
			} else {
				row = convertView;
			}
			final Album album = this.getItem(position);
			row.setTag(album);
			final TextView title = (TextView)row.findViewById(R.id.MusicItemTextViewTitle);
			final TextView subtitle = (TextView)row.findViewById(R.id.MusicItemTextViewSubtitle);
			final TextView subsubtitle = (TextView)row.findViewById(R.id.MusicItemTextViewSubSubtitle);
			final ImageView icon = (ImageView)row.findViewById(R.id.MusicItemImageViewArt);
			title.setText(album.name);
			subtitle.setText(album.artist);
			subsubtitle.setText(album.year > 0 ? String.valueOf(album.year) : "");
			
			HttpApiThread.music().getAlbumCover(new HttpApiHandler<Bitmap>(mActivity, album) {
				public void run() {
					if (album.getId() == ((Album)mTag).getId()) {
						if (value == null) {
							icon.setImageResource(R.drawable.home_music);
						} else {
							icon.setImageBitmap(value);
						}
					}
				}
			}, album, ThumbSize.small);
			return row;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		EventClient client = ConnectionManager.getEventClient(this);	
		try {
			switch (keyCode) {
				case KeyEvent.KEYCODE_VOLUME_UP:
					client.sendButton("R1", ButtonCodes.REMOTE_VOLUME_PLUS, false, true, true, (short)0, (byte)0);
					return true;
				case KeyEvent.KEYCODE_VOLUME_DOWN:
					client.sendButton("R1", ButtonCodes.REMOTE_VOLUME_MINUS, false, true, true, (short)0, (byte)0);
					return true;
			}
		} catch (IOException e) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}
}

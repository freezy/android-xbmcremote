package org.xbmc.android.remote.activity;

import org.xbmc.httpapi.type.ListType;
import org.xbmc.httpapi.type.MediaType;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class MediaTabContainerActivity extends TabActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TabHost host = getTabHost();
		
		
		
		host.addTab(host.newTabSpec("files").setIndicator("Files").setContent(createMediaIntent(MediaType.music, this)));  
		host.addTab(host.newTabSpec("albums").setIndicator("Albums").setContent(createMusicIntent(ListType.albums, this)));
		
		
		String activeTab = getIntent().getStringExtra("activeTab");
		
		if (activeTab != null)
			host.setCurrentTabByTag(activeTab);
	}
	
	private Intent createMediaIntent(MediaType mediaType, Context c) {
		Intent nextActivity = new Intent(c, MediaListActivity.class);
		Bundle extras = getIntent().getExtras();
		if (extras != null)
			nextActivity.putExtras(extras);
		nextActivity.putExtra("activeTab", "files");
		nextActivity.putExtra("shareType", mediaType.toString());
		return nextActivity;
	}
	
	private Intent createMusicIntent(ListType listType, Context c) {
		Intent nextActivity = new Intent(c, MusicListActivity.class);
		Bundle extras = getIntent().getExtras();
		if (extras != null)
			nextActivity.putExtras(extras);
		if (nextActivity.getStringExtra(MusicListActivity.EXTRA_LIST_TYPE) == null)
			nextActivity.putExtra(MusicListActivity.EXTRA_LIST_TYPE, listType.toString());
		nextActivity.putExtra("activeTab", "albums");
		return nextActivity;
	}
}

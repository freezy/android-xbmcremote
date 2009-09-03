package org.xbmc.android.remote.activity;

import java.util.ArrayList;

import org.xbmc.android.util.ConnectionManager;
import org.xbmc.httpapi.Message;
import org.xbmc.httpapi.HttpClient;
import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class LogViewerActivity extends ListActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		HttpClient instance = ConnectionManager.getHttpApiInstance(this);
		
		ArrayList<Message> log = new ArrayList<Message>();
		for (Message m : instance.getMessenger())
			log.add(m);
		
		setListAdapter(new ArrayAdapter<Message>(this, android.R.layout.simple_list_item_1, log));
		getListView().setTextFilterEnabled(true);
	}
}

package org.xbmc.android.remote.activity;

import java.util.ArrayList;

import org.xbmc.android.util.XBMCControl;
import org.xbmc.httpapi.Message;
import org.xbmc.httpapi.XBMC;
import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class LogViewerActivity extends ListActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		XBMC instance = XBMCControl.getHttpApiInstance(this);
		
		ArrayList<Message> log = new ArrayList<Message>();
		for (Message m : instance.getMessenger())
			log.add(m);
		
		setListAdapter(new ArrayAdapter<Message>(this, android.R.layout.simple_list_item_1, log));
		getListView().setTextFilterEnabled(true);
	}
}

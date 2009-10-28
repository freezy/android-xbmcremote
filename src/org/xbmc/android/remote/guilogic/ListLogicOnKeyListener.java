package org.xbmc.android.remote.guilogic;

import org.xbmc.httpapi.data.NamedResource;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.ListView;

public class ListLogicOnKeyListener<T extends NamedResource> implements OnKeyListener {

	public ListLogicOnKeyListener(){
		
	}
	
	@SuppressWarnings("unchecked")
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		boolean gotMatch = false;
		int startIndex = ((ListView)v).getSelectedItem() == null ? 0 : ((ListView)v).getSelectedItemPosition() +1;
		if(event.getAction() == KeyEvent.ACTION_DOWN){ //only snoop on key down - never mind the key-up-event
			//Only "eat" ascii input
			if((event.getDisplayLabel() >= 65 && event.getDisplayLabel() <= 90 ) || ( event.getDisplayLabel() >= 97 && event.getDisplayLabel() <= 122 )){
				for(int i = startIndex; i < ((ListView)v).getCount(); i++ ){
					if(((T)((ListView)v).getItemAtPosition(i)).getShortName().toLowerCase().charAt(0) == Character.toLowerCase(event.getDisplayLabel())){
						gotMatch = true;
						((ListView)v).setSelection(i);
						break;
					}
				}
				//Check if we should iterate again from the top
				if(!gotMatch && startIndex > 0){
					for(int i = 0; i < startIndex -1 ; i++){
						if(((T)((ListView)v).getItemAtPosition(i)).getShortName().toLowerCase().charAt(0) == Character.toLowerCase(event.getDisplayLabel())){
							gotMatch = true;
							((ListView)v).setSelection(i);
							break;
						}
					}
					
				}
				return true; //event is eaten
			}
		}
		return false; //event is NOT eaten
	}
	

}

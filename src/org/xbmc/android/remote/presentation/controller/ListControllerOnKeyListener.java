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

import org.xbmc.httpapi.data.NamedResource;

import android.content.Context;
import android.os.Build;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;

public class ListControllerOnKeyListener<T extends NamedResource> implements OnKeyListener {
	
	public static final String[] DEVICES_HANDLE_MENU_LONGPRESS = {"HTC Magic"};
	public static final int LONGPRESS_REPEATS = 10;

	public static boolean sHandleMenuLongPress;
	
	static{
		//Decide if MenuKey LongPress is handled by the device itself.
		sHandleMenuLongPress = true;
		for(int i = 0; i < DEVICES_HANDLE_MENU_LONGPRESS.length; i++){
			if(Build.MODEL.equals(DEVICES_HANDLE_MENU_LONGPRESS[i])){
				sHandleMenuLongPress = false;
				break;
			}
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU){
			switch(event.getAction()){
			case KeyEvent.ACTION_DOWN:
				if(!sHandleMenuLongPress)
					return false;
				return handleMenuKeyDown(v, event);
			case  KeyEvent.ACTION_UP:
				return handleMenuKeyUp(v, event);
			default:
				return false;
			}
		}
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if ( keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z ){
				int startIndex = ((ListView)v).getSelectedItem() == null ? 0 : ((ListView)v).getSelectedItemPosition() +1;
				int count = ((ListView)v).getCount();
				for(int i = startIndex; i < count; i++){
					if(((T)((ListView)v).getItemAtPosition(i)).getShortName().toLowerCase().charAt(0) == Character.toLowerCase(event.getDisplayLabel())){
						((ListView)v).setSelection(i);
						return true;
					}
				}
				//Check if we should iterate again from the top
				if(startIndex > 0){
					for(int i = 0; i < startIndex -1 ; i++){
						if(((T)((ListView)v).getItemAtPosition(i)).getShortName().toLowerCase().charAt(0) == Character.toLowerCase(event.getDisplayLabel())){
							((ListView)v).setSelection(i);
							return true;
						}
					}
				}			
				return true;
			}
		}
		//event is NOT eaten
		return false;
	}
	
	private boolean handleMenuKeyDown(View v, KeyEvent event){
		InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if(event.getRepeatCount() >= LONGPRESS_REPEATS){
			imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);
			mCreatingSoftKeyboard = true;
			return true;
		}
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		return false;
	}
	private boolean handleMenuKeyUp(View v, KeyEvent event){
		if(mCreatingSoftKeyboard){
			mCreatingSoftKeyboard = false;
			return true;
		}
		
		return false;
	}

	private boolean mCreatingSoftKeyboard = false;
}
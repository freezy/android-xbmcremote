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

import java.io.IOException;

import org.xbmc.android.remote.R;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.android.util.ErrorHandler;
import org.xbmc.httpapi.client.ControlClient;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;

public class RemoteActivity extends Activity {
	
	private ControlClient mControl;
	private Vibrator mVibrator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ErrorHandler.setActivity(this);
        setContentView(R.layout.remote_xbox);
        
        mControl = ConnectionManager.getHttpClient(this).control;
        mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
  	  	
		setupButtons();
    }
    
	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN)
			return KeyboardAction("enter");
		else if (Math.abs(event.getX()) > 0.1f)
			return KeyboardAction(event.getX() < 0 ? "left" : "right");
		else if (Math.abs(event.getY()) > 0.1f)
			return KeyboardAction(event.getY() < 0 ? "up" : "down");
		
		return false;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		char key = (char)event.getUnicodeChar();
		
		if (key > 'A' && key < 'z')
			return KeyboardAction("" + key);
		
//		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			
//		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	private boolean KeyboardAction(String button) {
		try {
			ConnectionManager.getEventClient(RemoteActivity.this).sendButton("KB", button, false, true, true, (short)0, (byte)0);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * Shortly vibrates in order to give the "click" impression
	 */
	private void vibrate() {
		mVibrator.vibrate(45);
	}
	
	private class OnKeyboardAction implements OnTouchListener {
		private final String mAction;
		private final int mUp, mDown;
		public OnKeyboardAction(String action, int up, int down) {
			mAction = action;
			mUp = up;
			mDown = down;
		}
		public boolean onTouch(View v, MotionEvent event) {
			
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				vibrate();
				KeyboardAction(mAction);
				((ImageButton)v).setImageResource(mDown);
				return true;
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				((ImageButton)v).setImageResource(mUp);
				return true;
			}
			return false;
		}	
	}
	
	/**
	 * Assigns the button events to the views.
	 */
	private void setupButtons() {

		// seek back
		findViewById(R.id.RemoteXboxImgBtnSeekBack).setOnTouchListener(new OnKeyboardAction("reverse", R.drawable.remote_xbox_play, R.drawable.remote_xbox_play_down));
		
		// play
		((ImageButton)findViewById(R.id.RemoteXboxImgBtnPlay)).setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					vibrate();
					KeyboardAction("p");
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_play_down);
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_play);
				}
				return true;
			}
		});
		// seek forward
		((ImageButton)findViewById(R.id.RemoteXboxImgBtnSeekForward)).setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					vibrate();
					KeyboardAction("forward");
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_seek_forward_down);
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_seek_forward);
				}
				return true;
			}
		});
		
		// previous
		((ImageButton)findViewById(R.id.RemoteXboxImgBtnPrevious)).setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					vibrate();
//					mControl.playPrevious();
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_previous_down);
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_previous);
				}
				return true;
			}
		});
		// stop
		((ImageButton)findViewById(R.id.RemoteXboxImgBtnStop)).setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					vibrate();
//					mControl.stop();
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_stop_down);
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_stop);
				}
				return true;
			}
		});
		// pause
		((ImageButton)findViewById(R.id.RemoteXboxImgBtnPause)).setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					vibrate();
//					mControl.pause();
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_pause_down);
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_pause);
				}
				return true;
			}
		});
		// next
		((ImageButton)findViewById(R.id.RemoteXboxImgBtnNext)).setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					vibrate();
//					mControl.playNext();
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_next_down);
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_next);
				}
				return true;
			}
		});
		
		// title
		((ImageButton)findViewById(R.id.RemoteXboxImgBtnTitle)).setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					vibrate();
					KeyboardAction("title");
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_title_down);
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_title);
				}
				return true;
			}
		});
		// up
		((ImageButton)findViewById(R.id.RemoteXboxImgBtnUp)).setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					vibrate();
					KeyboardAction("up");
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_up_down);
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_up);
				}
				return true;
			}
		});
		// info
		((ImageButton)findViewById(R.id.RemoteXboxImgBtnInfo)).setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					vibrate();
					KeyboardAction("i");
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_info_down);
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_info);
				}
				return true;
			}
		});
		
		// left
		((ImageButton)findViewById(R.id.RemoteXboxImgBtnLeft)).setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					vibrate();
					KeyboardAction("left");
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_left_down);
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_left);
				}
				return true;
			}
		});
		// select
		((ImageButton)findViewById(R.id.RemoteXboxImgBtnSelect)).setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					vibrate();
					KeyboardAction("enter");
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_select_down);
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_select);
				}
				return true;
			}
		});
		// right
		((ImageButton)findViewById(R.id.RemoteXboxImgBtnRight)).setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					vibrate();
					KeyboardAction("right");
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_right_down);
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_right);
				}
				return true;
			}
		});
		
		// menu
		((ImageButton)findViewById(R.id.RemoteXboxImgBtnMenu)).setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					vibrate();
					KeyboardAction("menu");
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_menu_down);
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_menu);
				}
				return true;
			}
		});
		// down
		((ImageButton)findViewById(R.id.RemoteXboxImgBtnDown)).setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					vibrate();
					KeyboardAction("down");
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_down_down);
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_down);
				}
				return true;
			}
		});
		// back 
		((ImageButton)findViewById(R.id.RemoteXboxImgBtnBack)).setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					vibrate();
					KeyboardAction("backspace");
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_back_down);
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					((ImageButton)v).setImageResource(R.drawable.remote_xbox_back);
				}
				return true;
			}
		});
		
	}
}
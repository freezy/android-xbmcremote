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

package org.xbmc.android.remote.presentation.activity;

import java.io.IOException;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.controller.AbstractController;
import org.xbmc.android.remote.presentation.controller.IController;
import org.xbmc.android.remote.presentation.controller.ListController;
import org.xbmc.android.util.KeyTracker;
import org.xbmc.android.util.KeyTracker.Stage;
import org.xbmc.android.util.OnLongPressBackKeyTracker;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.business.ITvShowManager;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Episode;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.api.type.ThumbSize;
import org.xbmc.eventclient.ButtonCodes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EpisodeDetailsActivity extends Activity {
	
	private static final String NO_DATA = "-";
	
    private ConfigurationManager mConfigurationManager;
    private EpisodeDetailsController mEpisodeDetailsController;

	private KeyTracker mKeyTracker;
    
    private static final int[] sStarImages = { R.drawable.stars_0, R.drawable.stars_1, R.drawable.stars_2, R.drawable.stars_3, R.drawable.stars_4, R.drawable.stars_5, R.drawable.stars_6, R.drawable.stars_7, R.drawable.stars_8, R.drawable.stars_9, R.drawable.stars_10 };
	
    public EpisodeDetailsActivity() {
    	if(Integer.parseInt(VERSION.SDK) < 5) {
	    	mKeyTracker = new KeyTracker(new OnLongPressBackKeyTracker() {
	
				@Override
				public void onLongPressBack(int keyCode, KeyEvent event,
						Stage stage, int duration) {
					onKeyLongPress(keyCode, event);
				}
	
				@Override
				public void onShortPressBack(int keyCode, KeyEvent event,
						Stage stage, int duration) {
					EpisodeDetailsActivity.super.onKeyDown(keyCode, event);
				}
				
	    	});
    	} 
	}
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tvepisodedetails);
		
		// remove nasty top fading edge
		FrameLayout topFrame = (FrameLayout)findViewById(android.R.id.content);
		topFrame.setForeground(null);
		
		final Episode episode = (Episode)getIntent().getSerializableExtra(ListController.EXTRA_EPISODE);
		mEpisodeDetailsController = new EpisodeDetailsController(this, episode);
		
		((TextView)findViewById(R.id.titlebar_text)).setText(episode.getName());
		
		Log.i("EpisodeDetailsActivity", "rating = " + episode.rating + ", index = " + ((int)Math.round(episode.rating % 10)) + ".");
		if (episode.rating > -1) {
			((ImageView)findViewById(R.id.tvepisodedetails_rating_stars)).setImageResource(sStarImages[(int)Math.round(episode.rating % 10)]);
		}
		((TextView)findViewById(R.id.tvepisodedetails_show)).setText(episode.showTitle);
		((TextView)findViewById(R.id.tvepisodedetails_first_aired)).setText(episode.firstAired);
		((TextView)findViewById(R.id.tvepisodedetails_director)).setText(episode.director);
		((TextView)findViewById(R.id.tvepisodedetails_writer)).setText(episode.writer);
		((TextView)findViewById(R.id.tvepisodedetails_rating)).setText(String.valueOf(episode.rating));
		
		mEpisodeDetailsController.setupPlayButton((Button)findViewById(R.id.tvepisodedetails_playbutton));
		mEpisodeDetailsController.loadCover(new Handler(), (ImageView)findViewById(R.id.tvepisodedetails_thumb));
		mEpisodeDetailsController.updateEpisodeDetails(
				(TextView)findViewById(R.id.tvepisodedetails_plot),
				(LinearLayout)findViewById(R.id.tvepisodedetails_datalayout));
		
		mConfigurationManager = ConfigurationManager.getInstance(this);
	}
	
	private static class EpisodeDetailsController extends AbstractController implements INotifiableController, IController {
		
		private ITvShowManager mShowManager;
		private IControlManager mControlManager;
		private final Episode mEpisode;
		
		EpisodeDetailsController(Activity activity, Episode episode) {
			super.onCreate(activity, new Handler());
			mActivity = activity;
			mEpisode = episode;
			mShowManager = ManagerFactory.getTvManager(this);
			mControlManager = ManagerFactory.getControlManager(this);
		}
		
		public void setupPlayButton(Button button) {
			button.setText("Play Episode");
			button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					mControlManager.playFile(new DataResponse<Boolean>() {
						public void run() {
							if (value) {
								mActivity.startActivity(new Intent(mActivity, NowPlayingActivity.class));
							}
						}
					}, mEpisode.getPath(), mActivity.getApplicationContext());
				}
			});
		}
		
		public void loadCover(final Handler handler, final ImageView imageView) {
			mShowManager.getCover(new DataResponse<Bitmap>() {
				public void run() {
					handler.post(new Runnable() {
						public void run() {
							if (value == null) {
								imageView.setImageResource(R.drawable.nocover);
							} else {
								imageView.setImageBitmap(value);
							}
						}
					});
				}
			}, mEpisode, ThumbSize.BIG, null, mActivity.getApplicationContext(), false);
		}
		
		public void updateEpisodeDetails(final TextView plotView, final LinearLayout dataLayout) {
			mShowManager.updateEpisodeDetails(new DataResponse<Episode>() {
				public void run() {
					final Episode episode = value;
					plotView.setText(episode.plot.equals("") ? NO_DATA : episode.plot);
					
					if (episode.actors != null) {
						final LayoutInflater inflater = mActivity.getLayoutInflater();
						int n = 0;
						for (Actor actor : episode.actors) {
							final View view = inflater.inflate(R.layout.actor_item, null);
							
							((TextView)view.findViewById(R.id.actor_name)).setText(actor.name);
							if (actor.role != null && !actor.role.equals("")) {
								((TextView)view.findViewById(R.id.actor_role)).setText("as " + actor.role);
							} else {
								((TextView)view.findViewById(R.id.actor_role)).setVisibility(View.GONE);
							}
							ImageButton img = ((ImageButton)view.findViewById(R.id.actor_image));
							mShowManager.getCover(new DataResponse<Bitmap>() {
								public void run() {
									if (value != null) {
										((ImageButton)view.findViewById(R.id.actor_image)).setImageBitmap(value);
									}
								}
							}, actor, ThumbSize.SMALL, null, mActivity.getApplicationContext(), false);
							
							img.setTag(actor);
							
							// this isn't working
//							img.setOnClickListener(new OnClickListener() {
//								public void onClick(View v) {
//									Intent nextActivity;
//									Actor actor = (Actor)v.getTag();
//									nextActivity = new Intent(view.getContext(), ListActivity.class);
//									nextActivity.putExtra(ListController.EXTRA_LIST_CONTROLLER, new TvShowListController());
//									nextActivity.putExtra(ListController.EXTRA_ACTOR, actor);
//									mActivity.startActivity(nextActivity);
//								}
//							});
							dataLayout.addView(view);
							n++;
						}
					}
				}
			}, mEpisode, mActivity.getApplicationContext());
		}

		public void onActivityPause() {
			mShowManager.setController(null);
//			mShowManager.postActivity();
			mControlManager.setController(null);
		}

		public void onActivityResume(Activity activity) {
			mShowManager.setController(this);
			mControlManager.setController(this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mEpisodeDetailsController.onActivityResume(this);
		mConfigurationManager.onActivityResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mEpisodeDetailsController.onActivityPause();
		mConfigurationManager.onActivityPause();
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean handled = (mKeyTracker != null)? mKeyTracker.doKeyUp(keyCode, event): false;
		return handled || super.onKeyUp(keyCode, event);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		IEventClientManager client = ManagerFactory.getEventClientManager(mEpisodeDetailsController);
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
			client.setController(null);
			return false;
		}
		client.setController(null);
		boolean handled =  (mKeyTracker != null)?mKeyTracker.doKeyDown(keyCode, event):false;
		return handled || super.onKeyDown(keyCode, event);
	}
	
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		Intent intent = new Intent(EpisodeDetailsActivity.this, HomeActivity.class);
		intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		return true;
	}
}
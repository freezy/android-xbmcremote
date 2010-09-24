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
import org.xbmc.android.remote.presentation.controller.TvShowListController;
import org.xbmc.android.util.KeyTracker;
import org.xbmc.android.util.OnLongPressBackKeyTracker;
import org.xbmc.android.util.KeyTracker.Stage;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.business.ITvShowManager;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.TvShow;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.api.type.ThumbSize;
import org.xbmc.eventclient.ButtonCodes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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

public class TvShowDetailsActivity extends Activity {
	
	private static final String NO_DATA = "-";
	
    private ConfigurationManager mConfigurationManager;
    private TvShowDetailsController mTvShowDetailsController;

	private KeyTracker mKeyTracker;
    
    private static final int[] sStarImages = { R.drawable.stars_0, R.drawable.stars_1, R.drawable.stars_2, R.drawable.stars_3, R.drawable.stars_4, R.drawable.stars_5, R.drawable.stars_6, R.drawable.stars_7, R.drawable.stars_8, R.drawable.stars_9, R.drawable.stars_10 };
	
    public TvShowDetailsActivity() {
    	mKeyTracker = new KeyTracker(new OnLongPressBackKeyTracker() {

			@Override
			public void onLongPressBack(int keyCode, KeyEvent event,
					Stage stage, int duration) {
				Intent intent = new Intent(TvShowDetailsActivity.this, HomeActivity.class);
				intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}

			@Override
			public void onShortPressBack(int keyCode, KeyEvent event,
					Stage stage, int duration) {
				TvShowDetailsActivity.super.onKeyDown(keyCode, event);
			}
			
		});
	}
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tvdetails);
		
		// remove nasty top fading edge
		FrameLayout topFrame = (FrameLayout)findViewById(android.R.id.content);
		topFrame.setForeground(null);
		
		final TvShow show = (TvShow)getIntent().getSerializableExtra(ListController.EXTRA_TVSHOW);
		mTvShowDetailsController = new TvShowDetailsController(this, show);
		
		((TextView)findViewById(R.id.titlebar_text)).setText(show.getName());
		
		Log.i("EpisodeDetailsActivity", "rating = " + show.rating + ", index = " + ((int)Math.round(show.rating % 10)) + ".");
		if (show.rating > -1) {
			((ImageView)findViewById(R.id.tvdetails_rating_stars)).setImageResource(sStarImages[(int)Math.round(show.rating % 10)]);
		}
		((TextView)findViewById(R.id.tvdetails_first_aired)).setText(show.firstAired);
		((TextView)findViewById(R.id.tvdetails_genre)).setText(show.genre);
		((TextView)findViewById(R.id.tvdetails_rating)).setText(String.valueOf(show.rating));
		
		mTvShowDetailsController.setupPlayButton((Button)findViewById(R.id.tvdetails_playbutton));
		mTvShowDetailsController.loadCover((ImageView)findViewById(R.id.tvdetails_thumb));
		mTvShowDetailsController.updateTvShowDetails(
				(TextView)findViewById(R.id.tvdetails_episodes),
				(TextView)findViewById(R.id.tvdetails_studio),
				(TextView)findViewById(R.id.tvdetails_parental),
				(TextView)findViewById(R.id.tvdetails_plot),
				(LinearLayout)findViewById(R.id.tvdetails_datalayout));
		
		mConfigurationManager = ConfigurationManager.getInstance(this);
	}
	
	private static class TvShowDetailsController extends AbstractController implements INotifiableController, IController {
		
		private ITvShowManager mShowManager;
		private IControlManager mControlManager;
		private final TvShow mShow;
		
		TvShowDetailsController(Activity activity, TvShow show) {
			super.onCreate(activity, new Handler());
			mActivity = activity;
			mShow = show;
			mShowManager = ManagerFactory.getTvManager(this);
			mControlManager = ManagerFactory.getControlManager(this);
		}
		
		public void setupPlayButton(Button button) {
			button.setText("Play Show");
			button.setEnabled(false);
		}
		
		public void loadCover(final ImageView imageView) {
			mShowManager.getCover(new DataResponse<Bitmap>() {
				public void run() {
					if (value == null) {
						imageView.setImageResource(R.drawable.nocover);
					} else {
						imageView.setImageBitmap(value);
					}
				}
			}, mShow, ThumbSize.BIG, null, mActivity.getApplicationContext(), false);
		}
		
		public void updateTvShowDetails(final TextView episodesVew, final TextView studioView, final TextView parentalView, final TextView plotView, final LinearLayout dataLayout) {
			mShowManager.updateTvShowDetails(new DataResponse<TvShow>() {
				public void run() {
					final TvShow show = value;
					episodesVew.setText(show.numEpisodes + " (" + show.watchedEpisodes + " Watched - " + (show.numEpisodes - show.watchedEpisodes) + " Unwatched)");
					studioView.setText(show.network);
					parentalView.setText(show.contentRating.equals("") ? NO_DATA : show.contentRating);
					plotView.setText(show.summary.equals("") ? NO_DATA : show.summary);
					
					if (show.actors != null) {
						final LayoutInflater inflater = mActivity.getLayoutInflater();
						int n = 0;
						for (Actor actor : show.actors) {
							final View view = inflater.inflate(R.layout.actor_item, null);
							
							((TextView)view.findViewById(R.id.actor_name)).setText(actor.name);
							((TextView)view.findViewById(R.id.actor_role)).setText("as " + actor.role);
							ImageButton img = ((ImageButton)view.findViewById(R.id.actor_image));
							mShowManager.getCover(new DataResponse<Bitmap>() {
								public void run() {
									if (value != null) {
										((ImageButton)view.findViewById(R.id.actor_image)).setImageBitmap(value);
									}
								}
							}, actor, ThumbSize.SMALL, null, mActivity.getApplicationContext(), false);
							
							img.setTag(actor);
							img.setOnClickListener(new OnClickListener() {
								public void onClick(View v) {
									Intent nextActivity;
									Actor actor = (Actor)v.getTag();
									nextActivity = new Intent(view.getContext(), ListActivity.class);
									nextActivity.putExtra(ListController.EXTRA_LIST_CONTROLLER, new TvShowListController());
									nextActivity.putExtra(ListController.EXTRA_ACTOR, actor);
									mActivity.startActivity(nextActivity);
								}
							});
							dataLayout.addView(view);
							n++;
						}
					}
				}
			}, mShow, mActivity.getApplicationContext());
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
		mTvShowDetailsController.onActivityResume(this);
		mConfigurationManager.onActivityResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mTvShowDetailsController.onActivityPause();
		mConfigurationManager.onActivityPause();
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean handled =  mKeyTracker.doKeyUp(keyCode, event);
		return handled || super.onKeyUp(keyCode, event);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		IEventClientManager client = ManagerFactory.getEventClientManager(mTvShowDetailsController);
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
		boolean handled =  mKeyTracker.doKeyDown(keyCode, event);
		return handled || super.onKeyDown(keyCode, event);
	}
}
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
import org.xbmc.android.remote.presentation.controller.MovieListController;
import org.xbmc.android.util.KeyTracker;
import org.xbmc.android.util.OnLongPressBackKeyTracker;
import org.xbmc.android.util.KeyTracker.Stage;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.business.IVideoManager;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Movie;
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
import android.widget.Toast;

public class MovieDetailsActivity extends Activity {
	
	private static final String NO_DATA = "-";
	
    private ConfigurationManager mConfigurationManager;
    private MovieDetailsController mMovieDetailsController;

	private KeyTracker mKeyTracker;
    
    private static final int[] sStarImages = { R.drawable.stars_0, R.drawable.stars_1, R.drawable.stars_2, R.drawable.stars_3, R.drawable.stars_4, R.drawable.stars_5, R.drawable.stars_6, R.drawable.stars_7, R.drawable.stars_8, R.drawable.stars_9, R.drawable.stars_10 };
	
    public MovieDetailsActivity() {
    	mKeyTracker = new KeyTracker(new OnLongPressBackKeyTracker() {

			@Override
			public void onLongPressBack(int keyCode, KeyEvent event,
					Stage stage, int duration) {
				Intent intent = new Intent(MovieDetailsActivity.this, HomeActivity.class);
				intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}

			@Override
			public void onShortPressBack(int keyCode, KeyEvent event,
					Stage stage, int duration) {
				MovieDetailsActivity.super.onKeyDown(keyCode, event);
			}
			
		});
	}
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.moviedetails);
		
		// remove nasty top fading edge
		FrameLayout topFrame = (FrameLayout)findViewById(android.R.id.content);
		topFrame.setForeground(null);
		
		final Movie movie = (Movie)getIntent().getSerializableExtra(ListController.EXTRA_MOVIE);
		mMovieDetailsController = new MovieDetailsController(this, movie);
		
		((TextView)findViewById(R.id.titlebar_text)).setText(movie.getName());
		
		Log.i("MovieDetailsActivity", "rating = " + movie.rating + ", index = " + ((int)Math.round(movie.rating % 10)) + ".");
		if (movie.rating > -1) {
			((ImageView)findViewById(R.id.moviedetails_rating_stars)).setImageResource(sStarImages[(int)Math.round(movie.rating % 10)]);
		}
		((TextView)findViewById(R.id.moviedetails_director)).setText(movie.director);
		((TextView)findViewById(R.id.moviedetails_genre)).setText(movie.genres);
		((TextView)findViewById(R.id.moviedetails_runtime)).setText(movie.runtime);
		((TextView)findViewById(R.id.moviedetails_rating)).setText(String.valueOf(movie.rating));
		
		mMovieDetailsController.setupPlayButton((Button)findViewById(R.id.moviedetails_playbutton));
		mMovieDetailsController.loadCover((ImageView)findViewById(R.id.moviedetails_poster));
		mMovieDetailsController.updateMovieDetails(new Handler(),
				(TextView)findViewById(R.id.moviedetails_rating_numvotes),
				(TextView)findViewById(R.id.moviedetails_studio),
				(TextView)findViewById(R.id.moviedetails_plot),
				(TextView)findViewById(R.id.moviedetails_parental),
				(Button)findViewById(R.id.moviedetails_trailerbutton),
				(LinearLayout)findViewById(R.id.moviedetails_datalayout));
		
		mConfigurationManager = ConfigurationManager.getInstance(this);
	}
	
	private static class MovieDetailsController extends AbstractController implements INotifiableController, IController {
		
		private IVideoManager mVideoManager;
		private IControlManager mControlManager;
		private final Movie mMovie;
		
		MovieDetailsController(Activity activity, Movie movie) {
			super.onCreate(activity, new Handler());
			mActivity = activity;
			mMovie = movie;
			mVideoManager = ManagerFactory.getVideoManager(this);
			mControlManager = ManagerFactory.getControlManager(this);
		}
		
		public void setupPlayButton(Button button) {
			button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					mControlManager.playFile(new DataResponse<Boolean>() {
						public void run() {
							if (value) {
								mActivity.startActivity(new Intent(mActivity, NowPlayingActivity.class));
							}
						}
					}, mMovie.getPath(), mActivity.getApplicationContext());
				}
			});
		}
		
		public void loadCover(final ImageView imageView) {
			mVideoManager.getCover(new DataResponse<Bitmap>() {
				public void run() {
					if (value == null) {
						imageView.setImageResource(R.drawable.nocover);
					} else {
						imageView.setImageBitmap(value);
					}
				}
			}, mMovie, ThumbSize.BIG, null, mActivity.getApplicationContext(), false);
		}
		
		public void updateMovieDetails(final Handler handler, final TextView numVotesView, final TextView studioView, final TextView plotView, final TextView parentalView, final Button trailerButton, final LinearLayout dataLayout) {
			mVideoManager.updateMovieDetails(new DataResponse<Movie>() {
				public void run() {
					final Movie movie = value;
					numVotesView.setText(movie.numVotes > 0 ? " (" + movie.numVotes + " votes)" : "");
					studioView.setText(movie.studio.equals("") ? NO_DATA : movie.studio);
					plotView.setText(movie.plot.equals("") ? NO_DATA : movie.plot);
					parentalView.setText(movie.rated.equals("") ? NO_DATA : movie.rated);
					if (movie.trailerUrl != null && !movie.trailerUrl.equals("")) {
						trailerButton.setEnabled(true);
						trailerButton.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								mControlManager.playFile(new DataResponse<Boolean>() {
									public void run() {
										if (value) {
											Toast toast = Toast.makeText(mActivity,  "Playing trailer for \"" + movie.getName() + "\"...", Toast.LENGTH_LONG);
											toast.show();
										}
									}
								}, movie.trailerUrl, mActivity.getApplicationContext());
							}
						});
					}
					
					if (movie.actors != null) {
						final LayoutInflater inflater = mActivity.getLayoutInflater();
						int n = 0;
						for (Actor actor : movie.actors) {
							final View view = inflater.inflate(R.layout.actor_item, null);
							
							((TextView)view.findViewById(R.id.actor_name)).setText(actor.name);
							((TextView)view.findViewById(R.id.actor_role)).setText("as " + actor.role);
							ImageButton img = ((ImageButton)view.findViewById(R.id.actor_image));
							mVideoManager.getCover(new DataResponse<Bitmap>() {
								public void run() {
									if (value != null) {
										handler.post(new Runnable() {
											public void run() {
												((ImageButton)view.findViewById(R.id.actor_image)).setImageBitmap(value);
											}
										});
									}
								}
							}, actor, ThumbSize.SMALL, null, mActivity.getApplicationContext(), false);
							
							img.setTag(actor);
							img.setOnClickListener(new OnClickListener() {
								public void onClick(View v) {
									Intent nextActivity;
									Actor actor = (Actor)v.getTag();
									nextActivity = new Intent(view.getContext(), ListActivity.class);
									nextActivity.putExtra(ListController.EXTRA_LIST_CONTROLLER, new MovieListController());
									nextActivity.putExtra(ListController.EXTRA_ACTOR, actor);
									mActivity.startActivity(nextActivity);
								}
							});
							dataLayout.addView(view);
							n++;
						}
					}
				}
			}, mMovie, mActivity.getApplicationContext());
		}

		public void onActivityPause() {
			mVideoManager.setController(null);
			mVideoManager.postActivity();
			mControlManager.setController(null);
		}

		public void onActivityResume(Activity activity) {
			mVideoManager.setController(this);
			mControlManager.setController(this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMovieDetailsController.onActivityResume(this);
		mConfigurationManager.onActivityResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mMovieDetailsController.onActivityPause();
		mConfigurationManager.onActivityPause();
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean handled =  mKeyTracker.doKeyUp(keyCode, event);
		return handled || super.onKeyUp(keyCode, event);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		IEventClientManager client = ManagerFactory.getEventClientManager(mMovieDetailsController);
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
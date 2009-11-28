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

import org.xbmc.android.remote.ConfigurationManager;
import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.ManagerThread;
import org.xbmc.android.remote.presentation.controller.ListController;
import org.xbmc.android.remote.presentation.controller.MovieListController;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.api.business.DataResponse;
import org.xbmc.eventclient.ButtonCodes;
import org.xbmc.eventclient.EventClient;
import org.xbmc.httpapi.data.Actor;
import org.xbmc.httpapi.data.Movie;
import org.xbmc.httpapi.type.ThumbSize;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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
    
    private static final int[] sStarImages = { R.drawable.stars_0, R.drawable.stars_1, R.drawable.stars_2, R.drawable.stars_3, R.drawable.stars_4, R.drawable.stars_5, R.drawable.stars_6, R.drawable.stars_7, R.drawable.stars_8, R.drawable.stars_9, R.drawable.stars_10 };
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.moviedetails);
		
		// remove nasty top fading edge
		FrameLayout topFrame = (FrameLayout)findViewById(android.R.id.content);
		topFrame.setForeground(null);
		
		final Movie movie = (Movie)getIntent().getSerializableExtra(ListController.EXTRA_MOVIE);
		((TextView)findViewById(R.id.titlebar_text)).setText(movie.getName());
		
		final ImageView posterView = ((ImageView)findViewById(R.id.moviedetails_poster));
		((ImageView)findViewById(R.id.moviedetails_rating_stars)).setImageResource(sStarImages[(int)Math.round(movie.rating % 10)]);
		((TextView)findViewById(R.id.moviedetails_director)).setText(movie.director);
		((TextView)findViewById(R.id.moviedetails_genre)).setText(movie.genres);
		((TextView)findViewById(R.id.moviedetails_runtime)).setText(movie.runtime);
		((TextView)findViewById(R.id.moviedetails_rating)).setText(String.valueOf(movie.rating));
		((Button)findViewById(R.id.moviedetails_playbutton)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ManagerThread.control().playFile(new DataResponse<Boolean>(MovieDetailsActivity.this) {
					public void run() {
						if (value) {
							mActivity.startActivity(new Intent(mActivity, NowPlayingActivity.class));
						}
					}
				}, movie.getPath());
			}
		});
		
		mConfigurationManager = ConfigurationManager.getInstance(this);
		mConfigurationManager.initKeyguard();
		
		// load the cover
		ManagerThread.video().getCover(new DataResponse<Bitmap>(this) {
			public void run() {
				if (value == null) {
					posterView.setImageResource(R.drawable.nocover);
				} else {
					posterView.setImageBitmap(value);
				}
			}
		}, movie, ThumbSize.BIG);
		
		ManagerThread.video().updateMovieDetails(new DataResponse<Movie>(this) {
			public void run() {
				final Movie movie = value;
				((TextView)findViewById(R.id.moviedetails_rating_numvotes)).setText(movie.numVotes > 0 ? " (" + movie.numVotes + " votes)" : "");
				((TextView)findViewById(R.id.moviedetails_studio)).setText(movie.studio.equals("") ? NO_DATA : movie.studio);
				((TextView)findViewById(R.id.moviedetails_plot)).setText(movie.plot.equals("") ? NO_DATA : movie.plot);
				((TextView)findViewById(R.id.moviedetails_parental)).setText(movie.rated.equals("") ? NO_DATA : movie.rated);
				if (movie.trailerUrl != null && !movie.trailerUrl.equals("")) {
					final Button trailerButton = (Button)findViewById(R.id.moviedetails_trailerbutton);
					trailerButton.setEnabled(true);
					trailerButton.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							ManagerThread.control().playFile(new DataResponse<Boolean>(MovieDetailsActivity.this) {
								public void run() {
									if (value) {
										Toast toast = Toast.makeText(MovieDetailsActivity.this,  "Playing trailer for \"" + movie.getName() + "\"...", Toast.LENGTH_LONG);
										toast.show();
									}
								}
							}, movie.trailerUrl);
						}
					});
				}
				
				if (movie.actors != null) {
					final LinearLayout dataLayout = ((LinearLayout)findViewById(R.id.moviedetails_datalayout));
					final LayoutInflater inflater = getLayoutInflater();
					int n = 0;
					for (Actor actor : movie.actors) {
						final View view = inflater.inflate(R.layout.actor_item, null);
						
						((TextView)view.findViewById(R.id.actor_name)).setText(actor.name);
						((TextView)view.findViewById(R.id.actor_role)).setText("as " + actor.role);
						ImageButton img = ((ImageButton)view.findViewById(R.id.actor_image));
						ManagerThread.video().getCover(new DataResponse<Bitmap>(MovieDetailsActivity.this, 0, R.drawable.person_small) {
							public void run() {
								if (value != null) {
									((ImageButton)view.findViewById(R.id.actor_image)).setImageBitmap(value);
								}
							}
						}, actor, ThumbSize.SMALL);
						
						img.setTag(actor);
						img.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								Intent nextActivity;
								Actor actor = (Actor)v.getTag();
								nextActivity = new Intent(view.getContext(), ListActivity.class);
								nextActivity.putExtra(ListController.EXTRA_LIST_LOGIC, new MovieListController());
								nextActivity.putExtra(ListController.EXTRA_ACTOR, actor);
								mActivity.startActivity(nextActivity);
							}
						});
						dataLayout.addView(view);
						n++;
					}
				}
			}
		}, movie);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mConfigurationManager.onActivityResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mConfigurationManager.onActivityPause();
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
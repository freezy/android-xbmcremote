package org.xbmc.api.object;

import java.util.List;

public class Season {

	public int number;
	public boolean watched = false;
	
	public TvShow show = null;
	public List<Episode> episodes = null;
	
	public Season(int number, boolean watched, TvShow show) {
		this.number = number;
		this.watched = watched;
		this.show = show;
	}
}

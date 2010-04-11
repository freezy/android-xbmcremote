package org.xbmc.api.object;

import java.io.Serializable;
import java.util.List;

public class Season implements Serializable {

	public int number;
	public boolean watched = false;
	
	public TvShow show = null;
	public List<Episode> episodes = null;
	
	public Season(int number, boolean watched, TvShow show) {
		this.number = number;
		this.watched = watched;
		this.show = show;
	}
	private static final long serialVersionUID = -7652780720536304140L;
}

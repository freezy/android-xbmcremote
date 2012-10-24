package org.xbmc.api.type;

public class Sort {
	public int sortBy;
	public String sortOrder; 
	public boolean ignoreArticle;
	
	public Sort(int sortBy, String sortOrder, boolean ignoreArticle) {
		this.sortBy = sortBy;
		this.sortOrder = sortOrder;
		this.ignoreArticle = ignoreArticle;
	}
}

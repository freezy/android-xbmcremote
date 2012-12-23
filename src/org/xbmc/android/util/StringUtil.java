package org.xbmc.android.util;

import java.util.List;

public class StringUtil {
	
	public static String join(String separator, List<String> data) {
		StringBuilder sb = new StringBuilder();
		for(String item : data){
		    if(sb.length()>0)sb.append(separator);
		    sb.append(item);
		}
		return sb.toString();
	}
}

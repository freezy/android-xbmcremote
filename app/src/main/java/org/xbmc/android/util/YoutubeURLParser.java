package org.xbmc.android.util;

import android.net.Uri;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeURLParser {
	public static String parseYoutubeURL(Uri playuri) {
		if (playuri.getHost().endsWith("youtube.com") || playuri.getHost().endsWith("youtu.be")) {
			// We'll need to get the v= parameter from the URL and use
			// that to send to XBMC
			final Pattern pattern = Pattern.compile("(?:https?:\\/\\/)?(?:www\\.)?youtu(?:.be\\/|be\\" +
					".com\\/watch\\?v=)([\\w-]{11})", Pattern.CASE_INSENSITIVE);
//			final Pattern pattern = Pattern.compile("^http(:?s)?:\\/\\/(?:www\\.)?(?:youtube\\.com|youtu\\.be)
// \\/watch\\?(?=.*v=([\\w-]+))(?:\\S+)?$", Pattern.CASE_INSENSITIVE);
//			final Pattern pattern = Pattern.compile(".*v=([a-z0-9_\\-]+)(?:&.)*", Pattern.CASE_INSENSITIVE);
			final Matcher matcher = pattern.matcher(playuri.toString());
			if (matcher.matches()) {
				return matcher.group(1);
			}
		}
		return null;
	}
}

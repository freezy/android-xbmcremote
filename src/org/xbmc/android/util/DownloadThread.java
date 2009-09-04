package org.xbmc.android.util;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class DownloadThread extends Thread {
	String[] paths;
	DownloadCallback callback;
	public DownloadThread(String[] paths, DownloadCallback callback) {
		this.paths = paths;
		this.callback = callback;
	}
	@Override
	public void run() {
		for (int i = 0; i < paths.length && !isInterrupted(); i++) {
			callback.onDownloadDone(Download(paths[i]));
		}
	}
	private byte[] Download(String pathToDownload) {
		try {
			final URL url = new URL(pathToDownload);
			final URLConnection uc = url.openConnection();
			
			final BufferedReader rd = new BufferedReader(new InputStreamReader(uc.getInputStream()), 8192);
			
			final StringBuilder sb = new StringBuilder();
			String line = "";
			while ((line = rd.readLine()) != null) {    
				sb.append(line);
			}
			
			rd.close();
			return Base64.decode(sb.toString().replace("<html>", "").replace("</html>", ""));
		} catch (Exception e) {
			return null;
		}
	}

}

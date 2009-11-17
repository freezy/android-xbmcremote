/*
 * Copyright (C) 2008 Romain Guy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xbmc.android.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.xbmc.httpapi.data.ICoverArt;
import org.xbmc.httpapi.type.MediaType;
import org.xbmc.httpapi.type.ThumbSize;

import android.graphics.Bitmap;
import android.util.Log;

public abstract class ImportUtilities {
	
	private static final String TAG = "ImportUtilities";
    private static final String CACHE_DIRECTORY = "xbmc";
    private static final double POSTER_AR = 1.8766756032171581769436997319035;

    public static File getCacheDirectory(String type, int size) {
        return IOUtilities.getExternalFile(CACHE_DIRECTORY + type + ThumbSize.getDir(size));
    }

    public static Bitmap addCoverToCache(ICoverArt cover, Bitmap bitmap, int thumbSize) {
    	Bitmap sizeToReturn = null;
    	File cacheDirectory;
    	for (int currentThumbSize : ThumbSize.values()) {
    		try {
    			cacheDirectory = ensureCache(MediaType.getArtFolder(cover.getMediaType()), currentThumbSize);
    		} catch (IOException e) {
    			return null;
    		}
    		File coverFile = new File(cacheDirectory, Crc32.formatAsHexLowerCase(cover.getCrc()));
    		FileOutputStream out = null;
    		try {
    			out = new FileOutputStream(coverFile);
    			int width = 0;
    			int height = 0;
    			final double ar = ((double)bitmap.getWidth()) / ((double)bitmap.getHeight());
    			switch (cover.getMediaType()) {
    				default:
    				case MediaType.PICTURES:
    				case MediaType.MUSIC:
    					if (ar < 1) {
    						width = ThumbSize.getPixel(currentThumbSize);
    						height = (int)(width / ar); 
    					} else {
    						height = ThumbSize.getPixel(currentThumbSize);
    						width = (int)(height * ar); 
    					}
    					break;
    				case MediaType.VIDEO:
    					if (ar > 0.98 && ar < 1.02) { 	// square
    						Log.i(TAG, "Format: SQUARE");
    						width = ThumbSize.getPixel(currentThumbSize);
    						height = ThumbSize.getPixel(currentThumbSize);
    					} else if (ar < 1) {			// portrait
    						Log.i(TAG, "Format: PORTRAIT");
    						width = ThumbSize.getPixel(currentThumbSize);
    						final int ph = (int)(POSTER_AR * width);
    						height = (int)(width / ar); 
    						if (height < ph) {
    							height = ph;
    							width = (int)(height * ar);
    						}
    					} else if (ar < 2) {			// landscape 16:9
    						Log.i(TAG, "Format: LANDSCAPE 16:9");
    						height = ThumbSize.getPixel(currentThumbSize);
    						width = (int)(height * ar); 
    					} else if (ar > 5) {			// wide banner
    						Log.i(TAG, "Format: BANNER");
    						width = ThumbSize.getPixel(currentThumbSize) * 2;
    						height = (int)(width / ar); 
    					} else {						// anything between wide banner and landscape 16:9
    						Log.i(TAG, "Format: BIZARRE");
    						height = ThumbSize.getPixel(currentThumbSize);
    						width = (int)(height * ar); 
    					}
    					break;
    			}
    			Log.i(TAG, "Resizing to " + width + "x" + height);
    			final Bitmap resized = Bitmap.createScaledBitmap(bitmap, width, height, true);
    			resized.compress(Bitmap.CompressFormat.PNG, 100, out);
    			if (thumbSize == currentThumbSize) {
    				sizeToReturn = resized;
    			}
    		} catch (FileNotFoundException e) {
    			return null;
    		} finally {
    			IOUtilities.closeStream(out);
    		}
    	}
        return sizeToReturn;
    }

    private static File ensureCache(String type, int size) throws IOException {
        File cacheDirectory = getCacheDirectory(type, size);
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs();
            new File(cacheDirectory, ".nomedia").createNewFile();
        }
        return cacheDirectory;
    }
    
    public static void purgeCache() {
    	final int size[] = ThumbSize.values();
    	final int[] mediaTypes = MediaType.getTypes();
    	for (int i = 0; i < mediaTypes.length; i++) {
    		String folder = MediaType.getArtFolder(mediaTypes[i]);
    		for (int j = 0; j < size.length; j++) {
    			File cacheDirectory = getCacheDirectory(folder, size[j]);
    			if (cacheDirectory.exists() && cacheDirectory.isDirectory()) {
    				for (File file : cacheDirectory.listFiles()) {
    					file.delete();
    				}
    			}
    		}
    	}
    }
}

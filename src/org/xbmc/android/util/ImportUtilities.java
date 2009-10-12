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
import org.xbmc.httpapi.type.ThumbSize;

import android.graphics.Bitmap;

public final class ImportUtilities {
    private static final String CACHE_DIRECTORY = "xbmc";

    private ImportUtilities() {
    }

    public static File getCacheDirectory(String type, ThumbSize size) {
        return IOUtilities.getExternalFile(CACHE_DIRECTORY + type + size.getDir());
    }

    public static boolean addCoverToCache(ICoverArt art, Bitmap bitmap) {
    	final ThumbSize size[] = { ThumbSize.small, ThumbSize.medium, ThumbSize.big };
    	File cacheDirectory;
    	for (int i = 0; i < size.length; i++) {
    		try {
    			cacheDirectory = ensureCache(art.getArtFolder(), size[i]);
    		} catch (IOException e) {
    			return false;
    		}
    		File coverFile = new File(cacheDirectory, String.format("%08x", art.getCrc()).toLowerCase());
    		FileOutputStream out = null;
    		try {
    			out = new FileOutputStream(coverFile);
    			final Bitmap resized = Bitmap.createScaledBitmap(bitmap, size[i].getPixel(), size[i].getPixel(), true);
    			resized.compress(Bitmap.CompressFormat.PNG, 100, out);
    		} catch (FileNotFoundException e) {
    			return false;
    		} finally {
    			IOUtilities.closeStream(out);
    		}
    	}
        return true;
    }

    private static File ensureCache(String type, ThumbSize size) throws IOException {
        File cacheDirectory = getCacheDirectory(type, size);
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs();
            new File(cacheDirectory, ".nomedia").createNewFile();
        }
        return cacheDirectory;
    }
    
    public static void purgeCache() {
    	final ThumbSize size[] = { ThumbSize.small, ThumbSize.medium, ThumbSize.big };
    	final String type[] = { "/music", "/video" };
    	for (int i = 0; i < type.length; i++) {
    		for (int j = 0; j < size.length; j++) {
    			File cacheDirectory = getCacheDirectory(type[i], size[j]);
    			if (cacheDirectory.exists() && cacheDirectory.isDirectory()) {
    				for (File file : cacheDirectory.listFiles()) {
    					file.delete();
    				}
    			}
    		}
    	}
    }
}

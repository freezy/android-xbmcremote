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

import android.os.Environment;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.Closeable;
import java.io.File;

public final class IOUtilities {
    private static final String LOG_TAG = "IOUtilities";

    public static final int IO_BUFFER_SIZE = 4 * 1024;

    public static File getExternalFile(String file) {
        return new File(Environment.getExternalStorageDirectory(), file);
    }

    /**
     * Copy the content of the input stream into the output stream, using a temporary
     * byte array buffer whose size is defined by {@link #IO_BUFFER_SIZE}.
     *
     * @param in The input stream to copy from.
     * @param out The output stream to copy to.
     *
     * @throws java.io.IOException If any error occurs during the copy.
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[IO_BUFFER_SIZE];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }

    /**
     * Closes the specified stream.
     *
     * @param stream The stream to close.
     */
    public static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                android.util.Log.e(LOG_TAG, "Could not close stream", e);
            }
        }
    }
}

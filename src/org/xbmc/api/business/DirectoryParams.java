package org.xbmc.api.business;

import android.content.Context;

import org.xbmc.api.object.FileLocation;

import java.util.ArrayList;

public class DirectoryParams {
    private final DataResponse<ArrayList<FileLocation>> response;
    private final String path;
    private final Context context;
    private final int mediaType;

    /**
     * @param response Response object
     * @param path     Path to the directory
     */
    public DirectoryParams(DataResponse<ArrayList<FileLocation>> response, String path, Context context, int mediaType) {
        this.response = response;
        this.path = path;
        this.context = context;
        this.mediaType = mediaType;
    }

    public DataResponse<ArrayList<FileLocation>> getResponse() {
        return response;
    }

    public String getPath() {
        return path;
    }

    public Context getContext() {
        return context;
    }

    public int getMediaType() {
        return mediaType;
    }
}

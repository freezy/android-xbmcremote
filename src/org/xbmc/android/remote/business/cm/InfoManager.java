package org.xbmc.android.remote.business.cm;

import java.util.ArrayList;

import org.xbmc.android.jsonrpc.api.AbstractCall;
import org.xbmc.android.jsonrpc.api.call.Application;
import org.xbmc.android.jsonrpc.api.call.Files;
import org.xbmc.android.jsonrpc.api.model.ApplicationModel;
import org.xbmc.android.jsonrpc.api.model.ApplicationModel.PropertyValue;
import org.xbmc.android.jsonrpc.api.model.ApplicationModel.PropertyValue.Version;
import org.xbmc.android.jsonrpc.api.model.ListModel;
import org.xbmc.android.jsonrpc.api.model.ListModel.FileItem;
import org.xbmc.android.jsonrpc.io.ApiCallback;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IInfoManager;
import org.xbmc.api.object.FileLocation;
import org.xbmc.api.type.MediaType;

import android.content.Context;

public class InfoManager extends AbstractManager implements IInfoManager {
	private int apiVersion = -1;

	public void getSystemVersion(final DataResponse<String> response,
			Context context) {
		getConnectionManager(context).call(
				new Application.GetProperties(
						ApplicationModel.PropertyValue.VERSION),
				new ApiCallback<ApplicationModel.PropertyValue>() {

					public void onResponse(AbstractCall<PropertyValue> apiCall) {
						PropertyValue result = apiCall.getResult();
						Version version = result.version;
						response.value = version.major + "." + version.minor
								+ " " + version.tag;
						InfoManager.this.onFinish(response);
					}

					public void onError(int code, String message, String hint) {
						InfoManager.this.onError(new Exception(message));
					}

				});

	}

	public int getAPIVersion(Context context) {
		return org.xbmc.android.jsonrpc.api.Version.get().getBranch().ordinal();
	}

	public void getShares(DataResponse<ArrayList<FileLocation>> response,
			int mediaType, Context context) {
		final String media = MediaType.getName(mediaType);
		
		call(new Files.GetSources(media,
				null, getSort(ListModel.Sort.Method.FILE)),
				new ApiHandler<ArrayList<FileLocation>, ListModel.SourceItem>() {
					@Override
					public ArrayList<FileLocation> handleResponse(
							AbstractCall<ListModel.SourceItem> apiCall) {
						ArrayList<FileLocation> result = new ArrayList<FileLocation>();
						
						ArrayList<ListModel.SourceItem> items = apiCall.getResults();
						for(ListModel.SourceItem item : items) {
							FileLocation location = new FileLocation(item);
							location.isDirectory = true;
							result.add(location);
						}
						if("video".equals(media) || "music".equals(media)) {
							FileLocation location = new FileLocation(("music".equals(media) ? "Music" : "Video") + " Playlists", "special://" + media + "playlists/");
							location.isDirectory = true;
							result.add(location);
						}
						
						
						return result;
					}
				}, response, context);
		


	}

	public void getDirectory(DataResponse<ArrayList<FileLocation>> response,
			String path, Context context, int mediaType) {
		call(new Files.GetDirectory(path, MediaType.getName(mediaType),
				getSort(ListModel.Sort.Method.FILE), FileItem.MIMETYPE,
				FileItem.FILE),
				new ApiHandler<ArrayList<FileLocation>, FileItem>() {
					@Override
					public ArrayList<FileLocation> handleResponse(
							AbstractCall<FileItem> apiCall) {
						ArrayList<FileLocation> result = new ArrayList<FileLocation>();
						
						ArrayList<FileItem> items = apiCall.getResults();
						for(FileItem item : items) {
							result.add(new FileLocation(item));
						}
						
						return result;
					}
				}, response, context);

	}

	public void getGuiSettingInt(DataResponse<Integer> response, int setting,
			Context context) {
		// TODO Auto-generated method stub

	}

	public void getGuiSettingBool(DataResponse<Boolean> response, int setting,
			Context context) {

	}

	public void setGuiSettingInt(DataResponse<Boolean> response, int field,
			int val, Context context) {
		// TODO Auto-generated method stub

	}

	public void setGuiSettingBool(DataResponse<Boolean> response, int field,
			boolean val, Context context) {
		// TODO Auto-generated method stub

	}

	public void getCurrentlyPlayingThumbURI(DataResponse<String> response,
			Context context) {
		
//		Integer player = getActivePlayerId(manager);
//		if (player == null) {
//			return null;
//		}
//
//		JsonNode result = mConnection.getJson(manager, "Player.GetItem", obj()
//				.p("playerid", player).p("properties", arr().add("thumbnail")));
//		JsonNode item = result.get("item");
//		if (item == null) {
//			return null;
//		}
//		String specialPath = item.get("thumbnail").getTextValue();
//		return mConnection.getVfsPath(specialPath);
		
		
	}

}

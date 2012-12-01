package org.xbmc.android.remote.business.cm;

import java.util.ArrayList;

import org.xbmc.android.jsonrpc.api.AbstractCall;
import org.xbmc.android.jsonrpc.api.call.Player;
import org.xbmc.android.jsonrpc.api.call.Player.GetActivePlayers;
import org.xbmc.android.jsonrpc.api.call.Player.GetActivePlayers.GetActivePlayersResult;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.data.IControlClient.ICurrentlyPlaying;
import org.xbmc.api.type.SeekType;

import android.content.Context;

public class ControlManager extends AbstractManager implements IControlManager {

	public void playFile(DataResponse<Boolean> response, String filename,
			Context context) {
		// TODO Auto-generated method stub

	}

	public void playFolder(DataResponse<Boolean> response, String foldername,
			String playlistType, Context context) {
		// TODO Auto-generated method stub

	}

	public void queueFolder(DataResponse<Boolean> response, String foldername,
			String playlistType, Context context) {
		// TODO Auto-generated method stub

	}

	public void playUrl(DataResponse<Boolean> response, String url,
			Context context) {
		// TODO Auto-generated method stub

	}

	public void playNext(DataResponse<Boolean> response, Context context) {
		// TODO Auto-generated method stub

	}

	public void addToPlaylist(DataResponse<Boolean> response,
			String fileOrFolder, Context context) {
		// TODO Auto-generated method stub

	}

	public void seek(DataResponse<Boolean> response, SeekType type,
			int progress, Context context) {
		// TODO Auto-generated method stub

	}

	public void updateLibrary(DataResponse<Boolean> response, String mediaType,
			Context context) {
		// TODO Auto-generated method stub

	}

	public void showPicture(DataResponse<Boolean> response, String filename,
			Context context) {
		// TODO Auto-generated method stub

	}

	public void getCurrentlyPlaying(DataResponse<ICurrentlyPlaying> response,
			Context context) {
		// TODO Auto-generated method stub

	}

	public void getPlaylistId(DataResponse<Integer> response, Context context) {
		call(new Player.GetActivePlayers(),
				new ApiHandler<Integer, GetActivePlayers.GetActivePlayersResult>() {
					@Override
					public Integer handleResponse(
							AbstractCall<GetActivePlayersResult> apiCall) {
						ArrayList<GetActivePlayersResult> results = apiCall.getResults();
						if(results.size() == 0) {
							return 0;
						}
						GetActivePlayersResult result = results.get(0);
						return result.playerid;
					}
				}, response, context);
	}

	public void setPlaylistId(DataResponse<Boolean> response, int id,
			Context context) {
		// TODO Auto-generated method stub

	}

	public void setPlaylistPos(DataResponse<Boolean> response, int position,
			Context context) {
		// TODO Auto-generated method stub

	}

	public void clearPlaylist(DataResponse<Boolean> response,
			String playlistId, Context context) {
		// TODO Auto-generated method stub

	}

	public void setGuiSetting(DataResponse<Boolean> response, int setting,
			String value, Context context) {
		// TODO Auto-generated method stub

	}

	public void getVolume(DataResponse<Integer> response, Context context) {
		// TODO Auto-generated method stub

	}

	public void sendText(DataResponse<Boolean> response, String text,
			Context context) {
		// TODO Auto-generated method stub

	}

}

package org.xbmc.jsonrpc.client;

import org.codehaus.jackson.JsonNode;
import org.xbmc.api.data.IControlClient.ICurrentlyPlaying;
import org.xbmc.api.info.PlayStatus;
import org.xbmc.api.type.MediaType;
import org.xbmc.jsonrpc.client.Client.ObjNode;

public class PictureClient {

	static ICurrentlyPlaying getCurrentlyPlaying(final Integer currentPlayer, final JsonNode item, final ObjNode props) {
		return new ICurrentlyPlaying() {
			private static final long serialVersionUID = 5036994329211476713L;
			
			public int getMediaType() {
				return MediaType.PICTURES;
			}

			public String getAlbum() {
				String[] path = item.get("file").getTextValue().replaceAll("\\\\", "/").split("/");
				return path[path.length - 2];
			}

			public String getArtist() {
				return "Image";
			}

			public int getDuration() {
				return 0;
			}

			public String getFilename() {
				return item.get("file").getTextValue();
			}

			public float getPercentage() {
				// TODO Auto-generated method stub
				return 0;
			}

			public int getPlayStatus() {
				if(currentPlayer == null) {
					return PlayStatus.STOPPED;
				}
				return PlayStatus.PLAYING;
			}

			public int getPlaylistPosition() {
				// TODO Auto-generated method stub
				return 0;
			}

			public int getTime() {
				return 0;
			}

			public String getTitle() {
				String[] path = item.get("file").getTextValue().split("/");
				return path[path.length - 1];
				//return map.get("Filename").substring(map.get("Filename").lastIndexOf("/") + 1);
			}

			public boolean isPlaying() {
				return true;
			}

			public int getHeight() {
				// TODO: is htere a way to get this info with the new api?
				return 0;
			}

			public int getWidth() {
				// TODO: is htere a way to get this info with the new api?
				return 0;
			}
			
			private int parseHeight(String resolution){
				String[] xy = resolution.split("x");
				if (xy.length != 2)
					return 0;
				try{
					return Integer.parseInt(xy[1].trim());
				} catch (NumberFormatException e) {
					return 0;
				}
			}
			private int parseWidth(String resolution){
				String[] xy = resolution.split("x");
				if (xy.length != 2)
					return 0;
				try{
					return Integer.parseInt(xy[0].trim());
				} catch (NumberFormatException e) {
					return 0;
				}
			}			
		};
	}

}

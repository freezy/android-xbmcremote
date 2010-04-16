/*
 *      Copyright (C) 2005-2010 Team XBMC
 *      http://xbmc.org
 *
 *  This Program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2, or (at your option)
 *  any later version.
 *
 *  This Program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XBMC Remote; see the file license.  If not, write to
 *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *  http://www.gnu.org/copyleft/gpl.html
 *
 */

package org.xbmc.jsonrpc;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * Wraps some calls in order to avoid JSON exceptions
 * @author Team XBMC
 */
public abstract class Helper {
	public final static ObjectMapper MAPPER = new ObjectMapper();
	
	public final static ObjectNode createObjectNode() {
		return MAPPER.createObjectNode();
	}
	public final static ArrayNode createArrayNode() {
		return MAPPER.createArrayNode();
	}
	
	public final static String getString(JsonNode obj, String key) {
		return getString(obj, key, "");
	}
	public final static String getString(JsonNode obj, String key, String ifNullResult) {
		return obj.get(key) == null ? ifNullResult : obj.get(key).getTextValue();
	}
	public final static int getInt(JsonNode obj, String key) {
		return obj.get(key) == null ? -1 : obj.get(key).getIntValue();
	}
}
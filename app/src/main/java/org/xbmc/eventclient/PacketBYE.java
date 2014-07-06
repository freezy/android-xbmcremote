package org.xbmc.eventclient;

/**
 * XBMC Event Client Class
 * <p/>
 * A BYE packet terminates the connection to XBMC.
 *
 * @author Stefan Agner
 */
public class PacketBYE extends Packet {

	/**
	 * A BYE packet terminates the connection to XBMC.
	 */
	public PacketBYE() {
		super(PT_BYE);
	}
}

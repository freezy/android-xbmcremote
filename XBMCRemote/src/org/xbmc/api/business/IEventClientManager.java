package org.xbmc.api.business;

import java.io.IOException;

/**
 * XBMC Event Client Class
 * 
 * Implements an XBMC-Client. This class can be used to implement your own
 * application which should act as a Input device for XBMC. Also starts a
 * Ping-Thread, which tells the XBMC EventServer that the client is alive.
 * Therefore if you close your application you SHOULD call stopClient()!
 * 
 * @author Team XBMC
 */
public interface IEventClientManager extends IManager {
	

	/**
	 * Displays a notification window in XBMC.
	 * 
	 * @param title    Message title
	 * @param message  The actual message
	 */
	public void sendNotification(String title, String message) throws IOException;
	public void sendNotification(String title, String message, byte icontype, byte[] icondata) throws IOException;

	/**
	 * Sends a Button event
	 * 
	 * @param code   Raw button code (default: 0)
	 * @param repeat This key press should repeat until released (default: 1) 
	 *               Note that queued pressed cannot repeat.
	 * @param down   If this is 1, it implies a press event, 0 implies a 
	 *               release event. (default: 1)
	 * @param queue  A queued key press means that the button event is executed
	 *               just once after which the next key press is processed. It 
	 *               can be used for macros. Currently there is no support for 
	 *               time delays between queued presses. (default: 0)
	 * @param amount Unimplemented for now; in the future it will be used for
	 *               specifying magnitude of analog key press events
	 * @param axis
	 */
	public void sendButton(short code, boolean repeat, boolean down, boolean queue, short amount, byte axis) throws IOException;

	/**
	 * Sends a Button event
	 * 
	 * @param map_name
	 *            A combination of map_name and button_name refers to a mapping
	 *            in the user's Keymap.xml or Lircmap.xml. map_name can be one
	 *            of the following:
	 *            <ul>
	 *            <li><code>KB</code> - Standard keyboard map (<code>&lt;keyboard&gt;</code> section)</li>
	 *            <li><code>XG</code> - Xbox gamepad map (<code>&lt;gamepad&gt;</code> section)</li>
	 *            <li><code>R1</code> - Xbox remote map (<code>&lt;remote&gt;</code> section)</li>
	 *            <li><code>R2</code> - Xbox universal remote map (<code>&lt;universalremote&gt;</code>
	 *            section)</li>
	 *            <li><code>LI:devicename</code> - LIRC remote map where <code>devicename</code> is
	 *            the actual device's name</li>
	 *            </ul>
	 * @param button_name
	 *            A button name defined in the map specified in map_name. For
	 *            example, if map_name is "KB" refering to the <keyboard>
	 *            section in Keymap.xml then, valid button_names include
	 *            "printscreen", "minus", "x", etc.
	 * @param repeat
	 *            This key press should repeat until released (default: 1) Note
	 *            that queued pressed cannot repeat.
	 * @param down
	 *            If this is 1, it implies a press event, 0 implies a release
	 *            event. (default: 1)
	 * @param queue
	 *            A queued key press means that the button event is executed
	 *            just once after which the next key press is processed. It can
	 *            be used for macros. Currently there is no support for time
	 *            delays between queued presses. (default: 0)
	 * @param amount
	 *            Unimplemented for now; in the future it will be used for
	 *            specifying magnitude of analog key press events
	 * @param axis
	 */
	public void sendButton(String map_name, String button_name, boolean repeat,
			boolean down, boolean queue, short amount, byte axis)
			throws IOException;

	/**
	 * Sets the mouse position in XBMC
	 * 
	 * @param x  Horizontal position ranging from 0 to 65535
	 * @param y  Vertical position ranging from 0 to 65535
	 */
	public void sendMouse(int x, int y) throws IOException;

	/**
	 * Tells XBMC to log the message to xbmc.log with the loglevel as specified.
	 * 
	 * @param loglevel
	 *            The log level, follows XBMC standard.
	 *            <ul>
	 *            <li>0 = DEBUG</li>
	 *            <li>1 = INFO</li>
	 *            <li>2 = NOTICE</li>
	 *            <li>3 = WARNING</li>
	 *            <li>4 = ERROR</li>
	 *            <li>5 = SEVERE</li>
	 *            </ul>
	 * @param logmessage
	 *            The message to log
	 */
	public void sendLog(byte loglevel, String logmessage) throws IOException;

	/**
	 * Tells XBMC to do the action specified, based on the type it knows were it
	 * needs to be sent.
	 * 
	 * @param actionmessage Actionmessage (as in scripting/skinning)
	 */
	public void sendAction(String actionmessage) throws IOException;

}
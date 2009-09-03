package org.xbmc.eventclient;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;

/**
 * XBMC Event Client Class
 * 
 * Implements an XBMC-Client. This class can be used to implement your own
 * application which should act as a Input device for XBMC. Also starts a
 * Ping-Thread, which tells the XBMC EventServer that the client is alive.
 * Therefore if you close your application you SHOULD call stopClient()!
 * 
 * 03.09.2009 freezy changed class name and member variables
 * 
 * @author Stefan Agner
 */
public class EventClient {
	
	private boolean mHasIcon = false;
	private String mDeviceName;
	private PingThread mPingThread;
	private byte mIconType = Packet.ICON_PNG;
	private byte[] mIconData;
	private InetAddress mHostAddress;
	private int mHostPort;

	/**
	 * Starts a XBMC EventClient.
	 * 
	 * @param hostAddress  Address of the Host running XBMC
	 * @param hostPort     Port of the Host running XBMC (default 9777)
	 * @param deviceName   Name of the Device
	 * @param iconFile     Path to the Iconfile (PNG, JPEG or GIF)
	 * @throws IOException
	 */
	public EventClient(InetAddress hostAddress, int hostPort, String deviceName, String iconFile) throws IOException {
		
		byte iconType = Packet.ICON_PNG;

		// Assume png as icon type
		if (iconFile.toLowerCase().endsWith(".jpeg"))
			iconType = Packet.ICON_JPEG;
		if (iconFile.toLowerCase().endsWith(".jpg"))
			iconType = Packet.ICON_JPEG;
		if (iconFile.toLowerCase().endsWith(".gif"))
			iconType = Packet.ICON_GIF;

		// Read the icon file to the byte array...
		FileInputStream iconFileStream = new FileInputStream(iconFile);
		byte[] iconData = new byte[iconFileStream.available()];
		iconFileStream.read(iconData);

		mHasIcon = true;

		// Call start-Method...
		startClient(hostAddress, hostPort, deviceName, iconType, iconData);
	}

	/**
	 * Starts a XBMC EventClient.
	 * 
	 * @param hostAddress  Address of the Host running XBMC
	 * @param hostPort     Port of the Host running XBMC (default 9777)
	 * @param deviceName   Name of the Device
	 * @param iconType     Type of the icon file (see Packet.ICON_PNG, 
	 *                     Packet.ICON_JPEG or Packet.ICON_GIF)
	 * @param iconData     The icon itself as a Byte-Array
	 * @throws IOException
	 */
	public EventClient(InetAddress hostAddress, int hostPort, String deviceName, byte iconType, byte[] iconData) throws IOException {
		mHasIcon = true;
		startClient(hostAddress, hostPort, deviceName, iconType, iconData);
	}

	/**
	 * Starts a XBMC EventClient without an icon.
	 * 
	 * @param hostAddress  Address of the Host running XBMC
	 * @param hostPort     Port of the Host running XBMC (default 9777)
	 * @param deviceName   Name of the Device
	 * @throws IOException
	 */
	public EventClient(InetAddress hostAddress, int hostPort, String deviceName)
			throws IOException {
		mHasIcon = false;
		byte iconType = Packet.ICON_NONE;
		byte[] iconData = null;
		startClient(hostAddress, hostPort, deviceName, iconType, iconData);
	}

	/**
	 * Starts a XBMC EventClient.
	 * 
	 * @param hostAddress  Address of the Host running XBMC
	 * @param hostPort     Port of the Host running XBMC (default 9777)
	 * @param deviceName   Name of the Device
	 * @param iconType     Type of the icon file (see Packet.ICON_PNG, 
	 *                     Packet.ICON_JPEG or Packet.ICON_GIF)
	 * @param iconData     The icon itself as a Byte-Array
	 * @throws IOException
	 */
	private void startClient(InetAddress hostAddress, int hostPort, String deviceName, byte iconType, byte[] iconData) throws IOException {

		// Save host address and port
		this.mHostAddress = hostAddress;
		this.mHostPort = hostPort;
		this.mDeviceName = deviceName;

		this.mIconType = iconType;
		this.mIconData = iconData;

		// Send Hello Packet...
		PacketHELO p;
		if (mHasIcon)
			p = new PacketHELO(deviceName, iconType, iconData);
		else
			p = new PacketHELO(deviceName);

		p.send(hostAddress, hostPort);

		// Start Thread (for Ping packets...)
		mPingThread = new PingThread(hostAddress, hostPort, 20000);
		mPingThread.start();
	}

	/**
	 * Stops the XBMC EventClient (especially the Ping-Thread)
	 * 
	 * @throws IOException
	 */
	public void stopClient() throws IOException {
		// Stop Ping-Thread...
		mPingThread.giveup();
		mPingThread.interrupt();

		PacketBYE p = new PacketBYE();
		p.send(mHostAddress, mHostPort);
	}

	/**
	 * Displays a notification window in XBMC.
	 * 
	 * @param title    Message title
	 * @param message  The actual message
	 */
	public void sendNotification(String title, String message) throws IOException {
		PacketNOTIFICATION p;
		if (mHasIcon)
			p = new PacketNOTIFICATION(title, message, mIconType, mIconData);
		else
			p = new PacketNOTIFICATION(title, message);
		p.send(mHostAddress, mHostPort);
	}

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
	public void sendButton(short code, boolean repeat, boolean down,
			boolean queue, short amount, byte axis) throws IOException {
		
		PacketBUTTON p = new PacketBUTTON(code, repeat, down, queue, amount, axis);
		p.send(mHostAddress, mHostPort);
	}

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
			throws IOException {
		
		PacketBUTTON p = new PacketBUTTON(map_name, button_name, repeat, down, queue, amount, axis);
		p.send(mHostAddress, mHostPort);
	}

	/**
	 * Sets the mouse position in XBMC
	 * 
	 * @param x  Horizontal position ranging from 0 to 65535
	 * @param y  Vertical position ranging from 0 to 65535
	 */
	public void sendMouse(int x, int y) throws IOException {
		PacketMOUSE p = new PacketMOUSE(x, y);
		p.send(mHostAddress, mHostPort);
	}

	/**
	 * Sends a ping to the XBMC EventServer
	 * 
	 * @throws IOException
	 */
	public void ping() throws IOException {
		PacketPING p = new PacketPING();
		p.send(mHostAddress, mHostPort);
	}

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
	public void sendLog(byte loglevel, String logmessage) throws IOException {
		PacketLOG p = new PacketLOG(loglevel, logmessage);
		p.send(mHostAddress, mHostPort);
	}

	/**
	 * Tells XBMC to do the action specified, based on the type it knows were it
	 * needs to be sent.
	 * 
	 * @param actionmessage Actionmessage (as in scripting/skinning)
	 */
	public void sendAction(String actionmessage) throws IOException {
		PacketACTION p = new PacketACTION(actionmessage);
		p.send(mHostAddress, mHostPort);
	}

	/**
	 * Implements a PingThread which tells XBMC EventServer that the Client is
	 * alive (this should be done at least every 60 seconds!
	 * 
	 * @author Stefan Agner
	 */
	class PingThread extends Thread {
		private InetAddress mHostAddress;
		private int mHostPort;
		private int mSleepTime;
		private boolean mGiveup = false;

		public PingThread(InetAddress hostAddress, int hostPort, int sleepTime) {
			super("XBMC EventClient Ping-Thread");
			mHostAddress = hostAddress;
			mHostPort = hostPort;
			mSleepTime = sleepTime;
		}

		public void giveup() {
			mGiveup = true;
		}

		public void run() {
			while (!mGiveup) {
				try {
					PacketPING p = new PacketPING();
					p.send(mHostAddress, mHostPort);
				} catch (IOException e) {

					e.printStackTrace();
				}

				try {
					Thread.sleep(mSleepTime);
				} catch (InterruptedException e) {
				}
			}
		}
	}
}

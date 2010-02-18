package org.xbmc.httpapi;

public class WifiStateException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3588074771970912287L;
	private int state;
	
	public WifiStateException(int state) {
		this.state = state;
	}

	public WifiStateException(String detailMessage, int state) {
		super(detailMessage);
		this.state = state;
	}

	public WifiStateException(Throwable throwable, int state) {
		super(throwable);
		this.state = state;
	}

	public WifiStateException(String detailMessage, Throwable throwable, int state) {
		super(detailMessage, throwable);
		this.state = state;
	}

	public int getState() {
		return state;
	}
}

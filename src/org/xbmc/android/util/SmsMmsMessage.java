package org.xbmc.android.util;

import java.io.ByteArrayInputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.gsm.SmsMessage;
import android.text.format.DateUtils;

public class SmsMmsMessage {
	
	private static final String PREFIX = "net.everythingandroid.smspopup.";
	private static final String EXTRAS_FROM_ADDRESS = PREFIX + "EXTRAS_FROM_ADDRESS";
	private static final String EXTRAS_MESSAGE_BODY = PREFIX + "EXTRAS_MESSAGE_BODY";
	private static final String EXTRAS_TIMESTAMP = PREFIX + "EXTRAS_TIMESTAMP";
	private static final String EXTRAS_UNREAD_COUNT = PREFIX + "EXTRAS_UNREAD_COUNT";
	private static final String EXTRAS_THREAD_ID = PREFIX + "EXTRAS_THREAD_ID";
	private static final String EXTRAS_CONTACT_ID = PREFIX + "EXTRAS_CONTACT_ID";
	private static final String EXTRAS_CONTACT_NAME = PREFIX + "EXTRAS_CONTACT_NAME";
	private static final String EXTRAS_CONTACT_PHOTO = PREFIX + "EXTRAS_CONTACT_PHOTO";
	private static final String EXTRAS_MESSAGE_TYPE = PREFIX + "EXTRAS_MESSAGE_TYPE";
	private static final String EXTRAS_MESSAGE_ID = PREFIX + "EXTRAS_MESSAGE_ID";
	public static final String EXTRAS_NOTIFY = PREFIX + "EXTRAS_NOTIFY";
	public static final String EXTRAS_REMINDER_COUNT = PREFIX + "EXTRAS_REMINDER_COUNT";
	public static final String EXTRAS_REPLYING = PREFIX + "EXTRAS_REPLYING";
	public static final String EXTRAS_QUICKREPLY = PREFIX + "EXTRAS_QUICKREPLY";

	public static final int MESSAGE_TYPE_SMS = 0;
	public static final int MESSAGE_TYPE_MMS = 1;

	private Context mContext;

	final private String mFromAddress;
	final private String mMessageBody;
	final private long mTimestamp;
	final private int mUnreadCount;
	final private long mThreadId;
	private String mContactId;
	private String mContactName;
	final private byte[] mContactPhoto;
	final private int mMessageType;
	
	private boolean mNotify = true;
	private int mReminderCount = 0;
	private long mMessageId = 0;

	/**
	 * Construct SmsMmsMessage with minimal information - this is useful for
	 * when a raw SMS comes in which just contains address, body and timestamp.
	 * We must then look in the database for the rest of the information
	 */
	public SmsMmsMessage(Context _context, String _fromAddress, String _messageBody, long _timestamp, int _messageType) {
		
		mContext = _context;
		mFromAddress = _fromAddress;
		mMessageBody = _messageBody == null ? "" : _messageBody;
		mTimestamp = _timestamp;
		mMessageType = _messageType;

		mContactId = SmsPopupUtils.getPersonIdFromPhoneNumber(mContext, mFromAddress);
		mContactName = SmsPopupUtils.getPersonName(mContext, mContactId, mFromAddress);
		mContactPhoto = SmsPopupUtils.getPersonPhoto(mContext, mContactId);

		mUnreadCount = SmsPopupUtils.getUnreadMessagesCount(mContext, mTimestamp);
		mThreadId = SmsPopupUtils.getThreadIdFromAddress(mContext, mFromAddress);

		setMessageId();

		if (mContactName == null) {
			mContactName = mContext.getString(android.R.string.unknownName);
		}
	}

	/**
	 * Construct SmsMmsMessage for getMmsDetails() - info fetched from the MMS
	 * database table
	 */
	public SmsMmsMessage(Context _context, String _fromAddress,
			String _messageBody, long _timestamp, long _threadId,
			int _unreadCount, int _messageType) {
		
		mContext = _context;
		mFromAddress = _fromAddress;
		mMessageBody = _messageBody == null ? "" : _messageBody;;
		mTimestamp = _timestamp;
		mMessageType = _messageType;

		// TODO: I think contactId can come the MMS table, this would save
		// this database lookup
		mContactId = SmsPopupUtils.getPersonIdFromPhoneNumber(mContext, mFromAddress);

		mContactName = SmsPopupUtils.getPersonName(mContext, mContactId, mFromAddress);
		mContactPhoto = SmsPopupUtils.getPersonPhoto(mContext, mContactId);

		mUnreadCount = _unreadCount;
		mThreadId = _threadId;

		setMessageId();

		if (mContactName == null) {
			mContactName = mContext.getString(android.R.string.unknownName);
		}
	}

	/**
	 * Construct SmsMmsMessage for getSmsDetails() - info fetched from the SMS
	 * database table
	 */
	public SmsMmsMessage(Context _context, String _fromAddress,
			String _contactId, String _messageBody, long _timestamp,
			long _threadId, int _unreadCount, long _messageId, int _messageType) {
		mContext = _context;
		mFromAddress = _fromAddress;
		mMessageBody = _messageBody == null ? "" : _messageBody;;
		mTimestamp = _timestamp;
		mMessageType = _messageType;
		mContactId = _contactId;

		if ("0".equals(mContactId))
			mContactId = null;

		mContactName = SmsPopupUtils.getPersonName(mContext, mContactId, mFromAddress);
		mContactPhoto = SmsPopupUtils.getPersonPhoto(mContext, mContactId);

		mUnreadCount = _unreadCount;
		mThreadId = _threadId;

		mMessageId = _messageId;

		if (mContactName == null) {
			mContactName = mContext.getString(android.R.string.unknownName);
		}
	}

	/**
	 * Construct SmsMmsMessage from an extras bundle
	 */
	public SmsMmsMessage(Context _context, Bundle b) {
		mContext = _context;
		mFromAddress = b.getString(EXTRAS_FROM_ADDRESS);
		mMessageBody = b.getString(EXTRAS_MESSAGE_BODY) == null ? "" : b.getString(EXTRAS_MESSAGE_BODY);
		mTimestamp = b.getLong(EXTRAS_TIMESTAMP);
		mContactId = b.getString(EXTRAS_CONTACT_ID);
		mContactName = b.getString(EXTRAS_CONTACT_NAME);
		mContactPhoto = b.getByteArray(EXTRAS_CONTACT_PHOTO);
		mUnreadCount = b.getInt(EXTRAS_UNREAD_COUNT, 1);
		mThreadId = b.getLong(EXTRAS_THREAD_ID, 0);
		mMessageType = b.getInt(EXTRAS_MESSAGE_TYPE, MESSAGE_TYPE_SMS);
		mNotify = b.getBoolean(EXTRAS_NOTIFY, false);
		mReminderCount = b.getInt(EXTRAS_REMINDER_COUNT, 0);
		mMessageId = b.getLong(EXTRAS_MESSAGE_ID, 0);
	}

	/**
	 * Construct SmsMmsMessage by specifying all data, only used for testing the
	 * notification from the preferences screen
	 */
	public SmsMmsMessage(Context _context, String _fromAddress,
			String _messageBody, long _timestamp, String _contactId,
			String _contactName, byte[] _contactPhoto, int _unreadCount,
			long _threadId, int _messageType) {
		mContext = _context;
		mFromAddress = _fromAddress;
		mMessageBody = _messageBody == null ? "" : _messageBody;
		mTimestamp = _timestamp;
		mContactId = _contactId;
		mContactName = _contactName;
		mContactPhoto = _contactPhoto;
		mUnreadCount = _unreadCount;
		mThreadId = _threadId;
		mMessageType = _messageType;
	}

	/**
	 * Convert all SmsMmsMessage data to an extras bundle to send via an intent
	 */
	public Bundle toBundle() {
		Bundle b = new Bundle();
		b.putString(EXTRAS_FROM_ADDRESS, mFromAddress);
		b.putString(EXTRAS_MESSAGE_BODY, mMessageBody);
		b.putLong(EXTRAS_TIMESTAMP, mTimestamp);
		b.putString(EXTRAS_CONTACT_ID, mContactId);
		b.putString(EXTRAS_CONTACT_NAME, mContactName);
		b.putByteArray(EXTRAS_CONTACT_PHOTO, mContactPhoto);
		b.putInt(EXTRAS_UNREAD_COUNT, mUnreadCount);
		b.putLong(EXTRAS_THREAD_ID, mThreadId);
		b.putInt(EXTRAS_MESSAGE_TYPE, mMessageType);
		b.putBoolean(EXTRAS_NOTIFY, mNotify);
		b.putInt(EXTRAS_REMINDER_COUNT, mReminderCount);
		b.putLong(EXTRAS_MESSAGE_ID, mMessageId);
		return b;
	}

	public static SmsMmsMessage getSmsfromPDUs(Context context, Object[] pdus) {
		SmsMessage[] msgs = new SmsMessage[pdus.length];
		String from;
		StringBuilder body = new StringBuilder();
		long timestamp;
		int msgtype = MESSAGE_TYPE_SMS;
		// public SmsMmsMessage(Context _context, String _fromAddress, String
		// _messageBody,
		// long _timestamp, int _messageType)
		for (int i = 0; i < msgs.length; i++) {
			msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
		}
		SmsMessage firstMessage = msgs[0];
		for (SmsMessage currentMessage : msgs) {
			if (currentMessage.getDisplayOriginatingAddress().equals(firstMessage.getDisplayOriginatingAddress())) {
				body.append(currentMessage.getDisplayMessageBody());
			}
		}
		timestamp = firstMessage.getTimestampMillis();
		from = firstMessage.getDisplayOriginatingAddress();
		return new SmsMmsMessage(context, from, body.toString(), timestamp, msgtype);
	}

	public Bitmap getContactPhoto() {
		if (mContactPhoto == null)
			return null;
		return BitmapFactory.decodeStream(new ByteArrayInputStream(mContactPhoto));
	}

	public int getUnreadCount() {
		return mUnreadCount;
	}

	public long getTimestamp() {
		return mTimestamp;
	}

	public CharSequence getFormattedTimestamp() {
		/*
		 * No need for my own format function now, the 1.5 SDK has this built in
		 * (this will detect the system settings and return the correct format)
		 */
		// return SMSPopupUtils.formatTimestamp(context, timestamp);
		return DateUtils.formatDateTime(mContext, mTimestamp, DateUtils.FORMAT_SHOW_TIME);
	}

	public String getContactName() {
		if (mContactName == null) {
			mContactName = mContext.getString(android.R.string.unknownName);
		}
		return mContactName;
	}

	public String getMessageBody() {
		return mMessageBody;
	}

	public long getThreadId() {
		return mThreadId;
	}

	public int getMessageType() {
		return mMessageType;
	}

	public boolean getNotify() {
		return mNotify;
	}

	public int getReminderCount() {
		return mReminderCount;
	}

	public void updateReminderCount(int count) {
		mReminderCount = count;
	}

	public void incrementReminderCount() {
		mReminderCount++;
	}

	public void setMessageId() {
		mMessageId = SmsPopupUtils.findMessageId(mContext, mThreadId, mTimestamp, mMessageType);
	}

	public long getMessageId() {
		if (mMessageId == 0) {
			setMessageId();
		}
		return mMessageId;
	}

	public String getContactId() {
		return mContactId;
	}

	// public boolean equals(SmsMmsMessage compareMessage) {
	// boolean equals = false;
	// if (PhoneNumberUtils.compare(this.fromAddress,
	// compareMessage.fromAddress) &&
	// this.compareTimeStamp(compareMessage.timestamp) &&
	// this.messageType == compareMessage.messageType) {
	// equals = true;
	// }
	// return equals;
	// }

	/**
	 * Check if this message is sufficiently the same as the provided parameters
	 */
	public boolean equals(String fromAddress, long timestamp,
			long timestamp_provider, String body) {
		boolean equals = false;

		if (PhoneNumberUtils.compare(this.mFromAddress, fromAddress)
				&& this.compareTimeStamp(timestamp, timestamp_provider)
				&& this.compareBody(body)) {
			equals = true;
		}
		return equals;
	}

	// private boolean compareTimeStamp(long compareTimestamp) {
	// return compareTimeStamp(compareTimestamp, 0);
	// }

	/*
	 * Compares the timestamps of a message, this is super hacky because the way
	 * which builds of Android store SMS timestamps changed in the cupcake
	 * branch - pre-cupcake it stored the timestamp provided by the telecom
	 * provider; post-cupcake it stored the system timestamp. Unfortunately this
	 * means we need to use 2 different ways to determine if the received
	 * message timestamp is sufficiently equal to the database timestamp :(
	 */
	private boolean compareTimeStamp(long compareTimestamp, long providerTimestamp) {
		
		final int MESSAGE_COMPARE_TIME_BUFFER = 2000;
		// final int buildVersion = Integer.valueOf(Build.VERSION.INCREMENTAL);

		/*
		 * On March 28th, 2009 - these are the latest builds that I could find:
		 * 128600 TMI-RC9 (Tmobile EU) 126986 PLAT-RC33 (Tmobile US) 129975
		 * Emulator (from Android 1.1 SDK r1) Hopefully anything later will have
		 * the updated SMS code that uses the system timestamp rather than the
		 * SMS timestamp
		 */
		// final int LATEST_BUILD = 129975;
		// boolean PRE_CUPCAKE = false;
		// if (buildVersion <= LATEST_BUILD) {
		// PRE_CUPCAKE = true;
		// }
		//
		// Log.v("DB timestamp = " + timestamp);
		// Log.v("Provider timestamp = " + providerTimestamp);
		// Log.v("System timestamp = " + compareTimestamp);

		/*
		 * If pre-cupcake we can just do a direct comparison as the Mms app
		 * stores the timestamp from the telecom provider (in the sms pdu)
		 */
		// if (PRE_CUPCAKE) {
		// Log.v("Build is pre-cupcake ("+buildVersion+"), doing direct SMS timestamp comparison");
		// Log.v("DB timestamp = " + timestamp);
		// Log.v("Intent timestamp = " + providerTimestamp);
		if (mTimestamp == providerTimestamp) {
			// Log.v("SMS Compare: compareTimestamp() - intent timestamp = provider timestamp");
			return true;
		} // else {
		// return false;
		// }
		// }

		/*
		 * If post-cupcake, the system app stores a system timestamp - the only
		 * problem is we have no way of knowing the exact time the system app
		 * used. So what we'll do is compare against our own system timestamp
		 * and add a buffer in. This is an awful way of doing this, but I don't
		 * see any other way around it :(
		 */
		// Log.v("Build is post-cupcake ("+buildVersion+"), doing approx. SMS timestamp comparison");
		// Log.v("DB timestamp = " + timestamp);
		// Log.v("Intent timestamp = " + compareTimestamp);

		if (mTimestamp < (compareTimestamp + MESSAGE_COMPARE_TIME_BUFFER)
				&& mTimestamp > (compareTimestamp - MESSAGE_COMPARE_TIME_BUFFER)) {
			// Log.v("SMS Compare: compareTimestamp() - timestamp is approx. the same");
			return true;
		}
		// Log.v("SMS Compare: compareTimestamp() - return false");
		return false;
	}

	/*
	 * Compare message body
	 */
	private boolean compareBody(String compareBody) {
		if (compareBody != null) {
			if (mMessageBody.length() != compareBody.length()) {
				// Log.v("SMS Compare: compareBody() - length is different");
				return false;
			}

			if (mMessageBody.equals(compareBody)) {
				// Log.v("SMS Compare: compareBody() - messageBody is the same");
				return true;
			}
		}
		// Log.v("SMS Compare: compareBody() - return false");
		return false;
	}

	public boolean replyToMessage(String quickreply) {
		// SmsMessageSender sender =
		// new SmsMessageSender(context, new String[] {fromAddress}, quickreply,
		// threadId);
		// return sender.sendMessage();
		return false;
	}
}

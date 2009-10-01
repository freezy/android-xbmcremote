package org.xbmc.android.util;

import android.net.Uri;

public class SMSConstants {
	public static final Uri CONTENT_URI = Uri.parse("content://sms/inbox");
	
	public static final String ID 					= "_id";
	public static final int ID_INT					= 0;
	public static final String THREAD_ID 			= "thread_id";
	public static final int THREAD_ID_INT 			= 1;
	public static final String ADDRESS 				= "address";
	public static final int ADDRESS_INT 			= 2;
	public static final String PERSON 				= "person";
	public static final int PERSON_INT 				= 3;
	public static final String DATE 				= "date";
	public static final int DATE_INT 				= 4;
	public static final String PROTOCOL 			= "protocol";
	public static final int PROTOCOL_INT 			= 5;
	public static final String READ 				= "read";
	public static final int READ_INT 				= 6;
	public static final String STATUS 				= "status";
	public static final int STATUS_INT 				= 7;
	public static final String TYPE 				= "type";
	public static final int TYPE_INT 				= 8;
	public static final String REPLY_PATH_PRESENT 	= "reply_path_present";
	public static final int REPLY_PATH_PRESENT_INT 	= 9;
	public static final String SUBJECT 				= "subject";
	public static final int SUBJECT_INT 			= 10;
	public static final String BODY 				= "body";
	public static final int BODY_INT 				= 11;
	public static final String SERVICE_CENTER 		= "service_center";
	public static final int SERVICE_CENTER_INT 		= 12;
	public static final String [] projection = {
		ID, THREAD_ID, ADDRESS, PERSON, DATE, PROTOCOL, READ, STATUS,
		TYPE, REPLY_PATH_PRESENT, SUBJECT, BODY, SERVICE_CENTER
	};
	
}

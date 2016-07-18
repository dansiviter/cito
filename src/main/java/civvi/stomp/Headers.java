package civvi.stomp;

import javax.ws.rs.core.HttpHeaders;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [14 Jul 2016]
 */
public enum Headers { ;
	public static final String CONTENT_TYPE = HttpHeaders.CONTENT_TYPE;
	public static final String CONTENT_LENGTH = HttpHeaders.CONTENT_LENGTH;
	public static final String ACCEPT_VERSION = "accept-version";

	public static final String VERSION = "version";
	public static final String HOST = "host";
	public static final String RECEIPT = "receipt";
	public static final String RECEIPT_ID = "receipt-id";

	public static final String LOGIN = "login";
	public static final String PASSCODE = "passcode";
	public static final String HEART_BEAT = "heart-beat";
	
	public static final String SESSION = "session";
	public static final String SERVER = "server";
	
	public static final String DESTINATION = "destination";
}

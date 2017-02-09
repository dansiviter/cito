/*
 * Copyright 2016-2017 Daniel Siviter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cito.stomp;

import javax.ws.rs.core.HttpHeaders;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [14 Jul 2016]
 */
public enum Headers { ;
	public static final String CONTENT_TYPE = HttpHeaders.CONTENT_TYPE.toLowerCase();
	public static final String CONTENT_LENGTH = HttpHeaders.CONTENT_LENGTH.toLowerCase();
	public static final String ACCEPT_VERSION = "accept-version";
	public static final String VERSION = "version";
	public static final String HOST = "host";
	public static final String RECIEPT = "receipt";
	public static final String RECIEPT_ID = "receipt-id";
	public static final String LOGIN = "login";
	public static final String PASSCODE = "passcode";
	public static final String HEART_BEAT = "heart-beat";
	public static final String SESSION = "session";
	public static final String SERVER = "server";
	public static final String DESTINATION = "destination";
	public static final String CORRELATION_ID = "correlation-id";
	public static final String REPLY_TO = "reply-to";
	public static final String EXPIRATION_TIME = "expires";
	public static final String PRIORITY = "priority";
	public static final String TYPE = "type";
	public static final String PERSISTENT = "persistent";
	public static final String MESSAGE_ID = "message-id";
	public static final String PRORITY = "priority";
	public static final String REDELIVERED = "redelivered";
	public static final String TIMESTAMP = "timestamp";
	public static final String SUBSCRIPTION = "subscription";
	public static final String ID = "id";
	public static final String ACK = "ack";
	public static final String TRANSACTION = "transaction";

	// custom 
	public static final String SELECTOR = "selector";
}

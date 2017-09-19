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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.core.HttpHeaders;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [14 Jul 2016]
 */
public interface Header {
	public static final Map<String, Header> HEADERS = new HashMap<>();


	/**
	 * @return the header value.
	 */
	String value();


	// --- Static Methods ---

	/**
	 * 
	 * @param value
	 * @return
	 */
	public static Header valueOf(String value) {
		Header header = HEADERS.get(value);

		if (header != null) {
			return header;
		}
		for (Header h : Standard.values()) {
			if (h.value().equalsIgnoreCase(value)) {
				header = h;
				break;
			}
		}
		if (header != null) {
			HEADERS.put(value, header);
			return header;
		}
		for (Header h : Custom.values()) {
			if (h.value().equalsIgnoreCase(value)) {
				header = h;
				break;
			}
		}
		if (header != null) {
			HEADERS.put(value, header);
			return header;
		}

		header = new StringHeader(value);
		HEADERS.put(value, header);
		return header;
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [18 Sep 2017]
	 */
	public enum Standard implements Header {
		CONTENT_TYPE(HttpHeaders.CONTENT_TYPE.toLowerCase()),
		CONTENT_LENGTH(HttpHeaders.CONTENT_LENGTH.toLowerCase()),
		ACCEPT_VERSION("accept-version"),
		VERSION("version"),
		HOST("host"),
		RECEIPT("receipt"),
		RECEIPT_ID("receipt-id"),
		LOGIN("login"),
		PASSCODE("passcode"),
		HEART_BEAT("heart-beat"),
		SESSION("session"),
		SERVER("server"),
		DESTINATION("destination"),
		CORRELATION_ID("correlation-id"),
		REPLY_TO("reply-to"),
		EXPIRATION_TIME("expires"),
		PRIORITY("priority"),
		TYPE("type"),
		PERSISTENT("persistent"),
		MESSAGE_ID("message-id"),
		PRORITY("priority"),
		REDELIVERED("redelivered"),
		TIMESTAMP("timestamp"),
		SUBSCRIPTION("subscription"),
		ID("id"),
		ACK("ack"),
		TRANSACTION("transaction");

		public final String value;

		Standard(String value) {
			this.value = value;
		}

		@Override
		public String value() {
			return this.value;
		}
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [18 Sep 2017]
	 */
	public enum Custom implements Header {
		SELECTOR("selector");

		public final String value;

		Custom(String value) {
			this.value = value;
		}

		@Override
		public String value() {
			return this.value;
		}
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [18 Sep 2017]
	 */
	public static class StringHeader implements Header {
		private final String value;

		StringHeader(String value) {
			this.value = value;
		}

		@Override
		public String value() {
			return value;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(this.value.toLowerCase());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null || getClass() != obj.getClass())
				return false;
			StringHeader other = (StringHeader) obj;
			return this.value.equalsIgnoreCase(other.value);
		}
	}
}

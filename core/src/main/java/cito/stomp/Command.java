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

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [12 Jul 2016]
 */
public enum Command {
	ABORT,
	ACK,
	BEGIN,
	COMMIT,
	CONNECT,
	CONNECTED,
	DISCONNECT,
	ERROR,
	MESSAGE,
	NACK,
	RECIEPT,
	SEND,
	STOMP,
	SUBSCRIBE,
	UNSUBSCRIBE,
	HEARTBEAT; // Special command type, doesn't actually exist in the spec..

	/**
	 * 
	 * @return
	 */
	public boolean server() {
		return this == CONNECTED || this == ERROR || this == MESSAGE || this == RECIEPT;
	}

	/**
	 * 
	 * @return
	 */
	public boolean destination() {
		return this == MESSAGE || this == SEND || this == SUBSCRIBE;
	}

	/**
	 * 
	 * @return
	 */
	public boolean subscriptionId() {
		return this == MESSAGE || this == SUBSCRIBE || this == UNSUBSCRIBE;
	}

	/**
	 * 
	 * @return
	 */
	public boolean body() {
		return this == SEND || this == MESSAGE || this == ERROR; 
	}

	/**
	 * 
	 * @return
	 */
	public boolean transaction() {
		return this == BEGIN || this == COMMIT || this == ABORT; 
	}
}

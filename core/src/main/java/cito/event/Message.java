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
package cito.event;

import javax.annotation.concurrent.Immutable;

import cito.stomp.Frame;
import cito.util.ToStringBuilder;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [19 Jul 2016]
 */
@Immutable
public class Message {
	private final String sessionId;
	private final Frame frame;

	public Message(Frame frame) {
		this(null, frame);
	}

	public Message(String sessionId, Frame frame) {
		this.sessionId = sessionId;
		this.frame = frame;
	}

	/**
	 * @return the originating session identifier. If this is a internally generated message (i.e. application code)
	 * then this will be {@code null}.
	 */
	public String sessionId() {
		return sessionId;
	}

	/**
	 * @return the STOMP frame.
	 */
	public Frame frame() {
		return frame;
	}

	@Override
	public String toString() {
		return ToStringBuilder.create(this)
				.append("sessionId", this.sessionId)
				.append("frame.command", this.frame.command())
				.toString();
	}
}

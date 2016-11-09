package cito.stomp.server.event;

import cito.stomp.Frame;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [19 Jul 2016]
 */
public class BasicMessageEvent implements MessageEvent {
	private final String sessionId;
	private final Frame frame;

	public BasicMessageEvent(Frame frame) {
		this(null, frame);
	}

	public BasicMessageEvent(String sessionId, Frame frame) {
		this.sessionId = sessionId;
		this.frame = frame;
	}

	@Override
	public String sessionId() {
		return sessionId;
	}

	@Override
	public Frame frame() {
		return frame;
	}
}

package cito.stomp.server.event;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import cito.io.ByteBufferInputStream;
import cito.stomp.Frame;
import cito.stomp.ext.Serialiser;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [9 Nov 2016]
 */
@Dependent
public class SerialisingMessageEvent implements MessageEvent {
	@Inject
	private Serialiser serialiser;

	private String sessionId;
	private Frame frame;
	private Object bean;

	/**
	 * 
	 * @param sessionId
	 * @param frame
	 */
	public void init(String sessionId, Frame frame) {
		this.sessionId = sessionId;
		this.frame = frame;
	}

	@Override
	public String sessionId() {
		return this.sessionId;
	}

	@Override
	public Frame frame() {
		return this.frame;
	}

	/**
	 * 
	 * @param type
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public <T> T getBean(Type type) throws IOException {
		if (bean == null) {
			try (InputStream is = new ByteBufferInputStream(frame().getBody())) {
				this.serialiser.readFrom(type, frame().contentType(), is);
			}
		}
		return (T) bean;
	}
}

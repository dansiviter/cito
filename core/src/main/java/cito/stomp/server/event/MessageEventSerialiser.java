package cito.stomp.server.event;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import cito.io.ByteBufferInputStream;
import cito.stomp.Frame;
import cito.stomp.ext.Serialiser;
import cito.stomp.server.annotation.Body;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jan 2017]
 */
@Dependent
public class MessageEventSerialiser {
	@Inject
	private Serialiser serialiser;
	@Inject
	private MessageEvent event;

	/**
	 * 
	 * @param cls
	 * @return
	 */
	public <T> T getBean(Class<T> cls) {
		return getBean((Type) cls);
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getBean(Type type) {
		final Frame frame = event.frame();
		try (InputStream is = new ByteBufferInputStream(frame.getBody())) {
			return (T) this.serialiser.readFrom(type, frame.contentType(), is);
		} catch (IOException e) {
			throw new IllegalStateException("Unable to serialise!", e);
		}
	}

	/**
	 * 
	 * @param ip
	 * @return
	 */
	@Produces @Body
	public Object get(InjectionPoint ip) {
		return getBean(ip.getType());
	}
}

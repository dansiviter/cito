package cito.stomp.ext;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [24 Aug 2016]
 * @param <T>
 */
public interface BodyWriter<T> {
	/**
	 * 
	 * @param type
	 * @param mediaType
	 * @return
	 */
	boolean isWriteable(Type type, MediaType mediaType);

	/**
	 * 
	 * @param t
	 * @param type
	 * @param mediaType
	 * @param os
	 * @throws IOException
	 */
	void writeTo(T t, Type type, MediaType mediaType, OutputStream os) throws IOException;
}

package cito.stomp.ext;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [24 Aug 2016]
 * @param <T>
 */
public interface BodyReader<T> {
	/**
	 * 
	 * @param type
	 * @param mediaType
	 * @return
	 */
	boolean isReadable(Type type, MediaType mediaType);

	/**
	 * 
	 * @param type
	 * @param mediaType
	 * @param is
	 * @return
	 * @throws java.io.IOException
	 */
	T readFrom(Type type, MediaType mediaType, InputStream is) throws IOException;
}

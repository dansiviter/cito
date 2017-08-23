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
package cito.ext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

/**
 * Performs [de]serialisation of beans.
 * 
 * @author Daniel Siviter
 * @since v1.0 [24 Aug 2016]
 * @see BodyReader
 * @see BodyWriter
 */
@ApplicationScoped
public class Serialiser {
	@Inject
	private Instance<BodyReader<?>> readers;
	@Inject
	private Instance<BodyWriter<?>> writers;

	/**
	 * 
	 * @param type
	 * @param mediaType
	 * @param is
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public <T> T readFrom(Type type, MediaType mediaType, InputStream is) throws IOException {
		for (BodyReader<?> reader : this.readers) {
			if (reader.isReadable(type, mediaType)) {
				return (T) reader.readFrom(type, mediaType, is);
			}
		}
		throw new IOException("Unable to read! [type=" + type + ",mediaType=" + mediaType + "]");
	}

	/**
	 * 
	 * @param t
	 * @param type
	 * @param mediaType
	 * @param os
	 * @throws IOException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> void writeTo(T t, Type type, MediaType mediaType, OutputStream os) throws IOException {
		for (BodyWriter<?> writer : this.writers) {
			if (writer.isWriteable(type, mediaType)) {
				((BodyWriter) writer).writeTo(t, type, mediaType, os);
				return;
			}
		}
		throw new IOException("Unable to write! [type=" + type + ",mediaType=" + mediaType + "]");
	}
}

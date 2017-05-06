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

import static java.nio.charset.Charset.forName;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.core.MediaType.CHARSET_PARAMETER;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.MediaType;

/**
 * {@link BodyWriter} and {@link BodyReader} for {@code text/plain} type.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Apr 2017]
 */
@ApplicationScoped
public class TextPlainSerialiser implements BodyWriter<Object>, BodyReader<Object> {
	@Override
	public boolean isReadable(Type type, MediaType mediaType) {
		return mediaType.isCompatible(MediaType.TEXT_PLAIN_TYPE) && (
				type == String.class ||
				(type instanceof Class<?> && Reader.class.isAssignableFrom((Class<?>) type)));
	}

	@Override
	public Object readFrom(Type type, MediaType mediaType, InputStream is) throws IOException {
		if (type == String.class) {
			final Charset charset;
			if (mediaType.getParameters().containsKey(CHARSET_PARAMETER)) {
				charset = Charset.forName(mediaType.getParameters().get(CHARSET_PARAMETER));
			} else {
				charset = StandardCharsets.UTF_8;
			}
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset))) {
				return reader.lines().collect(Collectors.joining("\n"));
			}
		} else if (type instanceof Class<?> && Reader.class.isAssignableFrom((Class<?>) type)) {
			return new InputStreamReader(is);
		}
		throw new IllegalArgumentException("Unsupported type!");
	}

	@Override
	public boolean isWriteable(Type type, MediaType mediaType) {
		return mediaType.isCompatible(MediaType.TEXT_PLAIN_TYPE) && (
				type == String.class ||
				(type instanceof Class<?> && Reader.class.isAssignableFrom((Class<?>) type)));
	}

	@Override
	public void writeTo(Object t, Type type, MediaType mediaType, OutputStream os)
			throws IOException
	{
		if (type == String.class) {
			os.write(getCharset(mediaType).encode(t.toString()).array());
		} else if (type instanceof Class<?> && Reader.class.isAssignableFrom((Class<?>) type)) {
			final Reader reader = (Reader) t;
			int c;
			while ((c = reader.read()) != -1) {
				os.write(c);
			}
		}
	}


	// --- Static Methods ---

	/**
	 * 
	 * @param mediaType
	 * @return
	 */
	private static Charset getCharset(MediaType mediaType) {
		if (mediaType.getParameters().containsKey(CHARSET_PARAMETER)) {
			return forName(mediaType.getParameters().get(CHARSET_PARAMETER));
		}
		return UTF_8;
	}
}

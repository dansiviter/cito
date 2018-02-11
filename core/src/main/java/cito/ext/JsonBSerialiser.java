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

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import cito.ext.BodyReader;
import cito.ext.BodyWriter;

/**
 * {@link BodyWriter} and {@link BodyReader} for {@code application/json} type using Json-B.
 *  
 * @author Daniel Siviter
 * @since v1.0 [1 May 2017]
 */
@ApplicationScoped
public class JsonBSerialiser implements BodyWriter<Object>, BodyReader<Object> {
	@Inject
	private Logger log;
	@Inject
	private Instance<Jsonb> jsonbInstance;

	private volatile Jsonb jsonb;

	@Override
	public boolean isReadable(Type type, MediaType mediaType) {
		return mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE);
	}

	@Override
	public Object readFrom(Type type, MediaType mediaType, InputStream is) throws IOException {
		return getJsonb().fromJson(is, type);
	}

	@Override
	public boolean isWriteable(Type type, MediaType mediaType) {
		return mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE);
	}

	@Override
	public void writeTo(Object t, Type type, MediaType mediaType, OutputStream os) throws IOException {
		getJsonb().toJson(t, type, os);
	}

	/**
	 * 
	 * @return
	 */
	private Jsonb getJsonb() {
		if (this.jsonbInstance.isUnsatisfied()) {
			if (this.jsonb == null) {
				this.jsonb = JsonbBuilder.create();
			}
			return this.jsonb;
		}
		return this.jsonbInstance.get();
	}

	@PreDestroy
	public void destroy() {
		if (this.jsonb != null) {
			try {
				this.jsonb.close();
			} catch (Exception e) {
				this.log.warn("Unable to close Jsonb!", e);
			}
		}
	}


	// --- Static Methods ---

	/**
	 * 
	 * @return
	 */
	@ApplicationScoped @Produces
	public static Jsonb create() {
		return JsonbBuilder.create();
	}
}

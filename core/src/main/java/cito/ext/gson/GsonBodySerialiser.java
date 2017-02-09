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
package cito.ext.gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import cito.ext.BodyReader;
import cito.ext.BodyWriter;

@ApplicationScoped
public class GsonBodySerialiser implements BodyWriter<Object>, BodyReader<Object> {
	@Inject
	private Gson gson;

	@Override
	public boolean isReadable(Type type, MediaType mediaType) {
		return mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE);
	}

	@Override
	public Object readFrom(Type type, MediaType mediaType, InputStream is) throws IOException {
		final JsonReader reader = new JsonReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		return this.gson.fromJson(reader, type);
	}

	@Override
	public boolean isWriteable(Type type, MediaType mediaType) {
		return mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE);
	}

	@Override
	public void writeTo(Object t, Type type, MediaType mediaType, OutputStream os) throws IOException {
		final JsonWriter writer = new JsonWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
		this.gson.toJson(t, type, writer);
		writer.flush();
	}
}

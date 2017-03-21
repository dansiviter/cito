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

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [24 Aug 2016]
 */
@ApplicationScoped
public class GsonProducer {
	@Produces @Dependent
	public static Gson gson() {
		return new GsonBuilder().registerTypeAdapterFactory(new TemporalTypeAdaptorFactory()).create();
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [22 Mar 2017]
	 */
	private static class TemporalTypeAdaptorFactory implements TypeAdapterFactory {
		private final Map<Class< ? extends Temporal>, TemporalTypeAdaptor> adaptors = new HashMap<>();

		public TemporalTypeAdaptorFactory() {
			this.adaptors.put(LocalDateTime.class, new TemporalTypeAdaptor(ISO_LOCAL_DATE_TIME, LocalDateTime::from));
			this.adaptors.put(LocalDate.class, new TemporalTypeAdaptor(ISO_LOCAL_DATE, LocalDate::from));
			this.adaptors.put(LocalTime.class, new TemporalTypeAdaptor(ISO_LOCAL_TIME, LocalTime::from));
			this.adaptors.put(Instant.class, new TemporalTypeAdaptor(ISO_INSTANT, Instant::from));
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
			final Class<T> cls = (Class<T>) typeToken.getRawType();
			final TemporalTypeAdaptor adaptor = this.adaptors.get(cls);
			return (TypeAdapter<T>) adaptor;
		}
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [22 Mar 2017]
	 */
	private static class TemporalTypeAdaptor extends TypeAdapter<TemporalAccessor> {
		private final DateTimeFormatter formatter;
		private final TemporalQuery<TemporalAccessor> query;

		/**
		 * @param isoLocalDateTime
		 * @param object
		 */
		public TemporalTypeAdaptor(DateTimeFormatter formatter, TemporalQuery<TemporalAccessor> query) {
			this.formatter = formatter;
			this.query = query;
		}

		@Override
		public void write(JsonWriter out, TemporalAccessor value) throws IOException {
			if (value == null) {
				out.nullValue();
				return;
			}
			out.value(this.formatter.format(value));
		}

		@Override
		public TemporalAccessor read(JsonReader in) throws IOException {
			if (in.peek() == JsonToken.NULL) {
				in.nextNull();
				return null;
			}
			return this.formatter.parse(in.nextString(), this.query);
		}
	}
}

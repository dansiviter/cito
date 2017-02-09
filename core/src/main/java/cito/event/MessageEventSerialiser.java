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
package cito.event;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import cito.annotation.Body;
import cito.ext.Serialiser;
import cito.io.ByteBufferInputStream;
import cito.stomp.Frame;

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

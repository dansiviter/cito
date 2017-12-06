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
package cito.sockjs;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

/**
 * A version of {@link Response} that is auto-closable. JAX-RS 2.1 does this by default, but unfortunately we have to
 * do this manually for the time being.
 * 
 * @author Daniel Siviter
 * @since v1.0 [6 Dec 2017]
 */
public class ClosableResponse extends Response implements AutoCloseable {
	private final Response delegate;

	/**
	 * 
	 * @param delegate
	 */
	private ClosableResponse(Response delegate) {
		this.delegate = delegate;
	}

	@Override
	public int getStatus() {
		return this.delegate.getStatus();
	}

	@Override
	public StatusType getStatusInfo() {
		return this.delegate.getStatusInfo();
	}

	@Override
	public Object getEntity() {
		return this.delegate.getEntity();
	}

	@Override
	public <T> T readEntity(Class<T> entityType) {
		return this.delegate.readEntity(entityType);
	}


	@Override
	public <T> T readEntity(GenericType<T> entityType) {
		return this.delegate.readEntity(entityType);
	}

	@Override
	public <T> T readEntity(Class<T> entityType, Annotation[] annotations) {
		return this.delegate.readEntity(entityType, annotations);
	}

	@Override
	public <T> T readEntity(GenericType<T> entityType, Annotation[] annotations) {
		return this.delegate.readEntity(entityType, annotations);
	}

	@Override
	public boolean hasEntity() {
		return this.delegate.hasEntity();
	}

	@Override
	public boolean bufferEntity() {
		return this.delegate.bufferEntity();
	}

	@Override
	public void close() {
		this.delegate.close();
	}

	@Override
	public MediaType getMediaType() {
		return this.delegate.getMediaType();
	}

	@Override
	public Locale getLanguage() {
		return this.delegate.getLanguage();
	}

	@Override
	public int getLength() {
		return this.delegate.getLength();
	}

	@Override
	public Set<String> getAllowedMethods() {
		return this.delegate.getAllowedMethods();
	}

	@Override
	public Map<String, NewCookie> getCookies() {
		return this.delegate.getCookies();
	}

	@Override
	public EntityTag getEntityTag() {
		return this.delegate.getEntityTag();
	}

	@Override
	public Date getDate() {
		return this.delegate.getDate();
	}

	@Override
	public Date getLastModified() {
		return this.delegate.getLastModified();
	}

	@Override
	public URI getLocation() {
		return this.delegate.getLocation();
	}

	@Override
	public Set<Link> getLinks() {
		return this.delegate.getLinks();
	}

	@Override
	public boolean hasLink(String relation) {
		return this.delegate.hasLink(relation);
	}

	@Override
	public Link getLink(String relation) {
		return this.delegate.getLink(relation);
	}

	@Override
	public Builder getLinkBuilder(String relation) {
		return this.delegate.getLinkBuilder(relation);
	}

	@Override
	public MultivaluedMap<String, Object> getMetadata() {
		return this.delegate.getMetadata();
	}

	@Override
	public MultivaluedMap<String, String> getStringHeaders() {
		return this.delegate.getStringHeaders();
	}

	@Override
	public String getHeaderString(String name) {
		return this.delegate.getHeaderString(name);
	}


	// --- Static Methods ---

	/**
	 * 
	 * @param delegate
	 * @return
	 */
	public static ClosableResponse closable(Response delegate) {
		return new ClosableResponse(delegate);
	}
}

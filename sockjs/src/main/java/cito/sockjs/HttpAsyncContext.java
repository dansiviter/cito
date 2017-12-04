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

import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple wrapper for HTTP requests.
 * 
 * @author Daniel Siviter
 * @since v1.0 [18 Feb 2017]
 */
public class HttpAsyncContext implements AsyncContext {
	private final AsyncContext delegate;

	public HttpAsyncContext(AsyncContext delegate) {
		this.delegate = delegate;
	}

	@Override
	public HttpServletRequest getRequest() {
		return (HttpServletRequest) this.delegate.getRequest();
	}

	@Override
	public HttpServletResponse getResponse() {
		return (HttpServletResponse) this.delegate.getResponse();
	}

	@Override
	public boolean hasOriginalRequestAndResponse() {
		return this.delegate.hasOriginalRequestAndResponse();
	}

	@Override
	public void dispatch() {
		this.delegate.dispatch();
	}

	@Override
	public void dispatch(String path) {
		this.delegate.dispatch(path);
	}

	@Override
	public void dispatch(ServletContext context, String path) {
		this.delegate.dispatch(context, path);
	}

	@Override
	public void complete() {
		this.delegate.complete();
	}

	@Override
	public void start(Runnable run) {
		this.delegate.start(run);
	}

	@Override
	public void addListener(AsyncListener listener) {
		this.delegate.addListener(listener);
	}

	@Override
	public void addListener(AsyncListener listener, ServletRequest servletRequest, ServletResponse servletResponse) {
		this.delegate.addListener(listener, servletRequest, servletResponse);
	}

	@Override
	public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException {
		return this.delegate.createListener(clazz);
	}

	@Override
	public void setTimeout(long timeout) {
		this.delegate.setTimeout(timeout);
	}

	@Override
	public long getTimeout() {
		return this.delegate.getTimeout();
	}
}

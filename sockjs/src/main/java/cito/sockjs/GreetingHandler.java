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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import cito.sockjs.nio.WriteStream;

/**
 * @author Daniel Siviter
 * @since v1.0 [2 Mar 2017]
 */
public class GreetingHandler extends AbstractHandler {
	private static final long serialVersionUID = -6439964384579190044L;
	private static final String PAYLOAD = "Welcome to SockJS!\n";
	static final String GREETING = "greeting";

	/**
	 * 
	 * @param servlet
	 */
	public GreetingHandler(Servlet servlet) {
		super(servlet, "text/plain;charset=UTF-8", "GET");
	}

	@Override
	protected void handle(HttpAsyncContext async) throws ServletException, IOException {
		sendNonBlock(async, HttpServletResponse.SC_OK, PAYLOAD);
	}
}

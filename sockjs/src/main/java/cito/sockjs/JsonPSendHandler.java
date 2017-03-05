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

import java.io.IOException;

import javax.servlet.ServletException;

/**
 * @author Daniel Siviter
 * @since v1.0 [1 Mar 2017]
 */
public class JsonPSendHandler extends AbstractSessionHandler {
	private static final long serialVersionUID = 6883526585964051391L;

	static final String JSONP_SEND = "jsonp_send";

	/**
	 * 
	 * @param servlet
	 */
	public JsonPSendHandler(Servlet servlet) {
		super(servlet, "text/plain;charset=UTF-8", false, "POST");
	}

	@Override
	protected void handle(HttpAsyncContext async, ServletSession session, boolean initial)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}

}

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
public class JsonPHandler extends AbstractSessionHandler {
	private static final long serialVersionUID = 5586170659349345979L;

	static final String JSONP = "jsonp";

	/**
	 * 
	 * @param servlet
	 */
	public JsonPHandler(Servlet servlet) {
		super(servlet, "application/javascript;charset=UTF-8", true, "GET");
	}

	@Override
	protected void handle(HttpAsyncContext async, ServletSession session, boolean initial)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}

}

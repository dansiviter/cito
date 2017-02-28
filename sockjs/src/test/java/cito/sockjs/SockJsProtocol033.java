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
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Performs SockJS protocol verification using the Python script from
 * <a href="https://github.com/sockjs/sockjs-protocol/tree/v0.3.3">here</a>
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Oct 2016]
 */
@Ignore
@Deprecated // getting python working was too much of a pain!
public class SockJsProtocol033 {
	@Test
	public void pythonTest() throws ScriptException, IOException {
		final ScriptEngine engine = new ScriptEngineManager().getEngineByName("python");
	
		try (final InputStream is = getClass().getResourceAsStream("sockjs-protocol-0.3.3.py")) {
			final Bindings bindings = engine.createBindings();
			bindings.put("SOCKJS_URL", "localhost:8080");
			final Object result = engine.eval(new InputStreamReader(is), bindings);
			System.out.println(result);
		}
	}
}

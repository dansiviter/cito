package cito.sockjs;

import java.io.IOException;
import java.io.InputStream;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Test;
import org.python.jline.internal.InputStreamReader;
import org.python.jsr223.PyScriptEngineFactory;
import org.python.util.PythonInterpreter;

/**
 * Performs SockJS protocol verification using the Python script from
 * <a href="https://github.com/sockjs/sockjs-protocol/tree/v0.3.3">here</a>
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Oct 2016]
 */
public class SockJsProtocol033 {
	@Test
	public void pythonTest() throws ScriptException, IOException {
		//		new ScriptEngineManager().getEngineFactories().forEach(e -> System.out.println(e.getEngineName()));
		final ScriptEngine engine = new ScriptEngineManager().getEngineByName("python");

		try (final InputStream is = getClass().getResourceAsStream("sockjs-protocol-0.3.3.py")) {
			final Bindings bindings = engine.createBindings();
			bindings.put("SOCKJS_URL", "localhost:8080");
			final Object result = engine.eval(new InputStreamReader(is), bindings);
			System.out.println(result);
		}
	}
}

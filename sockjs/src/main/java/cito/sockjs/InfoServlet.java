package cito.sockjs;

import static cito.sockjs.Headers.CONTENT_TYPE;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Dec 2016]
 */
public class InfoServlet extends AbstractServlet {
	private static final long serialVersionUID = 4503408709378376273L;

	private static final Random RANDOM = new SecureRandom();

	public InfoServlet(Context ctx) {
		super(ctx);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setHeader(CONTENT_TYPE, "application/json; charset=UTF-8");
		resp.setStatus(HttpServletResponse.SC_OK);
		final JsonObject json = createJson(generateEntropy(), true, true, "*:*");
		try (JsonWriter writer = Json.createWriter(resp.getWriter())) {
			writer.writeObject(json);
		}
	}

	/**
	 * Creates JSON payload:
	 * <pre>
	 * {
	 *   "entropy": -1261588729,
	 *   "origins": [
	 *     "*:*"
	 *   ],
	 *   "cookie_needed": true,
	 *   "websocket": true
	 * }
	 * </pre>
	 * 
	 * @return
	 */
	private static JsonObject createJson(long entropy, boolean cookieNeeded, boolean websocket, String... origins) {
		final JsonObjectBuilder json = Json.createObjectBuilder();
		json.add("entropy", entropy);
		if (origins.length > 0) {
			final JsonArrayBuilder originsArray = Json.createArrayBuilder();
			for (String origin : origins) {
				originsArray.add(origin);
			}
			json.add("origins", originsArray);
		}
		json.add("cookie_needed", cookieNeeded);
		json.add("websocket", websocket);
		return json.build();
	}

	/**
	 * 
	 * @return
	 */
	public static long generateEntropy() {
		return RANDOM.nextLong();
	}

	@Override
	protected Session createSession(String sessionId, AsyncContext asyncCtx) {
		// TODO Auto-generated method stub
		return null;
	}
}

package cito.sockjs;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Dec 2016]
 */
public class InfoHandler extends AbstractHandler {
	private static final long serialVersionUID = 4503408709378376273L;

	protected static final Logger LOG = LoggerFactory.getLogger(InfoHandler.class);

	private static final Random RANDOM = new SecureRandom();

	/**
	 * 
	 * @param servlet
	 */
	public InfoHandler(Servlet servlet) {
		super(servlet);
	}

	@Override
	public void service(HttpAsyncContext asyncCtx) throws ServletException, IOException {
		final HttpServletRequest req = asyncCtx.getRequest();
		final HttpServletResponse res = asyncCtx.getResponse();

		if ("OPTIONS".equals(req.getMethod())) {
			options(asyncCtx, "OPTIONS", "GET");
			return;
		}
		if (!"GET".equals(req.getMethod())) {
			sendErrorNonBlock(asyncCtx, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}

		setCors(req, res);
		setCacheControl(res);
		res.setContentType("application/json;charset=UTF-8");
		res.setStatus(HttpServletResponse.SC_OK);

		try (JsonGenerator generator = Json.createGenerator(res.getWriter())) {
			createJson(generator, generateEntropy(), true, this.servlet.ctx.isWebSocketSupported(), "*:*");
		}
		asyncCtx.complete();
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
	private static void createJson(JsonGenerator generator, long entropy, boolean cookieNeeded, boolean websocket, String... origins) {
		generator.writeStartObject();
		generator.write("entropy", entropy);
		if (origins.length > 0) { // currently ignored
			generator.writeStartArray("origins");
			for (String origin : origins) {
				generator.write(origin);
			}
			generator.writeEnd();
		}
		generator.write("cookie_needed", cookieNeeded);
		generator.write("websocket", websocket);
		generator.writeEnd();
	}

	/**
	 * 
	 * @return
	 */
	public static long generateEntropy() {
		return RANDOM.nextLong();
	}
}

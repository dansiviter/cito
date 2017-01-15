package cito.sockjs;

import static cito.sockjs.HashUtil.md5;
import static cito.sockjs.Headers.CACHE_CONTROL;
import static cito.sockjs.Headers.CONTENT_TYPE;
import static cito.sockjs.Headers.EXPIRES;
import static cito.sockjs.Headers.E_TAG;
import static cito.sockjs.Headers.IF_NONE_MATCH;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Dec 2016]
 */
@WebServlet(name = "SockJs iFrame Servlet", asyncSupported = true)
public class IFrameServlet extends AbstractServlet {
	private static final long serialVersionUID = -5544345272086874216L;
	private static final int CACHE_DURATION_SECONDS = 31536000; // 1 year

	private String template;
	private String md5Hash;

	public IFrameServlet(Context ctx) {
		super(ctx);
	}

	@Override
	public void init() throws ServletException {
		try (InputStream is = getServletContext().getResourceAsStream("/iframe.html")) {
			final BufferedReader buffer = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			this.template = buffer.lines().collect(Collectors.joining("\n"));
			this.template = this.template.replaceAll("${sockjs.url}", "TODO");
			this.md5Hash = "\"" + md5(this.template) + "\"";;
		} catch (IOException e) {
			throw new ServletException("Unable to load template!", e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO disable iFrame on CORS

		if (this.md5Hash.equals(req.getHeader(IF_NONE_MATCH))) {
			resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return;
		}
		resp.setHeader(CACHE_CONTROL, "public, max-age=" + CACHE_DURATION_SECONDS);
		resp.setHeader(CONTENT_TYPE, "text/html; charset=UTF-8");
		resp.setHeader(E_TAG, md5Hash);
		resp.setDateHeader(EXPIRES, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(CACHE_DURATION_SECONDS));
		resp.getWriter().print(this.template);
	}
}

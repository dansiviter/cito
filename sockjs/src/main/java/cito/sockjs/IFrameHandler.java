package cito.sockjs;

import static cito.sockjs.HashUtil.md5;
import static cito.sockjs.Headers.CACHE_CONTROL;
import static cito.sockjs.Headers.EXPIRES;
import static cito.sockjs.Headers.E_TAG;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Dec 2016]
 */
public class IFrameHandler extends AbstractHandler {
	private static final long serialVersionUID = -5544345272086874216L;

	private String template;
	private EntityTag eTag;

	/**
	 * 
	 * @param servlet
	 */
	public IFrameHandler(Servlet servlet) {
		super(servlet, "text/html;charset=UTF-8", "GET");
	}

	@Override
	public IFrameHandler init() throws ServletException {
		try (InputStream is = getClass().getResourceAsStream("iframe.html")) {
			final BufferedReader buffer = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			this.template = buffer.lines().collect(Collectors.joining("\n"));
			this.template = this.template.replace("${sockjs.url}", this.servlet.ctx.getConfig().sockJsUri());
			this.eTag = EntityTag.from(md5(this.template));
		} catch (IOException e) {
			throw new ServletException("Unable to load template!", e);
		}
		return this;
	}

	@Override
	protected void handle(HttpAsyncContext async) throws ServletException, IOException {
		final HttpServletRequest req = async.getRequest();
		final HttpServletResponse res = async.getResponse();

		if (!req.getRequestURI().endsWith(".html")) {
			this.servlet.log("Invalid path! [" + req.getRequestURI() + "]");
			sendErrorNonBlock(async, HttpServletResponse.SC_NOT_FOUND);
			async.complete();
			return;
		}

		final EntityTag eTag = EntityTag.ifNoneMatch(req);
		if (this.eTag.equals(eTag)) {
			res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			res.setContentType(null);
			async.complete();
			return;
		}
		res.setHeader(CACHE_CONTROL, "public, max-age=31536000"); // 1 year
		res.setHeader(E_TAG, this.eTag.toString());
		res.setDateHeader(EXPIRES, ZonedDateTime.now(ZoneOffset.UTC.normalized()).plusYears(1).toEpochSecond());
		res.getWriter().print(this.template);
		async.complete();
	}
}

package cito.sockjs;

import static cito.sockjs.HashUtil.md5;
import static cito.sockjs.Headers.CACHE_CONTROL;
import static cito.sockjs.Headers.EXPIRES;
import static cito.sockjs.Headers.E_TAG;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cito.sockjs.nio.WriteStream;

/**
 * Handles IFrame ({@code /iframe[.*].html}) connections.
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Dec 2016]
 */
public class IFrameHandler extends AbstractHandler {
	static final String IFRAME = "iframe";

	private byte[] template;
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
		try {
			final String template = Util.resourceToString(getClass(), "iframe.html");
			this.template = template.replace("${sockjs.url}", this.servlet.getConfig().sockJsUri()).getBytes(UTF_8);
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
			this.log.warn("Invalid path! [{}]", req.getRequestURI());
			sendNonBlock(async, HttpServletResponse.SC_NOT_FOUND);
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
		final ReadableByteChannel iFrameChannel = Channels.newChannel(new ByteArrayInputStream(this.template));
		res.getOutputStream().setWriteListener(new WriteStream(async, iFrameChannel, t -> {
			iFrameChannel.close();
			if (t != null) {
				this.log.warn("Unable to write entity!", t);
			}
			async.complete();
		}));
	}
}

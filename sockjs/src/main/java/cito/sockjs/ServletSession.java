package cito.sockjs;

import java.security.Principal;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.websocket.RemoteEndpoint;
import javax.websocket.RemoteEndpoint.Basic;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [3 Jan 2017]
 */
public abstract class ServletSession extends SessionAdapter {
	protected static final String FRAME_DELIMITER = "\n";

	private final Map<String, String> pathParams;
	protected final AsyncContext asyncCtx;

	private RemoteEndpoint.Basic basic;

	/**
	 * 
	 * @param sessionId
	 * @param asyncCtx
	 * @param sender
	 */
	public ServletSession(AsyncContext asyncCtx) {
		this.asyncCtx = asyncCtx;
		this.pathParams = Util.pathParams(getRequest());
	}
	
	/**
	 * 
	 */
	protected abstract Basic createBasic();

	private SockJsRequest getRequest() {
		return (SockJsRequest) this.asyncCtx.getRequest();
	}

	@Override
	public String getId() {
		return getPathParameters().get("session");
	}

	@Override
	public Basic getBasicRemote() {
		if (this.basic == null) {
			this.basic = createBasic();
		}
		return this.basic;
	}

	@Override
	public boolean isSecure() {
		return getRequest().isSecure();
	}
	
	@Override
	public String getQueryString() {
		return getRequest().getQueryString();
	}

	@Override
	public Map<String, String> getPathParameters() {
		return this.pathParams;
	}

	@Override
	public Principal getUserPrincipal() {
		return getRequest().getUserPrincipal();
	}
}

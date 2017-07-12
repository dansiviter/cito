# Citō SockJS #

This implements the [v0.3.3](https://sockjs.github.io/sockjs-protocol/sockjs-protocol-0.3.3.html) of the SockJS Protocol mapping to the JSR 356 WebSocket specification using Servlet Non-Blocking where possible.

Bootstrapping is done via implementing the `cito.sockjs.Config` class. This will define the configuration of the SockJS runtime including the `javax.websocket.Endpoint` handler that will receive messages and permit sending.

**Note:** This is a pure SockJS to WebSocket implementation and is actually separate to Citō messaging functionality. To link the two together you would create a concrete type of `cito.server.AbstractEndpoint`:

	public class MyEndpoint extends cito.server.AbstractEndpoint { ... }

	public static class MyConfig implements cito.sockjs.Config {
		@Override
		public String path() {
			return "my-cito-server";
		}
	
		@Override
		public Class<? extends Endpoint> endpointClass() {
			return MyEndpoint.class;
		}
	}

## Limitations ##

**Annotated Endpoint**
Presently only implementations of `javax.websocket.Endpoint` are permitted. Annotated setup has not yet been written (See [#25](https://github.com/dansiviter/cito/issues/25)).

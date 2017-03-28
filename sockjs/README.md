# Citō SockJS

:warning: This implementation is only partially complete and does not cover the whole SockJS specification.

This implements the [v0.3.3](https://sockjs.github.io/sockjs-protocol/sockjs-protocol-0.3.3.html) of the SockJS Protocol mapping to the JSR 356 WebSocket specification using Non-Blocking techniques.

Bootstrapping is done via implementing the `cito.sockjs.Initialiser` class. This will define the `javax.websocket.Endpoint` handler that will receive messages and permit sending. 

**Note:** This is a pure SockJS to WebSocket implementation and is actually separate to Citō messaging functionality. To link the two together you would create a concrete type of `cito.server.AbstractEndpoint`:

	public class MyEndpoint extends cito.server.AbstractEndpoint { ... }

	public static class MyConfig implements cito.sockjs.Config {
		@Override
		public String path() {
			return "my-server";
		}
	
		@Override
		public Class<? extends Endpoint> endpointClass() {
			return MyEndpoint.class;
		}
	}


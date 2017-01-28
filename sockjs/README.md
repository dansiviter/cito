# Citō SockJS

This implements the [v0.3.3](https://sockjs.github.io/sockjs-protocol/sockjs-protocol-0.3.3.html) of the SockJS Protocol to provide entrypoint into the Citō runtime.

**Note:** This is a pure SockJS to WebSocket implementation and is actually separate to Citō messaging functionality. To link the two together you would create a `javax.websocket.Endpoint` implementation:


	public class MyServer extends AbstractServer implements Endpoint {
		...
	}


Bootstrapping is done via implementing the `cito.sockjs.Initialiser` class. This will define the `javax.websocket.Endpoint` handler that will receive messages and permit sending. 
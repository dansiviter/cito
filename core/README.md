# Citō Core #

Citō uses a number of technologies from the JEE 7 catalogue, namely:

* JMS - Interface between the Broker and STOMP,
* CDI - Dependency injection and context,
* JAX-RS - MediaTypes (will potentially be removed to reduce dependencies),

Both JMS, JAX-RS and CDI implementations must exist for it to work, and JAX-B for performing [de]serialisation binding. If you're just using Strings or binary data, then JAX-B is not required.


## Deployment Models ##

Citō can be deployed in fours ways: 

* Standalone, embedded broker,
* Clustered, embedded broker,
* Standalone, remote JMS broker,
* Clustered, remote JMS broker.

Citō utilises [Apache ActiveMQ Artemis](http://activemq.apache.org/artemis/) to act as an embedded message broker. This is a very high performant broker although it should be possible to integrate your own using JMS API.

## Getting Started ##

First import the core into your project:

	<dependency>
		<groupId>io.cito</groupId>
		<artifactId>core</artifactId>
		<version>x.x.x</version>
	</dependency>

Core is automatically pulled in via both [Artemis](../artemis) and [WebSocket](../websocket) so no need to include if you're using either of those. It's recommended you use the [BOM](../bom) to ensure you have the correct versions and assist in upgrading at a later date.

## Messaging ##

Citō has rich messaging functionality for based on CDI events.

### `SEND` ###

When a user sends a `cito.event.Message` to the server this can be recieved using the `@OnSend` annotation:

	public void onSend(@Observes @OnSend Message) { ... }
	
It is also possible to perform automatic serialisation of beans using the `@Body` annotation:

	public void onSend(@Observes @OnSend Message, @Body MyBean myBean) { ... }

`@OnSend` accepts a destination pattern to match. See the 'Destination Filtering' section.

### `SUBSCRIBE` & `UNSUBSCRIBE` ###

When a user subscribes or unsubscribes to a destination the event can be captured using the `@OnSubscribe` and `@OnUnsubscribe`:

	public void onSubscribe(@Observes @OnSubscribe Message) { ... }

Both `@OnSubscribe` and `@OnUnsubscribe` accept a destination pattern to match. See the 'Destination Filtering' section.

For the first subscription and last unsubscription to a topic see the following 'Destination Changed' section.

### Destination Changed ###

It's often desirable to be informed when the first user requests data and the last one leaves. This is especially useful in circumstances such as a rate subscription so not to create multiple subscriptions to a downstream system improving resource utilisation. Events are received using the CDI `@Observes` mechanism on `cito.event.DestinationChanged`:

	public void on(@Observes DestinationChanged) { ... }

In this circumstance all events will be passed to the method. It is also possible to filter the event based on addition or removal using `@OnAdded` and `@OnRemoved` annotations respectively:

	public void onAdded(@Observes @OnAdded DestinationChanged) { ... }

Both `@OnAdded` and `@OnRemoved` accept a destination pattern to match. See the 'Destination Filtering' section.


### Destination Filtering ###

A number of annotations related to subscriptions can accept a destination pattern which will be filtered against. The pattern matching is based on POSIX GLOB patterns. For example, the pattern `/topic/hello.*` will match anything with the base pattern:

* `/topic/hello.world` - Matches
* `/topic/hello.another-world` - Matches
* `/queue/hello.world` - Doesn't match!
* `/topic/hello.world.another` - Doesn't match!

See `cito.stomp.Glob` for more information.

In addition to matching patterns it's also possible to use path parameters, via curly braces parenthesis (`{}`) and `@PathParam` annotation for use within the method. For example:

	public void on(
			@Observes @OnAdded("/topic/{param}.world") DestinationChanged,
			@PathParam("param") String param)
	{
		// for '/topic/hello.world', 'param' will be 'hello'
	}

See `cito.PathParser` for more information.


### Sending & Broadcasting ###

Sending a message to a client can be achieved in three ways:

* Broadcast to all those subscribed to a topic,
* Broadcast to all sessions for a user subscribed to a topic or queue,
* Send to a specific session for a user subscribed to a topic or queue.

To perform these inject the `cito.stomp.server.Support` class:

	@Inject
	private Support support;

Any bean passed as a payload will be serialised to the `MediaType` before being sent to the user. The default type is `application/json`.

**Broadcast to All**

This is the simplest method of sending and will be sent to everyone:

	this.support.broadcast("/topic/hello-world", MediaType.TEXT_PLAIN, "Hello");

*Warning:* As this is sent to everyone it also has inherent issues with data security so use only when necessary. 

**Broadcast to User Sessions**

This will broadcast, but to all sessions for a specific user:

	final Principal user = ... // the principal representing the user

	this.support.broadcastTo(user, "/topic/hello-world", MediaType.TEXT_PLAIN, "Hello");

**Send to Session**

Finally the least granular approach will only send to a specific session of a user:

	final String sessionId = ... // the user session
	this.support.sendTo(sessionId, "/topic/hello-world", MediaType.TEXT_PLAIN, "Hello");


## Security ##

By default all detinations are permitted to all users. However, it may be essential to prevent access for a user with a specific role, or just those who have passed authorisation. To do this implement `SecurityCustomiser` class:

	@Dependent // preferred scope
	public class Customiser implements SecurityCustomiser {
		@Override
		public void configure(SecurityRegistry registry) {
			registry.builder().nullDestination().permitAll().build(); // important for most message types including CONNECT, DISCONNECT
			registry.builder().matches("/topic/rate.*").principleExists().build(); // user must be logged in
			registry.builder().matches("/topic/rate.EURUSD").roles("trader", "sales").build(); // user has roles 'trader' OR 'sales', logged in is implied
		}
	}

*Warn:* Depending on the authorisation scheme in your app NULL destinations, such as `CONNECT`, may need to circumvent security so authorisation can be done. An example of it's usage is above.

If using multiple `SecurityCustomiser` classes It is possible to ensure priority of addition to the registry by using the `javax.annotation.@Priority` annotation. By default they'll be assigned a priority of 5000 and therefore processed in the order they are given to the registry by the CDI implementation.

Using `SecurityCustomiser` class means the rules will be analysed at start up. If you wish to alter the limitations at runtime use the `SecurityRegistry` class directly.


## Scope ##

To assist with maintaining beans in line with WebSocket sessions you can use the `@WebSocketScope`. This will be active during:

* a new connection is created - see `@OnOpen`,
* a message is received for a session - see `@OnMessage`,
* an error is observed - see `@OnError`,
* the connection is closed - see `@OnClose`.

After a `@OnClose` all beans associated with the scope will be destroyed.

*Warn:* As it is only possible to have one WebSocket scope active per thread, events may not be propagated to a bean observing events that originated from a different scope.


## Serialisation ##

Both receive, via `@OnSend` and `@Body` annotations, and send, via `Support` permit automatic serialisation of beans based on MIME/Content-Type. At the moment only JSON (via [GSON](https://github.com/google/gson) and the Content-Type `application/json`) is supported. However, it is possible to add your own by implementing the `cito.ext.BodyReader` and `cito.ext.BodyWriter` interfaces and making them available to the CDI runtime.

It is also possible to use the serialisation features using the `cito.ext.Serialiser` class:

	@Inject
	private Serialiser serialiser;
	
	public void doSomething() {
		InputStream is = ...
		MyBean myBean = serialiser.readFrom(MyBean.class, MediaType.APPLICATION_JSON, is);
	}


# Potential Future Work #

Create GitHub Issues for these to permit voting.


## Message Sending Injection ##

	@Inject
	@OnSend("/topic/myTopic")
	private Send myTopic;

Viable?


## PathParam Conversion/Serialisation ##

Interface based:

	public interface Converter<T> {
		T fromString(String s);
		String toString(T v);
	}
	
...or static:

	public class MyClass {
		public static MyClass fromString(String s) {
			return ...
		}
	}

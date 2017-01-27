# Citō Core

Citō uses a number of technologies from the JEE 7 catalogue, namely:

* JMS - Interface between the Broker and STOMP,
* CDI - Dependency injection and context,
* JAX-RS - MediaTypes (will potentially be removed to reduce dependencies),

Both JMS, JAX-RS and CDI implementations must exist for it to work, and JAX-B for performing [de]serialisation binding. If you're just using Strings or binary data, then JAX-B is not required.

## Deployment Models ##

Citō can be deployed in fours ways: 

* Standalone, embedded broker,
* Clustered, embedded broker,
* Standalone, remove JMS broker,
* Clustered, remote JMS broker.

In its simplest form it utilises [Apache ActiveMQ Artemis](http://activemq.apache.org/artemis/) to act as an embedded broker. This is a very high performant broker that before the HornetQ codebase was donated to Apache it posted an 8 million messages/sec on [SPECjms2007](http://planet.jboss.org/post/8_2_million_messages_second_with_specjms) benchmark in addition to scaling well. I'd challenge any application to actually have a need to send more than 8 million messages/second, but the option is there.

## Messaging ##

Citō has rich messaging functionality for based on CDI events.

### `SEND` ###

When a user sends a message to the server this can be recieved using the `@OnSend` annotation:

	public void onSend(@Observes @OnSend MessageEvent) { ... }
	
It is also possible to perform automatic serialisation of beans using the `@Payload` annotation:

	public void onSend(@Observes @OnSend MessageEvent, @Payload MyBean myBean) { ... }

`@OnSend` accepts a destination pattern to match. See the 'Destination Filtering' section.

### `SUBSCRIBE` & `UNSUBSCRIBE` ###

When a user subscribes or unsubscribes to a destination the event can be captured using the `@OnSubscribe` and `@OnUnSubscribe`

	public void onSubscribe(@Observes @OnSubscribe MessageEvent) { ... }

Both `@OnSubscribe` and `@OnUnSubscribe` accept a destination pattern to match. See the 'Destination Filtering' section.

For the first subscription and last unsubscription to a topic see the following 'Destination Changed' section.

### Destination Changed ###

It's often desirable to be informed when the first user requests data and the last one leaves. This is especially useful in circumstances such as a rate subscription so not to create multiple subscriptions to a downstream system improving resource utilisation. Events are received using the CDI `@Observes` mechanism on `DestinationEvent`:

	public void on(@Observes DestinationEvent) { ... }

In this circumstance all events will be passed to the method. It is also possible to filter the event based on addition or removal using `@OnAdded` and `@OnRemoved` annotations respectively:

	public void onAdded(@Observes @OnAdded DestinationEvent) { ... }

Both `@OnAdded` and `@OnRemoved` accept a destination pattern to match. See the 'Destination Filtering' section.

### Destination Filtering ###

A number of annotations related to subscriptions can accept a destination pattern which will be filtered against. The pattern matching is based on POSIX GLOB patterns. For example, the pattern `/topic/hello.*` will match anything with the base pattern:

* `/topic/hello.world` - Matches
* `/topic/hello.another-world` - Matches
* `/queue/hello.world` - Doesn't match!
* `/topic/hello.world.another` - Doesn't match!

See `cito.stomp.Glob` for more information.

In addition to matching patterns it's also possible to use path parameters, via curly braces parenthesis ('{}') and `@PathParam` annotation for use within the method. For example:

	public void on(
			@Observes @OnAdded("/topic/{param}.world") DestinationEvent,
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

** Send to Session **

Finally the least granular approach will only send to a specific session of a user:

	final String sessionId = ... // the user session
	this.support.sendTo(sessionId, "/topic/hello-world", MediaType.TEXT_PLAIN, "Hello");

	
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

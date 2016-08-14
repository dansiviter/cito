# Citō Core

Citō uses a number of technologies from the JEE 7 catalogue, namely:

* JMS - Interface between the Broker and STOMP,
* JAX-RS - MediaTypes (will potentially be removed to reduce dependencies),
* CDI - Dependency injection and context,
* JAX-B - [De]Serialise data sent.

Both JMS, JAX-RS and CDI implementations must exist for it to work, and JAX-B for performing [de]serialisation binding. If you're just using Strings or binary data, then JAX-B is not required.

## Deployment Models ##

Citō can be deployed in fours ways: 

* Standalone, embedded broker,
* Clustered, embedded broker,
* Standalone, remove JMS broker,
* Clustered, remote JMS broker.

In its simplest form it utilises [Apache ActiveMQ Artemis](http://activemq.apache.org/artemis/) to act as an embedded broker. This is a very high performant broker that before the HornetQ codebase was donated to Apache it posted an 8 million messages/sec on [SPECjms2007](http://planet.jboss.org/post/8_2_million_messages_second_with_specjms) benchmark in addition to scaling well. I'd challenge any application to actually have a need to send more than 8 million messages/second, but the option is there.
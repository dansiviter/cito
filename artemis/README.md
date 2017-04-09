# Citō Artemis

This is a Apache ActiveMQ Artemis (formally HornetQ) broker implementation.

By default this will create an embedded broker and connect to that automatically. If another implementation of `cito.broker.artemis.BrokerConfig` is found then this can be configured to use your own embedded config or a remote instance. Either way `DestinationChanged`s should be produced on the addition and removal of destinations.

## Getting Started ##

First import the core into your project:

	<dependency>
		<groupId>io.cito</groupId>
		<artifactId>artemis</artifactId>
		<version>x.x.x</version>
	</dependency>

It's recommended you use the [BOM](../bom) to ensure you have the correct versions and assist in upgrading at a later date.
# Citō Artemis

This is a Apache ActiveMQ Artemis (formally HornetQ) broker implementation.

By default this will create an embedded broker and connect to that automatically. If another implementation of `cito.broker.artemis.BrokerConfig` is found then this can be configured to use your own embedded config or a remote instance. Either way `DestinationEvent`s should be produce on the addition and removal of destinations.
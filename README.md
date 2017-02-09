# Citō (Working Title)

> /ˈki.to/ quickly, speedily, soon

Citō was created for a need for a simple, standards based, high performance, auditable, secure, high availability, bi-directional streaming technology. The author come from a background of designing and developing financial applications piping data across the web to an international user base where they've had their hands tied by inflexible and buggy proprietary technologies.

To achieve that it leverages JEE7 technology stack to ensure ease of integration, multiple deployment methods and flexible broker interoperability. In its simplest form it can run embedded (standalone or clustered) scaling to 10's of thousands of users with millions of messages on modest hardware. All this whilst being auditable and proving metrics and information for monitoring ensuring quality of service can be maintained.

For usage information see the individual projects:

* [Core](/core) - The crux of the technology,
* [Artemis](/artemis) - Connects to a Apache ActiveMQ Artemis broker implementation either embedded or remotely,
* [WebSocket](/websocket) - A basic WebSocket endpoint for those who don't need graceful fallback and browser compatibility,
* [SockJS](/sockjs) - A SockJS based endpoint to provide a high level of compatibility with legacy browsers and assist with piping data through corporate firewalls. **In development!**

## Frequently Asked Questions ##

**Why don't I just use a WebSocket broker directly?**

Citō adds value in three main ways:

* Broad Messaging Features: Eventing on receiving messages, subscriptions, etc,
* Simplicity and Flexibility of setup: Permitting complex topologies if required, but generally unnecessary,
* Unified Point of Origin: Streaming/Push data and ReST in one place promoting supportability and ease of scaling,

If you don't need the features then, by all means, don't use Citō. Simplicity is at the core adding a technology that's not needed goes against this.
[![Travis](https://img.shields.io/travis/dansiviter/cito/master.svg?style=flat-square)](https://travis-ci.org/dansiviter/cito) [![Codacy grade](https://img.shields.io/codacy/grade/61414d4b660e4afe9f463a7ece1dcfc1.svg?style=flat-square)](https://www.codacy.com/app/dansiviter/cito)

# Citō

> /ˈki.to/ quickly, speedily, soon

Citō was created for a need for a simple, standards based, high performance, auditable, secure, high availability, bi-directional streaming technology. The author come from a background of designing and developing financial applications piping data across the web to an international user base where they've had their hands tied by inflexible and buggy proprietary technologies.

To achieve that it leverages JEE7 technology stack to ensure ease of integration, multiple deployment methods and flexible broker interoperability. In its simplest form it can run embedded (standalone or clustered) scaling easily.

For usage information see the individual projects:

* [Core](/core) - The crux of the technology,
* [Artemis](/artemis) - Connects to a Apache ActiveMQ Artemis broker implementation for either embedded or remote deployment,
* [WebSocket](/websocket) - A basic WebSocket endpoint for those who don't need graceful fallback and browser compatibility,
* [SockJS](/sockjs) - A SockJS based endpoint to provide a high level of compatibility with legacy browsers and assist with piping data through corporate firewalls.
* [Bill of Materials](/bom) - A importable POM to assist with aligning version of modules and upgrades.


## Frequently Asked Questions ##

**Why don't I just use a WebSocket broker directly?**

Citō adds value in three main ways:

* Broad Messaging Features: events on receiving messages, first subscribe, last unsubscribe, etc,
* Simplicity and Flexibility of setup: Permitting complex topologies if required, but generally unnecessary,
* Unified Point of Origin: Streaming/Push data and ReST in one place promoting ease of scaling, support and maintenance,

If you don't need the features then, by all means, don't use Citō. Simplicity is at the core adding a technology that's not needed goes against this.

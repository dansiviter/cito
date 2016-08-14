package cito.stomp.server.example;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import cito.stomp.server.annotation.OnMessage;
import cito.stomp.server.annotation.OnSubscribe;
import cito.stomp.server.event.Message;

@ApplicationScoped
public class Foo {

	
	
	public void onSubscribeQueue(@Observes @OnSubscribe("/queue/.*") Message msg) {
		System.out.println("### OnSubscrbe queue called: " + msg.frame.getDestination());
	}
	
	public void onSubscribeTopic(@Observes @OnSubscribe("/topic/.*") Message msg) {
		System.out.println("### OnSubscrbe topic called: " + msg.frame.getDestination());
	}

	public void onMessageTopic(@Observes @OnMessage("/topic/.*") Message msg) {
		System.out.println("### OnMessage topic called: " + msg.frame.getDestination());
	}
}

package civvi.example;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Named;

import civvi.messaging.annotation.OnMessage;
import civvi.messaging.annotation.OnSubscribe;
import civvi.stomp.Frame;

@Named
@ApplicationScoped
public class MyApplicationClass {
	public void onMessage(@Observes @OnMessage Frame frame) {
		System.out.println(getClass().getSimpleName() + ":onMessage");
	}

	public void onSubscription(@Observes @OnSubscribe("this") Frame frame) {
		System.out.println(getClass().getSimpleName() + ":onSubscription");		
	}
}

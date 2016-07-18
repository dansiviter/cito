package civvi.example;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.inject.Named;

import civvi.messaging.annotation.OnMessage;
import civvi.messaging.annotation.OnSubscribe;
import civvi.stomp.Frame;

@Named
@SessionScoped
public class MySessionClass implements Serializable {
	private static final long serialVersionUID = -7408317968087838699L;

	public void onMessage(@Observes @OnMessage Frame frame) {
		System.out.println(getClass().getSimpleName() + ":onMessage");
	}

	public void onSubscription(@Observes @OnSubscribe("another") Frame frame) {
		System.out.println(getClass().getSimpleName() + ":onSubscription");		
	}
}

package civvi.activemq;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [19 Jul 2016]
 */
public class WebSocketContainerProducer {
	@Produces @ApplicationScoped
	public WebSocketContainer container() {
		return ContainerProvider.getWebSocketContainer();
	}
}

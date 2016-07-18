package civvi.messaging;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;

import org.junit.Test;

import civvi.stomp.Client;

public class ConnectionTest extends AbstractTest {
	@Test
	public void connect() throws DeploymentException, IOException, EncodeException, InterruptedException, ExecutionException, TimeoutException {
		final Client client = getClient();
		client.connect(10, TimeUnit.SECONDS);
		assertEquals(Client.State.CONNECTED, client.getState());
	}

}

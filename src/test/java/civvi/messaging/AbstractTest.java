package civvi.messaging;


import java.net.URI;

import javax.inject.Inject;
import javax.servlet.ServletException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.wildfly.swarm.ContainerFactory;
import org.wildfly.swarm.arquillian.adapter.InVM;
import org.wildfly.swarm.container.Container;

import civvi.stomp.Client;
import io.undertow.servlet.api.ServletContainer;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [15 Jul 2016]
 */
@RunWith(Arquillian.class)
@RunAsClient 
@InVM
public class AbstractTest implements ContainerFactory {
	@Inject
	protected Logger log;
	////	private final int port;
	//
	@ArquillianResource
	private URI baseURL;
	private Client client;
	//
	//	protected AbstractTest() {
	//		this.log = LoggerFactory.getLogger(getClass());
	////		this.port = findPort();
	////		this.log.info("Creating test http listener on port ''{}''.", port);
	//	}

	@Deployment(testable = false)
	public static Archive<WebArchive> createDeployment() {
		return ShrinkWrap
				.create(WebArchive.class)
				.addPackages(true, "javax.ws.rs")
				.addPackages(true, "civvi")
				.addPackages(true, "io.undertow.websockets")
				//				.addClasses(WebSocketServer.class, FrameEncoding.class, LogProvider.class, WebSocketSessionRegistry.class)
				//	                .addClasses(TestSuiteEnvironment.class, Alpha.class, Bravo.class, ComponentInterceptorBinding.class,
				//	                        ComponentInterceptor.class).addClasses(InjectionSupportTestCase.constructTestsHelperClasses)
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
				.addAsManifestResource(new StringAsset("io.undertow.websockets.jsr.UndertowContainerProvider"),
						"services/javax.websocket.ContainerProvider")
				.addAsManifestResource(new StringAsset("civvi.messaging.MessagingExtension"),
						"services/javax.enterprise.inject.spi.Extension")
				/*	.addAsManifestResource(createPermissionsXmlAsset(
						// Needed for the TestSuiteEnvironment.getServerAddress() and TestSuiteEnvironment.getHttpPort()
						new PropertyPermission("management.address", "read"),
						new PropertyPermission("node0", "read"),
						new PropertyPermission("jboss.http.port", "read"),
						// Needed for the serverContainer.connectToServer()
						new SocketPermission("*:" + TestSuiteEnvironment.getHttpPort(), "connect,resolve")),
						"permissions.xml")*/;
	}

	@Before
	public void setUp() throws ServletException {
		setUpClient();
	}

	private void setUpClient() {
		this.client = new Client(URI.create(this.baseURL.toString() + "/websocket"));
	}

	protected Client getClient() {
		return this.client;
	}
	//
	////	protected int getPort() {
	////		return this.port;
	////	}
	//
	@Override
	public Container newContainer(String... arg0) throws Exception {
		Container container = new Container();
		// ... configure the container ...
		return container;
	}

	//	/**
	//	 * Override to supply own port.
	//	 * 
	//	 * @return the port to run the test on. By default this will let the OS find a free port.
	//	 */
	//	private int findPort() {
	//		try (final ServerSocket socket = new ServerSocket(0)) {
	//			return socket.getLocalPort();
	//		} catch (IOException e) {
	//			throw new RuntimeException("I should never happen!", e);
	//		}
	//	}
	//
	//	@After
	//	public void tearDown() throws IOException {
	//		this.client.close();
	//		//		this.undertow.stop();
	//	}
}

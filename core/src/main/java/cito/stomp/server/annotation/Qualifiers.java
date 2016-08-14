package cito.stomp.server.annotation;

import javax.enterprise.util.AnnotationLiteral;

import cito.DestinationEvent.Type;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [19 Jul 2016]
 */
public enum Qualifiers { ;
	/**
	 * 
	 * 
	 * @param value
	 * @return
	 */
	public static FromClient fromClient() {
		return new FromSessionLiteral();
	}

	/**
	 * 
	 * @return
	 */
	public static FromBroker fromBroker() {
		return new FromBrokerLiteral();
	}
	
	/**
	 * 
	 * @return
	 */
	public static OnOpen onOpen() {
		return new OnOpenLiteral();
	}

	/**
	 * 
	 * @return
	 */
	public static OnClose onClose() {
		return new OnCloseLiteral();
	}

	/**
	 * 
	 * @return
	 */
	public static OnConnected onConnected() {
		return new OnConnectedLiteral();
	}

	/**
	 * 
	 * @return
	 */
	public static OnDisconnect onDisconnect() {
		return new OnDisconnectLiteral();
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	public static OnSubscribe onSubscribe(String value) {
		return new OnSubscribeLiteral(value == null ? "" : value);
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	public static OnUnsubscribe onUnsubscribe(String value) {
		return new OnUnsubscribeLiteral(value == null ? "" : value);
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	public static OnMessage onMessage(String value) {
		return new OnMessageLiteral(value == null ? "" : value);
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	public static OnDestination onDestinaton(Type type) {
		return new OnDestinationLiteral(type);
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [19 Jul 2016]
	 */
	private static class FromSessionLiteral extends AnnotationLiteral<FromClient> implements FromClient {
		private static final long serialVersionUID = -8517560200257874201L;
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [19 Jul 2016]
	 */
	private static class FromBrokerLiteral extends AnnotationLiteral<FromBroker> implements FromBroker {
		private static final long serialVersionUID = -8063702574657375558L;
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [19 Jul 2016]
	 */
	private static class OnOpenLiteral extends AnnotationLiteral<OnOpen> implements OnOpen {
		private static final long serialVersionUID = 3720802781401347308L;
	}
	
	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [19 Jul 2016]
	 */
	private static class OnCloseLiteral extends AnnotationLiteral<OnClose> implements OnClose {
		private static final long serialVersionUID = 7312343801462080191L;
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [12 Jul 2016]
	 */
	private static class OnConnectedLiteral extends AnnotationLiteral<OnConnected> implements OnConnected {
		private static final long serialVersionUID = 7181578239085215334L;
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [12 Jul 2016]
	 */
	private static class OnDisconnectLiteral extends AnnotationLiteral<OnDisconnect> implements OnDisconnect {
		private static final long serialVersionUID = 5499212867196812308L;
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [12 Jul 2016]
	 */
	private static class OnSubscribeLiteral extends AnnotationLiteral<OnSubscribe> implements OnSubscribe {
		private static final long serialVersionUID = 6498352376982414158L;

		private final String value;

		public OnSubscribeLiteral(String value) {
			this.value = value;
		}

		@Override
		public String value() {
			return value;
		}
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [12 Jul 2016]
	 */
	private static class OnUnsubscribeLiteral extends AnnotationLiteral<OnUnsubscribe> implements OnUnsubscribe {
		private static final long serialVersionUID = -1338083530350484474L;

		private final String value;

		public OnUnsubscribeLiteral(String value) {
			this.value = value;
		}

		@Override
		public String value() {
			return value;
		}
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [12 Jul 2016]
	 */
	private static class OnMessageLiteral extends AnnotationLiteral<OnMessage> implements OnMessage {
		private static final long serialVersionUID = 6498352376982414158L;

		private final String value;

		public OnMessageLiteral(String value) {
			this.value = value;
		}

		@Override
		public String value() {
			return value;
		}
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [18 Jul 2016]
	 */
	public static class OnDestinationLiteral extends AnnotationLiteral<OnDestination> implements OnDestination {
		private static final long serialVersionUID = -5817203291593199363L;

		private final Type type;

		public OnDestinationLiteral(Type type) {
			this.type = type;
		}

		@Override
		public Type type() {
			return this.type;
		}
	}
}


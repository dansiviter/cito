package civvi.messaging.annotation;

import javax.enterprise.util.AnnotationLiteral;

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
	public static OnError onError() {
		return new OnErrorLiteral();
	}

	/**
	 * 
	 * @return
	 */
	public static OnConnect onConnect() {
		return new OnConnectLiteral();
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
	public static OnMessage onMessage(String value) {
		return new OnMessageLiteral(value == null ? "" : value);
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
		private static final long serialVersionUID = -223554034460528022L;
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
	 * @since v1.0 [19 Jul 2016]
	 */
	private static class OnErrorLiteral extends AnnotationLiteral<OnError> implements OnError {
		private static final long serialVersionUID = -4264326777839946453L;
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [12 Jul 2016]
	 */
	private static class OnConnectLiteral extends AnnotationLiteral<OnConnect> implements OnConnect {
		private static final long serialVersionUID = 6498352376982414158L;
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
}


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


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [19 Jul 2016]
	 */
	private static class FromSessionLiteral
	extends AnnotationLiteral<FromClient> implements FromClient { }

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [19 Jul 2016]
	 */
	private static class FromBrokerLiteral extends AnnotationLiteral<FromBroker> implements FromBroker { }

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [19 Jul 2016]
	 */
	private static class OnOpenLiteral extends AnnotationLiteral<OnOpen> implements OnOpen { }

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [19 Jul 2016]
	 */
	private static class OnCloseLiteral extends AnnotationLiteral<OnClose> implements OnClose { }
	
	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [19 Jul 2016]
	 */
	private static class OnErrorLiteral extends AnnotationLiteral<OnError> implements OnError { }
}


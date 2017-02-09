/*
 * Copyright 2016-2017 Daniel Siviter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cito.annotation;

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
		return new FromClientLiteral();
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
	 * @param value
	 * @return
	 */
	public static FromServer fromServer() {
		return new FromServerLiteral();
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
	public static OnError onError() {
		return new OnErrorLiteral();
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
	public static OnSend onSend(String value) {
		return new OnSendLiteral(value == null ? "" : value);
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [19 Jul 2016]
	 */
	private static class FromClientLiteral extends AnnotationLiteral<FromClient> implements FromClient {
		private static final long serialVersionUID = -8517560200257874201L;
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [19 Jul 2016]
	 */
	private static class FromServerLiteral extends AnnotationLiteral<FromServer> implements FromServer {
		private static final long serialVersionUID = 1133434815775889010L;
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
	private static class OnErrorLiteral extends AnnotationLiteral<OnError> implements OnError {
		private static final long serialVersionUID = -2764506588497003639L;
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
	private static class OnSendLiteral extends AnnotationLiteral<OnSend> implements OnSend {
		private static final long serialVersionUID = 6498352376982414158L;

		private final String value;

		public OnSendLiteral(String value) {
			this.value = value;
		}

		@Override
		public String value() {
			return value;
		}
	}
}


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

import java.util.Arrays;
import java.util.Objects;

import javax.enterprise.util.AnnotationLiteral;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [19 Jul 2016]
 */
public enum Qualifiers { ;
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

		@Override
		public int hashCode() {
			return Objects.hashCode(this.value);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj) || getClass() != obj.getClass())
				return false;
			OnSubscribeLiteral other = (OnSubscribeLiteral) obj;
			return Objects.equals(value, other.value);
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

		@Override
		public int hashCode() {
			return Objects.hashCode(this.value);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj) || getClass() != obj.getClass())
				return false;
			OnUnsubscribeLiteral other = (OnUnsubscribeLiteral) obj;
			return Objects.equals(value, other.value);
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

		@Override
		public int hashCode() {
			return Objects.hashCode(this.value);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj) || getClass() != obj.getClass())
				return false;
			OnSendLiteral other = (OnSendLiteral) obj;
			return Objects.equals(value, other.value);
		}
	}
}


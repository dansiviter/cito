package civvi.messaging.annotation;

import javax.enterprise.util.AnnotationLiteral;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [12 Jul 2016]
 */
public class OnSubscribeLiteral extends AnnotationLiteral<OnSubscribe> implements OnSubscribe {
	private static final long serialVersionUID = 6498352376982414158L;

	private final String value;

	public OnSubscribeLiteral(String value) {
		this.value = value;
	}

	public OnSubscribeLiteral() {
		value = "";
	}

	@Override
	public String value() {
		return value;
	}
}

package civvi.messaging.annotation;

import javax.enterprise.util.AnnotationLiteral;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [12 Jul 2016]
 */
public class OnMessageLiteral extends AnnotationLiteral<OnMessage> implements OnMessage {
	private static final long serialVersionUID = 6498352376982414158L;
	
	private final String value;

	public OnMessageLiteral(String value) {
		this.value = value;
	}

	public OnMessageLiteral() {
		value = "";
	}

	@Override
	public String value() {
		return value;
	}
}

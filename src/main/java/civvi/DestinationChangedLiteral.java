package civvi;

import javax.enterprise.util.AnnotationLiteral;

import civvi.DestinationEvent.Type;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [18 Jul 2016]
 */
public class DestinationChangedLiteral extends AnnotationLiteral<DestinationChanged> implements DestinationChanged {
	private static final long serialVersionUID = -5817203291593199363L;

	private final Type type;

	public DestinationChangedLiteral(Type type) {
		this.type = type;
	}

	@Override
	public Type type() {
		return this.type;
	}
}

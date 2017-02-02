package cito.annotation;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.util.AnnotationLiteral;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [30 Aug 2016]
 */
public class RolesAllowedLiteral extends AnnotationLiteral<RolesAllowed> implements RolesAllowed {
	private static final long serialVersionUID = 1L;

	private final String[] value;

	public RolesAllowedLiteral(String[] value) {
		this.value = value;
	}

	@Override
	public String[] value() {
		return value;
	}
}

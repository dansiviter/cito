package cito.annotation;

import javax.annotation.security.PermitAll;
import javax.enterprise.util.AnnotationLiteral;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [30 Aug 2016]
 */
public class PermitAllLiteral extends AnnotationLiteral<PermitAll> implements PermitAll {
	private static final long serialVersionUID = 4966203528556218429L;
}

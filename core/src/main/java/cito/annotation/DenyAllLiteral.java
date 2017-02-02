package cito.annotation;

import javax.annotation.security.DenyAll;
import javax.enterprise.util.AnnotationLiteral;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [30 Aug 2016]
 */
public class DenyAllLiteral extends AnnotationLiteral<DenyAll> implements DenyAll {
	private static final long serialVersionUID = -2619507648848832423L;
}

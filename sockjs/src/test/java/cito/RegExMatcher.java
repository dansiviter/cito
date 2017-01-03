package cito;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [3 Jan 2017]
 */
public class RegExMatcher extends BaseMatcher<CharSequence> {
	private final String regEx;

	public RegExMatcher(String regEx){
		this.regEx = regEx;
	}

	@Override
	public boolean matches(Object o){
		return o.toString().matches(regEx);

	}

	@Override
	public void describeTo(Description description){
		description.appendText("matches regex=");
	}

	// --- Static methods ---

	/**
	 * 
	 * @param regex
	 * @return
	 */
	public static RegExMatcher regEx(String regex){
		return new RegExMatcher(regex);
	}
}
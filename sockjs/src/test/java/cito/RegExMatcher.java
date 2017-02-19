package cito;

import java.util.regex.Pattern;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [3 Jan 2017]
 */
public class RegExMatcher extends BaseMatcher<CharSequence> {
	private final Pattern regEx;

	public RegExMatcher(String regEx){
		this.regEx = Pattern.compile(regEx);
	}

	@Override
	public boolean matches(Object o){
		return o == null ? false : this.regEx.matcher(o.toString()).matches();
	}

	@Override
	public void describeTo(Description description){
		description.appendText("matches regex=").appendText(this.regEx.pattern());
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
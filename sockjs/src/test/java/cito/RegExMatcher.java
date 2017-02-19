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
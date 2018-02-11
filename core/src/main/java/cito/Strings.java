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

import static java.lang.Character.isWhitespace;

/**
 * String utilities.
 * 
 * @author Daniel Siviter
 * @since v1.0 [8 Jul 2017]
 */
public enum Strings { ;
	/**
	 * Verifies if the {@link String} is empty. This could be if it is {@code null} or empty.
	 * 
	 * @param seq the character sequence to check.
	 * @return {@code true} if empty.
	 */
	public static boolean isEmpty(CharSequence seq) {
		return seq == null || seq.length() == 0;
	}

	/**
	 * Verifies if the {@link String} is empty. This could be if it is {@code null}, empty, or just whitespace.
	 * 
	 * @param seq the character sequence to check.
	 * @return {@code true} if blank.
	 */
	public static boolean isBlank(CharSequence seq) {
		if (isEmpty(seq)) {
			return true;
		}
		for (int i = 0; i < seq.length(); i++) {
			if (!isWhitespace(seq.charAt(i))) {
				return false;
			}
		}
		return true;
	}
}

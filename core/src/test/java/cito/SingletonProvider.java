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

import javax.inject.Provider;

/**
 * A basic value holder that conforms to the {@link Provider} interface.
 * 
 * @author Daniel Siviter
 * @since v1.0 [28 Mar 2017]
 */
public class SingletonProvider<T> implements Provider<T> {
	private final T value;

	/**
	 * 
	 * @param value
	 */
	public SingletonProvider(T value) {
		this.value = value;
	}

	@Override
	public T get() {
		return this.value;
	}
}

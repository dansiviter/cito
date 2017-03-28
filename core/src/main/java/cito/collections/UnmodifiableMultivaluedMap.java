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
package cito.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.ws.rs.core.MultivaluedMap;

/**
 * A unmodifiable {@link MultivaluedMap}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [28 Mar 2017]
 */
@Immutable
public class UnmodifiableMultivaluedMap<K, V> implements MultivaluedMap<K, V> {
	private final MultivaluedMap<K, V> delegate;

	public UnmodifiableMultivaluedMap(@Nonnull MultivaluedMap<K, V> delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	public void putSingle(K k, V v) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(K k, V v) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V getFirst(K key) {
		return this.delegate.getFirst(key);
	}

	@Override
	@SafeVarargs
	public final void addAll(K k, V... vs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addAll(K k, List<V> list) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addFirst(K k, V v) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equalsIgnoreValueOrder(MultivaluedMap<K, V> omap) {
		return this.delegate.equalsIgnoreValueOrder(omap);
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Entry<K, List<V>>> entrySet() {
		return Collections.unmodifiableSet(this.delegate.entrySet());
	}

	@Override
	public List<V> get(Object key) {
		return this.delegate.get(key);
	}

	@Override
	public Set<K> keySet() {
		return Collections.unmodifiableSet(this.delegate.keySet());
	}

	@Override
	public List<V> put(K key, List<V> value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(Map<? extends K, ? extends List<V>> m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<V> remove(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<List<V>> values() {
		return Collections.unmodifiableCollection(this.delegate.values());
	}

	@Override
	public boolean containsKey(Object o) {
		return this.delegate.containsKey(o);
	}

	@Override
	public boolean containsValue(Object o) {
		return this.delegate.containsValue(o);
	}

	@Override
	public int size() {
		return this.delegate.size();
	}

	@Override
	public boolean isEmpty() {
		return this.delegate.isEmpty();
	}
}

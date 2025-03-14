/*
 * #%L
 * ELK Reasoner
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2011 Department of Computer Science, University of Oxford
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.semanticweb.elk.util.collections;

import java.io.IOException;
import java.io.Writer;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.collections.api.multimap.MutableMultimap;
import org.eclipse.collections.impl.factory.Multimaps;

/**
 * Some useful static methods for collections
 * 
 * @author "Yevgeny Kazakov"
 * @author Peter Skocovsky
 */
public class Operations {

	public static final MutableMultimap<?, ?> EMPTY_MULTIMAP = Multimaps.mutable.list.empty();

	@SuppressWarnings("unchecked")
	public static <S, T> MutableMultimap<S, T> emptyMultimap() {
		return (MutableMultimap<S, T>) EMPTY_MULTIMAP;
	}

	@SafeVarargs
	public static <T> Iterable<T> concat(
			final Iterable<? extends T>... inputs) {
		return concat(Arrays.asList(inputs));
	}

	public static <T> Iterator<T> singletonIterator(final T element) {
		return new Iterator<T>() {
			boolean hasNext = true;

			@Override
			public boolean hasNext() {
				return hasNext;
			}

			@Override
			public T next() {
				hasNext = false;
				return element;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public static <T> Iterable<T> singleton(final T element) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return singletonIterator(element);
			}
		};
	}

	/**
	 * Concatenates several {@link Iterable}s into one
	 * 
	 * @param inputs
	 *            the {@link Iterable} of {@link Iterable}s to be concatenated
	 * @param <T>
	 *            the type of elements of {@link Iterable}s
	 * @return {@link Iterable} consisting of all elements found in input
	 *         {@link Iterable}s
	 */
	public static <T> Iterable<T> concat(
			final Iterable<? extends Iterable<? extends T>> inputs) {
		assert inputs != null;

		return new Iterable<T>() {

			@Override
			public Iterator<T> iterator() {

				return new Iterator<T>() {
					Iterator<? extends Iterable<? extends T>> outer = inputs
							.iterator();

					Iterator<? extends T> inner;
					boolean hasNext = advance();

					@Override
					public boolean hasNext() {
						return hasNext;
					}

					@Override
					public T next() {
						if (hasNext) {
							T result = inner.next();
							hasNext = advance();
							return result;
						}
						throw new NoSuchElementException();
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}

					boolean advance() {
						while (true) {
							if (inner != null && inner.hasNext())
								return true;

							if (outer.hasNext())
								inner = outer.next().iterator();
							else
								return false;
						}
					}
				};
			}
		};
	}

	/**
	 * @param <T>
	 *            the type of elements of iterators
	 * @param first
	 *            the elements that should come first
	 * @param second
	 *            the elements that should come next
	 * @return an iterator that iterates that advances the two given iterators
	 *         at the same time and returns the values of the second iterator
	 */
	public static <T> Iterator<T> synchronize(Iterator<?> first,
			Iterator<? extends T> second) {

		return new Iterator<T>() {

			@Override
			public boolean hasNext() {
				return first.hasNext() && second.hasNext();
			}

			@Override
			public T next() {
				first.next();
				return second.next();
			}

		};

	}
	
	/**
	 * Splits the input {@link Iterable} on batches with at most given number of
	 * elements.
	 *  
	 * @param elements
	 *            the {@link Iterable} to be split
	 * @param <T>
	 *            the type of elements in the {@link Iterable}
	 * @param batchSize
	 *            the maximal number of elements in batches
	 * @return a {@link Iterable} of batches containing elements from the input
	 *         collection
	 * @see #concat(Iterable)
	 */
	public static <T> Iterable<ArrayList<T>> split(
			final Iterable<? extends T> elements, final int batchSize) {
		return new Iterable<ArrayList<T>>() {

			@Override
			public Iterator<ArrayList<T>> iterator() {
				return new Iterator<ArrayList<T>>() {

					final Iterator<? extends T> elementsIterator = elements
							.iterator();

					@Override
					public boolean hasNext() {
						return elementsIterator.hasNext();
					}

					@Override
					public ArrayList<T> next() {
						final ArrayList<T> nextBatch = new ArrayList<T>(
								batchSize);
						int count = 0;
						while (count++ < batchSize
								&& elementsIterator.hasNext()) {
							nextBatch.add(elementsIterator.next());
						}
						return nextBatch;
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException(
								"Deletion is not supported");
					}
				};
			}
		};
	}

	/**
	 * Splits the input {@link Collection} on batches with at most given number
	 * of elements.
	 * 
	 * @param elements
	 *            the {@link Collection} to be split
	 * @param <T>
	 *            the type of elements in the {@link Collection}           
	 * @param batchSize
	 *            the maximal number of elements in batches
	 * @return a {@link Collection} of batches containing elements from the
	 *         input collection
	 */
	public static <T> Collection<ArrayList<T>> split(
			final Collection<? extends T> elements, final int batchSize) {
		return new AbstractCollection<ArrayList<T>>() {

			@Override
			public Iterator<ArrayList<T>> iterator() {
				Iterable<ArrayList<T>> iterable = split(
						(Iterable<? extends T>) elements, batchSize);
				return iterable.iterator();
			}

			@Override
			public int size() {
				// rounding up
				return (elements.size() + batchSize - 1) / batchSize;
			}
		};
	}

	public static <T> Collection<T> getCollection(final Iterable<T> iterable,
			final int size) {
		return new AbstractCollection<T>() {

			@Override
			public Iterator<T> iterator() {
				return iterable.iterator();
			}

			@Override
			public int size() {
				return size;
			}
		};
	}

	/**
	 * @param <T>
	 *            the type of elements
	 * @param input
	 *            the input iterator over these elements
	 * @param condition
	 *            the condition used for filtering
	 * @return the filtered iterator
	 * 
	 */	
	public static <T> Iterable<T> filter(final Iterable<? extends T> input,
			final Condition<? super T> condition) {
		assert input != null;

		return new Iterable<T>() {

			@Override
			public Iterator<T> iterator() {

				return new Iterator<T>() {
					Iterator<? extends T> i = input.iterator();
					T next;
					boolean hasNext = advance();

					@Override
					public boolean hasNext() {
						return hasNext;
					}

					@Override
					public T next() {
						if (hasNext) {
							T result = next;
							hasNext = advance();
							return result;
						}
						throw new NoSuchElementException();
					}

					@Override
					public void remove() {
						i.remove();
					}

					boolean advance() {
						while (i.hasNext()) {
							next = i.next();
							if (condition.holds(next))
								return true;
						}
						return false;
					}
				};
			}
		};
	}

	@SuppressWarnings("unchecked")
	public static <T, S> Iterable<T> filter(final Iterable<S> input,
			final Class<T> type) {

		return (Iterable<T>) filter(input, new Condition<S>() {
			@Override
			public boolean holds(S element) {
				return type.isInstance(element);
			}
		});
	}

	/**
	 * Returns read-only view of the given set consisting of the elements
	 * satisfying a given condition, if the number of such elements is known
	 * 
	 * @param <T>
	 *            the type of elements
	 * @param input
	 *            the given set to be filtered
	 * @param condition
	 *            the condition used for filtering the set. Must be consistent
	 *            with equals() for T, that is: a.equals(b) must imply that
	 *            holds(a) == holds(b)
	 * @param size
	 *            the number of elements in the filtered set
	 * @return the set consisting of the elements of the input set satisfying
	 *         the given condition
	 */
	public static <T> Set<T> filter(final Set<? extends T> input,
			final Condition<? super T> condition, final int size) {
		return new Set<T>() {

			@Override
			public int size() {
				return size;
			}

			@Override
			public boolean isEmpty() {
				return size == 0;
			}

			@Override
			@SuppressWarnings("unchecked")
			public boolean contains(Object o) {

				if (!input.contains(o))
					return false;

				T elem = null;

				try {
					elem = (T) o;
				} catch (ClassCastException cce) {
					return false;
				}
				/*
				 * here's why the condition must be consistent with equals(): we
				 * check it on the passed element while we really need to check
				 * it on the element which is in the underlying set (and is
				 * equal to o according to equals()). However, as long as the
				 * condition is consistent, the result will be the same.
				 */
				return condition.holds(elem);
			}

			@Override
			public Iterator<T> iterator() {
				Iterable<T> iterable = filter(input, condition);
				return iterable.iterator();
			}

			@Override
			public Object[] toArray() {
				Object[] result = new Object[size];
				int i = 0;
				for (Object o : filter(input, condition)) {
					result[i++] = o;
				}
				return result;
			}

			@Override
			public <S> S[] toArray(S[] a) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean add(T e) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean remove(Object o) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean containsAll(Collection<?> c) {
				for (Object o : c) {
					if (contains(o))
						return false;
				}
				return true;
			}

			@Override
			public boolean addAll(Collection<? extends T> c) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean retainAll(Collection<?> c) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean removeAll(Collection<?> c) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void clear() {
				throw new UnsupportedOperationException();
			}

		};

	}

	/**
	 * Transformations of input values to output values
	 * 
	 * @param <I>
	 *            the type of the input of the transformation
	 * @param <O>
	 *            the type of the output of the transformation
	 */
	public interface Transformation<I, O> {
		/**
		 * Transforms the input element
		 * 
		 * @param element
		 *            the element to be transformed
		 * @return the result of the transformation
		 */
		public O transform(I element);
	}

	// TODO: get rid of Conditions in favour of transformations

	/**
	 * Transforms elements using a given {@link Transformation} the output
	 * elements consist of the result of the transformation in the same order;
	 * if the transformation returns {@code null}, it is not included in the
	 * output
	 * 
	 * @param <I>
	 *            the type of input of elements
	 * @param <O>
	 *            the type of output elements
	 * @param input
	 *            the input elements
	 * @param transformation
	 *            the transformation for elements
	 * @return the transformed output elements
	 * 
	 */
	public static <I, O> Iterable<O> map(final Iterable<I> input,
			final Transformation<? super I, O> transformation) {
		assert input != null;

		return new Iterable<O>() {

			@Override
			public Iterator<O> iterator() {

				return new Iterator<O>() {
					Iterator<I> i = input.iterator();
					O next;
					boolean hasNext = advance();

					@Override
					public boolean hasNext() {
						return hasNext;
					}

					@Override
					public O next() {
						if (hasNext) {
							O result = next;
							hasNext = advance();
							return result;
						}
						throw new NoSuchElementException();
					}

					@Override
					public void remove() {
						i.remove();
					}

					boolean advance() {
						while (i.hasNext()) {
							next = transformation.transform(i.next());
							if (next != null)
								return true;
						}
						return false;
					}
				};
			}
		};
	}

	public static <I, O> Iterable<O> mapConcat(final Iterable<I> input,
			final Transformation<? super I, Iterable<O>> transformation) {
		assert input != null;

		return new Iterable<O>() {

			@Override
			public Iterator<O> iterator() {
				return mapConcat(input.iterator(), transformation);
			}
		};
	}

	public static <I, O> Collection<O> map(final Collection<I> input,
			final Transformation<? super I, O> transformation) {
		assert input != null;

		return new AbstractCollection<O>() {

			@Override
			public Iterator<O> iterator() {
				return map(input.iterator(), transformation);
			}

			@Override
			public int size() {
				// this is an upper bound
				return input.size();
			}

		};
	}

	public static <I, O> Iterator<O> map(final Iterator<I> input,
			final Transformation<? super I, O> transformation) {
		return new Iterator<O>() {
			O next;
			boolean hasNext = advance();

			@Override
			public boolean hasNext() {
				return hasNext;
			}

			@Override
			public O next() {
				if (hasNext) {
					O result = next;
					hasNext = advance();
					return result;
				}
				throw new NoSuchElementException();
			}

			@Override
			public void remove() {
				input.remove();
			}

			boolean advance() {
				while (input.hasNext()) {
					next = transformation.transform(input.next());
					if (next != null)
						return true;
				}
				return false;
			}
		};
	}

	public static <I, O> Iterator<O> mapConcat(final Iterator<I> input,
			final Transformation<? super I, Iterable<O>> transformation) {
		return new Iterator<O>() {
			Iterator<O> nextIter = Collections.<O> emptyList().iterator();

			O next;

			@Override
			public boolean hasNext() {
				for (;;) {
					if (next != null) {
						return true;
					}

					if (nextIter.hasNext()) {
						next = nextIter.next();
						return true;
					}

					if (input.hasNext()) {
						nextIter = transformation.transform(input.next())
								.iterator();
					} else {
						return false;
					}
				}
			}

			@Override
			public O next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}

				O result = next;

				next = null;

				return result;
			}

			@Override
			public void remove() {
				input.remove();
			}

		};
	}

	/**
	 * Prints key-value entries present in the first {@link Multimap} but not in
	 * the second {@link Multimap} using the given {@link Writer} and prefixing
	 * all messages with a given prefix.
	 * 
	 * @param <K>
	 *            the type of the keys of the {@link Multimap}
	 * @param <V>
	 *            the type of the values of the {@link Multimap}
	 * @param first
	 *            the entries that should be printed
	 * @param second
	 *            the entries that should be skipped
	 * @param writer
	 *            the writer using which the entries should be printed
	 * @param prefix
	 *            the string to be appended to every line
	 * @throws IOException
	 *             if any I/O error occurs
	 */
	public static <K, V> void dumpDiff(MutableMultimap<K, V> first,
			MutableMultimap<K, V> second, Writer writer, String prefix)
			throws IOException {
		for (K key : first.keySet()) {
			Collection<V> firstValues = first.get(key).toList();
			Collection<V> secondValues = second.get(key).toList();
			dumpDiff(firstValues, secondValues, writer, prefix + key + "->");
		}

	}

	/**
	 * Prints the elements present in the first {@link Collection} but not in
	 * the second {@link Collection} using the given {@link Writer} and
	 * prefixing all messages with a given prefix.
	 * 
	 * @param <T>
	 *            the type of elements
	 * @param first
	 *            the elements to be printed
	 * @param second
	 *            the elements to be skipped
	 * @param writer
	 *            the writer using which the elements should be printed
	 * @param prefix
	 *            the string to be appended to every line
	 * @throws IOException
	 *             if any I/O error occurs
	 */
	public static <T> void dumpDiff(Collection<T> first, Collection<T> second,
			Writer writer, String prefix) throws IOException {
		for (T element : first)
			if (!second.contains(element))
				writer.append(prefix + element + "\n");
	}

	/**
	 * A simple object that transforms objects of type I to objects of type O
	 * 
	 * @author Pavel Klinov
	 * 
	 *         pavel.klinov@uni-ulm.de
	 * 
	 * @param <I>
	 *            the type of input objects
	 * @param <O>
	 *            the type of the output objects
	 */
	public interface Functor<I, O> {

		public O apply(I element);
	}

	/**
	 * An extension of {@link Functor} which can do the reverse transformation
	 * 
	 * @author Pavel Klinov
	 *
	 *         pavel.klinov@uni-ulm.de
	 * @param <I>
	 *            the type of input objects
	 * @param <O>
	 *            the type of output objects
	 */
	public interface FunctorEx<I, O> extends Functor<I, O> {

		/**
		 * The reason this method takes Objects rather than instances of O is
		 * because it's primarily used for an efficient implementation of
		 * {@link Set#contains(Object)}, which takes an Object
		 * 
		 * @param element
		 *            the output element to be converted back to input
		 * 
		 * @return Can return null if the transformation is not possible
		 */
		public I deapply(Object element);
	}

	/**
	 * An {@link Iterator} whose elements are obtained from another
	 * {@link Iterator} by applying a {@link Functor}
	 * 
	 * @author Pavel Klinov
	 *
	 *         pavel.klinov@uni-ulm.de
	 * 
	 * @param <I>
	 *            the type of elements of the input {@link Iterator}
	 * @param <O>
	 *            the type of elements of this {@link Iterator}
	 */
	private static class MapIterator<I, O> implements Iterator<O> {

		private final Iterator<? extends I> iter_;
		private final Functor<I, O> functor_;

		MapIterator(Iterator<? extends I> iter, Functor<I, O> functor) {
			iter_ = iter;
			functor_ = functor;
		}

		@Override
		public boolean hasNext() {
			return iter_.hasNext();
		}

		@Override
		public O next() {
			return functor_.apply(iter_.next());
		}

		@Override
		public void remove() {
			iter_.remove();
		}

	}

	/**
	 * A simple second-order map function for {@link Set}s
	 * 
	 * @param <I>
	 *            the type of elements of the input {@link Set}
	 * @param <O>
	 *            the type of elements of this {@link Set}
	 * @param input
	 *            the input {@link Set} to be converted
	 * @param functor
	 *            the {@link Functor} that applies to the input elements
	 * @return a set which contains the results of applying the {@link Functor} to the
	 *         elements in the input {@link Set}
	 */
	public static <I, O> Set<O> map(final Set<? extends I> input,
			final FunctorEx<I, O> functor) {
		return new AbstractSet<O>() {

			@Override
			public Iterator<O> iterator() {
				return new MapIterator<I, O>(input.iterator(), functor);
			}

			@Override
			public boolean contains(Object o) {
				I element = functor.deapply(o);

				return element == null ? false : input.contains(element);
			}

			@Override
			public int size() {
				return input.size();
			}

		};
	}

	public static <T> String toString(Iterable<T> iterable) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;

		for (T elem : iterable) {
			String elemStr = elem.toString();

			if (!first && !elemStr.isEmpty()) {
				builder.append(' ');
			}

			if (!elemStr.isEmpty()) {
				builder.append(elemStr);
			}

			first = false;
		}

		return builder.toString();
	}

	/**
	 * @param <T>
	 *            the type of elements
	 * @param elementComparator
	 *            a {@link Comparator} on these elements
	 * @return {@link Comparator} that compares {@link Iterable} according to
	 *         lexical order w.r.t. element ordering defined by the provided
	 *         element {@link Comparator}.
	 */
	public static final <T> Comparator<? super Iterable<T>> lexicalOrder(
			final Comparator<? super T> elementComparator) {
		return new Comparator<Iterable<T>>() {
			@Override
			public int compare(final Iterable<T> o1, final Iterable<T> o2) {

				final Iterator<T> list1 = o1 == null
						? Collections.<T> emptyList().iterator()
						: o1.iterator();
				final Iterator<T> list2 = o2 == null
						? Collections.<T> emptyList().iterator()
						: o2.iterator();

				while (list1.hasNext() || list2.hasNext()) {

					// index did not exceed the size of at least one list.
					if (!list1.hasNext()) {
						// list1 is shorter and coincides on its elements.
						return -1;
					}
					if (!list2.hasNext()) {
						// list2 is shorter and coincides on its elements.
						return 1;
					}
					// index exceeded the size of none of the lists.

					final int cmp = elementComparator.compare(list1.next(),
							list2.next());
					if (cmp != 0) {
						return cmp;
					}

				}
				/*
				 * index exceeded size of both lists and no difference was
				 * found, so the lists are the same.
				 */
				return 0;
			}
		};
	}

}

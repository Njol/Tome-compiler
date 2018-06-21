/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utils for collections and arrays. All methods will not print any errors for <tt>null</tt> collections/arrays, but will return false/-1/etc.
 * 
 * @author Peter Güttinger
 */
public abstract class CollectionUtils {
	private CollectionUtils() {}
	
	/**
	 * Finds an object in an array using {@link Object#equals(Object)} (can find null elements).
	 * 
	 * @param array The array to search in
	 * @param o The object to search for
	 * @return The index of the first occurrence of the given object or -1 if not found
	 */
	public static <T> int indexOf(final @Nullable T @Nullable [] array, final @Nullable T t) {
		if (array == null)
			return -1;
		for (int i = 0; i < array.length; i++) {
			final @Nullable T elem = array[i];
			if (elem == null ? t == null : elem.equals(t))
				return i;
		}
		return -1;
	}
	
	public static <T> int lastIndexOf(final @Nullable T @Nullable [] array, final @Nullable T t) {
		if (array == null)
			return -1;
		for (int i = array.length - 1; i >= 0; i--) {
			final @Nullable T elem = array[i];
			if (elem == null ? t == null : elem.equals(t))
				return i;
		}
		return -1;
	}
	
	public static <T> int indexOf(final @Nullable T @Nullable [] array, final @Nullable T t, final int start, final int end) {
		if (array == null)
			return -1;
		for (int i = start; i < end; i++) {
			final @Nullable T elem = array[i];
			if (elem == null ? t == null : elem.equals(t))
				return i;
		}
		return -1;
	}
	
	public static <T> boolean contains(final @Nullable T @Nullable [] array, final @Nullable T o) {
		return indexOf(array, o) != -1;
	}
	
	@SafeVarargs
	public static <T> boolean containsAny(final @Nullable T @Nullable [] array, final @Nullable T @Nullable... os) {
		if (array == null || os == null)
			return false;
		for (final @Nullable T o : os) {
			if (indexOf(array, o) != -1)
				return true;
		}
		return false;
	}
	
	@SafeVarargs
	public static <T> boolean containsAll(final @Nullable T @Nullable [] array, final @Nullable T @Nullable... os) {
		if (array == null || os == null)
			return false;
		for (final @Nullable T o : os) {
			if (indexOf(array, o) == -1)
				return false;
		}
		return true;
	}
	
	public static int indexOf(final int @Nullable [] array, final int num) {
		if (array == null)
			return -1;
		return indexOf(array, num, 0, array.length);
	}
	
	public static int indexOf(final int @Nullable [] array, final int num, final int start) {
		if (array == null)
			return -1;
		return indexOf(array, num, start, array.length);
	}
	
	public static int indexOf(final int @Nullable [] array, final int num, final int start, final int end) {
		if (array == null)
			return -1;
		for (int i = start; i < end; i++) {
			if (array[i] == num)
				return i;
		}
		return -1;
	}
	
	public final static boolean contains(final int @Nullable [] array, final int num) {
		return indexOf(array, num) != -1;
	}
	
	/**
	 * finds a string in an array of strings (ignoring case).
	 * 
	 * @param array the array to search in
	 * @param s the string to search for
	 * @return the index of the first occurrence of the given string or -1 if not found
	 */
	public static int indexOfIgnoreCase(final @Nullable String @Nullable [] array, final @Nullable String s) {
		if (array == null)
			return -1;
		int i = 0;
		for (final String a : array) {
			if (a == null ? s == null : a.equalsIgnoreCase(s))
				return i;
			i++;
		}
		return -1;
	}
	
	public static boolean containsIgnoreCase(final @Nullable String[] array, final @Nullable String s) {
		return indexOfIgnoreCase(array, s) != -1;
	}
	
	/**
	 * Finds an object in an iterable using {@link Object#equals(Object)}.
	 * 
	 * @param iter The iterable to search in
	 * @param o The object to search for
	 * @return The index of the first occurrence of the given object or -1 if not found
	 */
	public static <T> int indexOf(final @Nullable Iterable<T> iter, final @Nullable T o) {
		if (iter == null)
			return -1;
		int i = 0;
		for (final T a : iter) {
			if (a == null ? o == null : a.equals(o))
				return i;
			i++;
		}
		return -1;
	}
	
	/**
	 * Finds a string in a collection of strings (ignoring case).
	 * 
	 * @param iter The iterable to search in
	 * @param s The string to search for
	 * @return The index of the first occurrence of the given string or -1 if not found
	 */
	public static int indexOfIgnoreCase(final @Nullable Iterable<@Nullable String> iter, final @Nullable String s) {
		if (iter == null)
			return -1;
		int i = 0;
		for (final String a : iter) {
			if (a == null ? s == null : a.equalsIgnoreCase(s))
				return i;
			i++;
		}
		return -1;
	}
	
	/**
	 * @param classes Array of classes
	 * @param c The class to look for
	 * @return Whether the class or any of its superclasses are contained in the array
	 */
	public final static boolean containsSuperclass(final @Nullable Class<?> @Nullable [] classes, final @Nullable Class<?> c) {
		if (classes == null || c == null)
			return false;
		for (final Class<?> cl : classes) {
			if (cl == null)
				continue;
			if (cl.isAssignableFrom(c))
				return true;
		}
		return false;
	}
	
	/**
	 * @param classes Array of classes
	 * @param cs The classes to look for
	 * @return Whether the classes or any of their superclasses are contained in the array
	 */
	public final static boolean containsAnySuperclass(final @Nullable Class<?> @Nullable [] classes, final @Nullable Class<?> @Nullable... cs) {
		if (classes == null || cs == null)
			return false;
		for (final Class<?> cl : classes) {
			if (cl == null)
				continue;
			for (final Class<?> c : cs) {
				if (cl.isAssignableFrom(c))
					return true;
			}
		}
		return false;
	}
	
	private final static Random random = new Random();
	
	@Nullable
	public static <T> T getRandom(final @Nullable T @Nullable [] os) {
		if (os == null || os.length == 0)
			return null;
		return os[random.nextInt(os.length)];
	}
	
	@Nullable
	public static <T> T getRandom(final @Nullable T @Nullable [] os, final int start) {
		if (os == null || os.length == 0)
			return null;
		return os[random.nextInt(os.length - start) + start];
	}
	
	@Nullable
	public static <T> T getRandom(final @Nullable List<T> os) {
		if (os == null || os.isEmpty())
			return null;
		return os.get(random.nextInt(os.size()));
	}
	
	/**
	 * @param set The set of elements
	 * @param sub The set to test for being a subset of <tt>set</tt>
	 * @return Whether <tt>sub</tt> only contains elements out of <tt>set</tt> or not
	 */
	public static boolean isSubset(final @Nullable Object @Nullable [] set, final @Nullable Object @Nullable [] sub) {
		if (set == null || sub == null)
			return false;
		for (final Object s : set) {
			if (!contains(sub, s))
				return false;
		}
		return true;
	}
	
	/**
	 * Gets the intersection of the given sets, i.e. a set that only contains elements that occur in all given sets.
	 * 
	 * @param sets
	 * @return
	 */
	@SafeVarargs
	public final static <E> Set<E> intersection(final @Nullable Set<E> @Nullable... sets) {
		if (sets == null || sets.length == 0)
			return Collections.emptySet();
		final Set<E> l = new HashSet<>(sets[0]);
		for (int i = 1; i < sets.length; i++) {
			if (sets[i] == null)
				continue;
			l.retainAll(sets[i]);
		}
		return l;
	}
	
	/**
	 * Gets the union of the given sets, i.e. a set that contains all elements of the given sets.
	 * 
	 * @param sets
	 * @return
	 */
	@SafeVarargs
	public final static <E> Set<E> union(final @Nullable Set<E> @Nullable... sets) {
		if (sets == null || sets.length == 0)
			return Collections.emptySet();
		final Set<E> l = new HashSet<>(sets[0]);
		for (int i = 1; i < sets.length; i++) {
			if (sets[i] == null)
				continue;
			l.addAll(sets[i]);
		}
		return l;
	}
	
	/**
	 * Creates an array from the given objects. Useful for creating arrays of generic types.
	 * <p>
	 * The method is annotated {@link NonNull}, but will simply return null if null is passed.
	 * 
	 * @param array Some objects
	 * @return The passed array
	 */
	@SafeVarargs
	public static <T> T[] array(final T... array) {
		return array;
	}
	
	/**
	 * Creates a permutation of all integers in the interval [start, end]
	 * 
	 * @param start The lowest number which will be included in the permutation
	 * @param end The highest number which will be included in the permutation
	 * @return an array of length end - start + 1, or an empty array if start > end.
	 */
	public final static int[] permutation(final int start, final int end) {
		if (start > end)
			return new int[0];
		final int length = end - start + 1;
		final int[] r = new int[length];
		for (int i = 0; i < length; i++)
			r[i] = start + i;
		for (int i = length - 1; i > 0; i--) {
			final int j = random.nextInt(i + 1);
			final int b = r[i];
			r[i] = r[j];
			r[j] = b;
		}
		return r;
	}
	
	/**
	 * Creates a permutation of all bytes in the interval [start, end]
	 * 
	 * @param start The lowest number which will be included in the permutation
	 * @param end The highest number which will be included in the permutation
	 * @return an array of length end - start + 1, or an empty array if start > end.
	 */
	public final static byte[] permutation(final byte start, final byte end) {
		if (start > end)
			return new byte[0];
		final int length = end - start + 1;
		final byte[] r = new byte[length];
		for (byte i = 0; i < length; i++)
			r[i] = (byte) (start + i);
		for (int i = length - 1; i > 0; i--) {
			final int j = random.nextInt(i + 1);
			final byte b = r[i];
			r[i] = r[j];
			r[j] = b;
		}
		return r;
	}
	
	/**
	 * Shorthand for <code>{@link permutation permutation}(0, length - 1)</code>, i.e. returns an array of length <tt>length</tt> that is a permutation of the numbers <tt>0</tt> to
	 * <tt>length - 1</tt> (inclusive).
	 */
	public final static int[] permutation(final int length) {
		return permutation(0, length - 1);
	}
	
	/**
	 * Converts a collection of integers into a primitive int array.
	 * 
	 * @param ints The collection
	 * @return An int[] containing the elements of the given collection in the order they were returned by the collection's iterator.
	 * @throws ArrayIndexOutOfBoundsException if the collection contains more elements than it claims it does (e.g. if elements were added concurrently)
	 */
	@SuppressWarnings("null")
	public final static int[] toArray(final @Nullable Collection<Integer> ints) {
		if (ints == null)
			return new int[0];
		final int[] r = new int[ints.size()];
		int i = 0;
		for (final Integer n : ints)
			r[i++] = n;
		if (i != r.length) {// shouldn't happen if the collection is valid
			assert false : ints;
			return Arrays.copyOfRange(r, 0, i);
		}
		return r;
	}
	
	public final static float[] toFloats(final double @Nullable [] doubles) {
		if (doubles == null)
			return new float[0];
		final float[] floats = new float[doubles.length];
		for (int i = 0; i < floats.length; i++)
			floats[i] = (float) doubles[i];
		return floats;
	}
	
	@SuppressWarnings("unchecked")
	public final static <T> T[] concat(final Class<T> componentType, final T[]... arrays) {
		return (T[]) concat(componentType, (Object[]) arrays);
	}
	
	public final static Object concat(final Class<?> componentType, final Object... arrays) {
		int length = 0;
		for (final Object a : arrays)
			length += Array.getLength(a);
		final @NonNull Object r = Array.newInstance(componentType, length);
		int i = 0;
		for (final Object a : arrays) {
			System.arraycopy(a, 0, r, i, Array.getLength(a));
			i += Array.getLength(a);
		}
		return r;
	}
	
	public static <T extends Comparable<T>> int compare(final List<T> list1, final List<T> list2) {
		final Iterator<T> iter1 = list1.iterator(), iter2 = list2.iterator();
		while (iter1.hasNext() && iter2.hasNext()) {
			final int c = iter1.next().compareTo(iter2.next());
			if (c != 0)
				return c;
		}
		return iter1.hasNext() ? 1 : iter2.hasNext() ? -1 : 0;
	}
	
	@SafeVarargs
	public static <T> Collection<T> combine(final Collection<T>... collections) {
		int size = 0;
		for (final Collection<T> c : collections)
			size += c.size();
		final ArrayList<T> result = new ArrayList<>(size);
		for (final Collection<T> c : collections)
			result.addAll(c);
		return result;
	}
	
}

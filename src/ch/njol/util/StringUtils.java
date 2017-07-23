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

import java.util.Iterator;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public abstract class StringUtils {
	
	/**
	 * Checks an index for a string operation and produces an useful error message (unlike the default...)
	 * 
	 * @param s The string to check for
	 * @param index An index, must be >= 0 and <= s.length()
	 * @throws StringIndexOutOfBoundsException if the index is not in range
	 */
	public final static void checkIndex(final String s, final int index) {
		if (index < 0 || index >= s.length())
			throw new StringIndexOutOfBoundsException("invalid index " + index + " for string \"" + s + "\" (length " + s.length() + ")");
	}
	
	/**
	 * Checks indices for a string operation and produces an useful error message (unlike the default...)
	 * <p>
	 * Ignores the case where end < start (i.e. does not produce an error in this case).
	 * 
	 * @param s The string to check for
	 * @param start The start index, must be >= 0
	 * @param end The end index, must be <= s.length()
	 * @throws StringIndexOutOfBoundsException if the indices are not as described above
	 */
	public final static void checkIndices(final String s, final int start, final int end) {
		if (start < 0 || end > s.length())
			throw new StringIndexOutOfBoundsException("invalid start/end indices " + start + "," + end + " for string \"" + s + "\" (length " + s.length() + ")");
	}
	
	/**
	 * Appends the english order suffix to the given number.
	 * <p>
	 * Does not work well for negative numbers.
	 * 
	 * @param i the number
	 * @return 1st, 2nd, 3rd, 4th, etc.
	 */
	public static String fancyOrderNumber(final int i) {
		final int imod10 = i % 10;
		if (imod10 == 1)
			return i + "st";
		if (imod10 == 2)
			return i + "nd";
		if (imod10 == 3)
			return i + "rd";
		return i + "th";
	}
	
	/**
	 * Performs some action for all occurrences of a Pattern in a String.
	 * 
	 * @param s The string to search in
	 * @param p The pattern to search for
	 * @param function THe function to call for each found occurrence
	 */
	public final static void forEach(final String s, final Pattern p, final Consumer<Matcher> function) {
		final Matcher m = p.matcher(s);
		int x = 0;
		while (m.find(x)) {
			x = m.end();
			function.accept(m);
		}
	}
	
	public static int count(final String s, final char c) {
		return count(s, c, 0, s.length());
	}
	
	public static int count(final String s, final char c, final int start) {
		return count(s, c, start, s.length());
	}
	
	public static int count(final String s, final char c, final int start, final int end) {
		checkIndices(s, start, end);
		int r = 0;
		for (int i = start; i < end; i++) {
			if (s.charAt(i) == c)
				r++;
		}
		return r;
	}
	
	public final static boolean contains(final String s, final char c, final int start, final int end) {
		checkIndices(s, start, end);
		for (int i = start; i < end; i++) {
			if (s.charAt(i) == c)
				return true;
		}
		return false;
	}
	
	/**
	 * Gets a rounded english (##.##) representation of a number
	 * 
	 * @param d The number to be turned into a string
	 * @param accuracy Maximum number of digits after the period
	 * @return
	 */
	public static final String toString(final double d, final int accuracy) {
		assert accuracy >= 0;
		final String s = String.format(Locale.ENGLISH, "%." + Math.max(0, accuracy) + "f", d);
		if (!s.contains("."))
			return s;
		int c = s.length() - 1;
		while (s.charAt(c) == '0')
			c--;
		if (s.charAt(c) == '.')
			c--;
		return "" + s.substring(0, c + 1);
	}
	
	public static final String firstToUpper(final String s) {
		if (s.isEmpty())
			return s;
		if (Character.isUpperCase(s.charAt(0)))
			return s;
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}
	
	/**
	 * Equal to {@link String#substring(int, int)}, but allows negative indices that are counted from the end of the string.
	 * 
	 * @param s
	 * @param start
	 * @param end
	 * @return
	 */
	public static final String substring(final String s, int start, int end) {
		if (start < 0)
			start = start + s.length();
		if (end < 0)
			end = end + s.length();
		if (end < start)
			throw new IllegalArgumentException("invalid indices");
		return "" + s.substring(start, end);
	}
	
	/**
	 * Capitalises the first character of the string and all characters that follow periods, exclamation and question marks.
	 * 
	 * @param string
	 * @return
	 */
	public static String fixCapitalization(final String string) {
		final char[] s = string.toCharArray();
		int c = 0;
		while (c != -1) {
			while (c < s.length && (s[c] == '.' || s[c] == '!' || s[c] == '?' || Character.isWhitespace(s[c])))
				c++;
			if (c == s.length)
				return new String(s);
			if (c == 0 || Character.isWhitespace(s[c - 1]))
				s[c] = Character.toUpperCase(s[c]);
			c = indexOf(s, c + 1, '.', '!', '?');
		}
		return new String(s);
	}
	
	private final static int indexOf(final char[] s, final int start, final char... cs) {
		for (int i = start; i < s.length; i++) {
			for (final char c : cs)
				if (s[i] == c)
					return i;
		}
		return -1;
	}
	
	/**
	 * Shorthand for <tt>{@link #numberAt(CharSequence, int, boolean) numberAt}(s, index, true)</tt>
	 * 
	 * @param s
	 * @param index
	 * @return
	 */
	public final static double numberAfter(final CharSequence s, final int index) {
		return numberAt(s, index, true);
	}
	
	/**
	 * Shorthand for <tt>{@link #numberAt(CharSequence, int, boolean) numberAt}(s, index, false)</tt>
	 * 
	 * @param s
	 * @param index
	 * @return
	 */
	public final static double numberBefore(final CharSequence s, final int index) {
		return numberAt(s, index, false);
	}
	
	/**
	 * Finds a positive number in the given CharSequence, starting at the given index, and searching in the given direction.
	 * <p>
	 * The number has to start exactly at the given index (ignoring whitespace), and will only count if the other end of the number is either at an end of the string or padded by
	 * whitespace.
	 * 
	 * @param s The ChatSequence to search the number in
	 * @param index The index to start searching at (inclusive)
	 * @param forward Whether to search forwards or backwards
	 * @return The number found or -1 if no matching number was found
	 */
	public final static double numberAt(final CharSequence s, final int index, final boolean forward) {
		assert s != null;
		assert index >= 0 && index < s.length() : index;
		final int direction = forward ? 1 : -1;
		boolean stillWhitespace = true;
		boolean hasDot = false;
		int d1 = -1, d2 = -1;
		for (int i = index; i >= 0 && i < s.length(); i += direction) {
			final char c = s.charAt(i);
			if ('0' <= c && c <= '9') {
				if (d1 == -1)
					d1 = d2 = i;
				else
					d1 += direction;
				stillWhitespace = false;
			} else if (c == '.') {
				if (hasDot)
					break;
				if (d1 == -1)
					d1 = d2 = i;
				else
					d1 += direction;
				hasDot = true;
				stillWhitespace = false;
			} else if (Character.isWhitespace(c)) {
				if (stillWhitespace)
					continue;
				break;
			} else {
				break;
			}
		}
		if (d1 == -1)
			return -1;
		if (s.charAt(Math.min(d1, d2)) == '.')
			return -1;
		if (d1 + direction > 0 && d1 + direction < s.length() && !Character.isWhitespace(s.charAt(d1 + direction)))
			return -1;
		return Double.parseDouble(s.subSequence(Math.min(d1, d2), Math.max(d1, d2) + 1).toString());
	}
	
	public static boolean startsWithIgnoreCase(final String string, final String start) {
		return startsWithIgnoreCase(string, start, 0);
	}
	
	public static boolean startsWithIgnoreCase(final String string, final String start, final int offset) {
		assert string != null;
		assert start != null;
		if (string.length() < offset + start.length())
			return false;
		return string.substring(offset, start.length()).equalsIgnoreCase(start);
	}
	
	public static boolean endsWithIgnoreCase(final String string, final String end) {
		assert string != null;
		assert end != null;
		if (string.length() < end.length())
			return false;
		return string.substring(string.length() - end.length()).equalsIgnoreCase(end);
	}
	
	public final static String multiply(final @Nullable String s, final int amount) {
		assert amount >= 0 : amount;
		if (s == null)
			return "";
		if (amount <= 0)
			return "";
		if (amount == 1)
			return s;
		final char[] input = s.toCharArray();
		final char[] multiplied = new char[input.length * amount];
		for (int i = 0; i < amount; i++)
			System.arraycopy(input, 0, multiplied, i * input.length, input.length);
		return new String(multiplied);
	}
	
	public final static String multiply(final char c, final int amount) {
		if (amount == 0)
			return "";
		final char[] multiplied = new char[amount];
		for (int i = 0; i < amount; i++)
			multiplied[i] = c;
		return new String(multiplied);
	}
	
	public static String join(final @Nullable Object @Nullable [] strings) {
		if (strings == null)
			return "";
		return join(strings, "", 0, strings.length);
	}
	
	public static String join(final @Nullable Object @Nullable [] strings, final String delimiter) {
		if (strings == null)
			return "";
		return join(strings, delimiter, 0, strings.length);
	}
	
	public static String join(final @Nullable Object @Nullable [] strings, final String delimiter, final int start, final int end) {
		if (strings == null)
			return "";
		assert start >= 0 && start <= end && end <= strings.length : start + ", " + end + ", " + strings.length;
		if (start < 0 || start >= strings.length || start == end)
			return "";
		final StringBuilder b = new StringBuilder("" + strings[start]);
		for (int i = start + 1; i < end; i++) {
			b.append(delimiter);
			b.append(strings[i]);
		}
		return "" + b;
	}
	
	public static String join(final @Nullable Iterable<?> strings) {
		if (strings == null)
			return "";
		return join(strings.iterator(), "");
	}
	
	public static String join(final @Nullable Iterable<?> strings, final String delimiter) {
		if (strings == null)
			return "";
		return join(strings.iterator(), delimiter);
	}
	
	public static String join(final @Nullable Iterator<?> strings, final String delimiter) {
		if (strings == null || !strings.hasNext())
			return "";
		final StringBuilder b = new StringBuilder("" + strings.next());
		while (strings.hasNext()) {
			b.append(delimiter);
			b.append(strings.next());
		}
		return "" + b;
	}
	
	/**
	 * Scans the string starting at <tt>start</tt> for digits.
	 * 
	 * @param s
	 * @param start Index of the first digit
	 * @return The index <i>after</i> the last digit or <tt>start</tt> if there are no digits at the given index
	 */
	public final static int findLastDigit(final String s, final int start) {
		int end = start;
		char c;
		while (end < s.length() && '0' <= (c = s.charAt(end)) && c <= '9')
			end++;
		return end;
	}
	
	/**
	 * Searches for whether a String contains any of the characters of another string.
	 * 
	 * @param s
	 * @param chars
	 * @return
	 */
	public static boolean containsAny(final String s, final String chars) {
		for (int i = 0; i < chars.length(); i++) {
			if (s.indexOf(chars.charAt(i)) != -1)
				return true;
		}
		return false;
	}
	
	public final static boolean equals(final String s1, final String s2, final boolean caseSensitive) {
		return caseSensitive ? s1.equals(s2) : s1.equalsIgnoreCase(s2);
	}
	
	public final static boolean contains(final String haystack, final String needle, final boolean caseSensitive) {
		if (caseSensitive)
			return haystack.contains(needle);
		return haystack.toLowerCase().contains(needle.toLowerCase());
	}
	
	public final static String replace(final String haystack, final String needle, final String replacement, final boolean caseSensitive) {
		if (caseSensitive)
			return "" + haystack.replace(needle, replacement);
		return "" + haystack.replaceAll("(?ui)" + Pattern.quote(needle), replacement);
	}
	
}
